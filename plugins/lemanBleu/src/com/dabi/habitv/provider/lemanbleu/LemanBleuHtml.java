package com.dabi.habitv.provider.lemanbleu;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * HTML parsing helpers and URL builders for Léman Bleu public pages.
 */
final class LemanBleuHtml {

	private static final Pattern PROGRAM_LINK = Pattern.compile(
			"<a[^>]+href=\"([^\"]*emission=(\\d+)[^\"]*)\"[^>]*title=\"([^\"]+)\"",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern PROGRAM_LINK_TITLE_FIRST = Pattern.compile(
			"<a[^>]+title=\"([^\"]+)\"[^>]*href=\"([^\"]*emission=(\\d+)[^\"]*)\"",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern EPISODE_CARD = Pattern.compile(
			"<div class=\"videoList-item-title\">(.*?)</div>\\s*<a href=\"(/fr/Emissions/[^\"]+)\"",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private static final Pattern ITEM_TEXT = Pattern.compile(
			"videoList-item-text\">([^<]+)", Pattern.CASE_INSENSITIVE);

	private static final Pattern ITEM_DESC = Pattern.compile(
			"videoList-item-desc\">([^<]+)", Pattern.CASE_INSENSITIVE);

	private static final Pattern ITEM_DATE = Pattern.compile(
			"videoList-item-date\">([^<]+)", Pattern.CASE_INSENSITIVE);

	private static final Pattern DATA_VIDEO_URL = Pattern.compile(
			"data-video-url=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);

	private static final Pattern DATA_TITLE = Pattern.compile(
			"data-title=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);

	private static final Pattern H1 = Pattern.compile(
			"<h1[^>]*>(.*?)</h1>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private LemanBleuHtml() {
	}

	static String programsUrl() {
		return LemanBleuConf.HOME_URL + LemanBleuConf.PROGRAMS_PATH;
	}

	static String archiveUrl(final String emissionId) {
		return LemanBleuConf.HOME_URL + LemanBleuConf.ARCHIVE_PATH + emissionId;
	}

	static String showCategoryId(final String emissionId) {
		return LemanBleuConf.CATEGORY_SHOW_PREFIX + emissionId;
	}

	static String emissionIdFromCategoryId(final String categoryId) {
		if (StringUtils.isEmpty(categoryId) || !categoryId.startsWith(LemanBleuConf.CATEGORY_SHOW_PREFIX)) {
			return null;
		}
		final String id = categoryId.substring(LemanBleuConf.CATEGORY_SHOW_PREFIX.length()).trim();
		return StringUtils.isEmpty(id) ? null : id;
	}

	static boolean isShowCategory(final String categoryId) {
		return emissionIdFromCategoryId(categoryId) != null;
	}

	static boolean isLemanBleuEpisodeUrl(final String url) {
		if (StringUtils.isEmpty(url)) {
			return false;
		}
		try {
			final URI uri = URI.create(url.trim());
			final String scheme = uri.getScheme();
			if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
				return false;
			}
			final String host = uri.getHost();
			if (host == null) {
				return false;
			}
			final String lowerHost = host.toLowerCase(Locale.ROOT);
			if (!"lemanbleu.ch".equals(lowerHost)
					&& !"www.lemanbleu.ch".equals(lowerHost)
					&& !"videos.lemanbleu.ch".equals(lowerHost)) {
				return false;
			}
			final String path = uri.getPath();
			return path != null && path.toLowerCase(Locale.ROOT).contains("/fr/emissions/");
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

	static String toVideosHostUrl(final String url) {
		if (StringUtils.isEmpty(url)) {
			return url;
		}
		if (url.startsWith("http://www.lemanbleu.ch") || url.startsWith("https://www.lemanbleu.ch")) {
			return url.replace("://www.lemanbleu.ch", "://videos.lemanbleu.ch");
		}
		if (url.startsWith("/")) {
			return LemanBleuConf.HOME_URL + url;
		}
		return url;
	}

	static List<ProgramRef> parsePrograms(final String html) {
		if (StringUtils.isEmpty(html)) {
			return Collections.emptyList();
		}
		final Map<String, ProgramRef> byId = new LinkedHashMap<String, ProgramRef>();
		final Matcher matcher = PROGRAM_LINK.matcher(html);
		while (matcher.find()) {
			final String id = matcher.group(2);
			final String title = unescape(matcher.group(3));
			if (StringUtils.isNotEmpty(id) && StringUtils.isNotEmpty(title) && !byId.containsKey(id)) {
				byId.put(id, new ProgramRef(id, title));
			}
		}
		final Matcher alt = PROGRAM_LINK_TITLE_FIRST.matcher(html);
		while (alt.find()) {
			final String title = unescape(alt.group(1));
			final String id = alt.group(3);
			if (StringUtils.isNotEmpty(id) && StringUtils.isNotEmpty(title) && !byId.containsKey(id)) {
				byId.put(id, new ProgramRef(id, title));
			}
		}
		return new ArrayList<ProgramRef>(byId.values());
	}

	static List<EpisodeRef> parseArchiveEpisodes(final String html) {
		if (StringUtils.isEmpty(html)) {
			return Collections.emptyList();
		}
		final List<EpisodeRef> episodes = new ArrayList<EpisodeRef>();
		final Matcher matcher = EPISODE_CARD.matcher(html);
		while (matcher.find()) {
			final String block = matcher.group(1);
			final String path = matcher.group(2);
			final String text = firstGroup(ITEM_TEXT, block);
			final String desc = firstGroup(ITEM_DESC, block);
			final String date = firstGroup(ITEM_DATE, block);
			final String title = buildEpisodeTitle(text, desc, date);
			if (StringUtils.isEmpty(path) || StringUtils.isEmpty(title)) {
				continue;
			}
			episodes.add(new EpisodeRef(title, toVideosHostUrl(path)));
		}
		if (!episodes.isEmpty()) {
			return episodes;
		}
		// Fallback: bare episode links.
		final Matcher links = Pattern.compile("href=\"(/fr/Emissions/[^\"]+\\.html)\"",
				Pattern.CASE_INSENSITIVE).matcher(html);
		while (links.find()) {
			final String path = links.group(1);
			final String slug = path.substring(path.lastIndexOf('/') + 1).replace(".html", "");
			episodes.add(new EpisodeRef(slug.replace('-', ' '), toVideosHostUrl(path)));
		}
		return episodes;
	}

	static String extractBestMp4Url(final String episodeHtml) {
		if (StringUtils.isEmpty(episodeHtml)) {
			return null;
		}
		final Matcher matcher = DATA_VIDEO_URL.matcher(episodeHtml);
		if (!matcher.find()) {
			return null;
		}
		final String raw = matcher.group(1);
		String best = null;
		int bestScore = -1;
		for (final String token : raw.split(",")) {
			final String part = token.trim();
			if (!part.contains(".mp4")) {
				continue;
			}
			final String[] bits = part.split("\\|");
			final String url = bits[0].trim();
			if (!url.startsWith("http://") && !url.startsWith("https://")) {
				continue;
			}
			final String quality = bits.length > 1 ? bits[1].trim().toUpperCase(Locale.ROOT) : "";
			final int score = qualityScore(quality);
			if (score > bestScore) {
				bestScore = score;
				best = url;
			}
		}
		return best;
	}

	static String extractEpisodePageTitle(final String episodeHtml) {
		if (StringUtils.isEmpty(episodeHtml)) {
			return null;
		}
		final Matcher dataTitle = DATA_TITLE.matcher(episodeHtml);
		if (dataTitle.find()) {
			final String value = unescape(dataTitle.group(1)).replace('|', ' ').trim();
			if (StringUtils.isNotEmpty(value)) {
				return value;
			}
		}
		final Matcher h1 = H1.matcher(episodeHtml);
		if (h1.find()) {
			return stripTags(h1.group(1));
		}
		return null;
	}

	private static int qualityScore(final String quality) {
		if ("HD".equals(quality)) {
			return 4;
		}
		if ("HQ".equals(quality)) {
			return 3;
		}
		if ("MD".equals(quality)) {
			return 2;
		}
		if ("SD".equals(quality)) {
			return 1;
		}
		return 0;
	}

	private static String buildEpisodeTitle(final String text, final String desc, final String date) {
		final StringBuilder title = new StringBuilder();
		if (StringUtils.isNotEmpty(text)) {
			title.append(unescape(text).trim());
		}
		if (StringUtils.isNotEmpty(desc)) {
			if (title.length() > 0) {
				title.append(" - ");
			}
			title.append(unescape(desc).trim());
		}
		if (StringUtils.isNotEmpty(date)) {
			if (title.length() > 0) {
				title.append(" (");
				title.append(unescape(date).trim());
				title.append(")");
			} else {
				title.append(unescape(date).trim());
			}
		}
		return title.toString();
	}

	private static String firstGroup(final Pattern pattern, final String input) {
		final Matcher matcher = pattern.matcher(input);
		return matcher.find() ? matcher.group(1) : null;
	}

	private static String stripTags(final String html) {
		return unescape(html.replaceAll("(?s)<[^>]+>", " ").replaceAll("\\s+", " ").trim());
	}

	private static String unescape(final String value) {
		if (value == null) {
			return null;
		}
		return value.replace("&amp;", "&").replace("&quot;", "\"").replace("&#232;", "è")
				.replace("&#233;", "é").replace("&#224;", "à").replace("&eacute;", "é")
				.replace("&egrave;", "è").replace("&agrave;", "à").replace("&#160;", " ")
				.replace("&nbsp;", " ").trim();
	}

	static final class ProgramRef {
		final String id;
		final String title;

		ProgramRef(final String id, final String title) {
			this.id = id;
			this.title = title;
		}
	}

	static final class EpisodeRef {
		final String title;
		final String url;

		EpisodeRef(final String title, final String url) {
			this.title = title;
			this.url = url;
		}
	}
}
