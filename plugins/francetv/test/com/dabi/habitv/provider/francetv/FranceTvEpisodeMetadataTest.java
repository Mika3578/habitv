package com.dabi.habitv.provider.francetv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public class FranceTvEpisodeMetadataTest {

	@Test
	public void parsesBroadcastDateAndDurationFromApiItem() {
		final Map<String, Object> item = new LinkedHashMap<>();
		item.put("broadcast_begin_date", Integer.valueOf(1777756706));
		item.put("duration", Integer.valueOf(157));

		final Date date = FranceTvEpisodeMetadata.parseBroadcastDate(item);
		assertNotNull(date);
		assertEquals(1777756706000L, date.getTime());

		assertEquals(Long.valueOf(157), FranceTvEpisodeMetadata.parseDurationSeconds(item));
	}

	@Test
	public void applySetsEpisodeFields() {
		final Map<String, Object> item = new LinkedHashMap<>();
		item.put("broadcast_begin_date", Long.valueOf(1700000000L));
		item.put("duration", Long.valueOf(3600));

		final CategoryDTO program = new CategoryDTO("francetv", "JT 20h",
				"https://www.france.tv/france-2/journal-20-heures/", "mp4");
		final EpisodeDTO episode = new EpisodeDTO(program, "Episode 1",
				"https://www.france.tv/france-2/journal-20-heures/1-ep.html");

		FranceTvEpisodeMetadata.apply(item, episode);

		assertNotNull(episode.getEpisodeDate());
		assertEquals(Long.valueOf(3600), episode.getDurationSeconds());
		assertNull(episode.getSizeBytes());
		assertNotNull(episode.getMetadata());
		assertEquals("JT 20h", episode.getMetadata().getSeriesTitle());
		assertEquals("Episode 1", episode.getMetadata().getEpisodeTitle());
		assertEquals(Long.valueOf(3600), episode.getMetadata().getDurationSeconds());
		assertEquals("https://www.france.tv/france-2/journal-20-heures/1-ep.html",
				episode.getMetadata().getSourceUrl());
		assertNull(episode.getMetadata().getSeasonNumber());
		assertNull(episode.getMetadata().getEpisodeNumber());
	}

	@Test
	public void applyMapsSeasonAndChannelWithoutInventingEpisodeNumber() {
		final Map<String, Object> item = new LinkedHashMap<>();
		item.put("broadcast_begin_date", Long.valueOf(1700000000L));
		item.put("duration", Long.valueOf(100));
		item.put("season", Integer.valueOf(6));
		item.put("id", Integer.valueOf(42));

		final CategoryDTO program = new CategoryDTO("francetv", "Un si grand soleil",
				"https://www.france.tv/france-2/un-si-grand-soleil/", "mp4");
		final EpisodeDTO episode = new EpisodeDTO(program, "Titre",
				"https://www.france.tv/france-2/un-si-grand-soleil/ep.html");

		FranceTvEpisodeMetadata.apply(item, episode, "france-2_un-si-grand-soleil");

		assertEquals(Integer.valueOf(6), episode.getMetadata().getSeasonNumber());
		assertNull(episode.getMetadata().getEpisodeNumber());
		assertEquals("france-2", episode.getMetadata().getChannel());
		assertEquals("42", episode.getMetadata().getProviderEpisodeId());
	}

	@Test
	public void applyMapsOptionalDescriptionAndThumbnailWhenPresent() {
		final Map<String, Object> item = new LinkedHashMap<>();
		item.put("broadcast_begin_date", Long.valueOf(1700000000L));
		item.put("duration", Long.valueOf(100));
		item.put("description", "Synopsis");
		item.put("image_url", "https://example.test/thumb.jpg");

		final CategoryDTO program = new CategoryDTO("francetv", "JT 20h",
				"https://www.france.tv/france-2/journal-20-heures/", "mp4");
		final EpisodeDTO episode = new EpisodeDTO(program, "Episode 1",
				"https://www.france.tv/france-2/journal-20-heures/1-ep.html");

		FranceTvEpisodeMetadata.apply(item, episode, "france-2_journal");

		assertEquals("Synopsis", episode.getMetadata().getDescription());
		assertEquals("https://example.test/thumb.jpg", episode.getMetadata().getThumbnailUrl());
		assertNotNull(episode.getMetadata().getAirDate());
		assertNull(episode.getMetadata().getPublicationDate());
	}

	@Test
	public void fallsBackToBeginDateWhenBroadcastMissing() {
		final Map<String, Object> item = new LinkedHashMap<>();
		item.put("begin_date", Long.valueOf(1600000000L));

		final Date date = FranceTvEpisodeMetadata.parseBroadcastDate(item);
		assertNotNull(date);
		assertEquals(1600000000000L, date.getTime());
	}
}
