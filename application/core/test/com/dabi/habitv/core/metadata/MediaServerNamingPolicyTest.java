package com.dabi.habitv.core.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO;

public class MediaServerNamingPolicyTest {

	@Test
	public void numberedEpisodeBuildsSeasonFolder() {
		final EpisodeMetadataDTO metadata = base("Un si grand soleil", "Titre de l'épisode");
		metadata.setSeasonNumber(Integer.valueOf(6));
		metadata.setEpisodeNumber(Integer.valueOf(123));

		assertEquals(
				"Un si grand soleil/Season 06/Un si grand soleil - S06E123 - Titre de l'épisode.mp4",
				MediaServerNamingPolicy.buildRelativePath(metadata, "mp4"));
		assertEquals("S06E123", MediaServerNamingPolicy.formatSeasonEpisode(metadata));
	}

	@Test
	public void datedEpisodeUsesYearFolder() {
		final EpisodeMetadataDTO metadata = base("C dans l'air", "Titre de l'émission");
		metadata.setAirDate(date(2026, Calendar.SEPTEMBER, 15));

		assertEquals(
				"C dans l'air/2026/C dans l'air - 2026-09-15 - Titre de l'émission.mp4",
				MediaServerNamingPolicy.buildRelativePath(metadata, "mp4"));
	}

	@Test
	public void incompleteMetadataKeepsFlatSeriesFolder() {
		final EpisodeMetadataDTO metadata = base("Complément d'enquête", "Titre");
		assertEquals("Complément d'enquête/Complément d'enquête - Titre.mp4",
				MediaServerNamingPolicy.buildRelativePath(metadata, "mp4"));
	}

	@Test
	public void missingSeriesFallsBackToEpisodeTitle() {
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		metadata.setEpisodeTitle("Seul titre");
		assertEquals("Seul titre.mp4", MediaServerNamingPolicy.buildRelativePath(metadata, "mp4"));
	}

	@Test
	public void missingEpisodeTitleStillBuildsSeriesPath() {
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		metadata.setSeriesTitle("Emission");
		assertEquals("Emission/Emission.mp4", MediaServerNamingPolicy.buildRelativePath(metadata, "mp4"));
	}

	@Test
	public void neverFabricatesSeasonEpisodeWhenIncomplete() {
		final EpisodeMetadataDTO metadata = base("Show", "Title");
		metadata.setSeasonNumber(Integer.valueOf(1));
		assertEquals("", MediaServerNamingPolicy.formatSeasonEpisode(metadata));
		assertFalse(MediaServerNamingPolicy.buildRelativePath(metadata, "mp4").contains("S00"));
		assertFalse(MediaServerNamingPolicy.buildRelativePath(metadata, "mp4").contains("S01"));
	}

	@Test
	public void sanitizesWindowsIllegalCharactersWithoutStrippingAccents() {
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		metadata.setSeriesTitle("C dans l'air?");
		metadata.setEpisodeTitle("Émission : spéciale");
		final String path = MediaServerNamingPolicy.buildRelativePath(metadata, "mp4");
		assertTrue(path.contains("Émission"));
		assertTrue(path.contains("spéciale"));
		assertFalse(path.contains("?"));
		assertFalse(path.contains(":"));
	}

	@Test
	public void resolverDoesNotUseCategoryAsSeries() {
		final CategoryDTO thematic = new CategoryDTO("arte", "Documentaires", "fr:DOC", "mp4");
		final EpisodeDTO episode = new EpisodeDTO(thematic, "Un film", "https://arte.example/1");
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		metadata.setEpisodeTitle("Un film");
		episode.setMetadata(metadata);

		final EpisodeMetadataDTO resolved = EpisodeMetadataResolver.resolve(episode);
		assertNull(resolved.getSeriesTitle());
		assertEquals("Un film", resolved.getEpisodeTitle());
		assertEquals("Un film.mp4", MediaServerNamingPolicy.buildRelativePath(resolved, "mp4"));
	}

	@Test
	public void outputResolverUsesMediaServerToken() {
		final CategoryDTO program = new CategoryDTO("francetv", "C dans l'air", "id", "mp4");
		final EpisodeDTO episode = new EpisodeDTO(program, "Titre", "https://france.tv/1");
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		metadata.setSeriesTitle("C dans l'air");
		metadata.setEpisodeTitle("Titre");
		metadata.setAirDate(date(2026, Calendar.SEPTEMBER, 15));
		episode.setMetadata(metadata);

		final String path = EpisodeOutputPathResolver.resolve(
				"D:/media/" + NamingProfile.MEDIA_SERVER_TOKEN, episode);
		assertEquals("D:/media/C dans l'air/2026/C dans l'air - 2026-09-15 - Titre.mp4", path);
	}

	private static EpisodeMetadataDTO base(final String series, final String title) {
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		metadata.setSeriesTitle(series);
		metadata.setEpisodeTitle(title);
		return metadata;
	}

	private static java.util.Date date(final int year, final int month, final int day) {
		final Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.ROOT);
		calendar.clear();
		calendar.set(year, month, day, 12, 0, 0);
		return calendar.getTime();
	}
}
