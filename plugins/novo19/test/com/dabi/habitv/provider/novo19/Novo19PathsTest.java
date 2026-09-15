package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dabi.habitv.provider.novo19.dto.Novo19Rail;

public class Novo19PathsTest {

	@Test
	public void stripsQueryOnlyFromPublicPathResolution() {
		assertEquals("/details/sample",
				Novo19UrlBuilder.publicPathFromCategoryId(
						"https://novo19.ouest-france.fr/details/sample?autoplay=1"));
	}

	@Test
	public void stripsFragmentOnlyFromPublicPathResolution() {
		assertEquals("/details/sample",
				Novo19UrlBuilder.publicPathFromCategoryId(
						"https://novo19.ouest-france.fr/details/sample#player"));
	}

	@Test
	public void stripsQueryAndFragmentFromPublicPathResolution() {
		assertEquals("/details/sample",
				Novo19UrlBuilder.publicPathFromCategoryId(
						"https://novo19.ouest-france.fr/details/sample?autoplay=1#player"));
	}

	@Test
	public void stripsQueryAndFragmentFromBffPagePath() {
		final String url = Novo19UrlBuilder.bffPageByPath("/details/sample?foo=1#bar");
		assertEquals("https://novo19-bff.ouest-france.fr/api/1/public/frontends/web/pages/by-path/details/sample",
				url);
	}

	@Test
	public void rejectsUnapprovedBffHost() {
		try {
			Novo19UrlBuilder.bffAbsolutePath("https://evil.example.test/api/1/public/frontends/web/pages/categories");
			throw new AssertionError("expected IOException");
		} catch (final java.io.IOException e) {
			assertTrue(e.getMessage().contains("unapproved-bff-host"));
		}
	}

	@Test
	public void acceptsApprovedBffHost() throws Exception {
		assertEquals("https://novo19-bff.ouest-france.fr/api/1/public/frontends/web/pages/categories",
				Novo19UrlBuilder.bffAbsolutePath(
						"https://novo19-bff.ouest-france.fr/api/1/public/frontends/web/pages/categories"));
	}

	@Test
	public void canDownloadUsesParsedHostOnly() {
		assertTrue(Novo19UrlBuilder.isApprovedPublicDownloadUrl("https://novo19.ouest-france.fr/player/sample"));
		assertFalse(Novo19UrlBuilder.isApprovedPublicDownloadUrl("https://evil.com/novo19.ouest-france.fr/player"));
	}

	@Test
	public void canDownloadRecognisesNovo19Urls() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		assertEquals(Novo19PluginManager.DownloadableState.SPECIFIC,
				manager.canDownload("https://novo19.ouest-france.fr/player/sample"));
		assertEquals(Novo19PluginManager.DownloadableState.IMPOSSIBLE, manager.canDownload("https://example.com/video"));
		assertEquals(Novo19PluginManager.DownloadableState.IMPOSSIBLE,
				manager.canDownload("https://evil.com/novo19.ouest-france.fr/player/sample"));
	}

	@Test
	public void excludesPersonalAndLivePaths() {
		assertTrue(Novo19PathRules.isExcludedPublicPath("/mes-videos"));
		assertTrue(Novo19PathRules.isExcludedPublicPath("/player/novo19"));
		assertFalse(Novo19PathRules.isExcludedPublicPath("/details/sample"));
		assertFalse(Novo19PathRules.isExcludedPublicPath("/podcasts"));
	}

	@Test
	public void detectsRecommendationRailsPodcastPagesAndCatalogueSections() {
		assertTrue(Novo19PathRules.isRecommendationRailSrc(
				"/api/1/public/frontends/web/pages/BFF%7Casset-details,film-beta/sections/reco/tiles"));
		assertFalse(Novo19PathRules.isRecommendationRailSrc(
				"/api/1/public/frontends/web/pages/categories/sections/f6a789ee/tiles"));
		assertTrue(Novo19PathRules.isPodcastDetailRailSrc(
				"/api/1/public/frontends/web/pages/BFF%7Casset-details-podcast,podcast-alpha/sections/episodes/tiles"));
		assertEquals("Nos podcasts", Novo19PathRules.normalizeSectionTitle("Nos podcasts"));
		assertTrue(Novo19PathRules.isRecommendationRail(
				new Novo19Rail("reco", "Recommendations", "/sections/reco/tiles", null)));
		assertTrue(Novo19PathRules.isGenericCatalogueSectionTitle("Catalogue"));
		assertFalse(Novo19PathRules.isGenericCatalogueSectionTitle("Nos films"));
		assertTrue(Novo19PathRules.isCatalogueCarouselRail(
				new Novo19Rail("carousel", null, "/sections/carousel/tiles", null)));
		assertTrue(Novo19PathRules.isInfoEditorialRail(
				new Novo19Rail("info", "BANNER", "Info",
						"/sections/info/tiles", null)));
		assertTrue(Novo19PathRules.isTalkEditorialRail(
				new Novo19Rail("talk", "BANNER", "Notre talk",
						"/sections/talk/tiles", null)));
		assertTrue(Novo19PathRules.isCuratedSelectionRailTitle("Sélection Theme Alpha"));
		assertFalse(Novo19PathRules.isDocumentariesThemeRail(new Novo19Rail(
				"rail-curated", "CAROUSEL", "Sélection Theme Alpha", "/tiles", null)));
		assertTrue(Novo19PathRules.isDocumentariesMasterCatalogRail(new Novo19Rail(
				"rail-catalog", "CAROUSEL", Novo19Conf.SECTION_DOCUMENTARIES, "/tiles", null)));
		assertFalse(Novo19PathRules.isDocumentariesThemeRail(new Novo19Rail(
				"rail-catalog", "CAROUSEL", Novo19Conf.SECTION_DOCUMENTARIES, "/tiles", null)));
	}

	@Test
	public void exposesConfiguredCatalogueSectionLabels() {
		assertEquals("Nos films", Novo19Conf.SECTION_FILMS);
		assertEquals("Nos séries", Novo19Conf.SECTION_SERIES);
		assertEquals("Nos podcasts", Novo19Conf.SECTION_PODCASTS);
		assertEquals("Nos divertissements", Novo19Conf.SECTION_DIVERTISSEMENTS);
	}

	@Test
	public void podcastArgsDoNotDisableTlsVerification() {
		assertFalse(Novo19Conf.PODCAST_YT_DLP_ARGS.contains("--no-check-certificate"));
	}

}
