package com.dabi.habitv.provider.novo19;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.framework.FrameworkConf;

final class Novo19HttpClient {

	interface Transport {
		String get(String url, String authorizationHeader) throws IOException;

		String postJson(String url, String jsonBody) throws IOException;
	}

	private Novo19HttpClient() {
	}

	static Transport pluginTransport(final Proxy proxy) {
		return new PluginTransport(proxy);
	}

	private static final class PluginTransport implements Transport {

		private final Proxy proxy;

		private PluginTransport(final Proxy proxy) {
			this.proxy = proxy;
		}

		@Override
		public String get(final String url, final String authorizationHeader) throws IOException {
			final HttpURLConnection connection = openConnection(url, "GET");
			if (!StringUtils.isEmpty(authorizationHeader)) {
				connection.setRequestProperty("Authorization", authorizationHeader);
			}
			connection.setRequestProperty("Accept", "application/json");
			return readResponse(connection);
		}

		@Override
		public String postJson(final String url, final String jsonBody) throws IOException {
			final HttpURLConnection connection = openConnection(url, "POST");
			connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
			connection.setRequestProperty("Accept", "application/json");
			connection.setDoOutput(true);
			final byte[] payload = jsonBody.getBytes("UTF-8");
			connection.setFixedLengthStreamingMode(payload.length);
			final OutputStream output = connection.getOutputStream();
			try {
				output.write(payload);
			} finally {
				output.close();
			}
			return readResponse(connection);
		}

		private HttpURLConnection openConnection(final String url, final String method) throws IOException {
			final HttpURLConnection connection;
			if (proxy != null) {
				connection = (HttpURLConnection) new URL(url).openConnection(proxy);
			} else {
				connection = (HttpURLConnection) new URL(url).openConnection();
			}
			connection.setConnectTimeout(FrameworkConf.TIME_OUT_MS);
			connection.setReadTimeout(FrameworkConf.TIME_OUT_MS);
			connection.setRequestMethod(method);
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; Habitv/novo19)");
			return connection;
		}

		private static String readResponse(final HttpURLConnection connection) throws IOException {
			final int status = connection.getResponseCode();
			final InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
			if (stream == null) {
				throw new Novo19HttpException(status);
			}
			try {
				final String body = readUtf8(stream);
				if (status >= 400) {
					throw new Novo19HttpException(status);
				}
				return body;
			} finally {
				stream.close();
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
