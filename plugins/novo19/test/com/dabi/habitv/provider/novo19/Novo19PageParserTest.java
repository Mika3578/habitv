package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.provider.novo19.dto.Novo19BffPage;
import com.dabi.habitv.provider.novo19.dto.Novo19Tile;
import com.dabi.habitv.provider.novo19.dto.Novo19TilesResponse;

public class Novo19PageParserTest {

	@Test
	public void parsesCategoriesPage() throws Exception {
		final Novo19BffPage page = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.readFixture("bff-page-categories.json"), "fixture");
		assertEquals("RAILS", page.getType());
		assertEquals(7, page.getRails().size());
		assertEquals("f6a789ee-7e88-49c4-89ff-d4ef6ab044ce", page.getRails().get(0).getId());
		assertEquals("Nos séries", page.getRails().get(1).getTitle());
	}

	@Test
	public void parsesSeriesTiles() throws Exception {
		final Novo19TilesResponse response = Novo19PageParser.parseTilesEnvelope(
				Novo19FixtureSupport.readFixture("bff-tiles-series.json"), "fixture");
		assertEquals(1, response.getTiles().size());
		assertEquals("FBI : Portés disparus", response.getTiles().get(0).getTitle());
	}

	@Test
	public void parsesSeriesDetailsWithSeason() throws Exception {
		final Novo19BffPage page = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.readFixture("bff-page-series-details.json"), "fixture");
		assertEquals(1, page.getSeasons().size());
		assertEquals("Une petite ville bien tranquille", page.getSeasons().get(0).getEpisodes().get(0).getTitle());
	}

	@Test
	public void parsesFilmDetailsContent() throws Exception {
		final Novo19BffPage page = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.readFixture("bff-page-film-details.json"), "fixture");
		assertNotNull(page.getContent());
		assertEquals("Un plan d'enfer", page.getContent().getTitle());
	}

	@Test
	public void parsesEpisodeWithSubtitleAndDuration() throws Exception {
		final Novo19TilesResponse response = Novo19PageParser.parseTilesEnvelope(
				Novo19FixtureSupport.readFixture("bff-tiles-info-episodes.json"), "fixture");
		final Novo19Tile tile = response.getTiles().get(0);
		assertEquals("Emission du 16-06-26", tile.getTitle());
		assertNotNull(response.getMoreHref());
	}

	@Test
	public void parsesPlayerContentWithoutHref() throws Exception {
		final Novo19BffPage page = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.readFixture("bff-page-player-episode-no-href.json"), "fixture");
		assertNotNull(page.getContent());
		assertEquals("OF-00000661-03-0012_565BFFb", page.getContent().getAssetId());
	}

	@Test
	public void excludesMesVideosPath() throws Exception {
		final Novo19Tile tile = Novo19PageParser.parseTile(new com.fasterxml.jackson.databind.ObjectMapper()
				.readTree("{\"type\":\"COLLECTION\",\"title\":\"Mes vidéos\",\"href\":\"/mes-videos\"}"));
		assertEquals(null, tile);
	}

	@Test(expected = TechnicalException.class)
	public void malformedJsonFailsFast() throws Exception {
		Novo19PageParser.parsePageEnvelope(Novo19FixtureSupport.readFixture("bff-malformed.json"), "fixture");
	}

	@Test
	public void emptyJsonIsNonBlocking() throws Exception {
		final Novo19BffPage page = Novo19PageParser.parsePageEnvelope("{}", "fixture");
		assertTrue(page.getRails().isEmpty());
	}

	@Test
	public void missingNextDataIsNotRequiredForBffParser() {
		final Novo19BffPage page = Novo19PageParser.parsePageEnvelope("", "fixture");
		assertTrue(page.getRails().isEmpty());
		assertEquals(null, page.getTitle());
	}

}
