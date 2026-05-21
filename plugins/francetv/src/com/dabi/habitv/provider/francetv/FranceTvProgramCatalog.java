package com.dabi.habitv.provider.francetv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;

/**
 * Builds channel → rubrique (Info, Documentaires, …) → program hierarchy from
 * france.tv mobile API program listings.
 */
final class FranceTvProgramCatalog {

	/** Preferred rubrique order (url_complete slugs from france.tv API). */
	private static final List<String> RUBRIQUE_ORDER = Arrays.asList("cinema", "info", "documentaires",
			"series-et-fictions", "culture", "sport", "divertissement", "societe");

	private FranceTvProgramCatalog() {
	}

	static Collection<CategoryDTO> groupProgramsByRubrique(final String channelSlug,
			final List<Map<String, Object>> items) {
		final Map<String, CategoryDTO> rubriqueBySlug = new LinkedHashMap<>();
		CategoryDTO uncategorized = null;
		for (final Map<String, Object> item : items) {
			final Object rawPath = item.get("program_path");
			if (rawPath == null) {
				continue;
			}
			final String programPath = String.valueOf(rawPath);
			final String programUrl = FranceTvUrls.programPageUrl(programPath);
			if (StringUtils.isEmpty(programUrl)) {
				continue;
			}
			final String programName = programLabel(item, programPath);
			final CategoryDTO program = new CategoryDTO(FranceTvConf.NAME, programName, programUrl,
					FranceTvConf.EXTENSION);
			program.setDownloadable(true);

			final Map<String, Object> categoryMeta = categoryMeta(item);
			if (categoryMeta == null) {
				if (uncategorized == null) {
					uncategorized = new CategoryDTO(FranceTvConf.NAME, "Autres",
							FranceTvUrls.rubriquePageUrl(channelSlug, null), FranceTvConf.EXTENSION);
					uncategorized.setDownloadable(false);
				}
				uncategorized.addSubCategory(program);
			} else {
				rubriqueForProgram(rubriqueBySlug, channelSlug, categoryMeta).addSubCategory(program);
			}
		}
		return orderedRubriques(rubriqueBySlug, uncategorized);
	}

	private static Collection<CategoryDTO> orderedRubriques(final Map<String, CategoryDTO> rubriqueBySlug,
			final CategoryDTO uncategorized) {
		final List<CategoryDTO> ordered = new ArrayList<>();
		for (final String slug : RUBRIQUE_ORDER) {
			final CategoryDTO rubrique = rubriqueBySlug.remove(slug);
			if (rubrique != null) {
				ordered.add(rubrique);
			}
		}
		ordered.addAll(rubriqueBySlug.values());
		if (uncategorized != null && !uncategorized.getSubCategories().isEmpty()) {
			ordered.add(uncategorized);
		}
		return new LinkedHashSet<>(ordered);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> categoryMeta(final Map<String, Object> item) {
		final Object category = item.get("category");
		if (category instanceof Map) {
			return (Map<String, Object>) category;
		}
		return null;
	}

	private static CategoryDTO rubriqueForProgram(final Map<String, CategoryDTO> rubriqueBySlug,
			final String channelSlug, final Map<String, Object> categoryMeta) {
		final String urlComplete = stringValue(categoryMeta.get("url_complete"));
		final String key = StringUtils.isEmpty(urlComplete) ? "_default" : urlComplete;
		CategoryDTO rubrique = rubriqueBySlug.get(key);
		if (rubrique != null) {
			return rubrique;
		}
		final String label = rubriqueLabel(categoryMeta, urlComplete);
		rubrique = new CategoryDTO(FranceTvConf.NAME, label,
				FranceTvUrls.rubriquePageUrl(channelSlug, urlComplete), FranceTvConf.EXTENSION);
		rubrique.setDownloadable(false);
		rubriqueBySlug.put(key, rubrique);
		return rubrique;
	}

	private static String rubriqueLabel(final Map<String, Object> categoryMeta, final String urlComplete) {
		final String label = stringValue(categoryMeta.get("label"));
		if (StringUtils.isNotEmpty(label)) {
			return label;
		}
		if (StringUtils.isNotEmpty(urlComplete)) {
			return urlComplete.replace('-', ' ');
		}
		return "Autres";
	}

	private static String programLabel(final Map<String, Object> item, final String programPath) {
		final String label = stringValue(item.get("label"));
		if (StringUtils.isNotEmpty(label)) {
			return label;
		}
		final int lastUnderscore = programPath.lastIndexOf('_');
		final String segment = lastUnderscore >= 0 ? programPath.substring(lastUnderscore + 1) : programPath;
		return segment.replace('-', ' ');
	}

	private static String stringValue(final Object value) {
		return value == null ? "" : String.valueOf(value).trim();
	}
}
