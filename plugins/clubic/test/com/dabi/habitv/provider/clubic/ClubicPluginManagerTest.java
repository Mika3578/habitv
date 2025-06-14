package com.dabi.habitv.provider.clubic;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class ClubicPluginManagerTest extends BasePluginProviderTester {

	@Test
	public final void testProviderClubic() throws InstantiationException, IllegalAccessException, DownloadFailedException, ReflectiveOperationException {
		testPluginProvider(ClubicPluginManager.class, true);
	}

}
