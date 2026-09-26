package com.dabi.habitv.provider.arte;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;

/**
 * Host and scheme checks for catalogue metadata URLs (EMAC API and public site).
 */
final class ArteRequestUrls {

	private static final String EMAC_HOST = "api.arte.tv";

	private static final String EMAC_PATH_PREFIX = "/api/emac/v4/";

	private static final String SITE_HOST = "www.arte.tv";

	private ArteRequestUrls() {
	}

	static boolean isTrustedEmacApiUrl(final String url) {
		final URI uri = parseHttpsUri(url);
		if (uri == null) {
			return false;
		}
		if (!EMAC_HOST.equalsIgnoreCase(uri.getHost())) {
			return false;
		}
		final String path = uri.getPath();
		return path != null && path.startsWith(EMAC_PATH_PREFIX);
	}

	static boolean isTrustedPublicSiteUrl(final String url) {
		final URI uri = parseHttpsUri(url);
		return uri != null && SITE_HOST.equalsIgnoreCase(uri.getHost());
	}

	static boolean isTrustedCatalogueFetchUrl(final String url) {
		return isTrustedEmacApiUrl(url) || isTrustedPublicSiteUrl(url);
	}

	private static URI parseHttpsUri(final String url) {
		if (StringUtils.isEmpty(url)) {
			return null;
		}
		try {
			final URI uri = new URI(url);
			if (!"https".equalsIgnoreCase(uri.getScheme())) {
				return null;
			}
			if (StringUtils.isEmpty(uri.getHost())) {
				return null;
			}
			final int port = uri.getPort();
			if (port != -1 && port != 443) {
				return null;
			}
			return uri;
		} catch (final URISyntaxException e) {
			return null;
		}
	}
}
