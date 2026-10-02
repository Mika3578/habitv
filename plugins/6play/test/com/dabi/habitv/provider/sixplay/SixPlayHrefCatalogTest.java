package com.dabi.habitv.provider.sixplay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.junit.Test;

public class SixPlayHrefCatalogTest {

	@Test
	public void sitemapSnippetYieldsKnownFolderPaths() throws IOException {
		final String xml = readUtf8("test/resources/fixtures/6play/sitemap-service-snippet.xml");
		final Set<String> paths = SixPlayHrefCatalog.channelFolderPathsFromSitemap(xml);
		assertTrue(paths.contains("/w9/series-w9-f_18"));
		assertTrue(paths.contains("/m6/series-m6npu-f_106"));
		assertTrue(paths.contains("/gulli/les-tout-petits-gulli-f_659"));
		assertEquals("unknown channels must be ignored", 3, paths.size());
	}

	@Test
	public void programAndEpisodeHrefsParseFromMinimalHtml() throws IOException {
		final String folderHtml = readUtf8("test/resources/fixtures/6play/w9-series-folder-min.html");
		final Set<String> programs = SixPlayHrefCatalog.programPathsFromHtml(folderHtml);
		assertTrue(programs.contains("/desperate-housewives-p_840"));
		assertEquals(2, programs.size());

		final String programHtml = readUtf8("test/resources/fixtures/6play/smallville-program-min.html");
		final Set<String> episodes = SixPlayHrefCatalog.episodePathsFromHtml(programHtml);
		assertTrue(episodes.contains("/smallville-p_28305/s1-e1-bienvenue-sur-terre-c_13184405"));
		assertEquals(2, episodes.size());
	}

	@Test
	public void slugLabelsHumanizeProgramAndEpisodePaths() {
		assertEquals("Desperate Housewives",
				SixPlaySlugLabels.programTitleFromPath("/desperate-housewives-p_840"));
		assertTrue(SixPlaySlugLabels.episodeTitleFromPath(
				"/smallville-p_28305/s1-e1-bienvenue-sur-terre-c_13184405").contains("S1 E1"));
	}

	private static String readUtf8(final String relativePath) throws IOException {
		try (InputStream input = new FileInputStream(relativePath)) {
			final ByteArrayOutputStream output = new ByteArrayOutputStream();
			final byte[] buffer = new byte[4096];
			int read;
			while ((read = input.read(buffer)) != -1) {
				output.write(buffer, 0, read);
			}
			return output.toString("UTF-8");
		}
	}
}
