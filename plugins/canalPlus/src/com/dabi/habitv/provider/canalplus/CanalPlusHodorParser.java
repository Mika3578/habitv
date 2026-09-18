package com.dabi.habitv.provider.canalplus;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Parses hodor detail JSON ({@code detailType=detailPage}, {@code objectType=unit}).
 */
final class CanalPlusHodorParser {

	private CanalPlusHodorParser() {
	}

	static List<Object> extractStrates(final Map<String, Object> root) {
		if (root == null) {
			return null;
		}
		final List<Object> rootStrates = asObjectList(root.get("strates"));
		if (rootStrates != null) {
			return rootStrates;
		}
		final Map<String, Object> currentPage = asStringObjectMap(root.get("currentPage"));
		if (currentPage != null) {
			return asObjectList(currentPage.get("strates"));
		}
		return null;
	}

	static CanalPlusUnitMetadata parseUnitDetail(final Map<String, Object> root) {
		if (root == null) {
			return null;
		}
		final Map<String, Object> pageMap = asStringObjectMap(root.get("currentPage"));
		final Map<String, Object> resolvedPage = pageMap != null ? pageMap : root;

		String title = stringValue(resolvedPage.get("title"));
		String subtitle = stringValue(resolvedPage.get("subtitle"));
		String editorialTitle = stringValue(resolvedPage.get("editorialTitle"));
		if (StringUtils.isEmpty(title)) {
			title = editorialTitle;
		}

		String contentId = null;
		final Map<String, Object> contentMap = asStringObjectMap(resolvedPage.get("content"));
		if (contentMap != null) {
			contentId = stringValue(contentMap.get("contentID"));
			if (StringUtils.isEmpty(contentId)) {
				contentId = stringValue(contentMap.get("contentId"));
			}
			if (StringUtils.isEmpty(title)) {
				title = stringValue(contentMap.get("title"));
			}
			if (StringUtils.isEmpty(subtitle)) {
				subtitle = stringValue(contentMap.get("subtitle"));
			}
		}

		final String displayName = joinTitle(title, subtitle);
		return new CanalPlusUnitMetadata(contentId, displayName, stringValue(resolvedPage.get("summary")));
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> asStringObjectMap(final Object value) {
		return value instanceof Map ? (Map<String, Object>) value : null;
	}

	@SuppressWarnings("unchecked")
	private static List<Object> asObjectList(final Object value) {
		return value instanceof List ? (List<Object>) value : null;
	}

	private static String joinTitle(final String title, final String subtitle) {
		if (StringUtils.isEmpty(title)) {
			return subtitle;
		}
		if (StringUtils.isEmpty(subtitle)) {
			return title;
		}
		return title + " " + subtitle;
	}

	private static String stringValue(final Object value) {
		return value == null ? null : String.valueOf(value).trim();
	}

	static final class CanalPlusUnitMetadata {
		private final String contentId;
		private final String displayName;
		private final String summary;

		CanalPlusUnitMetadata(final String contentId, final String displayName, final String summary) {
			this.contentId = contentId;
			this.displayName = displayName;
			this.summary = summary;
		}

		String getContentId() {
			return contentId;
		}

		String getDisplayName() {
			return displayName;
		}

		String getSummary() {
			return summary;
		}
	}

}
