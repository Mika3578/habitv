package com.dabi.habitv.provider.tf1plus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Normalized TF1+ programme catalogue entry (discovered dynamically, cacheable).
 */
final class Tf1PlusCatalogueEntry {

	private final String programmeId;
	private final String slug;
	private final String title;
	private final String hubId;
	private final String urlSlug;
	private final String publicUrl;
	private final List<String> editorialCategoryTypes;
	private final List<String> rights;
	private final String thumbnailUrl;

	Tf1PlusCatalogueEntry(final String programmeId, final String slug, final String title, final String hubId,
			final String urlSlug, final String publicUrl, final List<String> editorialCategoryTypes,
			final List<String> rights, final String thumbnailUrl) {
		this.programmeId = programmeId == null ? "" : programmeId;
		this.slug = slug == null ? "" : slug;
		this.title = title == null ? "" : title;
		this.hubId = hubId == null ? "" : hubId;
		this.urlSlug = urlSlug == null ? "" : urlSlug;
		this.publicUrl = publicUrl == null ? "" : publicUrl;
		this.editorialCategoryTypes = copyList(editorialCategoryTypes);
		this.rights = copyList(rights);
		this.thumbnailUrl = thumbnailUrl == null ? "" : thumbnailUrl;
	}

	String getProgrammeId() {
		return programmeId;
	}

	String getSlug() {
		return slug;
	}

	String getTitle() {
		return title;
	}

	String getHubId() {
		return hubId;
	}

	String getUrlSlug() {
		return urlSlug;
	}

	String getPublicUrl() {
		return publicUrl;
	}

	List<String> getEditorialCategoryTypes() {
		return editorialCategoryTypes;
	}

	List<String> getRights() {
		return rights;
	}

	String getThumbnailUrl() {
		return thumbnailUrl;
	}

	String deduplicationKey() {
		if (!slug.isEmpty()) {
			return hubId + ":" + slug;
		}
		if (!programmeId.isEmpty()) {
			return hubId + ":id:" + programmeId;
		}
		return "";
	}

	private static List<String> copyList(final List<String> source) {
		if (source == null || source.isEmpty()) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(new ArrayList<String>(source));
	}

}
