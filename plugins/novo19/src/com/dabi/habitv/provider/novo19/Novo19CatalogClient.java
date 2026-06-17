package com.dabi.habitv.provider.novo19;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.provider.novo19.dto.Novo19BffPage;

final class Novo19CatalogClient {

	interface ContentLoader {
		String load(String url) throws IOException;
	}

	private final ContentLoader contentLoader;

	Novo19CatalogClient(final ContentLoader contentLoader) {
		this.contentLoader = contentLoader;
	}

	String fetchConfigJson() throws IOException {
		return contentLoader.load(Novo19UrlBuilder.bffConfigUrl());
	}

	Novo19BffPage fetchPageByPublicPath(final String publicPath) throws IOException {
		final String url = Novo19UrlBuilder.bffPageByPath(publicPath);
		final String json = contentLoader.load(url);
		return Novo19PageParser.parsePageEnvelope(json, url);
	}

	Novo19BffPage fetchPageByPublicUrl(final String publicUrl) throws IOException {
		final String path = Novo19UrlBuilder.publicPathFromCategoryId(publicUrl);
		if (StringUtils.isEmpty(path)) {
			return new Novo19BffPage(null, null, null, null, null, null);
		}
		return fetchPageByPublicPath(path);
	}

	String fetchTilesJson(final String bffPath) throws IOException {
		return contentLoader.load(Novo19UrlBuilder.bffAbsolutePath(bffPath));
	}

	private static final class HttpContentLoader implements ContentLoader {

		private final Novo19HttpClient.Transport transport;

		private HttpContentLoader(final Novo19HttpClient.Transport transport) {
			this.transport = transport;
		}

		@Override
		public String load(final String url) throws IOException {
			return transport.get(url, null);
		}

	}

	static ContentLoader httpContentLoader(final Novo19HttpClient.Transport transport) {
		return new HttpContentLoader(transport);
	}

}
