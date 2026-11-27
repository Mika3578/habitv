package com.dabi.habitv.provider.tfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

final class TfoClient {

	interface ContentLoader {
		String load(String url) throws IOException;
	}

	private final ContentLoader contentLoader;

	TfoClient(final ContentLoader contentLoader) {
		this.contentLoader = contentLoader;
	}

	String fetchCatalogPage(final String catalogSlug) throws IOException {
		return contentLoader.load(TfoUrls.catalogPageUrl(catalogSlug));
	}

	String fetchShowPage(final String path) throws IOException {
		return contentLoader.load(TfoUrls.absoluteUrl(path));
	}

	String fetchWatchPage(final String watchUrl) throws IOException {
		return contentLoader.load(watchUrl);
	}

	List<TfoHtml.CatalogItem> loadCatalogItems(final String catalogSlug) throws IOException {
		return TfoHtml.parseCatalogItems(fetchCatalogPage(catalogSlug));
	}

	List<Map<String, Object>> loadSeasons(final String seriePath) throws IOException {
		return TfoHtml.parseSeasons(fetchShowPage(seriePath));
	}
}
