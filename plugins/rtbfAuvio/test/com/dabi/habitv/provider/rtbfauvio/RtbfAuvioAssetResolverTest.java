package com.dabi.habitv.provider.rtbfauvio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;

public class RtbfAuvioAssetResolverTest {

	@Test
	public void resolvesAssetFromMediaSlug() {
		assertEquals("3517841_6BA97Bb", RtbfAuvioAssetResolver.resolveFromMediaUrl(
				"https://auvio.rtbf.be/media/alex-hugo-alex-hugo-3517841"));
	}

	@Test
	public void resolvesFromDownloadParam() {
		final DownloadParamDTO param = new DownloadParamDTO(
				"https://auvio.rtbf.be/media/demo-3518500", "out.mp4", RtbfAuvioConf.EXTENSION);
		assertEquals("3518500_6BA97Bb", RtbfAuvioAssetResolver.resolveAssetId(param));
	}

	@Test
	public void rejectsNonAuvioHost() {
		assertNull(RtbfAuvioAssetResolver.resolveFromMediaUrl("https://www.rtbf.be/"));
	}
}
