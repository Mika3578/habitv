package com.dabi.habitv.plugin.ffmpeg;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dabi.habitv.api.plugin.holder.DownloadProgressSnapshot;
import com.dabi.habitv.api.plugin.holder.DownloadStage;

/**
 * Parses ffmpeg {@code -progress pipe:1} key/value lines and optional stderr fallbacks.
 */
public final class FfmpegProgressParser {

	private static final Pattern STDERR_DURATION = Pattern.compile("Duration: (.*?), start:");

	private static final Pattern STDERR_TIME = Pattern.compile("time=(.*?) bitrate");

	public static final class State {
		private Double durationMicros;
		private Long outTimeMicros;

		public void clear() {
			durationMicros = null;
			outTimeMicros = null;
		}

		public Double getDurationMicros() {
			return durationMicros;
		}

		public Long getOutTimeMicros() {
			return outTimeMicros;
		}
	}

	private FfmpegProgressParser() {
	}

	public static DownloadProgressSnapshot parseLine(final String line, final State state) {
		if (line == null || line.isEmpty() || state == null) {
			return null;
		}
		final String trimmed = line.trim();
		if (trimmed.startsWith("duration=")) {
			state.durationMicros = parseDurationMicros(trimmed.substring("duration=".length()));
			return indeterminateRemux();
		}
		if (trimmed.startsWith("out_time_us=")) {
			state.outTimeMicros = parseLongValue(trimmed.substring("out_time_us=".length()));
		} else if (trimmed.startsWith("out_time_ms=")) {
			state.outTimeMicros = parseLongValue(trimmed.substring("out_time_ms=".length()));
		} else if (trimmed.startsWith("out_time=")) {
			state.outTimeMicros = parseOutTimeToMicros(trimmed.substring("out_time=".length()));
		} else {
			final DownloadProgressSnapshot fromStderr = parseStderrLine(trimmed, state);
			if (fromStderr != null) {
				return fromStderr;
			}
			return null;
		}
		return snapshotFromState(state);
	}

	static DownloadProgressSnapshot parseStderrLine(final String line, final State state) {
		if (state.durationMicros == null) {
			final Matcher durationMatcher = STDERR_DURATION.matcher(line);
			if (durationMatcher.find()) {
				final Long micros = parseHhMmSsDurationToMicros(durationMatcher.group(1));
				state.durationMicros = micros == null ? null : Double.valueOf(micros.doubleValue());
			}
		}
		final Matcher timeMatcher = STDERR_TIME.matcher(line);
		if (timeMatcher.find() && state.durationMicros != null) {
			final Long outMicros = parseHhMmSsDurationToMicros(timeMatcher.group(1));
			if (outMicros != null) {
				state.outTimeMicros = outMicros;
				return snapshotFromState(state);
			}
		}
		return null;
	}

	static DownloadProgressSnapshot snapshotFromState(final State state) {
		if (state.durationMicros == null || state.durationMicros.doubleValue() <= 0d) {
			return indeterminateRemux();
		}
		if (state.outTimeMicros == null) {
			return indeterminateRemux();
		}
		final double ratio = Math.min(1.0d,
				Math.max(0.0d, state.outTimeMicros.doubleValue() / state.durationMicros.doubleValue()));
		return DownloadProgressSnapshot.of(DownloadStage.REMUXING, Double.valueOf(ratio), null, null, null, null, null);
	}

	private static DownloadProgressSnapshot indeterminateRemux() {
		return DownloadProgressSnapshot.indeterminate(DownloadStage.REMUXING, null);
	}

	static Double parseDurationMicros(final String raw) {
		if (raw == null || raw.trim().isEmpty()) {
			return null;
		}
		try {
			final double seconds = Double.parseDouble(raw.trim().replace(',', '.'));
			if (seconds <= 0d) {
				return null;
			}
			return Double.valueOf(seconds * 1_000_000d);
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	static Long parseOutTimeToMicros(final String raw) {
		return parseHhMmSsDurationToMicros(raw);
	}

	static Long parseHhMmSsDurationToMicros(final String raw) {
		if (raw == null || raw.trim().isEmpty()) {
			return null;
		}
		final String[] parts = raw.trim().split(":");
		try {
			final double totalSeconds;
			if (parts.length == 3) {
				final long hours = Long.parseLong(parts[0]);
				final long minutes = Long.parseLong(parts[1]);
				final double seconds = Double.parseDouble(parts[2].replace(',', '.'));
				totalSeconds = hours * 3600d + minutes * 60d + seconds;
			} else if (parts.length == 2) {
				final long minutes = Long.parseLong(parts[0]);
				final double seconds = Double.parseDouble(parts[1].replace(',', '.'));
				totalSeconds = minutes * 60d + seconds;
			} else {
				return null;
			}
			if (totalSeconds < 0d) {
				return null;
			}
			return Long.valueOf(Math.round(totalSeconds * 1_000_000d));
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	static Long parseLongValue(final String raw) {
		if (raw == null || raw.trim().isEmpty()) {
			return null;
		}
		try {
			return Long.valueOf(Long.parseLong(raw.trim()));
		} catch (final NumberFormatException e) {
			return null;
		}
	}

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
