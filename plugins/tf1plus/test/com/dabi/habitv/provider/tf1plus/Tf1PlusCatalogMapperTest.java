package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public class Tf1PlusCatalogMapperTest {

	@Test
	public void mapsChannelGenreAndProgramTree() throws Exception {
		final Map<String, Object> body = Tf1PlusFixtureSupport.readMap("graphql-programs-tf1.json");
		final List<Map<String, Object>> programs = Tf1PlusJson
				.asMapList(Tf1PlusJson.nested(body, "data", "programs", "items"));
		final Map<String, List<Map<String, Object>>> byChannel = new LinkedHashMap<String, List<Map<String, Object>>>();
		byChannel.put("tf1", programs);
		byChannel.put("tmc", Collections.<Map<String, Object>>emptyList());
		byChannel.put("tfx", Collections.<Map<String, Object>>emptyList());
		byChannel.put("tf1-series-films", Collections.<Map<String, Object>>emptyList());
		byChannel.put("lci", Collections.<Map<String, Object>>emptyList());

		final Set<CategoryDTO> channels = Tf1PlusCatalogMapper.mapChannels(byChannel);
		assertEquals(5, channels.size());
		final CategoryDTO tf1 = find(channels, "TF1");
		assertNotNull(tf1);
		assertFalse(tf1.isDownloadable());
		assertEquals(2, tf1.getSubCategories().size());
		final CategoryDTO series = find(tf1.getSubCategories(), "Series and fiction");
		assertNotNull(series);
		final CategoryDTO program = find(series.getSubCategories(), "Sample Series");
		assertTrue(program.isDownloadable());
		assertEquals("sample-series", program.getParameter(Tf1PlusConf.PARAMETER_PROGRAM_SLUG));
	}

	@Test
	public void mapsReplayEpisodesAndSkipsExtracts() throws Exception {
		final Map<String, Object> body = Tf1PlusFixtureSupport.readMap("graphql-videos-replay.json");
		final List<Map<String, Object>> videos = Tf1PlusJson
				.asMapList(Tf1PlusJson.nested(body, "data", "programBySlug", "videos", "items"));
		final CategoryDTO program = new CategoryDTO(Tf1PlusConf.NAME, "Evening Magazine",
				"https://www.tf1.fr/tf1/evening-magazine", Tf1PlusConf.EXTENSION);
		program.addParameter(Tf1PlusConf.PARAMETER_KIND, Tf1PlusConf.KIND_PROGRAM);
		program.addParameter(Tf1PlusConf.PARAMETER_CHANNEL, "tf1");
		program.addParameter(Tf1PlusConf.PARAMETER_PROGRAM_SLUG, "evening-magazine");

		final Set<EpisodeDTO> episodes = Tf1PlusCatalogMapper.mapEpisodes(program, videos);
		assertEquals(1, episodes.size());
		final EpisodeDTO episode = episodes.iterator().next();
		assertEquals("Evening Magazine episode 1", episode.getName());
		assertEquals("https://www.tf1.fr/tf1/evening-magazine/videos/evening-magazine-episode-1.html", episode.getId());
		assertNotNull(episode.getMetadata());
		assertEquals("Evening Magazine", episode.getMetadata().getSeriesTitle());
		assertEquals(Integer.valueOf(2), episode.getMetadata().getSeasonNumber());
		assertEquals(Integer.valueOf(1), episode.getMetadata().getEpisodeNumber());
		assertEquals(Long.valueOf(2400L), episode.getMetadata().getDurationSeconds());
		assertEquals("TF1", episode.getMetadata().getChannel());
		assertEquals(Tf1PlusEpisodeMetadata.parseIsoDate("2026-09-18T18:00:00Z"),
				episode.getMetadata().getAirDate());
		assertEquals(Tf1PlusEpisodeMetadata.parseIsoDate("2026-09-18T18:05:00Z"),
				episode.getMetadata().getPublicationDate());
		assertEquals(episode.getMetadata().getAirDate(), episode.getEpisodeDate());
		assertNotNull(episode.getMetadata().getThumbnailUrl());
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
