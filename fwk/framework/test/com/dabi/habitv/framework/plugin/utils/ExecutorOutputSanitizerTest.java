package com.dabi.habitv.framework.plugin.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.ExecutorOutputSanitizer;

public class ExecutorOutputSanitizerTest {

	@Test
	public void sanitizeCommandRedactsApiKeyInUrl() {
		final String command = "yt-dlp \"https://example.test/v?id=1&key=AIzaSySecretKeyValue\"";
		final String sanitized = ExecutorOutputSanitizer.sanitizeCommand(command);
		assertTrue(sanitized.contains("key=***"));
		assertFalse(sanitized.contains("AIzaSySecretKeyValue"));
	}

	@Test
	public void sanitizeCommandRedactsCookiesPath() {
		final String command = "yt-dlp --cookies C:\\Users\\secret\\cookies.txt \"https://example.test/v\"";
		final String sanitized = ExecutorOutputSanitizer.sanitizeCommand(command);
		assertTrue(sanitized.contains("--cookies ***"));
		assertFalse(sanitized.contains("cookies.txt"));
	}

	@Test
	public void sanitizeOutputRedactsEmbeddedApiKeyAndTruncates() {
		final StringBuilder output = new StringBuilder("ERROR AIzaSySecretKeyValue ");
		for (int i = 0; i < 400; i++) {
			output.append('x');
		}
		final String sanitized = ExecutorOutputSanitizer.sanitizeOutput(output.toString());
		assertFalse(sanitized.contains("AIzaSySecretKeyValue"));
		assertTrue(sanitized.contains("AIza***"));
		assertTrue(sanitized.endsWith("..."));
		assertTrue(sanitized.length() <= 303);
	}
}
