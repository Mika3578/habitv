package com.dabi.habitv.plugin.youtube;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.utils.CmdExecutor;

/**
 * Runs yt-dlp (or a user-configured compatible binary) and parses download progress
 * from stdout/stderr lines such as {@code [download]  45.2% of ...}.
 */
public class YtDlpCmdExecutor extends CmdExecutor {

	private static final Pattern PROGRESS_PATTERN = Pattern
			.compile(".*\\s(\\d+.\\d+)%.*");

	public YtDlpCmdExecutor(final String cmdProcessor, final String cmd) {
		super(cmdProcessor, cmd, YoutubeConf.MAX_HUNG_TIME);
	}

	@Override
	protected String handleProgression(final String line) {
		final Matcher matcher = PROGRESS_PATTERN.matcher(line);
		final boolean hasMatched = matcher.find();
		String ret = null;
		if (hasMatched) {
			ret = matcher.group(matcher.groupCount());
		}
		return ret;
	}

	@Override
	protected boolean isSuccess(final String fullOutput) {
		return true;
	}

	@Override
	protected long getHungProcessTime() {
		return FrameworkConf.HUNG_PROCESS_TIME;
	}

}
