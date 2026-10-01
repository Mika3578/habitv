package com.dabi.habitv.provider.telequebec;

import com.dabi.habitv.api.plugin.exception.ExecutorOutputSanitizer;

final class TeleQuebecDiagnostics {

	static final String STRATEGY = "graphql-catalog-geo-playback";

	private final String operation;

	private String showSlug;

	private String sourceUrl;

	private int createdItems;

	private String rootCauseSummary = "ok";

	TeleQuebecDiagnostics(final String operation) {
		this.operation = operation;
	}

	void setShowSlug(final String showSlug) {
		this.showSlug = showSlug;
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
		final StringBuilder line = new StringBuilder("provider=TELEQUEBEC");
		line.append(" strategy=").append(STRATEGY);
		line.append(" operation=").append(operation);
		if (showSlug != null) {
			line.append(" showSlug=").append(showSlug);
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
