package com.dabi.habitv.provider.lemanbleu;

import com.dabi.habitv.framework.FrameworkConf;

/**
 * Léman Bleu public catalogue constants. Progressive Infomaniak MP4 URLs are
 * downloaded with curl. Login, DRM, and geo bypass are intentionally unsupported.
 */
interface LemanBleuConf {

	String NAME = "lemanBleu";

	/** Prefer videos host; www.lemanbleu.ch is often Cloudflare-challenged. */
	String HOME_URL = "https://videos.lemanbleu.ch";

	String PROGRAMS_PATH = "/fr/https-www-lemanbleu-ch-fr-emissions-1-Emissions-html/Emissions.html";

	String ARCHIVE_PATH = "/Scripts/Modules/CustomView/List.aspx?name=Emissions&idn=10701&emission=";

	String EXTENSION = FrameworkConf.MP4;

	String DOWNLOAD_UNAVAILABLE_MESSAGE =
			"This replay is currently unavailable, premium, or not supported. See logs for details.";

	String CATEGORY_SHOW_PREFIX = "lemanbleu:show:";

	String CHANNEL_LABEL = "Léman Bleu";
}
