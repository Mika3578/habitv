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
	public void programPageUrlPrefersExplicitParameter() {
		final CategoryDTO category = new CategoryDTO("plugin", "Cat", "slug-not-a-url", null);
		category.addParameter("PROGRAM_URL", "https://example.com/program");
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
		category.addParameter("PROGRAM_URL", "javascript:alert(1)");
		final EpisodeDTO episode = new EpisodeDTO(category, "ep", "http://video");
		assertEquals("https://fallback.example/show",
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
		final String longUrl = "https://example.com/abcdefghijklmnopqrstuvwxyz0123456789/extra";
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

	@Test
	public void formatProgramLinkLabelReturnsUnknownWhenCategoryIsNullAndNoUrl() {
		final EpisodeDTO nullCategoryEpisode = new EpisodeDTO(null, "ep1", "episode-id");
		assertEquals("Inconnu", EpisodeMetadataFormatting.formatProgramLinkLabel(nullCategoryEpisode));
	}

	@Test
	public void formatSourceReturnsEpisodeIdWhenCategoryIdIsNotHttpUrl() {
		final EpisodeDTO episode = new EpisodeDTO(
				new CategoryDTO("plugin", "program", "category-id", "mp4"), "ep1", "  episode-id  ");
		assertEquals("episode-id", EpisodeMetadataFormatting.formatSource(episode));
	}

	@Test
	public void formatSourceReturnsUnknownWhenEpisodeIdIsBlankAndNoProgramUrl() {
		final EpisodeDTO episode = new EpisodeDTO(
				new CategoryDTO("plugin", "program", "category-id", "mp4"), "ep1", "  ");
		assertEquals("Inconnu", EpisodeMetadataFormatting.formatSource(episode));
	}

	@Test
	public void shortenUrlDoesNotTruncateShortUrls() {
		final String shortUrl = "https://example.com/short";
		final EpisodeDTO episode = new EpisodeDTO(
				new CategoryDTO("plugin", null, shortUrl, "mp4"), "ep1", "id");
		assertEquals(shortUrl, EpisodeMetadataFormatting.formatProgramLinkLabel(episode));
	}

	@Test
	public void shortenUrlTruncatesAtExactlyMaxLength() {
		final String urlExactly48 = "https://example.com/xxxxxxxxxxxxxxxxxxxxxxxxxxxx";
		final EpisodeDTO episode48 = new EpisodeDTO(
				new CategoryDTO("plugin", null, urlExactly48, "mp4"), "ep1", "id");
		assertEquals(urlExactly48, EpisodeMetadataFormatting.formatProgramLinkLabel(episode48));

		final String urlExactly49 = "https://example.com/xxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
		final EpisodeDTO episode49 = new EpisodeDTO(
				new CategoryDTO("plugin", null, urlExactly49, "mp4"), "ep1", "id");
		assertEquals("https://example.com/xxxxxxxxxxxxxxxxxxxxxxxxx...",
				EpisodeMetadataFormatting.formatProgramLinkLabel(episode49));
	}
}
