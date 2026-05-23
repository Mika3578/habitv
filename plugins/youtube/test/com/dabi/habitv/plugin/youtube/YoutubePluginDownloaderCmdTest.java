package com.dabi.habitv.plugin.youtube;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface;
import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface.DownloadableState;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.plugin.utils.CmdExecutor;

public class YoutubePluginDownloaderCmdTest {

	@Before
	public void disablePreflight() {
		YtDlpRuntimeDiagnostics.setPreflightEnabled(false);
	}

	@After
	public void restorePreflight() {
		YtDlpRuntimeDiagnostics.setPreflightEnabled(true);
	}

	@Test
	public void youtubeUrlIsAcceptedBySpecificDownloader() {
		final YoutubePluginDownloader downloader = new YoutubePluginDownloader();
		assertEquals(DownloadableState.SPECIFIC,
				downloader.canDownload("https://www.youtube.com/watch?v=jNQXAC9IVRw"));
		assertEquals(DownloadableState.SPECIFIC,
				downloader.canDownload("https://youtu.be/jNQXAC9IVRw"));
		assertEquals(DownloadableState.IMPOSSIBLE,
				downloader.canDownload("https://example.org/foo"));
	}

	@Test
	public void getFilesToUpdateReturnsYtDlpArtifactId() {
		final YoutubePluginDownloader downloader = new YoutubePluginDownloader();
		assertArrayEquals(new String[] { "yt-dlp" }, downloader.getFilesToUpdate());
	}

	@Test
	public void downloadReturnsAProcessHolderWithConfiguredYtDlpBinary() throws Exception {
		final HashMap<String, String> downloaderName2Bin = new HashMap<>();
		downloaderName2Bin.put(YoutubeConf.NAME, "/usr/bin/yt-dlp");
		final DownloaderPluginHolder downloaders = new DownloaderPluginHolder(
				"/bin/sh -c #CMD#",
				Collections.<String, PluginDownloaderInterface>emptyMap(),
				downloaderName2Bin, "/tmp/out", "/tmp/idx", "/tmp/bin",
				"/tmp/plugins");

		final DownloadParamDTO param = new DownloadParamDTO(
				"https://www.youtube.com/watch?v=jNQXAC9IVRw",
				"/tmp/out/test.mp4", "mp4");

		final ProcessHolder holder = new YoutubePluginDownloader()
				.download(param, downloaders);
		assertNotNull(holder);
		assertTrue(holder instanceof YtDlpCmdExecutor);
		final String cmd = readCmd(holder);
		assertTrue(cmd.startsWith("/usr/bin/yt-dlp "));
		assertTrue(cmd.contains("https://www.youtube.com/watch?v=jNQXAC9IVRw"));
		assertTrue(cmd.contains("/tmp/out/test.mp4"));
		assertTrue(cmd.contains("-f \"bv*[ext=mp4]+ba[ext=m4a]/b[ext=mp4]/bv*+ba/b\""));
		assertTrue(cmd.contains("--merge-output-format mp4"));
		assertTrue(cmd.contains("--no-check-certificate"));
		assertFalse(cmd.contains("--audio-quality"));
		assertFalse(cmd.contains("--extract-audio"));
		assertFalse(cmd.contains("-x"));
		assertFalse(cmd.contains("--audio-format"));
		assertFalse(cmd.contains("--write-sub"));
		assertFalse(cmd.contains("--write-subs"));
		assertFalse(cmd.contains("--write-auto-sub"));
		assertFalse(cmd.contains("--write-auto-subs"));
		assertFalse(cmd.contains("--embed-subs"));
	}

	@Test
	public void downloadDailymotionUrlUsesSameDefaultVideoSelector() throws Exception {
		final HashMap<String, String> downloaderName2Bin = new HashMap<>();
		downloaderName2Bin.put(YoutubeConf.NAME, "/usr/bin/yt-dlp");
		final DownloaderPluginHolder downloaders = new DownloaderPluginHolder(
				"/bin/sh -c #CMD#",
				Collections.<String, PluginDownloaderInterface>emptyMap(),
				downloaderName2Bin, "/tmp/out", "/tmp/idx", "/tmp/bin",
				"/tmp/plugins");

		final DownloadParamDTO param = new DownloadParamDTO(
				"https://www.dailymotion.com/video/x9example",
				"/tmp/out/dm-test.mp4", "mp4");

		final ProcessHolder holder = new YoutubePluginDownloader()
				.download(param, downloaders);
		final String cmd = readCmd(holder);
		assertTrue(cmd.contains("-f \"bv*[ext=mp4]+ba[ext=m4a]/b[ext=mp4]/bv*+ba/b\""));
		assertTrue(cmd.contains("--merge-output-format mp4"));
		assertFalse(cmd.contains("--write-subs"));
		assertFalse(cmd.contains("--write-auto-subs"));
	}

	@Test
	public void downloadWithMp3ArgsIncludesExtractAudioFlags() throws Exception {
		final HashMap<String, String> downloaderName2Bin = new HashMap<>();
		downloaderName2Bin.put(YoutubeConf.NAME, "/usr/bin/yt-dlp");
		final DownloaderPluginHolder downloaders = new DownloaderPluginHolder(
				"/bin/sh -c #CMD#",
				Collections.<String, PluginDownloaderInterface>emptyMap(),
				downloaderName2Bin, "/tmp/out", "/tmp/idx", "/tmp/bin",
				"/tmp/plugins");

		final DownloadParamDTO param = new DownloadParamDTO(
				"https://www.youtube.com/watch?v=jNQXAC9IVRw",
				"/tmp/out/test.mp3", "mp3");
		param.addParam(com.dabi.habitv.framework.FrameworkConf.PARAMETER_ARGS,
				YoutubeConf.DUMP_CMD_MP3);

		final ProcessHolder holder = new YoutubePluginDownloader()
				.download(param, downloaders);
		final String cmd = readCmd(holder);
		assertTrue(cmd.contains("--extract-audio"));
		assertTrue(cmd.contains("--audio-format mp3"));
	}

	private static String readCmd(final ProcessHolder holder) throws Exception {
		final Field field = CmdExecutor.class.getDeclaredField("cmd");
		field.setAccessible(true);
		return (String) field.get(holder);
	}
}
