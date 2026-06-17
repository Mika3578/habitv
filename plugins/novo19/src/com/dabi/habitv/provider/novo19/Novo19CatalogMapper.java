package com.dabi.habitv.provider.novo19;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.provider.novo19.dto.Novo19BffPage;
import com.dabi.habitv.provider.novo19.dto.Novo19Season;
import com.dabi.habitv.provider.novo19.dto.Novo19Tile;

final class Novo19CatalogMapper {

	private static final String[] PUBLISHED_AT_PATTERNS = new String[] { "yyyy-MM-dd'T'HH:mm:ssX",
			"yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd" };

	private Novo19CatalogMapper() {
	}

	static CategoryDTO buildRootCategory() {
		final CategoryDTO root = new CategoryDTO(Novo19Conf.NAME, "NOVO19", Novo19Conf.HOME_URL, Novo19Conf.EXTENSION);
		root.setDownloadable(false);
		return root;
	}

	static CategoryDTO buildSectionCategory(final String title, final String publicPath) {
		final String sectionTitle = Novo19PathRules.normalizeSectionTitle(title);
		final CategoryDTO section = new CategoryDTO(Novo19Conf.NAME, sectionTitle,
				Novo19UrlBuilder.publicPageUrl(publicPath), Novo19Conf.EXTENSION);
		section.setDownloadable(false);
		return section;
	}

	static CategoryDTO buildProgramCategory(final Novo19Tile tile) {
		final String publicUrl = Novo19UrlBuilder.publicPageUrl(tile.getHref());
		final CategoryDTO program = new CategoryDTO(Novo19Conf.NAME, programLabel(tile), publicUrl,
				Novo19Conf.EXTENSION);
		program.setDownloadable(true);
		applyAssetParameter(program, tile.getAssetId());
		applyDescriptionParameter(program, tile.getDescription());
		if ("VOD".equals(tile.getType())) {
			program.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_FILM);
		} else if ("SERIE".equals(tile.getType())) {
			program.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_PROGRAM);
		} else if ("COLLECTION".equals(tile.getType())) {
			program.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_COLLECTION);
		} else if ("PODCAST".equals(tile.getType())) {
			program.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_PODCAST);
			program.addParameter(Novo19Conf.PARAMETER_AUDIO_CONTENT, "true");
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
		if (Novo19Conf.CONTENT_KIND_FILM.equals(contentKind)) {
			if (page.getContent() != null) {
				addEpisodeFromTile(category, episodes, page.getContent());
			}
			return episodes;
		}
		if (Novo19Conf.CONTENT_KIND_PODCAST.equals(contentKind)) {
			appendContentRailEpisodes(category, episodes, railTiles);
			return episodes;
		}
		if (!page.getSeasons().isEmpty()) {
			for (final Novo19Season season : page.getSeasons()) {
				for (final Novo19Tile tile : season.getEpisodes()) {
					addEpisodeFromTile(category, episodes, tile);
				}
			}
			return episodes;
		}
		if (Novo19Conf.CONTENT_KIND_COLLECTION.equals(contentKind)) {
			appendContentRailEpisodes(category, episodes, railTiles);
			return episodes;
		}
		if (Novo19Conf.CONTENT_KIND_PROGRAM.equals(contentKind)) {
			appendContentRailEpisodes(category, episodes, railTiles);
		}
		return episodes;
	}

	private static void appendContentRailEpisodes(final CategoryDTO category, final Set<EpisodeDTO> episodes,
			final Collection<Novo19Tile> railTiles) {
		if (railTiles == null) {
			return;
		}
		for (final Novo19Tile tile : railTiles) {
			if (Novo19PathRules.isContentEpisodeTileType(tile.getType())) {
				addEpisodeFromTile(category, episodes, tile);
			}
		}
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
		final String name = episodeLabel(category, tile);
		if (StringUtils.isEmpty(name)) {
			return;
		}
		final EpisodeDTO episode = new EpisodeDTO(category, name, episodeUrl);
		if (tile.getDurationSeconds() != null) {
			episode.setDurationSeconds(tile.getDurationSeconds());
		}
		final Date publishedAt = parsePublishedAt(tile.getPublishedAt());
		if (publishedAt != null) {
			episode.setEpisodeDate(publishedAt);
		}
		episodes.add(episode);
	}

	static String programLabel(final Novo19Tile tile) {
		if (!StringUtils.isEmpty(tile.getTitle())) {
			return tile.getTitle();
		}
		return tile.getSubtitle();
	}

	static String episodeLabel(final CategoryDTO category, final Novo19Tile tile) {
		if (Novo19Conf.CONTENT_KIND_PODCAST.equals(category.getParameter(Novo19Conf.PARAMETER_CONTENT_KIND))
				&& !StringUtils.isEmpty(tile.getTitle())) {
			return tile.getTitle().trim();
		}
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
			return Novo19UrlBuilder.publicPageUrl(tile.getHref());
		}
		return null;
	}

	static boolean isPodcastDetailPage(final Novo19BffPage page) {
		if (page == null) {
			return false;
		}
		for (final com.dabi.habitv.provider.novo19.dto.Novo19Rail rail : page.getRails()) {
			if (Novo19PathRules.isPodcastDetailRailSrc(rail.getSrc())) {
				return true;
			}
		}
		return false;
	}

	private static void applyAssetParameter(final CategoryDTO category, final String assetId) {
		if (!StringUtils.isEmpty(assetId)) {
			category.addParameter(Novo19Conf.PARAMETER_ASSET_ID, assetId);
		}
	}

	private static void applyDescriptionParameter(final CategoryDTO category, final String description) {
		if (!StringUtils.isEmpty(description)) {
			category.addParameter(Novo19Conf.PARAMETER_DESCRIPTION, description);
		}
	}

	private static int parseSeasonIndex(final String seasonIndexValue) {
		try {
			return Integer.parseInt(seasonIndexValue);
		} catch (final NumberFormatException e) {
			return -1;
		}
	}

	private static Date parsePublishedAt(final String value) {
		if (StringUtils.isEmpty(value)) {
			return null;
		}
		for (final String pattern : PUBLISHED_AT_PATTERNS) {
			try {
				final SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
				format.setTimeZone(TimeZone.getTimeZone("UTC"));
				format.setLenient(false);
				return format.parse(value);
			} catch (final ParseException e) {
				// try next pattern
			}
		}
		return null;
	}

}
