package com.dabi.habitv.provider.canalplus;

import org.junit.Test;

import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class CNewsPluginManagerTest extends BasePluginProviderTester {

	@Test
	public final void testProviderCNews() throws InstantiationException, IllegalAccessException {
		testPluginProvider(CNewsPluginManager.class, true);
	}

}
