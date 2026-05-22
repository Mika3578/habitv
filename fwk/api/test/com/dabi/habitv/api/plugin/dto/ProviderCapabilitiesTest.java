package com.dabi.habitv.api.plugin.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Verifies that {@link ProviderCapabilities} defaults are backward-compatible:
 * an instance with no fields populated reports no DRM, no integration, and
 * does not affect existing code paths.
 */
public class ProviderCapabilitiesTest {

	@Test
	public void defaultsAreNullAndNonProtected() {
		final ProviderCapabilities caps = new ProviderCapabilities();
		assertNull(caps.getRequiresDRM());
		assertNull(caps.getSupportsOfficialDRMPlayback());
		assertNull(caps.getSupportsOfficialOfflineAccess());
		assertNull(caps.getOfficialPlaybackUrlSupported());
		assertNull(caps.getOfficialIntegrationRequired());
		assertNull(caps.getOfficialIntegrationName());
		assertNull(caps.getOfficialIntegrationDocumentationUrl());
		assertNull(caps.getDirectDownloadSupported());
		assertNull(caps.getMetadataOnly());
		assertNull(caps.getLastCheckStatus());
		assertNull(caps.getLastCheckMessage());
		assertFalse("default capabilities must not classify as protected",
				caps.isProtectedContent());
	}

	@Test
	public void requiresDrmFlagsAsProtected() {
		final ProviderCapabilities caps = new ProviderCapabilities()
				.setRequiresDRM(Boolean.TRUE);
		assertTrue(caps.isProtectedContent());
	}

	@Test
	public void metadataOnlyFlagsAsProtected() {
		final ProviderCapabilities caps = new ProviderCapabilities()
				.setMetadataOnly(Boolean.TRUE);
		assertTrue(caps.isProtectedContent());
	}

	@Test
	public void officialIntegrationRequiredFlagsAsProtected() {
		final ProviderCapabilities caps = new ProviderCapabilities()
				.setOfficialIntegrationRequired(Boolean.TRUE);
		assertTrue(caps.isProtectedContent());
	}

	@Test
	public void chainedSettersBuildExpectedValues() {
		final ProviderCapabilities caps = new ProviderCapabilities()
				.setRequiresDRM(Boolean.TRUE)
				.setOfficialIntegrationName("example-official-sdk")
				.setOfficialIntegrationDocumentationUrl("https://example.test/sdk")
				.setLastCheckStatus(ProtectedContentStatus.OFFICIAL_PLAYBACK_ONLY)
				.setLastCheckMessage("classified offline from manifest markers");
		assertEquals(Boolean.TRUE, caps.getRequiresDRM());
		assertEquals("example-official-sdk", caps.getOfficialIntegrationName());
		assertEquals("https://example.test/sdk", caps.getOfficialIntegrationDocumentationUrl());
		assertEquals(ProtectedContentStatus.OFFICIAL_PLAYBACK_ONLY, caps.getLastCheckStatus());
		assertEquals("classified offline from manifest markers", caps.getLastCheckMessage());
	}

	@Test
	public void episodeDtoOptionalFieldsAreBackwardCompatible() {
		final CategoryDTO category = new CategoryDTO("p", "c", "id", "mp4");
		final EpisodeDTO episode = new EpisodeDTO(category, "name", "url");
		// Old plugins never touch the new fields; defaults are null and the
		// episode is not classified as DRM-protected.
		assertNull(episode.getProtectedContentStatus());
		assertNull(episode.getOfficialPlaybackUrl());
		assertFalse(episode.isDrmProtected());

		episode.setProtectedContentStatus(ProtectedContentStatus.OFFICIAL_PLAYBACK_ONLY);
		episode.setOfficialPlaybackUrl("https://provider.example/episode/123");
		assertTrue(episode.isDrmProtected());
		assertEquals("https://provider.example/episode/123", episode.getOfficialPlaybackUrl());
	}
}
