package com.dabi.habitv.provider.canalplus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Extracts Canal+ unit content ids ({@code 31338503_50017}) from modern page URLs
 * and hodor detail API URLs.
 */
final class CanalPlusContentIdParser {

	private static final Pattern CONTENT_ID = Pattern.compile("(\\d+_\\d+)");

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
		final Matcher genericMatcher = CONTENT_ID.matcher(input);
		if (genericMatcher.find()) {
			return genericMatcher.group(1);
		}
		return null;
	}

	static boolean isModernCanalPlusUrl(final String input) {
		if (StringUtils.isEmpty(input)) {
			return false;
		}
		final String lower = input.toLowerCase();
		return lower.contains(CanalPlusModernConf.PAGE_HOST)
				|| lower.contains(CanalPlusModernConf.HODOR_HOST)
				|| lower.contains("secure-gen-hapi.canal-plus.com")
				|| lower.contains(CanalPlusModernConf.ROUTE_MEUP_HOST)
				|| fromInput(input) != null;
	}

}
