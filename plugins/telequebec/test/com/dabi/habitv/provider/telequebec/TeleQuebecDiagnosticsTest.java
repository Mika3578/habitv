package com.dabi.habitv.provider.telequebec;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TeleQuebecDiagnosticsTest {

	@Test
	public void formatLogLineIncludesStrategy() {
		final TeleQuebecDiagnostics diagnostics = new TeleQuebecDiagnostics("download");
		diagnostics.setShowSlug("penelope-partout");
		diagnostics.setSourceUrl("https://www.telequebec.tv/regarder/penelope-partout/1/1?token=secret");
		diagnostics.setRootCauseSummary("geo-or-rights-unavailable");
		final String line = diagnostics.formatLogLine();
		assertTrue(line.contains("provider=TELEQUEBEC"));
		assertTrue(line.contains("strategy=graphql-catalog-geo-playback"));
		assertTrue(line.contains("showSlug=penelope-partout"));
		assertTrue(line.contains("cookiesEnabled=false"));
		assertTrue(!line.contains("token=secret"));
	}
}
