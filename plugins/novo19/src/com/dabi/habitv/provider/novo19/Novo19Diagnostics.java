package com.dabi.habitv.provider.novo19;

import com.dabi.habitv.api.plugin.exception.ExecutorOutputSanitizer;

/**
 * Sanitized diagnostics for NOVO19 catalogue operations.
 */
final class Novo19Diagnostics {

	static final String STRATEGY_BFF = "bff-json";

	private final String operation;

	private String sourcePath;

	private String endpointHost = "novo19-bff.ouest-france.fr";

	private String assetId;

	private int createdItems;

	private String rootCauseSummary = "ok";

	private int httpStatus;

	Novo19Diagnostics(final String operation) {
		this.operation = operation;
	}

	void setSourcePath(final String sourcePath) {
		this.sourcePath = sanitizePath(sourcePath);
	}

	void setEndpointHost(final String endpointHost) {
		if (endpointHost != null && !endpointHost.isEmpty()) {
			this.endpointHost = endpointHost;
		}
	}

	void setAssetId(final String assetId) {
		this.assetId = assetId;
	}

	void incrementCreatedItems() {
		createdItems++;
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

	String getRootCauseSummary() {
		return rootCauseSummary;
	}

	String formatLogLine() {
		final StringBuilder line = new StringBuilder("provider=NOVO19");
		line.append(" strategy=").append(STRATEGY_BFF);
		line.append(" operation=").append(operation);
		if (sourcePath != null) {
			line.append(" sourcePath=").append(sourcePath);
		}
		line.append(" endpointHost=").append(endpointHost);
		if (assetId != null) {
			line.append(" assetId=").append(assetId);
		}
		if (httpStatus > 0) {
			line.append(" httpStatus=").append(httpStatus);
		}
		line.append(" createdItems=").append(createdItems);
		line.append(" cookiesEnabled=false");
		line.append(" rootCause=").append(rootCauseSummary);
		return ExecutorOutputSanitizer.redactSecretsInText(line.toString());
	}

	private static String sanitizePath(final String path) {
		if (path == null) {
			return null;
		}
		final String redacted = ExecutorOutputSanitizer.redactSecretsInText(path);
		final int query = redacted.indexOf('?');
		return query >= 0 ? redacted.substring(0, query) : redacted;
	}

}
