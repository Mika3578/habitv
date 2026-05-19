package com.dabi.habitv;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import com.dabi.habitv.console.ConsoleLauncher;
import com.dabi.habitv.tray.HabiTvViewRunner;
import com.dabi.habitv.utils.LogUtils;

public class HabitvLauncher {

	public static void main(final String[] args) throws Exception {
		LogUtils.updateLog4jConfiguration();
		final boolean guiMode = args == null || args.length == 0;
		final File jfxrt = JavaFxRuntimeLocator.locate();
		if (jfxrt != null) {
			addToClasspath(jfxrt);
		} else if (guiMode) {
			System.err.println("JavaFX runtime (jfxrt.jar) was not found.");
			System.err.println("Set -D" + JavaFxRuntimeLocator.JFXRT_PATH_PROPERTY
					+ "=<path-to-jfxrt.jar> or use a JDK 8 that bundles JavaFX.");
			System.err.println("Checked paths:");
			for (final String path : JavaFxRuntimeLocator.candidatePathsForDiagnostics()) {
				System.err.println("  " + path);
			}
			System.exit(1);
		}

		if (guiMode) {
			HabiTvViewRunner.main(args);
		} else {
			ConsoleLauncher.main(args);
		}
	}

	private static void addToClasspath(final File jarFile) {
		try {
			final URL url = jarFile.toURI().toURL();
			final ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
			if (systemLoader instanceof URLClassLoader) {
				final Method addURL = URLClassLoader.class.getDeclaredMethod("addURL",
						URL.class);
				addURL.setAccessible(true);
				addURL.invoke(systemLoader, url);
			} else {
				throw new IllegalStateException(
						"System class loader is not a URLClassLoader: "
								+ systemLoader.getClass().getName());
			}
		} catch (final ReflectiveOperationException | java.net.MalformedURLException e) {
			throw new IllegalStateException("Failed to add JavaFX runtime to classpath: "
					+ jarFile.getAbsolutePath(), e);
		}
	}

}
