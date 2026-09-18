package com.dabi.habitv.provider.canalplus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class CanalPlusApprovedRetrieverTest {

	@Test
	public void catalogAllowlistAcceptsExactApprovedHostsOnly() {
		CanalPlusApprovedRetriever.requireAllowed(
				"https://www.canalplus.com/decouverte/h/31338503_50017",
				CanalPlusApprovedRetriever.CATALOG_HOSTS);
		CanalPlusApprovedRetriever.requireAllowed(
				"https://hodor.canalplus.pro/api/v2/mycanal/detail/hash/okapi/31338503_50017.json",
				CanalPlusApprovedRetriever.CATALOG_HOSTS);
		CanalPlusApprovedRetriever.requireAllowed(
				"http://service.mycanal.fr/page/abc/123.json",
				CanalPlusApprovedRetriever.CATALOG_HOSTS);
		try {
			CanalPlusApprovedRetriever.requireAllowed("https://evil.example/steal",
					CanalPlusApprovedRetriever.CATALOG_HOSTS);
			fail("unapproved host must be rejected");
		} catch (TechnicalException e) {
			assertTrue(e.getCause() instanceof IOException);
			assertTrue(e.getCause().getMessage().contains("Unapproved catalog host"));
			assertTrue(e.getCause().getMessage().contains("evil.example"));
		}
	}

	@Test
	public void resolveRedirectKeepsRelativeTargetsOnTheCurrentHost() {
		assertEquals("https://www.canalplus.com/chaines/cstar",
				CanalPlusApprovedRetriever.resolveRedirectTarget("https://www.canalplus.com/home",
						"/chaines/cstar"));
		assertEquals("https://hodor.canalplus.pro/api/v2/okapi/1.json",
				CanalPlusApprovedRetriever.resolveRedirectTarget(
						"https://www.canalplus.com/decouverte/h/31338503_50017",
						"https://hodor.canalplus.pro/api/v2/okapi/1.json"));
	}

	@Test
	public void catalogFetchRejectsUnapprovedHostBeforeConnect() {
		final CanalPlusPluginManager manager = new CanalPlusPluginManager();
		try {
			manager.getInputStreamFromUrl("https://evil.example/steal");
			fail("unapproved host must not be fetched");
		} catch (TechnicalException e) {
			assertTrue(e.getCause() instanceof IOException);
			assertTrue(e.getCause().getMessage().contains("Unapproved catalog host"));
		}
	}

	@Test
	public void doesNotFollowRedirectToUnapprovedHost() throws IOException {
		final HttpServer approved = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		final HttpServer evil = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		final int approvedPort = approved.getAddress().getPort();
		final int evilPort = evil.getAddress().getPort();
		final String approvedUrl = "http://127.0.0.1:" + approvedPort + "/catalog";
		final String evilUrl = "http://127.0.0.1:" + evilPort + "/secret";
		approved.createContext("/catalog", new HttpHandler() {
			@Override
			public void handle(final HttpExchange exchange) throws IOException {
				exchange.getResponseHeaders().set("Location", evilUrl);
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_TEMP, -1);
				exchange.close();
			}
		});
		evil.createContext("/secret", new HttpHandler() {
			@Override
			public void handle(final HttpExchange exchange) throws IOException {
				final byte[] body = "stolen".getBytes(StandardCharsets.UTF_8);
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, body.length);
				final OutputStream out = exchange.getResponseBody();
				out.write(body);
				out.close();
			}
		});
		approved.start();
		evil.start();
		try {
			CanalPlusApprovedRetriever.open(approvedUrl, null, new CanalPlusApprovedRetriever.HostAllowlist() {
				@Override
				public boolean isAllowed(final String url) {
					return url.startsWith("http://127.0.0.1:" + approvedPort);
				}
			});
			fail("redirect to an unapproved host must be rejected");
		} catch (TechnicalException e) {
			assertTrue(e.getCause() instanceof IOException);
			assertTrue(e.getCause().getMessage().contains("Unapproved catalog host"));
		} finally {
			approved.stop(0);
			evil.stop(0);
		}
	}

	@Test
	public void followsRedirectWhenTheTargetHostIsApproved() throws IOException {
		final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		final int port = server.getAddress().getPort();
		final String startUrl = "http://127.0.0.1:" + port + "/start";
		final String destUrl = "http://127.0.0.1:" + port + "/dest";
		server.createContext("/start", new HttpHandler() {
			@Override
			public void handle(final HttpExchange exchange) throws IOException {
				exchange.getResponseHeaders().set("Location", destUrl);
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_TEMP, -1);
				exchange.close();
			}
		});
		server.createContext("/dest", new HttpHandler() {
			@Override
			public void handle(final HttpExchange exchange) throws IOException {
				final byte[] body = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, body.length);
				final OutputStream out = exchange.getResponseBody();
				out.write(body);
				out.close();
			}
		});
		server.start();
		try {
			final InputStream input = CanalPlusApprovedRetriever.open(startUrl, null,
					new CanalPlusApprovedRetriever.HostAllowlist() {
						@Override
						public boolean isAllowed(final String url) {
							return url.startsWith("http://127.0.0.1:" + port);
						}
					});
			try {
				final byte[] buffer = new byte[32];
				final int read = input.read(buffer);
				assertEquals("{\"ok\":true}", new String(buffer, 0, read, StandardCharsets.UTF_8));
			} finally {
				input.close();
			}
		} finally {
			server.stop(0);
		}
	}

}
