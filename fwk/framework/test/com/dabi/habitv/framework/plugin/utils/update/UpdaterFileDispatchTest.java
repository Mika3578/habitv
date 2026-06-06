package com.dabi.habitv.framework.plugin.utils.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.utils.update.FindArtifactUtils.ArtifactVersion;

/**
 * Regression tests for the protected {@code updateFile(File, File)} hook used by
 * {@link ZipExeUpdater} and other updater subclasses.
 */
public class UpdaterFileDispatchTest {

	private static final String GROUP_ID = "com.dabi.habitv";
	private static final String ARTIFACT_ID = "testtool";
	private static final String ARTIFACT_VERSION = "4.1.0";
	private static final String RELATIVE_JAR = "com/dabi/habitv/testtool/4.1.0/testtool-4.1.0.jar";
	private static final String DOWNLOAD_PAYLOAD = "downloaded-artifact-bytes";

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private String previousUpdateUrl;
	private LocalTestHttpServer server;
	private final Map<String, String> responses = new HashMap<>();

	@Before
	public void setUp() throws IOException {
		previousUpdateUrl = System.getProperty(FrameworkConf.UPDATE_URL_PROPERTY);
		server = new LocalTestHttpServer(responses);
		System.setProperty(FrameworkConf.UPDATE_URL_PROPERTY,
				"http://127.0.0.1:" + server.getPort() + "/repository/");
		FindArtifactUtils.clearManifestCache();
	}

	@After
	public void tearDown() throws IOException {
		if (previousUpdateUrl == null) {
			System.clearProperty(FrameworkConf.UPDATE_URL_PROPERTY);
		} else {
			System.setProperty(FrameworkConf.UPDATE_URL_PROPERTY, previousUpdateUrl);
		}
		FindArtifactUtils.clearManifestCache();
		if (server != null) {
			server.stop();
		}
	}

	@Test
	public void postDownloadFlowInvokesFileBasedUpdateFileOverride() throws Exception {
		addManifestEntry();
		responses.put("/repository/" + RELATIVE_JAR, DOWNLOAD_PAYLOAD);

		final File root = temporaryFolder.newFolder("bin");
		final Path trustedRoot = root.toPath().toAbsolutePath().normalize();
		final RecordingUpdater updater = new RecordingUpdater(root.getAbsolutePath());

		updater.update(ARTIFACT_ID);

		assertEquals("Subclass updateFile(File, File) must be invoked after download", 1,
				updater.getFileUpdateCallCount());
		assertTrue("Downloaded temp must stay under trusted root",
				updater.getLastNewVersion().getAbsolutePath().startsWith(trustedRoot.toString()));
		assertEquals(DOWNLOAD_PAYLOAD,
				new String(Files.readAllBytes(updater.getLastNewVersion().toPath()), StandardCharsets.UTF_8));
		assertFalse("Destination must not receive a raw move when override handles the archive",
				Files.exists(trustedRoot.resolve("testtool.jar")));
	}

	@Test
	public void unzipStyleFileOverrideExtractsArchiveInsteadOfMovingIt() throws Exception {
		addManifestEntry();
		responses.put("/repository/" + RELATIVE_JAR, "zip-archive-bytes");

		final File root = temporaryFolder.newFolder("bin");
		final Path trustedRoot = root.toPath().toAbsolutePath().normalize();
		final UnzipStyleUpdater updater = new UnzipStyleUpdater(root.getAbsolutePath());

		updater.update(ARTIFACT_ID);

		assertTrue("ZipExeUpdater-style override must run", updater.isUnzipStyleOverrideCalled());
		assertTrue(Files.exists(trustedRoot.resolve("extracted-marker.txt")));
		assertEquals("zip-archive-bytes",
				new String(Files.readAllBytes(trustedRoot.resolve("extracted-marker.txt")), StandardCharsets.UTF_8));
		assertFalse(Files.exists(trustedRoot.resolve("testtool.jar")));
		assertFalse(Files.exists(trustedRoot.resolve("testtool.jar.tmp")));
	}

	@Test
	public void defaultUpdateFileReplacesDestinationWithoutPreDelete() throws Exception {
		final File root = temporaryFolder.newFolder("plugins");
		final File current = new File(root, "tool.jar");
		Files.write(current.toPath(), "old".getBytes(StandardCharsets.UTF_8));
		final File temp = new File(root, "tool.jar.tmp");
		Files.write(temp.toPath(), "new".getBytes(StandardCharsets.UTF_8));

		final DefaultUpdateFileUpdater updater = new DefaultUpdateFileUpdater(root.getAbsolutePath());
		updater.invokeDefaultUpdateFile(current, temp);

		assertEquals("new", new String(Files.readAllBytes(current.toPath()), StandardCharsets.UTF_8));
		assertFalse(temp.exists());
	}

	private void addManifestEntry() {
		responses.put("/repository/habitv-update-manifest.properties",
				"plugin|" + GROUP_ID + "|" + ARTIFACT_ID + "|" + ARTIFACT_VERSION + "|jar|" + RELATIVE_JAR + "\n");
	}

	private static final class RecordingUpdater extends Updater {

		private int fileUpdateCallCount;
		private File lastNewVersion;

		RecordingUpdater(final String folderToUpdate) {
			super(folderToUpdate, GROUP_ID, "4.1.0-SNAPSHOT", true);
		}

		int getFileUpdateCallCount() {
			return fileUpdateCallCount;
		}

		File getLastNewVersion() {
			return lastNewVersion;
		}

		@Override
		protected boolean deleteFiles() {
			return false;
		}

		@Override
		protected void onChecking(final String fileToUpdate) {
		}

		@Override
		protected String getCurrentVersion(final File currentFile) {
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
		protected boolean performUpdate(final File current, final ArtifactVersion artifactVersion) {
			return true;
		}

		@Override
		protected void updateFile(final File current, final File newVersion) {
			fileUpdateCallCount++;
			lastNewVersion = newVersion;
		}

		@Override
		protected void onUpdateError(final File current, final ArtifactVersion artifactNewVersion) {
		}

		@Override
		protected void onUpdateDone(final File current, final ArtifactVersion artifactNewVersion) {
		}

		@Override
		protected void onUpdate(final File current, final ArtifactVersion artifactNewVersion) {
		}
	}

	/**
	 * Emulates {@link ZipExeUpdater}'s protected {@code updateFile(File, File)}
	 * contract: extract archive contents and delete the temporary download.
	 */
	private static final class UnzipStyleUpdater extends Updater {

		private boolean unzipStyleOverrideCalled;

		UnzipStyleUpdater(final String folderToUpdate) {
			super(folderToUpdate, GROUP_ID, "4.1.0-SNAPSHOT", true);
		}

		boolean isUnzipStyleOverrideCalled() {
			return unzipStyleOverrideCalled;
		}

		@Override
		protected boolean deleteFiles() {
			return false;
		}

		@Override
		protected void onChecking(final String fileToUpdate) {
		}

		@Override
		protected String getCurrentVersion(final File currentFile) {
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
		protected boolean performUpdate(final File current, final ArtifactVersion artifactVersion) {
			return true;
		}

		@Override
		protected void updateFile(final File current, final File newVersion) {
			unzipStyleOverrideCalled = true;
			if (newVersion.exists()) {
				try {
					Files.write(new File(getFolderToUpdate(), "extracted-marker.txt").toPath(),
							Files.readAllBytes(newVersion.toPath()));
					Files.delete(newVersion.toPath());
				} catch (final IOException e) {
					throw new TechnicalException(e);
				}
			}
		}

		@Override
		protected void onUpdateError(final File current, final ArtifactVersion artifactNewVersion) {
		}

		@Override
		protected void onUpdateDone(final File current, final ArtifactVersion artifactNewVersion) {
		}

		@Override
		protected void onUpdate(final File current, final ArtifactVersion artifactNewVersion) {
		}
	}

	private static final class DefaultUpdateFileUpdater extends Updater {

		DefaultUpdateFileUpdater(final String folderToUpdate) {
			super(folderToUpdate, GROUP_ID, "4.1.0-SNAPSHOT", false);
		}

		void invokeDefaultUpdateFile(final File current, final File newVersion) {
			updateFile(current, newVersion);
		}

		@Override
		protected boolean deleteFiles() {
			return false;
		}

		@Override
		protected void onChecking(final String fileToUpdate) {
		}

		@Override
		protected String getCurrentVersion(final File currentFile) {
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
		protected boolean performUpdate(final File current, final ArtifactVersion artifactVersion) {
			return false;
		}

		@Override
		protected void onUpdateError(final File current, final ArtifactVersion artifactNewVersion) {
		}

		@Override
		protected void onUpdateDone(final File current, final ArtifactVersion artifactNewVersion) {
		}

		@Override
		protected void onUpdate(final File current, final ArtifactVersion artifactNewVersion) {
		}
	}

	private static final class LocalTestHttpServer {
		private final ServerSocket serverSocket;
		private final Thread acceptThread;
		private final Map<String, String> responses;
		private volatile boolean running = true;

		private LocalTestHttpServer(final Map<String, String> responses) throws IOException {
			this.responses = responses;
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress("127.0.0.1", 0));
			acceptThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (running) {
						try {
							handleClient(serverSocket.accept());
						} catch (IOException e) {
							if (running) {
								// ignore transient accept errors during shutdown
							}
						}
					}
				}
			}, "UpdaterFileDispatchTest-http");
			acceptThread.setDaemon(true);
			acceptThread.start();
		}

		private int getPort() {
			return serverSocket.getLocalPort();
		}

		private void stop() throws IOException {
			running = false;
			serverSocket.close();
			acceptThread.interrupt();
		}

		private void handleClient(final Socket client) {
			try {
				final BufferedReader reader = new BufferedReader(
						new InputStreamReader(client.getInputStream(), "US-ASCII"));
				final String requestLine = reader.readLine();
				if (requestLine == null) {
					return;
				}
				String line;
				while ((line = reader.readLine()) != null && !line.isEmpty()) {
					// consume request headers
				}
				final String path = parseRequestPath(requestLine);
				final String body = responses.get(path);
				final OutputStream out = client.getOutputStream();
				if (body == null) {
					writeHttpResponse(out, 404, "Not Found", new byte[0], "text/plain; charset=UTF-8");
				} else {
					final byte[] payload = body.getBytes("UTF-8");
					writeHttpResponse(out, 200, "OK", payload, "text/plain; charset=UTF-8");
				}
			} catch (IOException e) {
				// ignore client handling errors for test fixture robustness
			} finally {
				try {
					client.close();
				} catch (IOException e) {
					// ignore close errors
				}
			}
		}

		private static String parseRequestPath(final String requestLine) {
			final String[] parts = requestLine.split(" ");
			if (parts.length < 2) {
				return "/";
			}
			return parts[1];
		}

		private static void writeHttpResponse(final OutputStream out, final int statusCode, final String statusText,
				final byte[] payload, final String contentType) throws IOException {
			final String headers = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n"
					+ "Content-Type: " + contentType + "\r\n"
					+ "Content-Length: " + payload.length + "\r\n"
					+ "Connection: close\r\n\r\n";
			out.write(headers.getBytes("US-ASCII"));
			out.write(payload);
			out.flush();
		}
	}
}
