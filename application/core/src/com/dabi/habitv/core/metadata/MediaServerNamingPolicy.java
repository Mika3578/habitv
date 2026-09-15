package com.dabi.habitv.core.metadata;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO;
import com.dabi.habitv.utils.FileUtils;

/**
 * Builds MEDIA_SERVER relative paths from canonical metadata.
 * <p>
 * Rules:
 * <ul>
 * <li>season + episode known:
 * {@code Series/Season NN/Series - SNNENN - Title.ext}</li>
 * <li>air date known (dated show):
 * {@code Series/yyyy/Series - yyyy-MM-dd - Title.ext}</li>
 * <li>otherwise with series:
 * {@code Series/Series - Title.ext}</li>
 * <li>series missing: episode-title filename only</li>
 * </ul>
 * Missing optional parts collapse cleanly. Never fabricates {@code S00E00}.
 * Episode titles are cleaned by {@link EpisodeTitleNormalizer} so providers that
 * embed {@code Sx Ey} in display strings do not produce duplicated markers.
 */
public final class MediaServerNamingPolicy {

	private MediaServerNamingPolicy() {
	}

	public static String buildRelativePath(final EpisodeMetadataDTO metadata, final String extension) {
		final EpisodeMetadataDTO meta = metadata == null ? new EpisodeMetadataDTO() : metadata;
		final String series = sanitizeSegment(meta.getSeriesTitle());
		final String title = sanitizeSegment(EpisodeTitleNormalizer.forFilename(meta));
		final String ext = normalizeExtension(extension);

		if (isPresent(series) && meta.hasSeasonAndEpisode()) {
			final String seasonFolder = "Season "
					+ String.format(Locale.ROOT, "%02d", meta.getSeasonNumber().intValue());
			final String fileBase = joinDash(series, formatSeasonEpisode(meta), title);
			return joinPath(series, seasonFolder, fileBase + ext);
		}

		if (isPresent(series) && meta.getAirDate() != null) {
			final String year = format(meta.getAirDate(), "yyyy");
			final String air = format(meta.getAirDate(), "yyyy-MM-dd");
			final String fileBase = joinDash(series, air, title);
			return joinPath(series, year, fileBase + ext);
		}

		if (isPresent(series)) {
			final String fileBase = isPresent(title) ? joinDash(series, title) : series;
			return joinPath(series, fileBase + ext);
		}

		if (isPresent(title)) {
			return title + ext;
		}

		return "episode" + ext;
	}

	public static String formatSeasonEpisode(final EpisodeMetadataDTO metadata) {
		if (metadata == null || !metadata.hasSeasonAndEpisode()) {
			return "";
		}
		return String.format(Locale.ROOT, "S%02dE%02d", metadata.getSeasonNumber().intValue(),
				metadata.getEpisodeNumber().intValue());
	}

	private static String joinDash(final String... parts) {
		final List<String> present = new ArrayList<>();
		for (final String part : parts) {
			if (isPresent(part)) {
				present.add(part);
			}
		}
		if (present.isEmpty()) {
			return "";
		}
		final StringBuilder builder = new StringBuilder(present.get(0));
		for (int i = 1; i < present.size(); i++) {
			builder.append(" - ").append(present.get(i));
		}
		return builder.toString();
	}

	private static String joinPath(final String... parts) {
		final StringBuilder builder = new StringBuilder();
		for (final String part : parts) {
			if (!isPresent(part)) {
				continue;
			}
			if (builder.length() > 0) {
				builder.append('/');
			}
			builder.append(part);
		}
		return builder.toString();
	}

	private static String sanitizeSegment(final String value) {
		if (value == null) {
			return null;
		}
		final String sanitized = FileUtils.sanitizePathSegment(value);
		return isPresent(sanitized) ? sanitized : null;
	}

	private static String normalizeExtension(final String extension) {
		if (extension == null || extension.trim().isEmpty()) {
			return "";
		}
		final String trimmed = extension.trim();
		return trimmed.startsWith(".") ? trimmed : ("." + trimmed);
	}

	private static String format(final Date date, final String pattern) {
		return new SimpleDateFormat(pattern, Locale.ROOT).format(date);
	}

	private static boolean isPresent(final String value) {
		return value != null && !value.trim().isEmpty();
	}
}
