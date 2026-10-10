package com.dabi.habitv.provider.mlssoccer;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public class MLSSoccerListingOfflineTest {

	@Test
	public void findEpisodeReturnsEmptyWhenLegacyHighlightMarkupMissing() {
		final MLSSoccerPluginManager plugin = new MLSSoccerPluginManager() {
			@Override
			protected String getUrlContent(final String url) {
				return "<!DOCTYPE html><html><body><p>no node-title</p></body></html>";
			}
		};
		final CategoryDTO category = new CategoryDTO(MLSSoccerConf.NAME, "Highlights",
				"https://www.mlssoccer.com/videos/full-highlights", MLSSoccerConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(category);
		assertTrue(episodes.isEmpty());
	}
}
