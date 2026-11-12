package com.dabi.habitv.provider.telemb;

import java.io.IOException;
import java.util.List;

final class TeleMbClient {

	interface ContentLoader {
		String load(String url) throws IOException;
	}

	private final ContentLoader contentLoader;

	TeleMbClient(final ContentLoader contentLoader) {
		this.contentLoader = contentLoader;
	}

	List<TeleMbHtml.ShowRef> loadShows() throws IOException {
		return TeleMbHtml.parseShows(contentLoader.load(TeleMbUrls.emissionsIndexUrl()));
	}

	List<TeleMbHtml.EpisodeRef> loadShowEpisodes(final String slug) throws IOException {
		return TeleMbHtml.parseEpisodes(contentLoader.load(TeleMbUrls.showPageUrl(slug)), slug);
	}

	String resolveHlsUrl(final String episodePageUrl) throws IOException {
		final String pageHtml = contentLoader.load(episodePageUrl);
		final String videoId = TeleMbHtml.extractVideoId(pageHtml);
		if (videoId == null) {
			return null;
		}
		final String embedHtml = contentLoader.load(TeleMbUrls.freecasterEmbedUrl(videoId));
		return TeleMbHtml.extractHlsUrl(embedHtml);
	}
}
