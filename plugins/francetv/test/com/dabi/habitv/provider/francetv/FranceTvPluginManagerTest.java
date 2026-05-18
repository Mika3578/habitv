package com.dabi.habitv.provider.francetv;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class FranceTvPluginManagerTest extends BasePluginProviderTester {

	@Test
	public void test() throws InstantiationException, IllegalAccessException, DownloadFailedException {
		testPluginProvider(FranceTvPluginManager.class, true);
	}

}
