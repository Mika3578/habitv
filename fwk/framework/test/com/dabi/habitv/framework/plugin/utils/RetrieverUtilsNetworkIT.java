package com.dabi.habitv.framework.plugin.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * This is a network-dependent smoke test and must not run as part of deterministic unit tests.
 */
public class RetrieverUtilsNetworkIT {

	@Test(timeout = 10000)
	public void shouldRetrieveTitleFromStablePublicWebsite() {
		String title = RetrieverUtils.getTitleByUrl("https://example.com/");
		assertNotNull("Page title must not be null", title);
		assertFalse("Page title must not be empty", title.isEmpty());
		assertTrue("Page title must contain Example Domain", title.contains("Example Domain"));
	}

}
