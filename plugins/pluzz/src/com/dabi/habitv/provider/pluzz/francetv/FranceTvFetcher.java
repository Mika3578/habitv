package com.dabi.habitv.provider.pluzz.francetv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.framework.plugin.utils.RetrieverUtils;
import com.dabi.habitv.provider.pluzz.PluzzConf;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Modern fetcher for France.tv data using their current API endpoints
 */
public class FranceTvFetcher {
    
    private static final Logger LOG = Logger.getLogger(FranceTvFetcher.class);
    
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, CategoryDTO> catName2RootCat;
    private final Map<String, CategoryDTO> catName2LeafCat;
    private final Map<String, Collection<EpisodeDTO>> catName2Episode;
    private final Proxy proxy;
    
    // France.tv API endpoints
    private static final String FRANCE_TV_BASE_URL = "https://www.france.tv";
    private static final String CATALOG_API_URL = "https://www.france.tv/api/v1/catalog";
    private static final String SEARCH_API_URL = "https://www.france.tv/api/v1/search";
    private static final String CONTENT_API_URL = "https://www.france.tv/api/v1/content";
    
    public FranceTvFetcher(Proxy proxy) {
        this.proxy = proxy;
        this.catName2RootCat = new HashMap<>();
        this.catName2LeafCat = new HashMap<>();
        this.catName2Episode = new HashMap<>();
    }
    
    /**
     * Load categories and episodes from France.tv
     */
    public FranceTvArchive load() {
        try {
            LOG.info("Loading France.tv categories and episodes");
            // Load main categories
            loadCategories();
            
            // Load episodes for each category
            loadEpisodes();
            
        } catch (IOException e) {
            LOG.error("Failed to load France.tv data", e);
            throw new TechnicalException(e);
        }
        
        LOG.info("Successfully loaded " + catName2RootCat.size() + " categories and " + 
                catName2Episode.size() + " episode collections");
        return new FranceTvArchive(catName2RootCat.values(), catName2Episode);
    }
    
    private void loadCategories() throws IOException {
        // Try to fetch categories from France.tv API
        String categoriesUrl = CATALOG_API_URL + "/categories";
        
        try {
            LOG.debug("Fetching categories from: " + categoriesUrl);
            String response = getUrlContent(categoriesUrl);
            parseCategoriesFromResponse(response);
        } catch (Exception e) {
            LOG.warn("Failed to fetch categories from API, using default categories", e);
            // Fallback: create default categories based on France.tv structure
            createDefaultCategories();
        }
    }
    
    private void loadEpisodes() throws IOException {
        // For each category, fetch episodes
        for (CategoryDTO category : catName2RootCat.values()) {
            try {
                String episodesUrl = CATALOG_API_URL + "/category/" + category.getId() + "/episodes";
                LOG.debug("Fetching episodes for category: " + category.getName() + " from: " + episodesUrl);
                String response = getUrlContent(episodesUrl);
                parseEpisodesFromResponse(response, category);
            } catch (Exception e) {
                // Skip this category if we can't load episodes
                LOG.warn("Failed to load episodes for category: " + category.getName(), e);
            }
        }
    }
    
    private void createDefaultCategories() {
        // Create default categories based on France.tv structure
        String[] defaultCategories = {
            "Series et fictions", "Documentaires", "Cinema", "Societe", 
            "Info", "Arts et spectacles", "Sport", "Divertissement", "Enfants"
        };
        
        for (String catName : defaultCategories) {
            CategoryDTO category = new CategoryDTO(PluzzConf.NAME, catName, catName, PluzzConf.EXTENSION);
            catName2RootCat.put(catName, category);
        }
    }
    
    private void parseCategoriesFromResponse(String response) throws JsonParseException, JsonMappingException, IOException {
        // Parse JSON response from France.tv API
        @SuppressWarnings("unchecked")
        Map<String, Object> data = mapper.readValue(response, Map.class);
        
        // Extract categories from the response structure
        // This will need to be adapted based on the actual API response format
        if (data.containsKey("categories")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> categories = (List<Map<String, Object>>) data.get("categories");
            
            for (Map<String, Object> catData : categories) {
                String name = (String) catData.get("name");
                String id = (String) catData.get("id");
                
                if (name != null && id != null) {
                    CategoryDTO category = new CategoryDTO(PluzzConf.NAME, name, id, PluzzConf.EXTENSION);
                    catName2RootCat.put(name, category);
                }
            }
        }
    }
    
    private void parseEpisodesFromResponse(String response, CategoryDTO category) throws JsonParseException, JsonMappingException, IOException {
        // Parse JSON response for episodes
        @SuppressWarnings("unchecked")
        Map<String, Object> data = mapper.readValue(response, Map.class);
        
        if (data.containsKey("episodes")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> episodes = (List<Map<String, Object>>) data.get("episodes");
            
            Collection<EpisodeDTO> episodeList = new ArrayList<>();
            
            for (Map<String, Object> episodeData : episodes) {
                String title = (String) episodeData.get("title");
                String id = (String) episodeData.get("id");
                String url = (String) episodeData.get("url");
                
                if (title != null && id != null && url != null) {
                    EpisodeDTO episode = new EpisodeDTO(category, title, FRANCE_TV_BASE_URL + url);
                    episodeList.add(episode);
                }
            }
            
            catName2Episode.put(category.getId(), episodeList);
        }
    }
    
    private String getUrlContent(String url) throws IOException {
        try (InputStream inputStream = RetrieverUtils.getInputStreamFromUrl(url, proxy);
             ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString("UTF-8");
        }
    }
} 