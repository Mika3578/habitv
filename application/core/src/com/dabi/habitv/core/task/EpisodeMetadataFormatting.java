package com.dabi.habitv.core.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.framework.plugin.utils.DownloadUtils;

public final class EpisodeMetadataFormatting {

	private static final String UNKNOWN = "Inconnu";
	private static final int MAX_SHORT_URL_LENGTH = 48;
	private static final String PROGRAM_URL_PARAM = "PROGRAM_URL";

	private EpisodeMetadataFormatting() {
	}

	public static String formatDate(final Date date) {
		if (date == null) {
			return UNKNOWN;
		}
		return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(date);
	}

	public static String formatDuration(final Long durationSeconds) {
		if (durationSeconds == null || durationSeconds.longValue() < 0) {
			return UNKNOWN;
		}
		final long total = durationSeconds.longValue();
		final long hours = total / 3600;
		final long minutes = (total % 3600) / 60;
		final long seconds = total % 60;
		if (hours > 0) {
			return String.format(Locale.ENGLISH, "%d:%02d:%02d", Long.valueOf(hours),
					Long.valueOf(minutes), Long.valueOf(seconds));
		}
		return String.format(Locale.ENGLISH, "%d:%02d", Long.valueOf(minutes),
				Long.valueOf(seconds));
	}

	public static String formatSize(final Long sizeBytes) {
		if (sizeBytes == null || sizeBytes.longValue() < 0) {
			return UNKNOWN;
		}
		final long bytes = sizeBytes.longValue();
		if (bytes < 1024) {
			return bytes + " B";
		}
		if (bytes < 1024 * 1024) {
			return String.format(Locale.ENGLISH, "%.1f KB", Double.valueOf(bytes / 1024.0));
		}
		if (bytes < 1024L * 1024L * 1024L) {
			return String.format(Locale.ENGLISH, "%.1f MB",
					Double.valueOf(bytes / (1024.0 * 1024.0)));
		}
		return String.format(Locale.ENGLISH, "%.1f GB",
				Double.valueOf(bytes / (1024.0 * 1024.0 * 1024.0)));
	}

	/**
	 * Program (emission) page URL from the episode category, when the provider
	 * stores an HTTP(S) identifier on the downloadable category node or in
	 * {@link #PROGRAM_URL_PARAM}.
	 */
	public static String programPageUrl(final EpisodeDTO episode) {
		if (episode == null || episode.getCategory() == null) {
			return null;
		}
		final CategoryDTO category = episode.getCategory();
		final String explicitUrl = trimToHttpUrl(category.getParameter(PROGRAM_URL_PARAM));
		if (explicitUrl != null) {
			return explicitUrl;
		}
		return trimToHttpUrl(category.getId());
	}

	public static String formatProgramLinkLabel(final EpisodeDTO episode) {
		if (episode != null && episode.getCategory() != null
				&& episode.getCategory().getName() != null) {
			final String trimmedCategoryName = episode.getCategory().getName().trim();
			if (!trimmedCategoryName.isEmpty()) {
				return trimmedCategoryName;
			}
		}
		final String url = programPageUrl(episode);
		if (url == null) {
			return UNKNOWN;
		}
		return shortenUrl(url);
	}

	public static String formatSource(final EpisodeDTO episode) {
		final String programUrl = programPageUrl(episode);
		if (programUrl != null) {
			return programUrl;
		}
		if (episode == null || episode.getId() == null
				|| episode.getId().trim().isEmpty()) {
			return UNKNOWN;
		}
		return episode.getId().trim();
	}

	private static String shortenUrl(final String url) {
		if (url.length() <= MAX_SHORT_URL_LENGTH) {
			return url;
		}
		final int ellipsisLength = 3;
		final int prefixLength = MAX_SHORT_URL_LENGTH - ellipsisLength;
		return url.substring(0, prefixLength) + "...";
	}

	public static String formatStatusLabel(final String status) {
		if (status == null || status.trim().isEmpty()) {
			return UNKNOWN;
		}
		return status.trim();
	}

	private static String trimToHttpUrl(final String value) {
		if (value == null) {
			return null;
		}
		final String trimmed = value.trim();
		if (trimmed.isEmpty() || !DownloadUtils.isHttpUrl(trimmed)) {
			return null;
		}
		return trimmed;
	}
}
