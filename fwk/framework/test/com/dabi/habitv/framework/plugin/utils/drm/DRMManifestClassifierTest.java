package com.dabi.habitv.framework.plugin.utils.drm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.ProtectedContentStatus;

/**
 * Unit tests for {@link DRMManifestClassifier}. The classifier is purely
 * string-based and these tests load synthetic manifest fixtures from
 * test/resources/fixtures/drm/ &mdash; no live URL is contacted, no real
 * key material is involved.
 */
public class DRMManifestClassifierTest {

	private static final String FIXTURE_DIR = "test/resources/fixtures/drm/";

	@Test
	public void dashWithContentProtectionIsClassifiedAsOfficialPlaybackOnly() throws IOException {
		final String mpd = read(FIXTURE_DIR + "dash-drm.mpd");
		final DRMClassification result = DRMManifestClassifier.classify(mpd, ManifestType.DASH_MPD);
		assertTrue("ContentProtection must trigger DRM detection", result.isDrmDetected());
		assertEquals(ProtectedContentStatus.OFFICIAL_PLAYBACK_ONLY, result.getStatus());
		assertTrue("widevine marker must be reported", result.getSchemes().contains("widevine"));
		assertTrue("playready marker must be reported", result.getSchemes().contains("playready"));
	}

	@Test
	public void dashWithoutContentProtectionIsDirectDownloadSupported() throws IOException {
		final String mpd = read(FIXTURE_DIR + "dash-clear.mpd");
		final DRMClassification result = DRMManifestClassifier.classify(mpd, ManifestType.DASH_MPD);
		assertFalse(result.isDrmDetected());
		assertEquals(ProtectedContentStatus.DIRECT_DOWNLOAD_SUPPORTED, result.getStatus());
	}

	@Test
	public void hlsWithFairPlayKeyIsDrmProtected() throws IOException {
		final String m3u8 = read(FIXTURE_DIR + "hls-fairplay.m3u8");
		final DRMClassification result = DRMManifestClassifier.classify(m3u8, ManifestType.HLS_M3U8);
		assertTrue(result.isDrmDetected());
		// HLS with DRM markers is reported as OFFICIAL_PLAYBACK_ONLY at the
		// capability level; the queue maps that to the runtime state
		// DIRECT_DOWNLOAD_UNSUPPORTED_DRM (covered by DownloadTaskDrmGuardTest).
		assertEquals(ProtectedContentStatus.OFFICIAL_PLAYBACK_ONLY, result.getStatus());
		assertTrue(result.getSchemes().contains("fairplay"));
	}

	@Test
	public void hlsWithWidevineKeyformatIsDrmProtected() throws IOException {
		final String m3u8 = read(FIXTURE_DIR + "hls-widevine.m3u8");
		final DRMClassification result = DRMManifestClassifier.classify(m3u8, ManifestType.HLS_M3U8);
		assertTrue(result.isDrmDetected());
		assertEquals(ProtectedContentStatus.OFFICIAL_PLAYBACK_ONLY, result.getStatus());
		assertTrue(result.getSchemes().contains("widevine"));
	}

	@Test
	public void hlsMethodNoneIsNotDrm() throws IOException {
		final String m3u8 = read(FIXTURE_DIR + "hls-method-none.m3u8");
		final DRMClassification result = DRMManifestClassifier.classify(m3u8, ManifestType.HLS_M3U8);
		assertFalse(result.isDrmDetected());
		assertEquals(ProtectedContentStatus.DIRECT_DOWNLOAD_SUPPORTED, result.getStatus());
	}

	@Test
	public void hlsWithoutKeyIsNotDrm() throws IOException {
		final String m3u8 = read(FIXTURE_DIR + "hls-clear.m3u8");
		final DRMClassification result = DRMManifestClassifier.classify(m3u8, ManifestType.HLS_M3U8);
		assertFalse(result.isDrmDetected());
		assertEquals(ProtectedContentStatus.DIRECT_DOWNLOAD_SUPPORTED, result.getStatus());
	}

	@Test
	public void emptyManifestIsTreatedAsDirectDownloadSupported() {
		final DRMClassification result = DRMManifestClassifier.classify("", ManifestType.DASH_MPD);
		assertFalse(result.isDrmDetected());
		assertEquals(ProtectedContentStatus.DIRECT_DOWNLOAD_SUPPORTED, result.getStatus());
	}

	@Test
	public void nullManifestIsTreatedAsDirectDownloadSupported() {
		final DRMClassification result = DRMManifestClassifier.classify(null, ManifestType.UNKNOWN);
		assertFalse(result.isDrmDetected());
		assertEquals(ProtectedContentStatus.DIRECT_DOWNLOAD_SUPPORTED, result.getStatus());
	}

	@Test
	public void unknownManifestWithWidevineMarkerIsDrm() {
		final String body = "..urn:uuid:edef8ba9-79d6-4ace-a3c8-27dcd51d21ed..";
		final DRMClassification result = DRMManifestClassifier.classify(body, ManifestType.UNKNOWN);
		assertTrue(result.isDrmDetected());
		assertEquals(ProtectedContentStatus.OFFICIAL_PLAYBACK_ONLY, result.getStatus());
		assertTrue(result.getSchemes().contains("widevine"));
	}

	private static String read(final String relativePath) throws IOException {
		return new String(Files.readAllBytes(Paths.get(relativePath)), Charset.forName("UTF-8"));
	}
}
