package com.dabi.habitv.provider.pluzz;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class PluzzPluginManagerTest extends BasePluginProviderTester {

	@Test
	public void test() throws InstantiationException, IllegalAccessException, DownloadFailedException, ReflectiveOperationException {
		testPluginProvider(PluzzPluginManager.class, true);
	}

}
