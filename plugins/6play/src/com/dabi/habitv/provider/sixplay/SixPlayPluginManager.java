package com.dabi.habitv.provider.sixplay;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeMetadataDTO;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;

public class SixPlayPluginManager extends BasePluginWithProxy implements PluginProviderInterface { // NO_UCD

	@Override
	public String getName() {
		return SixPlayConf.NAME;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		if (category == null || StringUtils.isEmpty(category.getId())) {
			return episodes;
		}

		final String html = getUrlContent(category.getId());
		for (final String episodePath : SixPlayHrefCatalog.episodePathsFromHtml(html)) {
			final String episodeUrl = SixPlayHrefCatalog.toAbsoluteUrl(episodePath);
			final String name = SixPlaySlugLabels.episodeTitleFromPath(episodePath);
			if (StringUtils.isEmpty(name)) {
				continue;
			}
			final EpisodeDTO episode = new EpisodeDTO(category, name, episodeUrl);
			final EpisodeMetadataDTO metadata = new EpisodeMetadataDTO();
			if (category.getName() != null && !category.getName().trim().isEmpty()) {
				metadata.setSeriesTitle(category.getName().trim());
			}
			metadata.setEpisodeTitle(name.trim());
			metadata.setSourceUrl(episodeUrl);
			if (category.getFatherCategory() != null
					&& StringUtils.isNotEmpty(category.getFatherCategory().getName())) {
				metadata.setChannel(category.getFatherCategory().getName().trim());
			}
			episode.setMetadata(metadata);
			episodes.add(episode);
		}

		if (episodes.isEmpty()) {
			getLog().warn("provider=6play operation=episodes sourceUrl=" + category.getId()
					+ " rootCause=no-episode-hrefs-in-html cookiesEnabled=false");
		}
		return episodes;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Map<String, CategoryDTO> channelBySlug = new LinkedHashMap<>();
		for (final Map.Entry<String, String> channel : SixPlayConf.CHANNEL_SLUG_TO_LABEL.entrySet()) {
			final CategoryDTO channelCat = new CategoryDTO(SixPlayConf.NAME, channel.getValue(),
					SixPlayHrefCatalog.toAbsoluteUrl("/" + channel.getKey()), SixPlayConf.EXTENSION);
			channelBySlug.put(channel.getKey(), channelCat);
		}

		final String sitemapXml = getUrlContent(SixPlayConf.SITEMAP_SERVICE_URL);
		final Set<String> folderPaths = SixPlayHrefCatalog.channelFolderPathsFromSitemap(sitemapXml);
		if (folderPaths.isEmpty()) {
			getLog().warn("provider=6play operation=catalogue sourceUrl=" + SixPlayConf.SITEMAP_SERVICE_URL
					+ " rootCause=empty-sitemap-folders cookiesEnabled=false");
		}

		for (final String folderPath : folderPaths) {
			final String channelSlug = SixPlayHrefCatalog.channelSlugFromPath(folderPath);
			final CategoryDTO channelCat = channelBySlug.get(channelSlug);
			if (channelCat == null) {
				continue;
			}
			final String folderUrl = SixPlayHrefCatalog.toAbsoluteUrl(folderPath);
			final String folderHtml = getUrlContent(folderUrl);
			addProgramsFromHtml(channelCat, folderHtml);
		}

		final Set<CategoryDTO> categories = new LinkedHashSet<>();
		for (final CategoryDTO channelCat : channelBySlug.values()) {
			if (channelCat.getSubCategories() != null && !channelCat.getSubCategories().isEmpty()) {
				categories.add(channelCat);
			}
		}

		if (categories.isEmpty()) {
			getLog().warn("provider=6play operation=catalogue sourceUrl=" + SixPlayConf.HOME_URL
					+ " rootCause=no-programs-discovered cookiesEnabled=false"
					+ " note=check-sitemap-and-folder-pages");
		}
		return categories;
	}

	private void addProgramsFromHtml(final CategoryDTO channelCat, final String html) {
		final Set<String> existingUrls = programUrlsUnderChannel(channelCat);
		for (final String programPath : SixPlayHrefCatalog.programPathsFromHtml(html)) {
			final String programUrl = SixPlayHrefCatalog.toAbsoluteUrl(programPath);
			if (existingUrls.contains(programUrl)) {
				continue;
			}
			final String title = SixPlaySlugLabels.programTitleFromPath(programPath);
			if (StringUtils.isEmpty(title)) {
				continue;
			}
			final CategoryDTO programCat = new CategoryDTO(SixPlayConf.NAME, title, programUrl,
					SixPlayConf.EXTENSION);
			programCat.setDownloadable(true);
			channelCat.addSubCategory(programCat);
			existingUrls.add(programUrl);
		}
	}

	private Set<String> programUrlsUnderChannel(final CategoryDTO channelCat) {
		final Set<String> urls = new LinkedHashSet<>();
		final Collection<CategoryDTO> subs = channelCat.getSubCategories();
		if (subs == null) {
			return urls;
		}
		for (final CategoryDTO sub : subs) {
			if (sub != null && StringUtils.isNotEmpty(sub.getId())) {
				urls.add(sub.getId());
			}
		}
		return urls;
	}

}
