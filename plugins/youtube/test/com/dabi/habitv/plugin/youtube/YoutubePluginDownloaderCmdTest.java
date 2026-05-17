package com.dabi.habitv.plugin.youtube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface;
import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;

public class YoutubePluginDownloaderCmdTest {

	@Test
	public void youtubeUrlIsAcceptedBySpecificDownloader() {
		final YoutubePluginDownloader downloader = new YoutubePluginDownloader();
		assertEquals(DownloadableState.SPECIFIC,
				downloader.canDownload("https://www.youtube.com/watch?v=jNQXAC9IVRw"));
		assertEquals(DownloadableState.SPECIFIC,
				downloader.canDownload("https://youtu.be/jNQXAC9IVRw"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				downloader.canDownload("https://example.org/foo"));
	}

	@Test
	public void downloadReturnsAProcessHolderWithConfiguredYtDlpBinary() {
		final HashMap<String, String> downloaderName2Bin = new HashMap<>();
		downloaderName2Bin.put(YoutubeConf.NAME, "/usr/bin/yt-dlp");
		final DownloaderPluginHolder downloaders = new DownloaderPluginHolder(
				"/bin/sh -c #CMD#",
				Collections.<String, PluginDownloaderInterface>emptyMap(),
				downloaderName2Bin, "/tmp/out", "/tmp/idx", "/tmp/bin",
				"/tmp/plugins");

		final DownloadParamDTO param = new DownloadParamDTO(
				"https://www.youtube.com/watch?v=jNQXAC9IVRw",
				"/tmp/out/test.mp4", "mp4");

		final ProcessHolder holder = new YoutubePluginDownloader()
				.download(param, downloaders);
		assertNotNull(holder);
	}
}
