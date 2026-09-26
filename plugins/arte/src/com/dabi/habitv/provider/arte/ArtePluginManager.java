package com.dabi.habitv.provider.arte;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.api.PluginProviderDownloaderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.dabi.habitv.framework.plugin.utils.DownloadUtils;
import com.dabi.habitv.provider.arte.ArteCatalogDiscovery.ArteEmacTransport;
import com.dabi.habitv.provider.arte.ArteCatalogDiscovery.ArteLanguage;
import com.dabi.habitv.provider.arte.ArteCatalogDiscovery.ArtePageRef;
import com.dabi.habitv.provider.arte.ArteCategoryId.Kind;
import com.dabi.habitv.provider.arte.ArteContentClassifier.ContentKind;
import com.fasterxml.jackson.databind.JsonNode;

public class ArtePluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface { // NO_UCD

	private final ArteCatalogDiscovery catalogDiscovery;

	private final ArteEmacTransport transport;

	public ArtePluginManager() {
		this.transport = new ArteEmacTransport() {
			@Override
			public String get(final String url) {
				return ArtePluginManager.this.getUrlContent(url);
			}
		};
		this.catalogDiscovery = new ArteCatalogDiscovery(this.transport);
	}

	ArtePluginManager(final ArteCatalogDiscovery catalogDiscovery, final ArteEmacTransport transport) {
		this.catalogDiscovery = catalogDiscovery;
		this.transport = transport;
	}

	/** @deprecated use {@link ArteEmacJson#emacZonesNode(JsonNode)} */
	@Deprecated
	static JsonNode emacZonesNode(final JsonNode pageRoot) {
		return ArteEmacJson.emacZonesNode(pageRoot);
	}

	/** @deprecated use {@link ArteEmacJson#emacDataNode(JsonNode)} */
	@Deprecated
	static JsonNode emacDataNode(final JsonNode zoneRoot) {
		return ArteEmacJson.emacDataNode(zoneRoot);
	}

	@Override
	public String getName() {
		return ArteConf.NAME;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final ArteCategoryId categoryId = category == null ? null : ArteCategoryId.parse(category.getId());
		if (categoryId == null) {
			return new LinkedHashSet<>();
		}
		try {
			switch (categoryId.getKind()) {
			case ZONE:
			case LEGACY_ZONE:
				return loadEpisodesFromZone(category, categoryId);
			case COLLECTION:
				return loadEpisodesFromCollection(category, categoryId);
			case LEGACY_PAGE:
				return loadEpisodesFromPage(category, categoryId.getLanguageCode(), categoryId.getPageCode());
			default:
				return new LinkedHashSet<>();
			}
		} catch (final TechnicalException e) {
			logEpisodeFailure(categoryId, category.getId(), e);
			return new LinkedHashSet<>();
		}
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categories = new LinkedHashSet<>();
		try {
			for (final ArteLanguage language : catalogDiscovery.discoverLanguages()) {
				final CategoryDTO languageCat = new CategoryDTO(ArteConf.NAME, language.getLabel(),
						ArteConf.HOME_URL + "/" + language.getCode() + "/", ArteConf.EXTENSION);
				languageCat.setDownloadable(false);
				for (final ArtePageRef page : catalogDiscovery.discoverPages(language.getCode())) {
					languageCat.addSubCategory(buildPageCategory(language.getCode(), page));
				}
				if (!languageCat.getSubCategories().isEmpty()) {
					categories.add(languageCat);
				}
			}
		} catch (final RuntimeException e) {
			getLog().warn("provider=arte discovery=emac-home strategy=catalogue-tree rootCause="
					+ e.getClass().getSimpleName() + " message=" + e.getMessage());
		}
		return categories;
	}

	private CategoryDTO buildPageCategory(final String languageCode, final ArtePageRef page) {
		final CategoryDTO pageCategory = new CategoryDTO(ArteConf.NAME, page.getTitle(),
				ArteCategoryId.legacyPage(languageCode, page.getCode()), ArteConf.EXTENSION);
		pageCategory.setDownloadable(false);
		try {
			JsonNode pageRoot = catalogDiscovery.getDiscoveredPageRoot(page.getSourceUrl());
			if (pageRoot == null) {
				pageRoot = ArteEmacJson.parseTree(transport.get(page.getSourceUrl()), page.getSourceUrl());
			}
			for (final JsonNode zone : ArteEmacJson.emacZonesNode(pageRoot)) {
				if (ArteContentClassifier.shouldSkipZone(zone)) {
					continue;
				}
				final CategoryDTO zoneCategory = buildZoneCategory(languageCode, page.getCode(), zone);
				if (zoneCategory != null) {
					pageCategory.addSubCategory(zoneCategory);
				}
			}
		} catch (final RuntimeException e) {
			logCatalogSkip(languageCode, page.getCode(), null, page.getSourceUrl(), e);
		}
		return pageCategory;
	}

	private CategoryDTO buildZoneCategory(final String languageCode, final String pageCode, final JsonNode zone) {
		final String zoneId = zone.path("id").asText(null);
		final String zoneCode = zone.path("code").asText(null);
		final String zoneKey = StringUtils.isNotEmpty(zoneId) ? zoneId : zoneCode;
		if (StringUtils.isEmpty(zoneKey)) {
			return null;
		}
		final String zoneTitle = zone.path("title").asText(zoneKey);
		final CategoryDTO zoneCategory = new CategoryDTO(ArteConf.NAME, zoneTitle,
				ArteCategoryId.forZone(languageCode, pageCode, zoneKey), ArteConf.EXTENSION);
		zoneCategory.setDownloadable(true);
		boolean hasCollectionChild = false;
		for (final JsonNode item : zone.path("content").path("data")) {
			final String resolvedUrl = resolveUrl(item.path("url").asText(null));
			if (ArteContentClassifier.classifyTeaser(item, resolvedUrl) != ContentKind.COLLECTION) {
				continue;
			}
			final String collectionId = ArteContentClassifier.collectionId(item, resolvedUrl);
			if (StringUtils.isEmpty(collectionId)) {
				continue;
			}
			String title = item.path("title").asText(null);
			if (StringUtils.isEmpty(title)) {
				title = collectionId;
			}
			final CategoryDTO collectionCategory = new CategoryDTO(ArteConf.NAME, title,
					ArteCategoryId.forCollection(languageCode, collectionId), ArteConf.EXTENSION);
			collectionCategory.setDownloadable(true);
			zoneCategory.addSubCategory(collectionCategory);
			hasCollectionChild = true;
		}
		if (hasCollectionChild) {
			zoneCategory.setDownloadable(false);
		}
		return zoneCategory;
	}

	private Set<EpisodeDTO> loadEpisodesFromPage(final CategoryDTO category, final String languageCode,
			final String pageCode) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		final String pageUrl = ArteCatalogDiscovery.buildPageUrl(languageCode, pageCode);
		final JsonNode zones = ArteEmacJson.emacZonesNode(
				ArteEmacJson.parseTree(transport.get(pageUrl), pageUrl));
		if (!zones.isArray()) {
			return episodes;
		}
		for (final JsonNode zone : zones) {
			final String zoneKey = zone.path("id").asText(zone.path("code").asText(null));
			try {
				addZoneEpisodes(category, episodes, languageCode, pageCode, zone);
			} catch (final RuntimeException e) {
				logCatalogSkip(languageCode, pageCode, zoneKey, pageUrl, e);
			}
		}
		return episodes;
	}

	private Set<EpisodeDTO> loadEpisodesFromZone(final CategoryDTO category, final ArteCategoryId categoryId) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		final String languageCode = categoryId.getLanguageCode();
		final String pageCode = categoryId.getPageCode();
		final String zoneKey = categoryId.getZoneKey();
		final String pageUrl = ArteCatalogDiscovery.buildPageUrl(languageCode, pageCode);
		final JsonNode zones = ArteEmacJson.emacZonesNode(
				ArteEmacJson.parseTree(transport.get(pageUrl), pageUrl));
		for (final JsonNode zone : zones) {
			if (zoneMatches(zone, zoneKey)) {
				addZoneEpisodes(category, episodes, languageCode, pageCode, zone);
				return episodes;
			}
		}
		getLog().warn("provider=arte discovery=emac-page language=" + languageCode + " page=" + pageCode + " zone="
				+ zoneKey + " source=" + pageUrl + " rootCause=unknown-zone");
		return episodes;
	}

	private Set<EpisodeDTO> loadEpisodesFromCollection(final CategoryDTO category, final ArteCategoryId categoryId) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		final String languageCode = categoryId.getLanguageCode();
		final String collectionId = categoryId.getCollectionId();
		final String collectionUrl = ArteConf.EMAC_API_BASE + "/" + languageCode + "/web/collections/" + collectionId
				+ "/?authorizedCountry=" + ArteConf.AUTHORIZED_COUNTRY;
		final JsonNode collectionRoot = ArteEmacJson.parseTree(transport.get(collectionUrl), collectionUrl);
		for (final JsonNode zone : ArteEmacJson.emacZonesNode(collectionRoot)) {
			if (ArteContentClassifier.shouldSkipZone(zone)) {
				continue;
			}
			try {
				addZoneEpisodes(category, episodes, languageCode, "collection", zone);
			} catch (final RuntimeException e) {
				logCatalogSkip(languageCode, "collection", zone.path("id").asText(null), collectionUrl, e);
			}
		}
		return episodes;
	}

	private void addZoneEpisodes(final CategoryDTO category, final Set<EpisodeDTO> episodes, final String languageCode,
			final String pageCode, final JsonNode zone) {
		final JsonNode content = zone.path("content");
		final String zoneTitle = zone.path("title").asText(null);
		addEpisodesFromDataNode(category, episodes, content.path("data"), languageCode, zoneTitle);
		followZoneLink(category, episodes, languageCode, zone);
		loadZonePagination(category, episodes, languageCode, pageCode, zone, zoneTitle, content.path("pagination"));
	}

	private void followZoneLink(final CategoryDTO category, final Set<EpisodeDTO> episodes, final String languageCode,
			final JsonNode zone) {
		final JsonNode link = zone.path("link");
		if (!link.isObject() || StringUtils.isEmpty(link.path("url").asText(null))) {
			return;
		}
		final String linkUrl = resolveUrl(link.path("url").asText(null));
		if (!linkUrl.startsWith(ArteConf.EMAC_API_BASE)) {
			return;
		}
		try {
			final JsonNode linked = ArteEmacJson.parseTree(transport.get(linkUrl), linkUrl);
			addEpisodesFromDataNode(category, episodes, ArteEmacJson.emacDataNode(linked), languageCode,
					zone.path("title").asText(null));
		} catch (final RuntimeException e) {
			logCatalogSkip(languageCode, null, zone.path("id").asText(null), linkUrl, e);
		}
	}

	private void loadZonePagination(final CategoryDTO category, final Set<EpisodeDTO> episodes, final String languageCode,
			final String pageCode, final JsonNode zone, final String zoneTitle, final JsonNode pagination) {
		if (pagination.isMissingNode() || pagination.isNull()) {
			return;
		}
		if (followPaginationLinks(category, episodes, languageCode, zoneTitle, pagination)) {
			return;
		}
		final String zoneCode = zone.path("code").asText(null);
		if (StringUtils.isEmpty(zoneCode) || !pagination.has("pages")) {
			return;
		}
		final int pages = Math.min(pagination.path("pages").asInt(1), ArteConf.MAX_PAGINATION_REQUESTS);
		for (int pageNumber = 2; pageNumber <= pages; pageNumber++) {
			final String zoneUrl = buildLegacyZoneUrl(languageCode, zoneCode, pageCode, pageNumber);
			try {
				final JsonNode zoneRoot = ArteEmacJson.parseTree(transport.get(zoneUrl), zoneUrl);
				addEpisodesFromDataNode(category, episodes, ArteEmacJson.emacDataNode(zoneRoot), languageCode,
						zoneTitle);
			} catch (final TechnicalException e) {
				break;
			}
		}
	}

	private boolean followPaginationLinks(final CategoryDTO category, final Set<EpisodeDTO> episodes,
			final String languageCode, final String zoneTitle, final JsonNode pagination) {
		String nextUrl = pagination.path("links").path("next").asText(null);
		if (StringUtils.isEmpty(nextUrl) || !isSafePublicEmacUrl(nextUrl)) {
			return !StringUtils.isEmpty(pagination.path("links").path("next").asText(null));
		}
		int fetched = 1;
		while (!StringUtils.isEmpty(nextUrl) && fetched < ArteConf.MAX_PAGINATION_REQUESTS) {
			final JsonNode zoneRoot;
			try {
				zoneRoot = ArteEmacJson.parseTree(transport.get(nextUrl), nextUrl);
			} catch (final TechnicalException e) {
				break;
			}
			fetched++;
			addEpisodesFromDataNode(category, episodes, ArteEmacJson.emacDataNode(zoneRoot), languageCode, zoneTitle);
			nextUrl = ArteEmacJson.zonePagination(zoneRoot).path("links").path("next").asText(null);
			if (StringUtils.isNotEmpty(nextUrl) && !isSafePublicEmacUrl(nextUrl)) {
				break;
			}
		}
		return true;
	}

	private void addEpisodesFromDataNode(final CategoryDTO category, final Set<EpisodeDTO> episodes, final JsonNode data,
			final String languageCode, final String zoneTitle) {
		if (!data.isArray()) {
			return;
		}
		for (final JsonNode item : data) {
			final String resolvedUrl = resolveUrl(item.path("url").asText(null));
			if (!ArteContentClassifier.isPlayableShow(item, resolvedUrl)) {
				continue;
			}
			addEpisodeFromTeaser(category, episodes, item, languageCode, zoneTitle, resolvedUrl);
		}
	}

	private void addEpisodeFromTeaser(final CategoryDTO category, final Set<EpisodeDTO> episodes, final JsonNode item,
			final String languageCode, final String zoneTitle, final String url) {
		String title = item.path("title").asText(null);
		if (StringUtils.isEmpty(title)) {
			title = item.path("subtitle").asText(null);
		}
		if (StringUtils.isEmpty(title)) {
			return;
		}
		final EpisodeDTO episode = new EpisodeDTO(category, title, url);
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		metadata.setEpisodeTitle(title);
		metadata.setSourceUrl(url);
		metadata.setContentLanguage(languageCode);
		final String subtitle = item.path("subtitle").asText(null);
		metadata.setDescription(buildDescription(zoneTitle, title, subtitle));
		episode.setMetadata(metadata);
		episodes.add(episode);
	}

	private static String buildDescription(final String zoneTitle, final String title, final String subtitle) {
		final StringBuilder description = new StringBuilder();
		if (StringUtils.isNotEmpty(zoneTitle)) {
			description.append(zoneTitle);
		}
		if (StringUtils.isNotEmpty(subtitle) && !subtitle.equals(title)) {
			if (description.length() > 0) {
				description.append(" — ");
			}
			description.append(subtitle);
		}
		return description.length() == 0 ? null : description.toString();
	}

	private static boolean zoneMatches(final JsonNode zone, final String zoneKey) {
		return zoneKey.equals(zone.path("id").asText(null)) || zoneKey.equals(zone.path("code").asText(null));
	}

	private static boolean isSafePublicEmacUrl(final String url) {
		return StringUtils.isNotEmpty(url)
				&& (url.startsWith(ArteConf.EMAC_API_BASE + "/") || url.startsWith(ArteConf.HOME_URL));
	}

	private String buildLegacyZoneUrl(final String languageCode, final String zoneCode, final String pageCode,
			final int pageNumber) {
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

	private void logCatalogSkip(final String languageCode, final String pageCode, final String zoneKey,
			final String sourceUrl, final Exception e) {
		getLog().warn("provider=arte discovery=emac language=" + languageCode + " page=" + pageCode + " zone="
				+ zoneKey + " source=" + sourceUrl + " rootCause=" + e.getClass().getSimpleName() + " message="
				+ e.getMessage());
	}

	private void logEpisodeFailure(final ArteCategoryId categoryId, final String categoryIdentifier,
			final Exception e) {
		getLog().warn("provider=arte discovery=emac-episodes categoryId=" + categoryIdentifier + " language="
				+ categoryId.getLanguageCode() + " page=" + categoryId.getPageCode() + " zone="
				+ categoryId.getZoneKey() + " collection=" + categoryId.getCollectionId() + " rootCause="
				+ e.getClass().getSimpleName(), e);
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		return DownloadUtils.download(downloadParam, downloaders, "youtube");
	}

	@Override
	public DownloadableState canDownload(String downloadInput) {
		return downloadInput.contains("arte") ? DownloadableState.SPECIFIC : DownloadableState.IMPOSSIBLE;
	}

}
