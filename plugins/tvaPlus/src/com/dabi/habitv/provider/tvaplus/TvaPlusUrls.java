package com.dabi.habitv.provider.tvaplus;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

final class TvaPlusUrls {

	private static final Pattern EPISODE_SLUG =
			Pattern.compile("^/tva/[\\w-]+/saison-\\d+/episode-[\\w-]+-\\d+$");

	private static final Pattern RELATIVE_TVA_PATH =
			Pattern.compile("^/tva/[\\w-]+(/[\\w-]+)*$");

	private static final Pattern SHOW_NAME_SEGMENT = Pattern.compile("[\\w-]+");

	private TvaPlusUrls() {
	}

	static String tvaChannelUrl() {
		return TvaPlusConf.HOME_URL + TvaPlusConf.TVA_CHANNEL_PATH;
	}

	static String recentUrl() {
		return TvaPlusConf.HOME_URL + TvaPlusConf.RECENT_PATH;
	}

	static String showCategoryId(final String slug) {
		return TvaPlusConf.CATEGORY_SHOW_PREFIX + slug;
	}

	static String showSlugFromCategoryId(final String categoryId) {
		if (StringUtils.isEmpty(categoryId) || !categoryId.startsWith(TvaPlusConf.CATEGORY_SHOW_PREFIX)) {
			return null;
		}
		final String slug = categoryId.substring(TvaPlusConf.CATEGORY_SHOW_PREFIX.length()).trim();
		return StringUtils.isEmpty(slug) ? null : slug;
	}

	static boolean isShowCategory(final String categoryId) {
		return showSlugFromCategoryId(categoryId) != null;
	}

	static boolean isRecentCategory(final String categoryId) {
		return TvaPlusConf.CATEGORY_RECENT.equals(categoryId);
	}

	static String pageUrl(final String slugOrPath) {
		if (!isSafeRelativeTvaPath(slugOrPath)) {
			return null;
		}
		return TvaPlusConf.HOME_URL + slugOrPath;
	}

	/**
	 * Accept only relative {@code /tva/...} paths with word/hyphen segments.
	 * Reject absolute URLs and query/fragment so remote slugs cannot redirect fetches.
	 */
	static boolean isSafeRelativeTvaPath(final String slugOrPath) {
		if (StringUtils.isEmpty(slugOrPath) || !slugOrPath.startsWith("/tva/")) {
			return false;
		}
		if (slugOrPath.indexOf("://") >= 0 || slugOrPath.indexOf('?') >= 0 || slugOrPath.indexOf('#') >= 0) {
			return false;
		}
		return RELATIVE_TVA_PATH.matcher(slugOrPath).matches();
	}

	static boolean isTvaShowSlug(final String slug) {
		if (StringUtils.isEmpty(slug)) {
			return false;
		}
		return slug.startsWith("/tva/") && slug.indexOf('/', 5) < 0;
	}

	/**
	 * Reject category-derived show slugs that are not a single /tva/{name} path or
	 * that smuggle URL metacharacters.
	 */
	static boolean isSafeCategoryShowSlug(final String slug) {
		if (!isTvaShowSlug(slug)) {
			return false;
		}
		if (slug.indexOf("://") >= 0 || slug.indexOf('?') >= 0 || slug.indexOf('#') >= 0) {
			return false;
		}
		final String showName = slug.substring(5);
		return showName.length() > 0 && SHOW_NAME_SEGMENT.matcher(showName).matches();
	}

	static boolean isTvaEpisodeSlug(final String slug) {
		if (StringUtils.isEmpty(slug) || !slug.startsWith("/tva/")) {
			return false;
		}
		return EPISODE_SLUG.matcher(slug).matches();
	}

	static boolean isTvaPlusPageUrl(final String url) {
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
			return "www.tvaplus.ca".equals(lowerHost) || "tvaplus.ca".equals(lowerHost);
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

	static boolean isTvaPlusEpisodeUrl(final String url) {
		return sanitizeEpisodeUrl(url) != null;
	}

	/**
	 * Normalize download input: reject userInfo, drop query/fragment for validation.
	 */
	static String sanitizeEpisodeUrl(final String url) {
		if (!isTvaPlusPageUrl(url)) {
			return null;
		}
		try {
			final URI uri = URI.create(url.trim());
			if (uri.getUserInfo() != null) {
				return null;
			}
			final String path = uri.getPath();
			if (!isTvaEpisodeSlug(path)) {
				return null;
			}
			return TvaPlusConf.HOME_URL + path;
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}
}
