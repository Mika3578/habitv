package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.provider.novo19.dto.Novo19BffPage;
import com.dabi.habitv.provider.novo19.dto.Novo19Rail;
import com.dabi.habitv.provider.novo19.dto.Novo19Tile;
import com.dabi.habitv.provider.novo19.dto.Novo19TilesResponse;

public class Novo19ParsingTest {

	private static final String RECO_TILES_PATH = "/api/1/public/frontends/web/pages/BFF%7Casset-details,film-beta/sections/reco/tiles";

	@Test
	public void parsesCategoriesPage() throws Exception {
		final Novo19BffPage page = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.detailPage("catalogueRoot"), "fixture");
		assertEquals("RAILS", page.getType());
		assertEquals(7, page.getRails().size());
		assertEquals("f6a789ee-7e88-49c4-89ff-d4ef6ab044ce", page.getRails().get(0).getId());
		assertEquals("Nos séries", page.getRails().get(1).getTitle());
	}

	@Test
	public void parsesSeriesTiles() throws Exception {
		final Novo19TilesResponse response = Novo19PageParser.parseTilesEnvelope(
				Novo19FixtureSupport.catalogueTiles("series"), "fixture");
		assertEquals(1, response.getTiles().size());
		assertEquals("Series Alpha", response.getTiles().get(0).getTitle());
	}

	@Test
	public void parsesSeriesDetailsWithSeason() throws Exception {
		final Novo19BffPage page = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.detailPage("seriesAlpha"), "fixture");
		assertEquals(1, page.getSeasons().size());
		assertEquals("Episode Alpha", page.getSeasons().get(0).getEpisodes().get(0).getTitle());
	}

	@Test
	public void parsesFilmDetailsContent() throws Exception {
		final Novo19BffPage page = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.detailPage("filmAlpha"), "fixture");
		assertNotNull(page.getContent());
		assertEquals("Film Alpha", page.getContent().getTitle());
	}

	@Test
	public void parsesEpisodeWithSubtitleAndDuration() throws Exception {
		final Novo19TilesResponse response = Novo19PageParser.parseTilesEnvelope(
				Novo19FixtureSupport.episodeRails("collectionAlpha"), "fixture");
		final Novo19Tile tile = response.getTiles().get(0);
		assertEquals("Episode Alpha - 16-06-26", tile.getTitle());
		assertNotNull(response.getMoreHref());
	}

	@Test
	public void parsesNestedDataTilesEnvelope() throws Exception {
		final Novo19TilesResponse response = Novo19PageParser.parseTilesEnvelope(
				Novo19InlineFixtures.NESTED_DATA_TILES, "fixture");
		assertEquals(1, response.getTiles().size());
		assertEquals("Episode Alpha", response.getTiles().get(0).getTitle());
		assertEquals("/voir-plus/rail/details/programme-alpha/episodes", response.getMoreHref());
	}

	@Test
	public void parsesPlayerContentWithoutHref() throws Exception {
		final Novo19BffPage page = Novo19PageParser.parsePageEnvelope(
				Novo19InlineFixtures.PLAYER_SERIES_EPISODE_NO_HREF_PAGE, "fixture");
		assertNotNull(page.getContent());
		assertEquals("asset-series-beta-episode-beta", page.getContent().getAssetId());
	}

	@Test
	public void excludesMesVideosPath() throws Exception {
		final Novo19Tile tile = Novo19PageParser.parseTile(new com.fasterxml.jackson.databind.ObjectMapper()
				.readTree("{\"type\":\"COLLECTION\",\"title\":\"Mes vidéos\",\"href\":\"/mes-videos\"}"));
		assertEquals(null, tile);
	}

	@Test(expected = TechnicalException.class)
	public void malformedJsonFailsFast() throws Exception {
		Novo19PageParser.parsePageEnvelope(Novo19InlineFixtures.MALFORMED_JSON, "fixture");
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

	@Test
	public void recommendationBffPathIsNeverFetchedByPagination() throws Exception {
		final RecordingContentLoader loader = recordingLoader();
		final Novo19CatalogClient client = new Novo19CatalogClient(loader);
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("episode-rail");
		final List<Novo19Tile> tiles = Novo19Pagination.loadTiles(RECO_TILES_PATH, client::fetchTilesJson,
				diagnostics);
		assertTrue(tiles.isEmpty());
		assertTrue(loader.getRequestedUrls().isEmpty());
		assertEquals("ok", diagnostics.getRootCauseSummary());
	}

	@Test
	public void filmBetaEpisodeListingNeverRequestsRecommendationEndpoint() throws Exception {
		final RecordingContentLoader loader = recordingLoader();
		final Novo19PluginManager manager = new Novo19PluginManager(new Novo19CatalogClient(loader));
		final CategoryDTO filmBeta = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("tile-film-beta",
				"VOD", "Film Beta", null, null, 7200L, "/details/film-beta", "asset-film-beta"));
		final Set<EpisodeDTO> episodes = manager.findEpisode(filmBeta);
		assertEquals(1, episodes.size());
		assertEquals("Film Beta", episodes.iterator().next().getName());
		assertNoRecommendationRequests(loader.getRequestedUrls());
	}

	@Test
	public void seriesAlphaReturnsOnlyGenuineSeasonEpisodes() throws Exception {
		final RecordingContentLoader loader = recordingLoader();
		final Novo19PluginManager manager = new Novo19PluginManager(new Novo19CatalogClient(loader));
		final CategoryDTO series = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("asset-series-alpha", "SERIE",
				"Series Alpha", null, null, null, "/details/series-alpha", "asset-series-alpha"));
		final Set<EpisodeDTO> episodes = manager.findEpisode(series);
		assertEquals(1, episodes.size());
		assertTrue(episodes.iterator().next().getName().contains("Episode Alpha"));
		assertNoRecommendationRequests(loader.getRequestedUrls());
	}

	@Test
	public void collectionAlphaExtendedReturnsFiveGenuineEpisodes() throws Exception {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO program = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("asset-collection-alpha", "SERIE",
				"Collection Alpha", null, null, null, "/details/collection-alpha-extended",
				"asset-collection-alpha"));
		final Set<EpisodeDTO> episodes = manager.findEpisode(program);
		assertEquals(5, episodes.size());
	}

	@Test
	public void podcastRailKeepsValidEpisodesWithoutWholeRailParseError() throws Exception {
		final Map<String, String> responses = new HashMap<String, String>();
		final String page1Path = "/api/1/public/frontends/web/pages/BFF%7Casset-details-podcast,podcast-alpha/sections/episodes/tiles";
		final String page2Path = "/api/1/public/frontends/web/pages/BFF%7Casset-details-podcast,podcast-alpha/sections/episodes/tiles-page-2";
		responses.put(Novo19UrlBuilder.bffAbsolutePath(page1Path),
				Novo19FixtureSupport.episodeRails("podcastAlpha"));
		responses.put(Novo19UrlBuilder.bffAbsolutePath(page2Path),
				Novo19InlineFixtures.MALFORMED_JSON);
		final Novo19CatalogClient client = new Novo19CatalogClient(new MapContentLoader(responses));
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("episode-rail");
		final List<Novo19Tile> tiles = Novo19Pagination.loadTiles(page1Path, client::fetchTilesJson, diagnostics);
		assertEquals(3, tiles.size());
		assertEquals("partial-parse:invalid-page", diagnostics.getRootCauseSummary());
		assertFalse(diagnostics.getRootCauseSummary().startsWith("parse-error"));
	}

	@Test
	public void malformedTilesEnvelopeDoesNotThrowAndMarksEnvelopeUnparsed() throws Exception {
		final Novo19TilesResponse response = Novo19PageParser.parseTilesEnvelope(
				Novo19InlineFixtures.MALFORMED_JSON, "fixture");
		assertTrue(response.getTiles().isEmpty());
		assertFalse(response.isEnvelopeParsed());
	}

	@Test
	public void malformedIndividualTilesDoNotRemoveValidSiblings() throws Exception {
		final Novo19TilesResponse response = Novo19PageParser.parseTilesEnvelope(
				Novo19FixtureSupport.episodeRails("podcastAlpha"), "fixture");
		assertEquals(3, response.getTiles().size());
		assertTrue(response.isEnvelopeParsed());
	}

	@Test
	public void recommendationRailMetadataIsDetected() {
		final Novo19Rail rail = new Novo19Rail("reco", "Recommendations", RECO_TILES_PATH, null);
		assertTrue(Novo19PathRules.isRecommendationRail(rail));
	}

	private static RecordingContentLoader recordingLoader() throws IOException {
		final Map<String, String> responses = new HashMap<String, String>();
		responses.put(Novo19UrlBuilder.bffConfigUrl(), Novo19FixtureSupport.readFixture("bff-config.json"));
		responses.put(Novo19UrlBuilder.bffPageByPath("categories"), Novo19FixtureSupport.detailPage("catalogueRoot"));
		responses.put(Novo19UrlBuilder.bffPageByPath("details/film-beta"),
				Novo19FixtureSupport.detailPage("filmBeta"));
		responses.put(Novo19UrlBuilder.bffPageByPath("details/series-alpha"),
				Novo19FixtureSupport.detailPage("seriesAlpha"));
		return new RecordingContentLoader(responses);
	}

	private static void assertNoRecommendationRequests(final List<String> requestedUrls) {
		for (final String url : requestedUrls) {
			assertFalse("Unexpected recommendation request: " + url, url.contains("/sections/reco/"));
		}
	}

	private static final class RecordingContentLoader implements Novo19CatalogClient.ContentLoader {

		private final Map<String, String> responses;

		private final List<String> requestedUrls = new ArrayList<String>();

		private RecordingContentLoader(final Map<String, String> responses) {
			this.responses = responses;
		}

		@Override
		public String load(final String url) throws IOException {
			requestedUrls.add(url);
			final String body = responses.get(url);
			if (body == null) {
				throw new IOException("no fixture for " + url);
			}
			return body;
		}

		private List<String> getRequestedUrls() {
			return requestedUrls;
		}

	}

	private static final class MapContentLoader implements Novo19CatalogClient.ContentLoader {

		private final Map<String, String> responses;

		private MapContentLoader(final Map<String, String> responses) {
			this.responses = responses;
		}

		@Override
		public String load(final String url) throws IOException {
			final String body = responses.get(url);
			if (body == null) {
				throw new IOException("no fixture for " + url);
			}
			return body;
		}

	}

}
