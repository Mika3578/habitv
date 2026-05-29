package com.dabi.habitv.provider.francetv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rubrique / section heading on a France.tv public collection page.
 */
final class FranceTvDiscoverySection {

	private final String label;

	private final String slug;

	private final String sourceUrl;

	private final List<FranceTvDiscoveryItem> items = new ArrayList<>();

	FranceTvDiscoverySection(final String label, final String slug, final String sourceUrl) {
		this.label = label;
		this.slug = slug;
		this.sourceUrl = sourceUrl;
	}

	String getLabel() {
		return label;
	}

	String getSlug() {
		return slug;
	}

	String getSourceUrl() {
		return sourceUrl;
	}

	List<FranceTvDiscoveryItem> getItems() {
		return Collections.unmodifiableList(items);
	}

	void addItem(final FranceTvDiscoveryItem item) {
		items.add(item);
	}

	boolean hasReplayItems() {
		for (final FranceTvDiscoveryItem item : items) {
			if (item.isReplayCandidate()) {
				return true;
			}
		}
		return false;
	}

	int replayItemCount() {
		int count = 0;
		for (final FranceTvDiscoveryItem item : items) {
			if (item.isReplayCandidate()) {
				count++;
			}
		}
		return count;
	}
}
