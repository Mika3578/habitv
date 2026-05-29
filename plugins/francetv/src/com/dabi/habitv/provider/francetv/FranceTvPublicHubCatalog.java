package com.dabi.habitv.provider.francetv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Parses {@code /apps/channels/{hubSlug}} JSON into taxonomy-backed hub items.
 */
final class FranceTvPublicHubCatalog {

	static final String ROOT_CAUSE_NO_USABLE_TAXONOMY = "no-usable-taxonomy-candidates";

	private static final Comparator<FranceTvHubItem> LABEL_ORDER = new Comparator<FranceTvHubItem>() {
		@Override
		public int compare(final FranceTvHubItem left, final FranceTvHubItem right) {
			final String leftLabel = left == null || left.getLabel() == null ? "" : left.getLabel();
			final String rightLabel = right == null || right.getLabel() == null ? "" : right.getLabel();
			return leftLabel.compareToIgnoreCase(rightLabel);
		}
	};

	private FranceTvPublicHubCatalog() {
	}

	static HubDiscoveryResult discoverHubItems(final String hubSlug, final Map<String, Object> body) {
		final FranceTvPublicHubDiagnostics diagnostics = new FranceTvPublicHubDiagnostics(hubSlug);
		final List<FranceTvHubItem> items = new ArrayList<FranceTvHubItem>();
		if (body == null) {
			diagnostics.setRootCauseSummary("empty-response");
			return new HubDiscoveryResult(items, diagnostics);
		}
		final List<Map<String, Object>> collections = castItemList(body.get("collections"));
		diagnostics.setCollectionsFound(collections.size());
		for (final Map<String, Object> collection : collections) {
			for (final Map<String, Object> rawItem : castItemList(collection.get("items"))) {
				diagnostics.incrementItemsScanned();
				final FranceTvHubItem hubItem = toHubItem(hubSlug, rawItem);
				if (hubItem == null) {
					continue;
				}
				diagnostics.incrementTaxonomyCandidates();
				items.add(hubItem);
			}
		}
		if (items.isEmpty() && diagnostics.getCollectionsFound() == 0) {
			diagnostics.setRootCauseSummary("no-collections");
		} else if (items.isEmpty()) {
			diagnostics.setRootCauseSummary(ROOT_CAUSE_NO_USABLE_TAXONOMY);
		}
		return new HubDiscoveryResult(items, diagnostics);
	}

	/**
	 * Returns all navigable taxonomy-backed children for a hub. Configured seeds
	 * are listed first; remaining API candidates follow in deterministic label
	 * order without duplicates.
	 */
	static List<FranceTvHubItem> selectNavigableChildren(final String hubSlug,
			final List<FranceTvHubItem> candidates) {
		final List<FranceTvHubItem> safeCandidates = candidates == null ? Collections.<FranceTvHubItem>emptyList()
				: candidates;
		final Map<String, FranceTvHubItem> uniqueBySlug = new LinkedHashMap<String, FranceTvHubItem>();
		for (final FranceTvHubItem candidate : safeCandidates) {
			if (candidate == null || StringUtils.isEmpty(candidate.getTaxonomySlug())) {
				continue;
			}
			if (!isNavigableItemType(candidate.getItemType())) {
				continue;
			}
			if (!uniqueBySlug.containsKey(candidate.getTaxonomySlug())) {
				uniqueBySlug.put(candidate.getTaxonomySlug(), candidate);
			}
		}

		final List<FranceTvHubItem> ordered = new ArrayList<FranceTvHubItem>();
		final Set<String> seenSlugs = new LinkedHashSet<String>();
		appendConfiguredSeeds(hubSlug, uniqueBySlug, ordered, seenSlugs);

		final List<FranceTvHubItem> remaining = new ArrayList<FranceTvHubItem>();
		for (final FranceTvHubItem candidate : uniqueBySlug.values()) {
			if (seenSlugs.contains(candidate.getTaxonomySlug())) {
				continue;
			}
			remaining.add(candidate);
		}
		Collections.sort(remaining, LABEL_ORDER);
		for (final FranceTvHubItem candidate : remaining) {
			seenSlugs.add(candidate.getTaxonomySlug());
			ordered.add(candidate);
		}
		return ordered;
	}

	/** @deprecated use {@link #selectNavigableChildren(String, List)} */
	static List<FranceTvHubItem> selectConfiguredSeeds(final String hubSlug, final List<FranceTvHubItem> candidates) {
		return selectNavigableChildren(hubSlug, candidates);
	}

	private static void appendConfiguredSeeds(final String hubSlug, final Map<String, FranceTvHubItem> uniqueBySlug,
			final List<FranceTvHubItem> ordered, final Set<String> seenSlugs) {
		for (final String[] seed : FranceTvConf.PUBLIC_HUB_TAXONOMY_SEEDS) {
			if (seed.length < 3 || !hubSlug.equals(seed[0])) {
				continue;
			}
			final String taxonomySlug = seed[1];
			final String label = seed[2];
			if (!seenSlugs.add(taxonomySlug)) {
				continue;
			}
			final FranceTvHubItem candidate = uniqueBySlug.get(taxonomySlug);
			if (candidate != null) {
				ordered.add(withLabel(candidate, label));
			} else {
				ordered.add(buildFallbackSeed(hubSlug, taxonomySlug, label));
			}
		}
	}

	private static FranceTvHubItem buildFallbackSeed(final String hubSlug, final String taxonomySlug,
			final String label) {
		final String pageUrl = FranceTvUrls.programPageUrlFromTaxonomySlug(taxonomySlug);
		return new FranceTvHubItem(label, hubSlug, taxonomySlug, pageUrl, FranceTvHubItem.ItemType.UNKNOWN);
	}

	private static FranceTvHubItem withLabel(final FranceTvHubItem item, final String label) {
		if (item == null || label.equals(item.getLabel())) {
			return item;
		}
		return new FranceTvHubItem(label, item.getHubSlug(), item.getTaxonomySlug(), item.getPageUrl(),
				item.getItemType());
	}

	private static boolean isNavigableItemType(final FranceTvHubItem.ItemType itemType) {
		return itemType == FranceTvHubItem.ItemType.PROGRAM || itemType == FranceTvHubItem.ItemType.EVENT
				|| itemType == FranceTvHubItem.ItemType.COLLECTION;
	}

	private static FranceTvHubItem toHubItem(final String hubSlug, final Map<String, Object> rawItem) {
		if (rawItem == null) {
			return null;
		}
		final FranceTvHubItem.ItemType itemType = itemTypeFromRaw(String.valueOf(rawItem.get("type")));
		if (!isNavigableItemType(itemType)) {
			return null;
		}
		final String taxonomySlug = taxonomySlugFromItem(rawItem);
		if (StringUtils.isEmpty(taxonomySlug)) {
			return null;
		}
		final String label = itemLabel(rawItem);
		if (StringUtils.isEmpty(label)) {
			return null;
		}
		final String pageUrl = FranceTvUrls.programPageUrlFromTaxonomySlug(taxonomySlug);
		return new FranceTvHubItem(label, hubSlug, taxonomySlug, pageUrl, itemType);
	}

	static String taxonomySlugFromItem(final Map<String, Object> rawItem) {
		final Object programPath = rawItem.get("program_path");
		if (programPath != null && StringUtils.isNotEmpty(String.valueOf(programPath))) {
			return String.valueOf(programPath);
		}
		final Object urlComplete = rawItem.get("url_complete");
		if (urlComplete != null && StringUtils.isNotEmpty(String.valueOf(urlComplete))) {
			return String.valueOf(urlComplete);
		}
		final Object collectionPath = rawItem.get("collection_path");
		if (collectionPath != null && StringUtils.isNotEmpty(String.valueOf(collectionPath))) {
			final String path = String.valueOf(collectionPath);
			if (path.startsWith("collection_")) {
				return path.substring("collection_".length());
			}
			return path;
		}
		final Object path = rawItem.get("path");
		if (path != null && StringUtils.isNotEmpty(String.valueOf(path))) {
			return String.valueOf(path);
		}
		return null;
	}

	private static FranceTvHubItem.ItemType itemTypeFromRaw(final String type) {
		if ("program".equals(type)) {
			return FranceTvHubItem.ItemType.PROGRAM;
		}
		if ("event".equals(type)) {
			return FranceTvHubItem.ItemType.EVENT;
		}
		if ("collection".equals(type)) {
			return FranceTvHubItem.ItemType.COLLECTION;
		}
		return FranceTvHubItem.ItemType.UNKNOWN;
	}

	private static String itemLabel(final Map<String, Object> rawItem) {
		final Object label = rawItem.get("label");
		if (label != null && StringUtils.isNotEmpty(String.valueOf(label))) {
			return String.valueOf(label).trim();
		}
		final Object title = rawItem.get("title");
		if (title != null && StringUtils.isNotEmpty(String.valueOf(title))) {
			return String.valueOf(title).trim();
		}
		return null;
	}

	static final class HubDiscoveryResult {
		private final List<FranceTvHubItem> items;

		private final FranceTvPublicHubDiagnostics diagnostics;

		HubDiscoveryResult(final List<FranceTvHubItem> items, final FranceTvPublicHubDiagnostics diagnostics) {
			this.items = items == null ? Collections.<FranceTvHubItem>emptyList() : items;
			this.diagnostics = diagnostics;
		}

		List<FranceTvHubItem> getItems() {
			return items;
		}

		FranceTvPublicHubDiagnostics getDiagnostics() {
			return diagnostics;
		}
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> castItemList(final Object raw) {
		if (!(raw instanceof List)) {
			return Collections.emptyList();
		}
		final List<?> list = (List<?>) raw;
		final List<Map<String, Object>> items = new ArrayList<Map<String, Object>>(list.size());
		for (final Object entry : list) {
			if (entry instanceof Map) {
				items.add((Map<String, Object>) entry);
			}
		}
		return items;
	}

}
