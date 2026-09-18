package com.dabi.habitv.plugintester;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public class BasePluginProviderTesterTest extends BasePluginProviderTester {

	@Test
	public void testPluginProviderFailsFastWhenOnlyNonDownloadablePlaceholderIsPresent() {
		final AtomicBoolean findEpisodeCalled = new AtomicBoolean(false);
		final PluginProviderInterface plugin = new PluginProviderInterface() {
			@Override
			public String getName() {
				return "placeholder-provider";
			}

			@Override
			public Set<CategoryDTO> findCategory() {
				final CategoryDTO placeholder = new CategoryDTO("placeholder-provider",
						"Unavailable", "placeholder-provider#unavailable", "mp4");
				placeholder.setDownloadable(false);
				final Set<CategoryDTO> categories = new LinkedHashSet<>();
				categories.add(placeholder);
				return categories;
			}

			@Override
			public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
				findEpisodeCalled.set(true);
				return Collections.emptySet();
			}
		};

		try {
			testPluginProvider(plugin, false);
			Assert.fail("expected assertion when no downloadable category is present");
		} catch (AssertionError expected) {
			Assert.assertTrue(expected.getMessage().contains("no downloadable category"));
			Assert.assertTrue(expected.getMessage().contains("unavailable"));
		} catch (Exception unexpected) {
			throw new AssertionError("unexpected exception", unexpected);
		}
		Assert.assertFalse(findEpisodeCalled.get());
	}

	@Test
	public void testPluginProviderFindsEpisodesWhenDownloadableLeafExists() throws Exception {
		final CategoryDTO downloadable = new CategoryDTO("mixed-provider", "Shows", "shows", "mp4");
		downloadable.setDownloadable(true);
		final CategoryDTO placeholder = new CategoryDTO("mixed-provider", "Unavailable", "unavailable", "mp4");
		placeholder.setDownloadable(false);
		final PluginProviderInterface plugin = new PluginProviderInterface() {
			@Override
			public String getName() {
				return "mixed-provider";
			}

			@Override
			public Set<CategoryDTO> findCategory() {
				placeholder.addSubCategory(downloadable);
				final Set<CategoryDTO> categories = new LinkedHashSet<>();
				categories.add(placeholder);
				return categories;
			}

			@Override
			public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
				if (!downloadable.getId().equals(category.getId())) {
					return Collections.emptySet();
				}
				final Set<EpisodeDTO> episodes = new LinkedHashSet<>();
				episodes.add(new EpisodeDTO(category, "Episode 1", "ep-1"));
				return episodes;
			}
		};

		testPluginProvider(plugin, false);
	}
}
