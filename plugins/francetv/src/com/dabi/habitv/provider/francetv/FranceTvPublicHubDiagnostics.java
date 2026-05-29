package com.dabi.habitv.provider.francetv;

/**
 * Diagnostics for public hub API discovery ({@code /apps/channels/{hubSlug}}).
 */
final class FranceTvPublicHubDiagnostics {

	static final String STRATEGY = "public-api-channel-hub";

	private final String hubSlug;

	private int collectionsFound;

	private int itemsScanned;

	private int taxonomyCandidates;

	private int createdCategories;

	private int createdReplayItems;

	private String rootCauseSummary = "ok";

	FranceTvPublicHubDiagnostics(final String hubSlug) {
		this.hubSlug = hubSlug;
	}

	String getHubSlug() {
		return hubSlug;
	}

	int getCollectionsFound() {
		return collectionsFound;
	}

	void setCollectionsFound(final int collectionsFound) {
		this.collectionsFound = collectionsFound;
	}

	int getItemsScanned() {
		return itemsScanned;
	}

	void incrementItemsScanned() {
		itemsScanned++;
	}

	int getTaxonomyCandidates() {
		return taxonomyCandidates;
	}

	void incrementTaxonomyCandidates() {
		taxonomyCandidates++;
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

	void setCreatedReplayItems(final int createdReplayItems) {
		this.createdReplayItems = createdReplayItems;
	}

	String getRootCauseSummary() {
		return rootCauseSummary;
	}

	void setRootCauseSummary(final String rootCauseSummary) {
		this.rootCauseSummary = rootCauseSummary == null ? "unknown" : rootCauseSummary;
	}

	String formatLogLine(final String sourceUrl) {
		return "provider=France.tv sourceUrl=" + sourceUrl + " strategy=" + STRATEGY + " hubSlug=" + hubSlug
				+ " collectionsFound=" + collectionsFound + " itemsScanned=" + itemsScanned
				+ " taxonomyCandidates=" + taxonomyCandidates
				+ " createdCategories=" + createdCategories + " createdReplayItems=" + createdReplayItems
				+ " rootCause=" + rootCauseSummary;
	}

}
