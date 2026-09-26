package com.dabi.habitv.provider.tf1plus;

/**
 * Sanitized diagnostics for TF1+ HTML replay discovery.
 */
final class Tf1PlusDiagnostics {

	static final String STRATEGY_HTML = "public-html-replay";

	static final String STRATEGY_GRAPHQL = "public-graphql-replay";

	private final String strategy;

	private final String channelLabel;

	private String sourceUrl = "";

	private int anchorsScanned;

	private int candidateLinks;

	private int rejectedLinks;

	private int createdCategories;

	private int createdEpisodes;

	private String rootCauseSummary = "ok";

	Tf1PlusDiagnostics(final String channelLabel) {
		this(channelLabel, STRATEGY_HTML);
	}

	Tf1PlusDiagnostics(final String channelLabel, final String strategy) {
		this.channelLabel = channelLabel == null ? "" : channelLabel;
		this.strategy = strategy == null ? STRATEGY_HTML : strategy;
	}

	void setSourceUrl(final String sourceUrl) {
		this.sourceUrl = sourceUrl == null ? "" : sourceUrl;
	}

	int getAnchorsScanned() {
		return anchorsScanned;
	}

	void incrementAnchorsScanned() {
		anchorsScanned++;
	}

	int getCandidateLinks() {
		return candidateLinks;
	}

	void incrementCandidateLinks() {
		candidateLinks++;
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

	int getCreatedEpisodes() {
		return createdEpisodes;
	}

	void setCreatedEpisodes(final int createdEpisodes) {
		this.createdEpisodes = createdEpisodes;
	}

	String getRootCauseSummary() {
		return rootCauseSummary;
	}

	void setRootCauseSummary(final String rootCauseSummary) {
		this.rootCauseSummary = rootCauseSummary == null ? "unknown" : rootCauseSummary;
	}

	String formatLogLine() {
		return "provider=" + Tf1PlusConf.DISPLAY_NAME + " sourceUrl=" + sourceUrl + " strategy=" + strategy
				+ " channel=" + channelLabel + " anchorsScanned=" + anchorsScanned + " candidateLinks="
				+ candidateLinks + " rejectedLinks=" + rejectedLinks + " createdCategories=" + createdCategories
				+ " createdEpisodes=" + createdEpisodes + " cookiesEnabled=false rootCause=" + rootCauseSummary;
	}

}
