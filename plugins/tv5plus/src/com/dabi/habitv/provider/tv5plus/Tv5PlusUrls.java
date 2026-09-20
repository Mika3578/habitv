package com.dabi.habitv.provider.tv5plus;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

final class Tv5PlusUrls {

	private static final Pattern EPISODE_PATH = Pattern
			.compile("^/videos/([A-Za-z0-9][A-Za-z0-9_-]*)(?:/saisons/(\\d+)/episodes/(\\d+))?/?$");

	private Tv5PlusUrls() {
	}

	static String showCategoryId(final String slug) {
		return Tv5PlusConf.CATEGORY_SHOW_PREFIX + slug;
	}

	static String showSlugFromCategoryId(final String categoryId) {
		if (StringUtils.isEmpty(categoryId) || !categoryId.startsWith(Tv5PlusConf.CATEGORY_SHOW_PREFIX)) {
			return null;
		}
		final String slug = categoryId.substring(Tv5PlusConf.CATEGORY_SHOW_PREFIX.length()).trim();
		return StringUtils.isEmpty(slug) ? null : slug;
	}

	static boolean isShowCategory(final String categoryId) {
		return showSlugFromCategoryId(categoryId) != null;
	}

	static String movieWatchUrl(final String slug) {
		return Tv5PlusConf.YTDLP_HOST_URL + "/videos/" + slug;
	}

	static String episodeWatchUrl(final String slug, final int seasonNumber, final int episodeNumber) {
		return Tv5PlusConf.YTDLP_HOST_URL + "/videos/" + slug + "/saisons/" + seasonNumber + "/episodes/"
				+ episodeNumber;
	}

	/**
	 * Rewrite tv5plus.ca video URLs to tv5unis.ca so yt-dlp's extractors match.
	 */
	static String sanitizeEpisodeUrl(final String url) {
		if (StringUtils.isEmpty(url)) {
			return null;
		}
		try {
			final URI uri = URI.create(url.trim());
			final String scheme = uri.getScheme();
			if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
				return null;
			}
			if (uri.getUserInfo() != null) {
				return null;
			}
			final String host = uri.getHost();
			if (host == null) {
				return null;
			}
			final String lowerHost = host.toLowerCase(Locale.ROOT);
			if (!"www.tv5plus.ca".equals(lowerHost) && !"tv5plus.ca".equals(lowerHost)
					&& !"www.tv5unis.ca".equals(lowerHost) && !"tv5unis.ca".equals(lowerHost)) {
				return null;
			}
			final String rawPath = uri.getRawPath();
			final String path = uri.getPath();
			if (rawPath == null || path == null) {
				return null;
			}
			// Reject decoded or percent-encoded query/fragment separators smuggled into the path.
			if (path.indexOf('?') >= 0 || path.indexOf('#') >= 0) {
				return null;
			}
			final String lowerRaw = rawPath.toLowerCase(Locale.ROOT);
			if (lowerRaw.contains("%3f") || lowerRaw.contains("%23") || lowerRaw.contains("%253f")
					|| lowerRaw.contains("%2523")) {
				return null;
			}
			final Matcher matcher = EPISODE_PATH.matcher(rawPath);
			if (!matcher.matches()) {
				return null;
			}
			final String slug = matcher.group(1);
			if (matcher.group(2) != null && matcher.group(3) != null) {
				return episodeWatchUrl(slug, Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
			}
			return movieWatchUrl(slug);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	/** True when the sanitized URL is an episode path, not a movie/series root. */
	static boolean isEpisodeWatchUrl(final String url) {
		if (StringUtils.isEmpty(url)) {
			return false;
		}
		final String lower = url.toLowerCase(Locale.ROOT);
		return lower.contains("/saisons/") && lower.contains("/episodes/");
	}

	static boolean isTv5PlusEpisodeUrl(final String url) {
		return sanitizeEpisodeUrl(url) != null;
	}
}
