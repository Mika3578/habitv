package com.dabi.habitv.framework.plugin.utils.update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.utils.RetrieverUtils;

/**
 * Parses {@value FrameworkConf#UPDATE_MANIFEST_FILE} from the static repository.
 * <p>
 * Each non-comment line uses pipe-separated fields:
 * {@code type|groupId|artifactId|version|packaging|relativeUrl[|checksum]}
 */
public final class HabitvUpdateManifest {

	private static final Logger LOG = Logger.getLogger(HabitvUpdateManifest.class);

	public enum ArtifactKind {
		PLUGIN, TOOL, UNKNOWN
	}

	public static final class Entry {
		private final ArtifactKind kind;
		private final String groupId;
		private final String artifactId;
		private final String version;
		private final String packaging;
		private final String relativeUrl;
		private final String checksum;

		public Entry(final ArtifactKind kind, final String groupId, final String artifactId, final String version,
				final String packaging, final String relativeUrl, final String checksum) {
			this.kind = kind;
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.version = version;
			this.packaging = packaging;
			this.relativeUrl = relativeUrl;
			this.checksum = checksum;
		}

		public ArtifactKind getKind() {
			return kind;
		}

		public String getGroupId() {
			return groupId;
		}

		public String getArtifactId() {
			return artifactId;
		}

		public String getVersion() {
			return version;
		}

		public String getPackaging() {
			return packaging;
		}

		public String getRelativeUrl() {
			return relativeUrl;
		}

		public String getChecksum() {
			return checksum;
		}

		public String getDownloadUrl() {
			if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")) {
				return relativeUrl;
			}
			return UpdateRepositoryUrls.buildRepositoryUrl(relativeUrl);
		}
	}

	private final List<Entry> entries;

	private HabitvUpdateManifest(final List<Entry> entries) {
		this.entries = Collections.unmodifiableList(entries);
	}

	public List<Entry> getEntries() {
		return entries;
	}

	public List<Entry> findEntries(final String groupId, final String artifactId, final ArtifactKind kind) {
		final List<Entry> matches = new ArrayList<>();
		for (final Entry entry : entries) {
			if (!entry.groupId.equals(groupId) || !entry.artifactId.equals(artifactId)) {
				continue;
			}
			if (kind != null && entry.kind != kind && entry.kind != ArtifactKind.UNKNOWN) {
				continue;
			}
			matches.add(entry);
		}
		return matches;
	}

	public String[] getPluginArtifactIds() {
		final Set<String> ids = new LinkedHashSet<>();
		for (final Entry entry : entries) {
			if (entry.kind == ArtifactKind.PLUGIN) {
				ids.add(entry.artifactId);
			}
		}
		return ids.toArray(new String[ids.size()]);
	}

	public static HabitvUpdateManifest loadFromRepository() {
		final String manifestUrl = UpdateRepositoryUrls.buildRepositoryUrl(FrameworkConf.UPDATE_MANIFEST_FILE);
		try {
			final String content = RetrieverUtils.getUrlContent(manifestUrl, null);
			return parse(content);
		} catch (final RuntimeException e) {
			LOG.debug("Update manifest not available at " + manifestUrl + ": " + e.getMessage());
			return empty();
		}
	}

	public static HabitvUpdateManifest parse(final String content) {
		if (content == null || content.trim().isEmpty()) {
			return empty();
		}
		final List<Entry> parsed = new ArrayList<>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(content));
			String line;
			while ((line = reader.readLine()) != null) {
				final Entry entry = parseLine(line);
				if (entry != null) {
					parsed.add(entry);
				}
			}
		} catch (final IOException e) {
			throw new IllegalStateException("Failed to parse update manifest", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (final IOException e) {
					LOG.debug("Failed to close manifest reader", e);
				}
			}
		}
		return new HabitvUpdateManifest(parsed);
	}

	static Entry parseLine(final String line) {
		final String trimmed = normalizeLine(line);
		if (trimmed == null) {
			return null;
		}
		if (trimmed.isEmpty() || trimmed.startsWith("#")) {
			return null;
		}
		final String[] parts = trimmed.split("\\|", -1);
		if (parts.length < 6) {
			LOG.warn("Ignoring invalid manifest line (expected at least 6 fields): " + trimmed);
			return null;
		}
		final ArtifactKind kind = parseKind(parts[0]);
		final String checksum = parts.length > 6 ? parts[6] : null;
		return new Entry(kind, parts[1], parts[2], parts[3], parts[4], parts[5], checksum);
	}

	private static String normalizeLine(final String line) {
		if (line == null) {
			return null;
		}
		String trimmed = line.trim();
		if (trimmed.startsWith("\uFEFF")) {
			trimmed = trimmed.substring(1).trim();
		}
		return trimmed;
	}

	private static ArtifactKind parseKind(final String raw) {
		if ("plugin".equalsIgnoreCase(raw)) {
			return ArtifactKind.PLUGIN;
		}
		if ("tool".equalsIgnoreCase(raw)) {
			return ArtifactKind.TOOL;
		}
		return ArtifactKind.UNKNOWN;
	}

	public static HabitvUpdateManifest empty() {
		return new HabitvUpdateManifest(Collections.<Entry>emptyList());
	}

	public boolean isEmpty() {
		return entries.isEmpty();
	}
}
