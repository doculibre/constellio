package com.constellio.model.services.search;

import org.jdom2.Document;

public abstract class ElevationsXml {
    static final String ROOT = "elevate";
    static final String QUERY = "query";
    static final String QUERY_TEXT_ATTR = "text";
    static final String DOC = "doc";
    static final String DOC_EXCLUDE_ATTR = "exclude";
    static final String DOC_ID_ATTR = "id";

    protected Document document;

    public ElevationsXml(Document document) {
        this.document = document;
    }
}
