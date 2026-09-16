package com.dabi.habitv.provider.novo19;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO;
import com.dabi.habitv.provider.novo19.dto.Novo19BffPage;
import com.dabi.habitv.provider.novo19.dto.Novo19Season;
import com.dabi.habitv.provider.novo19.dto.Novo19Tile;

final class Novo19CatalogMapper {

	private static final String[] PUBLISHED_AT_PATTERNS = new String[] { "yyyy-MM-dd'T'HH:mm:ssX",
			"yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd" };

	private static final Pattern SEASON_EPISODE_PATTERN = Pattern.compile("^S(\\d+)E(\\d+)$",
			Pattern.CASE_INSENSITIVE);

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

	static CategoryDTO buildEditorialSectionCategory(final String editorialName) {
		final CategoryDTO section = new CategoryDTO(Novo19Conf.NAME, editorialName,
				Novo19UrlBuilder.publicPageUrl("/categories#editorial-" + editorialName.toLowerCase()),
				Novo19Conf.EXTENSION);
		section.setDownloadable(false);
		section.addParameter(Novo19Conf.PARAMETER_EDITORIAL_BUCKET, editorialName);
		return section;
	}

	static CategoryDTO buildProgramCategory(final Novo19Tile tile) {
		return buildProgramCategory(tile, programLabel(tile));
	}

	static CategoryDTO buildProgramCategory(final Novo19Tile tile, final String title) {
		final String publicUrl = Novo19UrlBuilder.publicPageUrl(tile.getHref());
		final CategoryDTO program = new CategoryDTO(Novo19Conf.NAME, title, publicUrl,
				extensionForTileType(tile.getType()));
		program.setDownloadable(true);
		applyAssetParameter(program, tile.getAssetId());
		applyDescriptionParameter(program, tile.getDescription());
		applyContentKindFromTileType(program, tile.getType());
		return program;
	}

	static CategoryDTO ensureExtensionMatchesContentKind(final CategoryDTO category) {
		if (category == null) {
			return null;
		}
		final String expected = extensionForContentKind(category);
		if (expected.equals(category.getExtension())) {
			return category;
		}
		return cloneWithExtension(category, expected);
	}

	static CategoryDTO cloneWithExtension(final CategoryDTO source, final String extension) {
		if (source == null) {
			return null;
		}
		final CategoryDTO clone = new CategoryDTO(source.getPlugin(), source.getName(), source.getId(), extension);
		clone.setDownloadable(source.isDownloadable());
		for (final Map.Entry<String, String> entry : source.getParameters().entrySet()) {
			clone.addParameter(entry.getKey(), entry.getValue());
		}
		for (final CategoryDTO subCategory : source.getSubCategories()) {
			clone.addSubCategory(cloneWithExtension(subCategory, extensionForChildCategory(source, subCategory)));
		}
		return clone;
	}

	private static String extensionForChildCategory(final CategoryDTO parent, final CategoryDTO child) {
		if (Novo19Conf.CONTENT_KIND_PODCAST.equals(parent.getParameter(Novo19Conf.PARAMETER_CONTENT_KIND))
				|| "true".equalsIgnoreCase(parent.getParameter(Novo19Conf.PARAMETER_AUDIO_CONTENT))) {
			return Novo19Conf.PODCAST_EXTENSION;
		}
		return extensionForContentKind(child);
	}

	static String extensionForContentKind(final CategoryDTO category) {
		if (category == null) {
			return Novo19Conf.EXTENSION;
		}
		if (Novo19Conf.CONTENT_KIND_PODCAST.equals(category.getParameter(Novo19Conf.PARAMETER_CONTENT_KIND))
				|| "true".equalsIgnoreCase(category.getParameter(Novo19Conf.PARAMETER_AUDIO_CONTENT))) {
			return Novo19Conf.PODCAST_EXTENSION;
		}
		return Novo19Conf.EXTENSION;
	}

	private static String extensionForTileType(final String tileType) {
		if ("PODCAST".equals(tileType)) {
			return Novo19Conf.PODCAST_EXTENSION;
		}
		return Novo19Conf.EXTENSION;
	}

	static CategoryDTO cloneProgramCategory(final CategoryDTO source) {
		if (source == null) {
			return null;
		}
		final CategoryDTO clone = new CategoryDTO(source.getPlugin(), source.getName(), source.getId(),
				source.getExtension());
		clone.setDownloadable(source.isDownloadable());
		for (final Map.Entry<String, String> entry : source.getParameters().entrySet()) {
			clone.addParameter(entry.getKey(), entry.getValue());
		}
		for (final CategoryDTO subCategory : source.getSubCategories()) {
			clone.addSubCategory(cloneProgramCategory(subCategory));
		}
		return clone;
	}

	private static void applyContentKindFromTileType(final CategoryDTO program, final String tileType) {
		if ("VOD".equals(tileType)) {
			program.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_FILM);
		} else if ("SERIE".equals(tileType)) {
			program.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_PROGRAM);
		} else if ("COLLECTION".equals(tileType)) {
			program.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_COLLECTION);
		} else if ("PODCAST".equals(tileType)) {
			program.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_PODCAST);
			program.addParameter(Novo19Conf.PARAMETER_AUDIO_CONTENT, "true");
		}
	}

	static void appendSeasonSubcategories(final CategoryDTO program, final Novo19BffPage detailPage) {
		if (program == null || detailPage == null) {
			return;
		}
		appendEmbeddedSeasonSubcategories(program, detailPage);
		appendSeasonRailSubcategories(program, detailPage);
	}

	private static String seasonExtensionForProgram(final CategoryDTO program) {
		if (program != null && !StringUtils.isEmpty(program.getExtension())) {
			return program.getExtension();
		}
		return Novo19Conf.EXTENSION;
	}

	private static void appendEmbeddedSeasonSubcategories(final CategoryDTO program, final Novo19BffPage detailPage) {
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
					seasonExtensionForProgram(program));
			seasonCategory.setDownloadable(true);
			seasonCategory.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_SEASON);
			seasonCategory.addParameter(Novo19Conf.PARAMETER_SEASON_INDEX, String.valueOf(season.getIndex()));
			program.addSubCategory(seasonCategory);
		}
	}

	private static void appendSeasonRailSubcategories(final CategoryDTO program, final Novo19BffPage detailPage) {
		for (final com.dabi.habitv.provider.novo19.dto.Novo19Rail rail : detailPage.getRails()) {
			if (!Novo19PathRules.isSeasonRail(rail)) {
				continue;
			}
			final String seasonId = program.getId() + "#season-rail-" + rail.getId();
			final CategoryDTO seasonCategory = new CategoryDTO(Novo19Conf.NAME, rail.getTitle(), seasonId,
					seasonExtensionForProgram(program));
			seasonCategory.setDownloadable(true);
			seasonCategory.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_SEASON);
			seasonCategory.addParameter(Novo19Conf.PARAMETER_SEASON_RAIL_SRC, rail.getSrc());
			program.addSubCategory(seasonCategory);
		}
	}

	static Set<EpisodeDTO> mapEpisodes(final CategoryDTO category, final Novo19BffPage page,
			final Collection<Novo19Tile> railTiles) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<EpisodeDTO>();
		final String seasonRailSrc = category.getParameter(Novo19Conf.PARAMETER_SEASON_RAIL_SRC);
		if (!StringUtils.isEmpty(seasonRailSrc)) {
			appendContentRailEpisodes(category, episodes, railTiles);
			return episodes;
		}
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
			appendContentRailEpisodes(category, episodes, railTiles);
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
			// Keep legacy episodeDate for indexes; canonical airDate stays unset.
			episode.setEpisodeDate(publishedAt);
		}
		episode.setMetadata(buildMetadata(category, tile, episodeUrl, publishedAt));
		episodes.add(episode);
	}

	static EpisodeMetadataDTO buildMetadata(final CategoryDTO category, final Novo19Tile tile,
			final String episodeUrl, final Date publishedAt) {
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		final String seriesTitle = seriesTitleFromCategory(category);
		if (seriesTitle != null) {
			metadata.setSeriesTitle(seriesTitle);
		}
		if (!StringUtils.isEmpty(tile.getTitle())) {
			metadata.setEpisodeTitle(tile.getTitle().trim());
		}
		final int[] seasonEpisode = parseSeasonEpisodeCode(tile.getSubtitle());
		if (seasonEpisode != null) {
			metadata.setSeasonNumber(Integer.valueOf(seasonEpisode[0]));
			metadata.setEpisodeNumber(Integer.valueOf(seasonEpisode[1]));
		}
		if (tile.getDurationSeconds() != null) {
			metadata.setDurationSeconds(tile.getDurationSeconds());
		}
		if (!StringUtils.isEmpty(tile.getDescription())) {
			metadata.setDescription(tile.getDescription().trim());
		}
		if (publishedAt != null) {
			metadata.setPublicationDate(publishedAt);
		}
		if (!StringUtils.isEmpty(tile.getId())) {
			metadata.setProviderEpisodeId(tile.getId().trim());
		} else if (!StringUtils.isEmpty(tile.getAssetId())) {
			metadata.setProviderEpisodeId(tile.getAssetId().trim());
		}
		if (!StringUtils.isEmpty(episodeUrl)) {
			metadata.setSourceUrl(episodeUrl);
		}
		metadata.setChannel(Novo19Conf.NAME);
		return metadata;
	}

	/**
	 * Strict {@code S&lt;season&gt;E&lt;episode&gt;} only (e.g. {@code S1E11}). Genre
	 * subtitles must not become season/episode numbers.
	 */
	static int[] parseSeasonEpisodeCode(final String subtitle) {
		if (StringUtils.isEmpty(subtitle)) {
			return null;
		}
		final Matcher matcher = SEASON_EPISODE_PATTERN.matcher(subtitle.trim());
		if (!matcher.matches()) {
			return null;
		}
		final Integer season = tryParsePositiveInt(matcher.group(1));
		final Integer episode = tryParsePositiveInt(matcher.group(2));
		if (season == null || episode == null) {
			return null;
		}
		return new int[] { season.intValue(), episode.intValue() };
	}

	/**
	 * @return positive int, or {@code null} when digits overflow {@code int} or are &lt;= 0
	 */
	private static Integer tryParsePositiveInt(final String raw) {
		try {
			final int value = Integer.parseInt(raw);
			return value > 0 ? Integer.valueOf(value) : null;
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	private static String seriesTitleFromCategory(final CategoryDTO category) {
		if (category == null) {
			return null;
		}
		if (Novo19Conf.CONTENT_KIND_SEASON.equals(category.getParameter(Novo19Conf.PARAMETER_CONTENT_KIND))
				&& category.getFatherCategory() != null
				&& !StringUtils.isEmpty(category.getFatherCategory().getName())) {
			return category.getFatherCategory().getName().trim();
		}
		if (!StringUtils.isEmpty(category.getName())) {
			return category.getName().trim();
		}
		return null;
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
