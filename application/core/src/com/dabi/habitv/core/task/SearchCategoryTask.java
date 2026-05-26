package com.dabi.habitv.core.task;

import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.pub.Publisher;
import com.dabi.habitv.core.event.SearchCategoryEvent;
import com.dabi.habitv.core.event.SearchCategoryStateEnum;
import com.dabi.habitv.framework.plugin.utils.CategoryTreeNormalizer;

public final class SearchCategoryTask extends
		AbstractTask<SearchCategoryResult> {

	private final String channel;

	private final PluginProviderInterface provider;

	private final Publisher<SearchCategoryEvent> searchCategoryPublisher;

	public SearchCategoryTask(final String channel,
			final PluginProviderInterface provider,
			final Publisher<SearchCategoryEvent> searchCategoryPublisher) {
		this.channel = channel;
		this.provider = provider;
		this.searchCategoryPublisher = searchCategoryPublisher;
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
			return new SearchCategoryResult(channel,
					CategoryTreeNormalizer.normalize(channel, provider.findCategory()));
		} catch (Exception e) {
			LOG.error("Failed to build categories for channel " + channel, e);
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
