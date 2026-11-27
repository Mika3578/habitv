package com.dabi.habitv.provider.lequipe;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.exception.TechnicalException;

public class LEquipeEndpointAvailabilityTest {

	@Test
	public void isUnavailableForHttp403() {
		assertTrue(LEquipeEndpointAvailability.isUnavailable(
				new TechnicalException(new IOException(
						"Server returned HTTP response code: 403 for URL: https://video.lequipe.fr"))));
	}

	@Test
	public void findCategoryReturnsEmptyWhenCatalogueForbidden() {
		final LEquipePluginManager plugin = new LEquipePluginManager() {
			@Override
			protected String getUrlContent(final String url) {
				throw new TechnicalException(new IOException(
						"Server returned HTTP response code: 403 for URL: " + url));
			}
		};
		final Set<CategoryDTO> categories = plugin.findCategory();
		assertTrue(categories.isEmpty());
	}
}
