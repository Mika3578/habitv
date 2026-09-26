package com.dabi.habitv.provider.arte;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Classifies EMAC teaser records for catalogue navigation vs playback.
 */
final class ArteContentClassifier {

	private static final Pattern VIDEO_EPISODE_URL = Pattern.compile(
			"https://www\\.arte\\.tv/[a-z]{2}/videos/(?:\\d{6}-\\d{3}-[AF]|RC-\\d+)/[^\"'\\s<>]+");

	private static final Pattern COLLECTION_ID = Pattern.compile("^RC-\\d+$");

	enum ContentKind {
		PLAYABLE_SHOW,
		COLLECTION,
		NAVIGATION_PAGE,
		UNSUPPORTED
	}

	private ArteContentClassifier() {
	}

	static ContentKind classifyTeaser(final JsonNode item, final String resolvedUrl) {
		if (item == null || item.isMissingNode()) {
			return ContentKind.UNSUPPORTED;
		}
		final String programId = item.path("programId").asText(null);
		if (isCollectionId(programId)) {
			return ContentKind.COLLECTION;
		}
		final JsonNode kind = item.path("kind");
		if (kind.path("isCollection").asBoolean(false) || "COLLECTION".equals(kind.path("code").asText())) {
			return ContentKind.COLLECTION;
		}
		if (StringUtils.isNotEmpty(resolvedUrl) && VIDEO_EPISODE_URL.matcher(resolvedUrl).find()) {
			if (resolvedUrl.contains("/videos/RC-")) {
				return ContentKind.COLLECTION;
			}
			return ContentKind.PLAYABLE_SHOW;
		}
		final String deeplink = item.path("deeplink").asText(null);
		if (StringUtils.isNotEmpty(deeplink) && deeplink.startsWith("arte://emac/")) {
			return ContentKind.NAVIGATION_PAGE;
		}
		if (StringUtils.isNotEmpty(resolvedUrl) && resolvedUrl.contains("/videos/")) {
			return ContentKind.NAVIGATION_PAGE;
		}
		return ContentKind.UNSUPPORTED;
	}

	static boolean isPlayableShow(final JsonNode item, final String resolvedUrl) {
		return classifyTeaser(item, resolvedUrl) == ContentKind.PLAYABLE_SHOW;
	}

	static String collectionId(final JsonNode item, final String resolvedUrl) {
		final String programId = item.path("programId").asText(null);
		if (isCollectionId(programId)) {
			return programId;
		}
		if (StringUtils.isNotEmpty(resolvedUrl) && resolvedUrl.contains("/videos/RC-")) {
			final int start = resolvedUrl.indexOf("/videos/RC-") + "/videos/".length();
			final int end = resolvedUrl.indexOf('/', start);
			final String id = end > start ? resolvedUrl.substring(start, end) : resolvedUrl.substring(start);
			if (isCollectionId(id)) {
				return id;
			}
		}
		return null;
	}

	static boolean shouldSkipZone(final JsonNode zone) {
		if (zone == null || zone.isMissingNode()) {
			return true;
		}
		if (zone.path("authenticatedContent").asBoolean(false)) {
			return true;
		}
		final String title = zone.path("title").asText("");
		final String lower = title.toLowerCase();
		if (lower.contains("newsletter") || lower.contains("boutique")) {
			return true;
		}
		if (lower.contains("event teaser")) {
			return true;
		}
		return false;
	}

	private static boolean isCollectionId(final String programId) {
		return StringUtils.isNotEmpty(programId) && COLLECTION_ID.matcher(programId).matches();
	}
}
