package com.dabi.habitv.core.updater;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.dabi.habitv.api.plugin.pub.Subscriber;
import com.dabi.habitv.core.event.UpdatePluginEvent;
import com.dabi.habitv.core.event.UpdatePluginStateEnum;
import com.dabi.habitv.framework.FrameworkConf;

public class UpdateManagerPathIntegrationTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private String previousUpdateEnabled;
	private String previousUpdateUrl;

	private LocalTestHttpServer server;
	private final Map<String, String> responses = new HashMap<>();

	@Before
	public void setUp() throws IOException {
		previousUpdateEnabled = System.getProperty(FrameworkConf.UPDATE_ENABLED_PROPERTY);
		previousUpdateUrl = System.getProperty(FrameworkConf.UPDATE_URL_PROPERTY);
		server = new LocalTestHttpServer(responses);
		System.setProperty(FrameworkConf.UPDATE_ENABLED_PROPERTY, "true");
		System.setProperty(FrameworkConf.UPDATE_URL_PROPERTY,
				"http://127.0.0.1:" + server.getPort() + "/repository/");
	}

	@After
	public void tearDown() throws IOException {
		restoreProperty(FrameworkConf.UPDATE_ENABLED_PROPERTY, previousUpdateEnabled);
		restoreProperty(FrameworkConf.UPDATE_URL_PROPERTY, previousUpdateUrl);
		if (server != null) {
			server.stop();
		}
	}

	@Test
	public void validPluginEntryResolvesInsideTemporaryPluginFolder() throws Exception {
		responses.put("/repository/plugins.txt", "arte\n");
		final File pluginDir = temporaryFolder.newFolder("plugins");
		final Path trustedRoot = pluginDir.toPath().toAbsolutePath().normalize();
		final Path expectedArtifact = trustedRoot.resolve("arte.jar").normalize();
		assertTrue(expectedArtifact.startsWith(trustedRoot));

		final List<String> checkedPlugins = new ArrayList<>();
		final AtomicBoolean completed = new AtomicBoolean(false);
		final UpdateManager updateManager = new UpdateManager(pluginDir.getAbsolutePath(), true);
		updateManager.getUpdatePublisher().attach(new Subscriber<UpdatePluginEvent>() {
			@Override
			public void update(final UpdatePluginEvent event) {
				if (event.getState() == UpdatePluginStateEnum.CHECKING) {
					checkedPlugins.add(event.getPlugin());
				}
				if (event.getState() == UpdatePluginStateEnum.ALL_DONE) {
					completed.set(true);
				}
			}
		});

		updateManager.process();

		assertTrue(completed.get());
		assertEquals(1, checkedPlugins.size());
		assertEquals("arte", checkedPlugins.get(0));
		assertFalse(Files.exists(pluginDir.toPath().resolve("escape.jar")));
	}

	@Test
	public void malformedThenValidPluginEntriesSkipTraversalAndContinue() throws Exception {
		responses.put("/repository/plugins.txt", "../escape\narte\n");
		final File pluginDir = temporaryFolder.newFolder("plugins");
		final File outsideDir = temporaryFolder.newFolder("outside");
		final Path outsideMarker = outsideDir.toPath().resolve("escape.jar");
		Files.write(outsideMarker, "untouched".getBytes(StandardCharsets.UTF_8));

		final List<String> checkedPlugins = new ArrayList<>();
		final AtomicBoolean completed = new AtomicBoolean(false);
		final UpdateManager updateManager = new UpdateManager(pluginDir.getAbsolutePath(), true);
		updateManager.getUpdatePublisher().attach(new Subscriber<UpdatePluginEvent>() {
			@Override
			public void update(final UpdatePluginEvent event) {
				if (event.getState() == UpdatePluginStateEnum.CHECKING) {
					checkedPlugins.add(event.getPlugin());
				}
				if (event.getState() == UpdatePluginStateEnum.ALL_DONE) {
					completed.set(true);
				}
			}
		});

		updateManager.process();

		assertTrue(completed.get());
		assertEquals(2, checkedPlugins.size());
		assertEquals("../escape", checkedPlugins.get(0));
		assertEquals("arte", checkedPlugins.get(1));
		assertTrue(Files.exists(outsideMarker));
		assertEquals("untouched", new String(Files.readAllBytes(outsideMarker), StandardCharsets.UTF_8));
		assertFalse(Files.exists(pluginDir.toPath().resolve("escape.jar")));
	}

	private static void restoreProperty(final String key, final String previousValue) {
		if (previousValue == null) {
			System.clearProperty(key);
		} else {
			System.setProperty(key, previousValue);
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
			}, "UpdateManagerPathIntegrationTest-http");
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
