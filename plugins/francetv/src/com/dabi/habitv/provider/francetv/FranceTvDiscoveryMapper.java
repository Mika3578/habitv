package com.dabi.habitv.provider.francetv;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

/**
 * Maps public-page discovery results into Habitv category and episode DTOs.
 */
final class FranceTvDiscoveryMapper {

	private FranceTvDiscoveryMapper() {
	}

	static Collection<CategoryDTO> toSectionCategories(final FranceTvDiscoveryResult result,
			final String collectionUrl) {
		final Set<CategoryDTO> sections = new LinkedHashSet<>();
		if (result == null) {
			return sections;
		}
		final String baseUrl = FranceTvUrls.stripSectionFragment(collectionUrl);
		for (final FranceTvDiscoverySection section : result.getSections()) {
			final CategoryDTO sectionCategory = new CategoryDTO(FranceTvConf.NAME, section.getLabel(),
					FranceTvUrls.sectionCategoryId(baseUrl, section.getSlug()), FranceTvConf.EXTENSION);
			sectionCategory.setDownloadable(section.hasReplayItems());
			sections.add(sectionCategory);
		}
		return sections;
	}

	static CategoryDTO toCollectionCategory(final FranceTvDiscoveryResult result, final String collectionUrl) {
		final String baseUrl = FranceTvUrls.stripSectionFragment(collectionUrl);
		final String label = StringUtils.isNotEmpty(result.getPageTitle()) ? result.getPageTitle()
				: FranceTvUrls.collectionLabelFromUrl(baseUrl);
		final CategoryDTO collection = new CategoryDTO(FranceTvConf.NAME, label, baseUrl, FranceTvConf.EXTENSION);
		collection.setDownloadable(false);
		collection.addSubCategories(toSectionCategories(result, baseUrl));
		return collection;
	}

	static Set<EpisodeDTO> toEpisodes(final FranceTvDiscoveryResult result, final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		if (result == null || category == null) {
			return episodes;
		}
		final String sectionSlug = FranceTvUrls.sectionSlugFromCategoryId(category.getId());
		for (final FranceTvDiscoveryItem item : result.replayItemsForSection(sectionSlug)) {
			final EpisodeDTO episode = new EpisodeDTO(category, item.getTitle(), item.getItemUrl());
			if (item.getPublicationDate() != null) {
				episode.setEpisodeDate(item.getPublicationDate());
			}
			if (item.getDurationSeconds() != null) {
				episode.setDurationSeconds(item.getDurationSeconds());
			}
			episodes.add(episode);
		}
		return episodes;
	}
}
