package com.dabi.habitv.provider.tvlux;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

final class TvLuxUrls {

	// Episode pages are /replay/{show}/{title}_{id}; exclude /replay/{show}/page_{n} pagination.
	private static final Pattern EPISODE_PATH = Pattern.compile("^/replay/[^/]+/(?!page_\\d+)[^/]+_\\d+/?$");

	private TvLuxUrls() {
	}

	static String replayIndexUrl() {
		return TvLuxConf.HOME_URL + TvLuxConf.REPLAY_PATH;
	}

	static String showCategoryId(final String slug) {
		return TvLuxConf.CATEGORY_SHOW_PREFIX + slug;
	}

	static String showSlugFromCategoryId(final String categoryId) {
		if (StringUtils.isEmpty(categoryId) || !categoryId.startsWith(TvLuxConf.CATEGORY_SHOW_PREFIX)) {
			return null;
		}
		final String slug = categoryId.substring(TvLuxConf.CATEGORY_SHOW_PREFIX.length()).trim();
		return StringUtils.isEmpty(slug) ? null : slug;
	}

	static boolean isShowCategory(final String categoryId) {
		return showSlugFromCategoryId(categoryId) != null;
	}

	static String showPageUrl(final String slug) {
		return TvLuxConf.HOME_URL + "/replay/" + slug;
	}

	static String absoluteUrl(final String pathOrUrl) {
		if (StringUtils.isEmpty(pathOrUrl)) {
			return null;
		}
		if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
			return pathOrUrl;
		}
		if (pathOrUrl.startsWith("/")) {
			return TvLuxConf.HOME_URL + pathOrUrl;
		}
		return TvLuxConf.HOME_URL + "/" + pathOrUrl;
	}

	static String freecasterEmbedUrl(final String videoId) {
		return TvLuxConf.FRECAST_EMBED_PREFIX + videoId;
	}

	static boolean isTvLuxPageUrl(final String url) {
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
			final String lower = host.toLowerCase(Locale.ROOT);
			return "www.tvlux.be".equals(lower) || "tvlux.be".equals(lower);
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

	static String sanitizeEpisodeUrl(final String url) {
		if (!isTvLuxPageUrl(url)) {
			return null;
		}
		try {
			final URI uri = URI.create(url.trim());
			final String path = uri.getPath();
			if (path == null || !EPISODE_PATH.matcher(path).matches()) {
				return null;
			}
			return TvLuxConf.HOME_URL + path;
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
			if (!"https".equalsIgnoreCase(uri.getScheme())) {
				return null;
			}
			if (uri.getUserInfo() != null) {
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
			if (!TvLuxConf.FRECASTER_HLS_HOST.equals(lower)) {
				return null;
			}
			final String path = uri.getPath();
			if (path == null || !path.toLowerCase(Locale.ROOT).endsWith(".m3u8")) {
				return null;
			}
			return "https://" + TvLuxConf.FRECASTER_HLS_HOST + path;
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}
}
