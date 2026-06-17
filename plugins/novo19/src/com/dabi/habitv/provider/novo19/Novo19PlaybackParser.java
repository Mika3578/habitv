package com.dabi.habitv.provider.novo19;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

final class Novo19PlaybackParser {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private Novo19PlaybackParser() {
	}

	static String parseSessionToken(final String json, final String sourceUrl) {
		final JsonNode root = parseRoot(json, sourceUrl);
		final String token = textValue(root, "sessionToken");
		return StringUtils.isEmpty(token) ? null : token;
	}

	static String selectReplayStreamUrl(final String json, final String sourceUrl) {
		final JsonNode root = parseRoot(json, sourceUrl);
		final JsonNode formats = root.path("formats");
		if (!formats.isArray()) {
			return null;
		}
		final List<String> hlsCandidates = new ArrayList<>();
		final List<String> dashCandidates = new ArrayList<>();
		final List<String> otherCandidates = new ArrayList<>();
		for (final JsonNode formatNode : formats) {
			if (!isPublicReplayFormat(formatNode)) {
				continue;
			}
			final String locator = textValue(formatNode, "mediaLocator");
			if (StringUtils.isEmpty(locator)) {
				continue;
			}
			final String format = textValue(formatNode, "format");
			if ("HLS".equalsIgnoreCase(format) || locator.contains(".m3u8") || locator.contains("/manifest")) {
				hlsCandidates.add(locator);
			} else if ("DASH".equalsIgnoreCase(format) || locator.contains(".mpd")) {
				dashCandidates.add(locator);
			} else {
				otherCandidates.add(locator);
			}
		}
		if (!hlsCandidates.isEmpty()) {
			return hlsCandidates.get(0);
		}
		if (!dashCandidates.isEmpty()) {
			return dashCandidates.get(0);
		}
		return otherCandidates.isEmpty() ? null : otherCandidates.get(0);
	}

	static boolean hasFormats(final String json, final String sourceUrl) {
		final JsonNode root = parseRoot(json, sourceUrl);
		final JsonNode formats = root.path("formats");
		return formats.isArray() && formats.size() > 0;
	}

	static boolean hasOnlyProtectedFormats(final String json, final String sourceUrl) {
		final JsonNode root = parseRoot(json, sourceUrl);
		final JsonNode formats = root.path("formats");
		if (!formats.isArray() || formats.size() == 0) {
			return false;
		}
		for (final JsonNode formatNode : formats) {
			if (isPublicReplayFormat(formatNode)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isPublicReplayFormat(final JsonNode formatNode) {
		if (formatNode == null || formatNode.isMissingNode()) {
			return false;
		}
		if (isProtectedDrm(formatNode.path("drm"))) {
			return false;
		}
		return !StringUtils.isEmpty(textValue(formatNode, "mediaLocator"));
	}

	private static boolean isProtectedDrm(final JsonNode drmNode) {
		if (drmNode == null || drmNode.isMissingNode() || drmNode.isNull()) {
			return false;
		}
		if (drmNode.isBoolean()) {
			return drmNode.asBoolean();
		}
		if (drmNode.isObject()) {
			return drmNode.size() > 0;
		}
		if (drmNode.isArray()) {
			return drmNode.size() > 0;
		}
		if (drmNode.isTextual()) {
			return !StringUtils.isEmpty(drmNode.asText());
		}
		return true;
	}

	private static String textValue(final JsonNode node, final String field) {
		if (node == null || node.isMissingNode()) {
			return null;
		}
		final String value = node.path(field).asText(null);
		return StringUtils.isEmpty(value) ? null : value.trim();
	}

	private static JsonNode parseRoot(final String json, final String sourceUrl) {
		if (StringUtils.isEmpty(json)) {
			return MAPPER.createObjectNode();
		}
		try {
			return MAPPER.readTree(json);
		} catch (final IOException e) {
			throw new TechnicalException("Cannot parse NOVO19 playback response from " + sourceUrl, e);
		}
	}

}
