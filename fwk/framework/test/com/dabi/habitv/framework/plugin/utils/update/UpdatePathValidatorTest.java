package com.dabi.habitv.framework.plugin.utils.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UpdatePathValidatorTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void acceptsNormalJarFilename() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		final Path resolved = UpdatePathValidator.resolveArtifactFile(root, "youtube", "jar");
		assertTrue(resolved.startsWith(root.toPath().toAbsolutePath().normalize()));
		assertEquals("youtube.jar", resolved.getFileName().toString());
	}

	@Test
	public void acceptsHyphenatedArtifactId() throws IOException {
		final File root = temporaryFolder.newFolder("bin");
		final Path resolved = UpdatePathValidator.resolveArtifactFile(root, "yt-dlp", "exe");
		assertEquals("yt-dlp.exe", resolved.getFileName().toString());
	}

	@Test
	public void acceptsConsecutiveDotsInsideArtifactId() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		final Path resolved = UpdatePathValidator.resolveArtifactFile(root, "plugin..backup", "jar");
		assertEquals("plugin..backup.jar", resolved.getFileName().toString());
		assertTrue(resolved.startsWith(root.toPath().toAbsolutePath().normalize()));
	}

	@Test(expected = InvalidUpdatePathException.class)
	public void rejectsColonInArtifactId() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		UpdatePathValidator.resolveArtifactFile(root, "bad:name", "jar");
	}

	@Test(expected = InvalidUpdatePathException.class)
	public void rejectsAsteriskInArtifactId() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		UpdatePathValidator.resolveArtifactFile(root, "bad*name", "jar");
	}

	@Test(expected = InvalidUpdatePathException.class)
	public void rejectsQuestionMarkInArtifactId() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		UpdatePathValidator.resolveArtifactFile(root, "bad?name", "jar");
	}

	@Test(expected = InvalidUpdatePathException.class)
	public void rejectsStandaloneParentDirectoryArtifactId() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		UpdatePathValidator.resolveArtifactFile(root, "..", "jar");
	}

	@Test
	public void tempFileStaysUnderTrustedRoot() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		final Path trustedRoot = UpdatePathValidator.normalizeTrustedRoot(root);
		final Path artifact = UpdatePathValidator.resolveArtifactFile(root, "arte", "jar");
		final Path temp = UpdatePathValidator.resolveDownloadTempFile(artifact, trustedRoot);
		assertTrue(temp.startsWith(trustedRoot));
		assertEquals("arte.jar.tmp", temp.getFileName().toString());
	}

	@Test(expected = InvalidUpdatePathException.class)
	public void rejectsParentDirectoryTraversalUnix() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		UpdatePathValidator.resolveArtifactFile(root, "../escape", "jar");
	}

	@Test(expected = InvalidUpdatePathException.class)
	public void rejectsParentDirectoryTraversalWindows() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		UpdatePathValidator.resolveArtifactFile(root, "..\\escape", "jar");
	}

	@Test(expected = InvalidUpdatePathException.class)
	public void rejectsNestedParentDirectoryTraversal() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		UpdatePathValidator.resolveArtifactFile(root, "dir/../../escape", "jar");
	}

	@Test(expected = InvalidUpdatePathException.class)
	public void rejectsAbsolutePosixPath() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		UpdatePathValidator.resolveArtifactFile(root, "/absolute/escape", "jar");
	}

	@Test(expected = InvalidUpdatePathException.class)
	public void rejectsWindowsDriveBackslashPath() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		UpdatePathValidator.resolveArtifactFile(root, "C:\\absolute\\escape", "jar");
	}

	@Test(expected = InvalidUpdatePathException.class)
	public void rejectsWindowsDriveForwardSlashPath() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		UpdatePathValidator.resolveArtifactFile(root, "C:/absolute/escape", "jar");
	}

	@Test(expected = InvalidUpdatePathException.class)
	public void rejectsUncBackslashPath() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		UpdatePathValidator.resolveArtifactFile(root, "\\\\server\\share\\escape", "jar");
	}

	@Test(expected = InvalidUpdatePathException.class)
	public void rejectsUncForwardSlashPath() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		UpdatePathValidator.resolveArtifactFile(root, "//server/share/escape", "jar");
	}

	@Test(expected = InvalidUpdatePathException.class)
	public void rejectsBlankArtifactId() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		UpdatePathValidator.resolveArtifactFile(root, "   ", "jar");
	}

	@Test(expected = InvalidUpdatePathException.class)
	public void rejectsControlCharacterArtifactId() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		UpdatePathValidator.resolveArtifactFile(root, "bad\u0000name", "jar");
	}

	@Test
	public void sanitizeForLogRemovesControlCharactersAndNewlines() {
		assertEquals("bad?name", UpdatePathValidator.sanitizeForLog("bad\u0000name"));
		assertEquals("line?injection", UpdatePathValidator.sanitizeForLog("line\rinjection"));
		assertFalse(UpdatePathValidator.sanitizeForLog("line\rinjection").contains("\r"));
		assertFalse(UpdatePathValidator.sanitizeForLog("line\ninjection").contains("\n"));
	}

	@Test
	public void invalidEntryDoesNotCreateFilesOutsideTrustedRoot() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		final File outside = temporaryFolder.newFolder("outside");
		final Path outsideMarker = outside.toPath().resolve("escape.jar");
		Files.write(outsideMarker, "marker".getBytes(StandardCharsets.UTF_8));

		try {
			UpdatePathValidator.resolveArtifactFile(root, "../outside/escape", "jar");
		} catch (final InvalidUpdatePathException expected) {
			// expected
		}

		assertTrue(Files.exists(outsideMarker));
		assertEquals("marker", new String(Files.readAllBytes(outsideMarker), StandardCharsets.UTF_8));
		assertFalse(Files.exists(root.toPath().resolve("escape.jar")));
	}
}
