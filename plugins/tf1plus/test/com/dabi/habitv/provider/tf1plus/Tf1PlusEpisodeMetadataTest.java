package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public class Tf1PlusEpisodeMetadataTest {

	@Test
	public void mapsBroadcastToAirDateAndPublishedToPublication() {
		final Map<String, Object> video = new LinkedHashMap<String, Object>();
		video.put("date", "2026-09-18T18:00:00Z");
		video.put("published", "2026-09-18T18:05:00Z");
		final EpisodeDTO episode = episode();

		Tf1PlusEpisodeMetadata.apply(video, episode, episode.getCategory());

		final Date air = Tf1PlusEpisodeMetadata.parseIsoDate("2026-09-18T18:00:00Z");
		final Date published = Tf1PlusEpisodeMetadata.parseIsoDate("2026-09-18T18:05:00Z");
		assertEquals(air, episode.getMetadata().getAirDate());
		assertEquals(published, episode.getMetadata().getPublicationDate());
		assertEquals(air, episode.getEpisodeDate());
	}

	@Test
	public void publishedOnlyDoesNotBecomeAirDate() {
		final Map<String, Object> video = new LinkedHashMap<String, Object>();
		video.put("published", "2026-09-18T18:05:00Z");
		final EpisodeDTO episode = episode();

		Tf1PlusEpisodeMetadata.apply(video, episode, episode.getCategory());

		assertNull(episode.getMetadata().getAirDate());
		assertNull(episode.getEpisodeDate());
		assertEquals(Tf1PlusEpisodeMetadata.parseIsoDate("2026-09-18T18:05:00Z"),
				episode.getMetadata().getPublicationDate());
	}

	@Test
	public void broadcastOnlySetsAirDateWithoutPublication() {
		final Map<String, Object> video = new LinkedHashMap<String, Object>();
		video.put("date", "2026-09-18T18:00:00Z");
		final EpisodeDTO episode = episode();

		Tf1PlusEpisodeMetadata.apply(video, episode, episode.getCategory());

		final Date air = Tf1PlusEpisodeMetadata.parseIsoDate("2026-09-18T18:00:00Z");
		assertEquals(air, episode.getMetadata().getAirDate());
		assertEquals(air, episode.getEpisodeDate());
		assertNull(episode.getMetadata().getPublicationDate());
	}

	private static EpisodeDTO episode() {
		final CategoryDTO program = new CategoryDTO(Tf1PlusConf.NAME, "Evening Magazine",
				"https://www.tf1.fr/tf1/evening-magazine", Tf1PlusConf.EXTENSION);
		return new EpisodeDTO(program, "Evening Magazine episode 1",
				"https://www.tf1.fr/tf1/evening-magazine/videos/evening-magazine-episode-1.html");
	}

}
