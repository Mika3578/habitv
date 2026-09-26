package com.dabi.habitv.provider.playrts;

import com.dabi.habitv.api.plugin.exception.ExecutorOutputSanitizer;

/**
 * Sanitized catalogue/download diagnostics for Play RTS.
 */
final class PlayRtsDiagnostics {

	static final String STRATEGY_PLAY_API_YTDLP = "play-v3-api-ytdlp";

	private final String operation;

	private String showId;

	private String sourceUrl;

	private int createdItems;

	private String rootCauseSummary = "ok";

	private int httpStatus;

	PlayRtsDiagnostics(final String operation) {
		this.operation = operation;
	}

	void setShowId(final String showId) {
		this.showId = showId;
	}

	void setSourceUrl(final String sourceUrl) {
		this.sourceUrl = sanitizeUrl(sourceUrl);
	}

	void setCreatedItems(final int createdItems) {
		this.createdItems = createdItems;
	}

	void setRootCauseSummary(final String rootCauseSummary) {
		this.rootCauseSummary = rootCauseSummary == null ? "unknown" : rootCauseSummary;
	}

	void setHttpStatus(final int httpStatus) {
		this.httpStatus = httpStatus;
	}

	String formatLogLine() {
		final StringBuilder line = new StringBuilder("provider=PLAYRTS");
		line.append(" strategy=").append(STRATEGY_PLAY_API_YTDLP);
		line.append(" operation=").append(operation);
		if (showId != null) {
			line.append(" showId=").append(showId);
		}
		if (sourceUrl != null) {
			line.append(" sourceUrl=").append(sourceUrl);
		}
		if (httpStatus > 0) {
			line.append(" httpStatus=").append(httpStatus);
		}
		line.append(" createdItems=").append(createdItems);
		line.append(" cookiesEnabled=false");
		line.append(" rootCause=").append(rootCauseSummary);
		return ExecutorOutputSanitizer.redactSecretsInText(line.toString());
	}

	private static String sanitizeUrl(final String url) {
		if (url == null) {
			return null;
		}
		final String redacted = ExecutorOutputSanitizer.redactSecretsInText(url);
		final int query = redacted.indexOf('?');
		return query >= 0 ? redacted.substring(0, query) : redacted;
	}
}
