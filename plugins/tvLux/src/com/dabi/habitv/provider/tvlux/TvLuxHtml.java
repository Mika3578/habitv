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
			String hrefValue = html.substring(start, end).trim();
			if (hrefValue.startsWith("https://www.tvlux.be")) {
				hrefValue = hrefValue.substring("https://www.tvlux.be".length());
			} else if (hrefValue.startsWith("http://www.tvlux.be")) {
				hrefValue = hrefValue.substring("http://www.tvlux.be".length());
			}
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
			if (bySlug.containsKey(slug)) {
				continue;
			}
			bySlug.put(slug, new ShowRef(humanize(slug), slug));
		}
		return new ArrayList<ShowRef>(bySlug.values());
	}

	static List<EpisodeRef> parseEpisodes(final String html) {
		if (StringUtils.isEmpty(html)) {
			return Collections.emptyList();
		}
		final Map<String, EpisodeRef> byUrl = new LinkedHashMap<String, EpisodeRef>();
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
			final String absolute = TvLuxUrls.absoluteUrl(hrefValue);
			if (TvLuxUrls.sanitizeEpisodeUrl(absolute) == null) {
				continue;
			}
			final String path = URI_PATH(absolute);
			final String title = humanize(path.substring(path.lastIndexOf('/') + 1).replaceFirst("_\\d+$", ""));
			if (!byUrl.containsKey(absolute)) {
				byUrl.put(absolute, new EpisodeRef(title, absolute));
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
		final String url = normalized.substring(start, end).trim();
		return url.startsWith("http") ? url : null;
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
		return haystack.toLowerCase(Locale.ROOT).indexOf(needle.toLowerCase(Locale.ROOT), from);
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
