package com.dabi.habitv.framework.plugin.utils;

import org.junit.Test;

/**
 * This is a network-dependent smoke test and must not run as part of deterministic unit tests.
 */
public class RetrieverUtilsNetworkIT {

	@Test
	public void shouldRetrieveBeinSportsTitleFromLiveWebsite() {
		RetrieverUtils.getTitleByUrl("https://www.beinsports.com/fr-fr/videos");
	}

}
