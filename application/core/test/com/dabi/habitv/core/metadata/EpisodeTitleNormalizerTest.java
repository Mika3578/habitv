package com.dabi.habitv.core.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO;

public class EpisodeTitleNormalizerTest {

	@Test
	public void stripsSpacedPrefixWhenNumbersMatch() {
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		metadata.setEpisodeTitle("S8 E2013 - Épisode du vendredi");
		metadata.setSeasonNumber(Integer.valueOf(8));
		metadata.setEpisodeNumber(Integer.valueOf(2013));
		assertEquals("Épisode du vendredi", EpisodeTitleNormalizer.forFilename(metadata));
	}

	@Test
	public void stripsCompactPrefixWhenNumbersMatch() {
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		metadata.setEpisodeTitle("S01E06 - La flamme");
		metadata.setSeasonNumber(Integer.valueOf(1));
		metadata.setEpisodeNumber(Integer.valueOf(6));
		assertEquals("La flamme", EpisodeTitleNormalizer.forFilename(metadata));
	}

	@Test
	public void keepsPrefixWhenNumbersDoNotMatch() {
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		metadata.setEpisodeTitle("S1 E2 - Other show reference");
		metadata.setSeasonNumber(Integer.valueOf(8));
		metadata.setEpisodeNumber(Integer.valueOf(2013));
		assertEquals("S1 E2 - Other show reference", EpisodeTitleNormalizer.forFilename(metadata));
	}

	@Test
	public void leavesTitleWhenSeasonEpisodeIncomplete() {
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		metadata.setEpisodeTitle("S8 E2013 - Épisode");
		metadata.setSeasonNumber(Integer.valueOf(8));
		assertEquals("S8 E2013 - Épisode", EpisodeTitleNormalizer.forFilename(metadata));
	}

	@Test
	public void nullSafe() {
		assertNull(EpisodeTitleNormalizer.forFilename(null));
		assertNull(EpisodeTitleNormalizer.forFilename(new EpisodeMetadataDTO()));
	}
}
