package com.dabi.habitv.core.config;


public interface HabitTvConf {

	String DEFAULT_EXPORTER = "cmd";

	String GRABCONFIG_XML_FILE = "grabconfig.xml";

	String ENCODING = "UTF-8";

	String OLD_CONF_FILE = "config.xml";

	String CONF_FILE = "configuration.xml";	

	String STAT_ENABLED_PROPERTY = "habitv.stat.enabled";

	String STAT_URL_PROPERTY = "habitv.stat.url";

	// Telemetry endpoint is disabled by default and requires explicit opt-in.
	String STAT_URL_DISABLED = "";
	
	String LOG_FILE = "habiTv.log"; 

}
