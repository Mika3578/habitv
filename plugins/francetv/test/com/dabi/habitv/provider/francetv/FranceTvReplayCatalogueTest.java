package com.dabi.habitv.provider.francetv;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

/**
 * Live smoke test: france.tv replay catalogue (channels, programs, episodes).
 */
public class FranceTvReplayCatalogueTest {

	private static final int MIN_PROGRAMS_PER_CHANNEL = 3;

	private static final int MIN_EPISODES_ON_SAMPLE_PROGRAM = 1;

	@Test
	public void replayCatalogueFromFranceTv() {
		final FranceTvPluginManager plugin = new FranceTvPluginManager();

		final Set<CategoryDTO> channels = plugin.findCategory();
		assertFalse("expected france.tv channel roots", channels.isEmpty());
		assertTrue("expected france.tv channels (France 2–5), got " + channels.size(),
				channels.size() == FranceTvConf.CHANNEL_SLUGS.length);

		int totalPrograms = 0;
		int programsWithEpisodes = 0;

		for (final CategoryDTO channel : channels) {
			assertFalse("channel must have id (url)", channel.getId().isEmpty());
			assertFalse("channel must have name", channel.getName().isEmpty());
			assertFalse("channel should not be downloadable leaf", channel.isDownloadable());

			final Set<CategoryDTO> programs = channel.getSubCategories();
			assertTrue("channel " + channel.getName() + " should expose programs, got " + programs.size(),
					programs.size() >= MIN_PROGRAMS_PER_CHANNEL);
			totalPrograms += programs.size();

			for (final CategoryDTO program : programs) {
				assertTrue("program must be downloadable", program.isDownloadable());
				assertTrue("program id must be france.tv url", program.getId().startsWith(FranceTvConf.HOME_URL));

				final Set<EpisodeDTO> episodes = plugin.findEpisode(program);
				if (!episodes.isEmpty()) {
					programsWithEpisodes++;
					for (final EpisodeDTO episode : episodes) {
						assertFalse(episode.getName().isEmpty());
						assertTrue(episode.getId().startsWith(FranceTvConf.HOME_URL));
						assertTrue(episode.getId().endsWith(".html"));
					}
					if (programsWithEpisodes == 1) {
						assertTrue("sample program should list replay episodes",
								episodes.size() >= MIN_EPISODES_ON_SAMPLE_PROGRAM);
					}
				}
			}
		}

		assertTrue("expected programs across channels, got " + totalPrograms, totalPrograms > 15);
		assertTrue("expected at least one program with replay episodes, scanned " + totalPrograms + " programs",
				programsWithEpisodes > 0);
	}

}
