package com.constellio.model.services.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.events.ConfigUpdatedEventListener;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.services.collections.CollectionsListManagerRuntimeException.CollectionsListManagerRuntimeException_NoSuchCollection;

public class CollectionsListManager implements StatefulService, ConfigUpdatedEventListener {

	private static final String CONFIG_FILE_PATH = "/collections.xml";

	private final ConfigManager configManager;

	List<String> collections = new ArrayList<>();
	List<String> collectionsExcludingSystem = new ArrayList<>();

	List<CollectionsListManagerListener> listeners = new ArrayList<>();

	public CollectionsListManager(ConfigManager configManager) {
		this.configManager = configManager;
	}

	@Override
	public void initialize() {
		configManager.createXMLDocumentIfInexistent(CONFIG_FILE_PATH, newCollectionDocumentAlteration());
		configManager.registerListener(CONFIG_FILE_PATH, this);

		if (configManager.exist(CONFIG_FILE_PATH)) {
			// Config manager not mocked
			setCollections(readCollections());
		} else {
			setCollections(new ArrayList<String>());
		}

	}

	private void setCollections(List<String> collections) {
		this.collections = Collections.unmodifiableList(collections);
		this.collectionsExcludingSystem = new ArrayList<>(collections);
		this.collectionsExcludingSystem.remove(Collection.SYSTEM_COLLECTION);
		this.collectionsExcludingSystem = Collections.unmodifiableList(collectionsExcludingSystem);
	}

	public List<CollectionsListManagerListener> getListeners() {
		return listeners;
	}

	public void addCollection(String collection, List<String> languages) {
		configManager.updateXML(CONFIG_FILE_PATH, addCollectionDocumentAlteration(collection, languages));
	}

	public List<String> getCollections() {
		return collections;
	}

	public List<String> getCollectionsExcludingSystem() {
		return collectionsExcludingSystem;
	}

	public void registerCollectionsListener(CollectionsListManagerListener listener) {
		listeners.add(listener);
	}

	@Override
	public void onConfigUpdated(String configPath) {
		List<String> newCollectionList = readCollections();

		for (String elementOfNewCollectionList : newCollectionList) {
			if (!collections.contains(elementOfNewCollectionList)) {
				for (CollectionsListManagerListener listener : listeners) {
					listener.onCollectionCreated(elementOfNewCollectionList);
				}
			}
		}
		setCollections(newCollectionList);
	}

	public List<String> readCollections() {
		Document document = configManager.getXML(CONFIG_FILE_PATH).getDocument();
		List<String> collections = new ArrayList<>();
		for (Element collectionElement : document.getRootElement().getChildren()) {
			collections.add(collectionElement.getName());
		}
		return Collections.unmodifiableList(collections);
	}

	public void remove(String collection) {
		Document document = configManager.getXML(CONFIG_FILE_PATH).getDocument();
		for (Element collectionElement : document.getRootElement().getChildren()) {
			if (collection.equals(collectionElement.getName())) {
				configManager.updateXML(CONFIG_FILE_PATH, removeCollectionDocumentAlteration(collection));
			}
		}
	}

	private DocumentAlteration newCollectionDocumentAlteration() {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				document.addContent(new Element("collections"));
			}
		};
	}

	private DocumentAlteration addCollectionDocumentAlteration(final String collectionCode, final List<String> languages) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				Element collection = new Element(collectionCode);
				collection.setAttribute("languages", StringUtils.join(languages, ","));
				document.getRootElement().addContent(collection);
			}
		};
	}

	private DocumentAlteration removeCollectionDocumentAlteration(final String collection) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				document.getRootElement().removeChild(collection);
			}
		};
	}

	@Override
	public void close() {

	}

	public List<String> getCollectionLanguages(String collection) {
		Document document = configManager.getXML(CONFIG_FILE_PATH).getDocument();
		for (Element collectionElement : document.getRootElement().getChildren()) {
			if (collection.equals(collectionElement.getName())) {
				return Arrays.asList(collectionElement.getAttributeValue("languages").split(","));
			}
		}
		throw new CollectionsListManagerRuntimeException_NoSuchCollection(collection);
	}
}
