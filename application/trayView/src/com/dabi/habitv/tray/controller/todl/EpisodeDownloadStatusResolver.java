package com.dabi.habitv.tray.controller.todl;

import java.util.Set;

import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.core.dao.DownloadedDAO;
import com.dabi.habitv.core.event.EpisodeStateEnum;

final class EpisodeDownloadStatusResolver {

	private EpisodeDownloadStatusResolver() {
	}

	static String resolve(final EpisodeDTO episode,
			final Set<String> downloadedEpisodeNames,
			final EpisodeStateEnum liveState) {
		if (episode == null) {
			return "Inconnu";
		}
		if (DownloadedDAO.containsEpisodeOrLegacyName(downloadedEpisodeNames,
				episode)) {
			return "Téléchargé";
		}
		if (liveState != null) {
			if (liveState == EpisodeStateEnum.DOWNLOAD_STARTING) {
				return "Téléchargement";
			}
			if (liveState == EpisodeStateEnum.TO_DOWNLOAD) {
				return "En file";
			}
			if (liveState.isInProgress()) {
				return "En cours";
			}
		}
		return "Disponible";
	}
}
