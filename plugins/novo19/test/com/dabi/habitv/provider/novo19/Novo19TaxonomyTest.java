package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.provider.novo19.dto.Novo19BffPage;
import com.dabi.habitv.provider.novo19.dto.Novo19Tile;

public class Novo19TaxonomyTest {

	@Test
	public void normalizesBoilerplateAndGenreLabels() {
		assertNull(Novo19TaxonomyMapper.normalizeTheme("Documentaire"));
		assertNull(Novo19TaxonomyMapper.normalizeTheme("Docs et Magazines"));
		assertNull(Novo19TaxonomyMapper.normalizeTheme("Policiers / Thrillers"));
		assertNull(Novo19TaxonomyMapper.normalizeTheme("Sélection Theme Alpha"));
		assertEquals("Histoire", Novo19TaxonomyMapper.normalizeTheme("Histoire"));
		assertEquals("Société", Novo19TaxonomyMapper.normalizeTheme("Société"));
	}

	@Test
	public void resolvesThemesFromDetailCategoriesTileSubtitleAndRailHint() throws Exception {
		final Novo19BffPage seriesBetaPage = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.detailPage("seriesBeta"), "fixture");
		final Novo19Tile tile = new Novo19Tile("asset-series-beta", "SERIE", "Series Beta",
				"Société", null, null, "/details/series-beta", "asset-series-beta");
		final Set<String> carouselThemes = Novo19TaxonomyMapper.resolveProgramThemes(tile, seriesBetaPage, null);
		assertTrue(carouselThemes.contains("Société"));
		assertFalse(carouselThemes.contains("Documentaire"));
		final Set<String> railThemes = Novo19TaxonomyMapper.resolveProgramThemes(tile, seriesBetaPage, "Société");
		assertEquals(1, railThemes.size());
		assertTrue(railThemes.contains("Société"));
	}

	@Test
	public void themeRailDiscoveryDoesNotInheritUnrelatedContentCategories() throws Exception {
		final Novo19BffPage filmGammaPage = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.detailPage("filmGamma"), "fixture");
		final Novo19Tile tile = new Novo19Tile("asset-film-gamma", "VOD", "Film Gamma",
				null, null, null, "/details/film-gamma", "asset-film-gamma");
		final Set<String> immersionThemes = Novo19TaxonomyMapper.resolveProgramThemes(tile, filmGammaPage, "Immersion");
		assertTrue(immersionThemes.contains("Immersion"));
		assertFalse(immersionThemes.contains("Politique"));
		assertFalse(immersionThemes.contains("Histoire"));
	}

	@Test
	public void programmeAlphaAppearsUnderPodcastsAndHistoireWithoutDuplicates() {
		final CategoryDTO root = Novo19TestSupport.rootFromFixtures();
		final CategoryDTO podcasts = Novo19TestSupport.findChildByName(root, "Nos podcasts");
		final CategoryDTO histoire = Novo19TestSupport.findChildByName(
				Novo19TestSupport.findChildByName(root, Novo19Conf.SECTION_DOCUMENTARIES), "Histoire");
		assertNotNull(podcasts);
		assertNotNull(histoire);
		assertNotNull(Novo19TestSupport.findChildByName(podcasts, "Programme Alpha"));
		assertNotNull(Novo19TestSupport.findChildByName(histoire, "Programme Alpha"));
		assertEquals(1, countProgramsNamed(podcasts, "Programme Alpha"));
		assertEquals(1, countProgramsNamed(histoire, "Programme Alpha"));
	}

	@Test
	public void filmGammaAppearsOnlyInIdentifiedThemes() {
		final CategoryDTO root = Novo19TestSupport.rootFromFixtures();
		final CategoryDTO documentaries = Novo19TestSupport.findChildByName(root, Novo19Conf.SECTION_DOCUMENTARIES);
		assertNotNull(Novo19TestSupport.findChildByName(
				Novo19TestSupport.findChildByName(documentaries, "Histoire"), "Film Gamma"));
		assertNotNull(Novo19TestSupport.findChildByName(
				Novo19TestSupport.findChildByName(documentaries, "Politique"), "Film Gamma"));
		assertNotNull(Novo19TestSupport.findChildByName(
				Novo19TestSupport.findChildByName(documentaries, "Patrimoine et découvertes"), "Film Gamma"));
		assertNull(Novo19TestSupport.findChildByName(
				Novo19TestSupport.findChildByName(documentaries, "Immersion"), "Film Gamma"));
		assertNull(Novo19TestSupport.findChildByName(
				Novo19TestSupport.findChildByName(documentaries, "Société"), "Film Gamma"));
		assertEquals(1, countProgramsNamed(
				Novo19TestSupport.findChildByName(documentaries, "Histoire"), "Film Gamma"));
	}

	@Test
	public void unknownThemeFallsBackToSansThematique() {
		final Set<String> themes = Novo19TaxonomyMapper.resolveProgramThemes(
				new Novo19Tile("standalone", "VOD", "Standalone documentary", null, null, null,
						"/details/standalone-documentary", "standalone"),
				new Novo19BffPage("DETAILS", "standalone", "Standalone documentary", null, null, null, null), null);
		assertEquals(1, themes.size());
		assertTrue(themes.contains(Novo19Conf.THEME_UNCLASSIFIED));
	}

	@Test
	public void seriesBetaAppearsUnderSocieteWithSeasonRails() {
		final CategoryDTO root = Novo19TestSupport.rootFromFixtures();
		final CategoryDTO seriesBeta = Novo19TestSupport.findChildByName(
				Novo19TestSupport.findChildByName(
						Novo19TestSupport.findChildByName(root, Novo19Conf.SECTION_DOCUMENTARIES), "Société"),
				"Series Beta");
		assertNotNull(seriesBeta);
		assertNotNull(Novo19TestSupport.findChildByName(seriesBeta, "Saison 3"));
		assertEquals(Novo19Conf.CONTENT_KIND_PROGRAM,
				seriesBeta.getParameter(Novo19Conf.PARAMETER_CONTENT_KIND));
	}

	@Test
	public void multiThemeProgramAppearsUnderEachThemeOnce() {
		final CategoryDTO root = Novo19TestSupport.rootFromFixtures();
		final CategoryDTO documentaries = Novo19TestSupport.findChildByName(root, Novo19Conf.SECTION_DOCUMENTARIES);
		final CategoryDTO immersion = Novo19TestSupport.findChildByName(documentaries, "Immersion");
		final CategoryDTO societe = Novo19TestSupport.findChildByName(documentaries, "Société");
		assertNotNull(Novo19TestSupport.findChildByName(immersion, "Theme Alpha Programme"));
		assertNotNull(Novo19TestSupport.findChildByName(societe, "Theme Alpha Programme"));
		assertEquals(1, countProgramsNamed(immersion, "Theme Alpha Programme"));
	}

	@Test
	public void seriesBetaSeasonListsEpisodesFromSeasonRail() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO root = manager.findCategory().iterator().next();
		final CategoryDTO saison3 = Novo19TestSupport.findChildByName(
				Novo19TestSupport.findChildByName(
						Novo19TestSupport.findChildByName(
								Novo19TestSupport.findChildByName(root, Novo19Conf.SECTION_DOCUMENTARIES),
								"Société"),
						"Series Beta"),
				"Saison 3");
		assertNotNull(saison3);
		final Set<EpisodeDTO> episodes = manager.findEpisode(saison3);
		assertEquals(2, episodes.size());
	}

	@Test
	public void programmeAlphaEpisodeRailSkipsRecommendations() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO program = Novo19CatalogMapper.buildProgramCategory(
				new Novo19Tile("programme-alpha", "SERIE", "Programme Alpha", null, null, null,
						"/details/programme-alpha", "asset-programme-alpha"),
				"Programme Alpha");
		program.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_PROGRAM);
		final Set<EpisodeDTO> episodes = manager.findEpisode(program);
		assertEquals(1, episodes.size());
		assertTrue(episodes.iterator().next().getName().startsWith("Episode Alpha"));
	}

	@Test
	public void canonicalProgramIdUsesStableAssetId() throws Exception {
		final Novo19BffPage seriesBetaPage = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.detailPage("seriesBeta"), "fixture");
		final Novo19Tile tile = new Novo19Tile("tile", "SERIE", "Series Beta", null, null,
				null, "/details/series-beta", "tile");
		assertEquals("asset-series-beta",
				Novo19TaxonomyMapper.canonicalProgramId(tile, seriesBetaPage));
	}

	private static int countProgramsNamed(final CategoryDTO parent, final String name) {
		int count = 0;
		for (final CategoryDTO child : parent.getSubCategories()) {
			if (name.equals(child.getName())) {
				count++;
			}
		}
		return count;
	}

}
