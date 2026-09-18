package com.dabi.habitv.provider.bfmtv;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

final class BfmTvCatalogMapper {

	private BfmTvCatalogMapper() {
	}

	static CategoryDTO mapChannel(final String channelId, final List<Map<String, Object>> programs) {
		final CategoryDTO channel = new CategoryDTO(BfmTvConf.NAME, BfmTvConf.channelLabel(channelId),
				BfmTvConf.channelPageUrl(channelId), BfmTvConf.EXTENSION);
		channel.setDownloadable(false);
		channel.addParameter(BfmTvConf.PARAMETER_KIND, BfmTvConf.KIND_CHANNEL);
		channel.addParameter(BfmTvConf.PARAMETER_CHANNEL, channelId);
		if (programs != null) {
			for (final Map<String, Object> program : programs) {
				final CategoryDTO mapped = mapProgram(channelId, program);
				if (mapped != null) {
					channel.addSubCategory(mapped);
				}
			}
		}
		return channel;
	}

	static CategoryDTO mapProgram(final String channelId, final Map<String, Object> program) {
		final String title = BfmTvJson.firstString(program, "title");
		final String categoryId = BfmTvJson.firstString(program, "categories");
		if (StringUtils.isEmpty(title) || StringUtils.isEmpty(categoryId)) {
			return null;
		}
		final CategoryDTO category = new CategoryDTO(BfmTvConf.NAME, title, programId(channelId, categoryId),
				BfmTvConf.EXTENSION);
		category.setDownloadable(true);
		category.addParameter(BfmTvConf.PARAMETER_KIND, BfmTvConf.KIND_PROGRAM);
		category.addParameter(BfmTvConf.PARAMETER_CHANNEL, channelId);
		category.addParameter(BfmTvConf.PARAMETER_CATEGORY, categoryId);
		return category;
	}

	static Set<EpisodeDTO> mapEpisodes(final CategoryDTO program, final List<Map<String, Object>> videos) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<EpisodeDTO>();
		if (program == null || videos == null) {
			return episodes;
		}
		for (final Map<String, Object> video : videos) {
			final String url = BfmTvJson.firstString(video, "long_url", "short_url");
			final String title = BfmTvJson.firstString(video, "title");
			if (StringUtils.isEmpty(url) || StringUtils.isEmpty(title) || !BfmTvUrls.isApprovedPublicDownloadUrl(url)) {
				continue;
			}
			final EpisodeDTO episode = new EpisodeDTO(program, title, url);
			BfmTvEpisodeMetadata.apply(video, episode, program);
			episodes.add(episode);
		}
		return episodes;
	}

	private static String programId(final String channelId, final String categoryId) {
		return BfmTvConf.HOME_URL + "/replay/" + channelId + "/" + categoryId;
	}

}
