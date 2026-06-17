package com.dabi.habitv.provider.novo19;

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

final class Novo19PlaybackClient {

	private final Novo19HttpClient.Transport transport;

	private String sessionToken;

	Novo19PlaybackClient(final Novo19HttpClient.Transport transport) {
		this.transport = transport;
	}

	String resolveReplayStreamUrl(final String assetId, final Novo19Diagnostics diagnostics) throws IOException {
		if (StringUtils.isEmpty(assetId)) {
			return null;
		}
		final String playUrl = Novo19UrlBuilder.redBeePlayUrl(assetId);
		diagnostics.setEndpointHost("exposure.api.redbee.live");
		try {
			final String playJson = transport.get(playUrl, authorizationHeader());
			if (Novo19PlaybackParser.hasOnlyProtectedFormats(playJson, playUrl)) {
				diagnostics.setRootCauseSummary("drm-protected");
				return null;
			}
			final String streamUrl = Novo19PlaybackParser.selectReplayStreamUrl(playJson, playUrl);
			if (StringUtils.isEmpty(streamUrl)) {
				diagnostics.setRootCauseSummary("no-public-format");
				return null;
			}
			diagnostics.setRootCauseSummary("ok");
			return streamUrl;
		} catch (final Novo19HttpException e) {
			diagnostics.setHttpStatus(e.getStatus());
			diagnostics.setRootCauseSummary(Novo19HttpStatus.summarizeFailure(e.getStatus()));
			throw e;
		}
	}

	private String authorizationHeader() throws IOException {
		if (StringUtils.isEmpty(sessionToken)) {
			sessionToken = authenticate();
		}
		return "Bearer " + sessionToken;
	}

	private String authenticate() throws IOException {
		final String deviceId = UUID.randomUUID().toString();
		final String body = "{\"deviceId\":\"" + deviceId + "\",\"device\":{\"deviceId\":\"" + deviceId
				+ "\",\"name\":\"WEB\",\"type\":\"WEB\"}}";
		final String authUrl = Novo19UrlBuilder.redBeeAnonymousAuthUrl();
		try {
			final String json = transport.postJson(authUrl, body);
			final String token = Novo19PlaybackParser.parseSessionToken(json, authUrl);
			if (StringUtils.isEmpty(token)) {
				throw new IOException("missing-session-token");
			}
			return token;
		} catch (final Novo19HttpException e) {
			throw e;
		}
	}

}
