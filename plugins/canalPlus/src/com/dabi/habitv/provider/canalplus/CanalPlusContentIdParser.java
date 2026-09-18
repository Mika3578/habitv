package com.dabi.habitv.provider.canalplus;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Extracts Canal+ unit content ids ({@code 31338503_50017}) from modern page URLs
 * and hodor detail API URLs.
 */
final class CanalPlusContentIdParser {

	private static final Pattern PAGE_CONTENT_ID = Pattern.compile("/h/(\\d+_\\d+)(?:/|$|\\?)");

	private static final Pattern HODOR_CONTENT_ID = Pattern.compile("/okapi/(\\d+_\\d+)\\.json");

	private CanalPlusContentIdParser() {
	}

	static String fromInput(final String input) {
		if (StringUtils.isEmpty(input)) {
			return null;
		}
		final Matcher pageMatcher = PAGE_CONTENT_ID.matcher(input);
		if (pageMatcher.find()) {
			return pageMatcher.group(1);
		}
		final Matcher hodorMatcher = HODOR_CONTENT_ID.matcher(input);
		if (hodorMatcher.find()) {
			return hodorMatcher.group(1);
		}
		if (input.matches("\\d+_\\d+")) {
			return input;
		}
		return null;
	}

	static boolean isModernCanalPlusUrl(final String input) {
		final String host = hostOf(input);
		return isCanalPlusPageHost(host)
				|| CanalPlusModernConf.HODOR_HOST.equals(host)
				|| CanalPlusModernConf.SECURE_HAPI_HOST.equals(host)
				|| CanalPlusModernConf.ROUTE_MEUP_HOST.equals(host);
	}

	static boolean isCanalPlusPageUrl(final String input) {
		return isCanalPlusPageHost(hostOf(input));
	}

	static boolean isHodorUrl(final String input) {
		return CanalPlusModernConf.HODOR_HOST.equals(hostOf(input));
	}

	private static boolean isCanalPlusPageHost(final String host) {
		return CanalPlusModernConf.PAGE_HOST.equals(host)
				|| CanalPlusModernConf.PAGE_WWW_HOST.equals(host);
	}

	static String hostOf(final String input) {
		if (StringUtils.isEmpty(input)) {
			return null;
		}
		try {
			final URI uri = new URI(input);
			final String host = uri.getHost();
			return host == null ? null : host.toLowerCase(Locale.ROOT);
		} catch (URISyntaxException e) {
			return null;
		}
	}

}
