package com.dabi.habitv.provider.lemanbleu;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LemanBleuDiagnosticsTest {

	@Test
	public void formatLogLineIncludesStrategyAndOmitsQuery() {
		final LemanBleuDiagnostics diagnostics = new LemanBleuDiagnostics("download");
		diagnostics.setShowId("56242");
		diagnostics.setSourceUrl("https://videos.lemanbleu.ch/fr/Emissions/1-x.html?token=secret");
		diagnostics.setCreatedItems(0);
		diagnostics.setRootCauseSummary("mp4-not-found");
		final String line = diagnostics.formatLogLine();
		assertTrue(line.contains("provider=LEMANBLEU"));
		assertTrue(line.contains("strategy=html-infomaniak-curl"));
		assertTrue(line.contains("showId=56242"));
		assertTrue(line.contains("cookiesEnabled=false"));
		assertTrue(line.contains("rootCause=mp4-not-found"));
		assertTrue(!line.contains("token=secret"));
		assertTrue(!line.contains("?token="));
	}
}
