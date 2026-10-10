package com.dabi.habitv.provider.tfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Index-based HTML/JSON helpers for TFO catalogue and watch pages.
 * Avoids ReDoS-prone nested attribute regexes on untrusted HTML.
 */
final class TfoHtml {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final TypeReference<List<Map<String, Object>>> LIST_OF_MAP =
			new TypeReference<List<Map<String, Object>>>() {
			};

	private TfoHtml() {
	}

	static List<CatalogItem> parseCatalogItems(final String html) throws IOException {
		if (StringUtils.isEmpty(html)) {
			return Collections.emptyList();
		}
		final String attrJson = extractAttributeJson(html, "data-items");
		if (attrJson == null) {
			return Collections.emptyList();
		}
		final List<Map<String, Object>> rawItems = MAPPER.readValue(attrJson, LIST_OF_MAP);
		final List<CatalogItem> items = new ArrayList<CatalogItem>();
		final Map<String, CatalogItem> byPath = new LinkedHashMap<String, CatalogItem>();
		for (final Map<String, Object> raw : rawItems) {
			if (raw == null) {
				continue;
			}
			final String title = asString(raw.get("title"));
			final String path = TfoUrls.normalizePath(asString(raw.get("url")));
			if (StringUtils.isEmpty(title) || StringUtils.isEmpty(path)) {
				continue;
			}
			if (!TfoUrls.isSeriePath(path) && !TfoUrls.isProductPath(path)) {
				continue;
			}
			if (byPath.containsKey(path)) {
				continue;
			}
			final CatalogItem item = new CatalogItem(title, path);
			byPath.put(path, item);
			items.add(item);
		}
		return items;
	}

	static List<Map<String, Object>> parseSeasons(final String html) throws IOException {
		if (StringUtils.isEmpty(html)) {
			return Collections.emptyList();
		}
		final String normalized = html.replace("&quot;", "\"").replace("\\/", "/");
		final String marker = "\"seasons\":";
		final int markerAt = normalized.indexOf(marker);
		if (markerAt < 0) {
			return Collections.emptyList();
		}
		int arrayStart = markerAt + marker.length();
		while (arrayStart < normalized.length()
				&& Character.isWhitespace(normalized.charAt(arrayStart))) {
			arrayStart++;
		}
		final String arrayJson = extractJsonArray(normalized, arrayStart);
		if (arrayJson == null) {
			return Collections.emptyList();
		}
		return MAPPER.readValue(arrayJson, LIST_OF_MAP);
	}

	static List<EpisodeRef> episodesFromSeasons(final List<Map<String, Object>> seasons) {
		if (seasons == null || seasons.isEmpty()) {
			return Collections.emptyList();
		}
		final List<EpisodeRef> episodes = new ArrayList<EpisodeRef>();
		for (final Map<String, Object> season : seasons) {
			if (season == null) {
				continue;
			}
			@SuppressWarnings("unchecked")
			final List<Map<String, Object>> products = (List<Map<String, Object>>) season.get("products");
			if (products == null) {
				continue;
			}
			for (final Map<String, Object> product : products) {
				if (product == null) {
					continue;
				}
				final String title = firstNonEmpty(asString(product.get("name")), asString(product.get("episodeName")));
				final String videoPath = asString(product.get("videoUrl"));
				final String watchUrl = TfoUrls.absoluteUrl(videoPath);
				if (StringUtils.isEmpty(title) || !TfoUrls.isTfoWatchUrl(watchUrl)) {
					continue;
				}
				final Integer epNum = asInt(product.get("episodeNumber"));
				final Integer seNum = asInt(product.get("seasonNumber"));
				episodes.add(new EpisodeRef(title, watchUrl, seNum, epNum, asString(product.get("description"))));
			}
		}
		return episodes;
	}

	static WatchMedia extractWatchMedia(final String watchHtml) {
		if (StringUtils.isEmpty(watchHtml)) {
			return null;
		}
		final String playlist = extractAttributeValue(watchHtml, "data-video-playlist");
		final String videoId = extractAttributeValue(watchHtml, "data-video-id");
		if (StringUtils.isEmpty(playlist) || !playlist.toLowerCase(Locale.ROOT).contains(".m3u8")) {
			return null;
		}
		return new WatchMedia(playlist, videoId);
	}

	/**
	 * Locate {@code attr="..."} with index scans and decode HTML entities.
	 */
	static String extractAttributeJson(final String html, final String attrName) {
		final String value = extractAttributeValue(html, attrName);
		if (value == null) {
			return null;
		}
		return value.replace("&quot;", "\"").replace("&#39;", "'").replace("&amp;", "&").replace("\\/", "/");
	}

	static String extractAttributeValue(final String html, final String attrName) {
		final String lower = html.toLowerCase(Locale.ROOT);
		final String needle = attrName.toLowerCase(Locale.ROOT) + "=\"";
		final int start = lower.indexOf(needle);
		if (start < 0) {
			return null;
		}
		final int valueStart = start + needle.length();
		final int valueEnd = html.indexOf('"', valueStart);
		if (valueEnd < 0) {
			return null;
		}
		return html.substring(valueStart, valueEnd);
	}

	static String extractJsonArray(final String text, final int arrayStart) {
		if (arrayStart < 0 || arrayStart >= text.length() || text.charAt(arrayStart) != '[') {
			return null;
		}
		int depth = 0;
		boolean inString = false;
		boolean escape = false;
		for (int i = arrayStart; i < text.length(); i++) {
			final char ch = text.charAt(i);
			if (inString) {
				if (escape) {
					escape = false;
				} else if (ch == '\\') {
					escape = true;
				} else if (ch == '"') {
					inString = false;
				}
				continue;
			}
			if (ch == '"') {
				inString = true;
			} else if (ch == '[') {
				depth++;
			} else if (ch == ']') {
				depth--;
				if (depth == 0) {
					return text.substring(arrayStart, i + 1);
				}
			}
		}
		return null;
	}

	static String asString(final Object value) {
		return value == null ? null : String.valueOf(value).trim();
	}

	static Integer asInt(final Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return Integer.valueOf(((Number) value).intValue());
		}
		final String text = String.valueOf(value).trim();
		if (text.isEmpty()) {
			return null;
		}
		try {
			return Integer.valueOf(Integer.parseInt(text));
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	private static String firstNonEmpty(final String first, final String second) {
		if (StringUtils.isNotEmpty(first)) {
			return first;
		}
		if (StringUtils.isNotEmpty(second)) {
			return second;
		}
		return "";
	}

	static final class CatalogItem {
		final String title;
		final String path;

		CatalogItem(final String title, final String path) {
			this.title = title;
			this.path = path;
		}
	}

	static final class EpisodeRef {
		final String title;
		final String watchUrl;
		final Integer seasonNumber;
		final Integer episodeNumber;
		final String description;

		EpisodeRef(final String title, final String watchUrl, final Integer seasonNumber,
				final Integer episodeNumber, final String description) {
			this.title = title;
			this.watchUrl = watchUrl;
			this.seasonNumber = seasonNumber;
			this.episodeNumber = episodeNumber;
			this.description = description;
		}
	}

	static final class WatchMedia {
		final String playlistUrl;
		final String videoId;

		WatchMedia(final String playlistUrl, final String videoId) {
			this.playlistUrl = playlistUrl;
			this.videoId = videoId;
		}
	}
}
