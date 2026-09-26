package com.dabi.habitv.provider.arte;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.provider.arte.ArteCatalogDiscovery.ArteLanguage;
import com.dabi.habitv.provider.arte.ArteCatalogDiscovery.ArtePageRef;

/**
 * Offline coverage for dynamic EMAC catalogue discovery. Expectations are
 * independent from {@link ArteConf#FALLBACK_PAGE_CODES}.
 */
public class ArteCatalogDiscoveryOfflineTest {

	private static final List<String> EXPECTED_LANGUAGE_CODES = Arrays.asList("fr", "de", "en", "es", "pl", "it", "ro");

	private static final Set<String> EXPECTED_FR_PAGE_CODES = new HashSet<>(Arrays.asList("DOR", "SER", "ARTE_CONCERT",
			"DEC", "ACT"));

	@Test
	public void discoverLanguagesFromHomeAlternativeLanguages() throws IOException {
		final Map<String, String> urls = discoveryUrlMap();
		final ArteCatalogDiscovery discovery = new ArteCatalogDiscovery(urls::get);

		final List<ArteLanguage> languages = discovery.discoverLanguages();

		assertTrue("must expose seven public languages", languages.size() >= 7);
		for (final String code : EXPECTED_LANGUAGE_CODES) {
			boolean found = false;
			for (final ArteLanguage language : languages) {
				if (code.equals(language.getCode())) {
					found = true;
					break;
				}
			}
			assertTrue("missing language " + code, found);
		}
	}

	@Test
	public void discoverPagesIncludesTravelDiscoveryAndConcertFallbacks() throws IOException {
		final Map<String, String> urls = discoveryUrlMap();
		final ArteCatalogDiscovery discovery = new ArteCatalogDiscovery(urls::get);

		final List<ArtePageRef> pages = discovery.discoverPages("fr");
		final Set<String> codes = new HashSet<>();
		for (final ArtePageRef page : pages) {
			codes.add(page.getCode());
		}
		for (final String expected : EXPECTED_FR_PAGE_CODES) {
			assertTrue("catalogue must include " + expected, codes.contains(expected));
		}
		assertFalse("must not be limited to the old seven-page PR #55 set",
				codes.size() <= 7 && !codes.contains("DEC") && !codes.contains("ARTE_CONCERT"));
	}

	@Test
	public void findCategoryBuildsLanguagePageZoneHierarchy() throws IOException {
		final Map<String, String> urls = discoveryUrlMap();
		final ArtePluginManager plugin = new ArtePluginManager(new ArteCatalogDiscovery(urls::get), urls::get);

		final Set<CategoryDTO> roots = plugin.findCategory();
		assertFalse(roots.isEmpty());
		CategoryDTO fr = null;
		for (final CategoryDTO language : roots) {
			if (language.getId().endsWith("/fr/")) {
				fr = language;
				break;
			}
		}
		assertTrue("French language container required", fr != null);
		assertFalse(fr.isDownloadable());
		boolean dorWithZone = false;
		for (final CategoryDTO page : fr.getSubCategories()) {
			if (page.getId().endsWith(":DOR")) {
				assertFalse("page container must not be downloadable", page.isDownloadable());
				for (final CategoryDTO zone : page.getSubCategories()) {
					assertTrue("zone listing id uses z/ prefix", zone.getId().startsWith("z/fr/DOR/"));
					dorWithZone = true;
				}
			}
		}
		assertTrue("Documentaries page must expose zone listings", dorWithZone);
	}

	private static Map<String, String> discoveryUrlMap() throws IOException {
		final Map<String, String> urls = new HashMap<>();
		final String homeFr = ArteCatalogDiscovery.buildHomeUrl("fr");
		urls.put(homeFr, readFixture("test/resources/fixtures/arte/emac-home-fr.json"));
		urls.put(ArteConf.EMAC_API_BASE + "/fr/tv/pages/HOME/?authorizedCountry=FR",
				readFixture("test/resources/fixtures/arte/emac-home-fr.json"));
		for (final String code : EXPECTED_FR_PAGE_CODES) {
			final String pageUrl = ArteCatalogDiscovery.buildPageUrl("fr", code);
			urls.put(pageUrl,
					"{\"code\":\"" + code + "\",\"metadata\":{\"title\":\"" + code + " title\"},\"zones\":[{\"id\":\"z-"
							+ code + "\",\"code\":\"z-" + code + "\",\"title\":\"Listing\",\"content\":{\"data\":[]}}]}");
		}
		return urls;
	}

	private static String readFixture(final String relativePath) throws IOException {
		try (InputStream input = new FileInputStream(relativePath)) {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final byte[] buffer = new byte[4096];
			int read;
			while ((read = input.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			return out.toString("UTF-8");
		}
	}
}
