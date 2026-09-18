package com.dabi.habitv.provider.bfmtv;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BfmTvUrlsTest {

	@Test
	public void acceptsPublicReplayPages() {
		assertTrue(BfmTvUrls.isApprovedPublicDownloadUrl(
				"https://www.bfmtv.com/replay-emissions/morning-news/video-morning-news-friday-18-september-2026_VN-202609180001.html"));
		assertTrue(BfmTvUrls.isApprovedPublicDownloadUrl(
				"https://www.bfmtv.com/economie/replay-emissions/morning-news/video-business.html"));
		assertTrue(BfmTvUrls.isApprovedPublicDownloadUrl(
				"https://bfmbusiness.bfmtv.com/replay-emissions/morning-news/video-business.html"));
	}

	@Test
	public void rejectsUnrelatedOrSpoofedUrls() {
		assertFalse(BfmTvUrls.isApprovedPublicDownloadUrl(
				"https://evil.com/www.bfmtv.com/replay-emissions/x.html"));
		assertFalse(BfmTvUrls.isApprovedPublicDownloadUrl("https://www.bfmtv.com/economie/"));
		assertFalse(BfmTvUrls.isApprovedPublicDownloadUrl(
				"http://www.bfmtv.com/replay-emissions/morning-news/video-x.html"));
		assertFalse(BfmTvUrls.isApprovedPublicDownloadUrl(
				"file://www.bfmtv.com/replay-emissions/morning-news/video-x.html"));
		assertFalse(BfmTvUrls.isApprovedPublicDownloadUrl(
				"https://attacker.bfmtv.com/replay-emissions/morning-news/video-x.html"));
		assertFalse(BfmTvUrls.isApprovedPublicDownloadUrl(
				"https://images.bfmtv.com/replay-emissions/morning-news/video-x.html"));
		assertFalse(BfmTvUrls.isApprovedPublicDownloadUrl(null));
	}

}
