package com.dabi.habitv.tray.controller.todl;

import java.util.Set;

import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.core.dao.DownloadedDAO;
import com.dabi.habitv.core.event.EpisodeStateEnum;

final class EpisodeDownloadStatusResolver {

	/** Semantic UI state: drives icon, accent color and primary action. */
	enum UiState {
		AVAILABLE, QUEUED, DOWNLOADING, DOWNLOADED, ERROR, UNKNOWN
	}

	private EpisodeDownloadStatusResolver() {
	}

	static String resolve(final EpisodeDTO episode,
			final Set<String> downloadedEpisodeNames,
			final EpisodeStateEnum liveState) {
		return label(resolveState(episode, downloadedEpisodeNames, liveState));
	}

	static UiState resolveState(final EpisodeDTO episode,
			final Set<String> downloadedEpisodeNames,
			final EpisodeStateEnum liveState) {
		if (episode == null) {
			return UiState.UNKNOWN;
		}
		if (downloadedEpisodeNames != null
				&& DownloadedDAO.containsEpisodeOrLegacyName(downloadedEpisodeNames,
						episode)) {
			return UiState.DOWNLOADED;
		}
		if (liveState != null) {
			if (liveState == EpisodeStateEnum.DOWNLOAD_STARTING) {
				return UiState.DOWNLOADING;
			}
			if (liveState == EpisodeStateEnum.TO_DOWNLOAD) {
				return UiState.QUEUED;
			}
			if (liveState.hasFailed()) {
				return UiState.ERROR;
			}
			if (liveState.isInProgress()) {
				return UiState.DOWNLOADING;
			}
			if (liveState == EpisodeStateEnum.READY) {
				return UiState.DOWNLOADED;
			}
		}
		return UiState.AVAILABLE;
	}

	static String label(final UiState state) {
		switch (state) {
		case AVAILABLE:
			return "Disponible";
		case QUEUED:
			return "En file";
		case DOWNLOADING:
			return "En cours";
		case DOWNLOADED:
			return "Téléchargé";
		case ERROR:
			return "Erreur";
		default:
			return "Inconnu";
		}
	}
}
