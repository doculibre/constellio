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
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;

import java.util.*;

public class SynonymsConfigurationsManager extends AbstractOneXMLConfigPerCollectionManager<List<String>> {
	public static final String SYNONYME_FILE_PATH = "/synonyms.xml";

	public SynonymsConfigurationsManager(ConfigManager configManager, CollectionsListManager collectionsListManager, ConstellioCacheManager cacheManager) {
		super(configManager, collectionsListManager, cacheManager);
	}

	@Override
	public void close() {
	}

	@Override
	protected String getCollectionFolderRelativeConfigPath() {
		return SYNONYME_FILE_PATH;
	}

	@Override
	protected ConstellioCache getConstellioCache() {
		return cacheManager.getCache(SynonymsConfigurationsManager.class.getName());
	}

	@Override
	protected XMLConfigReader<List<String>> xmlConfigReader() {
		return new XMLConfigReader<List<String>>() {
			@Override
			public List<String> read(String collection, Document document) {
				return new SynonymsReader(document).load();
			}
		};
	}

	@Override
	protected DocumentAlteration createConfigAlteration() {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				SynonymsWriter writer = new SynonymsWriter(document);
				writer.initRootElement();
			}
		};
	}

	public void createCollectionSynonyms(String collection) {
		createCollection(collection);
	}

	public void setSynonyms(String collection, final List<String> synonyms) {
		DocumentAlteration documentAlteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				SynonymsWriter writer = new SynonymsWriter(document);
				writer.update(synonyms);
			}
		};

		updateCollection(collection, documentAlteration);
	}

	public List<String> getSynonyms(String collection){
		List<String> synonyms = getCollection(collection);
		if(synonyms != null) {
			return Collections.unmodifiableList(synonyms);
		}

		return Collections.unmodifiableList(new ArrayList<String>());
	}

	public String computeSynonyms(String collection, String query) {
		query = StringUtils.defaultString(query);

		if(StringUtils.contains(query, "\"")) {
			return query;
		}

		ArrayList<String> sQueries = new ArrayList<>(new HashSet<>(Arrays.asList(StringUtils.split(query))));
		List<String> synonyms = getSynonyms(collection);

		for(int i = 0; i < synonyms.size() && !synonyms.isEmpty(); i++) {
			String synonym = synonyms.get(i);
			ListIterator<String> listIterator = sQueries.listIterator();

			while(listIterator.hasNext()) {
				String sQuery = listIterator.next();

				if (StringUtils.containsIgnoreCase(synonym, sQuery)) {
					query = StringUtils.replaceAll(query, sQuery, synonym);
					listIterator.remove();
					break;
				}
			}
		}

		return query;
	}

	@Override
	public void onValueModified(String collection, List<String> synonyms) {
	}
}
