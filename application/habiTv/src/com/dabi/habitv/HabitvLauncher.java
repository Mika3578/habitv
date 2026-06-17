package com.dabi.habitv;

import com.dabi.habitv.console.ConsoleLauncher;
import com.dabi.habitv.tray.HabiTvViewRunner;
import com.dabi.habitv.utils.LogUtils;

public class HabitvLauncher {

	private static final String JAVAFX_APPLICATION_CLASS = "javafx.application.Application";

	public static void main(final String[] args) throws Exception {
		LogUtils.updateLog4jConfiguration();

		if (isGuiMode(args)) {
			if (isJavaFxAvailable()) {
				HabiTvViewRunner.main(args);
			} else {
				System.err.println(getJavaFxMissingMessage());
				System.exit(1);
			}
		} else {
			ConsoleLauncher.main(args);
		}
	}

	static boolean isGuiMode(final String[] args) {
		return args == null || args.length == 0;
	}

	static boolean isJavaFxAvailable() {
		try {
			Class.forName(JAVAFX_APPLICATION_CLASS);
			return true;
		} catch (final ClassNotFoundException e) {
			return false;
		}
	}

	static String getJavaFxMissingMessage() {
		return "ERROR: JavaFX is not available on this JVM.\n\n"
				+ "Habitv GUI mode requires JavaFX (OpenJFX) to run.\n"
				+ "To fix this, choose one of the following options:\n\n"
				+ "  1. Use a JDK that bundles JavaFX (e.g. Liberica Full JDK, Azul Zulu FX)\n"
				+ "  2. Download OpenJFX from https://openjfx.io and add it to the module-path\n"
				+ "  3. Run Habitv in CLI mode by passing a command-line argument (e.g. --help)\n";
	}

}
