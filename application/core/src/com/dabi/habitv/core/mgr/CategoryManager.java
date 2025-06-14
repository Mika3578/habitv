package com.dabi.habitv.core.mgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.StatusEnum;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.api.plugin.holder.ProviderPluginHolder;
import com.dabi.habitv.api.plugin.pub.Publisher;
import com.dabi.habitv.core.config.HabitTvConf;
import com.dabi.habitv.core.event.SearchCategoryEvent;
import com.dabi.habitv.core.event.SearchCategoryStateEnum;
import com.dabi.habitv.core.task.SearchCategoryResult;
import com.dabi.habitv.core.task.SearchCategoryTask;
import com.dabi.habitv.core.task.TaskMgr;
import com.dabi.habitv.core.task.TaskMgrListener;
import com.dabi.habitv.core.task.TaskTypeEnum;
import com.dabi.habitv.utils.DirUtils;

public class CategoryManager extends AbstractManager {

	private final TaskMgr<SearchCategoryTask, SearchCategoryResult> searchCategoryMgr;

	private final Publisher<SearchCategoryEvent> searchCategoryPublisher;

	private static final Logger LOG = Logger.getLogger(CategoryManager.class);

	CategoryManager(final ProviderPluginHolder providerPluginHolder,
			final Map<String, Integer> taskName2PoolSize) {
		super(providerPluginHolder);
		// task mgrs
		searchCategoryMgr = new TaskMgr<SearchCategoryTask, SearchCategoryResult>(
				TaskTypeEnum.CATEGORY.getPoolSize(),
				buildCategoryTaskMgrListener(), null);
		// publisher
		searchCategoryPublisher = new Publisher<>();
	}

	public Map<String, CategoryDTO> findCategory() {
		return findCategory(null);
	}

	public Map<String, CategoryDTO> findCategory(List<String> pluginList) {
		final Map<String, CategoryDTO> channel2Categories = new HashMap<>();
		final List<SearchCategoryTask> taskList = new ArrayList<>();
		// search is parallelized, the final result will be build with the
		// future result
		Collection<PluginProviderInterface> providerPlugins = getProviderPluginHolder()
				.getPlugins();
		searchCategoryPublisher.addNews(new SearchCategoryEvent(
				SearchCategoryStateEnum.STARTING, String
						.valueOf(providerPlugins.size())));
		for (final PluginProviderInterface provider : providerPlugins) {
			if (pluginList == null || pluginList.contains(provider.getName())) {
				final SearchCategoryTask searchCategoryTask = new SearchCategoryTask(
						provider.getName(), provider, searchCategoryPublisher);
				searchCategoryMgr.addTask(provider, searchCategoryTask);
				taskList.add(searchCategoryTask);
			}
		}
		SearchCategoryResult searchCategoryResult;
		for (final SearchCategoryTask searchTask : taskList) {
			try {
				searchCategoryResult = searchTask.getResult();

				CategoryDTO categoryPlugin = new CategoryDTO(
						searchCategoryResult.getChannel(),
						searchCategoryResult.getCategoryList());
				categoryPlugin.setState(searchCategoryResult.isSuccess() ? null
						: StatusEnum.FAILED);

				channel2Categories.put(categoryPlugin.getId(), categoryPlugin);
			} catch (final TechnicalException e) {
				searchCategoryPublisher.addNews(new SearchCategoryEvent(
						SearchCategoryStateEnum.ERROR,
						HabitTvConf.GRABCONFIG_XML_FILE));
				// throw new TechnicalException(e);
				// if one plugin failed keep generating the grabconfig file
				LOG.error(
						"one plugin failed, keep generating the grabconfig file",
						e);
			}
		}
		searchCategoryPublisher.addNews(new SearchCategoryEvent(
				SearchCategoryStateEnum.ALL_DONE));
		return channel2Categories;
	}

	private TaskMgrListener buildCategoryTaskMgrListener() {
		return new TaskMgrListener() {

			@Override
			public void onFailed(final Throwable throwable) {
				searchCategoryPublisher.addNews(new SearchCategoryEvent(
						SearchCategoryStateEnum.ERROR, DirUtils
								.getGrabConfigPath()));
			}

			@Override
			public void onAllTreatmentDone() {
				searchCategoryPublisher.addNews(new SearchCategoryEvent(
						SearchCategoryStateEnum.DONE, DirUtils
								.getGrabConfigPath()));
			}
		};
	}

	public Publisher<SearchCategoryEvent> getSearchCategoryPublisher() {
		return searchCategoryPublisher;
	}

	void forceEnd() {
		searchCategoryMgr.shutdownNow();
	}

}
