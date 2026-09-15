package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

public class Novo19DownloadOfflineTest {

	@Test
	public void delegatesReplayDownloadToYoutubeDownloader() throws Exception {
		final Map<String, String> transport = new HashMap<String, String>();
		transport.put(Novo19UrlBuilder.redBeeAnonymousAuthUrl(),
				Novo19FixtureSupport.redbeeScenario("authAnonymous"));
		transport.put(Novo19UrlBuilder.redBeePlayUrl("asset-collection-alpha-episode-alpha"),
				Novo19FixtureSupport.redbeeScenario("publicHls"));
		final Novo19HttpClient.Transport http = new MapTransport(transport);
		final Novo19CatalogClient catalogClient = playbackAwareCatalogClient();
		final RecordingDownloader downloader = new RecordingDownloader();
		final Novo19PluginManager manager = new Novo19PluginManager(catalogClient, new Novo19PlaybackClient(http));
		final DownloadParamDTO param = new DownloadParamDTO(
				"https://novo19.ouest-france.fr/player/collection-alpha-episode-alpha", "out.mp4",
				Novo19Conf.EXTENSION);
		final DownloaderPluginHolder holder = buildHolder(downloader);
		manager.download(param, holder);
		assertTrue(downloader.lastInput.endsWith("replay.m3u8"));
		assertTrue(downloader.lastArgs == null || !downloader.lastArgs.contains("--extract-audio"));
	}

	@Test
	public void delegatesPodcastDownloadWithAudioOnlyArgs() throws Exception {
		final Map<String, String> transport = new HashMap<String, String>();
		transport.put(Novo19UrlBuilder.redBeeAnonymousAuthUrl(),
				Novo19FixtureSupport.redbeeScenario("authAnonymous"));
		transport.put(Novo19UrlBuilder.redBeePlayUrl("asset-podcast-alpha-episode-alpha"),
				Novo19FixtureSupport.redbeeScenario("podcastHls"));
		final Map<String, String> catalogResponses = new HashMap<String, String>();
		catalogResponses.put(Novo19UrlBuilder.bffPageByPath("player/podcast-alpha-episode-alpha"),
				Novo19InlineFixtures.PLAYER_PODCAST_EPISODE_PAGE);
		final Novo19PluginManager manager = new Novo19PluginManager(new Novo19CatalogClient(new CatalogMapLoader(catalogResponses)),
				new Novo19PlaybackClient(new MapTransport(transport)));
		final DownloadParamDTO param = new DownloadParamDTO("https://novo19.ouest-france.fr/player/podcast-alpha-episode-alpha",
				"out.mp4", Novo19Conf.EXTENSION);
		param.addParam(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_PODCAST);
		param.addParam(Novo19Conf.PARAMETER_AUDIO_CONTENT, "true");
		final RecordingDownloader downloader = new RecordingDownloader();
		manager.download(param, buildHolder(downloader));
		assertTrue(downloader.lastInput.endsWith("replay.m3u8"));
		assertTrue(downloader.lastArgs.contains("--extract-audio"));
	}

	@Test(expected = DownloadFailedException.class)
	public void blocksObjectDrmReplayDownload() throws Exception {
		final Map<String, String> transport = new HashMap<String, String>();
		transport.put(Novo19UrlBuilder.redBeeAnonymousAuthUrl(),
				Novo19FixtureSupport.redbeeScenario("authAnonymous"));
		transport.put(Novo19UrlBuilder.redBeePlayUrl("asset-series-beta-episode-beta"),
				Novo19FixtureSupport.redbeeScenario("protected"));
		final Map<String, String> catalogResponses = new HashMap<String, String>();
		catalogResponses.put(Novo19UrlBuilder.bffPageByPath(
				"player/series-beta-episode-beta"),
				Novo19InlineFixtures.PLAYER_SERIES_EPISODE_NO_HREF_PAGE);
		final Novo19PluginManager manager = new Novo19PluginManager(
				new Novo19CatalogClient(new CatalogMapLoader(catalogResponses)),
				new Novo19PlaybackClient(new MapTransport(transport)));
		final DownloadParamDTO param = new DownloadParamDTO(
				"https://novo19.ouest-france.fr/player/series-beta-episode-beta",
				"out.mp4", Novo19Conf.EXTENSION);
		manager.download(param, buildHolder(new RecordingDownloader()));
	}

	@Test(expected = DownloadFailedException.class)
	public void blocksLiveAssetDownload() throws Exception {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures(),
				new Novo19PlaybackClient(new MapTransport(new HashMap<String, String>())));
		final DownloadParamDTO param = new DownloadParamDTO("https://novo19.ouest-france.fr/details/live", "out.mp4",
				Novo19Conf.EXTENSION);
		param.addParam(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_FILM);
		param.addParam(Novo19Conf.PARAMETER_ASSET_ID, Novo19Conf.LIVE_ASSET_ID);
		manager.download(param, buildHolder(new RecordingDownloader()));
	}

	@Test
	public void liveAssetGuardRecognisesChannelAssetId() {
		assertTrue(Novo19PathRules.isLiveReplayAsset(Novo19Conf.LIVE_ASSET_ID));
	}

	@Test(expected = DownloadFailedException.class)
	public void downloadIsUnavailableInCatalogPr() throws Exception {
		final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
		manager.download(null, null);
	}

	@Test
	public void downloadUnavailableMessageIsUserFriendly() {
		try {
			final Novo19PluginManager manager = new Novo19PluginManager(Novo19FixtureSupport.clientWithFixtures());
			manager.download(null, null);
		} catch (final DownloadFailedException e) {
			assertEquals(Novo19Conf.DOWNLOAD_UNAVAILABLE_MESSAGE, e.getMessage());
		}
	}

	@Test
	public void diagnosticsRedactAuthorizationValues() throws Exception {
		final Map<String, String> transport = new HashMap<String, String>();
		transport.put(Novo19UrlBuilder.redBeeAnonymousAuthUrl(),
				"Bearer offline-session-token");
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("download");
		diagnostics.setSourcePath("/entitlement/play?token=secret");
		final String line = diagnostics.formatLogLine();
		assertFalse(line.contains("secret"));
		assertFalse(line.contains("Bearer"));
	}

	private static DownloaderPluginHolder buildHolder(final PluginDownloaderInterface downloader) {
		final Map<String, PluginDownloaderInterface> downloaders = new HashMap<String, PluginDownloaderInterface>();
		downloaders.put(FrameworkConf.YOUTUBE, downloader);
		return new DownloaderPluginHolder("cmd", downloaders, new HashMap<String, String>(), ".", ".", ".", ".");
	}

	private static Novo19CatalogClient playbackAwareCatalogClient() throws Exception {
		final Map<String, String> responses = new HashMap<String, String>();
		responses.put(Novo19UrlBuilder.bffPageByPath("player/collection-alpha-episode-alpha"),
				Novo19InlineFixtures.PLAYER_COLLECTION_EPISODE_PAGE);
		return new Novo19CatalogClient(new CatalogMapLoader(responses));
	}

	private static final class MapTransport implements Novo19HttpClient.Transport {

		private final Map<String, String> responses;

		private MapTransport(final Map<String, String> responses) {
			this.responses = responses;
		}

		@Override
		public String get(final String url, final String authorizationHeader) throws java.io.IOException {
			return load(url);
		}

		@Override
		public String postJson(final String url, final String jsonBody) throws java.io.IOException {
			return load(url);
		}

		private String load(final String url) throws java.io.IOException {
			if (!responses.containsKey(url)) {
				throw new java.io.IOException("missing " + url);
			}
			return responses.get(url);
		}

	}

	private static final class CatalogMapLoader implements Novo19CatalogClient.ContentLoader {

		private final Map<String, String> responses;

		private CatalogMapLoader(final Map<String, String> responses) {
			this.responses = responses;
		}

		@Override
		public String load(final String url) throws java.io.IOException {
			if (!responses.containsKey(url)) {
				throw new java.io.IOException("missing " + url);
			}
			return responses.get(url);
		}

	}

	private static final class RecordingDownloader implements PluginDownloaderInterface {

		private String lastInput;

		private String lastArgs;

		@Override
		public String getName() {
			return FrameworkConf.YOUTUBE;
		}

		@Override
		public DownloadableState canDownload(final String downloadInput) {
			return DownloadableState.SPECIFIC;
		}

		@Override
		public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
				throws DownloadFailedException {
			lastInput = downloadParam.getDownloadInput();
			lastArgs = downloadParam.getParam(FrameworkConf.PARAMETER_ARGS);
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
