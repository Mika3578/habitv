package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.provider.novo19.dto.Novo19Tile;

public class Novo19CatalogHierarchyTest {

	@Test
	public void rootHasNoRedundantCatalogueSection() {
		final CategoryDTO root = Novo19TestSupport.rootFromFixtures();
		assertNull(Novo19TestSupport.findChildByName(root, "Catalogue"));
		final CategoryDTO documentaries = Novo19TestSupport.findChildByName(root, Novo19Conf.SECTION_DOCUMENTARIES);
		assertNotNull(documentaries);
		assertNotNull(Novo19TestSupport.findChildByName(documentaries, "Histoire"));
		assertNotNull(Novo19TestSupport.findChildByName(documentaries, "Société"));
		assertNotNull(Novo19TestSupport.findChildByName(
				Novo19TestSupport.findChildByName(documentaries, "Sans thématique"), "Standalone documentary"));
	}

	@Test
	public void nosPodcastsSectionRemainsVisible() {
		final CategoryDTO root = Novo19TestSupport.rootFromFixtures();
		final CategoryDTO podcasts = Novo19TestSupport.findChildByName(root, "Nos podcasts");
		assertNotNull(podcasts);
		final CategoryDTO program = Novo19TestSupport.findChildByName(podcasts, "Podcast Alpha");
		assertNotNull(program);
		assertEquals(Novo19Conf.CONTENT_KIND_PODCAST, program.getParameter(Novo19Conf.PARAMETER_CONTENT_KIND));
		assertEquals("true", program.getParameter(Novo19Conf.PARAMETER_AUDIO_CONTENT));
	}

	@Test
	public void filmBetaFilmInfersContentKindFromDetailWhenGrabConfigOmitsParameter() throws Exception {
		final java.util.Map<String, String> responses = new java.util.HashMap<String, String>();
		responses.put(Novo19UrlBuilder.bffPageByPath("details/film-beta"),
				Novo19InlineFixtures.FILM_BETA_PLAYBACK_INFOS_PAGE);
		final Novo19PluginManager manager = new Novo19PluginManager(new Novo19CatalogClient(new MapLoader(responses)));
		final CategoryDTO filmBeta = new CategoryDTO(Novo19Conf.NAME, "Film Beta",
				"https://novo19.ouest-france.fr/details/film-beta", Novo19Conf.EXTENSION);
		filmBeta.setDownloadable(true);
		filmBeta.addParameter(Novo19Conf.PARAMETER_ASSET_ID, "asset-film-beta");
		final Set<EpisodeDTO> episodes = manager.findEpisode(filmBeta);
		assertEquals(1, episodes.size());
		assertEquals("https://novo19.ouest-france.fr/player/film-beta", episodes.iterator().next().getId());
	}

	@Test
	public void filmBetaFilmUsesPlaybackInfosWhenContentHrefMissing() throws Exception {
		final java.util.Map<String, String> responses = new java.util.HashMap<String, String>();
		responses.put(Novo19UrlBuilder.bffPageByPath("details/film-beta"),
				Novo19InlineFixtures.FILM_BETA_PLAYBACK_INFOS_PAGE);
		final Novo19PluginManager manager = new Novo19PluginManager(new Novo19CatalogClient(new MapLoader(responses)));
		final CategoryDTO filmBeta = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("tile-film-beta",
				"VOD", "Film Beta", null, null, 7005L, "/details/film-beta", "asset-film-beta"));
		final Set<EpisodeDTO> episodes = manager.findEpisode(filmBeta);
		assertEquals(1, episodes.size());
		final EpisodeDTO episode = episodes.iterator().next();
		assertEquals("Film Beta", episode.getName());
		assertEquals("https://novo19.ouest-france.fr/player/film-beta", episode.getId());
	}

	@Test
	public void seriesWithSeasonRailsListsEpisodesAtProgramRoot() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO seriesBeta = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile(
				"asset-series-beta", "SERIE", "Series Beta", null,
				null, null, "/details/series-beta",
				"asset-series-beta"));
		final Set<EpisodeDTO> episodes = manager.findEpisode(seriesBeta);
		assertEquals(2, episodes.size());
		assertTrue(containsEpisodeNamePrefix(episodes, "Episode Beta"));
	}

	@Test
	public void standaloneDocumentaryMapsToSingleItem() throws Exception {
		final CategoryDTO category = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("asset-standalone-documentary",
				"VOD", "Standalone documentary", null, null, null, "/details/standalone-documentary",
				"asset-standalone-documentary"));
		final Set<EpisodeDTO> episodes = Novo19CatalogMapper.mapEpisodes(category,
				Novo19PageParser.parsePageEnvelope(
						"{\"page\":{\"type\":\"DETAILS\",\"content\":{\"id\":\"asset-standalone-documentary\",\"type\":\"VOD\",\"title\":\"Standalone documentary\",\"href\":\"/player/standalone-documentary\",\"source\":{\"id\":\"asset-standalone-documentary\"}}}}",
						"inline"),
				null);
		assertEquals(1, episodes.size());
		assertEquals("Standalone documentary", episodes.iterator().next().getName());
	}

	private static boolean containsEpisodeNamePrefix(final Set<EpisodeDTO> episodes, final String prefix) {
		for (final EpisodeDTO episode : episodes) {
			if (episode.getName() != null && episode.getName().startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	private static final class MapLoader implements Novo19CatalogClient.ContentLoader {

		private final java.util.Map<String, String> responses;

		private MapLoader(final java.util.Map<String, String> responses) {
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
