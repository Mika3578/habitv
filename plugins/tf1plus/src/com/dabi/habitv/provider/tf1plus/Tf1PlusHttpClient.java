package com.dabi.habitv.provider.tf1plus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.framework.FrameworkConf;

final class Tf1PlusHttpClient {

	interface Transport {
		String get(String url) throws IOException;
	}

	interface ProxySource {
		Proxy current();
	}

	private Tf1PlusHttpClient() {
	}

	static Transport pluginTransport(final ProxySource proxySource) {
		return new PluginTransport(proxySource);
	}

	static Proxy proxyForRequest(final ProxySource proxySource) {
		return proxySource == null ? null : proxySource.current();
	}

	private static final class PluginTransport implements Transport {

		private final ProxySource proxySource;

		private PluginTransport(final ProxySource proxySource) {
			this.proxySource = proxySource;
		}

		@Override
		public String get(final String url) throws IOException {
			final Proxy proxy = proxyForRequest(proxySource);
			final HttpURLConnection connection;
			if (proxy != null) {
				connection = (HttpURLConnection) new URL(url).openConnection(proxy);
			} else {
				connection = (HttpURLConnection) new URL(url).openConnection();
			}
			try {
				connection.setConnectTimeout(FrameworkConf.TIME_OUT_MS);
				connection.setReadTimeout(FrameworkConf.TIME_OUT_MS);
				connection.setRequestMethod("GET");
				connection.setRequestProperty("User-Agent", Tf1PlusConf.USER_AGENT);
				connection.setRequestProperty("Accept", "application/json");
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setRequestProperty("Referer", Tf1PlusConf.REFERER);
				final int status = connection.getResponseCode();
				final InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
				if (stream == null) {
					throw new IOException("http-" + status);
				}
				try {
					final String body = readUtf8(stream);
					if (status >= 400) {
						throw new IOException("http-" + status);
					}
					if (StringUtils.isEmpty(body)) {
						throw new IOException("empty-body");
					}
					return body;
				} finally {
					stream.close();
				}
			} finally {
				connection.disconnect();
			}
		}
	}

	static String readUtf8(final InputStream input) throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final byte[] buffer = new byte[256];
		int read;
		while ((read = input.read(buffer)) != -1) {
			output.write(buffer, 0, read);
		}
		return output.toString("UTF-8");
	}

}
