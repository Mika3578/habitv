package com.dabi.habitv.utils;

import java.io.InputStream;
import java.text.Normalizer;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.dabi.habitv.api.plugin.exception.TechnicalException;

public final class FileUtils {

	private FileUtils() {

	}

	/**
	 * replace illegal characters in a filename with "_" illegal characters : :
	 * \ / * ? | < >
	 * <p>
	 * Legacy behavior: also strips accents / non-ASCII. Prefer
	 * {@link #sanitizePathSegment(String)} for MEDIA_SERVER naming so French
	 * titles keep their accents.
	 * 
	 * @param name
	 * @return
	 */
	public static String sanitizeFilename(final String name) {
		return removeNonASCII(name.replaceAll("[%\\s,:\\\\/*?!|<>&«»()\"\'\\$]",
				"_").replaceAll("__", "_"));
	}

	/**
	 * Filesystem-safe path segment that preserves Unicode (including French
	 * accents). Replaces only Windows-illegal and control characters, then
	 * collapses whitespace.
	 *
	 * @param name raw segment
	 * @return sanitized segment, never null
	 */
	public static String sanitizePathSegment(final String name) {
		if (name == null) {
			return "";
		}
		final StringBuilder builder = new StringBuilder(name.length());
		boolean lastWasSpace = false;
		for (int i = 0; i < name.length(); i++) {
			final char c = name.charAt(i);
			if (c < 32 || c == 127 || c == '<' || c == '>' || c == ':' || c == '"' || c == '/'
					|| c == '\\' || c == '|' || c == '?' || c == '*') {
				if (!lastWasSpace && builder.length() > 0) {
					builder.append(' ');
					lastWasSpace = true;
				}
				continue;
			}
			if (Character.isWhitespace(c)) {
				if (!lastWasSpace && builder.length() > 0) {
					builder.append(' ');
					lastWasSpace = true;
				}
				continue;
			}
			builder.append(c);
			lastWasSpace = false;
		}
		String result = builder.toString().trim();
		while (result.endsWith(".")) {
			result = result.substring(0, result.length() - 1).trim();
		}
		return result;
	}

	private static String removeNonASCII(final String string) {
		return Normalizer.normalize(string, Normalizer.Form.NFD)
				.replaceAll("[\u0300-\u036F]", "")
				.replaceAll("[^\\x00-\\x7F]", "");
	}

	public static void setValidation(final Unmarshaller unmarshaller,
			final String xsdFile) {
		final Schema schema = buildSchema(xsdFile);
		unmarshaller.setSchema(schema);
	}

	public static void setValidation(final Marshaller unmarshaller,
			final String xsdFile) {
		final Schema schema = buildSchema(xsdFile);
		unmarshaller.setSchema(schema);
	}

	private static Schema buildSchema(final String xsdFile) {
		final SchemaFactory schemaFactory = SchemaFactory
				.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
		final Schema schema;
		try {
			schema = schemaFactory.newSchema(new StreamSource(
					getInputFileInClasspath(xsdFile)));
		} catch (final SAXException e) {
			throw new TechnicalException(e);
		}
		return schema;
	}

	private static InputStream getInputFileInClasspath(final String file) {
		return Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(file);
	}
}
