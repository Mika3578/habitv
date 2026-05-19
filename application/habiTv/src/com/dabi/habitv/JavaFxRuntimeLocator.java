package com.dabi.habitv;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
