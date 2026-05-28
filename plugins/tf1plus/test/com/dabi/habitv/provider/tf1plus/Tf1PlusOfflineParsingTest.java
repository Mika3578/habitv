package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
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
		assertEquals(5, categories.size());

		Set<String> names = new HashSet<>();
		for (CategoryDTO category : categories) {
			names.add(category.getName());
			assertFalse(category.isDownloadable());
		}
		assertEquals(new HashSet<String>(java.util.Arrays.asList("TF1", "TMC", "TFX", "TF1 Séries Films", "LCI")), names);
	}

	@Test
	public void shouldDiscoverProgramCategoriesFromTf1ReplayChannelPage() {
		Tf1PlusPluginManager plugin = new FixtureTf1PlusPluginManager();
		Set<CategoryDTO> categories = plugin.findCategory();
		CategoryDTO tf1 = findByName(categories, "TF1");
		assertNotNull(tf1);
		Set<CategoryDTO> subCategories = tf1.getSubCategories();
		assertEquals(3, subCategories.size());
		Set<String> programNames = new HashSet<String>();
		for (CategoryDTO subCategory : subCategories) {
			programNames.add(subCategory.getName());
			assertTrue(subCategory.isDownloadable());
		}
		assertEquals(new HashSet<String>(java.util.Arrays.asList("Demain nous appartient", "Ici tout commence", "Koh-Lanta")), programNames);
	}

	@Test
	public void shouldIgnoreFooterNavigationReplayAndExternalLinks() {
		Tf1PlusPluginManager plugin = new FixtureTf1PlusPluginManager();
		CategoryDTO tf1 = findByName(plugin.findCategory(), "TF1");
		assertNotNull(tf1);
		Map<String, String> urlsByName = new HashMap<String, String>();
		for (CategoryDTO subCategory : tf1.getSubCategories()) {
			urlsByName.put(subCategory.getName(), subCategory.getId());
		}
		assertNull(urlsByName.get("Replay"));
		assertNull(urlsByName.get("Videos"));
		assertNull(urlsByName.get("Direct"));
		assertNull(urlsByName.get("Account"));
		assertNull(urlsByName.get("External"));
	}

	@Test
	public void shouldParseEpisodeRowsFromProgramPageWithShortNamesOnly() {
		Tf1PlusPluginManager plugin = new FixtureTf1PlusPluginManager();
		CategoryDTO category = new CategoryDTO(Tf1PlusConf.NAME, "Demain nous appartient", "https://www.tf1.fr/tf1/demain-nous-appartient", Tf1PlusConf.EXTENSION);
		Set<EpisodeDTO> episodes = plugin.findEpisode(category);
		assertEquals(1, episodes.size());
		EpisodeDTO episode = episodes.iterator().next();
		assertEquals("https://www.tf1.fr/tf1/demain-nous-appartient/videos/demain-nous-appartient-du-mercredi-27-mai-2026-episode-2213.html", episode.getId());
		assertEquals("Saison 13 Episode 38 du 27 mai 2026", episode.getName());
		assertFalse(episode.getName().contains("thumbnail.example"));
		assertNotNull(episode.getEpisodeDate());
		assertEquals(Long.valueOf(1560L), episode.getDurationSeconds());
	}

	@Test
	public void shouldKeepNaturalEpisodeTitleUnchanged() {
		Tf1PlusPluginManager plugin = new FixtureTf1PlusPluginManager();
		CategoryDTO category = new CategoryDTO(Tf1PlusConf.NAME, "Miraculous", "https://www.tf1.fr/tf1/miraculous", Tf1PlusConf.EXTENSION);
		Set<EpisodeDTO> episodes = plugin.findEpisode(category);
		assertEquals(1, episodes.size());
		EpisodeDTO episode = episodes.iterator().next();
		assertEquals("Miraculous : Les Aventures de Ladybug et Chat Noir - Renverse-Coeurs", episode.getName());
		assertFalse(episode.getName().contains("thumbnail.example"));
		assertNotNull(episode.getEpisodeDate());
		assertEquals(Long.valueOf(1320L), episode.getDurationSeconds());
	}

	@Test
	public void shouldFailGracefullyWhenReplayPageIsUnreachable() {
		Tf1PlusPluginManager plugin = new Tf1PlusPluginManager() {
			@Override
			protected String getUrlContent(String url) {
				throw new IllegalStateException("offline fixture failure");
			}
		};
		CategoryDTO category = new CategoryDTO(Tf1PlusConf.NAME, "TF1", Tf1PlusConf.TF1_REPLAY_URL, Tf1PlusConf.EXTENSION);
		Set<EpisodeDTO> episodes = plugin.findEpisode(category);
		assertTrue(episodes.isEmpty());
	}

	private CategoryDTO findByName(Set<CategoryDTO> categories, String name) {
		for (CategoryDTO category : categories) {
			if (name.equals(category.getName())) {
				return category;
			}
		}
		return null;
	}
}
