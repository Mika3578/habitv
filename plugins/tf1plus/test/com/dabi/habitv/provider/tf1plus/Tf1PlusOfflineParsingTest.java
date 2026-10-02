package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public class Tf1PlusOfflineParsingTest {

	@Test
	public void shouldExposeExpectedTopLevelChannelCategories() {
		Tf1PlusPluginManager plugin = new Tf1PlusPluginManager() {
			@Override
			protected String getUrlContent(String url) {
				return "";
			}
		};

		Set<CategoryDTO> categories = plugin.findCategory();
		assertEquals(7, categories.size());

		Set<String> names = new HashSet<String>();
		for (CategoryDTO category : categories) {
			names.add(category.getName());
			assertFalse(category.isDownloadable());
		}
		assertEquals(new HashSet<String>(java.util.Arrays.asList("TF1", "TMC", "TFX", "TF1 Séries Films", "LCI",
				"ARTE", "LCP - Public Sénat")), names);
	}

	@Test
	public void shouldDiscoverTf1EditorialRubricsFromGraphqlFixtures() {
		Tf1PlusPluginManager plugin = new FixtureTf1PlusPluginManager();
		CategoryDTO tf1 = findByName(plugin.findCategory(), "TF1");
		assertNotNull(tf1);

		Set<CategoryDTO> rubrics = tf1.getSubCategories();
		assertEquals(4, rubrics.size());
		assertNotNull(findByName(rubrics, "Séries"));
		assertNotNull(findByName(rubrics, "Films"));
		assertNotNull(findByName(rubrics, "Divertissements"));
		assertNotNull(findByName(rubrics, "Infos & Mag"));

		CategoryDTO series = findByName(rubrics, "Séries");
		Set<String> seriesPrograms = programNames(series);
		assertEquals(new HashSet<String>(java.util.Arrays.asList("Demain nous appartient", "Ici tout commence")),
				seriesPrograms);

		CategoryDTO films = findByName(rubrics, "Films");
		assertEquals(new HashSet<String>(java.util.Arrays.asList("Fixture movie")), programNames(films));

		CategoryDTO entertainment = findByName(rubrics, "Divertissements");
		assertEquals(new HashSet<String>(java.util.Arrays.asList("Koh-Lanta")), programNames(entertainment));

		CategoryDTO info = findByName(rubrics, "Infos & Mag");
		assertEquals(new HashSet<String>(java.util.Arrays.asList("Journal de 13 heures")), programNames(info));
	}

	@Test
	public void shouldDiscoverFlatProgramsForTmcFromGraphqlFixtures() {
		Tf1PlusPluginManager plugin = new FixtureTf1PlusPluginManager();
		CategoryDTO tmc = findByName(plugin.findCategory(), "TMC");
		assertNotNull(tmc);
		assertEquals(new HashSet<String>(java.util.Arrays.asList("Fixture replay")), programNames(tmc));
	}

	@Test
	public void shouldDiscoverFlatProgramsForArteFromGraphqlFixtures() {
		Tf1PlusPluginManager plugin = new FixtureTf1PlusPluginManager();
		CategoryDTO arte = findByName(plugin.findCategory(), "ARTE");
		assertNotNull(arte);
		assertEquals(new HashSet<String>(java.util.Arrays.asList("Fixture documentary")), programNames(arte));
	}

	@Test
	public void shouldIgnoreFooterNavigationReplayAndExternalLinks() {
		Tf1PlusPluginManager plugin = new FixtureTf1PlusPluginManager();
		CategoryDTO tf1 = findByName(plugin.findCategory(), "TF1");
		assertNotNull(tf1);
		Map<String, String> urlsByName = collectDownloadablePrograms(tf1);
		assertNull(urlsByName.get("Replay"));
		assertNull(urlsByName.get("Videos"));
		assertNull(urlsByName.get("Direct"));
		assertNull(urlsByName.get("Account"));
		assertNull(urlsByName.get("External"));
	}

	@Test
	public void shouldParseEpisodesFromGraphqlWithBasicRightsOnly() {
		Tf1PlusPluginManager plugin = new FixtureTf1PlusPluginManager();
		CategoryDTO category = new CategoryDTO(Tf1PlusConf.NAME, "Demain nous appartient",
				"https://www.tf1.fr/tf1/demain-nous-appartient", Tf1PlusConf.EXTENSION);
		Set<EpisodeDTO> episodes = plugin.findEpisode(category);
		assertEquals(1, episodes.size());
		EpisodeDTO episode = episodes.iterator().next();
		assertEquals(
				"https://www.tf1.fr/tf1/demain-nous-appartient/videos/demain-nous-appartient-du-mercredi-27-mai-2026-episode-2213.html",
				episode.getId());
		assertEquals("Saison 13 Episode 38 du 27 mai 2026", episode.getName());
		assertNotNull(episode.getEpisodeDate());
		assertEquals(Long.valueOf(1560L), episode.getDurationSeconds());
	}

	@Test
	public void shouldKeepNaturalEpisodeTitleFromGraphqlFixture() {
		Tf1PlusPluginManager plugin = new FixtureTf1PlusPluginManager();
		CategoryDTO category = new CategoryDTO(Tf1PlusConf.NAME, "Miraculous", "https://www.tf1.fr/tf1/miraculous",
				Tf1PlusConf.EXTENSION);
		Set<EpisodeDTO> episodes = plugin.findEpisode(category);
		assertEquals(1, episodes.size());
		EpisodeDTO episode = episodes.iterator().next();
		assertEquals("Miraculous : Les Aventures de Ladybug et Chat Noir - Renverse-Coeurs", episode.getName());
		assertEquals("https://www.tf1.fr/tf1/miraculous/videos/miraculous-renverse-coeurs.html", episode.getId());
		assertEquals(Long.valueOf(1320L), episode.getDurationSeconds());
	}

	@Test
	public void shouldExcludeMaxOnlyEpisodesWhenPremiumDownloadIsDisabled() {
		Tf1PlusPluginManager plugin = new FixtureTf1PlusPluginManager();
		CategoryDTO category = new CategoryDTO(Tf1PlusConf.NAME, "Automoto", "https://www.tf1.fr/tf1/automoto",
				Tf1PlusConf.EXTENSION);
		Set<EpisodeDTO> episodes = plugin.findEpisode(category);
		assertTrue(episodes.isEmpty());
	}

	@Test
	public void shouldExposeMaxOnlyAutomotoEpisodeWhenPremiumDownloadIsEnabled() {
		Tf1PlusPluginManager plugin = new FixtureTf1PlusPluginManager() {
			@Override
			protected boolean isPremiumDownloadEnabled() {
				return true;
			}
		};
		CategoryDTO category = new CategoryDTO(Tf1PlusConf.NAME, "Automoto", "https://www.tf1.fr/tf1/automoto",
				Tf1PlusConf.EXTENSION);
		Set<EpisodeDTO> episodes = plugin.findEpisode(category);
		assertEquals(1, episodes.size());
		EpisodeDTO episode = episodes.iterator().next();
		assertEquals("Automoto du 14 juin 2026", episode.getName());
		assertTrue(episode.getId().contains("#habitvTf1=fa698bd7-1328-467c-8cb3-4167b119973f,premium"));
		assertTrue(Tf1PlusEpisodeUrl.requiresPremiumDownload(episode.getId()));
	}

	@Test
	public void shouldExposeLegacyPremiumEpisodeWhenPremiumDownloadIsEnabled() {
		Tf1PlusPluginManager plugin = new FixtureTf1PlusPluginManager() {
			@Override
			protected boolean isPremiumDownloadEnabled() {
				return true;
			}
		};
		CategoryDTO category = new CategoryDTO(Tf1PlusConf.NAME, "Legacy premium",
				"https://www.tf1.fr/tf1/fixture-legacy-premium-replay", Tf1PlusConf.EXTENSION);
		Set<EpisodeDTO> episodes = plugin.findEpisode(category);
		assertEquals(1, episodes.size());
		EpisodeDTO episode = episodes.iterator().next();
		assertTrue(episode.getId().contains("#habitvTf1=14510494,premium"));
	}

	@Test
	public void shouldTagBasicMaxReplayWithPremiumFragmentWhenPremiumDownloadIsEnabled() {
		Tf1PlusPluginManager plugin = new FixtureTf1PlusPluginManager() {
			@Override
			protected boolean isPremiumDownloadEnabled() {
				return true;
			}
		};
		CategoryDTO category = new CategoryDTO(Tf1PlusConf.NAME, "Fixture basic max",
				"https://www.tf1.fr/tf1/fixture-basic-max-replay", Tf1PlusConf.EXTENSION);
		Set<EpisodeDTO> episodes = plugin.findEpisode(category);
		assertEquals(1, episodes.size());
		EpisodeDTO episode = episodes.iterator().next();
		assertTrue(episode.getId().contains("#habitvTf1=e72e51c8-af61-4278-b04e-34b1c8302b44,premium"));
		assertTrue(Tf1PlusEpisodeUrl.requiresPremiumDownload(episode.getId()));
	}

	@Test
	public void shouldExposePremiumAndBasicEpisodeWhenPremiumDownloadIsEnabled() {
		Tf1PlusPluginManager plugin = new FixtureTf1PlusPluginManager() {
			@Override
			protected boolean isPremiumDownloadEnabled() {
				return true;
			}
		};
		CategoryDTO category = new CategoryDTO(Tf1PlusConf.NAME, "Fixture replay",
				"https://www.tf1.fr/tmc/fixture-premium-basic-replay", Tf1PlusConf.EXTENSION);
		Set<EpisodeDTO> episodes = plugin.findEpisode(category);
		assertEquals(1, episodes.size());
		EpisodeDTO episode = episodes.iterator().next();
		assertTrue(episode.getId().contains("#habitvTf1=14510094,premium"));
		assertTrue(Tf1PlusEpisodeUrl.requiresPremiumDownload(episode.getId()));
	}

	@Test
	public void shouldParsePremiumEpisodeFragment() {
		assertEquals("14510494", Tf1PlusEpisodeUrl.parsePremiumStreamId(
				"https://www.tf1.fr/tf1/automoto/videos/automoto-du-31-mai-2026.html#habitvTf1=14510494,premium"));
		assertEquals("https://www.tf1.fr/tf1/automoto/videos/automoto-du-31-mai-2026.html",
				Tf1PlusEpisodeUrl.pageUrlWithoutFragment(
						"https://www.tf1.fr/tf1/automoto/videos/automoto-du-31-mai-2026.html#habitvTf1=14510494,premium"));
	}

	@Test
	public void shouldParseLegacyPremiumEpisodeFragment() {
		assertEquals("14510494", Tf1PlusEpisodeUrl.parsePremiumStreamId(
				"https://www.tf1.fr/tf1/automoto/videos/automoto-du-31-mai-2026.html#habitvTf1=14510494,protected"));
	}

	@Test
	public void shouldFailGracefullyWhenReplayPageIsUnreachable() {
		Tf1PlusPluginManager plugin = new Tf1PlusPluginManager() {
			@Override
			protected String getUrlContent(String url) {
				throw new IllegalStateException("offline fixture failure");
			}

			@Override
			public java.io.InputStream getInputStreamFromUrl(String url) {
				throw new IllegalStateException("offline fixture failure");
			}
		};
		CategoryDTO category = new CategoryDTO(Tf1PlusConf.NAME, "TF1", Tf1PlusConf.TF1_REPLAY_URL, Tf1PlusConf.EXTENSION);
		Set<EpisodeDTO> episodes = plugin.findEpisode(category);
		assertTrue(episodes.isEmpty());
	}

	@Test
	public void shouldReportSanitizedDiagnosticsForGraphqlStrategy() {
		Tf1PlusDiagnostics diagnostics = new Tf1PlusDiagnostics("TF1", Tf1PlusDiagnostics.STRATEGY_GRAPHQL);
		diagnostics.setSourceUrl(Tf1PlusConf.GRAPHQL_URL);
		diagnostics.setRootCauseSummary("no-basic-rights-episodes-found");
		String line = diagnostics.formatLogLine();
		assertTrue(line.contains("provider=TF1+"));
		assertTrue(line.contains("strategy=public-graphql-replay"));
		assertTrue(line.contains("cookiesEnabled=false"));
		assertFalse(line.toLowerCase().contains("cookie="));
	}

	@Test
	public void shouldUseTf1plusPluginName() {
		assertEquals("tf1plus", new Tf1PlusPluginManager().getName());
	}

	private Set<String> programNames(final CategoryDTO parent) {
		Set<String> names = new HashSet<String>();
		for (CategoryDTO subCategory : parent.getSubCategories()) {
			if (subCategory.isDownloadable()) {
				names.add(subCategory.getName());
			} else {
				names.addAll(programNames(subCategory));
			}
		}
		return names;
	}

	private Map<String, String> collectDownloadablePrograms(final CategoryDTO parent) {
		Map<String, String> urlsByName = new HashMap<String, String>();
		for (CategoryDTO subCategory : parent.getSubCategories()) {
			if (subCategory.isDownloadable()) {
				urlsByName.put(subCategory.getName(), subCategory.getId());
			} else {
				urlsByName.putAll(collectDownloadablePrograms(subCategory));
			}
		}
		return urlsByName;
	}

	private CategoryDTO findByName(final Set<CategoryDTO> categories, final String name) {
		for (CategoryDTO category : categories) {
			if (name.equals(category.getName())) {
				return category;
			}
		}
		return null;
	}
}
