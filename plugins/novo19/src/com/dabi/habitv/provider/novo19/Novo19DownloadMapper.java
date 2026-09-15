package com.dabi.habitv.provider.novo19;

import org.apache.commons.lang.StringUtils;

import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.framework.FrameworkConf;

final class Novo19DownloadMapper {

	private Novo19DownloadMapper() {
	}

	static DownloadParamDTO buildDelegatedDownload(final DownloadParamDTO source, final String streamUrl) {
		final DownloadParamDTO delegated = DownloadParamDTO.buildDownloadParam(source, streamUrl);
		if (isAudioContent(source)) {
			delegated.addParam(FrameworkConf.PARAMETER_ARGS, Novo19Conf.PODCAST_YT_DLP_ARGS);
		}
		return delegated;
	}

	static boolean isAudioContent(final DownloadParamDTO downloadParam) {
		if (downloadParam == null) {
			return false;
		}
		if ("true".equalsIgnoreCase(downloadParam.getParam(Novo19Conf.PARAMETER_AUDIO_CONTENT))) {
			return true;
		}
		return Novo19Conf.CONTENT_KIND_PODCAST
				.equals(downloadParam.getParam(Novo19Conf.PARAMETER_CONTENT_KIND));
	}

	static String summarizeFailure(final String rootCause) {
		if (StringUtils.isEmpty(rootCause)) {
			return "unknown";
		}
		return rootCause;
	}

}
