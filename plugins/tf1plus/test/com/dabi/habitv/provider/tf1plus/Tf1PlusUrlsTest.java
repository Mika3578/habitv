package com.dabi.habitv.provider.tf1plus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Tf1PlusUrlsTest {

	@Test
	public void acceptsPublicTf1ReplayPages() {
		assertTrue(Tf1PlusUrls.isApprovedPublicDownloadUrl(
				"https://www.tf1.fr/tf1/evening-magazine/videos/evening-magazine-episode-1.html"));
		assertTrue(Tf1PlusUrls.isApprovedPublicDownloadUrl(
				"https://tf1.fr/tmc/sample-show/videos/sample-show-episode-2.html"));
	}

	@Test
	public void rejectsUnrelatedOrSpoofedUrls() {
		assertFalse(Tf1PlusUrls.isApprovedPublicDownloadUrl("https://example.com/tf1.fr/videos/x.html"));
		assertFalse(Tf1PlusUrls.isApprovedPublicDownloadUrl("https://www.tf1.fr/tf1/evening-magazine"));
		assertFalse(Tf1PlusUrls.isApprovedPublicDownloadUrl("https://www.wat.tv/video/sample"));
		assertFalse(Tf1PlusUrls.isApprovedPublicDownloadUrl(
				"http://www.tf1.fr/tf1/evening-magazine/videos/evening-magazine-episode-1.html"));
		assertFalse(Tf1PlusUrls.isApprovedPublicDownloadUrl(
				"file://www.tf1.fr/tf1/evening-magazine/videos/evening-magazine-episode-1.html"));
		assertFalse(Tf1PlusUrls.isApprovedPublicDownloadUrl(null));
	}

	@Test
	public void buildsGraphqlAndVideoUrls() {
		final String graphql = Tf1PlusUrls.graphqlUrl("909c68c0", "{}");
		assertTrue(graphql.startsWith("https://www.tf1.fr/graphql/web?id=909c68c0&variables="));
		assertEquals("https://www.tf1.fr/tf1/evening-magazine/videos/evening-magazine-episode-1.html",
				Tf1PlusUrls.videoPageUrl("tf1", "evening-magazine", "evening-magazine-episode-1"));
	}

}
