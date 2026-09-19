package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Tf1PlusDiagnosticsTest {

	@Test
	public void formatLogLineIncludesStrategyAndOmitsSecrets() {
		final Tf1PlusDiagnostics diagnostics = new Tf1PlusDiagnostics("catalogue");
		diagnostics.setChannel("tmc");
		diagnostics.setSourceUrl("https://www.tf1.fr/graphql/web?id=483ce0f&token=secret-value");
		diagnostics.setCreatedItems(3);
		diagnostics.setRootCauseSummary("ok");

		final String line = diagnostics.formatLogLine();
		assertTrue(line.contains("provider=TF1PLUS"));
		assertTrue(line.contains("strategy=graphql-ytdlp"));
		assertTrue(line.contains("channel=tmc"));
		assertTrue(line.contains("cookiesEnabled=false"));
		assertTrue(line.contains("createdItems=3"));
		assertEquals(-1, line.indexOf("secret-value"));
		assertEquals(-1, line.indexOf("?"));
	}
}
