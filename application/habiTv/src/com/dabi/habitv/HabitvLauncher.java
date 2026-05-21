package com.dabi.habitv;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

/**
 * Resolves the JavaFX 2.x {@code jfxrt.jar} for JDK 8 layouts (Oracle, Zulu, etc.).
 */
final class JavaFxRuntimeLocator {

	static final String JFXRT_PATH_PROPERTY = "habitv.jfxrt.path";

	private static final String[] RELATIVE_PATHS = {
			"lib" + File.separator + "jfxrt.jar",
			"jre" + File.separator + "lib" + File.separator + "ext" + File.separator + "jfxrt.jar",
			"lib" + File.separator + "ext" + File.separator + "jfxrt.jar",
	};

	private JavaFxRuntimeLocator() {
	}

	static File locate() {
		final String override = System.getProperty(JFXRT_PATH_PROPERTY);
		if (override != null && !override.trim().isEmpty()) {
			final File overrideFile = new File(override.trim());
			if (overrideFile.isFile()) {
				return overrideFile;
			}
		}
		final File javaHome = new File(System.getProperty("java.home"));
		for (final String relativePath : RELATIVE_PATHS) {
			final File candidate = new File(javaHome, relativePath);
			if (candidate.isFile()) {
				return candidate;
			}
		}
		final File jdkLib = parentJdkLibCandidate(javaHome);
		if (jdkLib != null && jdkLib.isFile()) {
			return jdkLib;
		}
		return null;
	}

	static List<String> candidatePathsForDiagnostics() {
		final List<String> paths = new ArrayList<>();
		final String override = System.getProperty(JFXRT_PATH_PROPERTY);
		if (override != null && !override.trim().isEmpty()) {
			paths.add(override.trim());
		}
		final File javaHome = new File(System.getProperty("java.home"));
		for (final String relativePath : RELATIVE_PATHS) {
			paths.add(new File(javaHome, relativePath).getAbsolutePath());
		}
		final File jdkLib = parentJdkLibCandidate(javaHome);
		if (jdkLib != null) {
			paths.add(jdkLib.getAbsolutePath());
		}
		return Collections.unmodifiableList(paths);
	}

	private static File parentJdkLibCandidate(final File javaHome) {
		final File parent = javaHome.getParentFile();
		if (parent == null) {
			return null;
		}
		return new File(parent, "lib" + File.separator + "jfxrt.jar");
	}
}
