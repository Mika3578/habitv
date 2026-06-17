package com.dabi.habitv.provider.novo19;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.dabi.habitv.provider.novo19.dto.Novo19BffPage;

final class Novo19CatalogClient {

	interface ContentLoader {
		String load(String url) throws IOException;
	}

	private final ContentLoader contentLoader;

	Novo19CatalogClient(final BasePluginWithProxy plugin) {
		this.contentLoader = new PluginContentLoader(plugin);
	}

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

	private static final class PluginContentLoader implements ContentLoader {

		private final BasePluginWithProxy plugin;

		private PluginContentLoader(final BasePluginWithProxy plugin) {
			this.plugin = plugin;
		}

		@Override
		public String load(final String url) throws IOException {
			try (java.io.InputStream input = plugin.getInputStreamFromUrl(url)) {
				final java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
				final byte[] buffer = new byte[256];
				int read;
				while ((read = input.read(buffer)) != -1) {
					output.write(buffer, 0, read);
				}
				return output.toString("UTF-8");
			}
		}

	}

}
