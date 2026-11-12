package com.dabi.habitv.tray.controller.todl;

import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

final class EpisodeRows {

	private EpisodeRows() {
	}

	/** The loading placeholder row has no category and must not offer episode actions. */
	static boolean isActionable(final EpisodeDTO episode) {
		return episode != null && episode.getCategory() != null;
	}
}
