package com.dabi.habitv.framework.plugin.utils.update;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.utils.AlphanumComparator;
import com.dabi.habitv.framework.plugin.utils.RetrieverUtils;
import com.dabi.habitv.framework.plugin.utils.update.HabitvUpdateManifest.ArtifactKind;
import com.dabi.habitv.framework.plugin.utils.update.HabitvUpdateManifest.Entry;

import java.io.StringReader;

public class FindArtifactUtils {

	private static final Logger LOG = Logger.getLogger(FindArtifactUtils.class);

	private static List<String> EXCLUDE = Arrays.asList("Parent Directory", "Name", "Last modified", "Size", "Description");

	private static final String SNAPSHOT_DISABLED_MESSAGE =
			"Skipping SNAPSHOT plugin artifact because snapshot updates are disabled. Enable -Dhabitv.update.autoriseSnapshot=true for development bootstrap.";

	private static volatile HabitvUpdateManifest cachedManifest;

	private static volatile boolean manifestLookupAttempted;

	public static class ArtifactVersion {
		public enum ResolutionSource {
			MANIFEST, MAVEN_METADATA, DIRECTORY_LISTING
		}

		private String artifactId;
		private final String url;
		private final String version;
		private final ResolutionSource source;

		private ArtifactVersion(final String url, final String version, final ResolutionSource source) {
			this.url = url;
			this.version = version;
			this.source = source;
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

		public ResolutionSource getSource() {
			return source;
		}

		@Override
		public String toString() {
			return "ArtifactVersion [url=" + url + ", version=" + version + ", source=" + source + "]";
		}

	}

	public static ArtifactVersion findLastVersionUrl(final String groupId, final String artifactId, final String coreVersion,
			final boolean autoriseSnapshot, String extension) {
		extension = normalizeExtension(extension);
		LOG.info("Resolving artifact update for " + artifactId + " (extension=" + extension + ")");
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
			final ArtifactVersion toolVersion = findLastVersionUrl(toolsArtifactUrl, artifactId, versionMaj,
					autoriseSnapshot, extension, kind);
			if (toolVersion != null) {
				toolVersion.setArtifactId(artifactId);
				return toolVersion;
			}
		}

		final String artifactURL = UpdateRepositoryUrls.buildRepositoryUrl(groupIdUrl + "/" + artifactId);
		final ArtifactVersion lastVersion = findLastVersionUrl(artifactURL, artifactId, versionMaj, autoriseSnapshot,
				extension, kind);
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

	static String extractSnapshotValueFromMetadata(final String metadataContent, final String extension) {
		if (metadataContent == null || metadataContent.trim().isEmpty()) {
			return null;
		}
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false);
			factory.setExpandEntityReferences(false);
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setXIncludeAware(false);
			final Document document = factory.newDocumentBuilder().parse(
					new InputSource(new StringReader(metadataContent)));
			final NodeList snapshotVersionNodes = document.getElementsByTagName("snapshotVersion");
			final List<String> values = new LinkedList<>();
			for (int i = 0; i < snapshotVersionNodes.getLength(); i++) {
				final Node snapshotVersionNode = snapshotVersionNodes.item(i);
				final String extensionValue = findChildNodeText(snapshotVersionNode, "extension");
				if (!extension.equalsIgnoreCase(extensionValue)) {
					continue;
				}
				final String classifier = findChildNodeText(snapshotVersionNode, "classifier");
				if (classifier != null && !classifier.trim().isEmpty()) {
					continue;
				}
				final String value = findChildNodeText(snapshotVersionNode, "value");
				if (value != null && !value.trim().isEmpty()) {
					values.add(value.trim());
				}
			}
			if (values.isEmpty()) {
				return null;
			}
			Collections.sort(values, AlphanumComparator.INSTANCE);
			return values.get(values.size() - 1);
		} catch (final Exception e) {
			LOG.warn("Failed to parse maven-metadata.xml for snapshot artifact resolution", e);
			return null;
		}
	}

	private static String findChildNodeText(final Node parent, final String childName) {
		final NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			final Node child = children.item(i);
			if (childName.equals(child.getNodeName())) {
				return child.getTextContent();
			}
		}
		return null;
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
		final Map<String, List<Entry>> versionToEntries = new HashMap<>();
		boolean snapshotEntryFound = false;
		for (final Entry entry : matches) {
			if (!matchesExtension(entry, extension)) {
				continue;
			}
			if (entry.getVersion() != null && entry.getVersion().contains("SNAPSHOT")
					&& (versionMaj == null || entry.getVersion().startsWith(versionMaj))) {
				snapshotEntryFound = true;
			}
			if (!isVersionEligible(entry.getVersion(), versionMaj, autoriseSnapshot)) {
				continue;
			}
			versions.add(entry.getVersion());
			List<Entry> entriesForVersion = versionToEntries.get(entry.getVersion());
			if (entriesForVersion == null) {
				entriesForVersion = new LinkedList<>();
				versionToEntries.put(entry.getVersion(), entriesForVersion);
			}
			entriesForVersion.add(entry);
		}
		final String version = findLastVersion(versionMaj, versions, autoriseSnapshot);
		if (version == null) {
			if (!autoriseSnapshot && snapshotEntryFound && kind == ArtifactKind.PLUGIN) {
				LOG.warn(SNAPSHOT_DISABLED_MESSAGE);
			}
			return null;
		}
		final List<Entry> entries = versionToEntries.get(version);
		if (entries == null || entries.isEmpty()) {
			return null;
		}
		Collections.sort(entries, (left, right) -> AlphanumComparator.INSTANCE
				.compare(left.getRelativeUrl(), right.getRelativeUrl()));
		final Entry entry = entries.get(entries.size() - 1);
		return new ArtifactVersion(entry.getDownloadUrl(manifest.getBaseUrl()), version, ArtifactVersion.ResolutionSource.MANIFEST);
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

	private static String normalizeExtension(final String extension) {
		if (extension == null) {
			return null;
		}
		final String trimmed = extension.trim();
		if (trimmed.startsWith(".")) {
			return trimmed.substring(1);
		}
		return trimmed;
	}

	private static ArtifactVersion findLastVersionUrl(final String artifactURL, final String artifactId, final String versionMaj,
			final boolean autoriseSnapshot, final String extension, final ArtifactKind kind) {
		List<String> items = findItems(Type.DIR, artifactURL + "/");
		final String version = findLastVersion(versionMaj, items, autoriseSnapshot);
		if (version == null) {
			if (!autoriseSnapshot && hasSnapshotCandidate(items, versionMaj) && kind == ArtifactKind.PLUGIN) {
				LOG.warn(SNAPSHOT_DISABLED_MESSAGE);
			}
			return null;
		}
		final String artifactVersionUrl = artifactURL + "/" + version;
		if (version.contains("SNAPSHOT")) {
			return findSnapshotVersionFromMetadata(artifactVersionUrl, artifactId, version, extension);
		}
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
			return new ArtifactVersion(artifactVersionUrl + "/" + files.get(files.size() - 1), version,
					ArtifactVersion.ResolutionSource.DIRECTORY_LISTING);
		}
	}

	private static ArtifactVersion findSnapshotVersionFromMetadata(final String artifactVersionUrl, final String artifactId,
			final String logicalVersion, final String extension) {
		final String metadataUrl = artifactVersionUrl + "/maven-metadata.xml";
		try {
			final String metadataContent = RetrieverUtils.getUrlContent(metadataUrl, null);
			final String snapshotValue = extractSnapshotValueFromMetadata(metadataContent, extension);
			if (snapshotValue == null) {
				LOG.warn("No <snapshotVersion> " + extension + " entry found in " + metadataUrl);
				return null;
			}
			final String finalFileName = artifactId + "-" + snapshotValue + "." + extension;
			return new ArtifactVersion(artifactVersionUrl + "/" + finalFileName, logicalVersion,
					ArtifactVersion.ResolutionSource.MAVEN_METADATA);
		} catch (final RuntimeException e) {
			LOG.warn("maven-metadata.xml unavailable at " + metadataUrl + ": " + e.getMessage());
			return null;
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

	private static boolean hasSnapshotCandidate(final List<String> versions, final String versionRef) {
		for (final String version : versions) {
			if (versionRef != null && !version.startsWith(versionRef)) {
				continue;
			}
			if (version.contains("SNAPSHOT")) {
				return true;
			}
		}
		return false;
	}

	private static boolean isDirectory(final String attr) {
		return attr.endsWith("/");
	}
}
