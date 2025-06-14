package com.dabi.habitv.plugin.curl;

import org.junit.Test;

import com.dabi.habitv.plugintester.BasePluginUpdateTester;

public class CurlPluginDownloaderTest extends BasePluginUpdateTester{

	@Test
	public final void testCurl() throws InstantiationException, IllegalAccessException, ReflectiveOperationException {
		testUpdatablePlugin(CurlPluginDownloader.class);
	}

}
