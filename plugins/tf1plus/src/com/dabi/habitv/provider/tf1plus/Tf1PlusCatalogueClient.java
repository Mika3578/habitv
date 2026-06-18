package com.dabi.habitv.provider.tf1plus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;

/**
 * Paginated TF1+ catalogue discovery client. Fetches and normalizes programme metadata
 * from public GraphQL endpoints.
 */
final class Tf1PlusCatalogueClient {

	private final Tf1PlusGraphqlClient graphqlClient;

	Tf1PlusCatalogueClient(final BasePluginWithProxy plugin) {
		this.graphqlClient = new Tf1PlusGraphqlClient(plugin);
	}

	Tf1PlusCatalogueClient(final Tf1PlusGraphqlClient graphqlClient) {
		this.graphqlClient = graphqlClient;
	}

	Tf1PlusCataloguePageResult fetchProgramPage(final Tf1PlusHubDescriptor hub, final int offset, final int limit)
			throws IOException {
		final List<Map<String, Object>> items = graphqlClient.fetchProgramItemsPage(hub.getGraphqlChannelSlug(), offset,
				limit);
		final List<Tf1PlusCatalogueEntry> entries = new ArrayList<Tf1PlusCatalogueEntry>(items.size());
		for (final Map<String, Object> item : items) {
			final Tf1PlusCatalogueEntry entry = normalizeProgramme(hub, item);
			if (entry != null) {
				entries.add(entry);
			}
		}
		final boolean lastPage = items.isEmpty() || items.size() < limit;
		return new Tf1PlusCataloguePageResult(entries, offset, limit, lastPage);
	}

	void fetchAllProgrammesForHub(final Tf1PlusHubDescriptor hub, final Tf1PlusCatalogueRefreshResult refreshResult)
			throws IOException {
		final int pageSize = Tf1PlusConf.CATALOGUE_PAGE_SIZE;
		final int safetyLimit = Tf1PlusConf.CATALOGUE_SAFETY_PROGRAMME_LIMIT;
		int offset = 0;
		int pagesRequested = 0;
		int hubFetched = 0;
		int hubAdded = 0;
		int hubDuplicates = 0;
		int hubRejected = 0;
		boolean suspectedCap = false;
		final Set<String> seenKeys = new HashSet<String>();

		while (offset < safetyLimit) {
			pagesRequested++;
			final Tf1PlusCataloguePageResult page = fetchProgramPage(hub, offset, pageSize);
			hubFetched += page.getEntries().size();
			refreshResult.addProgrammesFetched(page.getEntries().size());

			if (page.getEntries().isEmpty()) {
				if (offset > 0 && offset % pageSize == 0) {
					suspectedCap = true;
				}
				break;
			}

			for (final Tf1PlusCatalogueEntry entry : page.getEntries()) {
				final String key = entry.deduplicationKey();
				if (key.isEmpty()) {
					hubRejected++;
					refreshResult.incrementProgrammesRejected("missing-slug");
					continue;
				}
				if (seenKeys.contains(key)) {
					hubDuplicates++;
					refreshResult.incrementDuplicatesSkipped();
					continue;
				}
				if (isExcludedProgrammeSlug(entry.getSlug())) {
					hubRejected++;
					refreshResult.incrementProgrammesRejected("excluded-slug");
					continue;
				}
				seenKeys.add(key);
				hubAdded++;
				refreshResult.incrementProgrammesAdded();
				if (Tf1PlusEditorialRubricRegistry.resolveFromApiTypes(entry.getEditorialCategoryTypes()) == null
						&& !entry.getEditorialCategoryTypes().isEmpty()) {
					refreshResult.incrementUnknownRubricCount();
				}
				refreshResult.addEntry(entry);
			}

			if (page.isLastPage()) {
				if (page.getEntries().size() == pageSize && offset == 0) {
					final Tf1PlusCataloguePageResult probe = fetchProgramPage(hub, pageSize, pageSize);
					if (probe.getEntries().isEmpty()) {
						suspectedCap = true;
					} else {
						offset += pageSize;
						continue;
					}
				}
				break;
			}
			offset += pageSize;
		}

		if (suspectedCap) {
			refreshResult.setSuspectedApiCapReached(true);
		}

		refreshResult.addHubSummary(hub.getHubId() + "|label=" + hub.getDisplayLabel() + "|graphqlSlug="
				+ hub.getGraphqlChannelSlug() + "|urlSlug=" + hub.getUrlSlug() + "|limit=" + pageSize + "|pages="
				+ pagesRequested + "|offsets=" + offset + "|fetched=" + hubFetched + "|added=" + hubAdded
				+ "|duplicates=" + hubDuplicates + "|rejected=" + hubRejected + "|apiCap=" + suspectedCap);
	}

	Tf1PlusCatalogueEntry normalizeProgramme(final Tf1PlusHubDescriptor hub, final Map<String, Object> program) {
		if (program == null) {
			return null;
		}
		final String slug = Tf1PlusGraphqlClient.stringValue(program.get("slug"));
		final String title = Tf1PlusGraphqlClient.stringValue(program.get("name"));
		if (StringUtils.isEmpty(slug) || StringUtils.isEmpty(title)) {
			return null;
		}
		final String programmeId = Tf1PlusGraphqlClient.stringValue(program.get("id"));
		final List<String> editorialTypes = extractEditorialTypes(program);
		final List<String> rights = extractRights(program);
		final String thumbnail = extractThumbnail(program);
		final String publicUrl = buildProgramUrl(hub.getUrlSlug(), slug);
		return new Tf1PlusCatalogueEntry(programmeId, slug, title, hub.getHubId(), hub.getUrlSlug(), publicUrl,
				editorialTypes, rights, thumbnail);
	}

	static String buildProgramUrl(final String channelSlug, final String programmeSlug) {
		return Tf1PlusConf.HOME_URL + "/" + channelSlug + "/" + programmeSlug;
	}

	static boolean isExcludedProgrammeSlug(final String slug) {
		return slug != null && Tf1PlusConf.EXCLUDED_PROGRAM_SLUGS.contains(slug.toLowerCase(Locale.ROOT));
	}

	private static List<String> extractEditorialTypes(final Map<String, Object> program) {
		final List<String> types = new ArrayList<String>();
		for (final Map<String, Object> category : Tf1PlusGraphqlClient.castItemList(program.get("categories"))) {
			final String type = Tf1PlusGraphqlClient.stringValue(category.get("type"));
			if (!type.isEmpty()) {
				types.add(type);
			}
		}
		return types;
	}

	private static List<String> extractRights(final Map<String, Object> program) {
		final List<String> rights = new ArrayList<String>();
		final Object rawRights = program.get("rights");
		if (rawRights instanceof List) {
			for (final Object entry : (List<?>) rawRights) {
				if (entry != null) {
					rights.add(String.valueOf(entry));
				}
			}
		}
		return rights;
	}

	private static String extractThumbnail(final Map<String, Object> program) {
		final Map<String, Object> image = Tf1PlusGraphqlClient.asMap(program.get("image"));
		return Tf1PlusGraphqlClient.stringValue(image.get("url"));
	}

}
