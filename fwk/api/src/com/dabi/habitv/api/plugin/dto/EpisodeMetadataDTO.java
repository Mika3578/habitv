package com.dabi.habitv.api.plugin.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * Canonical semantic media metadata for an episode.
 * <p>
 * Distinct from legacy {@link EpisodeDTO} display fields ({@code name},
 * {@code num}, category label). Providers populate only fields they know
 * reliably; core naming never invents missing season/episode numbers.
 */
public class EpisodeMetadataDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String seriesTitle;

	private String episodeTitle;

	private Integer seasonNumber;

	private Integer episodeNumber;

	private Date airDate;

	private Long durationSeconds;

	private String description;

	private String channel;

	private String providerEpisodeId;

	private String sourceUrl;

	public String getSeriesTitle() {
		return seriesTitle;
	}

	public void setSeriesTitle(final String seriesTitle) {
		this.seriesTitle = blankToNull(seriesTitle);
	}

	public String getEpisodeTitle() {
		return episodeTitle;
	}

	public void setEpisodeTitle(final String episodeTitle) {
		this.episodeTitle = blankToNull(episodeTitle);
	}

	public Integer getSeasonNumber() {
		return seasonNumber;
	}

	public void setSeasonNumber(final Integer seasonNumber) {
		this.seasonNumber = seasonNumber;
	}

	public Integer getEpisodeNumber() {
		return episodeNumber;
	}

	public void setEpisodeNumber(final Integer episodeNumber) {
		this.episodeNumber = episodeNumber;
	}

	public Date getAirDate() {
		return airDate == null ? null : new Date(airDate.getTime());
	}

	public void setAirDate(final Date airDate) {
		this.airDate = airDate == null ? null : new Date(airDate.getTime());
	}

	public Long getDurationSeconds() {
		return durationSeconds;
	}

	public void setDurationSeconds(final Long durationSeconds) {
		this.durationSeconds = durationSeconds;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = blankToNull(description);
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(final String channel) {
		this.channel = blankToNull(channel);
	}

	public String getProviderEpisodeId() {
		return providerEpisodeId;
	}

	public void setProviderEpisodeId(final String providerEpisodeId) {
		this.providerEpisodeId = blankToNull(providerEpisodeId);
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(final String sourceUrl) {
		this.sourceUrl = blankToNull(sourceUrl);
	}

	/**
	 * @return true only when both season and episode numbers are present
	 */
	public boolean hasSeasonAndEpisode() {
		return seasonNumber != null && episodeNumber != null;
	}

	private static String blankToNull(final String value) {
		if (value == null) {
			return null;
		}
		final String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
