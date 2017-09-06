package com.constellio.model.services.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.entities.ResultsElevation;
import com.constellio.model.services.search.entities.ResultsExclusion;

public class SearchConfigurationsManager {

	Map<String, Map<String, List<String>>> elevatedRecords = new HashMap<>();

	Map<String, Map<String, List<String>>> excludedRecords = new HashMap<>();

	List<String> synonyms = new ArrayList<>();

	public boolean isElevated(String query, Record record) {
		Map<String, List<String>> collectionElevations = elevatedRecords.get(record.getCollection());

		if (collectionElevations == null) {
			return false;
		} else {
			List<String> collectionElevatedRecords = collectionElevations.get(query);
			return collectionElevatedRecords == null ? false : collectionElevatedRecords.contains(record.getId());
		}

	}

	public boolean isExcluded(String query, Record record) {
		Map<String, List<String>> collectionExclusions = excludedRecords.get(record.getCollection());

		if (collectionExclusions == null) {
			return false;
		} else {
			List<String> collectionElevatedRecords = collectionExclusions.get(query);
			return collectionElevatedRecords == null ? false : collectionElevatedRecords.contains(record.getId());
		}
	}

	public void setElevated(String query, Record record, boolean value) {

		Map<String, List<String>> collectionElevations = elevatedRecords.get(record.getCollection());

		if (collectionElevations == null) {
			collectionElevations = new HashMap<>();
			elevatedRecords.put(record.getCollection(), collectionElevations);
		}

		if (!collectionElevations.containsKey(record.getCollection())) {
			collectionElevations.put(record.getCollection(), new ArrayList<String>());
		}
		collectionElevations.get(record.getCollection()).add(record.getId());
	}

	public void setExcluded(String query, Record record, boolean value) {
		Map<String, List<String>> collectionElevations = excludedRecords.get(record.getCollection());

		if (collectionElevations == null) {
			collectionElevations = new HashMap<>();
			excludedRecords.put(record.getCollection(), collectionElevations);
		}

		if (!collectionElevations.containsKey(record.getCollection())) {
			collectionElevations.put(record.getCollection(), new ArrayList<String>());
		}
		collectionElevations.get(record.getCollection()).add(record.getId());
	}

	public List<ResultsElevation> getElevations(String collection) {
		Map<String, List<String>> collectionElevations = elevatedRecords.get(collection);
		if (collectionElevations == null) {
			return Collections.emptyList();
		} else {
			List<ResultsElevation> elevations = new ArrayList<>();

			for (Map.Entry<String, List<String>> anElevation : collectionElevations.entrySet()) {
				elevations.add(new ResultsElevation(anElevation.getKey(), new ArrayList<>(anElevation.getValue())));
			}

			return elevations;
		}
	}

	public List<ResultsExclusion> getExclusions(String collection) {
		Map<String, List<String>> collectionElevations = elevatedRecords.get(collection);
		if (collectionElevations == null) {
			return Collections.emptyList();
		} else {
			List<ResultsExclusion> elevations = new ArrayList<>();

			for (Map.Entry<String, List<String>> anElevation : collectionElevations.entrySet()) {
				elevations.add(new ResultsExclusion(anElevation.getKey(), new ArrayList<>(anElevation.getValue())));
			}

			return elevations;
		}
	}

	public void setElevatedRecords(String collection, String query, List<String> ids) {
		if (ids == null || ids.isEmpty()) {
			if (elevatedRecords.containsKey(collection)) {
				elevatedRecords.get(collection).remove(query);
			}
		} else {
			if (!elevatedRecords.containsKey(collection)) {
				elevatedRecords.put(collection, new HashMap<String, List<String>>());
			}
			elevatedRecords.get(collection).put(query, new ArrayList<>(ids));
		}
	}

	public void setExcludedRecords(String collection, String query, List<String> ids) {
		if (ids == null || ids.isEmpty()) {
			if (excludedRecords.containsKey(collection)) {
				excludedRecords.get(collection).remove(query);
			}
		} else {
			if (!excludedRecords.containsKey(collection)) {
				excludedRecords.put(collection, new HashMap<String, List<String>>());
			}
			excludedRecords.get(collection).put(query, new ArrayList<>(ids));
		}
	}

	public void removeCollectionExclusions(String collection) {
		excludedRecords.remove(collection);
	}

	public void removeCollectionElevations(String collection) {
		elevatedRecords.remove(collection);
	}

	public List<String> getSynonyms() {
		return Collections.unmodifiableList(synonyms);
	}

	public void setSynonyms(List<String> synonyms) {
		this.synonyms = new ArrayList<>(synonyms);
	}
}
