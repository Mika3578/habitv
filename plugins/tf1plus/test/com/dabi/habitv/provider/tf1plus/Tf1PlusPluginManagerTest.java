package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public class Tf1PlusPluginManagerTest {

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

		List<String> names = new ArrayList<>();
		for (CategoryDTO category : categories) {
			names.add(category.getName());
			assertFalse(category.isDownloadable());
		}
		assertEquals(Arrays.asList("TF1", "TMC", "TFX", "TF1 Séries Films", "LCI"), names);
	}

	@Test
	public void shouldParseReplayItemFromOfflineFixture() {
		Tf1PlusPluginManager plugin = new FixtureTf1PlusPluginManager();
		CategoryDTO category = new CategoryDTO(Tf1PlusConf.NAME, "TF1", Tf1PlusConf.TF1_REPLAY_URL, Tf1PlusConf.EXTENSION);
		Set<EpisodeDTO> episodes = plugin.findEpisode(category);
		assertEquals(1, episodes.size());
		EpisodeDTO episode = episodes.iterator().next();
		assertEquals("https://www.tf1.fr/tf1/replay/journal-20h/videos/jt-20h-edition-du-15-mai-2026.html", episode.getId());
		assertTrue(episode.getName().contains("JT 20H"));
		assertTrue(episode.getName().contains("Edition du 15 mai"));
		assertTrue(episode.getName().contains("thumbnail.example"));
		assertTrue(episode.getName().contains("2026-05-15"));
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
}
