package com.constellio.model.services.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.records.Record;

public class SearchConfigurationsManager {

	Map<String, List<String>> elevatedRecords = new HashMap<>();

	Map<String, List<String>> excludedRecords = new HashMap<>();

	List<String> synonyms = new ArrayList<>();

	public boolean isElevated(Record record) {
		List<String> collectionElevatedRecords = elevatedRecords.get(record.getCollection());
		return collectionElevatedRecords == null ? false : collectionElevatedRecords.contains(record.getId());
	}

	public boolean isExcluded(Record record) {
		List<String> collectionExcludedRecords = excludedRecords.get(record.getCollection());
		return collectionExcludedRecords == null ? false : collectionExcludedRecords.contains(record.getId());
	}

	public void setElevated(Record record, boolean value) {
		if (!elevatedRecords.containsKey(record.getCollection())) {
			elevatedRecords.put(record.getCollection(), new ArrayList<String>());
		}
		elevatedRecords.get(record.getCollection()).add(record.getId());
	}

	public void setExcluded(Record record, boolean value) {
		if (!excludedRecords.containsKey(record.getCollection())) {
			excludedRecords.put(record.getCollection(), new ArrayList<String>());
		}
		excludedRecords.get(record.getCollection()).add(record.getId());
	}

	public List<String> getElevatedRecords(String collection) {
		List<String> collectionElevatedRecords = elevatedRecords.get(collection);
		return collectionElevatedRecords == null ?
				Collections.<String>emptyList() :
				Collections.unmodifiableList(collectionElevatedRecords);
	}

	public List<String> getExcludedRecords(String collection) {
		List<String> collectionExcludedRecords = excludedRecords.get(collection);
		return collectionExcludedRecords == null ?
				Collections.<String>emptyList() :
				Collections.unmodifiableList(collectionExcludedRecords);
	}

	public void setElevatedRecords(String collection, List<String> ids) {
		if (ids == null || ids.isEmpty()) {
			elevatedRecords.remove(collection);
		} else {
			elevatedRecords.put(collection, new ArrayList<>(ids));
		}
	}

	public void setExcludedRecords(String collection, List<String> ids) {
		if (ids == null || ids.isEmpty()) {
			excludedRecords.remove(collection);
		} else {
			excludedRecords.put(collection, new ArrayList<>(ids));
		}
	}

	public List<String> getSynonyms() {
		return Collections.unmodifiableList(synonyms);
	}

	public void setSynonyms(List<String> synonyms) {
		this.synonyms = new ArrayList<>(synonyms);
	}
}
