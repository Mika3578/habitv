package com.dabi.habitv.provider.novo19;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;
import com.dabi.habitv.framework.plugin.utils.CmdExecutor;

final class Novo19ReplayHelperCmdExecutor extends CmdExecutor {

	private static final Pattern PROGRESS_PATTERN = Pattern.compile(".*\\s(\\d+(?:\\.\\d+)?)%.*");

	private final String[] argv;

	Novo19ReplayHelperCmdExecutor(final String cmdProcessor, final String[] argv, final long maxHungTime) {
		super(cmdProcessor, joinForLog(argv), maxHungTime);
		this.argv = argv;
	}

	@Override
	protected Process buildProcess() throws ExecutorFailedException {
		try {
			final ProcessBuilder builder = new ProcessBuilder(argv);
			final Map<String, String> overrides = getProcessEnvironment();
			if (overrides != null && !overrides.isEmpty()) {
				builder.environment().putAll(overrides);
			}
			return builder.start();
		} catch (final IOException e) {
			throw new ExecutorFailedException(joinForLog(argv), e.getMessage(), e.getMessage(), e);
		}
	}

	@Override
	protected Map<String, String> getProcessEnvironment() {
		return Novo19ReplayHelperConfig.buildProcessEnvironmentOverrides();
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

	private static String joinForLog(final String[] argv) {
		if (argv == null || argv.length == 0) {
			return "";
		}
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < argv.length; i++) {
			if (i > 0) {
				builder.append(' ');
			}
			builder.append(argv[i]);
		}
		return builder.toString();
	}

}
