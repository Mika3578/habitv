package com.dabi.habitv.plugin.youtube;

import java.util.HashMap;

import org.junit.Test;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.plugintester.BasePluginUpdateTester;

public class YoutubePluginDownloaderTest extends BasePluginUpdateTester {

	@Test
	public void testDownload() {
		
		YoutubePluginDownloader downloader = new YoutubePluginDownloader();
		
		DownloadParamDTO downloadParam = new DownloadParamDTO("downloadInput", "$downloadOutput", "extension");
		DownloaderPluginHolder downloaders = new DownloaderPluginHolder("cmdProcessor", new HashMap<String, PluginDownloaderInterface>(), new HashMap<String, String>(),"downloadOutputDir", "indexDir", "binDir", "pluginDir");
		downloader.download(downloadParam , downloaders );
		
	}

	@Test
	public final void testYoutube() throws InstantiationException, IllegalAccessException, ReflectiveOperationException {
		testUpdatablePlugin(YoutubePluginDownloader.class);
	}
}
