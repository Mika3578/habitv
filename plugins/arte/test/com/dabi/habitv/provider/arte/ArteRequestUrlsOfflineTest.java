package com.dabi.habitv.provider.arte;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ArteRequestUrlsOfflineTest {

	@Test
	public void acceptsTrustedEmacAndSiteUrls() {
		assertTrue(ArteRequestUrls.isTrustedEmacApiUrl(
				"https://api.arte.tv/api/emac/v4/fr/web/pages/DOR/?authorizedCountry=FR"));
		assertTrue(ArteRequestUrls.isTrustedPublicSiteUrl("https://www.arte.tv/fr/videos/119999-000-A/example/"));
		assertTrue(ArteRequestUrls.isTrustedCatalogueFetchUrl(
				"https://api.arte.tv/api/emac/v4/fr/web/zones/listing/content?authorizedCountry=FR&page=2"));
	}

	@Test
	public void rejectsLookalikeHostsAndMalformedUrls() {
		assertFalse(ArteRequestUrls.isTrustedEmacApiUrl("https://api.arte.tv.evil/api/emac/v4/fr/web/pages/DOR/"));
		assertFalse(ArteRequestUrls.isTrustedPublicSiteUrl("https://www.arte.tv.evil/fr/videos/"));
		assertFalse(ArteRequestUrls.isTrustedPublicSiteUrl("https://attacker.example/arte.tv/fr/videos/"));
		assertFalse(ArteRequestUrls.isTrustedEmacApiUrl("http://api.arte.tv/api/emac/v4/fr/web/pages/DOR/"));
		assertFalse(ArteRequestUrls.isTrustedEmacApiUrl("not-a-url"));
		assertFalse(ArteRequestUrls.isTrustedCatalogueFetchUrl("ftp://www.arte.tv/fr/videos/"));
	}
}
