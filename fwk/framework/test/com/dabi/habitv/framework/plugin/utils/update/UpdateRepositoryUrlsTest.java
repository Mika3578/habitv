package com.dabi.habitv.framework.plugin.utils.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class UpdateRepositoryUrlsTest {

	@Test
	public void normalizeBaseUrlTrimsWhitespaceAndTrailingSlash() {
		assertEquals("https://example.invalid/repository",
				UpdateRepositoryUrls.normalizeBaseUrl("  https://example.invalid/repository/  "));
	}

	@Test
	public void normalizeBaseUrlAllowsNull() {
		assertNull(UpdateRepositoryUrls.normalizeBaseUrl(null));
	}
}
