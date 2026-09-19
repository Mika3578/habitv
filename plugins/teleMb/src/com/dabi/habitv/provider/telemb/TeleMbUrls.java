package com.dabi.habitv.provider.telemb;

import java.net.URI;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

final class TeleMbUrls {

	private TeleMbUrls() {
	}

	static String emissionsIndexUrl() {
		return TeleMbConf.HOME_URL + TeleMbConf.EMISSIONS_PATH;
	}

	static String showCategoryId(final String slug) {
		return TeleMbConf.CATEGORY_SHOW_PREFIX + slug;
	}

	static String showSlugFromCategoryId(final String categoryId) {
		if (StringUtils.isEmpty(categoryId) || !categoryId.startsWith(TeleMbConf.CATEGORY_SHOW_PREFIX)) {
			return null;
		}
		final String slug = categoryId.substring(TeleMbConf.CATEGORY_SHOW_PREFIX.length()).trim();
		return StringUtils.isEmpty(slug) ? null : slug;
	}

	static boolean isShowCategory(final String categoryId) {
		return showSlugFromCategoryId(categoryId) != null;
	}

	static String showPageUrl(final String slug) {
		return TeleMbConf.HOME_URL + "/emission/" + slug;
	}

	static String absoluteUrl(final String pathOrUrl) {
		if (StringUtils.isEmpty(pathOrUrl)) {
			return null;
		}
		if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
			return pathOrUrl;
		}
		if (pathOrUrl.startsWith("/")) {
			return TeleMbConf.HOME_URL + pathOrUrl;
		}
		return TeleMbConf.HOME_URL + "/" + pathOrUrl;
	}

	static String freecasterEmbedUrl(final String videoId) {
		return TeleMbConf.FRECAST_EMBED_PREFIX + videoId;
	}

	static boolean isTeleMbPageUrl(final String url) {
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
			return "www.telemb.be".equals(lower) || "telemb.be".equals(lower);
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

	static String sanitizeEpisodeUrl(final String url) {
		if (!isTeleMbPageUrl(url)) {
			return null;
		}
		try {
			final URI uri = URI.create(url.trim());
			final String rawPath = uri.getRawPath();
			final String path = uri.getPath();
			if (rawPath == null || path == null) {
				return null;
			}
			if (path.indexOf('?') >= 0 || path.indexOf('#') >= 0) {
				return null;
			}
			final String lowerRaw = rawPath.toLowerCase(Locale.ROOT);
			if (lowerRaw.contains("%3f") || lowerRaw.contains("%23")) {
				return null;
			}
			if (!isEpisodePath(rawPath)) {
				return null;
			}
			return TeleMbConf.HOME_URL + rawPath;
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	static boolean isEpisodePath(final String path) {
		if (StringUtils.isEmpty(path) || !path.startsWith("/replay/")) {
			return false;
		}
		final String trimmed = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
		final String[] parts = trimmed.split("/");
		// "", "replay", ...
		if (parts.length == 6 && "emission".equals(parts[2]) && isDigits(parts[5])) {
			return StringUtils.isNotEmpty(parts[3]) && StringUtils.isNotEmpty(parts[4]);
		}
		if (parts.length == 5 && isDigits(parts[4])) {
			final String section = parts[2];
			if ("emission".equals(section) || "emissions".equals(section)) {
				return false;
			}
			return StringUtils.isNotEmpty(section) && StringUtils.isNotEmpty(parts[3]);
		}
		return false;
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
			if (!TeleMbConf.FRECASTER_HLS_HOST.equals(lower)) {
				return null;
			}
			final String rawPath = uri.getRawPath();
			final String path = uri.getPath();
			if (rawPath == null || path == null) {
				return null;
			}
			if (path.indexOf('?') >= 0 || path.indexOf('#') >= 0) {
				return null;
			}
			final String lowerRaw = rawPath.toLowerCase(Locale.ROOT);
			if (lowerRaw.contains("%3f") || lowerRaw.contains("%23")) {
				return null;
			}
			if (!path.toLowerCase(Locale.ROOT).contains(".m3u8")) {
				return null;
			}
			return "https://" + TeleMbConf.FRECASTER_HLS_HOST + rawPath;
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	private static boolean isDigits(final String value) {
		if (StringUtils.isEmpty(value)) {
			return false;
		}
		for (int i = 0; i < value.length(); i++) {
			if (!Character.isDigit(value.charAt(i))) {
				return false;
			}
		}
		return true;
	}
}
