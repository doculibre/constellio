package com.constellio.app.services.sip.xsd;

import org.xml.sax.SAXException;

import java.util.List;

public class XMLDocumentValidatorException extends Exception {

	String xml;

	List<String> schemaFilenames;

	public XMLDocumentValidatorException(String xml, List<String> schemaFilenames, SAXException exception) {
		super(exception);
		this.schemaFilenames = schemaFilenames;
		this.xml = xml;
	}

	public String getXml() {
		return xml;
	}

	public List<String> getSchemaFilenames() {
		return schemaFilenames;
	}
}
