package com.dabi.habitv.api.plugin.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

public class EpisodeMetadataDTOTest {

	@Test
	public void blanksBecomeNullAndSeasonEpisodeRequiresBoth() {
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		metadata.setSeriesTitle("  ");
		metadata.setEpisodeTitle(" Titre ");
		assertNull(metadata.getSeriesTitle());
		assertEquals("Titre", metadata.getEpisodeTitle());
		assertFalse(metadata.hasSeasonAndEpisode());

		metadata.setSeasonNumber(Integer.valueOf(6));
		assertFalse(metadata.hasSeasonAndEpisode());
		metadata.setEpisodeNumber(Integer.valueOf(123));
		assertTrue(metadata.hasSeasonAndEpisode());
	}

	@Test
	public void airDateIsDefensivelyCopied() {
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		final Date original = new Date(1_700_000_000_000L);
		metadata.setAirDate(original);
		original.setTime(0L);
		assertEquals(1_700_000_000_000L, metadata.getAirDate().getTime());
	}

	@Test
	public void publicationDateAndThumbnailAreOptional() {
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		metadata.setPublicationDate(new Date(1_700_000_000_000L));
		metadata.setThumbnailUrl(" https://example.test/a.jpg ");
		metadata.setContentLanguage(" fr ");
		assertEquals(1_700_000_000_000L, metadata.getPublicationDate().getTime());
		assertEquals("https://example.test/a.jpg", metadata.getThumbnailUrl());
		assertEquals("fr", metadata.getContentLanguage());
		assertNull(metadata.getAirDate());
	}
}
