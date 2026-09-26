package com.dabi.habitv.provider.arte;

import java.util.HashSet;
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
			final Set<String> zoneTitlesOnPage = new HashSet<>();
			for (final JsonNode zone : ArteEmacJson.emacZonesNode(pageRoot)) {
				if (ArteContentClassifier.shouldSkipZone(zone)) {
					continue;
				}
				final CategoryDTO zoneCategory = buildZoneCategory(languageCode, page.getCode(), zone, zoneTitlesOnPage);
				if (zoneCategory != null) {
					pageCategory.addSubCategory(zoneCategory);
				}
			}
		} catch (final RuntimeException e) {
			logCatalogSkip(languageCode, page.getCode(), null, page.getSourceUrl(), e);
		}
		return pageCategory;
	}

	private CategoryDTO buildZoneCategory(final String languageCode, final String pageCode, final JsonNode zone,
			final Set<String> zoneTitlesOnPage) {
		final String zoneId = zone.path("id").asText(null);
		final String zoneCode = zone.path("code").asText(null);
		final String zoneKey = StringUtils.isNotEmpty(zoneId) ? zoneId : zoneCode;
		if (StringUtils.isEmpty(zoneKey)) {
			return null;
		}
		final String zoneTitle = uniqueZoneTitle(zone.path("title").asText(zoneKey), zoneKey, zoneTitlesOnPage);
		final CategoryDTO zoneCategory = new CategoryDTO(ArteConf.NAME, zoneTitle,
				ArteCategoryId.forZone(languageCode, pageCode, zoneKey), ArteConf.EXTENSION);
		zoneCategory.setDownloadable(true);
		boolean hasCollectionChild = false;
		boolean hasPlayableItem = false;
		hasPlayableItem = scanTeasersForPlayable(zone.path("content").path("data")) || hasPlayableItem;
		hasCollectionChild = addCollectionChildren(languageCode, zoneCategory, zone.path("content").path("data"))
				|| hasCollectionChild;
		final DeferredLinkScan deferredLink = scanDeferredLinkContent(languageCode, zoneCategory, zone);
		hasPlayableItem = deferredLink.hasPlayable || hasPlayableItem;
		hasCollectionChild = deferredLink.hasCollections || hasCollectionChild;
		hasCollectionChild = discoverCollectionsInPaginatedZone(languageCode, pageCode, zoneCategory, zone)
				|| hasCollectionChild;
		if (!hasPlayableItem && !hasCollectionChild && !hasDeferredZoneContent(zone)) {
			return null;
		}
		if (hasCollectionChild && !hasPlayableItem && !hasDeferredZoneContent(zone)) {
			zoneCategory.setDownloadable(false);
		}
		return zoneCategory;
	}

	private boolean hasDeferredZoneContent(final JsonNode zone) {
		if (hasEmacZoneLink(zone)) {
			return true;
		}
		final JsonNode pagination = zone.path("content").path("pagination");
		if (pagination.isMissingNode() || pagination.isNull()) {
			return false;
		}
		final String nextUrl = pagination.path("links").path("next").asText(null);
		if (StringUtils.isNotEmpty(nextUrl) && ArteRequestUrls.isTrustedCatalogueFetchUrl(nextUrl)) {
			return true;
		}
		return pagination.path("pages").asInt(1) > 1 && StringUtils.isNotEmpty(zone.path("code").asText(null));
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
				addZoneEpisodes(category, episodes, languageCode, collectionId, zone);
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
		if (!ArteRequestUrls.isTrustedEmacApiUrl(linkUrl)) {
			return;
		}
		try {
			final JsonNode linked = ArteEmacJson.parseTree(transport.get(linkUrl), linkUrl);
			final String zoneTitle = zone.path("title").asText(null);
			final JsonNode linkedZones = ArteEmacJson.emacZonesNode(linked);
			if (linkedZones.isArray() && linkedZones.size() > 0) {
				for (final JsonNode linkedZone : linkedZones) {
					addZoneEpisodes(category, episodes, languageCode, null, linkedZone);
				}
			} else {
				addEpisodesFromDataNode(category, episodes, ArteEmacJson.emacDataNode(linked), languageCode, zoneTitle);
				loadZonePagination(category, episodes, languageCode, null, linked, zoneTitle,
						ArteEmacJson.zonePagination(linked));
			}
		} catch (final RuntimeException e) {
			logCatalogSkip(languageCode, null, zone.path("id").asText(null), linkUrl, e);
		}
	}

	private void loadZonePagination(final CategoryDTO category, final Set<EpisodeDTO> episodes, final String languageCode,
			final String pageCode, final JsonNode zone, final String zoneTitle, final JsonNode pagination) {
		if (pagination.isMissingNode() || pagination.isNull()) {
			return;
		}
		final int linkPages = followPaginationLinks(category, episodes, languageCode, zoneTitle, pagination);
		if (linkPages < 0) {
			return;
		}
		final String zoneCode = zone.path("code").asText(null);
		if (StringUtils.isEmpty(zoneCode) || !pagination.has("pages")) {
			return;
		}
		final int pages = Math.min(pagination.path("pages").asInt(1), ArteConf.MAX_PAGINATION_REQUESTS);
		for (int pageNumber = Math.max(2, linkPages + 1); pageNumber <= pages; pageNumber++) {
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

	/**
	 * @return highest page index reached via {@code links.next} (1 = inline page
	 *         only), or -1 when the full chain was consumed with no remaining pages
	 */
	private int followPaginationLinks(final CategoryDTO category, final Set<EpisodeDTO> episodes,
			final String languageCode, final String zoneTitle, final JsonNode pagination) {
		String nextUrl = pagination.path("links").path("next").asText(null);
		if (StringUtils.isEmpty(nextUrl) || !ArteRequestUrls.isTrustedCatalogueFetchUrl(nextUrl)) {
			return 0;
		}
		int fetched = 1;
		while (!StringUtils.isEmpty(nextUrl) && fetched < ArteConf.MAX_PAGINATION_REQUESTS) {
			final JsonNode zoneRoot;
			try {
				zoneRoot = ArteEmacJson.parseTree(transport.get(nextUrl), nextUrl);
			} catch (final TechnicalException e) {
				return fetched;
			}
			fetched++;
			addEpisodesFromDataNode(category, episodes, ArteEmacJson.emacDataNode(zoneRoot), languageCode, zoneTitle);
			nextUrl = ArteEmacJson.zonePagination(zoneRoot).path("links").path("next").asText(null);
			if (StringUtils.isNotEmpty(nextUrl) && !ArteRequestUrls.isTrustedCatalogueFetchUrl(nextUrl)) {
				return fetched;
			}
		}
		if (StringUtils.isEmpty(nextUrl)) {
			return -1;
		}
		return fetched;
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

	private boolean hasEmacZoneLink(final JsonNode zone) {
		final String linkUrl = resolveUrl(zone.path("link").path("url").asText(null));
		return ArteRequestUrls.isTrustedEmacApiUrl(linkUrl);
	}

	private String buildLegacyZoneUrl(final String languageCode, final String zoneCode, final String pageCode,
			final int pageNumber) {
		final String pageId = StringUtils.isEmpty(pageCode) ? zoneCode : pageCode;
		return ArteConf.EMAC_API_BASE + "/" + languageCode + "/web/zones/" + zoneCode + "/content?page=" + pageNumber
				+ "&pageId=" + pageId + "&authorizedCountry=" + ArteConf.AUTHORIZED_COUNTRY;
	}

	private String resolveUrl(final String url) {
		return ArteCatalogDiscovery.resolvePublicSiteUrl(url);
	}

	private static String uniqueZoneTitle(final String title, final String zoneKey, final Set<String> seenTitles) {
		if (seenTitles.add(title)) {
			return title;
		}
		return title + " (" + zoneKey + ")";
	}

	private boolean scanTeasersForPlayable(final JsonNode data) {
		if (!data.isArray()) {
			return false;
		}
		for (final JsonNode item : data) {
			final String resolvedUrl = ArteCatalogDiscovery.resolvePublicSiteUrl(item.path("url").asText(null));
			if (ArteContentClassifier.classifyTeaser(item, resolvedUrl) == ContentKind.PLAYABLE_SHOW) {
				return true;
			}
		}
		return false;
	}

	private boolean addCollectionChildren(final String languageCode, final CategoryDTO zoneCategory, final JsonNode data) {
		if (!data.isArray()) {
			return false;
		}
		boolean added = false;
		for (final JsonNode item : data) {
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
			added = true;
		}
		return added;
	}

	private DeferredLinkScan scanDeferredLinkContent(final String languageCode, final CategoryDTO zoneCategory,
			final JsonNode zone) {
		final JsonNode linked = fetchTrustedZoneLink(zone);
		if (linked == null) {
			return DeferredLinkScan.NONE;
		}
		boolean hasPlayable = false;
		boolean hasCollections = false;
		final JsonNode linkedZones = ArteEmacJson.emacZonesNode(linked);
		if (linkedZones.isArray() && linkedZones.size() > 0) {
			for (final JsonNode linkedZone : linkedZones) {
				hasPlayable = scanTeasersForPlayable(linkedZone.path("content").path("data")) || hasPlayable;
				hasCollections = addCollectionChildren(languageCode, zoneCategory, linkedZone.path("content").path("data"))
						|| hasCollections;
			}
		} else {
			hasPlayable = scanTeasersForPlayable(ArteEmacJson.emacDataNode(linked));
			hasCollections = addCollectionChildren(languageCode, zoneCategory, ArteEmacJson.emacDataNode(linked));
		}
		return new DeferredLinkScan(hasPlayable, hasCollections);
	}

	private boolean discoverCollectionsInPaginatedZone(final String languageCode, final String pageCode,
			final CategoryDTO zoneCategory, final JsonNode zone) {
		final JsonNode pagination = zone.path("content").path("pagination");
		if (pagination.isMissingNode() || pagination.path("pages").asInt(1) <= 1) {
			return false;
		}
		boolean added = false;
		String nextUrl = pagination.path("links").path("next").asText(null);
		int pageNumber = 2;
		final int pages = Math.min(pagination.path("pages").asInt(1), ArteConf.MAX_PAGINATION_REQUESTS);
		while (pageNumber <= pages) {
			JsonNode pageRoot = null;
			if (StringUtils.isNotEmpty(nextUrl) && ArteRequestUrls.isTrustedEmacApiUrl(nextUrl)) {
				try {
					pageRoot = ArteEmacJson.parseTree(transport.get(nextUrl), nextUrl);
					nextUrl = ArteEmacJson.zonePagination(pageRoot).path("links").path("next").asText(null);
				} catch (final RuntimeException e) {
					nextUrl = null;
				}
			}
			if (pageRoot == null) {
				final String zoneCode = zone.path("code").asText(null);
				if (StringUtils.isEmpty(zoneCode)) {
					break;
				}
				final String zoneUrl = buildLegacyZoneUrl(languageCode, zoneCode, pageCode, pageNumber);
				try {
					pageRoot = ArteEmacJson.parseTree(transport.get(zoneUrl), zoneUrl);
				} catch (final RuntimeException e) {
					break;
				}
				nextUrl = null;
			}
			added = addCollectionChildren(languageCode, zoneCategory, ArteEmacJson.emacDataNode(pageRoot)) || added;
			pageNumber++;
		}
		return added;
	}

	private static final class DeferredLinkScan {
		private static final DeferredLinkScan NONE = new DeferredLinkScan(false, false);

		private final boolean hasPlayable;
		private final boolean hasCollections;

		private DeferredLinkScan(final boolean hasPlayable, final boolean hasCollections) {
			this.hasPlayable = hasPlayable;
			this.hasCollections = hasCollections;
		}
	}

	private JsonNode fetchTrustedZoneLink(final JsonNode zone) {
		final String linkUrl = resolveUrl(zone.path("link").path("url").asText(null));
		if (!ArteRequestUrls.isTrustedEmacApiUrl(linkUrl)) {
			return null;
		}
		try {
			return ArteEmacJson.parseTree(transport.get(linkUrl), linkUrl);
		} catch (final RuntimeException e) {
			return null;
		}
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
