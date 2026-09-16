package com.dabi.habitv.core.metadata;

import java.util.Date;

import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO;

/**
 * Resolves canonical episode metadata for naming.
 * <p>
 * Precedence: explicit structured provider metadata, then already-populated
 * legacy {@link EpisodeDTO} date/duration/title fields. Never maps
 * {@code category.name} to series title unless a provider already set
 * {@link EpisodeMetadataDTO#getSeriesTitle()}.
 * <p>
 * Legacy {@link EpisodeDTO#getEpisodeDate()} is only promoted to
 * {@link EpisodeMetadataDTO#getAirDate()} when the provider did not already set
 * a {@link EpisodeMetadataDTO#getPublicationDate()} (publication must not become
 * a fake broadcast date for MEDIA_SERVER dated paths).
 */
public final class EpisodeMetadataResolver {

	private EpisodeMetadataResolver() {
	}

	public static EpisodeMetadataDTO resolve(final EpisodeDTO episode) {
		final EpisodeMetadataDTO resolved = new EpisodeMetadataDTO();
		if (episode == null) {
			return resolved;
		}

		final EpisodeMetadataDTO explicit = episode.getMetadata();
		if (explicit != null) {
			copyNonNull(explicit, resolved);
		}

		if (resolved.getEpisodeTitle() == null && episode.getName() != null
				&& !episode.getName().trim().isEmpty()) {
			resolved.setEpisodeTitle(episode.getName().trim());
		}
		if (resolved.getAirDate() == null && resolved.getPublicationDate() == null
				&& episode.getEpisodeDate() != null) {
			resolved.setAirDate(episode.getEpisodeDate());
		}
		if (resolved.getDurationSeconds() == null && episode.getDurationSeconds() != null) {
			resolved.setDurationSeconds(episode.getDurationSeconds());
		}
		if (resolved.getSourceUrl() == null && episode.getId() != null
				&& !episode.getId().trim().isEmpty()) {
			resolved.setSourceUrl(episode.getId().trim());
		}
		return resolved;
	}

	private static void copyNonNull(final EpisodeMetadataDTO from, final EpisodeMetadataDTO to) {
		if (from.getSeriesTitle() != null) {
			to.setSeriesTitle(from.getSeriesTitle());
		}
		if (from.getEpisodeTitle() != null) {
			to.setEpisodeTitle(from.getEpisodeTitle());
		}
		if (from.getSeasonNumber() != null) {
			to.setSeasonNumber(from.getSeasonNumber());
		}
		if (from.getEpisodeNumber() != null) {
			to.setEpisodeNumber(from.getEpisodeNumber());
		}
		final Date airDate = from.getAirDate();
		if (airDate != null) {
			to.setAirDate(airDate);
		}
		final Date publicationDate = from.getPublicationDate();
		if (publicationDate != null) {
			to.setPublicationDate(publicationDate);
		}
		if (from.getDurationSeconds() != null) {
			to.setDurationSeconds(from.getDurationSeconds());
		}
		if (from.getDescription() != null) {
			to.setDescription(from.getDescription());
		}
		if (from.getThumbnailUrl() != null) {
			to.setThumbnailUrl(from.getThumbnailUrl());
		}
		if (from.getChannel() != null) {
			to.setChannel(from.getChannel());
		}
		if (from.getContentLanguage() != null) {
			to.setContentLanguage(from.getContentLanguage());
		}
		if (from.getProviderEpisodeId() != null) {
			to.setProviderEpisodeId(from.getProviderEpisodeId());
		}
		if (from.getSourceUrl() != null) {
			to.setSourceUrl(from.getSourceUrl());
		}
	}
}
