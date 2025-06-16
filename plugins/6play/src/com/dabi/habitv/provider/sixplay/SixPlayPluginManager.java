package com.dabi.habitv.provider.sixplay;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;

public class SixPlayPluginManager extends BasePluginWithProxy implements PluginProviderInterface { // NO_UCD

	private static final Logger LOG = Logger.getLogger(SixPlayPluginManager.class);

	// Cache for storing successful requests
	private static final Map<String, Document> documentCache = new ConcurrentHashMap<>();
	private static final Map<String, Set<CategoryDTO>> categoryCache = new ConcurrentHashMap<>();

	// Maximum number of retries for failed requests
	private static final int MAX_RETRIES = 2;

	// Shorter timeout for tests (5 seconds)
	private static final int TEST_TIMEOUT_MS = 5000;

	// Optimized selectors for categories
	private static final String[] CATEGORY_SELECTORS = {
		"a[href*='/programmes/']",  // Primary selector for programs
		"a[href*='/emissions/']",   // Alternative selector for shows
		"a[href*='/replay/']",      // Replay section
		".folders__list a",         // Legacy selector
		"nav a",                    // Navigation menu
		"a[href*='/videos/']",      // Videos section
		"a[href*='/series/']",      // Series section
		"a[href*='/documentaires/']", // Documentaries section
		"a[href*='/divertissement/']", // Entertainment section
		"a[href*='/info-magazines/']", // News and magazines section
		"a[href*='/jeunesse/']",    // Youth section
		"a[href*='/sport/']",       // Sports section
		"a[href*='/m6/']",          // M6 channel
		"a[href*='/w9/']",          // W9 channel
		"a[href*='/6ter/']",        // 6ter channel
		"a[href*='/gulli/']",       // Gulli channel
		"a[href*='/paris-premiere/']", // Paris Premiere channel
		"a[href*='/teva/']"         // Teva channel
	};

	// Optimized selectors for episodes
	private static final String[] EPISODE_SELECTORS = {
		"a[href*='/videos/']",      // Primary selector for videos
		".tvshow-bloc__content a",  // Legacy selector
		".episode-list a",          // Episode list
		".video-list a",            // Video list
		"a[href*='/replay/']",      // Replay videos
		"a[data-video-id]",         // Videos with data-video-id attribute
		"a.card",                   // Card links
		"a.thumbnail",              // Thumbnail links
		"a.episode",                // Episode links
		"a.video",                  // Video links
		"a.program",                // Program links
		"a.show",                   // Show links
		"a.replay",                 // Replay links
		"a[class*='video']",        // Any link with 'video' in its class
		"a[class*='episode']",      // Any link with 'episode' in its class
		"a[class*='replay']"        // Any link with 'replay' in its class
	};

	@Override
	public String getName() {
		return SixPlayConf.NAME;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		LOG.info("Finding episodes for category: " + category.getName() + " (" + category.getId() + ")");

		Document doc = getDocumentWithRetry(category.getId());
		if (doc != null) {
			LOG.info("Document retrieved successfully, trying selectors...");

			// Try each selector until we find episodes
			for (String selector : EPISODE_SELECTORS) {
				LOG.info("Trying selector: " + selector);
				Elements episodeElements = doc.select(selector);
				LOG.info("Found " + episodeElements.size() + " elements with selector: " + selector);

				if (!episodeElements.isEmpty()) {
					for (Element aEp : episodeElements) {
						String href = aEp.attr("href");
						if (href != null && !href.isEmpty()) {
							href = getFullUrl(href);
							// Try multiple title selectors
							String name = null;
							for (String titleSelector : new String[]{".tile__name", ".episode-title", ".video-title", "h1", "h2", "h3"}) {
								Elements titles = aEp.select(titleSelector);
								if (!titles.isEmpty()) {
									name = titles.first().text().trim();
									break;
								}
							}

							// If no title found in elements, try the element's own text
							if (name == null || name.isEmpty()) {
								name = aEp.text().trim();
							}

							if (!StringUtils.isEmpty(name)) {
								LOG.info("Found episode: " + name + " -> " + href);
								episodes.add(new EpisodeDTO(category, name, href));
							}
						}
					}

					// If we found episodes, break the loop
					if (!episodes.isEmpty()) {
						LOG.info("Found " + episodes.size() + " episodes, stopping search");
						break;
					}
				}
			}
		} else {
			LOG.warn("Failed to retrieve document for category: " + category.getId());
		}

		return episodes;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		LOG.info("Finding categories...");
		final Set<CategoryDTO> categories = new LinkedHashSet<>();

		Document doc = getDocumentWithRetry(SixPlayConf.HOME_URL);
		if (doc != null) {
			LOG.info("Document retrieved successfully, trying selectors...");

			// Try each selector until we find categories
			for (String selector : CATEGORY_SELECTORS) {
				LOG.info("Trying selector: " + selector);
				Elements mainCatElements = doc.select(selector);
				LOG.info("Found " + mainCatElements.size() + " elements with selector: " + selector);

				if (!mainCatElements.isEmpty()) {
					for (final Element mainCatA : mainCatElements) {
						String href = mainCatA.attr("href");
						if (isValidCategoryUrl(href)) {
							String channel = mainCatA.text().trim();
							if (isValidCategoryName(channel)) {
								String fullUrl = getFullUrl(href);
								LOG.info("Found category: " + channel + " -> " + fullUrl);
								CategoryDTO channelCat = new CategoryDTO(SixPlayConf.NAME, channel, fullUrl, SixPlayConf.EXTENSION);
								Collection<CategoryDTO> subCategories = findCategoryByMainCat(fullUrl);
								channelCat.addSubCategories(subCategories);

								// If no subcategories were found, mark this category as downloadable
								if (subCategories.isEmpty()) {
									LOG.info("No subcategories found for " + channel + ", marking as downloadable");
									channelCat.setDownloadable(true);
								}

								categories.add(channelCat);
							}
						}
					}

					// If we found categories, break the loop
					if (!categories.isEmpty()) {
						LOG.info("Found " + categories.size() + " categories, stopping search");
						break;
					}
				}
			}
		} else {
			LOG.warn("Failed to retrieve document for home URL");
		}

		return categories;
	}

	private Collection<CategoryDTO> findCategoryByMainCat(String mainCatUrl) {
		LOG.info("Finding subcategories for: " + mainCatUrl);
		final Set<CategoryDTO> categories = new LinkedHashSet<>();

		Document doc = getDocumentWithRetry(mainCatUrl);
		if (doc != null) {
			// Try multiple selectors for subcategories
			String[] subCatSelectors = {
				"a[href*='/videos/']",
				"a[href*='/replay/']",
				".mosaic-programs a",
				".programs a",
				".shows a"
			};

			for (String selector : subCatSelectors) {
				LOG.info("Trying subcategory selector: " + selector);
				Elements aCatElements = doc.select(selector);
				LOG.info("Found " + aCatElements.size() + " elements with selector: " + selector);

				for (Element aCat : aCatElements) {
					String href = aCat.attr("href");
					if (href != null && !href.isEmpty()) {
						href = getFullUrl(href);
						// Try multiple title selectors
						String name = null;
						for (String titleSelector : new String[]{".tile__title", "h1", "h2", "h3", ".title"}) {
							Elements titles = aCat.select(titleSelector);
							if (!titles.isEmpty()) {
								name = titles.first().text().trim();
								break;
							}
						}

						// If no title found in elements, try the element's own text
						if (name == null || name.isEmpty()) {
							name = aCat.text().trim();
						}

						if (!name.isEmpty()) {
							LOG.info("Found subcategory: " + name + " -> " + href);
							CategoryDTO catCat = new CategoryDTO(SixPlayConf.NAME, name, href, SixPlayConf.EXTENSION);
							catCat.setDownloadable(true);
							categories.add(catCat);
						}
					}
				}

				if (!categories.isEmpty()) {
					LOG.info("Found " + categories.size() + " subcategories, stopping search");
					break;
				}
			}
		} else {
			LOG.warn("Failed to retrieve document for URL: " + mainCatUrl);
		}

		return categories;
	}

	private Document getDocumentWithRetry(String url) {
		// Try to get from cache first
		if (documentCache.containsKey(url)) {
			LOG.info("Using cached document for: " + url);
			return documentCache.get(url);
		}

		Document doc = null;
		int retries = 0;

		while (retries <= MAX_RETRIES) {
			try {
				LOG.info("Fetching URL: " + url + " (attempt " + (retries + 1) + ")");
				String content = getUrlContent(url);
				if (content != null && !content.isEmpty()) {
					doc = Jsoup.parse(content);
					// Cache successful result
					documentCache.put(url, doc);
					LOG.info("Successfully retrieved and parsed document");
					break;
				} else {
					LOG.warn("Empty content received from URL: " + url);
				}
			} catch (Exception e) {
				LOG.warn("Failed to fetch URL " + url + " (attempt " + (retries + 1) + "): " + e.getMessage());
				retries++;
				if (retries <= MAX_RETRIES) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			}
		}

		return doc;
	}

	private boolean isValidCategoryUrl(String href) {
		return href != null && href.length() > 5 && !href.startsWith("#") && !href.startsWith("javascript:");
	}

	private boolean isValidCategoryName(String name) {
		return !name.isEmpty() && name.length() > 1;
	}

	private String getFullUrl(String url) {
		if (url.startsWith("/")) {
			url = SixPlayConf.HOME_URL + url;
		}
		// Normalize double slashes in the URL path
		return url.replaceAll("([^:])//", "$1/");
	}
}
