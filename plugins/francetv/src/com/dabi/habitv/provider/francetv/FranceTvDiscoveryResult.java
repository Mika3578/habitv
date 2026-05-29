package com.dabi.habitv.provider.francetv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parsed France.tv public collection page with diagnostics.
 */
final class FranceTvDiscoveryResult {

	private final String sourceUrl;

	private final String pageTitle;

	private final List<FranceTvDiscoverySection> sections = new ArrayList<>();

	private final FranceTvDiscoveryDiagnostics diagnostics = new FranceTvDiscoveryDiagnostics();

	FranceTvDiscoveryResult(final String sourceUrl) {
		this.sourceUrl = sourceUrl;
		this.pageTitle = null;
	}

	FranceTvDiscoveryResult(final String sourceUrl, final String pageTitle) {
		this.sourceUrl = sourceUrl;
		this.pageTitle = pageTitle;
	}

	String getSourceUrl() {
		return sourceUrl;
	}

	String getPageTitle() {
		return pageTitle;
	}

	List<FranceTvDiscoverySection> getSections() {
		return Collections.unmodifiableList(sections);
	}

	void addSection(final FranceTvDiscoverySection section) {
		sections.add(section);
	}

	FranceTvDiscoveryDiagnostics getDiagnostics() {
		return diagnostics;
	}

	boolean isSuccessful() {
		return !"parse-failed".equals(diagnostics.getRootCauseSummary())
				&& !"empty-page".equals(diagnostics.getRootCauseSummary());
	}

	List<FranceTvDiscoveryItem> replayItemsForSection(final String sectionSlug) {
		final List<FranceTvDiscoveryItem> replayItems = new ArrayList<>();
		for (final FranceTvDiscoverySection section : sections) {
			if (sectionSlug != null && !sectionSlug.equals(section.getSlug())) {
				continue;
			}
			for (final FranceTvDiscoveryItem item : section.getItems()) {
				if (item.isReplayCandidate()) {
					replayItems.add(item);
				}
			}
		}
		return replayItems;
	}
}
