package com.dabi.habitv.framework.plugin.utils;

import org.junit.Test;
import org.junit.Ignore;

public class TestUrl {

	@Test
	@Ignore("Network-dependent test; skipped in CI.")
	public void test() {

		RetrieverUtils.getTitleByUrl("http://www.beinsports.fr");
		// RetrieverUtils.getTitleByUrl("http%3A%2F%2Fwww.beinsports.fr%2Fvideos%2Ftitle%2FLiga%20%3A%20Barcelone%204-0%20Almeria%2Farticle%2F3kgv5a6r3li41xod5wm0znvz5");
		// RetrieverUtils.getTitleByUrl("http://www.beinsports.fr/videos/title/Liga : Barcelone 4-0 Almeria/article/3kgv5a6r3li41xod5wm0znvz5");
	}

}
