package com.dabi.habitv.api.plugin.api;

import com.dabi.habitv.api.plugin.dto.EpisodeDTO;

/**
 * Boundary interface for future, <em>official provider-authorized</em> DRM/CDM
 * playback or offline integrations.
 *
 * <p>This interface intentionally exposes only <em>playback-URL</em> and
 * <em>capability-flag</em> primitives. It is the architectural seam at which a
 * provider-authorized SDK or partner integration could be plugged in. By
 * design, the contract <strong>does not</strong> expose:
 * <ul>
 *   <li>license-server endpoints or license challenge/response APIs,</li>
 *   <li>key acquisition, key storage, or key extraction methods,</li>
 *   <li>manifest decryption or media decryption primitives,</li>
 *   <li>CDM emulation hooks.</li>
 * </ul>
 *
 * <p>Implementations may only delegate to <em>official, contractually
 * authorized</em> SDKs/APIs/EME flows provided by the upstream provider. Any
 * implementation that attempted bypass, key extraction, or CDM emulation
 * would be out of contract for this interface and is explicitly out of scope
 * for Habitv.
 */
public interface OfficialProtectedPlaybackIntegration {

	/** Display name of the official integration (e.g. partner SDK identifier). */
	String getIntegrationName();

	/** Name of the upstream provider this integration is authorized for. */
	String getProviderName();

	/**
	 * @return {@code true} when the integration is configured and usable in
	 *         the current runtime (SDK present, credentials provisioned, etc.).
	 */
	boolean isAvailable();

	/** @return {@code true} if the official integration covers playback. */
	boolean supportsPlayback();

	/** @return {@code true} if the official integration covers offline/download access. */
	boolean supportsOfflineAccess();

	/**
	 * Return the official playback URL (provider page, partner deep link, or
	 * EME-bound stream URL) for the given episode, or {@code null} if no
	 * official URL can be produced.
	 *
	 * <p>This URL is intended to be opened by the user's browser or by an
	 * authorized SDK. It is never used as input to a direct downloader.
	 */
	String getOfficialPlaybackUrl(EpisodeDTO episode);

	/** Short human-readable status message (suitable for UI display). */
	String getStatusMessage();
}
