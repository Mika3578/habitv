package com.dabi.habitv.provider.sixplay;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

final class SixPlayHrefCatalog {

	private static final Pattern PROGRAM_HREF = Pattern.compile("href=\"(/[^\"\\s]+-p_\\d+)\"", Pattern.CASE_INSENSITIVE);

	private static final Pattern EPISODE_HREF = Pattern.compile("href=\"(/[^\"\\s]+-c_\\d+)\"", Pattern.CASE_INSENSITIVE);

	private static final Pattern SITEMAP_LOC = Pattern.compile("<loc>https://www\\.m6\\.fr([^<]+)</loc>",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern FOLDER_SUFFIX = Pattern.compile("-f_\\d+/?$");

	private SixPlayHrefCatalog() {
	}

	static Set<String> programPathsFromHtml(final String html) {
		final Set<String> paths = new LinkedHashSet<>();
		if (StringUtils.isEmpty(html)) {
			return paths;
		}
		final Matcher matcher = PROGRAM_HREF.matcher(html);
		while (matcher.find()) {
			paths.add(matcher.group(1));
		}
		return paths;
	}

	static Set<String> episodePathsFromHtml(final String html) {
		final Set<String> paths = new LinkedHashSet<>();
		if (StringUtils.isEmpty(html)) {
			return paths;
		}
		final Matcher matcher = EPISODE_HREF.matcher(html);
		while (matcher.find()) {
			paths.add(matcher.group(1));
		}
		return paths;
	}

	static Set<String> channelFolderPathsFromSitemap(final String sitemapXml) {
		final Set<String> paths = new LinkedHashSet<>();
		if (StringUtils.isEmpty(sitemapXml)) {
			return paths;
		}
		final Matcher matcher = SITEMAP_LOC.matcher(sitemapXml);
		while (matcher.find()) {
			final String path = matcher.group(1);
			if (!isChannelFolderPath(path)) {
				continue;
			}
			final String channelSlug = channelSlugFromPath(path);
			if (!SixPlayConf.CHANNEL_SLUG_TO_LABEL.containsKey(channelSlug)) {
				continue;
			}
			paths.add(path);
		}
		return paths;
	}

	private static boolean isChannelFolderPath(final String path) {
		if (StringUtils.isEmpty(path) || !path.startsWith("/")) {
			return false;
		}
		final int secondSlash = path.indexOf('/', 1);
		if (secondSlash < 0) {
			return false;
		}
		return FOLDER_SUFFIX.matcher(path).find();
	}

	static String channelSlugFromPath(final String path) {
		if (StringUtils.isEmpty(path) || !path.startsWith("/")) {
			return "";
		}
		final int end = path.indexOf('/', 1);
		if (end < 0) {
			return "";
		}
		return path.substring(1, end);
	}

	static String toAbsoluteUrl(final String pathOrUrl) {
		if (StringUtils.isEmpty(pathOrUrl)) {
			return SixPlayConf.HOME_URL;
		}
		if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
			return pathOrUrl;
		}
		if (pathOrUrl.startsWith("/")) {
			return SixPlayConf.HOME_URL.substring(0, SixPlayConf.HOME_URL.length() - 1) + pathOrUrl;
		}
		return SixPlayConf.HOME_URL + pathOrUrl;
	}

}
