package com.dabi.habitv.framework.plugin.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.ExecutorOutputSanitizer;

public class ExecutorOutputSanitizerTest {

	private static final String SECRET_TOKEN = "super-secret-token-value";
	private static final String SECRET_API_KEY = "AIzaSySecretKeyValue";
	private static final String COOKIE_PATH = "C:\\Users\\Jane Doe\\Browser Profile\\cookies.txt";

	@Test
	public void sanitizeCommandRedactsApiKeyInUrl() {
		final String command = "yt-dlp \"https://example.test/v?id=1&key=" + SECRET_API_KEY + "\"";
		final String sanitized = ExecutorOutputSanitizer.sanitizeCommand(command);
		assertTrue(sanitized.contains("key=***"));
		assertFalse(sanitized.contains(SECRET_API_KEY));
	}

	@Test
	public void sanitizeCommandRedactsUnquotedCookiesPath() {
		final String command = "yt-dlp --cookies C:\\Users\\secret\\cookies.txt \"https://example.test/v\"";
		final String sanitized = ExecutorOutputSanitizer.sanitizeCommand(command);
		assertTrue(sanitized.contains("--cookies ***"));
		assertFalse(sanitized.contains("cookies.txt"));
	}

	@Test
	public void sanitizeCommandRedactsQuotedCookiesPathWithSpaces() {
		final String command = "yt-dlp --cookies \"" + COOKIE_PATH + "\" \"https://example.test/v\"";
		final String sanitized = ExecutorOutputSanitizer.sanitizeCommand(command);
		assertTrue(sanitized.contains("--cookies ***"));
		assertFalse(sanitized.contains("Jane Doe"));
		assertFalse(sanitized.contains("cookies.txt"));
	}

	@Test
	public void sanitizeCommandRedactsSingleQuotedCookiesPathWithSpaces() {
		final String command = "yt-dlp --cookies '" + COOKIE_PATH + "' \"https://example.test/v\"";
		final String sanitized = ExecutorOutputSanitizer.sanitizeCommand(command);
		assertTrue(sanitized.contains("--cookies ***"));
		assertFalse(sanitized.contains("Jane Doe"));
	}

	@Test
	public void sanitizeCommandRedactsQuotedCookiesFromBrowserProfile() {
		final String command = "yt-dlp --cookies-from-browser \"firefox:default profile\" \"https://example.test/v\"";
		final String sanitized = ExecutorOutputSanitizer.sanitizeCommand(command);
		assertTrue(sanitized.contains("--cookies-from-browser ***"));
		assertFalse(sanitized.contains("firefox:default profile"));
	}

	@Test
	public void sanitizeOutputRedactsBareAccessTokenAtLineStart() {
		final String output = "access_token=" + SECRET_TOKEN;
		final String sanitized = ExecutorOutputSanitizer.sanitizeOutput(output);
		assertTrue(sanitized.contains("access_token=***"));
		assertFalse(sanitized.contains(SECRET_TOKEN));
	}

	@Test
	public void sanitizeOutputRedactsBareApiKeyInMixedLine() {
		final String output = "Download failed api_key=" + SECRET_TOKEN + " for replay";
		final String sanitized = ExecutorOutputSanitizer.sanitizeOutput(output);
		assertTrue(sanitized.contains("api_key=***"));
		assertFalse(sanitized.contains(SECRET_TOKEN));
	}

	@Test
	public void sanitizeOutputRedactsAuthorizationBearerValue() {
		final String output = "authorization=Bearer " + SECRET_TOKEN;
		final String sanitized = ExecutorOutputSanitizer.sanitizeOutput(output);
		assertTrue(sanitized.contains("authorization=***"));
		assertFalse(sanitized.contains(SECRET_TOKEN));
	}

	@Test
	public void sanitizeOutputPreservesNormalNonSecretParameters() {
		final String output = "video_id=episode-123 status=failed";
		final String sanitized = ExecutorOutputSanitizer.sanitizeOutput(output);
		assertTrue(sanitized.contains("video_id=episode-123"));
		assertTrue(sanitized.contains("status=failed"));
	}

	@Test
	public void sanitizeOutputRedactsEmbeddedApiKeyAndTruncates() {
		final StringBuilder output = new StringBuilder("ERROR " + SECRET_API_KEY + " ");
		for (int i = 0; i < 400; i++) {
			output.append('x');
		}
		final String sanitized = ExecutorOutputSanitizer.sanitizeOutput(output.toString());
		assertFalse(sanitized.contains(SECRET_API_KEY));
		assertTrue(sanitized.contains("AIza***"));
		assertTrue(sanitized.endsWith("..."));
		assertTrue(sanitized.length() <= 303);
	}
}
