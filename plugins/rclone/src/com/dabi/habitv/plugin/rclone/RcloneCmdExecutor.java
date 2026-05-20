package com.dabi.habitv.plugin.rclone;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dabi.habitv.framework.plugin.utils.CmdExecutor;

public class RcloneCmdExecutor extends CmdExecutor {

	private static final Pattern PROGRESS_PATTERN = Pattern
			.compile("Transferred:.*?(\\d{1,3})\\s*%");

	private static final Pattern ERROR_PATTERN = Pattern
			.compile("(?i)\\b(ERROR|Failed to copy)\\b");

	public RcloneCmdExecutor(final String cmdProcessor, final String cmd) {
		super(cmdProcessor, cmd, RcloneConf.MAX_HUNG_TIME);
	}

	@Override
	protected String handleProgression(final String line) {
		final Matcher matcher = PROGRESS_PATTERN.matcher(line);
		return matcher.find() ? matcher.group(1) : null;
	}

	@Override
	protected boolean isSuccess(final String fullOutput) {
		if (fullOutput == null) {
			return false;
		}
		return fullOutput.contains("Transferred:")
				&& !ERROR_PATTERN.matcher(fullOutput).find();
	}
}
