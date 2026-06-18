package com.dabi.habitv.provider.tf1plus;

import java.util.Collections;
import java.util.List;

/**
 * One page of TF1+ programme catalogue results.
 */
final class Tf1PlusCataloguePageResult {

	private final List<Tf1PlusCatalogueEntry> entries;
	private final int requestedOffset;
	private final int requestedLimit;
	private final boolean lastPage;

	Tf1PlusCataloguePageResult(final List<Tf1PlusCatalogueEntry> entries, final int requestedOffset,
			final int requestedLimit, final boolean lastPage) {
		this.entries = entries == null ? Collections.<Tf1PlusCatalogueEntry>emptyList()
				: Collections.unmodifiableList(entries);
		this.requestedOffset = requestedOffset;
		this.requestedLimit = requestedLimit;
		this.lastPage = lastPage;
	}

	List<Tf1PlusCatalogueEntry> getEntries() {
		return entries;
	}

	int getRequestedOffset() {
		return requestedOffset;
	}

	int getRequestedLimit() {
		return requestedLimit;
	}

	boolean isLastPage() {
		return lastPage;
	}

}
