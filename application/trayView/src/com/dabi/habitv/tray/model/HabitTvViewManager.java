package com.dabi.habitv.tray.model;

import java.util.Collection;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.core.config.UserConfig;
import com.dabi.habitv.core.config.XMLUserConfig;
import com.dabi.habitv.core.dao.GrabConfigDAO;
import com.dabi.habitv.core.dao.GrabConfigDAO.LoadModeEnum;
import com.dabi.habitv.core.event.SearchEvent;
import com.dabi.habitv.core.event.SearchStateEnum;
import com.dabi.habitv.core.mgr.CoreManager;
import com.dabi.habitv.core.task.BatchEnqueueResult;
import com.dabi.habitv.tray.subscriber.CoreSubscriber;
import com.dabi.habitv.tray.subscriber.SubscriberAdapter;
import com.dabi.habitv.tray.subscriber.UpdateSubscriber;
import com.dabi.habitv.tray.subscriber.UpdateSubscriberAdapter;
import com.dabi.habitv.utils.DirUtils;

public class HabitTvViewManager extends Observable {

	private static final Logger LOG = Logger
			.getLogger(HabitTvViewManager.class);

	private final CoreManager coreManager;

	private final UserConfig userConfig;

	private final ProgressionModel progressionModel;

	private Thread demonThread;

	private final GrabConfigDAO grabConfigDAO;

	public HabitTvViewManager() {
		this(XMLUserConfig.initConfig());
	}

	public HabitTvViewManager(UserConfig userConfig) {
		super();
		this.userConfig = userConfig;
		grabConfigDAO = new GrabConfigDAO(DirUtils.getGrabConfigPath());
		coreManager = new CoreManager(userConfig);
		progressionModel = new ProgressionModel();
	}

	public void attach(final CoreSubscriber coreSubscriber) {
		final SubscriberAdapter subscriberAdapter = new SubscriberAdapter(
				coreSubscriber);
		coreManager.getCategoryManager().getSearchCategoryPublisher()
				.attach(subscriberAdapter.buildSearchCategorySubscriber());
		coreManager.getEpisodeManager().getRetreivePublisher()
				.attach(subscriberAdapter.buildRetreiveSubscriber());
		coreManager.getEpisodeManager().getSearchPublisher()
				.attach(subscriberAdapter.buildSearchSubscriber());
		coreManager.getPluginManager().getUpdatePluginPublisher()
				.attach(subscriberAdapter.buildUpdateSubscriber());
		coreManager.getPluginManager().getUpdatablePluginPublisher()
				.attach(subscriberAdapter.buildUpdatablePluginSubscriber());
	}

	public void attach(final UpdateSubscriber updateSubscriber) {
		final UpdateSubscriberAdapter subscriberAdapter = new UpdateSubscriberAdapter(
				updateSubscriber);
		coreManager.getPluginManager().getUpdatePluginPublisher()
				.attach(subscriberAdapter.buildUpdateSubscriber());
		coreManager.getPluginManager().getUpdatablePluginPublisher()
				.attach(subscriberAdapter.buildUpdatablePluginSubscriber());
	}

	public ProgressionModel getProgressionModel() {
		return progressionModel;
	}

	public void startDownloadCheckDemon() {

		demonThread = new Thread() {
			@Override
			public void run() {
				boolean interrupted = false;
				final long confDemonTime;
				confDemonTime = userConfig.getDemonCheckTime();
				final long demonTime = confDemonTime * 1000L;
				boolean still = true;
				// demon mode
				while (still) {
					if (interrupted) {
						interrupted = false;
					} else {
						try {
							if (grabConfigDAO.exist()) {
								coreManager.retreiveEpisode(grabConfigDAO
										.load());
							} else {
								grabConfigDAO.saveGrabConfig(findCategories());
							}
						} catch (final Exception e) {
							LOG.error("", e);
							coreManager
									.getEpisodeManager()
									.getSearchPublisher()
									.addNews(
											new SearchEvent(
													SearchStateEnum.ERROR, e));
							still = false;
						}
					}
					if (still) {
						try {
							Thread.sleep(demonTime);
						} catch (final InterruptedException e) {
							// may have been interrupted by a manually start
							interrupted = true;
						}
					}
				}
			}

		};

		demonThread.start();
	}

	public void startDownloadCheck() {

		demonThread.interrupt();
		(new Thread() {
			@Override
			public void run() {
				coreManager.retreiveEpisode(grabConfigDAO.load());
			}

		}).start();
	}

	public void forceEnd() {
		coreManager.forceEnd();
	}

	public void clear() {
		progressionModel.clear();
	}

	public UserConfig getUserConfig() {
		return userConfig;
	}

	public void updateGrabConfig() {
		grabConfigDAO.updateGrabConfig(findCategories());
	}

	public Map<String, CategoryDTO> findCategories() {
		return coreManager.findCategory();
	}

	public void reDoExport() {
		coreManager.reTryExport();
	}

	public boolean hasExportToResume() {
		return coreManager.hasExportToResume();
	}

	public void clearExport() {
		coreManager.clearExport();
	}

	public void update() {
		coreManager.update();
	}

	public void cleanCategories() {
		grabConfigDAO.clean();
	}

	public Map<String, CategoryDTO> loadCategories() {
		buildGrabConfigIfNeeded();
		return grabConfigDAO.load(LoadModeEnum.ALL);
	}

	public void buildGrabConfigIfNeeded() {
		if (!grabConfigDAO.exist()) {
			grabConfigDAO.saveGrabConfig(coreManager.findCategory());
		}
	}

	public void updateGrabconfig(Map<String, CategoryDTO> channel2Categories) {
		grabConfigDAO.updateGrabConfig(channel2Categories);
	}

	public void saveGrabConfig(Map<String, CategoryDTO> channel2Categories) {
		grabConfigDAO.saveGrabConfig(channel2Categories);
	}

	public void saveConfig(UserConfig userConfig) {
		try {
			XMLUserConfig.saveConfig(userConfig);
		} catch (JAXBException e) {
			throw new TechnicalException(e);
		}
	}

	public void setDownloaded(EpisodeDTO episode) {
		coreManager.setDownloaded(episode);
	}

	public void restart(EpisodeDTO episode, boolean exportOnly) {
		coreManager.restart(episode, exportOnly);
	}

	public BatchEnqueueResult enqueueEpisodesForDownload(
			final Collection<EpisodeDTO> episodes) {
		return coreManager.enqueueEpisodesForDownload(episodes);
	}

	public Collection<EpisodeDTO> findEpisodeByCategory(CategoryDTO category) {
		return coreManager.findEpisodeByCategory(category);
	}

	public Set<String> findDownloadedEpisodes(CategoryDTO category) {
		return coreManager.findDownloadedEpisodes(category);
	}

	public void cancel(EpisodeDTO episode) {
		coreManager.cancel(episode);
	}

}
