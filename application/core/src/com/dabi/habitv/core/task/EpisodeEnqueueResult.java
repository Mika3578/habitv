package com.dabi.habitv.core.task;

import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public final class EpisodeEnqueueResult {

	private final EpisodeDTO episode;
	private final TaskState state;
	private final EnqueueSkipReason skipReason;

	public EpisodeEnqueueResult(final EpisodeDTO episode,
			final TaskState state, final EnqueueSkipReason skipReason) {
		this.episode = episode;
		this.state = state;
		this.skipReason = skipReason;
	}

	public EpisodeDTO getEpisode() {
		return episode;
	}

	public TaskState getState() {
		return state;
	}

	public EnqueueSkipReason getSkipReason() {
		return skipReason;
	}

	public boolean wasAdded() {
		return state == TaskState.ADDED;
	}

	public boolean wasSkipped() {
		return skipReason != null;
	}
}
