package com.dabi.habitv.provider.sixplay;

import org.junit.Ignore;
import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class SixPlayPluginManagerTest extends BasePluginProviderTester {

	// Live catalogue depends on sitemap + SSR folder pages; keep ignored in default CI.
	@Test
	@Ignore
	public final void testProviderWat() throws InstantiationException, IllegalAccessException, DownloadFailedException {
		testPluginProvider(SixPlayPluginManager.class, true);
	}

}
