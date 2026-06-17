package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

public class Novo19HttpClientRetryTest {

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
