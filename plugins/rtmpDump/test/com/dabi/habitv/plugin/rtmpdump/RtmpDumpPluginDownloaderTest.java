package com.dabi.habitv.plugin.rtmpdump;

import org.junit.Test;

import com.dabi.habitv.plugintester.BasePluginUpdateTester;

public class RtmpDumpPluginDownloaderTest extends BasePluginUpdateTester{

	@Test
	public final void testRtmpDump() throws InstantiationException, IllegalAccessException, ReflectiveOperationException {
		testUpdatablePlugin(RtmpDumpPluginDownloader.class);
	}

}
