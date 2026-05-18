package com.dabi.habitv.utils;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.dabi.habitv.api.plugin.exception.TechnicalException;

public class XMLUtils {

	public static Object buildAnyElement(String elementName,
			String elementValue) {
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
	

	public static String getTagValue(Object node) {
		return ((org.w3c.dom.Node) node).getTextContent();
	}

	public static String getTagName(Object node) {
		return ((org.w3c.dom.Node) node).getLocalName();
	}

	public static void setTagValue(Object node, String value) {
		((org.w3c.dom.Node) node).setTextContent(value);
	}
}
