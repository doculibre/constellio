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

}
