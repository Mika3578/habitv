package com.dabi.habitv.provider.canalplus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;
import com.dabi.habitv.provider.canalplus.CanalPlusHodorParser.CanalPlusUnitMetadata;
import com.dabi.habitv.provider.canalplus.CanalPlusPlaysetParser.CanalPlusPlaysetItem;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Resolves modern Canal+ unit streams (hodor + secure-gen-hapi playset/view).
 * DRM decryption is out of scope; this class resolves metadata and signed media entry points.
 */
final class CanalPlusModernStreamSupport {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private CanalPlusModernStreamSupport() {
	}

	static boolean isModernInput(final String downloadInput) {
		return CanalPlusContentIdParser.isModernCanalPlusUrl(downloadInput);
	}

	static CanalPlusUnitMetadata loadUnitMetadata(final BasePluginWithProxy plugin, final String downloadInput)
			throws IOException {
		final String detailUrl = resolveDetailPageUrl(plugin, downloadInput);
		if (StringUtils.isEmpty(detailUrl)) {
			return null;
		}
		@SuppressWarnings("unchecked")
		final Map<String, Object> detail = MAPPER.readValue(plugin.getInputStreamFromUrl(detailUrl), Map.class);
		return CanalPlusHodorParser.parseUnitDetail(detail);
	}

	static String resolveDetailPageUrl(final BasePluginWithProxy plugin, final String downloadInput)
			throws IOException {
		if (StringUtils.isEmpty(downloadInput)) {
			return null;
		}
		if (downloadInput.contains(CanalPlusModernConf.HODOR_HOST)) {
			return downloadInput;
		}
		if (downloadInput.contains(CanalPlusModernConf.PAGE_HOST)) {
			try (InputStream input = plugin.getInputStreamFromUrl(downloadInput)) {
				final String html = readUtf8(input);
				return CanalPlusPageDataParser.extractDetailPageUrl(html);
			}
		}
		return null;
	}

	static CanalPlusPlaysetItem loadSelectedPlaysetItem(final BasePluginWithProxy plugin, final String contentId)
			throws IOException {
		if (StringUtils.isEmpty(contentId)) {
			return null;
		}
		final String playsetUrl = String.format(CanalPlusModernConf.PLAYSET_URL_TEMPLATE, contentId);
		@SuppressWarnings("unchecked")
		final Map<String, Object> playset = MAPPER.readValue(plugin.getInputStreamFromUrl(playsetUrl), Map.class);
		return CanalPlusPlaysetParser.selectDownloadItem(playset);
	}

	static String resolveRoutedMediaUrl(final BasePluginWithProxy plugin, final String mediaUrl) throws IOException {
		if (StringUtils.isEmpty(mediaUrl)) {
			return null;
		}
		if (mediaUrl.contains(".mpd")) {
			return mediaUrl;
		}
		try (InputStream input = plugin.getInputStreamFromUrl(mediaUrl)) {
			final String body = readUtf8(input);
			if (StringUtils.isEmpty(body)) {
				return null;
			}
			final String trimmed = body.trim();
			if (trimmed.startsWith("http")) {
				return trimmed;
			}
		}
		return null;
	}

	static void assertDrmDownloadSupported() {
		throw new DownloadFailedException(buildDrmNotSupportedMessage());
	}

	static String buildDrmNotSupportedMessage() {
		return "Canal+ modern streams use PlayReady-protected DASH download (DRM_MKPC_PLAYREADY_DASH_DOWNLOAD). "
				+ "Habitv can resolve hodor/playset/view metadata but cannot decrypt segments yet. "
				+ "Use an external DRM-capable downloader for now.";
	}

	private static String readUtf8(final InputStream input) throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final byte[] buffer = new byte[4096];
		int read;
		while ((read = input.read(buffer)) != -1) {
			output.write(buffer, 0, read);
		}
		return output.toString("UTF-8");
	}

}
