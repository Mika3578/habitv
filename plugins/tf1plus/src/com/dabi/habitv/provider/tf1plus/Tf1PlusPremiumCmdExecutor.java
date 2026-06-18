package com.dabi.habitv.provider.tf1plus;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;
import com.dabi.habitv.framework.plugin.utils.CmdExecutor;

/**
 * Runs the TF1+ premium Python helper and parses {@code [download] <percent>%}
 * lines for the Habitv download UI (same convention as yt-dlp).
 */
final class Tf1PlusPremiumCmdExecutor extends CmdExecutor {

	private static final String CMD_TOKEN = "#CMD#";

	private static final Pattern PROGRESS_PATTERN = Pattern.compile(".*\\s(\\d+(?:\\.\\d+)?)%.*");

	private final String cmdProcessorValue;

	private final String[] commandArgv;

	Tf1PlusPremiumCmdExecutor(final String cmdProcessor, final String[] commandArgv, final long maxHungTime) {
		super(cmdProcessor, formatCommandForLog(commandArgv), maxHungTime);
		this.cmdProcessorValue = cmdProcessor;
		this.commandArgv = commandArgv.clone();
	}

	@Override
	protected Map<String, String> getProcessEnvironment() {
		final Map<String, String> overrides = Tf1PlusPremiumDownloadConfig.buildProcessEnvironmentOverrides();
		if (overrides == null) {
			final Map<String, String> unbuffered = new java.util.HashMap<String, String>();
			unbuffered.put("PYTHONUNBUFFERED", "1");
			return unbuffered;
		}
		overrides.put("PYTHONUNBUFFERED", "1");
		return overrides;
	}

	@Override
	protected Process buildProcess() throws ExecutorFailedException {
		try {
			final Map<String, String> envOverrides = getProcessEnvironment();
			if (cmdProcessorValue == null || cmdProcessorValue.isEmpty()) {
				return startProcess(commandArgv, envOverrides);
			}
			final String shellCommand = joinArgvForShell(commandArgv);
			final String[] cmdArgs = cmdProcessorValue.split(" ");
			for (int i = 0; i < cmdArgs.length; i++) {
				if (cmdArgs[i].contains(CMD_TOKEN)) {
					cmdArgs[i] = cmdArgs[i].replace(CMD_TOKEN, shellCommand);
				}
			}
			return startProcess(cmdArgs, envOverrides);
		} catch (final IOException e) {
			throw new ExecutorFailedException(formatCommandForLog(commandArgv), e.getMessage(), e.getMessage(), e);
		}
	}

	private static Process startProcess(final String[] argv, final Map<String, String> envOverrides)
			throws IOException {
		final ProcessBuilder builder = new ProcessBuilder(argv);
		if (envOverrides != null && !envOverrides.isEmpty()) {
			builder.environment().putAll(envOverrides);
		}
		return builder.start();
	}

	static String formatCommandForLog(final String[] commandArgv) {
		return joinArgvForShell(commandArgv);
	}

	private static String joinArgvForShell(final String[] commandArgv) {
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < commandArgv.length; i++) {
			if (i > 0) {
				builder.append(' ');
			}
			builder.append(quoteForShell(commandArgv[i]));
		}
		return builder.toString();
	}

	private static String quoteForShell(final String value) {
		if (value == null || value.isEmpty()) {
			return "\"\"";
		}
		if (value.indexOf(' ') < 0 && value.indexOf('"') < 0) {
			return value;
		}
		return "\"" + value.replace("\"", "\\\"") + "\"";
	}

	@Override
	protected String handleProgression(final String line) {
		if (line == null || line.isEmpty()) {
			return null;
		}
		final Matcher matcher = PROGRESS_PATTERN.matcher(line);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
}
