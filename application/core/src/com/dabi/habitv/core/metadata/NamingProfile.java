package com.dabi.habitv.core.metadata;

import java.util.regex.Pattern;

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

	/**
	 * Matches TokenReplacer syntax for {@code #MEDIA_SERVER_PATH#}: optional
	 * {@code _CUT} suffix and optional {@code §param} before the closing {@code #}.
	 * Does not match bare {@code MEDIA_SERVER_PATH} text without token delimiters.
	 */
	private static final Pattern MEDIA_SERVER_TOKEN_PATTERN = Pattern
			.compile("#MEDIA_SERVER_PATH(?:_CUT)?(?:§[^#]*)?#");

	public static NamingProfile fromDownloadOutputTemplate(final String template) {
		if (template != null && MEDIA_SERVER_TOKEN_PATTERN.matcher(template).find()) {
			return MEDIA_SERVER;
		}
		return LEGACY;
	}
}
