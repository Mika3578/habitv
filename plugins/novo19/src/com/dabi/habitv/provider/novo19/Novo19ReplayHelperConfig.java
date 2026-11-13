package com.dabi.habitv.provider.novo19;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

final class Novo19ReplayHelperConfig {

	private static final String PROP_DISABLED = "habitv.novo19.helper.disabled";

	private Novo19ReplayHelperConfig() {
	}

	static boolean isConfigured() {
		if (isDisabledByProperty()) {
			return false;
		}
		return hasLocalDevice() && hasDownloadBackend();
	}

	static boolean isDisabledByProperty() {
		return "true".equalsIgnoreCase(System.getProperty(PROP_DISABLED));
	}

	static String userFacingConfigurationMessage() {
		if (!hasLocalDevice()) {
			return Novo19Conf.DOWNLOAD_HELPER_MISSING_MESSAGE;
		}
		if (!hasDownloadBackend()) {
			return Novo19Conf.DOWNLOAD_HELPER_MISSING_MESSAGE;
		}
		return Novo19Conf.DOWNLOAD_HELPER_MISSING_MESSAGE;
	}

	static String pythonCommand() {
		final String configured = firstNonEmpty(
				System.getenv("NOVO19_PYTHON"),
				System.getProperty("habitv.novo19.python"),
				System.getenv("TF1_PYTHON"),
				System.getProperty("habitv.tf1plus.python"));
		if (StringUtils.isEmpty(configured)) {
			return "python";
		}
		return configured.trim();
	}

	static String nM3u8DlRePath() {
		return firstNonEmpty(
				System.getenv("N_M3U8DL_RE"),
				System.getProperty("habitv.novo19.nM3u8dlRe"),
				System.getProperty("habitv.tf1plus.nM3u8dlRe"));
	}

	static String resolveDevicePath() {
		final String fromEnv = firstNonEmpty(
				System.getenv("NOVO19_DEVICE_PATH"),
				System.getenv("WVD_PATH"),
				System.getenv("TF1_DEVICE_PATH"),
				System.getProperty("habitv.novo19.devicePath"),
				System.getProperty("habitv.tf1plus.devicePath"),
				System.getProperty("habitv.tf1plus.wvdPath"));
		if (StringUtils.isEmpty(fromEnv)) {
			return null;
		}
		final File file = new File(fromEnv.trim());
		if (file.isFile()) {
			return file.getAbsolutePath();
		}
		return null;
	}

	static Map<String, String> buildProcessEnvironmentOverrides() {
		final Map<String, String> env = new HashMap<String, String>();
		env.put("PYTHONUNBUFFERED", "1");
		putIfPresent(env, "N_M3U8DL_RE", nM3u8DlRePath());
		final String devicePath = resolveDevicePath();
		putIfPresent(env, "NOVO19_DEVICE_PATH", devicePath);
		putIfPresent(env, "WVD_PATH", devicePath);
		putIfPresent(env, "TF1_DEVICE_PATH", devicePath);
		return env;
	}

	private static boolean hasLocalDevice() {
		return !StringUtils.isEmpty(resolveDevicePath());
	}

	private static boolean hasDownloadBackend() {
		final String path = nM3u8DlRePath();
		return !StringUtils.isEmpty(path) && new File(path).isFile();
	}

	private static void putIfPresent(final Map<String, String> env, final String key, final String value) {
		if (!StringUtils.isEmpty(value)) {
			env.put(key, value);
		}
	}

	private static String firstNonEmpty(final String... values) {
		if (values == null) {
			return null;
		}
		for (final String value : values) {
			if (!StringUtils.isEmpty(value)) {
				return value;
			}
		}
		return null;
	}

}
