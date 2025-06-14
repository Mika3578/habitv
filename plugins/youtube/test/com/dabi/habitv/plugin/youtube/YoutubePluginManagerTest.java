package com.dabi.habitv.plugin.youtube;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class YoutubePluginManagerTest extends BasePluginProviderTester {

	@Test
	public final void test() throws InstantiationException, IllegalAccessException, DownloadFailedException, ReflectiveOperationException {
		testPluginProvider(YoutubePluginManager.class, true);
	}

}
