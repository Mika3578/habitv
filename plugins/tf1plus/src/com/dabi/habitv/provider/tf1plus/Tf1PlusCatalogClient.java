package com.dabi.habitv.provider.tf1plus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

final class Tf1PlusCatalogClient {

	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
	};

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final Tf1PlusHttpClient.Transport transport;

	Tf1PlusCatalogClient(final Tf1PlusHttpClient.Transport transport) {
		this.transport = transport;
	}

	List<Map<String, Object>> fetchPrograms(final String channelSlug) throws IOException {
		final List<Map<String, Object>> programs = new ArrayList<Map<String, Object>>();
		for (int page = 0; page < Tf1PlusConf.MAX_PROGRAM_PAGES; page++) {
			final Map<String, Object> variables = new LinkedHashMap<String, Object>();
			final Map<String, Object> context = new LinkedHashMap<String, Object>();
			context.put("persona", "PERSONA_2");
			context.put("application", "WEB");
			context.put("device", "DESKTOP");
			context.put("os", "WINDOWS");
			final Map<String, Object> filter = new LinkedHashMap<String, Object>();
			filter.put("channel", channelSlug);
			variables.put("context", context);
			variables.put("filter", filter);
			variables.put("offset", Integer.valueOf(itemOffset(page, Tf1PlusConf.PROGRAM_PAGE_SIZE)));
			variables.put("limit", Integer.valueOf(Tf1PlusConf.PROGRAM_PAGE_SIZE));
			final Map<String, Object> body = fetchGraphql(Tf1PlusConf.QUERY_PROGRAMS, MAPPER.writeValueAsString(variables));
			final List<Map<String, Object>> items = Tf1PlusJson.asMapList(Tf1PlusJson.nested(body, "data", "programs", "items"));
			if (items.isEmpty()) {
				break;
			}
			programs.addAll(items);
			if (items.size() < Tf1PlusConf.PROGRAM_PAGE_SIZE) {
				break;
			}
		}
		return programs;
	}

	List<Map<String, Object>> fetchReplayVideos(final String programSlug) throws IOException {
		final List<Map<String, Object>> videos = new ArrayList<Map<String, Object>>();
		for (int page = 0; page < Tf1PlusConf.MAX_VIDEO_PAGES; page++) {
			final Map<String, Object> variables = new LinkedHashMap<String, Object>();
			variables.put("programSlug", programSlug);
			variables.put("offset", Integer.valueOf(itemOffset(page, Tf1PlusConf.VIDEO_PAGE_SIZE)));
			variables.put("limit", Integer.valueOf(Tf1PlusConf.VIDEO_PAGE_SIZE));
			final Map<String, Object> sort = new LinkedHashMap<String, Object>();
			sort.put("type", "DATE");
			sort.put("order", "DESC");
			variables.put("sort", sort);
			final List<String> types = new ArrayList<String>();
			types.add(Tf1PlusConf.VIDEO_TYPE_REPLAY);
			variables.put("types", types);
			final Map<String, Object> body = fetchGraphql(Tf1PlusConf.QUERY_VIDEOS, MAPPER.writeValueAsString(variables));
			final List<Map<String, Object>> items = Tf1PlusJson
					.asMapList(Tf1PlusJson.nested(body, "data", "programBySlug", "videos", "items"));
			if (items.isEmpty()) {
				break;
			}
			videos.addAll(items);
			if (items.size() < Tf1PlusConf.VIDEO_PAGE_SIZE) {
				break;
			}
		}
		return videos;
	}

	private Map<String, Object> fetchGraphql(final String queryId, final String variablesJson) throws IOException {
		final String raw = transport.get(Tf1PlusUrls.graphqlUrl(queryId, variablesJson));
		final Map<String, Object> body = MAPPER.readValue(raw, MAP_TYPE);
		if (body == null) {
			throw new IOException("empty-graphql");
		}
		return body;
	}

	static int itemOffset(final int page, final int pageSize) {
		return page * pageSize;
	}

}
