package com.dabi.habitv.core.metadata;

import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO;
import com.dabi.habitv.core.token.TokenReplacer;

/**
 * Resolves the final download output path from the configured template and
 * naming profile.
 * <p>
 * Prefer {@link MediaNamingService#resolveOutputPath(String, EpisodeDTO)} as the
 * public entry point; this class remains the profile implementation detail.
 */
public final class EpisodeOutputPathResolver {

	private EpisodeOutputPathResolver() {
	}

	public static String resolve(final String downloadOutputTemplate, final EpisodeDTO episode) {
		final String template = downloadOutputTemplate == null ? "" : downloadOutputTemplate;
		final NamingProfile profile = NamingProfile.fromDownloadOutputTemplate(template);
		if (profile == NamingProfile.MEDIA_SERVER) {
			return resolveMediaServerTemplate(template, episode);
		}
		return TokenReplacer.replaceAll(template, episode);
	}

	private static String resolveMediaServerTemplate(final String template, final EpisodeDTO episode) {
		final EpisodeMetadataDTO metadata = EpisodeMetadataResolver.resolve(episode);
		final String extension = episode != null && episode.getCategory() != null
				? episode.getCategory().getExtension()
				: null;
		final String mediaPath = MediaServerNamingPolicy.buildRelativePath(metadata, extension);
		return TokenReplacer.replaceAll(
				template.replace(NamingProfile.MEDIA_SERVER_TOKEN, mediaPath), episode);
	}
}
