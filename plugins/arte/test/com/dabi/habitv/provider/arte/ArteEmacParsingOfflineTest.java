package com.dabi.habitv.provider.arte;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.TechnicalException;

/**
 * Offline test covering {@link ArtePluginManager} category browsing and EMAC
 * teaser parsing against captured JSON fixtures. Runs without network access so
 * the parsing contract can be verified in CI sandboxes where arte.tv is
 * unreachable.
 */
public class ArteEmacParsingOfflineTest {

	private static final String PAGE_FIXTURE = "test/resources/fixtures/arte/emac-page-DOR.json";
	private static final String ZONE_PAGE2_FIXTURE = "test/resources/fixtures/arte/emac-zone-page2.json";

	@Test
	public void findCategoryReturnsLanguageTreeWithStablePageCodes() {
		final ArtePluginManager plugin = new ArtePluginManager();

		final Set<CategoryDTO> languages = plugin.findCategory();

		assertEquals("one CategoryDTO per supported language", ArteConf.LANGUAGES.length, languages.size());
		for (final CategoryDTO language : languages) {
			assertFalse("language container must not be downloadable", language.isDownloadable());
			assertEquals("each language exposes the configured page codes",
					ArteConf.PAGE_CODES.length, language.getSubCategories().size());
			for (final CategoryDTO sub : language.getSubCategories()) {
				assertTrue("sub-category id must include language:pageCode separator",
						sub.getId().contains(":"));
				assertTrue("sub-categories must be downloadable", sub.isDownloadable());
			}
		}
	}

	@Test
	public void findEpisodeParsesTeasersFiltersInvalidUrlsAndDedupes() throws IOException {
		final Map<String, String> urlToContent = new HashMap<>();
		final String pageUrl = "https://www.arte.tv/api/rproxy/emac/v4/fr/web/pages/DOR/?authorizedCountry=FR";
		urlToContent.put(pageUrl, readFixture(PAGE_FIXTURE));
		urlToContent.put(
				"https://www.arte.tv/api/rproxy/emac/v4/fr/web/zones/listing_DOCUMENTARIES_main/content?page=2&pageId=DOR&authorizedCountry=FR",
				readFixture(ZONE_PAGE2_FIXTURE));

		final RecordingArtePlugin plugin = new RecordingArtePlugin(urlToContent);
		final CategoryDTO category = new CategoryDTO(ArteConf.NAME, "Documentaries", "fr:DOR", ArteConf.EXTENSION);

		final Set<EpisodeDTO> episodes = plugin.findEpisode(category);

		final List<String> ids = new ArrayList<>();
		final List<String> names = new ArrayList<>();
		for (final EpisodeDTO episode : episodes) {
			ids.add(episode.getId());
			names.add(episode.getName());
		}

		assertTrue("absolute https URL preserved",
				ids.contains("https://www.arte.tv/fr/videos/119999-001-A/test-documentary-two/"));
		assertTrue("relative URL resolved against HOME_URL",
				ids.contains("https://www.arte.tv/fr/videos/119999-000-A/test-documentary-one/"));
		assertTrue("teaser without title falls back to subtitle",
				names.contains("Subtitle Only Three"));
		assertTrue("zone pagination fetched page 2",
				ids.contains("https://www.arte.tv/fr/videos/119999-200-A/page-two-item/"));
		assertFalse("non-episode URL filtered out by EPISODE_URL_PATTERN",
				ids.contains("https://www.arte.tv/fr/programmes/119999/"));
		assertFalse("teaser without title or subtitle dropped",
				ids.contains("https://www.arte.tv/fr/videos/119999-003-A/test-no-title/"));
		assertEquals("duplicate URL collapses into a single episode",
				new java.util.HashSet<>(ids).size(), ids.size());
		assertEquals("expected episodes after filtering and dedup", 6, episodes.size());
	}

	@Test
	public void findEpisodeRejectsInvalidCategoryIdentifiers() {
		final ArtePluginManager plugin = new ArtePluginManager();
		assertTrue(plugin.findEpisode(new CategoryDTO(ArteConf.NAME, "x", "", ArteConf.EXTENSION)).isEmpty());
		assertTrue(plugin.findEpisode(new CategoryDTO(ArteConf.NAME, "x", "fr", ArteConf.EXTENSION)).isEmpty());
		assertTrue(plugin.findEpisode(new CategoryDTO(ArteConf.NAME, "x", "fr:", ArteConf.EXTENSION)).isEmpty());
	}

	@Test
	public void findEpisodeReturnsEmptyOnUpstreamFailure() {
		final RecordingArtePlugin plugin = new RecordingArtePlugin(new HashMap<String, String>());
		final CategoryDTO category = new CategoryDTO(ArteConf.NAME, "Documentaries", "fr:DOR", ArteConf.EXTENSION);

		assertTrue("network failures must surface as an empty set, not a runtime exception",
				plugin.findEpisode(category).isEmpty());
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

	private static final class RecordingArtePlugin extends ArtePluginManager {

		private final Map<String, String> urlToContent;

		RecordingArtePlugin(final Map<String, String> urlToContent) {
			this.urlToContent = urlToContent;
		}

		@Override
		protected String getUrlContent(final String url) {
			final String content = urlToContent.get(url);
			if (content == null) {
				throw new TechnicalException("unexpected URL fetched: " + url);
			}
			return content;
		}
	}
}
