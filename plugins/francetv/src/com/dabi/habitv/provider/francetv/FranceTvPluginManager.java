package com.dabi.habitv.provider.francetv;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.dabi.habitv.api.plugin.api.PluginProviderDownloaderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.dabi.habitv.framework.plugin.utils.DownloadUtils;

public class FranceTvPluginManager extends BasePluginWithProxy implements PluginProviderDownloaderInterface {

	@Override
	public String getName() {
		return FranceTvConf.NAME;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		final String categoryUrl = category.getId();
		final String pathPrefix = toPathPrefix(categoryUrl);
		if (pathPrefix == null) {
			return episodes;
		}

		final Document doc = Jsoup.parse(getUrlContent(categoryUrl));
		for (final Element link : doc.select("a[href]")) {
			final String href = link.attr("href");
			if (!href.startsWith(pathPrefix) || !href.endsWith(".html")) {
				continue;
			}
			if (href.endsWith("/direct.html")) {
				continue;
			}
			final String name = episodeName(link, href);
			if (StringUtils.isEmpty(name)) {
				continue;
			}
			episodes.add(new EpisodeDTO(category, name, toAbsoluteUrl(href)));
		}
		return episodes;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categories = new LinkedHashSet<>();
		for (final String slug : FranceTvConf.CHANNEL_SLUGS) {
			final String channelUrl = FranceTvConf.HOME_URL + "/" + slug + "/";
			final CategoryDTO channel = new CategoryDTO(FranceTvConf.NAME, channelLabel(slug), channelUrl,
					FranceTvConf.EXTENSION);
			channel.setDownloadable(false);
			channel.addSubCategories(findPrograms(channelUrl, slug));
			categories.add(channel);
		}
		return categories;
	}

	private Collection<CategoryDTO> findPrograms(final String channelUrl, final String slug) {
		final Set<CategoryDTO> programs = new LinkedHashSet<>();
		final Set<String> programPaths = new LinkedHashSet<>();
		final String channelPrefix = "/" + slug + "/";
		final Pattern programRootPattern = Pattern.compile("^/" + Pattern.quote(slug) + "/([^/]+)/");

		final Document doc = Jsoup.parse(getUrlContent(channelUrl));
		for (final Element link : doc.select("a[href^='" + channelPrefix + "']")) {
			final String href = link.attr("href");
			if (!href.endsWith("/") || href.contains(".html")) {
				continue;
			}
			final Matcher matcher = programRootPattern.matcher(href);
			if (!matcher.find()) {
				continue;
			}
			programPaths.add("/" + slug + "/" + matcher.group(1) + "/");
		}

		for (final String programPath : programPaths) {
			final String programUrl = FranceTvConf.HOME_URL + programPath;
			final String programName = programLabel(programPath);
			final CategoryDTO program = new CategoryDTO(FranceTvConf.NAME, programName, programUrl,
					FranceTvConf.EXTENSION);
			program.setDownloadable(true);
			programs.add(program);
		}
		return programs;
	}

	@Override
	public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
			throws DownloadFailedException {
		return DownloadUtils.download(downloadParam, downloaders, FrameworkConf.YOUTUBE);
	}

	@Override
	public DownloadableState canDownload(final String downloadInput) {
		if (downloadInput.contains("france.tv") || downloadInput.contains("france2.")
				|| downloadInput.contains("france3.") || downloadInput.contains("france4.")
				|| downloadInput.contains("france5.") || downloadInput.contains("franceo.")
				|| downloadInput.contains("pluzz.")) {
			return DownloadableState.SPECIFIC;
		}
		return DownloadableState.IMPOSSIBLE;
	}

	private static String channelLabel(final String slug) {
		if ("france-o".equals(slug)) {
			return "France O";
		}
		return "France " + slug.substring(slug.length() - 1);
	}

	private static String programLabel(final String programPath) {
		final String slug = programPath.replaceAll("/$", "");
		final int lastSlash = slug.lastIndexOf('/');
		String segment = lastSlash >= 0 ? slug.substring(lastSlash + 1) : slug;
		return segment.replace('-', ' ');
	}

	private static String episodeName(final Element link, final String href) {
		String name = link.attr("title");
		if (StringUtils.isEmpty(name)) {
			name = link.text();
		}
		if (StringUtils.isEmpty(name)) {
			name = href.replaceAll(".*/([^/]+)\\.html$", "$1").replace('-', ' ');
		}
		return name.trim();
	}

	private static String toAbsoluteUrl(final String href) {
		if (href.startsWith("http://") || href.startsWith("https://")) {
			return href;
		}
		if (href.startsWith("/")) {
			return FranceTvConf.HOME_URL + href;
		}
		return FranceTvConf.HOME_URL + "/" + href;
	}

	private static String toPathPrefix(final String categoryUrl) {
		if (StringUtils.isEmpty(categoryUrl)) {
			return null;
		}
		final String withoutHost = categoryUrl.replace(FranceTvConf.HOME_URL, "");
		if (!withoutHost.startsWith("/")) {
			return null;
		}
		return withoutHost.endsWith("/") ? withoutHost : withoutHost + "/";
	}

}
