package com.dabi.habitv.provider.tf1plus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;

/**
 * Builds Habitv category trees from normalized TF1+ catalogue entries.
 */
final class Tf1PlusTreeBuilder {

	Set<CategoryDTO> buildTree(final List<Tf1PlusCatalogueEntry> entries) {
		final Map<String, List<Tf1PlusCatalogueEntry>> byHub = groupByHub(entries);
		final Set<CategoryDTO> roots = new LinkedHashSet<CategoryDTO>();
		for (final Tf1PlusHubDescriptor hub : Tf1PlusHubRegistry.enabledHubs()) {
			if (!hub.isEnabled()) {
				continue;
			}
			final CategoryDTO hubCategory = new CategoryDTO(Tf1PlusConf.NAME, hub.getDisplayLabel(),
					hub.getReplayUrl(), Tf1PlusConf.EXTENSION);
			hubCategory.setDownloadable(false);
			final List<Tf1PlusCatalogueEntry> hubEntries = byHub.get(hub.getHubId());
			if (hubEntries != null && !hubEntries.isEmpty()) {
				if (hub.getTreeStyle() == Tf1PlusHubDescriptor.TreeStyle.GROUPED_BY_RUBRIC) {
					hubCategory.addSubCategories(buildGroupedTf1Categories(hub, hubEntries));
				} else {
					hubCategory.addSubCategories(buildFlatProgrammeCategories(hubEntries));
				}
			}
			roots.add(hubCategory);
		}
		return roots;
	}

	private Map<String, List<Tf1PlusCatalogueEntry>> groupByHub(final List<Tf1PlusCatalogueEntry> entries) {
		final Map<String, List<Tf1PlusCatalogueEntry>> grouped = new LinkedHashMap<String, List<Tf1PlusCatalogueEntry>>();
		if (entries == null) {
			return grouped;
		}
		for (final Tf1PlusCatalogueEntry entry : entries) {
			List<Tf1PlusCatalogueEntry> hubEntries = grouped.get(entry.getHubId());
			if (hubEntries == null) {
				hubEntries = new ArrayList<Tf1PlusCatalogueEntry>();
				grouped.put(entry.getHubId(), hubEntries);
			}
			hubEntries.add(entry);
		}
		return grouped;
	}

	private Set<CategoryDTO> buildFlatProgrammeCategories(final List<Tf1PlusCatalogueEntry> entries) {
		final Set<CategoryDTO> programmes = new LinkedHashSet<CategoryDTO>();
		final Set<String> seenUrls = new HashSet<String>();
		for (final Tf1PlusCatalogueEntry entry : entries) {
			final CategoryDTO programme = toProgrammeCategory(entry);
			if (programme != null && seenUrls.add(programme.getId())) {
				programmes.add(programme);
			}
		}
		return programmes;
	}

	private Set<CategoryDTO> buildGroupedTf1Categories(final Tf1PlusHubDescriptor hub,
			final List<Tf1PlusCatalogueEntry> entries) {
		final Map<String, Set<CategoryDTO>> rubricProgrammes = new LinkedHashMap<String, Set<CategoryDTO>>();
		final Set<String> assignedSlugs = new HashSet<String>();
		for (final Tf1PlusEditorialRubric rubric : Tf1PlusEditorialRubricRegistry.knownRubrics()) {
			rubricProgrammes.put(rubric.getLabel(), new LinkedHashSet<CategoryDTO>());
		}
		rubricProgrammes.put(Tf1PlusEditorialRubricRegistry.FALLBACK_LABEL, new LinkedHashSet<CategoryDTO>());

		for (final Tf1PlusCatalogueEntry entry : entries) {
			final CategoryDTO programme = toProgrammeCategory(entry);
			if (programme == null || !assignedSlugs.add(entry.getSlug())) {
				continue;
			}
			final Tf1PlusEditorialRubric rubric = resolveRubric(entry);
			final String rubricLabel = rubric == null ? Tf1PlusEditorialRubricRegistry.FALLBACK_LABEL
					: rubric.getLabel();
			Set<CategoryDTO> bucket = rubricProgrammes.get(rubricLabel);
			if (bucket == null) {
				bucket = new LinkedHashSet<CategoryDTO>();
				rubricProgrammes.put(rubricLabel, bucket);
			}
			bucket.add(programme);
		}

		final Set<CategoryDTO> rubricCategories = new LinkedHashSet<CategoryDTO>();
		for (final Map.Entry<String, Set<CategoryDTO>> rubricEntry : rubricProgrammes.entrySet()) {
			if (rubricEntry.getValue().isEmpty()) {
				continue;
			}
			final CategoryDTO rubricCategory = new CategoryDTO(Tf1PlusConf.NAME, rubricEntry.getKey(),
					buildEditorialCategoryId(hub.getUrlSlug(), rubricEntry.getKey()), Tf1PlusConf.EXTENSION);
			rubricCategory.setDownloadable(false);
			rubricCategory.addSubCategories(rubricEntry.getValue());
			rubricCategories.add(rubricCategory);
		}
		return rubricCategories;
	}

	private Tf1PlusEditorialRubric resolveRubric(final Tf1PlusCatalogueEntry entry) {
		Tf1PlusEditorialRubric rubric = Tf1PlusEditorialRubricRegistry.resolveFromApiTypes(
				entry.getEditorialCategoryTypes());
		if (rubric == null) {
			rubric = Tf1PlusEditorialRubricRegistry.resolveSlugFallback(entry.getSlug());
		}
		return rubric;
	}

	private CategoryDTO toProgrammeCategory(final Tf1PlusCatalogueEntry entry) {
		if (entry == null || StringUtils.isEmpty(entry.getSlug()) || StringUtils.isEmpty(entry.getTitle())) {
			return null;
		}
		final CategoryDTO programme = new CategoryDTO(Tf1PlusConf.NAME, normalizeLabel(entry.getTitle()),
				entry.getPublicUrl(), Tf1PlusConf.EXTENSION);
		programme.setDownloadable(true);
		return programme;
	}

	private String buildEditorialCategoryId(final String channelSlug, final String rubricLabel) {
		return Tf1PlusConf.HOME_URL + "/" + channelSlug + "/rubrique/" + rubricLabel.toLowerCase(Locale.ROOT)
				.replace(' ', '-');
	}

	private String normalizeLabel(final String raw) {
		if (raw == null) {
			return "";
		}
		return raw.replaceAll("\\s+", " ").trim();
	}

}
