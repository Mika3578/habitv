package com.dabi.habitv.framework.plugin.utils.update;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.utils.AlphanumComparator;
import com.dabi.habitv.framework.plugin.utils.RetrieverUtils;

public class FindArtifactUtils {
	private static List<String> EXCLUDE = Arrays.asList("Parent Directory", "Name", "Last modified", "Size", "Description");

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
		final String groupIdUrl = groupId.replace(".", "/");

		// Ensure we don't add double slashes
		String baseUrl = FrameworkConf.UPDATE_URL;
		if (baseUrl.endsWith("/")) {
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}
		final String artifactURL = baseUrl + "/" + groupIdUrl + "/" + artifactId;

		final ArtifactVersion lastVersion = findLastVersionUrl(artifactURL, versionMaj, autoriseSnapshot, extension);
		if (lastVersion == null) {
			return null;
		}
		lastVersion.setArtifactId(artifactId);
		return lastVersion;
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
		// Ensure we don't add a double slash
		String url = artifactURL;
		if (!url.endsWith("/")) {
			url += "/";
		}
		List<String> items = findItems(Type.DIR, url);
		final String version = findLastVersion(versionMaj, items, autoriseSnapshot);
		if (version == null) {
			return null;
		}
		// Ensure we don't add double slashes
		String baseVersionUrl = artifactURL;
		if (baseVersionUrl.endsWith("/")) {
			baseVersionUrl = baseVersionUrl.substring(0, baseVersionUrl.length() - 1);
		}
		final String artifactVersionUrl = baseVersionUrl + "/" + version;

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
			// Ensure we don't add double slashes
			String baseFileUrl = artifactVersionUrl;
			if (baseFileUrl.endsWith("/")) {
				baseFileUrl = baseFileUrl.substring(0, baseFileUrl.length() - 1);
			}
			return new ArtifactVersion(baseFileUrl + "/" + files.get(files.size() - 1), version);
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
		} catch (com.dabi.habitv.api.plugin.exception.TechnicalException e) {
			// Repository is not accessible, return empty list
			return new LinkedList<>();
		}
	}

	private static String findLastVersion(final String versionRef, final List<String> items, final boolean autoriseSnapshot) {
		Collections.sort(items, AlphanumComparator.INSTANCE);
		for (int i = items.size() - 1; i >= 0; i--) {
			final String version = items.get(i);
			if (versionRef == null
					|| (version.startsWith(versionRef) && ((autoriseSnapshot && version.contains("SNAPSHOT") || !version
							.contains("SNAPSHOT"))))) {
				return version;
			}
		}
		return null;
	}

	private static boolean isDirectory(final String attr) {
		return attr.endsWith("/");
	}
}
