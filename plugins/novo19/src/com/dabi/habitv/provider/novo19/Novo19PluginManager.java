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
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.dabi.habitv.provider.novo19.dto.Novo19BffPage;
import com.dabi.habitv.provider.novo19.dto.Novo19Rail;
import com.dabi.habitv.provider.novo19.dto.Novo19Tile;

public class Novo19PluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	private final Novo19CatalogClient catalogClient;

	public Novo19PluginManager() {
		this.catalogClient = new Novo19CatalogClient(this);
	}

	Novo19PluginManager(final Novo19CatalogClient catalogClient) {
		this.catalogClient = catalogClient;
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
				if (Novo19PathRules.isGenericCatalogueSectionTitle(rail.getTitle())) {
					addProgramsFromRail(root, rail, diagnostics);
				} else {
					final CategoryDTO section = buildSectionFromRail(rail, diagnostics);
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
		throw new DownloadFailedException(Novo19Conf.DOWNLOAD_UNAVAILABLE_MESSAGE);
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		if (downloadInput != null && downloadInput.contains("novo19.ouest-france.fr")) {
			return DownloadableState.SPECIFIC;
		}
		return DownloadableState.IMPOSSIBLE;
	}

	private void addProgramsFromRail(final CategoryDTO parent, final Novo19Rail rail,
			final Novo19Diagnostics diagnostics) {
		if (rail == null || StringUtils.isEmpty(rail.getSrc())) {
			return;
		}
		final Novo19Diagnostics tileDiagnostics = new Novo19Diagnostics("rail-tiles");
		tileDiagnostics.setSourcePath(rail.getSrc());
		final List<Novo19Tile> tiles = Novo19Pagination.loadTiles(rail.getSrc(), catalogClient::fetchTilesJson,
				tileDiagnostics);
		for (final Novo19Tile tile : tiles) {
			addProgramTile(parent, tile);
		}
		logDiagnostics(tileDiagnostics);
	}

	private CategoryDTO buildSectionFromRail(final Novo19Rail rail, final Novo19Diagnostics diagnostics) {
		if (rail == null || StringUtils.isEmpty(rail.getSrc())) {
			return null;
		}
		final String sectionTitle = StringUtils.isEmpty(rail.getTitle()) ? "Catalogue" : rail.getTitle();
		final CategoryDTO section = Novo19CatalogMapper.buildSectionCategory(sectionTitle, "/categories");
		final Novo19Diagnostics tileDiagnostics = new Novo19Diagnostics("rail-tiles");
		tileDiagnostics.setSourcePath(rail.getSrc());
		final List<Novo19Tile> tiles = Novo19Pagination.loadTiles(rail.getSrc(), catalogClient::fetchTilesJson,
				tileDiagnostics);
		for (final Novo19Tile tile : tiles) {
			addProgramTile(section, tile);
		}
		logDiagnostics(tileDiagnostics);
		return section;
	}

	private void addProgramTile(final CategoryDTO parent, final Novo19Tile tile) {
		if (Novo19PathRules.isProgramTileType(tile.getType())) {
			final CategoryDTO program = Novo19CatalogMapper.buildProgramCategory(tile);
			enrichProgramWithSeasons(program);
			parent.addSubCategory(program);
		} else if (Novo19PathRules.isCollectionProgramTile(tile)) {
			parent.addSubCategory(Novo19CatalogMapper.buildProgramCategory(tile));
		}
	}

	private void enrichProgramWithSeasons(final CategoryDTO program) {
		if (program == null || !Novo19Conf.CONTENT_KIND_PROGRAM.equals(program.getParameter(Novo19Conf.PARAMETER_CONTENT_KIND))) {
			return;
		}
		try {
			final Novo19BffPage detailPage = catalogClient.fetchPageByPublicUrl(program.getId());
			Novo19CatalogMapper.appendSeasonSubcategories(program, detailPage);
		} catch (final IOException e) {
			getLog().debug("NOVO19 season enrichment skipped for " + program.getId() + ": " + e.getMessage());
		} catch (final RuntimeException e) {
			getLog().debug("NOVO19 season enrichment skipped for " + program.getId() + ": " + e.getMessage());
		}
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
		if (Novo19Conf.CONTENT_KIND_PROGRAM.equals(contentKind) && !page.getSeasons().isEmpty()) {
			return false;
		}
		return Novo19Conf.CONTENT_KIND_COLLECTION.equals(contentKind)
				|| Novo19Conf.CONTENT_KIND_PODCAST.equals(contentKind)
				|| (Novo19Conf.CONTENT_KIND_PROGRAM.equals(contentKind) && page.getSeasons().isEmpty());
	}

	private List<Novo19Tile> loadEpisodeRailTiles(final Novo19BffPage page, final Novo19Diagnostics diagnostics) {
		final Set<Novo19Tile> tiles = new LinkedHashSet<>();
		for (final Novo19Rail rail : page.getRails()) {
			if (Novo19PathRules.isRecommendationRail(rail) || StringUtils.isEmpty(rail.getSrc())) {
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
