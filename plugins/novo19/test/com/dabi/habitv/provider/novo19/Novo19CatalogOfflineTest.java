package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.provider.novo19.dto.Novo19Tile;

public class Novo19CatalogOfflineTest {

	@Test
	public void fixtureBaselineIsAvailableLocally() throws IOException {
		final String fixturePath = "test/resources/fixtures/novo19/fixture-baseline.txt";
		assertTrue("missing local fixture: " + fixturePath, new File(fixturePath).exists());
		try (InputStream input = new FileInputStream(fixturePath)) {
			final String content = readUtf8(input);
			assertTrue(content.contains("provider=novo19"));
			assertTrue(content.contains("network=disabled"));
			assertTrue(content.contains("catalogueSource=bff-json"));
		}
	}

	@Test
	public void masterDocumentaryCatalogIncludesProgramsOutsideThemeRails() {
		final CategoryDTO root = Novo19TestSupport.rootFromFixtures();
		final CategoryDTO documentaries = Novo19TestSupport.findChildByName(root, Novo19Conf.SECTION_DOCUMENTARIES);
		final CategoryDTO societe = Novo19TestSupport.findChildByName(documentaries, "Société");
		assertNotNull(Novo19TestSupport.findChildByName(societe, "Programme Beta"));
	}

	@Test
	public void buildsCategoryTreeFromFixtures() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final Set<CategoryDTO> categories = manager.findCategory();
		assertEquals(1, categories.size());
		final CategoryDTO root = categories.iterator().next();
		assertFalse(root.isDownloadable());
		assertFalse(root.getSubCategories().isEmpty());
	}

	@Test
	public void findEpisodeReturnsEmptySetWhenPageUnavailable() throws Exception {
		final Novo19PluginManager manager = new Novo19PluginManager(new Novo19CatalogClient(new FailingLoader()));
		final CategoryDTO category = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("missing", "COLLECTION",
				"Missing", null, null, null, "/details/missing", "missing-id"));
		assertTrue(manager.findEpisode(category).isEmpty());
	}

	@Test
	public void findCategoryReturnsEmptySetWhenCatalogueUnavailable() throws Exception {
		final Novo19PluginManager manager = new Novo19PluginManager(new Novo19CatalogClient(new FailingLoader()));
		assertTrue(manager.findCategory().isEmpty());
	}

	@Test
	public void findCategoryReturnsEmptySetWhenParsedCatalogueHasNoPrograms() throws Exception {
		final java.util.Map<String, String> responses = new java.util.HashMap<String, String>();
		responses.put(Novo19UrlBuilder.bffConfigUrl(), Novo19FixtureSupport.readFixture("bff-config.json"));
		responses.put(Novo19UrlBuilder.bffPageByPath("categories"),
				Novo19InlineFixtures.EMPTY_CATEGORIES_PAGE);
		final Novo19PluginManager manager = new Novo19PluginManager(new Novo19CatalogClient(url -> {
			final String body = responses.get(url);
			if (body == null) {
				throw new java.io.IOException("no fixture for " + url);
			}
			return body;
		}));
		assertTrue(manager.findCategory().isEmpty());
	}

	private static final class FailingLoader implements Novo19CatalogClient.ContentLoader {

		@Override
		public String load(final String url) throws java.io.IOException {
			throw new java.io.IOException("fixture unavailable");
		}

	}

	private static String readUtf8(final InputStream input) throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final byte[] buffer = new byte[256];
		int read;
		while ((read = input.read(buffer)) != -1) {
			output.write(buffer, 0, read);
		}
		return output.toString("UTF-8");
	}

}
