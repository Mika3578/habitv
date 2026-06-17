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
import com.dabi.habitv.provider.novo19.dto.Novo19TilesResponse;

public class Novo19CatalogHierarchyTest {

	@Test
	public void rootHasNoRedundantCatalogueSection() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO root = manager.findCategory().iterator().next();
		assertNull(findChildByName(root, "Catalogue"));
		assertNotNull(findChildByName(root, "Standalone documentary"));
	}

	@Test
	public void nosPodcastsSectionRemainsVisible() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO root = manager.findCategory().iterator().next();
		final CategoryDTO podcasts = findChildByName(root, "Nos podcasts");
		assertNotNull(podcasts);
		final CategoryDTO program = findChildByName(podcasts, "Le royaume des contes");
		assertNotNull(program);
		assertEquals(Novo19Conf.CONTENT_KIND_PODCAST, program.getParameter(Novo19Conf.PARAMETER_CONTENT_KIND));
		assertEquals("true", program.getParameter(Novo19Conf.PARAMETER_AUDIO_CONTENT));
	}

	@Test
	public void podcastProgramListsOnlyGenuineAudioEpisodes() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO program = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("podcast-royaume_565BFFb",
				"PODCAST", "Le royaume des contes", null, null, null, "/details/le-royaume-des-contes",
				"podcast-royaume_565BFFb"));
		final Set<EpisodeDTO> episodes = manager.findEpisode(program);
		assertEquals(3, episodes.size());
		assertTrue(containsEpisodeName(episodes, "L'Oie d'or"));
		assertTrue(containsEpisodeName(episodes, "La Cigale et la Fourmi"));
		assertTrue(containsEpisodeName(episodes, "Les animaux malades de la peste"));
	}

	@Test
	public void infernoFilmContainsOnlyInferno() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO inferno = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("film-inferno_565BFFb",
				"VOD", "Inferno", null, null, 7200L, "/details/inferno", "inferno_565BFFb"));
		final Set<EpisodeDTO> episodes = manager.findEpisode(inferno);
		assertEquals(1, episodes.size());
		assertEquals("Inferno", episodes.iterator().next().getName());
	}

	@Test
	public void seriesContainsOnlyOwnSeasonEpisodes() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO series = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("serie-fbi_565BFFb", "SERIE",
				"FBI : Portés disparus", null, null, null, "/details/fbi-portes-disparus", "serie-fbi_565BFFb"));
		final Set<EpisodeDTO> episodes = manager.findEpisode(series);
		assertEquals(1, episodes.size());
		assertTrue(episodes.iterator().next().getName().contains("Une petite ville bien tranquille"));
	}

	@Test
	public void standaloneDocumentaryMapsToSingleItem() throws Exception {
		final CategoryDTO category = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("doc-standalone_565BFFb",
				"VOD", "Standalone documentary", null, null, null, "/details/standalone-documentary",
				"doc-standalone_565BFFb"));
		final Set<EpisodeDTO> episodes = Novo19CatalogMapper.mapEpisodes(category,
				Novo19PageParser.parsePageEnvelope(
						"{\"page\":{\"type\":\"DETAILS\",\"content\":{\"id\":\"doc-standalone_565BFFb\",\"type\":\"VOD\",\"title\":\"Standalone documentary\",\"href\":\"/player/standalone-documentary\",\"source\":{\"id\":\"doc-standalone_565BFFb\"}}}}",
						"inline"),
				null);
		assertEquals(1, episodes.size());
		assertEquals("Standalone documentary", episodes.iterator().next().getName());
	}

	@Test
	public void malformedTilesDoNotRemoveValidSiblings() throws Exception {
		final Novo19TilesResponse tiles = Novo19PageParser.parseTilesEnvelope(
				Novo19FixtureSupport.readFixture("bff-tiles-podcast-episodes.json"), "fixture");
		assertEquals(3, tiles.getTiles().size());
	}

	private static CategoryDTO findChildByName(final CategoryDTO parent, final String name) {
		if (parent == null) {
			return null;
		}
		for (final CategoryDTO child : parent.getSubCategories()) {
			if (name.equals(child.getName())) {
				return child;
			}
			final CategoryDTO nested = findChildByName(child, name);
			if (nested != null) {
				return nested;
			}
		}
		return null;
	}

	private static boolean containsEpisodeName(final Set<EpisodeDTO> episodes, final String name) {
		for (final EpisodeDTO episode : episodes) {
			if (name.equals(episode.getName())) {
				return true;
			}
		}
		return false;
	}

}
