package com.dabi.habitv.provider.novo19;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.provider.novo19.dto.Novo19Tile;
import com.dabi.habitv.provider.novo19.dto.Novo19TilesResponse;

final class Novo19Pagination {

	interface TilesFetcher {
		String fetchTilesJson(String bffPath) throws IOException;
	}

	private Novo19Pagination() {
	}

	static List<Novo19Tile> loadTiles(final String initialBffPath, final TilesFetcher fetcher,
			final Novo19Diagnostics diagnostics) {
		final Set<String> visited = new LinkedHashSet<>();
		final List<Novo19Tile> tiles = new ArrayList<>();
		String nextPath = initialBffPath;
		int pageCount = 0;
		while (!StringUtils.isEmpty(nextPath) && pageCount < Novo19Conf.MAX_PAGINATION_PAGES) {
			final String absolute = Novo19UrlBuilder.bffAbsolutePath(nextPath);
			if (!visited.add(absolute)) {
				break;
			}
			pageCount++;
			try {
				final String json = fetcher.fetchTilesJson(nextPath);
				final Novo19TilesResponse response = Novo19PageParser.parseTilesEnvelope(json, absolute);
				tiles.addAll(response.getTiles());
				nextPath = resolveNextTilesPath(response.getMoreHref());
			} catch (final IOException e) {
				diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
				break;
			} catch (final RuntimeException e) {
				diagnostics.setRootCauseSummary("parse-error:" + e.getClass().getSimpleName());
				break;
			}
		}
		diagnostics.setCreatedItems(tiles.size());
		return tiles;
	}

	private static String resolveNextTilesPath(final String moreHref) {
		if (StringUtils.isEmpty(moreHref)) {
			return null;
		}
		if (moreHref.startsWith("/api/")) {
			return moreHref;
		}
		if (moreHref.startsWith("/voir-plus/")) {
			final String publicPath = moreHref.substring("/voir-plus/".length());
			return Novo19Conf.SLUG_RESOLVER_PREFIX + publicPath;
		}
		return null;
	}

}
