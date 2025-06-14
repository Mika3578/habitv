package com.dabi.habitv.provider.beinsport;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class BeinSportPluginManagerTest extends BasePluginProviderTester {

	@Test
	public final void testBeinSport() throws InstantiationException, IllegalAccessException, DownloadFailedException, ReflectiveOperationException {
		testPluginProvider(BeinSportPluginManager.class, true);
	}
}
