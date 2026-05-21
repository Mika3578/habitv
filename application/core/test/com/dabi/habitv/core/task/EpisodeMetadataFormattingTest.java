package com.dabi.habitv.core.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public class EpisodeMetadataFormattingTest {

	@Test
	public void formatDateHandlesNull() {
		assertEquals("Inconnu", EpisodeMetadataFormatting.formatDate(null));
	}

	@Test
	public void formatDurationHandlesNull() {
		assertEquals("Inconnu", EpisodeMetadataFormatting.formatDuration(null));
	}

	@Test
	public void formatSizeHandlesNull() {
		assertEquals("Inconnu", EpisodeMetadataFormatting.formatSize(null));
	}

	@Test
	public void sortByNameUsesComparable() {
		final EpisodeDTO first = episode("b");
		final EpisodeDTO second = episode("a");
		assertEquals(-1, second.compareTo(first));
	}

	@Test
	public void missingMetadataDoesNotThrow() {
		final EpisodeDTO episode = episode("test");
		EpisodeMetadataFormatting.formatDate(episode.getEpisodeDate());
		EpisodeMetadataFormatting.formatDuration(episode.getDurationSeconds());
		EpisodeMetadataFormatting.formatSize(episode.getSizeBytes());
		EpisodeMetadataFormatting.formatSource(episode);
	}

	private static EpisodeDTO episode(final String name) {
		return new EpisodeDTO(new CategoryDTO("plugin", "cat", "cat", null), name,
				"http://example/" + name);
	}

	@Test
	public void formatDateFormatsValue() {
		final Calendar calendar = new GregorianCalendar(2024, Calendar.JANUARY, 15);
		final Date date = calendar.getTime();
		assertEquals("2024-01-15", EpisodeMetadataFormatting.formatDate(date));
	}

	@Test
	public void programPageUrlNullEpisode() {
		assertNull(EpisodeMetadataFormatting.programPageUrl(null));
	}

	@Test
	public void programPageUrlPrefersExplicitParameter() {
		final CategoryDTO category = new CategoryDTO("plugin", "Cat", "slug-not-a-url", null);
		category.addParameter(EpisodeMetadataFormatting.PROGRAM_URL_PARAM,
				"https://example.com/program");
		final EpisodeDTO episode = new EpisodeDTO(category, "ep", "http://video");
		assertEquals("https://example.com/program",
				EpisodeMetadataFormatting.programPageUrl(episode));
	}

	@Test
	public void programPageUrlFallsBackToHttpIdentifier() {
		final CategoryDTO category = new CategoryDTO("plugin", "Cat",
				"https://www.france.tv/france-5/c-dans-l-air/", null);
		final EpisodeDTO episode = new EpisodeDTO(category, "ep", "http://video");
		assertEquals("https://www.france.tv/france-5/c-dans-l-air/",
				EpisodeMetadataFormatting.programPageUrl(episode));
	}

	@Test
	public void programPageUrlReturnsNullForNonUrlIdentifier() {
		final CategoryDTO category = new CategoryDTO("plugin", "Cat", "internal-slug", null);
		final EpisodeDTO episode = new EpisodeDTO(category, "ep", "http://video");
		assertNull(EpisodeMetadataFormatting.programPageUrl(episode));
	}

	@Test
	public void programPageUrlIgnoresNonHttpExplicitParameter() {
		final CategoryDTO category = new CategoryDTO("plugin", "Cat",
				"https://fallback.example/show", null);
		category.addParameter(EpisodeMetadataFormatting.PROGRAM_URL_PARAM, "javascript:alert(1)");
		final EpisodeDTO episode = new EpisodeDTO(category, "ep", "http://video");
		assertEquals("https://fallback.example/show",
				EpisodeMetadataFormatting.programPageUrl(episode));
	}

	@Test
	public void formatProgramLinkLabelUsesCategoryName() {
		final CategoryDTO category = new CategoryDTO("plugin", "C dans l'air", "id", null);
		final EpisodeDTO episode = new EpisodeDTO(category, "ep", "http://video");
		assertEquals("C dans l'air",
				EpisodeMetadataFormatting.formatProgramLinkLabel(episode));
	}

	@Test
	public void formatProgramLinkLabelFallsBackToUnknown() {
		assertEquals("Inconnu", EpisodeMetadataFormatting.formatProgramLinkLabel(null));
	}
}
