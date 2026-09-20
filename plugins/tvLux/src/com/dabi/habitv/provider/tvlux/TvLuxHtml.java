package com.dabi.habitv.provider.tvlux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Index-based HTML helpers for TV Lux replay pages and Freecaster embeds.
 */
final class TvLuxHtml {

	private static final String VIDEO_ID_MARKER = "data-video-id=\"";

	private TvLuxHtml() {
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
			if (!hrefValue.startsWith("/replay/")) {
				continue;
			}
			final String path = hrefValue.split("[?#]", 2)[0];
			final String[] parts = path.split("/");
			// /replay/{slug} only
			if (parts.length != 3 || StringUtils.isEmpty(parts[2]) || parts[2].startsWith("page_")) {
				continue;
			}
			final String slug = parts[2];
			if (!TvLuxUrls.isSafeShowSlug(slug) || bySlug.containsKey(slug.toLowerCase(Locale.ROOT))) {
				continue;
			}
			final String normalizedSlug = slug.toLowerCase(Locale.ROOT);
			final String anchorTitle = extractAnchorTitle(html, end);
			final String title = StringUtils.isEmpty(anchorTitle) ? humanize(normalizedSlug) : anchorTitle;
			bySlug.put(normalizedSlug, new ShowRef(title, normalizedSlug));
		}
		return new ArrayList<ShowRef>(bySlug.values());
	}

	static List<EpisodeRef> parseEpisodes(final String html, final String showSlug) {
		if (StringUtils.isEmpty(html) || StringUtils.isEmpty(showSlug)) {
			return Collections.emptyList();
		}
		final Map<String, EpisodeRef> byUrl = new LinkedHashMap<String, EpisodeRef>();
		final String prefix = "/replay/" + showSlug + "/";
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
			final String hrefValue = html.substring(start, end).trim();
			final String sanitized = TvLuxUrls.sanitizeEpisodeUrl(TvLuxUrls.absoluteUrl(hrefValue));
			if (sanitized == null) {
				continue;
			}
			final String path = URI_PATH(sanitized);
			if (!path.startsWith(prefix)) {
				continue;
			}
			final String anchorTitle = extractAnchorTitle(html, end);
			final String title = StringUtils.isEmpty(anchorTitle)
					? humanize(stripTrailingEpisodeId(pathLastSegment(path)))
					: anchorTitle;
			if (!byUrl.containsKey(sanitized)) {
				byUrl.put(sanitized, new EpisodeRef(title, sanitized));
			}
		}
		return new ArrayList<EpisodeRef>(byUrl.values());
	}

	private static String extractAnchorTitle(final String html, final int hrefQuoteEnd) {
		final int tagClose = html.indexOf('>', hrefQuoteEnd);
		if (tagClose < 0) {
			return null;
		}
		final int closeA = indexOfIgnoreCase(html, "</a>", tagClose);
		if (closeA <= tagClose || closeA - tagClose > 400) {
			return null;
		}
		final String inside = html.substring(tagClose + 1, closeA);
		final String direct = cleanTitle(stripTags(inside));
		return StringUtils.isEmpty(direct) ? null : direct;
	}

	private static String cleanTitle(final String raw) {
		if (raw == null) {
			return null;
		}
		final String collapsed = collapseWhitespace(raw.replace('\u00a0', ' '));
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
		final String url = normalized.substring(start, end).trim();
		return TvLuxUrls.sanitizeHlsUrl(url);
	}

	private static String stripHost(final String hrefValue) {
		if (hrefValue.startsWith("https://www.tvlux.be")) {
			return hrefValue.substring("https://www.tvlux.be".length());
		}
		if (hrefValue.startsWith("http://www.tvlux.be")) {
			return hrefValue.substring("http://www.tvlux.be".length());
		}
		if (hrefValue.startsWith("https://tvlux.be")) {
			return hrefValue.substring("https://tvlux.be".length());
		}
		if (hrefValue.startsWith("http://tvlux.be")) {
			return hrefValue.substring("http://tvlux.be".length());
		}
		return hrefValue;
	}

	private static String pathLastSegment(final String path) {
		final int slashPos = path.lastIndexOf('/');
		return slashPos < 0 ? path : path.substring(slashPos + 1);
	}

	private static String stripTrailingEpisodeId(final String segment) {
		final int underscore = segment.lastIndexOf('_');
		if (underscore <= 0 || underscore >= segment.length() - 1) {
			return segment;
		}
		for (int i = underscore + 1; i < segment.length(); i++) {
			if (!Character.isDigit(segment.charAt(i))) {
				return segment;
			}
		}
		return segment.substring(0, underscore);
	}

	private static String URI_PATH(final String absolute) {
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
