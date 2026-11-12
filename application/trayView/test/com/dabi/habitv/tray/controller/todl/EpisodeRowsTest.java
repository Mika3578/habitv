package com.dabi.habitv.tray.controller.todl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public class EpisodeRowsTest {

	@Test
	public void nullEpisodeIsNotActionable() {
		assertFalse(EpisodeRows.isActionable(null));
	}

	@Test
	public void loadingPlaceholderIsNotActionable() {
		assertFalse(EpisodeRows.isActionable(new EpisodeDTO(null, "Chargement...", "")));
	}

	@Test
	public void episodeWithCategoryIsActionable() {
		final CategoryDTO category = new CategoryDTO("francetv", "Meteo", "meteo", "mp4");
		assertTrue(EpisodeRows.isActionable(new EpisodeDTO(category, "Episode", "https://example.org/v")));
	}
}
