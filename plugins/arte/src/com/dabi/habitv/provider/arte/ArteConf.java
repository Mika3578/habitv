package com.dabi.habitv.provider.arte;

import com.dabi.habitv.framework.FrameworkConf;

interface ArteConf {

	String NAME = "arte";

	// New Arte API URLs
	String HOME_URL = "https://www.arte.tv";
	String API_BASE_URL = "https://api.arte.tv/api/emac/v4";
	
	// Language codes for different Arte sites
	String[] LANGUAGES = {"en", "fr", "de", "es", "pl", "it"};
	
	// Main category IDs
	String[] CATEGORIES = {
		"CIN",    // Cinema
		"DOR",    // Documentaries and Reportage
		"CRE",    // Creative
		"POL",    // Politics and Society
		"SCI",    // Science
		"CON",    // Concerts
		"JEU",    // Youth
		"SPO",    // Sports
		"REPLAY"  // Replay videos
	};

	String EXTENSION = FrameworkConf.MP4;

}
