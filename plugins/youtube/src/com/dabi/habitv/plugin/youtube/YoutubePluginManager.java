package com.dabi.habitv.plugin.youtube;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;

import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.dabi.habitv.framework.plugin.tpl.TemplateIdBuilder;
import com.dabi.habitv.framework.plugin.tpl.TemplateUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

public class YoutubePluginManager extends BasePluginWithProxy implements PluginProviderInterface {
	private static final String KEY = "key";
	private static final String PLAYLIST_ID = "playlistId";
	private static final String PUBLISHED_AFTER = "publishedAfter";

	private static final String VIDEO_CATEGORY_ID = "videoCategoryId";
	private static final String TOPIC_ID = "topicId";
	private static final String CHANNEL_ID = "channelId";
	private static final String QUERY = "q";
	private static final String MAX_RESULTS = "maxResults";
	private static final String DAYS = "days";
	private static final String TOP = "Top";
	private static final String PLAYLIST = "Playlist";
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // 2017-05-01T00:00:00Z
	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String getName() {
		return YoutubeConf.NAME;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(CategoryDTO category) {
		requireApiKey();
		if (PLAYLIST.equals(category.getFatherCategory().getName())) {
			return findEpisodePlaylist(category, TemplateUtils.getParamValues(category.getId()));
		} else if (TOP.equals(category.getFatherCategory().getName())) {
			return findEpisodeTop(category, TemplateUtils.getParamValues(category.getId()));
		} else {
			throw new TechnicalException(category.getFatherCategory().getId() + " unknow");
		}
	}

	static void requireApiKey() {
		if (YoutubeConf.resolveApiKey() == null) {
			throw new TechnicalException(
					"YouTube Data API v3 key is missing. Set the 'HABITV_YOUTUBE_API_KEY' environment variable"
							+ " or the 'habitv.youtube.apiKey' system property before starting habitv."
							+ " You can also set the YouTube API key in habitv configuration and restart the application."
							+ " Create a key at https://console.cloud.google.com/ (enable YouTube Data API v3).");
		}
	}

	private Set<EpisodeDTO> findEpisodeTop(CategoryDTO category, Map<String, String> params) {
		String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&order=viewCount&type=video";
		url = addParam(url, params, KEY, YoutubeConf.resolveApiKey());
		String days = params.get(DAYS);
		if (days != null) {
			String publishedAfter = dateFormat.format(DateUtils.addDays(new Date(), -Integer.valueOf(days)));
			url = addParam(url, PUBLISHED_AFTER, publishedAfter);
		}
		url = addParam(url, params, MAX_RESULTS);
		url = addParam(url, params, QUERY);
		url = addParam(url, params, CHANNEL_ID);
		url = addParam(url, params, TOPIC_ID);
		url = addParam(url, params, VIDEO_CATEGORY_ID);
		return findEpisodesFromUrl(category, url);
	}

	private String addParam(String url, Map<String, String> params, String param) {
		return addParam(url, params, param, null);
	}

	private String addParam(String url, Map<String, String> params, String param, String defaultValue) {
		String paramValue = params.get(param);
		paramValue = paramValue == null ? defaultValue : paramValue;
		return addParam(url, param, paramValue);
	}

	private String addParam(String url, String param, String paramValue) {
		if (paramValue != null) {
			url = url + "&" + param + "=" + paramValue;
		}
		return url;
	}

	private Set<EpisodeDTO> findEpisodePlaylist(CategoryDTO category, Map<String, String> params) {
		String url = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet";
		url = addParam(url, params, KEY, YoutubeConf.resolveApiKey());
		url = addParam(url, params, PLAYLIST_ID);
		url = addParam(url, params, MAX_RESULTS, "50");
		return findEpisodesFromUrl(category, url);

	}

	private Set<EpisodeDTO> findEpisodesFromUrl(CategoryDTO category, String url) {
		final Set<EpisodeDTO> episodeList = new LinkedHashSet<>();

		JsonNode jsonNode;
		try {
			jsonNode = objectMapper.readTree(getInputStreamFromUrl(url));
			for (JsonNode item : jsonNode.get("items")) {
				JsonNode snippet = item.get("snippet");
				String id;
				if (snippet.has("resourceId")) {
					id = snippet.get("resourceId").get("videoId").asText();
				} else {
					id = item.get("id").get("videoId").asText();
				}
				String href = YoutubeConf.BASE_URL + "/watch?v=" + id;
				String name = snippet.get("title").textValue();
				episodeList.add(new EpisodeDTO(category, name, href));
			}
		} catch (IOException e) {
			throw new TechnicalException("youtube api request failed: " + sanitizeUrl(url), e);
		}
		return episodeList;
	}

	private String sanitizeUrl(String url) {
		return url.replaceAll("([?&]key=)[^&]+", "$1***");
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categoryList = new LinkedHashSet<>();
		
		CategoryDTO playListCat = TemplateUtils.buildCategoryTemplate(getName(), PLAYLIST, buildPlaylistTemplateID());
		playListCat.addSubCategory(buildFrance24LiveCat());
		categoryList.add(playListCat);
		
		CategoryDTO topTemplate = TemplateUtils.buildCategoryTemplate(getName(), TOP, buildTopTemplateID());
		topTemplate.addSubCategory(buildTop100AllTimes());
		categoryList.add(topTemplate);
		
		return categoryList;
	}

	private String buildTopTemplateID() {
		//@formatter:off
		TemplateIdBuilder templateIdBuilder = new TemplateIdBuilder()
				.addTemplateParam(MAX_RESULTS, "Taille", "10")
				.addTemplateParam(DAYS,"Nombre de jours", "30")
				.addTemplateParam(QUERY, "Mots clés", "")
				.addTemplateParam(CHANNEL_ID, "Identifiant Chaîne", null)
				.addTemplateParam(TOPIC_ID, "Topic", null)
				.addTemplateParam(VIDEO_CATEGORY_ID, "Identifiant de catégorie", null)
				.addComment("Saisissez le détail du top");
		//@formatter:on
		return templateIdBuilder.buildID();
	}

	private String buildPlaylistTemplateID() {
		//@formatter:off
		TemplateIdBuilder templateIdBuilder = new TemplateIdBuilder()
				.addTemplateParam(PLAYLIST_ID, "Identifiant", null)
				.addTemplateParam(MAX_RESULTS, "Taille", "10")
				.addComment("Saisissez le détail de la playlist");
		//@formatter:on
		return templateIdBuilder.buildID();
	}

	private CategoryDTO buildTop100AllTimes() {
		return TemplateUtils.buildSampleCat(getName(), "Top 50 All Times", ImmutableMap.of(DAYS, "36500", MAX_RESULTS, "50"));
	}

	private CategoryDTO buildFrance24LiveCat() {
		return TemplateUtils.buildSampleCat(getName(), "France24 Live EN", ImmutableMap.of(PLAYLIST_ID, "PLCUKIeZnrIUkh8TuvqH-uEdE5JHZWtk7x"));
	}

}
