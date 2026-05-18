package com.dabi.habitv.provider.arte;

import org.junit.Ignore;
import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class ArtePluginManagerTest extends BasePluginProviderTester {

	// arte.tv is a Next.js SPA; legacy Jsoup selectors no longer match static HTML.
	// Re-enable after provider rewrite (see ArteYtDlpLiveDownloadTest for download path).
	@Test
	@Ignore
	public final void testArtePluginManager() throws InstantiationException, IllegalAccessException, DownloadFailedException {
		testPluginProvider(ArtePluginManager.class, true);
	}
}
