package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dabi.habitv.provider.novo19.dto.Novo19BffPage;

public class Novo19CatalogClientTest {

	@Test
	public void fetchPageByPublicPathUsesFixtureLoader() throws Exception {
		final Novo19CatalogClient client = Novo19FixtureSupport.clientWithFixtures();
		final Novo19BffPage page = client.fetchPageByPublicPath("categories");
		assertEquals("RAILS", page.getType());
		assertEquals(2, page.getRails().size());
	}

	@Test
	public void ioErrorFromLoaderIsPropagated() {
		final Novo19CatalogClient client = new Novo19CatalogClient(new FailingLoader());
		try {
			client.fetchPageByPublicPath("categories");
		} catch (final Exception e) {
			assertTrue(e instanceof java.io.IOException);
			return;
		}
		throw new AssertionError("expected IOException");
	}

	@Test
	public void unavailableEnvelopeParsesToEmptyPage() throws Exception {
		final Novo19CatalogClient client = new Novo19CatalogClient(new SingleResponseLoader(
				Novo19FixtureSupport.readFixture("bff-unavailable.json")));
		final Novo19BffPage page = client.fetchPageByPublicPath("categories");
		assertTrue(page.getRails().isEmpty());
	}

	private static final class FailingLoader implements Novo19CatalogClient.ContentLoader {

		@Override
		public String load(final String url) throws java.io.IOException {
			throw new java.io.IOException("http-404");
		}

	}

	private static final class SingleResponseLoader implements Novo19CatalogClient.ContentLoader {

		private final String body;

		private SingleResponseLoader(final String body) {
			this.body = body;
		}

		@Override
		public String load(final String url) throws java.io.IOException {
			return body;
		}

	}

}
