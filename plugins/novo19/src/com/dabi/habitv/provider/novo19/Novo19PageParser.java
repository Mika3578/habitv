package com.dabi.habitv.provider.novo19;

import java.io.IOException;
import java.util.ArrayList;
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

	static Novo19TilesResponse parseTilesEnvelope(final String json, final String sourceUrl) {
		final JsonNode root = parseRoot(json, sourceUrl);
		if (root == null || root.isMissingNode()) {
			return new Novo19TilesResponse(null, null);
		}
		final JsonNode data = root.has("tiles") ? root : root;
		final List<Novo19Tile> tiles = new ArrayList<>();
		final JsonNode tilesNode = data.path("tiles");
		if (tilesNode.isArray()) {
			for (final JsonNode tileNode : tilesNode) {
				final Novo19Tile tile = parseTile(tileNode);
				if (tile != null) {
					tiles.add(tile);
				}
			}
		}
		final String moreHref = readMoreHref(data.path("more"));
		return new Novo19TilesResponse(tiles, moreHref);
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
		final Novo19Tile content = parseTile(page.path("content"));
		return new Novo19BffPage(textValue(page, "type"), textValue(page, "id"), textValue(page, "title"), rails,
				seasons, content);
	}

	private static Novo19Rail parseRail(final JsonNode railNode) {
		if (railNode == null || railNode.isMissingNode()) {
			return null;
		}
		return new Novo19Rail(textValue(railNode, "id"), textValue(railNode, "title"), textValue(railNode, "src"),
				readMoreHref(railNode.path("more")));
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
					final Novo19Tile episode = parseTile(episodeNode);
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
		if (Novo19PathRules.isExcludedPublicPath(href)) {
			return null;
		}
		final String assetId = resolveAssetId(tileNode);
		Long duration = null;
		if (tileNode.has("duration")) {
			duration = Long.valueOf(tileNode.path("duration").asLong(0L));
			if (duration.longValue() <= 0L) {
				duration = null;
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

	private static JsonNode parseRoot(final String json, final String sourceUrl) {
		if (StringUtils.isEmpty(json)) {
			return null;
		}
		try {
			return MAPPER.readTree(json);
		} catch (final IOException e) {
			throw new TechnicalException("Cannot parse NOVO19 BFF response from " + sourceUrl, e);
		}
	}

}
