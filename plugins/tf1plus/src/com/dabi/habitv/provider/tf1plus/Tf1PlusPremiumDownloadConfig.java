package com.dabi.habitv.provider.tf1plus;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Resolves TF1+ premium-replay settings: OS environment first, then JVM system
 * properties set from {@code configuration.xml} at Habitv startup
 * ({@link Tf1PlusConf#PROPERTY_EMAIL}, etc.).
 */
final class Tf1PlusPremiumDownloadConfig {

	private Tf1PlusPremiumDownloadConfig() {
	}

	static boolean isConfigured() {
		return computeConfigured();
	}

	static boolean hasCredentials() {
		return hasText(resolve(Tf1PlusConf.ENV_TF1_EMAIL, Tf1PlusConf.PROPERTY_EMAIL))
				&& hasText(resolve(Tf1PlusConf.ENV_TF1_PASSWORD, Tf1PlusConf.PROPERTY_PASSWORD));
	}

	static String userFacingConfigurationMessage() {
		if (!hasCredentials()) {
			return Tf1PlusConf.USER_MESSAGE_TF1_CREDENTIALS_REQUIRED;
		}
		if (!hasLocalDevice()) {
			return Tf1PlusConf.USER_MESSAGE_TF1_DEVICE_PATH_REQUIRED;
		}
		if (!hasDownloadBackend()) {
			return Tf1PlusConf.USER_MESSAGE_TF1_DOWNLOAD_BACKEND_REQUIRED;
		}
		return Tf1PlusConf.USER_MESSAGE_PREMIUM_REPLAY_NOT_CONFIGURED;
	}

	/**
	 * Safe one-line status for logs (no secrets).
	 */
	static String configurationStatusForLog() {
		final StringBuilder status = new StringBuilder();
		if (computeConfigured()) {
			status.append("configured");
		} else {
			status.append("not-configured");
			if (!hasText(resolve(Tf1PlusConf.ENV_TF1_EMAIL, Tf1PlusConf.PROPERTY_EMAIL))) {
				status.append(";missing-tf1-email");
			}
			if (!hasText(resolve(Tf1PlusConf.ENV_TF1_PASSWORD, Tf1PlusConf.PROPERTY_PASSWORD))) {
				status.append(";missing-tf1-password");
			}
			if (!hasLocalDevice()) {
				status.append(";missing-or-invalid-device-path");
			}
			if (!hasDownloadBackend()) {
				status.append(";missing-download-backend");
			}
		}
		appendSourceHint(status, Tf1PlusConf.ENV_TF1_EMAIL, Tf1PlusConf.PROPERTY_EMAIL, "tf1-email");
		appendSourceHint(status, Tf1PlusConf.ENV_TF1_PASSWORD, Tf1PlusConf.PROPERTY_PASSWORD, "tf1-password");
		appendDevicePathSourceHint(status);
		appendSourceHint(status, Tf1PlusConf.ENV_N_M3U8DL_RE, Tf1PlusConf.PROPERTY_N_M3U8DL_RE, "n-m3u8dl-re");
		return status.toString();
	}

	/**
	 * Merged settings for the Python child process.
	 */
	static Map<String, String> buildProcessEnvironmentOverrides() {
		if (!computeConfigured()) {
			return null;
		}
		final Map<String, String> overrides = new HashMap<String, String>();
		putResolvedEnv(overrides, Tf1PlusConf.ENV_TF1_EMAIL, Tf1PlusConf.PROPERTY_EMAIL);
		putResolvedEnv(overrides, Tf1PlusConf.ENV_TF1_PASSWORD, Tf1PlusConf.PROPERTY_PASSWORD);
		putResolvedDevicePathEnv(overrides);
		putResolvedEnv(overrides, Tf1PlusConf.ENV_N_M3U8DL_RE, Tf1PlusConf.PROPERTY_N_M3U8DL_RE);
		putResolvedEnv(overrides, Tf1PlusConf.ENV_MEDIAFLOW_URL, Tf1PlusConf.PROPERTY_MEDIAFLOW_URL);
		putResolvedEnv(overrides, Tf1PlusConf.ENV_MEDIAFLOW_PASSWORD, Tf1PlusConf.PROPERTY_MEDIAFLOW_PASSWORD);
		putResolvedEnv(overrides, Tf1PlusConf.ENV_TF1_PYTHON, Tf1PlusConf.PROPERTY_PYTHON);
		return overrides.isEmpty() ? null : overrides;
	}

	static String pythonCommand() {
		final String configured = resolve(Tf1PlusConf.ENV_TF1_PYTHON, Tf1PlusConf.PROPERTY_PYTHON);
		if (hasText(configured)) {
			return configured;
		}
		return "python";
	}

	static String localDevicePath() {
		return resolveDevicePath();
	}

	static String nM3u8DlRePath() {
		return resolve(Tf1PlusConf.ENV_N_M3U8DL_RE, Tf1PlusConf.PROPERTY_N_M3U8DL_RE);
	}

	static String mediaflowUrl() {
		return resolve(Tf1PlusConf.ENV_MEDIAFLOW_URL, Tf1PlusConf.PROPERTY_MEDIAFLOW_URL);
	}

	private static boolean computeConfigured() {
		return hasText(resolve(Tf1PlusConf.ENV_TF1_EMAIL, Tf1PlusConf.PROPERTY_EMAIL))
				&& hasText(resolve(Tf1PlusConf.ENV_TF1_PASSWORD, Tf1PlusConf.PROPERTY_PASSWORD))
				&& hasLocalDevice()
				&& hasDownloadBackend();
	}

	private static boolean hasLocalDevice() {
		final String devicePath = resolveDevicePath();
		return hasText(devicePath) && new File(devicePath).isFile();
	}

	private static String resolveDevicePath() {
		final String fromPrimaryEnv = readEnv(Tf1PlusConf.ENV_DEVICE_PATH);
		if (hasText(fromPrimaryEnv)) {
			return fromPrimaryEnv;
		}
		final String fromLegacyEnv = readEnv(Tf1PlusConf.ENV_WVD_PATH);
		if (hasText(fromLegacyEnv)) {
			return fromLegacyEnv;
		}
		final String fromPrimaryProperty = readProperty(Tf1PlusConf.PROPERTY_DEVICE_PATH);
		if (hasText(fromPrimaryProperty)) {
			return fromPrimaryProperty;
		}
		return readProperty(Tf1PlusConf.PROPERTY_WVD_PATH);
	}

	private static void putResolvedDevicePathEnv(final Map<String, String> overrides) {
		final String devicePath = resolveDevicePath();
		if (hasText(devicePath)) {
			overrides.put(Tf1PlusConf.ENV_DEVICE_PATH, devicePath);
		}
	}

	private static void appendDevicePathSourceHint(final StringBuilder status) {
		if (hasText(readEnv(Tf1PlusConf.ENV_DEVICE_PATH))) {
			status.append(";device-path-source=env");
		} else if (hasText(readEnv(Tf1PlusConf.ENV_WVD_PATH))) {
			status.append(";device-path-source=legacy-env");
		} else if (hasText(readProperty(Tf1PlusConf.PROPERTY_DEVICE_PATH))) {
			status.append(";device-path-source=configuration-xml");
		} else if (hasText(readProperty(Tf1PlusConf.PROPERTY_WVD_PATH))) {
			status.append(";device-path-source=legacy-configuration-xml");
		}
	}

	private static boolean hasDownloadBackend() {
		final String nM3u8 = nM3u8DlRePath();
		if (hasText(nM3u8) && new File(nM3u8).isFile()) {
			return true;
		}
		return hasText(mediaflowUrl());
	}

	private static String resolve(final String envName, final String propertyName) {
		final String fromEnv = readEnv(envName);
		if (hasText(fromEnv)) {
			return fromEnv;
		}
		return readProperty(propertyName);
	}

	private static String readEnv(final String name) {
		final String value = System.getenv(name);
		return value == null ? "" : value.trim();
	}

	private static String readProperty(final String name) {
		final String value = System.getProperty(name);
		return value == null ? "" : value.trim();
	}

	private static void putResolvedEnv(final Map<String, String> overrides, final String envName,
			final String propertyName) {
		final String value = resolve(envName, propertyName);
		if (hasText(value)) {
			overrides.put(envName, value);
		}
	}

	private static void appendSourceHint(final StringBuilder status, final String envName, final String propertyName,
			final String label) {
		if (hasText(readEnv(envName))) {
			status.append(';').append(label).append("-source=env");
		} else if (hasText(readProperty(propertyName))) {
			status.append(';').append(label).append("-source=configuration-xml");
		}
	}

	private static boolean hasText(final String value) {
		return value != null && value.length() > 0;
	}
}
