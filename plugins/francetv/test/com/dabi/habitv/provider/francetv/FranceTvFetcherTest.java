package com.dabi.habitv.provider.francetv;

import java.util.Collection;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.provider.francetv.francetv.FranceTvFetcher;
import com.dabi.habitv.provider.francetv.francetv.FranceTvArchive;
import com.dabi.habitv.provider.francetv.FranceTvConf;
import com.dabi.habitv.provider.francetv.FranceTvPluginManager;

public class FranceTvFetcherTest {

    @Test
    public void testBasicFunctionality() {
        System.out.println("=== FranceTvFetcherTest: Basic Functionality ===");
        
        try {
            // Use sample data mode to avoid network calls
            FranceTvFetcher fetcher = new FranceTvFetcher(null, false);
            
            // Load sample data (no network calls)
            System.out.println("Loading sample data...");
            FranceTvArchive archive = fetcher.load();
            
            assertNotNull("Archive should not be null", archive);
            
            Collection<CategoryDTO> categories = archive.getCategories();
            System.out.println("Found " + categories.size() + " categories");
            
            assertNotNull("Categories should not be null", categories);
            assertFalse("Categories should not be empty", categories.isEmpty());
            assertTrue("Should have at least 5 categories", categories.size() >= 5);
            
            // Test episode discovery for first category
            CategoryDTO firstCategory = categories.iterator().next();
            System.out.println("Testing category: " + firstCategory.getName());
            
            Map<String, Collection<EpisodeDTO>> episodeMap = archive.getCatName2Episode();
            Collection<EpisodeDTO> episodes = episodeMap.get(firstCategory.getId());
            
            if (episodes != null) {
                System.out.println("Found " + episodes.size() + " episodes");
                assertTrue("Should have at least 1 episode", episodes.size() >= 1);
                
                // Print some sample episodes
                int count = 0;
                for (EpisodeDTO episode : episodes) {
                    if (count++ < 3) { // Show first 3 episodes
                        System.out.println("- " + episode.getName() + " (" + episode.getId() + ")");
                    }
                }
            } else {
                System.out.println("No episodes found for category: " + firstCategory.getId());
            }
            
            System.out.println("=== Test completed successfully ===");
            
        } catch (Exception e) {
            System.err.println("Error during test: " + e.getMessage());
            e.printStackTrace();
            fail("Test failed with exception: " + e.getMessage());
        }
    }
    
    @Test
    public void testPluginManagerWithLimitedScope() {
        System.out.println("=== FranceTvFetcherTest: Plugin Manager Test ===");
        
        try {
            // Test with a very quick verification - just check that we can instantiate
            FranceTvPluginManager manager = new FranceTvPluginManager();
            assertNotNull("Plugin manager should not be null", manager);
            assertEquals("Plugin name should be francetv", "francetv", manager.getName());
            
            System.out.println("Plugin manager basic test completed successfully");
            
        } catch (Exception e) {
            System.err.println("Error during plugin manager test: " + e.getMessage());
            fail("Plugin manager test failed: " + e.getMessage());
        }
    }
}
