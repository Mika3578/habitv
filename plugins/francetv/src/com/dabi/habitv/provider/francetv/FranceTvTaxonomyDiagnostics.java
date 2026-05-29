package com.dabi.habitv.provider.francetv;

/**
 * Diagnostics for taxonomy episode listing ({@code /generic/taxonomy/.../contents}).
 */
final class FranceTvTaxonomyDiagnostics {

	static final String STRATEGY = "taxonomy-api";

	private final String taxonomyPath;

	private int page;

	private int createdReplayItems;

	private String rootCauseSummary = "ok";

	FranceTvTaxonomyDiagnostics(final String taxonomyPath) {
		this.taxonomyPath = taxonomyPath;
	}

	void setPage(final int page) {
		this.page = page;
	}

	void incrementCreatedReplayItems() {
		createdReplayItems++;
	}

	void setCreatedReplayItems(final int createdReplayItems) {
		this.createdReplayItems = createdReplayItems;
	}

	void setRootCauseSummary(final String rootCauseSummary) {
		this.rootCauseSummary = rootCauseSummary == null ? "unknown" : rootCauseSummary;
	}

	String getRootCauseSummary() {
		return rootCauseSummary;
	}

	String formatLogLine(final String sourceUrl) {
		return "provider=France.tv sourceUrl=" + sourceUrl + " strategy=" + STRATEGY + " taxonomyPath=" + taxonomyPath
				+ " page=" + page + " createdReplayItems=" + createdReplayItems + " rootCause=" + rootCauseSummary;
	}

}
