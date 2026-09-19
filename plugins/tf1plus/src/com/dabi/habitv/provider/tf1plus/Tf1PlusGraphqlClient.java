package com.dabi.habitv.provider.tf1plus;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Thin GraphQL client for the public TF1+ web catalogue.
 */
final class Tf1PlusGraphqlClient {

	interface ContentLoader {
		String load(String url) throws IOException;
	}

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final ContentLoader contentLoader;

	Tf1PlusGraphqlClient(final ContentLoader contentLoader) {
		this.contentLoader = contentLoader;
	}

	List<Map<String, Object>> fetchPrograms(final String channelSlug) throws IOException {
		final Map<String, Object> variables = new LinkedHashMap<String, Object>();
		final Map<String, Object> context = new LinkedHashMap<String, Object>();
		context.put("persona", "PERSONA_2");
		context.put("application", "WEB");
		context.put("device", "DESKTOP");
		context.put("os", "WINDOWS");
		variables.put("context", context);
		final Map<String, Object> filter = new LinkedHashMap<String, Object>();
		filter.put("channel", channelSlug);
		variables.put("filter", filter);
		variables.put("offset", Integer.valueOf(0));
		variables.put("limit", Integer.valueOf(Tf1PlusConf.PROGRAM_PAGE_LIMIT));

		final JsonNode root = fetchJson(Tf1PlusConf.QUERY_PROGRAMS, variables);
		final JsonNode items = root.path("data").path("programs").path("items");
		return toObjectList(items);
	}

	List<Map<String, Object>> fetchReplayVideos(final String programSlug) throws IOException {
		final List<Map<String, Object>> all = new ArrayList<Map<String, Object>>();
		for (int page = 0; page < Tf1PlusConf.MAX_VIDEO_PAGES; page++) {
			final Map<String, Object> variables = new LinkedHashMap<String, Object>();
			variables.put("programSlug", programSlug);
			variables.put("offset", Integer.valueOf(page * Tf1PlusConf.VIDEO_PAGE_LIMIT));
			variables.put("limit", Integer.valueOf(Tf1PlusConf.VIDEO_PAGE_LIMIT));
			final Map<String, Object> sort = new LinkedHashMap<String, Object>();
			sort.put("type", "DATE");
			sort.put("order", "DESC");
			variables.put("sort", sort);
			variables.put("types", Collections.singletonList(Tf1PlusConf.VIDEO_TYPE_REPLAY));

			final JsonNode root = fetchJson(Tf1PlusConf.QUERY_VIDEOS, variables);
			final JsonNode items = root.path("data").path("programBySlug").path("videos").path("items");
			final List<Map<String, Object>> pageItems = toObjectList(items);
			if (pageItems.isEmpty()) {
				break;
			}
			all.addAll(pageItems);
			if (pageItems.size() < Tf1PlusConf.VIDEO_PAGE_LIMIT) {
				break;
			}
		}
		return all;
	}

	String programsSourceUrl(final String channelSlug) {
		return buildGraphqlUrl(Tf1PlusConf.QUERY_PROGRAMS, "channel=" + channelSlug);
	}

	String videosSourceUrl(final String programSlug) {
		return buildGraphqlUrl(Tf1PlusConf.QUERY_VIDEOS, "programSlug=" + programSlug);
	}

	private JsonNode fetchJson(final String queryId, final Map<String, Object> variables) throws IOException {
		final String variablesJson = OBJECT_MAPPER.writeValueAsString(variables);
		final String url = Tf1PlusConf.GRAPHQL_URL + "?id=" + encode(queryId) + "&variables=" + encode(variablesJson);
		final String body = contentLoader.load(url);
		if (StringUtils.isEmpty(body)) {
			throw new IOException("Empty TF1+ GraphQL response for query " + queryId);
		}
		return OBJECT_MAPPER.readTree(body);
	}

	private static String buildGraphqlUrl(final String queryId, final String hint) {
		return Tf1PlusConf.GRAPHQL_URL + "?id=" + queryId + "&" + hint;
	}

	private static String encode(final String value) throws UnsupportedEncodingException {
		// Java 8 baseline: Charset overload of URLEncoder.encode arrived in Java 10.
		return URLEncoder.encode(value, "UTF-8");
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
