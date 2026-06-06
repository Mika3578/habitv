package com.dabi.habitv.framework.plugin.utils.update;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Validates updater-controlled path components before they are used in local
 * filesystem operations. Remote plugin lists, manifests and artifact metadata
 * must pass through this boundary.
 */
public final class UpdatePathValidator {

	private static final String WINDOWS_FORBIDDEN_FILENAME_CHARS = "<>:\"|?*";

	private static final Pattern WINDOWS_DRIVE_PATH = Pattern.compile("^[A-Za-z]:[/\\\\].*");

	private static final Pattern UNC_PATH = Pattern.compile("^[/\\\\]{2}[^/\\\\]+[/\\\\].*");

	private UpdatePathValidator() {
	}

	public static Path resolveArtifactFile(final File trustedRootDir, final String artifactId, final String extension) {
		validateFilenameComponent(artifactId, "artifactId");
		validateFilenameComponent(extension, "extension");
		final Path normalizedRoot = normalizeTrustedRoot(trustedRootDir);
		final Path resolved = normalizedRoot.resolve(artifactId + "." + extension).normalize();
		ensureUnderTrustedRoot(normalizedRoot, resolved, artifactId + "." + extension);
		return resolved;
	}

	public static Path resolveDownloadTempFile(final Path validatedArtifactFile, final Path normalizedRoot) {
		if (validatedArtifactFile == null) {
			throw new InvalidUpdatePathException("Validated artifact path is required.");
		}
		final Path resolved = validatedArtifactFile.resolveSibling(
				validatedArtifactFile.getFileName().toString() + ".tmp").normalize();
		ensureUnderTrustedRoot(normalizedRoot, resolved, validatedArtifactFile.getFileName() + ".tmp");
		return resolved;
	}

	public static Path normalizeTrustedRoot(final File trustedRootDir) {
		if (trustedRootDir == null) {
			throw new InvalidUpdatePathException("Trusted updater root is required.");
		}
		return trustedRootDir.toPath().toAbsolutePath().normalize();
	}

	public static void validateFilenameComponent(final String value, final String fieldName) {
		if (value == null || value.trim().isEmpty()) {
			throw new InvalidUpdatePathException("Rejected blank " + fieldName + " for updater path resolution.");
		}
		if (containsUnsafeControlCharacter(value)) {
			throw new InvalidUpdatePathException("Rejected " + fieldName + " containing unsafe control characters.");
		}
		if (value.indexOf('/') >= 0 || value.indexOf('\\') >= 0) {
			throw new InvalidUpdatePathException("Rejected " + fieldName + " containing path separators.");
		}
		rejectWindowsForbiddenFilenameCharacters(value, fieldName);
		if (value.startsWith("/") || value.startsWith("\\")) {
			throw new InvalidUpdatePathException("Rejected absolute " + fieldName + " path.");
		}
		if (WINDOWS_DRIVE_PATH.matcher(value).matches()) {
			throw new InvalidUpdatePathException("Rejected Windows drive-qualified " + fieldName + " path.");
		}
		if (UNC_PATH.matcher(value).matches()) {
			throw new InvalidUpdatePathException("Rejected UNC " + fieldName + " path.");
		}
		final Path candidate = parsePath(value, fieldName);
		rejectParentDirectorySegments(candidate, fieldName);
		if (candidate.isAbsolute()) {
			throw new InvalidUpdatePathException("Rejected absolute " + fieldName + " path.");
		}
	}

	private static void rejectParentDirectorySegments(final Path candidate, final String fieldName) {
		for (int i = 0; i < candidate.getNameCount(); i++) {
			final String segment = candidate.getName(i).toString();
			if (".".equals(segment) || "..".equals(segment)) {
				throw new InvalidUpdatePathException(
						"Rejected parent-directory segment in " + fieldName + " path.");
			}
		}
	}

	public static String sanitizeForLog(final String value) {
		if (value == null) {
			return "<null>";
		}
		final StringBuilder sanitized = new StringBuilder(value.length());
		for (int i = 0; i < value.length(); i++) {
			final char current = value.charAt(i);
			if (current == '\r' || current == '\n' || current < 0x20 || current == 0x7F) {
				sanitized.append('?');
			} else {
				sanitized.append(current);
			}
		}
		return sanitized.toString();
	}

	private static void rejectWindowsForbiddenFilenameCharacters(final String value, final String fieldName) {
		for (int i = 0; i < value.length(); i++) {
			if (WINDOWS_FORBIDDEN_FILENAME_CHARS.indexOf(value.charAt(i)) >= 0) {
				throw new InvalidUpdatePathException(
						"Rejected " + fieldName + " containing Windows-forbidden filename characters.");
			}
		}
	}

	private static Path parsePath(final String value, final String fieldName) {
		try {
			return Paths.get(value);
		} catch (final InvalidPathException e) {
			throw new InvalidUpdatePathException("Rejected invalid " + fieldName + " path.", e);
		}
	}

	private static void ensureUnderTrustedRoot(final Path normalizedRoot, final Path resolved,
			final String describedValue) {
		if (!resolved.startsWith(normalizedRoot)) {
			throw new InvalidUpdatePathException(
					"Rejected updater path outside trusted root for value \"" + sanitizeForLog(describedValue) + "\".");
		}
	}

	private static boolean containsUnsafeControlCharacter(final String value) {
		for (int i = 0; i < value.length(); i++) {
			final char current = value.charAt(i);
			if (current < 0x20 || current == 0x7F) {
				return true;
			}
		}
		return false;
	}
}
