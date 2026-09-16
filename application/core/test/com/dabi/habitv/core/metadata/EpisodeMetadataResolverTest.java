package com.dabi.habitv.core.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO;

public class EpisodeMetadataResolverTest {

	@Test
	public void publicationDateBlocksLegacyEpisodeDateAsAirDate() {
		final CategoryDTO category = new CategoryDTO("novo19", "Show", "https://example.test/show", "mp4");
		final EpisodeDTO episode = new EpisodeDTO(category, "Ep", "https://example.test/ep");
		final Date published = new Date(1_700_000_000_000L);
		episode.setEpisodeDate(published);
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		metadata.setPublicationDate(published);
		metadata.setEpisodeTitle("Ep");
		episode.setMetadata(metadata);

		final EpisodeMetadataDTO resolved = EpisodeMetadataResolver.resolve(episode);
		assertEquals(published, resolved.getPublicationDate());
		assertNull(resolved.getAirDate());
	}

	@Test
	public void legacyEpisodeDateBecomesAirDateWhenNoPublicationDate() {
		final CategoryDTO category = new CategoryDTO("file", "Cat", "id", "mp4");
		final EpisodeDTO episode = new EpisodeDTO(category, "Name", "url");
		final Date legacy = new Date(1_600_000_000_000L);
		episode.setEpisodeDate(legacy);

		final EpisodeMetadataDTO resolved = EpisodeMetadataResolver.resolve(episode);
		assertEquals(legacy, resolved.getAirDate());
		assertNull(resolved.getPublicationDate());
	}
}
