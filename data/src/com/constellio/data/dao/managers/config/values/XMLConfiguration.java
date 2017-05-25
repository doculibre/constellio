package com.constellio.data.dao.managers.config.values;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jdom2.Document;

public class XMLConfiguration implements Serializable {

	private final String version;

	private final String hash;

	private final Document document;

	public XMLConfiguration(String version, String hash, Document document) {
		super();
		this.version = version == null || version.equals("-1") ? hash : version;
		this.hash = hash;
		this.document = document;
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
		} catch (java.io.IOException | ClassNotFoundException | RuntimeException e) {
			throw e;
		} 
    }
	
	private void writeObject(java.io.ObjectOutputStream stream)
	            throws java.io.IOException {
		try {
			System.out.println("Serialized count for " + version + "." + hash + " : " + (++serializedCount) + " " + new java.util.Date());
			stream.defaultWriteObject();
		} catch (java.io.IOException | RuntimeException e) {
			throw e;
		} 
	}
	
}
