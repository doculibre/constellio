package com.constellio.data.dao.services;

import org.jdom2.Element;

public class XMLElementUtils {

	public static Element newElementWithContent(String elementName, String textContent) {
		return new Element(elementName).addContent(textContent);
	}

	public static Element newElementWithContent(String elementName, boolean booleanContent) {
		return new Element(elementName).addContent(Boolean.toString(booleanContent));
	}

}
