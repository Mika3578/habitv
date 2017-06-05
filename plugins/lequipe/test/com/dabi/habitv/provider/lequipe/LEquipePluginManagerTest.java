package com.dabi.habitv.provider.lequipe;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class LEquipePluginManagerTest extends BasePluginProviderTester {
	
	@Test
	public final void testProviderLEquipe() throws InstantiationException, IllegalAccessException, DownloadFailedException {
		testPluginProvider(LEquipePluginManager.class, true);
	}

}
