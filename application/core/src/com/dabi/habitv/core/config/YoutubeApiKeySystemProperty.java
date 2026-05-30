package com.dabi.habitv.core.config;

/**
 * Applies the optional YouTube Data API key from user configuration to the JVM
 * system property {@value #PROPERTY_NAME}, without clearing values supplied
 * externally (for example {@code -Dhabitv.youtube.apiKey=...}).
 */
public final class YoutubeApiKeySystemProperty {

	public static final String PROPERTY_NAME = "habitv.youtube.apiKey";

	private static boolean appliedFromUserConfig;

	private YoutubeApiKeySystemProperty() {
	}

	public static void applyFromUserConfig(final String youtubeApiKey) {
		final String sanitized = YoutubeApiKeyConfig.sanitizePlainConfigValue(youtubeApiKey);
		if (sanitized != null) {
			System.setProperty(PROPERTY_NAME, sanitized);
			appliedFromUserConfig = true;
			return;
		}
		if (appliedFromUserConfig) {
			System.clearProperty(PROPERTY_NAME);
			appliedFromUserConfig = false;
		}
	}

	static void resetStateForTests() {
		appliedFromUserConfig = false;
	}

	static boolean isAppliedFromUserConfigForTests() {
		return appliedFromUserConfig;
	}
}
