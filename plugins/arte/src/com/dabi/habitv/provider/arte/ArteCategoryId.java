package com.dabi.habitv.provider.arte;

import org.apache.commons.lang.StringUtils;

/**
 * Stable internal category identifiers.
 *
 * <ul>
 * <li>{@code z/{lang}/{page}/{zoneId}} — one EMAC listing (preferred leaf)</li>
 * <li>{@code c/{lang}/{collectionId}} — EMAC collection (e.g. {@code RC-028069})</li>
 * <li>{@code {lang}:{page}} — legacy merged page (backward compatible)</li>
 * <li>{@code {lang}:{page}:{zoneKey}} — legacy zone key (code or id)</li>
 * </ul>
 */
final class ArteCategoryId {

	private static final String ZONE_PREFIX = "z/";

	private static final String COLLECTION_PREFIX = "c/";

	private static final String LEGACY_SEPARATOR = ":";

	enum Kind {
		ZONE,
		COLLECTION,
		LEGACY_PAGE,
		LEGACY_ZONE
	}

	private final Kind kind;

	private final String languageCode;

	private final String pageCode;

	private final String zoneKey;

	private final String collectionId;

	private ArteCategoryId(final Kind kind, final String languageCode, final String pageCode, final String zoneKey,
			final String collectionId) {
		this.kind = kind;
		this.languageCode = languageCode;
		this.pageCode = pageCode;
		this.zoneKey = zoneKey;
		this.collectionId = collectionId;
	}

	static String forZone(final String languageCode, final String pageCode, final String zoneId) {
		return ZONE_PREFIX + languageCode + "/" + pageCode + "/" + zoneId;
	}

	static String forCollection(final String languageCode, final String collectionId) {
		return COLLECTION_PREFIX + languageCode + "/" + collectionId;
	}

	static String legacyPage(final String languageCode, final String pageCode) {
		return languageCode + LEGACY_SEPARATOR + pageCode;
	}

	static ArteCategoryId parse(final String categoryId) {
		if (StringUtils.isEmpty(categoryId)) {
			return null;
		}
		if (categoryId.startsWith(ZONE_PREFIX)) {
			final String[] parts = categoryId.split("/", 4);
			if (parts.length != 4 || StringUtils.isEmpty(parts[1]) || StringUtils.isEmpty(parts[2])
					|| StringUtils.isEmpty(parts[3])) {
				return null;
			}
			return new ArteCategoryId(Kind.ZONE, parts[1], parts[2], parts[3], null);
		}
		if (categoryId.startsWith(COLLECTION_PREFIX)) {
			final String[] parts = categoryId.split("/", 3);
			if (parts.length != 3 || StringUtils.isEmpty(parts[1]) || StringUtils.isEmpty(parts[2])) {
				return null;
			}
			return new ArteCategoryId(Kind.COLLECTION, parts[1], null, null, parts[2]);
		}
		final String[] parts = categoryId.split(LEGACY_SEPARATOR, -1);
		if (parts.length < 2 || parts.length > 3) {
			return null;
		}
		if (StringUtils.isEmpty(parts[0]) || StringUtils.isEmpty(parts[1])) {
			return null;
		}
		if (parts.length == 3) {
			if (StringUtils.isEmpty(parts[2])) {
				return null;
			}
			return new ArteCategoryId(Kind.LEGACY_ZONE, parts[0], parts[1], parts[2], null);
		}
		return new ArteCategoryId(Kind.LEGACY_PAGE, parts[0], parts[1], null, null);
	}

	Kind getKind() {
		return kind;
	}

	String getLanguageCode() {
		return languageCode;
	}

	String getPageCode() {
		return pageCode;
	}

	String getZoneKey() {
		return zoneKey;
	}

	String getCollectionId() {
		return collectionId;
	}
}
