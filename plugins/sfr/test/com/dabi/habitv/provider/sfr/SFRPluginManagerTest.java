package com.dabi.habitv.provider.sfr;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;
import com.dabi.habitv.plugintester.BasePluginProviderTester;

public class SFRPluginManagerTest extends BasePluginProviderTester {

	@Test
	public final void testSFR() throws InstantiationException, IllegalAccessException, DownloadFailedException {
		testPluginProvider(SFRPluginManager.class, true);
	}

	@Test
	public final void specificFindEp() throws DownloadFailedException {
		Set<EpisodeDTO> ep;

		ep = new SFRPluginManager().findEpisode(new CategoryDTO("sfr+", "Premier League",
				"footballpremierleague", "mp4"));
		LOG.error(ep);
	}

	@Test
	public final void urlPatternCapturesFirstQuotedValueOnly() throws Exception {
		final Field patternField = SFRPluginManager.class.getDeclaredField("URL_PATTERN");
		patternField.setAccessible(true);
		final Pattern pattern = (Pattern) patternField.get(null);

		final String content = "var a = \"before\"; var url = \"https://video.sfr.fr/video.m4u\"; var b = \"after\";";
		final Matcher matcher = pattern.matcher(content);
		assertTrue(matcher.find());
		assertEquals("https://video.sfr.fr/video.m4u", matcher.group(matcher.groupCount()));
		assertFalse(matcher.find());
	}

	@Test
	public final void urlPatternRejectsUnquotedValue() throws Exception {
		final Field patternField = SFRPluginManager.class.getDeclaredField("URL_PATTERN");
		patternField.setAccessible(true);
		final Pattern pattern = (Pattern) patternField.get(null);

		assertFalse(pattern.matcher("var url = 'https://video.sfr.fr/video.m4u';").find());
		assertFalse(pattern.matcher("var url = \"unterminated").find());
	}
}
