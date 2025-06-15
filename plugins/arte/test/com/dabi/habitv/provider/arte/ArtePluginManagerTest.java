package com.dabi.habitv.provider.arte;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class ArtePluginManagerTest extends BasePluginProviderTester {

	@Test
	public final void testArtePluginManager() throws InstantiationException, IllegalAccessException, DownloadFailedException, ReflectiveOperationException {
		testPluginProvider(ArtePluginManager.class, true);
	}
	
	@Test
	public final void testArteReplayCategory() throws InstantiationException, IllegalAccessException, DownloadFailedException, ReflectiveOperationException {
		ArtePluginManager manager = new ArtePluginManager();
		
		// Test that replay categories are created
		java.util.Set<CategoryDTO> categories = manager.findCategory();
		boolean foundReplay = false;
		for (CategoryDTO category : categories) {
			if (category.getName().equals("Replay")) {
				foundReplay = true;
				break;
			}
		}
		
		assert foundReplay : "Replay category should be found";
	}
}
