package com.dabi.habitv.provider.francetv;

import java.util.Collection;

import org.junit.Test;
import static org.junit.Assert.*;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.provider.francetv.francetv.FranceTvFetcher;
import com.dabi.habitv.provider.francetv.francetv.FranceTvArchive;
import com.dabi.habitv.provider.francetv.FranceTvConf;

public class FranceTvFetcherFastTest {
    
    @Test
    public void testBasicInstantiation() {
        System.out.println("=== Testing basic FranceTvFetcher instantiation ===");
        
        try {
            // Test basic instantiation without network calls
            FranceTvFetcher fetcher = new FranceTvFetcher(null, false); // Use sample data mode
            assertNotNull("Fetcher should not be null", fetcher);
            
            System.out.println("✓ Basic instantiation test passed!");
        } catch (Exception e) {
            System.err.println("✗ Basic test failed: " + e.getMessage());
            e.printStackTrace();
            fail("Basic instantiation should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testSampleDataMode() {
        System.out.println("=== Testing sample data mode (no network calls) ===");
        
        try {
            // Use sample data mode to avoid network calls
            FranceTvFetcher fetcher = new FranceTvFetcher(null, false);
            
            System.out.println("Loading sample data (no network calls)...");
            FranceTvArchive archive = fetcher.load();
            
            assertNotNull("Archive should not be null", archive);
            
            Collection<CategoryDTO> categories = archive.getCategories();
            System.out.println("Found " + categories.size() + " categories");
            
            assertNotNull("Categories should not be null", categories);
            assertFalse("Categories should not be empty", categories.isEmpty());
            
            // Check that we have the expected categories
            assertTrue("Should have multiple categories", categories.size() >= 5);
            
            System.out.println("✓ Sample data test completed successfully!");
        } catch (Exception e) {
            System.err.println("✗ Sample data test failed: " + e.getMessage());
            e.printStackTrace();
            fail("Sample data test should not throw exception: " + e.getMessage());
        }
    }
}
