package com.dabi.habitv.provider.francetv;

import com.dabi.habitv.framework.FrameworkConf;

public interface FranceTvConf {

	String NAME = "francetv";

	String EXTENSION = FrameworkConf.MP4;
	long MAX_CACHE_ARCHIVE_TIME_MS = 300000L; // 5 minutes cache

	String BASE_URL = "https://www.france.tv";
	// Test mode configuration - limits crawling scope for faster tests
	int TEST_MODE_MAX_EPISODES_PER_CATEGORY = 2;  // Even more restrictive
	int TEST_MODE_MAX_CATEGORIES = 1;  // Only test one category
	int TEST_MODE_TIMEOUT_MS = 3000;  // Shorter timeout
	// Modern France.tv API endpoint for video information
	String WS_JSON = "http://webservices.francetelevisions.fr/tools/getInfosOeuvre/v2/?idDiffusion=%s&catalogue=FranceTv&callback=webserviceCallback_%s";

}
