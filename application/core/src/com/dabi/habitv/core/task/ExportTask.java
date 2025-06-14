package com.dabi.habitv.core.task;

import com.dabi.habitv.api.plugin.api.PluginExporterInterface;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.ExportDTO;
import com.dabi.habitv.api.plugin.exception.ExportFailedException;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.api.plugin.pub.Publisher;
import com.dabi.habitv.core.event.EpisodeStateEnum;
import com.dabi.habitv.core.event.RetreiveEvent;
import com.dabi.habitv.core.token.TokenReplacer;

/**
 * Task for exporting episodes.
 * Handles the export process for episodes using plugin exporters.
 */
public class ExportTask extends AbstractEpisodeTask {

    /** The export configuration. */
    private final ExportDTO export;

    /** The plugin exporter interface. */
    private final PluginExporterInterface pluginExporter;

    /** The publisher for events. */
    private final Publisher<RetreiveEvent> publisher;

    /** The rank of this export task. */
    private final int rank;

    /**
     * Constructs a new ExportTask.
     *
     * @param episode the episode to export
     * @param exportConfig the export configuration
     * @param exporter the plugin exporter interface
     * @param eventPublisher the publisher for events
     * @param taskRank the rank of this task
     */
    public ExportTask(final EpisodeDTO episode, final ExportDTO exportConfig,
            final PluginExporterInterface exporter,
            final Publisher<RetreiveEvent> eventPublisher, final int taskRank) {
        super(episode);
        this.export = exportConfig;
        this.pluginExporter = exporter;
        this.publisher = eventPublisher;
        this.rank = taskRank;
    }

    @Override
    protected void adding() {
        LOG.info("Episode to export " + getEpisode() + " " + export.getName());
        publisher.addNews(new RetreiveEvent(getEpisode(),
                EpisodeStateEnum.TO_EXPORT));
    }

    @Override
    protected void failed(final Throwable e) {
        LOG.error("Episode failed to export " + getEpisode() + " "
                + export.getName(), e);
        publisher.addNews(new RetreiveEvent(getEpisode(),
                EpisodeStateEnum.EXPORT_FAILED, e, export.getOutput()));
    }

    @Override
    protected void ended() {
        LOG.error("Episode export ended" + getEpisode() + " "
                + export.getName());
    }

    @Override
    protected void started() {
        LOG.error("Episode export starting" + getEpisode() + " "
                + export.getName());
    }

    @Override
    protected Object doCall() throws ExportFailedException {
        final String cmd = TokenReplacer.replaceAll(export.getCmd(),
                getEpisode());
        ProcessHolder processHolder = pluginExporter.export(
                export.getCmdProcessor(), cmd);
        publisher.addNews(new RetreiveEvent(getEpisode(),
                EpisodeStateEnum.EXPORT_STARTING, export.getOutput(),
                processHolder));
        processHolder.start();
        return null;
    }

    @Override
    public int hashCode() {
        return getEpisode().hashCode() + export.hashCode() + export.hashCode()
                + publisher.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (obj instanceof ExportTask) {
            final ExportTask exportTask = (ExportTask) obj;
            ret = getEpisode().equals(exportTask.getEpisode());
            if (export.getCmd() != null) {
                ret = ret && export.getCmd().equals(export.getCmd());
            }
        } else {
            ret = false;
        }
        return ret;
    }

    @Override
    public String toString() {
        return getEpisode() + " " + export.getName() + " "
                + pluginExporter.getName();
    }

    /**
     * Gets the rank of this export task.
     *
     * @return the rank of this task
     */
    public int getRank() {
        return rank;
    }

    @Override
    protected void canceled() {
        LOG.error("Episode canceled : " + getEpisode() + " "
                + export.getName());
    }
} 