package com.dabi.habitv.provider.canalplus;

import org.junit.Test;

import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class D8PluginManagerTest extends BasePluginProviderTester {

	@Test
	public final void testProviderD8() throws InstantiationException, IllegalAccessException, ReflectiveOperationException {
		testPluginProvider(D8PluginManager.class, true);
	}

}
