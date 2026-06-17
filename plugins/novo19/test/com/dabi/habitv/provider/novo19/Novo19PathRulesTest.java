package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Novo19PathRulesTest {

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
				"/api/1/public/frontends/web/pages/BFF%7Casset-details,inferno/sections/reco/tiles"));
		assertFalse(Novo19PathRules.isRecommendationRailSrc(
				"/api/1/public/frontends/web/pages/categories/sections/f6a789ee/tiles"));
		assertTrue(Novo19PathRules.isPodcastDetailRailSrc(
				"/api/1/public/frontends/web/pages/BFF%7Casset-details-podcast,le-royaume-des-contes/sections/episodes/tiles"));
		assertEquals("Nos podcasts", Novo19PathRules.normalizeSectionTitle("Nos podcasts"));
		assertTrue(Novo19PathRules.isGenericCatalogueSectionTitle("Catalogue"));
		assertFalse(Novo19PathRules.isGenericCatalogueSectionTitle("Nos films"));
	}

}
