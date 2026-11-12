package com.dabi.habitv.provider.tvcom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Index-based HTML helpers for TVCom emissions pages and Freecaster embeds.
 */
final class TvComHtml {

	private static final String VIDEO_ID_MARKER = "data-video-id=\"";

	private TvComHtml() {
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
			String hrefValue = html.substring(start, end).trim();
			hrefValue = stripHost(hrefValue);
			if (!hrefValue.startsWith("/emission/")) {
				continue;
			}
			final String path = hrefValue.split("[?#]", 2)[0];
			final String[] parts = path.split("/");
			// /emission/{slug}
			if (parts.length != 3 || StringUtils.isEmpty(parts[2])) {
				continue;
			}
			final String slug = parts[2];
			if ("emissions".equals(slug) || bySlug.containsKey(slug)) {
				continue;
			}
			final String title = extractNearbyTitle(html, href, end) ;
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
			String hrefValue = html.substring(start, end).trim();
			final String absolute = TvComUrls.absoluteUrl(hrefValue);
			final String sanitized = TvComUrls.sanitizeEpisodeUrl(absolute);
			if (sanitized == null) {
				continue;
			}
			final String path = uriPath(sanitized);
			final boolean showEpisode = path.startsWith(prefix);
			final boolean legacyEpisode = path.startsWith("/replay/emissions/");
			if (!showEpisode && !legacyEpisode) {
				continue;
			}
			final String title = extractNearbyTitle(html, href, end);
			final String fallback = humanize(pathLastSegmentBeforeId(path));
			final String resolvedTitle = StringUtils.isEmpty(title) ? fallback : title;
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
		return TvComUrls.sanitizeHlsUrl(normalized.substring(start, end).trim());
	}

	private static String stripHost(final String hrefValue) {
		if (hrefValue.startsWith("https://www.tvcom.be")) {
			return hrefValue.substring("https://www.tvcom.be".length());
		}
		if (hrefValue.startsWith("http://www.tvcom.be")) {
			return hrefValue.substring("http://www.tvcom.be".length());
		}
		if (hrefValue.startsWith("https://tvcom.be")) {
			return hrefValue.substring("https://tvcom.be".length());
		}
		if (hrefValue.startsWith("http://tvcom.be")) {
			return hrefValue.substring("http://tvcom.be".length());
		}
		return hrefValue;
	}

	private static String extractNearbyTitle(final String html, final int hrefPos, final int hrefEnd) {
		// Prefer a <span> title inside the same anchor.
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
		// Look backward for <h2><span>Title</span></h2> (episode cards).
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
		final String unescaped = StringEscapeUtils.unescapeHtml(raw);
		final String collapsed = collapseWhitespace(unescaped.replace('\u00a0', ' '));
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
		return StringUtils.indexOfIgnoreCase(haystack, needle, from);
	}

	private static int lastIndexOfIgnoreCase(final String haystack, final String needle) {
		return StringUtils.lastIndexOfIgnoreCase(haystack, needle);
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
