package com.dabi.habitv.api.plugin.dto;

/**
 * Classification of provider/episode content with respect to official
 * DRM/CDM support boundaries.
 *
 * <p>These values are used only to <em>classify</em> content and to drive
 * UI / queue behavior. They never imply that Habitv decrypts protected
 * media, extracts keys, emulates a CDM, or otherwise bypasses DRM.
 */
public enum ProtectedContentStatus {

	/** Direct (non-DRM) download is supported by Habitv. */
	DIRECT_DOWNLOAD_SUPPORTED,

	/** Content is DRM-protected; only official playback (provider site / app / EME) is allowed. */
	OFFICIAL_PLAYBACK_ONLY,

	/** An official provider-authorized DRM/CDM integration exists and is available. */
	OFFICIAL_DRM_INTEGRATION_AVAILABLE,

	/** An official integration would be required, but none is configured or available. */
	OFFICIAL_DRM_INTEGRATION_REQUIRED,

	/** Public metadata is exposed; the media itself is DRM-protected and not downloadable. */
	METADATA_ONLY_DRM_PROTECTED,

	/** Provider requires authentication that Habitv does not currently negotiate. */
	AUTH_REQUIRED_UNSUPPORTED,

	/** Provider enforces region locking that prevents access from the current environment. */
	REGION_LOCKED_UNSUPPORTED,

	/** Provider endpoint or parser is broken / obsolete and no replacement is wired up. */
	BROKEN_OR_OBSOLETE;
}
