package com.dabi.habitv.provider.tf1plus;

import com.dabi.habitv.api.plugin.exception.ExecutorOutputSanitizer;

/**
 * Sanitized catalogue/download diagnostics for TF1+.
 */
final class Tf1PlusDiagnostics {

	static final String STRATEGY_GRAPHQL_YTDLP = "graphql-ytdlp";

	private final String operation;

	private String channel;

	private String programSlug;

	private String sourceUrl;

	private int createdItems;

	private String rootCauseSummary = "ok";

	private int httpStatus;

	Tf1PlusDiagnostics(final String operation) {
		this.operation = operation;
	}

	void setChannel(final String channel) {
		this.channel = channel;
	}

	void setProgramSlug(final String programSlug) {
		this.programSlug = programSlug;
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
		final StringBuilder line = new StringBuilder("provider=TF1PLUS");
		line.append(" strategy=").append(STRATEGY_GRAPHQL_YTDLP);
		line.append(" operation=").append(operation);
		if (channel != null) {
			line.append(" channel=").append(channel);
		}
		if (programSlug != null) {
			line.append(" programSlug=").append(programSlug);
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
