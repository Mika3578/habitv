package com.dabi.habitv.provider.lemanbleu;

import com.dabi.habitv.api.plugin.exception.ExecutorOutputSanitizer;

/**
 * Sanitized catalogue/download diagnostics for Léman Bleu.
 */
final class LemanBleuDiagnostics {

	static final String STRATEGY_HTML_INFOMANIAK_CURL = "html-infomaniak-curl";

	private final String operation;

	private String showId;

	private String sourceUrl;

	private int createdItems;

	private String rootCauseSummary = "ok";

	LemanBleuDiagnostics(final String operation) {
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

	String formatLogLine() {
		final StringBuilder line = new StringBuilder("provider=LEMANBLEU");
		line.append(" strategy=").append(STRATEGY_HTML_INFOMANIAK_CURL);
		line.append(" operation=").append(operation);
		if (showId != null) {
			line.append(" showId=").append(showId);
		}
		if (sourceUrl != null) {
			line.append(" sourceUrl=").append(sourceUrl);
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
