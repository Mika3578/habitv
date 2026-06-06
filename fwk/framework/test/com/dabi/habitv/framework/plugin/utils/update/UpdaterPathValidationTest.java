package com.dabi.habitv.framework.plugin.utils.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UpdaterPathValidationTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private final List<String> loggedMessages = new ArrayList<>();

	private AppenderSkeleton appender;

	@Before
	public void setUp() {
		appender = new AppenderSkeleton() {
			@Override
			protected void append(final LoggingEvent event) {
				if (event.getLevel().isGreaterOrEqual(Level.WARN)) {
					loggedMessages.add(event.getRenderedMessage());
				}
			}

			@Override
			public void close() {
			}

			@Override
			public boolean requiresLayout() {
				return false;
			}
		};
		Logger.getLogger(Updater.class).addAppender(appender);
	}

	@After
	public void tearDown() {
		if (appender != null) {
			Logger.getLogger(Updater.class).removeAppender(appender);
		}
	}

	@Test
	public void continuesWithValidEntryAfterWindowsForbiddenCharacter() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		final TestUpdater updater = new TestUpdater(root.getAbsolutePath());
		updater.update("bad:name", "arte");

		assertEquals(2, updater.getCheckedArtifactIds().size());
		assertEquals("bad:name", updater.getCheckedArtifactIds().get(0));
		assertEquals("arte", updater.getCheckedArtifactIds().get(1));
		assertTrue(containsSkippingUnsafeMessage());
	}

	@Test
	public void continuesWithValidEntryAfterMalformedEntry() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		final TestUpdater updater = new TestUpdater(root.getAbsolutePath());
		updater.update("../outside/escape", "arte");

		assertEquals(2, updater.getCheckedArtifactIds().size());
		assertEquals("../outside/escape", updater.getCheckedArtifactIds().get(0));
		assertEquals("arte", updater.getCheckedArtifactIds().get(1));
		assertTrue(containsSkippingUnsafeMessage());
	}

	@Test
	public void skipsUnsafeArtifactWithoutTouchingFilesOutsideRoot() throws IOException {
		final File root = temporaryFolder.newFolder("plugins");
		final File outside = temporaryFolder.newFolder("outside");
		final Path outsideMarker = outside.toPath().resolve("escape.jar");
		Files.write(outsideMarker, "safe".getBytes(StandardCharsets.UTF_8));

		final TestUpdater updater = new TestUpdater(root.getAbsolutePath());
		updater.update("../outside/escape");

		assertTrue(Files.exists(outsideMarker));
		assertFalse(Files.exists(root.toPath().resolve("escape.jar")));
		assertTrue(containsSkippingUnsafeMessage());
	}

	private boolean containsSkippingUnsafeMessage() {
		for (final String message : loggedMessages) {
			if (message != null && message.contains("Skipping unsafe update entry")) {
				assertFalse(message.contains("\r"));
				assertFalse(message.contains("\n"));
				return true;
			}
		}
		return false;
	}

	private static final class TestUpdater extends Updater {

		private final List<String> checkedArtifactIds = new ArrayList<>();

		TestUpdater(final String folderToUpdate) {
			super(folderToUpdate, "com.dabi.habitv", "4.1.0-SNAPSHOT", false);
		}

		List<String> getCheckedArtifactIds() {
			return checkedArtifactIds;
		}

		@Override
		protected boolean deleteFiles() {
			return false;
		}

		@Override
		protected void onChecking(final String fileToUpdate) {
			checkedArtifactIds.add(fileToUpdate);
		}

		@Override
		protected String getCurrentVersion(final java.io.File currentFile) {
			return null;
		}

		@Override
		protected String getLocalExtension() {
			return "jar";
		}

		@Override
		protected String getServerExtension() {
			return "jar";
		}

		@Override
		protected boolean performUpdate(final java.io.File current,
				final FindArtifactUtils.ArtifactVersion artifactVersion) {
			return false;
		}

		@Override
		protected void onUpdateError(final java.io.File current,
				final FindArtifactUtils.ArtifactVersion artifactNewVersion) {
		}

		@Override
		protected void onUpdateDone(final java.io.File current,
				final FindArtifactUtils.ArtifactVersion artifactNewVersion) {
		}

		@Override
		protected void onUpdate(final java.io.File current,
				final FindArtifactUtils.ArtifactVersion artifactNewVersion) {
		}
	}
}
