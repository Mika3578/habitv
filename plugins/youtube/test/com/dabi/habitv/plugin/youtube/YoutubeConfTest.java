package com.dabi.habitv.plugin.youtube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class YoutubeConfTest {

	@Test
	public void defaultWindowsExecutableIsYtDlpExe() {
		assertEquals("yt-dlp.exe", YoutubeConf.DEFAULT_WINDOWS_EXE);
	}

	@Test
	public void defaultLinuxBinaryIsYtDlp() {
		assertEquals("yt-dlp", YoutubeConf.DEFAULT_LINUX_BIN_PATH);
	}

	@Test
	public void defaultVideoCommandIncludesUrlAndOutputPlaceholders() {
		assertTrue(YoutubeConf.DUMP_CMD.contains("#VIDEO_URL#"));
		assertTrue(YoutubeConf.DUMP_CMD.contains("#FILE_DEST#"));
		assertTrue(YoutubeConf.DUMP_CMD.contains("--write-sub"));
		assertTrue(YoutubeConf.DUMP_CMD.contains("--write-auto-sub"));
		assertTrue(YoutubeConf.DUMP_CMD.contains("--no-check-certificate"));
	}

	@Test
	public void defaultMp3CommandIncludesExtractAudioFlags() {
		assertTrue(YoutubeConf.DUMP_CMD_MP3.contains("--extract-audio"));
		assertTrue(YoutubeConf.DUMP_CMD_MP3.contains("--audio-format mp3"));
	}

	@Test
	public void normalizeApiKeyReturnsNullForNullInput() {
		assertNull(YoutubeConf.normalizeApiKey(null));
	}

	@Test
	public void normalizeApiKeyReturnsNullForBlankInput() {
		assertNull(YoutubeConf.normalizeApiKey("   "));
	}

	@Test
	public void normalizeApiKeyTrimsWhitespace() {
		assertEquals("api-key-value", YoutubeConf.normalizeApiKey("  api-key-value  "));
	}
}
