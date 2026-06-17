package com.dabi.habitv.provider.novo19;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.provider.novo19.dto.Novo19BffPage;
import com.dabi.habitv.provider.novo19.dto.Novo19Rail;
import com.dabi.habitv.provider.novo19.dto.Novo19Season;
import com.dabi.habitv.provider.novo19.dto.Novo19Tile;
import com.dabi.habitv.provider.novo19.dto.Novo19TilesResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

final class Novo19PageParser {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private Novo19PageParser() {
	}

	static Novo19BffPage parsePageEnvelope(final String json, final String sourceUrl) {
		final JsonNode root = parseRoot(json, sourceUrl);
		if (root == null || root.isMissingNode()) {
			return new Novo19BffPage(null, null, null, null, null, null);
		}
		final JsonNode page = root.has("page") ? root.path("page") : root;
		return parsePageNode(page);
	}

	static String resolveTilesSourcePath(final String json) {
		final JsonNode root = readRootOrNull(json);
		if (root == null || root.isMissingNode() || root.has("tiles")) {
			return null;
		}
		final String src = textValue(root.path("page"), "src");
		return StringUtils.isEmpty(src) ? null : src;
	}

	static Novo19TilesResponse parseTilesEnvelope(final String json, final String sourceUrl) {
		final JsonNode root = readRootOrNull(json);
		if (root == null || root.isMissingNode()) {
			return new Novo19TilesResponse(Collections.<Novo19Tile>emptyList(), null, false);
		}
		final JsonNode data = root.has("tiles") ? root : root.path("data");
		final List<Novo19Tile> tiles = new ArrayList<>();
		final JsonNode tilesNode = data.path("tiles");
		if (tilesNode.isArray()) {
			for (final JsonNode tileNode : tilesNode) {
				final Novo19Tile tile = parseTileSafely(tileNode);
				if (tile != null) {
					tiles.add(tile);
				}
			}
		}
		final String moreHref = readMoreHref(data.path("more"));
		return new Novo19TilesResponse(tiles, moreHref, true);
	}

	private static Novo19BffPage parsePageNode(final JsonNode page) {
		if (page == null || page.isMissingNode()) {
			return new Novo19BffPage(null, null, null, null, null, null);
		}
		final List<Novo19Rail> rails = new ArrayList<>();
		final JsonNode railsNode = page.path("rails");
		if (railsNode.isArray()) {
			for (final JsonNode railNode : railsNode) {
				final Novo19Rail rail = parseRail(railNode);
				if (rail != null && !StringUtils.isEmpty(rail.getSrc())) {
					rails.add(rail);
				}
			}
		}
		final List<Novo19Season> seasons = parseSeasons(page.path("seasons"));
		Novo19Tile content = parseTileSafely(page.path("content"));
		content = enrichContentFromPlaybackInfos(page, content);
		final List<String> contentCategories = parseStringArray(page.path("content").path("category"));
		return new Novo19BffPage(textValue(page, "type"), textValue(page, "id"), textValue(page, "title"), rails,
				seasons, content, contentCategories);
	}

	private static Novo19Tile enrichContentFromPlaybackInfos(final JsonNode pageNode, final Novo19Tile content) {
		if (content == null || pageNode == null || pageNode.isMissingNode()) {
			return content;
		}
		final JsonNode playbackInfos = pageNode.path("playbackInfos");
		if (!playbackInfos.isArray()) {
			return content;
		}
		String href = content.getHref();
		String assetId = content.getAssetId();
		for (final JsonNode infoNode : playbackInfos) {
			if (!"COMPLETE".equalsIgnoreCase(textValue(infoNode, "type"))) {
				continue;
			}
			if (StringUtils.isEmpty(href)) {
				href = textValue(infoNode, "player");
			}
			if (StringUtils.isEmpty(assetId)) {
				assetId = textValue(infoNode, "assetId");
			}
			break;
		}
		if (href == content.getHref() && assetId == content.getAssetId()) {
			return content;
		}
		return new Novo19Tile(content.getId(), content.getType(), content.getTitle(), content.getSubtitle(),
				content.getDescription(), content.getDurationSeconds(), href, assetId, content.getPublishedAt());
	}

	private static List<String> parseStringArray(final JsonNode arrayNode) {
		final List<String> values = new ArrayList<String>();
		if (arrayNode == null || !arrayNode.isArray()) {
			return values;
		}
		for (final JsonNode item : arrayNode) {
			if (item != null && !item.isNull()) {
				final String text = item.asText(null);
				if (!StringUtils.isEmpty(text)) {
					values.add(text.trim());
				}
			}
		}
		return values;
	}

	private static Novo19Rail parseRail(final JsonNode railNode) {
		if (railNode == null || railNode.isMissingNode()) {
			return null;
		}
		return new Novo19Rail(textValue(railNode, "id"), textValue(railNode, "type"), textValue(railNode, "title"),
				textValue(railNode, "src"), readMoreHref(railNode.path("more")));
	}

	private static List<Novo19Season> parseSeasons(final JsonNode seasonsNode) {
		final List<Novo19Season> seasons = new ArrayList<>();
		if (!seasonsNode.isArray()) {
			return seasons;
		}
		int index = 0;
		for (final JsonNode seasonNode : seasonsNode) {
			final List<Novo19Tile> episodes = new ArrayList<>();
			final JsonNode episodesNode = seasonNode.path("episodes");
			if (episodesNode.isArray()) {
				for (final JsonNode episodeNode : episodesNode) {
					final Novo19Tile episode = parseTileSafely(episodeNode);
					if (episode != null) {
						episodes.add(episode);
					}
				}
			}
			final String title = textValue(seasonNode, "title");
			if (!episodes.isEmpty() || !StringUtils.isEmpty(title)) {
				seasons.add(new Novo19Season(title, index, episodes));
			}
			index++;
		}
		return seasons;
	}

	static Novo19Tile parseTile(final JsonNode tileNode) {
		if (tileNode == null || tileNode.isMissingNode() || tileNode.isNull()) {
			return null;
		}
		final String type = textValue(tileNode, "type");
		if (!Novo19PathRules.isSupportedTileType(type) && !"TRAILER".equals(type)) {
			return null;
		}
		if ("TRAILER".equals(type)) {
			return null;
		}
		final String href = textValue(tileNode, "href");
		if (!StringUtils.isEmpty(href) && Novo19PathRules.isExcludedPublicPath(href)) {
			return null;
		}
		final String assetId = resolveAssetId(tileNode);
		Long duration = null;
		if (tileNode.has("duration")) {
			final JsonNode durationNode = tileNode.get("duration");
			if (durationNode != null && durationNode.isNumber()) {
				final long value = durationNode.asLong(0L);
				if (value > 0L) {
					duration = Long.valueOf(value);
				}
			}
		}
		return new Novo19Tile(textValue(tileNode, "id"), type, textValue(tileNode, "title"),
				textValue(tileNode, "subtitle"), textValue(tileNode, "description"), duration, href, assetId,
				textValue(tileNode, "publishedAt"));
	}

	private static String resolveAssetId(final JsonNode tileNode) {
		final String explicit = textValue(tileNode, "assetId");
		if (!StringUtils.isEmpty(explicit)) {
			return explicit;
		}
		final String id = textValue(tileNode, "id");
		if (!StringUtils.isEmpty(id)) {
			return id;
		}
		return textValue(tileNode.path("source"), "id");
	}

	private static String readMoreHref(final JsonNode moreNode) {
		if (moreNode == null || moreNode.isMissingNode()) {
			return null;
		}
		return textValue(moreNode, "href");
	}

	private static String textValue(final JsonNode node, final String field) {
		if (node == null || node.isMissingNode()) {
			return null;
		}
		final String value = node.path(field).asText(null);
		return StringUtils.isEmpty(value) ? null : value.trim();
	}

	private static Novo19Tile parseTileSafely(final JsonNode tileNode) {
		try {
			return parseTile(tileNode);
		} catch (final RuntimeException e) {
			return null;
		}
	}

	private static JsonNode readRootOrNull(final String json) {
		if (StringUtils.isEmpty(json)) {
			return null;
		}
		try {
			return MAPPER.readTree(json);
		} catch (final IOException e) {
			return null;
		}
	}

	private static JsonNode parseRoot(final String json, final String sourceUrl) {
		if (StringUtils.isEmpty(json)) {
			return null;
		}
		final JsonNode root = readRootOrNull(json);
		if (root == null) {
			throw new TechnicalException("Cannot parse NOVO19 BFF response from " + sourceUrl);
		}
		return root;
	}

}
