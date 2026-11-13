package com.dabi.habitv.provider.rtbfauvio;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;

final class RtbfAuvioDownloadMapper {

	private RtbfAuvioDownloadMapper() {
	}

	static DownloadParamDTO buildDelegatedDownload(final DownloadParamDTO source, final String streamUrl) {
		return DownloadParamDTO.buildDownloadParam(source, streamUrl);
	}

	static String summarizeFailure(final String rootCause) {
		if (StringUtils.isEmpty(rootCause)) {
			return "unknown";
		}
		return rootCause;
	}

}
