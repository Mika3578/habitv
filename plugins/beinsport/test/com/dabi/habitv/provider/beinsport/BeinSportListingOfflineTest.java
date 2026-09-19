package com.dabi.habitv.provider.beinsport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;

public class BeinSportListingOfflineTest {

	@Test
	public void homeUrlUsesHttpsPublicHost() {
		assertEquals("https://www.beinsports.com/us/videos", BeinSportConf.VIDEOS_URL);
		assertTrue(BeinSportConf.HOME_URL.startsWith("https://"));
	}

	@Test
	public void findCategoryKeepsRootWhenLegacySelectBoxMissing() throws IOException {
		final BeinSportPluginManager plugin = new BeinSportPluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				return new ByteArrayInputStream(
						"<!DOCTYPE html><html><body><p>no bein-selectBox</p></body></html>"
								.getBytes(StandardCharsets.UTF_8));
			}
		};
		final Set<CategoryDTO> categories = plugin.findCategory();
		assertEquals(1, categories.size());
		assertTrue(categories.iterator().next().getSubCategories().isEmpty());
	}
}
