package com.dabi.habitv.provider.francetv;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;

/**
 * Builds the {@code Pages publiques (france.tv)} category subtree from mobile
 * Yatta channel hub API responses.
 */
final class FranceTvPublicCategoryTreeBuilder {

	static final String PUBLIC_HUB_ORDER_PARAM = "publicHubOrder";

	interface ChannelHubLoader {
		Map<String, Object> loadChannelHub(String hubSlug) throws IOException;
	}

	interface HubDiagnosticsListener {
		void onHubDiscovery(FranceTvPublicHubDiagnostics diagnostics, String sourceUrl);
	}

	private final ChannelHubLoader channelHubLoader;

	private final HubDiagnosticsListener diagnosticsListener;

	private final Logger log;

	FranceTvPublicCategoryTreeBuilder(final ChannelHubLoader channelHubLoader,
			final HubDiagnosticsListener diagnosticsListener, final Logger log) {
		this.channelHubLoader = channelHubLoader;
		this.diagnosticsListener = diagnosticsListener;
		this.log = log;
	}

	CategoryDTO buildPublicRootCategory() {
		final CategoryDTO publicRoot = new CategoryDTO(FranceTvConf.NAME, "Pages publiques (france.tv)",
				FranceTvConf.HOME_URL + "/", FranceTvConf.EXTENSION);
		publicRoot.setDownloadable(false);

		for (final String hubSlug : FranceTvConf.publicRootHubDisplayOrder()) {
			publicRoot.addSubCategory(buildHubCategory(hubSlug));
		}
		logGeneratedPublicTree(publicRoot);
		return publicRoot;
	}

	private void logGeneratedPublicTree(final CategoryDTO publicRoot) {
		if (log == null || !log.isDebugEnabled()) {
			return;
		}
		log.debug("Generated France.tv public hub tree (pre-grabconfig merge):\n"
				+ formatPublicHubDebugReport(publicRoot));
	}

	private CategoryDTO buildHubCategory(final String hubSlug) {
		try {
			final Map<String, Object> body = channelHubLoader.loadChannelHub(hubSlug);
			return buildHubCategoryFromBody(hubSlug, body);
		} catch (IOException e) {
			final FranceTvPublicHubDiagnostics diagnostics = new FranceTvPublicHubDiagnostics(hubSlug);
			diagnostics.setRootCauseSummary("io-error:" + e.getClass().getSimpleName());
			return buildHubCategoryFromChildren(hubSlug,
					FranceTvPublicHubCatalog.selectNavigableChildren(hubSlug,
							java.util.Collections.<FranceTvHubItem>emptyList()),
					diagnostics);
		} catch (RuntimeException e) {
			final FranceTvPublicHubDiagnostics diagnostics = new FranceTvPublicHubDiagnostics(hubSlug);
			diagnostics.setRootCauseSummary("parse-error:" + e.getClass().getSimpleName());
			return buildHubCategoryFromChildren(hubSlug,
					FranceTvPublicHubCatalog.selectNavigableChildren(hubSlug,
							java.util.Collections.<FranceTvHubItem>emptyList()),
					diagnostics);
		}
	}

	private CategoryDTO buildHubCategoryFromBody(final String hubSlug, final Map<String, Object> body) {
		final FranceTvPublicHubCatalog.HubDiscoveryResult discovery = FranceTvPublicHubCatalog
				.discoverHubItems(hubSlug, body);
		final List<FranceTvHubItem> children = FranceTvPublicHubCatalog.selectNavigableChildren(hubSlug,
				discovery.getItems());
		final FranceTvPublicHubDiagnostics diagnostics = discovery.getDiagnostics();
		resolveRootCause(hubSlug, children, diagnostics);
		return buildHubCategoryFromChildren(hubSlug, children, diagnostics);
	}

	private static void resolveRootCause(final String hubSlug, final List<FranceTvHubItem> children,
			final FranceTvPublicHubDiagnostics diagnostics) {
		if (!children.isEmpty()) {
			diagnostics.setRootCauseSummary("ok");
			return;
		}
		if (FranceTvConf.hasConfiguredHubSeeds(hubSlug)) {
			diagnostics.setRootCauseSummary("configured-seed-fallback");
			return;
		}
		if ("empty-response".equals(diagnostics.getRootCauseSummary())
				|| "no-collections".equals(diagnostics.getRootCauseSummary())
				|| FranceTvPublicHubCatalog.ROOT_CAUSE_NO_USABLE_TAXONOMY
						.equals(diagnostics.getRootCauseSummary())) {
			diagnostics.setRootCauseSummary(FranceTvPublicHubCatalog.ROOT_CAUSE_NO_USABLE_TAXONOMY);
			return;
		}
		diagnostics.setRootCauseSummary(FranceTvPublicHubCatalog.ROOT_CAUSE_NO_USABLE_TAXONOMY);
	}

	private CategoryDTO buildHubCategoryFromChildren(final String hubSlug, final List<FranceTvHubItem> children,
			final FranceTvPublicHubDiagnostics diagnostics) {
		final CategoryDTO hub = hubContainerCategory(hubSlug);
		diagnostics.setCreatedCategories(children.size());
		diagnostics.setCreatedReplayItems(0);
		logHubDiagnostics(diagnostics, hubSourceUrl(hubSlug));
		for (final FranceTvHubItem child : children) {
			final CategoryDTO seedCategory = toSeedCategory(child);
			if (seedCategory != null) {
				hub.addSubCategory(seedCategory);
			}
		}
		return hub;
	}

	private static CategoryDTO toSeedCategory(final FranceTvHubItem seed) {
		final String pageUrl = StringUtils.isNotEmpty(seed.getPageUrl()) ? seed.getPageUrl()
				: FranceTvUrls.programPageUrlFromTaxonomySlug(seed.getTaxonomySlug());
		if (StringUtils.isEmpty(pageUrl)) {
			return null;
		}
		final CategoryDTO category = new CategoryDTO(FranceTvConf.NAME, seed.getLabel(), pageUrl,
				FranceTvConf.EXTENSION);
		category.setDownloadable(true);
		return category;
	}

	private void logHubDiagnostics(final FranceTvPublicHubDiagnostics diagnostics, final String sourceUrl) {
		if (diagnostics == null) {
			return;
		}
		if (diagnosticsListener != null) {
			diagnosticsListener.onHubDiscovery(diagnostics, sourceUrl);
			return;
		}
		if (log == null) {
			return;
		}
		final String line = diagnostics.formatLogLine(sourceUrl);
		if ("ok".equals(diagnostics.getRootCauseSummary())
				|| "configured-seed-fallback".equals(diagnostics.getRootCauseSummary())) {
			log.info(line);
		} else {
			log.warn(line);
		}
	}

	private static String hubSourceUrl(final String hubSlug) {
		return FranceTvConf.API_MOBILE_URL + "/apps/channels/" + hubSlug + "?platform=" + FranceTvConf.API_PLATFORM;
	}

	private static CategoryDTO hubContainerCategory(final String hubSlug) {
		final String hubUrl = FranceTvUrls.hubPageUrl(hubSlug);
		final CategoryDTO hub = new CategoryDTO(FranceTvConf.NAME, FranceTvUrls.channelLabel(hubSlug), hubUrl,
				FranceTvConf.EXTENSION);
		hub.setDownloadable(false);
		hub.addParameter(PUBLIC_HUB_ORDER_PARAM, String.valueOf(FranceTvConf.publicHubOrderIndex(hubSlug)));
		return hub;
	}

	static String formatPublicHubDebugReport(final CategoryDTO publicRoot) {
		if (publicRoot == null) {
			return "";
		}
		final StringBuilder builder = new StringBuilder();
		builder.append(publicRoot.getName()).append('\n');
		for (final CategoryDTO hub : publicRoot.getSubCategories()) {
			builder.append("- hub=").append(hub.getName());
			builder.append(" url=").append(hub.getId());
			builder.append(" order=").append(hub.getParameter(PUBLIC_HUB_ORDER_PARAM));
			builder.append(" downloadable=").append(hub.isDownloadable());
			builder.append(" childCount=").append(hub.getSubCategories().size());
			builder.append('\n');
			for (final CategoryDTO child : hub.getSubCategories()) {
				builder.append("    * child=").append(child.getName());
				builder.append(" url=").append(child.getId());
				builder.append(" downloadable=").append(child.isDownloadable());
				builder.append('\n');
			}
		}
		return builder.toString();
	}

	static String formatTreeShape(final CategoryDTO root, final int maxDepth) {
		final StringBuilder builder = new StringBuilder();
		appendTreeNode(builder, root, 0, maxDepth);
		return builder.toString();
	}

	private static void appendTreeNode(final StringBuilder builder, final CategoryDTO node, final int depth,
			final int maxDepth) {
		if (node == null) {
			return;
		}
		for (int i = 0; i < depth; i++) {
			builder.append("  ");
		}
		builder.append(node.getName());
		if (StringUtils.isNotEmpty(node.getId())) {
			builder.append(" [").append(node.getId()).append(']');
		}
		builder.append('\n');
		if (depth >= maxDepth) {
			return;
		}
		for (final CategoryDTO child : node.getSubCategories()) {
			appendTreeNode(builder, child, depth + 1, maxDepth);
		}
	}

}
