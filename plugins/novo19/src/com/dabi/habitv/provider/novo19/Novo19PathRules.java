package com.dabi.habitv.provider.novo19;

import org.apache.commons.lang.StringUtils;

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

	static boolean isRecommendationRailSrc(final String railSrc) {
		if (StringUtils.isEmpty(railSrc)) {
			return false;
		}
		return railSrc.contains("/sections/reco/") || railSrc.endsWith("/reco/tiles")
				|| railSrc.contains("/sections/reco/tiles");
	}

	static boolean isPodcastDetailRailSrc(final String railSrc) {
		return !StringUtils.isEmpty(railSrc) && railSrc.contains("asset-details-podcast");
	}

	static boolean isGenericCatalogueSectionTitle(final String title) {
		if (StringUtils.isEmpty(title)) {
			return true;
		}
		return "catalogue".equalsIgnoreCase(title.trim()) || "catégories".equalsIgnoreCase(title.trim())
				|| "categories".equalsIgnoreCase(title.trim());
	}

	static String normalizeSectionTitle(final String title) {
		if (StringUtils.isEmpty(title)) {
			return "Catalogue";
		}
		return title.trim();
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
		return path;
	}

}
