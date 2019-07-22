package com.constellio.model.services.collections;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.events.ConfigUpdatedEventListener;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.services.collections.CollectionsListManagerRuntimeException.CollectionsListManagerRuntimeException_NoSuchCollection;
import com.constellio.model.services.collections.exceptions.CollectionIdNotSetRuntimeException;
import com.constellio.model.services.collections.exceptions.NoMoreCollectionAvalibleException;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
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

		for (String collection : collections) {
			byte collectionId = getCollectionInfo(collection).getCollectionId();
			int collectionIndex = collectionId - Byte.MIN_VALUE;
			collectionKeys[collectionIndex] = collection;

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


	public void addCollection(String collection, List<String> languages, byte collectionId) {
		configManager.updateXML(CONFIG_FILE_PATH, addCollectionDocumentAlteration(collection, languages, collectionId));
		collectionInfoCache.remove(collection);
	}

	public void giveCollectionIdToCollectionThatDontHaveOne() throws NoMoreCollectionAvalibleException {
		for (String currentCollection : getCollections()) {
			Byte collectionId = getCollectionIdFromCollectionKeysArray(currentCollection);

			if (collectionId == null) {
				configManager.updateXML(CONFIG_FILE_PATH, addNewCollectionIdAlteration(currentCollection,
						getNextCollectionIdAndReserve(currentCollection)));
			}
		}
	}

	public Byte getCollectionIdFromCollectionKeysArray(String collectionCode) {
		for (int i = 0; i < collectionKeys.length; i++) {
			String currentCollectionCode = collectionKeys[i];
			if (Strings.isNotBlank(currentCollectionCode) && currentCollectionCode.equals(collectionCode)) {
				return (byte) (i + Byte.MIN_VALUE);
			}
		}

		return null;
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
		int collectionId = getCollectionId(collection);

		collectionKeys[collectionId - Byte.MIN_VALUE] = null;

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

	private DocumentAlteration addNewCollectionIdAlteration(final String collectionCode, byte collectionId) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {

				for (Element collectionElement : document.getRootElement().getChildren()) {
					if (collectionCode.equals(collectionElement.getName())) {
						collectionElement.setAttribute("byteId", collectionId + "");
					}
				}
			}
		};
	}

	private DocumentAlteration addCollectionDocumentAlteration(final String collectionCode,
															   final List<String> languages,
															   final byte collectionId) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				Element collection = new Element(collectionCode);
				collection.setAttribute("languages", StringUtils.join(languages, ","));
				collection.setAttribute("byteId", collectionId + "");
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

	public byte getNextCollectionIdAndReserve(String collectionCode) throws NoMoreCollectionAvalibleException {
		for (int i = 0; i < collectionKeys.length; i++) {
			String collectionKey = collectionKeys[i];
			if (collectionKey == null) {
				collectionKeys[i] = collectionCode;
				return (byte) (i + Byte.MIN_VALUE);
			}
		}

		throw new NoMoreCollectionAvalibleException();
	}

	public byte getCollectionId(String collectionCode) {
		for (int i = 0; i < collectionKeys.length; i++) {
			String collectionKey = collectionKeys[i];
			if (collectionCode.equals(collectionKey)) {
				return (byte) (i + Byte.MIN_VALUE);
			}
		}

		throw new CollectionIdNotSetRuntimeException(collectionCode);
	}

	public String getCollectionCode(byte collectionId) {
		int collectionIndex = ((int) collectionId) - Byte.MIN_VALUE;
		return collectionKeys[collectionIndex];
	}

	public CollectionInfo getCollectionInfo(String collectionCode) {
		CollectionInfo cachedInfo = collectionInfoCache.get(collectionCode);
		byte byteId;

		if (cachedInfo == null) {
			String mainDataLanguage = modelLayerConfiguration.getMainDataLanguage();

			Element collectionElement = getElementFromFile(collectionCode);

			if (collectionElement == null) {
				throw new CollectionsListManagerRuntimeException_NoSuchCollection(collectionCode);
			}

			String idAsString = collectionElement.getAttributeValue("byteId");
			if (Strings.isNotBlank(idAsString)) {
				byteId = Byte.parseByte(idAsString);

			} else {
				try {
					byteId = getNextCollectionIdAndReserve(collectionCode);
				} catch (NoMoreCollectionAvalibleException e) {
					throw new RuntimeException(e);
				}
			}

			if (Collection.SYSTEM_COLLECTION.equals(collectionCode)) {
				cachedInfo = new CollectionInfo(byteId, collectionCode, mainDataLanguage, asList(mainDataLanguage));
			} else {
				List<String> collectionLanguages = Arrays.asList(collectionElement.getAttributeValue("languages").split(","));

				cachedInfo = new CollectionInfo(byteId, collectionCode, mainDataLanguage, collectionLanguages);
			}

			synchronized (collectionInfoCache) {
				collectionInfoCache.put(collectionCode, cachedInfo);
			}
		}

		return cachedInfo;
	}

	public Element getElementFromFile(String collectionCode) {
		Document document = configManager.getXML(CONFIG_FILE_PATH).getDocument();

		for (Element collectionElement : document.getRootElement().getChildren()) {
			if (collectionCode.equals(collectionElement.getName())) {
				return collectionElement;
			}
		}

		return null;
	}

	public byte registerPendingCollectionInfo(String code, String mainDataLanguage,
											  List<String> languages) throws NoMoreCollectionAvalibleException {
		byte collectionId;
		try {
			collectionId = getCollectionId(code);
		} catch (CollectionIdNotSetRuntimeException e) {
			collectionId = getNextCollectionIdAndReserve(code);
		}
		collectionInfoCache.put(code, new CollectionInfo(collectionId, code, mainDataLanguage, languages));
		return collectionId;
	}

	public String getMainDataLanguage() {
		return getCollectionInfo(Collection.SYSTEM_COLLECTION).getMainSystemLanguage().getCode();

	}

}
