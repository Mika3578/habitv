package com.dabi.habitv.provider.tfo;

import java.net.URI;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

/**
 * Category id helpers and host-safe URL classification for TFO.
 */
final class TfoUrls {

	private TfoUrls() {
	}

	static String catalogCategoryId(final String catalogSlug) {
		return TfoConf.CATEGORY_CATALOG_PREFIX + catalogSlug;
	}

	static String catalogSlugFromCategoryId(final String categoryId) {
		if (StringUtils.isEmpty(categoryId) || !categoryId.startsWith(TfoConf.CATEGORY_CATALOG_PREFIX)) {
			return null;
		}
		final String slug = categoryId.substring(TfoConf.CATEGORY_CATALOG_PREFIX.length()).trim();
		return StringUtils.isEmpty(slug) ? null : slug;
	}

	static boolean isCatalogCategory(final String categoryId) {
		return catalogSlugFromCategoryId(categoryId) != null;
	}

	static String showCategoryId(final String path) {
		return TfoConf.CATEGORY_SHOW_PREFIX + normalizePath(path);
	}

	static String showPathFromCategoryId(final String categoryId) {
		if (StringUtils.isEmpty(categoryId) || !categoryId.startsWith(TfoConf.CATEGORY_SHOW_PREFIX)) {
			return null;
		}
		final String path = categoryId.substring(TfoConf.CATEGORY_SHOW_PREFIX.length()).trim();
		return StringUtils.isEmpty(path) ? null : path;
	}

	static boolean isShowCategory(final String categoryId) {
		return showPathFromCategoryId(categoryId) != null;
	}

	static String catalogPageUrl(final String catalogSlug) {
		return TfoConf.HOME_URL + "/" + catalogSlug;
	}

	static String absoluteUrl(final String pathOrUrl) {
		if (StringUtils.isEmpty(pathOrUrl)) {
			return null;
		}
		if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
			return pathOrUrl;
		}
		if (pathOrUrl.startsWith("/")) {
			return TfoConf.HOME_URL + pathOrUrl;
		}
		return TfoConf.HOME_URL + "/" + pathOrUrl;
	}

	static String normalizePath(final String pathOrUrl) {
		if (StringUtils.isEmpty(pathOrUrl)) {
			return null;
		}
		try {
			if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
				final URI uri = URI.create(pathOrUrl.trim());
				return uri.getPath();
			}
		} catch (final IllegalArgumentException e) {
			return null;
		}
		return pathOrUrl.startsWith("/") ? pathOrUrl : "/" + pathOrUrl;
	}

	static boolean isSeriePath(final String path) {
		return path != null && path.toLowerCase(Locale.ROOT).startsWith("/serie/");
	}

	static boolean isProductPath(final String path) {
		if (path == null) {
			return false;
		}
		final String lower = path.toLowerCase(Locale.ROOT);
		return lower.startsWith("/film/") || lower.startsWith("/titre/");
	}

	static String regarderUrlFromProductPath(final String path) {
		final String normalized = normalizePath(path);
		if (normalized == null) {
			return null;
		}
		final String lower = normalized.toLowerCase(Locale.ROOT);
		if (lower.startsWith("/film/")) {
			return absoluteUrl("/regarder/" + normalized.substring("/film/".length()));
		}
		if (lower.startsWith("/titre/")) {
			return absoluteUrl("/regarder/" + normalized.substring("/titre/".length()));
		}
		return null;
	}

	/**
	 * Accepts only http(s) TFO hosts without URI user-info.
	 */
	static boolean isTfoPageUrl(final String url) {
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
			return "tfo.org".equals(lowerHost) || "www.tfo.org".equals(lowerHost);
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

	static boolean isTfoWatchUrl(final String url) {
		if (!isTfoPageUrl(url)) {
			return false;
		}
		try {
			final String path = URI.create(url.trim()).getPath();
			return path != null && path.toLowerCase(Locale.ROOT).startsWith("/regarder/");
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}
}
