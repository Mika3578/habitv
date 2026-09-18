package com.dabi.habitv.provider.bfmtv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public class BfmTvCatalogMapperTest {

	@Test
	public void mapsReplayPrograms() throws Exception {
		final List<Map<String, Object>> items = replayPrograms();
		final CategoryDTO channel = BfmTvCatalogMapper.mapChannel("bfmtv", items);
		assertEquals("BFMTV", channel.getName());
		assertEquals("https://www.bfmtv.com/replay/bfmtv", channel.getId());
		assertEquals(2, channel.getSubCategories().size());
		final CategoryDTO morning = find(channel.getSubCategories(), "Morning News");
		assertNotNull(morning);
		assertTrue(morning.isDownloadable());
		assertEquals("1001", morning.getParameter(BfmTvConf.PARAMETER_CATEGORY));
	}

	@Test
	public void mapsReplayEpisodes() throws Exception {
		final Map<String, Object> body = BfmTvFixtureSupport.readMap("videos-page.json");
		final CategoryDTO program = new CategoryDTO(BfmTvConf.NAME, "Morning News",
				"https://www.bfmtv.com/replay/bfmtv/1001", BfmTvConf.EXTENSION);
		program.addParameter(BfmTvConf.PARAMETER_KIND, BfmTvConf.KIND_PROGRAM);
		program.addParameter(BfmTvConf.PARAMETER_CHANNEL, "bfmtv");
		program.addParameter(BfmTvConf.PARAMETER_CATEGORY, "1001");
		final Set<EpisodeDTO> episodes = BfmTvCatalogMapper.mapEpisodes(program, BfmTvJson.asMapList(body.get("videos")));
		assertEquals(1, episodes.size());
		final EpisodeDTO episode = episodes.iterator().next();
		assertEquals("Morning News Friday 18 September 2026", episode.getName());
		assertTrue(episode.getId().startsWith("https://www.bfmtv.com/replay-emissions/"));
		assertNotNull(episode.getMetadata());
		assertEquals("Morning News", episode.getMetadata().getSeriesTitle());
		assertEquals(Long.valueOf(3600L), episode.getMetadata().getDurationSeconds());
		assertNotNull(episode.getMetadata().getAirDate());
		assertEquals("BFMTV", episode.getMetadata().getChannel());
	}

	private static List<Map<String, Object>> replayPrograms() throws Exception {
		final Map<String, Object> body = BfmTvFixtureSupport.readMap("replay-page.json");
		final List<Map<String, Object>> contents = BfmTvJson.asMapList(BfmTvJson.nested(body, "page", "contents"));
		final List<Map<String, Object>> programs = new ArrayList<Map<String, Object>>();
		for (final Map<String, Object> content : contents) {
			for (final Map<String, Object> element : BfmTvJson.asMapList(content.get("elements"))) {
				programs.addAll(BfmTvJson.asMapList(element.get("items")));
			}
		}
		return programs;
	}

	private static CategoryDTO find(final Iterable<CategoryDTO> categories, final String name) {
		for (final CategoryDTO category : categories) {
			if (name.equals(category.getName())) {
				return category;
			}
		}
		return null;
	}

}
