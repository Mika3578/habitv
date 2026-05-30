package com.dabi.habitv.plugin.youtube;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

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

	private static final Logger LOG = Logger.getLogger(YoutubePluginManager.class);

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
		if (category == null) {
			logSkip(null, "category is null");
			return new LinkedHashSet<>();
		}
		if (category.isTemplate()) {
			logSkip(category, "template category");
			return new LinkedHashSet<>();
		}
		if (!category.isDownloadable()) {
			logSkip(category, "non-downloadable category");
			return new LinkedHashSet<>();
		}
		if (category.getFatherCategory() == null) {
			logSkip(category, "father category is null");
			return new LinkedHashSet<>();
		}
		final Map<String, String> params = TemplateUtils.getParamValues(category.getId());
		if (params.containsKey(PLAYLIST_ID) || hasAncestorNamed(category, PLAYLIST)) {
			return findEpisodePlaylist(category, params);
		}
		if (hasAncestorNamed(category, TOP)) {
			return findEpisodeTop(category, params);
		}
		logSkip(category, "unknown category hierarchy");
		return new LinkedHashSet<>();
	}

	private boolean hasAncestorNamed(final CategoryDTO category, final String expectedName) {
		CategoryDTO current = category.getFatherCategory();
		while (current != null) {
			if (expectedName.equals(current.getName())) {
				return true;
			}
			current = current.getFatherCategory();
		}
		return false;
	}

	private void logSkip(final CategoryDTO category, final String reason) {
		final String categoryName = category == null ? "n/a" : category.getName();
		final String fatherName = category == null || category.getFatherCategory() == null ? "n/a"
				: category.getFatherCategory().getName();
		LOG.info("provider=YouTube, category=" + categoryName + ", father=" + fatherName + ", reason=" + reason);
	}

	private Set<EpisodeDTO> findEpisodeTop(CategoryDTO category, Map<String, String> params) {
		final String apiKey = YoutubeConf.resolveApiKey();
		if (apiKey == null) {
			logSkip(category, YoutubeConf.apiKeySkipReason());
			return new LinkedHashSet<>();
		}
		final String days = params.get(DAYS);
		final String publishedAfter = YoutubeDataApiSupport.resolvePublishedAfterForSearch(days, dateFormat, new Date());
		if (shouldUseMostPopularChart(params, publishedAfter)) {
			return findEpisodeMostPopular(category, params, apiKey);
		}
		return findEpisodeTopFromSearch(category, params, apiKey, days, publishedAfter);
	}

	private boolean shouldUseMostPopularChart(final Map<String, String> params, final String publishedAfter) {
		if (publishedAfter != null) {
			return false;
		}
		return StringUtils.isBlank(params.get(QUERY))
				&& StringUtils.isBlank(params.get(CHANNEL_ID))
				&& StringUtils.isBlank(params.get(TOPIC_ID))
				&& StringUtils.isBlank(params.get(VIDEO_CATEGORY_ID));
	}

	private Set<EpisodeDTO> findEpisodeMostPopular(final CategoryDTO category, final Map<String, String> params,
			final String apiKey) {
		String url = "https://www.googleapis.com/youtube/v3/videos?part=snippet&chart="
				+ YoutubeDataApiSupport.CHART_MOST_POPULAR;
		url = YoutubeDataApiSupport.appendApiKeyParam(url, apiKey);
		url = addParam(url, params, MAX_RESULTS, "50");
		return findEpisodesFromUrl(category, url);
	}

	private Set<EpisodeDTO> findEpisodeTopFromSearch(final CategoryDTO category, final Map<String, String> params,
			final String apiKey, final String days, final String publishedAfter) {
		String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&order=viewCount&type=video";
		url = YoutubeDataApiSupport.appendApiKeyParam(url, apiKey);
		if (publishedAfter != null) {
			url = addParam(url, PUBLISHED_AFTER, publishedAfter);
		} else if (days != null && !days.trim().isEmpty()) {
			logYoutubeApiDiagnostic(category, "search", "skipped",
					"publishedAfter omitted (all-time window; days=" + days.trim() + ")", url);
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
		final String playlistId = params.get(PLAYLIST_ID);
		if (StringUtils.isBlank(playlistId)) {
			logSkip(category, "Skipping YouTube playlist category because playlistId is missing.");
			return new LinkedHashSet<>();
		}
		final String apiKey = YoutubeConf.resolveApiKey();
		if (apiKey == null) {
			logSkip(category, YoutubeConf.apiKeySkipReason());
			return new LinkedHashSet<>();
		}
		String url = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet";
		url = YoutubeDataApiSupport.appendApiKeyParam(url, apiKey);
		url = addParam(url, params, PLAYLIST_ID);
		url = addParam(url, params, MAX_RESULTS, "50");
		return findEpisodesFromUrl(category, url);

	}

	private Set<EpisodeDTO> findEpisodesFromUrl(CategoryDTO category, String url) {
		final Set<EpisodeDTO> episodeList = new LinkedHashSet<>();
		if (YoutubeDataApiSupport.isDataApiUrl(url) && !YoutubeDataApiSupport.urlHasApiKey(url)) {
			logSkip(category, YoutubeDataApiSupport.MISSING_API_KEY_MESSAGE);
			return episodeList;
		}

		JsonNode jsonNode;
		try {
			jsonNode = objectMapper.readTree(getInputStreamFromUrl(url));
			final JsonNode items = jsonNode.get("items");
			if (items == null || !items.iterator().hasNext()) {
				if (YoutubeDataApiSupport.isSearchApiUrl(url)) {
					logYoutubeApiDiagnostic(category, "search", "empty", YoutubeDataApiSupport.SEARCH_EMPTY_MESSAGE, url);
				} else if (YoutubeDataApiSupport.isVideosApiUrl(url)) {
					logYoutubeApiDiagnostic(category, "videos", "empty", YoutubeDataApiSupport.VIDEOS_EMPTY_MESSAGE, url);
				}
				return episodeList;
			}
			for (JsonNode item : items) {
				JsonNode snippet = item.get("snippet");
				final String id = resolveVideoId(item, snippet);
				if (id == null) {
					continue;
				}
				String href = YoutubeConf.BASE_URL + "/watch?v=" + id;
				String name = snippet.get("title").textValue();
				episodeList.add(new EpisodeDTO(category, name, href));
			}
		} catch (TechnicalException e) {
			final String summary = YoutubeDataApiSupport.summarizeRecoverableApiError(e);
			if (summary != null) {
				logApiFailure(category, url, summary);
				return episodeList;
			}
			throw new TechnicalException(YoutubeDataApiSupport.buildSafeApiFailureMessage(url), e);
		} catch (IOException e) {
			final String summary = YoutubeDataApiSupport.summarizeRecoverableApiError(e);
			if (summary != null) {
				logApiFailure(category, url, summary);
				return episodeList;
			}
			throw new TechnicalException(YoutubeDataApiSupport.buildSafeApiFailureMessage(url), e);
		}
		return episodeList;
	}

	private void logApiFailure(final CategoryDTO category, final String url, final String reason) {
		final String categoryName = category == null ? "n/a" : category.getName();
		final String fatherName = category == null || category.getFatherCategory() == null ? "n/a"
				: category.getFatherCategory().getName();
		LOG.warn("provider=YouTube, category=" + categoryName + ", father=" + fatherName + ", reason=" + reason
				+ ", url=" + YoutubeDataApiSupport.redactUrl(url));
	}

	private String resolveVideoId(final JsonNode item, final JsonNode snippet) {
		if (snippet != null && snippet.has("resourceId")) {
			return snippet.get("resourceId").get("videoId").asText();
		}
		final JsonNode idNode = item.get("id");
		if (idNode == null) {
			return null;
		}
		if (idNode.isTextual()) {
			return idNode.asText();
		}
		if (idNode.has("videoId")) {
			return idNode.get("videoId").asText();
		}
		return null;
	}

	private void logYoutubeApiDiagnostic(final CategoryDTO category, final String endpoint, final String status,
			final String reason, final String url) {
		final String categoryName = category == null ? "n/a" : category.getName();
		LOG.info("provider=YouTube, category=" + categoryName + ", endpoint=" + endpoint + ", status=" + status
				+ ", reason=" + reason + ", url=" + YoutubeDataApiSupport.redactUrl(url));
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
		return TemplateUtils.buildSampleCat(getName(), "Top 50 All Times", ImmutableMap.of(MAX_RESULTS, "50"));
	}

	private CategoryDTO buildFrance24LiveCat() {
		return TemplateUtils.buildSampleCat(getName(), "France24 Live EN", ImmutableMap.of(PLAYLIST_ID, "PLCUKIeZnrIUkh8TuvqH-uEdE5JHZWtk7x"));
	}

}
