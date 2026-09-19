package com.dabi.habitv.provider.telequebec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Thin GraphQL client for the public Télé-Québec pc-cms catalogue.
 */
final class TeleQuebecClient {

	interface ContentLoader {
		String load(String url) throws IOException;
	}

	interface GraphqlPoster {
		String post(String url, String jsonBody) throws IOException;
	}

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final String COLLECTION_QUERY =
			"query($slug:String!){ productByRootProductSlug(rootProductSlug:$slug){"
					+ " id title slug productType seasonCount"
					+ " seasons { id title seasonNumber episodeCount }"
					+ " } }";

	private static final String EPISODE_QUERY =
			"query($slug:String!,$s:Int,$e:Int){ productByRootProductSlug(rootProductSlug:$slug, seasonNumber:$s, episodeNumber:$e){"
					+ " id title episodeNumber seasonNumber availabilityStatus videoCanonicalUrl duration"
					+ " videoElement { __typename"
					+ " ... on RestrictedVideo { id mediaId code reason }"
					+ " ... on Video { id mediaId drmProtected"
					+ " encodings { hls { url } progressive { url } } } }"
					+ " } }";

	private final ContentLoader contentLoader;
	private final GraphqlPoster graphqlPoster;

	TeleQuebecClient(final ContentLoader contentLoader, final GraphqlPoster graphqlPoster) {
		this.contentLoader = contentLoader;
		this.graphqlPoster = graphqlPoster;
	}

	String homeSourceUrl() {
		return TeleQuebecConf.HOME_URL + "/";
	}

	String fetchHomeHtml() throws IOException {
		return contentLoader.load(homeSourceUrl());
	}

	Map<String, Object> fetchCollection(final String slug) throws IOException {
		final Map<String, Object> variables = new LinkedHashMap<String, Object>();
		variables.put("slug", slug);
		final JsonNode root = postGraphql(COLLECTION_QUERY, variables);
		return toObjectMap(root.path("data").path("productByRootProductSlug"));
	}

	Map<String, Object> fetchEpisode(final String slug, final int season, final int episode) throws IOException {
		final Map<String, Object> variables = new LinkedHashMap<String, Object>();
		variables.put("slug", slug);
		variables.put("s", Integer.valueOf(season));
		variables.put("e", Integer.valueOf(episode));
		final JsonNode root = postGraphql(EPISODE_QUERY, variables);
		return toObjectMap(root.path("data").path("productByRootProductSlug"));
	}

	List<Map<String, Object>> fetchSeasonEpisodes(final String slug, final int seasonNumber, final int episodeCount)
			throws IOException {
		final List<Map<String, Object>> episodes = new ArrayList<Map<String, Object>>();
		final int limit = Math.min(episodeCount, TeleQuebecConf.MAX_EPISODES_PER_SEASON);
		for (int episode = 1; episode <= limit; episode++) {
			final Map<String, Object> item = fetchEpisode(slug, seasonNumber, episode);
			if (item == null || item.isEmpty()) {
				continue;
			}
			episodes.add(item);
		}
		return episodes;
	}

	private JsonNode postGraphql(final String query, final Map<String, Object> variables) throws IOException {
		final Map<String, Object> body = new LinkedHashMap<String, Object>();
		body.put("query", query);
		body.put("variables", variables);
		final String payload = MAPPER.writeValueAsString(body);
		final String response = graphqlPoster.post(TeleQuebecConf.GRAPHQL_URL, payload);
		final JsonNode root = MAPPER.readTree(response);
		if (root.path("errors").isArray() && root.path("errors").size() > 0) {
			final String message = root.path("errors").get(0).path("message").asText("graphql-error");
			throw new IOException(message);
		}
		return root;
	}

	@SuppressWarnings("unchecked")
	static Map<String, Object> toObjectMap(final JsonNode node) {
		if (node == null || node.isMissingNode() || node.isNull() || !node.isObject()) {
			return Collections.emptyMap();
		}
		return MAPPER.convertValue(node, Map.class);
	}

	static Map<String, Object> parseGraphqlDataProduct(final String responseBody) throws IOException {
		final JsonNode root = MAPPER.readTree(responseBody);
		if (root.path("errors").isArray() && root.path("errors").size() > 0) {
			final String message = root.path("errors").get(0).path("message").asText("graphql-error");
			throw new IOException(message);
		}
		return toObjectMap(root.path("data").path("productByRootProductSlug"));
	}

	static String asString(final Map<String, Object> map, final String key) {
		if (map == null || map.get(key) == null) {
			return null;
		}
		final String value = String.valueOf(map.get(key)).trim();
		return StringUtils.isEmpty(value) || "null".equals(value) ? null : value;
	}

	static Integer asInt(final Map<String, Object> map, final String key) {
		if (map == null || map.get(key) == null) {
			return null;
		}
		final Object value = map.get(key);
		if (value instanceof Number) {
			return Integer.valueOf(((Number) value).intValue());
		}
		try {
			return Integer.valueOf(String.valueOf(value).trim());
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	static List<Map<String, Object>> asObjectList(final Map<String, Object> map, final String key) {
		if (map == null || map.get(key) == null || !(map.get(key) instanceof List)) {
			return Collections.emptyList();
		}
		final List<?> raw = (List<?>) map.get(key);
		final List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
		for (final Object item : raw) {
			if (item instanceof Map) {
				out.add((Map<String, Object>) item);
			}
		}
		return out;
	}

	static String extractStreamUrl(final Map<String, Object> episode) {
		if (episode == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		final Map<String, Object> videoElement = (Map<String, Object>) episode.get("videoElement");
		if (videoElement == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		final Map<String, Object> encodings = (Map<String, Object>) videoElement.get("encodings");
		if (encodings == null) {
			return null;
		}
		final String progressive = nestedUrl(encodings, "progressive");
		if (StringUtils.isNotEmpty(progressive)) {
			return progressive;
		}
		return nestedUrl(encodings, "hls");
	}

	static boolean isDrmProtected(final Map<String, Object> episode) {
		if (episode == null) {
			return false;
		}
		@SuppressWarnings("unchecked")
		final Map<String, Object> videoElement = (Map<String, Object>) episode.get("videoElement");
		if (videoElement == null) {
			return false;
		}
		final Object drm = videoElement.get("drmProtected");
		return Boolean.TRUE.equals(drm) || "true".equalsIgnoreCase(String.valueOf(drm));
	}

	static boolean isPlaybackUnavailable(final Map<String, Object> episode) {
		if (episode == null || episode.isEmpty()) {
			return true;
		}
		@SuppressWarnings("unchecked")
		final Map<String, Object> videoElement = (Map<String, Object>) episode.get("videoElement");
		if (videoElement == null) {
			return true;
		}
		final String type = asString(videoElement, "__typename");
		if ("RestrictedVideo".equals(type)) {
			return true;
		}
		if (isDrmProtected(episode)) {
			return true;
		}
		if (StringUtils.isNotEmpty(extractStreamUrl(episode))) {
			return false;
		}
		final String mediaId = asString(videoElement, "mediaId");
		return mediaId == null || "-1".equals(mediaId);
	}

	@SuppressWarnings("unchecked")
	private static String nestedUrl(final Map<String, Object> encodings, final String key) {
		final Object nested = encodings.get(key);
		if (!(nested instanceof Map)) {
			return null;
		}
		return asString((Map<String, Object>) nested, "url");
	}
}
