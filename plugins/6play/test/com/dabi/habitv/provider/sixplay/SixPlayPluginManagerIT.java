package com.dabi.habitv.provider.sixplay;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class SixPlayPluginManagerIT extends BasePluginProviderTester {

	@Test
	public final void testProviderWat() throws InstantiationException, IllegalAccessException, DownloadFailedException {
		testPluginProvider(SixPlayPluginManager.class, true);
	}

}
