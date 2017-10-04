package com.constellio.model.services.search;

import java.util.*;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.data.TextView;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.apache.commons.lang.StringUtils;

public class SearchConfigurationsManager {
	public static final String ELEVATE_FILE_NAME = "/elevate.xml";
	public static final String SYNONYME_FILE_PATH = "/synonyms.txt";

	ConstellioCache constellioCache;

	List<String> synonyms = new ArrayList<>();

	DataLayerFactory dataLayerFactory;
	ConstellioCacheManager constellioCacheManager;

	ModelLayerFactory modelLayerFactory;
	private BigVaultServer server;

	public SearchConfigurationsManager(DataLayerFactory dataLayerFacotry, ModelLayerFactory modelLayerFactory) {
		this.dataLayerFactory = dataLayerFacotry;
		this.modelLayerFactory = modelLayerFactory;
		server = dataLayerFacotry.getRecordsVaultServer();
		constellioCacheManager = dataLayerFactory.getSettingsCacheManager();
		constellioCache = constellioCacheManager.getCache(SearchConfigurationsManager.class.getName());
		initialize();
	}

	public void initialize() {
		synonyms = getSynonymsOnServer();
		Elevations elevations = getAllElevationsFromDisk();

		for(Elevations.QueryElevation queryElevation : elevations.getQueryElevations()) {
			constellioCache.put(queryElevation.getQuery(), (ArrayList) queryElevation.getDocElevations());
		}
	}

	public List<Elevations.QueryElevation.DocElevation> getDocElevation(String query){
		return constellioCache.get(query);
	}

	public Elevations getAllElevationsFromDisk() {
		AtomicFileSystem solrFileSystem = server.getSolrFileSystem();

		DataWithVersion readData = solrFileSystem.readData(ELEVATE_FILE_NAME);
		ElevationsView anElevationsView = readData.getView(new ElevationsView());

		// Facebook.com youtube.com

		Elevations elevations = anElevationsView.getData();

		return elevations;
	}

	public List<String> getAllQuery() {
		List<String> allQuery = new ArrayList<>();
		for(Iterator<String> iterator = constellioCache.keySet(); iterator.hasNext();) {
			allQuery.add(iterator.next());
		}
		return allQuery;
	}

	public boolean isElevated(String freeTextQuery, Record record) {
		boolean found = false;
		// Facebook.com youtube.com

		ArrayList<Elevations.QueryElevation.DocElevation> docElevations = constellioCache.get(freeTextQuery);

		if(docElevations != null) {
			for (Elevations.QueryElevation.DocElevation docElevation : docElevations) {
				if (record.getId().equals(docElevation.getId())&& !docElevation.isExclude()) {
					found = true;
					break;
				}
			}
		}

		return found;
	}

	public boolean isExcluded(String freeTextQuery, Record record) {
		boolean found = false;
		// Facebook.com youtube.com

		ArrayList<Elevations.QueryElevation.DocElevation> docElevations = constellioCache.get(freeTextQuery);

		if(docElevations != null) {
			for (Elevations.QueryElevation.DocElevation docElevation : docElevations) {
				if (record.getId().equals(docElevation.getId())&& docElevation.isExclude()) {
					found = true;
					break;
				}
			}
		}

		return found;
	}

	public void removeQuery(String query) {
		AtomicFileSystem solrFileSystem = server.getSolrFileSystem();

		DataWithVersion readData = solrFileSystem.readData(ELEVATE_FILE_NAME);
		ElevationsView anElevationsView = readData.getView(new ElevationsView());

		Elevations elevations = anElevationsView.getData();

		elevations.removeQueryElevation(query);

		anElevationsView.setData(elevations);
		readData.setDataFromView(anElevationsView);

		solrFileSystem.writeData(ELEVATE_FILE_NAME, readData);
		solrFileSystem.close();


		constellioCache.remove(query);

		server.reload();
	}

	public void removeAllExclusion(String query) {
		AtomicFileSystem solrFileSystem = server.getSolrFileSystem();

		DataWithVersion readData = solrFileSystem.readData(ELEVATE_FILE_NAME);
		ElevationsView anElevationsView = readData.getView(new ElevationsView());

		Elevations elevations = anElevationsView.getData();

		elevations.removeAllExclusion(query);

		anElevationsView.setData(elevations);
		readData.setDataFromView(anElevationsView);

		Elevations.QueryElevation queryElevation = elevations.getQueryElevation(query);

		solrFileSystem.writeData(ELEVATE_FILE_NAME, readData);
		solrFileSystem.close();

		if(queryElevation != null && queryElevation.getDocElevations().size() > 0) {
			constellioCache.put(query, (ArrayList) queryElevation.getDocElevations());
		} else {
			constellioCache.remove(query);
		}

		server.reload();
	}

	public void removeAllElevation(String query) {
		AtomicFileSystem solrFileSystem = server.getSolrFileSystem();

		DataWithVersion readData = solrFileSystem.readData(ELEVATE_FILE_NAME);
		ElevationsView anElevationsView = readData.getView(new ElevationsView());

		Elevations elevations = anElevationsView.getData();

		elevations.removeAllElevation(query);

		anElevationsView.setData(elevations);
		readData.setDataFromView(anElevationsView);

		solrFileSystem.writeData(ELEVATE_FILE_NAME, readData);
		solrFileSystem.close();

		Elevations.QueryElevation queryElevation = elevations.getQueryElevation(query);
		if(queryElevation != null && queryElevation.getDocElevations().size() > 0) {
			constellioCache.put(query, (ArrayList) queryElevation.getDocElevations());
		} else {
			constellioCache.remove(query);
		}
	}


	public void removeElevated(String freeTextQuery, String recordId) {
		if (StringUtils.isBlank(freeTextQuery)) {
			freeTextQuery = "*:*";
		}
		AtomicFileSystem solrFileSystem = server.getSolrFileSystem();

		DataWithVersion readData = solrFileSystem.readData(ELEVATE_FILE_NAME);
		ElevationsView anElevationsView = readData.getView(new ElevationsView());

		Elevations elevations = anElevationsView.getData();

		elevations.removeElevation(freeTextQuery, recordId);

		anElevationsView.setData(elevations);
		readData.setDataFromView(anElevationsView);

		solrFileSystem.writeData(ELEVATE_FILE_NAME, readData);
		solrFileSystem.close();

		ArrayList<Elevations.QueryElevation.DocElevation> docElevations = constellioCache.get(freeTextQuery);
		for (Iterator<Elevations.QueryElevation.DocElevation> iterator = docElevations.iterator(); iterator.hasNext(); ) {
			Elevations.QueryElevation.DocElevation docElevation = iterator.next();
			if(docElevation.getId().equals(recordId)) {
				iterator.remove();
				break;
			}
		}

		if(docElevations.size() < 1) {
			constellioCache.remove(freeTextQuery);
		}

		server.reload();
	}


	public void setElevated(String freeTextQuery, Record record, boolean isExcluded) {

		if (StringUtils.isBlank(freeTextQuery)) {
			freeTextQuery = "*:*";
		}
		AtomicFileSystem solrFileSystem = server.getSolrFileSystem();

		DataWithVersion readData = solrFileSystem.readData(ELEVATE_FILE_NAME);
		ElevationsView anElevationsView = readData.getView(new ElevationsView());

		Elevations elevations = anElevationsView.getData();

		elevations.addOrUpdate(
				new Elevations.QueryElevation().setQuery(freeTextQuery)
						.addDocElevation(new Elevations.QueryElevation.DocElevation(record.getId(), isExcluded)));

		anElevationsView.setData(elevations);
		readData.setDataFromView(anElevationsView);

		solrFileSystem.writeData(ELEVATE_FILE_NAME, readData);
		solrFileSystem.close();

		Elevations.QueryElevation queryElevation = elevations.getQueryElevation(freeTextQuery);

		constellioCache.put(freeTextQuery,(ArrayList) queryElevation.getDocElevations());

		server.reload();
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
