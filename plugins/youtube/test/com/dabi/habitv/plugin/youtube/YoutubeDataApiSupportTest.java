package com.dabi.habitv.plugin.youtube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.TechnicalException;

public class YoutubeDataApiSupportTest {

	@Test
	public void appendApiKeyParamAddsKeyWhenPresent() {
		final String candidate = YoutubeTestSecrets.googleApiKeyPlaceholder();
		final String url = YoutubeDataApiSupport.appendApiKeyParam(
				"https://www.googleapis.com/youtube/v3/search?part=snippet", candidate);
		assertTrue(YoutubeDataApiSupport.urlHasApiKey(url));
		assertTrue(url.contains("key=" + YoutubeConf.normalizeApiKey(candidate)));
	}

	@Test
	public void appendApiKeyParamLeavesUrlUnchangedWhenKeyMissing() {
		final String base = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet";
		assertEquals(base, YoutubeDataApiSupport.appendApiKeyParam(base, null));
		assertFalse(YoutubeDataApiSupport.urlHasApiKey(base));
	}

	@Test
	public void redactUrlMasksApiKey() {
		final String redacted = YoutubeDataApiSupport.redactUrl(
				"https://www.googleapis.com/youtube/v3/search?part=snippet&key=secret-key&maxResults=10");
		assertFalse(redacted.contains("secret-key"));
		assertTrue(redacted.contains("key=***"));
	}

	@Test
	public void redactUrlMasksMalformedPathPrefixedKey() {
		final String embedded = YoutubeTestSecrets.googleApiKeyPlaceholder();
		final String redacted = YoutubeDataApiSupport.redactUrl(
				"https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&key=C:/Users/example/habitv/"
						+ embedded);
		assertFalse(redacted.contains(embedded));
		assertTrue(redacted.contains("key=***"));
	}

	@Test
	public void redactSecretsInTextMasksEmbeddedGoogleApiKeys() {
		final String embedded = YoutubeTestSecrets.googleApiKeyPlaceholder();
		final String redacted = YoutubeDataApiSupport.redactSecretsInText(
				"failure for key=C:/Users/example/" + embedded);
		assertFalse(redacted.contains(embedded));
		assertTrue(redacted.contains("AIza***"));
	}

	@Test
	public void redactUrlMasksAccessTokenParameter() {
		final String redacted = YoutubeDataApiSupport.redactUrl(
				"https://example.com/callback?access_token=secret-token&other=1");
		assertFalse(redacted.contains("secret-token"));
		assertTrue(redacted.contains("access_token=***"));
	}

	@Test
	public void buildSafeApiFailureMessageNeverContainsRawKey() {
		final String secret = YoutubeTestSecrets.urlQuerySecret();
		final String message = YoutubeDataApiSupport.buildSafeApiFailureMessage(
				"https://www.googleapis.com/youtube/v3/search?key=" + secret);
		assertFalse(message.contains(secret));
		assertTrue(message.contains("key=***"));
	}

	@Test
	public void forbiddenSummaryListsLikelyCauses() {
		assertTrue(YoutubeDataApiSupport.FORBIDDEN_SUMMARY.contains("HTTP 403"));
		assertTrue(YoutubeDataApiSupport.FORBIDDEN_SUMMARY.contains("API key"));
		assertTrue(YoutubeDataApiSupport.FORBIDDEN_SUMMARY.contains("quota"));
	}

	@Test
	public void recoverableApiErrorDetectsWrappedHttp403() {
		final TechnicalException wrapped = new TechnicalException(new java.io.IOException(
				"Server returned HTTP response code: 403 for URL: https://www.googleapis.com/youtube/v3/search"));
		assertEquals(YoutubeDataApiSupport.FORBIDDEN_SUMMARY,
				YoutubeDataApiSupport.summarizeRecoverableApiError(wrapped));
	}

	@Test
	public void recoverableApiErrorDetectsWrappedHttp400() {
		final TechnicalException wrapped = new TechnicalException(new java.io.IOException(
				"Server returned HTTP response code: 400 for URL: https://www.googleapis.com/youtube/v3/search?key=***"));
		assertEquals(YoutubeDataApiSupport.BAD_REQUEST_SUMMARY,
				YoutubeDataApiSupport.summarizeRecoverableApiError(wrapped));
	}

	@Test
	public void recoverableApiErrorReturnsNullForOtherErrors() {
		assertNull(YoutubeDataApiSupport.summarizeRecoverableApiError(
				new TechnicalException(new java.io.IOException("Server returned HTTP response code: 500"))));
	}

	@Test
	public void resolvePublishedAfterForSearchOmitsAllTimeWindow() {
		final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		final Date reference = new GregorianCalendar(2026, Calendar.MAY, 30).getTime();
		assertNull(YoutubeDataApiSupport.resolvePublishedAfterForSearch("36500", format, reference));
		assertNull(YoutubeDataApiSupport.resolvePublishedAfterForSearch(null, format, reference));
	}

	@Test
	public void resolvePublishedAfterForSearchKeepsRecentWindow() throws Exception {
		final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		final Date reference = new GregorianCalendar(2026, Calendar.MAY, 30).getTime();
		final String publishedAfter = YoutubeDataApiSupport.resolvePublishedAfterForSearch("30", format, reference);
		assertTrue(publishedAfter != null && publishedAfter.compareTo(YoutubeDataApiSupport.MIN_PUBLISHED_AFTER_RFC3339) >= 0);
		assertTrue(publishedAfter.startsWith("2026-"));
	}
}
