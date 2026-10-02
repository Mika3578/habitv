package com.dabi.habitv.provider.icitoutv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Next.js {@code __NEXT_DATA__} helpers for ICI TOU.TV. Uses index scans instead
 * of ReDoS-prone script regexes.
 */
final class IciToutTvHtml {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final String NEXT_DATA_MARKER = "id=\"__next_data__\"";

	private IciToutTvHtml() {
	}

	static String extractNextDataJson(final String html) {
		if (StringUtils.isEmpty(html)) {
			return null;
		}
		final String lower = html.toLowerCase(Locale.ROOT);
		final int marker = lower.indexOf(NEXT_DATA_MARKER);
		if (marker < 0) {
			return null;
		}
		final int openTagEnd = html.indexOf('>', marker);
		if (openTagEnd < 0 || openTagEnd + 1 >= html.length()) {
			return null;
		}
		final int closeTag = lower.indexOf("</script>", openTagEnd + 1);
		if (closeTag < 0) {
			return null;
		}
		final String json = html.substring(openTagEnd + 1, closeTag).trim();
		return StringUtils.isEmpty(json) ? null : json;
	}

	static List<ShowRef> parseFreeShows(final String html) throws IOException {
		final String json = extractNextDataJson(html);
		if (json == null) {
			return Collections.emptyList();
		}
		final JsonNode items = MAPPER.readTree(json).path("props").path("pageProps").path("data").path("items");
		final List<ShowRef> shows = new ArrayList<ShowRef>();
		if (!items.isArray()) {
			return shows;
		}
		for (final JsonNode item : items) {
			if (!"Show".equalsIgnoreCase(text(item, "type"))) {
				continue;
			}
			final String tier = text(item, "tier");
			if ("Premium".equalsIgnoreCase(tier)) {
				continue;
			}
			final String title = text(item, "title");
			final String slug = text(item, "url");
			if (StringUtils.isEmpty(title) || StringUtils.isEmpty(slug) || slug.contains("/")) {
				continue;
			}
			shows.add(new ShowRef(title, slug, tier));
		}
		return shows;
	}

	static List<EpisodeRef> parseShowEpisodes(final String html) throws IOException {
		final String json = extractNextDataJson(html);
		if (json == null) {
			return Collections.emptyList();
		}
		final JsonNode root = MAPPER.readTree(json).path("props").path("pageProps").path("data");
		final JsonNode content = root.path("content");
		final List<EpisodeRef> episodes = new ArrayList<EpisodeRef>();
		if (!content.isArray()) {
			return episodes;
		}
		for (final JsonNode block : content) {
			final JsonNode lineups = block.path("lineups");
			if (!lineups.isArray()) {
				continue;
			}
			for (final JsonNode lineup : lineups) {
				if ("Premium".equalsIgnoreCase(text(lineup, "tier"))) {
					continue;
				}
				final JsonNode items = lineup.path("items");
				if (!items.isArray()) {
					continue;
				}
				for (final JsonNode item : items) {
					if ("Premium".equalsIgnoreCase(text(item, "tier"))) {
						continue;
					}
					final String path = text(item, "url");
					final String title = firstNonEmpty(text(item, "title"), text(item, "infoTitle"), path);
					final String watchUrl = IciToutTvUrls.absoluteWatchUrl(path);
					if (StringUtils.isEmpty(title) || !IciToutTvUrls.isIciToutTvEpisodeUrl(watchUrl)) {
						continue;
					}
					episodes.add(new EpisodeRef(title, watchUrl, asInt(item.get("idMedia")), text(item, "tier")));
				}
			}
		}
		return episodes;
	}

	private static String text(final JsonNode node, final String field) {
		final JsonNode value = node.get(field);
		if (value == null || value.isNull()) {
			return null;
		}
		final String text = value.asText();
		return StringUtils.isEmpty(text) ? null : text.trim();
	}

	private static Integer asInt(final JsonNode node) {
		if (node == null || node.isNull()) {
			return null;
		}
		if (node.isNumber()) {
			return Integer.valueOf(node.asInt());
		}
		try {
			return Integer.valueOf(Integer.parseInt(node.asText().trim()));
		} catch (final Exception e) {
			return null;
		}
	}

	private static String firstNonEmpty(final String... values) {
		for (final String value : values) {
			if (StringUtils.isNotEmpty(value)) {
				return value;
			}
		}
		return "";
	}

	static final class ShowRef {
		final String title;
		final String slug;
		final String tier;

		ShowRef(final String title, final String slug, final String tier) {
			this.title = title;
			this.slug = slug;
			this.tier = tier;
		}
	}

	static final class EpisodeRef {
		final String title;
		final String watchUrl;
		final Integer idMedia;
		final String tier;

		EpisodeRef(final String title, final String watchUrl, final Integer idMedia, final String tier) {
			this.title = title;
			this.watchUrl = watchUrl;
			this.idMedia = idMedia;
			this.tier = tier;
		}
	}
}
