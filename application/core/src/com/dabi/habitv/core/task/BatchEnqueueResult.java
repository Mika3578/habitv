package com.dabi.habitv.core.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class BatchEnqueueResult {

	private final List<EpisodeEnqueueResult> results;

	public BatchEnqueueResult(final List<EpisodeEnqueueResult> results) {
		Objects.requireNonNull(results, "results must not be null");
		this.results = Collections.unmodifiableList(new ArrayList<>(results));
	}

	public List<EpisodeEnqueueResult> getResults() {
		return results;
	}

	public int getAddedCount() {
		int count = 0;
		for (final EpisodeEnqueueResult result : results) {
			if (result.wasAdded()) {
				count++;
			}
		}
		return count;
	}

	public int getSkippedCount() {
		return results.size() - getAddedCount();
	}
}
