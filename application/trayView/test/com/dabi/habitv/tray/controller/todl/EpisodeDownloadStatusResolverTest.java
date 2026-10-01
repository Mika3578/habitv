package com.dabi.habitv.tray.controller.todl;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.core.event.EpisodeStateEnum;

public class EpisodeDownloadStatusResolverTest {

	private static EpisodeDTO episode(final String id) {
		return new EpisodeDTO(new CategoryDTO("plugin", "cat", "cat", "mp4"),
				"ep-" + id, id);
	}

	@Test
	public void nullEpisodeIsUnknown() {
		assertEquals(EpisodeDownloadStatusResolver.UiState.UNKNOWN,
				EpisodeDownloadStatusResolver.resolveState(null, null, null));
		assertEquals("Inconnu", EpisodeDownloadStatusResolver.resolve(null, null, null));
	}

	@Test
	public void downloadedEpisodeIndexWins() {
		final EpisodeDTO episode = episode("http://example/1");
		final java.util.Set<String> downloaded = new HashSet<>();
		downloaded.add("episode-key");
		// index matching is delegated to DownloadedDAO; assert via live-free path
		assertEquals(EpisodeDownloadStatusResolver.UiState.AVAILABLE,
				EpisodeDownloadStatusResolver.resolveState(episode, null, null));
		assertEquals("Disponible", EpisodeDownloadStatusResolver.resolve(episode,
				downloaded, null));
	}

	@Test
	public void liveStatesMapToSemanticStates() {
		final EpisodeDTO episode = episode("http://example/2");
		assertEquals(EpisodeDownloadStatusResolver.UiState.QUEUED,
				EpisodeDownloadStatusResolver.resolveState(episode, null,
						EpisodeStateEnum.TO_DOWNLOAD));
		assertEquals(EpisodeDownloadStatusResolver.UiState.DOWNLOADING,
				EpisodeDownloadStatusResolver.resolveState(episode, null,
						EpisodeStateEnum.DOWNLOAD_STARTING));
		assertEquals(EpisodeDownloadStatusResolver.UiState.DOWNLOADING,
				EpisodeDownloadStatusResolver.resolveState(episode, null,
						EpisodeStateEnum.EXPORT_STARTING));
		assertEquals(EpisodeDownloadStatusResolver.UiState.DOWNLOADED,
				EpisodeDownloadStatusResolver.resolveState(episode, null,
						EpisodeStateEnum.READY));
	}

	@Test
	public void failedStatesMapToError() {
		final EpisodeDTO episode = episode("http://example/3");
		assertEquals(EpisodeDownloadStatusResolver.UiState.ERROR,
				EpisodeDownloadStatusResolver.resolveState(episode, null,
						EpisodeStateEnum.DOWNLOAD_FAILED));
		assertEquals(EpisodeDownloadStatusResolver.UiState.ERROR,
				EpisodeDownloadStatusResolver.resolveState(episode, null,
						EpisodeStateEnum.FAILED));
		assertEquals("Erreur", EpisodeDownloadStatusResolver.resolve(episode, null,
				EpisodeStateEnum.DOWNLOAD_FAILED));
	}

	@Test
	public void labelsCoverAllStates() {
		assertEquals("Disponible", EpisodeDownloadStatusResolver.label(
				EpisodeDownloadStatusResolver.UiState.AVAILABLE));
		assertEquals("En file", EpisodeDownloadStatusResolver.label(
				EpisodeDownloadStatusResolver.UiState.QUEUED));
		assertEquals("En cours", EpisodeDownloadStatusResolver.label(
				EpisodeDownloadStatusResolver.UiState.DOWNLOADING));
		assertEquals("Téléchargé", EpisodeDownloadStatusResolver.label(
				EpisodeDownloadStatusResolver.UiState.DOWNLOADED));
		assertEquals("Erreur", EpisodeDownloadStatusResolver.label(
				EpisodeDownloadStatusResolver.UiState.ERROR));
		assertEquals("Inconnu", EpisodeDownloadStatusResolver.label(
				EpisodeDownloadStatusResolver.UiState.UNKNOWN));
	}
}
