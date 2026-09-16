package com.dabi.habitv.core.metadata;

import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO;
import com.dabi.habitv.core.token.TokenReplacer;

/**
 * Single entry point for Habitv output naming.
 * <p>
 * <strong>Global ownership:</strong> providers only populate
 * {@link EpisodeMetadataDTO} facts. This service (and the policies it calls)
 * owns all filename / directory decisions. Provider-specific path builders must
 * not be added under {@code plugins/}.
 * <p>
 * Profiles:
 * <ul>
 * <li>{@link NamingProfile#LEGACY} — configured token template only</li>
 * <li>{@link NamingProfile#MEDIA_SERVER} — canonical interoperable paths</li>
 * </ul>
 */
public final class MediaNamingService {

	private MediaNamingService() {
	}

	/**
	 * Resolve the download output path for an episode.
	 *
	 * @param downloadOutputTemplate user {@code downloadOuput} template
	 * @param episode episode (metadata optional)
	 * @return absolute or relative path after token / profile expansion
	 */
	public static String resolveOutputPath(final String downloadOutputTemplate,
			final EpisodeDTO episode) {
		return EpisodeOutputPathResolver.resolve(downloadOutputTemplate, episode);
	}

	/**
	 * Resolve canonical metadata used for semantic tokens and MEDIA_SERVER paths.
	 */
	public static EpisodeMetadataDTO resolveMetadata(final EpisodeDTO episode) {
		return EpisodeMetadataResolver.resolve(episode);
	}

	/**
	 * Filename-oriented episode title after global normalization.
	 */
	public static String filenameEpisodeTitle(final EpisodeDTO episode) {
		return EpisodeTitleNormalizer.forFilename(resolveMetadata(episode));
	}

	/**
	 * Expand a free-form token template (LEGACY semantics for historic tokens).
	 */
	public static String expandTokens(final String template, final EpisodeDTO episode) {
		return TokenReplacer.replaceAll(template == null ? "" : template, episode);
	}
}
