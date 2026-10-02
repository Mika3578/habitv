package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.Test;

import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;

public class Tf1PlusReplayHelperScriptExtractionTest {

	private static final String SCRIPT_ENTRY = "scripts/tf1plus_helper.py";

	@Test
	public void shouldResolveScriptFromTf1PlusJarInPluginsDirectory() throws Exception {
		final File pluginsDir = new File("target/test-plugins-dir");
		pluginsDir.mkdirs();
		// Deterministic fixture jar: the test never depends on a packaged
		// artifact version and never silently skips when the jar is absent.
		final File fixtureJar = new File(pluginsDir, "tf1plus-fixture.jar");
		buildFixtureJar(fixtureJar);

		final File extracted = new File(pluginsDir, "tf1plus_helper.py");
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

	private static void buildFixtureJar(final File jarFile) throws IOException {
		final File script = new File(SCRIPT_ENTRY);
		assertTrue("bundled script not found: " + script.getAbsolutePath(), script.isFile());
		try (JarOutputStream out = new JarOutputStream(new FileOutputStream(jarFile))) {
			out.putNextEntry(new JarEntry(SCRIPT_ENTRY));
			java.nio.file.Files.copy(script.toPath(), out);
			out.closeEntry();
		}
	}

	private static String invokeResolveScriptPath(final DownloaderPluginHolder downloaders) throws Exception {
		final java.lang.reflect.Method method = Tf1PlusReplayHelperExecutor.class
				.getDeclaredMethod("resolveScriptPath", String.class);
		method.setAccessible(true);
		return (String) method.invoke(null, downloaders.getPluginDir());
	}
}