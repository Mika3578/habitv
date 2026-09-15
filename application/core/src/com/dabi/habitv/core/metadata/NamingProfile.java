package com.dabi.habitv.core.metadata;

/**
 * Output naming profile.
 * <p>
 * {@link #LEGACY} keeps the configured token template unchanged.
 * {@link #MEDIA_SERVER} builds Plex/Jellyfin/Kodi-friendly relative paths from
 * canonical {@link com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO}.
 */
public enum NamingProfile {
	LEGACY,
	MEDIA_SERVER;

	public static final String MEDIA_SERVER_TOKEN = "#MEDIA_SERVER_PATH#";

	public static NamingProfile fromDownloadOutputTemplate(final String template) {
		if (template != null && template.contains(MEDIA_SERVER_TOKEN)) {
			return MEDIA_SERVER;
		}
		return LEGACY;
	}
}
