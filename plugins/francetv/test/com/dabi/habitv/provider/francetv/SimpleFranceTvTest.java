package com.dabi.habitv.provider.francetv;

import static org.junit.Assert.*;
import org.junit.Test;
import com.dabi.habitv.api.plugin.api.PluginProviderDownloaderInterface;

/**
 * A simple test to demonstrate testing in this project.
 * This test verifies basic functionality of the FranceTvPluginManager.
 */
public class SimpleFranceTvTest {

    @Test
    public void testPluginName() {
        // Create an instance of the plugin manager
        FranceTvPluginManager manager = new FranceTvPluginManager();

        // Verify that the plugin name matches the expected value
        assertEquals("Plugin name should match the configured name", 
                     FranceTvConf.NAME, 
                     manager.getName());

        System.out.println("[DEBUG_LOG] Plugin name test passed: " + manager.getName());
    }

    @Test
    public void testCanDownload() {
        // Create an instance of the plugin manager
        FranceTvPluginManager manager = new FranceTvPluginManager();

        // Test URLs that should be downloadable
        assertEquals("Should be able to download from france.tv", 
                     PluginProviderDownloaderInterface.DownloadableState.SPECIFIC, 
                     manager.canDownload("https://www.france.tv/some-video"));

        assertEquals("Should be able to download from france2", 
                     PluginProviderDownloaderInterface.DownloadableState.SPECIFIC, 
                     manager.canDownload("https://france2.fr/some-video"));

        // Test URL that should not be downloadable
        assertEquals("Should not be able to download from other domains", 
                     PluginProviderDownloaderInterface.DownloadableState.IMPOSSIBLE, 
                     manager.canDownload("https://www.example.com/video"));

        System.out.println("[DEBUG_LOG] Can download tests passed");
    }
}
