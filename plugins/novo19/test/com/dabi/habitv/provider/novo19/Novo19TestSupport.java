package com.dabi.habitv.provider.novo19;

import java.util.Set;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

final class Novo19TestSupport {

	private Novo19TestSupport() {
	}

	static CategoryDTO rootFromFixtures() {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		return manager.findCategory().iterator().next();
	}

	static CategoryDTO findChildByName(final CategoryDTO parent, final String name) {
		if (parent == null) {
			return null;
		}
		for (final CategoryDTO child : parent.getSubCategories()) {
			if (name.equals(child.getName())) {
				return child;
			}
			final CategoryDTO nested = findChildByName(child, name);
			if (nested != null) {
				return nested;
			}
		}
		return null;
	}

	static boolean containsEpisodeName(final Set<EpisodeDTO> episodes, final String name) {
		for (final EpisodeDTO episode : episodes) {
			if (name.equals(episode.getName())) {
				return true;
			}
		}
		return false;
	}

}
