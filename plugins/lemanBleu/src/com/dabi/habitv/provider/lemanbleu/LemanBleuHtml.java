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
			"<h1[^>]*>([^<]*(?:<(?!/h1>)[^<]*)*)</h1>", Pattern.CASE_INSENSITIVE);

	private static final Pattern FALLBACK_EPISODE_HREF = Pattern.compile(
			"href=\"(/fr/Emissions/[^\"]+\\.html)\"", Pattern.CASE_INSENSITIVE);

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

	/**
	 * Scan {@code <a>} open tags with indexOf only — avoids ReDoS-prone
	 * {@code <a[^>]+...} attribute regexes on untrusted HTML.
	 */
	static List<ProgramRef> parsePrograms(final String html) {
		if (StringUtils.isEmpty(html)) {
			return Collections.emptyList();
		}
		final Map<String, ProgramRef> byId = new LinkedHashMap<String, ProgramRef>();
		final String lower = html.toLowerCase(Locale.ROOT);
		int searchFrom = 0;
		while (true) {
			final int aStart = lower.indexOf("<a", searchFrom);
			if (aStart < 0) {
				break;
			}
			if (aStart + 2 < html.length()) {
				final char next = html.charAt(aStart + 2);
				if (next != ' ' && next != '\t' && next != '\n' && next != '\r' && next != '/') {
					searchFrom = aStart + 2;
					continue;
				}
			}
			final int tagEnd = html.indexOf('>', aStart);
			if (tagEnd < 0) {
				break;
			}
			searchFrom = tagEnd + 1;
			final String tag = html.substring(aStart, tagEnd + 1);
			final String href = quotedAttr(tag, "href");
			final String title = quotedAttr(tag, "title");
			final String id = emissionIdFromHref(href);
			if (StringUtils.isNotEmpty(id) && StringUtils.isNotEmpty(title) && !byId.containsKey(id)) {
				byId.put(id, new ProgramRef(id, unescape(title)));
			}
		}
		return new ArrayList<ProgramRef>(byId.values());
	}

	static List<EpisodeRef> parseArchiveEpisodes(final String html) {
		if (StringUtils.isEmpty(html)) {
			return Collections.emptyList();
		}
		final List<EpisodeRef> episodes = new ArrayList<EpisodeRef>();
		final String lower = html.toLowerCase(Locale.ROOT);
		final String titleMarker = "class=\"videolist-item-title\"";
		int searchFrom = 0;
		while (true) {
			final int titlePos = lower.indexOf(titleMarker, searchFrom);
			if (titlePos < 0) {
				break;
			}
			final int openEnd = html.indexOf('>', titlePos);
			if (openEnd < 0) {
				break;
			}
			final int closeDiv = lower.indexOf("</div>", openEnd + 1);
			if (closeDiv < 0) {
				break;
			}
			final String block = html.substring(openEnd + 1, closeDiv);
			searchFrom = closeDiv + 6;
			final int nextTitle = lower.indexOf(titleMarker, searchFrom);
			final int hrefLimit = nextTitle < 0 ? html.length() : nextTitle;
			final String path = findEmissionHref(html, lower, closeDiv, hrefLimit);
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
		final Matcher links = FALLBACK_EPISODE_HREF.matcher(html);
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

	private static String findEmissionHref(final String html, final String lower, final int from,
			final int limit) {
		int search = from;
		while (search < limit) {
			final int hrefPos = lower.indexOf("href=\"", search);
			if (hrefPos < 0 || hrefPos >= limit) {
				return null;
			}
			final int pathStart = hrefPos + 6;
			final int pathEnd = html.indexOf('"', pathStart);
			if (pathEnd < 0 || pathEnd > limit) {
				return null;
			}
			final String path = html.substring(pathStart, pathEnd);
			if (path.toLowerCase(Locale.ROOT).startsWith("/fr/emissions/")) {
				return path;
			}
			search = pathEnd + 1;
		}
		return null;
	}

	private static String quotedAttr(final String tag, final String name) {
		final String lower = tag.toLowerCase(Locale.ROOT);
		final String needle = name.toLowerCase(Locale.ROOT) + "=\"";
		final int start = lower.indexOf(needle);
		if (start < 0) {
			return null;
		}
		final int valueStart = start + needle.length();
		final int valueEnd = tag.indexOf('"', valueStart);
		if (valueEnd < 0) {
			return null;
		}
		return tag.substring(valueStart, valueEnd);
	}

	private static String emissionIdFromHref(final String href) {
		if (StringUtils.isEmpty(href)) {
			return null;
		}
		final String lower = href.toLowerCase(Locale.ROOT);
		final int idx = lower.indexOf("emission=");
		if (idx < 0) {
			return null;
		}
		final int start = idx + "emission=".length();
		int end = start;
		while (end < href.length() && Character.isDigit(href.charAt(end))) {
			end++;
		}
		if (end == start) {
			return null;
		}
		return href.substring(start, end);
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
		final StringBuilder out = new StringBuilder();
		boolean inTag = false;
		for (int i = 0; i < html.length(); i++) {
			final char c = html.charAt(i);
			if (c == '<') {
				inTag = true;
				continue;
			}
			if (c == '>') {
				inTag = false;
				out.append(' ');
				continue;
			}
			if (!inTag) {
				out.append(c);
			}
		}
		return unescape(out.toString().replaceAll("\\s+", " ").trim());
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
