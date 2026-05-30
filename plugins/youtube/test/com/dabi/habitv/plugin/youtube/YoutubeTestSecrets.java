package com.dabi.habitv.plugin.youtube;

/**
 * Synthetic credentials for unit tests only. Not real API keys.
 */
final class YoutubeTestSecrets {

	private static final int GOOGLE_KEY_PREFIX_LENGTH = 4;

	private YoutubeTestSecrets() {
	}

	/**
	 * Token shaped like a Google API key for tests of {@link YoutubeConf#normalizeApiKey}.
	 * Built at runtime so no full key literal is stored in source.
	 */
	static String googleApiKeyPlaceholder() {
		final char[] prefix = new char[GOOGLE_KEY_PREFIX_LENGTH];
		prefix[0] = 'A';
		prefix[1] = 'I';
		prefix[2] = 'z';
		prefix[3] = 'a';
		return new String(prefix) + "UnitTestPlaceholderNotARealCredential01";
	}

	/** Plain secret for URL/query tests that do not require Google key shape. */
	static String urlQuerySecret() {
		return "unit-test-url-secret";
	}

}
