package com.dabi.habitv.provider.footyroom;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class FootyroomPluginManagerTest extends BasePluginProviderTester {
	
	@Test
	public final void test() throws InstantiationException, IllegalAccessException, DownloadFailedException, ReflectiveOperationException {
		testPluginProvider(FootyroomPluginManager.class, true);
	}
}
