package com.dabi.habitv.core.event;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum EpisodeStateEnum {
	BUILD_INDEX, TO_DOWNLOAD, DOWNLOAD_STARTING, DOWNLOADED, READY, DOWNLOAD_FAILED, EXPORT_STARTING, EXPORT_FAILED, FAILED, TO_EXPORT, STOPPED, TO_MANY_FAILED,
	// Official DRM/CDM boundary classifications. None of these states imply
	// that Habitv decrypted, bypassed, or otherwise circumvented DRM.
	DRM_REQUIRED_OFFICIAL_PLAYBACK_ONLY,
	DRM_REQUIRED_OFFICIAL_INTEGRATION_MISSING,
	DRM_SUPPORTED_BY_OFFICIAL_INTEGRATION,
	DIRECT_DOWNLOAD_UNSUPPORTED_DRM,
	METADATA_ONLY_DRM_PROTECTED,
	PROVIDER_REQUIRES_AUTH,
	PROVIDER_REGION_LOCKED,
	PROVIDER_BROKEN_OR_OBSOLETE;

	public static final Set<EpisodeStateEnum> IN_PROGRESS = new HashSet<>(
			Arrays.asList(DOWNLOAD_STARTING, EXPORT_STARTING, DOWNLOADED,
					TO_DOWNLOAD, TO_EXPORT));

	public static final Set<EpisodeStateEnum> HAS_FAILED = new HashSet<>(
			Arrays.asList(DOWNLOAD_FAILED, EXPORT_FAILED, FAILED, STOPPED,
					TO_MANY_FAILED));

	public static final Set<EpisodeStateEnum> IS_EXPORT = new HashSet<>(
			Arrays.asList(EXPORT_STARTING, EXPORT_FAILED, FAILED, TO_DOWNLOAD,
					TO_EXPORT));

	/**
	 * Terminal-but-not-failed states triggered by the official DRM/CDM
	 * boundary. Items in these states must not be retried by the download
	 * queue, but they also are not classified as download failures.
	 */
	public static final Set<EpisodeStateEnum> DRM_BLOCKED = new HashSet<>(
			Arrays.asList(DRM_REQUIRED_OFFICIAL_PLAYBACK_ONLY,
					DRM_REQUIRED_OFFICIAL_INTEGRATION_MISSING,
					DIRECT_DOWNLOAD_UNSUPPORTED_DRM,
					METADATA_ONLY_DRM_PROTECTED,
					PROVIDER_REQUIRES_AUTH,
					PROVIDER_REGION_LOCKED,
					PROVIDER_BROKEN_OR_OBSOLETE));

	public boolean isInProgress() {
		return IN_PROGRESS.contains(this);
	}

	public boolean hasFailed() {
		return HAS_FAILED.contains(this);
	}

	public boolean isExport() {
		return IS_EXPORT.contains(this);
	}

	public boolean isDrmBlocked() {
		return DRM_BLOCKED.contains(this);
	}

	public boolean isDone() {
		return hasFailed() || this == EpisodeStateEnum.READY || isDrmBlocked();
	}
}
