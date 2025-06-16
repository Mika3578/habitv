package com.dabi.habitv.downloader.aria2;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.framework.FrameworkConf;

public class Aria2PluginBasicTest {

    private Aria2PluginDownloader plugin;
    private DownloaderPluginHolder downloaders;

    @Before
    public void setUp() {
        plugin = new Aria2PluginDownloader();
        downloaders = new DownloaderPluginHolder("cmd", null, null, "downloads", "index", "bin", "plugins");
    }

    @Test
    public void testGetName() {
        assertEquals("aria2", plugin.getName());
    }

    @Test
    public void testCanDownload() {
        // Test with a torrent file
        assertEquals(DownloadableState.SPECIFIC, plugin.canDownload("http://example.com/file.torrent"));

        // Test with a non-torrent file
        assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload("http://example.com/file.mp4"));
    }

    @Test
    public void testBuildCommand() {
        // Create a download parameter
        DownloadParamDTO downloadParam = new DownloadParamDTO(
            "http://example.com/file.torrent",
            "C:/downloads/output/file.mp4",
            "mp4"
        );

        // Set a custom parameter to verify command building
        downloadParam.addParam(FrameworkConf.PARAMETER_ARGS, "--custom-arg");

        try {
            // This will not actually download anything, but will build the command
            plugin.download(downloadParam, downloaders);
            // If we get here without exception, the command was built successfully
            assertTrue(true);
        } catch (Exception e) {
            // We're not expecting any exceptions during command building
            fail("Exception during command building: " + e.getMessage());
        }
    }
}
