package com.dabi.habitv.plugin.youtube;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

/**
 * This is a live provider smoke test and must not run as part of deterministic unit tests.
 */
public class YoutubePluginManagerIT extends BasePluginProviderTester {

	@Test
	public final void test() throws InstantiationException, IllegalAccessException, DownloadFailedException {
		testPluginProvider(YoutubePluginManager.class, true);
	}

}
