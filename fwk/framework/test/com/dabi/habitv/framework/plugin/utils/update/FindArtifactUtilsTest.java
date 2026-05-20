package com.dabi.habitv.framework.plugin.utils.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.utils.update.FindArtifactUtils.ArtifactVersion;
import com.dabi.habitv.framework.plugin.utils.update.FindArtifactUtils.ArtifactVersion.ResolutionSource;

public class FindArtifactUtilsTest {

	private static final String GROUP_ID = "com.dabi.habitv";
	private static final String ARTIFACT_ID = "youtube";
	private static final String SNAPSHOT_VERSION = "4.1.0-SNAPSHOT";
	private static final String TIMESTAMPED_VALUE = "4.1.0-20260518.163022-1";
	private static final String TIMESTAMPED_JAR = "youtube-" + TIMESTAMPED_VALUE + ".jar";

	private String previousUpdateUrl;
	private LocalTestHttpServer server;
	private final Map<String, String> responses = new HashMap<>();

	@Before
	public void setUp() throws IOException {
		previousUpdateUrl = System.getProperty(FrameworkConf.UPDATE_URL_PROPERTY);
		server = new LocalTestHttpServer(responses);
		final String baseUrl = "http://127.0.0.1:" + server.getPort() + "/repository/";
		System.setProperty(FrameworkConf.UPDATE_URL_PROPERTY, baseUrl);
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
	public void resolvesTimestampedSnapshotJarFromManifestWithoutReconstruction() {
		addManifest("plugin|com.dabi.habitv|youtube|4.1.0-SNAPSHOT|jar|com/dabi/habitv/youtube/4.1.0-SNAPSHOT/"
				+ TIMESTAMPED_JAR);

		final ArtifactVersion resolved = FindArtifactUtils.findLastVersionUrl(GROUP_ID, ARTIFACT_ID, SNAPSHOT_VERSION,
				true, "jar");

		assertNotNull(resolved);
		assertEquals(ResolutionSource.MANIFEST, resolved.getSource());
		assertEquals(expectedRepositoryUrl("com/dabi/habitv/youtube/4.1.0-SNAPSHOT/" + TIMESTAMPED_JAR),
				resolved.getUrl());
	}

	@Test
	public void fallsBackToMavenMetadataAndKeepsJarSnapshotVersionOnly() {
		addManifest("plugin|com.dabi.habitv|arte|4.1.0-SNAPSHOT|jar|com/dabi/habitv/arte/4.1.0-SNAPSHOT/arte.jar");
		addVersionListing();
		addSnapshotMetadata();

		final ArtifactVersion resolved = FindArtifactUtils.findLastVersionUrl(GROUP_ID, ARTIFACT_ID, SNAPSHOT_VERSION,
				true, "jar");

		assertNotNull(resolved);
		assertEquals(ResolutionSource.MAVEN_METADATA, resolved.getSource());
		assertEquals(expectedRepositoryUrl("com/dabi/habitv/youtube/4.1.0-SNAPSHOT/" + TIMESTAMPED_JAR),
				resolved.getUrl());
		assertTrue("Resolution must ignore sources classifier entry", !resolved.getUrl().contains("sources"));
	}

	@Test
	public void rejectsSnapshotWhenSnapshotsDisabled() {
		addManifest("plugin|com.dabi.habitv|arte|4.1.0-SNAPSHOT|jar|com/dabi/habitv/arte/4.1.0-SNAPSHOT/arte.jar");
		addVersionListing();
		addSnapshotMetadata();

		final ArtifactVersion resolved = FindArtifactUtils.findLastVersionUrl(GROUP_ID, ARTIFACT_ID, SNAPSHOT_VERSION,
				false, "jar");

		assertNull(resolved);
	}

	@Test
	public void logsSnapshotSkipReasonWhenSnapshotsDisabled() {
		addManifest("plugin|com.dabi.habitv|arte|4.1.0-SNAPSHOT|jar|com/dabi/habitv/arte/4.1.0-SNAPSHOT/arte.jar");
		addVersionListing();
		addSnapshotMetadata();

		final MemoryAppender appender = new MemoryAppender();
		final Logger logger = Logger.getLogger(FindArtifactUtils.class);
		logger.addAppender(appender);
		try {
			final ArtifactVersion resolved = FindArtifactUtils.findLastVersionUrl(GROUP_ID, ARTIFACT_ID, SNAPSHOT_VERSION,
					false, "jar");
			assertNull(resolved);
		} finally {
			logger.removeAppender(appender);
		}

		assertTrue(appender.contains("Skipping SNAPSHOT plugin artifact because snapshot updates are disabled."));
	}

	@Test
	public void returnsNullWhenSnapshotMetadataUnavailable() {
		// No manifest entry for youtube and no metadata - only a version directory listing
		addVersionListing();
		// Metadata endpoint intentionally absent (no addSnapshotMetadata call)

		final ArtifactVersion resolved = FindArtifactUtils.findLastVersionUrl(GROUP_ID, ARTIFACT_ID, SNAPSHOT_VERSION,
				true, "jar");

		assertNull("SNAPSHOT resolution must fail when maven-metadata.xml is missing, not fall back to directory listing",
				resolved);
	}

	@Test
	public void noWarningWhenSnapshotMajorVersionMismatch() {
		// Manifest has a SNAPSHOT for a different major version (5.0, not 4.1)
		addManifest("plugin|com.dabi.habitv|youtube|5.0-SNAPSHOT|jar|com/dabi/habitv/youtube/5.0-SNAPSHOT/youtube-5.0-SNAPSHOT.jar");

		final MemoryAppender appender = new MemoryAppender();
		final Logger logger = Logger.getLogger(FindArtifactUtils.class);
		logger.addAppender(appender);
		try {
			// Resolve with coreVersion that resolves to major "4.1"
			final ArtifactVersion resolved = FindArtifactUtils.findLastVersionUrl(GROUP_ID, ARTIFACT_ID, SNAPSHOT_VERSION,
					false, "jar");
			assertNull(resolved);
		} finally {
			logger.removeAppender(appender);
		}

		assertTrue("No snapshot-disabled warning expected when the only SNAPSHOT is for a different major version",
				!appender.contains("Skipping SNAPSHOT plugin artifact"));
	}

	@Test
	public void manifestEntryDownloadUrlUsesManifestBaseUrlWhenRelative() {
		final String baseUrl = "http://127.0.0.1:" + server.getPort() + "/repository";
		final String relativeUrl = "com/dabi/habitv/youtube/4.1.0-SNAPSHOT/" + TIMESTAMPED_JAR;
		final HabitvUpdateManifest.Entry entry = HabitvUpdateManifest.parseLine(
				"plugin|com.dabi.habitv|youtube|4.1.0-SNAPSHOT|jar|" + relativeUrl);
		assertNotNull(entry);
		final String downloadUrl = entry.getDownloadUrl(UpdateRepositoryUrls.normalizeBaseUrl(baseUrl));
		assertEquals(UpdateRepositoryUrls.normalizeBaseUrl(baseUrl) + "/" + relativeUrl, downloadUrl);
	}

	@Test
	public void extractSnapshotValueFromMetadataUsesJarExtensionWithoutClassifier() {
		final String metadata = buildMetadata();
		final String snapshotValue = FindArtifactUtils.extractSnapshotValueFromMetadata(metadata, "jar");
		assertEquals(TIMESTAMPED_VALUE, snapshotValue);
	}

	private void addManifest(final String line) {
		responses.put("/repository/habitv-update-manifest.properties", line + "\n");
	}

	private void addVersionListing() {
		responses.put("/repository/com/dabi/habitv/youtube/", "<html><body><a href=\"4.1.0-SNAPSHOT/\">4.1.0-SNAPSHOT/</a></body></html>");
	}

	private void addSnapshotMetadata() {
		responses.put("/repository/com/dabi/habitv/youtube/4.1.0-SNAPSHOT/maven-metadata.xml", buildMetadata());
	}

	private String buildMetadata() {
		return ""
				+ "<metadata>\n"
				+ "  <groupId>com.dabi.habitv</groupId>\n"
				+ "  <artifactId>youtube</artifactId>\n"
				+ "  <version>4.1.0-SNAPSHOT</version>\n"
				+ "  <versioning>\n"
				+ "    <snapshotVersions>\n"
				+ "      <snapshotVersion><extension>pom</extension><value>4.1.0-20260518.163022-1</value></snapshotVersion>\n"
				+ "      <snapshotVersion><extension>jar</extension><classifier>sources</classifier><value>4.1.0-20260518.163022-1</value></snapshotVersion>\n"
				+ "      <snapshotVersion><extension>jar</extension><value>" + TIMESTAMPED_VALUE + "</value></snapshotVersion>\n"
				+ "    </snapshotVersions>\n"
				+ "  </versioning>\n"
				+ "</metadata>\n";
	}

	private String expectedRepositoryUrl(final String relativePath) {
		return UpdateRepositoryUrls.normalizeBaseUrl(System.getProperty(FrameworkConf.UPDATE_URL_PROPERTY)) + "/"
				+ relativePath;
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
			}, "FindArtifactUtilsTest-http");
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

	private static final class MemoryAppender extends AppenderSkeleton {
		private final StringBuilder events = new StringBuilder();

		@Override
		protected void append(final LoggingEvent event) {
			if (event.getLevel().isGreaterOrEqual(Level.WARN)) {
				events.append(String.valueOf(event.getRenderedMessage())).append('\n');
			}
		}

		@Override
		public void close() {
			// no-op
		}

		@Override
		public boolean requiresLayout() {
			return false;
		}

		private boolean contains(final String expected) {
			return events.toString().contains(expected);
		}
	}
}
