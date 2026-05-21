package com.dabi.habitv.provider.canalplus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.TechnicalException;

public class CanalPlusEndpointAvailabilityTest {

	@Test
	public void shortCauseMessageReturnsUnknownCauseWhenThrowableIsNull() {
		assertEquals("unknown cause", CanalPlusEndpointAvailability.shortCauseMessage(null));
	}

	@Test
	public void shortCauseMessageFormatsUnknownHostException() {
		assertEquals("UnknownHostException: service.mycanal.fr",
				CanalPlusEndpointAvailability.shortCauseMessage(new UnknownHostException("service.mycanal.fr")));
	}

	@Test
	public void shortCauseMessageUsesDeepestRootCause() {
		RuntimeException nested = new RuntimeException(new TechnicalException(new UnknownHostException("service.mycanal.fr")));
		assertEquals("UnknownHostException: service.mycanal.fr", CanalPlusEndpointAvailability.shortCauseMessage(nested));
	}

	@Test
	public void shortCauseMessageFormatsHttp403Message() {
		TechnicalException wrapped = new TechnicalException(
				new IOException("Server returned HTTP response code: 403 for URL: https://www.canalplus.com/chaines/c8"));
		assertEquals("IOException: HTTP 403 for URL: https://www.canalplus.com/chaines/c8",
				CanalPlusEndpointAvailability.shortCauseMessage(wrapped));
	}

	@Test
	public void canalPlusCategoryDiscoveryReturnsEmptyWhenLegacyHostIsUnavailable() {
		CanalPlusPluginManager manager = new CanalPlusPluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				throw new TechnicalException(new UnknownHostException("service.mycanal.fr"));
			}
		};

		assertTrue(manager.findCategory().isEmpty());
	}

	@Test
	public void cStarCategoryDiscoveryReturnsEmptyWhenEndpointIsForbidden() {
		CStarPluginManager manager = new CStarPluginManager() {
			@Override
			protected String getUrlContent(final String url, final String encoding) {
				throw new TechnicalException(new IOException(
						"Server returned HTTP response code: 403 for URL: https://www.canalplus.com/chaines/cstar"));
			}
		};

		assertTrue(manager.findCategory().isEmpty());
	}

	@Test
	public void unavailableMessageIsStableForProviderLevelDiagnostics() {
		assertEquals(
				"canalPlus: Canal+ provider endpoint is no longer reachable or requires protected access.",
				CanalPlusEndpointAvailability.buildCategoryUnavailableMessage("canalPlus"));
	}

	@Test
	public void unavailableMessageIncludesShortCause() {
		assertEquals(
				"cstar: Canal+ provider endpoint is no longer reachable or requires protected access. Cause: IOException: HTTP 403 for URL: https://www.canalplus.com/chaines/cstar",
				CanalPlusEndpointAvailability.buildCategoryUnavailableMessage("cstar",
						new TechnicalException(new IOException(
								"Server returned HTTP response code: 403 for URL: https://www.canalplus.com/chaines/cstar"))));
	}
}
