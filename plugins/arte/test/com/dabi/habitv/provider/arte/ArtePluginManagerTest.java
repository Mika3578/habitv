package com.dabi.habitv.provider.arte;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class ArtePluginManagerTest extends BasePluginProviderTester {

	@Test
	public final void testArtePluginManager() throws InstantiationException, IllegalAccessException, DownloadFailedException {
		testPluginProvider(ArtePluginManager.class, true);
	}
}
