package com.dabi.habitv.provider.rtbfauvio;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;

final class RtbfAuvioAssetResolver {

	private static final Pattern MEDIA_NUMERIC_SUFFIX = Pattern.compile("-([0-9]+)$");

	private RtbfAuvioAssetResolver() {
	}

	static String resolveAssetId(final DownloadParamDTO downloadParam) {
		if (downloadParam == null) {
			return null;
		}
		final String fromParam = downloadParam.getParam(RtbfAuvioConf.PARAMETER_ASSET_ID);
		if (!StringUtils.isEmpty(fromParam)) {
			return RtbfAuvioUrlBuilder.formatEntitlementAssetId(fromParam);
		}
		return resolveFromMediaUrl(downloadParam.getDownloadInput());
	}

	static String resolveFromMediaUrl(final String mediaUrl) {
		if (StringUtils.isEmpty(mediaUrl) || !RtbfAuvioUrlBuilder.isApprovedPublicDownloadUrl(mediaUrl)) {
			return null;
		}
		String path = mediaUrl;
		final int query = path.indexOf('?');
		if (query >= 0) {
			path = path.substring(0, query);
		}
		final int slash = path.lastIndexOf('/');
		final String slug = slash >= 0 ? path.substring(slash + 1) : path;
		final Matcher matcher = MEDIA_NUMERIC_SUFFIX.matcher(slug);
		if (!matcher.find()) {
			return null;
		}
		return RtbfAuvioUrlBuilder.formatEntitlementAssetId(matcher.group(1));
	}

}
