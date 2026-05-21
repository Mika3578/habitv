package com.dabi.habitv.provider.canalplus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState;

public class CNewsPluginWiringTest {

	@Test
	public void pluginNameMatchesConfName() {
		assertEquals("cnews", new CNewsPluginManager().getName());
	}

	@Test
	public void canDownloadAcceptsCNewsChannelUrl() {
		CNewsPluginManager plugin = new CNewsPluginManager();
		assertEquals(DownloadableState.SPECIFIC, plugin.canDownload("https://www.canalplus.com/chaines/cnews"));
		assertEquals(DownloadableState.SPECIFIC, plugin.canDownload("https://www.canalplus.com/cnews/some-program"));
	}

	@Test
	public void canDownloadRejectsUnrelatedUrl() {
		CNewsPluginManager plugin = new CNewsPluginManager();
		assertEquals(DownloadableState.IMPOSSIBLE, plugin.canDownload("https://www.example.com/"));
	}

	@Test
	public void homeUrlPointsAtCanalPlusCNewsChannelPage() {
		assertEquals("https://www.canalplus.com/chaines/cnews", CNewsConf.HOME_URL);
		assertNotNull(CNewsConf.VIDEO_INFO_URL);
	}

}
