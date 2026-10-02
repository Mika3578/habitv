package com.dabi.habitv.provider.tf1plus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sanitized diagnostics for a TF1+ catalogue refresh operation.
 */
final class Tf1PlusCatalogueRefreshResult {

	private final long startedAtMillis;
	private long finishedAtMillis;
	private boolean cacheHit;
	private boolean cacheStale;
	private boolean refreshSucceeded;
	private boolean usedStaleCacheOnFailure;
	private String failureRootCause = "";
	private int programmesFetched;
	private int programmesAdded;
	private int duplicatesSkipped;
	private int programmesRejected;
	private int unknownRubricCount;
	private boolean suspectedApiCapReached;
	private final Map<String, Integer> rejectionReasons = new LinkedHashMap<String, Integer>();
	private final List<String> hubSummaries = new ArrayList<String>();

	Tf1PlusCatalogueRefreshResult() {
		this.startedAtMillis = System.currentTimeMillis();
	}

	void markFinished() {
		finishedAtMillis = System.currentTimeMillis();
	}

	void setCacheHit(final boolean cacheHit) {
		this.cacheHit = cacheHit;
	}

	void setCacheStale(final boolean cacheStale) {
		this.cacheStale = cacheStale;
	}

	void setRefreshSucceeded(final boolean refreshSucceeded) {
		this.refreshSucceeded = refreshSucceeded;
	}

	void setUsedStaleCacheOnFailure(final boolean usedStaleCacheOnFailure) {
		this.usedStaleCacheOnFailure = usedStaleCacheOnFailure;
	}

	void setFailureRootCause(final String failureRootCause) {
		this.failureRootCause = failureRootCause == null ? "" : failureRootCause;
	}

	void addProgrammesFetched(final int count) {
		programmesFetched += count;
	}

	void incrementProgrammesAdded() {
		programmesAdded++;
	}

	void incrementDuplicatesSkipped() {
		duplicatesSkipped++;
	}

	void incrementProgrammesRejected(final String reason) {
		programmesRejected++;
		final String key = reason == null || reason.isEmpty() ? "unknown" : reason;
		final Integer current = rejectionReasons.get(key);
		rejectionReasons.put(key, Integer.valueOf(current == null ? 1 : current.intValue() + 1));
	}

	void incrementUnknownRubricCount() {
		unknownRubricCount++;
	}

	void setSuspectedApiCapReached(final boolean suspectedApiCapReached) {
		this.suspectedApiCapReached = suspectedApiCapReached;
	}

	void addHubSummary(final String summary) {
		if (summary != null && !summary.isEmpty()) {
			hubSummaries.add(summary);
		}
	}

	List<Tf1PlusCatalogueEntry> getEntries() {
		return entries;
	}

	private final List<Tf1PlusCatalogueEntry> entries = new ArrayList<Tf1PlusCatalogueEntry>();

	void addEntry(final Tf1PlusCatalogueEntry entry) {
		entries.add(entry);
	}

	List<Tf1PlusCatalogueEntry> entriesView() {
		return Collections.unmodifiableList(entries);
	}

	long getDurationMillis() {
		final long end = finishedAtMillis > 0L ? finishedAtMillis : System.currentTimeMillis();
		return end - startedAtMillis;
	}

	String formatLogLine() {
		final StringBuilder builder = new StringBuilder();
		builder.append("provider=").append(Tf1PlusConf.DISPLAY_NAME);
		builder.append(" event=catalogue-refresh");
		builder.append(" cacheHit=").append(cacheHit);
		builder.append(" cacheStale=").append(cacheStale);
		builder.append(" refreshSucceeded=").append(refreshSucceeded);
		builder.append(" usedStaleCacheOnFailure=").append(usedStaleCacheOnFailure);
		builder.append(" programmesFetched=").append(programmesFetched);
		builder.append(" programmesAdded=").append(programmesAdded);
		builder.append(" duplicatesSkipped=").append(duplicatesSkipped);
		builder.append(" programmesRejected=").append(programmesRejected);
		builder.append(" unknownRubricCount=").append(unknownRubricCount);
		builder.append(" suspectedApiCapReached=").append(suspectedApiCapReached);
		builder.append(" durationMs=").append(getDurationMillis());
		if (!failureRootCause.isEmpty()) {
			builder.append(" rootCause=").append(failureRootCause);
		}
		if (!rejectionReasons.isEmpty()) {
			builder.append(" rejectionSummary=").append(rejectionReasons);
		}
		for (final String hubSummary : hubSummaries) {
			builder.append(" hub=").append(hubSummary);
		}
		builder.append(" cookiesEnabled=false");
		return builder.toString();
	}

}
