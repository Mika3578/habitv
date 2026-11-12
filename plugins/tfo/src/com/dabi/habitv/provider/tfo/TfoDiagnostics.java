package com.dabi.habitv.provider.tfo;

import java.net.URI;

import com.dabi.habitv.api.plugin.exception.ExecutorOutputSanitizer;

/**
 * Sanitized catalogue/download diagnostics for TFO.
 */
final class TfoDiagnostics {

	static final String STRATEGY = "html-catalog-jwplayer-hls";

	private final String operation;

	private String catalog;

	private String showPath;

	private String sourceUrl;

	private int createdItems;

	private String rootCauseSummary = "ok";

	TfoDiagnostics(final String operation) {
		this.operation = operation;
	}

	void setCatalog(final String catalog) {
		this.catalog = catalog;
	}

	void setShowPath(final String showPath) {
		this.showPath = showPath;
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
		final StringBuilder line = new StringBuilder("provider=TFO");
		line.append(" strategy=").append(STRATEGY);
		line.append(" operation=").append(operation);
		if (catalog != null) {
			line.append(" catalog=").append(catalog);
		}
		if (showPath != null) {
			line.append(" showPath=").append(showPath);
		}
		if (sourceUrl != null) {
			line.append(" sourceUrl=").append(sourceUrl);
		}
		line.append(" createdItems=").append(createdItems);
		line.append(" cookiesEnabled=false");
		line.append(" rootCause=").append(rootCauseSummary);
		return ExecutorOutputSanitizer.redactSecretsInText(line.toString());
	}

	/**
	 * Rebuild scheme/host/path only so query and fragment never appear in logs.
	 */
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
				final String path = uri.getPath();
				if (path != null) {
					safe.append(path);
				}
				return safe.toString();
			}
		} catch (final IllegalArgumentException ignored) {
			// fall through
		}
		int cut = redacted.length();
		final int query = redacted.indexOf('?');
		final int fragment = redacted.indexOf('#');
		if (query >= 0) {
			cut = Math.min(cut, query);
		}
		if (fragment >= 0) {
			cut = Math.min(cut, fragment);
		}
		return redacted.substring(0, cut);
	}
}
