package com.dabi.habitv;

import java.io.File;

import org.fuin.utils4j.Utils4J;
import org.apache.log4j.Logger;

import com.dabi.habitv.console.ConsoleLauncher;
import com.dabi.habitv.tray.HabiTvViewRunner;
import com.dabi.habitv.utils.HabitvLogger;

public class HabitvLauncher {

	private static final Logger LOG = HabitvLogger.getLogger(HabitvLauncher.class);

	public static void main(final String[] args) throws Exception {
		// Initialize unified logging system early
		HabitvLogger.initialize();
		
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
