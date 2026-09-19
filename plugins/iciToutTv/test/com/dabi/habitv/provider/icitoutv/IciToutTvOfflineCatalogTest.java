package com.dabi.habitv.provider.icitoutv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public class IciToutTvOfflineCatalogTest {

	@Test
	public void findCategoryParsesFreeShowsAndSkipsPremium() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put(IciToutTvUrls.freeCollectionUrl(), read("test/resources/fixtures/icitoutv/gratuit.html"));
		final IciToutTvPluginManager plugin = newRecordingPlugin(pages);
		final Set<CategoryDTO> categories = plugin.findCategory();
		assertEquals(4, categories.size());
		for (final CategoryDTO category : categories) {
			assertTrue(category.isDownloadable());
			assertTrue(category.getId().startsWith(IciToutTvConf.CATEGORY_SHOW_PREFIX));
			assertFalse(category.getId().contains("/"));
		}
	}

	@Test
	public void findEpisodeParsesNonPremiumLineupItems() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put("https://ici.tou.tv/tout-le-monde-en-parle",
				read("test/resources/fixtures/icitoutv/show-tlmep.html"));
		final IciToutTvPluginManager plugin = newRecordingPlugin(pages);
		final CategoryDTO show = new CategoryDTO(IciToutTvConf.NAME, "Tout le monde en parle",
				IciToutTvUrls.showCategoryId("tout-le-monde-en-parle"), IciToutTvConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(show);
		assertEquals(2, episodes.size());
		for (final EpisodeDTO episode : episodes) {
			assertTrue(IciToutTvUrls.isIciToutTvEpisodeUrl(episode.getId()));
			assertNotNull(episode.getMetadata());
			assertEquals(IciToutTvConf.CHANNEL_LABEL, episode.getMetadata().getChannel());
		}
	}

	@Test
	public void canDownloadAcceptsEpisodePathsOnly() {
		final IciToutTvPluginManager plugin = new IciToutTvPluginManager();
		assertEquals(DownloadableState.SPECIFIC,
				plugin.canDownload("https://ici.tou.tv/tout-le-monde-en-parle/s22e01"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("https://ici.tou.tv/tout-le-monde-en-parle"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("https://attacker.example/ici.tou.tv/foo/s01e01"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("https://user:pass@ici.tou.tv/foo/s01e01"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload(null));
	}

	@Test
	public void diagnosticsStripQueryAndFragment() {
		final IciToutTvDiagnostics diagnostics = new IciToutTvDiagnostics("download");
		diagnostics.setSourceUrl("https://ici.tou.tv/foo/s01e01?x=1#token=leak");
		final String line = diagnostics.formatLogLine();
		assertTrue(line.contains("sourceUrl=https://ici.tou.tv/foo/s01e01"));
		assertFalse(line.contains("token=leak"));
	}

	private static IciToutTvPluginManager newRecordingPlugin(final Map<String, String> pages) {
		return new IciToutTvPluginManager(new IciToutTvClient(new IciToutTvClient.ContentLoader() {
			@Override
			public String load(final String url) throws IOException {
				final String body = pages.get(url);
				if (body == null) {
					throw new IOException("unexpected " + url);
				}
				return body;
			}
		}));
	}

	private static String read(final String path) throws IOException {
		try (InputStream input = new FileInputStream(path)) {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final byte[] buffer = new byte[4096];
			int read;
			while ((read = input.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			return out.toString("UTF-8");
		}
	}
}
