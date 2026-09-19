package com.dabi.habitv.provider.globalnews;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;

public class GlobalNewsSpaListingOfflineTest {

	@Test
	public void findCategoryReturnsEmptyWhenLegacyNavigationMissing() {
		final GlobalNewsPluginManager plugin = new GlobalNewsPluginManager() {
			@Override
			protected String getUrlContent(final String url) {
				return "<!DOCTYPE html><html><body><p>no video-navigation</p></body></html>";
			}
		};
		final Set<CategoryDTO> categories = plugin.findCategory();
		assertTrue(categories.isEmpty());
	}

	@Test
	public void homeUrlUsesHttpsPublicHost() {
		assertEquals("https://globalnews.ca/national/videos/", GlobalNewsConf.VIDEO_HOME_URL);
		assertTrue(GlobalNewsConf.VIDEO_HOME_URL.startsWith("https://"));
	}
}
