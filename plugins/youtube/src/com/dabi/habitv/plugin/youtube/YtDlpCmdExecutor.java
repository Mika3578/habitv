package com.dabi.habitv.plugin.youtube;

import java.util.Map;

import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;
import com.dabi.habitv.api.plugin.holder.DownloadProgressSnapshot;
import com.dabi.habitv.api.plugin.holder.DownloadStage;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.utils.CmdExecutor;

/**
 * Runs yt-dlp (or a user-configured compatible binary) and parses download progress
 * from stdout/stderr into a {@link DownloadProgressSnapshot}.
 */
public class YtDlpCmdExecutor extends CmdExecutor {

	private final String executablePath;

	private final String binDir;

	private volatile DownloadProgressSnapshot progressSnapshot;

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
	public void start() {
		progressSnapshot = null;
		super.start();
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
		final DownloadProgressSnapshot previous = progressSnapshot;
		final DownloadProgressSnapshot parsed = YtDlpProgressParser.parse(line, previous);
		if (parsed == null) {
			return null;
		}
		progressSnapshot = parsed;
		final String progression = YtDlpProgressParser.toProgressionString(parsed);
		if (progression != null) {
			return progression;
		}
		// Keep hung-process detection alive during post-processing without a percentage.
		return "stage:" + parsed.getStage().name();
	}

	@Override
	public String getProgression() {
		final DownloadProgressSnapshot snapshot = progressSnapshot;
		if (snapshot != null) {
			if (snapshot.isIndeterminate() || snapshot.getStage().isPostProcessing()) {
				return null;
			}
			return YtDlpProgressParser.toProgressionString(snapshot);
		}
		return super.getProgression();
	}

	@Override
	public DownloadProgressSnapshot getProgressSnapshot() {
		final DownloadProgressSnapshot snapshot = progressSnapshot;
		if (snapshot != null) {
			return snapshot;
		}
		final String progression = super.getProgression();
		if (progression != null && progression.startsWith("stage:")) {
			return DownloadProgressSnapshot.indeterminate(DownloadStage.POST_PROCESSING, null);
		}
		return DownloadProgressSnapshot.fromProgressionString(progression);
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
