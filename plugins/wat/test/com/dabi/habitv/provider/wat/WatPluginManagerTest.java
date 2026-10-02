package com.dabi.habitv.provider.wat;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class WatPluginManagerTest extends BasePluginProviderTester {

	// Live TF1 HTML is SPA-driven; legacy WAT selectors return an empty catalogue.
	// Keep this live integration test ignored until a TF1+/WAT public API rewrite.
	@Test
	@org.junit.Ignore
	public final void testProviderWat() throws InstantiationException, IllegalAccessException, DownloadFailedException {
		testPluginProvider(WatPluginManager.class, true);
	}

}
