package com.constellio.model.services.search;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.search.entities.SearchBoost;
import com.constellio.model.utils.AbstractOneXMLConfigPerCollectionManager;
import com.constellio.model.utils.XMLConfigReader;
import org.jdom2.Document;

import java.util.ArrayList;
import java.util.List;

public class SearchBoostManager extends AbstractOneXMLConfigPerCollectionManager<List<SearchBoost>> {
	public static String SEARCH_BOOST_CONFIG = "/searchBoost.xml";

	public SearchBoostManager(ConfigManager configManager, CollectionsListManager collectionsListManager,
							  ConstellioCacheManager cacheManager) {
		super(configManager, collectionsListManager, cacheManager);
	}

	@Override
	protected String getCollectionFolderRelativeConfigPath() {
		return SEARCH_BOOST_CONFIG;
	}

	@Override
	protected ConstellioCache getConstellioCache() {
		return cacheManager.getCache(SearchBoostManager.class.getName());
	}

	@Override
	protected DocumentAlteration createConfigAlteration() {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				SearchBoostWriter writer = newSearchBoostWriter(document);
				writer.createEmptySearchBoost();
			}
		};
	}

	public void createCollectionSearchBoost(String collection) {
		createCollection(collection);
	}

	public void add(String collection, final SearchBoost searchBoost) {
		DocumentAlteration alteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				SearchBoostWriter writer = newSearchBoostWriter(document);
				writer.add(searchBoost);
			}
		};
		updateCollection(collection, alteration);
	}

	public void delete(String collection, final SearchBoost searchBoost) {
		DocumentAlteration alteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				SearchBoostWriter writer = newSearchBoostWriter(document);
				writer.delete(searchBoost.getType(), searchBoost.getKey());
			}
		};
		updateCollection(collection, alteration);
	}

	public List<SearchBoost> getAllSearchBoosts(String collection) {
		return getCollection(collection);
	}

	public List<SearchBoost> getAllSearchBoostsByMetadataType(String collection) {
		return getAllSearchBoostsByType(SearchBoost.METADATA_TYPE, collection);
	}

	public List<SearchBoost> getAllSearchBoostsByQueryType(String collection) {
		return getAllSearchBoostsByType(SearchBoost.QUERY_TYPE, collection);
	}

	private List<SearchBoost> getAllSearchBoostsByType(String type, String collection) {
		List<SearchBoost> searchBoostListByType = new ArrayList<>();
		List<SearchBoost> searchBoostList = getAllSearchBoosts(collection);
		for (SearchBoost searchBoost : searchBoostList) {
			if (searchBoost.getType().equals(type)) {
				searchBoostListByType.add(searchBoost);
			}
		}
		return searchBoostListByType;
	}

	private SearchBoostWriter newSearchBoostWriter(Document document) {
		return new SearchBoostWriter(document);
	}

	private SearchBoostReader newSearchBoostReader(Document document) {
		return new SearchBoostReader(document);
	}

	protected XMLConfigReader<List<SearchBoost>> xmlConfigReader() {
		return new XMLConfigReader<List<SearchBoost>>() {
			@Override
			public List<SearchBoost> read(String collection, Document document) {
				return newSearchBoostReader(document).getAll();
			}
		};
	}

	@Override
	public void close() {
	}

	@Override
	public void onValueModified(String collection, List<SearchBoost> newValue) {
	}
}
