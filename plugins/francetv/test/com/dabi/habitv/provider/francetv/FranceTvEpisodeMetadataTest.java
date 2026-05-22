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
