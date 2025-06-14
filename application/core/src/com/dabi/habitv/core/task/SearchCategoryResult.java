package com.dabi.habitv.core.task;

import java.util.Collections;
import java.util.Set;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;

/**
 * Result of a category search operation.
 * Contains the search results for categories in a specific channel.
 */
public class SearchCategoryResult {

    /** The channel name. */
    private final String channel;

    /** The list of categories found. */
    private final Set<CategoryDTO> categoryList;

    /** Whether the search was successful. */
    private final boolean success;

    /**
     * Constructs a successful search result.
     * 
     * @param channelName the channel name
     * @param categories the list of categories found
     */
    public SearchCategoryResult(final String channelName,
            final Set<CategoryDTO> categories) {
        this.channel = channelName;
        this.categoryList = categories;
        this.success = true;
    }

    /**
     * Constructs a failed search result.
     * 
     * @param channelName the channel name
     */
    public SearchCategoryResult(final String channelName) {
        this.channel = channelName;
        this.categoryList = Collections.emptySet();
        this.success = false;
    }

    /**
     * Gets the channel name.
     * 
     * @return the channel name
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Gets the list of categories found.
     * 
     * @return the list of categories
     */
    public Set<CategoryDTO> getCategoryList() {
        return categoryList;
    }

    /**
     * Checks if the search was successful.
     * 
     * @return true if the search was successful
     */
    public boolean isSuccess() {
        return success;
    }
} 