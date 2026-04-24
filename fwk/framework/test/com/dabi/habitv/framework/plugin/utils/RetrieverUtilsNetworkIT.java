package com.dabi.habitv.framework.plugin.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * This is a network-dependent smoke test and must not run as part of deterministic unit tests.
 */
public class RetrieverUtilsNetworkIT {

	@Test
	public void shouldRetrieveBeinSportsTitleFromLiveWebsite() {
		String title = RetrieverUtils.getTitleByUrl("https://www.beinsports.com/fr-fr/videos");
		assertNotNull("Page title must not be null", title);
		assertFalse("Page title must not be empty", title.isEmpty());
	}

}
