package com.dabi.habitv.core.metadata;

/**
 * Output naming profile.
 * <p>
 * {@link #LEGACY} keeps a free-form token template.
 * {@link #MEDIA_SERVER} builds Plex/Jellyfin/Kodi-friendly relative paths from
 * canonical {@link com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO}. New user
 * configs default to a template containing {@link #MEDIA_SERVER_TOKEN}.
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
