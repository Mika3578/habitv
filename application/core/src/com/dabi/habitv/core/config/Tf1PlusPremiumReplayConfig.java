package com.dabi.habitv.core.config;

/**
 * Plain-string handling for TF1+ premium-replay entries in user configuration.
 */
public final class Tf1PlusPremiumReplayConfig {

	private Tf1PlusPremiumReplayConfig() {
	}

	public static String sanitizePlainValue(final String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		if (trimmed.length() >= 2
				&& ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
						|| (trimmed.startsWith("'") && trimmed.endsWith("'")))) {
			trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
		}
		return trimmed.isEmpty() ? null : trimmed;
	}
}
