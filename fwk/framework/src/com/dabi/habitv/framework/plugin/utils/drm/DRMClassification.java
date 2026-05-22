package com.dabi.habitv.framework.plugin.utils.drm;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import com.dabi.habitv.api.plugin.dto.ProtectedContentStatus;

/**
 * Immutable result of inspecting a streaming manifest for DRM markers.
 *
 * <p>{@link #getSchemes()} lists the DRM scheme markers that were textually
 * present in the manifest (e.g. {@code "widevine"}, {@code "playready"}). The
 * presence of a marker is used <em>only</em> for classification &mdash; no
 * license server is contacted, no key material is fetched, and no media is
 * decrypted. {@link #getStatus()} maps those markers to the official Habitv
 * DRM boundary state.
 */
public final class DRMClassification implements Serializable {

	private static final long serialVersionUID = 1L;

	private final boolean drmDetected;
	private final ProtectedContentStatus status;
	private final Set<String> schemes;
	private final String reason;

	DRMClassification(final boolean drmDetected,
			final ProtectedContentStatus status,
			final Set<String> schemes,
			final String reason) {
		this.drmDetected = drmDetected;
		this.status = status;
		this.schemes = schemes == null
				? Collections.<String>emptySet()
				: Collections.unmodifiableSet(new TreeSet<>(schemes));
		this.reason = reason;
	}

	public boolean isDrmDetected() {
		return drmDetected;
	}

	public ProtectedContentStatus getStatus() {
		return status;
	}

	public Set<String> getSchemes() {
		return schemes;
	}

	public String getReason() {
		return reason;
	}

	@Override
	public String toString() {
		return "DRMClassification{drm=" + drmDetected
				+ ", status=" + status
				+ ", schemes=" + schemes
				+ ", reason=" + reason + "}";
	}
}
