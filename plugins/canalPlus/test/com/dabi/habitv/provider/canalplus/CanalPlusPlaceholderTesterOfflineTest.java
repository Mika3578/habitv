package com.dabi.habitv.provider.canalplus;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class CanalPlusPlaceholderTesterOfflineTest extends BasePluginProviderTester {

	@Test
	public void canalPlusPlaceholderFailsFastWithoutRetryLoop() {
		final CanalPlusPluginManager manager = new CanalPlusPluginManager() {
			@Override
			public java.io.InputStream getInputStreamFromUrl(final String url) {
				throw new TechnicalException(new java.net.UnknownHostException("service.mycanal.fr"));
			}
		};
		try {
			testPluginProvider(manager, false);
			fail("expected assertion when Canal+ discovery returns only a placeholder");
		} catch (AssertionError expected) {
			assertTrue(expected.getMessage().contains("no downloadable category"));
		} catch (DownloadFailedException unexpected) {
			throw new AssertionError("unexpected download failure", unexpected);
		}
	}

	@Test
	public void cStarPlaceholderFailsFastWithoutRetryLoop() {
		final CStarPluginManager manager = new CStarPluginManager() {
			@Override
			protected String getUrlContent(final String url, final String encoding) {
				throw new TechnicalException(new IOException(
						"Server returned HTTP response code: 403 for URL: https://www.canalplus.com/chaines/cstar"));
			}
		};
		try {
			testPluginProvider(manager, true);
			fail("expected assertion when CStar discovery returns only a placeholder");
		} catch (AssertionError expected) {
			assertTrue(expected.getMessage().contains("no downloadable category"));
		} catch (DownloadFailedException unexpected) {
			throw new AssertionError("unexpected download failure", unexpected);
		}
	}
}
