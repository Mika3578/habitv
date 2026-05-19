package com.dabi.habitv.provider.francetv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Thin client for france.tv mobile catalogue API (api-mobile.yatta.francetv.fr).
 */
final class FranceTvApiClient {

	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
	};

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final BasePluginWithProxy plugin;

	FranceTvApiClient(final BasePluginWithProxy plugin) {
		this.plugin = plugin;
	}

	List<Map<String, Object>> fetchPrograms(final String channelSlug) throws IOException {
		final List<Map<String, Object>> programs = new ArrayList<>();
		for (int page = 0; page < 100; page++) {
			final String url = FranceTvConf.API_MOBILE_URL + "/apps/regions/" + channelSlug + "/programs"
					+ "?platform=" + FranceTvConf.API_PLATFORM + "&page=" + page;
			final Map<String, Object> body = fetchJson(url);
			final List<Map<String, Object>> items = castItemList(body.get("items"));
			if (items.isEmpty()) {
				break;
			}
			programs.addAll(items);
			if (items.size() < 20) {
				break;
			}
		}
		return programs;
	}

	List<Map<String, Object>> fetchEpisodes(final String programPath) throws IOException {
		final List<Map<String, Object>> episodes = new ArrayList<>();
		for (int page = 0; page < 100; page++) {
			final String url = FranceTvConf.API_MOBILE_URL + "/generic/taxonomy/" + programPath + "/contents"
					+ "?platform=" + FranceTvConf.API_PLATFORM + "&page=" + page;
			final Map<String, Object> body = fetchJson(url);
			final List<Map<String, Object>> items = castItemList(body.get("items"));
			if (items.isEmpty()) {
				break;
			}
			episodes.addAll(items);
			if (items.size() < 20) {
				break;
			}
		}
		return episodes;
	}

	private Map<String, Object> fetchJson(final String url) throws IOException {
		return MAPPER.readValue(plugin.getInputStreamFromUrl(url), MAP_TYPE);
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> castItemList(final Object raw) {
		if (!(raw instanceof List)) {
			return Collections.emptyList();
		}
		final List<?> list = (List<?>) raw;
		final List<Map<String, Object>> items = new ArrayList<>(list.size());
		for (final Object entry : list) {
			if (entry instanceof Map) {
				items.add((Map<String, Object>) entry);
			}
		}
		return items;
	}

}
