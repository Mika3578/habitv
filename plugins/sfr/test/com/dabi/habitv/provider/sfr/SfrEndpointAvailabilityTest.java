package com.dabi.habitv.provider.sfr;

import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.exception.TechnicalException;

public class SfrEndpointAvailabilityTest {

	@Test
	public void isUnavailableForUnknownHost() {
		assertTrue(SfrEndpointAvailability.isUnavailable(
				new TechnicalException(new UnknownHostException("sport.sfr.fr"))));
	}

	@Test
	public void findCategoryReturnsEmptyWhenHostUnresolvable() {
		final SFRPluginManager plugin = new SFRPluginManager() {
			@Override
			protected String getUrlContent(final String url) {
				throw new TechnicalException(new UnknownHostException("sport.sfr.fr"));
			}
		};
		final Set<CategoryDTO> categories = plugin.findCategory();
		assertTrue(categories.isEmpty());
	}
}
