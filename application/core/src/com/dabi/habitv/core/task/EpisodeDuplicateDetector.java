package com.dabi.habitv.core.task;

import java.util.HashSet;
import java.util.Set;

import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public final class EpisodeDuplicateDetector {

	private final Set<EpisodeIdentity> seenInBatch = new HashSet<>();

	public EnqueueSkipReason detectSkipReason(final EpisodeDTO episode,
			final boolean alreadyDownloaded, final boolean alreadyQueued,
			final boolean alreadyDownloading) {
		if (alreadyDownloaded) {
			return EnqueueSkipReason.ALREADY_DOWNLOADED;
		}
		if (alreadyDownloading) {
			return EnqueueSkipReason.ALREADY_DOWNLOADING;
		}
		if (alreadyQueued) {
			return EnqueueSkipReason.ALREADY_QUEUED;
		}
		final EpisodeIdentity identity = EpisodeIdentity.fromEpisode(episode);
		if (identity != null && !seenInBatch.add(identity)) {
			return EnqueueSkipReason.DUPLICATE_IN_BATCH;
		}
		return null;
	}

	public void resetBatch() {
		seenInBatch.clear();
	}
}
