package com.dabi.habitv.plugin.ffmpeg;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.holder.DownloadProgressSnapshot;
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

	public FFMPEGCmdExecutor(final String cmdProcessor, final String cmd) {
		super(cmdProcessor, cmd, FFMPEGConf.MAX_HUNG_TIME);
	}

	private long toLong(final String duration) {
		return Double.valueOf(Double.parseDouble(duration)).longValue();
	}

	@Override
	public void start() {
		progressSnapshot = null;
		super.start();
	}

	@Override
	protected String handleProgression(final String line) {
		LOG.debug(line);
		final DownloadProgressSnapshot parsed = FfmpegProgressParser.parseLine(line, progressState);
		if (parsed != null) {
			progressSnapshot = parsed;
			final String progression = FfmpegProgressParser.toProgressionString(parsed);
			if (progression != null) {
				return progression;
			}
			return "stage:" + parsed.getStage().name();
		}
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
	public DownloadProgressSnapshot getProgressSnapshot() {
		final DownloadProgressSnapshot snapshot = progressSnapshot;
		if (snapshot != null) {
			return snapshot;
		}
		return DownloadProgressSnapshot.fromProgressionString(getProgression());
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
		final boolean hasMatched = matcher.find();
		Long ret = null;
		if (hasMatched) {
			final String durationFormatted = matcher
					.group(matcher.groupCount());
			final String[] durationSplitted = durationFormatted.split(":");
			final long hours = Long.valueOf(durationSplitted[0]);
			final long minutes = Long.valueOf(durationSplitted[1]);
			final long seconds = Double.valueOf(durationSplitted[2])
					.longValue();
			ret = TimeUnit.SECONDS.convert(hours, TimeUnit.HOURS)
					+ TimeUnit.SECONDS.convert(minutes, TimeUnit.MINUTES)
					+ seconds;
		}
		return ret;
	}

}
