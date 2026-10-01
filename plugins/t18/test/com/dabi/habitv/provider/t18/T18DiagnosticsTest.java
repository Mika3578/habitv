package com.dabi.habitv.provider.t18;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class T18DiagnosticsTest {

	@Test
	public void formatLogLineIncludesStrategy() {
		final T18Diagnostics diagnostics = new T18Diagnostics("download");
		diagnostics.setSourceUrl("https://t18.fr/prog/x/y?token=secret");
		diagnostics.setRootCauseSummary("private-dailymotion-unsupported");
		final String line = diagnostics.formatLogLine();
		assertTrue(line.contains("provider=T18"));
		assertTrue(line.contains("strategy=html-catalog-private-dailymotion"));
		assertTrue(line.contains("cookiesEnabled=false"));
		assertEquals(-1, line.indexOf("secret"));
	}
}
