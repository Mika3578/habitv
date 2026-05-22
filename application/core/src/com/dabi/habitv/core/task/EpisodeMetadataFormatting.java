package com.dabi.habitv.core.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public final class EpisodeMetadataFormatting {

	private static final String UNKNOWN = "Inconnu";

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
	 * stores an HTTP(S) identifier on the downloadable category node.
	 */
	public static String programPageUrl(final EpisodeDTO episode) {
		if (episode == null || episode.getCategory() == null) {
			return null;
		}
		final String categoryId = episode.getCategory().getId();
		if (categoryId == null) {
			return null;
		}
		final String trimmedCategoryId = categoryId.trim();
		if (trimmedCategoryId.isEmpty()) {
			return null;
		}
		if (trimmedCategoryId.startsWith("http://")
				|| trimmedCategoryId.startsWith("https://")) {
			return trimmedCategoryId;
		}
		return null;
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
		if (url.length() <= 48) {
			return url;
		}
		return url.substring(0, 45) + "...";
	}

	public static String formatStatusLabel(final String status) {
		if (status == null || status.trim().isEmpty()) {
			return UNKNOWN;
		}
		return status;
	}
}
