package com.dabi.habitv.provider.francetv;

import java.util.Date;

/**
 * Replay candidate or collection-only link discovered on a public France.tv page.
 */
final class FranceTvDiscoveryItem {

	private final String title;

	private final String itemUrl;

	private final Long durationSeconds;

	private final Date publicationDate;

	private final boolean collectionOnly;

	private final String sectionSlug;

	FranceTvDiscoveryItem(final String title, final String itemUrl, final Long durationSeconds,
			final Date publicationDate, final boolean collectionOnly, final String sectionSlug) {
		this.title = title;
		this.itemUrl = itemUrl;
		this.durationSeconds = durationSeconds;
		this.publicationDate = publicationDate;
		this.collectionOnly = collectionOnly;
		this.sectionSlug = sectionSlug;
	}

	String getTitle() {
		return title;
	}

	String getItemUrl() {
		return itemUrl;
	}

	Long getDurationSeconds() {
		return durationSeconds;
	}

	Date getPublicationDate() {
		return publicationDate;
	}

	boolean isCollectionOnly() {
		return collectionOnly;
	}

	String getSectionSlug() {
		return sectionSlug;
	}

	boolean isReplayCandidate() {
		return !collectionOnly;
	}
}
