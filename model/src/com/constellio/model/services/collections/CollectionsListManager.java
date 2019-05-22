package com.constellio.model.services.collections;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.events.ConfigUpdatedEventListener;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.services.collections.CollectionsListManagerRuntimeException.CollectionsListManagerRuntimeException_NoSuchCollection;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class CollectionsListManager implements StatefulService, ConfigUpdatedEventListener {

	private static final String CONFIG_FILE_PATH = "/collections.xml";

	private final ConfigManager configManager;

	List<String> collections = new ArrayList<>();
	List<String> collectionsExcludingSystem = new ArrayList<>();

	//Language never change
	private Map<String, CollectionInfo> collectionInfoCache = new HashMap<>();

	List<CollectionsListManagerListener> listeners = new ArrayList<>();

	ModelLayerConfiguration modelLayerConfiguration;

	private short instanceId;

	private String[] collectionKeys = new String[256];

	public CollectionsListManager(ModelLayerFactory modelLayerFactory) {
		this.modelLayerConfiguration = modelLayerFactory.getConfiguration();
		this.instanceId = modelLayerFactory.getInstanceId();
		this.configManager = modelLayerFactory.getDataLayerFactory().getConfigManager();
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
		setCollections(readCollections());

		for (CollectionsListManagerListener listener : listeners) {
			listener.onCollectionDeleted(collection);
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

	private DocumentAlteration addCollectionDocumentAlteration(final String collectionCode,
															   final List<String> languages) {
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

		CollectionInfo collectionInfo = getCollectionInfo(collection);

		if (collectionInfo != null) {
			return collectionInfo.getCollectionLanguesCodes();
		}

		throw new CollectionsListManagerRuntimeException_NoSuchCollection(collection);
	}

	public byte getCollectionId(String collectionCode) {
		int firstNullPosition = -1;
		for (int i = 0; i < collectionKeys.length; i++) {
			String collectionKey = collectionKeys[i];
			if (collectionKey == null && firstNullPosition == -1) {
				firstNullPosition = i;
			}
			if (collectionCode.equals(collectionKey)) {
				return (byte) (i + Byte.MIN_VALUE);
			}
		}

		if (firstNullPosition == -1) {
			throw new ImpossibleRuntimeException("Collection limit reached");

		} else {
			collectionKeys[firstNullPosition] = collectionCode;
			return (byte) (firstNullPosition + Byte.MIN_VALUE);
		}

	}

	public String getCollectionCode(byte collectionId) {
		int collectionIndex = ((int) collectionId) - Byte.MIN_VALUE;
		return collectionKeys[collectionIndex];
	}

	public CollectionInfo getCollectionInfo(String collectionCode) {
		CollectionInfo cachedInfo = collectionInfoCache.get(collectionCode);

		if (cachedInfo == null) {
			String mainDataLanguage = modelLayerConfiguration.getMainDataLanguage();

			byte collectionId = getCollectionId(collectionCode);
			if (Collection.SYSTEM_COLLECTION.equals(collectionCode)) {

				cachedInfo = new CollectionInfo(collectionId, collectionCode, mainDataLanguage, asList(mainDataLanguage));

			} else {

				List<String> collectionLanguages = new ArrayList<>();
				Document document = configManager.getXML(CONFIG_FILE_PATH).getDocument();
				for (Element collectionElement : document.getRootElement().getChildren()) {
					if (collectionCode.equals(collectionElement.getName())) {
						collectionLanguages = Arrays.asList(collectionElement.getAttributeValue("languages").split(","));
					}
				}

				cachedInfo = new CollectionInfo(collectionId, collectionCode, mainDataLanguage, collectionLanguages);
			}

			synchronized (collectionInfoCache) {
				collectionInfoCache.put(collectionCode, cachedInfo);
			}
		}

		return cachedInfo;

	}

	public void registerPendingCollectionInfo(String code, String mainDataLanguage,
											  List<String> languages) {
		byte collectionId = getCollectionId(code);
		collectionInfoCache.put(code, new CollectionInfo(collectionId, code, mainDataLanguage, languages));
	}

	public String getMainDataLanguage() {
		return getCollectionInfo(Collection.SYSTEM_COLLECTION).getMainSystemLanguage().getCode();

	}

}
