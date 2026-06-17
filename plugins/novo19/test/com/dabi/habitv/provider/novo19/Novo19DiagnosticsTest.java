package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Novo19DiagnosticsTest {

	@Test
	public void logLineIsSanitizedAndStructured() {
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("catalogue");
		diagnostics.setSourcePath("/categories");
		diagnostics.setAssetId("asset-123");
		diagnostics.setCreatedItems(3);
		final String line = diagnostics.formatLogLine();
		assertTrue(line.contains("provider=NOVO19"));
		assertTrue(line.contains("operation=catalogue"));
		assertTrue(line.contains("assetId=asset-123"));
		assertTrue(line.contains("cookiesEnabled=false"));
		assertFalse(line.contains("Bearer"));
	}

	@Test
	public void stripsQueryParametersFromSourcePath() {
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("playback");
		diagnostics.setSourcePath("/entitlement/play?token=secret-value");
		final String line = diagnostics.formatLogLine();
		assertTrue(line.contains("sourcePath=/entitlement/play"));
		assertFalse(line.contains("secret-value"));
		assertFalse(line.contains("token="));
	}

}
