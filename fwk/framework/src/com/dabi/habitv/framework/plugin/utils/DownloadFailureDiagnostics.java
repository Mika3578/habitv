package com.dabi.habitv.framework.plugin.utils;

import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;
import com.dabi.habitv.api.plugin.exception.ExecutorOutputSanitizer;

/**
 * Builds sanitized download failure summaries for logs and UI.
 */
public final class DownloadFailureDiagnostics {

	public static final String CLASSIFICATION_DRM_PROTECTED = "drm-protected";
	public static final String CLASSIFICATION_GEO_RESTRICTED = "geo-restricted";
	public static final String CLASSIFICATION_AUTH_REQUIRED = "auth-required";
	public static final String CLASSIFICATION_PRIVATE_VIDEO = "private-video";
	public static final String CLASSIFICATION_UNAVAILABLE = "unavailable";
	public static final String CLASSIFICATION_FORMAT_NOT_FOUND = "format-not-found";
	public static final String CLASSIFICATION_HTTP_403 = "http-403";
	public static final String CLASSIFICATION_HTTP_404 = "http-404";
	public static final String CLASSIFICATION_UNSUPPORTED_URL = "unsupported-url";
	public static final String CLASSIFICATION_LIVE_UPCOMING = "live-upcoming";

	private static final String DEFAULT_USER_MESSAGE = "Download failed. See the log for sanitized diagnostics.";

	private DownloadFailureDiagnostics() {
	}

	public static String buildUserMessage(final EpisodeDTO episode, final Throwable failure) {
		final String classified = classifyFailure(failure);
		if (classified != null) {
			return classified;
		}
		final String lastLine = extractLastMeaningfulLine(failure);
		if (lastLine != null && !lastLine.isEmpty()) {
			return ExecutorOutputSanitizer.redactSecretsInText(lastLine);
		}
		return DEFAULT_USER_MESSAGE;
	}

	public static String getClassificationKey(final Throwable failure) {
		return classifyFailureKey(failure);
	}

	public static String formatLogLine(final EpisodeDTO episode, final String providerName,
			final Throwable failure) {
		final StringBuilder line = new StringBuilder("downloadFailure");
		line.append(" provider=").append(providerName == null ? "unknown" : providerName);
		if (episode != null && episode.getCategory() != null) {
			line.append(" channel=").append(episode.getCategory().getName());
		}
		if (episode != null) {
			line.append(" episode=").append(episode.getName());
		}
		final String rootCause = classifyFailure(failure);
		if (rootCause != null) {
			line.append(" rootCause=").append(rootCause);
		} else {
			final String lastLine = extractLastMeaningfulLine(failure);
			if (lastLine != null && !lastLine.isEmpty()) {
				line.append(" detail=").append(ExecutorOutputSanitizer.redactSecretsInText(lastLine));
			}
		}
		final String outputSnippet = extractSanitizedOutputSnippet(failure);
		if (outputSnippet != null && !outputSnippet.isEmpty()) {
			line.append(" outputSnippet=").append(outputSnippet);
		}
		return line.toString();
	}

	static String classifyFailure(final Throwable failure) {
		final String key = classifyFailureKey(failure);
		if (key == null) {
			return null;
		}
		return messageForClassificationKey(key);
	}

	static String classifyFailureKey(final Throwable failure) {
		final String haystack = collectSearchText(failure);
		if (haystack.isEmpty()) {
			return null;
		}
		final String lower = haystack.toLowerCase();
		if (containsAny(lower, "drm", "widevine", "fairplay", "playready", "copyright protection")) {
			return CLASSIFICATION_DRM_PROTECTED;
		}
		if (containsAny(lower, "not available in your country", "geo restricted", "geoblocked",
				"only available in ", "this video is not available")) {
			return CLASSIFICATION_GEO_RESTRICTED;
		}
		if (containsAny(lower, "sign in to confirm", "sign in to view", "login required", "please log in",
				"members only", "subscription required", "premium content", "paywall")) {
			return CLASSIFICATION_AUTH_REQUIRED;
		}
		if (containsAny(lower, "private video", "video is private")) {
			return CLASSIFICATION_PRIVATE_VIDEO;
		}
		if (containsAny(lower, "video unavailable", "content is not available", "removed by the uploader",
				"this live event has ended")) {
			return CLASSIFICATION_UNAVAILABLE;
		}
		if (containsAny(lower, "requested format is not available", "no video formats found",
				"format is not available")) {
			return CLASSIFICATION_FORMAT_NOT_FOUND;
		}
		if (containsAny(lower, "http error 403", "403 forbidden", "http response code: 403", "http 403")) {
			return CLASSIFICATION_HTTP_403;
		}
		if (containsAny(lower, "http error 404", "404 not found", "http response code: 404", "http 404")) {
			return CLASSIFICATION_HTTP_404;
		}
		if (containsAny(lower, "unsupported url", "no suitable extractor", "unable to extract")) {
			return CLASSIFICATION_UNSUPPORTED_URL;
		}
		if (containsAny(lower, "live event will begin", "premieres in")) {
			return CLASSIFICATION_LIVE_UPCOMING;
		}
		return null;
	}

	private static String messageForClassificationKey(final String key) {
		if (CLASSIFICATION_DRM_PROTECTED.equals(key)) {
			return "Content appears DRM-protected. Habitv cannot bypass DRM.";
		}
		if (CLASSIFICATION_GEO_RESTRICTED.equals(key)) {
			return "Content is unavailable in this region (geo restriction).";
		}
		if (CLASSIFICATION_AUTH_REQUIRED.equals(key)) {
			return "Content requires authentication or a subscription. Habitv does not bypass login or paywalls.";
		}
		if (CLASSIFICATION_PRIVATE_VIDEO.equals(key)) {
			return "Video is private or requires account access.";
		}
		if (CLASSIFICATION_UNAVAILABLE.equals(key)) {
			return "Video is unavailable or no longer published.";
		}
		if (CLASSIFICATION_FORMAT_NOT_FOUND.equals(key)) {
			return "No compatible download format was found for this replay.";
		}
		if (CLASSIFICATION_HTTP_403.equals(key)) {
			return "Provider rejected the download request (HTTP 403).";
		}
		if (CLASSIFICATION_HTTP_404.equals(key)) {
			return "Replay page or media endpoint was not found (HTTP 404).";
		}
		if (CLASSIFICATION_UNSUPPORTED_URL.equals(key)) {
			return "URL is not supported by the configured downloader.";
		}
		if (CLASSIFICATION_LIVE_UPCOMING.equals(key)) {
			return "Live or upcoming content is not downloadable yet.";
		}
		return null;
	}

	private static String collectSearchText(final Throwable failure) {
		final StringBuilder text = new StringBuilder();
		Throwable current = failure;
		while (current != null) {
			if (current.getMessage() != null) {
				text.append(current.getMessage()).append('\n');
			}
			if (current instanceof ExecutorFailedException) {
				final ExecutorFailedException executorFailure = (ExecutorFailedException) current;
				if (executorFailure.getFullOuput() != null) {
					text.append(executorFailure.getFullOuput()).append('\n');
				}
				if (executorFailure.getLastLine() != null) {
					text.append(executorFailure.getLastLine()).append('\n');
				}
			}
			current = current.getCause();
		}
		return text.toString();
	}

	private static String extractLastMeaningfulLine(final Throwable failure) {
		Throwable current = failure;
		while (current != null) {
			if (current instanceof ExecutorFailedException) {
				final ExecutorFailedException executorFailure = (ExecutorFailedException) current;
				if (executorFailure.getLastLine() != null && !executorFailure.getLastLine().trim().isEmpty()) {
					return executorFailure.getLastLine().trim();
				}
			}
			if (current.getMessage() != null && !current.getMessage().trim().isEmpty()) {
				return current.getMessage().trim();
			}
			current = current.getCause();
		}
		return null;
	}

	private static String extractSanitizedOutputSnippet(final Throwable failure) {
		Throwable current = failure;
		while (current != null) {
			if (current instanceof ExecutorFailedException) {
				final String snippet = ExecutorOutputSanitizer
						.sanitizeOutput(((ExecutorFailedException) current).getFullOuput());
				if (snippet != null && !snippet.isEmpty()) {
					return snippet;
				}
			}
			current = current.getCause();
		}
		return null;
	}

	private static boolean containsAny(final String haystack, final String... needles) {
		for (final String needle : needles) {
			if (haystack.contains(needle)) {
				return true;
			}
		}
		return false;
	}

	public static Throwable toUserFacingFailure(final Throwable failure, final EpisodeDTO episode) {
		if (failure == null) {
			return null;
		}
		final String userMessage = buildUserMessage(episode, failure);
		if (failure instanceof DownloadFailedException) {
			return new DownloadFailedException(userMessage, failure);
		}
		return new DownloadFailedException(userMessage, failure);
	}
}
