package com.dabi.habitv.tray.logo;

/**
 * Probe for classpath resources. Extracted for deterministic unit tests.
 */
public interface ClasspathResourceProbe {

	boolean exists(String classpathResource);
}
