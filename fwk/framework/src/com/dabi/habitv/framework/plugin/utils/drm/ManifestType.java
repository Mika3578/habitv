package com.dabi.habitv.framework.plugin.utils.drm;

/**
 * Type of streaming manifest passed to {@link DRMManifestClassifier}.
 */
public enum ManifestType {
	DASH_MPD,
	HLS_M3U8,
	UNKNOWN
}
