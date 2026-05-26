package com.dabi.habitv.provider.francetv;

import java.util.Date;
import java.util.Map;

import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

/**
 * Maps france.tv mobile API content items to {@link EpisodeDTO} optional metadata.
 */
final class FranceTvEpisodeMetadata {

	private FranceTvEpisodeMetadata() {
	}

	static void apply(final Map<String, Object> item, final EpisodeDTO episode) {
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
}
