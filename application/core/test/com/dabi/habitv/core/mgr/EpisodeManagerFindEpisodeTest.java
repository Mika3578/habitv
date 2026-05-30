package com.dabi.habitv.core.mgr;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ExporterPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProviderPluginHolder;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.tpl.TemplateUtils;
public class EpisodeManagerFindEpisodeTest {

	private static final String YOUTUBE_PLUGIN = "youtube";

	@Test
	public void findEpisodeByCategorySkipsTemplateWithoutCallingProvider() {
		final AtomicBoolean providerCalled = new AtomicBoolean(false);
		final EpisodeManager episodeManager = buildEpisodeManager(providerCalled);
		final CategoryDTO template = TemplateUtils.buildCategoryTemplate(YOUTUBE_PLUGIN, "Top", "maxResults=10");

		assertTrue(episodeManager.findEpisodeByCategory(template).isEmpty());
		assertFalse(providerCalled.get());
	}

	@Test
	public void findEpisodeByCategorySkipsNonDownloadableWithoutCallingProvider() {
		final AtomicBoolean providerCalled = new AtomicBoolean(false);
		final EpisodeManager episodeManager = buildEpisodeManager(providerCalled);
		final CategoryDTO category = new CategoryDTO(YOUTUBE_PLUGIN, "blocked", "maxResults=10", FrameworkConf.MP4);
		category.setDownloadable(false);

		assertTrue(episodeManager.findEpisodeByCategory(category).isEmpty());
		assertFalse(providerCalled.get());
	}

	private EpisodeManager buildEpisodeManager(final AtomicBoolean providerCalled) {
		final PluginProviderInterface provider = new PluginProviderInterface() {
			@Override
			public String getName() {
				return YOUTUBE_PLUGIN;
			}

			@Override
			public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
				providerCalled.set(true);
				fail("provider findEpisode must not be called for skipped categories");
				return Collections.emptySet();
			}

			@Override
			public Set<CategoryDTO> findCategory() {
				return Collections.emptySet();
			}
		};
		final HashMap<String, PluginProviderInterface> providers = new HashMap<>();
		providers.put(YOUTUBE_PLUGIN, provider);
		return new EpisodeManager(
				new DownloaderPluginHolder("cmd", Collections.emptyMap(), Collections.emptyMap(), "out", "index",
						"bin", "plugins"),
				new ExporterPluginHolder(Collections.emptyMap(), Collections.emptyList()),
				new ProviderPluginHolder(providers),
				Collections.emptyMap(),
				null,
				1,
				"target/test-app");
	}
}
