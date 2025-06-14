package com.dabi.habitv.utils;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.dabi.habitv.api.plugin.exception.TechnicalException;

/**
 * Utility class for XML operations.
 * This class provides static methods for common XML operations.
 */
public final class XMLUtils {

	/**
	 * Private constructor to prevent instantiation of utility class.
	 */
	private XMLUtils() {
		// Utility class - prevent instantiation
	}

	/**
	 * Creates a new XML element with the given name and value.
	 * 
	 * @param document the XML document
	 * @param elementName the name of the element
	 * @param elementValue the value of the element
	 * @return the created XML element
	 */
	public static Element createElement(final Document document, final String elementName, 
			final String elementValue) {
		Element element = document.createElement(elementName);
		element.setTextContent(elementValue);
		return element;
	}

	/**
	 * Builds any XML element with the given name and value.
	 * 
	 * @param elementName the name of the element
	 * @param elementValue the value of the element
	 * @return the created XML element
	 */
	public static Object buildAnyElement(final String elementName, final String elementValue) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document doc;
		try {
			doc = factory.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			throw new TechnicalException(e);
		}
		Element node = doc.createElement(elementName);
		node.setTextContent(elementValue);
		return node;
	}
	
	/**
	 * Gets the text content of a node.
	 * 
	 * @param node the XML node
	 * @return the text content of the node
	 */
	public static String getTagValue(final Object node) {
		return ((Node) node).getTextContent();
	}

	/**
	 * Gets the local name of a node.
	 * 
	 * @param node the XML node
	 * @return the local name of the node
	 */
	public static String getTagName(final Object node) {
		return ((Node) node).getLocalName();
	}
}
