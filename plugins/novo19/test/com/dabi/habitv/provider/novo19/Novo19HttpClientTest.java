package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.Test;

public class Novo19HttpClientTest {

	@Test
	public void classifiesCommonStatuses() {
		assertEquals("http-401", Novo19HttpStatus.summarizeFailure(401));
		assertEquals("http-403", Novo19HttpStatus.summarizeFailure(403));
		assertEquals("http-404", Novo19HttpStatus.summarizeFailure(404));
		assertEquals("http-429", Novo19HttpStatus.summarizeFailure(429));
		assertEquals("http-5xx", Novo19HttpStatus.summarizeFailure(503));
		assertEquals("http-error", Novo19HttpStatus.summarizeFailure(418));
	}

	@Test
	public void readUtf8PreservesMultibyteCharactersAcrossBufferBoundaries() throws IOException {
		final String expected = "Thématique";
		final byte[] bytes = expected.getBytes(Charset.forName("UTF-8"));
		final ByteArrayInputStream input = new ChunkedInputStream(bytes, 3);
		assertEquals(expected, Novo19HttpClient.readUtf8(input));
	}

	@Test
	public void retriesTransientHttp503ThenSucceeds() throws Exception {
		final CountingAttempt attempt = new CountingAttempt(1, "{\"ok\":true}");
		final String body = Novo19HttpClient.getWithRetry(attempt, "https://novo19-bff.ouest-france.fr/config", null);
		assertEquals("{\"ok\":true}", body);
		assertEquals(2, attempt.calls);
	}

	@Test
	public void exhaustsRetryOnPersistent503() {
		final CountingAttempt attempt = new CountingAttempt(Novo19Conf.BFF_GET_MAX_ATTEMPTS, null);
		try {
			Novo19HttpClient.getWithRetry(attempt, "https://novo19-bff.ouest-france.fr/config", null);
			throw new AssertionError("expected Novo19HttpException");
		} catch (final Novo19HttpException e) {
			assertEquals(503, e.getStatus());
			assertEquals(Novo19Conf.BFF_GET_MAX_ATTEMPTS, attempt.calls);
		} catch (final IOException e) {
			throw new AssertionError(e);
		}
	}

	private static final class ChunkedInputStream extends ByteArrayInputStream {

		private final int chunkSize;

		private ChunkedInputStream(final byte[] bytes, final int chunkSize) {
			super(bytes);
			this.chunkSize = chunkSize;
		}

		@Override
		public synchronized int read(final byte[] buffer, final int offset, final int length) {
			return super.read(buffer, offset, Math.min(length, chunkSize));
		}

	}

	private static final class CountingAttempt implements Novo19HttpClient.GetAttempt {

		private final int failUntilCall;

		private final String successBody;

		private int calls;

		private CountingAttempt(final int failUntilCall, final String successBody) {
			this.failUntilCall = failUntilCall;
			this.successBody = successBody;
		}

		@Override
		public String execute(final String url, final String authorizationHeader) throws IOException {
			calls++;
			if (calls <= failUntilCall) {
				throw new Novo19HttpException(503);
			}
			return successBody;
		}

	}

}
