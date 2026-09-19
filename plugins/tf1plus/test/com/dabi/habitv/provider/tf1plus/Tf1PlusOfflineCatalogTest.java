package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

public class Tf1PlusOfflineCatalogTest {

	private static final String PROGRAMS_TMC = "test/resources/fixtures/tf1plus/programs-tmc.json";
	private static final String VIDEOS_QUOTIDIEN = "test/resources/fixtures/tf1plus/videos-quotidien.json";

	@Test
	public void findCategoryBuildsNativeChannelsAndProgramsFromGraphql() throws IOException {
		final Map<String, String> fixtures = new HashMap<String, String>();
		fixtures.put("channel%22%3A%22tmc", readFixture(PROGRAMS_TMC));
		fixtures.put("id=483ce0f", "{\"data\":{\"programs\":{\"items\":[]}}}");
		final Tf1PlusPluginManager plugin = newRecordingPlugin(fixtures);

		final Set<CategoryDTO> categories = plugin.findCategory();
		assertEquals(Tf1PlusConf.CHANNELS.length, categories.size());

		CategoryDTO tmc = null;
		for (final CategoryDTO channel : categories) {
			assertFalse(channel.isDownloadable());
			assertTrue(channel.getId().startsWith(Tf1PlusConf.CATEGORY_CHANNEL_PREFIX));
			if ("TMC".equals(channel.getName())) {
				tmc = channel;
			}
		}
		assertNotNull(tmc);
		assertFalse(tmc.getSubCategories().isEmpty());
		boolean foundQuotidien = false;
		for (final CategoryDTO program : tmc.getSubCategories()) {
			assertTrue(program.isDownloadable());
			assertTrue(program.getId().startsWith(Tf1PlusConf.CATEGORY_PROGRAM_PREFIX));
			assertFalse(program.getName().toLowerCase().contains("novo19"));
			if (program.getId().contains("quotidien-avec-yann-barthes")) {
				foundQuotidien = true;
			}
		}
		assertTrue(foundQuotidien);
	}

	@Test
	public void findEpisodeParsesReplayPageUrls() throws IOException {
		final Map<String, String> fixtures = new HashMap<String, String>();
		fixtures.put("id=a6f9cf0e", readFixture(VIDEOS_QUOTIDIEN));
		final Tf1PlusPluginManager plugin = newRecordingPlugin(fixtures);
		final CategoryDTO program = new CategoryDTO(Tf1PlusConf.NAME, "Quotidien",
				Tf1PlusUrls.programCategoryId("tmc", "quotidien-avec-yann-barthes"), Tf1PlusConf.EXTENSION);

		final Set<EpisodeDTO> episodes = plugin.findEpisode(program);
		assertFalse(episodes.isEmpty());
		for (final EpisodeDTO episode : episodes) {
			assertTrue(episode.getId().contains("tf1.fr/"));
			assertTrue(episode.getId().contains("/videos/"));
			assertNotNull(episode.getMetadata());
			assertEquals(episode.getId(), episode.getMetadata().getSourceUrl());
			assertEquals("Quotidien", episode.getMetadata().getSeriesTitle());
			assertNotNull("publication date mapped from GraphQL date", episode.getEpisodeDate());
			assertNotNull(episode.getMetadata().getPublicationDate());
			assertNull("GraphQL date must not be treated as airDate", episode.getMetadata().getAirDate());
		}
	}

	@Test
	public void findEpisodeReturnsEmptyForChannelCategory() {
		final Tf1PlusPluginManager plugin = newRecordingPlugin(new HashMap<String, String>());
		final CategoryDTO channel = new CategoryDTO(Tf1PlusConf.NAME, "TMC",
				Tf1PlusUrls.channelCategoryId("tmc"), Tf1PlusConf.EXTENSION);
		assertTrue(plugin.findEpisode(channel).isEmpty());
	}

	@Test
	public void canDownloadAcceptsTf1VideoPagesOnly() {
		final Tf1PlusPluginManager plugin = new Tf1PlusPluginManager();
		assertEquals(DownloadableState.SPECIFIC,
				plugin.canDownload(
						"https://www.tf1.fr/tmc/quotidien-avec-yann-barthes/videos/quotidien-premiere-partie.html"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("https://www.tf1.fr/tmc/quotidien-avec-yann-barthes"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload("https://www.france.tv/france-2/"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				plugin.canDownload("https://attacker.example/tf1.fr/videos/spoof.html"));
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload(null));
	}

	@Test
	public void urlsRejectNovo19AndParseCategoryIds() {
		assertEquals("tmc", Tf1PlusUrls.channelSlugFromCategoryId(Tf1PlusUrls.channelCategoryId("tmc")));
		assertEquals("quotidien-avec-yann-barthes",
				Tf1PlusUrls.programSlugFromCategoryId(
						Tf1PlusUrls.programCategoryId("tmc", "quotidien-avec-yann-barthes")));
		assertTrue(Tf1PlusUrls.isTf1PlusPageUrl("https://www.tf1.fr/tf1/foo/videos/bar.html"));
		assertTrue(Tf1PlusUrls.isTf1PlusPageUrl("https://tf1.fr/tf1/foo/videos/bar.html"));
		assertFalse(Tf1PlusUrls.isTf1PlusPageUrl("https://www.tf1.fr/novo19/foo/videos/bar.html"));
		assertFalse(Tf1PlusUrls.isTf1PlusPageUrl("https://attacker.example/tf1.fr/videos/bar.html"));
		assertFalse(Tf1PlusUrls.isTf1PlusVideoPageUrl("https://www.tf1.fr/tmc/program-only"));
		assertEquals("", Tf1PlusUrls.programName(null));
		assertNull(Tf1PlusUrls.programSlug(null));
		assertNull(Tf1PlusUrls.videoUrl(null));
	}

	private static Tf1PlusPluginManager newRecordingPlugin(final Map<String, String> fixturesByQueryHint) {
		final Tf1PlusGraphqlClient client = new Tf1PlusGraphqlClient(new Tf1PlusGraphqlClient.ContentLoader() {
			@Override
			public String load(final String url) throws IOException {
				// Prefer specific hints (channel slug) before generic query ids.
				for (final Map.Entry<String, String> entry : fixturesByQueryHint.entrySet()) {
					if (!entry.getKey().startsWith("id=") && url.contains(entry.getKey())) {
						return entry.getValue();
					}
				}
				for (final Map.Entry<String, String> entry : fixturesByQueryHint.entrySet()) {
					if (entry.getKey().startsWith("id=") && url.contains(entry.getKey())) {
						return entry.getValue();
					}
				}
				throw new IOException("unexpected URL: " + url);
			}
		});
		return new Tf1PlusPluginManager(client);
	}

	private static String readFixture(final String relativePath) throws IOException {
		try (InputStream input = new FileInputStream(relativePath)) {
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
