package com.dabi.habitv.provider.novo19;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Novo19PathRulesTest {

	@Test
	public void excludesPersonalAndLivePaths() {
		assertTrue(Novo19PathRules.isExcludedPublicPath("/mes-videos"));
		assertTrue(Novo19PathRules.isExcludedPublicPath("/player/novo19"));
		assertTrue(Novo19PathRules.isExcludedPublicPath("/podcasts"));
		assertFalse(Novo19PathRules.isExcludedPublicPath("/details/sample"));
	}

}
