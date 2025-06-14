package com.dabi.habitv.plugin.ffmpeg;

import org.junit.Test;

import com.dabi.habitv.plugintester.BasePluginUpdateTester;

public class FFMPEGPluginDownloaderTest extends BasePluginUpdateTester{

	@Test
	public final void testFFMPEG() throws InstantiationException, IllegalAccessException, ReflectiveOperationException {
		testUpdatablePlugin(FFMPEGPluginDownloader.class);
	}

}
