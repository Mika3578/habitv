package com.dabi.habitv.provider.tf1plus;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;

final class Tf1PlusUrls {

	private Tf1PlusUrls() {
	}

	static String graphqlUrl(final String queryId, final String variablesJson) {
		return Tf1PlusConf.GRAPHQL_URL + "?id=" + queryId + "&variables=" + encodeQuery(variablesJson);
	}

	static String videoPageUrl(final String channelSlug, final String programSlug, final String videoSlug) {
		return Tf1PlusConf.HOME_URL + "/" + channelSlug + "/" + programSlug + "/videos/" + videoSlug + ".html";
	}

	static String programPageUrl(final String channelSlug, final String programSlug) {
		return Tf1PlusConf.HOME_URL + "/" + channelSlug + "/" + programSlug;
	}

	static boolean isApprovedPublicDownloadUrl(final String downloadInput) {
		if (StringUtils.isEmpty(downloadInput)) {
			return false;
		}
		try {
			final URL url = new URL(downloadInput);
			if (!isTf1Host(url.getHost())) {
				return false;
			}
			final String path = url.getPath() == null ? "" : url.getPath();
			return path.contains("/videos/") && path.endsWith(".html");
		} catch (final MalformedURLException e) {
			return false;
		}
	}

	static boolean isTf1Host(final String host) {
		if (StringUtils.isEmpty(host)) {
			return false;
		}
		return Tf1PlusConf.PUBLIC_HOST.equalsIgnoreCase(host) || Tf1PlusConf.PUBLIC_HOST_BARE.equalsIgnoreCase(host);
	}

	private static String encodeQuery(final String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new IllegalStateException("utf-8", e);
		}
	}

}
