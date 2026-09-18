package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.net.InetSocketAddress;
import java.net.Proxy;

import org.junit.Test;

public class Tf1PlusHttpClientTest {

	@Test
	public void proxyForRequestReadsCurrentValue() {
		final Proxy[] holder = new Proxy[1];
		final Tf1PlusHttpClient.ProxySource source = new Tf1PlusHttpClient.ProxySource() {
			@Override
			public Proxy current() {
				return holder[0];
			}
		};
		assertNull(Tf1PlusHttpClient.proxyForRequest(source));
		final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080));
		holder[0] = proxy;
		assertSame(proxy, Tf1PlusHttpClient.proxyForRequest(source));
	}

}
