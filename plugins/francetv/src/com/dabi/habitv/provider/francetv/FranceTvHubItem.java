package com.dabi.habitv.provider.francetv;

/**
 * A taxonomy-backed child discovered from a public channel hub API response.
 */
final class FranceTvHubItem {

	enum ItemType {
		PROGRAM, EVENT, COLLECTION, UNKNOWN
	}

	private final String label;

	private final String hubSlug;

	private final String taxonomySlug;

	private final String pageUrl;

	private final ItemType itemType;

	FranceTvHubItem(final String label, final String hubSlug, final String taxonomySlug, final String pageUrl,
			final ItemType itemType) {
		this.label = label;
		this.hubSlug = hubSlug;
		this.taxonomySlug = taxonomySlug;
		this.pageUrl = pageUrl;
		this.itemType = itemType;
	}

	String getLabel() {
		return label;
	}

	String getHubSlug() {
		return hubSlug;
	}

	String getTaxonomySlug() {
		return taxonomySlug;
	}

	String getPageUrl() {
		return pageUrl;
	}

	ItemType getItemType() {
		return itemType;
	}

}
