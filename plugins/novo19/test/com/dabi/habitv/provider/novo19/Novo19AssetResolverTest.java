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
				"https://novo19.ouest-france.fr/player/on-a-de-l-info-emission-du-16-06-26", "out.mp4",
				Novo19Conf.EXTENSION);
		final String assetId = Novo19AssetResolver.resolveAssetId(param, playbackAwareClient());
		assertEquals("news-episode_565BFFb", assetId);
	}

	@Test
	public void resolvesFilmAssetFromDetailsPage() throws Exception {
		final DownloadParamDTO param = new DownloadParamDTO("https://novo19.ouest-france.fr/details/un-plan-d-enfer",
				"out.mp4", Novo19Conf.EXTENSION);
		final String assetId = Novo19AssetResolver.resolveAssetId(param, Novo19FixtureSupport.clientWithFixtures());
		assertEquals("film-plan_565BFFb", assetId);
	}

	@Test
	public void resolvesFilmAssetFromCategoryParameterWhenPathUnavailable() throws Exception {
		final DownloadParamDTO param = new DownloadParamDTO("https://novo19.ouest-france.fr/details/un-plan-d-enfer",
				"out.mp4", Novo19Conf.EXTENSION);
		param.addParam(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_FILM);
		param.addParam(Novo19Conf.PARAMETER_ASSET_ID, "film-plan_565BFFb");
		assertEquals("film-plan_565BFFb",
				Novo19AssetResolver.resolveAssetId(param, Novo19FixtureSupport.clientWithFixtures()));
	}

	@Test
	public void resolvesPlayerAssetWhenContentHasNoHref() throws Exception {
		final DownloadParamDTO param = new DownloadParamDTO(
				"https://novo19.ouest-france.fr/player/bucheron-un-metier-a-hauts-risques-chantiers-risques-face-a-la-crise",
				"out.mp4", Novo19Conf.EXTENSION);
		final java.util.Map<String, String> responses = new java.util.HashMap<String, String>();
		responses.put(
				Novo19UrlBuilder.bffPageByPath(
						"player/bucheron-un-metier-a-hauts-risques-chantiers-risques-face-a-la-crise"),
				Novo19FixtureSupport.readFixture("bff-page-player-episode-no-href.json"));
		final String assetId = Novo19AssetResolver.resolveAssetId(param, new Novo19CatalogClient(new MapLoader(responses)));
		assertEquals("OF-00000661-03-0012_565BFFb", assetId);
	}

	@Test
	public void excludesLivePlayerPath() throws Exception {
		final DownloadParamDTO param = new DownloadParamDTO("https://novo19.ouest-france.fr/player/novo19", "out.mp4",
				Novo19Conf.EXTENSION);
		assertNull(Novo19AssetResolver.resolveAssetId(param, Novo19FixtureSupport.clientWithFixtures()));
	}

	@Test
	public void returnsNullForMissingPlayerFixture() throws Exception {
		final DownloadParamDTO param = new DownloadParamDTO("https://novo19.ouest-france.fr/player/missing-episode",
				"out.mp4", Novo19Conf.EXTENSION);
		try {
			Novo19AssetResolver.resolveAssetId(param, playbackAwareClient());
		} catch (final java.io.IOException e) {
			assertTrue(e.getMessage().contains("missing"));
		}
	}

	private static Novo19CatalogClient playbackAwareClient() throws Exception {
		final java.util.Map<String, String> responses = new java.util.HashMap<String, String>();
		responses.put(Novo19UrlBuilder.bffPageByPath("player/on-a-de-l-info-emission-du-16-06-26"),
				Novo19FixtureSupport.readFixture("bff-page-player-episode.json"));
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
