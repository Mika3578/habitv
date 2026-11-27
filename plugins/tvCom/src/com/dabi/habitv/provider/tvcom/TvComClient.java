package com.dabi.habitv.provider.tvcom;

import java.io.IOException;
import java.util.List;

final class TvComClient {

	interface ContentLoader {
		String load(String url) throws IOException;
	}

	private final ContentLoader contentLoader;

	TvComClient(final ContentLoader contentLoader) {
		this.contentLoader = contentLoader;
	}

	List<TvComHtml.ShowRef> loadShows() throws IOException {
		return TvComHtml.parseShows(contentLoader.load(TvComUrls.emissionsIndexUrl()));
	}

	List<TvComHtml.EpisodeRef> loadShowEpisodes(final String slug) throws IOException {
		return TvComHtml.parseEpisodes(contentLoader.load(TvComUrls.showPageUrl(slug)), slug);
	}

	String resolveHlsUrl(final String episodePageUrl) throws IOException {
		final String pageHtml = contentLoader.load(episodePageUrl);
		final String videoId = TvComHtml.extractVideoId(pageHtml);
		if (videoId == null) {
			return null;
		}
		final String embedHtml = contentLoader.load(TvComUrls.freecasterEmbedUrl(videoId));
		return TvComHtml.extractHlsUrl(embedHtml);
	}
}
