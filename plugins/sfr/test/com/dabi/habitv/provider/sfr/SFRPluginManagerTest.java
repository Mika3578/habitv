package com.dabi.habitv.provider.sfr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class SFRPluginManagerTest extends BasePluginProviderTester {

	@Test
	public void parseDownloadUrlFromPageContentCapturesFirstQuotedUrl() {
		final String content = "var other = \"ignored\"; var url = \"//cdn.example/video.mp4\"; var url2 = \"//later\";";
		assertEquals("//cdn.example/video.mp4", SFRPluginManager.parseDownloadUrlFromPageContent(content));
	}

	@Test
	public void parseDownloadUrlFromPageContentReturnsNullWhenMissing() {
		assertNull(SFRPluginManager.parseDownloadUrlFromPageContent("no download marker"));
	}

	@Test
	public final void testSFR() throws InstantiationException, IllegalAccessException, DownloadFailedException {
		testPluginProvider(SFRPluginManager.class, true);
	}
	
	@Test
	public final void specificFindEp() throws DownloadFailedException {
		Set<EpisodeDTO> ep;

		ep = new SFRPluginManager().findEpisode(new CategoryDTO("sfr+", "Premier League",
				"footballpremierleague", "mp4"));
		LOG.error(ep);
	}
}
