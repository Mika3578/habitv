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

	interface GetAttempt {
		String execute(String url, String authorizationHeader) throws IOException;
	}

	private Novo19HttpClient() {
	}

	static Transport pluginTransport(final Proxy proxy) {
		return new PluginTransport(proxy);
	}

	static boolean isRetriableStatus(final int status) {
		return status == 429 || status == 502 || status == 503 || status == 504;
	}

	static String getWithRetry(final GetAttempt attempt, final String url, final String authorizationHeader)
			throws IOException {
		IOException lastIo = null;
		Novo19HttpException lastHttp = null;
		for (int attemptIndex = 1; attemptIndex <= Novo19Conf.BFF_GET_MAX_ATTEMPTS; attemptIndex++) {
			try {
				return attempt.execute(url, authorizationHeader);
			} catch (final Novo19HttpException e) {
				lastHttp = e;
				if (!isRetriableStatus(e.getStatus()) || attemptIndex >= Novo19Conf.BFF_GET_MAX_ATTEMPTS) {
					throw e;
				}
			} catch (final IOException e) {
				lastIo = e;
				if (attemptIndex >= Novo19Conf.BFF_GET_MAX_ATTEMPTS) {
					throw e;
				}
			}
			sleepBeforeRetry();
		}
		if (lastHttp != null) {
			throw lastHttp;
		}
		if (lastIo != null) {
			throw lastIo;
		}
		throw new IOException("retry-exhausted:" + url);
	}

	private static void sleepBeforeRetry() throws IOException {
		if (Novo19Conf.BFF_GET_RETRY_DELAY_MS <= 0L) {
			return;
		}
		try {
			Thread.sleep(Novo19Conf.BFF_GET_RETRY_DELAY_MS);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("retry-interrupted", e);
		}
	}

	private static final class PluginTransport implements Transport {

		private final Proxy proxy;

		private PluginTransport(final Proxy proxy) {
			this.proxy = proxy;
		}

		@Override
		public String get(final String url, final String authorizationHeader) throws IOException {
			return getWithRetry(new GetAttempt() {
				@Override
				public String execute(final String requestUrl, final String authHeader) throws IOException {
					return executeGet(requestUrl, authHeader);
				}
			}, url, authorizationHeader);
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

		private String executeGet(final String url, final String authorizationHeader) throws IOException {
			final HttpURLConnection connection = openConnection(url, "GET");
			if (!StringUtils.isEmpty(authorizationHeader)) {
				connection.setRequestProperty("Authorization", authorizationHeader);
			}
			connection.setRequestProperty("Accept", "application/json");
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
