package com.dabi.habitv.core.config;

/**
 * Plain-string handling for the optional YouTube Data API key in user configuration.
 * Must never be resolved as a filesystem path.
 */
public final class YoutubeApiKeyConfig {

	private YoutubeApiKeyConfig() {
	}

	public static String sanitizePlainConfigValue(final String value) {
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
