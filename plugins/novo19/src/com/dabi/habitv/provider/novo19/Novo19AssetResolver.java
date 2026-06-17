package com.dabi.habitv.provider.novo19;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.provider.novo19.dto.Novo19BffPage;
import com.dabi.habitv.provider.novo19.dto.Novo19Tile;

final class Novo19AssetResolver {

	private Novo19AssetResolver() {
	}

	static String resolveAssetId(final DownloadParamDTO downloadParam, final Novo19CatalogClient catalogClient)
			throws IOException {
		if (downloadParam == null) {
			return null;
		}
		final String publicPath = Novo19UrlBuilder.publicPathFromCategoryId(downloadParam.getDownloadInput());
		if (!StringUtils.isEmpty(publicPath) && !Novo19PathRules.isExcludedPublicPath(publicPath)) {
			if (publicPath.startsWith("/player/") || publicPath.startsWith("/details/")) {
				final String assetFromPage = resolveFromBffPage(catalogClient, publicPath);
				if (!StringUtils.isEmpty(assetFromPage)) {
					return assetFromPage;
				}
			}
		}
		final String contentKind = downloadParam.getParam(Novo19Conf.PARAMETER_CONTENT_KIND);
		final String categoryAssetId = downloadParam.getParam(Novo19Conf.PARAMETER_ASSET_ID);
		if (Novo19Conf.CONTENT_KIND_FILM.equals(contentKind) && !StringUtils.isEmpty(categoryAssetId)) {
			return categoryAssetId;
		}
		return null;
	}

	private static String resolveFromBffPage(final Novo19CatalogClient catalogClient, final String publicPath)
			throws IOException {
		final String pagePath = trimLeadingSlash(publicPath);
		final Novo19BffPage page = catalogClient.fetchPageByPublicPath(pagePath);
		final Novo19Tile content = page.getContent();
		if (content == null) {
			return null;
		}
		return content.getAssetId();
	}

	private static String trimLeadingSlash(final String path) {
		String normalized = path;
		while (normalized.startsWith("/")) {
			normalized = normalized.substring(1);
		}
		return normalized;
	}

}
