package com.dabi.habitv.plugin.youtube;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.tpl.TemplateUtils;
import com.google.common.collect.ImmutableMap;

public class YoutubePluginManagerApiDiagnosticsTest {

	private static final String API_KEY_PROPERTY = "habitv.youtube.apiKey";

	@Before
	public void setUpApiKey() {
		System.setProperty(API_KEY_PROPERTY, YoutubeTestSecrets.googleApiKeyPlaceholder());
	}

	@After
	public void tearDownApiKey() {
		System.clearProperty(API_KEY_PROPERTY);
	}

	@Test
	public void findEpisodeWithoutApiKeyReturnsEmptyWithoutNetwork() {
		System.clearProperty(API_KEY_PROPERTY);
		final AtomicBoolean networkCalled = new AtomicBoolean(false);
		final YoutubePluginManager manager = new YoutubePluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				networkCalled.set(true);
				fail("network must not be called when API key is missing");
				return null;
			}
		};
		final CategoryDTO leaf = downloadablePlaylistLeaf();

		assertTrue(manager.findEpisode(leaf).isEmpty());
		assertFalse(networkCalled.get());
	}

	@Test
	public void findEpisodeWithPathPrefixedApiKeyUsesExtractedKeyInUrl() {
		final String placeholder = YoutubeTestSecrets.googleApiKeyPlaceholder();
		System.setProperty(API_KEY_PROPERTY, "C:/Users/example/habitv/" + placeholder);
		final AtomicReference<String> requestedUrl = new AtomicReference<>();
		final YoutubePluginManager manager = new YoutubePluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				requestedUrl.set(url);
				return new java.io.ByteArrayInputStream("{\"items\":[]}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
			}
		};
		manager.findEpisode(downloadablePlaylistLeaf());
		assertNotNull(requestedUrl.get());
		assertTrue(YoutubeDataApiSupport.urlHasApiKey(requestedUrl.get()));
		assertTrue(requestedUrl.get().contains("key=" + placeholder));
		assertFalse(requestedUrl.get().contains("C:/Users"));
	}

	@Test
	public void findEpisodeOnHttp403ReturnsEmptyWithoutThrowing() {
		final YoutubePluginManager manager = new YoutubePluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				assertTrue("request URL must include API key", YoutubeDataApiSupport.urlHasApiKey(url));
				assertTrue("request URL must carry normalized key",
						url.contains("key=" + YoutubeConf.normalizeApiKey(System.getProperty(API_KEY_PROPERTY))));
				throw new TechnicalException(new java.io.IOException(
						"Server returned HTTP response code: 403 for URL: "
								+ YoutubeDataApiSupport.redactUrl(url)));
			}
		};
		final CategoryDTO leaf = downloadablePlaylistLeaf();

		final Set<EpisodeDTO> episodes = manager.findEpisode(leaf);
		assertTrue(episodes.isEmpty());
	}

	@Test
	public void nonRecoverableTechnicalExceptionMessageIsRedactedWithoutUnsafeCause() {
		assertNonRecoverableApiFailureDoesNotLeakSecret(unsafeHttp500TechnicalExceptionWithSecret());
	}

	@Test
	public void nonRecoverableIOExceptionMessageIsRedactedWithoutUnsafeCause() {
		final String secret = YoutubeTestSecrets.urlQuerySecret();
		final YoutubePluginManager manager = new YoutubePluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				return new InputStream() {
					@Override
					public int read() throws java.io.IOException {
						throw new java.io.IOException("Server returned HTTP response code: 500 for URL: "
								+ YoutubeDataApiSupport.appendApiKeyParam(
										"https://www.googleapis.com/youtube/v3/search?part=snippet", secret));
					}
				};
			}
		};
		try {
			manager.findEpisode(downloadablePlaylistLeaf());
			fail("expected TechnicalException");
		} catch (TechnicalException e) {
			assertSafeNonRecoverableApiFailure(e, secret);
		}
	}

	private static TechnicalException unsafeHttp500TechnicalExceptionWithSecret() {
		return new TechnicalException(unsafeHttp500WithSecret());
	}

	private static java.io.IOException unsafeHttp500WithSecret() {
		final String secret = YoutubeTestSecrets.urlQuerySecret();
		return new java.io.IOException("Server returned HTTP response code: 500 for URL: "
				+ YoutubeDataApiSupport.appendApiKeyParam(
						"https://www.googleapis.com/youtube/v3/search?part=snippet", secret));
	}

	private void assertNonRecoverableApiFailureDoesNotLeakSecret(final TechnicalException failure) {
		final String secret = YoutubeTestSecrets.urlQuerySecret();
		final YoutubePluginManager manager = new YoutubePluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				throw failure;
			}
		};
		try {
			manager.findEpisode(downloadablePlaylistLeaf());
			fail("expected TechnicalException");
		} catch (TechnicalException e) {
			assertSafeNonRecoverableApiFailure(e, secret);
		}
	}

	private static void assertSafeNonRecoverableApiFailure(final TechnicalException e, final String secret) {
		assertTrue(e.getMessage().contains("key=***"));
		assertTrue(e.getMessage().contains("provider=YouTube"));
		assertFalse(e.getMessage().contains(secret));
		assertNoRawSecretInCauseChain(e, secret);
	}

	private static void assertNoRawSecretInCauseChain(final Throwable throwable, final String secret) {
		for (Throwable current = throwable; current != null; current = current.getCause()) {
			if (current.getMessage() != null) {
				assertFalse(current.getMessage().contains(secret));
			}
		}
	}

	@Test
	public void findEpisodeOnHttp400ReturnsEmptyWithoutThrowing() {
		final YoutubePluginManager manager = new YoutubePluginManager() {
			@Override
			public InputStream getInputStreamFromUrl(final String url) {
				throw new TechnicalException(new java.io.IOException(
						"Server returned HTTP response code: 400 for URL: "
								+ YoutubeDataApiSupport.redactUrl(url)));
			}
		};

		assertTrue(manager.findEpisode(downloadablePlaylistLeaf()).isEmpty());
	}

	private CategoryDTO downloadablePlaylistLeaf() {
		final CategoryDTO playlistRoot = new CategoryDTO(YoutubeConf.NAME, "Playlist", "playlist-root",
				FrameworkConf.MP4);
		final CategoryDTO programmes = new CategoryDTO(YoutubeConf.NAME, "Programmes", "programmes", FrameworkConf.MP4);
		programmes.setDownloadable(false);
		final CategoryDTO leaf = TemplateUtils.buildSampleCat(YoutubeConf.NAME, "France24 Live EN",
				ImmutableMap.of("playlistId", "PLCUKIeZnrIUkh8TuvqH-uEdE5JHZWtk7x"));
		playlistRoot.addSubCategory(programmes);
		programmes.addSubCategory(leaf);
		return leaf;
	}
}
