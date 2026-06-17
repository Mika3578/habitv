package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class Novo19PlaybackClientTest {

	@Test
	public void mapsHttp403ToDiagnostics() throws Exception {
		final Map<String, String> transport = new HashMap<String, String>();
		transport.put(Novo19UrlBuilder.redBeeAnonymousAuthUrl(),
				Novo19FixtureSupport.readFixture("redbee-auth-anonymous.json"));
		final Novo19PlaybackClient client = new Novo19PlaybackClient(new StatusTransport(transport, 403));
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("playback");
		try {
			client.resolveReplayStreamUrl("news-episode_565BFFb", diagnostics);
		} catch (final Novo19HttpException e) {
			assertEquals(403, e.getStatus());
		}
		assertEquals("http-403", diagnostics.getRootCauseSummary());
		assertEquals(403, diagnostics.getHttpStatus());
	}

	@Test
	public void returnsNullForDrmOnlyReplay() throws Exception {
		final Map<String, String> transport = new HashMap<String, String>();
		transport.put(Novo19UrlBuilder.redBeeAnonymousAuthUrl(),
				Novo19FixtureSupport.readFixture("redbee-auth-anonymous.json"));
		transport.put(Novo19UrlBuilder.redBeePlayUrl("news-episode_565BFFb"),
				Novo19FixtureSupport.readFixture("redbee-play-live-drm.json"));
		final Novo19PlaybackClient client = new Novo19PlaybackClient(new MapTransport(transport));
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("playback");
		assertNull(client.resolveReplayStreamUrl("news-episode_565BFFb", diagnostics));
		assertEquals("drm-protected", diagnostics.getRootCauseSummary());
	}

	private static final class MapTransport implements Novo19HttpClient.Transport {

		private final Map<String, String> responses;

		private MapTransport(final Map<String, String> responses) {
			this.responses = responses;
		}

		@Override
		public String get(final String url, final String authorizationHeader) throws java.io.IOException {
			if (!responses.containsKey(url)) {
				throw new java.io.IOException("missing " + url);
			}
			return responses.get(url);
		}

		@Override
		public String postJson(final String url, final String jsonBody) throws java.io.IOException {
			if (!responses.containsKey(url)) {
				throw new java.io.IOException("missing " + url);
			}
			return responses.get(url);
		}

	}

	private static final class StatusTransport implements Novo19HttpClient.Transport {

		private final Map<String, String> responses;

		private final int playStatus;

		private StatusTransport(final Map<String, String> responses, final int playStatus) {
			this.responses = responses;
			this.playStatus = playStatus;
		}

		@Override
		public String get(final String url, final String authorizationHeader) throws java.io.IOException {
			if (url.contains("/entitlement/")) {
				throw new Novo19HttpException(playStatus);
			}
			return responses.get(url);
		}

		@Override
		public String postJson(final String url, final String jsonBody) throws java.io.IOException {
			return responses.get(url);
		}

	}

}
