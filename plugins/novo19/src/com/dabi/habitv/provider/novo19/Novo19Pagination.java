package com.dabi.habitv.provider.novo19;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.provider.novo19.dto.Novo19Tile;
import com.dabi.habitv.provider.novo19.dto.Novo19TilesResponse;

final class Novo19Pagination {

	static final String ROOT_CAUSE_PAGINATION_LIMIT = "pagination-limit";

	interface TilesFetcher {
		String fetchTilesJson(String bffPath) throws IOException;
	}

	private Novo19Pagination() {
	}

	static List<Novo19Tile> loadTiles(final String initialBffPath, final TilesFetcher fetcher,
			final Novo19Diagnostics diagnostics) {
		return loadTiles(initialBffPath, fetcher, diagnostics, Novo19Conf.MAX_PAGINATION_PAGES);
	}

	static List<Novo19Tile> loadTiles(final String initialBffPath, final TilesFetcher fetcher,
			final Novo19Diagnostics diagnostics, final int maxPages) {
		if (Novo19PathRules.isRecommendationBffPath(initialBffPath)) {
			return Collections.emptyList();
		}
		final Set<String> visited = new LinkedHashSet<String>();
		final List<Novo19Tile> tiles = new ArrayList<Novo19Tile>();
		String nextPath = initialBffPath;
		int pageCount = 0;
		boolean hitPaginationLimit = false;
		while (!StringUtils.isEmpty(nextPath) && pageCount < maxPages) {
			if (Novo19PathRules.isRecommendationBffPath(nextPath)) {
				break;
			}
			String absolute;
			try {
				absolute = Novo19UrlBuilder.bffAbsolutePath(nextPath);
			} catch (final IOException e) {
				if (tiles.isEmpty()) {
					diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
				} else {
					diagnostics.setRootCauseSummary("partial-parse:io-error");
				}
				break;
			}
			if (!visited.add(absolute)) {
				break;
			}
			pageCount++;
			try {
				String json = fetcher.fetchTilesJson(nextPath);
				String parseSource = absolute;
				final String tilesSourcePath = Novo19PageParser.resolveTilesSourcePath(json);
				if (!StringUtils.isEmpty(tilesSourcePath)) {
					json = fetcher.fetchTilesJson(tilesSourcePath);
					parseSource = Novo19UrlBuilder.bffAbsolutePath(tilesSourcePath);
				}
				final Novo19TilesResponse response = Novo19PageParser.parseTilesEnvelope(json, parseSource);
				tiles.addAll(response.getTiles());
				if (!response.isEnvelopeParsed()) {
					if (tiles.isEmpty()) {
						diagnostics.setRootCauseSummary("parse-error:TechnicalException");
					} else {
						diagnostics.setRootCauseSummary("partial-parse:invalid-page");
					}
					break;
				}
				nextPath = resolveNextTilesPath(response.getMoreHref());
			} catch (final IOException e) {
				if (tiles.isEmpty()) {
					diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
				} else {
					diagnostics.setRootCauseSummary("partial-parse:io-error");
				}
				break;
			} catch (final RuntimeException e) {
				if (tiles.isEmpty()) {
					diagnostics.setRootCauseSummary("parse-error:" + e.getClass().getSimpleName());
				} else {
					diagnostics.setRootCauseSummary("partial-parse:runtime-error");
				}
				break;
			}
		}
		if (pageCount >= maxPages && !StringUtils.isEmpty(nextPath)) {
			hitPaginationLimit = true;
		}
		diagnostics.setCreatedItems(tiles.size());
		if (hitPaginationLimit) {
			diagnostics.setRootCauseSummary(ROOT_CAUSE_PAGINATION_LIMIT);
		} else if (StringUtils.isEmpty(diagnostics.getRootCauseSummary()) && !tiles.isEmpty()) {
			diagnostics.setRootCauseSummary("ok");
		}
		return tiles;
	}

	private static String resolveNextTilesPath(final String moreHref) {
		if (StringUtils.isEmpty(moreHref)) {
			return null;
		}
		if (Novo19PathRules.isRecommendationBffPath(moreHref)) {
			return null;
		}
		if (moreHref.startsWith("/api/")) {
			return moreHref;
		}
		if (moreHref.startsWith("/voir-plus/")) {
			final String resolved = Novo19Conf.SLUG_RESOLVER_PREFIX + moreHref.substring(1);
			if (Novo19PathRules.isRecommendationBffPath(resolved)) {
				return null;
			}
			return resolved;
		}
		return null;
	}

}
