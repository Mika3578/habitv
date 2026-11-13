package com.dabi.habitv.provider.rtbfauvio;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

final class RtbfAuvioUrlBuilder {

	private RtbfAuvioUrlBuilder() {
	}

	static String exposureAnonymousAuthUrl() {
		return RtbfAuvioConf.EXPOSURE_BASE_URL + RtbfAuvioConf.ANONYMOUS_AUTH_PATH;
	}

	static String exposureAccountAuthUrl() {
		return RtbfAuvioConf.EXPOSURE_BASE_URL + RtbfAuvioConf.ACCOUNT_AUTH_PATH;
	}

	static String exposurePlayUrl(final String assetId) {
		return RtbfAuvioConf.EXPOSURE_BASE_URL + RtbfAuvioConf.PLAY_PATH_PREFIX + assetId
				+ RtbfAuvioConf.PLAY_PATH_SUFFIX;
	}

	static String formatEntitlementAssetId(final String numericMediaId) {
		if (StringUtils.isEmpty(numericMediaId)) {
			return null;
		}
		if (numericMediaId.endsWith(RtbfAuvioConf.ASSET_ID_SUFFIX)) {
			return numericMediaId;
		}
		return numericMediaId + RtbfAuvioConf.ASSET_ID_SUFFIX;
	}

	static boolean isApprovedPublicDownloadUrl(final String downloadInput) {
		if (StringUtils.isEmpty(downloadInput)) {
			return false;
		}
		try {
			return RtbfAuvioConf.PUBLIC_HOST.equalsIgnoreCase(hostFromUrl(downloadInput));
		} catch (final IOException e) {
			return false;
		}
	}

	static String mediaUrl(final String path) {
		if (StringUtils.isEmpty(path)) {
			return null;
		}
		if (path.startsWith("http://") || path.startsWith("https://")) {
			return path;
		}
		if (path.startsWith("/")) {
			return RtbfAuvioConf.HOME_URL + path;
		}
		return RtbfAuvioConf.HOME_URL + "/" + path;
	}

	private static String hostFromUrl(final String url) throws IOException {
		try {
			return new URL(url).getHost();
		} catch (final MalformedURLException e) {
			throw new IOException("malformed-url:" + url, e);
		}
	}

}
