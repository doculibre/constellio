package com.constellio.data.dao.services.bigVault;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;

public class TermVectoreResponse {
	private String uniqueKeyFieldName;
	private String searchedDocId;

	private Map<String, Map<String, Map<String, Map<String, Double>>>> doc2FieldTermVectors = new TreeMap<>();

	public TermVectoreResponse(QueryResponse docTVResponse) {
		@SuppressWarnings("unchecked")
		NamedList<Object> termVecotorsResponse = (NamedList<Object>) docTVResponse.getResponse().get("termVectors");
		init(termVecotorsResponse);
	}

	@SuppressWarnings("unchecked")
	public void init(NamedList<Object> termVecotorsResponse) {
		Iterator<Entry<String, Object>> iterDocs = termVecotorsResponse.iterator();
		while (iterDocs.hasNext()) {
			Entry<String, Object> aDoc = iterDocs.next();
			if (aDoc.getKey().equals("uniqueKeyFieldName")) {
				uniqueKeyFieldName = aDoc.getValue().toString();
			} else {
				NamedList<Object> fields = (NamedList<Object>) aDoc.getValue();
				searchedDocId = aDoc.getKey();
				if (searchedDocId.equals("warnings"))
					return;
				Iterator<Entry<String, Object>> iterField = fields.iterator();

				String docId = null;
				Map<String, Map<String, Map<String, Double>>> fieldTermVectors = new TreeMap<>();
				while (iterField.hasNext()) {
					Entry<String, Object> field = iterField.next();
					if (field.getKey().equals("uniqueKey")) {
						docId = field.getValue().toString();
					} else {
						String fieldName = field.getKey();
						NamedList<NamedList<Object>> termVectors = (NamedList<NamedList<Object>>) field.getValue();
						Iterator<Entry<String, NamedList<Object>>> iterTermVector = termVectors.iterator();
						Map<String, Map<String, Double>> aTermVector = new TreeMap<String, Map<String, Double>>();
						while (iterTermVector.hasNext()) {
							Entry<String, NamedList<Object>> termVector = iterTermVector.next();
							Map<String, Double> info = new TreeMap<>();
							Iterator<Entry<String, Object>> iterInfo = termVector.getValue().iterator();
							while (iterInfo.hasNext()) {
								Entry<String, Object> solrInfo = iterInfo.next();
								info.put(solrInfo.getKey(), Double.parseDouble(solrInfo.getValue().toString()));
							}
							aTermVector.put(termVector.getKey(), info);
						}
						fieldTermVectors.put(fieldName, aTermVector);
					}
				}
				doc2FieldTermVectors.put(docId, fieldTermVectors);
			}
		}
	}

	public Map<String, Map<String, Map<String, Map<String, Double>>>> getDoc2FieldTermVectors() {
		return doc2FieldTermVectors;
	}

	public String getSearchedDocId() {
		return searchedDocId;
	}

	public String getUniqueKeyFieldName() {
		return uniqueKeyFieldName;
	}

}