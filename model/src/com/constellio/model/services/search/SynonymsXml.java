package com.constellio.model.services.search;

import org.jdom2.Document;

public abstract class SynonymsXml {
    static final String ROOT = "synonyms";
    static final String DOC = "doc";

    protected Document document;

    public SynonymsXml(Document document) {
        this.document = document;
    }
}
