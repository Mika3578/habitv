package com.dabi.habitv.provider.tf1plus;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

final class Tf1PlusCatalogMapper {

	private Tf1PlusCatalogMapper() {
	}

	static Set<CategoryDTO> mapChannels(final Map<String, List<Map<String, Object>>> programsByChannel) {
		final Set<CategoryDTO> channels = new LinkedHashSet<CategoryDTO>();
		for (final String slug : Tf1PlusConf.CHANNEL_SLUGS) {
			final CategoryDTO channel = new CategoryDTO(Tf1PlusConf.NAME, Tf1PlusConf.channelLabel(slug),
					Tf1PlusConf.HOME_URL + "/" + slug, Tf1PlusConf.EXTENSION);
			channel.setDownloadable(false);
			channel.addParameter(Tf1PlusConf.PARAMETER_KIND, Tf1PlusConf.KIND_CHANNEL);
			channel.addParameter(Tf1PlusConf.PARAMETER_CHANNEL, slug);
			final List<Map<String, Object>> programs = programsByChannel.get(slug);
			if (programs != null) {
				addPrograms(channel, slug, programs);
			}
			channels.add(channel);
		}
		return channels;
	}

	static void addPrograms(final CategoryDTO channel, final String channelSlug,
			final List<Map<String, Object>> programs) {
		final Map<String, CategoryDTO> genres = new LinkedHashMap<String, CategoryDTO>();
		for (final Map<String, Object> program : programs) {
			final CategoryDTO programCategory = mapProgram(channelSlug, program);
			if (programCategory == null) {
				continue;
			}
			final String genreName = genreLabel(program);
			CategoryDTO genre = genres.get(genreName);
			if (genre == null) {
				genre = new CategoryDTO(Tf1PlusConf.NAME, genreName,
						channel.getId() + "/genre/" + sanitizeId(genreName), Tf1PlusConf.EXTENSION);
				genre.setDownloadable(false);
				genre.addParameter(Tf1PlusConf.PARAMETER_KIND, Tf1PlusConf.KIND_GENRE);
				genre.addParameter(Tf1PlusConf.PARAMETER_CHANNEL, channelSlug);
				channel.addSubCategory(genre);
				genres.put(genreName, genre);
			}
			genre.addSubCategory(programCategory);
		}
	}

	static CategoryDTO mapProgram(final String channelSlug, final Map<String, Object> program) {
		final String slug = Tf1PlusJson.firstString(program, "slug");
		final String name = Tf1PlusJson.firstString(program, "name");
		if (StringUtils.isEmpty(slug) || StringUtils.isEmpty(name)) {
			return null;
		}
		final CategoryDTO category = new CategoryDTO(Tf1PlusConf.NAME, name,
				Tf1PlusUrls.programPageUrl(channelSlug, slug), Tf1PlusConf.EXTENSION);
		category.setDownloadable(true);
		category.addParameter(Tf1PlusConf.PARAMETER_KIND, Tf1PlusConf.KIND_PROGRAM);
		category.addParameter(Tf1PlusConf.PARAMETER_CHANNEL, channelSlug);
		category.addParameter(Tf1PlusConf.PARAMETER_PROGRAM_SLUG, slug);
		return category;
	}

	static Set<EpisodeDTO> mapEpisodes(final CategoryDTO program, final List<Map<String, Object>> videos) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<EpisodeDTO>();
		if (program == null || videos == null) {
			return episodes;
		}
		final String channelSlug = program.getParameter(Tf1PlusConf.PARAMETER_CHANNEL);
		final String programSlug = program.getParameter(Tf1PlusConf.PARAMETER_PROGRAM_SLUG);
		for (final Map<String, Object> video : videos) {
			final String type = Tf1PlusJson.firstString(video, "type");
			if (type != null && !Tf1PlusConf.VIDEO_TYPE_REPLAY.equals(type)) {
				continue;
			}
			final String url = videoUrl(channelSlug, programSlug, video);
			final String title = episodeTitle(video);
			if (StringUtils.isEmpty(url) || StringUtils.isEmpty(title) || !Tf1PlusUrls.isApprovedPublicDownloadUrl(url)) {
				continue;
			}
			final EpisodeDTO episode = new EpisodeDTO(program, title, url);
			Tf1PlusEpisodeMetadata.apply(video, episode, program);
			episodes.add(episode);
		}
		return episodes;
	}

	static String genreLabel(final Map<String, Object> program) {
		final List<Map<String, Object>> categories = Tf1PlusJson.asMapList(program.get("categories"));
		for (final Map<String, Object> category : categories) {
			if (Boolean.TRUE.equals(category.get("main"))) {
				final String label = Tf1PlusJson.firstString(category, "label");
				if (label != null) {
					return label;
				}
			}
		}
		if (!categories.isEmpty()) {
			final String label = Tf1PlusJson.firstString(categories.get(0), "label");
			if (label != null) {
				return label;
			}
		}
		return "Programs";
	}

	static String videoUrl(final String channelSlug, final String programSlug, final Map<String, Object> video) {
		final String direct = Tf1PlusJson.firstString(video, "url");
		if (Tf1PlusUrls.isApprovedPublicDownloadUrl(direct)) {
			return direct;
		}
		final String videoSlug = Tf1PlusJson.firstString(video, "slug");
		if (StringUtils.isEmpty(channelSlug) || StringUtils.isEmpty(programSlug) || StringUtils.isEmpty(videoSlug)) {
			return null;
		}
		return Tf1PlusUrls.videoPageUrl(channelSlug, programSlug, videoSlug);
	}

	static String episodeTitle(final Map<String, Object> video) {
		final Map<String, Object> decoration = Tf1PlusJson.asMap(video.get("decoration"));
		final String label = Tf1PlusJson.firstString(decoration, "label");
		if (label != null) {
			return label;
		}
		return Tf1PlusJson.firstString(video, "title", "slug");
	}

	private static String sanitizeId(final String label) {
		return label.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
	}

}
