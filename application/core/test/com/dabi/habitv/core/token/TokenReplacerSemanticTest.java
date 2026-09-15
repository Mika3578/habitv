package com.dabi.habitv.core.token;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO;

public class TokenReplacerSemanticTest {

	private EpisodeDTO episode;

	@Before
	public void setUp() {
		final CategoryDTO category = new CategoryDTO("francetv", "CategoryLabel", "catId", "mp4");
		episode = new EpisodeDTO(category, "LegacyDisplayName", "https://example/ep");
		episode.setNum(5);
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		metadata.setSeriesTitle("Un si grand soleil");
		metadata.setEpisodeTitle("Titre sémantique");
		metadata.setSeasonNumber(Integer.valueOf(6));
		metadata.setEpisodeNumber(Integer.valueOf(3));
		metadata.setAirDate(airDate(2026, Calendar.SEPTEMBER, 15));
		episode.setMetadata(metadata);
		episode.setEpisodeDate(airDate(2026, Calendar.SEPTEMBER, 15));
		TokenReplacer.setCutSize(5);
	}

	@Test
	public void legacyTokensKeepHistoricalSemantics() {
		assertEquals("CategoryLabel", TokenReplacer.replaceAll("#TVSHOW_NAME#", episode));
		assertEquals("LegacyDisplayName", TokenReplacer.replaceAll("#EPISODE_NAME#", episode));
		assertEquals("5", TokenReplacer.replaceAll("#NUM#", episode));
		final String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		assertEquals(today, TokenReplacer.replaceAll("#DATE§yyyy-MM-dd#", episode));
	}

	@Test
	public void airDateUsesBroadcastDateNotDownloadDate() {
		final String air = TokenReplacer.replaceAll("#AIR_DATE§yyyy-MM-dd#", episode);
		final String downloadDate = TokenReplacer.replaceAll("#DATE§yyyy-MM-dd#", episode);
		assertEquals("2026-09-15", air);
		assertNotEquals("AIR_DATE must not silently equal download DATE when dates differ",
				"1970-01-01", air);
		// When run on a day other than the fixture air date, prove DATE != AIR_DATE.
		if (!"2026-09-15".equals(downloadDate)) {
			assertNotEquals(downloadDate, air);
		}
		assertEquals("2026-09-15", TokenReplacer.replaceAll("#EPISODE_DATE§yyyy-MM-dd#", episode));
	}

	@Test
	public void semanticTokensUseCanonicalMetadata() {
		assertEquals("Un si grand soleil", TokenReplacer.replaceAll("#SERIES_NAME#", episode));
		assertEquals("Un si grand soleil", TokenReplacer.replaceAll("#SHOW_NAME#", episode));
		assertEquals("Titre sémantique", TokenReplacer.replaceAll("#EPISODE_TITLE#", episode));
		assertEquals("6", TokenReplacer.replaceAll("#SEASON_NUMBER#", episode));
		assertEquals("3", TokenReplacer.replaceAll("#EPISODE_NUMBER#", episode));
		assertEquals("S06E03", TokenReplacer.replaceAll("#SEASON_EPISODE#", episode));
	}

	@Test
	public void seasonEpisodeTokenStaysEmptyWithoutBothNumbers() {
		episode.getMetadata().setEpisodeNumber(null);
		assertEquals("", TokenReplacer.replaceAll("#SEASON_EPISODE#", episode));
		assertFalse(TokenReplacer.replaceAll("#SEASON_EPISODE#", episode).contains("S00"));
	}

	@Test
	public void semanticTokensPreserveFrenchAccents() {
		episode.getMetadata().setSeriesTitle("C dans l'air");
		episode.getMetadata().setEpisodeTitle("Émission spéciale");
		assertEquals("C dans l'air", TokenReplacer.replaceAll("#SERIES_NAME#", episode));
		assertEquals("Émission spéciale", TokenReplacer.replaceAll("#EPISODE_TITLE#", episode));
	}

	private static Date airDate(final int year, final int month, final int day) {
		final Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.ROOT);
		calendar.clear();
		calendar.set(year, month, day, 12, 0, 0);
		return calendar.getTime();
	}
}
