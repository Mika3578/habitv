package com.dabi.habitv.core.plugin;

import static org.junit.Assert.assertSame;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public class PluginFactoryLegacyAliasTest {

	@Test
	public void registerLegacyPluginAliasesMapsWatToTf1plus() {
		final Map<String, PluginProviderInterface> plugins = new HashMap<String, PluginProviderInterface>();
		final PluginProviderInterface tf1plus = stubProvider("tf1plus");
		plugins.put("tf1plus", tf1plus);
		PluginFactory.registerLegacyPluginAliases(plugins);
		assertSame(tf1plus, plugins.get("wat"));
	}

	@Test
	public void registerLegacyPluginAliasesDoesNotOverrideExistingWatEntry() {
		final Map<String, PluginProviderInterface> plugins = new HashMap<String, PluginProviderInterface>();
		final PluginProviderInterface tf1plus = stubProvider("tf1plus");
		final PluginProviderInterface legacyWat = stubProvider("wat");
		plugins.put("tf1plus", tf1plus);
		plugins.put("wat", legacyWat);
		PluginFactory.registerLegacyPluginAliases(plugins);
		assertSame(legacyWat, plugins.get("wat"));
	}

	private static PluginProviderInterface stubProvider(final String name) {
		return new PluginProviderInterface() {
			@Override
			public String getName() {
				return name;
			}

			@Override
			public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
				return Collections.emptySet();
			}

			@Override
			public Set<CategoryDTO> findCategory() {
				return Collections.emptySet();
			}
		};
	}
}
