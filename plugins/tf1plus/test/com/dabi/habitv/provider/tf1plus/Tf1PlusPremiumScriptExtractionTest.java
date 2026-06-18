package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.junit.Test;

import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;

public class Tf1PlusPremiumScriptExtractionTest {

	@Test
	public void shouldResolveScriptFromTf1PlusJarInPluginsDirectory() throws Exception {
		final File sourceJar = new File("target/tf1plus-4.1.0-SNAPSHOT.jar");
		if (!sourceJar.isFile()) {
			return;
		}
		final File pluginsDir = new File("target/test-plugins-dir");
		pluginsDir.mkdirs();
		final File deployedJar = new File(pluginsDir, "tf1plus-4.1.0-SNAPSHOT.jar");
		Files.copy(sourceJar.toPath(), deployedJar.toPath(), StandardCopyOption.REPLACE_EXISTING);

		final File extracted = new File(pluginsDir, "tf1plus_premium_download.py");
		if (extracted.isFile()) {
			extracted.delete();
		}

		final DownloaderPluginHolder downloaders = new DownloaderPluginHolder("cmd", null, null, "out", "index",
				"bin", pluginsDir.getAbsolutePath());
		final String path = invokeResolveScriptPath(downloaders);
		assertNotNull(path);
		assertTrue(new File(path).isFile());
		assertTrue(new File(path).length() > 0L);
		assertTrue(path.contains(pluginsDir.getAbsolutePath()));
	}

	private static String invokeResolveScriptPath(final DownloaderPluginHolder downloaders) throws Exception {
		final java.lang.reflect.Method method = Tf1PlusPremiumDownloadExecutor.class
				.getDeclaredMethod("resolveScriptPath", String.class);
		method.setAccessible(true);
		return (String) method.invoke(null, downloaders.getPluginDir());
	}
}
