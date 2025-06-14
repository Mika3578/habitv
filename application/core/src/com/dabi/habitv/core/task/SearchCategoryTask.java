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
	private final Publisher<SearchCategoryEvent> searchCategoryPublisher;

	/**
	 * Constructs a new SearchCategoryTask.
	 * 
	 * @param channelName the channel to search
	 * @param providerInstance the plugin provider
	 * @param publisher the publisher for events
	 */
	public SearchCategoryTask(final String channelName,
			final PluginProviderInterface providerInstance,
			final Publisher<SearchCategoryEvent> publisher) {
		this.channel = channelName;
		this.provider = providerInstance;
		this.searchCategoryPublisher = publisher;
	}

	@Override
	protected void adding() {
		LOG.error("Waiting for Grabbing categories for " + channel);
		searchCategoryPublisher.addNews(new SearchCategoryEvent(channel,
				SearchCategoryStateEnum.CHANNEL_CATEGORIES_TO_BUILD));
	}

	@Override
	protected void failed(final Throwable e) {
		LOG.error("Grabbing categories for " + channel + " failed", e);
		searchCategoryPublisher.addNews(new SearchCategoryEvent(channel,
				SearchCategoryStateEnum.ERROR));
	}

	@Override
	protected void ended() {
		LOG.info("Grabbing categories for " + channel + " done");
		searchCategoryPublisher.addNews(new SearchCategoryEvent(channel,
				SearchCategoryStateEnum.CATEGORIES_BUILT));
	}

	@Override
	protected void started() {
		LOG.info("Grabbing categories for " + channel + "...");
		searchCategoryPublisher.addNews(new SearchCategoryEvent(channel,
				SearchCategoryStateEnum.BUILDING_CATEGORIES));
	}

	@Override
	protected SearchCategoryResult doCall() {
		try {
			return new SearchCategoryResult(channel, provider.findCategory());
		} catch (Exception e) {
			LOG.error("", e);
			return new SearchCategoryResult(channel);
		}
	}

	@Override
	public String toString() {
		return "SearchingCategory" + channel;
	}

	@Override
	protected void canceled() {
		LOG.info("Canceled Grabbing categories for " + channel + ".");
	}

}
