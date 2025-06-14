package com.dabi.habitv.downloader.aria2;

import org.junit.Test;

import com.dabi.habitv.plugintester.BasePluginUpdateTester;

public class Aria2PluginDownloaderTest extends BasePluginUpdateTester {

	@Test
	public final void testAria2() throws InstantiationException, IllegalAccessException, ReflectiveOperationException {
		testUpdatablePlugin(Aria2PluginDownloader.class);
	}

}
