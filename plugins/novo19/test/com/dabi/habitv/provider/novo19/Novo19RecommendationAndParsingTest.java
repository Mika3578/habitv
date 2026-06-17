package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import com.dabi.habitv.provider.novo19.dto.Novo19Rail;
import com.dabi.habitv.provider.novo19.dto.Novo19Tile;
import com.dabi.habitv.provider.novo19.dto.Novo19TilesResponse;

public class Novo19RecommendationAndParsingTest {

	private static final String RECO_TILES_PATH = "/api/1/public/frontends/web/pages/BFF%7Casset-details,inferno/sections/reco/tiles";

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
	public void infernoEpisodeListingNeverRequestsRecommendationEndpoint() throws Exception {
		final RecordingContentLoader loader = recordingLoader();
		final Novo19PluginManager manager = new Novo19PluginManager(new Novo19CatalogClient(loader));
		final CategoryDTO inferno = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("film-inferno_565BFFb",
				"VOD", "Inferno", null, null, 7200L, "/details/inferno", "inferno_565BFFb"));
		final Set<EpisodeDTO> episodes = manager.findEpisode(inferno);
		assertEquals(1, episodes.size());
		assertEquals("Inferno", episodes.iterator().next().getName());
		assertNoRecommendationRequests(loader.getRequestedUrls());
	}

	@Test
	public void fbiReturnsOnlyGenuineSeasonEpisodes() throws Exception {
		final RecordingContentLoader loader = recordingLoader();
		final Novo19PluginManager manager = new Novo19PluginManager(new Novo19CatalogClient(loader));
		final CategoryDTO series = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("serie-fbi_565BFFb", "SERIE",
				"FBI : Portés disparus", null, null, null, "/details/fbi-portes-disparus", "serie-fbi_565BFFb"));
		final Set<EpisodeDTO> episodes = manager.findEpisode(series);
		assertEquals(1, episodes.size());
		assertTrue(episodes.iterator().next().getName().contains("Une petite ville bien tranquille"));
		assertNoRecommendationRequests(loader.getRequestedUrls());
	}

	@Test
	public void vosObjetsValentDeLOrReturnsFiveGenuineEpisodes() throws Exception {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO program = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("vos-objets_565BFFb", "SERIE",
				"Vos objets valent de l'or", null, null, null, "/details/vos-objets-valent-de-l-or",
				"c58fca47-497b-4166-89cd-13d6b5bf3a2d_565BFFb"));
		final Set<EpisodeDTO> episodes = manager.findEpisode(program);
		assertEquals(5, episodes.size());
	}

	@Test
	public void podcastRailKeepsValidEpisodesWithoutWholeRailParseError() throws Exception {
		final Map<String, String> responses = new HashMap<>();
		final String page1Path = "/api/1/public/frontends/web/pages/BFF%7Casset-details-podcast,le-royaume-des-contes/sections/episodes/tiles";
		final String page2Path = "/api/1/public/frontends/web/pages/BFF%7Casset-details-podcast,le-royaume-des-contes/sections/episodes/tiles-page-2";
		responses.put(Novo19UrlBuilder.bffAbsolutePath(page1Path),
				Novo19FixtureSupport.readFixture("bff-tiles-podcast-episodes-page1.json"));
		responses.put(Novo19UrlBuilder.bffAbsolutePath(page2Path),
				Novo19FixtureSupport.readFixture("bff-malformed.json"));
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
				Novo19FixtureSupport.readFixture("bff-malformed.json"), "fixture");
		assertTrue(response.getTiles().isEmpty());
		assertFalse(response.isEnvelopeParsed());
	}

	@Test
	public void malformedIndividualTilesDoNotRemoveValidSiblings() throws Exception {
		final Novo19TilesResponse response = Novo19PageParser.parseTilesEnvelope(
				Novo19FixtureSupport.readFixture("bff-tiles-podcast-episodes.json"), "fixture");
		assertEquals(3, response.getTiles().size());
		assertTrue(response.isEnvelopeParsed());
	}

	@Test
	public void recommendationRailMetadataIsDetected() {
		final Novo19Rail rail = new Novo19Rail("reco", "Recommendations", RECO_TILES_PATH, null);
		assertTrue(Novo19PathRules.isRecommendationRail(rail));
	}

	private static RecordingContentLoader recordingLoader() throws IOException {
		final Map<String, String> responses = new HashMap<>();
		responses.put(Novo19UrlBuilder.bffConfigUrl(), Novo19FixtureSupport.readFixture("bff-config.json"));
		responses.put(Novo19UrlBuilder.bffPageByPath("categories"), Novo19FixtureSupport.readFixture("bff-page-categories.json"));
		responses.put(Novo19UrlBuilder.bffPageByPath("details/inferno"),
				Novo19FixtureSupport.readFixture("bff-page-inferno-film.json"));
		responses.put(Novo19UrlBuilder.bffPageByPath("details/fbi-portes-disparus"),
				Novo19FixtureSupport.readFixture("bff-page-series-details.json"));
		return new RecordingContentLoader(responses);
	}

	private static void assertNoRecommendationRequests(final List<String> requestedUrls) {
		for (final String url : requestedUrls) {
			assertFalse("Unexpected recommendation request: " + url, url.contains("/sections/reco/"));
		}
	}

	private static final class RecordingContentLoader implements Novo19CatalogClient.ContentLoader {

		private final Map<String, String> responses;

		private final List<String> requestedUrls = new ArrayList<>();

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
