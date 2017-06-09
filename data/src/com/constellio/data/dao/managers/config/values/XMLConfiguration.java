package com.constellio.data.dao.managers.config.values;

import java.io.IOException;
import java.io.Serializable;

import com.constellio.data.dao.managers.config.ConfigManagerRuntimeException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class XMLConfiguration implements Serializable {

	private final String version;

	private final String hash;

	private final String xml;

	private transient Document document;

	public XMLConfiguration(String version, String hash, Document document) {
		super();
		this.version = version == null || version.equals("-1") ? hash : version;
		this.hash = hash;
		this.document = document;
		this.xml = document == null ? null : toXml(document);
	}

	private String toXml(Document document) {
		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());
		return xmlOutput.outputString(document);
	}

	private Document toDocument(String xml) {
		SAXBuilder builder = new SAXBuilder();
		try {
			return builder.build(xml);
		} catch (JDOMException e) {
			throw new ConfigManagerRuntimeException("JDOM2 Exception", e);
		} catch (IOException e) {
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("build Document JDOM2 from file", e);
		}
	}

	public String getHash() {
		return version;
	}

	public String getRealHash() {
		return hash;
	}

	public Document getDocument() {
		return document.clone();
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	private int serializedCount = 0;
	private int deserializedCount = 0;
	
	private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException {
		try {
			stream.defaultReadObject();
			System.out.println("Deserialized count for " + version + "." + hash + " : " + (++deserializedCount) + " " + new java.util.Date());
//			try {
//				throw new RuntimeException(super.toString());
//			} catch (RuntimeException e) {
////				String stackTrace = org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e);
//				e.printStackTrace();
//			}
		} catch (java.io.IOException | ClassNotFoundException | RuntimeException e) {
			throw e;
		}
		this.document = toDocument(xml);
    }
	
	private void writeObject(java.io.ObjectOutputStream stream)
	            throws java.io.IOException {
		try {
			try {
				throw new RuntimeException(super.toString());
			} catch (RuntimeException e) {
//				String stackTrace = org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e);
				e.printStackTrace();
			}
			System.out.println("Serialized count for " + version + "." + hash + " : " + (++serializedCount) + " " + new java.util.Date());
			stream.defaultWriteObject();
		} catch (java.io.IOException | RuntimeException e) {
			throw e;
		} 
	}
	
}
