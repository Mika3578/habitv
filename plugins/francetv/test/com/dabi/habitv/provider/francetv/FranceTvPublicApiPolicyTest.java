package com.dabi.habitv.provider.francetv;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Guards against introducing resolver endpoints outside this PR scope.
 */
public class FranceTvPublicApiPolicyTest {

	private static final List<String> FORBIDDEN_SNIPPETS = Arrays.asList("getInfosOeuvre", "k7.ftven.fr",
			"yt-dlp");

	private static final List<String> SOURCE_FILES = Arrays.asList(
			"src/com/dabi/habitv/provider/francetv/FranceTvApiClient.java",
			"src/com/dabi/habitv/provider/francetv/FranceTvPluginManager.java",
			"src/com/dabi/habitv/provider/francetv/FranceTvPublicCategoryTreeBuilder.java",
			"src/com/dabi/habitv/provider/francetv/FranceTvPublicHubCatalog.java",
			"src/com/dabi/habitv/provider/francetv/FranceTvConf.java",
			"src/com/dabi/habitv/provider/francetv/FranceTvUrls.java");

	@Test
	public void publicApiDiscoverySourcesDoNotReferenceForbiddenResolvers() throws IOException {
		for (final String relativePath : SOURCE_FILES) {
			final String content = readUtf8(relativePath);
			for (final String snippet : FORBIDDEN_SNIPPETS) {
				assertFalse(relativePath + " must not reference " + snippet, content.contains(snippet));
			}
		}
	}

	@Test
	public void htmlFallbackDiscoveryDoesNotReferenceGetInfosOeuvre() throws IOException {
		final String content = readUtf8("src/com/dabi/habitv/provider/francetv/FranceTvPublicPageDiscovery.java");
		assertFalse(content.contains("getInfosOeuvre"));
		assertFalse(content.contains("k7.ftven.fr"));
	}

	private static String readUtf8(final String relativePath) throws IOException {
		final File file = new File(relativePath);
		assertTrue("missing " + relativePath, file.exists());
		final FileInputStream input = new FileInputStream(file);
		try {
			final byte[] buffer = new byte[(int) file.length()];
			final int read = input.read(buffer);
			return new String(buffer, 0, read, StandardCharsets.UTF_8);
		} finally {
			input.close();
		}
	}

}
