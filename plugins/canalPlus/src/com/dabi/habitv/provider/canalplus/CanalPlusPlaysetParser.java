package com.dabi.habitv.provider.canalplus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Selects the PlayReady DASH download playset item and builds the view request body
 * for {@code secure-gen-hapi.canal-plus.com/conso/view}.
 */
final class CanalPlusPlaysetParser {

	private CanalPlusPlaysetParser() {
	}

	@SuppressWarnings("unchecked")
	static CanalPlusPlaysetItem selectDownloadItem(final Map<String, Object> playsetRoot) {
		if (playsetRoot == null) {
			return null;
		}
		final Object available = playsetRoot.get("available");
		if (!(available instanceof List)) {
			return null;
		}
		for (final Object itemObject : (List<Object>) available) {
			if (!(itemObject instanceof Map)) {
				continue;
			}
			final Map<String, Object> item = (Map<String, Object>) itemObject;
			final String drmType = stringValue(item.get("drmType"));
			if (drmType != null && drmType.contains("PLAYREADY") && drmType.contains("DOWNLOAD")) {
				return toPlaysetItem(item);
			}
		}
		for (final Object itemObject : (List<Object>) available) {
			if (!(itemObject instanceof Map)) {
				continue;
			}
			final Map<String, Object> item = (Map<String, Object>) itemObject;
			final String quality = stringValue(item.get("quality"));
			if ("HD".equalsIgnoreCase(quality)) {
				return toPlaysetItem(item);
			}
		}
		return null;
	}

	private static CanalPlusPlaysetItem toPlaysetItem(final Map<String, Object> item) {
		final String drmType = stringValue(item.get("drmType"));
		final String quality = stringValue(item.get("quality"));
		final String idKey = stringValue(item.get("idKey"));
		final String hash = stringValue(item.get("hash"));
		final String functionalType = stringValue(item.get("functionalType"));
		final String contentId = extractContentIdFromHash(hash);
		return new CanalPlusPlaysetItem(contentId, drmType, quality, idKey, hash,
				StringUtils.isEmpty(functionalType) ? "MOVIE" : functionalType);
	}

	private static String extractContentIdFromHash(final String hash) {
		if (StringUtils.isEmpty(hash)) {
			return null;
		}
		final int separator = hash.indexOf('|');
		return separator > 0 ? hash.substring(0, separator) : hash;
	}

	static Map<String, Object> buildViewRequestBody(final CanalPlusPlaysetItem item) {
		final Map<String, Object> body = new LinkedHashMap<String, Object>();
		body.put("comMode", CanalPlusModernConf.COM_MODE_CATCHUP_NOLIMIT);
		body.put("contentId", item.getContentId());
		body.put("distMode", CanalPlusModernConf.DIST_MODE_CATCHUP);
		body.put("distTechnology", CanalPlusModernConf.DIST_TECHNOLOGY_DOWNLOAD);
		body.put("drmType", item.getDrmType());
		body.put("functionalType", item.getFunctionalType());
		body.put("hash", item.getHash());
		body.put("idKey", item.getIdKey());
		body.put("quality", item.getQuality());
		return body;
	}

	@SuppressWarnings("unchecked")
	static String extractMediaUrl(final Map<String, Object> viewRoot) {
		if (viewRoot == null) {
			return null;
		}
		final Object medias = viewRoot.get("medias");
		if (!(medias instanceof List)) {
			return null;
		}
		for (final Object mediaObject : (List<Object>) medias) {
			if (!(mediaObject instanceof Map)) {
				continue;
			}
			final Map<String, Object> media = (Map<String, Object>) mediaObject;
			final String type = stringValue(media.get("type"));
			if ("video".equalsIgnoreCase(type) || StringUtils.isEmpty(type)) {
				final String url = stringValue(media.get("url"));
				if (StringUtils.isNotEmpty(url)) {
					return url;
				}
			}
		}
		return null;
	}

	private static String stringValue(final Object value) {
		return value == null ? null : String.valueOf(value).trim();
	}

	static final class CanalPlusPlaysetItem {
		private final String contentId;
		private final String drmType;
		private final String quality;
		private final String idKey;
		private final String hash;
		private final String functionalType;

		CanalPlusPlaysetItem(final String contentId, final String drmType, final String quality,
				final String idKey, final String hash, final String functionalType) {
			this.contentId = contentId;
			this.drmType = drmType;
			this.quality = quality;
			this.idKey = idKey;
			this.hash = hash;
			this.functionalType = functionalType;
		}

		String getContentId() {
			return contentId;
		}

		String getDrmType() {
			return drmType;
		}

		String getQuality() {
			return quality;
		}

		String getIdKey() {
			return idKey;
		}

		String getHash() {
			return hash;
		}

		String getFunctionalType() {
			return functionalType;
		}
	}

}
