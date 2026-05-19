package com.dabi.habitv.provider.francetv;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

final class FranceTvUrls {

	private FranceTvUrls() {
	}

	static String programPageUrl(final String programPath) {
		if (StringUtils.isEmpty(programPath)) {
			return null;
		}
		final int separator = programPath.indexOf('_');
		if (separator <= 0 || separator >= programPath.length() - 1) {
			return null;
		}
		final String channel = programPath.substring(0, separator);
		final String program = programPath.substring(separator + 1);
		return FranceTvConf.HOME_URL + "/" + channel + "/" + program + "/";
	}

	static String programPathFromCategoryUrl(final String categoryUrl) {
		if (StringUtils.isEmpty(categoryUrl) || !categoryUrl.startsWith(FranceTvConf.HOME_URL)) {
			return null;
		}
		String path = categoryUrl.substring(FranceTvConf.HOME_URL.length());
		if (!path.startsWith("/")) {
			return null;
		}
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		final String[] segments = path.split("/");
		if (segments.length < 3 || StringUtils.isEmpty(segments[1]) || StringUtils.isEmpty(segments[2])) {
			return null;
		}
		return segments[1] + "_" + segments[2];
	}

	static String episodePageUrl(final Map<String, Object> item) {
		final String programPath = programPathFromItem(item);
		if (programPath == null) {
			return null;
		}
		final int separator = programPath.indexOf('_');
		if (separator <= 0 || separator >= programPath.length() - 1) {
			return null;
		}
		final String channel = programPath.substring(0, separator);
		final String program = programPath.substring(separator + 1);

		final Object rawId = item.get("id");
		if (rawId == null) {
			return null;
		}
		final String videoId = String.valueOf(rawId);
		final String titleSlug = slugify(episodeTitle(item));

		final StringBuilder url = new StringBuilder(FranceTvConf.HOME_URL).append('/').append(channel).append('/')
				.append(program).append('/');

		final Object rawSeason = item.get("season");
		if (rawSeason instanceof Number && ((Number) rawSeason).intValue() > 0) {
			url.append(program).append("-saison-").append(((Number) rawSeason).intValue()).append('/');
		}

		url.append(videoId).append('-').append(titleSlug).append(".html");
		return url.toString();
	}

	static boolean isReplayVideoType(final String type) {
		return "integrale".equals(type) || "unitaire".equals(type);
	}

	static String channelLabel(final String slug) {
		switch (slug) {
		case "france-2":
			return "France 2";
		case "france-3":
			return "France 3";
		case "france-4":
			return "France 4";
		case "france-5":
			return "France 5";
		case "la1ere":
			return "La 1ère";
		default:
			return slug;
		}
	}

	private static String programPathFromItem(final Map<String, Object> item) {
		final Object program = item.get("program");
		if (!(program instanceof Map)) {
			return null;
		}
		final Object programPath = ((Map<?, ?>) program).get("program_path");
		return programPath == null ? null : String.valueOf(programPath);
	}

	private static String episodeTitle(final Map<String, Object> item) {
		final Object title = item.get("title");
		if (title != null && StringUtils.isNotEmpty(String.valueOf(title))) {
			return String.valueOf(title);
		}
		final Object episodeTitle = item.get("episode_title");
		return episodeTitle == null ? "episode" : String.valueOf(episodeTitle);
	}

	private static String slugify(final String text) {
		String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
		normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		normalized = normalized.toLowerCase(Locale.ROOT);
		normalized = normalized.replaceAll("[^a-z0-9]+", "-");
		normalized = normalized.replaceAll("^-+|-+$", "");
		if (StringUtils.isEmpty(normalized)) {
			return "episode";
		}
		return normalized;
	}

}
