package com.dabi.habitv.framework.plugin.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ZipUtilsTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void extractsSafeEntryUnderOutputFolder() throws IOException {
		final File outputDir = temporaryFolder.newFolder("out");
		final File zipFile = temporaryFolder.newFile("safe.zip");
		writeZip(zipFile, "nested/safe.txt", "hello");

		ZipUtils.unZipIt(zipFile, outputDir.getAbsolutePath());

		final File extracted = new File(outputDir, "nested/safe.txt");
		assertTrue(extracted.isFile());
		assertTrue(new String(Files.readAllBytes(extracted.toPath()), StandardCharsets.UTF_8).contains("hello"));
	}

	@Test
	public void rejectsZipEntryThatEscapesOutputFolder() throws IOException {
		final File outputDir = temporaryFolder.newFolder("out");
		final File outsideMarker = temporaryFolder.newFile("outside-marker.txt");
		Files.write(outsideMarker.toPath(), "untouched".getBytes(StandardCharsets.UTF_8));
		final File zipFile = temporaryFolder.newFile("evil.zip");
		writeZip(zipFile, "../outside-marker.txt", "evil");

		ZipUtils.unZipIt(zipFile, outputDir.getAbsolutePath());

		assertTrue(outsideMarker.isFile());
		assertTrue(new String(Files.readAllBytes(outsideMarker.toPath()), StandardCharsets.UTF_8).contains("untouched"));
		assertFalse(new File(outputDir, "outside-marker.txt").exists());
	}

	private static void writeZip(final File zipFile, final String entryName, final String content) throws IOException {
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
			zos.putNextEntry(new ZipEntry(entryName));
			zos.write(content.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
		}
	}
}
