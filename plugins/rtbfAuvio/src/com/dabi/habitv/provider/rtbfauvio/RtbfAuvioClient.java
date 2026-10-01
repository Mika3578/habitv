package com.dabi.habitv.provider.rtbfauvio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

final class RtbfAuvioClient {

	interface ContentLoader {
		String load(String url) throws IOException;
	}

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final String NEXT_DATA_MARKER = "id=\"__next_data__\"";

	private final ContentLoader contentLoader;

	RtbfAuvioClient(final ContentLoader contentLoader) {
		this.contentLoader = contentLoader;
	}

	List<String> findMediaListWidgetIds(final String channelPageHtml) throws IOException {
		if (StringUtils.isEmpty(channelPageHtml)) {
			return Collections.emptyList();
		}
		final String json = extractNextDataJson(channelPageHtml);
		if (json == null) {
			return Collections.emptyList();
		}
		final JsonNode root = MAPPER.readTree(json);
		final JsonNode ids = root.path("props").path("pageProps").path("rootData").path("preloadedWidgetIds");
		final List<String> widgetIds = new ArrayList<String>();
		if (ids.isArray()) {
			for (final JsonNode id : ids) {
				widgetIds.add(id.asText());
			}
		}
		return widgetIds;
	}

	/**
	 * Locate {@code __NEXT_DATA__} with index scans only — avoids ReDoS-prone
	 * regex against untrusted HTML.
	 */
	static String extractNextDataJson(final String html) {
		final String lower = html.toLowerCase(Locale.ROOT);
		final int marker = lower.indexOf(NEXT_DATA_MARKER);
		if (marker < 0) {
			return null;
		}
		final int openTagEnd = html.indexOf('>', marker);
		if (openTagEnd < 0 || openTagEnd + 1 >= html.length()) {
			return null;
		}
		final int closeTag = lower.indexOf("</script>", openTagEnd + 1);
		if (closeTag < 0) {
			return null;
		}
		final String json = html.substring(openTagEnd + 1, closeTag).trim();
		return StringUtils.isEmpty(json) ? null : json;
	}

	String fetchChannelPage(final String channelSlug) throws IOException {
		return contentLoader.load(channelPageUrl(channelSlug));
	}

	List<Map<String, Object>> fetchMediaListItems(final String widgetId) throws IOException {
		final String url = widgetUrl(widgetId);
		final JsonNode root = MAPPER.readTree(contentLoader.load(url));
		final JsonNode data = root.path("data");
		if (!"MEDIA_LIST".equals(data.path("type").asText())) {
			return Collections.emptyList();
		}
		return toObjectList(data.path("content"));
	}

	static String channelPageUrl(final String channelSlug) {
		return RtbfAuvioConf.HOME_URL + "/chaine/" + channelSlug;
	}

	static String widgetUrl(final String widgetId) {
		return RtbfAuvioConf.BFF_BASE + "/widgets/" + widgetId;
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> toObjectList(final JsonNode items) {
		final List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		if (items == null || !items.isArray()) {
			return result;
		}
		for (final JsonNode item : items) {
			if (item != null && item.isObject()) {
				result.add(MAPPER.convertValue(item, Map.class));
			}
		}
		return result;
	}
}
