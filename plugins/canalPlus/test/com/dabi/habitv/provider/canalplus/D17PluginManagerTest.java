package com.dabi.habitv.provider.canalplus;

import org.junit.Test;

import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class D17PluginManagerTest extends BasePluginProviderTester {

	@Test
	public final void testProviderD17() throws InstantiationException, IllegalAccessException, ReflectiveOperationException {
		testPluginProvider(D17PluginManager.class, true);
	}

}
