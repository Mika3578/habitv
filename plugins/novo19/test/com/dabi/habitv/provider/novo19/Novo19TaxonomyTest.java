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
		assertNull(Novo19TaxonomyMapper.normalizeTheme("La sélection Brut"));
		assertEquals("Histoire", Novo19TaxonomyMapper.normalizeTheme("Histoire"));
		assertEquals("Société", Novo19TaxonomyMapper.normalizeTheme("Société"));
	}

	@Test
	public void resolvesThemesFromDetailCategoriesTileSubtitleAndRailHint() throws Exception {
		final Novo19BffPage bucheron = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.readFixture("bff-page-bucheron.json"), "fixture");
		final Novo19Tile tile = new Novo19Tile("bucheron_565BFFb", "SERIE", "Bûcheron, un métier à hauts risques",
				"Société", null, null, "/details/bucheron-un-metier-a-hauts-risques", "bucheron_565BFFb");
		final Set<String> carouselThemes = Novo19TaxonomyMapper.resolveProgramThemes(tile, bucheron, null);
		assertTrue(carouselThemes.contains("Société"));
		assertFalse(carouselThemes.contains("Documentaire"));
		final Set<String> railThemes = Novo19TaxonomyMapper.resolveProgramThemes(tile, bucheron, "Société");
		assertEquals(1, railThemes.size());
		assertTrue(railThemes.contains("Société"));
	}

	@Test
	public void themeRailDiscoveryDoesNotInheritUnrelatedContentCategories() throws Exception {
		final Novo19BffPage elysee = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.readFixture("bff-page-elysee.json"), "fixture");
		final Novo19Tile tile = new Novo19Tile("elysee-secrets_565BFFb", "VOD", "Elysée, les secrets d'un palais",
				null, null, null, "/details/elysee-les-secrets-d-un-palais", "elysee-secrets_565BFFb");
		final Set<String> immersionThemes = Novo19TaxonomyMapper.resolveProgramThemes(tile, elysee, "Immersion");
		assertTrue(immersionThemes.contains("Immersion"));
		assertFalse(immersionThemes.contains("Politique"));
		assertFalse(immersionThemes.contains("Histoire"));
	}

	@Test
	public void cuisinonsAppearsUnderPodcastsAndHistoireWithoutDuplicates() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO root = manager.findCategory().iterator().next();
		final CategoryDTO podcasts = findChildByName(root, "Nos podcasts");
		final CategoryDTO histoire = findChildByName(findChildByName(root, Novo19Conf.SECTION_DOCUMENTARIES),
				"Histoire");
		assertNotNull(podcasts);
		assertNotNull(histoire);
		assertNotNull(findChildByName(podcasts, "Cuisinons l'histoire"));
		assertNotNull(findChildByName(histoire, "Cuisinons l'histoire"));
		assertEquals(1, countProgramsNamed(podcasts, "Cuisinons l'histoire"));
		assertEquals(1, countProgramsNamed(histoire, "Cuisinons l'histoire"));
	}

	@Test
	public void cuisinonsAppearsUnderHistoire() {
		cuisinonsAppearsUnderPodcastsAndHistoireWithoutDuplicates();
	}

	@Test
	public void elyseeAppearsOnlyInEvidentifiedThemes() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO root = manager.findCategory().iterator().next();
		final CategoryDTO documentaries = findChildByName(root, Novo19Conf.SECTION_DOCUMENTARIES);
		assertNotNull(findChildByName(findChildByName(documentaries, "Histoire"),
				"Elysée, les secrets d'un palais"));
		assertNotNull(findChildByName(findChildByName(documentaries, "Politique"),
				"Elysée, les secrets d'un palais"));
		assertNotNull(findChildByName(findChildByName(documentaries, "Patrimoine et découvertes"),
				"Elysée, les secrets d'un palais"));
		assertNull(findChildByName(findChildByName(documentaries, "Immersion"),
				"Elysée, les secrets d'un palais"));
		assertNull(findChildByName(findChildByName(documentaries, "Société"),
				"Elysée, les secrets d'un palais"));
		assertEquals(1, countProgramsNamed(findChildByName(documentaries, "Histoire"),
				"Elysée, les secrets d'un palais"));
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
	public void bucheronAppearsUnderSocieteWithSeasonRails() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO root = manager.findCategory().iterator().next();
		final CategoryDTO bucheron = findChildByName(
				findChildByName(findChildByName(root, Novo19Conf.SECTION_DOCUMENTARIES), "Société"),
				"Bûcheron, un métier à hauts risques");
		assertNotNull(bucheron);
		assertNotNull(findChildByName(bucheron, "Saison 3"));
		assertEquals(Novo19Conf.CONTENT_KIND_PROGRAM,
				bucheron.getParameter(Novo19Conf.PARAMETER_CONTENT_KIND));
	}

	@Test
	public void multiThemeProgramAppearsUnderEachThemeOnce() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO root = manager.findCategory().iterator().next();
		final CategoryDTO documentaries = findChildByName(root, Novo19Conf.SECTION_DOCUMENTARIES);
		final CategoryDTO immersion = findChildByName(documentaries, "Immersion");
		final CategoryDTO societe = findChildByName(documentaries, "Société");
		assertNotNull(findChildByName(immersion, "Aux commandes des géants des mers"));
		assertNotNull(findChildByName(societe, "Aux commandes des géants des mers"));
		assertEquals(1, countProgramsNamed(immersion, "Aux commandes des géants des mers"));
	}

	@Test
	public void bucheronSeasonListsEpisodesFromSeasonRail() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO root = manager.findCategory().iterator().next();
		final CategoryDTO saison3 = findChildByName(
				findChildByName(
						findChildByName(findChildByName(root, Novo19Conf.SECTION_DOCUMENTARIES), "Société"),
						"Bûcheron, un métier à hauts risques"),
				"Saison 3");
		assertNotNull(saison3);
		final Set<EpisodeDTO> episodes = manager.findEpisode(saison3);
		assertEquals(2, episodes.size());
	}

	@Test
	public void cuisinonsEpisodeRailSkipsRecommendations() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO program = Novo19CatalogMapper.buildProgramCategory(
				new Novo19Tile("cuisinons", "SERIE", "Cuisinons l'histoire", null, null, null,
						"/details/cuisinons-l-histoire", "cuisinons-lhistoire_565BFFb"),
				"Cuisinons l'histoire");
		program.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_PROGRAM);
		final Set<EpisodeDTO> episodes = manager.findEpisode(program);
		assertEquals(1, episodes.size());
		assertTrue(episodes.iterator().next().getName().startsWith("Le croissant"));
	}

	@Test
	public void canonicalProgramIdUsesStableAssetId() throws Exception {
		final Novo19BffPage bucheron = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.readFixture("bff-page-bucheron.json"), "fixture");
		final Novo19Tile tile = new Novo19Tile("tile", "SERIE", "Bûcheron, un métier à hauts risques", null, null,
				null, "/details/bucheron-un-metier-a-hauts-risques", "tile");
		assertEquals("18e2900d-e0fb-4282-8110-9505ab91e1e9_565BFFb",
				Novo19TaxonomyMapper.canonicalProgramId(tile, bucheron));
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
