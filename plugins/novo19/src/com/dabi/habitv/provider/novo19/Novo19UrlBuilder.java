package com.dabi.habitv.provider.novo19;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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
		normalized = stripQueryAndFragment(normalized);
		while (normalized.startsWith("/")) {
			normalized = normalized.substring(1);
		}
		return Novo19Conf.BFF_BASE_URL + Novo19Conf.SLUG_RESOLVER_PREFIX + normalized;
	}

	static String bffAbsolutePath(final String bffPath) throws IOException {
		if (StringUtils.isEmpty(bffPath)) {
			return null;
		}
		if (bffPath.startsWith("http://") || bffPath.startsWith("https://")) {
			requireApprovedBffHost(hostFromUrl(bffPath));
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
		path = stripQueryAndFragment(path);
		if (path.contains("#")) {
			path = path.substring(0, path.indexOf('#'));
		}
		return path;
	}

	static boolean isApprovedPublicDownloadUrl(final String downloadInput) {
		if (StringUtils.isEmpty(downloadInput)) {
			return false;
		}
		try {
			return Novo19Conf.PUBLIC_HOST.equalsIgnoreCase(hostFromUrl(downloadInput));
		} catch (final IOException e) {
			return false;
		}
	}

	static String stripQueryAndFragment(final String path) {
		if (StringUtils.isEmpty(path)) {
			return path;
		}
		String normalized = path;
		final int query = normalized.indexOf('?');
		if (query >= 0) {
			normalized = normalized.substring(0, query);
		}
		final int fragment = normalized.indexOf('#');
		if (fragment >= 0) {
			normalized = normalized.substring(0, fragment);
		}
		return normalized;
	}

	static String redBeeAnonymousAuthUrl() {
		return Novo19Conf.REDBEE_BASE_URL + Novo19Conf.REDBEE_ANONYMOUS_AUTH_PATH;
	}

	static String redBeePlayUrl(final String assetId) {
		return Novo19Conf.REDBEE_BASE_URL + Novo19Conf.REDBEE_PLAY_PATH_PREFIX + assetId
				+ Novo19Conf.REDBEE_PLAY_PATH_SUFFIX;
	}

	static void requireApprovedBffHost(final String host) throws IOException {
		if (StringUtils.isEmpty(host) || !Novo19Conf.BFF_HOST.equalsIgnoreCase(host.trim())) {
			throw new IOException("unapproved-bff-host:" + host);
		}
	}

	private static String hostFromUrl(final String url) throws IOException {
		try {
			return new URL(url).getHost();
		} catch (final MalformedURLException e) {
			throw new IOException("malformed-url:" + url, e);
		}
	}

}
