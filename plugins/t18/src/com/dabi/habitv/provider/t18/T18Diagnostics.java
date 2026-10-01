package com.dabi.habitv.provider.t18;

import com.dabi.habitv.api.plugin.exception.ExecutorOutputSanitizer;

final class T18Diagnostics {

	private final String operation;

	private String sourceUrl;

	private int createdItems;

	private String rootCauseSummary = "ok";

	T18Diagnostics(final String operation) {
		this.operation = operation;
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
		final StringBuilder line = new StringBuilder("provider=T18");
		line.append(" strategy=").append(T18Conf.STRATEGY);
		line.append(" operation=").append(operation);
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
