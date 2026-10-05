package com.dabi.habitv.plugin.ffmpeg;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.holder.DownloadProgressSnapshot;
import com.dabi.habitv.api.plugin.holder.DownloadStage;
import com.dabi.habitv.framework.plugin.utils.CmdExecutor;

public class FFMPEGCmdExecutor extends CmdExecutor {

	private static final Logger LOG = Logger.getLogger(FFMPEGCmdExecutor.class);

	private static final Pattern PERCENTAGE_PATTERN = Pattern
			.compile(".*\\((\\d+\\.+\\d+)%\\).*");

	private static final Pattern DURATION_PATTERN = Pattern
			.compile("Duration: (.*?), start:");

	private static final Pattern TIME_PATTERN = Pattern
			.compile("time=(.*?) bitrate");

	private Long duration = null;

	private final FfmpegProgressParser.State progressState = new FfmpegProgressParser.State();

	private volatile DownloadProgressSnapshot progressSnapshot;

	private String lastProgressActivityToken;

	public FFMPEGCmdExecutor(final String cmdProcessor, final String cmd) {
		super(cmdProcessor, cmd, FFMPEGConf.MAX_HUNG_TIME);
	}

	private long toLong(final String duration) {
		return Double.valueOf(Double.parseDouble(duration)).longValue();
	}

	@Override
	public void start() {
		synchronized (getProgressLock()) {
			progressSnapshot = null;
			duration = null;
			progressState.clear();
			lastProgressActivityToken = null;
		}
		super.start();
	}

	@Override
	protected String handleProgression(final String line) {
		LOG.debug(line);
		final DownloadProgressSnapshot parsed;
		final String progressionResult;
		synchronized (getProgressLock()) {
			parsed = FfmpegProgressParser.parseLine(line, progressState);
			if (parsed != null) {
				progressSnapshot = parsed;
				final String progression = FfmpegProgressParser.toProgressionString(parsed);
				if (progression != null) {
					progressionResult = progression;
				} else {
					progressionResult = "stage:" + parsed.getStage().name();
				}
			} else {
				progressionResult = handleLegacyProgressionLine(line);
			}
			lastProgressActivityToken = FfmpegProgressParser.toActivityToken(parsed, progressState, line);
		}
		return progressionResult;
	}

	@Override
	protected String progressionActivityTokenFor(final String line, final String progressionResult) {
		if (lastProgressActivityToken != null) {
			return lastProgressActivityToken;
		}
		return progressionResult;
	}

	private String handleLegacyProgressionLine(final String line) {
		if (duration == null) {
			duration = findDuration(line);
		}
		final Matcher matcher = TIME_PATTERN.matcher(line);
		final boolean hasMatched = matcher.find();
		String ret = null;
		if (hasMatched && duration != null) {
			final String stringDuration = matcher.group(matcher.groupCount());
			final String[] durationTab = stringDuration.split(":");
			long currentDuration = 0;
			final int l = durationTab.length;
			if (l > 0) {
				currentDuration += toLong(durationTab[l - 1]);
				if (l > 1) {
					currentDuration += TimeUnit.MINUTES
							.toSeconds(toLong(durationTab[l - 2]));
				}
				if (l > 2) {
					currentDuration += TimeUnit.HOURS
							.toSeconds(toLong(durationTab[l - 3]));
				}
			}
			ret = String.valueOf((currentDuration * PERCENTAGE / duration));
		} else {
			ret = matchPercentage(line);
		}
		LOG.debug("ret " + ret);
		return ret;
	}

	@Override
	public String getProgression() {
		synchronized (getProgressLock()) {
			final DownloadProgressSnapshot snapshot = progressSnapshot;
			if (snapshot != null) {
				if (snapshot.isIndeterminate()) {
					return null;
				}
				return FfmpegProgressParser.toProgressionString(snapshot);
			}
			final String progression = super.getProgression();
			if (progression != null && progression.startsWith("stage:")) {
				return null;
			}
			return progression;
		}
	}

	@Override
	public DownloadProgressSnapshot getProgressSnapshot() {
		synchronized (getProgressLock()) {
			final DownloadProgressSnapshot snapshot = progressSnapshot;
			if (snapshot != null) {
				return snapshot;
			}
			return snapshotFromLegacyProgressionString(getProgression());
		}
	}

	static DownloadProgressSnapshot snapshotFromLegacyProgressionString(final String progression) {
		if (progression == null || progression.trim().isEmpty()) {
			return DownloadProgressSnapshot.indeterminate(DownloadStage.REMUXING, null);
		}
		try {
			final double percent = Double.parseDouble(progression.trim().replace(',', '.'));
			if (Double.isNaN(percent) || percent < 0) {
				return DownloadProgressSnapshot.indeterminate(DownloadStage.REMUXING, null);
			}
			final double ratio = Math.min(1.0d, percent / 100.0d);
			return DownloadProgressSnapshot.of(DownloadStage.REMUXING, Double.valueOf(ratio), null, null, null,
					null, null);
		} catch (final NumberFormatException e) {
			return DownloadProgressSnapshot.indeterminate(DownloadStage.REMUXING, progression);
		}
	}

	private String matchPercentage(final String line) {
		final Matcher matcher = PERCENTAGE_PATTERN.matcher(line);
		final boolean hasMatched = matcher.find();
		String ret = null;
		if (hasMatched) {
			ret = matcher.group(matcher.groupCount());
		}
		return ret;
	}

	private static Long findDuration(final String line) {
		final Matcher matcher = DURATION_PATTERN.matcher(line);
		if (!matcher.find()) {
			return null;
		}
		final String durationFormatted = matcher.group(matcher.groupCount()).trim();
		if (durationFormatted.isEmpty() || "N/A".equalsIgnoreCase(durationFormatted)
				|| "NA".equalsIgnoreCase(durationFormatted)) {
			return null;
		}
		try {
			final String[] durationSplitted = durationFormatted.split(":");
			if (durationSplitted.length < 3) {
				return null;
			}
			final long hours = Long.valueOf(durationSplitted[0]);
			final long minutes = Long.valueOf(durationSplitted[1]);
			final long seconds = Double.valueOf(durationSplitted[2]).longValue();
			return Long.valueOf(TimeUnit.SECONDS.convert(hours, TimeUnit.HOURS)
					+ TimeUnit.SECONDS.convert(minutes, TimeUnit.MINUTES) + seconds);
		} catch (final NumberFormatException e) {
			return null;
		}
	}

}
