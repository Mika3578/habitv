package com.dabi.habitv.provider.sfr;

import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

/**
 * This is a live provider smoke test and must not run as part of deterministic unit tests.
 */
public class SFRPluginManagerIT extends BasePluginProviderTester {

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
