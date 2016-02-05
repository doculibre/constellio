package com.constellio.model.services.search.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQuerySignature;

public class SerializableSearchCache implements Serializable {

	Map<String, Map<String, List<String>>> highlights = new HashMap<>();
	private LogicalSearchQuerySignature previousQuery;

	private List<Record> results = new ArrayList<>();
	private List<String> resultIds = new ArrayList<>();
	private int size = -1;

	public Record getCachedRecord(int index) {
		if (index < results.size()) {
			return results.get(index);
		}
		return null;
	}

	public String getCachedId(int index) {
		if (index < resultIds.size()) {
			return resultIds.get(index);
		}
		return null;
	}

	public void setRecord(int i, Record record) {
		while (results.size() <= i) {
			results.add(null);
		}
		results.set(i, record
		);
	}

	public void setRecordId(int i, String id) {
		while (resultIds.size() <= i) {
			resultIds.add(null);
		}
		resultIds.set(i, id);
	}

	public void setRecordHighLighting(String id, Map<String, List<String>> recordHighlighting) {
		highlights.put(id, recordHighlighting);
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public void clear() {
		resultIds = new ArrayList<>();
		highlights.clear();
		size = -1;
	}

	public void initializeFor(LogicalSearchQuery query) {
		LogicalSearchQuerySignature querySignature = LogicalSearchQuerySignature.signature(query);

		if (previousQuery == null || !querySignature.equals(previousQuery)) {
			previousQuery = querySignature;
			clear();
		}
	}

	public Map<String, Map<String, List<String>>> getHighlightingMap() {
		return highlights;
	}

}
