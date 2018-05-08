package com.constellio.model.services.search;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.data.TextView;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.search.Elevations.QueryElevation;
import com.constellio.model.services.search.Elevations.QueryElevation.DocElevation;
import com.constellio.model.utils.AbstractOneXMLConfigPerCollectionManager;
import com.constellio.model.utils.XMLConfigReader;
import org.jdom2.Document;

import java.util.*;

public class SearchConfigurationsManager extends AbstractOneXMLConfigPerCollectionManager<Elevations> {
	public static final String ELEVATE_FILE_NAME = "/elevate.xml";
	public static final String SYNONYME_FILE_PATH = "/synonyms.txt";

	List<String> synonyms = new ArrayList<>();

	DataLayerFactory dataLayerFactory;

	public SearchConfigurationsManager(ConfigManager configManager, CollectionsListManager collectionsListManager, ConstellioCacheManager cacheManager, DataLayerFactory dataLayerFacotry) {
		super(configManager, collectionsListManager, cacheManager);

		this.dataLayerFactory = dataLayerFacotry;

		initializeLocal();
	}

	public void initializeLocal() {
		synonyms = getSynonymsOnServer();
	}

	@Override
	public void close() {

	}

	@Override
	protected String getCollectionFolderRelativeConfigPath() {
		return ELEVATE_FILE_NAME;
	}

	@Override
	protected ConstellioCache getConstellioCache() {
		return cacheManager.getCache(SearchConfigurationsManager.class.getName());
	}

	@Override
	protected XMLConfigReader<Elevations> xmlConfigReader() {
		return new XMLConfigReader<Elevations>() {
			@Override
			public Elevations read(String collection, Document document) {
				return new ElevationsReader(document).load();
			}
		};
	}

	@Override
	protected DocumentAlteration createConfigAlteration() {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				ElevationsWriter writer = new ElevationsWriter(document);
				writer.initRootElement();
			}
		};
	}

	public void createCollectionElevations(String collection) {
		createCollection(collection);
	}

	public void updateCollectionElevations(String collection, final Elevations elevations) {
		DocumentAlteration documentAlteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				ElevationsWriter writer = new ElevationsWriter(document);
				writer.update(elevations);
			}
		};

		updateCollection(collection, documentAlteration);
	}

	public List<DocElevation> getDocElevations(String collection, String query){
		Elevations elevations = getCollection(collection);
		if(elevations != null) {
			List<QueryElevation> queryElevations = elevations.getQueryElevations();
			for (QueryElevation queryElevation:queryElevations) {
				if(Objects.equals(queryElevation.getQuery(), query)) {
					return queryElevation.getDocElevations();
				}
			}
		}

		return new ArrayList<>();
	}

	public List<String> getDocExlusions(String collection) {
		Elevations elevations = getCollection(collection);
		if(elevations != null) {
			return elevations.getDocExclusions();
		}

		return new ArrayList<>();
	}

	public List<String> getAllQuery(String collection) {
		List<String> allQuery = new ArrayList<>();
		Elevations elevations = getCollection(collection);
		if(elevations != null) {
			List<QueryElevation> queryElevations = elevations.getQueryElevations();
			for (QueryElevation queryElevation:queryElevations) {
				allQuery.add(queryElevation.getQuery());
			}
		}
		return allQuery;
	}

	public boolean isElevated(String collection, String freeTextQuery, Record record) {
		return isElevated(collection, freeTextQuery, record.getId());
	}

	public boolean isElevated(String collection, String freeTextQuery, String recordId) {
		boolean found = false;

		List<DocElevation> docElevations = getDocElevations(collection, freeTextQuery);

		if(docElevations != null) {
			for (DocElevation docElevation : docElevations) {
				if (recordId.equals(docElevation.getId())) {
					found = true;
					break;
				}
			}
		}

		return found;
	}

	public boolean isExcluded(String collection, Record record) {
		Elevations elevations = getCollection(collection);
		return elevations.getDocExclusions().contains(record.getId());
	}

	public void removeAllElevation(String collection) {
		Elevations elevations = getCollection(collection);
		if(elevations != null) {
			elevations.removeAllElevation();
			updateCollectionElevations(collection, elevations);
		}
	}

	public void removeExclusion(String collection, String id) {
		Elevations elevations = getCollection(collection);
		if(elevations != null) {
			elevations.removeDocExclusion(id);
			updateCollectionElevations(collection, elevations);
		}
	}

	public void removeAllExclusion(String collection) {
		Elevations elevations = getCollection(collection);
		if(elevations != null) {
			elevations.removeAllDocExclusion();
			updateCollectionElevations(collection, elevations);
		}
	}

	public void removeQueryElevation(String collection, String query) {
		Elevations elevations = getCollection(collection);
		if(elevations != null && elevations.removeQueryElevation(query)) {
			updateCollectionElevations(collection, elevations);
		}
	}

	public void removeElevated(String collection, String freeTextQuery, String recordId) {
		Elevations elevations = getCollection(collection);
		if(elevations != null && elevations.removeDocElevation(freeTextQuery, recordId)) {
			updateCollectionElevations(collection, elevations);
		}
	}

	public void setElevated(String collection, String freeTextQuery, Record record) {
		setElevated(collection, freeTextQuery, record.getId());
	}

	public void setElevated(String collection, String freeTextQuery, String recordId) {
		Elevations elevations = getCollection(collection);
		if(elevations != null) {
			elevations.addOrUpdate(
					new QueryElevation(freeTextQuery)
							.addDocElevation(new DocElevation(recordId, freeTextQuery)));

			updateCollectionElevations(collection, elevations);
		}
	}

	public void setExcluded(String collection, Record record) {
		setExcluded(collection, record.getId());
	}

	public void setExcluded(String collection, String recordId) {
		Elevations elevations = getCollection(collection);
		if(elevations != null && elevations.addDocExclusion(recordId)) {
			updateCollectionElevations(collection, elevations);
		}
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

		if(solrFileSystem != null) {
			DataWithVersion readData = solrFileSystem.readData(SYNONYME_FILE_PATH);
			TextView aStringView = readData.getView(new TextView());
			aStringView.setData(getSynonymeAsOneString());
			readData.setDataFromView(aStringView);
			solrFileSystem.writeData(SYNONYME_FILE_PATH, readData);
			server.reload();
		}
	}

	private List<String> getSynonymsOnServer() {
		BigVaultServer server = dataLayerFactory.getRecordsVaultServer();
		AtomicFileSystem solrFileSystem = server.getSolrFileSystem();

		if(solrFileSystem != null) {
			DataWithVersion readData = solrFileSystem.readData(SYNONYME_FILE_PATH);

			TextView aStringView = readData.getView(new TextView());

			String synonymsAsOneString = aStringView.getData();

			return Arrays.asList(synonymsAsOneString.split("\\r\\n|\\n|\\r"));
		} else  {
			return new ArrayList<>();
		}
	}


	private String getSynonymeAsOneString() {
		StringBuilder allSynonyms = new StringBuilder();

		for(String synonym : synonyms) {
			allSynonyms.append(synonym + "\n");
		}

		return allSynonyms.toString();
	}

	@Override
	public void onValueModified(String collection, Elevations newValue) {

	}
}
