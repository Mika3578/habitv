package com.dabi.habitv.provider.canalplus;

import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class CanalPlusPluginManagerTest extends BasePluginProviderTester{

	@Test
	public final void testProviderCanalPlus() throws InstantiationException, IllegalAccessException, DownloadFailedException, ReflectiveOperationException {
		testPluginProvider(CanalPlusPluginManager.class, false);
	}

	@Test
	@Ignore
	public final void specificFindEpCanal() throws DownloadFailedException {
		Set<EpisodeDTO> ep = new CanalPlusPluginManager().findEpisode(
		        new CategoryDTO("c+", "Football", "http://service.mycanal.fr/page/0e269fd07e3e10af4099b87b77825782/4086.json?cache=300000", "mp4"));
		LOG.error(ep);
	}

}
