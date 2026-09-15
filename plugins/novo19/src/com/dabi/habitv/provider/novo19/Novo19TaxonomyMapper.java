package com.dabi.habitv.provider.novo19;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.provider.novo19.dto.Novo19BffPage;
import com.dabi.habitv.provider.novo19.dto.Novo19Rail;
import com.dabi.habitv.provider.novo19.dto.Novo19Tile;

final class Novo19TaxonomyMapper {

	private static final String[] BOILERPLATE_THEMES = new String[] { "documentaire", "docs et magazines",
			"docs & magazines", "novo19", "catégories", "categories", "catalogue" };

	private Novo19TaxonomyMapper() {
	}

	static Set<String> resolveProgramThemes(final Novo19Tile discoveryTile, final Novo19BffPage detailPage,
			final String railThemeHint) {
		final Set<String> themes = new LinkedHashSet<String>();
		if (!StringUtils.isEmpty(railThemeHint)) {
			addTheme(themes, railThemeHint);
			if (discoveryTile != null) {
				addTheme(themes, discoveryTile.getSubtitle());
			}
		} else {
			if (detailPage != null) {
				for (final String category : detailPage.getContentCategories()) {
					addTheme(themes, category);
				}
			}
			if (discoveryTile != null) {
				addTheme(themes, discoveryTile.getSubtitle());
			}
		}
		if (themes.isEmpty()) {
			themes.add(Novo19Conf.THEME_UNCLASSIFIED);
		}
		return themes;
	}

	static String normalizeTheme(final String raw) {
		if (StringUtils.isEmpty(raw)) {
			return null;
		}
		final String trimmed = raw.trim();
		if (isBoilerplateTheme(trimmed) || isGenreSubtitle(trimmed) || isSectionTitleExclusion(trimmed)) {
			return null;
		}
		return trimmed;
	}

	static String canonicalProgramId(final Novo19Tile tile, final Novo19BffPage detailPage) {
		if (detailPage != null && detailPage.getContent() != null
				&& !StringUtils.isEmpty(detailPage.getContent().getAssetId())) {
			return detailPage.getContent().getAssetId();
		}
		if (detailPage != null && !StringUtils.isEmpty(detailPage.getId())) {
			return detailPage.getId();
		}
		if (tile != null && !StringUtils.isEmpty(tile.getAssetId())) {
			return tile.getAssetId();
		}
		if (tile != null && !StringUtils.isEmpty(tile.getHref())) {
			return tile.getHref();
		}
		return null;
	}

	static CategoryDTO buildThemeCategory(final CategoryDTO documentariesSection, final String themeName) {
		final String themeId = documentariesSection.getId() + "#theme-" + slugify(themeName);
		final CategoryDTO theme = new CategoryDTO(Novo19Conf.NAME, themeName, themeId, Novo19Conf.EXTENSION);
		theme.setDownloadable(false);
		return theme;
	}

	static String themeHintFromDocumentariesRail(final Novo19Rail rail) {
		if (rail == null || !Novo19PathRules.isDocumentariesThemeRail(rail)) {
			return null;
		}
		return rail.getTitle();
	}

	static void applyDetailContentKind(final CategoryDTO program, final Novo19BffPage detailPage) {
		if (program == null || detailPage == null || detailPage.getContent() == null) {
			return;
		}
		final String contentType = detailPage.getContent().getType();
		if ("SERIE".equals(contentType)) {
			program.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_PROGRAM);
		} else if ("VOD".equals(contentType)) {
			program.addParameter(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_FILM);
		}
	}

	static boolean isBoilerplateTheme(final String value) {
		if (StringUtils.isEmpty(value)) {
			return true;
		}
		final String normalized = value.trim().toLowerCase(Locale.FRENCH);
		for (final String boilerplate : BOILERPLATE_THEMES) {
			if (normalized.equals(boilerplate)) {
				return true;
			}
		}
		return false;
	}

	static boolean isGenreSubtitle(final String value) {
		return !StringUtils.isEmpty(value) && value.contains(" / ");
	}

	private static void addTheme(final Set<String> themes, final String raw) {
		final String normalized = normalizeTheme(raw);
		if (!StringUtils.isEmpty(normalized)) {
			themes.add(normalized);
		}
	}

	private static boolean isSectionTitleExclusion(final String value) {
		if (StringUtils.isEmpty(value)) {
			return false;
		}
		final String normalized = value.trim().toLowerCase(Locale.FRENCH);
		if (Novo19PathRules.isCuratedSelectionRailTitle(value)) {
			return true;
		}
		if (normalized.equals(Novo19Conf.EDITORIAL_INFO.toLowerCase(Locale.FRENCH))
				|| normalized.equals(Novo19Conf.EDITORIAL_TALK.toLowerCase(Locale.FRENCH))) {
			return true;
		}
		return isConfiguredCatalogueSectionTitle(normalized);
	}

	private static boolean isConfiguredCatalogueSectionTitle(final String normalized) {
		return normalized.equals(Novo19Conf.SECTION_DOCUMENTARIES.toLowerCase(Locale.FRENCH))
				|| normalized.equals(Novo19Conf.SECTION_FILMS.toLowerCase(Locale.FRENCH))
				|| normalized.equals(Novo19Conf.SECTION_SERIES.toLowerCase(Locale.FRENCH))
				|| normalized.equals(Novo19Conf.SECTION_PODCASTS.toLowerCase(Locale.FRENCH))
				|| normalized.equals(Novo19Conf.SECTION_DIVERTISSEMENTS.toLowerCase(Locale.FRENCH));
	}

	private static String slugify(final String value) {
		return value.trim().toLowerCase(Locale.FRENCH).replaceAll("[^a-z0-9]+", "-");
	}

	static final class ProgramRegistry {

		private final Set<String> registeredIds = new LinkedHashSet<String>();

		boolean registerIfAbsent(final String canonicalProgramId) {
			if (StringUtils.isEmpty(canonicalProgramId)) {
				return true;
			}
			return registeredIds.add(canonicalProgramId);
		}
	}

	static final class ThematicSectionBuilder {

		private final CategoryDTO section;

		private final Map<String, CategoryDTO> programsByCanonicalId = new java.util.LinkedHashMap<String, CategoryDTO>();

		private final Map<String, Set<String>> themesByCanonicalId = new java.util.LinkedHashMap<String, Set<String>>();

		private final Map<String, CategoryDTO> themeBuckets = new java.util.LinkedHashMap<String, CategoryDTO>();

		private final Map<String, ProgramRegistry> registriesByTheme = new java.util.LinkedHashMap<String, ProgramRegistry>();

		ThematicSectionBuilder(final CategoryDTO section) {
			this.section = section;
		}

		void registerProgram(final CategoryDTO program, final Collection<String> themes) {
			if (program == null || themes == null || themes.isEmpty()) {
				return;
			}
			final String canonicalId = program.getParameter(Novo19Conf.PARAMETER_CANONICAL_PROGRAM_ID);
			if (StringUtils.isEmpty(canonicalId)) {
				return;
			}
			programsByCanonicalId.put(canonicalId, program);
			Set<String> accumulated = themesByCanonicalId.get(canonicalId);
			if (accumulated == null) {
				accumulated = new LinkedHashSet<String>();
				themesByCanonicalId.put(canonicalId, accumulated);
			}
			accumulated.addAll(themes);
		}

		void attachToRoot(final CategoryDTO root) {
			for (final Map.Entry<String, CategoryDTO> entry : programsByCanonicalId.entrySet()) {
				final String canonicalId = entry.getKey();
				final CategoryDTO program = entry.getValue();
				final Set<String> themes = themesByCanonicalId.get(canonicalId);
				if (themes == null) {
					continue;
				}
				for (final String theme : themes) {
					final String themeName = StringUtils.isEmpty(theme) ? Novo19Conf.THEME_UNCLASSIFIED : theme;
					ProgramRegistry registry = registriesByTheme.get(themeName);
					if (registry != null && !registry.registerIfAbsent(canonicalId)) {
						continue;
					}
					if (registry == null) {
						registry = new ProgramRegistry();
						registry.registerIfAbsent(canonicalId);
						registriesByTheme.put(themeName, registry);
					}
					getOrCreateThemeBucket(themeName).addSubCategory(Novo19CatalogMapper.cloneProgramCategory(program));
				}
			}
			for (final CategoryDTO themeBucket : themeBuckets.values()) {
				if (!themeBucket.getSubCategories().isEmpty()) {
					section.addSubCategory(themeBucket);
				}
			}
			if (!section.getSubCategories().isEmpty()) {
				root.addSubCategory(section);
			}
		}

		private CategoryDTO getOrCreateThemeBucket(final String themeName) {
			CategoryDTO bucket = themeBuckets.get(themeName);
			if (bucket == null) {
				bucket = buildThemeCategory(section, themeName);
				themeBuckets.put(themeName, bucket);
			}
			return bucket;
		}
	}

}
