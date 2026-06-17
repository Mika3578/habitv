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
				Novo19FixtureSupport.readFixture("redbee-auth-anonymous.json"));
		transport.put(Novo19UrlBuilder.redBeePlayUrl("news-episode_565BFFb"),
				Novo19FixtureSupport.readFixture("redbee-play-replay-hls.json"));
		final Novo19HttpClient.Transport http = new MapTransport(transport);
		final Novo19CatalogClient catalogClient = playbackAwareCatalogClient();
		final RecordingDownloader downloader = new RecordingDownloader();
		final Novo19PluginManager manager = new Novo19PluginManager(catalogClient, new Novo19PlaybackClient(http));
		final DownloadParamDTO param = new DownloadParamDTO(
				"https://novo19.ouest-france.fr/player/on-a-de-l-info-emission-du-16-06-26", "out.mp4",
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
				Novo19FixtureSupport.readFixture("redbee-auth-anonymous.json"));
		transport.put(Novo19UrlBuilder.redBeePlayUrl("podcast-episode_565BFFb"),
				Novo19FixtureSupport.readFixture("redbee-play-podcast-hls.json"));
		final Map<String, String> catalogResponses = new HashMap<String, String>();
		catalogResponses.put(Novo19UrlBuilder.bffPageByPath("player/le-royaume-episode-1"),
				Novo19FixtureSupport.readFixture("bff-page-player-podcast.json"));
		final Novo19PluginManager manager = new Novo19PluginManager(new Novo19CatalogClient(new CatalogMapLoader(catalogResponses)),
				new Novo19PlaybackClient(new MapTransport(transport)));
		final DownloadParamDTO param = new DownloadParamDTO("https://novo19.ouest-france.fr/player/le-royaume-episode-1",
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
				Novo19FixtureSupport.readFixture("redbee-auth-anonymous.json"));
		transport.put(Novo19UrlBuilder.redBeePlayUrl("OF-00000661-03-0012_565BFFb"),
				Novo19FixtureSupport.readFixture("redbee-play-replay-object-drm.json"));
		final Map<String, String> catalogResponses = new HashMap<String, String>();
		catalogResponses.put(Novo19UrlBuilder.bffPageByPath(
				"player/bucheron-un-metier-a-hauts-risques-chantiers-risques-face-a-la-crise"),
				Novo19FixtureSupport.readFixture("bff-page-player-episode-no-href.json"));
		final Novo19PluginManager manager = new Novo19PluginManager(
				new Novo19CatalogClient(new CatalogMapLoader(catalogResponses)),
				new Novo19PlaybackClient(new MapTransport(transport)));
		final DownloadParamDTO param = new DownloadParamDTO(
				"https://novo19.ouest-france.fr/player/bucheron-un-metier-a-hauts-risques-chantiers-risques-face-a-la-crise",
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
		responses.put(Novo19UrlBuilder.bffPageByPath("player/on-a-de-l-info-emission-du-16-06-26"),
				Novo19FixtureSupport.readFixture("bff-page-player-episode.json"));
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
