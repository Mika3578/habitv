package com.dabi.habitv.provider.tf1plus;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dabi.habitv.framework.FrameworkConf;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Local JSON cache for TF1+ catalogue entries.
 */
final class Tf1PlusCatalogueCache {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
	};

	private final Path cacheFile;

	Tf1PlusCatalogueCache() {
		this(resolveDefaultCachePath());
	}

	Tf1PlusCatalogueCache(final Path cacheFile) {
		this.cacheFile = cacheFile;
	}

	Path getCacheFile() {
		return cacheFile;
	}

	boolean exists() {
		return Files.isRegularFile(cacheFile);
	}

	Snapshot load() {
		if (!exists()) {
			return Snapshot.missing();
		}
		try {
			final byte[] bytes = Files.readAllBytes(cacheFile);
			final Map<String, Object> root = MAPPER.readValue(bytes, MAP_TYPE);
			final int schemaVersion = intValue(root.get("schemaVersion"));
			if (schemaVersion != Tf1PlusConf.CATALOGUE_CACHE_SCHEMA_VERSION) {
				return Snapshot.invalid("schema-mismatch");
			}
			final long cachedAtMillis = longValue(root.get("cachedAtMillis"));
			final List<Tf1PlusCatalogueEntry> entries = parseEntries(root.get("entries"));
			return Snapshot.loaded(cachedAtMillis, entries);
		} catch (IOException e) {
			return Snapshot.invalid("cache-read-failed");
		}
	}

	void save(final List<Tf1PlusCatalogueEntry> entries) throws IOException {
		final Map<String, Object> root = new LinkedHashMap<String, Object>();
		root.put("schemaVersion", Integer.valueOf(Tf1PlusConf.CATALOGUE_CACHE_SCHEMA_VERSION));
		root.put("cachedAtMillis", Long.valueOf(System.currentTimeMillis()));
		root.put("entries", serializeEntries(entries));
		final File parent = cacheFile.getParent().toFile();
		if (!parent.exists() && !parent.mkdirs()) {
			throw new IOException("unable to create catalogue cache directory: " + parent.getAbsolutePath());
		}
		Files.write(cacheFile, MAPPER.writeValueAsString(root).getBytes(StandardCharsets.UTF_8));
	}

	long resolveTtlMillis() {
		final String rawHours = System.getProperty(Tf1PlusConf.PROPERTY_CATALOGUE_CACHE_TTL_HOURS);
		if (rawHours == null || rawHours.trim().isEmpty()) {
			return Tf1PlusConf.CATALOGUE_DEFAULT_CACHE_TTL_MS;
		}
		try {
			final long hours = Long.parseLong(rawHours.trim());
			if (hours <= 0L) {
				return Tf1PlusConf.CATALOGUE_DEFAULT_CACHE_TTL_MS;
			}
			return hours * 60L * 60L * 1000L;
		} catch (NumberFormatException e) {
			return Tf1PlusConf.CATALOGUE_DEFAULT_CACHE_TTL_MS;
		}
	}

	static Path resolveDefaultCachePath() {
		final String override = System.getProperty(Tf1PlusConf.PROPERTY_CATALOGUE_CACHE_PATH);
		if (override != null && !override.trim().isEmpty()) {
			return new File(override.trim()).toPath();
		}
		return new File(FrameworkConf.USER_HOME + "/.habitv/cache/tf1plus-catalogue.json").toPath();
	}

	private static List<Map<String, Object>> serializeEntries(final List<Tf1PlusCatalogueEntry> entries) {
		final List<Map<String, Object>> serialized = new ArrayList<Map<String, Object>>();
		if (entries == null) {
			return serialized;
		}
		for (final Tf1PlusCatalogueEntry entry : entries) {
			final Map<String, Object> map = new LinkedHashMap<String, Object>();
			map.put("programmeId", entry.getProgrammeId());
			map.put("slug", entry.getSlug());
			map.put("title", entry.getTitle());
			map.put("hubId", entry.getHubId());
			map.put("urlSlug", entry.getUrlSlug());
			map.put("publicUrl", entry.getPublicUrl());
			map.put("editorialCategoryTypes", entry.getEditorialCategoryTypes());
			map.put("rights", entry.getRights());
			map.put("thumbnailUrl", entry.getThumbnailUrl());
			serialized.add(map);
		}
		return serialized;
	}

	@SuppressWarnings("unchecked")
	private static List<Tf1PlusCatalogueEntry> parseEntries(final Object raw) {
		if (!(raw instanceof List)) {
			return Collections.emptyList();
		}
		final List<Tf1PlusCatalogueEntry> entries = new ArrayList<Tf1PlusCatalogueEntry>();
		for (final Object item : (List<?>) raw) {
			if (!(item instanceof Map)) {
				continue;
			}
			final Map<String, Object> map = (Map<String, Object>) item;
			entries.add(new Tf1PlusCatalogueEntry(stringValue(map.get("programmeId")), stringValue(map.get("slug")),
					stringValue(map.get("title")), stringValue(map.get("hubId")), stringValue(map.get("urlSlug")),
					stringValue(map.get("publicUrl")), stringList(map.get("editorialCategoryTypes")),
					stringList(map.get("rights")), stringValue(map.get("thumbnailUrl"))));
		}
		return entries;
	}

	@SuppressWarnings("unchecked")
	private static List<String> stringList(final Object raw) {
		if (!(raw instanceof List)) {
			return Collections.emptyList();
		}
		final List<String> values = new ArrayList<String>();
		for (final Object entry : (List<?>) raw) {
			if (entry != null) {
				values.add(String.valueOf(entry));
			}
		}
		return values;
	}

	private static String stringValue(final Object raw) {
		return raw == null ? "" : String.valueOf(raw).trim();
	}

	private static int intValue(final Object raw) {
		if (raw instanceof Number) {
			return ((Number) raw).intValue();
		}
		try {
			return Integer.parseInt(String.valueOf(raw));
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private static long longValue(final Object raw) {
		if (raw instanceof Number) {
			return ((Number) raw).longValue();
		}
		try {
			return Long.parseLong(String.valueOf(raw));
		} catch (NumberFormatException e) {
			return 0L;
		}
	}

	static final class Snapshot {

		private final boolean present;
		private final boolean valid;
		private final String invalidReason;
		private final long cachedAtMillis;
		private final List<Tf1PlusCatalogueEntry> entries;

		private Snapshot(final boolean present, final boolean valid, final String invalidReason,
				final long cachedAtMillis, final List<Tf1PlusCatalogueEntry> entries) {
			this.present = present;
			this.valid = valid;
			this.invalidReason = invalidReason == null ? "" : invalidReason;
			this.cachedAtMillis = cachedAtMillis;
			this.entries = entries == null ? Collections.<Tf1PlusCatalogueEntry>emptyList() : entries;
		}

		static Snapshot missing() {
			return new Snapshot(false, false, "missing", 0L, Collections.<Tf1PlusCatalogueEntry>emptyList());
		}

		static Snapshot invalid(final String reason) {
			return new Snapshot(true, false, reason, 0L, Collections.<Tf1PlusCatalogueEntry>emptyList());
		}

		static Snapshot loaded(final long cachedAtMillis, final List<Tf1PlusCatalogueEntry> entries) {
			return new Snapshot(true, true, "", cachedAtMillis, entries);
		}

		boolean isPresent() {
			return present;
		}

		boolean isValid() {
			return valid;
		}

		String getInvalidReason() {
			return invalidReason;
		}

		long getCachedAtMillis() {
			return cachedAtMillis;
		}

		List<Tf1PlusCatalogueEntry> getEntries() {
			return entries;
		}

		boolean isFresh(final long ttlMillis) {
			if (!valid || cachedAtMillis <= 0L) {
				return false;
			}
			return System.currentTimeMillis() - cachedAtMillis <= ttlMillis;
		}

		boolean isStale(final long ttlMillis) {
			return valid && !isFresh(ttlMillis);
		}

	}

}
