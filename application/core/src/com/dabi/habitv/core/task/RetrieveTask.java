package com.dabi.habitv.core.task;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.dabi.habitv.api.plugin.api.PluginExporterInterface;
import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.ExportDTO;
import com.dabi.habitv.api.plugin.exception.InvalidEpisodeException;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ExporterPluginHolder;
import com.dabi.habitv.api.plugin.pub.Publisher;
import com.dabi.habitv.core.config.HabitTvConf;
import com.dabi.habitv.core.dao.DownloadedDAO;
import com.dabi.habitv.core.dao.EpisodeExportState;
import com.dabi.habitv.core.event.EpisodeStateEnum;
import com.dabi.habitv.core.event.RetreiveEvent;
import com.dabi.habitv.core.token.TokenReplacer;

/**
 * Task for retrieving episodes.
 * Handles the complete retrieval process including download and export.
 */
public class RetrieveTask extends AbstractEpisodeTask {

    /** Publisher for retrieve events. */
    private final Publisher<RetreiveEvent> retreivePublisher;

    /** Task adder for managing sub-tasks. */
    private final TaskAdder taskAdder;

    /** Exporter plugin holder. */
    private final ExporterPluginHolder exporter;

    /** Plugin provider interface. */
    private final PluginProviderInterface provider;

    /** Downloader plugin holder. */
    private final DownloaderPluginHolder downloaders;

    /** Data access object for downloaded episodes. */
    private final DownloadedDAO downloadDAO;

    /** Export state for episode resumption. */
    private EpisodeExportState episodeExportState;

    /** Whether this is a manual retrieval. */
    private final boolean manual;

    /**
     * Constructs a new RetrieveTask.
     * 
     * @param episode the episode to retrieve
     * @param publisher the publisher for events
     * @param taskAdderInstance the task adder
     * @param exporterHolder the exporter plugin holder
     * @param providerInstance the plugin provider
     * @param downloaderHolder the downloader plugin holder
     * @param downloadDAOInstance the download DAO
     * @param isManual whether this is a manual retrieval
     */
    public RetrieveTask(final EpisodeDTO episode,
            final Publisher<RetreiveEvent> publisher,
            final TaskAdder taskAdderInstance,
            final ExporterPluginHolder exporterHolder,
            final PluginProviderInterface providerInstance,
            final DownloaderPluginHolder downloaderHolder,
            final DownloadedDAO downloadDAOInstance,
            final boolean isManual) {
        super(episode);
        this.retreivePublisher = publisher;
        this.taskAdder = taskAdderInstance;
        this.exporter = exporterHolder;
        this.provider = providerInstance;
        this.downloadDAO = downloadDAOInstance;
        this.downloaders = downloaderHolder;
        this.episodeExportState = null;
        this.manual = isManual;
    }

    @Override
    protected void adding() {
        LOG.info("Episode to retreive " + getEpisode());
        retreivePublisher.addNews(new RetreiveEvent(getEpisode(),
                EpisodeStateEnum.TO_DOWNLOAD));
    }

    @Override
    protected void failed(final Throwable e) {
        LOG.error("Episode failed to retreive " + getEpisode(), e);
    }

    @Override
    protected void ended() {
        LOG.error("Episode is ready " + getEpisode());
        retreivePublisher.addNews(new RetreiveEvent(getEpisode(),
                EpisodeStateEnum.READY));
    }

    @Override
    protected void started() {
        // No specific action needed on start
    }

    @Override
    protected Object doCall() throws InterruptedException, ExecutionException {
        if (!exportOnly()) {
            check();
            download();
        }
        export(exporter.getExporterList());
        return null;
    }

    /**
     * Checks if this is an export-only task.
     * 
     * @return true if this is an export-only task
     */
    private boolean exportOnly() {
        return episodeExportState != null;
    }

    /**
     * Exports the episode using the provided exporter list.
     * 
     * @param exporterList the list of exporters to use
     * @throws InterruptedException if interrupted during execution
     * @throws ExecutionException if execution fails
     */
    private void export(final List<ExportDTO> exporterList)
            throws InterruptedException, ExecutionException {
        int i = 0;
        for (final ExportDTO export : exporterList) {
            if (validCondition(export, getEpisode())
                    && episodeExportResume(i)) {
                final PluginExporterInterface pluginexporter = exporter
                        .getPlugin(export.getName(),
                                HabitTvConf.DEFAULT_EXPORTER);

                final ExportTask exportTask = new ExportTask(getEpisode(),
                        export, pluginexporter, retreivePublisher, i);
                taskAdder.addExportTask(exportTask, export.getName());
                // wait for the current exportTask before running an other
                exportTask.waitEndOfTreatment();
                // sub export tasks are run asynchronously
                if (!export.getExporter().isEmpty()) {
                    export(export.getExporter());
                }
            }
            i++;
        }
    }

    /**
     * Checks if episode export should resume at the given step.
     * 
     * @param i the export step index
     * @return true if export should resume at this step
     */
    private boolean episodeExportResume(final int i) {
        // soit il n'y a pas à reprendre soit il faut reprendre et c'est l'étape
        // à reprendre
        return episodeExportState == null
                || i >= episodeExportState.getState();
    }

    /**
     * Validates the condition for an export.
     * 
     * @param export the export configuration
     * @param episode the episode to validate
     * @return true if the condition is valid
     */
    private boolean validCondition(final ExportDTO export,
            final EpisodeDTO episode) {
        boolean ret = true;
        if (export.getConditionReference() != null) {
            final String reference = export.getConditionReference();
            final String actualString = TokenReplacer.replaceAll(reference,
                    episode);
            ret = actualString.matches(export.getConditionPattern());
        }
        return ret;
    }

    /**
     * Downloads the episode.
     */
    private void download() {
        final DownloadTask downloadTask = new DownloadTask(getEpisode(),
                provider, downloaders, retreivePublisher, downloadDAO, manual);
        taskAdder.addDownloadTask(downloadTask, getEpisode().getCategory()
                .getPlugin());
        downloadTask.waitEndOfTreatment();
    }

    /**
     * Checks the episode validity.
     */
    private void check() {
        try {
            provider.checkEpisode(getEpisode());
        } catch (final InvalidEpisodeException e) {
            throw new TechnicalException(e);
        }
    }

    @Override
    public String toString() {
        return "RT" + getEpisode() + " "
                + (provider == null ? "no provider" : provider.getName())
                + " " + (exporter == null ? "no exporter" : exporter.getName());
    }

    /**
     * Sets the episode export state for resumption.
     * 
     * @param exportState the export state to set
     */
    public void setEpisodeExportState(
            final EpisodeExportState exportState) {
        this.episodeExportState = exportState;
    }

    @Override
    protected void canceled() {
        LOG.info("Cancel of " + getEpisode() + " done");
        retreivePublisher.addNews(new RetreiveEvent(getEpisode(),
                EpisodeStateEnum.STOPPED));
    }
} 