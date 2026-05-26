package com.dabi.habitv.core.task;

import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ExporterPluginHolder;
import com.dabi.habitv.api.plugin.pub.Publisher;
import com.dabi.habitv.core.dao.DlErrorDAO;
import com.dabi.habitv.core.dao.DownloadedDAO;
import com.dabi.habitv.core.event.RetreiveEvent;
import com.dabi.habitv.core.event.SearchEvent;
import com.dabi.habitv.core.event.SearchStateEnum;
import com.dabi.habitv.utils.FilterUtils;

public class SearchTask extends AbstractTask<Object> {

	private final PluginProviderInterface provider;

	private final Set<CategoryDTO> categoryDTOs;

	private final TaskAdder taskAdder;

	private final Publisher<SearchEvent> searchPublisher;

	private final Publisher<RetreiveEvent> retreivePublisher;

	private final DownloaderPluginHolder downloader;

	private final ExporterPluginHolder exporter;

	private static final Logger LOG = Logger.getLogger(SearchTask.class);

	public SearchTask(final PluginProviderInterface provider, final Set<CategoryDTO> categoryDTOs, final TaskAdder taskAdder,
			final Publisher<SearchEvent> searchPublisher, final Publisher<RetreiveEvent> retreivePublisher,
			final DownloaderPluginHolder downloader, final ExporterPluginHolder exporter) {
		this.provider = provider;
		this.categoryDTOs = categoryDTOs;
		this.taskAdder = taskAdder;
		this.searchPublisher = searchPublisher;
		this.retreivePublisher = retreivePublisher;
		this.downloader = downloader;
		this.exporter = exporter;
	}

	@Override
	protected void adding() {
		LOG.info("Waiting for Searching episode for " + getCategory());
	}

	@Override
	protected void failed(final Throwable e) {
		LOG.error("Searching episode for " + provider.getName() + " failed", e);
		searchPublisher.addNews(new SearchEvent(provider.getName(), SearchStateEnum.ERROR));
	}

	@Override
	protected void ended() {
		LOG.info("Searching episode for " + provider.getName() + " done");
		searchPublisher.addNews(new SearchEvent(provider.getName(), SearchStateEnum.DONE));
	}

	@Override
	protected void started() {
		LOG.info("Searching episode for " + provider.getName() + " is starting");
		searchPublisher.addNews(new SearchEvent(provider.getName(), SearchStateEnum.CHECKING_EPISODES));
	}

	@Override
	protected Object doCall() {
		findEpisodeByCategories(categoryDTOs);
		return null;
	}

	private void findEpisodeByCategories(final Collection<CategoryDTO> categoryDTOs) {
		for (final CategoryDTO category : categoryDTOs) {
			try {
				findEpisodesByCategory(category);
			} catch (Exception e) {
				searchPublisher.addNews(new SearchEvent(provider.getName(), category.getName(), SearchStateEnum.ERROR, e));
			}
		}
	}

	private void findEpisodesByCategory(final CategoryDTO category) {
		if (category.isDownloadable() && category.isSelected()) {
			// dao to find dowloaded episodes
			final DownloadedDAO dlDAO = buildDownloadDAO(category);
			final Set<String> dlFiles = dlDAO.findDownloadedFiles();
			final boolean indexCreated = dlDAO.isIndexCreated();
			// dao to find error download episodes
			final DlErrorDAO errorDAO = new DlErrorDAO();
			final Set<String> errorFiles = errorDAO.findDownloadedErrorFiles();

			// get list of downloadable episodes
			final Set<EpisodeDTO> episodeList = provider.findEpisode(category);
			if (!indexCreated && !episodeList.isEmpty()) {
				LOG.info("Creating index for " + category.getName());
				searchPublisher.addNews(new SearchEvent(provider.getName(), category.getName(), SearchStateEnum.BUILD_INDEX));
			}
			// reinit index to purge non available files from index
			// dlDAO.initIndex(); provider may re add an old dl file, so
			// we
			// shoudl'nt reinit index
			// filter episode lister by include/exclude and already
			// downloaded
			boolean isDownloaded;
			boolean isErrorDownloaded;
			int i = 0;
			for (final EpisodeDTO episode : episodeList) {
				episode.setNum(i);
				isDownloaded = DownloadedDAO.containsEpisodeOrLegacyName(dlFiles,
						episode);
				isErrorDownloaded = errorFiles.contains(episode.getFullNameNoNum());
				if (indexCreated && FilterUtils.filterByIncludeExcludeAndDownloaded(episode, category.getInclude(), category.getExclude())
						&& !isDownloaded && !isErrorDownloaded) {
					// producer download the file
					taskAdder.addRetreiveTask(new RetrieveTask(episode, retreivePublisher, taskAdder, exporter, provider, downloader,
							dlDAO, false));

				} else {
					// if index has not been created the first run will
					// only
					// fill this file
					if (!isDownloaded && !isErrorDownloaded) {
						dlDAO.addDownloadedFiles(false, episode);
					}
				}
				i++;
			}
		} else {
			LOG.info("La catégorie n'est pas téléchargeable : " + category);
		}

		if (!category.getSubCategories().isEmpty()) {
			findEpisodeByCategories(category.getSubCategories());
		}

	}

	protected DownloadedDAO buildDownloadDAO(final CategoryDTO category) {
		return new DownloadedDAO(category, downloader.getIndexDir());
	}

	@Override
	public String toString() {
		return "Searching" + provider.getName();
	}

	@Override
	protected void canceled() {
		LOG.info("Cancel episode search for " + provider.getName() + ".");
	}
}
