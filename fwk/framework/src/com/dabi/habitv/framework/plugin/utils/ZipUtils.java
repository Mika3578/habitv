package com.dabi.habitv.framework.plugin.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {

	public static void unZipIt(File zipFile, String outputFolder) {
		byte[] buffer = new byte[1024];
		File folder = new File(outputFolder);
		if (!folder.exists()) {
			folder.mkdir();
		}
		final Path outputRoot = folder.toPath().toAbsolutePath().normalize();

		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				if (!ze.isDirectory()) {
					Path resolved = outputRoot.resolve(ze.getName()).normalize();
					ensureUnderOutputRoot(outputRoot, resolved, ze.getName());
					File newFile = resolved.toFile();
					new File(newFile.getParent()).mkdirs();
					try (FileOutputStream fos = new FileOutputStream(newFile)) {
						int len;
						while ((len = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
					}
				}
				zis.closeEntry();
				ze = zis.getNextEntry();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static void ensureUnderOutputRoot(final Path outputRoot, final Path resolved, final String entryName)
			throws IOException {
		if (!resolved.startsWith(outputRoot)) {
			throw new IOException("Rejected zip entry outside output folder: " + entryName);
		}
		final Path relative = outputRoot.relativize(resolved);
		for (int i = 0; i < relative.getNameCount(); i++) {
			if ("..".equals(relative.getName(i).toString())) {
				throw new IOException("Rejected zip entry with parent-directory segment: " + entryName);
			}
		}
	}

}
