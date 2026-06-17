package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.provider.novo19.dto.Novo19BffPage;
import com.dabi.habitv.provider.novo19.dto.Novo19Tile;
import com.dabi.habitv.provider.novo19.dto.Novo19TilesResponse;

public class Novo19CatalogMapperTest {

	@Test
	public void mapsProgramCategoryWithAssetParameter() {
		final Novo19Tile tile = new Novo19Tile("serie-1", "SERIE", "FBI : Portés disparus", null, null, null,
				"/details/fbi-portes-disparus", "serie-1_565BFFb");
		final CategoryDTO category = Novo19CatalogMapper.buildProgramCategory(tile);
		assertEquals("https://novo19.ouest-france.fr/details/fbi-portes-disparus", category.getId());
		assertEquals("serie-1_565BFFb", category.getParameter(Novo19Conf.PARAMETER_ASSET_ID));
		assertTrue(category.isDownloadable());
	}

	@Test
	public void mapsEpisodeLabelWithSubtitle() {
		final Novo19Tile tile = new Novo19Tile("ep-1", "EPISODE", "Episode title", "S1E11", null, 1200L,
				"/player/sample-episode", "ep-1");
		assertEquals("Episode title - S1E11", Novo19CatalogMapper.episodeLabel(tile));
	}

	@Test
	public void suppressesDuplicateEpisodeIds() throws Exception {
		final CategoryDTO category = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("col", "COLLECTION",
				"On a de l'info", null, null, null, "/details/on-a-de-l-info", "col-id"));
		final Novo19BffPage page = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.readFixture("bff-page-info-collection.json"), "fixture");
		final Novo19TilesResponse tiles = Novo19PageParser.parseTilesEnvelope(
				Novo19FixtureSupport.readFixture("bff-tiles-info-episodes.json"), "fixture");
		final Set<EpisodeDTO> episodes = Novo19CatalogMapper.mapEpisodes(category, page, tiles.getTiles());
		assertEquals(1, episodes.size());
	}

	@Test
	public void mapsFilmEpisodeFromContentWhenRailsEmpty() throws Exception {
		final CategoryDTO category = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("film", "VOD",
				"Un plan d'enfer", null, null, 5196L, "/details/un-plan-d-enfer", "film-plan_565BFFb"));
		final Novo19BffPage page = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.readFixture("bff-page-film-details.json"), "fixture");
		final Set<EpisodeDTO> episodes = Novo19CatalogMapper.mapEpisodes(category, page, null);
		assertEquals(1, episodes.size());
		assertEquals(5196L, episodes.iterator().next().getDurationSeconds().longValue());
	}

	@Test
	public void appendsSeasonSubcategories() throws Exception {
		final CategoryDTO program = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("serie", "SERIE",
				"FBI : Portés disparus", null, null, null, "/details/fbi-portes-disparus", "serie-fbi"));
		final Novo19BffPage page = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.readFixture("bff-page-series-details.json"), "fixture");
		Novo19CatalogMapper.appendSeasonSubcategories(program, page);
		assertEquals(1, program.getSubCategories().size());
		assertEquals(Novo19Conf.CONTENT_KIND_SEASON,
				program.getSubCategories().iterator().next().getParameter(Novo19Conf.PARAMETER_CONTENT_KIND));
	}

	@Test
	public void seasonCategoryMapsOnlySeasonEpisodes() throws Exception {
		final CategoryDTO program = Novo19CatalogMapper.buildProgramCategory(new Novo19Tile("serie", "SERIE",
				"FBI : Portés disparus", null, null, null, "/details/fbi-portes-disparus", "serie-fbi"));
		final Novo19BffPage page = Novo19PageParser.parsePageEnvelope(
				Novo19FixtureSupport.readFixture("bff-page-series-details.json"), "fixture");
		Novo19CatalogMapper.appendSeasonSubcategories(program, page);
		final CategoryDTO season = program.getSubCategories().iterator().next();
		final Set<EpisodeDTO> episodes = Novo19CatalogMapper.mapEpisodes(season, page, null);
		assertEquals(1, episodes.size());
		assertTrue(episodes.iterator().next().getName().contains("Une petite ville bien tranquille"));
	}

	@Test
	public void missingDescriptionAndThumbnailDoNotBlockEpisode() {
		final CategoryDTO category = new CategoryDTO(Novo19Conf.NAME, "Info", "https://novo19.ouest-france.fr/details/info",
				Novo19Conf.EXTENSION);
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		Novo19CatalogMapper.addEpisodeFromTile(category, episodes,
				new Novo19Tile("ep", "EPISODE", "Title only", null, null, null, "/player/title-only", "ep-id"));
		assertEquals(1, episodes.size());
	}

}
