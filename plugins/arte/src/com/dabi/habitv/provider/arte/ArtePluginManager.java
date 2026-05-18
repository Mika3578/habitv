package com.dabi.habitv.provider.arte;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.api.PluginProviderDownloaderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.dabi.habitv.framework.plugin.utils.DownloadUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ArtePluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface { // NO_UCD

	private static final Pattern EPISODE_URL_PATTERN = Pattern.compile(
			"https://www\\.arte\\.tv/[a-z]{2}/videos/\\d{6}-\\d{3}-[AF]/[^\"'\\s<>]+");

	private static final String CATEGORY_ID_SEPARATOR = ":";

	private static final int MAX_ZONE_PAGES = 25;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String getName() {
		return ArteConf.NAME;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final String[] categoryParts = parseCategoryId(category.getId());
		if (categoryParts == null) {
			return new LinkedHashSet<>();
		}
		try {
			return loadEpisodesFromPage(category, categoryParts[0], categoryParts[1]);
		} catch (final TechnicalException e) {
			return new LinkedHashSet<>();
		}
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categories = new LinkedHashSet<>();

		for (final String[] language : ArteConf.LANGUAGES) {
			final String langCode = language[0];
			final String langLabel = language[1];
			final String homeUrl = ArteConf.HOME_URL + "/" + langCode + "/";
			final CategoryDTO languageCat = new CategoryDTO(ArteConf.NAME, langLabel, homeUrl, ArteConf.EXTENSION);
			languageCat.setDownloadable(false);
			for (final String[] pageCode : ArteConf.PAGE_CODES) {
				final String categoryId = buildCategoryId(langCode, pageCode[0]);
				final CategoryDTO mainCategory = new CategoryDTO(ArteConf.NAME, pageCode[1], categoryId, ArteConf.EXTENSION);
				mainCategory.setDownloadable(true);
				languageCat.addSubCategory(mainCategory);
			}
			categories.add(languageCat);
		}

		return categories;
	}

	private Set<EpisodeDTO> loadEpisodesFromPage(final CategoryDTO category, final String languageCode, final String pageCode) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		final String pageUrl = buildPageUrl(languageCode, pageCode);
		final JsonNode pageRoot = parseJson(getUrlContent(pageUrl), pageUrl);
		final JsonNode zones = pageRoot.path("value").path("zones");
		if (!zones.isArray()) {
			return episodes;
		}
		for (final JsonNode zone : zones) {
			final JsonNode content = zone.path("content");
			addEpisodesFromDataNode(category, episodes, content.path("data"));
			loadZonePagination(category, episodes, languageCode, pageCode, zone.path("code").asText(), content.path("pagination"));
		}
		return episodes;
	}

	private void loadZonePagination(final CategoryDTO category, final Set<EpisodeDTO> episodes, final String languageCode,
			final String pageCode, final String zoneCode, final JsonNode pagination) {
		if (StringUtils.isEmpty(zoneCode) || !pagination.has("pages")) {
			return;
		}
		final int pages = Math.min(pagination.path("pages").asInt(1), MAX_ZONE_PAGES);
		for (int pageNumber = 2; pageNumber <= pages; pageNumber++) {
			final String zoneUrl = buildZoneUrl(languageCode, zoneCode, pageCode, pageNumber);
			try {
				final JsonNode zoneRoot = parseJson(getUrlContent(zoneUrl), zoneUrl);
				addEpisodesFromDataNode(category, episodes, zoneRoot.path("value").path("data"));
			} catch (final TechnicalException e) {
				// EMAC sometimes reports extra pages that return HTTP 400; keep already fetched episodes.
				break;
			}
		}
	}

	private void addEpisodesFromDataNode(final CategoryDTO category, final Set<EpisodeDTO> episodes, final JsonNode data) {
		if (!data.isArray()) {
			return;
		}
		final Map<String, EpisodeDTO> episodeByUrl = new LinkedHashMap<>();
		for (final EpisodeDTO episode : episodes) {
			episodeByUrl.put(episode.getId(), episode);
		}
		for (final JsonNode item : data) {
			addEpisodeFromTeaser(category, episodeByUrl, item);
		}
		episodes.clear();
		episodes.addAll(episodeByUrl.values());
	}

	private void addEpisodeFromTeaser(final CategoryDTO category, final Map<String, EpisodeDTO> episodeByUrl, final JsonNode item) {
		final String url = resolveUrl(item.path("url").asText(null));
		if (StringUtils.isEmpty(url) || !EPISODE_URL_PATTERN.matcher(url).find()) {
			return;
		}
		String title = item.path("title").asText(null);
		if (StringUtils.isEmpty(title)) {
			title = item.path("subtitle").asText(null);
		}
		if (StringUtils.isEmpty(title)) {
			return;
		}
		if (!episodeByUrl.containsKey(url)) {
			episodeByUrl.put(url, new EpisodeDTO(category, title, url));
		}
	}

	private String buildCategoryId(final String languageCode, final String pageCode) {
		return languageCode + CATEGORY_ID_SEPARATOR + pageCode;
	}

	private String[] parseCategoryId(final String categoryId) {
		if (StringUtils.isEmpty(categoryId)) {
			return null;
		}
		final String[] parts = categoryId.split(CATEGORY_ID_SEPARATOR);
		if (parts.length != 2 || StringUtils.isEmpty(parts[0]) || StringUtils.isEmpty(parts[1])) {
			return null;
		}
		return parts;
	}

	private String buildPageUrl(final String languageCode, final String pageCode) {
		return ArteConf.EMAC_API_BASE + "/" + languageCode + "/web/pages/" + pageCode + "/?authorizedCountry="
				+ ArteConf.AUTHORIZED_COUNTRY;
	}

	private String buildZoneUrl(final String languageCode, final String zoneCode, final String pageCode, final int pageNumber) {
		return ArteConf.EMAC_API_BASE + "/" + languageCode + "/web/zones/" + zoneCode + "/content?page=" + pageNumber
				+ "&pageId=" + pageCode + "&authorizedCountry=" + ArteConf.AUTHORIZED_COUNTRY;
	}

	private String resolveUrl(final String url) {
		if (StringUtils.isEmpty(url)) {
			return url;
		}
		if (url.startsWith("http://") || url.startsWith("https://")) {
			return url;
		}
		if (url.startsWith("/")) {
			return ArteConf.HOME_URL + url;
		}
		return ArteConf.HOME_URL + "/" + url;
	}

	private JsonNode parseJson(final String json, final String sourceUrl) {
		try {
			return objectMapper.readTree(json);
		} catch (final IOException e) {
			throw new TechnicalException("Cannot parse Arte EMAC response from " + sourceUrl, e);
		}
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders) throws DownloadFailedException {
		return DownloadUtils.download(downloadParam, downloaders, "youtube");
	}

	@Override
	public DownloadableState canDownload(String downloadInput) {
		return downloadInput.contains("arte") ? DownloadableState.SPECIFIC : DownloadableState.IMPOSSIBLE;
	}

}
