package com.dabi.habitv.provider.rtbfauvio;

import com.dabi.habitv.api.plugin.exception.ExecutorOutputSanitizer;

final class RtbfAuvioDiagnostics {

	private final String operation;

	private String channel;

	private String sourceUrl;

	private int createdItems;

	private String rootCauseSummary = "ok";

	RtbfAuvioDiagnostics(final String operation) {
		this.operation = operation;
	}

	void setChannel(final String channel) {
		this.channel = channel;
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
		final StringBuilder line = new StringBuilder("provider=RTBFAUVIO");
		line.append(" strategy=").append(RtbfAuvioConf.STRATEGY);
		line.append(" operation=").append(operation);
		if (channel != null) {
			line.append(" channel=").append(channel);
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
