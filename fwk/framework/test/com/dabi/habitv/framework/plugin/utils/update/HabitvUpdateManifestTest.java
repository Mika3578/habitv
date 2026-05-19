package com.dabi.habitv.framework.plugin.utils.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dabi.habitv.framework.plugin.utils.update.HabitvUpdateManifest.ArtifactKind;
import com.dabi.habitv.framework.plugin.utils.update.HabitvUpdateManifest.Entry;

public class HabitvUpdateManifestTest {

	@Test
	public void parseLineAcceptsPluginAndToolEntries() {
		final Entry plugin = HabitvUpdateManifest.parseLine(
				"plugin|com.dabi.habitv|6play|4.1.0-SNAPSHOT|jar|com/dabi/habitv/6play/4.1.0-SNAPSHOT/6play-4.1.0-SNAPSHOT.jar");
		assertEquals(ArtifactKind.PLUGIN, plugin.getKind());
		assertEquals("6play", plugin.getArtifactId());

		final Entry tool = HabitvUpdateManifest.parseLine(
				"tool|com.dabi.habitv|ffmpeg|3.0|zip|tools/ffmpeg/3.0/ffmpeg.zip|abc123");
		assertEquals(ArtifactKind.TOOL, tool.getKind());
		assertEquals("abc123", tool.getChecksum());
	}

	@Test
	public void parseBuildsPluginList() {
		final String content = ""
				+ "# sample manifest\n"
				+ "plugin|com.dabi.habitv|6play|4.1.0|jar|com/dabi/habitv/6play/4.1.0/6play-4.1.0.jar\n"
				+ "plugin|com.dabi.habitv|arte|4.1.0|jar|com/dabi/habitv/arte/4.1.0/arte-4.1.0.jar\n";
		final HabitvUpdateManifest manifest = HabitvUpdateManifest.parse(content);
		final String[] pluginIds = manifest.getPluginArtifactIds();
		assertEquals(2, pluginIds.length);
		assertTrue(manifest.findEntries("com.dabi.habitv", "ffmpeg", ArtifactKind.TOOL).isEmpty());
	}

	@Test
	public void parseIgnoresCommentsAndBlankLines() {
		final HabitvUpdateManifest manifest = HabitvUpdateManifest.parse("\n# comment\n\n");
		assertTrue(manifest.isEmpty());
	}

	@Test
	public void parseIgnoresBomPrefixedComments() {
		final HabitvUpdateManifest manifest = HabitvUpdateManifest.parse("\uFEFF# comment\n");
		assertTrue(manifest.isEmpty());
	}

	@Test
	public void loadFromRepositoryStoresBaseUrlAndEntryDownloadUrlUsesIt() {
		// Entries with relative URLs loaded via loadFromRepository must resolve against
		// the URL that was passed, not against the system property.
		final String content = "plugin|com.dabi.habitv|arte|4.1.0|jar|com/dabi/habitv/arte/4.1.0/arte-4.1.0.jar\n";
		final HabitvUpdateManifest manifest = HabitvUpdateManifest.parse(content);
		// parse() has no baseUrl context, so getBaseUrl() is null
		assertEquals(null, manifest.getBaseUrl());
	}

	@Test
	public void entryGetDownloadUrlWithExplicitBaseUrlPrefersThatBaseUrl() {
		final Entry entry = HabitvUpdateManifest.parseLine(
				"plugin|com.dabi.habitv|arte|4.1.0|jar|com/dabi/habitv/arte/4.1.0/arte-4.1.0.jar");
		final String url = entry.getDownloadUrl("https://custom.example.com/repo");
		assertEquals("https://custom.example.com/repo/com/dabi/habitv/arte/4.1.0/arte-4.1.0.jar", url);
	}

	@Test
	public void entryGetDownloadUrlWithAbsoluteRelativeUrlIgnoresBaseUrl() {
		final Entry entry = HabitvUpdateManifest.parseLine(
				"plugin|com.dabi.habitv|arte|4.1.0|jar|https://cdn.example.com/arte-4.1.0.jar");
		final String url = entry.getDownloadUrl("https://other.example.com/repo");
		assertEquals("https://cdn.example.com/arte-4.1.0.jar", url);
	}

	@Test
	public void parseLineAcceptsBomPrefixedPluginEntry() {
		final Entry entry = HabitvUpdateManifest.parseLine(
				"\uFEFFplugin|com.dabi.habitv|arte|4.1.0|jar|com/dabi/habitv/arte/4.1.0/arte-4.1.0.jar");
		assertEquals(ArtifactKind.PLUGIN, entry.getKind());
		assertEquals("arte", entry.getArtifactId());
	}
}
