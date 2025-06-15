package com.dabi.habitv.provider.francetv.francetv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.provider.francetv.FranceTvConf;

/**
 * Simple fetcher that gets content from France.tv homepage instead of category pages
 */
public class SimpleFranceTvFetcher {
    private static final String FRANCETV_ID = "francetv";
    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("francetv.debug", "false"));
    
    // Helper methods for controlled logging
    private static void logInfo(String message) {
        if (DEBUG) {
            System.out.println("[SimpleFranceTvFetcher] " + message);
        }
    }
    
    private static void logError(String message) {
        if (DEBUG) {
            System.err.println("[SimpleFranceTvFetcher ERROR] " + message);
        }
    }

    private final Map<String, CategoryDTO> catName2RootCat;
    private final Map<String, Collection<EpisodeDTO>> catName2Episode;

    // France.tv URLs
    private static final String FRANCE_TV_BASE_URL = "https://www.france.tv";

    public SimpleFranceTvFetcher() {
        this.catName2RootCat = new HashMap<>();
        this.catName2Episode = new HashMap<>();
    }

    /**
     * Load data by scraping the France.tv homepage
     */
    public FranceTvArchive load() {
        try {
            // Create a single main category
            createMainCategory();

            // Load content from homepage
            loadContentFromHomepage();

        } catch (Exception e) {
            logError("Failed to load France.tv data: " + e.getMessage());
            // Return empty archive instead of throwing exception for now
        }

        return new FranceTvArchive(catName2RootCat.values(), catName2Episode);
    }

    private void createMainCategory() {        CategoryDTO mainCategory = new CategoryDTO(FranceTvConf.NAME, "France.tv", FRANCETV_ID, FranceTvConf.EXTENSION);
        mainCategory.setDownloadable(true);
        catName2RootCat.put(FRANCETV_ID, mainCategory);
    }

    private void loadContentFromHomepage() throws IOException {
        String url = FRANCE_TV_BASE_URL + "/";

        logInfo("Fetching content from: " + url);

        // Connect to France.tv homepage
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get();

        System.out.println("Successfully fetched homepage, title: " + doc.title());

        CategoryDTO mainCategory = catName2RootCat.get("francetv");
        Collection<EpisodeDTO> episodes = new ArrayList<>();

        // Look for video/content links on the homepage
        // Try multiple selectors to find content
        String[] selectors = {
            "a[href*='.html']",           // All HTML links
            "a[href*='/films/']",         // Film links
            "a[href*='/series-et-fictions/']", // Series links
            "a[href*='/documentaires/']", // Documentary links
            "a[href*='/france-2/']",      // France 2 content
            "a[href*='/france-3/']",      // France 3 content
            "a[href*='/france-5/']"       // France 5 content
        };

        for (String selector : selectors) {
            Elements links = doc.select(selector);
            System.out.println("Found " + links.size() + " links with selector: " + selector);

            for (Element link : links) {
                String href = link.attr("href");
                String title = extractTitle(link);

                if (isValidContentLink(href, title)) {
                    String fullUrl = href.startsWith("http") ? href : FRANCE_TV_BASE_URL + href;
                    EpisodeDTO episode = new EpisodeDTO(mainCategory, title, fullUrl);
                    episodes.add(episode);

                    if (episodes.size() >= 20) { // Limit for testing
                        break;
                    }
                }
            }

            if (!episodes.isEmpty()) {
                break; // Stop after finding content with first successful selector
            }
        }

        System.out.println("Found " + episodes.size() + " episodes total");
        catName2Episode.put(mainCategory.getId(), episodes);
    }

    private String extractTitle(Element link) {
        // Try multiple methods to get title
        String title = link.attr("title");
        if (title.trim().isEmpty()) {
            title = link.attr("alt");
        }
        if (title.trim().isEmpty()) {
            title = link.text();
        }
        if (title.trim().isEmpty()) {
            Elements imgElements = link.select("img");
            if (!imgElements.isEmpty() && imgElements.first() != null) {
                title = imgElements.first().attr("alt");
            }
        }
        return title.trim().isEmpty() ? "Unknown" : title.trim();
    }

    private boolean isValidContentLink(String href, String title) {
        if (href == null || href.trim().isEmpty()) {
            return false;
        }

        if (title == null || title.trim().isEmpty() || title.equals("Unknown")) {
            return false;
        }

        // Must be HTML content
        if (!href.endsWith(".html")) {
            return false;
        }

        // Exclude navigation and system pages
        if (href.contains("/direct.html") || 
            href.contains("/toutes-les-videos") ||
            href.contains("/recherche") ||
            href.contains("/confidentialite") ||
            href.contains("/mentions-legales")) {
            return false;
        }

        // Should have some meaningful content path
        return href.contains("/films/") || 
               href.contains("/series-") || 
               href.contains("/documentaires/") ||
               href.contains("/france-") ||
               href.contains("/info/") ||
               href.contains("/sport/") ||
               href.contains("/enfants/");
    }
}
