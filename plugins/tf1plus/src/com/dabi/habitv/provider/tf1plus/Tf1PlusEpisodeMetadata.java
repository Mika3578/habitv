package com.dabi.habitv.provider.tf1plus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO;

final class Tf1PlusEpisodeMetadata {

	private Tf1PlusEpisodeMetadata() {
	}

	static void apply(final Map<String, Object> video, final EpisodeDTO episode, final CategoryDTO program) {
		if (video == null || episode == null) {
			return;
		}
		final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
		if (program != null && StringUtils.isNotEmpty(program.getName())) {
			metadata.setSeriesTitle(program.getName().trim());
		}
		if (StringUtils.isNotEmpty(episode.getName())) {
			metadata.setEpisodeTitle(episode.getName().trim());
		}
		final Map<String, Object> decoration = Tf1PlusJson.asMap(video.get("decoration"));
		if (StringUtils.isEmpty(metadata.getSeriesTitle())) {
			metadata.setSeriesTitle(Tf1PlusJson.firstString(decoration, "programLabel"));
		}
		final String description = Tf1PlusJson.firstString(decoration, "description");
		if (description != null) {
			metadata.setDescription(description);
		}
		metadata.setThumbnailUrl(firstImageUrl(decoration.get("images")));
		metadata.setSeasonNumber(Tf1PlusJson.asPositiveInt(video.get("season")));
		metadata.setEpisodeNumber(Tf1PlusJson.asPositiveInt(video.get("episode")));
		final Map<String, Object> playingInfos = Tf1PlusJson.asMap(video.get("playingInfos"));
		final Long duration = Tf1PlusJson.asPositiveLong(playingInfos.get("duration"));
		if (duration != null) {
			metadata.setDurationSeconds(duration);
			episode.setDurationSeconds(duration);
		}
		final Date published = parseIsoDate(Tf1PlusJson.firstString(video, "published", "date"));
		if (published != null) {
			metadata.setPublicationDate(published);
		}
		final Map<String, Object> broadcastChannel = Tf1PlusJson.asMap(video.get("broadcastChannel"));
		final String channel = Tf1PlusJson.firstString(broadcastChannel, "label", "slug");
		if (channel != null) {
			metadata.setChannel(channel);
		} else if (program != null && StringUtils.isNotEmpty(program.getParameter(Tf1PlusConf.PARAMETER_CHANNEL))) {
			metadata.setChannel(Tf1PlusConf.channelLabel(program.getParameter(Tf1PlusConf.PARAMETER_CHANNEL)));
		}
		metadata.setProviderEpisodeId(Tf1PlusJson.firstString(video, "id", "emId"));
		if (StringUtils.isNotEmpty(episode.getId())) {
			metadata.setSourceUrl(episode.getId());
		}
		episode.setMetadata(metadata);
	}

	private static String firstImageUrl(final Object imagesRaw) {
		final List<Map<String, Object>> images = Tf1PlusJson.asMapList(imagesRaw);
		for (final Map<String, Object> image : images) {
			final String type = Tf1PlusJson.firstString(image, "type");
			if (type != null && "PREVIEW".equals(type)) {
				continue;
			}
			final String url = firstSourceUrl(image);
			if (url != null && !url.endsWith(".mp4")) {
				return url;
			}
		}
		return null;
	}

	private static String firstSourceUrl(final Map<String, Object> image) {
		final List<Map<String, Object>> sources = Tf1PlusJson.asMapList(image.get("sources"));
		for (final Map<String, Object> source : sources) {
			final String url = Tf1PlusJson.firstString(source, "url");
			if (url != null) {
				return url;
			}
		}
		return null;
	}

	static Date parseIsoDate(final String raw) {
		if (StringUtils.isEmpty(raw)) {
			return null;
		}
		final String value = raw.trim();
		final String[] patterns = { "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd" };
		for (final String pattern : patterns) {
			final SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			try {
				return format.parse(value);
			} catch (final ParseException ignored) {
				// try next pattern
			}
		}
		return null;
	}

}
