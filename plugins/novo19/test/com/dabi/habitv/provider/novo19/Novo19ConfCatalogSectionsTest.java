package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Novo19ConfCatalogSectionsTest {

	@Test
	public void exposesConfiguredCatalogueSectionLabels() {
		assertEquals("Nos films", Novo19Conf.SECTION_FILMS);
		assertEquals("Nos séries", Novo19Conf.SECTION_SERIES);
		assertEquals("Nos podcasts", Novo19Conf.SECTION_PODCASTS);
		assertEquals("Nos divertissements", Novo19Conf.SECTION_DIVERTISSEMENTS);
	}

	@Test
	public void podcastArgsDoNotDisableTlsVerification() {
		assertFalse(Novo19Conf.PODCAST_YT_DLP_ARGS.contains("--no-check-certificate"));
	}

}
