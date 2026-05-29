package com.dabi.habitv.provider.francetv;

import java.util.LinkedHashSet;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.framework.FrameworkConf;

interface FranceTvConf {

	String NAME = "francetv";

	String HOME_URL = "https://www.france.tv";

	String API_MOBILE_URL = "https://api-mobile.yatta.francetv.fr";

	String API_PLATFORM = "apps";

	String[] CHANNEL_SLUGS = { "france-2", "france-3", "france-4", "france-5", "la1ere" };

	/** Partner and primary hub slugs under {@code Pages publiques (france.tv)} (display order). */
	String[] PUBLIC_ROOT_DISPLAY_ORDER = { "sport", "franceinfo", "arte", "tv5-monde", "france-24", "ina", "lcp",
			"public-senat", "mieux" };

	/** Top-level france.tv category landing pages (URL browse only; not listed in the category tree). */
	String[] PUBLIC_CATEGORY_HUB_SLUGS = { "series-et-fictions", "documentaires", "films", "societe", "info",
			"spectacles-et-culture", "jeux-et-divertissements", "enfants", "podcasts" };

	/** All public hub slugs accepted for HTML browse URL classification. */
	String[] PUBLIC_HUB_SLUGS = buildPublicHubSlugs();

	/**
	 * Configured taxonomy seeds nested under a public hub ({@code hubSlug},
	 * {@code taxonomySlug}, display label). Used when API matching is ambiguous.
	 */
	String[][] PUBLIC_HUB_TAXONOMY_SEEDS = {
			{ "sport", "sport_tennis_roland-garros", "Roland-Garros" },
			{ "franceinfo", "franceinfo_l-info-s-eclaire", "L'info s'éclaire" } };

	String EXTENSION = FrameworkConf.MP4;

	static String[] publicRootHubDisplayOrder() {
		return PUBLIC_ROOT_DISPLAY_ORDER;
	}

	static boolean isCuratedPublicRootHub(final String hubSlug) {
		if (StringUtils.isEmpty(hubSlug)) {
			return false;
		}
		for (final String slug : PUBLIC_ROOT_DISPLAY_ORDER) {
			if (slug.equals(hubSlug)) {
				return true;
			}
		}
		return false;
	}

	static int publicHubOrderIndex(final String hubSlug) {
		if (StringUtils.isEmpty(hubSlug)) {
			return Integer.MAX_VALUE;
		}
		for (int i = 0; i < PUBLIC_ROOT_DISPLAY_ORDER.length; i++) {
			if (PUBLIC_ROOT_DISPLAY_ORDER[i].equals(hubSlug)) {
				return i;
			}
		}
		return Integer.MAX_VALUE;
	}

	static boolean hasConfiguredHubSeeds(final String hubSlug) {
		if (StringUtils.isEmpty(hubSlug)) {
			return false;
		}
		for (final String[] seed : PUBLIC_HUB_TAXONOMY_SEEDS) {
			if (seed.length >= 1 && hubSlug.equals(seed[0])) {
				return true;
			}
		}
		return false;
	}

	static String[] buildPublicHubSlugs() {
		final LinkedHashSet<String> slugs = new LinkedHashSet<String>();
		for (final String slug : PUBLIC_ROOT_DISPLAY_ORDER) {
			slugs.add(slug);
		}
		for (final String slug : PUBLIC_CATEGORY_HUB_SLUGS) {
			slugs.add(slug);
		}
		return slugs.toArray(new String[slugs.size()]);
	}

}
