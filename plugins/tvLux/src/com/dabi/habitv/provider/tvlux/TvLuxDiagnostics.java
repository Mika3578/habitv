package com.dabi.habitv.provider.tvlux;

import java.net.URI;
import java.util.Locale;

import com.dabi.habitv.api.plugin.exception.ExecutorOutputSanitizer;

final class TvLuxDiagnostics {

	static final String STRATEGY = "html-freecaster-hls";

	private final String operation;

	private String showSlug;

	private String sourceUrl;

	private int createdItems;

	private String rootCauseSummary = "ok";

	TvLuxDiagnostics(final String operation) {
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
		final StringBuilder line = new StringBuilder("provider=TVLUX");
		line.append(" strategy=").append(STRATEGY);
		line.append(" operation=").append(operation);
		if (showSlug != null) {
			line.append(" showSlug=").append(showSlug);
		}
		if (sourceUrl != null) {
			line.append(" sourceUrl=").append(sourceUrl);
		}
		line.append(" createdItems=").append(createdItems);
		line.append(" cookiesEnabled=optional");
		line.append(" rootCause=").append(rootCauseSummary);
		return ExecutorOutputSanitizer.redactSecretsInText(line.toString());
	}

	private static String sanitizeUrl(final String url) {
		if (url == null) {
			return null;
		}
		final String redacted = ExecutorOutputSanitizer.redactSecretsInText(url);
		try {
			final URI uri = URI.create(redacted.trim());
			final String scheme = uri.getScheme();
			final String host = uri.getHost();
			if (scheme != null && host != null) {
				final StringBuilder safe = new StringBuilder();
				safe.append(scheme).append("://").append(host);
				if (uri.getPort() >= 0) {
					safe.append(':').append(uri.getPort());
				}
				final String rawPath = uri.getRawPath();
				if (rawPath != null) {
					safe.append(truncateAtUnsafeDelimiter(rawPath));
				}
				return safe.toString();
			}
		} catch (final IllegalArgumentException ignored) {
			// fall through
		}
		return truncateAtUnsafeDelimiter(redacted);
	}

	/**
	 * Truncate before query/fragment markers, encoded delimiters, and CR/LF so
	 * diagnostic lines cannot reintroduce secrets or inject extra log rows.
	 */
	private static String truncateAtUnsafeDelimiter(final String value) {
		int cut = value.length();
		cut = earlierIndex(cut, value.indexOf('?'));
		cut = earlierIndex(cut, value.indexOf('#'));
		cut = earlierIndex(cut, value.indexOf('\r'));
		cut = earlierIndex(cut, value.indexOf('\n'));
		final String lower = value.toLowerCase(Locale.ROOT);
		cut = earlierIndex(cut, lower.indexOf("%3f"));
		cut = earlierIndex(cut, lower.indexOf("%23"));
		cut = earlierIndex(cut, lower.indexOf("%0a"));
		cut = earlierIndex(cut, lower.indexOf("%0d"));
		return value.substring(0, cut);
	}

	private static int earlierIndex(final int current, final int candidate) {
		if (candidate >= 0 && candidate < current) {
			return candidate;
		}
		return current;
	}
}
