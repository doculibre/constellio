package com.constellio.data.utils;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import java.util.ArrayList;
import java.util.List;

public class SolrDataUtils {

	public static List<SolrInputDocument> toInputDocuments(List<SolrDocument> documents) {
		List<SolrInputDocument> inputDocuments = new ArrayList<>();
		for (SolrDocument document : documents) {
			SolrInputDocument inputDocument = new SolrInputDocument();
			for (String fieldName : document.getFieldNames()) {
				if (!fieldName.equals("_version_")) {
					Object value = document.get(fieldName);
					inputDocument.addField(fieldName, value);
				}
			}
			inputDocuments.add(inputDocument);
		}

		return inputDocuments;
	}

}
