package com.dabi.habitv.provider.icitoutv;

import java.io.IOException;
import java.util.List;

final class IciToutTvClient {

	interface ContentLoader {
		String load(String url) throws IOException;
	}

	private final ContentLoader contentLoader;

	IciToutTvClient(final ContentLoader contentLoader) {
		this.contentLoader = contentLoader;
	}

	List<IciToutTvHtml.ShowRef> loadFreeShows() throws IOException {
		return IciToutTvHtml.parseFreeShows(contentLoader.load(IciToutTvUrls.freeCollectionUrl()));
	}

	List<IciToutTvHtml.EpisodeRef> loadShowEpisodes(final String slug) throws IOException {
		return IciToutTvHtml.parseShowEpisodes(contentLoader.load(IciToutTvUrls.showPageUrl(slug)));
	}
}
