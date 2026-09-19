package com.dabi.habitv.provider.tvaplus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class TvaPlusClient {

	interface ContentLoader {
		String load(String url) throws IOException;
	}

	private final ContentLoader contentLoader;

	TvaPlusClient(final ContentLoader contentLoader) {
		this.contentLoader = contentLoader;
	}

	List<TvaPlusHtml.ShowRef> loadTvaShows() throws IOException {
		return TvaPlusHtml.parseTvaShows(contentLoader.load(TvaPlusUrls.tvaChannelUrl()));
	}

	List<TvaPlusHtml.EpisodeRef> loadShowEpisodes(final String showSlug) throws IOException {
		final String showHtml = contentLoader.load(TvaPlusUrls.pageUrl(showSlug));
		final List<TvaPlusHtml.SeasonRef> seasons = TvaPlusHtml.parseShowSeasons(showHtml);
		final Map<String, TvaPlusHtml.EpisodeRef> byUrl = new LinkedHashMap<String, TvaPlusHtml.EpisodeRef>();
		int seasonCount = 0;
		for (final TvaPlusHtml.SeasonRef season : seasons) {
			if (seasonCount >= TvaPlusConf.MAX_SEASONS_PER_SHOW) {
				break;
			}
			seasonCount++;
			addEpisodes(byUrl, loadSeasonEpisodes(season.slug));
		}
		return new ArrayList<TvaPlusHtml.EpisodeRef>(byUrl.values());
	}

	List<TvaPlusHtml.EpisodeRef> loadRecentTvaEpisodes() throws IOException {
		return TvaPlusHtml.parseRecentTvaEpisodes(contentLoader.load(TvaPlusUrls.recentUrl()));
	}

	private List<TvaPlusHtml.EpisodeRef> loadSeasonEpisodes(final String seasonSlug) throws IOException {
		final String seasonHtml = contentLoader.load(TvaPlusUrls.pageUrl(seasonSlug));
		List<TvaPlusHtml.EpisodeRef> episodes = TvaPlusHtml.parseSeasonEpisodes(seasonHtml);
		if (!episodes.isEmpty()) {
			return episodes;
		}
		final String carouselSlug = TvaPlusHtml.episodesCarouselSlug(seasonHtml);
		if (carouselSlug == null) {
			return episodes;
		}
		return TvaPlusHtml.parseSeasonEpisodes(contentLoader.load(TvaPlusUrls.pageUrl(carouselSlug)));
	}

	private static void addEpisodes(final Map<String, TvaPlusHtml.EpisodeRef> byUrl,
			final List<TvaPlusHtml.EpisodeRef> episodes) {
		for (final TvaPlusHtml.EpisodeRef episode : episodes) {
			if (episode.watchUrl != null && !byUrl.containsKey(episode.watchUrl)) {
				byUrl.put(episode.watchUrl, episode);
			}
		}
	}
}
