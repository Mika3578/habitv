package com.dabi.habitv.provider.canalplus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CanalPlusContentIdParserTest {

	@Test
	public void extractsContentIdFromModernPageUrl() {
		final String url = "https://www.canalplus.com/decouverte/les-10-hotels-les-plus-incroyables-de-france/h/31338503_50017";
		assertEquals("31338503_50017", CanalPlusContentIdParser.fromInput(url));
		assertTrue(CanalPlusContentIdParser.isModernCanalPlusUrl(url));
	}

	@Test
	public void extractsContentIdFromHodorDetailUrl() {
		final String url = "https://hodor.canalplus.pro/api/v2/mycanal/detail/hash/okapi/31338503_50017.json?detailType=detailPage&objectType=unit";
		assertEquals("31338503_50017", CanalPlusContentIdParser.fromInput(url));
	}

	@Test
	public void returnsNullForUnrelatedInput() {
		assertNull(CanalPlusContentIdParser.fromInput("https://example.com/video/1"));
	}

}
