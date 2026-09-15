package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;

public class Novo19AssetResolverTest {

	@Test
	public void resolvesAssetFromPlayerPageFixture() throws Exception {
		final DownloadParamDTO param = new DownloadParamDTO(
				"https://novo19.ouest-france.fr/player/collection-alpha-episode-alpha", "out.mp4",
				Novo19Conf.EXTENSION);
		final String assetId = Novo19AssetResolver.resolveAssetId(param, playbackAwareClient());
		assertEquals("asset-collection-alpha-episode-alpha", assetId);
	}

	@Test
	public void resolvesFilmAssetFromDetailsPage() throws Exception {
		final DownloadParamDTO param = new DownloadParamDTO("https://novo19.ouest-france.fr/details/film-alpha",
				"out.mp4", Novo19Conf.EXTENSION);
		final String assetId = Novo19AssetResolver.resolveAssetId(param, Novo19FixtureSupport.clientWithFixtures());
		assertEquals("asset-film-alpha", assetId);
	}

	@Test
	public void resolvesFilmAssetFromCategoryParameterWhenPathUnavailable() throws Exception {
		final DownloadParamDTO param = new DownloadParamDTO("https://novo19.ouest-france.fr/details/film-alpha",
				"out.mp4", Novo19Conf.EXTENSION);
		param.addParam(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_FILM);
		param.addParam(Novo19Conf.PARAMETER_ASSET_ID, "asset-film-alpha");
		assertEquals("asset-film-alpha",
				Novo19AssetResolver.resolveAssetId(param, Novo19FixtureSupport.clientWithFixtures()));
	}

	@Test
	public void resolvesPlayerAssetWhenContentHasNoHref() throws Exception {
		final DownloadParamDTO param = new DownloadParamDTO(
				"https://novo19.ouest-france.fr/player/series-beta-episode-beta",
				"out.mp4", Novo19Conf.EXTENSION);
		final java.util.Map<String, String> responses = new java.util.HashMap<String, String>();
		responses.put(
				Novo19UrlBuilder.bffPageByPath(
						"player/series-beta-episode-beta"),
				Novo19InlineFixtures.PLAYER_SERIES_EPISODE_NO_HREF_PAGE);
		final String assetId = Novo19AssetResolver.resolveAssetId(param, new Novo19CatalogClient(new MapLoader(responses)));
		assertEquals("asset-series-beta-episode-beta", assetId);
	}

	@Test
	public void excludesLivePlayerPath() throws Exception {
		final DownloadParamDTO param = new DownloadParamDTO("https://novo19.ouest-france.fr/player/novo19", "out.mp4",
				Novo19Conf.EXTENSION);
		assertNull(Novo19AssetResolver.resolveAssetId(param, Novo19FixtureSupport.clientWithFixtures()));
	}

	@Test
	public void propagatesIoExceptionForMissingPlayerFixture() throws Exception {
		final DownloadParamDTO param = new DownloadParamDTO("https://novo19.ouest-france.fr/player/missing-episode",
				"out.mp4", Novo19Conf.EXTENSION);
		try {
			Novo19AssetResolver.resolveAssetId(param, playbackAwareClient());
			throw new AssertionError("expected IOException");
		} catch (final java.io.IOException e) {
			assertTrue(e.getMessage().contains("missing"));
		}
	}

	private static Novo19CatalogClient playbackAwareClient() throws Exception {
		final java.util.Map<String, String> responses = new java.util.HashMap<String, String>();
		responses.put(Novo19UrlBuilder.bffPageByPath("player/collection-alpha-episode-alpha"),
				Novo19InlineFixtures.PLAYER_COLLECTION_EPISODE_PAGE);
		return new Novo19CatalogClient(new MapLoader(responses));
	}

	private static final class MapLoader implements Novo19CatalogClient.ContentLoader {

		private final java.util.Map<String, String> responses;

		private MapLoader(final java.util.Map<String, String> responses) {
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

}
