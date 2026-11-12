package com.dabi.habitv.provider.sixplay;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

final class SixPlaySlugLabels {

	private static final Pattern PROGRAM_SLUG = Pattern.compile("/([^/]+)-p_\\d+/?$");

	private static final Pattern EPISODE_SEGMENT = Pattern.compile("^.+?/([^/]+)-c_\\d+/?$");

	private SixPlaySlugLabels() {
	}

	static String programTitleFromPath(final String programPath) {
		if (StringUtils.isEmpty(programPath)) {
			return "";
		}
		final Matcher matcher = PROGRAM_SLUG.matcher(programPath);
		if (!matcher.matches()) {
			return humanizeSlug(programPath);
		}
		return humanizeSlug(matcher.group(1));
	}

	static String episodeTitleFromPath(final String episodePath) {
		if (StringUtils.isEmpty(episodePath)) {
			return "";
		}
		final Matcher matcher = EPISODE_SEGMENT.matcher(episodePath);
		if (!matcher.matches()) {
			return humanizeSlug(episodePath);
		}
		String segment = matcher.group(1);
		final int clipIndex = segment.indexOf("-c_");
		if (clipIndex > 0) {
			segment = segment.substring(0, clipIndex);
		}
		return humanizeSlug(segment);
	}

	private static String humanizeSlug(final String slug) {
		if (StringUtils.isEmpty(slug)) {
			return "";
		}
		String normalized = slug.replace('-', ' ').trim();
		if (normalized.isEmpty()) {
			return "";
		}
		final StringBuilder builder = new StringBuilder();
		boolean capitalizeNext = true;
		for (int i = 0; i < normalized.length(); i++) {
			char ch = normalized.charAt(i);
			if (Character.isWhitespace(ch)) {
				capitalizeNext = true;
				builder.append(ch);
				continue;
			}
			if (capitalizeNext) {
				builder.append(Character.toTitleCase(ch));
				capitalizeNext = false;
			} else {
				builder.append(Character.toLowerCase(ch));
			}
		}
		return builder.toString();
	}

	static String folderTitleFromPath(final String folderPath) {
		if (StringUtils.isEmpty(folderPath)) {
			return "";
		}
		final int lastSlash = folderPath.lastIndexOf('/');
		String segment = lastSlash >= 0 ? folderPath.substring(lastSlash + 1) : folderPath;
		final int folderSuffix = segment.lastIndexOf("-f_");
		if (folderSuffix > 0) {
			segment = segment.substring(0, folderSuffix);
		}
		return humanizeSlug(segment);
	}

}
