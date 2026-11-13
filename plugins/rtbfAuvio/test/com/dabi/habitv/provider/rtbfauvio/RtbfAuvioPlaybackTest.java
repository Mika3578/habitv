package com.dabi.habitv.provider.rtbfauvio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class RtbfAuvioPlaybackTest {

	@Test
	public void selectsPublicHlsLocator() throws Exception {
		final String url = RtbfAuvioPlaybackParser.selectReplayStreamUrl(
				RtbfAuvioFixtureSupport.playbackScenario("publicHls"), "fixture");
		assertTrue(url.endsWith("replay.m3u8"));
	}

	@Test
	public void rejectsRestrictedOnlyPayload() throws Exception {
		assertTrue(RtbfAuvioPlaybackParser.hasOnlyUnusableFormats(
				RtbfAuvioFixtureSupport.playbackScenario("restrictedOnly"), "fixture"));
		assertNull(RtbfAuvioPlaybackParser.selectReplayStreamUrl(
				RtbfAuvioFixtureSupport.playbackScenario("restrictedOnly"), "fixture"));
	}

	@Test
	public void parsesAnonymousSessionToken() throws Exception {
		assertEquals("offline-session-token", RtbfAuvioPlaybackParser.parseSessionToken(
				RtbfAuvioFixtureSupport.playbackScenario("authAnonymous"), "fixture"));
	}

	@Test
	public void returnsNullWhenNoUsableFormat() throws Exception {
		final Map<String, String> transport = new HashMap<String, String>();
		transport.put(RtbfAuvioUrlBuilder.exposureAnonymousAuthUrl(),
				RtbfAuvioFixtureSupport.playbackScenario("authAnonymous"));
		transport.put(RtbfAuvioUrlBuilder.exposurePlayUrl("3518500_6BA97Bb"),
				RtbfAuvioFixtureSupport.playbackScenario("restrictedOnly"));
		final RtbfAuvioPlaybackClient client = new RtbfAuvioPlaybackClient(new MapTransport(transport));
		final RtbfAuvioDiagnostics diagnostics = new RtbfAuvioDiagnostics("playback");
		assertNull(client.resolveReplayStreamUrl("3518500_6BA97Bb", diagnostics));
		assertEquals("no-usable-format", diagnostics.getRootCauseSummary());
	}

	private static final class MapTransport implements RtbfAuvioHttpClient.Transport {

		private final Map<String, String> responses;

		private MapTransport(final Map<String, String> responses) {
			this.responses = responses;
		}

		@Override
		public String get(final String url, final String authorizationHeader) {
			final String body = responses.get(url);
			if (body == null) {
				throw new RuntimeException("unexpected url " + url);
			}
			return body;
		}

		@Override
		public String postJson(final String url, final String jsonBody) {
			final String body = responses.get(url);
			if (body == null) {
				throw new RuntimeException("unexpected url " + url);
			}
			return body;
		}
	}
}
