package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.dabi.habitv.provider.novo19.dto.Novo19Tile;

public class Novo19PaginationTest {

	@Test
	public void followsMoreHrefPagination() throws Exception {
		final Map<String, String> responses = new HashMap<>();
		responses.put(Novo19UrlBuilder.bffAbsolutePath("/tiles/page-1"), Novo19FixtureSupport.episodeRails("collectionAlpha"));
		responses.put(Novo19UrlBuilder.bffAbsolutePath(
				"/api/1/public/frontends/web/pages/by-path/voir-plus/rail/details/collection-alpha/collection-alpha-episodes"),
				Novo19FixtureSupport.paginationTiles("collectionPage2"));
		final Novo19CatalogClient client = new Novo19CatalogClient(new MapLoader(responses));
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("pagination");
		final List<Novo19Tile> tiles = Novo19Pagination.loadTiles("/tiles/page-1", client::fetchTilesJson, diagnostics);
		assertEquals(3, tiles.size());
		assertEquals("Episode Beta - 15-06-26", tiles.get(2).getTitle());
		assertEquals("ok", diagnostics.getRootCauseSummary());
	}

	@Test
	public void followsMosaicSeeMorePage() throws Exception {
		final Map<String, String> responses = new HashMap<>();
		responses.put(Novo19UrlBuilder.bffAbsolutePath("/tiles/histoire-page-1"),
				Novo19InlineFixtures.HISTOIRE_PAGINATION_PAGE_1);
		responses.put(
				Novo19UrlBuilder.bffAbsolutePath(
						"/api/1/public/frontends/web/pages/by-path/voir-plus/rail/documentaires-et-magazines/histoire"),
				Novo19InlineFixtures.VOIR_PLUS_MOSAIC_PAGE);
		responses.put(Novo19UrlBuilder.bffAbsolutePath(
				"/api/1/public/frontends/web/pages/BFF%7Crail-see-more,documentaires-et-magazines,histoire/sections/mosaic/tiles"),
				Novo19InlineFixtures.VOIR_PLUS_MOSAIC_TILES);
		final Novo19CatalogClient client = new Novo19CatalogClient(new MapLoader(responses));
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("mosaic-pagination");
		final List<Novo19Tile> tiles = Novo19Pagination.loadTiles("/tiles/histoire-page-1", client::fetchTilesJson,
				diagnostics);
		assertEquals(2, tiles.size());
		assertEquals("Theme Beta Programme", tiles.get(1).getTitle());
		assertEquals("ok", diagnostics.getRootCauseSummary());
	}

	@Test
	public void reportsPaginationLimitWhenMorePagesRemain() throws Exception {
		final Map<String, String> responses = new HashMap<String, String>();
		responses.put(Novo19UrlBuilder.bffAbsolutePath("/tiles/page-1"),
				"{\"tiles\":[{\"id\":\"a\",\"type\":\"VOD\",\"title\":\"A\",\"href\":\"/details/a\"}],"
						+ "\"more\":{\"href\":\"/api/1/public/frontends/web/pages/tiles/page-2\"}}");
		responses.put(Novo19UrlBuilder.bffAbsolutePath("/api/1/public/frontends/web/pages/tiles/page-2"),
				"{\"tiles\":[{\"id\":\"b\",\"type\":\"VOD\",\"title\":\"B\",\"href\":\"/details/b\"}]}");
		final Novo19CatalogClient client = new Novo19CatalogClient(new MapLoader(responses));
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("pagination-limit");
		final List<Novo19Tile> tiles = Novo19Pagination.loadTiles("/tiles/page-1", client::fetchTilesJson, diagnostics,
				1);
		assertEquals(1, tiles.size());
		assertEquals(Novo19Pagination.ROOT_CAUSE_PAGINATION_LIMIT, diagnostics.getRootCauseSummary());
	}

	@Test
	public void ioErrorIsNonBlocking() throws Exception {
		final Novo19CatalogClient client = new Novo19CatalogClient(new MapLoader(new HashMap<String, String>()));
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("pagination");
		final List<Novo19Tile> tiles = Novo19Pagination.loadTiles("/missing", client::fetchTilesJson, diagnostics);
		assertEquals(0, tiles.size());
		assertFalse("ok".equals(diagnostics.getRootCauseSummary()));
	}

	@Test
	public void paginationStopsOnVisitedLoop() throws Exception {
		final String pagePath = "/api/1/public/frontends/web/pages/tiles/loop-page";
		final Map<String, String> responses = new HashMap<String, String>();
		responses.put(Novo19UrlBuilder.bffAbsolutePath(pagePath),
				"{\"tiles\":[],\"more\":{\"href\":\"" + pagePath + "\"},\"version\":\"0.9.1\"}");
		final Novo19CatalogClient client = new Novo19CatalogClient(new MapLoader(responses));
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("pagination-loop");
		final List<Novo19Tile> tiles = Novo19Pagination.loadTiles(pagePath, client::fetchTilesJson, diagnostics);
		assertEquals(0, tiles.size());
		assertEquals("ok", diagnostics.getRootCauseSummary());
	}

	private static final class MapLoader implements Novo19CatalogClient.ContentLoader {

		private final Map<String, String> responses;

		private MapLoader(final Map<String, String> responses) {
			this.responses = responses;
		}

		@Override
		public String load(final String url) throws java.io.IOException {
			if (!responses.containsKey(url)) {
				throw new java.io.IOException("missing " + url);
			}
			return responses.get(url);
		}

	}

}
