package com.dabi.habitv.provider.icitoutv;

import java.net.URI;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

final class IciToutTvUrls {

	private IciToutTvUrls() {
	}

	static String freeCollectionUrl() {
		return IciToutTvConf.HOME_URL + IciToutTvConf.FREE_COLLECTION_PATH;
	}

	static String showCategoryId(final String slug) {
		return IciToutTvConf.CATEGORY_SHOW_PREFIX + slug;
	}

	static String showSlugFromCategoryId(final String categoryId) {
		if (StringUtils.isEmpty(categoryId) || !categoryId.startsWith(IciToutTvConf.CATEGORY_SHOW_PREFIX)) {
			return null;
		}
		final String slug = categoryId.substring(IciToutTvConf.CATEGORY_SHOW_PREFIX.length()).trim();
		return StringUtils.isEmpty(slug) ? null : slug;
	}

	static boolean isShowCategory(final String categoryId) {
		return showSlugFromCategoryId(categoryId) != null;
	}

	static String showPageUrl(final String slug) {
		return IciToutTvConf.HOME_URL + "/" + slug;
	}

	static String absoluteWatchUrl(final String pathOrUrl) {
		if (StringUtils.isEmpty(pathOrUrl)) {
			return null;
		}
		if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
			return pathOrUrl;
		}
		if (pathOrUrl.startsWith("/")) {
			return IciToutTvConf.HOME_URL + pathOrUrl;
		}
		return IciToutTvConf.HOME_URL + "/" + pathOrUrl;
	}

	static boolean isIciToutTvPageUrl(final String url) {
		if (StringUtils.isEmpty(url)) {
			return false;
		}
		try {
			final URI uri = URI.create(url.trim());
			final String scheme = uri.getScheme();
			if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
				return false;
			}
			if (uri.getUserInfo() != null) {
				return false;
			}
			final String host = uri.getHost();
			if (host == null) {
				return false;
			}
			final String lowerHost = host.toLowerCase(Locale.ROOT);
			return "ici.tou.tv".equals(lowerHost) || "tou.tv".equals(lowerHost) || "www.tou.tv".equals(lowerHost);
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * Episode paths look like {@code /show-slug/s01e02} (season/episode token).
	 */
	static boolean isIciToutTvEpisodeUrl(final String url) {
		if (!isIciToutTvPageUrl(url)) {
			return false;
		}
		try {
			final String path = URI.create(url.trim()).getPath();
			if (path == null) {
				return false;
			}
			final String[] parts = path.split("/");
			// ["", "slug", "s22e01"]
			if (parts.length < 3) {
				return false;
			}
			final String token = parts[parts.length - 1].toLowerCase(Locale.ROOT);
			return token.matches("s\\d+e\\d+") || token.matches("s\\d+c\\d+");
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}
}
