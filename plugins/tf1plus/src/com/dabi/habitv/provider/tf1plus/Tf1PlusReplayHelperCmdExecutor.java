package com.dabi.habitv.provider.tf1plus;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dabi.habitv.framework.plugin.utils.ConfiguredScriptCmdExecutor;

/**
 * Runs the TF1+ replay helper and parses {@code [download] <percent>%}
 * lines for the Habitv download UI (same convention as yt-dlp).
 */
final class Tf1PlusReplayHelperCmdExecutor extends ConfiguredScriptCmdExecutor {

	private static final Pattern PROGRESS_PATTERN = Pattern.compile(".*\\s(\\d+(?:\\.\\d+)?)%.*");

	Tf1PlusReplayHelperCmdExecutor(final String cmdProcessor, final String[] commandArgv, final long maxHungTime) {
		super(cmdProcessor, commandArgv, maxHungTime);
	}

	@Override
	protected Map<String, String> getProcessEnvironment() {
		final Map<String, String> overrides = Tf1PlusReplayHelperConfig.buildProcessEnvironmentOverrides();
		if (overrides == null) {
			final Map<String, String> unbuffered = new java.util.HashMap<String, String>();
			unbuffered.put("PYTHONUNBUFFERED", "1");
			return unbuffered;
		}
		overrides.put("PYTHONUNBUFFERED", "1");
		return overrides;
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