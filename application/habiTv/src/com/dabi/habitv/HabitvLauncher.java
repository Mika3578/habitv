package com.dabi.habitv;

import java.io.File;

import org.fuin.utils4j.Utils4J;

import com.dabi.habitv.console.ConsoleLauncher;
import com.dabi.habitv.tray.HabiTvViewRunner;
import com.dabi.habitv.utils.LogUtils;

public class HabitvLauncher {

	public static void main(final String[] args) throws Exception {
		LogUtils.updateLog4jConfiguration();
		Utils4J.addToClasspath("file:///" + System.getProperty("java.home")
				+ File.separator + "lib" + File.separator + "jfxrt.jar");
		System.out.println(System.getProperty("java.home"));

		if (args == null || args.length == 0) {
			HabiTvViewRunner.main(args);
		} else {
			ConsoleLauncher.main(args);
		}
	}

}
