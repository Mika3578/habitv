package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.dabi.habitv.provider.novo19.dto.Novo19Tile;

public class Novo19PaginationTest {

	@Test
	public void followsMoreHrefPagination() throws Exception {
		final Map<String, String> responses = new HashMap<>();
		responses.put(Novo19UrlBuilder.bffAbsolutePath("/tiles/page-1"), Novo19FixtureSupport.readFixture("bff-tiles-info-episodes.json"));
		responses.put(Novo19UrlBuilder.bffAbsolutePath(
				"/api/1/public/frontends/web/pages/by-path/rail/details/on-a-de-l-info/info-episodes"),
				Novo19FixtureSupport.readFixture("bff-pagination-more.json"));
		final Novo19CatalogClient client = new Novo19CatalogClient(new MapLoader(responses));
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("pagination");
		final List<Novo19Tile> tiles = Novo19Pagination.loadTiles("/tiles/page-1", client::fetchTilesJson, diagnostics);
		assertEquals(3, tiles.size());
		assertEquals("Emission du 15-06-26", tiles.get(2).getTitle());
		assertEquals("ok", diagnostics.getRootCauseSummary());
	}

	@Test
	public void ioErrorIsNonBlocking() throws Exception {
		final Novo19CatalogClient client = new Novo19CatalogClient(new MapLoader(new HashMap<String, String>()));
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("pagination");
		final List<Novo19Tile> tiles = Novo19Pagination.loadTiles("/missing", client::fetchTilesJson, diagnostics);
		assertEquals(0, tiles.size());
		assertFalse("ok".equals(diagnostics.getRootCauseSummary()));
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
