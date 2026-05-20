package com.dabi.habitv.tray.controller.todl;

import java.util.Set;

import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.core.event.EpisodeStateEnum;

final class EpisodeDownloadStatusResolver {

	private EpisodeDownloadStatusResolver() {
	}

	static String resolve(final EpisodeDTO episode,
			final Set<String> downloadedEpisodeNames,
			final EpisodeStateEnum liveState) {
		if (episode == null) {
			return "Unknown";
		}
		if (downloadedEpisodeNames != null
				&& downloadedEpisodeNames.contains(episode.getName())) {
			return "Downloaded";
		}
		if (liveState != null) {
			if (liveState == EpisodeStateEnum.DOWNLOAD_STARTING) {
				return "Downloading";
			}
			if (liveState == EpisodeStateEnum.TO_DOWNLOAD) {
				return "Queued";
			}
			if (liveState.isInProgress()) {
				return "In progress";
			}
		}
		return "Available";
	}
}
