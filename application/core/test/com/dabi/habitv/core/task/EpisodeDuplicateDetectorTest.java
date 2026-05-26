package com.dabi.habitv.core.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

public class EpisodeDuplicateDetectorTest {

	@Test
	public void skipsAlreadyDownloaded() {
		final EpisodeDuplicateDetector detector = new EpisodeDuplicateDetector();
		final EpisodeDTO episode = episode("arte", "show", "ep1", "http://x/1");
		assertEquals(EnqueueSkipReason.ALREADY_DOWNLOADED,
				detector.detectSkipReason(episode, true, false, false));
	}

	@Test
	public void skipsAlreadyQueued() {
		final EpisodeDuplicateDetector detector = new EpisodeDuplicateDetector();
		final EpisodeDTO episode = episode("arte", "show", "ep1", "http://x/1");
		assertEquals(EnqueueSkipReason.ALREADY_QUEUED,
				detector.detectSkipReason(episode, false, true, false));
	}

	@Test
	public void skipsAlreadyDownloading() {
		final EpisodeDuplicateDetector detector = new EpisodeDuplicateDetector();
		final EpisodeDTO episode = episode("arte", "show", "ep1", "http://x/1");
		assertEquals(EnqueueSkipReason.ALREADY_DOWNLOADING,
				detector.detectSkipReason(episode, false, false, true));
	}

	@Test
	public void skipsDuplicateInBatch() {
		final EpisodeDuplicateDetector detector = new EpisodeDuplicateDetector();
		final EpisodeDTO episode = episode("arte", "show", "ep1", "http://x/1");
		assertNull(detector.detectSkipReason(episode, false, false, false));
		assertEquals(EnqueueSkipReason.DUPLICATE_IN_BATCH,
				detector.detectSkipReason(episode, false, false, false));
	}

	private static EpisodeDTO episode(final String plugin, final String category,
			final String name, final String id) {
		return new EpisodeDTO(new CategoryDTO(plugin, category, category, null),
				name, id);
	}
}
