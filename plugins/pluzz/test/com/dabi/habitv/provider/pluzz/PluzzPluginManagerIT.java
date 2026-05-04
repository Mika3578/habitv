package com.dabi.habitv.provider.pluzz;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

/**
 * This is a live provider smoke test and must not run as part of deterministic unit tests.
 */
public class PluzzPluginManagerIT extends BasePluginProviderTester {

	@Test
	public void test() throws InstantiationException, IllegalAccessException, DownloadFailedException {
		testPluginProvider(PluzzPluginManager.class, true);
	}

}
