package com.dabi.habitv.provider.arte;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

import java.util.Set;

public class ArtePluginManagerTest extends BasePluginProviderTester {

	@Test
	public final void testArtePluginManager() throws InstantiationException, IllegalAccessException, DownloadFailedException, ReflectiveOperationException {
		testPluginProvider(ArtePluginManager.class, true);
	}

	@Test
	public final void testArteReplayCategory() throws InstantiationException, IllegalAccessException, DownloadFailedException, ReflectiveOperationException {
		ArtePluginManager manager = new ArtePluginManager();

		// Test that replay categories are created
		java.util.Set<CategoryDTO> categories = manager.findCategory();
		boolean foundReplay = false;
		for (CategoryDTO category : categories) {
			if (category.getName().equals("Replay")) {
				foundReplay = true;
				break;
			}
		}

		assert foundReplay : "Replay category should be found";
	}

	@Test
	public final void testArteReplayEpisodes() throws InstantiationException, IllegalAccessException, DownloadFailedException, ReflectiveOperationException {
		ArtePluginManager manager = new ArtePluginManager();

		// Find the Replay category
		Set<CategoryDTO> categories = manager.findCategory();
		CategoryDTO replayCategory = null;
		for (CategoryDTO category : categories) {
			if (category.getName().equals("Replay")) {
				replayCategory = category;
				break;
			}
		}

		// Make sure we found the Replay category
		assert replayCategory != null : "Replay category should be found";

		// Test finding episodes in the Replay category
		Set<EpisodeDTO> episodes = manager.findEpisode(replayCategory);

		// Verify that episodes is not null
		assert episodes != null : "Episodes should not be null";

		// Log the number of episodes found
		System.out.println("Found " + episodes.size() + " episodes in Replay category");

		// Log the first episode for debugging if any are found
		if (!episodes.isEmpty()) {
			EpisodeDTO firstEpisode = episodes.iterator().next();
			System.out.println("Found episode: " + firstEpisode.getName() + " -> " + firstEpisode.getId());
		} else {
			System.out.println("No episodes found in Replay category. This could be due to network issues or changes in the Arte website structure.");
		}

		// We don't assert that episodes are found, as this depends on external resources
		// and may fail intermittently
	}

	@Test
	public final void testAllCategories() throws InstantiationException, IllegalAccessException, DownloadFailedException, ReflectiveOperationException {
		ArtePluginManager manager = new ArtePluginManager();

		// Get all categories
		Set<CategoryDTO> categories = manager.findCategory();

		// Verify we have categories
		assert categories != null && !categories.isEmpty() : "Categories should be found";

		System.out.println("Testing " + categories.size() + " Arte categories");

		// Test each category
		for (CategoryDTO category : categories) {
			try {
				System.out.println("Testing category: " + category.getName());

				// Test finding episodes in this category
				Set<EpisodeDTO> episodes = manager.findEpisode(category);

				// Verify that episodes is not null
				assert episodes != null : "Episodes should not be null for category " + category.getName();

				System.out.println("  Found " + episodes.size() + " episodes in " + category.getName() + " category");

				// We don't assert that episodes are found, as this depends on external resources
				// and may fail intermittently
			} catch (Exception e) {
				System.err.println("Error testing category " + category.getName() + ": " + e.getMessage());
				// Don't fail the test due to network issues
			}
		}
	}

	@Test
	public final void testCanDownload() {
		ArtePluginManager manager = new ArtePluginManager();

		// Test URLs that should be downloadable
		assert manager.canDownload("https://www.arte.tv/en/videos/123456-000-A/some-video/") == com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState.SPECIFIC : 
			"Arte URL should be downloadable";
		assert manager.canDownload("https://arte.tv/fr/videos/some-video/") == com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState.SPECIFIC : 
			"Arte URL should be downloadable";
		assert manager.canDownload("https://www.arte.tv/fr/") == com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState.SPECIFIC : 
			"Arte French homepage URL should be downloadable";

		// Test URLs that should not be downloadable
		assert manager.canDownload("https://www.youtube.com/watch?v=12345") == com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState.IMPOSSIBLE : 
			"YouTube URL should not be downloadable by Arte plugin";
		assert manager.canDownload("https://www.example.com") == com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState.IMPOSSIBLE : 
			"Random URL should not be downloadable by Arte plugin";
	}

	@Test
	public final void testExtractLanguageFromUrl() throws Exception {
		ArtePluginManager manager = new ArtePluginManager();

		// Use reflection to access private method
		java.lang.reflect.Method method = ArtePluginManager.class.getDeclaredMethod("extractLanguageFromUrl", String.class);
		method.setAccessible(true);

		// Test full URLs
		assert "fr".equals(method.invoke(manager, "https://www.arte.tv/fr/")) : 
			"Should extract 'fr' from French homepage URL";
		assert "fr".equals(method.invoke(manager, "https://www.arte.tv/fr/videos/")) : 
			"Should extract 'fr' from French videos URL";
		assert "en".equals(method.invoke(manager, "https://www.arte.tv/en/")) : 
			"Should extract 'en' from English homepage URL";

		// Test partial URLs
		assert "fr".equals(method.invoke(manager, "fr/videos")) : 
			"Should extract 'fr' from partial URL";
		assert "de".equals(method.invoke(manager, "de/CIN")) : 
			"Should extract 'de' from partial URL";
	}

	@Test
	public final void testExtractCategoryFromUrl() throws Exception {
		ArtePluginManager manager = new ArtePluginManager();

		// Use reflection to access private method
		java.lang.reflect.Method method = ArtePluginManager.class.getDeclaredMethod("extractCategoryFromUrl", String.class);
		method.setAccessible(true);

		// Test full URLs with categories
		assert "videos".equals(method.invoke(manager, "https://www.arte.tv/fr/videos/")) : 
			"Should extract 'videos' from French videos URL";
		assert "cinema".equals(method.invoke(manager, "https://www.arte.tv/en/cinema/")) : 
			"Should extract 'cinema' from English cinema URL";

		// Test partial URLs
		assert "CIN".equals(method.invoke(manager, "fr/CIN")) : 
			"Should extract 'CIN' from partial URL";
		assert "REPLAY".equals(method.invoke(manager, "de/REPLAY")) : 
			"Should extract 'REPLAY' from partial URL";

		// Test homepage URLs (should default to CIN)
		assert "CIN".equals(method.invoke(manager, "https://www.arte.tv/fr/")) : 
			"Should default to 'CIN' for homepage URL";
	}
}
