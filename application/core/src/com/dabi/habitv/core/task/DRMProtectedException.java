package com.dabi.habitv.core.task;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.core.event.EpisodeStateEnum;

/**
 * Signals that the direct downloader was skipped for an episode because it is
 * classified as DRM-protected (or otherwise unsupported for direct download
 * under Habitv's official DRM/CDM boundary).
 *
 * <p>This is not a download failure. {@link DownloadTask} translates it into
 * the appropriate {@link EpisodeStateEnum} so the UI / queue can show why
 * direct download was skipped and which official playback URL (if any) is
 * available.
 */
public final class DRMProtectedException extends DownloadFailedException {

	private static final long serialVersionUID = 1L;

	private final EpisodeStateEnum state;
	private final String statusMessage;
	private final String officialPlaybackUrl;

	public DRMProtectedException(final EpisodeStateEnum state,
			final String statusMessage,
			final String officialPlaybackUrl) {
		super(buildMessage(state, statusMessage, officialPlaybackUrl));
		this.state = state;
		this.statusMessage = statusMessage;
		this.officialPlaybackUrl = officialPlaybackUrl;
	}

	private static String buildMessage(final EpisodeStateEnum state,
			final String statusMessage,
			final String officialPlaybackUrl) {
		final StringBuilder sb = new StringBuilder();
		sb.append("DRM-protected; direct download skipped (").append(state).append(")");
		if (statusMessage != null && !statusMessage.isEmpty()) {
			sb.append(": ").append(statusMessage);
		}
		if (officialPlaybackUrl != null && !officialPlaybackUrl.isEmpty()) {
			sb.append(" — official playback URL: ").append(officialPlaybackUrl);
		}
		return sb.toString();
	}

	public EpisodeStateEnum getState() {
		return state;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public String getOfficialPlaybackUrl() {
		return officialPlaybackUrl;
	}
}
