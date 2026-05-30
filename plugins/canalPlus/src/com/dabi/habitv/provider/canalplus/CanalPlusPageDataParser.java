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

	private static final Pattern WINDOW_DATA_URL_PAGE = Pattern.compile(
			"\"URLPage\"\\s*:\\s*\"(https://hodor\\.canalplus\\.pro[^\"]+)\"");

	private CanalPlusPageDataParser() {
	}

	static String extractDetailPageUrl(final String html) {
		if (StringUtils.isEmpty(html)) {
			return null;
		}
		final Matcher reactMatcher = REACT_DETAIL_PAGE.matcher(html);
		if (reactMatcher.find()) {
			return unescapeJsonUrl(reactMatcher.group(1));
		}
		final Matcher dataMatcher = WINDOW_DATA_URL_PAGE.matcher(html);
		if (dataMatcher.find()) {
			return unescapeJsonUrl(dataMatcher.group(1));
		}
		return null;
	}

	private static String unescapeJsonUrl(final String url) {
		return url.replace("\\/", "/");
	}

}
