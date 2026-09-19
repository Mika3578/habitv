package com.dabi.habitv.provider.playrts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Thin HTTP/JSON client for the public Play RTS catalogue APIs.
 */
final class PlayRtsClient {

	interface ContentLoader {
		String load(String url) throws IOException;
	}

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final ContentLoader contentLoader;

	PlayRtsClient(final ContentLoader contentLoader) {
		this.contentLoader = contentLoader;
	}

	List<Map<String, Object>> fetchShows() throws IOException {
		final String url = showsSourceUrl();
		final JsonNode root = OBJECT_MAPPER.readTree(contentLoader.load(url));
		return toObjectList(root.path("data"));
	}

	List<Map<String, Object>> fetchVideosByShowId(final String showId) throws IOException {
		if (StringUtils.isEmpty(showId)) {
			return Collections.emptyList();
		}
		final List<Map<String, Object>> all = new ArrayList<Map<String, Object>>();
		final Set<String> seenPages = new LinkedHashSet<String>();
		String url = mediaListSourceUrl(showId);
		for (int page = 0; page < PlayRtsConf.MAX_VIDEO_PAGES && StringUtils.isNotEmpty(url); page++) {
			if (!seenPages.add(url)) {
				break;
			}
			final JsonNode root = OBJECT_MAPPER.readTree(contentLoader.load(url));
			final List<Map<String, Object>> pageItems = extractMediaList(root);
			if (pageItems.isEmpty()) {
				break;
			}
			all.addAll(pageItems);
			url = nextPageUrl(root);
		}
		return all;
	}

	String showsSourceUrl() {
		return PlayRtsConf.API_BASE + PlayRtsConf.SHOWS_PATH;
	}

	String mediaListSourceUrl(final String showId) {
		return String.format(Locale.ROOT, PlayRtsConf.MEDIA_LIST_BY_SHOW_TEMPLATE, showId);
	}

	private static List<Map<String, Object>> extractMediaList(final JsonNode root) {
		if (root == null) {
			return Collections.emptyList();
		}
		final JsonNode mediaList = root.path("mediaList");
		if (mediaList.isArray()) {
			return toObjectList(mediaList);
		}
		final JsonNode data = root.path("data");
		if (data.isObject()) {
			final JsonNode nested = data.path("data");
			if (nested.isArray()) {
				return toObjectList(nested);
			}
		}
		if (data.isArray()) {
			return toObjectList(data);
		}
		return Collections.emptyList();
	}

	private static String nextPageUrl(final JsonNode root) {
		if (root == null) {
			return null;
		}
		final String next = root.path("next").asText(null);
		if (StringUtils.isEmpty(next) || "null".equalsIgnoreCase(next)) {
			return null;
		}
		if (next.startsWith("http://") || next.startsWith("https://")) {
			return next;
		}
		if (next.startsWith("/")) {
			return PlayRtsConf.IL_BASE + next;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> toObjectList(final JsonNode items) {
		final List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		if (items == null || !items.isArray()) {
			return result;
		}
		for (final JsonNode item : items) {
			if (item != null && item.isObject()) {
				result.add(OBJECT_MAPPER.convertValue(item, Map.class));
			}
		}
		return result;
	}
}
