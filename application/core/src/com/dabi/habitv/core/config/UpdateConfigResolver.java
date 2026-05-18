package com.dabi.habitv.core.config;

import org.apache.log4j.Logger;

import com.dabi.habitv.framework.FrameworkConf;

/**
 * Resolves update-related configuration values with optional JVM overrides.
 */
public final class UpdateConfigResolver {

	private static final Logger LOG = Logger.getLogger(UpdateConfigResolver.class);

	private UpdateConfigResolver() {
	}

	public static boolean resolveAutoriseSnapshot(final boolean fromConfiguration) {
		final String override = System.getProperty(FrameworkConf.UPDATE_AUTORISE_SNAPSHOT_PROPERTY);
		if (override == null) {
			return fromConfiguration;
		}
		final Boolean parsed = parseBoolean(override);
		if (parsed == null) {
			LOG.warn("Ignoring invalid " + FrameworkConf.UPDATE_AUTORISE_SNAPSHOT_PROPERTY
					+ " value \"" + override + "\"; using configuration value " + fromConfiguration);
			return fromConfiguration;
		}
		if (parsed.booleanValue() != fromConfiguration) {
			LOG.info("Overriding autoriseSnapshot from configuration (" + fromConfiguration
					+ ") with system property " + FrameworkConf.UPDATE_AUTORISE_SNAPSHOT_PROPERTY
					+ "=" + parsed);
		}
		return parsed.booleanValue();
	}

	private static Boolean parseBoolean(final String value) {
		if (value == null) {
			return null;
		}
		final String trimmed = value.trim();
		if ("true".equalsIgnoreCase(trimmed)) {
			return Boolean.TRUE;
		}
		if ("false".equalsIgnoreCase(trimmed)) {
			return Boolean.FALSE;
		}
		return null;
	}
}
