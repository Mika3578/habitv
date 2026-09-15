package com.dabi.habitv.provider.novo19;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.provider.novo19.dto.Novo19Rail;
import com.dabi.habitv.provider.novo19.dto.Novo19Tile;

final class Novo19PathRules {

	private Novo19PathRules() {
	}

	static boolean isExcludedPublicPath(final String href) {
		if (StringUtils.isEmpty(href)) {
			return true;
		}
		final String path = normalizePath(href);
		if (path.startsWith("/mes-videos")) {
			return true;
		}
		if (path.equals("/player/novo19") || path.startsWith("/player/novo19/")) {
			return true;
		}
		if (path.contains("/preferences/")) {
			return true;
		}
		return false;
	}

	static boolean isSupportedTileType(final String type) {
		if (StringUtils.isEmpty(type)) {
			return false;
		}
		return "COLLECTION".equals(type) || "SERIE".equals(type) || "VOD".equals(type) || "EPISODE".equals(type)
				|| "PODCAST".equals(type) || "AUDIO".equals(type);
	}

	static boolean isEpisodeTileType(final String type) {
		return "EPISODE".equals(type) || "VOD".equals(type);
	}

	static boolean isContentEpisodeTileType(final String type) {
		return "EPISODE".equals(type) || "PODCAST".equals(type) || "AUDIO".equals(type);
	}

	static boolean isProgramTileType(final String type) {
		return "SERIE".equals(type) || "VOD".equals(type) || "PODCAST".equals(type);
	}

	static boolean isCollectionProgramTile(final Novo19Tile tile) {
		return tile != null && "COLLECTION".equals(tile.getType()) && !StringUtils.isEmpty(tile.getHref())
				&& tile.getHref().startsWith("/details/");
	}

	static boolean isProgramDetailTile(final Novo19Tile tile) {
		if (tile == null || StringUtils.isEmpty(tile.getHref()) || isExcludedPublicPath(tile.getHref())) {
			return false;
		}
		if (!tile.getHref().startsWith("/details/")) {
			return false;
		}
		return isProgramTileType(tile.getType()) || isCollectionProgramTile(tile);
	}

	static boolean isProgramDiscoverableTile(final Novo19Tile tile) {
		if (tile == null || isExcludedPublicPath(tile.getHref()) || isEditorialNavigationTile(tile)) {
			return false;
		}
		return isProgramTileType(tile.getType()) || isCollectionProgramTile(tile);
	}

	static boolean isCatalogueCarouselRail(final Novo19Rail rail) {
		return rail != null && !isRecommendationRail(rail) && StringUtils.isEmpty(rail.getTitle());
	}

	static boolean isInfoEditorialRail(final Novo19Rail rail) {
		return isEditorialBannerRail(rail) && railTitleContains(rail, "info");
	}

	static boolean isTalkEditorialRail(final Novo19Rail rail) {
		return isEditorialBannerRail(rail) && railTitleContains(rail, "talk");
	}

	static boolean isEditorialBannerRail(final Novo19Rail rail) {
		return rail != null && "BANNER".equals(rail.getType()) && !StringUtils.isEmpty(rail.getSrc());
	}

	static boolean isCuratedSelectionRailTitle(final String title) {
		if (StringUtils.isEmpty(title)) {
			return false;
		}
		final String normalized = title.trim().toLowerCase(Locale.FRENCH);
		return normalized.contains("sélection") || normalized.contains("selection");
	}

	static String editorialBucketForArtworkTile(final Novo19Tile tile) {
		if (!isCollectionProgramTile(tile) || StringUtils.isEmpty(tile.getTitle())) {
			return null;
		}
		final String label = tile.getTitle().trim();
		if (Novo19Conf.EDITORIAL_INFO.equalsIgnoreCase(label)) {
			return Novo19Conf.EDITORIAL_INFO;
		}
		if (Novo19Conf.EDITORIAL_TALK.equalsIgnoreCase(label)) {
			return Novo19Conf.EDITORIAL_TALK;
		}
		return null;
	}

	static boolean isEditorialNavigationTile(final Novo19Tile tile) {
		if (tile == null || StringUtils.isEmpty(tile.getHref())) {
			return false;
		}
		final String path = normalizePath(tile.getHref());
		return "/series".equals(path) || "/films".equals(path) || "/documentaires-et-magazines".equals(path)
				|| "/sport".equals(path) || "/divertissements".equals(path) || "/podcasts".equals(path)
				|| "/homepage".equals(path) || "/categories".equals(path) || "/recherche".equals(path);
	}

	static boolean isRecommendationRailSrc(final String railSrc) {
		if (StringUtils.isEmpty(railSrc)) {
			return false;
		}
		return railSrc.contains("/sections/reco/") || railSrc.endsWith("/reco/tiles")
				|| railSrc.contains("/sections/reco/tiles");
	}

	static boolean isRecommendationRailId(final String railId) {
		return !StringUtils.isEmpty(railId) && "reco".equalsIgnoreCase(railId.trim());
	}

	static boolean isRecommendationRailTitle(final String title) {
		if (StringUtils.isEmpty(title)) {
			return false;
		}
		final String normalized = title.trim().toLowerCase();
		return normalized.contains("recommend") || normalized.contains("recommand");
	}

	static boolean isRecommendationRail(final Novo19Rail rail) {
		if (rail == null) {
			return false;
		}
		return isRecommendationRailSrc(rail.getSrc()) || isRecommendationRailId(rail.getId())
				|| isRecommendationRailTitle(rail.getTitle());
	}

	static boolean isRecommendationBffPath(final String bffPath) {
		return isRecommendationRailSrc(bffPath);
	}

	static boolean isPodcastDetailRailSrc(final String railSrc) {
		return !StringUtils.isEmpty(railSrc) && railSrc.contains("asset-details-podcast");
	}

	static boolean isLiveReplayAsset(final String assetId) {
		if (StringUtils.isEmpty(assetId)) {
			return false;
		}
		return Novo19Conf.LIVE_ASSET_ID.equals(assetId) || assetId.startsWith("novo19_");
	}

	static boolean isGenericCatalogueSectionTitle(final String title) {
		if (StringUtils.isEmpty(title)) {
			return false;
		}
		return "catalogue".equalsIgnoreCase(title.trim()) || "catégories".equalsIgnoreCase(title.trim())
				|| "categories".equalsIgnoreCase(title.trim());
	}

	static boolean isDocumentariesSectionRail(final Novo19Rail rail) {
		return rail != null && Novo19Conf.SECTION_DOCUMENTARIES.equals(rail.getTitle());
	}

	static boolean isDocumentariesMasterCatalogRail(final Novo19Rail rail) {
		if (rail == null || StringUtils.isEmpty(rail.getSrc()) || isRecommendationRail(rail)) {
			return false;
		}
		return Novo19Conf.SECTION_DOCUMENTARIES.equals(rail.getTitle());
	}

	static boolean isPromotedBannerRail(final Novo19Rail rail) {
		return rail != null && "BANNER".equals(rail.getType()) && !StringUtils.isEmpty(rail.getSrc())
				&& !isRecommendationRail(rail);
	}

	static boolean isDocumentariesThemeRail(final Novo19Rail rail) {
		if (rail == null || StringUtils.isEmpty(rail.getTitle()) || isRecommendationRail(rail)) {
			return false;
		}
		if ("BANNER".equals(rail.getType())) {
			return false;
		}
		final String title = rail.getTitle().trim();
		if (title.contains(":")) {
			return false;
		}
		if (Novo19Conf.SECTION_DOCUMENTARIES.equals(title) || isCuratedSelectionRailTitle(title)) {
			return false;
		}
		if (isInfoEditorialRail(rail) || isTalkEditorialRail(rail)) {
			return false;
		}
		return !title.contains("?") && title.length() <= 80;
	}

	static boolean isSeasonRail(final Novo19Rail rail) {
		if (rail == null || StringUtils.isEmpty(rail.getTitle()) || StringUtils.isEmpty(rail.getSrc())) {
			return false;
		}
		final String title = rail.getTitle().trim().toLowerCase(Locale.FRENCH);
		return title.startsWith("saison ") && rail.getSrc().contains("asset-details-serie");
	}

	static String normalizeSectionTitle(final String title) {
		if (StringUtils.isEmpty(title)) {
			return "";
		}
		return title.trim();
	}

	private static boolean railTitleContains(final Novo19Rail rail, final String fragment) {
		return rail != null && !StringUtils.isEmpty(rail.getTitle())
				&& rail.getTitle().trim().toLowerCase().contains(fragment.toLowerCase());
	}

	private static String normalizePath(final String href) {
		String path = href;
		if (path.startsWith("http://") || path.startsWith("https://")) {
			final int idx = path.indexOf('/', 8);
			path = idx >= 0 ? path.substring(idx) : "/";
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		final int query = path.indexOf('?');
		if (query >= 0) {
			path = path.substring(0, query);
		}
		final int fragment = path.indexOf('#');
		if (fragment >= 0) {
			path = path.substring(0, fragment);
		}
		return path;
	}

}
