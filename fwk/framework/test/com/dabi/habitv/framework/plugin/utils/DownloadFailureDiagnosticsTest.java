package com.dabi.habitv.framework.plugin.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;

public class DownloadFailureDiagnosticsTest {

	@Test
	public void getClassificationKeyReturnsStableIdentifierForDrmFailure() {
		final ExecutorFailedException failure = new ExecutorFailedException("yt-dlp \"url\"",
				"ERROR: Content is DRM protected by Widevine", "ERROR: Content is DRM protected by Widevine", null);
		assertEquals(DownloadFailureDiagnostics.CLASSIFICATION_DRM_PROTECTED,
				DownloadFailureDiagnostics.getClassificationKey(failure));
	}

	@Test
	public void buildUserMessageClassifiesDrmFailures() {
		final ExecutorFailedException failure = new ExecutorFailedException("yt-dlp \"url\"",
				"ERROR: Content is DRM protected by Widevine", "ERROR: Content is DRM protected by Widevine", null);
		final String message = DownloadFailureDiagnostics.buildUserMessage(null, failure);
		assertEquals("Content appears rights-protected. Habitv cannot retrieve it.", message);
	}

	@Test
	public void buildUserMessageClassifiesGeoRestriction() {
		final ExecutorFailedException failure = new ExecutorFailedException("yt-dlp \"url\"",
				"This video is not available in your country.", "This video is not available in your country.", null);
		assertEquals("Content is unavailable in this region (geo restriction).",
				DownloadFailureDiagnostics.buildUserMessage(null, failure));
	}

	@Test
	public void buildUserMessageRedactsSecretsFromLastLine() {
		final ExecutorFailedException failure = new ExecutorFailedException(
				"yt-dlp \"https://example.test/v?key=AIzaSySecretKeyValue\"",
				"download failed", "HTTP 500 for https://example.test/v?key=AIzaSySecretKeyValue", null);
		final String message = DownloadFailureDiagnostics.buildUserMessage(null, failure);
		assertFalse(message.contains("AIzaSySecretKeyValue"));
		assertTrue(message.contains("key=***"));
	}

	@Test
	public void formatLogLineIncludesProviderEpisodeAndSanitizedSnippet() {
		final CategoryDTO category = new CategoryDTO("TF1", "Journal", "journal", "mp4");
		final EpisodeDTO episode = new EpisodeDTO(category, "Episode title", "episode-id");
		final ExecutorFailedException failure = new ExecutorFailedException("yt-dlp \"url\"",
				"ERROR: Video unavailable\n", "ERROR: Video unavailable", null);
		final String line = DownloadFailureDiagnostics.formatLogLine(episode, "youtube", failure);
		assertTrue(line.contains("provider=youtube"));
		assertTrue(line.contains("channel=Journal"));
		assertTrue(line.contains("episode=Episode title"));
		assertTrue(line.contains("rootCause=Video is unavailable or no longer published."));
	}

	@Test
	public void toUserFacingFailureWrapsCauseWithClassifiedMessage() {
		final CategoryDTO category = new CategoryDTO("France.tv", "Series", "series", "mp4");
		final EpisodeDTO episode = new EpisodeDTO(category, "Episode title", "episode-id");
		final DownloadFailedException cause = new DownloadFailedException(new ExecutorFailedException("yt-dlp \"url\"",
				"ERROR: Sign in to confirm your age", "ERROR: Sign in to confirm your age", null));
		final Throwable userFacing = DownloadFailureDiagnostics.toUserFacingFailure(cause, episode);
		assertEquals(
				"Content requires sign-in or a subscription. Habitv does not support restricted access.",
				userFacing.getMessage());
		assertTrue(userFacing.getCause() instanceof DownloadFailedException);
	}

	@Test
	public void buildUserMessageClassifiesFormatHttpAndLiveFailures() {
		assertEquals("No compatible format was found for this replay.",
				DownloadFailureDiagnostics.buildUserMessage(null, new ExecutorFailedException("yt-dlp \"url\"",
						"ERROR: Requested format is not available", "ERROR: Requested format is not available", null)));
		assertEquals("Provider rejected the request (HTTP 403).",
				DownloadFailureDiagnostics.buildUserMessage(null, new ExecutorFailedException("yt-dlp \"url\"",
						"ERROR: Unable to download webpage: HTTP Error 403: Forbidden",
						"ERROR: Unable to download webpage: HTTP Error 403: Forbidden", null)));
		assertEquals("URL is not supported by the configured tool plugin.",
				DownloadFailureDiagnostics.buildUserMessage(null, new ExecutorFailedException("yt-dlp \"url\"",
						"ERROR: Unsupported URL", "ERROR: Unsupported URL", null)));
		assertEquals("Live or upcoming content is not available for retrieval yet.",
				DownloadFailureDiagnostics.buildUserMessage(null, new ExecutorFailedException("yt-dlp \"url\"",
						"ERROR: This live event will begin in a few moments",
						"ERROR: This live event will begin in a few moments", null)));
		assertEquals("Episode retrieval failed. See the log for sanitized diagnostics.",
				DownloadFailureDiagnostics.buildUserMessage(null,
						new ExecutorFailedException("yt-dlp \"url\"", "", "", null)));
	}
}
