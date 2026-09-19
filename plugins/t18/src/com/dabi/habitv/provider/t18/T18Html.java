package com.dabi.habitv.provider.t18;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * HTML helpers for T18 public catalogue pages.
 */
final class T18Html {

	private static final Pattern PROG_HREF = Pattern.compile("href=\"(/prog/[^\"]+)\"");

	private static final Pattern TITLE_TAG = Pattern.compile("<title>([^<]+)</title>", Pattern.CASE_INSENSITIVE);

	private T18Html() {
	}

	static Map<String, String> extractProgramCategories(final String html) {
		final Map<String, String> programs = new LinkedHashMap<String, String>();
		if (StringUtils.isEmpty(html)) {
			return programs;
		}
		final Matcher matcher = PROG_HREF.matcher(html);
		while (matcher.find()) {
			final String path = matcher.group(1);
			if (isEpisodePath(path)) {
				continue;
			}
			final String slug = programSlug(path);
			if (StringUtils.isEmpty(slug) || programs.containsKey(slug)) {
				continue;
			}
			programs.put(slug, humanizeSlug(slug));
		}
		return programs;
	}

	static Map<String, String> extractEpisodePaths(final String html, final String programSlug) {
		final Map<String, String> episodes = new LinkedHashMap<String, String>();
		if (StringUtils.isEmpty(html) || StringUtils.isEmpty(programSlug)) {
			return episodes;
		}
		final Matcher matcher = PROG_HREF.matcher(html);
		final String prefix = "/prog/" + programSlug + "/";
		while (matcher.find()) {
			final String path = matcher.group(1);
			if (!path.startsWith(prefix)) {
				continue;
			}
			final String episodeSlug = path.substring(prefix.length());
			if (StringUtils.isEmpty(episodeSlug) || episodeSlug.contains("/")) {
				continue;
			}
			episodes.put(path, humanizeSlug(episodeSlug));
		}
		return episodes;
	}

	static boolean isEpisodePath(final String path) {
		if (StringUtils.isEmpty(path) || !path.startsWith("/prog/")) {
			return false;
		}
		final String rest = path.substring("/prog/".length());
		return rest.indexOf('/') >= 0;
	}

	static String programSlug(final String path) {
		if (StringUtils.isEmpty(path) || !path.startsWith("/prog/")) {
			return null;
		}
		final String rest = path.substring("/prog/".length());
		final int slash = rest.indexOf('/');
		return slash < 0 ? rest : rest.substring(0, slash);
	}

	static String absoluteUrl(final String pathOrUrl) {
		if (StringUtils.isEmpty(pathOrUrl)) {
			return pathOrUrl;
		}
		if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
			return pathOrUrl;
		}
		if (pathOrUrl.startsWith("/")) {
			return T18Conf.HOME_URL + pathOrUrl;
		}
		return T18Conf.HOME_URL + "/" + pathOrUrl;
	}

	static boolean isT18Url(final String url) {
		return StringUtils.isNotEmpty(url) && url.toLowerCase().contains("t18.fr/");
	}

	static String pageTitle(final String html) {
		if (StringUtils.isEmpty(html)) {
			return null;
		}
		final Matcher matcher = TITLE_TAG.matcher(html);
		if (!matcher.find()) {
			return null;
		}
		String title = matcher.group(1).trim();
		final int dash = title.indexOf(" - ");
		if (dash > 0) {
			title = title.substring(0, dash).trim();
		}
		return title;
	}

	static boolean mentionsPrivateDailymotionEmbed(final String html) {
		return StringUtils.isNotEmpty(html) && html.contains("data-dailymotion-video-id");
	}

	private static String humanizeSlug(final String slug) {
		if (StringUtils.isEmpty(slug)) {
			return "";
		}
		String cleaned = slug;
		cleaned = cleaned.replaceFirst("-\\d+$", "");
		cleaned = cleaned.replace('-', ' ');
		if (cleaned.isEmpty()) {
			return slug;
		}
		return Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
	}
}
