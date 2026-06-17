package com.dabi.habitv.provider.novo19;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.api.PluginProviderDownloaderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.dabi.habitv.framework.plugin.utils.DownloadUtils;
import com.dabi.habitv.provider.novo19.dto.Novo19BffPage;
import com.dabi.habitv.provider.novo19.dto.Novo19Rail;
import com.dabi.habitv.provider.novo19.dto.Novo19Tile;

public class Novo19PluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	private final Novo19CatalogClient catalogClient;

	private final Novo19PlaybackClient playbackClient;

	public Novo19PluginManager() {
		this.catalogClient = new Novo19CatalogClient(this);
		this.playbackClient = new Novo19PlaybackClient(Novo19HttpClient.pluginTransport(getHttpProxy()));
	}

	Novo19PluginManager(final Novo19CatalogClient catalogClient) {
		this(catalogClient, new Novo19PlaybackClient(Novo19HttpClient.pluginTransport(null)));
	}

	Novo19PluginManager(final Novo19CatalogClient catalogClient, final Novo19PlaybackClient playbackClient) {
		this.catalogClient = catalogClient;
		this.playbackClient = playbackClient;
	}

	@Override
	public String getName() {
		return Novo19Conf.NAME;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("catalogue");
		diagnostics.setSourcePath("/categories");
		final Set<CategoryDTO> categories = new LinkedHashSet<>();
		try {
			final Novo19BffPage categoriesPage = catalogClient.fetchPageByPublicPath("categories");
			final CategoryDTO root = Novo19CatalogMapper.buildRootCategory();
			for (final Novo19Rail rail : categoriesPage.getRails()) {
				if (Novo19PathRules.isRecommendationRail(rail)) {
					continue;
				}
				if (Novo19PathRules.isCatalogueCarouselRail(rail)) {
					processCatalogueCarousel(root, rail, diagnostics);
				} else if (Novo19PathRules.isInfoEditorialRail(rail)) {
					processEditorialProgramsRail(root, Novo19Conf.EDITORIAL_INFO, rail, diagnostics);
				} else if (Novo19PathRules.isTalkEditorialRail(rail)) {
					processEditorialProgramsRail(root, Novo19Conf.EDITORIAL_TALK, rail, diagnostics);
				} else if (Novo19PathRules.isDocumentariesSectionRail(rail)) {
					processThematicDocumentariesSection(root, rail, diagnostics);
				} else if (Novo19PathRules.isGenericCatalogueSectionTitle(rail.getTitle())) {
					addProgramsFromRail(root, rail, diagnostics);
				} else if (!StringUtils.isEmpty(rail.getTitle())) {
					final CategoryDTO section = buildNamedSectionFromRail(rail, diagnostics);
					if (section != null && !section.getSubCategories().isEmpty()) {
						root.addSubCategory(section);
					}
				}
			}
			if (!root.getSubCategories().isEmpty()) {
				categories.add(root);
			}
			diagnostics.setCreatedItems(countCategories(root));
			logDiagnostics(diagnostics);
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			logDiagnostics(diagnostics);
			getLog().warn("NOVO19 category discovery failed safely: " + e.getMessage());
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("parse-error:" + e.getClass().getSimpleName());
			logDiagnostics(diagnostics);
			getLog().warn("NOVO19 category discovery failed safely: " + e.getMessage());
		}
		return categories;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		if (category == null || StringUtils.isEmpty(category.getId())) {
			return new LinkedHashSet<>();
		}
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("episodes");
		diagnostics.setSourcePath(Novo19UrlBuilder.publicPathFromCategoryId(category.getId()));
		diagnostics.setAssetId(category.getParameter(Novo19Conf.PARAMETER_ASSET_ID));
		try {
			final Novo19BffPage page = catalogClient.fetchPageByPublicUrl(category.getId());
			ensureContentKind(category, page);
			final String seasonRailSrc = category.getParameter(Novo19Conf.PARAMETER_SEASON_RAIL_SRC);
			if (!StringUtils.isEmpty(seasonRailSrc)) {
				final Novo19Diagnostics railDiagnostics = new Novo19Diagnostics("season-rail");
				railDiagnostics.setSourcePath(seasonRailSrc);
				final List<Novo19Tile> seasonTiles = Novo19Pagination.loadTiles(seasonRailSrc,
						catalogClient::fetchTilesJson, railDiagnostics);
				logDiagnostics(railDiagnostics);
				final Set<EpisodeDTO> episodes = Novo19CatalogMapper.mapEpisodes(category, page, seasonTiles);
				diagnostics.setCreatedItems(episodes.size());
				logDiagnostics(diagnostics);
				return episodes;
			}
			final List<Novo19Tile> railTiles = shouldLoadEpisodeRails(category, page)
					? loadEpisodeRailTiles(page, diagnostics) : java.util.Collections.<Novo19Tile>emptyList();
			final Set<EpisodeDTO> episodes = Novo19CatalogMapper.mapEpisodes(category, page, railTiles);
			diagnostics.setCreatedItems(episodes.size());
			logDiagnostics(diagnostics);
			return episodes;
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			logDiagnostics(diagnostics);
			getLog().warn("NOVO19 episode listing failed safely for " + category.getId() + ": " + e.getMessage());
			return new LinkedHashSet<>();
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("parse-error:" + e.getClass().getSimpleName());
			logDiagnostics(diagnostics);
			getLog().warn("NOVO19 episode listing failed safely for " + category.getId() + ": " + e.getMessage());
			return new LinkedHashSet<>();
		}
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		final Novo19Diagnostics diagnostics = new Novo19Diagnostics("download");
		diagnostics.setSourcePath(downloadParam == null ? null : downloadParam.getDownloadInput());
		try {
			final String assetId = Novo19AssetResolver.resolveAssetId(downloadParam, catalogClient);
			if (StringUtils.isEmpty(assetId) || Novo19PathRules.isLiveReplayAsset(assetId)) {
				diagnostics.setRootCauseSummary("unsupported-asset");
				logDiagnostics(diagnostics);
				throw new DownloadFailedException(Novo19Conf.DOWNLOAD_UNAVAILABLE_MESSAGE);
			}
			diagnostics.setAssetId(assetId);
			final String streamUrl = playbackClient.resolveReplayStreamUrl(assetId, diagnostics);
			if (StringUtils.isEmpty(streamUrl)) {
				logDiagnostics(diagnostics);
				throw new DownloadFailedException(Novo19Conf.DOWNLOAD_UNAVAILABLE_MESSAGE);
			}
			logDiagnostics(diagnostics);
			final DownloadParamDTO delegated = Novo19DownloadMapper.buildDelegatedDownload(downloadParam, streamUrl);
			return DownloadUtils.download(delegated, downloaders, FrameworkConf.YOUTUBE);
		} catch (final Novo19HttpException e) {
			diagnostics.setHttpStatus(e.getStatus());
			diagnostics.setRootCauseSummary(Novo19DownloadMapper.summarizeFailure(
					Novo19HttpStatus.summarizeFailure(e.getStatus())));
			logDiagnostics(diagnostics);
			getLog().warn("NOVO19 download failed safely: " + e.getMessage());
			throw new DownloadFailedException(Novo19Conf.DOWNLOAD_UNAVAILABLE_MESSAGE);
		} catch (final IOException e) {
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			logDiagnostics(diagnostics);
			getLog().warn("NOVO19 download failed safely: " + e.getMessage());
			throw new DownloadFailedException(Novo19Conf.DOWNLOAD_UNAVAILABLE_MESSAGE);
		} catch (final RuntimeException e) {
			diagnostics.setRootCauseSummary("parse-error:" + e.getClass().getSimpleName());
			logDiagnostics(diagnostics);
			getLog().warn("NOVO19 download failed safely: " + e.getMessage());
			throw new DownloadFailedException(Novo19Conf.DOWNLOAD_UNAVAILABLE_MESSAGE);
		}
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		if (downloadInput != null && downloadInput.contains("novo19.ouest-france.fr")) {
			return DownloadableState.SPECIFIC;
		}
		return DownloadableState.IMPOSSIBLE;
	}

	private void processCatalogueCarousel(final CategoryDTO root, final Novo19Rail rail,
			final Novo19Diagnostics diagnostics) {
		final List<Novo19Tile> tiles = loadRailTiles(rail, diagnostics);
		for (final Novo19Tile tile : tiles) {
			final String editorialBucket = Novo19PathRules.editorialBucketForArtworkTile(tile);
			if (editorialBucket == null) {
				continue;
			}
			try {
				final CategoryDTO program = resolveProgramCategory(tile);
				if (program != null) {
					addProgramIfAbsent(getOrCreateEditorial(root, editorialBucket), program);
				}
			} catch (final IOException e) {
				getLog().debug("NOVO19 shortcut program skipped for " + tile.getHref() + ": " + e.getMessage());
			}
		}
	}

	private void processThematicDocumentariesSection(final CategoryDTO root, final Novo19Rail rail,
			final Novo19Diagnostics diagnostics) {
		final CategoryDTO section = Novo19CatalogMapper.buildSectionCategory(Novo19Conf.SECTION_DOCUMENTARIES,
				"/" + Novo19Conf.DOCUMENTARIES_PUBLIC_PATH);
		final Novo19TaxonomyMapper.ThematicSectionBuilder builder = new Novo19TaxonomyMapper.ThematicSectionBuilder(
				section);
		for (final Novo19Tile tile : loadRailTiles(rail, diagnostics)) {
			registerThematicProgram(builder, tile, null);
		}
		try {
			final Novo19BffPage documentariesPage = catalogClient
					.fetchPageByPublicPath(Novo19Conf.DOCUMENTARIES_PUBLIC_PATH);
			for (final Novo19Rail themeRail : documentariesPage.getRails()) {
				if (!Novo19PathRules.isDocumentariesThemeRail(themeRail)) {
					continue;
				}
				final String themeHint = Novo19TaxonomyMapper.themeHintFromDocumentariesRail(themeRail);
				for (final Novo19Tile tile : loadRailTiles(themeRail, diagnostics)) {
					registerThematicProgram(builder, tile, themeHint);
				}
			}
		} catch (final IOException e) {
			getLog().debug("NOVO19 documentaries section enrichment skipped: " + e.getMessage());
		}
		builder.attachToRoot(root);
	}

	private void registerThematicProgram(final Novo19TaxonomyMapper.ThematicSectionBuilder builder,
			final Novo19Tile tile, final String railThemeHint) {
		if (!Novo19PathRules.isProgramDiscoverableTile(tile)) {
			return;
		}
		try {
			final CategoryDTO program = resolveProgramCategory(tile);
			if (program == null) {
				return;
			}
			final Novo19BffPage detailPage = catalogClient.fetchPageByPublicUrl(program.getId());
			final Set<String> themes = Novo19TaxonomyMapper.resolveProgramThemes(tile, detailPage, railThemeHint);
			builder.registerProgram(program, themes);
		} catch (final IOException e) {
			getLog().debug("NOVO19 thematic program skipped for " + tile.getHref() + ": " + e.getMessage());
		}
	}

	private void processEditorialProgramsRail(final CategoryDTO root, final String editorialName,
			final Novo19Rail rail, final Novo19Diagnostics diagnostics) {
		final CategoryDTO editorialSection = getOrCreateEditorial(root, editorialName);
		final List<Novo19Tile> tiles = loadRailTiles(rail, diagnostics);
		for (final Novo19Tile tile : tiles) {
			if (!Novo19PathRules.isProgramDiscoverableTile(tile)) {
				continue;
			}
			try {
				final CategoryDTO program = resolveProgramCategory(tile);
				if (program != null) {
					addProgramIfAbsent(editorialSection, program);
				}
			} catch (final IOException e) {
				getLog().debug("NOVO19 editorial program skipped for " + tile.getHref() + ": " + e.getMessage());
			}
		}
	}

	private void addProgramsFromRail(final CategoryDTO parent, final Novo19Rail rail,
			final Novo19Diagnostics diagnostics) {
		final List<Novo19Tile> tiles = loadRailTiles(rail, diagnostics);
		for (final Novo19Tile tile : tiles) {
			addProgramTile(parent, tile);
		}
	}

	private CategoryDTO buildNamedSectionFromRail(final Novo19Rail rail, final Novo19Diagnostics diagnostics) {
		if (rail == null || StringUtils.isEmpty(rail.getSrc()) || StringUtils.isEmpty(rail.getTitle())) {
			return null;
		}
		final CategoryDTO section = Novo19CatalogMapper.buildSectionCategory(rail.getTitle(), "/categories");
		final List<Novo19Tile> tiles = loadRailTiles(rail, diagnostics);
		for (final Novo19Tile tile : tiles) {
			addProgramTile(section, tile);
		}
		return section;
	}

	private List<Novo19Tile> loadRailTiles(final Novo19Rail rail, final Novo19Diagnostics parentDiagnostics) {
		if (rail == null || StringUtils.isEmpty(rail.getSrc())) {
			return java.util.Collections.emptyList();
		}
		final Novo19Diagnostics tileDiagnostics = new Novo19Diagnostics("rail-tiles");
		tileDiagnostics.setSourcePath(rail.getSrc());
		final List<Novo19Tile> tiles = Novo19Pagination.loadTiles(rail.getSrc(), catalogClient::fetchTilesJson,
				tileDiagnostics);
		logDiagnostics(tileDiagnostics);
		return tiles;
	}

	private void addProgramTile(final CategoryDTO parent, final Novo19Tile tile) {
		if (!Novo19PathRules.isProgramDiscoverableTile(tile)) {
			return;
		}
		try {
			final CategoryDTO program = resolveProgramCategory(tile);
			if (program != null) {
				parent.addSubCategory(program);
			}
		} catch (final IOException e) {
			getLog().debug("NOVO19 program skipped for " + tile.getHref() + ": " + e.getMessage());
		}
	}

	private CategoryDTO resolveProgramCategory(final Novo19Tile tile) throws IOException {
		if (tile == null || StringUtils.isEmpty(tile.getHref())) {
			return null;
		}
		if (Novo19PathRules.isProgramDetailTile(tile)) {
			final Novo19BffPage detailPage = catalogClient.fetchPageByPublicUrl(Novo19UrlBuilder.publicPageUrl(tile.getHref()));
			final String title = StringUtils.isEmpty(detailPage.getTitle()) ? Novo19CatalogMapper.programLabel(tile)
					: detailPage.getTitle();
			final CategoryDTO program = Novo19CatalogMapper.buildProgramCategory(tile, title);
			Novo19TaxonomyMapper.applyDetailContentKind(program, detailPage);
			if (!detailPage.getSeasons().isEmpty()
					&& Novo19Conf.CONTENT_KIND_COLLECTION.equals(program.getParameter(Novo19Conf.PARAMETER_CONTENT_KIND))) {
				program.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_PROGRAM);
			}
			program.addParameter(Novo19Conf.PARAMETER_CANONICAL_PROGRAM_ID,
					Novo19TaxonomyMapper.canonicalProgramId(tile, detailPage));
			enrichProgramWithSeasons(program, detailPage);
			return program;
		}
		final CategoryDTO program = Novo19CatalogMapper.buildProgramCategory(tile);
		program.addParameter(Novo19Conf.PARAMETER_CANONICAL_PROGRAM_ID,
				Novo19TaxonomyMapper.canonicalProgramId(tile, null));
		enrichProgramWithSeasons(program);
		return program;
	}

	private void enrichProgramWithSeasons(final CategoryDTO program) {
		enrichProgramWithSeasons(program, null);
	}

	private void enrichProgramWithSeasons(final CategoryDTO program, final Novo19BffPage prefetchedPage) {
		if (program == null
				|| !Novo19Conf.CONTENT_KIND_PROGRAM.equals(program.getParameter(Novo19Conf.PARAMETER_CONTENT_KIND))) {
			return;
		}
		try {
			final Novo19BffPage detailPage = prefetchedPage != null ? prefetchedPage
					: catalogClient.fetchPageByPublicUrl(program.getId());
			Novo19CatalogMapper.appendSeasonSubcategories(program, detailPage);
		} catch (final IOException e) {
			getLog().debug("NOVO19 season enrichment skipped for " + program.getId() + ": " + e.getMessage());
		} catch (final RuntimeException e) {
			getLog().debug("NOVO19 season enrichment skipped for " + program.getId() + ": " + e.getMessage());
		}
	}

	private static CategoryDTO getOrCreateEditorial(final CategoryDTO root, final String editorialName) {
		for (final CategoryDTO child : root.getSubCategories()) {
			if (editorialName.equals(child.getName())) {
				return child;
			}
		}
		final CategoryDTO section = Novo19CatalogMapper.buildEditorialSectionCategory(editorialName);
		root.addSubCategory(section);
		return section;
	}

	private static void addProgramIfAbsent(final CategoryDTO parent, final CategoryDTO program) {
		if (program == null) {
			return;
		}
		for (final CategoryDTO existing : parent.getSubCategories()) {
			if (program.getId().equals(existing.getId())) {
				return;
			}
		}
		parent.addSubCategory(program);
	}

	private static void ensureContentKind(final CategoryDTO category, final Novo19BffPage page) {
		if (!StringUtils.isEmpty(category.getParameter(Novo19Conf.PARAMETER_CONTENT_KIND))) {
			return;
		}
		if (Novo19CatalogMapper.isPodcastDetailPage(page)) {
			category.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_PODCAST);
			category.addParameter(Novo19Conf.PARAMETER_AUDIO_CONTENT, "true");
		}
	}

	private static boolean shouldLoadEpisodeRails(final CategoryDTO category, final Novo19BffPage page) {
		final String contentKind = category.getParameter(Novo19Conf.PARAMETER_CONTENT_KIND);
		if (Novo19Conf.CONTENT_KIND_FILM.equals(contentKind)) {
			return false;
		}
		return Novo19Conf.CONTENT_KIND_COLLECTION.equals(contentKind)
				|| Novo19Conf.CONTENT_KIND_PODCAST.equals(contentKind)
				|| Novo19Conf.CONTENT_KIND_PROGRAM.equals(contentKind);
	}

	private List<Novo19Tile> loadEpisodeRailTiles(final Novo19BffPage page, final Novo19Diagnostics diagnostics) {
		final Set<Novo19Tile> tiles = new LinkedHashSet<>();
		for (final Novo19Rail rail : page.getRails()) {
			if (Novo19PathRules.isRecommendationRail(rail) || Novo19PathRules.isSeasonRail(rail)
					|| StringUtils.isEmpty(rail.getSrc())) {
				continue;
			}
			final Novo19Diagnostics railDiagnostics = new Novo19Diagnostics("episode-rail");
			railDiagnostics.setSourcePath(rail.getSrc());
			tiles.addAll(Novo19Pagination.loadTiles(rail.getSrc(), catalogClient::fetchTilesJson, railDiagnostics));
			logDiagnostics(railDiagnostics);
		}
		return new java.util.ArrayList<>(tiles);
	}

	private static int countCategories(final CategoryDTO root) {
		int count = 1;
		for (final CategoryDTO child : root.getSubCategories()) {
			count += countCategories(child);
		}
		return count;
	}

	private void logDiagnostics(final Novo19Diagnostics diagnostics) {
		if (diagnostics == null) {
			return;
		}
		final String line = diagnostics.formatLogLine();
		if ("ok".equals(diagnostics.getRootCauseSummary())) {
			getLog().info(line);
		} else {
			getLog().warn(line);
		}
	}

}
