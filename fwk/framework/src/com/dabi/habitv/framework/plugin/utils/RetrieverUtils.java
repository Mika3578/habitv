package com.dabi.habitv.framework.plugin.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.jsoup.Jsoup;

import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.framework.FrameworkConf;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

/**
 * utils function to retrieve remote data
 * 
 */
public final class RetrieverUtils {

	private static final String USER_AGENT = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)";

	private RetrieverUtils() {

	}

	/**
	 * Create an inputStream from an URL throws runtime technical exception if
	 * fail
	 * 
	 * @param url
	 *            the URL
	 * @param proxy
	 *            the proxy to use for connection
	 * @return the input stream
	 */
	public static InputStream getInputStreamFromUrl(final String url, final Proxy proxy) {
		return getInputStreamFromUrl(url, FrameworkConf.TIME_OUT_MS, proxy);
	}

	public static InputStream getInputStreamFromUrl(final String url, final Integer timeOut, final Proxy proxy) {
		try {
			HttpURLConnection hc = openConnection(url, proxy);
			prepareConnection(timeOut, hc);

			boolean redirect = false;

			// normally, 3xx is redirect
			int status = hc.getResponseCode();
			if (status != HttpURLConnection.HTTP_OK) {	
				if (status == HttpURLConnection.HTTP_MOVED_TEMP
						|| status == HttpURLConnection.HTTP_MOVED_PERM
						|| status == HttpURLConnection.HTTP_SEE_OTHER) {
					redirect = true;
				}
			}

			if (redirect) {
				// get redirect url from "location" header field
				String newUrl = hc.getHeaderField("Location");
				// open the new connnection again
				hc = (HttpURLConnection) new URL(newUrl).openConnection();
				prepareConnection(timeOut, hc);
			}

			return hc.getInputStream();
		} catch (final IOException e) {
			throw new TechnicalException(e);
		}
	}

	private static void prepareConnection(final Integer timeOut, final HttpURLConnection hc) {
		if (timeOut != null) {
			hc.setConnectTimeout(timeOut);
			hc.setReadTimeout(timeOut);
		}
		hc.setRequestProperty("User-Agent", USER_AGENT);
	}

	private static HttpURLConnection openConnection(final String url, final Proxy proxy) throws IOException {
		if (useProxy(proxy)) {
			return (HttpURLConnection) (new URL(url)).openConnection(proxy);
		} else {
			return (HttpURLConnection) (new URL(url)).openConnection();
		}
	}

	public static boolean useProxy(final Proxy proxy) {
		return proxy != null && !hasGlobalProxy();
	}

	private static boolean hasGlobalProxy() {
		final String globaProxy = System.getProperty("http.proxyHost");
		return globaProxy != null && !globaProxy.isEmpty();
	}

	/**
	 * Unmarshal the inputstream to an Object to be casted
	 * 
	 * @param input
	 *            the input stream
	 * @param unmarshallerPackage
	 *            the jaxb entities package
	 * @param classLoader
	 *            the classLoader since plugin can be load with a different
	 *            classloader
	 * @return the unmarshalled object to be casted
	 */
	public static Object unmarshalInputStream(final InputStream input, final String unmarshallerPackage, final ClassLoader classLoader) {
		try {
			final JAXBContext context;
			if (classLoader == null) {
				context = JAXBContext.newInstance(unmarshallerPackage);
			} else {
				context = JAXBContext.newInstance(unmarshallerPackage, classLoader);
			}
			return context.createUnmarshaller().unmarshal(input);
		} catch (final JAXBException e) {
			throw new TechnicalException(e);
		}
	}

	/**
	 * @see #unmarshalInputStream(InputStream, String) without the classloader
	 */
	public static Object unmarshalInputStream(final InputStream input, final String unmarshallerPackage) {
		return unmarshalInputStream(input, unmarshallerPackage, null);
	}

	public static String getUrlContent(final String url, final Proxy proxy) {
		return getUrlContent(url, null, proxy);
	}

	public static String getUrlContent(final String url, final String encoding, final Proxy proxy) {

		final InputStream in = getInputStreamFromUrl(url, proxy);
		final BufferedReader reader;
		try {
			if (encoding != null) {
				reader = new BufferedReader(new InputStreamReader(in, encoding));
			} else {
				reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			}
		} catch (final UnsupportedEncodingException e) {
			throw new TechnicalException(e);
		}
		final StringBuilder sb = new StringBuilder();

		String readLine;
		try {
			while ((readLine = reader.readLine()) != null) {
				sb.append(readLine).append("\r\n");
			}
		} catch (final IOException e) {
			throw new TechnicalException(e);
		}

		return sb.toString();

	}

	public static byte[] getUrlContentBytes(final String url, final Proxy proxy) {
		final InputStream in = getInputStreamFromUrl(url, null, proxy);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i;
		try {
			while ((i = in.read()) != -1) {
				baos.write(i);
				baos.flush();
			}
			baos.close();
			in.close();
		} catch (final IOException e) {
			throw new TechnicalException(e);
		} finally {
			try {
				in.close();
			} catch (final IOException e) {
				// should log ?
			}
		}
		return baos.toByteArray();
	}

	public static String getTitleByUrl(String url) {
		try {
			// Configure Jsoup to follow redirects and use a proper user agent
			return Jsoup.connect(url)
					.userAgent(USER_AGENT)
					.followRedirects(true)
					.timeout(FrameworkConf.TIME_OUT_MS)
					.get()
					.title();
		} catch (IOException e) {
			final SyndFeedInput input = new SyndFeedInput();
			try {
				final SyndFeed feed = input.build(new XmlReader(getInputStreamFromUrl(url, null)));
				return feed.getTitle();
			} catch (IllegalArgumentException | FeedException | IOException e1) {
				throw new TechnicalException(e);
			}
		}
	}
}
