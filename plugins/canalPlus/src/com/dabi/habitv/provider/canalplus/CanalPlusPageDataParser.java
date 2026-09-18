package com.dabi.habitv.provider.canalplus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Parses embedded page bootstrap data from canalplus.com HTML (as captured by a
 * real browser session). Used to resolve hodor detail API URLs without executing JS.
 */
final class CanalPlusPageDataParser {

	private static final Pattern REACT_DETAIL_PAGE = Pattern.compile(
			"\"queryKey\"\\s*:\\s*\\[\\s*\"detailPage\"\\s*,\\s*\"([^\"]+)\"\\s*\\]");

	private static final Pattern REACT_QUERY_HODOR = Pattern.compile(
			"\"queryKey\"\\s*:\\s*\\[\\s*\"([^\"]+)\"\\s*,\\s*\"(https://hodor\\.canalplus\\.pro[^\"]+)\"\\s*\\]");

	private static final Pattern WINDOW_DATA_URL_PAGE = Pattern.compile(
			"\"URLPage\"\\s*:\\s*\"(https://hodor\\.canalplus\\.pro[^\"]+)\"");

	private CanalPlusPageDataParser() {
	}

	static String extractDetailPageUrl(final String html) {
		if (StringUtils.isEmpty(html)) {
			return null;
		}
		final Matcher reactMatcher = REACT_DETAIL_PAGE.matcher(html);
		while (reactMatcher.find()) {
			final String url = approvedHodorUrl(reactMatcher.group(1));
			if (url != null) {
				return url;
			}
		}
		return firstHodorUrlPage(html);
	}

	static String extractCatalogPageUrl(final String html) {
		if (StringUtils.isEmpty(html)) {
			return null;
		}
		final String landingUrl = firstReactQueryUrl(html, "landingPage");
		if (landingUrl != null) {
			return landingUrl;
		}
		final Matcher reactMatcher = REACT_QUERY_HODOR.matcher(html);
		while (reactMatcher.find()) {
			final String queryKey = reactMatcher.group(1);
			final String url = approvedHodorUrl(reactMatcher.group(2));
			if (url != null && !"detailPage".equals(queryKey)
					&& CanalPlusContentIdParser.fromInput(url) == null) {
				return url;
			}
		}
		return firstHodorCatalogUrlPage(html);
	}

	private static String firstReactQueryUrl(final String html, final String expectedKey) {
		final Matcher reactMatcher = REACT_QUERY_HODOR.matcher(html);
		while (reactMatcher.find()) {
			if (expectedKey.equals(reactMatcher.group(1))) {
				final String url = approvedHodorUrl(reactMatcher.group(2));
				if (url != null) {
					return url;
				}
			}
		}
		return null;
	}

	private static String firstHodorUrlPage(final String html) {
		final Matcher dataMatcher = WINDOW_DATA_URL_PAGE.matcher(html);
		while (dataMatcher.find()) {
			final String url = approvedHodorUrl(dataMatcher.group(1));
			if (url != null) {
				return url;
			}
		}
		return null;
	}

	private static String firstHodorCatalogUrlPage(final String html) {
		final Matcher dataMatcher = WINDOW_DATA_URL_PAGE.matcher(html);
		while (dataMatcher.find()) {
			final String url = approvedHodorUrl(dataMatcher.group(1));
			if (url != null && CanalPlusContentIdParser.fromInput(url) == null) {
				return url;
			}
		}
		return null;
	}

	private static String approvedHodorUrl(final String rawUrl) {
		if (StringUtils.isEmpty(rawUrl)) {
			return null;
		}
		final String url = unescapeJsonUrl(rawUrl);
		return CanalPlusContentIdParser.isHodorUrl(url) ? url : null;
	}

	private static String unescapeJsonUrl(final String url) {
		return url.replace("\\/", "/");
	}

}
