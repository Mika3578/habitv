package com.dabi.habitv.plugin.youtube;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dabi.habitv.api.plugin.holder.DownloadProgressSnapshot;
import com.dabi.habitv.api.plugin.holder.DownloadStage;

/**
 * Deterministic parser for yt-dlp stdout/stderr progress lines.
 * Isolated for offline unit tests; does not invoke yt-dlp.
 */
public final class YtDlpProgressParser {

	static final String DETAIL_VIDEO = "Vidéo";
	static final String DETAIL_AUDIO = "Audio";

	private static final Pattern ANSI = Pattern.compile("\u001B\\[[0-9;]*m");

	private static final Pattern DOWNLOAD_PROGRESS = Pattern.compile(
			"\\[download\\]\\s+(\\d+(?:[.,]\\d+)?)%\\s+of\\s+(?:~\\s*)?(\\S+)(?:\\s+at\\s+(\\S+))?(?:\\s+ETA\\s+(\\S+))?",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern DOWNLOAD_PROGRESS_SIMPLE = Pattern.compile(
			"\\[download\\]\\s+(\\d+(?:[.,]\\d+)?)%",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern DOWNLOAD_FRAGMENT = Pattern.compile(
			"\\[download\\]\\s+(\\d+(?:[.,]\\d+)?)%\\s+of\\s+~?\\s*(\\S+)\\s+in\\s+.*",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern DOWNLOAD_DESTINATION = Pattern.compile(
			"\\[download\\]\\s+Destination:\\s+(.+)",
			Pattern.CASE_INSENSITIVE);

	private YtDlpProgressParser() {
	}

	/**
	 * Parses one yt-dlp output line into a progress snapshot.
	 *
	 * @param line           raw line
	 * @param previous       previous snapshot, may be {@code null}
	 * @return updated snapshot, or {@code null} when the line carries no progress information
	 */
	public static DownloadProgressSnapshot parse(final String line, final DownloadProgressSnapshot previous) {
		if (line == null || line.isEmpty()) {
			return null;
		}
		final String trimmed = stripAnsi(line).trim();

		final DownloadStage stageFromTag = detectPostProcessingStage(trimmed);
		if (stageFromTag != null) {
			return DownloadProgressSnapshot.indeterminate(stageFromTag, shortDetail(trimmed));
		}

		final Matcher destinationMatcher = DOWNLOAD_DESTINATION.matcher(trimmed);
		if (destinationMatcher.find()) {
			final String streamLabel = detectStreamLabel(destinationMatcher.group(1));
			// Destination announces the next transfer; no numeric progress yet.
			return DownloadProgressSnapshot.indeterminate(DownloadStage.DOWNLOADING, streamLabel);
		}

		if (isPreparingLine(trimmed)) {
			final String detail = shortDetail(trimmed);
			if (previous != null && previous.getStage() == DownloadStage.DOWNLOADING) {
				// Do not regress an active download (including Destination) to preparing.
				return null;
			}
			return DownloadProgressSnapshot.indeterminate(DownloadStage.PREPARING, detail);
		}

		Matcher matcher = DOWNLOAD_PROGRESS.matcher(trimmed);
		boolean matched = matcher.find();
		if (!matched) {
			matcher = DOWNLOAD_FRAGMENT.matcher(trimmed);
			matched = matcher.find();
		}
		if (matched) {
			return buildDownloadSnapshot(matcher, true, previous);
		}

		matcher = DOWNLOAD_PROGRESS_SIMPLE.matcher(trimmed);
		if (matcher.find()) {
			return buildDownloadSnapshot(matcher, false, previous);
		}

		return null;
	}

	private static DownloadProgressSnapshot buildDownloadSnapshot(final Matcher matcher,
			final boolean withSizeSpeedEta, final DownloadProgressSnapshot previous) {
		final double percent = parseLocaleNumber(matcher.group(1));
		final Double ratio = Double.valueOf(Math.min(1.0d, Math.max(0.0d, percent / 100.0d)));

		Long totalBytes = null;
		Long downloadedBytes = null;
		Double bytesPerSecond = null;
		Long etaSeconds = null;

		if (withSizeSpeedEta && matcher.groupCount() >= 2 && matcher.group(2) != null) {
			totalBytes = parseSizeToBytes(matcher.group(2));
			if (totalBytes != null) {
				downloadedBytes = Long.valueOf(Math.round(totalBytes.doubleValue() * ratio.doubleValue()));
			}
		}
		if (withSizeSpeedEta && matcher.groupCount() >= 3 && matcher.group(3) != null
				&& !"Unknown".equalsIgnoreCase(matcher.group(3))) {
			bytesPerSecond = parseSpeedToBytesPerSecond(matcher.group(3));
		}
		if (withSizeSpeedEta && matcher.groupCount() >= 4 && matcher.group(4) != null
				&& !"Unknown".equalsIgnoreCase(matcher.group(4))) {
			etaSeconds = parseEtaToSeconds(matcher.group(4));
		}

		if (downloadedBytes == null && totalBytes != null) {
			downloadedBytes = Long.valueOf(Math.round(totalBytes.doubleValue() * ratio.doubleValue()));
		}

		final String detail = resolveStreamDetail(previous, ratio);
		return DownloadProgressSnapshot.of(DownloadStage.DOWNLOADING, ratio, downloadedBytes, totalBytes,
				bytesPerSecond, etaSeconds, detail);
	}

	/**
	 * Keeps Vidéo/Audio labels across progress lines. When progress jumps from nearly complete
	 * back to a low percentage (typical yt-dlp video-then-audio), switch Vidéo → Audio.
	 */
	static String resolveStreamDetail(final DownloadProgressSnapshot previous, final Double ratio) {
		if (previous == null) {
			return DETAIL_VIDEO;
		}
		final String previousDetail = previous.getDetail();
		if (previous.getProgressRatio() != null && ratio != null
				&& previous.getProgressRatio().doubleValue() >= 0.85d
				&& ratio.doubleValue() <= 0.15d
				&& previous.getStage() == DownloadStage.DOWNLOADING
				&& !DETAIL_AUDIO.equals(previousDetail)) {
			return DETAIL_AUDIO;
		}
		if (DETAIL_VIDEO.equals(previousDetail) || DETAIL_AUDIO.equals(previousDetail)) {
			return previousDetail;
		}
		return DETAIL_VIDEO;
	}

	static String detectStreamLabel(final String destination) {
		if (destination == null) {
			return DETAIL_VIDEO;
		}
		final String lower = destination.toLowerCase(Locale.ROOT);
		if (lower.endsWith(".m4a") || lower.endsWith(".aac") || lower.endsWith(".mp3")
				|| lower.endsWith(".opus") || lower.endsWith(".ogg") || lower.endsWith(".wma")) {
			return DETAIL_AUDIO;
		}
		// Common YouTube DASH audio format ids in filenames: .f139/.f140/.f249/.f250/.f251
		if (lower.matches(".*\\.f(139|140|249|250|251)\\.[a-z0-9]+$")) {
			return DETAIL_AUDIO;
		}
		return DETAIL_VIDEO;
	}

	static DownloadStage detectPostProcessingStage(final String line) {
		final String lower = line.toLowerCase(Locale.ROOT);
		if (lower.contains("[merger]")) {
			return DownloadStage.MERGING;
		}
		if (lower.contains("[extractaudio]")) {
			return DownloadStage.REMUXING;
		}
		if (lower.contains("[ffmpeg]")) {
			return DownloadStage.REMUXING;
		}
		if (lower.contains("[embedsubtitle]") || lower.contains("[subtitlesconvertor]")
				|| lower.contains("[writesubtitles]")) {
			return DownloadStage.SUBTITLES;
		}
		if (lower.contains("[metadata]") || lower.contains("[embedthumbnail]")
				|| lower.contains("[xattrmetadata]")) {
			return DownloadStage.METADATA;
		}
		if (lower.contains("[movefiles]") || lower.contains("[fixupmtime]")) {
			return DownloadStage.FINALIZING;
		}
		if (lower.contains("[postprocess]")) {
			return DownloadStage.POST_PROCESSING;
		}
		return null;
	}

	private static boolean isPreparingLine(final String line) {
		final String lower = line.toLowerCase(Locale.ROOT);
		return lower.startsWith("[info]")
				|| lower.contains("downloading webpage")
				|| lower.contains("extracting url")
				|| lower.contains("downloading m3u8 information")
				|| lower.contains("downloading android player api json")
				|| lower.contains("downloading player")
				|| lower.contains("downloading json metadata");
	}

	private static String shortDetail(final String line) {
		String detail = line;
		if (detail.length() > 120) {
			detail = detail.substring(0, 117) + "...";
		}
		return detail;
	}

	static String stripAnsi(final String raw) {
		return ANSI.matcher(raw).replaceAll("");
	}

	static double parseLocaleNumber(final String raw) {
		return Double.parseDouble(raw.trim().replace(',', '.'));
	}

	/**
	 * Parses yt-dlp size tokens such as {@code 10.00MiB}, {@code 159.6MiB}, {@code 512KiB}.
	 */
	static Long parseSizeToBytes(final String raw) {
		if (raw == null) {
			return null;
		}
		final String token = raw.trim();
		if (token.isEmpty() || "Unknown".equalsIgnoreCase(token) || "~".equals(token)) {
			return null;
		}
		final Matcher matcher = Pattern.compile("(~)?(\\d+(?:[.,]\\d+)?)\\s*([KMG]?i?B)", Pattern.CASE_INSENSITIVE)
				.matcher(token);
		if (!matcher.matches()) {
			return null;
		}
		final double value = parseLocaleNumber(matcher.group(2));
		final String unit = matcher.group(3).toUpperCase(Locale.ROOT);
		final double factor;
		if ("B".equals(unit)) {
			factor = 1d;
		} else if ("KB".equals(unit) || "KIB".equals(unit)) {
			factor = "KB".equals(unit) ? 1000d : 1024d;
		} else if ("MB".equals(unit) || "MIB".equals(unit)) {
			factor = "MB".equals(unit) ? 1000d * 1000d : 1024d * 1024d;
		} else if ("GB".equals(unit) || "GIB".equals(unit)) {
			factor = "GB".equals(unit) ? 1000d * 1000d * 1000d : 1024d * 1024d * 1024d;
		} else {
			return null;
		}
		return Long.valueOf(Math.round(value * factor));
	}

	static Double parseSpeedToBytesPerSecond(final String raw) {
		final Long perSecond = parseSizeToBytes(raw.replace("/s", "").trim());
		return perSecond == null ? null : Double.valueOf(perSecond.doubleValue());
	}

	/**
	 * Parses ETA tokens such as {@code 00:18}, {@code 01:04:32}, or {@code 18}.
	 */
	static Long parseEtaToSeconds(final String raw) {
		if (raw == null || raw.isEmpty() || "Unknown".equalsIgnoreCase(raw)) {
			return null;
		}
		final String[] parts = raw.trim().split(":");
		try {
			if (parts.length == 1) {
				return Long.valueOf(Long.parseLong(parts[0]));
			}
			if (parts.length == 2) {
				final long minutes = Long.parseLong(parts[0]);
				final long seconds = Long.parseLong(parts[1]);
				return Long.valueOf(minutes * 60L + seconds);
			}
			if (parts.length == 3) {
				final long hours = Long.parseLong(parts[0]);
				final long minutes = Long.parseLong(parts[1]);
				final long seconds = Long.parseLong(parts[2]);
				return Long.valueOf(hours * 3600L + minutes * 60L + seconds);
			}
		} catch (final NumberFormatException e) {
			return null;
		}
		return null;
	}

	/**
	 * Formats a percentage for the legacy {@link com.dabi.habitv.api.plugin.holder.ProcessHolder#getProgression()}
	 * string without forcing a useless trailing {@code .0} when the value is integral.
	 */
	public static String toProgressionString(final DownloadProgressSnapshot snapshot) {
		if (snapshot == null || snapshot.getProgressRatio() == null) {
			return null;
		}
		final double percent = snapshot.getProgressRatio().doubleValue() * 100.0d;
		if (Math.abs(percent - Math.rint(percent)) < 0.05d) {
			return Long.toString(Math.round(percent));
		}
		return String.format(Locale.US, "%.1f", percent);
	}
}
