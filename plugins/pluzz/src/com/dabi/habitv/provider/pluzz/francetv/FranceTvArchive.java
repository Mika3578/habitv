package com.dabi.habitv.provider.pluzz.francetv;

import java.util.Collection;
import java.util.Map;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

/**
 * Archive class for France.tv data containing categories and episodes
 */
public class FranceTvArchive {
    
    private Collection<CategoryDTO> categories;
    
    private Map<String, Collection<EpisodeDTO>> catName2Episode;

    public FranceTvArchive(Collection<CategoryDTO> categories, Map<String, Collection<EpisodeDTO>> catName2Episode) {
        super();
        this.categories = categories;
        this.catName2Episode = catName2Episode;
    }

    public Collection<CategoryDTO> getCategories() {
        return categories;
    }

    public void setCategories(Collection<CategoryDTO> categories) {
        this.categories = categories;
    }

    public Map<String, Collection<EpisodeDTO>> getCatName2Episode() {
        return catName2Episode;
    }

    public void setCatName2Episode(Map<String, Collection<EpisodeDTO>> catName2Episode) {
        this.catName2Episode = catName2Episode;
    }
} 