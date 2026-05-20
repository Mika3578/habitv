package com.dabi.habitv.provider.arte;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.junit.After;
import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.holder.ProcessHolder;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.utils.CmdExecutor;

/**
 * Opt-in live download test: ARTE episode URL via {@link ArtePluginManager}
 * delegating to yt-dlp through the "youtube" downloader key.
 *
 * <p>Run only when explicitly enabled (network + yt-dlp + ffmpeg required):
 * <pre>
 * mvn -B -ntp -pl plugins/arte -am \
 *   "-Dhabitv.liveTests=true" \
 *   "-Dtest=ArteYtDlpLiveDownloadTest" \
 *   "-Dsurefire.failIfNoSpecifiedTests=false" test
 * </pre>
 * Optional: {@code -Dhabitv.ytdlp.path=/path/to/yt-dlp}
 */
public class ArteYtDlpLiveDownloadTest {

	private static final String LIVE_TESTS_PROPERTY = "habitv.liveTests";
	private static final String YT_DLP_PATH_PROPERTY = "habitv.ytdlp.path";

	private static final boolean IS_WINDOWS = System.getProperty("os.name", "").toLowerCase().contains("win");
	private static final String CMD_PROCESSOR = IS_WINDOWS ? "cmd.exe /c #CMD#" : "/bin/sh -c #CMD#";

	/** Stable replay URL; 15s low-quality clip keeps bandwidth small. */
	private static final String SAMPLE_EPISODE_URL = "https://www.arte.tv/fr/videos/019729-000-A/talons-aiguilles/";

	private static final String YT_DLP_ARGS = " \"#VIDEO_URL#\" -o \"#FILE_DEST#\""
			+ " -f VF-STF-426+VF-STF-audio_0-fran\u00e7ais"
			+ " --download-sections \"*0:00-0:15\""
			+ " --no-check-certificate --no-write-sub --no-write-auto-sub";

	private File outputDir;

	@After
	public void tearDown() {
		if (outputDir == null || !outputDir.exists()) {
			return;
		}
		final File[] files = outputDir.listFiles();
		if (files != null) {
			for (final File file : files) {
				if (!file.delete()) {
					file.deleteOnExit();
				}
			}
		}
		if (!outputDir.delete()) {
			outputDir.deleteOnExit();
		}
	}

	@Test
	public void downloadsShortArteClipViaYtDlp() throws DownloadFailedException, IOException {
		assumeTrue("Set -Dhabitv.liveTests=true to run network live tests",
				"true".equalsIgnoreCase(System.getProperty(LIVE_TESTS_PROPERTY)));

		final String ytDlpBinary = resolveYtDlpBinary();
		assumeNotNull("yt-dlp not found; install it or set -D" + YT_DLP_PATH_PROPERTY + "=...", ytDlpBinary);

		outputDir = new File("target/arte-ytdlp-live-test");
		if (!outputDir.exists() && !outputDir.mkdirs()) {
			throw new IOException("cannot create output dir: " + outputDir.getAbsolutePath());
		}

		final String outputPath = new File(outputDir, "arte-live-clip.tmp.mp4").getAbsolutePath();
		final CategoryDTO category = new CategoryDTO(ArteConf.NAME, "live-test", SAMPLE_EPISODE_URL, ArteConf.EXTENSION);
		final EpisodeDTO episode = new EpisodeDTO(category, "Talons aiguilles", SAMPLE_EPISODE_URL);
		final DownloadParamDTO downloadParam = new DownloadParamDTO(episode.getId(), outputPath, ArteConf.EXTENSION);
		downloadParam.addParam(FrameworkConf.PARAMETER_ARGS, YT_DLP_ARGS);

		final Map<String, com.dabi.habitv.api.plugin.api.PluginDownloaderInterface> downloaders = new HashMap<>();
		downloaders.put("youtube", new YtDlpPassthroughDownloader());
		final Map<String, String> binPaths = new HashMap<>();
		binPaths.put("youtube", ytDlpBinary);
		final DownloaderPluginHolder holder = new DownloaderPluginHolder(CMD_PROCESSOR,
				downloaders, binPaths, outputDir.getAbsolutePath(), outputDir.getAbsolutePath(),
				outputDir.getAbsolutePath(), outputDir.getAbsolutePath());

		final ArtePluginManager provider = new ArtePluginManager();
		final ProcessHolder process = provider.download(downloadParam, holder);
		process.start();

		final File outputFile = findDownloadedMedia(outputDir);
		assertTrue("expected a non-empty mp4 under " + outputDir.getAbsolutePath()
				+ " (yt-dlp may name the file from the video title)", outputFile != null && outputFile.length() > 0);
	}

	private static File findDownloadedMedia(final File directory) {
		final File[] files = directory.listFiles();
		if (files == null) {
			return null;
		}
		File best = null;
		for (final File file : files) {
			if (!file.isFile()) {
				continue;
			}
			final String name = file.getName().toLowerCase();
			if (!name.endsWith(".mp4") && !name.endsWith(".mp4.part")) {
				continue;
			}
			if (best == null || file.length() > best.length()) {
				best = file;
			}
		}
		return best != null && best.getName().endsWith(".part") ? new File(best.getParentFile(),
				best.getName().substring(0, best.getName().length() - 5)) : best;
	}

	private static String resolveYtDlpBinary() throws IOException {
		final String configured = System.getProperty(YT_DLP_PATH_PROPERTY);
		if (configured != null && !configured.trim().isEmpty()) {
			final File file = new File(configured.trim());
			assumeTrue("habitv.ytdlp.path does not exist: " + file.getAbsolutePath(), file.isFile());
			return file.getAbsolutePath();
		}

		final String[] command = IS_WINDOWS
				? new String[] { "cmd.exe", "/c", "where yt-dlp" }
				: new String[] { "sh", "-c", "command -v yt-dlp" };

		final Process process = Runtime.getRuntime().exec(command);
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
			final String line = reader.readLine();
			process.waitFor();
			if (line == null || line.trim().isEmpty()) {
				return null;
			}
			final File candidate = new File(line.trim());
			return candidate.isFile() ? candidate.getAbsolutePath() : line.trim();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
	}

	private static final class YtDlpPassthroughDownloader implements com.dabi.habitv.api.plugin.api.PluginDownloaderInterface {

		@Override
		public String getName() {
			return "youtube";
		}

		@Override
		public DownloadableState canDownload(final String downloadInput) {
			return DownloadableState.SPECIFIC;
		}

		@Override
		public ProcessHolder download(final DownloadParamDTO downloadParam, final DownloaderPluginHolder downloaders)
				throws DownloadFailedException {
			String cmd = downloaders.getBinPath("youtube") + " ";
			final String cmdParam = downloadParam.getParam(FrameworkConf.PARAMETER_ARGS);
			cmd += cmdParam;
			cmd = cmd.replaceFirst(FrameworkConf.DOWNLOAD_INPUT,
					Matcher.quoteReplacement(downloadParam.getDownloadInput()));
			cmd = cmd.replaceFirst(FrameworkConf.DOWNLOAD_DESTINATION,
					Matcher.quoteReplacement(downloadParam.getDownloadOutput()));
			return new CmdExecutor(downloaders.getCmdProcessor(), cmd, -1);
		}
	}
}
