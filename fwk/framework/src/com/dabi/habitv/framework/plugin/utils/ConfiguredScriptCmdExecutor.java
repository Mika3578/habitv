package com.dabi.habitv.framework.plugin.utils;

import java.io.IOException;
import java.util.Map;

import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;

/**
 * Runs a locally provisioned helper script for a provider's configured
 * download path (see docs/provider-configured-download.md).
 *
 * Executes the helper as a plain argument array; when the user configured
 * a command processor, the argv is joined and substituted into the
 * {@code #CMD#} token of that template. Environment overrides and progress
 * parsing stay provider-specific.
 */
public abstract class ConfiguredScriptCmdExecutor extends CmdExecutor {

	public static final String CMD_TOKEN = "#CMD#";

	private final String cmdProcessorValue;

	private final String[] commandArgv;

	protected ConfiguredScriptCmdExecutor(final String cmdProcessor, final String[] commandArgv,
			final long maxHungTime) {
		super(cmdProcessor, formatCommandForLog(commandArgv), maxHungTime);
		this.cmdProcessorValue = cmdProcessor;
		this.commandArgv = commandArgv.clone();
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

	public static String formatCommandForLog(final String[] commandArgv) {
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
}