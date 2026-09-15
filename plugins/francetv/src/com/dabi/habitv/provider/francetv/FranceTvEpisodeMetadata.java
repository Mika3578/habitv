package com.dabi.habitv.provider.francetv;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO;

/**
 * Maps france.tv mobile API content items to {@link EpisodeDTO} optional
 * metadata and canonical {@link EpisodeMetadataDTO}.
 * <p>
 * FranceTV program categories are treated as series/show titles. Season is
 * taken from the API when present; episode numbers are left absent unless the
 * API supplies a reliable value (never invented from title text).
 */
final class FranceTvEpisodeMetadata {

	private FranceTvEpisodeMetadata() {
	}

	static void apply(final Map<String, Object> item, final EpisodeDTO episode) {
		apply(item, episode, null);
	}

	static void apply(final Map<String, Object> item, final EpisodeDTO episode,
			final String programPath) {
		if (item == null || episode == null) {
			return;
		}
		final Date broadcastDate = parseBroadcastDate(item);
		if (broadcastDate != null) {
			episode.setEpisodeDate(broadcastDate);
		}
		final Long duration = parseDurationSeconds(item);
		if (duration != null) {
			episode.setDurationSeconds(duration);
		}

		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		final CategoryDTO category = episode.getCategory();
		if (category != null && StringUtils.isNotEmpty(category.getName())) {
			// FranceTV leaf program categories are show/program names.
			metadata.setSeriesTitle(category.getName().trim());
		}
		if (StringUtils.isNotEmpty(episode.getName())) {
			metadata.setEpisodeTitle(episode.getName().trim());
		}
		if (broadcastDate != null) {
			metadata.setAirDate(broadcastDate);
		}
		if (duration != null) {
			metadata.setDurationSeconds(duration);
		}
		final Integer season = parsePositiveNumber(item.get("season"));
		if (season != null) {
			metadata.setSeasonNumber(season);
		}
		final Integer episodeNumber = firstPositiveNumber(item, "episode_number", "episode");
		if (episodeNumber != null) {
			metadata.setEpisodeNumber(episodeNumber);
		}
		final String providerId = stringValue(item.get("id"));
		if (providerId != null) {
			metadata.setProviderEpisodeId(providerId);
		}
		if (StringUtils.isNotEmpty(episode.getId())) {
			metadata.setSourceUrl(episode.getId());
		}
		final String channel = channelFromProgramPath(programPath);
		if (channel != null) {
			metadata.setChannel(channel);
		}
		episode.setMetadata(metadata);
	}

	static Date parseBroadcastDate(final Map<String, Object> item) {
		final Long seconds = firstEpochSeconds(item, "broadcast_begin_date", "begin_date");
		if (seconds == null) {
			return null;
		}
		return new Date(seconds.longValue() * 1000L);
	}

	static Long parseDurationSeconds(final Map<String, Object> item) {
		final Object raw = item.get("duration");
		if (raw instanceof Number) {
			final long value = ((Number) raw).longValue();
			return value >= 0 ? Long.valueOf(value) : null;
		}
		return null;
	}

	private static Long firstEpochSeconds(final Map<String, Object> item, final String... keys) {
		for (final String key : keys) {
			final Object raw = item.get(key);
			if (raw instanceof Number) {
				final long value = ((Number) raw).longValue();
				if (value > 0) {
					return Long.valueOf(value);
				}
			}
		}
		return null;
	}

	private static Integer firstPositiveNumber(final Map<String, Object> item, final String... keys) {
		for (final String key : keys) {
			final Integer value = parsePositiveNumber(item.get(key));
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	private static Integer parsePositiveNumber(final Object raw) {
		if (!(raw instanceof Number)) {
			return null;
		}
		final int value = ((Number) raw).intValue();
		return value > 0 ? Integer.valueOf(value) : null;
	}

	private static String stringValue(final Object raw) {
		if (raw == null) {
			return null;
		}
		final String value = String.valueOf(raw).trim();
		return value.isEmpty() ? null : value;
	}

	private static String channelFromProgramPath(final String programPath) {
		if (StringUtils.isEmpty(programPath)) {
			return null;
		}
		final int separator = programPath.indexOf('_');
		if (separator <= 0) {
			return null;
		}
		return programPath.substring(0, separator);
	}
}
