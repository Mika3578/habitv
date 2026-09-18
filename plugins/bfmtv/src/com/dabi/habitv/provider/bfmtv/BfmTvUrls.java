package com.dabi.habitv.provider.bfmtv;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

final class BfmTvUrls {

	private BfmTvUrls() {
	}

	static String tokenUrl(final String channelId) {
		return BfmTvConf.API_BASE_URL + "/" + channelId + "-applications/";
	}

	static String replayPageUrl(final String channelId, final String token) {
		return BfmTvConf.API_BASE_URL + "/" + channelId + "-applications/" + token + "/getPage?pagename=replay";
	}

	static String videosUrl(final String channelId, final String token, final String categoryId, final int page) {
		return BfmTvConf.API_BASE_URL + "/" + channelId + "-applications/" + token + "/getVideosList?category="
				+ categoryId + "&count=" + BfmTvConf.VIDEO_PAGE_SIZE + "&page=" + page;
	}

	static boolean isApprovedPublicDownloadUrl(final String downloadInput) {
		if (StringUtils.isEmpty(downloadInput)) {
			return false;
		}
		try {
			final URL url = new URL(downloadInput);
			if (!isBfmHost(url.getHost())) {
				return false;
			}
			final String path = url.getPath() == null ? "" : url.getPath();
			return path.contains("/replay-emissions/") && path.endsWith(".html");
		} catch (final MalformedURLException e) {
			return false;
		}
	}

	static boolean isBfmHost(final String host) {
		if (StringUtils.isEmpty(host)) {
			return false;
		}
		final String normalized = host.toLowerCase(Locale.US);
		return BfmTvConf.PUBLIC_HOST.equals(normalized) || BfmTvConf.PUBLIC_HOST_BARE.equals(normalized)
				|| normalized.endsWith("." + BfmTvConf.PUBLIC_HOST_BARE);
	}

}
