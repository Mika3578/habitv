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
		addToClasspath("file:///" + System.getProperty("java.home")
				+ File.separator + "lib" + File.separator + "jfxrt.jar");
		System.out.println(System.getProperty("java.home"));

		if (args == null || args.length == 0) {
			HabiTvViewRunner.main(args);
		} else {
			ConsoleLauncher.main(args);
		}
	}

	private static void addToClasspath(final String urlSpec) {
		try {
			final URL url = new URL(urlSpec);
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
			throw new IllegalStateException("Failed to add URL to classpath: "
					+ urlSpec, e);
		}
	}

}
