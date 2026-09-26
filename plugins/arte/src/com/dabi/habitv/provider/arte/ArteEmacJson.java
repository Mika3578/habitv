package com.dabi.habitv.provider.arte;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Shared EMAC JSON helpers. Preserves PR #234 root-level and legacy
 * {@code value.*} shapes.
 */
final class ArteEmacJson {

	private static final Pattern EMAC_PAGE_DEEPLINK = Pattern.compile("arte://emac/([A-Z][A-Z0-9_]+)");

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private ArteEmacJson() {
	}

	static JsonNode parseTree(final String json, final String sourceUrl) {
		try {
			return OBJECT_MAPPER.readTree(json);
		} catch (final IOException e) {
			throw new TechnicalException("Cannot parse Arte EMAC response from " + sourceUrl, e);
		}
	}

	static JsonNode emacZonesNode(final JsonNode pageRoot) {
		final JsonNode zones = pageRoot.path("zones");
		if (zones.isArray()) {
			return zones;
		}
		return pageRoot.path("value").path("zones");
	}

	static JsonNode emacDataNode(final JsonNode zoneRoot) {
		final JsonNode data = zoneRoot.path("data");
		if (data.isArray()) {
			return data;
		}
		return zoneRoot.path("value").path("data");
	}

	static JsonNode zonePagination(final JsonNode zoneRoot) {
		final JsonNode pagination = zoneRoot.path("pagination");
		if (!pagination.isMissingNode() && !pagination.isNull()) {
			return pagination;
		}
		return zoneRoot.path("value").path("pagination");
	}

	static String pageTitle(final JsonNode pageRoot) {
		final String title = pageRoot.path("metadata").path("title").asText(null);
		if (StringUtils.isNotEmpty(title)) {
			final int pipe = title.indexOf('|');
			return pipe > 0 ? title.substring(0, pipe).trim() : title.trim();
		}
		return pageRoot.path("code").asText(null);
	}

	static void collectEmacPageCodes(final JsonNode node, final Set<String> pageCodes) {
		if (node == null || node.isMissingNode()) {
			return;
		}
		if (node.isObject()) {
			final String deeplink = node.path("deeplink").asText(null);
			if (StringUtils.isNotEmpty(deeplink)) {
				addPageCodeFromDeeplink(deeplink, pageCodes);
			}
			final JsonNode link = node.path("link");
			if (link.isObject()) {
				final String page = link.path("page").asText(null);
				if (StringUtils.isNotEmpty(page)) {
					pageCodes.add(page);
				}
			}
			final Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
			while (fields.hasNext()) {
				collectEmacPageCodes(fields.next().getValue(), pageCodes);
			}
		} else if (node.isArray()) {
			for (final JsonNode child : node) {
				collectEmacPageCodes(child, pageCodes);
			}
		}
	}

	static void addPageCodesFromHtml(final String html, final Set<String> pageCodes) {
		if (StringUtils.isEmpty(html)) {
			return;
		}
		final Matcher matcher = EMAC_PAGE_DEEPLINK.matcher(html);
		while (matcher.find()) {
			pageCodes.add(matcher.group(1));
		}
	}

	private static void addPageCodeFromDeeplink(final String deeplink, final Set<String> pageCodes) {
		if (!deeplink.startsWith("arte://emac/")) {
			return;
		}
		final String code = deeplink.substring("arte://emac/".length());
		if (StringUtils.isNotEmpty(code)) {
			pageCodes.add(code);
		}
	}
}
