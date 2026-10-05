package com.dabi.habitv.plugin.youtube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.dabi.habitv.api.plugin.holder.DownloadProgressSnapshot;
import com.dabi.habitv.api.plugin.holder.DownloadStage;

public class YoutubeConfTest {

	private static final String WINDOWS_CMD_PROCESSOR = "cmd.exe /c #CMD#";
	private static final String UNIX_CMD_PROCESSOR = "/bin/sh -c #CMD#";
	private static final String UNIX_JSON_DEST = "\\\"dest\\\":%(progress.filename)j";
	private static final String WINDOWS_JSON_DEST = "\"\"dest\"\":%(progress.filename)j";

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
		assertTrue(YoutubeConf.DUMP_CMD.contains("-f \"bv*[ext=mp4]+ba[ext=m4a]/b[ext=mp4]/bv*+ba/b\""));
		assertTrue(YoutubeConf.DUMP_CMD.contains("--merge-output-format mp4"));
		assertTrue(YoutubeConf.DUMP_CMD.contains("--newline"));
		assertTrue(YoutubeConf.DUMP_CMD.contains("--no-check-certificate"));
		assertFalse(YoutubeConf.DUMP_CMD.contains("--progress-template"));
		assertTrue(YoutubeConf.augmentBuiltInDumpCommand(YoutubeConf.DUMP_CMD, "2024.01.01", "/bin/sh -c #CMD#")
				.contains("--progress-template"));
		assertTrue(YoutubeConf.augmentBuiltInDumpCommand(YoutubeConf.DUMP_CMD, "2023.09.01", "/bin/sh -c #CMD#")
				.equals(YoutubeConf.DUMP_CMD));
		assertTrue(YoutubeConf.progressTemplateFlagsFor(WINDOWS_CMD_PROCESSOR).contains("%(progress._percent)j"));
		assertFalse(YoutubeConf.progressTemplateFlagsFor(WINDOWS_CMD_PROCESSOR).contains("%(progress._percent)s"));
		assertTrue(YoutubeConf.progressTemplateFlagsFor(UNIX_CMD_PROCESSOR).contains(UNIX_JSON_DEST));
		assertTrue(YoutubeConf.progressTemplateFlagsFor(WINDOWS_CMD_PROCESSOR).contains(WINDOWS_JSON_DEST));
		assertFalse(YoutubeConf.DUMP_CMD.contains("--audio-quality"));
		assertFalse(YoutubeConf.DUMP_CMD.contains("--extract-audio"));
		assertFalse(YoutubeConf.DUMP_CMD.contains("-x"));
		assertFalse(YoutubeConf.DUMP_CMD.contains("--audio-format"));
		assertFalse(YoutubeConf.DUMP_CMD.contains("--write-sub"));
		assertFalse(YoutubeConf.DUMP_CMD.contains("--write-subs"));
		assertFalse(YoutubeConf.DUMP_CMD.contains("--write-auto-sub"));
		assertFalse(YoutubeConf.DUMP_CMD.contains("--write-auto-subs"));
		assertFalse(YoutubeConf.DUMP_CMD.contains("--all-subs"));
		assertFalse(YoutubeConf.DUMP_CMD.contains("--embed-subs"));
	}

	@Test
	public void unixProgressTemplateKeepsBackslashJsonQuotes() {
		final String flags = YoutubeConf.progressTemplateFlagsFor(UNIX_CMD_PROCESSOR);
		assertEquals(YoutubeConf.PROGRESS_TEMPLATE_FLAGS, flags);
		assertTrue(flags.contains(UNIX_JSON_DEST));
		assertFalse(flags.contains(WINDOWS_JSON_DEST));
		assertTrue(flags.contains("%(progress._percent)j"));
		assertFalse(flags.contains("%%(progress._percent)j"));
	}

	@Test
	public void windowsProgressTemplateUsesDoubledJsonQuotes() {
		final String flags = YoutubeConf.progressTemplateFlagsFor(WINDOWS_CMD_PROCESSOR);
		assertTrue(flags.contains(WINDOWS_JSON_DEST));
		assertFalse(flags.contains(UNIX_JSON_DEST));
		assertTrue(flags.contains("%(progress._percent)j"));
		assertFalse(flags.contains("%%(progress._percent)j"));
		assertTrue(YoutubeConf.progressTemplateFlagsFor("C:\\Windows\\System32\\cmd.exe /c #CMD#")
				.contains(WINDOWS_JSON_DEST));
		assertTrue(YoutubeConf.progressTemplateFlagsFor("CMD.EXE /c #CMD#").contains(WINDOWS_JSON_DEST));
	}

	@Test
	public void windowsCmdConstructionYieldsParseableProgressJson() {
		assertProgressTemplatesParseAfterUnquote(WINDOWS_CMD_PROCESSOR, true);
	}

	@Test
	public void unixCmdConstructionYieldsParseableProgressJson() {
		assertProgressTemplatesParseAfterUnquote(UNIX_CMD_PROCESSOR, false);
	}

	private static void assertProgressTemplatesParseAfterUnquote(final String cmdProcessor,
			final boolean windowsCmd) {
		final String command = YoutubeConf.augmentBuiltInDumpCommand(YoutubeConf.DUMP_CMD, "2024.01.01",
				cmdProcessor);
		final String afterTokenReplace = replaceCmdToken(cmdProcessor, command);
		final List<String> args = windowsCmd ? tokenizeCmdExe(afterTokenReplace) : tokenizeSh(afterTokenReplace);
		final List<String> templates = progressTemplateValues(args);
		assertEquals(2, templates.size());

		final String downloadLine = instantiatePlaceholders(templates.get(0));
		assertTrue(downloadLine.contains(YoutubeConf.PROGRESS_LINE_PREFIX));
		final DownloadProgressSnapshot download = YtDlpProgressParser.parse(downloadLine, null);
		assertNotNull(download);
		assertEquals(DownloadStage.DOWNLOADING, download.getStage());
		assertEquals(0.114d, download.getProgressRatio().doubleValue(), 0.000001d);
		assertEquals("Vidéo", download.getDetail());

		final String postprocessLine = instantiatePlaceholders(templates.get(1));
		final DownloadProgressSnapshot post = YtDlpProgressParser.parse(postprocessLine, download);
		assertNotNull(post);
		assertEquals(DownloadStage.MERGING, post.getStage());
		assertTrue(post.isIndeterminate());
	}

	private static String replaceCmdToken(final String cmdProcessor, final String cmd) {
		final String[] cmdArgs = cmdProcessor.split(" ");
		for (int i = 0; i < cmdArgs.length; i++) {
			if (cmdArgs[i].contains("#CMD#")) {
				cmdArgs[i] = cmdArgs[i].replace("#CMD#", cmd);
			}
		}
		return cmdArgs[cmdArgs.length - 1];
	}

	private static List<String> progressTemplateValues(final List<String> args) {
		final List<String> values = new ArrayList<String>();
		for (int i = 0; i < args.size(); i++) {
			if ("--progress-template".equals(args.get(i)) && i + 1 < args.size()) {
				values.add(args.get(i + 1));
			}
		}
		return values;
	}

	private static String instantiatePlaceholders(final String template) {
		return template.replace("%(progress._percent)j", "11.4")
				.replace("%(progress._total_bytes_str)s", "159.60MiB")
				.replace("%(progress._speed_str)s", "7.80MiB/s")
				.replace("%(progress._eta_str)s", "00:18")
				.replace("%(progress.filename)j", "\"episode.f137.mp4\"")
				.replace("%(postprocessor)s", "Merger");
	}

	/**
	 * cmd.exe {@code /c} quote rules: {@code "} toggles quoting and {@code ""} emits a literal quote.
	 */
	private static List<String> tokenizeCmdExe(final String command) {
		final List<String> args = new ArrayList<String>();
		final StringBuilder current = new StringBuilder();
		boolean inQuotes = false;
		for (int i = 0; i < command.length(); i++) {
			final char c = command.charAt(i);
			if (c == '"') {
				if (inQuotes && i + 1 < command.length() && command.charAt(i + 1) == '"') {
					current.append('"');
					i++;
				} else {
					inQuotes = !inQuotes;
				}
			} else if ((c == ' ' || c == '\t') && !inQuotes) {
				if (current.length() > 0) {
					args.add(current.toString());
					current.setLength(0);
				}
			} else {
				current.append(c);
			}
		}
		if (current.length() > 0) {
			args.add(current.toString());
		}
		return args;
	}

	/** POSIX {@code sh -c} double-quote rules: {@code \"} emits a literal quote. */
	private static List<String> tokenizeSh(final String command) {
		final List<String> args = new ArrayList<String>();
		final StringBuilder current = new StringBuilder();
		boolean inDouble = false;
		for (int i = 0; i < command.length(); i++) {
			final char c = command.charAt(i);
			if (c == '\\' && inDouble && i + 1 < command.length()) {
				final char next = command.charAt(i + 1);
				if (next == '"' || next == '\\' || next == '$' || next == '`') {
					current.append(next);
					i++;
					continue;
				}
			}
			if (c == '"') {
				inDouble = !inDouble;
				continue;
			}
			if ((c == ' ' || c == '\t') && !inDouble) {
				if (current.length() > 0) {
					args.add(current.toString());
					current.setLength(0);
				}
				continue;
			}
			current.append(c);
		}
		if (current.length() > 0) {
			args.add(current.toString());
		}
		return args;
	}

	@Test
	public void defaultMp3CommandIncludesExtractAudioFlags() {
		assertTrue(YoutubeConf.DUMP_CMD_MP3.contains("--extract-audio"));
		assertTrue(YoutubeConf.DUMP_CMD_MP3.contains("--audio-format mp3"));
		assertTrue(YoutubeConf.DUMP_CMD_MP3.contains("--newline"));
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
	public void normalizeApiKeyTrimsWhitespaceAroundGoogleKey() {
		final String key = YoutubeTestSecrets.googleApiKeyPlaceholder();
		assertEquals(key, YoutubeConf.normalizeApiKey("  " + key + "  "));
	}

	@Test
	public void normalizeApiKeyExtractsEmbeddedGoogleKeyFromPathPrefix() {
		final String key = YoutubeTestSecrets.googleApiKeyPlaceholder();
		assertEquals(key, YoutubeConf.normalizeApiKey("C:/Users/example/habitv/" + key));
	}

	@Test
	public void normalizeApiKeyReturnsNullForPathWithoutGoogleKey() {
		assertNull(YoutubeConf.normalizeApiKey("C:/Users/example/habitv/"));
	}

	@Test
	public void apiKeySkipReasonReportsInvalidKeyWhenPathOnlyConfigured() {
		System.setProperty("habitv.youtube.apiKey", "C:/Users/example/habitv/");
		try {
			assertEquals(YoutubeDataApiSupport.INVALID_API_KEY_MESSAGE, YoutubeConf.apiKeySkipReason());
		} finally {
			System.clearProperty("habitv.youtube.apiKey");
		}
	}
}
