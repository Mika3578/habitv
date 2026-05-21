package com.dabi.habitv.plugin.youtube;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;
import com.dabi.habitv.framework.plugin.utils.CmdExecutor;

/**
 * Detects yt-dlp PyInstaller bootstrap failures and runs a lightweight
 * {@code --version} preflight before real download commands.
 */
public final class YtDlpRuntimeDiagnostics {

	private static final Logger LOG = Logger.getLogger(YtDlpRuntimeDiagnostics.class);

	static final String USER_MESSAGE_PREFIX = "yt-dlp failed before the download started";

	private static final String[] PYINSTALLER_SIGNATURES = { "[PYI-", "Failed to extract", "Cryptodome", "_MEI" };

	private static boolean preflightEnabled = true;

	private YtDlpRuntimeDiagnostics() {
	}

	static void setPreflightEnabled(final boolean enabled) {
		preflightEnabled = enabled;
	}

	static boolean isPreflightEnabled() {
		return preflightEnabled;
	}

	public static boolean isBootstrapExtractionFailure(final String output) {
		if (output == null || output.isEmpty()) {
			return false;
		}
		for (final String signature : PYINSTALLER_SIGNATURES) {
			if (output.contains(signature)) {
				return true;
			}
		}
		return false;
	}

	public static String buildBootstrapFailureUserMessage(final String executablePath) {
		final StringBuilder message = new StringBuilder(USER_MESSAGE_PREFIX);
		message.append(". The yt-dlp binary could not start (PyInstaller extraction failure).");
		message.append(" Likely causes: corrupted yt-dlp.exe, antivirus blocking temp extraction,");
		message.append(" full TEMP or system drive, or bad permissions on the temp directory.");
		message.append(" Recovery: stop Habitv, clear TEMP _MEI folders, replace yt-dlp.exe");
		if (executablePath != null && !executablePath.isEmpty()) {
			message.append(" (current path: ").append(executablePath).append(')');
		}
		message.append(", then run yt-dlp.exe --version outside Habitv before retrying downloads.");
		return message.toString();
	}

	static File computeYtDlpTempDir(final String binDir) {
		File home = new File(binDir);
		if (home.getName().equalsIgnoreCase("bin")) {
			final File parent = home.getParentFile();
			if (parent != null) {
				home = parent;
			}
		}
		return new File(home, "tmp" + File.separator + "yt-dlp");
	}

	public static File resolveYtDlpTempDir(final String binDir) {
		final File tempDir = computeYtDlpTempDir(binDir);
		if (!tempDir.exists() && !tempDir.mkdirs()) {
			LOG.warn("Could not create yt-dlp temp directory: " + tempDir.getAbsolutePath());
		}
		return tempDir;
	}

	public static Map<String, String> buildYtDlpEnvironment(final String binDir) {
		final File tempDir = resolveYtDlpTempDir(binDir);
		final String tempPath = tempDir.getAbsolutePath();
		final Map<String, String> env = new HashMap<String, String>();
		env.put("TEMP", tempPath);
		env.put("TMP", tempPath);
		return Collections.unmodifiableMap(env);
	}

	public static void logExecutableDiagnostics(final String executablePath, final String binDir) {
		LOG.info("yt-dlp executable path: " + executablePath);
		final File executable = new File(executablePath);
		LOG.info("yt-dlp executable exists: " + executable.exists());
		if (executable.exists()) {
			LOG.info("yt-dlp executable size bytes: " + executable.length());
			LOG.info("yt-dlp executable last modified: " + new Date(executable.lastModified()));
		}
		final String tempPath = computeYtDlpTempDir(binDir).getAbsolutePath();
		LOG.info("yt-dlp process TEMP: " + tempPath);
		LOG.info("yt-dlp process TMP: " + tempPath);
	}

	public static void runPreflight(final String cmdProcessor, final String executablePath, final String binDir) {
		if (!preflightEnabled) {
			return;
		}
		logExecutableDiagnostics(executablePath, binDir);
		final String versionCmd = executablePath + " --version";
		final Map<String, String> env = buildYtDlpEnvironment(binDir);
		final CmdExecutor versionExecutor = new CmdExecutor(cmdProcessor, versionCmd, 1000) {
			@Override
			protected long getHungProcessTime() {
				return 1000;
			}

			@Override
			protected Map<String, String> getProcessEnvironment() {
				return env;
			}

			@Override
			protected boolean isSuccess(final String fullOutput) {
				return true;
			}
		};
		try {
			versionExecutor.start();
		} catch (final ExecutorFailedException e) {
			throw asBootstrapFailureIfNeeded(versionCmd, e.getFullOuput(), executablePath, e);
		}
		final String fullOutput = versionExecutor.getFullOutput();
		if (isBootstrapExtractionFailure(fullOutput)) {
			throw new ExecutorFailedException(versionCmd, fullOutput,
					buildBootstrapFailureUserMessage(executablePath), null);
		}
		final String versionLine = fullOutput == null ? "" : fullOutput.trim();
		LOG.info("yt-dlp version: " + versionLine);
	}

	static ExecutorFailedException asBootstrapFailureIfNeeded(final String cmd, final String fullOutput,
			final String executablePath, final ExecutorFailedException cause) {
		if (isBootstrapExtractionFailure(fullOutput)
				|| (cause != null && isBootstrapExtractionFailure(cause.getLastLine()))) {
			return new ExecutorFailedException(cmd, fullOutput, buildBootstrapFailureUserMessage(executablePath), cause);
		}
		return cause;
	}
}
