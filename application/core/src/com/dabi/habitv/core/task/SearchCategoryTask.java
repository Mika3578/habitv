package com.dabi.habitv.core.task;

import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.pub.Publisher;
import com.dabi.habitv.core.event.SearchCategoryEvent;
import com.dabi.habitv.core.event.SearchCategoryStateEnum;

/**
 * Task for searching categories in a channel.
 * Handles the category search process for a specific channel.
 */
public final class SearchCategoryTask extends
        AbstractTask<SearchCategoryResult> {

    /** The channel to search categories for. */
    private final String channel;

    /** The plugin provider interface. */
    private final PluginProviderInterface provider;

    /** Publisher for search category events. */
    private final Publisher<SearchCategoryEvent> publisher;

    /**
     * Constructs a new SearchCategoryTask.
     * 
     * @param channelName the channel to search categories for
     * @param providerInstance the plugin provider
     * @param publisherInstance the publisher for events
     */
    public SearchCategoryTask(final String channelName,
            final PluginProviderInterface providerInstance,
            final Publisher<SearchCategoryEvent> publisherInstance) {
        this.channel = channelName;
        this.provider = providerInstance;
        this.publisher = publisherInstance;
    }

    @Override
    protected void adding() {
        LOG.info("Waiting for search category of " + channel);
    }

    @Override
    protected void failed(final Throwable e) {
        LOG.error("Search category failed for " + channel, e);
        publisher.addNews(new SearchCategoryEvent(channel,
                SearchCategoryStateEnum.ERROR));
    }

    @Override
    protected void ended() {
        LOG.info("Search category of " + channel + " done");
        publisher.addNews(new SearchCategoryEvent(channel,
                SearchCategoryStateEnum.DONE));
    }

    @Override
    protected void started() {
        LOG.info("Search category of " + channel + " is starting");
        publisher.addNews(new SearchCategoryEvent(channel,
                SearchCategoryStateEnum.STARTING));
    }

    @Override
    protected void canceled() {
        LOG.info("Cancel of search category " + channel + " done");
        publisher.addNews(new SearchCategoryEvent(channel,
                SearchCategoryStateEnum.IDLE));
    }

    @Override
    protected SearchCategoryResult doCall() throws Exception {
        try {
            final java.util.Set<com.dabi.habitv.api.plugin.dto.CategoryDTO> categories = provider
                    .findCategory();
            return new SearchCategoryResult(channel, categories);
        } catch (final Exception e) {
            LOG.error("Error searching categories for channel " + channel, e);
            return new SearchCategoryResult(channel);
        }
    }

    @Override
    public String toString() {
        return "SC" + channel + " "
                + (provider == null ? "no provider" : provider.getName());
    }
} 