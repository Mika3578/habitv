package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Novo19UrlBuilderTest {

	@Test
	public void stripsQueryAndFragmentFromPublicPathResolution() {
		assertEquals("/details/sample",
				Novo19UrlBuilder.publicPathFromCategoryId(
						"https://novo19.ouest-france.fr/details/sample?autoplay=1#player"));
	}

	@Test
	public void stripsQueryAndFragmentFromBffPagePath() {
		final String url = Novo19UrlBuilder.bffPageByPath("/details/sample?foo=1#bar");
		assertEquals("https://novo19-bff.ouest-france.fr/api/1/public/frontends/web/pages/by-path/details/sample",
				url);
	}

	@Test
	public void rejectsUnapprovedBffHost() {
		try {
			Novo19UrlBuilder.bffAbsolutePath("https://evil.example.test/api/1/public/frontends/web/pages/categories");
			throw new AssertionError("expected IOException");
		} catch (final java.io.IOException e) {
			assertTrue(e.getMessage().contains("unapproved-bff-host"));
		}
	}

	@Test
	public void acceptsApprovedBffHost() throws Exception {
		assertEquals("https://novo19-bff.ouest-france.fr/api/1/public/frontends/web/pages/categories",
				Novo19UrlBuilder.bffAbsolutePath(
						"https://novo19-bff.ouest-france.fr/api/1/public/frontends/web/pages/categories"));
	}

	@Test
	public void canDownloadUsesParsedHostOnly() {
		assertTrue(Novo19UrlBuilder.isApprovedPublicDownloadUrl("https://novo19.ouest-france.fr/player/sample"));
		assertFalse(Novo19UrlBuilder.isApprovedPublicDownloadUrl("https://evil.com/novo19.ouest-france.fr/player"));
	}

}
