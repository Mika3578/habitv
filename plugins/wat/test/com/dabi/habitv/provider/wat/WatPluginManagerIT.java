package com.dabi.habitv.provider.wat;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

/**
 * This is a live provider smoke test and must not run as part of deterministic unit tests.
 */
public class WatPluginManagerIT extends BasePluginProviderTester {

	@Test
	public final void testProviderWat() throws InstantiationException, IllegalAccessException, DownloadFailedException {
		testPluginProvider(WatPluginManager.class, true);
	}

}
