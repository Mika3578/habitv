package com.dabi.habitv.provider.tvlux;

import java.io.IOException;
import java.util.List;

final class TvLuxClient {

	interface ContentLoader {
		String load(String url) throws IOException;
	}

	private final ContentLoader contentLoader;

	TvLuxClient(final ContentLoader contentLoader) {
		this.contentLoader = contentLoader;
	}

	List<TvLuxHtml.ShowRef> loadShows() throws IOException {
		return TvLuxHtml.parseShows(contentLoader.load(TvLuxUrls.replayIndexUrl()));
	}

	List<TvLuxHtml.EpisodeRef> loadShowEpisodes(final String slug) throws IOException {
		return TvLuxHtml.parseEpisodes(contentLoader.load(TvLuxUrls.showPageUrl(slug)), slug);
	}

	String resolveHlsUrl(final String episodePageUrl) throws IOException {
		final String pageHtml = contentLoader.load(episodePageUrl);
		final String videoId = TvLuxHtml.extractVideoId(pageHtml);
		if (videoId == null) {
			return null;
		}
		final String embedHtml = contentLoader.load(TvLuxUrls.freecasterEmbedUrl(videoId));
		return TvLuxHtml.extractHlsUrl(embedHtml);
	}
}
