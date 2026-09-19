package com.dabi.habitv.provider.telemb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Index-based HTML helpers for Télé MB emissions pages and Freecaster embeds.
 */
final class TeleMbHtml {

	private static final String VIDEO_ID_MARKER = "data-video-id=\"";

	private TeleMbHtml() {
	}

	static List<ShowRef> parseShows(final String html) {
		if (StringUtils.isEmpty(html)) {
			return Collections.emptyList();
		}
		final Map<String, ShowRef> bySlug = new LinkedHashMap<String, ShowRef>();
		int from = 0;
		while (true) {
			final int href = indexOfIgnoreCase(html, "href=\"", from);
			if (href < 0) {
				break;
			}
			final int start = href + 6;
			final int end = html.indexOf('"', start);
			if (end < 0) {
				break;
			}
			from = end + 1;
			String hrefValue = stripHost(html.substring(start, end).trim());
			if (!hrefValue.startsWith("/emission/")) {
				continue;
			}
			final String path = hrefValue.split("[?#]", 2)[0];
			final String[] parts = path.split("/");
			if (parts.length != 3 || StringUtils.isEmpty(parts[2])) {
				continue;
			}
			final String slug = parts[2];
			if ("emissions".equals(slug) || bySlug.containsKey(slug)) {
				continue;
			}
			final String title = extractNearbyTitle(html, href, end);
			bySlug.put(slug, new ShowRef(StringUtils.isEmpty(title) ? humanize(slug) : title, slug));
		}
		return new ArrayList<ShowRef>(bySlug.values());
	}

	static List<EpisodeRef> parseEpisodes(final String html, final String showSlug) {
		if (StringUtils.isEmpty(html) || StringUtils.isEmpty(showSlug)) {
			return Collections.emptyList();
		}
		final Map<String, EpisodeRef> byUrl = new LinkedHashMap<String, EpisodeRef>();
		final String prefix = "/replay/emission/" + showSlug + "/";
		int from = 0;
		while (true) {
			final int href = indexOfIgnoreCase(html, "href=\"", from);
			if (href < 0) {
				break;
			}
			final int start = href + 6;
			final int end = html.indexOf('"', start);
			if (end < 0) {
				break;
			}
			from = end + 1;
			final String absolute = TeleMbUrls.absoluteUrl(html.substring(start, end).trim());
			final String sanitized = TeleMbUrls.sanitizeEpisodeUrl(absolute);
			if (sanitized == null) {
				continue;
			}
			final String path = uriPath(sanitized);
			if (!path.startsWith(prefix)) {
				continue;
			}
			final String title = extractNearbyTitle(html, href, end);
			final String resolvedTitle = StringUtils.isEmpty(title) ? humanize(pathLastSegmentBeforeId(path)) : title;
			if (!byUrl.containsKey(sanitized)) {
				byUrl.put(sanitized, new EpisodeRef(resolvedTitle, sanitized));
			}
		}
		return new ArrayList<EpisodeRef>(byUrl.values());
	}

	static String extractVideoId(final String html) {
		if (StringUtils.isEmpty(html)) {
			return null;
		}
		final String lower = html.toLowerCase(Locale.ROOT);
		final int marker = lower.indexOf(VIDEO_ID_MARKER);
		if (marker < 0) {
			return null;
		}
		final int start = marker + VIDEO_ID_MARKER.length();
		final int end = html.indexOf('"', start);
		if (end <= start) {
			return null;
		}
		final String id = html.substring(start, end).trim();
		return StringUtils.isEmpty(id) ? null : id;
	}

	static String extractHlsUrl(final String embedHtml) {
		if (StringUtils.isEmpty(embedHtml)) {
			return null;
		}
		final String normalized = embedHtml.replace("\\/", "/");
		final String marker = "\"type\":\"application/x-mpegurl\",\"src\":\"";
		final int idx = indexOfIgnoreCase(normalized, marker, 0);
		if (idx < 0) {
			return null;
		}
		final int start = idx + marker.length();
		final int end = normalized.indexOf('"', start);
		if (end <= start) {
			return null;
		}
		return TeleMbUrls.sanitizeHlsUrl(normalized.substring(start, end).trim());
	}

	private static String stripHost(final String hrefValue) {
		if (hrefValue.startsWith("https://www.telemb.be")) {
			return hrefValue.substring("https://www.telemb.be".length());
		}
		if (hrefValue.startsWith("http://www.telemb.be")) {
			return hrefValue.substring("http://www.telemb.be".length());
		}
		if (hrefValue.startsWith("https://telemb.be")) {
			return hrefValue.substring("https://telemb.be".length());
		}
		if (hrefValue.startsWith("http://telemb.be")) {
			return hrefValue.substring("http://telemb.be".length());
		}
		return hrefValue;
	}

	private static String extractNearbyTitle(final String html, final int hrefPos, final int hrefEnd) {
		final int closeA = indexOfIgnoreCase(html, "</a>", hrefEnd);
		if (closeA > hrefEnd && closeA - hrefEnd < 400) {
			final int tagClose = html.indexOf('>', hrefEnd);
			if (tagClose > hrefEnd && tagClose < closeA) {
				final String inside = html.substring(tagClose + 1, closeA);
				final int spanOpen = indexOfIgnoreCase(inside, "<span>", 0);
				if (spanOpen >= 0) {
					final int spanStart = spanOpen + 6;
					final int spanClose = indexOfIgnoreCase(inside, "</span>", spanStart);
					if (spanClose > spanStart) {
						return cleanTitle(inside.substring(spanStart, spanClose));
					}
				}
				final String direct = cleanTitle(stripTags(inside));
				if (!StringUtils.isEmpty(direct)) {
					return direct;
				}
			}
		}
		final int lookFrom = Math.max(0, hrefPos - 600);
		final String before = html.substring(lookFrom, hrefPos);
		final int h2 = lastIndexOfIgnoreCase(before, "<h2>");
		if (h2 >= 0) {
			final String region = before.substring(h2);
			final int spanOpen = indexOfIgnoreCase(region, "<span>", 0);
			if (spanOpen >= 0) {
				final int spanStart = spanOpen + 6;
				final int spanClose = indexOfIgnoreCase(region, "</span>", spanStart);
				if (spanClose > spanStart) {
					return cleanTitle(region.substring(spanStart, spanClose));
				}
			}
		}
		return null;
	}

	private static String cleanTitle(final String raw) {
		if (raw == null) {
			return null;
		}
		final String collapsed = collapseWhitespace(StringEscapeUtils.unescapeHtml(raw).replace('\u00a0', ' '));
		return StringUtils.isEmpty(collapsed) ? null : collapsed;
	}

	private static String stripTags(final String raw) {
		final StringBuilder out = new StringBuilder(raw.length());
		boolean inTag = false;
		for (int i = 0; i < raw.length(); i++) {
			final char c = raw.charAt(i);
			if (c == '<') {
				inTag = true;
				out.append(' ');
				continue;
			}
			if (c == '>') {
				inTag = false;
				continue;
			}
			if (!inTag) {
				out.append(c);
			}
		}
		return out.toString();
	}

	private static String collapseWhitespace(final String raw) {
		final StringBuilder out = new StringBuilder(raw.length());
		boolean gap = false;
		for (int i = 0; i < raw.length(); i++) {
			final char c = raw.charAt(i);
			if (Character.isWhitespace(c)) {
				gap = true;
				continue;
			}
			if (gap && out.length() > 0) {
				out.append(' ');
			}
			gap = false;
			out.append(c);
		}
		return out.toString().trim();
	}

	private static String pathLastSegmentBeforeId(final String path) {
		final String trimmed = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
		final int last = trimmed.lastIndexOf('/');
		if (last < 0) {
			return trimmed;
		}
		final int prev = trimmed.lastIndexOf('/', last - 1);
		if (prev < 0) {
			return trimmed.substring(last + 1);
		}
		return trimmed.substring(prev + 1, last);
	}

	private static String uriPath(final String absolute) {
		try {
			final String path = java.net.URI.create(absolute).getPath();
			return path == null ? absolute : path;
		} catch (final IllegalArgumentException e) {
			return absolute;
		}
	}

	private static int indexOfIgnoreCase(final String haystack, final String needle, final int from) {
		return haystack.toLowerCase(Locale.ROOT).indexOf(needle.toLowerCase(Locale.ROOT), from);
	}

	private static int lastIndexOfIgnoreCase(final String haystack, final String needle) {
		return haystack.toLowerCase(Locale.ROOT).lastIndexOf(needle.toLowerCase(Locale.ROOT));
	}

	private static String humanize(final String slug) {
		if (StringUtils.isEmpty(slug)) {
			return "Episode";
		}
		final String spaced = slug.replace('-', ' ').trim();
		if (spaced.isEmpty()) {
			return slug;
		}
		return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
	}

	/**
	 * Parse an explicit French broadcast date from a title such as
	 * {@code Les Infos du samedi 19 septembre 2026}. Returns null when absent.
	 */
	static java.util.Date parseFrenchBroadcastDate(final String title) {
		if (StringUtils.isEmpty(title)) {
			return null;
		}
		final String lower = title.toLowerCase(Locale.ROOT);
		final String[] months = { "janvier", "février", "fevrier", "mars", "avril", "mai", "juin", "juillet",
				"août", "aout", "septembre", "octobre", "novembre", "décembre", "decembre" };
		final int[] monthNums = { 0, 1, 1, 2, 3, 4, 5, 6, 7, 7, 8, 9, 10, 11, 11 };
		for (int m = 0; m < months.length; m++) {
			final int monthAt = lower.indexOf(months[m]);
			if (monthAt < 0) {
				continue;
			}
			int dayEnd = monthAt;
			while (dayEnd > 0 && Character.isWhitespace(lower.charAt(dayEnd - 1))) {
				dayEnd--;
			}
			int dayStart = dayEnd;
			while (dayStart > 0 && Character.isDigit(lower.charAt(dayStart - 1))) {
				dayStart--;
			}
			if (dayStart >= dayEnd) {
				continue;
			}
			final String dayToken = lower.substring(dayStart, dayEnd);
			int yearStart = monthAt + months[m].length();
			while (yearStart < lower.length() && Character.isWhitespace(lower.charAt(yearStart))) {
				yearStart++;
			}
			int yearEnd = yearStart;
			while (yearEnd < lower.length() && Character.isDigit(lower.charAt(yearEnd))) {
				yearEnd++;
			}
			if (yearEnd - yearStart != 4) {
				continue;
			}
			try {
				final int day = Integer.parseInt(dayToken);
				final int year = Integer.parseInt(lower.substring(yearStart, yearEnd));
				if (day < 1 || day > 31 || year < 1990 || year > 2100) {
					continue;
				}
				final java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
				cal.clear();
				cal.set(java.util.Calendar.YEAR, year);
				cal.set(java.util.Calendar.MONTH, monthNums[m]);
				cal.set(java.util.Calendar.DAY_OF_MONTH, day);
				return cal.getTime();
			} catch (final NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	static final class ShowRef {
		final String title;
		final String slug;

		ShowRef(final String title, final String slug) {
			this.title = title;
			this.slug = slug;
		}
	}

	static final class EpisodeRef {
		final String title;
		final String watchUrl;

		EpisodeRef(final String title, final String watchUrl) {
			this.title = title;
			this.watchUrl = watchUrl;
		}
	}
}
