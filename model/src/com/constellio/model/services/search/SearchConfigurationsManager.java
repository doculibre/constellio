package com.constellio.model.services.search;

import java.util.*;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.data.TextView;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.entities.ResultsElevation;
import com.constellio.model.services.search.entities.ResultsExclusion;

public class SearchConfigurationsManager {

	public static final String SYNONYME_FILE_PATH = "/synonyms.txt";

	Map<String, Map<String, List<String>>> elevatedRecords = new HashMap<>();

	Map<String, Map<String, List<String>>> excludedRecords = new HashMap<>();

	List<String> synonyms = new ArrayList<>();

	DataLayerFactory dataLayerFactory;

	public SearchConfigurationsManager(DataLayerFactory dataLayerFacotry) {
		this.dataLayerFactory = dataLayerFacotry;
		initialize();
	}

	public void initialize() {
		synonyms = getSynonymsOnServer();
}

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
		setSynonyms();
	}

	private void setSynonyms() {
		BigVaultServer server = dataLayerFactory.getRecordsVaultServer();
		AtomicFileSystem solrFileSystem = server.getSolrFileSystem();

		DataWithVersion readData = solrFileSystem.readData(SYNONYME_FILE_PATH);
		TextView aStringView = readData.getView(new TextView());
		aStringView.setData(getSynonymeAsOneString());
		readData.setDataFromView(aStringView);

		solrFileSystem.writeData(SYNONYME_FILE_PATH, readData);
		server.reload();
	}

	private List<String> getSynonymsOnServer() {
		BigVaultServer server = dataLayerFactory.getRecordsVaultServer();
		AtomicFileSystem solrFileSystem = server.getSolrFileSystem();

		DataWithVersion readData = solrFileSystem.readData(SYNONYME_FILE_PATH);
		TextView aStringView = readData.getView(new TextView());

		String synonymsAsOneString = aStringView.getData();

		return Arrays.asList(synonymsAsOneString.split("\\r\\n|\\n|\\r"));
	}


	private String getSynonymeAsOneString() {
		StringBuilder allSynonyms = new StringBuilder();

		for(String synonym : synonyms) {
			allSynonyms.append(synonym + "\n");
		}

		return allSynonyms.toString();
	}
}
