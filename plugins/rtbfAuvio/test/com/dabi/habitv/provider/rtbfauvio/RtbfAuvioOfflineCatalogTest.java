package com.dabi.habitv.provider.rtbfauvio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;

public class RtbfAuvioOfflineCatalogTest {

	@Test
	public void findCategoryExposesNativeTvChannels() {
		final RtbfAuvioPluginManager plugin = new RtbfAuvioPluginManager();
		final Set<CategoryDTO> categories = plugin.findCategory();
		assertEquals(3, categories.size());
		boolean foundUne = false;
		for (final CategoryDTO category : categories) {
			assertTrue(category.getId().contains("/chaine/"));
			if ("La Une".equals(category.getName())) {
				foundUne = true;
			}
		}
		assertTrue(foundUne);
	}

	@Test
	public void findEpisodeUsesMediaListWidgets() throws IOException {
		final Map<String, String> pages = new HashMap<String, String>();
		pages.put("https://auvio.rtbf.be/chaine/la-une-1",
				read("test/resources/fixtures/rtbfAuvio/channel-la-une.html"));
		pages.put("https://bff-service.rtbf.be/auvio/v1.23/widgets/19288",
				read("test/resources/fixtures/rtbfAuvio/widget-19288.json"));
		pages.put("https://bff-service.rtbf.be/auvio/v1.23/widgets/19488",
				read("test/resources/fixtures/rtbfAuvio/widget-19488.json"));
		final RtbfAuvioPluginManager plugin = new RtbfAuvioPluginManager(new RtbfAuvioClient(
				new RtbfAuvioClient.ContentLoader() {
					@Override
					public String load(final String url) throws IOException {
						final String body = pages.get(url);
						if (body == null) {
							throw new IOException("unexpected " + url);
						}
						return body;
					}
				}));
		final CategoryDTO channel = new CategoryDTO(RtbfAuvioConf.NAME, "La Une",
				"https://auvio.rtbf.be/chaine/la-une-1", RtbfAuvioConf.EXTENSION);
		final Set<EpisodeDTO> episodes = plugin.findEpisode(channel);
		assertFalse(episodes.isEmpty());
		for (final EpisodeDTO episode : episodes) {
			assertTrue(episode.getId().contains("auvio.rtbf.be/media/"));
			assertEquals("La Une", episode.getMetadata().getChannel());
		}
	}

	@Test
	public void downloadFailsClosedForRedbeeDrm() {
		final RtbfAuvioPluginManager plugin = new RtbfAuvioPluginManager();
		final DownloadParamDTO param = new DownloadParamDTO("https://auvio.rtbf.be/media/demo-1", "/tmp/x.mp4",
				RtbfAuvioConf.EXTENSION);
		try {
			plugin.download(param, null);
			throw new AssertionError("expected failure");
		} catch (final DownloadFailedException e) {
			assertEquals(RtbfAuvioConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e.getMessage());
		}
	}

	@Test
	public void canDownloadAcceptsAuvioMediaUrls() {
		final RtbfAuvioPluginManager plugin = new RtbfAuvioPluginManager();
		assertEquals(DownloadableState.SPECIFIC,
				plugin.canDownload("https://auvio.rtbf.be/media/alex-hugo-3517841"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload("https://www.rtbf.be/"));
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
