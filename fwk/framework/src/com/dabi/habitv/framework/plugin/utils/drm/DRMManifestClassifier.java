package com.dabi.habitv.framework.plugin.utils.drm;

import java.util.Locale;
import java.util.TreeSet;

import com.dabi.habitv.api.plugin.dto.ProtectedContentStatus;

/**
 * Safe DRM marker classifier for DASH MPD and HLS m3u8 manifests.
 *
 * <p>This classifier performs <em>string inspection only</em> against an
 * already-fetched manifest. It is deliberately limited to detecting public
 * markers (DASH {@code ContentProtection} elements, HLS {@code EXT-X-KEY}
 * lines with non-{@code NONE} method, and well-known DRM scheme UUIDs /
 * keyformats). It MUST NOT:
 * <ul>
 *   <li>open network connections,</li>
 *   <li>fetch or process license challenges/responses,</li>
 *   <li>obtain, cache, derive, or store decryption keys,</li>
 *   <li>decrypt manifests or media,</li>
 *   <li>emulate a CDM in any form.</li>
 * </ul>
 *
 * <p>The result is used only to feed Habitv's official DRM boundary
 * classification (see {@link ProtectedContentStatus}).
 */
public final class DRMManifestClassifier {

	// Well-known DRM scheme markers. Detection of these strings is purely
	// for classification; Habitv does not implement any of them.
	private static final String WIDEVINE_UUID = "edef8ba9-79d6-4ace-a3c8-27dcd51d21ed";
	private static final String PLAYREADY_UUID = "9a04f079-9840-4286-ab92-e65be0885f95";
	private static final String FAIRPLAY_UUID = "94ce86fb-07ff-4f43-adb8-93d2fa968ca2";
	private static final String MARLIN_UUID = "5e629af5-38da-4063-8977-97ffbd9902d4";
	private static final String CLEARKEY_UUID = "e2719d58-a985-b3c9-781a-b030af78d30e";

	private static final String HLS_KEYFORMAT_FAIRPLAY = "com.apple.streamingkeydelivery";
	private static final String HLS_KEYFORMAT_PLAYREADY = "com.microsoft.playready";
	private static final String HLS_KEYFORMAT_WIDEVINE = "urn:uuid:" + WIDEVINE_UUID;

	private DRMManifestClassifier() {
	}

	/**
	 * Classify a manifest body. {@code null} or empty bodies return a
	 * no-DRM result with status {@link ProtectedContentStatus#DIRECT_DOWNLOAD_SUPPORTED}.
	 */
	public static DRMClassification classify(final String manifest, final ManifestType type) {
		if (manifest == null || manifest.isEmpty()) {
			return new DRMClassification(false,
					ProtectedContentStatus.DIRECT_DOWNLOAD_SUPPORTED,
					null,
					"empty-manifest");
		}
		switch (type == null ? ManifestType.UNKNOWN : type) {
		case DASH_MPD:
			return classifyDash(manifest);
		case HLS_M3U8:
			return classifyHls(manifest);
		default:
			return classifyUnknown(manifest);
		}
	}

	private static DRMClassification classifyDash(final String manifest) {
		final String lower = manifest.toLowerCase(Locale.ROOT);
		final TreeSet<String> schemes = new TreeSet<>();
		final boolean hasContentProtection = lower.contains("<contentprotection");
		collectKnownSchemes(lower, schemes);
		if (hasContentProtection || !schemes.isEmpty()) {
			// DASH ContentProtection: by default we treat as official-playback-only
			// unless callers later refine via official integration capability flags.
			return new DRMClassification(true,
					ProtectedContentStatus.OFFICIAL_PLAYBACK_ONLY,
					schemes,
					"dash-content-protection");
		}
		return new DRMClassification(false,
				ProtectedContentStatus.DIRECT_DOWNLOAD_SUPPORTED,
				schemes,
				"dash-no-content-protection");
	}

	private static DRMClassification classifyHls(final String manifest) {
		final TreeSet<String> schemes = new TreeSet<>();
		final String lower = manifest.toLowerCase(Locale.ROOT);
		boolean drm = false;
		for (final String rawLine : manifest.split("\\r?\\n")) {
			final String line = rawLine.trim();
			if (!line.startsWith("#EXT-X-KEY")) {
				continue;
			}
			// Non-protected HLS may include a #EXT-X-KEY:METHOD=NONE row;
			// any other method indicates encrypted media.
			final String upper = line.toUpperCase(Locale.ROOT);
			if (upper.contains("METHOD=NONE")) {
				continue;
			}
			if (upper.contains("METHOD=")) {
				drm = true;
				// Identify well-known DRM keyformats when present.
				final String lowerLine = line.toLowerCase(Locale.ROOT);
				if (lowerLine.contains(HLS_KEYFORMAT_FAIRPLAY)) {
					schemes.add("fairplay");
				}
				if (lowerLine.contains(HLS_KEYFORMAT_PLAYREADY)) {
					schemes.add("playready");
				}
				if (lowerLine.contains(HLS_KEYFORMAT_WIDEVINE)) {
					schemes.add("widevine");
				}
				if (lowerLine.contains("method=sample-aes") || lowerLine.contains("method=sample-aes-ctr")) {
					// Apple sample-AES is commonly used with FairPlay; mark scheme
					// when the keyformat is not present explicitly.
					schemes.add("sample-aes");
				}
				if (lowerLine.contains("method=aes-128")) {
					schemes.add("aes-128");
				}
			}
		}
		// Even outside #EXT-X-KEY, manifest may reference DRM scheme UUIDs.
		collectKnownSchemes(lower, schemes);
		if (drm || hasOnlyEncryptedAesScheme(schemes) || hasKnownDrmScheme(schemes)) {
			return new DRMClassification(true,
					ProtectedContentStatus.OFFICIAL_PLAYBACK_ONLY,
					schemes,
					"hls-ext-x-key");
		}
		return new DRMClassification(false,
				ProtectedContentStatus.DIRECT_DOWNLOAD_SUPPORTED,
				schemes,
				"hls-no-ext-x-key");
	}

	private static DRMClassification classifyUnknown(final String manifest) {
		final String lower = manifest.toLowerCase(Locale.ROOT);
		final TreeSet<String> schemes = new TreeSet<>();
		collectKnownSchemes(lower, schemes);
		if (!schemes.isEmpty()) {
			return new DRMClassification(true,
					ProtectedContentStatus.OFFICIAL_PLAYBACK_ONLY,
					schemes,
					"unknown-manifest-with-drm-marker");
		}
		return new DRMClassification(false,
				ProtectedContentStatus.DIRECT_DOWNLOAD_SUPPORTED,
				schemes,
				"unknown-manifest-no-drm-marker");
	}

	private static void collectKnownSchemes(final String lower, final TreeSet<String> schemes) {
		if (lower.contains(WIDEVINE_UUID) || lower.contains("widevine")) {
			schemes.add("widevine");
		}
		if (lower.contains(PLAYREADY_UUID) || lower.contains("playready")) {
			schemes.add("playready");
		}
		if (lower.contains(FAIRPLAY_UUID) || lower.contains("fairplay")) {
			schemes.add("fairplay");
		}
		if (lower.contains(MARLIN_UUID) || lower.contains("marlin")) {
			schemes.add("marlin");
		}
		if (lower.contains(CLEARKEY_UUID) || lower.contains("clearkey")) {
			schemes.add("clearkey");
		}
	}

	private static boolean hasKnownDrmScheme(final TreeSet<String> schemes) {
		return schemes.contains("widevine")
				|| schemes.contains("playready")
				|| schemes.contains("fairplay")
				|| schemes.contains("marlin")
				|| schemes.contains("clearkey");
	}

	/**
	 * Plain AES-128 alone (no DRM keyformat) is ambiguous: it may be
	 * provider-encrypted streaming without commercial DRM. We still treat it
	 * as protected for direct download purposes &mdash; Habitv does not try to
	 * fetch or interpret the key.
	 */
	private static boolean hasOnlyEncryptedAesScheme(final TreeSet<String> schemes) {
		return schemes.contains("aes-128") || schemes.contains("sample-aes");
	}
}
