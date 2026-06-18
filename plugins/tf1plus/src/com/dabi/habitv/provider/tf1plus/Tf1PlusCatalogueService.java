package com.dabi.habitv.provider.tf1plus;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;

/**
 * Orchestrates TF1+ catalogue refresh, cache usage, and category tree building.
 */
final class Tf1PlusCatalogueService {

	private static final Logger LOG = Logger.getLogger(Tf1PlusCatalogueService.class);

	private final Tf1PlusCatalogueClient catalogueClient;
	private final Tf1PlusCatalogueCache catalogueCache;
	private final Tf1PlusTreeBuilder treeBuilder;
	private final Tf1PlusHtmlCatalogueSupplement htmlSupplement;
	private final AtomicBoolean refreshInProgress = new AtomicBoolean(false);

	Tf1PlusCatalogueService(final BasePluginWithProxy plugin) {
		this(new Tf1PlusCatalogueClient(plugin), new Tf1PlusCatalogueCache(), new Tf1PlusTreeBuilder(),
				new Tf1PlusHtmlCatalogueSupplement(new Tf1PlusHtmlCatalogueSupplement.UrlContentProvider() {
					@Override
					public String getUrlContent(final String url) {
						return "";
					}
				}));
	}

	Tf1PlusCatalogueService(final Tf1PlusCatalogueClient catalogueClient, final Tf1PlusCatalogueCache catalogueCache,
			final Tf1PlusTreeBuilder treeBuilder, final Tf1PlusHtmlCatalogueSupplement htmlSupplement) {
		this.catalogueClient = catalogueClient;
		this.catalogueCache = catalogueCache;
		this.treeBuilder = treeBuilder;
		this.htmlSupplement = htmlSupplement;
	}

	java.util.Set<CategoryDTO> buildCategoryTree() {
		final long ttlMillis = catalogueCache.resolveTtlMillis();
		final Tf1PlusCatalogueCache.Snapshot snapshot = catalogueCache.load();
		if (snapshot.isValid() && snapshot.isFresh(ttlMillis)) {
			LOG.debug("TF1+ catalogue cache hit (fresh)");
			scheduleRefreshIfStale(snapshot, ttlMillis, false);
			return treeBuilder.buildTree(snapshot.getEntries());
		}
		if (snapshot.isValid() && snapshot.isStale(ttlMillis)) {
			LOG.info("TF1+ catalogue cache hit (stale); scheduling background refresh");
			scheduleRefreshIfStale(snapshot, ttlMillis, true);
			return treeBuilder.buildTree(snapshot.getEntries());
		}

		final Tf1PlusCatalogueRefreshResult refreshResult = refreshLiveCatalogue();
		if (refreshResult.entriesView().isEmpty()) {
			if (snapshot.isValid()) {
				LOG.warn(Tf1PlusConf.USER_MESSAGE_CATALOGUE_CACHED);
				return treeBuilder.buildTree(snapshot.getEntries());
			}
			LOG.warn(Tf1PlusConf.USER_MESSAGE_CATALOGUE_UNAVAILABLE);
			return treeBuilder.buildTree(Collections.<Tf1PlusCatalogueEntry>emptyList());
		}
		return treeBuilder.buildTree(refreshResult.entriesView());
	}

	void scheduleRefreshIfStale(final Tf1PlusCatalogueCache.Snapshot snapshot, final long ttlMillis,
			final boolean alreadyKnownStale) {
		if (!alreadyKnownStale && !snapshot.isStale(ttlMillis)) {
			return;
		}
		triggerBackgroundRefresh();
	}

	void triggerBackgroundRefresh() {
		if (!refreshInProgress.compareAndSet(false, true)) {
			return;
		}
		final Thread refreshThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					refreshLiveCatalogue();
				} finally {
					refreshInProgress.set(false);
				}
			}
		}, "tf1plus-catalogue-refresh");
		refreshThread.setDaemon(true);
		refreshThread.start();
	}

	Tf1PlusCatalogueRefreshResult refreshLiveCatalogue() {
		final Tf1PlusCatalogueRefreshResult result = new Tf1PlusCatalogueRefreshResult();
		try {
			for (final Tf1PlusHubDescriptor hub : Tf1PlusHubRegistry.enabledHubs()) {
				if (!hub.isEnabled()) {
					continue;
				}
				final int beforeCount = result.entriesView().size();
				try {
					catalogueClient.fetchAllProgrammesForHub(hub, result);
				} catch (IOException hubFailure) {
					result.incrementProgrammesRejected("hub-fetch-failed");
					result.addHubSummary(hub.getHubId() + "|error=" + hubFailure.getMessage());
					LOG.debug("TF1+ catalogue hub refresh failed for " + hub.getHubId() + ": " + hubFailure.getMessage());
				}
				if (htmlSupplement != null && result.entriesView().size() == beforeCount) {
					for (final Tf1PlusCatalogueEntry htmlEntry : htmlSupplement.discoverProgrammes(hub)) {
						final String key = htmlEntry.deduplicationKey();
						if (key.isEmpty()) {
							result.incrementProgrammesRejected("html-missing-slug");
							continue;
						}
						boolean duplicate = false;
						for (final Tf1PlusCatalogueEntry existing : result.entriesView()) {
							if (key.equals(existing.deduplicationKey())) {
								duplicate = true;
								break;
							}
						}
						if (duplicate) {
							result.incrementDuplicatesSkipped();
							continue;
						}
						result.incrementProgrammesAdded();
						result.addEntry(htmlEntry);
					}
				}
			}
			if (!result.entriesView().isEmpty()) {
				try {
					catalogueCache.save(result.entriesView());
					result.setRefreshSucceeded(true);
				} catch (IOException cacheFailure) {
					result.setFailureRootCause("cache-write-failed");
					LOG.debug("TF1+ catalogue cache write failed: " + cacheFailure.getMessage());
				}
			} else {
				result.setFailureRootCause("no-catalogue-entries");
			}
		} catch (RuntimeException e) {
			result.setFailureRootCause("catalogue-refresh-failed");
			LOG.debug("TF1+ catalogue refresh failed: " + e.getMessage());
		} finally {
			result.markFinished();
			LOG.info(result.formatLogLine());
		}
		return result;
	}

}
