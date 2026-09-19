package com.dabi.habitv.provider.clubic;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.dabi.habitv.framework.plugin.utils.DownloadUtils;

public class ClubicPluginManager extends BasePluginWithProxy implements
		PluginProviderInterface { // NO_UCD

	/**
	 * Next.js flight payload embeds VideoShort objects as escaped JSON:
	 * {@code \"id\":\"xxxxxxxxxxx\",\"title\":\"...\"}.
	 */
	private static final Pattern ESCAPED_VIDEO_SHORT = Pattern.compile(
			"\\\\\"id\\\\\":\\\\\"([A-Za-z0-9_-]{11})\\\\\",\\\\\"title\\\\\":\\\\\"(.*?)\\\\\"");

	@Override
	public String getName() {
		return ClubicConf.NAME;
	}

	@Override
	public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();

		final String html = getUrlContent(category.getId(), ClubicConf.ENCODING);
		org.jsoup.nodes.Document doc = Jsoup.parse(html);

		final Elements listing = doc.select(".listingVideo");
		if (!listing.isEmpty()) {
			final Element nav = listing.get(0);
			for (Element liElement : nav.children()) {
				final Elements asElement = liElement.select("a");
				for (Element aElement : asElement) {
					if (aElement.hasAttr("href") && aElement.hasAttr("title")) {
						final String url = aElement.attr("href");
						final String name = aElement.attr("title");
						episodes.add(new EpisodeDTO(category, name, getUrl(url)));
						break;
					}
				}
			}
			return episodes;
		}

		episodes.addAll(parseYoutubeShorts(category, html));
		if (episodes.isEmpty()) {
			getLog().warn("provider=clubic operation=episodes sourceUrl=" + category.getId()
					+ " rootCause=listing-selectors-obsolete cookiesEnabled=false"
					+ " note=public-video-html-no-longer-matches-legacy-or-shorts-parser");
		}
		return episodes;
	}

	@Override
	public Set<CategoryDTO> findCategory() {
		final Set<CategoryDTO> categories = new LinkedHashSet<>();

		final String html = getUrlContent(ClubicConf.HOME_VIDEO_URL, ClubicConf.ENCODING);
		final org.jsoup.nodes.Document doc = Jsoup.parse(html);

		final Elements listing = doc.select(".listingChaine");
		if (!listing.isEmpty()) {
			final Element nav = listing.get(0);
			for (final Element chaineElement : nav.children()) {
				final Elements asElement = chaineElement.select("a");
				for (Element aElement : asElement) {
					if (aElement.hasAttr("href") && aElement.hasAttr("title")) {
						final String url = aElement.attr("href");
						final String name = aElement.attr("title");
						final CategoryDTO categoryDTO = new CategoryDTO(
								ClubicConf.NAME, name, getUrl(url),
								ClubicConf.EXTENSION);
						categoryDTO.setDownloadable(true);
						categories.add(categoryDTO);
						break;
					}
				}
			}
			return categories;
		}

		// Current Clubic /video page is a YouTube Shorts carousel (public embeds).
		if (ESCAPED_VIDEO_SHORT.matcher(html).find()
				|| html.contains("data-video-id=")
				|| html.contains("youtube.com/shorts/")) {
			final CategoryDTO shorts = new CategoryDTO(ClubicConf.NAME,
					ClubicConf.CATEGORY_SHORTS, ClubicConf.HOME_VIDEO_URL,
					ClubicConf.EXTENSION);
			shorts.setDownloadable(true);
			categories.add(shorts);
			return categories;
		}

		getLog().warn("provider=clubic operation=catalogue sourceUrl=" + ClubicConf.HOME_VIDEO_URL
				+ " rootCause=listing-selectors-obsolete cookiesEnabled=false"
				+ " note=public-video-html-no-longer-matches-legacy-scraper");
		return categories;
	}

	Set<EpisodeDTO> parseYoutubeShorts(final CategoryDTO category, final String html) {
		final Set<EpisodeDTO> episodes = new LinkedHashSet<>();
		final Matcher matcher = ESCAPED_VIDEO_SHORT.matcher(html);
		while (matcher.find()) {
			final String videoId = matcher.group(1);
			final String title = unescapeJsonString(matcher.group(2)).trim();
			if (title.isEmpty()) {
				continue;
			}
			final String url = ClubicConf.YOUTUBE_SHORTS_URL_PREFIX + videoId;
			episodes.add(new EpisodeDTO(category, title, url));
		}
		return episodes;
	}

	private static String unescapeJsonString(final String escaped) {
		return escaped
				.replace("\\n", " ")
				.replace("\\\"", "\"")
				.replace("\\/", "/")
				.replace("\\\\", "\\");
	}

	private String getUrl(String href) {
		return DownloadUtils.isHttpUrl(href) ? href
				: (ClubicConf.HOME_URL + href);
	}
}
