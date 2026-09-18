package com.dabi.habitv.provider.bfmtv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

final class BfmTvCatalogClient {

	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
	};

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final BfmTvHttpClient.Transport transport;

	BfmTvCatalogClient(final BfmTvHttpClient.Transport transport) {
		this.transport = transport;
	}

	String fetchToken(final String channelId) throws IOException {
		final Map<String, Object> body = fetchJson(BfmTvUrls.tokenUrl(channelId));
		final String token = BfmTvJson.firstString(BfmTvJson.asMap(body.get("session")), "token");
		if (StringUtils.isEmpty(token)) {
			throw new IOException("missing-session-token");
		}
		return token;
	}

	List<Map<String, Object>> fetchReplayPrograms(final String channelId) throws IOException {
		final String token = fetchToken(channelId);
		final Map<String, Object> body = fetchJson(BfmTvUrls.replayPageUrl(channelId, token));
		final List<Map<String, Object>> contents = BfmTvJson.asMapList(BfmTvJson.nested(body, "page", "contents"));
		final List<Map<String, Object>> programs = new ArrayList<Map<String, Object>>();
		for (final Map<String, Object> content : contents) {
			final List<Map<String, Object>> elements = BfmTvJson.asMapList(content.get("elements"));
			for (final Map<String, Object> element : elements) {
				programs.addAll(BfmTvJson.asMapList(element.get("items")));
			}
		}
		return programs;
	}

	List<Map<String, Object>> fetchVideos(final String channelId, final String categoryId) throws IOException {
		final String token = fetchToken(channelId);
		final List<Map<String, Object>> videos = new ArrayList<Map<String, Object>>();
		for (int page = 1; page <= BfmTvConf.MAX_VIDEO_PAGES; page++) {
			final Map<String, Object> body = fetchJson(BfmTvUrls.videosUrl(channelId, token, categoryId, page));
			final List<Map<String, Object>> items = BfmTvJson.asMapList(body.get("videos"));
			if (items.isEmpty()) {
				break;
			}
			videos.addAll(items);
			if (items.size() < BfmTvConf.VIDEO_PAGE_SIZE) {
				break;
			}
		}
		return videos;
	}

	private Map<String, Object> fetchJson(final String url) throws IOException {
		final Map<String, Object> body = MAPPER.readValue(transport.get(url), MAP_TYPE);
		if (body == null) {
			throw new IOException("empty-json");
		}
		return body;
	}

}
