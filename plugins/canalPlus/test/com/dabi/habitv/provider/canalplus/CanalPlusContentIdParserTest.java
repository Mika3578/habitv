package com.dabi.habitv.provider.canalplus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

	@Test
	public void doesNotTreatUnrelatedNumericIdsAsModernCanalPlusUrls() {
		assertFalse(CanalPlusContentIdParser.isModernCanalPlusUrl("https://www.youtube.com/watch?v=abc_123"));
		assertFalse(CanalPlusContentIdParser.isModernCanalPlusUrl("123_456"));
	}

	@Test
	public void rejectsUnrelatedHostsThatOnlyMentionCanalPlusInTheUrl() {
		assertFalse(CanalPlusContentIdParser.isModernCanalPlusUrl(
				"https://evil.example/?next=https://www.canalplus.com/decouverte/h/31338503_50017"));
		assertFalse(CanalPlusContentIdParser.isModernCanalPlusUrl("https://evilcanalplus.com/h/31338503_50017"));
		assertFalse(CanalPlusContentIdParser.isCanalPlusPageUrl(
				"https://evil.example/?next=https://www.canalplus.com/foo"));
		assertFalse(CanalPlusContentIdParser.isHodorUrl(
				"https://evil.example/?u=https://hodor.canalplus.pro/api/v2/mycanal/detail/hash/okapi/1.json"));
	}

	@Test
	public void acceptsExactApprovedModernHosts() {
		assertTrue(CanalPlusContentIdParser.isModernCanalPlusUrl("https://www.canalplus.com/foo"));
		assertTrue(CanalPlusContentIdParser.isCanalPlusPageUrl("https://canalplus.com/foo"));
		assertTrue(CanalPlusContentIdParser.isHodorUrl(
				"https://hodor.canalplus.pro/api/v2/mycanal/detail/hash/okapi/31338503_50017.json"));
		assertTrue(CanalPlusContentIdParser.isModernCanalPlusUrl(
				"https://secure-gen-hapi.canal-plus.com/conso/playset/unit/31338503_50017"));
		assertTrue(CanalPlusContentIdParser.isModernCanalPlusUrl("https://routemeup.canalplus-bo.net/path"));
	}

}
