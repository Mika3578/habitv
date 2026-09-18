package com.dabi.habitv.provider.bfmtv;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO;

final class BfmTvEpisodeMetadata {

	private BfmTvEpisodeMetadata() {
	}

	static void apply(final Map<String, Object> video, final EpisodeDTO episode, final CategoryDTO program) {
		if (video == null || episode == null) {
			return;
		}
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		if (program != null && StringUtils.isNotEmpty(program.getName())) {
			metadata.setSeriesTitle(program.getName().trim());
		}
		if (StringUtils.isEmpty(metadata.getSeriesTitle())) {
			metadata.setSeriesTitle(BfmTvJson.firstString(video, "section"));
		}
		if (StringUtils.isNotEmpty(episode.getName())) {
			metadata.setEpisodeTitle(episode.getName().trim());
		}
		final String description = BfmTvJson.firstString(video, "description");
		if (description != null) {
			metadata.setDescription(description);
		}
		metadata.setThumbnailUrl(BfmTvJson.firstString(video, "image", "image_small"));
		final Long durationMs = BfmTvJson.asPositiveLong(video.get("video_duration_ms"));
		if (durationMs != null) {
			final Long seconds = Long.valueOf(durationMs.longValue() / 1000L);
			if (seconds.longValue() > 0L) {
				metadata.setDurationSeconds(seconds);
				episode.setDurationSeconds(seconds);
			}
		}
		final Date airDate = unixSeconds(video.get("begin_date"));
		if (airDate != null) {
			metadata.setAirDate(airDate);
			episode.setEpisodeDate(airDate);
		}
		if (program != null && StringUtils.isNotEmpty(program.getParameter(BfmTvConf.PARAMETER_CHANNEL))) {
			metadata.setChannel(BfmTvConf.channelLabel(program.getParameter(BfmTvConf.PARAMETER_CHANNEL)));
		}
		metadata.setProviderEpisodeId(BfmTvJson.firstString(video, "video", "contenu_id"));
		if (StringUtils.isNotEmpty(episode.getId())) {
			metadata.setSourceUrl(episode.getId());
		}
		episode.setMetadata(metadata);
	}

	static Date unixSeconds(final Object raw) {
		final Long seconds = BfmTvJson.asPositiveLong(raw);
		if (seconds == null) {
			return null;
		}
		return new Date(seconds.longValue() * 1000L);
	}

}
