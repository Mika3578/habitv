package com.dabi.habitv.provider.rtbfauvio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

final class RtbfAuvioPlaybackParser {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private RtbfAuvioPlaybackParser() {
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
		final List<String> hlsCandidates = new ArrayList<String>();
		final List<String> dashCandidates = new ArrayList<String>();
		final List<String> otherCandidates = new ArrayList<String>();
		for (final JsonNode formatNode : formats) {
			if (!isUsableFormat(formatNode)) {
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

	static boolean hasOnlyUnusableFormats(final String json, final String sourceUrl) {
		final JsonNode root = parseRoot(json, sourceUrl);
		final JsonNode formats = root.path("formats");
		if (!formats.isArray() || formats.size() == 0) {
			return false;
		}
		for (final JsonNode formatNode : formats) {
			if (isUsableFormat(formatNode)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isUsableFormat(final JsonNode formatNode) {
		if (formatNode == null || formatNode.isMissingNode()) {
			return false;
		}
		if (hasAccessRestriction(formatNode.path("drm"))) {
			return false;
		}
		return !StringUtils.isEmpty(textValue(formatNode, "mediaLocator"));
	}

	private static boolean hasAccessRestriction(final JsonNode restrictionNode) {
		if (restrictionNode == null || restrictionNode.isMissingNode() || restrictionNode.isNull()) {
			return false;
		}
		if (restrictionNode.isBoolean()) {
			return restrictionNode.asBoolean();
		}
		if (restrictionNode.isObject()) {
			return restrictionNode.size() > 0;
		}
		if (restrictionNode.isArray()) {
			return restrictionNode.size() > 0;
		}
		if (restrictionNode.isTextual()) {
			return !StringUtils.isEmpty(restrictionNode.asText());
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
			throw new TechnicalException("Cannot parse RTBF Auvio playback response from " + sourceUrl, e);
		}
	}

}
