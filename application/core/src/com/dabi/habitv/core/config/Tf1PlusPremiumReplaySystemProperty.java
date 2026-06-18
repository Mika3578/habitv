package com.dabi.habitv.core.config;

/**
 * Applies TF1+ premium-replay settings from {@code configuration.xml} to JVM system
 * properties read by the {@code tf1plus} plugin. Environment variables still take
 * precedence in the plugin when set.
 */
public final class Tf1PlusPremiumReplaySystemProperty {

	public static final String PROPERTY_EMAIL = "habitv.tf1plus.email";

	public static final String PROPERTY_PASSWORD = "habitv.tf1plus.password";

	public static final String PROPERTY_DEVICE_PATH = "habitv.tf1plus.devicePath";

	/** Legacy property alias; prefer {@link #PROPERTY_DEVICE_PATH}. */
	public static final String PROPERTY_WVD_PATH = "habitv.tf1plus.wvdPath";

	public static final String PROPERTY_N_M3U8DL_RE = "habitv.tf1plus.nM3u8dlRe";

	public static final String PROPERTY_MEDIAFLOW_URL = "habitv.tf1plus.mediaflowUrl";

	public static final String PROPERTY_MEDIAFLOW_PASSWORD = "habitv.tf1plus.mediaflowPassword";

	public static final String PROPERTY_PYTHON = "habitv.tf1plus.python";

	private static boolean appliedFromUserConfig;

	private Tf1PlusPremiumReplaySystemProperty() {
	}

	public static void applyFromUserConfig(final Tf1PlusPremiumReplaySettings settings) {
		if (settings == null) {
			clearAppliedProperties();
			return;
		}
		final boolean any = applyProperty(PROPERTY_EMAIL, settings.getEmail())
				| applyProperty(PROPERTY_PASSWORD, settings.getPassword())
				| applyDevicePath(settings.getDevicePath())
				| applyProperty(PROPERTY_N_M3U8DL_RE, settings.getNM3u8DlRePath())
				| applyProperty(PROPERTY_MEDIAFLOW_URL, settings.getMediaflowUrl())
				| applyProperty(PROPERTY_MEDIAFLOW_PASSWORD, settings.getMediaflowPassword())
				| applyProperty(PROPERTY_PYTHON, settings.getPythonCommand());
		if (!any) {
			clearAppliedProperties();
		} else {
			appliedFromUserConfig = true;
		}
	}

	private static boolean applyDevicePath(final String value) {
		final String sanitized = Tf1PlusPremiumReplayConfig.sanitizePlainValue(value);
		if (sanitized == null) {
			return false;
		}
		System.setProperty(PROPERTY_DEVICE_PATH, sanitized);
		return true;
	}

	private static boolean applyProperty(final String propertyName, final String value) {
		final String sanitized = Tf1PlusPremiumReplayConfig.sanitizePlainValue(value);
		if (sanitized != null) {
			System.setProperty(propertyName, sanitized);
			return true;
		}
		return false;
	}

	private static void clearAppliedProperties() {
		if (!appliedFromUserConfig) {
			return;
		}
		clearProperty(PROPERTY_EMAIL);
		clearProperty(PROPERTY_PASSWORD);
		clearProperty(PROPERTY_DEVICE_PATH);
		clearProperty(PROPERTY_WVD_PATH);
		clearProperty(PROPERTY_N_M3U8DL_RE);
		clearProperty(PROPERTY_MEDIAFLOW_URL);
		clearProperty(PROPERTY_MEDIAFLOW_PASSWORD);
		clearProperty(PROPERTY_PYTHON);
		appliedFromUserConfig = false;
	}

	private static void clearProperty(final String propertyName) {
		if (System.getProperty(propertyName) != null) {
			System.clearProperty(propertyName);
		}
	}

	static void resetStateForTests() {
		clearAppliedProperties();
	}
}
