package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class Tf1PlusRightsTest {

	@Test
	public void shouldTreatBasicMaxWithAuthAsPremiumReplayCandidate() {
		final Map<String, Object> video = videoWithRights(true, "BASIC", "MAX");
		assertTrue(Tf1PlusRights.hasDownloadableRights(video, false));
		assertFalse(Tf1PlusRights.isYtDlpEligible(video));
		assertTrue(Tf1PlusRights.shouldUsePremiumReplay(video, true));
		assertFalse(Tf1PlusRights.shouldUsePremiumReplay(video, false));
	}

	@Test
	public void shouldHideMaxOnlyWithoutPremiumConfiguration() {
		final Map<String, Object> video = videoWithRights(true, "MAX");
		assertFalse(Tf1PlusRights.hasDownloadableRights(video, false));
		assertTrue(Tf1PlusRights.isSubscriptionOnly(video));
		assertTrue(Tf1PlusRights.shouldUsePremiumReplay(video, true));
	}

	@Test
	public void shouldKeepLegacyBasicOnlyOnYtDlpPath() {
		final Map<String, Object> video = videoWithRights(false, "BASIC");
		assertTrue(Tf1PlusRights.isYtDlpEligible(video));
		assertTrue(Tf1PlusRights.hasDownloadableRights(video, false));
		assertFalse(Tf1PlusRights.shouldUsePremiumReplay(video, true));
	}

	@Test
	public void shouldSupportLegacyPremiumOnlyRights() {
		final Map<String, Object> video = videoWithRights(false, "PREMIUM");
		assertFalse(Tf1PlusRights.hasDownloadableRights(video, false));
		assertTrue(Tf1PlusRights.hasDownloadableRights(video, true));
		assertTrue(Tf1PlusRights.shouldUsePremiumReplay(video, true));
	}

	@Test
	public void shouldResolveAutomotoProgramSlugAlias() {
		assertEquals("auto-moto", Tf1PlusProgramSlug.resolveForGraphql("automoto"));
		assertEquals("demain-nous-appartient", Tf1PlusProgramSlug.resolveForGraphql("demain-nous-appartient"));
	}

	@Test
	public void shouldNotTreatEmptyRightsWithUuidAsPremiumWhenConfigured() {
		final Map<String, Object> video = new HashMap<String, Object>();
		video.put("rights", Collections.<String>emptyList());
		video.put("id", "eae451da-f173-4db9-9f32-c36f810a512d");
		assertFalse(Tf1PlusRights.hasDownloadableRights(video, true));
		assertFalse(Tf1PlusRights.shouldUsePremiumReplay(video, true));
	}

	@Test
	public void shouldResolvePremiumDeliveryIdFromGraphqlUuid() {
		final Map<String, Object> video = new HashMap<String, Object>();
		video.put("id", "fa698bd7-1328-467c-8cb3-4167b119973f");
		assertEquals("fa698bd7-1328-467c-8cb3-4167b119973f",
				Tf1PlusGraphqlClient.resolvePremiumDeliveryId(video));
		assertEquals("", Tf1PlusGraphqlClient.resolveMediaStreamId(video));
	}

	private static Map<String, Object> videoWithRights(final boolean authEnabled, final String... rights) {
		final Map<String, Object> video = new HashMap<String, Object>();
		video.put("rights", Arrays.asList(rights));
		video.put("authEnabled", Boolean.valueOf(authEnabled));
		if (rights.length == 1 && "MAX".equals(rights[0])) {
			video.put("id", "e6c3e088-0545-480b-b331-e6a9a2ddeb3a");
		} else {
			video.put("id", "e72e51c8-af61-4278-b04e-34b1c8302b44");
		}
		return video;
	}
}
