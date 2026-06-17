package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.Test;

public class Novo19HttpClientReadUtf8Test {

	@Test
	public void readUtf8PreservesMultibyteCharactersAcrossBufferBoundaries() throws IOException {
		final String expected = "Thématique";
		final byte[] bytes = expected.getBytes(Charset.forName("UTF-8"));
		final ByteArrayInputStream input = new ChunkedInputStream(bytes, 3);
		assertEquals(expected, Novo19HttpClient.readUtf8(input));
	}

	private static final class ChunkedInputStream extends ByteArrayInputStream {

		private final int chunkSize;

		private ChunkedInputStream(final byte[] bytes, final int chunkSize) {
			super(bytes);
			this.chunkSize = chunkSize;
		}

		@Override
		public synchronized int read(final byte[] buffer, final int offset, final int length) {
			return super.read(buffer, offset, Math.min(length, chunkSize));
		}

	}

}
