package com.dabi.habitv.core.task;

import static org.junit.Assert.assertEquals;

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
	public void programPageUrlUsesCategoryHttpId() {
		final CategoryDTO program = new CategoryDTO("francetv", "Journal 20h",
				"https://www.france.tv/france-2/journal-20-heures/", "mp4");
		final EpisodeDTO episode = new EpisodeDTO(program, "ep1",
				"https://www.france.tv/france-2/journal-20-heures/1-ep.html");
		assertEquals("https://www.france.tv/france-2/journal-20-heures/",
				EpisodeMetadataFormatting.programPageUrl(episode));
		assertEquals("Journal 20h", EpisodeMetadataFormatting.formatProgramLinkLabel(episode));
	}

	@Test
	public void programPageUrlTrimsCategoryHttpId() {
		final CategoryDTO program = new CategoryDTO("francetv", "Journal 20h",
				"  https://www.france.tv/france-2/journal-20-heures/  ", "mp4");
		final EpisodeDTO episode = new EpisodeDTO(program, "ep1",
				"https://www.france.tv/france-2/journal-20-heures/1-ep.html");
		assertEquals("https://www.france.tv/france-2/journal-20-heures/",
				EpisodeMetadataFormatting.programPageUrl(episode));
	}

	@Test
	public void formatProgramLinkLabelUsesNameWithoutProgramUrl() {
		final CategoryDTO program = new CategoryDTO("francetv", "Journal 20h", "journal-20h",
				"mp4");
		final EpisodeDTO episode = new EpisodeDTO(program, "ep1",
				"https://www.france.tv/france-2/journal-20-heures/1-ep.html");
		assertEquals("Journal 20h", EpisodeMetadataFormatting.formatProgramLinkLabel(episode));
	}

	@Test
	public void formatProgramLinkLabelFallsBackToShortenedProgramUrlWhenNameMissing() {
		final String longUrl = "  https://example.com/abcdefghijklmnopqrstuvwxyz0123456789/extra  ";
		final EpisodeDTO nullNameEpisode = new EpisodeDTO(
				new CategoryDTO("plugin", null, longUrl, "mp4"), "ep1", "episode-id");
		assertEquals("https://example.com/abcdefghijklmnopqrstuvwxy...",
				EpisodeMetadataFormatting.formatProgramLinkLabel(nullNameEpisode));
		final EpisodeDTO blankNameEpisode = new EpisodeDTO(
				new CategoryDTO("plugin", "   ", longUrl, "mp4"), "ep2", "episode-id");
		assertEquals("https://example.com/abcdefghijklmnopqrstuvwxy...",
				EpisodeMetadataFormatting.formatProgramLinkLabel(blankNameEpisode));
	}

	@Test
	public void formatSourcePrefersTrimmedProgramPageUrlForHttpAndHttpsCategoryIds() {
		final EpisodeDTO httpEpisode = new EpisodeDTO(
				new CategoryDTO("plugin", "program", "  http://example.com/program  ", "mp4"), "ep1",
				"episode-id");
		assertEquals("http://example.com/program", EpisodeMetadataFormatting.formatSource(httpEpisode));
		final EpisodeDTO httpsEpisode = new EpisodeDTO(
				new CategoryDTO("plugin", "program", "  https://example.com/program  ", "mp4"), "ep2",
				"episode-id");
		assertEquals("https://example.com/program",
				EpisodeMetadataFormatting.formatSource(httpsEpisode));
	}

	@Test
	public void formatStatusLabelTrimsWhitespace() {
		assertEquals("DONE", EpisodeMetadataFormatting.formatStatusLabel("  DONE  "));
	}
}
