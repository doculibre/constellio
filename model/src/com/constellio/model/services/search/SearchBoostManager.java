package com.constellio.model.services.search;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.search.entities.SearchBoost;
import com.constellio.model.utils.OneXMLConfigPerCollectionManager;
import com.constellio.model.utils.OneXMLConfigPerCollectionManagerListener;
import com.constellio.model.utils.XMLConfigReader;

public class SearchBoostManager implements StatefulService, OneXMLConfigPerCollectionManagerListener<List<SearchBoost>> {
	private String SEARCH_BOOST_CONFIG = "/searchBoost.xml";
	private OneXMLConfigPerCollectionManager<List<SearchBoost>> oneXMLConfigPerCollectionManager;
	private ConfigManager configManager;
	private CollectionsListManager collectionsListManager;

	public SearchBoostManager(ConfigManager configManager, CollectionsListManager collectionsListManager) {
		this.configManager = configManager;
		this.collectionsListManager = collectionsListManager;
	}

	@Override
	public void initialize() {
		DocumentAlteration createConfigAlteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				SearchBoostWriter writer = newSearchBoostWriter(document);
				writer.createEmptySearchBoost();
			}
		};
		this.oneXMLConfigPerCollectionManager = new OneXMLConfigPerCollectionManager<>(configManager, collectionsListManager,
				SEARCH_BOOST_CONFIG, xmlConfigReader(), this, createConfigAlteration);
	}

	public void createCollectionSearchBoost(String collection) {
		DocumentAlteration createConfigAlteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				SearchBoostWriter writer = newSearchBoostWriter(document);
				writer.createEmptySearchBoost();
			}
		};
		oneXMLConfigPerCollectionManager.createCollectionFile(collection, createConfigAlteration);
	}

	public void add(String collection, final SearchBoost searchBoost) {
		DocumentAlteration alteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				SearchBoostWriter writer = newSearchBoostWriter(document);
				writer.add(searchBoost);
			}
		};
		oneXMLConfigPerCollectionManager.updateXML(collection, alteration);
	}

	public void delete(String collection, final SearchBoost searchBoost) {
		DocumentAlteration alteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				SearchBoostWriter writer = newSearchBoostWriter(document);
				writer.delete(searchBoost.getType(), searchBoost.getKey());
			}
		};
		oneXMLConfigPerCollectionManager.updateXML(collection, alteration);
	}

	public List<SearchBoost> getAllSearchBoosts(String collection) {
		return oneXMLConfigPerCollectionManager.get(collection);
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

	private XMLConfigReader<List<SearchBoost>> xmlConfigReader() {
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
