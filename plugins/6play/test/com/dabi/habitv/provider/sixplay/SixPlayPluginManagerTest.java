package com.dabi.habitv.provider.sixplay;

import org.junit.Ignore;
import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class SixPlayPluginManagerTest extends BasePluginProviderTester {

	// 6play.fr is now a JavaScript SPA; the legacy .folders__list / .mosaic-programs
	// markup that this scraper relies on no longer exists in the static HTML, so the
	// live integration test always returns an empty category list. Re-enable once
	// the provider is rewritten against the new site (or a recorded fixture).
	@Test
	@Ignore
	public final void testProviderWat() throws InstantiationException, IllegalAccessException, DownloadFailedException {
		testPluginProvider(SixPlayPluginManager.class, true);
	}

}
