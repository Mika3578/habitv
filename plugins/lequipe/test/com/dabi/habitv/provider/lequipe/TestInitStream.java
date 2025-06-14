package com.dabi.habitv.provider.lequipe;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.dabi.habitv.api.plugin.exception.TechnicalException;

@Ignore
public class TestInitStream {

	private static final Pattern PLALYER_APPPATTERN = Pattern.compile("<playerAppToken>(.*)</playerAppToken>");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void test() {
		try {
			final String url = "http://api.kewego.com/config/getStreamInit/";
			final URLConnection hc = (new java.net.URI(url).toURL()).openConnection();
			hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:14.0) Gecko/20100101 Firefox/14.0.1");
			// hc.setRequestProperty("Referer",
			// "http://s.kewego.com/swf/kp.swf?v=20120530");
			hc.setRequestProperty("X-KDORIGIN", "http%3A//video.lequipe.fr/video/football/foot-l1-video-bande-annonce-saison-2012-2013/%3Fsig%3Db640e3b69ces");
			// Construct data
			String data = URLEncoder.encode("player_type", "UTF-8") + "=" + URLEncoder.encode("kp", "UTF-8");
			// language_code=fr&amp;playerKey=0f932efcc6b9&amp;configKey=&amp;suffix=&amp;sig=3d44175f395s
			// final String sig = "b640e3b69ces";
			// language_code=fr&amp;playerKey=b905c4789fb0&amp;&amp;suffix=&amp;sig=3aa70baf76cs
			// final String sig = "3d44175f395s";
			final String sig = "3aa70baf76cs";
			data += "&" + URLEncoder.encode("sig", "UTF-8") + "=" + URLEncoder.encode(sig, "UTF-8");
			// String playerKey = "b7a2e6ce1157";
			// final String playerKey = "0f932efcc6b9";
			final String playerKey = "b905c4789fb0";
			data += "&" + URLEncoder.encode("playerKey", "UTF-8") + "=" + URLEncoder.encode(playerKey, "UTF-8");
			data += "&" + URLEncoder.encode("request_verbose", "UTF-8") + "=" + URLEncoder.encode("false", "UTF-8");
			data += "&" + URLEncoder.encode("language_code", "UTF-8") + "=" + URLEncoder.encode("fr", "UTF-8");
			hc.setDoOutput(true);
			final OutputStreamWriter wr = new OutputStreamWriter(hc.getOutputStream());
			wr.write(data);
			wr.flush();

			// Get the response
			final BufferedReader rd = new BufferedReader(new InputStreamReader(hc.getInputStream()));
			String line;
			final StringBuilder builder = new StringBuilder();
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
				builder.append(line);
			}
			wr.close();
			rd.close();

			final Matcher matcher = PLALYER_APPPATTERN.matcher(builder.toString());
			final boolean hasMatched = matcher.find();
			String ret = null;
			if (hasMatched) {
				ret = matcher.group(matcher.groupCount());
			}
			assertEquals(160, ret.length());
			System.out.println("curl \"http://api.kewego.com/video/getStream/?appToken=" + ret + "&sig=" + sig + "&format=w640&v=2749\""
					+ "  -C - -L -g -A \"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)\" -o \"test.mp4\" ");
		} catch (final IOException | URISyntaxException e) {
			throw new TechnicalException(e);
		}
	}
}
