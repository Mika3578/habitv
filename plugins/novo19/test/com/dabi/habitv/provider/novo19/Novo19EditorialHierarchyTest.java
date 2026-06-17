package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.provider.novo19.dto.Novo19Tile;

public class Novo19EditorialHierarchyTest {

	@Test
	public void categoriesPageHasNoCatalogueNode() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO root = manager.findCategory().iterator().next();
		assertNull(findChildByName(root, "Catalogue"));
	}

	@Test
	public void infoSectionContainsProgramsNotArtworkLabels() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO root = manager.findCategory().iterator().next();
		final CategoryDTO info = findChildByName(root, Novo19Conf.EDITORIAL_INFO);
		assertNotNull(info);
		assertNull(findChildByName(info, "Info"));
		final CategoryDTO onADeLInfo = findChildByName(info, "On a de l'info");
		assertNotNull(onADeLInfo);
		assertEquals("https://novo19.ouest-france.fr/details/on-a-de-l-info", onADeLInfo.getId());
		final CategoryDTO leMag = findChildByName(info, "On a de l'info - Le mag");
		assertNotNull(leMag);
	}

	@Test
	public void talkSectionContainsOnADuNouveauProgram() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO root = manager.findCategory().iterator().next();
		final CategoryDTO talk = findChildByName(root, Novo19Conf.EDITORIAL_TALK);
		assertNotNull(talk);
		assertNull(findChildByName(talk, "Talk"));
		final CategoryDTO onADuNouveau = findChildByName(talk, "On a du nouveau");
		assertNotNull(onADuNouveau);
		assertEquals(Novo19Conf.CONTENT_KIND_PROGRAM,
				onADuNouveau.getParameter(Novo19Conf.PARAMETER_CONTENT_KIND));
	}

	@Test
	public void infoProgramListsEpisodesNotUnderArtworkLabel() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO root = manager.findCategory().iterator().next();
		final CategoryDTO program = findChildByName(findChildByName(root, Novo19Conf.EDITORIAL_INFO),
				"On a de l'info");
		final Set<EpisodeDTO> episodes = manager.findEpisode(program);
		assertEquals(2, episodes.size());
		for (final EpisodeDTO episode : episodes) {
			assertEquals("On a de l'info", episode.getCategory().getName());
			assertFalse("Info".equals(episode.getCategory().getName()));
		}
	}

	@Test
	public void talkProgramMergesSeasonAndPaginatedRailEpisodes() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO program = Novo19CatalogMapper.buildProgramCategory(
				new Novo19Tile("talk-shortcut_565BFFb", "COLLECTION", "Talk", null, null, null,
						"/details/on-a-du-nouveau", "talk-shortcut_565BFFb"),
				"On a du nouveau");
		program.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_PROGRAM);
		final Set<EpisodeDTO> episodes = manager.findEpisode(program);
		assertEquals(3, episodes.size());
		assertTrue(containsEpisodeTitle(episodes, "Emission du 22-05-26"));
		assertTrue(containsEpisodeTitle(episodes, "Emission du 21-05-26"));
		assertTrue(containsEpisodeTitle(episodes, "Emission du 20-05-26"));
	}

	@Test
	public void paginationStopsOnVisitedLoop() throws Exception {
		final String pagePath = "/api/1/public/frontends/web/pages/tiles/loop-page";
		final Map<String, String> responses = new HashMap<>();
		responses.put(Novo19UrlBuilder.bffAbsolutePath(pagePath),
				"{\"tiles\":[],\"more\":{\"href\":\"" + pagePath + "\"},\"version\":\"0.9.1\"}");
		final Novo19CatalogClient client = new Novo19CatalogClient(new MapLoader(responses));
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("pagination-loop");
		final List<Novo19Tile> tiles = Novo19Pagination.loadTiles(pagePath, client::fetchTilesJson, diagnostics);
		assertEquals(0, tiles.size());
		assertEquals("ok", diagnostics.getRootCauseSummary());
	}

	@Test
	public void editorialNavigationTilesAreSkipped() {
		assertTrue(Novo19PathRules.isEditorialNavigationTile(
				new Novo19Tile("nav-series", "COLLECTION", "Series", null, null, null, "/series", "nav-series")));
		assertFalse(Novo19PathRules.isEditorialNavigationTile(new Novo19Tile("info-shortcut", "COLLECTION", "Info",
				null, null, null, "/details/on-a-de-l-info", "info-shortcut")));
	}

	@Test
	public void duplicateProgramsAreNotAddedTwice() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO root = manager.findCategory().iterator().next();
		final CategoryDTO info = findChildByName(root, Novo19Conf.EDITORIAL_INFO);
		final Set<String> programIds = new HashSet<>();
		for (final CategoryDTO program : info.getSubCategories()) {
			assertTrue(programIds.add(program.getId()));
		}
		assertEquals(2, info.getSubCategories().size());
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

	private static boolean containsEpisodeTitle(final Set<EpisodeDTO> episodes, final String title) {
		for (final EpisodeDTO episode : episodes) {
			if (title.equals(episode.getName()) || episode.getName().startsWith(title)) {
				return true;
			}
		}
		return false;
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
