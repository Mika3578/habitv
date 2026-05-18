package com.dabi.habitv.framework.plugin.utils.update;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.utils.AlphanumComparator;
import com.dabi.habitv.framework.plugin.utils.RetrieverUtils;
import com.dabi.habitv.framework.plugin.utils.update.HabitvUpdateManifest.ArtifactKind;
import com.dabi.habitv.framework.plugin.utils.update.HabitvUpdateManifest.Entry;

public class FindArtifactUtils {

	private static final Logger LOG = Logger.getLogger(FindArtifactUtils.class);

	private static List<String> EXCLUDE = Arrays.asList("Parent Directory", "Name", "Last modified", "Size", "Description");

	private static volatile HabitvUpdateManifest cachedManifest;

	private static volatile boolean manifestLookupAttempted;

	public static class ArtifactVersion {
		private String artifactId;
		private final String url;
		private final String version;

		private ArtifactVersion(final String url, final String version) {
			this.url = url;
			this.version = version;
		}

		public String getUrl() {
			return url;
		}

		public String getVersion() {
			return version;
		}

		public String getArtifactId() {
			return artifactId;
		}

		public void setArtifactId(final String artifactId) {
			this.artifactId = artifactId;
		}

		@Override
		public String toString() {
			return "ArtifactVersion [url=" + url + ", version=" + version + "]";
		}

	}

	public static ArtifactVersion findLastVersionUrl(final String groupId, final String artifactId, final String coreVersion,
			final boolean autoriseSnapshot, String extension) {
		final String versionMaj = getVersionMaj(coreVersion);
		final ArtifactKind kind = "zip".equalsIgnoreCase(extension) ? ArtifactKind.TOOL : ArtifactKind.PLUGIN;

		final ArtifactVersion fromManifest = findLastVersionFromManifest(groupId, artifactId, versionMaj, autoriseSnapshot,
				extension, kind);
		if (fromManifest != null) {
			fromManifest.setArtifactId(artifactId);
			return fromManifest;
		}

		final String groupIdUrl = groupId.replace(".", "/");
		if (kind == ArtifactKind.TOOL) {
			final String toolsArtifactUrl = UpdateRepositoryUrls.buildRepositoryUrl(FrameworkConf.TOOLS_REPOSITORY_PREFIX
					+ "/" + artifactId);
			final ArtifactVersion toolVersion = findLastVersionUrl(toolsArtifactUrl, versionMaj, autoriseSnapshot, extension);
			if (toolVersion != null) {
				toolVersion.setArtifactId(artifactId);
				return toolVersion;
			}
		}

		final String artifactURL = UpdateRepositoryUrls.buildRepositoryUrl(groupIdUrl + "/" + artifactId);
		final ArtifactVersion lastVersion = findLastVersionUrl(artifactURL, versionMaj, autoriseSnapshot, extension);
		if (lastVersion == null) {
			return null;
		}
		lastVersion.setArtifactId(artifactId);
		return lastVersion;
	}

	static void clearManifestCache() {
		cachedManifest = null;
		manifestLookupAttempted = false;
	}

	private static HabitvUpdateManifest getManifest() {
		if (!manifestLookupAttempted) {
			synchronized (FindArtifactUtils.class) {
				if (!manifestLookupAttempted) {
					cachedManifest = HabitvUpdateManifest.loadFromRepository();
					manifestLookupAttempted = true;
				}
			}
		}
		return cachedManifest != null ? cachedManifest : HabitvUpdateManifest.empty();
	}

	private static ArtifactVersion findLastVersionFromManifest(final String groupId, final String artifactId,
			final String versionMaj, final boolean autoriseSnapshot, final String extension, final ArtifactKind kind) {
		final HabitvUpdateManifest manifest = getManifest();
		if (manifest.isEmpty()) {
			return null;
		}
		final List<Entry> matches = manifest.findEntries(groupId, artifactId, kind);
		if (matches.isEmpty()) {
			return null;
		}
		final List<String> versions = new LinkedList<>();
		final java.util.Map<String, Entry> versionToEntry = new java.util.HashMap<>();
		for (final Entry entry : matches) {
			if (!matchesExtension(entry, extension)) {
				continue;
			}
			if (!isVersionEligible(entry.getVersion(), versionMaj, autoriseSnapshot)) {
				continue;
			}
			versions.add(entry.getVersion());
			versionToEntry.put(entry.getVersion(), entry);
		}
		final String version = findLastVersion(versionMaj, versions, autoriseSnapshot);
		if (version == null) {
			return null;
		}
		final Entry entry = versionToEntry.get(version);
		return new ArtifactVersion(entry.getDownloadUrl(), version);
	}

	private static boolean matchesExtension(final Entry entry, final String extension) {
		if (extension == null || extension.isEmpty()) {
			return true;
		}
		if (entry.getPackaging() != null && entry.getPackaging().equalsIgnoreCase(extension)) {
			return true;
		}
		return entry.getRelativeUrl().toLowerCase().endsWith("." + extension.toLowerCase());
	}

	private static boolean isVersionEligible(final String version, final String versionMaj, final boolean autoriseSnapshot) {
		if (versionMaj != null && !version.startsWith(versionMaj)) {
			return false;
		}
		return autoriseSnapshot || !version.contains("SNAPSHOT");
	}

	private static String getVersionMaj(final String coreVersion) {
		final String versionMaj;
		if (coreVersion == null) {
			versionMaj = null;
		} else {
			final String[] coreVersionSplit = coreVersion.split("\\.");
			versionMaj = coreVersionSplit[0] + "." + coreVersionSplit[1];
		}
		return versionMaj;
	}

	private static ArtifactVersion findLastVersionUrl(final String artifactURL, final String versionMaj, final boolean autoriseSnapshot,
			final String extension) {
		List<String> items = findItems(Type.DIR, artifactURL + "/");
		final String version = findLastVersion(versionMaj, items, autoriseSnapshot);
		if (version == null) {
			return null;
		}
		final String artifactVersionUrl = artifactURL + "/" + version;
		items = findItems(Type.FILE, artifactVersionUrl);
		final List<String> files = new LinkedList<>();
		for (final String file : items) {
			if (file.endsWith("." + extension)) {
				files.add(file);
			}
		}
		if (files.isEmpty()) {
			return null;
		} else {
			Collections.sort(files);
			return new ArtifactVersion(artifactVersionUrl + "/" + files.get(files.size() - 1), version);
		}
	}

	private enum Type {
		FILE, DIR, ALL,
	}

	private static List<String> findItems(final Type type, final String url) {
		try {
			final org.jsoup.nodes.Document doc = Jsoup.parse(RetrieverUtils.getUrlContent(url, null));

			final Elements select = doc.select("a");

			final List<String> items = new LinkedList<>();
			if (!select.isEmpty()) {

				for (final Element aElement : select) {
					final String hRef = aElement.attr("href");
					final String text = aElement.text();
					final boolean isDirectory = isDirectory(hRef);
					if (!EXCLUDE.contains(text)
							&& (type == Type.ALL || (type == Type.DIR && isDirectory) || (type == Type.FILE && !isDirectory))) {
						items.add(hRef.replace("/", ""));
					}
				}
			}
			return items;
		} catch (final RuntimeException e) {
			LOG.debug("Directory listing unavailable at " + url + ": " + e.getMessage());
			return Collections.emptyList();
		}
	}

	private static String findLastVersion(final String versionRef, final List<String> items, final boolean autoriseSnapshot) {
		Collections.sort(items, AlphanumComparator.INSTANCE);
		for (int i = items.size() - 1; i >= 0; i--) {
			final String version = items.get(i);
			if (isVersionEligible(version, versionRef, autoriseSnapshot)) {
				return version;
			}
		}
		return null;
	}

	private static boolean isDirectory(final String attr) {
		return attr.endsWith("/");
	}
}
