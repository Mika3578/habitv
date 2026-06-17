package com.dabi.habitv.provider.novo19;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.provider.novo19.dto.Novo19BffPage;
import com.dabi.habitv.provider.novo19.dto.Novo19Season;
import com.dabi.habitv.provider.novo19.dto.Novo19Tile;

final class Novo19CatalogMapper {

	private Novo19CatalogMapper() {
	}

	static CategoryDTO buildRootCategory() {
		final CategoryDTO root = new CategoryDTO(Novo19Conf.NAME, "NOVO19", Novo19Conf.HOME_URL, Novo19Conf.EXTENSION);
		root.setDownloadable(false);
		return root;
	}

	static CategoryDTO buildSectionCategory(final String title, final String publicPath) {
		final CategoryDTO section = new CategoryDTO(Novo19Conf.NAME, title, Novo19UrlBuilder.publicPageUrl(publicPath),
				Novo19Conf.EXTENSION);
		section.setDownloadable(false);
		return section;
	}

	static CategoryDTO buildProgramCategory(final Novo19Tile tile) {
		final String publicUrl = Novo19UrlBuilder.publicPageUrl(tile.getHref());
		final CategoryDTO program = new CategoryDTO(Novo19Conf.NAME, programLabel(tile), publicUrl,
				Novo19Conf.EXTENSION);
		program.setDownloadable(true);
		applyAssetParameter(program, tile.getAssetId());
		if ("VOD".equals(tile.getType())) {
			program.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_FILM);
		} else if ("SERIE".equals(tile.getType())) {
			program.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_PROGRAM);
		} else if ("COLLECTION".equals(tile.getType())) {
			program.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_COLLECTION);
		}
		return program;
	}

	static void appendSeasonSubcategories(final CategoryDTO program, final Novo19BffPage detailPage) {
		if (program == null || detailPage == null) {
			return;
		}
		final List<Novo19Season> seasons = detailPage.getSeasons();
		if (seasons.isEmpty()) {
			return;
		}
		for (final Novo19Season season : seasons) {
			if (season.getEpisodes().isEmpty()) {
				continue;
			}
			final String seasonTitle = StringUtils.isEmpty(season.getTitle()) ? ("Season " + (season.getIndex() + 1))
					: season.getTitle();
			final String seasonId = program.getId() + "#season-" + season.getIndex();
			final CategoryDTO seasonCategory = new CategoryDTO(Novo19Conf.NAME, seasonTitle, seasonId,
					Novo19Conf.EXTENSION);
			seasonCategory.setDownloadable(true);
			seasonCategory.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_SEASON);
			seasonCategory.addParameter(Novo19Conf.PARAMETER_SEASON_INDEX, String.valueOf(season.getIndex()));
			program.addSubCategory(seasonCategory);
		}
	}

	static Set<EpisodeDTO> mapEpisodes(final CategoryDTO category, final Novo19BffPage page,
			final Collection<Novo19Tile> railTiles) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		final String seasonIndexValue = category.getParameter(Novo19Conf.PARAMETER_SEASON_INDEX);
		if (!StringUtils.isEmpty(seasonIndexValue)) {
			appendSeasonEpisodes(category, page, episodes, parseSeasonIndex(seasonIndexValue));
			return episodes;
		}
		final String contentKind = category.getParameter(Novo19Conf.PARAMETER_CONTENT_KIND);
		if (!page.getSeasons().isEmpty()) {
			for (final Novo19Season season : page.getSeasons()) {
				for (final Novo19Tile tile : season.getEpisodes()) {
					addEpisodeFromTile(category, episodes, tile);
				}
			}
		}
		if (railTiles != null) {
			for (final Novo19Tile tile : railTiles) {
				if (Novo19PathRules.isEpisodeTileType(tile.getType()) || "EPISODE".equals(tile.getType())) {
					addEpisodeFromTile(category, episodes, tile);
				}
			}
		}
		if (episodes.isEmpty() && Novo19Conf.CONTENT_KIND_FILM.equals(contentKind) && page.getContent() != null) {
			addEpisodeFromTile(category, episodes, page.getContent());
		}
		return episodes;
	}

	private static void appendSeasonEpisodes(final CategoryDTO category, final Novo19BffPage page,
			final Set<EpisodeDTO> episodes, final int seasonIndex) {
		if (page.getSeasons().isEmpty() || seasonIndex < 0 || seasonIndex >= page.getSeasons().size()) {
			return;
		}
		for (final Novo19Tile tile : page.getSeasons().get(seasonIndex).getEpisodes()) {
			addEpisodeFromTile(category, episodes, tile);
		}
	}

	static void addEpisodeFromTile(final CategoryDTO category, final Set<EpisodeDTO> episodes, final Novo19Tile tile) {
		if (tile == null) {
			return;
		}
		final String episodeUrl = episodeUrl(tile);
		if (StringUtils.isEmpty(episodeUrl)) {
			return;
		}
		final String name = episodeLabel(tile);
		if (StringUtils.isEmpty(name)) {
			return;
		}
		final EpisodeDTO episode = new EpisodeDTO(category, name, episodeUrl);
		if (tile.getDurationSeconds() != null) {
			episode.setDurationSeconds(tile.getDurationSeconds());
		}
		episodes.add(episode);
	}

	static String programLabel(final Novo19Tile tile) {
		if (!StringUtils.isEmpty(tile.getTitle())) {
			return tile.getTitle();
		}
		return tile.getSubtitle();
	}

	static String episodeLabel(final Novo19Tile tile) {
		final StringBuilder label = new StringBuilder();
		if (!StringUtils.isEmpty(tile.getTitle())) {
			label.append(tile.getTitle());
		}
		if (!StringUtils.isEmpty(tile.getSubtitle())) {
			if (label.length() > 0) {
				label.append(" - ");
			}
			label.append(tile.getSubtitle());
		}
		return label.toString().trim();
	}

	static String episodeUrl(final Novo19Tile tile) {
		if (!StringUtils.isEmpty(tile.getHref())) {
			if (tile.getHref().startsWith("/player/")) {
				return Novo19UrlBuilder.publicPageUrl(tile.getHref());
			}
			if (tile.getHref().startsWith("/details/")) {
				return Novo19UrlBuilder.publicPageUrl(tile.getHref());
			}
			return Novo19UrlBuilder.publicPageUrl(tile.getHref());
		}
		return null;
	}

	private static void applyAssetParameter(final CategoryDTO category, final String assetId) {
		if (!StringUtils.isEmpty(assetId)) {
			category.addParameter(Novo19Conf.PARAMETER_ASSET_ID, assetId);
		}
	}

	private static int parseSeasonIndex(final String seasonIndexValue) {
		try {
			return Integer.parseInt(seasonIndexValue);
		} catch (final NumberFormatException e) {
			return -1;
		}
	}

}
