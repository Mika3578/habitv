package com.dabi.habitv.provider.tf1plus;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

final class Tf1PlusEpisodeUrl {

	static final String FRAGMENT_PREFIX = "#habitvTf1=";
	static final String FRAGMENT_MODE_PREMIUM = "premium";
	/** Pre-rename fragment suffix; accepted when parsing catalog URLs only. */
	private static final String LEGACY_FRAGMENT_SUFFIX = "protected";
	private static final String PUBLIC_HOST = "tf1.fr";

	private Tf1PlusEpisodeUrl() {
	}

	static boolean isApprovedPublicDownloadUrl(final String downloadInput) {
		if (StringUtils.isEmpty(downloadInput)) {
			return false;
		}
		try {
			final String host = new URL(pageUrlWithoutFragment(downloadInput)).getHost();
			if (StringUtils.isEmpty(host)) {
				return false;
			}
			final String normalized = host.toLowerCase(Locale.ROOT);
			return PUBLIC_HOST.equals(normalized) || normalized.endsWith("." + PUBLIC_HOST);
		} catch (final MalformedURLException e) {
			return false;
		}
	}

	static String withPremiumStreamId(final String pageUrl, final String streamId) {
		if (StringUtils.isEmpty(pageUrl) || StringUtils.isEmpty(streamId)) {
			return pageUrl;
		}
		final int fragmentIndex = pageUrl.indexOf('#');
		final String base = fragmentIndex >= 0 ? pageUrl.substring(0, fragmentIndex) : pageUrl;
		return base + FRAGMENT_PREFIX + streamId + "," + FRAGMENT_MODE_PREMIUM;
	}

	static boolean requiresPremiumDownload(final String episodeUrl) {
		return parsePremiumStreamId(episodeUrl) != null;
	}

	static String parsePremiumStreamId(final String episodeUrl) {
		if (StringUtils.isEmpty(episodeUrl)) {
			return null;
		}
		final int markerIndex = episodeUrl.indexOf(FRAGMENT_PREFIX);
		if (markerIndex < 0) {
			return null;
		}
		final String payload = episodeUrl.substring(markerIndex + FRAGMENT_PREFIX.length());
		final int endIndex = payload.indexOf(',');
		final String streamId = endIndex >= 0 ? payload.substring(0, endIndex) : payload;
		if (StringUtils.isEmpty(streamId)) {
			return null;
		}
		if (endIndex >= 0) {
			final String mode = payload.substring(endIndex + 1);
			if (!FRAGMENT_MODE_PREMIUM.equals(mode) && !LEGACY_FRAGMENT_SUFFIX.equals(mode)) {
				return null;
			}
		}
		return streamId;
	}

	static String pageUrlWithoutFragment(final String episodeUrl) {
		if (StringUtils.isEmpty(episodeUrl)) {
			return episodeUrl;
		}
		final int fragmentIndex = episodeUrl.indexOf('#');
		return fragmentIndex >= 0 ? episodeUrl.substring(0, fragmentIndex) : episodeUrl;
	}
}
