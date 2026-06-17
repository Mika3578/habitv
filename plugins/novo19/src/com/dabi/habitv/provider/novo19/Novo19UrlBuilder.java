package com.dabi.habitv.provider.novo19;

import org.apache.commons.lang.StringUtils;

final class Novo19UrlBuilder {

	private Novo19UrlBuilder() {
	}

	static String publicPageUrl(final String href) {
		if (StringUtils.isEmpty(href)) {
			return null;
		}
		if (href.startsWith("http://") || href.startsWith("https://")) {
			return href;
		}
		if (href.startsWith("/")) {
			return Novo19Conf.HOME_URL + href;
		}
		return Novo19Conf.HOME_URL + "/" + href;
	}

	static String bffConfigUrl() {
		return Novo19Conf.BFF_BASE_URL + Novo19Conf.CONFIG_PATH;
	}

	static String bffPageByPath(final String publicPath) {
		String normalized = publicPath == null ? "" : publicPath.trim();
		while (normalized.startsWith("/")) {
			normalized = normalized.substring(1);
		}
		return Novo19Conf.BFF_BASE_URL + Novo19Conf.SLUG_RESOLVER_PREFIX + normalized;
	}

	static String bffAbsolutePath(final String bffPath) {
		if (StringUtils.isEmpty(bffPath)) {
			return null;
		}
		if (bffPath.startsWith("http://") || bffPath.startsWith("https://")) {
			return bffPath;
		}
		if (bffPath.startsWith("/")) {
			return Novo19Conf.BFF_BASE_URL + bffPath;
		}
		return Novo19Conf.BFF_BASE_URL + "/" + bffPath;
	}

	static String publicPathFromCategoryId(final String categoryId) {
		if (StringUtils.isEmpty(categoryId) || !categoryId.startsWith(Novo19Conf.HOME_URL)) {
			return null;
		}
		String path = categoryId.substring(Novo19Conf.HOME_URL.length());
		if (path.contains("#")) {
			path = path.substring(0, path.indexOf('#'));
		}
		return path;
	}

	static String redBeeAnonymousAuthUrl() {
		return Novo19Conf.REDBEE_BASE_URL + Novo19Conf.REDBEE_ANONYMOUS_AUTH_PATH;
	}

	static String redBeePlayUrl(final String assetId) {
		return Novo19Conf.REDBEE_BASE_URL + Novo19Conf.REDBEE_PLAY_PATH_PREFIX + assetId
				+ Novo19Conf.REDBEE_PLAY_PATH_SUFFIX;
	}

}
