package com.dabi.habitv.provider.canalplus;

import org.junit.Test;

import com.dabi.habitv.plugintester.BasePluginProviderTester;

/**
 * This is a live provider smoke test and must not run as part of deterministic unit tests.
 */
public class D8PluginManagerIT extends BasePluginProviderTester {

	@Test
	public final void testProviderD8() throws InstantiationException, IllegalAccessException {
		testPluginProvider(D8PluginManager.class, true);
	}

}
