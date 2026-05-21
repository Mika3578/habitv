package com.dabi.habitv.plugin.youtube;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.utils.CmdExecutor;

/**
 * Runs yt-dlp (or a user-configured compatible binary) and parses download progress
 * from stdout/stderr lines such as {@code [download]  45.2% of ...}.
 */
public class YtDlpCmdExecutor extends CmdExecutor {

	private static final Pattern PROGRESS_PATTERN = Pattern
			.compile(".*\\s(\\d+.\\d+)%.*");

	private final String executablePath;

	private final String binDir;

	public YtDlpCmdExecutor(final String cmdProcessor, final String cmd) {
		this(cmdProcessor, cmd, null, null);
	}

	public YtDlpCmdExecutor(final String cmdProcessor, final String cmd, final String executablePath,
			final String binDir) {
		super(cmdProcessor, cmd, YoutubeConf.MAX_HUNG_TIME);
		this.executablePath = executablePath;
		this.binDir = binDir;
	}

	@Override
	protected Map<String, String> getProcessEnvironment() {
		if (binDir == null) {
			return null;
		}
		return YtDlpRuntimeDiagnostics.buildYtDlpEnvironment(binDir);
	}

	@Override
	protected ExecutorFailedException buildFailureException(final String failedCmd, final String fullOutput,
			final String lastLine, final Throwable cause) {
		final String pathForMessage = executablePath != null ? executablePath : failedCmd;
		if (YtDlpRuntimeDiagnostics.isBootstrapExtractionFailure(fullOutput)
				|| YtDlpRuntimeDiagnostics.isBootstrapExtractionFailure(lastLine)) {
			return new ExecutorFailedException(failedCmd, fullOutput,
					YtDlpRuntimeDiagnostics.buildBootstrapFailureUserMessage(pathForMessage), cause);
		}
		return super.buildFailureException(failedCmd, fullOutput, lastLine, cause);
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
