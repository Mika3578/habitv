package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.provider.novo19.dto.Novo19Tile;

public class Novo19CatalogOfflineTest {

	@Test
	public void masterDocumentaryCatalogIncludesProgramsOutsideThemeRails() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO root = manager.findCategory().iterator().next();
		final CategoryDTO documentaries = findChildByName(root, Novo19Conf.SECTION_DOCUMENTARIES);
		final CategoryDTO societe = findChildByName(documentaries, "Société");
		assertNotNull(findChildByName(societe, "On a de l'info - Le mag"));
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
	public void findsInfoEpisodesFromCollectionFixture() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		final CategoryDTO collection = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("info", "COLLECTION",
				"On a de l'info", null, null, null, "/details/on-a-de-l-info", "info-id"));
		final Set<EpisodeDTO> episodes = manager.findEpisode(collection);
		assertEquals(2, episodes.size());
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

	@Test(expected = DownloadFailedException.class)
	public void downloadIsUnavailableInCatalogPr() throws Exception {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		manager.download(null, null);
	}

	@Test
	public void downloadUnavailableMessageIsUserFriendly() {
		try {
			final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
			manager.download(null, null);
		} catch (final DownloadFailedException e) {
			assertEquals(Novo19Conf.DOWNLOAD_UNAVAILABLE_MESSAGE, e.getMessage());
		}
	}

	@Test
	public void canDownloadRecognisesNovo19Urls() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		assertEquals(Novo19PluginManager.DownloadableState.SPECIFIC,
				manager.canDownload("https://novo19.ouest-france.fr/player/sample"));
		assertEquals(Novo19PluginManager.DownloadableState.IMPOSSIBLE, manager.canDownload("https://example.com/video"));
		assertEquals(Novo19PluginManager.DownloadableState.IMPOSSIBLE,
				manager.canDownload("https://evil.com/novo19.ouest-france.fr/player/sample"));
	}

	private static final class FailingLoader implements Novo19CatalogClient.ContentLoader {

		@Override
		public String load(final String url) throws java.io.IOException {
			throw new java.io.IOException("fixture unavailable");
		}

	}

	private static CategoryDTO findChildByName(final CategoryDTO parent, final String name) {
		if (parent == null) {
			return null;
		}
		for (final CategoryDTO child : parent.getSubCategories()) {
			if (name.equals(child.getName())) {
				return child;
			}
			final CategoryDTO nested = findChildByName(child, name);
			if (nested != null) {
				return nested;
			}
		}
		return null;
	}

}
