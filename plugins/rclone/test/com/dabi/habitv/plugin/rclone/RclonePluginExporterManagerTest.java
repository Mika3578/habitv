package com.dabi.habitv.plugin.rclone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class RclonePluginExporterManagerTest {

	@Test
	public void getNameReturnsRclone() {
		assertEquals("rclone", new RclonePluginExporterManager().getName());
	}

	@Test
	public void exportReturnsExecutorWithProvidedCmd() throws Exception {
		final RclonePluginExporterManager manager = new RclonePluginExporterManager();
		assertNotNull(manager.export("/bin/sh -c #CMD#",
				"rclone copy /tmp/foo remote:bar"));
	}
}
