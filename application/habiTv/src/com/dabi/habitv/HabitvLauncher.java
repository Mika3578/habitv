package com.dabi.habitv;

import java.io.File;

import org.fuin.utils4j.Utils4J;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.dabi.habitv.console.ConsoleLauncher;
import com.dabi.habitv.tray.HabiTvViewRunner;
import com.dabi.habitv.utils.HabitvLogger;

public class HabitvLauncher {

	private static Logger LOG;

	public static void main(final String[] args) {
		// Crée le dossier de logs utilisateur si besoin
		File userDir = new File(System.getProperty("user.home") + "/habitv");
		if (!userDir.exists() && !userDir.mkdirs()) {
			System.err.println("Impossible de créer le dossier utilisateur: " + userDir.getAbsolutePath());
		}

		// Initialize unified logging system early
		// This will load log4j.properties from the classpath
		HabitvLogger.initialize();

		// Initialize logger after logging system is configured
		LOG = HabitvLogger.getLogger(HabitvLauncher.class);

		Utils4J.addToClasspath("file:///" + System.getProperty("java.home")
				+ File.separator + "lib" + File.separator + "jfxrt.jar");
		LOG.info("Java home: " + System.getProperty("java.home"));
		LOG.info("habiTv application starting...");

		if (args == null || args.length == 0) {
			LOG.info("Starting GUI mode");
			HabiTvViewRunner.main(args);
		} else {
			LOG.info("Starting console mode with " + args.length + " arguments");
			ConsoleLauncher.main(args);
		}
	}

}
