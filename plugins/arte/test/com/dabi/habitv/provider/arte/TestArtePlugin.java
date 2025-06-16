package com.dabi.habitv.provider.arte;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import java.util.Set;

/**
 * Manual test class for the Arte plugin.
 * This class provides a simple way to manually test the Arte plugin functionality.
 * For automated testing, see ArtePluginManagerTest.java
 */
public class TestArtePlugin {
    public static void main(String[] args) {
        try {
            ArtePluginManager manager = new ArtePluginManager();

            System.out.println("Testing Arte Plugin...");
            System.out.println("======================");

            // Test category discovery
            Set<CategoryDTO> categories = manager.findCategory();
            System.out.println("Found " + categories.size() + " categories:");

            // Test all categories
            for (CategoryDTO category : categories) {
                System.out.println("\n  - " + category.getName() + " (ID: " + category.getId() + ")");

                try {
                    System.out.println("    Testing category: " + category.getName());
                    long startTime = System.currentTimeMillis();
                    Set<EpisodeDTO> episodes = manager.findEpisode(category);
                    long endTime = System.currentTimeMillis();

                    System.out.println("    Found " + episodes.size() + " episodes in " + 
                                      (endTime - startTime) + "ms");

                    // Show first few episodes
                    int count = 0;
                    for (EpisodeDTO episode : episodes) {
                        if (count < 3) {
                            System.out.println("      - " + episode.getName() + " -> " + episode.getId());
                            count++;
                        } else {
                            break;
                        }
                    }

                    if (episodes.isEmpty()) {
                        System.out.println("      No episodes found. This could be due to network issues or changes in the Arte website structure.");
                    }
                } catch (Exception e) {
                    System.err.println("    Error testing category " + category.getName() + ": " + e.getMessage());
                }
            }

            System.out.println("\nTesting complete!");

        } catch (Exception e) {
            System.err.println("Error testing arte plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
