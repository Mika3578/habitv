package com.dabi.habitv.provider.tf1plus;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Interprets TF1+ GraphQL {@code rights} and {@code authEnabled} for catalogue visibility
 * and download routing. Live API uses {@code MAX} (TF1+ MAX) where older payloads used
 * {@code PREMIUM}.
 */
final class Tf1PlusRights {

	private Tf1PlusRights() {
	}

	static boolean hasDownloadableRights(final Map<String, Object> video, final boolean premiumDownloadEnabled) {
		if (isYtDlpEligible(video)) {
			return true;
		}
		if (hasBasicRights(video)) {
			return true;
		}
		if (!premiumDownloadEnabled) {
			return false;
		}
		return isSubscriptionOnly(video) || hasMaxRights(video) || hasPremiumRights(video)
				|| StringUtils.isNotEmpty(Tf1PlusGraphqlClient.resolveMediaStreamId(video));
	}

	static boolean shouldUsePremiumReplay(final Map<String, Object> video, final boolean premiumDownloadEnabled) {
		if (!premiumDownloadEnabled || isYtDlpEligible(video)) {
			return false;
		}
		return hasPremiumRights(video) || hasMaxRights(video) || isAuthEnabled(video)
				|| StringUtils.isNotEmpty(Tf1PlusGraphqlClient.resolveMediaStreamId(video));
	}

	static boolean isYtDlpEligible(final Map<String, Object> video) {
		return hasBasicRights(video) && !hasMaxRights(video) && !hasPremiumRights(video) && !isAuthEnabled(video);
	}

	static boolean isSubscriptionOnly(final Map<String, Object> video) {
		return !hasBasicRights(video) && (hasMaxRights(video) || hasPremiumRights(video));
	}

	static boolean hasBasicRights(final Map<String, Object> video) {
		return rightsSet(video).contains(Tf1PlusConf.RIGHT_BASIC);
	}

	static boolean hasPremiumRights(final Map<String, Object> video) {
		return rightsSet(video).contains(Tf1PlusConf.RIGHT_PREMIUM);
	}

	static boolean hasMaxRights(final Map<String, Object> video) {
		return rightsSet(video).contains(Tf1PlusConf.RIGHT_MAX);
	}

	static boolean isAuthEnabled(final Map<String, Object> video) {
		final Object authEnabled = video.get("authEnabled");
		return authEnabled instanceof Boolean && Boolean.TRUE.equals(authEnabled);
	}

	private static Set<String> rightsSet(final Map<String, Object> video) {
		if (video == null) {
			return Collections.emptySet();
		}
		final Object rights = video.get("rights");
		if (!(rights instanceof List)) {
			return Collections.emptySet();
		}
		final Set<String> normalized = new HashSet<String>();
		for (final Object entry : (List<?>) rights) {
			if (entry != null) {
				normalized.add(String.valueOf(entry).trim().toUpperCase());
			}
		}
		return normalized;
	}

}
