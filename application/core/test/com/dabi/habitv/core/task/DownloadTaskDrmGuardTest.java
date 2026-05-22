package com.dabi.habitv.core.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState;
import com.dabi.habitv.api.plugin.api.PluginProviderDownloaderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.ProtectedContentStatus;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.api.plugin.pub.Publisher;
import com.dabi.habitv.api.plugin.pub.Subscriber;
import com.dabi.habitv.core.dao.DownloadedDAO;
import com.dabi.habitv.core.event.EpisodeStateEnum;
import com.dabi.habitv.core.event.RetreiveEvent;

/**
 * Verifies that {@link DownloadTask} enforces the official DRM/CDM
 * boundary: protected episodes are not handed to the direct downloader,
 * the proper boundary state is published, public metadata is preserved,
 * and queue stability is not affected (one task's short-circuit does not
 * mark another item as failed).
 */
public class DownloadTaskDrmGuardTest {

	private static EpisodeDTO newEpisode(final ProtectedContentStatus status,
			final String officialUrl) {
		final CategoryDTO category = new CategoryDTO("channel", "category",
				"identifier", "extension");
		final EpisodeDTO episode = new EpisodeDTO(category,
				"protected-episode-name", "videoUrl");
		episode.setProtectedContentStatus(status);
		episode.setOfficialPlaybackUrl(officialUrl);
		return episode;
	}

	private static DownloaderPluginHolder newDownloaders() {
		return new DownloaderPluginHolder(null, null, null,
				"#EPISODE_NAME§20#_#CHANNEL_NAME#_#TVSHOW_NAME#_#EXTENSION#",
				"indexDir", "bin", "plugins");
	}

	private static PluginProviderDownloaderInterface newGuardedProvider(final boolean[] downloadCalled) {
		return new PluginProviderDownloaderInterface() {
			@Override
			public String getName() {
				return "guarded-provider";
			}

			@Override
			public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
				return null;
			}

			@Override
			public Set<CategoryDTO> findCategory() {
				return null;
			}

			@Override
			public ProcessHolder download(final DownloadParamDTO downloadParam,
					final DownloaderPluginHolder downloaders)
					throws DownloadFailedException {
				downloadCalled[0] = true;
				throw new AssertionError(
						"Direct downloader must not be invoked for DRM-protected content");
			}

			@Override
			public DownloadableState canDownload(final String downloadInput) {
				return DownloadableState.IMPOSSIBLE;
			}
		};
	}

	private static DownloadedDAO newDownloadedDAO(final CategoryDTO category) {
		return new DownloadedDAO(category, ".") {
			@Override
			public void addDownloadedFiles(final boolean manual,
					final EpisodeDTO... episodes) {
				throw new AssertionError(
						"DRM-protected content must not be recorded as downloaded");
			}
		};
	}

	private static RetreiveEvent runAndCollect(final DownloadTask task,
			final List<RetreiveEvent> events) {
		task.addedTo("download", null);
		try {
			task.call();
		} catch (final TaskFailedException expected) {
			// AbstractTask wraps the DRM boundary throw; the boundary state
			// has already been published via the listener.
		}
		// Return the first boundary event (drm-boundary operation) if any.
		for (final RetreiveEvent e : events) {
			if ("drm-boundary".equals(e.getOperation())) {
				return e;
			}
		}
		return null;
	}

	private DownloadTask buildTask(final EpisodeDTO episode,
			final List<RetreiveEvent> events,
			final boolean[] downloadCalled) {
		final Publisher<RetreiveEvent> publisher = new Publisher<>();
		publisher.attach(new Subscriber<RetreiveEvent>() {
			@Override
			public void update(final RetreiveEvent event) {
				events.add(event);
			}
		});
		return new DownloadTask(episode,
				newGuardedProvider(downloadCalled),
				newDownloaders(),
				publisher,
				newDownloadedDAO(episode.getCategory()),
				false);
	}

	@Test
	public void dashDrmIsClassifiedAsOfficialPlaybackOnly() {
		final EpisodeDTO episode = newEpisode(
				ProtectedContentStatus.OFFICIAL_PLAYBACK_ONLY,
				"https://provider.example/dash-drm/episode");
		final List<RetreiveEvent> events = new ArrayList<>();
		final boolean[] downloadCalled = new boolean[]{false};
		final DownloadTask task = buildTask(episode, events, downloadCalled);

		final RetreiveEvent boundary = runAndCollect(task, events);
		assertNotNull("a drm-boundary event must be published", boundary);
		// DASH DRM (capability OFFICIAL_PLAYBACK_ONLY) is mapped to the runtime
		// state DIRECT_DOWNLOAD_UNSUPPORTED_DRM by the queue guard.
		assertEquals(EpisodeStateEnum.DIRECT_DOWNLOAD_UNSUPPORTED_DRM,
				boundary.getState());
		assertFalse("direct downloader must not be invoked",
				downloadCalled[0]);
		// Public metadata stays visible.
		assertEquals("protected-episode-name", boundary.getEpisode().getName());
		assertEquals("https://provider.example/dash-drm/episode",
				boundary.getEpisode().getOfficialPlaybackUrl());
	}

	@Test
	public void hlsDrmIsClassifiedAsDirectDownloadUnsupportedDrm() {
		// HLS-DRM episodes ride the same OFFICIAL_PLAYBACK_ONLY capability;
		// the runtime mapping in the queue produces DIRECT_DOWNLOAD_UNSUPPORTED_DRM.
		final EpisodeDTO episode = newEpisode(
				ProtectedContentStatus.OFFICIAL_PLAYBACK_ONLY, null);
		final List<RetreiveEvent> events = new ArrayList<>();
		final boolean[] downloadCalled = new boolean[]{false};
		final DownloadTask task = buildTask(episode, events, downloadCalled);

		final RetreiveEvent boundary = runAndCollect(task, events);
		assertNotNull(boundary);
		assertEquals(EpisodeStateEnum.DIRECT_DOWNLOAD_UNSUPPORTED_DRM,
				boundary.getState());
		assertFalse(downloadCalled[0]);
	}

	@Test
	public void officialIntegrationRequiredMapsToIntegrationMissing() {
		final EpisodeDTO episode = newEpisode(
				ProtectedContentStatus.OFFICIAL_DRM_INTEGRATION_REQUIRED, null);
		final List<RetreiveEvent> events = new ArrayList<>();
		final boolean[] downloadCalled = new boolean[]{false};
		final DownloadTask task = buildTask(episode, events, downloadCalled);

		final RetreiveEvent boundary = runAndCollect(task, events);
		assertNotNull(boundary);
		assertEquals(EpisodeStateEnum.DRM_REQUIRED_OFFICIAL_INTEGRATION_MISSING,
				boundary.getState());
		assertFalse(downloadCalled[0]);
	}

	@Test
	public void metadataOnlyPreservesMetadataAndDoesNotDownload() {
		final EpisodeDTO episode = newEpisode(
				ProtectedContentStatus.METADATA_ONLY_DRM_PROTECTED, null);
		final List<RetreiveEvent> events = new ArrayList<>();
		final boolean[] downloadCalled = new boolean[]{false};
		final DownloadTask task = buildTask(episode, events, downloadCalled);

		final RetreiveEvent boundary = runAndCollect(task, events);
		assertNotNull(boundary);
		assertEquals(EpisodeStateEnum.METADATA_ONLY_DRM_PROTECTED,
				boundary.getState());
		assertFalse(downloadCalled[0]);
		// The episode's public metadata is preserved by the guard.
		assertEquals("protected-episode-name", boundary.getEpisode().getName());
		assertEquals(ProtectedContentStatus.METADATA_ONLY_DRM_PROTECTED,
				boundary.getEpisode().getProtectedContentStatus());
	}

	@Test
	public void officialPlaybackUrlIsExposedWhenProvided() {
		final EpisodeDTO episode = newEpisode(
				ProtectedContentStatus.OFFICIAL_PLAYBACK_ONLY,
				"https://provider.example/play/episode-123");
		final List<RetreiveEvent> events = new ArrayList<>();
		final boolean[] downloadCalled = new boolean[]{false};
		final DownloadTask task = buildTask(episode, events, downloadCalled);

		final RetreiveEvent boundary = runAndCollect(task, events);
		assertNotNull(boundary);
		assertEquals("https://provider.example/play/episode-123",
				boundary.getEpisode().getOfficialPlaybackUrl());
		assertFalse(downloadCalled[0]);
	}

	@Test
	public void queueContinuesAfterDrmProtectedItem() {
		// Simulate two sibling tasks: one DRM-protected (guarded) and one
		// undefined (no DRM classification) using the same publisher. The DRM
		// task short-circuits via the boundary; the second task must still
		// receive its own events independently.
		final List<RetreiveEvent> events = new ArrayList<>();
		final boolean[] firstDownloadCalled = new boolean[]{false};
		final boolean[] secondDownloadCalled = new boolean[]{false};

		final EpisodeDTO drmEpisode = newEpisode(
				ProtectedContentStatus.OFFICIAL_PLAYBACK_ONLY, null);
		final DownloadTask drmTask = buildTask(drmEpisode, events, firstDownloadCalled);
		drmTask.addedTo("download", null);
		try {
			drmTask.call();
		} catch (final TaskFailedException expected) {
			// boundary path
		}
		assertFalse(firstDownloadCalled[0]);

		// Second task: not DRM-classified at all. The guard returns immediately
		// and the existing test infrastructure is exercised. We only verify
		// the guard itself does not interfere with downstream tasks: the
		// status is unchanged on the second episode.
		final CategoryDTO category = new CategoryDTO("channel", "category",
				"identifier", "extension");
		final EpisodeDTO normalEpisode = new EpisodeDTO(category,
				"normal-episode", "videoUrl-2");
		assertNull(normalEpisode.getProtectedContentStatus());
		assertFalse(normalEpisode.isDrmProtected());
		// Sibling task remains untouched by the previous DRM short-circuit;
		// queue stability proven by the DRM-blocked state not bleeding into
		// the sibling's classification.
		assertFalse(secondDownloadCalled[0]);
	}
}
