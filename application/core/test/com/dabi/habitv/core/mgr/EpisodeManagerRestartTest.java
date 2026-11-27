package com.dabi.habitv.core.mgr;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ExporterPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProviderPluginHolder;

/**
 * Regression tests for the queued-download restart path: an episode without a
 * usable category must produce a controlled technical error instead of
 * reaching the index DAO with invalid state.
 */
public class EpisodeManagerRestartTest {

	@Test
	public void restartRefusesEpisodeWithoutCategory() {
		final EpisodeManager episodeManager = buildEpisodeManager();
		final EpisodeDTO episode = new EpisodeDTO(null, "queued-episode", "id");
		try {
			episodeManager.restart(episode, false);
			fail("restart must refuse an episode without a category");
		} catch (final TechnicalException e) {
			assertNotNull(e.getMessage());
		} catch (final NullPointerException e) {
			fail("restart must not throw a raw NullPointerException");
		}
	}

	@Test
	public void restartRefusesNullEpisodeWithoutSideEffects() {
		final EpisodeManager episodeManager = buildEpisodeManager();
		// Matches the previous contract: a null episode is silently ignored.
		episodeManager.restart(null, false);
	}

	private EpisodeManager buildEpisodeManager() {
		return new EpisodeManager(
				new DownloaderPluginHolder("cmd", Collections.emptyMap(), Collections.emptyMap(), "out", "index",
						"bin", "plugins"),
				new ExporterPluginHolder(Collections.emptyMap(), Collections.emptyList()),
				new ProviderPluginHolder(new HashMap<String, com.dabi.habitv.api.plugin.api.PluginProviderInterface>()),
				Collections.emptyMap(),
				null,
				1,
				"target/test-app");
	}
}
