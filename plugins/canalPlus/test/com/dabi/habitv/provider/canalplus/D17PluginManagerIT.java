package com.dabi.habitv.provider.canalplus;

import org.junit.Test;

import com.dabi.habitv.plugintester.BasePluginProviderTester;

/**
 * This is a live provider smoke test and must not run as part of deterministic unit tests.
 */
public class D17PluginManagerIT extends BasePluginProviderTester {

	@Test
	public final void testProviderD17() throws InstantiationException, IllegalAccessException {
		testPluginProvider(D17PluginManager.class, true);
	}

}
