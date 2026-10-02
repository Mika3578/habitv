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
		final String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
