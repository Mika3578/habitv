package com.dabi.habitv.provider.sfr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SFRDeclaredVideoUrlTest {

	@Test
	public void extractsFirstQuotedUrl() {
		assertEquals("//cdn.example/video.mp4",
				SFRPluginManager.extractDeclaredVideoUrl("var other = 1;\nvar url = \"//cdn.example/video.mp4\";\n"));
	}

	@Test
	public void doesNotConsumePastTheFirstClosingQuote() {
		assertEquals("first",
				SFRPluginManager.extractDeclaredVideoUrl("var url = \"first\"; var url = \"second\";"));
	}

	@Test
	public void returnsNullWhenDeclarationIsMissing() {
		assertNull(SFRPluginManager.extractDeclaredVideoUrl("<html>no video</html>"));
		assertNull(SFRPluginManager.extractDeclaredVideoUrl(null));
	}

}
