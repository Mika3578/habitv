package com.dabi.habitv.provider.rtbfauvio;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

final class RtbfAuvioPlaybackClient {

	private final RtbfAuvioHttpClient.Transport transport;

	private volatile String sessionToken;

	RtbfAuvioPlaybackClient(final RtbfAuvioHttpClient.Transport transport) {
		this.transport = transport;
	}

	String resolveReplayStreamUrl(final String assetId, final RtbfAuvioDiagnostics diagnostics) throws IOException {
		if (StringUtils.isEmpty(assetId)) {
			return null;
		}
		try {
			return resolveReplayStreamUrlOnce(assetId, diagnostics);
		} catch (final RtbfAuvioHttpException e) {
			if (e.getStatus() == 401) {
				sessionToken = null;
				try {
					return resolveReplayStreamUrlOnce(assetId, diagnostics);
				} catch (final RtbfAuvioHttpException retryFailure) {
					recordHttpFailure(diagnostics, retryFailure);
					throw retryFailure;
				}
			}
			recordHttpFailure(diagnostics, e);
			throw e;
		}
	}

	private static void recordHttpFailure(final RtbfAuvioDiagnostics diagnostics, final RtbfAuvioHttpException e) {
		diagnostics.setHttpStatus(e.getStatus());
		diagnostics.setRootCauseSummary(RtbfAuvioHttpStatus.summarizeFailure(e.getStatus()));
	}

	private String resolveReplayStreamUrlOnce(final String assetId, final RtbfAuvioDiagnostics diagnostics)
			throws IOException {
		final String playUrl = RtbfAuvioUrlBuilder.exposurePlayUrl(assetId);
		diagnostics.setEndpointHost(RtbfAuvioConf.EXPOSURE_HOST);
		final String playJson = transport.get(playUrl, authorizationHeader());
		if (!RtbfAuvioPlaybackParser.hasFormats(playJson, playUrl)) {
			diagnostics.setRootCauseSummary("empty-playback");
			return null;
		}
		if (RtbfAuvioPlaybackParser.hasOnlyUnusableFormats(playJson, playUrl)) {
			diagnostics.setRootCauseSummary("no-usable-format");
			return null;
		}
		final String streamUrl = RtbfAuvioPlaybackParser.selectReplayStreamUrl(playJson, playUrl);
		if (StringUtils.isEmpty(streamUrl)) {
			diagnostics.setRootCauseSummary("no-usable-format");
			return null;
		}
		diagnostics.setRootCauseSummary("ok");
		return streamUrl;
	}

	private String authorizationHeader() throws IOException {
		if (StringUtils.isEmpty(sessionToken)) {
			sessionToken = authenticate();
		}
		return "Bearer " + sessionToken;
	}

	private String authenticate() throws IOException {
		final String deviceId = java.util.UUID.randomUUID().toString();
		final String deviceJson = "{\"deviceId\":\"" + deviceId + "\",\"name\":\"WEB\",\"type\":\"WEB\"}";
		final String accountJwt = RtbfAuvioLocalAuth.optionalAccountJwt();
		final String body;
		final String authUrl;
		if (!StringUtils.isEmpty(accountJwt)) {
			authUrl = RtbfAuvioUrlBuilder.exposureAccountAuthUrl();
			body = "{\"deviceId\":\"" + deviceId + "\",\"device\":" + deviceJson + ",\"jwt\":\"" + accountJwt + "\"}";
		} else {
			authUrl = RtbfAuvioUrlBuilder.exposureAnonymousAuthUrl();
			body = "{\"deviceId\":\"" + deviceId + "\",\"device\":" + deviceJson + "}";
		}
		final String json = transport.postJson(authUrl, body);
		final String token = RtbfAuvioPlaybackParser.parseSessionToken(json, authUrl);
		if (StringUtils.isEmpty(token)) {
			throw new IOException("missing-session-token");
		}
		return token;
	}

}
