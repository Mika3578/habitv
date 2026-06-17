package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.framework.FrameworkConf;

public class Novo19DownloadMapperTest {

	@Test
	public void mapsPodcastToAudioOnlyYtDlpArgs() {
		final DownloadParamDTO source = new DownloadParamDTO("https://novo19.ouest-france.fr/player/podcast", "out.mp4",
				Novo19Conf.EXTENSION);
		source.addParam(Novo19Conf.PARAMETER_CONTENT_KIND, Novo19Conf.CONTENT_KIND_PODCAST);
		source.addParam(Novo19Conf.PARAMETER_AUDIO_CONTENT, "true");
		final DownloadParamDTO delegated = Novo19DownloadMapper.buildDelegatedDownload(source,
				"https://cdn.example.test/replay.m3u8");
		assertEquals("https://cdn.example.test/replay.m3u8", delegated.getDownloadInput());
		assertTrue(delegated.getParam(FrameworkConf.PARAMETER_ARGS).contains("--extract-audio"));
		assertTrue(delegated.getParam(FrameworkConf.PARAMETER_ARGS).contains("--audio-format mp3"));
	}

	@Test
	public void keepsVideoDownloadWithoutCustomArgs() {
		final DownloadParamDTO source = new DownloadParamDTO("https://novo19.ouest-france.fr/player/episode", "out.mp4",
				Novo19Conf.EXTENSION);
		source.addParam(FrameworkConf.PARAMETER_EMBED_SUBTITLES, "true");
		final DownloadParamDTO delegated = Novo19DownloadMapper.buildDelegatedDownload(source,
				"https://cdn.example.test/replay.m3u8");
		assertFalse(delegated.getParams().containsKey(FrameworkConf.PARAMETER_ARGS));
		assertEquals("true", delegated.getParam(FrameworkConf.PARAMETER_EMBED_SUBTITLES));
	}

	@Test
	public void summarizesHttpFailures() {
		assertEquals("http-403", Novo19DownloadMapper.summarizeFailure(Novo19HttpStatus.summarizeFailure(403)));
	}

}
