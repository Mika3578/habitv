package com.dabi.habitv.provider.bfmtv;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.net.InetSocketAddress;
import java.net.Proxy;

import org.junit.Test;

public class BfmTvHttpClientTest {

	@Test
	public void proxyForRequestReadsCurrentValue() {
		final Proxy[] holder = new Proxy[1];
		final BfmTvHttpClient.ProxySource source = new BfmTvHttpClient.ProxySource() {
			@Override
			public Proxy current() {
				return holder[0];
			}
		};
		assertNull(BfmTvHttpClient.proxyForRequest(source));
		final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080));
		holder[0] = proxy;
		assertSame(proxy, BfmTvHttpClient.proxyForRequest(source));
	}

}
