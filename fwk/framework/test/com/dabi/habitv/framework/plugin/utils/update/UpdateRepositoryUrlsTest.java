package com.dabi.habitv.framework.plugin.utils.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.After;
import org.junit.Test;

import com.dabi.habitv.framework.FrameworkConf;

public class UpdateRepositoryUrlsTest {

	private String previousUpdateUrl;

	@After
	public void tearDown() {
		if (previousUpdateUrl == null) {
			System.clearProperty(FrameworkConf.UPDATE_URL_PROPERTY);
		} else {
			System.setProperty(FrameworkConf.UPDATE_URL_PROPERTY, previousUpdateUrl);
		}
	}

	@Test
	public void normalizeBaseUrlTrimsWhitespaceAndTrailingSlash() {
		assertEquals("https://example.invalid/repository",
				UpdateRepositoryUrls.normalizeBaseUrl("  https://example.invalid/repository/  "));
	}

	@Test
	public void normalizeBaseUrlRemovesDuplicateTrailingSlashes() {
		assertEquals("https://example.invalid/repository",
				UpdateRepositoryUrls.normalizeBaseUrl("https://example.invalid/repository///"));
	}

	@Test
	public void normalizeBaseUrlAllowsNull() {
		assertNull(UpdateRepositoryUrls.normalizeBaseUrl(null));
	}

	@Test
	public void normalizeBaseUrlPreservesCanonicalHttpsRuntimeBase() {
		assertEquals("https://mika3578.github.io/habitv-repo/repository",
				UpdateRepositoryUrls.normalizeBaseUrl(FrameworkConf.UPDATE_URL));
	}

	@Test
	public void buildRepositoryUrlUsesConfiguredFileDeploymentBase() throws Exception {
		final File repositoryRoot = File.createTempFile("habitv-repo-test-", "");
		if (!repositoryRoot.delete() || !repositoryRoot.mkdir()) {
			throw new IllegalStateException("Failed to create temp repository directory");
		}
		try {
			final String configuredBase = repositoryRoot.toURI().toString() + "///";
			previousUpdateUrl = System.setProperty(FrameworkConf.UPDATE_URL_PROPERTY, configuredBase);
			final String expectedBase = UpdateRepositoryUrls.normalizeBaseUrl(configuredBase);
			assertEquals(expectedBase + "/" + FrameworkConf.PLUGINS_LIST_FILE,
					UpdateRepositoryUrls.buildRepositoryUrl(FrameworkConf.PLUGINS_LIST_FILE));
		} finally {
			deleteRecursively(repositoryRoot);
		}
	}

	private static void deleteRecursively(final File file) {
		if (file.isDirectory()) {
			final File[] children = file.listFiles();
			if (children != null) {
				for (final File child : children) {
					deleteRecursively(child);
				}
			}
		}
		file.delete();
	}
}
