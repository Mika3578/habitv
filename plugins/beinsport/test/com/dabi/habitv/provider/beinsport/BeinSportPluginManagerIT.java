package com.dabi.habitv.provider.beinsport;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

/**
 * This is a live provider smoke test and must not run as part of deterministic unit tests.
 */
public class BeinSportPluginManagerIT extends BasePluginProviderTester {

	@Test
	public final void testBeinSport() throws InstantiationException, IllegalAccessException, DownloadFailedException {
		testPluginProvider(BeinSportPluginManager.class, true);
	}
}
