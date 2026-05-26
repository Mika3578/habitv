package com.dabi.habitv.provider.francetv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class FranceTvUrlsTest {

	@Test
	public void programPageUrlSplitsChannelAndProgram() {
		assertEquals("https://www.france.tv/france-2/enchaines/",
				FranceTvUrls.programPageUrl("france-2_enchaines"));
		assertEquals("https://www.france.tv/france-5/c-dans-l-air/",
				FranceTvUrls.programPageUrl("france-5_c-dans-l-air"));
	}

	@Test
	public void programPageUrlRejectsMalformedPaths() {
		assertNull(FranceTvUrls.programPageUrl(null));
		assertNull(FranceTvUrls.programPageUrl(""));
		assertNull(FranceTvUrls.programPageUrl("noseparator"));
		assertNull(FranceTvUrls.programPageUrl("_orphan"));
		assertNull(FranceTvUrls.programPageUrl("orphan_"));
	}

	@Test
	public void programPathFromCategoryUrlExtractsChannelAndProgram() {
		assertEquals("france-2_enchaines",
				FranceTvUrls.programPathFromCategoryUrl("https://www.france.tv/france-2/enchaines/"));
		assertEquals("france-3_la-meteo",
				FranceTvUrls.programPathFromCategoryUrl("https://www.france.tv/france-3/la-meteo"));
	}

	@Test
	public void programPathFromCategoryUrlRejectsUnrelatedOrShallowUrls() {
		assertNull(FranceTvUrls.programPathFromCategoryUrl(null));
		assertNull(FranceTvUrls.programPathFromCategoryUrl(""));
		assertNull(FranceTvUrls.programPathFromCategoryUrl("https://www.france.tv/"));
		assertNull(FranceTvUrls.programPathFromCategoryUrl("https://www.france.tv/france-2/"));
		assertNull(FranceTvUrls.programPathFromCategoryUrl("https://example.com/france-2/enchaines/"));
	}

	@Test
	public void episodePageUrlBuildsCanonicalReplayUrl() {
		final Map<String, Object> item = sampleEpisode(8402202L, "Réparer", "france-2_enchaines", 1);
		final String url = FranceTvUrls.episodePageUrl(item);
		assertEquals(
				"https://www.france.tv/france-2/enchaines/enchaines-saison-1/8402202-reparer.html",
				url);
	}

	@Test
	public void episodePageUrlOmitsSeasonSegmentWhenAbsentOrZero() {
		final Map<String, Object> item = sampleEpisode(99L, "Pilote", "france-5_doc", 0);
		final String url = FranceTvUrls.episodePageUrl(item);
		assertEquals("https://www.france.tv/france-5/doc/99-pilote.html", url);

		final Map<String, Object> withoutSeason = sampleEpisode(42L, "Pilote", "france-5_doc", null);
		assertEquals("https://www.france.tv/france-5/doc/42-pilote.html",
				FranceTvUrls.episodePageUrl(withoutSeason));
	}

	@Test
	public void episodePageUrlFallsBackToPlaceholderSlugWhenTitleHasNoUsableChars() {
		final Map<String, Object> item = sampleEpisode(7L, "—", "france-4_show", null);
		assertEquals("https://www.france.tv/france-4/show/7-episode.html",
				FranceTvUrls.episodePageUrl(item));
	}

	@Test
	public void episodePageUrlReturnsNullWhenItemIsIncomplete() {
		assertNull("missing program node", FranceTvUrls.episodePageUrl(new HashMap<String, Object>()));

		final Map<String, Object> noId = new LinkedHashMap<>();
		final Map<String, Object> programNode = new LinkedHashMap<>();
		programNode.put("program_path", "france-2_x");
		noId.put("program", programNode);
		assertNull("missing video id", FranceTvUrls.episodePageUrl(noId));

		final Map<String, Object> malformedPath = new LinkedHashMap<>();
		final Map<String, Object> badProgram = new LinkedHashMap<>();
		badProgram.put("program_path", "noseparator");
		malformedPath.put("program", badProgram);
		malformedPath.put("id", 1L);
		assertNull("invalid program_path", FranceTvUrls.episodePageUrl(malformedPath));
	}

	@Test
	public void isReplayVideoTypeAcceptsKnownReplayKinds() {
		assertTrue(FranceTvUrls.isReplayVideoType("integrale"));
		assertTrue(FranceTvUrls.isReplayVideoType("unitaire"));
		assertFalse(FranceTvUrls.isReplayVideoType("extrait"));
		assertFalse(FranceTvUrls.isReplayVideoType("live"));
		assertFalse(FranceTvUrls.isReplayVideoType(""));
	}

	@Test
	public void sectionPageUrlUsesCategorySlugUnderChannel() {
		assertEquals("https://www.france.tv/france-3/cinema/",
				FranceTvUrls.sectionPageUrl("france-3", "cinema"));
		assertEquals("https://www.france.tv/france-2/",
				FranceTvUrls.sectionPageUrl("france-2", null));
	}

	@Test
	public void channelLabelMapsKnownSlugs() {
		assertEquals("France 2", FranceTvUrls.channelLabel("france-2"));
		assertEquals("France 3", FranceTvUrls.channelLabel("france-3"));
		assertEquals("France 4", FranceTvUrls.channelLabel("france-4"));
		assertEquals("France 5", FranceTvUrls.channelLabel("france-5"));
		assertEquals("La 1ère", FranceTvUrls.channelLabel("la1ere"));
	}

	@Test
	public void channelLabelEchoesUnknownSlug() {
		assertEquals("franceinfo", FranceTvUrls.channelLabel("franceinfo"));
		assertEquals("", FranceTvUrls.channelLabel(""));
	}

	private static Map<String, Object> sampleEpisode(final long id, final String title,
			final String programPath, final Integer season) {
		final Map<String, Object> item = new LinkedHashMap<>();
		item.put("id", id);
		item.put("title", title);
		final Map<String, Object> programNode = new LinkedHashMap<>();
		programNode.put("program_path", programPath);
		item.put("program", programNode);
		if (season != null) {
			item.put("season", season);
		}
		return item;
	}

}
