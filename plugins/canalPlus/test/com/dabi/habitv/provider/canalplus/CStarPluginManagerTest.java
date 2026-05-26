package com.dabi.habitv.provider.canalplus;

import org.junit.Test;

import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class CStarPluginManagerTest extends BasePluginProviderTester {

	@Test
	public final void testProviderCStar() throws InstantiationException, IllegalAccessException {
		testPluginProvider(CStarPluginManager.class, true);
	}

}
