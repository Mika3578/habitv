package com.dabi.habitv.provider.tvcom;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

final class TvComUrls {

	private static final Pattern EPISODE_PATH = Pattern
			.compile("^/replay/emission/[^/]+/[^/]+/\\d+/?$");

	private static final Pattern LEGACY_EPISODE_PATH = Pattern
			.compile("^/replay/emissions/[^/]+/\\d+/?$");

	private TvComUrls() {
	}

	static String emissionsIndexUrl() {
		return TvComConf.HOME_URL + TvComConf.EMISSIONS_PATH;
	}

	static String showCategoryId(final String slug) {
		return TvComConf.CATEGORY_SHOW_PREFIX + slug;
	}

	static String showSlugFromCategoryId(final String categoryId) {
		if (StringUtils.isEmpty(categoryId) || !categoryId.startsWith(TvComConf.CATEGORY_SHOW_PREFIX)) {
			return null;
		}
		final String slug = categoryId.substring(TvComConf.CATEGORY_SHOW_PREFIX.length()).trim();
		return StringUtils.isEmpty(slug) ? null : slug;
	}

	static boolean isShowCategory(final String categoryId) {
		return showSlugFromCategoryId(categoryId) != null;
	}

	static String showPageUrl(final String slug) {
		return TvComConf.HOME_URL + "/emission/" + slug;
	}

	static String absoluteUrl(final String pathOrUrl) {
		if (StringUtils.isEmpty(pathOrUrl)) {
			return null;
		}
		if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
			return pathOrUrl;
		}
		if (pathOrUrl.startsWith("/")) {
			return TvComConf.HOME_URL + pathOrUrl;
		}
		return TvComConf.HOME_URL + "/" + pathOrUrl;
	}

	static String freecasterEmbedUrl(final String videoId) {
		return TvComConf.FRECAST_EMBED_PREFIX + videoId;
	}

	static boolean isTvComPageUrl(final String url) {
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
			final int port = uri.getPort();
			if (port != -1 && port != 80 && port != 443) {
				return false;
			}
			final String host = uri.getHost();
			if (host == null) {
				return false;
			}
			final String lower = host.toLowerCase(Locale.ROOT);
			return "www.tvcom.be".equals(lower) || "tvcom.be".equals(lower);
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

	static String sanitizeEpisodeUrl(final String url) {
		if (!isTvComPageUrl(url)) {
			return null;
		}
		try {
			final URI uri = URI.create(url.trim());
			final String rawPath = uri.getRawPath();
			if (rawPath == null) {
				return null;
			}
			final String lowerRaw = rawPath.toLowerCase(Locale.ROOT);
			if (lowerRaw.contains("%3f") || lowerRaw.contains("%23")) {
				return null;
			}
			if (!EPISODE_PATH.matcher(rawPath).matches() && !LEGACY_EPISODE_PATH.matcher(rawPath).matches()) {
				return null;
			}
			return TvComConf.HOME_URL + rawPath;
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	static String sanitizeHlsUrl(final String url) {
		if (StringUtils.isEmpty(url)) {
			return null;
		}
		try {
			final URI uri = URI.create(url.trim());
			if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getUserInfo() != null) {
				return null;
			}
			final int port = uri.getPort();
			if (port != -1 && port != 443) {
				return null;
			}
			final String host = uri.getHost();
			if (host == null) {
				return null;
			}
			final String lower = host.toLowerCase(Locale.ROOT);
			if (!TvComConf.FRECASTER_HLS_HOST.equals(lower)) {
				return null;
			}
			final String rawPath = uri.getRawPath();
			final String path = uri.getPath();
			if (rawPath == null || path == null) {
				return null;
			}
			final String lowerRaw = rawPath.toLowerCase(Locale.ROOT);
			if (lowerRaw.contains("%3f") || lowerRaw.contains("%23")) {
				return null;
			}
			if (!path.toLowerCase(Locale.ROOT).contains(".m3u8")) {
				return null;
			}
			return "https://" + TvComConf.FRECASTER_HLS_HOST + rawPath;
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}
}
