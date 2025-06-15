package com.dabi.habitv.provider.francetv.francetv;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.provider.francetv.FranceTvConf;
import com.dabi.habitv.provider.francetv.francetv.FranceTvArchive;

/**
 * Comprehensive content crawler for France.tv that discovers ALL real content
 * instead of using sample episodes.
 */
public class FranceTvContentCrawler {
    private final ExecutorService executorService;
    private final Set<String> discoveredUrls;
    private final Map<String, CategoryDTO> categories;
    private final Map<String, Collection<EpisodeDTO>> categoryEpisodes;
    private final boolean testMode;
    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("francetv.debug", "false"));
    
    // Configuration
    private static final int MAX_THREADS = 8;
    private static final int PAGE_TIMEOUT = 15000;
    private static final int MAX_DEPTH = 3;
    private static final int MAX_EPISODES_PER_CATEGORY = 1000;
    
    // France.tv website structure
    private static final String BASE_URL = "https://www.france.tv";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
      // Main category entry points for comprehensive crawling
    private static final Map<String, String[]> CATEGORY_DISCOVERY_URLS = new HashMap<>();
    static {
        CATEGORY_DISCOVERY_URLS.put("Sport", new String[]{
            "/sport/",
            "/sport/football/",
            "/sport/tennis/",
            "/sport/cyclisme/",
            "/sport/basketball/",
            "/sport/rugby/",
            "/sport/natation/",
            "/sport/athletisme/",
            "/sport/autres-sports/"
        });
          CATEGORY_DISCOVERY_URLS.put("Séries & fictions", new String[]{
            "/series-et-fictions/"
        });
        
        CATEGORY_DISCOVERY_URLS.put("Documentaires", new String[]{
            "/documentaires/",
            "/documentaires/documentaires-societe/",
            "/documentaires/documentaires-histoire/",
            "/documentaires/documentaires-science/"
        });
        
        CATEGORY_DISCOVERY_URLS.put("Cinéma", new String[]{
            "/films/",
            "/films/films-comedie/",
            "/films/films-drame/",
            "/films/films-suspense/",
            "/films/courts-metrages/"
        });
        
        CATEGORY_DISCOVERY_URLS.put("Info", new String[]{
            "/info/",
            "/info/decryptage-et-investigation/",
            "/info/emission-politique/",
            "/info/jt/",
            "/info/meteo/"
        });
        
        CATEGORY_DISCOVERY_URLS.put("Arts & spectacles", new String[]{
            "/spectacles-et-culture/",
            "/spectacles-et-culture/theatre-et-danse/",
            "/spectacles-et-culture/musique/",
            "/spectacles-et-culture/opera/"
        });
        
        CATEGORY_DISCOVERY_URLS.put("Divertissement", new String[]{
            "/jeux-et-divertissements/",
            "/jeux-et-divertissements/jeux/",
            "/jeux-et-divertissements/emissions-de-divertissement/",
            "/jeux-et-divertissements/lifestyle/",
            "/jeux-et-divertissements/humour/"
        });
        
        CATEGORY_DISCOVERY_URLS.put("Enfants", new String[]{
            "/enfants/",
            "/enfants/dessins-animes/",
            "/enfants/series/",
            "/enfants/emissions/",
            "/enfants/films/"
        });
        
        CATEGORY_DISCOVERY_URLS.put("Société", new String[]{
            "/societe/"
        });
    }
    
    // Patterns for finding content URLs
    private static final Pattern CONTENT_URL_PATTERN = Pattern.compile("/(\\d{7,})-[^/]*\\.html");    private static final Pattern CATEGORY_PAGINATION_PATTERN = Pattern.compile("\\?page=(\\d+)");
    
    public FranceTvContentCrawler() {
        this(false);
    }
    
    public FranceTvContentCrawler(boolean testMode) {
        this.testMode = testMode;
        this.executorService = Executors.newFixedThreadPool(testMode ? 2 : MAX_THREADS);
        this.discoveredUrls = Collections.synchronizedSet(new HashSet<>());
        this.categories = new ConcurrentHashMap<>();
        this.categoryEpisodes = new ConcurrentHashMap<>();
    }
    
    /**
     * Perform comprehensive content discovery across all France.tv categories
     */
    public FranceTvArchive crawlAllContent() {
        logInfo("Starting comprehensive France.tv content discovery...");
        
        try {
            // Initialize categories
            initializeCategories();
              // Crawl content for each category concurrently
            List<Future<Void>> crawlTasks = new ArrayList<>();
            int categoryCount = 0;
            for (Map.Entry<String, String[]> entry : CATEGORY_DISCOVERY_URLS.entrySet()) {
                final String categoryName = entry.getKey();
                final String[] urls = entry.getValue();
                  // In test mode, limit number of categories
                if (testMode && categoryCount >= FranceTvConf.TEST_MODE_MAX_CATEGORIES) {
                    logInfo("Test mode: skipping remaining categories after " + categoryCount);
                    break;
                }
                
                Future<Void> task = executorService.submit(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        crawlCategoryContent(categoryName, urls);
                        return null;
                    }
                });
                crawlTasks.add(task);
                categoryCount++;
            }
              // Wait for all crawling tasks to complete
            for (Future<Void> task : crawlTasks) {
                try {
                    task.get(10, TimeUnit.MINUTES); // 10 minute timeout per category
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    logError("Category crawling task interrupted: " + e.getMessage());
                } catch (Exception e) {
                    logError("Category crawling task failed: " + e.getMessage());
                }
            }
            
            // Print discovery summary
            printDiscoverySummary();
            
        } finally {
            executorService.shutdown();
        }
        
        return new FranceTvArchive(categories.values(), categoryEpisodes);
    }
    
    /**
     * Initialize category structure
     */
    private void initializeCategories() {
        for (String categoryName : CATEGORY_DISCOVERY_URLS.keySet()) {
            String categoryId = categoryName.toLowerCase().replace(" ", "-").replace("&", "et");
            CategoryDTO category = new CategoryDTO(FranceTvConf.NAME, categoryName, categoryId, FranceTvConf.EXTENSION);
            category.setDownloadable(true);
            categories.put(categoryName, category);
            categoryEpisodes.put(categoryId, Collections.synchronizedList(new ArrayList<>()));
        }
    }
    
    /**
     * Crawl content for a specific category
     */
    private void crawlCategoryContent(String categoryName, String[] categoryUrls) {
        logInfo("Crawling category: " + categoryName);
        CategoryDTO category = categories.get(categoryName);
        Collection<EpisodeDTO> episodes = categoryEpisodes.get(category.getId());
        
        Set<String> processedUrls = new HashSet<>();
        
        for (String categoryUrl : categoryUrls) {            try {
                // Crawl main category page
                crawlCategoryPage(BASE_URL + categoryUrl, category, episodes, processedUrls, 0);
                
                // Look for pagination and crawl additional pages
                crawlPaginatedPages(BASE_URL + categoryUrl, category, episodes, processedUrls);
                
                // Check limits based on mode
                int maxEpisodes = testMode ? FranceTvConf.TEST_MODE_MAX_EPISODES_PER_CATEGORY : MAX_EPISODES_PER_CATEGORY;
                if (episodes.size() >= maxEpisodes) {
                    logInfo("Reached maximum episodes for category: " + categoryName);
                    break;
                }
                
            } catch (Exception e) {
                logError("Error crawling category " + categoryName + " URL " + categoryUrl + ": " + e.getMessage());
            }
        }
        
        logInfo("Completed crawling " + categoryName + ": found " + episodes.size() + " episodes");
    }
    
    /**
     * Crawl a specific category page for content
     */
    private void crawlCategoryPage(String url, CategoryDTO category, Collection<EpisodeDTO> episodes, 
                                 Set<String> processedUrls, int depth) {
        
        if (depth > MAX_DEPTH || processedUrls.contains(url)) {
            return;
        }
        
        processedUrls.add(url);
          try {
            int timeout = testMode ? FranceTvConf.TEST_MODE_TIMEOUT_MS : PAGE_TIMEOUT;
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(timeout)
                    .get();
            
            // Find all content links on this page
            Set<String> contentUrls = extractContentUrls(doc);
            
            // Create episodes from discovered content URLs
            int maxEpisodes = testMode ? FranceTvConf.TEST_MODE_MAX_EPISODES_PER_CATEGORY : MAX_EPISODES_PER_CATEGORY;
            for (String contentUrl : contentUrls) {
                if (!discoveredUrls.contains(contentUrl) && episodes.size() < maxEpisodes) {
                    discoveredUrls.add(contentUrl);
                    
                    String title = extractTitleFromUrl(contentUrl);
                    EpisodeDTO episode = new EpisodeDTO(category, title, contentUrl);
                    episodes.add(episode);
                }
            }
            
            // If we're not at max depth, follow sub-category links
            if (depth < MAX_DEPTH) {
                Elements subCategoryLinks = doc.select("a[href*='" + url.replace(BASE_URL, "") + "']");
                for (Element link : subCategoryLinks) {
                    String subUrl = link.absUrl("href");
                    if (!subUrl.equals(url) && subUrl.startsWith(BASE_URL)) {
                        crawlCategoryPage(subUrl, category, episodes, processedUrls, depth + 1);
                    }
                }
            }
            
        } catch (IOException e) {
            logError("Error fetching page " + url + ": " + e.getMessage());
        }
    }
    
    /**
     * Crawl paginated category pages
     */
    private void crawlPaginatedPages(String baseUrl, CategoryDTO category, Collection<EpisodeDTO> episodes, 
                                   Set<String> processedUrls) {
        try {
            Document doc = Jsoup.connect(baseUrl)
                    .userAgent(USER_AGENT)
                    .timeout(PAGE_TIMEOUT)
                    .get();
            
            // Look for pagination links
            Elements paginationLinks = doc.select("a[href*='?page='], a[href*='&page=']");
            Set<Integer> pageNumbers = new HashSet<>();
            
            for (Element link : paginationLinks) {
                String href = link.attr("href");
                Matcher matcher = CATEGORY_PAGINATION_PATTERN.matcher(href);
                if (matcher.find()) {
                    int pageNum = Integer.parseInt(matcher.group(1));
                    if (pageNum <= 20) { // Limit to first 20 pages
                        pageNumbers.add(pageNum);
                    }
                }
            }
            
            // Crawl each pagination page
            for (Integer pageNum : pageNumbers) {
                String pageUrl = baseUrl + (baseUrl.contains("?") ? "&" : "?") + "page=" + pageNum;
                crawlCategoryPage(pageUrl, category, episodes, processedUrls, 0);
                
                if (episodes.size() >= MAX_EPISODES_PER_CATEGORY) {
                    break;
                }
            }
            
        } catch (IOException e) {
            logError("Error crawling paginated pages for " + baseUrl + ": " + e.getMessage());
        }
    }
    
    /**
     * Extract all content URLs from a page
     */
    private Set<String> extractContentUrls(Document doc) {
        Set<String> contentUrls = new HashSet<>();
        
        // Look for links that match France.tv content URL patterns
        Elements allLinks = doc.select("a[href]");
          for (Element link : allLinks) {
            String absoluteUrl = link.absUrl("href");
            
            // Check if this looks like a France.tv content URL
            if (isContentUrl(absoluteUrl)) {
                contentUrls.add(absoluteUrl);
            }
        }
        
        return contentUrls;
    }
    
    /**
     * Check if a URL is a France.tv content URL
     */
    private boolean isContentUrl(String url) {
        if (url == null || !url.startsWith(BASE_URL)) {
            return false;
        }
        
        // Must end with .html and contain a numeric ID
        return CONTENT_URL_PATTERN.matcher(url).find();
    }
    
    /**
     * Extract title from France.tv URL
     */
    private String extractTitleFromUrl(String url) {
        if (url == null) return "Unknown Content";
        
        String[] parts = url.split("/");
        if (parts.length > 0) {
            String filename = parts[parts.length - 1];
            if (filename.endsWith(".html")) {
                String title = filename.substring(0, filename.length() - 5);
                
                // Remove numeric ID prefix (e.g., "7267013-title" -> "title")
                if (title.matches("\\d+-.*")) {
                    title = title.replaceFirst("\\d+-", "");
                }
                
                // Convert to readable format
                title = title.replace("-", " ");
                return capitalizeWords(title);
            }
        }
        
        return "France.tv Content";
    }
    
    /**
     * Capitalize words in a title
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
     * Print summary of content discovery
     */
    private void printDiscoverySummary() {        logInfo("\n=== FRANCE.TV CONTENT DISCOVERY SUMMARY ===");
        logInfo("Total unique content URLs discovered: " + discoveredUrls.size());
        
        int totalEpisodes = 0;
        for (Map.Entry<String, Collection<EpisodeDTO>> entry : categoryEpisodes.entrySet()) {
            String categoryId = entry.getKey();
            int episodeCount = entry.getValue().size();
            totalEpisodes += episodeCount;
          // Find category name
        String categoryName = null;
        for (CategoryDTO cat : categories.values()) {
            if (cat.getId().equals(categoryId)) {
                categoryName = cat.getName();
                break;
            }
        }
        if (categoryName == null) {
            categoryName = categoryId;
        }
            
            logInfo(categoryName + ": " + episodeCount + " episodes");
        }
          logInfo("Total episodes across all categories: " + totalEpisodes);
        logInfo("Categories discovered: " + categories.size());
        logInfo("=== DISCOVERY COMPLETE ===\n");
    }
    
    /**
     * Get statistics about discovered content
     */
    public Map<String, Integer> getDiscoveryStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalUrls", discoveredUrls.size());
        stats.put("totalCategories", categories.size());
          int totalEpisodes = 0;
        for (Collection<EpisodeDTO> episodes : categoryEpisodes.values()) {
            totalEpisodes += episodes.size();
        }
        stats.put("totalEpisodes", totalEpisodes);
        
        return stats;
    }
    
    // Helper methods for controlled logging
    private static void logInfo(String message) {
        if (DEBUG) {
            System.out.println("[FranceTvCrawler] " + message);
        }
    }
    
    private static void logError(String message) {
        if (DEBUG) {
            System.err.println("[FranceTvCrawler ERROR] " + message);
        }
    }
}
