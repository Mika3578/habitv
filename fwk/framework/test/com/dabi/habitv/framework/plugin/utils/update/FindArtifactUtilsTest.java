package com.dabi.habitv.framework.plugin.utils.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
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
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class FindArtifactUtilsTest {

	private static final String GROUP_ID = "com.dabi.habitv";
	private static final String ARTIFACT_ID = "youtube";
	private static final String SNAPSHOT_VERSION = "4.1.0-SNAPSHOT";
	private static final String TIMESTAMPED_VALUE = "4.1.0-20260518.163022-1";
	private static final String TIMESTAMPED_JAR = "youtube-" + TIMESTAMPED_VALUE + ".jar";

	private String previousUpdateUrl;
	private HttpServer server;
	private final Map<String, String> responses = new HashMap<>();

	@Before
	public void setUp() throws IOException {
		previousUpdateUrl = System.getProperty(FrameworkConf.UPDATE_URL_PROPERTY);
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/", new FixtureHandler(responses));
		server.start();
		final String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/repository/";
		System.setProperty(FrameworkConf.UPDATE_URL_PROPERTY, baseUrl);
		FindArtifactUtils.clearManifestCache();
	}

	@After
	public void tearDown() {
		if (previousUpdateUrl == null) {
			System.clearProperty(FrameworkConf.UPDATE_URL_PROPERTY);
		} else {
			System.setProperty(FrameworkConf.UPDATE_URL_PROPERTY, previousUpdateUrl);
		}
		FindArtifactUtils.clearManifestCache();
		if (server != null) {
			server.stop(0);
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

	private static final class FixtureHandler implements HttpHandler {
		private final Map<String, String> responses;

		private FixtureHandler(final Map<String, String> responses) {
			this.responses = responses;
		}

		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			final String body = responses.get(exchange.getRequestURI().getPath());
			if (body == null) {
				exchange.sendResponseHeaders(404, -1);
				exchange.close();
				return;
			}
			final byte[] payload = body.getBytes("UTF-8");
			exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
			exchange.sendResponseHeaders(200, payload.length);
			try (OutputStream stream = exchange.getResponseBody()) {
				stream.write(payload);
			}
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
