package com.dabi.habitv.framework.plugin.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.dabi.habitv.api.plugin.exception.TechnicalException;

public class ZipUtils {

	public static void unZipIt(File zipFile, String outputFolder) {
		byte[] buffer = new byte[1024];
		File folder = new File(outputFolder);
		if (!folder.exists()) {
			folder.mkdir();
		}
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
			final File outputRoot = folder.getCanonicalFile();
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				if (!ze.isDirectory()) {
					extractEntry(zis, buffer, outputRoot, ze);
				}
				zis.closeEntry();
				ze = zis.getNextEntry();
			}
		} catch (IOException ex) {
			throw new TechnicalException(ex);
		}
	}

	private static void extractEntry(final ZipInputStream zis, final byte[] buffer, final File outputRoot,
			final ZipEntry ze) throws IOException {
		final File target = safeTarget(outputRoot, ze.getName());
		target.getParentFile().mkdirs();
		try (FileOutputStream fos = new FileOutputStream(target)) {
			int len;
			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
		}
	}

	private static File safeTarget(final File outputRoot, final String entryName) throws IOException {
		// Canonical paths resolve symlinks and "..", so the prefix check cannot be
		// bypassed by traversal or alternate representations of the root.
		final File target = new File(outputRoot, entryName);
		final File canonical = target.getCanonicalFile();
		if (!canonical.getPath().startsWith(outputRoot.getPath() + File.separator)) {
			throw new IOException("Rejected zip entry outside output folder: " + entryName);
		}
		return canonical;
	}

}