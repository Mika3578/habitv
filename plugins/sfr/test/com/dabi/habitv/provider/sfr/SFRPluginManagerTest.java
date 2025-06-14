package com.dabi.habitv.provider.sfr;

import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class SFRPluginManagerTest extends BasePluginProviderTester {

	@Test
	public final void testSFR() throws InstantiationException, IllegalAccessException, DownloadFailedException, ReflectiveOperationException {
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
