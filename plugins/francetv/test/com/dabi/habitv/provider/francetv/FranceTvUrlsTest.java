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
	public void programPathFromCategoryUrlExtractsPublicHubTaxonomyPaths() {
		assertEquals("sport_tennis_roland-garros",
				FranceTvUrls.programPathFromCategoryUrl(
						"https://www.france.tv/sport/tennis/roland-garros/"));
		assertEquals("franceinfo_l-info-s-eclaire",
				FranceTvUrls.programPathFromCategoryUrl(
						"https://www.france.tv/franceinfo/l-info-s-eclaire/"));
	}

	@Test
	public void programPageUrlFromTaxonomySlugBuildsBrowseUrls() {
		assertEquals("https://www.france.tv/sport/tennis/roland-garros/",
				FranceTvUrls.programPageUrlFromTaxonomySlug("sport_tennis_roland-garros"));
		assertEquals("https://www.france.tv/franceinfo/l-info-s-eclaire/",
				FranceTvUrls.programPageUrlFromTaxonomySlug("franceinfo_l-info-s-eclaire"));
	}

	@Test
	public void episodePageUrlUsesExplicitTaxonomyPathWhenProgramNodeMissing() {
		final Map<String, Object> item = new LinkedHashMap<String, Object>();
		item.put("id", 8533820L);
		item.put("title", "3e tour : Peyton Stearns vs Belinda Bencic");
		final String url = FranceTvUrls.episodePageUrl(item, "sport_tennis_roland-garros");
		assertTrue(url.startsWith("https://www.france.tv/sport/tennis/roland-garros/8533820-"));
		assertTrue(url.endsWith(".html"));
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
		assertEquals("Franceinfo", FranceTvUrls.channelLabel("franceinfo"));
		assertEquals("unknown-slug", FranceTvUrls.channelLabel("unknown-slug"));
		assertEquals("", FranceTvUrls.channelLabel(""));
	}

	@Test
	public void publicCollectionPageUrlDetectsSportLandingPages() {
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl(
				"https://www.france.tv/sport/tennis/roland-garros/"));
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl(
				"https://www.france.tv/sport/tennis/roland-garros/#section-en-direct"));
		assertFalse(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/france-2/enchaines/"));
		assertFalse(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/france-2/"));
	}

	@Test
	public void publicCollectionPageUrlDetectsCategoryLandingPages() {
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/series-et-fictions/"));
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/documentaires/"));
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/films/"));
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/societe/"));
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/info/"));
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/spectacles-et-culture/"));
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/sport/"));
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/jeux-et-divertissements/"));
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/enfants/"));
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/podcasts/"));
		assertEquals("Séries & fictions", FranceTvUrls.channelLabel("series-et-fictions"));
		assertEquals("Cinéma", FranceTvUrls.channelLabel("films"));
		assertEquals("Société", FranceTvUrls.channelLabel("societe"));
	}

	@Test
	public void publicCollectionPageUrlDetectsPartnerChannelHubs() {
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/arte/"));
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/tv5-monde/"));
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/france-24/"));
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/ina/"));
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/lcp/"));
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/public-senat/"));
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/mieux/"));
		assertFalse(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/mieux/direct.html"));
		assertEquals("Arte", FranceTvUrls.channelLabel("arte"));
		assertEquals("TV5 Monde Plus", FranceTvUrls.channelLabel("tv5-monde"));
		assertEquals("Public Sénat", FranceTvUrls.channelLabel("public-senat"));
	}

	@Test
	public void publicHubContainerUrlDetectsCuratedHubLandingPagesOnly() {
		assertTrue(FranceTvUrls.isPublicHubContainerUrl("https://www.france.tv/ina/"));
		assertTrue(FranceTvUrls.isPublicHubContainerUrl("https://www.france.tv/sport/"));
		assertTrue(FranceTvUrls.isPublicHubContainerUrl("https://www.france.tv/mieux/"));
		assertFalse(FranceTvUrls.isPublicHubContainerUrl("https://www.france.tv/ina/l-ina-eclaire-l-actu/"));
		assertFalse(FranceTvUrls.isPublicHubContainerUrl("https://www.france.tv/france-2/enchaines/"));
		assertFalse(FranceTvUrls.isPublicHubContainerUrl("https://www.france.tv/mieux/direct.html"));
	}

	@Test
	public void publicCollectionPageUrlDetectsFranceinfoHubAndProgramPages() {
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/franceinfo/"));
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl("https://www.france.tv/franceinfo/l-info-s-eclaire/"));
		assertTrue(FranceTvUrls.isPublicCollectionPageUrl(
				"https://www.france.tv/franceinfo/l-info-s-eclaire/#section-les-editions"));
		assertFalse(FranceTvUrls.isPublicCollectionPageUrl(
				"https://www.france.tv/franceinfo/l-info-s-eclaire/8472138-emission-du-vendredi-29-mai-2026.html"));
	}

	@Test
	public void sectionCategoryIdUsesStableFragment() {
		final String collection = "https://www.france.tv/sport/tennis/roland-garros/";
		assertEquals(collection + "#section-en-direct",
				FranceTvUrls.sectionCategoryId(collection, "en-direct"));
		assertEquals("en-direct",
				FranceTvUrls.sectionSlugFromCategoryId(collection + "#section-en-direct"));
		assertEquals(collection, FranceTvUrls.collectionUrlFromCategoryId(collection + "#section-en-direct"));
	}

	@Test
	public void videoReplayUrlDetectsNumericHtmlPaths() {
		assertTrue(FranceTvUrls.isVideoReplayUrl(
				"https://www.france.tv/sport/tennis/roland-garros/8533859-3e-tour.html"));
		assertFalse(FranceTvUrls.isVideoReplayUrl("https://www.france.tv/sport/direct.html"));
		assertFalse(FranceTvUrls.isVideoReplayUrl("https://www.franceinfo.fr/roland-garros/"));
	}

	@Test
	public void absoluteFranceTvUrlResolvesRelativePaths() {
		assertEquals("https://www.france.tv/sport/direct.html",
				FranceTvUrls.absoluteFranceTvUrl("/sport/direct.html"));
		assertEquals("https://www.france.tv/sport/tennis/roland-garros/8533859-x.html",
				FranceTvUrls.absoluteFranceTvUrl("/sport/tennis/roland-garros/8533859-x.html"));
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
