package com.dabi.habitv.provider.rtbfauvio;

import org.apache.commons.lang.StringUtils;

final class RtbfAuvioLocalAuth {

	private RtbfAuvioLocalAuth() {
	}

	static String optionalAccountJwt() {
		final String fromEnv = System.getenv(RtbfAuvioConf.ENV_ID_TOKEN);
		if (!StringUtils.isEmpty(fromEnv)) {
			return fromEnv.trim();
		}
		final String fromProperty = System.getProperty(RtbfAuvioConf.PROPERTY_ID_TOKEN);
		if (!StringUtils.isEmpty(fromProperty)) {
			return fromProperty.trim();
		}
		return null;
	}

}
