package com.dabi.habitv.provider.tf1plus;



import java.io.IOException;

import java.net.URLEncoder;

import java.util.ArrayList;

import java.util.Collections;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;

import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;



/**

 * TF1+ public GraphQL client (persisted queries).

 */

final class Tf1PlusGraphqlClient {



	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {

	};



	private static final int PAGE_SIZE = 500;

	private static final ObjectMapper MAPPER = new ObjectMapper();



	private final BasePluginWithProxy plugin;



	Tf1PlusGraphqlClient(final BasePluginWithProxy plugin) {

		this.plugin = plugin;

	}



	List<Map<String, Object>> fetchProgramsByChannel(final String channelSlug) throws IOException {
		final List<Map<String, Object>> programs = new ArrayList<Map<String, Object>>();
		int offset = 0;
		while (offset < Tf1PlusConf.CATALOGUE_SAFETY_PROGRAMME_LIMIT) {
			final List<Map<String, Object>> page = fetchProgramItemsPage(channelSlug, offset, PAGE_SIZE);
			if (page.isEmpty()) {
				break;
			}
			programs.addAll(page);
			if (page.size() < PAGE_SIZE) {
				break;
			}
			offset += PAGE_SIZE;
		}
		return programs;
	}

	List<Map<String, Object>> fetchProgramItemsPage(final String channelSlug, final int offset, final int limit)
			throws IOException {
		final Map<String, Object> variables = buildProgramVariables(channelSlug, offset, limit);
		final Map<String, Object> data = fetchGraphql(Tf1PlusConf.GRAPHQL_PROGRAMS_BY_CHANNEL, variables);
		final Map<String, Object> programsNode = asMap(data.get("programs"));
		return castItemList(programsNode.get("items"));
	}



	List<Map<String, Object>> fetchVideosByProgram(final String programSlug) throws IOException {
		final String graphqlSlug = Tf1PlusProgramSlug.resolveForGraphql(programSlug);
		final List<Map<String, Object>> videos = new ArrayList<Map<String, Object>>();
		int offset = 0;
		while (offset < Tf1PlusConf.CATALOGUE_SAFETY_PROGRAMME_LIMIT) {
			final List<Map<String, Object>> page = fetchVideoItemsPage(graphqlSlug, offset, PAGE_SIZE);
			if (page.isEmpty()) {
				break;
			}
			videos.addAll(page);
			if (page.size() < PAGE_SIZE) {
				break;
			}
			offset += PAGE_SIZE;
		}
		return videos;
	}

	List<Map<String, Object>> fetchVideoItemsPage(final String programSlug, final int offset, final int limit)
			throws IOException {
		final Map<String, Object> variables = buildVideoVariables(programSlug, offset, limit);
		final Map<String, Object> data = fetchGraphql(Tf1PlusConf.GRAPHQL_VIDEOS_BY_PROGRAM, variables);
		final Map<String, Object> programNode = asMap(data.get("programBySlug"));
		final Map<String, Object> videosNode = asMap(programNode.get("videos"));
		return castItemList(videosNode.get("items"));
	}



	Map<String, Object> fetchVideoByVideoSlug(final String programSlug, final String videoSlug) throws IOException {

		final Map<String, Object> variables = new LinkedHashMap<String, Object>();

		variables.put("programSlug", Tf1PlusProgramSlug.resolveForGraphql(programSlug));

		variables.put("slug", videoSlug);

		final Map<String, Object> data = fetchGraphql(Tf1PlusConf.GRAPHQL_VIDEO_BY_SLUG, variables);

		return asMap(data.get("videoBySlug"));

	}

	String fetchStreamIdByVideoSlug(final String programSlug, final String videoSlug) throws IOException {

		return stringValue(fetchVideoByVideoSlug(programSlug, videoSlug).get("streamId"));

	}



	String graphqlSourceUrl(final String queryId, final Map<String, Object> variables) throws IOException {

		return Tf1PlusConf.GRAPHQL_URL + "?id=" + queryId + "&variables="

				+ URLEncoder.encode(MAPPER.writeValueAsString(variables), "UTF-8");

	}



	private Map<String, Object> buildProgramVariables(final String channelSlug, final int offset) {
		return buildProgramVariables(channelSlug, offset, PAGE_SIZE);
	}

	private Map<String, Object> buildProgramVariables(final String channelSlug, final int offset, final int limit) {

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

		variables.put("offset", Integer.valueOf(offset));

		variables.put("limit", Integer.valueOf(limit));

		return variables;

	}

	private Map<String, Object> buildVideoVariables(final String programSlug, final int offset, final int limit) {
		final Map<String, Object> variables = new LinkedHashMap<String, Object>();
		variables.put("programSlug", programSlug);
		variables.put("offset", Integer.valueOf(offset));
		variables.put("limit", Integer.valueOf(limit));
		final Map<String, Object> sort = new LinkedHashMap<String, Object>();
		sort.put("type", "DATE");
		sort.put("order", "DESC");
		variables.put("sort", sort);
		variables.put("types", Collections.singletonList("REPLAY"));
		return variables;
	}



	private Map<String, Object> fetchGraphql(final String queryId, final Map<String, Object> variables)

			throws IOException {

		final String url = graphqlSourceUrl(queryId, variables);

		final Map<String, Object> root = MAPPER.readValue(plugin.getInputStreamFromUrl(url), MAP_TYPE);

		final Object data = root.get("data");

		if (!(data instanceof Map)) {

			throw new IOException("TF1+ GraphQL response missing data node");

		}

		@SuppressWarnings("unchecked")

		final Map<String, Object> dataMap = (Map<String, Object>) data;

		return dataMap;

	}



	@SuppressWarnings("unchecked")

	static List<Map<String, Object>> castItemList(final Object raw) {

		if (!(raw instanceof List)) {

			return Collections.emptyList();

		}

		final List<?> list = (List<?>) raw;

		final List<Map<String, Object>> items = new ArrayList<Map<String, Object>>(list.size());

		for (final Object entry : list) {

			if (entry instanceof Map) {

				items.add((Map<String, Object>) entry);

			}

		}

		return items;

	}



	@SuppressWarnings("unchecked")

	static Map<String, Object> asMap(final Object raw) {

		if (raw instanceof Map) {

			return (Map<String, Object>) raw;

		}

		return Collections.emptyMap();

	}



	static String stringValue(final Object raw) {

		return raw == null ? "" : String.valueOf(raw).trim();

	}

	/**
	 * Numeric mediainfo id used by TF1 delivery API (distinct from GraphQL UUID {@code id}).
	 */
	static String resolveMediaStreamId(final Map<String, Object> video) {
		if (video == null) {
			return "";
		}
		final String streamId = stringValue(video.get("streamId"));
		if (isNumericMediaStreamId(streamId)) {
			return streamId;
		}
		final String id = stringValue(video.get("id"));
		if (isNumericMediaStreamId(id)) {
			return id;
		}
		return "";
	}

	/**
	 * Stream reference for the premium replay helper: numeric mediainfo id or GraphQL UUID.
	 */
	static String resolvePremiumDeliveryId(final Map<String, Object> video) {
		if (video == null) {
			return "";
		}
		final String streamId = stringValue(video.get("streamId"));
		if (StringUtils.isNotEmpty(streamId)) {
			return streamId;
		}
		return stringValue(video.get("id"));
	}

	static boolean isNumericMediaStreamId(final String value) {
		if (StringUtils.isEmpty(value)) {
			return false;
		}
		for (int index = 0; index < value.length(); index++) {
			if (!Character.isDigit(value.charAt(index))) {
				return false;
			}
		}
		return value.length() >= 5;
	}

}
