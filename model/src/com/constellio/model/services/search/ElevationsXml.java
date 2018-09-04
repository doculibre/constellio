package com.constellio.model.services.search;

import org.jdom2.Document;

public abstract class ElevationsXml {
	static final String ROOT = "elevationOrExclusion";
	static final String ELEVATION = "elevate";
	static final String QUERY = "query";
	static final String QUERY_TEXT_ATTR = "text";
	static final String DOC = "doc";
	static final String DOC_ID_ATTR = "id";
	static final String EXCLUSION = "exclude";

	protected Document document;

	public ElevationsXml(Document document) {
		this.document = document;
	}
}
