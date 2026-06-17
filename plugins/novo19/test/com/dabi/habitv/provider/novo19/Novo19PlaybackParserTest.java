package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Novo19PlaybackParserTest {

	@Test
	public void selectsPublicHlsLocator() throws Exception {
		final String url = Novo19PlaybackParser.selectReplayStreamUrl(
				Novo19FixtureSupport.readFixture("redbee-play-replay-hls.json"), "fixture");
		assertTrue(url.endsWith("replay.m3u8"));
	}

	@Test
	public void fallsBackToDashWhenHlsMissing() throws Exception {
		final String url = Novo19PlaybackParser.selectReplayStreamUrl(
				Novo19FixtureSupport.readFixture("redbee-play-replay-dash-only.json"), "fixture");
		assertTrue(url.endsWith("replay.mpd"));
	}

	@Test
	public void rejectsDrmOnlyPayload() throws Exception {
		assertTrue(Novo19PlaybackParser.hasOnlyProtectedFormats(
				Novo19FixtureSupport.readFixture("redbee-play-live-drm.json"), "fixture"));
	}

	@Test
	public void parsesAnonymousSessionToken() throws Exception {
		assertEquals("offline-session-token", Novo19PlaybackParser.parseSessionToken(
				Novo19FixtureSupport.readFixture("redbee-auth-anonymous.json"), "fixture"));
	}

	@Test
	public void handlesEmptyPayloadSafely() {
		assertTrue(Novo19PlaybackParser.hasOnlyProtectedFormats("", "fixture"));
		assertNull(Novo19PlaybackParser.selectReplayStreamUrl("", "fixture"));
	}

	@Test
	public void handlesMalformedPayloadSafely() {
		try {
			Novo19PlaybackParser.selectReplayStreamUrl("{not-json", "fixture");
		} catch (final RuntimeException e) {
			assertTrue(e.getMessage().contains("Cannot parse NOVO19 playback response"));
		}
	}

}
