package com.dabi.habitv.provider.sixplay;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class SixPlayPluginManagerTest extends BasePluginProviderTester {

	private static final Logger LOG = Logger.getLogger(SixPlayPluginManagerTest.class);

	@Test
	public final void testProviderWat() throws InstantiationException, IllegalAccessException, DownloadFailedException, ReflectiveOperationException {
		testPluginProvider(SixPlayPluginManager.class, true);
	}

	@Test
	public final void testDebugWebsite() throws Exception {
		SixPlayPluginManager plugin = new SixPlayPluginManager();
		
		// Test the findCategory method directly
		LOG.info("Testing findCategory method...");
		try {
			java.util.Set<com.dabi.habitv.api.plugin.dto.CategoryDTO> categories = plugin.findCategory();
			LOG.info("Categories found: " + categories.size());
			for (com.dabi.habitv.api.plugin.dto.CategoryDTO cat : categories) {
				LOG.info("Category: " + cat.getName() + " -> " + cat.getId());
			}
		} catch (Exception e) {
			LOG.error("Exception during findCategory: " + e.getMessage(), e);
		}
	}

}
