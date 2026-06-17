package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

	@Test
	public void reportsEmptyPlaybackSeparatelyFromDrm() throws Exception {
		final Map<String, String> transport = new HashMap<String, String>();
		transport.put(Novo19UrlBuilder.redBeeAnonymousAuthUrl(),
				Novo19FixtureSupport.readFixture("redbee-auth-anonymous.json"));
		transport.put(Novo19UrlBuilder.redBeePlayUrl("empty-asset_565BFFb"),
				Novo19FixtureSupport.readFixture("redbee-play-empty-formats.json"));
		final Novo19PlaybackClient client = new Novo19PlaybackClient(new MapTransport(transport));
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("playback");
		assertNull(client.resolveReplayStreamUrl("empty-asset_565BFFb", diagnostics));
		assertEquals("empty-playback", diagnostics.getRootCauseSummary());
	}

	@Test
	public void refreshesSessionAfter401AndRetriesOnce() throws Exception {
		final Map<String, String> transport = new HashMap<String, String>();
		transport.put(Novo19UrlBuilder.redBeeAnonymousAuthUrl(),
				Novo19FixtureSupport.readFixture("redbee-auth-anonymous.json"));
		transport.put(Novo19UrlBuilder.redBeePlayUrl("news-episode_565BFFb"),
				Novo19FixtureSupport.readFixture("redbee-play-replay-hls.json"));
		final Novo19PlaybackClient client = new Novo19PlaybackClient(
				new UnauthorizedOnceTransport(transport, Novo19UrlBuilder.redBeePlayUrl("news-episode_565BFFb")));
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("playback");
		assertTrue(client.resolveReplayStreamUrl("news-episode_565BFFb", diagnostics).endsWith("replay.m3u8"));
		assertEquals("ok", diagnostics.getRootCauseSummary());
	}

	@Test
	public void repeated401FailsAfterSingleRetry() throws Exception {
		final Map<String, String> transport = new HashMap<String, String>();
		transport.put(Novo19UrlBuilder.redBeeAnonymousAuthUrl(),
				Novo19FixtureSupport.readFixture("redbee-auth-anonymous.json"));
		final Novo19PlaybackClient client = new Novo19PlaybackClient(new AlwaysUnauthorizedTransport());
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("playback");
		try {
			client.resolveReplayStreamUrl("news-episode_565BFFb", diagnostics);
			throw new AssertionError("expected Novo19HttpException");
		} catch (final Novo19HttpException e) {
			assertEquals(401, e.getStatus());
		}
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

	private static final class UnauthorizedOnceTransport implements Novo19HttpClient.Transport {

		private final Map<String, String> responses;

		private final String playUrl;

		private boolean unauthorizedConsumed;

		private UnauthorizedOnceTransport(final Map<String, String> responses, final String playUrl) {
			this.responses = responses;
			this.playUrl = playUrl;
		}

		@Override
		public String get(final String url, final String authorizationHeader) throws java.io.IOException {
			if (playUrl.equals(url) && !unauthorizedConsumed) {
				unauthorizedConsumed = true;
				throw new Novo19HttpException(401);
			}
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

	private static final class AlwaysUnauthorizedTransport implements Novo19HttpClient.Transport {

		@Override
		public String get(final String url, final String authorizationHeader) throws java.io.IOException {
			if (url.contains("/entitlement/")) {
				throw new Novo19HttpException(401);
			}
			return Novo19FixtureSupport.readFixture("redbee-auth-anonymous.json");
		}

		@Override
		public String postJson(final String url, final String jsonBody) throws java.io.IOException {
			return Novo19FixtureSupport.readFixture("redbee-auth-anonymous.json");
		}

	}

}
