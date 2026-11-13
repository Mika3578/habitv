package com.dabi.habitv.provider.rtbfauvio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.FrameworkConf;

public class RtbfAuvioDownloadOfflineTest {

	@Test
	public void delegatesReplayDownloadToYoutubeDownloader() throws Exception {
		final Map<String, String> transport = new HashMap<String, String>();
		transport.put(RtbfAuvioUrlBuilder.exposureAnonymousAuthUrl(),
				RtbfAuvioFixtureSupport.playbackScenario("authAnonymous"));
		transport.put(RtbfAuvioUrlBuilder.exposurePlayUrl("3517841_6BA97Bb"),
				RtbfAuvioFixtureSupport.playbackScenario("publicHls"));
		final RecordingDownloader downloader = new RecordingDownloader();
		final RtbfAuvioPluginManager manager = new RtbfAuvioPluginManager(new RtbfAuvioClient(
				new RtbfAuvioClient.ContentLoader() {
					@Override
					public String load(final String url) {
						throw new UnsupportedOperationException(url);
					}
				}), new RtbfAuvioPlaybackClient(new MapTransport(transport)));
		final DownloadParamDTO param = new DownloadParamDTO(
				"https://auvio.rtbf.be/media/alex-hugo-alex-hugo-3517841", "out.mp4", RtbfAuvioConf.EXTENSION);
		manager.download(param, buildHolder(downloader));
		assertTrue(downloader.lastInput.endsWith("replay.m3u8"));
	}

	@Test(expected = DownloadFailedException.class)
	public void blocksRestrictedReplayDownload() throws Exception {
		final Map<String, String> transport = new HashMap<String, String>();
		transport.put(RtbfAuvioUrlBuilder.exposureAnonymousAuthUrl(),
				RtbfAuvioFixtureSupport.playbackScenario("authAnonymous"));
		transport.put(RtbfAuvioUrlBuilder.exposurePlayUrl("3518500_6BA97Bb"),
				RtbfAuvioFixtureSupport.playbackScenario("restrictedOnly"));
		final RtbfAuvioPluginManager manager = new RtbfAuvioPluginManager(new RtbfAuvioClient(
				new RtbfAuvioClient.ContentLoader() {
					@Override
					public String load(final String url) {
						throw new UnsupportedOperationException(url);
					}
				}), new RtbfAuvioPlaybackClient(new MapTransport(transport)));
		final DownloadParamDTO param = new DownloadParamDTO(
				"https://auvio.rtbf.be/media/les-fetes-de-wallonie-3518500", "out.mp4", RtbfAuvioConf.EXTENSION);
		manager.download(param, buildHolder(new RecordingDownloader()));
	}

	@Test
	public void downloadUnavailableMessageIsUserFriendly() {
		try {
			final RtbfAuvioPluginManager manager = new RtbfAuvioPluginManager();
			manager.download(null, null);
		} catch (final DownloadFailedException e) {
			assertEquals(RtbfAuvioConf.DOWNLOAD_UNAVAILABLE_MESSAGE, e.getMessage());
		}
	}

	private static DownloaderPluginHolder buildHolder(final PluginDownloaderInterface downloader) {
		final Map<String, PluginDownloaderInterface> downloaders = new HashMap<String, PluginDownloaderInterface>();
		downloaders.put(FrameworkConf.YOUTUBE, downloader);
		return new DownloaderPluginHolder("cmd", downloaders, new HashMap<String, String>(), ".", ".", ".", ".");
	}

	private static final class MapTransport implements RtbfAuvioHttpClient.Transport {

		private final Map<String, String> responses;

		private MapTransport(final Map<String, String> responses) {
			this.responses = responses;
		}

		@Override
		public String get(final String url, final String authorizationHeader) {
			final String body = responses.get(url);
			if (body == null) {
				throw new RuntimeException("unexpected url " + url);
			}
			return body;
		}

		@Override
		public String postJson(final String url, final String jsonBody) {
			final String body = responses.get(url);
			if (body == null) {
				throw new RuntimeException("unexpected url " + url);
			}
			return body;
		}
	}

	private static final class RecordingDownloader implements PluginDownloaderInterface {

		private String lastInput;

		@Override
		public String getName() {
			return FrameworkConf.YOUTUBE;
		}

		@Override
		public com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState canDownload(
				final String downloadInput) {
			return com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState.SPECIFIC;
		}

		@Override
		public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
				throws DownloadFailedException {
			lastInput = downloadParam.getDownloadInput();
			return new ProcessHolder() {
				@Override
				public void start() {
				}

				@Override
				public void stop() {
				}

				@Override
				public String getProgression() {
					return null;
				}
			};
		}
	}
}
