package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO;
import com.dabi.habitv.provider.novo19.dto.Novo19BffPage;
import com.dabi.habitv.provider.novo19.dto.Novo19Rail;
import com.dabi.habitv.provider.novo19.dto.Novo19Season;
import com.dabi.habitv.provider.novo19.dto.Novo19Tile;
import com.dabi.habitv.provider.novo19.dto.Novo19TilesResponse;

public class Novo19CatalogMapperTest {

	@Test
	public void mapsProgramCategoryWithAssetParameter() {
		final Novo19Tile tile = new Novo19Tile("serie-1", "SERIE", "Series Alpha", null, null, null,
				"/details/series-alpha", "asset-series-alpha");
		final CategoryDTO category = Novo19CatalogMapper.buildProgramCategory(tile);
		assertEquals("https://novo19.ouest-france.fr/details/series-alpha", category.getId());
		assertEquals("asset-series-alpha", category.getParameter(Novo19Conf.PARAMETER_ASSET_ID));
		assertTrue(category.isDownloadable());
	}

	@Test
	public void mapsEpisodeLabelWithSubtitle() {
		final CategoryDTO category = new CategoryDTO(Novo19Conf.NAME, "Programme Gamma",
				"https://novo19.ouest-france.fr/details/programme-gamma",
				Novo19Conf.EXTENSION);
		category.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_PROGRAM);
		final Novo19Tile tile = new Novo19Tile("ep-1", "EPISODE", "Episode title", "S1E11", null, 1200L,
				"/player/sample-episode", "ep-1");
		assertEquals("Episode title - S1E11", Novo19CatalogMapper.episodeLabel(category, tile));
	}

	@Test
	public void suppressesDuplicateEpisodeIds() throws Exception {
		final CategoryDTO category = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("col", "COLLECTION",
				"Collection Alpha", null, null, null, "/details/collection-alpha", "col-id"));
		final Novo19BffPage page = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.detailPage("collectionAlpha"), "fixture");
		final Novo19TilesResponse tiles = Novo19PageParser.parseTilesEnvelope(
				Novo19FixtureSupport.episodeRails("collectionAlpha"), "fixture");
		final Set<EpisodeDTO> episodes = Novo19CatalogMapper.mapEpisodes(category, page, tiles.getTiles());
		assertEquals(1, episodes.size());
	}

	@Test
	public void mapsFilmEpisodeFromContentWhenRailsEmpty() throws Exception {
		final CategoryDTO category = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("film", "VOD",
				"Film Alpha", null, null, 5196L, "/details/film-alpha", "asset-film-alpha"));
		final Novo19BffPage page = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.detailPage("filmAlpha"), "fixture");
		final Set<EpisodeDTO> episodes = Novo19CatalogMapper.mapEpisodes(category, page, null);
		assertEquals(1, episodes.size());
		assertEquals(5196L, episodes.iterator().next().getDurationSeconds().longValue());
	}

	@Test
	public void filmIgnoresRecommendationRailTiles() throws Exception {
		final CategoryDTO category = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("film", "VOD", "Film Beta",
				null, null, 7200L, "/details/film-beta", "asset-film-beta"));
		final Novo19BffPage page = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.detailPage("filmBeta"), "fixture");
		final Novo19TilesResponse recoTiles = Novo19PageParser.parseTilesEnvelope(
				Novo19FixtureSupport.episodeRails("filmBetaReco"), "fixture");
		final Set<EpisodeDTO> episodes = Novo19CatalogMapper.mapEpisodes(category, page, recoTiles.getTiles());
		assertEquals(1, episodes.size());
		assertEquals("Film Beta", episodes.iterator().next().getName());
	}

	@Test
	public void appendsSeasonSubcategories() throws Exception {
		final CategoryDTO program = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("serie", "SERIE",
				"Series Alpha", null, null, null, "/details/series-alpha", "asset-series-alpha"));
		final Novo19BffPage page = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.detailPage("seriesAlpha"), "fixture");
		Novo19CatalogMapper.appendSeasonSubcategories(program, page);
		assertEquals(1, program.getSubCategories().size());
		assertEquals(Novo19Conf.CONTENT_KIND_SEASON,
				program.getSubCategories().iterator().next().getParameter(Novo19Conf.PARAMETER_CONTENT_KIND));
	}

	@Test
	public void seasonCategoryMapsOnlySeasonEpisodes() throws Exception {
		final CategoryDTO program = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("serie", "SERIE",
				"Series Alpha", null, null, null, "/details/series-alpha", "asset-series-alpha"));
		final Novo19BffPage page = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.detailPage("seriesAlpha"), "fixture");
		Novo19CatalogMapper.appendSeasonSubcategories(program, page);
		final CategoryDTO season = program.getSubCategories().iterator().next();
		final Set<EpisodeDTO> episodes = Novo19CatalogMapper.mapEpisodes(season, page, null);
		assertEquals(1, episodes.size());
		assertTrue(episodes.iterator().next().getName().contains("Episode Alpha"));
	}

	@Test
	public void missingDescriptionAndThumbnailDoNotBlockEpisode() {
		final CategoryDTO category = new CategoryDTO(Novo19Conf.NAME, "Info", "https://novo19.ouest-france.fr/details/info",
				Novo19Conf.EXTENSION);
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		Novo19CatalogMapper.addEpisodeFromTile(category, episodes,
				new Novo19Tile("ep", "EPISODE", "Title only", null, null, null, "/player/title-only", "ep-id"));
		assertEquals(1, episodes.size());
		assertNotNull(episodes.iterator().next().getMetadata());
		assertEquals("Title only", episodes.iterator().next().getMetadata().getEpisodeTitle());
		assertNull(episodes.iterator().next().getMetadata().getAirDate());
	}

	@Test
	public void mapsCanonicalMetadataFromSeasonEpisodeSubtitle() {
		final CategoryDTO program = new CategoryDTO(Novo19Conf.NAME, "Series Alpha",
				"https://novo19.ouest-france.fr/details/series-alpha", Novo19Conf.EXTENSION);
		program.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_PROGRAM);
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		Novo19CatalogMapper.addEpisodeFromTile(program, episodes,
				new Novo19Tile("ep-1", "EPISODE", "Episode Alpha", "S1E11", "Synopsis text", 2491L,
						"/player/series-alpha-episode-alpha", "asset-series-alpha-episode-alpha"));
		final EpisodeDTO episode = episodes.iterator().next();
		final EpisodeMetadataDTO metadata = episode.getMetadata();
		assertNotNull(metadata);
		assertEquals("Series Alpha", metadata.getSeriesTitle());
		assertEquals("Episode Alpha", metadata.getEpisodeTitle());
		assertEquals(Integer.valueOf(1), metadata.getSeasonNumber());
		assertEquals(Integer.valueOf(11), metadata.getEpisodeNumber());
		assertEquals(Long.valueOf(2491L), metadata.getDurationSeconds());
		assertEquals("Synopsis text", metadata.getDescription());
		assertEquals("ep-1", metadata.getProviderEpisodeId());
		assertEquals("https://novo19.ouest-france.fr/player/series-alpha-episode-alpha", metadata.getSourceUrl());
		assertNull(metadata.getAirDate());
		assertNull(metadata.getPublicationDate());
		assertNull(metadata.getThumbnailUrl());
	}

	@Test
	public void publishedAtMapsToPublicationDateNotAirDate() {
		final CategoryDTO category = new CategoryDTO(Novo19Conf.NAME, "Podcast Alpha",
				"https://novo19.ouest-france.fr/details/podcast-alpha", Novo19Conf.PODCAST_EXTENSION);
		category.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_PODCAST);
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		Novo19CatalogMapper.addEpisodeFromTile(category, episodes,
				new Novo19Tile("pod-1", "AUDIO", "Episode 1", null, "Desc", 600L, "/player/pod-1", "pod-1",
						"2026-01-15T10:00:00Z"));
		final EpisodeMetadataDTO metadata = episodes.iterator().next().getMetadata();
		assertNotNull(metadata.getPublicationDate());
		assertNull(metadata.getAirDate());
		assertEquals(episodes.iterator().next().getEpisodeDate(), metadata.getPublicationDate());
	}

	@Test
	public void genreSubtitleIsNotSeasonEpisode() {
		assertNull(Novo19CatalogMapper.parseSeasonEpisodeCode("Société"));
		assertNull(Novo19CatalogMapper.parseSeasonEpisodeCode("Histoire"));
		final int[] parsed = Novo19CatalogMapper.parseSeasonEpisodeCode("S3E10");
		assertNotNull(parsed);
		assertEquals(3, parsed[0]);
		assertEquals(10, parsed[1]);
	}

	@Test
	public void seasonEpisodeOverflowReturnsNull() {
		assertNull(Novo19CatalogMapper.parseSeasonEpisodeCode("S99999999999E1"));
		assertNull(Novo19CatalogMapper.parseSeasonEpisodeCode("S1E99999999999"));
	}

	@Test
	public void seasonEpisodeNonPositiveReturnsNull() {
		assertNull(Novo19CatalogMapper.parseSeasonEpisodeCode("S0E1"));
		assertNull(Novo19CatalogMapper.parseSeasonEpisodeCode("S1E0"));
	}

	@Test
	public void mapsPodcastProgramWithAudioParameter() {
		final Novo19Tile tile = new Novo19Tile("asset-podcast-alpha", "PODCAST", "Podcast Alpha", null,
				"Synthetic subtitle", null, "/details/podcast-alpha", "asset-podcast-alpha");
		final CategoryDTO category = Novo19CatalogMapper.buildProgramCategory(tile);
		assertEquals(Novo19Conf.CONTENT_KIND_PODCAST, category.getParameter(Novo19Conf.PARAMETER_CONTENT_KIND));
		assertEquals("true", category.getParameter(Novo19Conf.PARAMETER_AUDIO_CONTENT));
		assertEquals(Novo19Conf.PODCAST_EXTENSION, category.getExtension());
		assertEquals("Synthetic subtitle", category.getParameter(Novo19Conf.PARAMETER_DESCRIPTION));
	}

	@Test
	public void mapsVideoProgramWithMp4Extension() {
		final Novo19Tile tile = new Novo19Tile("asset-series-alpha", "SERIE", "Series Alpha", null, null, null,
				"/details/series-alpha", "asset-series-alpha");
		final CategoryDTO category = Novo19CatalogMapper.buildProgramCategory(tile);
		assertEquals(Novo19Conf.EXTENSION, category.getExtension());
		assertEquals(Novo19Conf.CONTENT_KIND_PROGRAM, category.getParameter(Novo19Conf.PARAMETER_CONTENT_KIND));
	}

	@Test
	public void ensureExtensionMatchesContentKindRewrapsPodcastCategory() {
		final CategoryDTO videoCategory = new CategoryDTO(Novo19Conf.NAME, "Podcast Alpha",
				"https://novo19.ouest-france.fr/details/podcast-alpha", Novo19Conf.EXTENSION);
		videoCategory.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_PODCAST);
		videoCategory.addParameter(Novo19Conf.PARAMETER_AUDIO_CONTENT, "true");
		final CategoryDTO podcastCategory = Novo19CatalogMapper.ensureExtensionMatchesContentKind(videoCategory);
		assertEquals(Novo19Conf.PODCAST_EXTENSION, podcastCategory.getExtension());
	}

	@Test
	public void mapsPodcastEpisodesFromContentRail() throws Exception {
		final CategoryDTO category = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("asset-podcast-alpha",
				"PODCAST", "Podcast Alpha", null, null, null, "/details/podcast-alpha",
				"asset-podcast-alpha"));
		final Novo19BffPage page = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.detailPage("podcastAlpha"), "fixture");
		final Novo19TilesResponse tiles = Novo19PageParser.parseTilesEnvelope(
				Novo19FixtureSupport.episodeRails("podcastAlpha"), "fixture");
		final Set<EpisodeDTO> episodes = Novo19CatalogMapper.mapEpisodes(category, page, tiles.getTiles());
		assertEquals(3, episodes.size());
		assertTrue(Novo19TestSupport.containsEpisodeName(episodes, "Episode Alpha"));
		assertTrue(Novo19TestSupport.containsEpisodeName(episodes, "Episode Beta"));
		assertTrue(Novo19TestSupport.containsEpisodeName(episodes, "Episode Gamma"));
	}

	@Test
	public void podcastEpisodeUsesTitleOnlyLabel() {
		final CategoryDTO category = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("podcast", "PODCAST",
				"Podcast Alpha", null, null, null, "/details/podcast-alpha", "podcast-id"));
		final Novo19Tile tile = new Novo19Tile("ep", "PODCAST", "Episode Alpha", "Episode 1", null, 600L,
				"/player/podcast-alpha-episode-alpha", "ep-id");
		assertEquals("Episode Alpha", Novo19CatalogMapper.episodeLabel(category, tile));
	}

	@Test
	public void podcastSeasonSubcategoryInheritsMp3Extension() {
		final CategoryDTO podcast = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("asset-podcast-alpha",
				"PODCAST", "Podcast Alpha", null, null, null, "/details/podcast-alpha", "asset-podcast-alpha"));
		final List<Novo19Tile> episodes = Arrays.asList(new Novo19Tile("ep", "PODCAST", "Episode Alpha", null, null,
				600L, "/player/podcast-alpha-episode-alpha", "ep-id"));
		final Novo19BffPage page = new Novo19BffPage("PODCAST", "asset-podcast-alpha", "Podcast Alpha",
				Collections.<Novo19Rail>emptyList(), Arrays.asList(new Novo19Season("Season 1", 0, episodes)), null);
		Novo19CatalogMapper.appendSeasonSubcategories(podcast, page);
		assertEquals(1, podcast.getSubCategories().size());
		assertEquals(Novo19Conf.PODCAST_EXTENSION, podcast.getSubCategories().iterator().next().getExtension());
	}

}
