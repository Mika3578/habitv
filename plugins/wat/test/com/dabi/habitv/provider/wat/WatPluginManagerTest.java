package com.dabi.habitv.provider.wat;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class WatPluginManagerTest extends BasePluginProviderTester {

	@Test
	public final void testProviderWat() throws InstantiationException, IllegalAccessException, DownloadFailedException, ReflectiveOperationException {
		testPluginProvider(WatPluginManager.class, true);
	}

}
