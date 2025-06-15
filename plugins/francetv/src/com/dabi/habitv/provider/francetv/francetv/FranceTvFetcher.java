package com.dabi.habitv.provider.francetv.francetv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.framework.plugin.utils.RetrieverUtils;
import com.dabi.habitv.provider.francetv.FranceTvConf;

/**
 * Modern fetcher for France.tv data using comprehensive web crawling
 */
public class FranceTvFetcher {
    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("francetv.debug", "false"));
    
    // Helper methods for controlled logging
    private static void logInfo(String message) {
        if (DEBUG) {
            System.out.println("[FranceTvFetcher] " + message);
        }
    }
    
    private static void logError(String message) {
        if (DEBUG) {
            System.err.println("[FranceTvFetcher ERROR] " + message);
        }
    }
    
    private final Proxy proxy;
    private final boolean useComprehensiveCrawling;
      public FranceTvFetcher(Proxy proxy) {
        this(proxy, true); // Default to comprehensive crawling
    }
    
    public FranceTvFetcher(Proxy proxy, boolean useComprehensiveCrawling) {
        this.proxy = proxy;
        this.useComprehensiveCrawling = useComprehensiveCrawling;
    }
    
    /**
     * Load categories and episodes from France.tv with test mode limitations
     */    public FranceTvArchive loadTestMode() {
        try {
            logInfo("Loading France.tv content using test mode (limited crawling)...");
            FranceTvContentCrawler crawler = new FranceTvContentCrawler(true); // Enable test mode
            return crawler.crawlAllContent();
        } catch (Exception e) {
            logError("Failed to load France.tv test data: " + e.getMessage());
            e.printStackTrace();
            return createEmptyArchive();
        }
    }
    
    /**
     * Load categories and episodes from France.tv
     */
    public FranceTvArchive load() {
        try {
            if (useComprehensiveCrawling) {
                return loadWithComprehensiveCrawling();
            } else {
                return loadWithSampleData();
            }
        } catch (Exception e) {
            throw new TechnicalException(e);
        }
    }
    
    /**
     * Load all real content from France.tv using comprehensive crawling
     */
    private FranceTvArchive loadWithComprehensiveCrawling() {
        logInfo("Loading France.tv content using comprehensive crawling...");
        
        FranceTvContentCrawler crawler = new FranceTvContentCrawler();
        FranceTvArchive archive = crawler.crawlAllContent();
        
        Map<String, Integer> stats = crawler.getDiscoveryStats();        logInfo("Comprehensive crawling completed:");
        logInfo("- Total content URLs: " + stats.get("totalUrls"));
        logInfo("- Total episodes: " + stats.get("totalEpisodes"));
        logInfo("- Categories: " + stats.get("totalCategories"));
        
        return archive;
    }
    
    /**
     * Load with sample data (fallback mode)
     */
    private FranceTvArchive loadWithSampleData() {
        logInfo("Loading France.tv content using sample data...");
        
        Map<String, CategoryDTO> catName2RootCat = new HashMap<>();
        Map<String, Collection<EpisodeDTO>> catName2Episode = new HashMap<>();
        
        // Create main categories
        createMainCategories(catName2RootCat);
        
        // Load sample episodes
        loadSampleEpisodesFromCategoryPages(catName2RootCat, catName2Episode);
        
        return new FranceTvArchive(catName2RootCat.values(), catName2Episode);
    }
      /**
     * Create main categories based on France.tv website structure
     */
    private void createMainCategories(Map<String, CategoryDTO> catName2RootCat) {
        // Category URL mappings based on the actual France.tv website structure
        Map<String, String> categoryUrls = new HashMap<>();
        categoryUrls.put("Séries & fictions", "/series-et-fictions/");
        categoryUrls.put("Documentaires", "/documentaires/");
        categoryUrls.put("Cinéma", "/films/");
        categoryUrls.put("Société", "/societe/");
        categoryUrls.put("Info", "/info/");
        categoryUrls.put("Arts & spectacles", "/spectacles-et-culture/");
        categoryUrls.put("Sport", "/sport/");
        categoryUrls.put("Divertissement", "/jeux-et-divertissements/");
        categoryUrls.put("Enfants", "/enfants/");
        
        for (Map.Entry<String, String> entry : categoryUrls.entrySet()) {
            String categoryName = entry.getKey();
            String categoryId = entry.getValue().replace("/", ""); // Remove slashes for ID
            
            CategoryDTO category = new CategoryDTO(FranceTvConf.NAME, categoryName, categoryId, FranceTvConf.EXTENSION);
            category.setDownloadable(true);
            catName2RootCat.put(categoryName, category);
        }
    }
    
    /**
     * Load sample episodes by using known France.tv content patterns
     */
    private void loadSampleEpisodesFromCategoryPages(Map<String, CategoryDTO> catName2RootCat, 
                                                    Map<String, Collection<EpisodeDTO>> catName2Episode) {
        // Create sample episodes based on known France.tv URL patterns and actual content
        createSampleEpisodesForCategory("Sport", new String[]{
            "https://www.france.tv/sport/cyclisme/criterium-du-dauphine/7267013-le-resume-de-l-etape-7.html",
            "https://www.france.tv/sport/tennis/roland-garros/7259399-roland-garros-une-edition-2025-inoubliable.html",
            "https://www.france.tv/sport/natation/championnats-de-france-de-natation/7243805-finales-du-samedi-14-juin-2025.html",
            "https://www.france.tv/sport/autres-sports/7243745-coupe-du-monde-de-canoe-kayak-finales-de-canoe-slalom.html"
        }, catName2RootCat, catName2Episode);
        
        createSampleEpisodesForCategory("Séries & fictions", new String[]{
            "https://www.france.tv/series-et-fictions/drame/sample-series-episode.html",
            "https://www.france.tv/series-et-fictions/comedie/sample-comedy-episode.html"
        }, catName2RootCat, catName2Episode);
        
        createSampleEpisodesForCategory("Documentaires", new String[]{
            "https://www.france.tv/documentaires/documentaires-societe/sample-documentary.html",
            "https://www.france.tv/documentaires/documentaires-histoire/sample-history-doc.html"
        }, catName2RootCat, catName2Episode);
        
        createSampleEpisodesForCategory("Cinéma", new String[]{
            "https://www.france.tv/films/drame/sample-drama-film.html",
            "https://www.france.tv/films/comedie/sample-comedy-film.html"
        }, catName2RootCat, catName2Episode);
        
        createSampleEpisodesForCategory("Info", new String[]{
            "https://www.france.tv/info/actualites/sample-news-report.html",
            "https://www.france.tv/info/politique/sample-political-news.html"
        }, catName2RootCat, catName2Episode);
    }    /**
     * Create sample episodes for a category with known URL patterns
     */
    private void createSampleEpisodesForCategory(String categoryName, String[] sampleUrls,
                                               Map<String, CategoryDTO> catName2RootCat,
                                               Map<String, Collection<EpisodeDTO>> catName2Episode) {
        CategoryDTO category = catName2RootCat.get(categoryName);
        if (category != null) {
            Collection<EpisodeDTO> episodes = new ArrayList<>();
            
            for (String url : sampleUrls) {
                String title = extractTitleFromUrl(url);
                EpisodeDTO episode = new EpisodeDTO(category, title, url);
                episodes.add(episode);
            }
            
            catName2Episode.put(category.getId(), episodes);
        }
    }
    
    /**
     * Extract a title from a France.tv URL
     */
    private String extractTitleFromUrl(String url) {
        if (url == null) return "Unknown";
        
        // Extract the filename part and convert to readable title
        String[] parts = url.split("/");
        if (parts.length > 0) {
            String filename = parts[parts.length - 1];
            if (filename.endsWith(".html")) {
                String title = filename.substring(0, filename.length() - 5);
                // Remove numeric prefix if present (e.g., "7267013-le-resume-de-l-etape-7")
                if (title.matches("\\d+-.*")) {
                    title = title.replaceFirst("\\d+-", "");
                }
                // Replace hyphens with spaces and capitalize
                title = title.replace("-", " ");
                return capitalizeWords(title);
            }
        }
        
        return "France.tv Content";
    }
    
    /**
     * Capitalize first letter of each word
     */
    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return str;
        
        String[] words = str.split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
                result.append(" ");
            }
        }
          return result.toString().trim();
    }
    
    /**
     * Get content from URL
     */
    private String getUrlContent(String url) throws IOException {
        try (InputStream inputStream = RetrieverUtils.getInputStreamFromUrl(url, proxy);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }
    
    /**
     * Extract media ID from France.tv URL for video downloading
     */
    public String extractMediaIdFromUrl(String url) {
        if (url == null) {
            return null;
        }
        
        // Pattern for France.tv content URLs: .../{id}-{title}.html
        Pattern pattern = Pattern.compile("/(\\d+)[-_][^/]*\\.html");
        Matcher matcher = pattern.matcher(url);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Fallback: try to find any number in the URL
        pattern = Pattern.compile("/(\\d{7,})");
        matcher = pattern.matcher(url);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    /**
     * Extract media ID from page content (fallback method)
     */
    public String extractMediaIdFromPage(String url) throws IOException {
        String content = getUrlContent(url);
        Document doc = Jsoup.parse(content);
        
        // Look for video player elements or data attributes
        Elements videoElements = doc.select("[data-video-id], [data-media-id], [id*='video'], [id*='player']");
        
        for (Element element : videoElements) {
            // Try various data attributes
            String mediaId = element.attr("data-video-id");
            if (mediaId != null && !mediaId.isEmpty() && mediaId.matches("\\d+")) {
                return mediaId;
            }
            
            mediaId = element.attr("data-media-id");
            if (mediaId != null && !mediaId.isEmpty() && mediaId.matches("\\d+")) {
                return mediaId;
            }
            
            // Try ID attribute
            String id = element.attr("id");
            if (id != null && id.matches(".*\\d{7,}.*")) {
                Pattern pattern = Pattern.compile("(\\d{7,})");
                Matcher matcher = pattern.matcher(id);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }
        
        // Look in JavaScript/JSON embedded in page
        Pattern scriptPattern = Pattern.compile("\"idDiffusion\"\\s*:\\s*\"?(\\d+)\"?");
        Matcher scriptMatcher = scriptPattern.matcher(content);
        if (scriptMatcher.find()) {
            return scriptMatcher.group(1);
        }
          return null;
    }
    
    /**
     * Create an empty archive for fallback scenarios
     */
    private FranceTvArchive createEmptyArchive() {
        return new FranceTvArchive(new java.util.ArrayList<>(), new java.util.HashMap<>());
    }
}
