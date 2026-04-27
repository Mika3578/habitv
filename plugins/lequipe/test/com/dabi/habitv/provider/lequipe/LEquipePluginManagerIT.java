package com.dabi.habitv.provider.lequipe;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

/**
 * This is a live provider smoke test and must not run as part of deterministic unit tests.
 */
public class LEquipePluginManagerIT extends BasePluginProviderTester {
	
	@Test
	public final void testProviderLEquipe() throws InstantiationException, IllegalAccessException, DownloadFailedException {
		testPluginProvider(LEquipePluginManager.class, true);
	}

}
