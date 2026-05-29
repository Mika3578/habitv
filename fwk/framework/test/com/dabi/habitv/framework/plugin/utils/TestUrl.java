package com.dabi.habitv.framework.plugin.utils;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Offline coverage for {@link RetrieverUtils#getTitleByUrl(String)}.
 * Does not call external websites (deterministic CI).
 */
public class TestUrl {

	private static final String EXPECTED_TITLE = "Offline fixture title";
	private static final String HTML = "<!DOCTYPE html><html><head><title>" + EXPECTED_TITLE
			+ "</title></head><body></body></html>";

	private HttpServer server;
	private String localUrl;

	@Before
	public void setUp() throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/", new HttpHandler() {
			@Override
			public void handle(final HttpExchange exchange) throws IOException {
				final byte[] body = htmlBytes();
				exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
				exchange.sendResponseHeaders(200, body.length);
				final OutputStream out = exchange.getResponseBody();
				out.write(body);
				out.close();
			}
		});
		server.start();
		localUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
	}

	@After
	public void tearDown() {
		if (server != null) {
			server.stop(0);
		}
	}

	@Test
	public void getTitleByUrl_extractsTitleFromLocalHtmlPage() {
		assertEquals(EXPECTED_TITLE, RetrieverUtils.getTitleByUrl(localUrl));
	}

	private static byte[] htmlBytes() {
		try {
			return HTML.getBytes("UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}
}
