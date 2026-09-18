package com.dabi.habitv.provider.canalplus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.utils.RetrieverUtils;

/**
 * Fetches Canal+ / CStar catalog URLs while keeping redirects on approved hosts.
 */
final class CanalPlusApprovedRetriever {

	private static final String USER_AGENT = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)";

	private static final int MAX_REDIRECTS = 5;

	interface HostAllowlist {
		boolean isAllowed(String url);
	}

	static final HostAllowlist CATALOG_HOSTS = new HostAllowlist() {
		@Override
		public boolean isAllowed(final String url) {
			return CanalPlusContentIdParser.isModernCanalPlusUrl(url)
					|| CanalPlusContentIdParser.isLegacyCanalPlusUrl(url);
		}
	};

	private CanalPlusApprovedRetriever() {
	}

	static InputStream openCatalog(final String url, final Proxy proxy) {
		return open(url, proxy, CATALOG_HOSTS);
	}

	static String readCatalog(final String url, final String encoding, final Proxy proxy) {
		return read(url, encoding, proxy, CATALOG_HOSTS);
	}

	static InputStream open(final String url, final Proxy proxy, final HostAllowlist allowlist) {
		requireAllowed(url, allowlist);
		String currentUrl = url;
		HttpURLConnection connection = null;
		try {
			for (int hop = 0; hop <= MAX_REDIRECTS; hop++) {
				connection = openConnection(currentUrl, proxy);
				connection.setInstanceFollowRedirects(false);
				prepareConnection(connection);
				final int status = connection.getResponseCode();
				if (!isRedirect(status)) {
					return connection.getInputStream();
				}
				final String location = connection.getHeaderField("Location");
				connection.disconnect();
				connection = null;
				currentUrl = resolveRedirectTarget(currentUrl, location);
				requireAllowed(currentUrl, allowlist);
			}
			throw new TechnicalException(new IOException("Too many catalog redirects"));
		} catch (final IOException e) {
			if (connection != null) {
				connection.disconnect();
			}
			throw new TechnicalException(e);
		}
	}

	static String read(final String url, final String encoding, final Proxy proxy, final HostAllowlist allowlist) {
		final InputStream in = open(url, proxy, allowlist);
		final BufferedReader reader;
		try {
			if (encoding != null) {
				reader = new BufferedReader(new InputStreamReader(in, encoding));
			} else {
				reader = new BufferedReader(new InputStreamReader(in, FrameworkConf.UTF8));
			}
		} catch (final UnsupportedEncodingException e) {
			throw new TechnicalException(e);
		}
		final StringBuffer sb = new StringBuffer();
		try {
			String readLine;
			while ((readLine = reader.readLine()) != null) {
				sb.append(readLine + "\r\n");
			}
		} catch (final IOException e) {
			throw new TechnicalException(e);
		} finally {
			try {
				in.close();
			} catch (final IOException e) {
				// ignore close failures after a completed read
			}
		}
		return sb.toString();
	}

	static String resolveRedirectTarget(final String currentUrl, final String location) {
		if (StringUtils.isEmpty(StringUtils.trimToNull(location))) {
			throw new TechnicalException(new IOException("Redirect without Location header"));
		}
		try {
			final URI current = new URI(currentUrl);
			final URI resolved = current.resolve(new URI(location.trim()));
			if (resolved.getHost() == null) {
				throw new TechnicalException(new IOException("Redirect Location is not absolute"));
			}
			return resolved.toString();
		} catch (final URISyntaxException e) {
			throw new TechnicalException(e);
		}
	}

	static void requireAllowed(final String url, final HostAllowlist allowlist) {
		if (allowlist == null || !allowlist.isAllowed(url)) {
			throw new TechnicalException(
					new IOException("Unapproved catalog host: " + CanalPlusContentIdParser.hostOf(url)));
		}
	}

	private static boolean isRedirect(final int status) {
		return status == HttpURLConnection.HTTP_MOVED_TEMP
				|| status == HttpURLConnection.HTTP_MOVED_PERM
				|| status == HttpURLConnection.HTTP_SEE_OTHER
				|| status == 307
				|| status == 308;
	}

	private static HttpURLConnection openConnection(final String url, final Proxy proxy) throws IOException {
		if (RetrieverUtils.useProxy(proxy)) {
			return (HttpURLConnection) new URL(url).openConnection(proxy);
		}
		return (HttpURLConnection) new URL(url).openConnection();
	}

	private static void prepareConnection(final HttpURLConnection connection) {
		connection.setConnectTimeout(FrameworkConf.TIME_OUT_MS);
		connection.setReadTimeout(FrameworkConf.TIME_OUT_MS);
		connection.setRequestProperty("User-Agent", USER_AGENT);
	}

}
