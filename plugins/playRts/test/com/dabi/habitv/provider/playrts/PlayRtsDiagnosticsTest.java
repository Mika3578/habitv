package com.dabi.habitv.provider.playrts;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PlayRtsDiagnosticsTest {

	@Test
	public void formatLogLineIncludesStrategyAndOmitsSecrets() {
		final PlayRtsDiagnostics diagnostics = new PlayRtsDiagnostics("download");
		diagnostics.setShowId("5917099");
		diagnostics.setSourceUrl("https://www.rts.ch/play/tv/-/video/x?urn=urn:rts:video:abc&token=secret");
		diagnostics.setCreatedItems(0);
		diagnostics.setRootCauseSummary("geo-restricted");
		final String line = diagnostics.formatLogLine();
		assertTrue(line.contains("provider=PLAYRTS"));
		assertTrue(line.contains("strategy=play-v3-api-ytdlp"));
		assertTrue(line.contains("operation=download"));
		assertTrue(line.contains("showId=5917099"));
		assertTrue(line.contains("cookiesEnabled=false"));
		assertTrue(line.contains("rootCause=geo-restricted"));
		assertTrue(!line.contains("token=secret"));
		assertTrue(!line.contains("?urn="));
	}
}
