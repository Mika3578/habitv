package com.dabi.habitv.core.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.dabi.habitv.api.plugin.api.PluginProviderDownloaderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.exception.ExecutorFailedException;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.api.plugin.pub.Publisher;
import com.dabi.habitv.api.plugin.pub.Subscriber;
import com.dabi.habitv.core.dao.DownloadedDAO;
import com.dabi.habitv.core.event.EpisodeStateEnum;
import com.dabi.habitv.core.event.RetreiveEvent;

public class DownloadTaskTest {

	private DownloadTask task;

	private boolean downloaded = false;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	final ExecutorFailedException executorFailedException = new ExecutorFailedException(
			"cmd", "fullOuput", "lastline", null);
	final DownloadFailedException downloadFailedException = new DownloadFailedException(
			executorFailedException);

	public void init(final boolean toFail) {
		final CategoryDTO category = new CategoryDTO("channel", "category",
				"identifier", "extension");
		final EpisodeDTO episode = new EpisodeDTO(category,
				"episode1234567890123456789012345678901234567890123456789",
				"videoUrl");
		final PluginProviderDownloaderInterface provider = new PluginProviderDownloaderInterface() {

			@Override
			public String getName() {
				return "provider";
			}

			@Override
			public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
				return null;
			}

			@Override
			public Set<CategoryDTO> findCategory() {
				return null;
			}

			MockProcessHolder processHolder = new MockProcessHolder() {
				@Override
				public void start() {
					try (FileOutputStream fileOutputStream = new FileOutputStream(
							downloadParam.getDownloadOutput())) {
						fileOutputStream.write(1);
					} catch (IOException e1) {
						throw new TechnicalException(e1);
					}

					processHolder.setProgression("0");

					try {
						Thread.sleep(10);
					} catch (final InterruptedException e) {
						fail(e.getMessage());
					}

					processHolder.setProgression("50");

					if (toFail) {
						throw downloadFailedException;
					}

					try {
						Thread.sleep(10);
					} catch (final InterruptedException e) {
						fail(e.getMessage());
					}

					processHolder.setProgression("100");
				}
			};
			private DownloadParamDTO downloadParam;

			@Override
			public ProcessHolder download(final DownloadParamDTO downloadParam,
					final DownloaderPluginHolder downloadersr)
					throws DownloadFailedException {
				this.downloadParam = downloadParam;
				assertEquals(
						"episode1234567890123_channel_category_extension.tmp",
						downloadParam.getDownloadOutput());

				return processHolder;
			}

			@Override
			public DownloadableState canDownload(final String downloadInput) {
				return DownloadableState.IMPOSSIBLE;
			}
		};
		final DownloaderPluginHolder downloader = new DownloaderPluginHolder(
				null, null, null,
				"#EPISODE_NAME§20#_#CHANNEL_NAME#_#TVSHOW_NAME#_#EXTENSION#",
				"indexDir", "bin", "plugins");
		final Publisher<RetreiveEvent> publisher = new Publisher<>();
		final Subscriber<RetreiveEvent> subscriber = new Subscriber<RetreiveEvent>() {

			private int i = 0;

			@Override
			public void update(final RetreiveEvent event) {
				switch (i) {
				case 0:
					assertEquals(new RetreiveEvent(episode,
							EpisodeStateEnum.DOWNLOAD_STARTING), event);
					break;
				case 1:
					if (toFail) {
						assertEquals(new RetreiveEvent(episode,
								EpisodeStateEnum.DOWNLOAD_FAILED,
								downloadFailedException, "download"), event);
					} else {
						assertEquals(new RetreiveEvent(episode,
								EpisodeStateEnum.DOWNLOADED), event);
					}
					break;
				case 2:
					assertEquals(new RetreiveEvent(episode,
							EpisodeStateEnum.DOWNLOADED), event);
					break;
				default:
					fail("unexpected event" + event);
					break;
				}
				i++;
			}
		};
		publisher.attach(subscriber);
		final DownloadedDAO downloadedDAO = new DownloadedDAO(category, ".") {

			@Override
			public void addDownloadedFiles(final boolean manual,
					final EpisodeDTO... episodes) {
				downloaded = true;
			}

		};
		task = new DownloadTask(episode, provider, downloader, publisher,
				downloadedDAO, false);
		assertTrue(task.equals(task));
		assertEquals(task.hashCode(), task.hashCode());
	}

	@Test
	public final void testDownloadTaskSuccess() {
		init(false);
		task.addedTo("download", null);
		task.call();
		assertTrue(downloaded);
	}

	@Test(expected = TaskFailedException.class)
	public final void testDownloadTaskFailed() {
		init(true);
		task.addedTo("download", null);
		task.call();
		assertFalse(downloaded);
	}

	// @Test
	// public final void testDownloadRemovePreviousFile() throws IOException {
	// final String filename =
	// "episode1234567890123_channel_category_extension";
	// final FileWriter fileWriter = new FileWriter(filename);
	// fileWriter.write("test");
	// fileWriter.close();
	// init(false);
	// task.addedTo("download", null);
	// task.call();
	// assertTrue(downloaded);
	// assertTrue(!(new File(filename)).exists());
	// }


	public final void testCancelActiveDownloadStopsProcessAndPublishesStopped() throws InterruptedException {
		final CategoryDTO category = new CategoryDTO("channel", "category",
				"identifier", "extension");
		final EpisodeDTO episode = new EpisodeDTO(category, "cancelEpisode", "videoUrl");

		final boolean[] stopCalled = {false};
		final Object lock = new Object();
		final boolean[] holderStarted = {false};

		final MockProcessHolder blockingHolder = new MockProcessHolder() {
			@Override
			public void start() {
				synchronized (lock) {
					holderStarted[0] = true;
					lock.notifyAll();
					while (!stopCalled[0]) {
						try {
							lock.wait(5000);
						} catch (final InterruptedException e) {
							return;
						}
					}
				}
			}

			@Override
			public void stop() {
				synchronized (lock) {
					stopCalled[0] = true;
					lock.notifyAll();
				}
			}
		};

		final PluginProviderDownloaderInterface provider = new PluginProviderDownloaderInterface() {
			@Override
			public String getName() { return "provider"; }
			@Override
			public Set<EpisodeDTO> findEpisode(final CategoryDTO cat) { return null; }
			@Override
			public Set<CategoryDTO> findCategory() { return null; }
			@Override
			public ProcessHolder download(final DownloadParamDTO downloadParam,
					final DownloaderPluginHolder downloaders) throws DownloadFailedException {
				return blockingHolder;
			}
			@Override
			public DownloadableState canDownload(final String downloadInput) {
				return DownloadableState.IMPOSSIBLE;
			}
		};

		final DownloaderPluginHolder downloader = new DownloaderPluginHolder(
				null, null, null,
				"#EPISODE_NAME§20#_#CHANNEL_NAME#_#TVSHOW_NAME#_#EXTENSION#",
				"indexDir", "bin", "plugins");
		final Publisher<RetreiveEvent> publisher = new Publisher<>();
		final boolean[] stoppedEventReceived = {false};
		final boolean[] downloadFailedEventReceived = {false};
		publisher.attach(new Subscriber<RetreiveEvent>() {
			@Override
			public void update(final RetreiveEvent event) {
				if (event.getState() == EpisodeStateEnum.STOPPED) {
					stoppedEventReceived[0] = true;
				} else if (event.getState() == EpisodeStateEnum.DOWNLOAD_FAILED) {
					downloadFailedEventReceived[0] = true;
				}
			}
		});
		final DownloadedDAO downloadedDAO = new DownloadedDAO(category, ".") {
			@Override
			public void addDownloadedFiles(final boolean manual, final EpisodeDTO... episodes) {
				downloaded = true;
			}
		};

		task = new DownloadTask(episode, provider, downloader, publisher, downloadedDAO, false);

		final TaskMgr<AbstractTask<Object>, Object> cancelTaskMgr =
				new TaskMgr<AbstractTask<Object>, Object>(1,
						new TaskMgrListener() {
							@Override public void onAllTreatmentDone() {}
							@Override public void onFailed(final Throwable t) {}
						}, null);

		cancelTaskMgr.addTask(episode, (AbstractTask<Object>) task);

		// Wait until the process holder's start() is entered
		synchronized (lock) {
			while (!holderStarted[0]) {
				lock.wait(5000);
			}
		}

		// Cancel the active download task
		cancelTaskMgr.cancelTask(episode);

		// Wait for the STOPPED event (or timeout)
		final long deadline = System.currentTimeMillis() + 5000;
		while (!stoppedEventReceived[0] && System.currentTimeMillis() < deadline) {
			Thread.sleep(25);
		}

		assertTrue("STOPPED event must be published on cancel", stoppedEventReceived[0]);
		assertFalse("DOWNLOAD_FAILED must not be published on cancel", downloadFailedEventReceived[0]);
		assertTrue("processHolder.stop() must be called on cancel", stopCalled[0]);
		assertFalse("episode must not be marked as downloaded on cancel", downloaded);

		cancelTaskMgr.shutdownNow();
	}

	// @Test
	// public final void testDownloadRemovePreviousFile() throws IOException {
	// final String filename =
	// "episode1234567890123_channel_category_extension";
	// final FileWriter fileWriter = new FileWriter(filename);
	// fileWriter.write("test");
	// fileWriter.close();
	// init(false);
	// task.addedTo("download", null);
	// task.call();
	// assertTrue(downloaded);
	// assertTrue(!(new File(filename)).exists());
	// }

	@Test
	@Ignore
	public final void testMatchingFiles() {
		File file = DownloadTask
				.findFileWithoutExtension("D:\\media\\video\\regular\\avi\\20150731 - JAMAIS_ENTRE_AMIS_Bande_Annonce_Sexe_-Comdie_-_201.flv");
		assertNotNull(file);

	}
}
