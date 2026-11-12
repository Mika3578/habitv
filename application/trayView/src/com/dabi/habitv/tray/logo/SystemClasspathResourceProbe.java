package com.dabi.habitv.tray.logo;

/**
 * Default probe using the system class loader (same pattern as tray icons).
 */
public final class SystemClasspathResourceProbe implements ClasspathResourceProbe {

	@Override
	public boolean exists(final String classpathResource) {
		if (classpathResource == null || classpathResource.isEmpty()) {
			return false;
		}
		return ClassLoader.getSystemResource(classpathResource) != null;
	}
}
