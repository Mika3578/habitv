package com.dabi.habitv.api.plugin.dto;

import java.io.Serializable;

/**
 * Optional, backward-compatible capability descriptor for a provider plugin.
 *
 * <p>All fields are optional and default to {@code null} or {@code false}.
 * Plugins that do not opt in keep their existing behavior unchanged.
 *
 * <p>The capability set intentionally describes <em>official, provider-authorized</em>
 * DRM/CDM and offline integrations only. It does not expose, nor is it intended
 * to expose, any DRM-bypass, key-extraction, or CDM-emulation primitives.
 */
public final class ProviderCapabilities implements Serializable {

	private static final long serialVersionUID = 1L;

	private Boolean requiresDRM;
	private Boolean supportsOfficialDRMPlayback;
	private Boolean supportsOfficialOfflineAccess;
	private Boolean officialPlaybackUrlSupported;
	private Boolean officialIntegrationRequired;
	private String officialIntegrationName;
	private String officialIntegrationDocumentationUrl;
	private Boolean directDownloadSupported;
	private Boolean metadataOnly;
	private ProtectedContentStatus lastCheckStatus;
	private String lastCheckMessage;

	public ProviderCapabilities() {
		// all fields default to null; backward-compatible no-op state
	}

	public Boolean getRequiresDRM() {
		return requiresDRM;
	}

	public ProviderCapabilities setRequiresDRM(final Boolean requiresDRM) {
		this.requiresDRM = requiresDRM;
		return this;
	}

	public Boolean getSupportsOfficialDRMPlayback() {
		return supportsOfficialDRMPlayback;
	}

	public ProviderCapabilities setSupportsOfficialDRMPlayback(final Boolean v) {
		this.supportsOfficialDRMPlayback = v;
		return this;
	}

	public Boolean getSupportsOfficialOfflineAccess() {
		return supportsOfficialOfflineAccess;
	}

	public ProviderCapabilities setSupportsOfficialOfflineAccess(final Boolean v) {
		this.supportsOfficialOfflineAccess = v;
		return this;
	}

	public Boolean getOfficialPlaybackUrlSupported() {
		return officialPlaybackUrlSupported;
	}

	public ProviderCapabilities setOfficialPlaybackUrlSupported(final Boolean v) {
		this.officialPlaybackUrlSupported = v;
		return this;
	}

	public Boolean getOfficialIntegrationRequired() {
		return officialIntegrationRequired;
	}

	public ProviderCapabilities setOfficialIntegrationRequired(final Boolean v) {
		this.officialIntegrationRequired = v;
		return this;
	}

	public String getOfficialIntegrationName() {
		return officialIntegrationName;
	}

	public ProviderCapabilities setOfficialIntegrationName(final String name) {
		this.officialIntegrationName = name;
		return this;
	}

	public String getOfficialIntegrationDocumentationUrl() {
		return officialIntegrationDocumentationUrl;
	}

	public ProviderCapabilities setOfficialIntegrationDocumentationUrl(final String url) {
		this.officialIntegrationDocumentationUrl = url;
		return this;
	}

	public Boolean getDirectDownloadSupported() {
		return directDownloadSupported;
	}

	public ProviderCapabilities setDirectDownloadSupported(final Boolean v) {
		this.directDownloadSupported = v;
		return this;
	}

	public Boolean getMetadataOnly() {
		return metadataOnly;
	}

	public ProviderCapabilities setMetadataOnly(final Boolean v) {
		this.metadataOnly = v;
		return this;
	}

	public ProtectedContentStatus getLastCheckStatus() {
		return lastCheckStatus;
	}

	public ProviderCapabilities setLastCheckStatus(final ProtectedContentStatus s) {
		this.lastCheckStatus = s;
		return this;
	}

	public String getLastCheckMessage() {
		return lastCheckMessage;
	}

	public ProviderCapabilities setLastCheckMessage(final String message) {
		this.lastCheckMessage = message;
		return this;
	}

	/**
	 * Convenience: true when the capability set indicates DRM-protected content
	 * that Habitv must not attempt to direct-download.
	 */
	public boolean isProtectedContent() {
		return Boolean.TRUE.equals(requiresDRM)
				|| Boolean.TRUE.equals(metadataOnly)
				|| Boolean.TRUE.equals(officialIntegrationRequired);
	}
}
