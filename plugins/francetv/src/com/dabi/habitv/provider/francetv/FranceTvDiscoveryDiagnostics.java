package com.dabi.habitv.provider.francetv;

/**
 * Counters and root-cause summary for a France.tv public page discovery run.
 */
final class FranceTvDiscoveryDiagnostics {

	static final String STRATEGY = "public-html-section-cards";

	private int headingsFound;

	private int linksScanned;

	private int candidateItemLinks;

	private int rejectedLinks;

	private int createdCategories;

	private int createdReplayItems;

	private String rootCauseSummary = "ok";

	FranceTvDiscoveryDiagnostics() {
	}

	int getHeadingsFound() {
		return headingsFound;
	}

	void setHeadingsFound(final int headingsFound) {
		this.headingsFound = headingsFound;
	}

	int getLinksScanned() {
		return linksScanned;
	}

	void incrementLinksScanned() {
		linksScanned++;
	}

	int getCandidateItemLinks() {
		return candidateItemLinks;
	}

	void incrementCandidateItemLinks() {
		candidateItemLinks++;
	}

	int getRejectedLinks() {
		return rejectedLinks;
	}

	void incrementRejectedLinks() {
		rejectedLinks++;
	}

	int getCreatedCategories() {
		return createdCategories;
	}

	void setCreatedCategories(final int createdCategories) {
		this.createdCategories = createdCategories;
	}

	int getCreatedReplayItems() {
		return createdReplayItems;
	}

	void incrementCreatedReplayItems() {
		createdReplayItems++;
	}

	String getRootCauseSummary() {
		return rootCauseSummary;
	}

	void setRootCauseSummary(final String rootCauseSummary) {
		this.rootCauseSummary = rootCauseSummary == null ? "unknown" : rootCauseSummary;
	}

	String formatLogLine(final String sourceUrl) {
		return "provider=France.tv sourceUrl=" + sourceUrl + " strategy=" + STRATEGY + " headingsFound="
				+ headingsFound + " linksScanned=" + linksScanned + " candidateItemLinks=" + candidateItemLinks
				+ " rejectedLinks=" + rejectedLinks + " createdCategories=" + createdCategories
				+ " createdReplayItems=" + createdReplayItems + " rootCause=" + rootCauseSummary;
	}
}
