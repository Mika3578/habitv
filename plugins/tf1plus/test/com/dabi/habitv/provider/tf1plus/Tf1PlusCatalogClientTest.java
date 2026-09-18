package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Tf1PlusCatalogClientTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Test
	public void itemOffsetAdvancesByPageSize() {
		assertEquals(0, Tf1PlusCatalogClient.itemOffset(0, 100));
		assertEquals(100, Tf1PlusCatalogClient.itemOffset(1, 100));
		assertEquals(40, Tf1PlusCatalogClient.itemOffset(2, 20));
	}

	@Test
	public void programRequestsUseItemOffsets() throws Exception {
		final List<Integer> offsets = new ArrayList<Integer>();
		final Tf1PlusCatalogClient client = new Tf1PlusCatalogClient(new Tf1PlusHttpClient.Transport() {
			@Override
			public String get(final String url) throws IOException {
				final int offset = offsetFromUrl(url);
				offsets.add(Integer.valueOf(offset));
				final int count = offset == 0 ? Tf1PlusConf.PROGRAM_PAGE_SIZE : 1;
				return programPage(count);
			}
		});
		assertEquals(Tf1PlusConf.PROGRAM_PAGE_SIZE + 1, client.fetchPrograms("tf1").size());
		assertEquals(Integer.valueOf(0), offsets.get(0));
		assertEquals(Integer.valueOf(Tf1PlusConf.PROGRAM_PAGE_SIZE), offsets.get(1));
	}

	@Test
	public void videoRequestsUseItemOffsets() throws Exception {
		final List<Integer> offsets = new ArrayList<Integer>();
		final Tf1PlusCatalogClient client = new Tf1PlusCatalogClient(new Tf1PlusHttpClient.Transport() {
			@Override
			public String get(final String url) throws IOException {
				final int offset = offsetFromUrl(url);
				offsets.add(Integer.valueOf(offset));
				final int count = offset == 0 ? Tf1PlusConf.VIDEO_PAGE_SIZE : 1;
				return videoPage(count);
			}
		});
		assertEquals(Tf1PlusConf.VIDEO_PAGE_SIZE + 1, client.fetchReplayVideos("evening-magazine").size());
		assertEquals(Integer.valueOf(0), offsets.get(0));
		assertEquals(Integer.valueOf(Tf1PlusConf.VIDEO_PAGE_SIZE), offsets.get(1));
	}

	private static int offsetFromUrl(final String url) throws IOException {
		final int marker = url.indexOf("variables=");
		if (marker < 0) {
			throw new IOException("missing-variables");
		}
		try {
			final String json = URLDecoder.decode(url.substring(marker + "variables=".length()), "UTF-8");
			final Map<?, ?> variables = MAPPER.readValue(json, Map.class);
			return ((Number) variables.get("offset")).intValue();
		} catch (final UnsupportedEncodingException e) {
			throw new IOException("utf-8", e);
		}
	}

	private static String programPage(final int count) throws IOException {
		final StringBuilder json = new StringBuilder();
		json.append("{\"data\":{\"programs\":{\"items\":[");
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				json.append(',');
			}
			json.append("{\"slug\":\"program-").append(i).append("\",\"name\":\"Program ").append(i).append("\"}");
		}
		json.append("]}}}");
		return json.toString();
	}

	private static String videoPage(final int count) throws IOException {
		final StringBuilder json = new StringBuilder();
		json.append("{\"data\":{\"programBySlug\":{\"videos\":{\"items\":[");
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				json.append(',');
			}
			json.append("{\"slug\":\"video-").append(i).append("\",\"type\":\"REPLAY\"}");
		}
		json.append("]}}}}");
		return json.toString();
	}

}
