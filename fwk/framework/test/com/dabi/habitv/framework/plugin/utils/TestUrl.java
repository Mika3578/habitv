package com.dabi.habitv.framework.plugin.utils;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestUrl {

	@Test
	public void test() {
		// Utiliser un site plus stable pour les tests
		String title = RetrieverUtils.getTitleByUrl("https://www.google.com");
		assertNotNull("Le titre ne doit pas être null", title);
		assertTrue("Le titre doit contenir du texte", !title.isEmpty());

		// Test avec redirection - commenté car pourrait être instable en CI
		// String beinsportsTitle = RetrieverUtils.getTitleByUrl("http://www.beinsports.fr");
		// assertNotNull(beinsportsTitle);
	}

}
