package com.dabi.habitv.provider.novo19;

import org.apache.commons.lang.StringUtils;

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
		if (path.startsWith("/podcasts")) {
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
		return "COLLECTION".equals(type) || "SERIE".equals(type) || "VOD".equals(type) || "EPISODE".equals(type);
	}

	static boolean isEpisodeTileType(final String type) {
		return "EPISODE".equals(type) || "VOD".equals(type);
	}

	static boolean isProgramTileType(final String type) {
		return "SERIE".equals(type) || "VOD".equals(type);
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
