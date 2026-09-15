package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.provider.novo19.dto.Novo19Tile;

public class Novo19EditorialHierarchyTest {

	@Test
	public void infoSectionContainsProgramsNotArtworkLabels() {
		final CategoryDTO root = Novo19TestSupport.rootFromFixtures();
		final CategoryDTO info = Novo19TestSupport.findChildByName(root, Novo19Conf.EDITORIAL_INFO);
		assertNotNull(info);
		assertNull(Novo19TestSupport.findChildByName(info, "Info"));
		final CategoryDTO collectionAlpha = Novo19TestSupport.findChildByName(info, "Collection Alpha");
		assertNotNull(collectionAlpha);
		assertEquals("https://novo19.ouest-france.fr/details/collection-alpha", collectionAlpha.getId());
		final CategoryDTO leMag = Novo19TestSupport.findChildByName(info, "Programme Beta");
		assertNotNull(leMag);
	}

	@Test
	public void talkSectionContainsTalkProgrammeAlpha() {
		final CategoryDTO root = Novo19TestSupport.rootFromFixtures();
		final CategoryDTO talk = Novo19TestSupport.findChildByName(root, Novo19Conf.EDITORIAL_TALK);
		assertNotNull(talk);
		assertNull(Novo19TestSupport.findChildByName(talk, "Talk"));
		final CategoryDTO talkProgrammeAlpha = Novo19TestSupport.findChildByName(talk, "Talk Programme Alpha");
		assertNotNull(talkProgrammeAlpha);
		assertEquals(Novo19Conf.CONTENT_KIND_PROGRAM,
				talkProgrammeAlpha.getParameter(Novo19Conf.PARAMETER_CONTENT_KIND));
	}

	@Test
	public void infoProgramListsEpisodesNotUnderArtworkLabel() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO root = manager.findCategory().iterator().next();
		final CategoryDTO program = Novo19TestSupport.findChildByName(
				Novo19TestSupport.findChildByName(root, Novo19Conf.EDITORIAL_INFO), "Collection Alpha");
		final Set<EpisodeDTO> episodes = manager.findEpisode(program);
		assertEquals(2, episodes.size());
		for (final EpisodeDTO episode : episodes) {
			assertEquals("Collection Alpha", episode.getCategory().getName());
			assertFalse("Info".equals(episode.getCategory().getName()));
		}
	}

	@Test
	public void talkProgramMergesSeasonAndPaginatedRailEpisodes() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO program = Novo19CatalogMapper.buildProgramCategory(
				new Novo19Tile("asset-talk-programme-alpha", "COLLECTION", "Talk", null, null, null,
						"/details/talk-programme-alpha", "asset-talk-programme-alpha"),
				"Talk Programme Alpha");
		program.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_PROGRAM);
		final Set<EpisodeDTO> episodes = manager.findEpisode(program);
		assertEquals(3, episodes.size());
		assertTrue(containsEpisodeTitle(episodes, "Episode Alpha - 22-05-26"));
		assertTrue(containsEpisodeTitle(episodes, "Episode Beta - 21-05-26"));
		assertTrue(containsEpisodeTitle(episodes, "Episode Gamma - 20-05-26"));
	}

	@Test
	public void editorialNavigationTilesAreSkipped() {
		assertTrue(Novo19PathRules.isEditorialNavigationTile(
				new Novo19Tile("nav-series", "COLLECTION", "Series", null, null, null, "/series", "nav-series")));
		assertFalse(Novo19PathRules.isEditorialNavigationTile(new Novo19Tile("info-shortcut", "COLLECTION", "Info",
				null, null, null, "/details/collection-alpha", "info-shortcut")));
	}

	@Test
	public void duplicateProgramsAreNotAddedTwice() {
		final CategoryDTO root = Novo19TestSupport.rootFromFixtures();
		final CategoryDTO info = Novo19TestSupport.findChildByName(root, Novo19Conf.EDITORIAL_INFO);
		final Set<String> programIds = new HashSet<String>();
		for (final CategoryDTO program : info.getSubCategories()) {
			assertTrue(programIds.add(program.getId()));
		}
		assertEquals(2, info.getSubCategories().size());
	}

	private static boolean containsEpisodeTitle(final Set<EpisodeDTO> episodes, final String title) {
		for (final EpisodeDTO episode : episodes) {
			if (title.equals(episode.getName()) || episode.getName().startsWith(title)) {
				return true;
			}
		}
		return false;
	}

}
