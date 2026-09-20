package com.dabi.habitv.provider.tvaplus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Next.js {@code __NEXT_DATA__} helpers for TVA+. Uses index scans instead of
 * ReDoS-prone script regexes.
 */
final class TvaPlusHtml {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final String NEXT_DATA_MARKER = "id=\"__next_data__\"";

	private TvaPlusHtml() {
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

	static List<ShowRef> parseTvaShows(final String html) throws IOException {
		final JsonNode entity = staticEntity(html);
		if (entity == null) {
			return Collections.emptyList();
		}
		final Map<String, ShowRef> bySlug = new LinkedHashMap<String, ShowRef>();
		collectShows(entity, bySlug);
		return new ArrayList<ShowRef>(bySlug.values());
	}

	static List<SeasonRef> parseShowSeasons(final String html) throws IOException {
		final JsonNode entity = staticEntity(html);
		if (entity == null) {
			return Collections.emptyList();
		}
		final JsonNode seasons = entity.path("knownEntities").path("seasons").path("associatedEntities");
		final List<SeasonRef> result = new ArrayList<SeasonRef>();
		if (!seasons.isArray()) {
			return result;
		}
		for (final JsonNode season : seasons) {
			final String slug = text(season, "slug");
			if (!TvaPlusUrls.isSafeRelativeTvaPath(slug)) {
				continue;
			}
			final Integer number = asInt(season.get("seasonNumber"));
			result.add(new SeasonRef(slug, number, text(season, "name")));
		}
		return result;
	}

	static List<EpisodeRef> parseSeasonEpisodes(final String html) throws IOException {
		final JsonNode entity = staticEntity(html);
		if (entity == null) {
			return Collections.emptyList();
		}
		final List<EpisodeRef> episodes = new ArrayList<EpisodeRef>();
		final Set<String> seen = new LinkedHashSet<String>();

		final JsonNode related = entity.path("knownEntities").path("relatedVideos").path("associatedEntities");
		if (related.isArray()) {
			for (final JsonNode carousel : related) {
				if (!isEpisodesCarousel(carousel)) {
					continue;
				}
				collectEpisodes(carousel.path("associatedEntities"), episodes, seen);
				final String carouselSlug = text(carousel, "slug");
				if (episodes.isEmpty() && StringUtils.isNotEmpty(carouselSlug)) {
					// Caller may fetch carouselSlug page when nested list is empty.
				}
			}
		}

		// Season "tous-les-episodes" pages expose episodes on associatedEntities directly.
		if ("CAROUSEL".equalsIgnoreCase(text(entity, "typology"))
				&& isEpisodesCarousel(entity)) {
			collectEpisodes(entity.path("associatedEntities"), episodes, seen);
		}

		collectEpisodes(entity.path("associatedEntities"), episodes, seen);
		return episodes;
	}

	static List<EpisodeRef> parseRecentTvaEpisodes(final String html) throws IOException {
		final JsonNode entity = staticEntity(html);
		if (entity == null) {
			return Collections.emptyList();
		}
		final List<EpisodeRef> episodes = new ArrayList<EpisodeRef>();
		final Set<String> seen = new LinkedHashSet<String>();
		collectEpisodes(entity.path("associatedEntities"), episodes, seen);
		return episodes;
	}

	static String episodesCarouselSlug(final String seasonHtml) throws IOException {
		final JsonNode entity = staticEntity(seasonHtml);
		if (entity == null) {
			return null;
		}
		final JsonNode related = entity.path("knownEntities").path("relatedVideos").path("associatedEntities");
		if (!related.isArray()) {
			return null;
		}
		for (final JsonNode carousel : related) {
			if (isEpisodesCarousel(carousel)) {
				// Always return the dedicated carousel page slug when present.
				// Nested associatedEntities may be non-empty yet contain only
				// non-PUBLIC items that parseSeasonEpisodes already filtered out;
				// the client calls this only after an empty parse result.
				final String slug = text(carousel, "slug");
				return TvaPlusUrls.isSafeRelativeTvaPath(slug) ? slug : null;
			}
		}
		return null;
	}

	private static JsonNode staticEntity(final String html) throws IOException {
		final String json = extractNextDataJson(html);
		if (json == null) {
			return null;
		}
		return MAPPER.readTree(json).path("props").path("pageProps").path("staticEntity");
	}

	private static void collectShows(final JsonNode node, final Map<String, ShowRef> bySlug) {
		if (node == null || node.isMissingNode() || node.isNull()) {
			return;
		}
		if (node.isObject()) {
			if (isVideoShow(node) && isPublic(node)) {
				final String slug = text(node, "slug");
				final String name = firstNonEmpty(text(node, "name"), text(node, "label"), text(node, "title"));
				if (TvaPlusUrls.isSafeCategoryShowSlug(slug) && StringUtils.isNotEmpty(name)
						&& !bySlug.containsKey(slug)) {
					bySlug.put(slug, new ShowRef(name, slug));
				}
			}
			final JsonNode associated = node.get("associatedEntities");
			if (associated != null && associated.isArray()) {
				for (final JsonNode child : associated) {
					collectShows(child, bySlug);
				}
			}
			return;
		}
		if (node.isArray()) {
			for (final JsonNode child : node) {
				collectShows(child, bySlug);
			}
		}
	}

	private static void collectEpisodes(final JsonNode nodes, final List<EpisodeRef> episodes,
			final Set<String> seen) {
		if (nodes == null || !nodes.isArray()) {
			return;
		}
		for (final JsonNode node : nodes) {
			if (!isPublic(node)) {
				continue;
			}
			final String slug = text(node, "slug");
			if (!TvaPlusUrls.isTvaEpisodeSlug(slug) || seen.contains(slug)) {
				continue;
			}
			final String name = firstNonEmpty(text(node, "name"), text(node, "label"), text(node, "title"), slug);
			final Integer season = asInt(node.get("seasonNumber"));
			final Integer episode = asInt(node.get("episodeNumber"));
			final String title = buildEpisodeTitle(name, season, episode, slug);
			seen.add(slug);
			episodes.add(new EpisodeRef(title, TvaPlusUrls.pageUrl(slug), season, episode));
		}
	}

	private static String buildEpisodeTitle(final String name, final Integer season, final Integer episode,
			final String slug) {
		if (StringUtils.isNotEmpty(name) && !name.equals(slug) && !name.startsWith("/")) {
			if (season != null && episode != null) {
				return "S" + season + "E" + episode + " - " + name;
			}
			return name;
		}
		if (season != null && episode != null) {
			return "S" + season + "E" + episode;
		}
		return slug;
	}

	private static boolean isVideoShow(final JsonNode node) {
		final String typology = text(node, "typology");
		final String discriminator = text(node, "discriminator");
		return "VIDEO_SHOW".equalsIgnoreCase(typology) || "VideoShowEntity".equalsIgnoreCase(discriminator);
	}

	private static boolean isEpisodesCarousel(final JsonNode node) {
		final String name = firstNonEmpty(text(node, "name"), text(node, "label"));
		if (StringUtils.isEmpty(name)) {
			return false;
		}
		final String lower = name.toLowerCase(Locale.ROOT);
		return lower.contains("épisode") || lower.contains("episode") || "épisodes".equals(lower)
				|| "episodes".equals(lower);
	}

	private static boolean isPublic(final JsonNode node) {
		final JsonNode groups = node.path("permission").path("groups");
		if (!groups.isArray() || groups.size() == 0) {
			// Fail closed: missing permission metadata must not be treated as free/public.
			return false;
		}
		boolean publicGroup = false;
		for (final JsonNode group : groups) {
			final String value = group.asText();
			if (StringUtils.isEmpty(value)) {
				continue;
			}
			final String upper = value.toUpperCase(Locale.ROOT);
			if (upper.contains("PREMIUM") || upper.contains("SUBSCRIPTION") || upper.contains("ILLICO")
					|| upper.contains("PAY")) {
				return false;
			}
			if ("PUBLIC".equals(upper)) {
				publicGroup = true;
			}
		}
		return publicGroup;
	}

	private static String text(final JsonNode node, final String field) {
		if (node == null || node.isMissingNode()) {
			return null;
		}
		final JsonNode value = node.get(field);
		if (value == null || value.isNull()) {
			return null;
		}
		final String text = value.asText();
		return StringUtils.isEmpty(text) ? null : text.trim();
	}

	private static Integer asInt(final JsonNode node) {
		if (node == null || node.isNull() || node.isMissingNode()) {
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

		ShowRef(final String title, final String slug) {
			this.title = title;
			this.slug = slug;
		}
	}

	static final class SeasonRef {
		final String slug;
		final Integer seasonNumber;
		final String name;

		SeasonRef(final String slug, final Integer seasonNumber, final String name) {
			this.slug = slug;
			this.seasonNumber = seasonNumber;
			this.name = name;
		}
	}

	static final class EpisodeRef {
		final String title;
		final String watchUrl;
		final Integer seasonNumber;
		final Integer episodeNumber;

		EpisodeRef(final String title, final String watchUrl, final Integer seasonNumber,
				final Integer episodeNumber) {
			this.title = title;
			this.watchUrl = watchUrl;
			this.seasonNumber = seasonNumber;
			this.episodeNumber = episodeNumber;
		}
	}
}
