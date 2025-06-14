package com.dabi.habitv.core.task;

import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

/**
 * Abstract base class for episode-related tasks.
 * Provides common functionality for tasks that operate on episodes.
 */
abstract class AbstractEpisodeTask extends AbstractTask<Object> {

    /** The episode this task operates on. */
    private final EpisodeDTO episode;

    /**
     * Constructs a new AbstractEpisodeTask.
     * 
     * @param episodeDTO the episode this task operates on
     */
    AbstractEpisodeTask(final EpisodeDTO episodeDTO) {
        this.episode = episodeDTO;
    }

    /**
     * Gets the episode this task operates on.
     * 
     * @return the episode
     */
    public EpisodeDTO getEpisode() {
        return episode;
    }
} 