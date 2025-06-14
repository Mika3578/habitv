package com.dabi.habitv.provider.sixplay;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class SixPlayPluginManagerTest extends BasePluginProviderTester {

	@Test
	public final void testProviderWat() throws InstantiationException, IllegalAccessException, DownloadFailedException, ReflectiveOperationException {
		testPluginProvider(SixPlayPluginManager.class, true);
	}

	@Test
	public final void testDebugWebsite() throws Exception {
		SixPlayPluginManager plugin = new SixPlayPluginManager();
		
		// Test the findCategory method directly
		System.out.println("Testing findCategory method...");
		try {
			java.util.Set<com.dabi.habitv.api.plugin.dto.CategoryDTO> categories = plugin.findCategory();
			System.out.println("Categories found: " + categories.size());
			for (com.dabi.habitv.api.plugin.dto.CategoryDTO cat : categories) {
				System.out.println("Category: " + cat.getName() + " -> " + cat.getId());
			}
		} catch (Exception e) {
			System.out.println("Exception during findCategory: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
