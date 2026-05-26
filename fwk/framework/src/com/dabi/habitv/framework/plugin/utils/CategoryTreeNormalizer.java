package com.dabi.habitv.framework.plugin.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;

/**
 * Pads shallow provider category trees to a consistent depth below the plugin
 * node: channel → category → program.
 *
 * Only synthetic nodes created by this normalizer are forced to be
 * non-downloadable. Existing provider nodes keep their original downloadability
 * semantics because some providers intentionally expose downloadable branches.
 */
public final class CategoryTreeNormalizer {

	public static final String DEFAULT_CHANNEL_NAME = "Principal";

	public static final String DEFAULT_CATEGORY_NAME = "Programmes";

	/** Target depth from channel root to downloadable leaf (3 nodes). */
	private static final int TARGET_DEPTH = 3;

	private CategoryTreeNormalizer() {
	}

	public static Set<CategoryDTO> normalize(final String pluginName, final Set<CategoryDTO> roots) {
		if (roots == null || roots.isEmpty()) {
			return roots;
		}
		final Set<CategoryDTO> normalized = new LinkedHashSet<>();
		final List<CategoryDTO> shallowRoots = new ArrayList<>();
		for (final CategoryDTO root : roots) {
			final int depth = structureDepth(root);
			if (depth >= TARGET_DEPTH) {
				normalized.add(root);
			} else if (depth == 2) {
				insertCategoryLevel(root, DEFAULT_CATEGORY_NAME);
				normalized.add(root);
			} else {
				shallowRoots.add(root);
			}
		}
		if (!shallowRoots.isEmpty()) {
			normalized.add(wrapRootsInDefaultChannel(pluginName, shallowRoots));
		}
		return normalized;
	}

	private static CategoryDTO wrapRootsInDefaultChannel(final String pluginName, final List<CategoryDTO> roots) {
		final String channelId = syntheticId(pluginName, "channel");
		final CategoryDTO channel = new CategoryDTO(pluginName, DEFAULT_CHANNEL_NAME, channelId,
				resolveExtension(roots));
		channel.setDownloadable(false);
		final CategoryDTO category = new CategoryDTO(pluginName, DEFAULT_CATEGORY_NAME,
				syntheticId(channelId, "programmes"), resolveExtension(roots));
		category.setDownloadable(false);
		for (final CategoryDTO root : roots) {
			category.addSubCategory(root);
		}
		channel.addSubCategory(category);
		return channel;
	}

	private static void insertCategoryLevel(final CategoryDTO parent, final String categoryName) {
		final Collection<CategoryDTO> children = parent.getSubCategories();
		if (children == null || children.isEmpty()) {
			return;
		}
		final CategoryDTO category = new CategoryDTO(parent.getPlugin(), categoryName,
				syntheticId(parent.getId(), "programmes"), parent.getExtension());
		category.setDownloadable(false);
		for (final CategoryDTO child : new ArrayList<>(children)) {
			category.addSubCategory(child);
		}
		parent.getSubCategories().clear();
		parent.addSubCategory(category);
	}

	static int structureDepth(final CategoryDTO node) {
		final Collection<CategoryDTO> subs = node.getSubCategories();
		final boolean hasSubs = subs != null && !subs.isEmpty();
		if (!hasSubs) {
			return node.isDownloadable() ? 1 : 0;
		}
		int maxChild = 0;
		for (final CategoryDTO child : subs) {
			maxChild = Math.max(maxChild, structureDepth(child));
		}
		return maxChild == 0 ? 1 : 1 + maxChild;
	}

	private static String resolveExtension(final List<CategoryDTO> roots) {
		for (final CategoryDTO root : roots) {
			if (root.getExtension() != null) {
				return root.getExtension();
			}
		}
		return null;
	}

	private static String syntheticId(final String parentId, final String suffix) {
		return parentId + "#" + suffix;
	}
}
