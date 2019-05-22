package com.constellio.model.utils;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.events.ConfigUpdatedEventListener;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.data.dao.services.cache.AutoReloadingConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheOptions;
import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.collections.CollectionsListManagerListener;
import com.constellio.model.services.schemas.MetadataSchemasManagerRuntimeException;
import org.jdom2.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_MODIFIED;
import static com.constellio.data.dao.services.cache.InsertionReason.WAS_OBTAINED;

public class OneXMLConfigPerCollectionManager<T> implements ConfigUpdatedEventListener, CollectionsListManagerListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(OneXMLConfigPerCollectionManager.class);

	private final String collectionFolderRelativeConfigPath;

	private final XMLConfigReader<T> configReader;

	private final ConfigManager configManager;

	private final CollectionsListManager collectionsListManager;

	//	private final Map<String, T> cache = new HashMap<>();

	private final ConstellioCache cache;

	private final OneXMLConfigPerCollectionManagerListener<T> listener;

	private final DocumentAlteration newDocumentAlteration;

	public OneXMLConfigPerCollectionManager(
			ConfigManager configManager, CollectionsListManager collectionsListManager,
			String collectionFolderRelativeConfigPath,
			XMLConfigReader<T> configReader, OneXMLConfigPerCollectionManagerListener<T> listener,
			ConstellioCache cache) {
		this(configManager, collectionsListManager, collectionFolderRelativeConfigPath, configReader, listener, null, cache);
	}

	public OneXMLConfigPerCollectionManager(
			final ConfigManager configManager, CollectionsListManager collectionsListManager,
			String collectionFolderRelativeConfigPath,
			XMLConfigReader<T> configReader, OneXMLConfigPerCollectionManagerListener<T> listener,
			final DocumentAlteration newDocumentAlteration, ConstellioCache cache) {

		cache.setOptions(new ConstellioCacheOptions().setInvalidateRemotelyWhenPutting(true));
		this.cache = new AutoReloadingConstellioCache(cache) {
			@Override
			protected <T extends Serializable> T reload(String collection) {
				String configPath = getConfigPath(collection);
				XMLConfiguration config = configManager.getXML(configPath);
				if (config == null) {
					return null;
				}

				return (T) parse(collection, config);

			}
		};
		this.newDocumentAlteration = newDocumentAlteration;
		this.configReader = configReader;
		this.configManager = configManager;
		this.collectionsListManager = collectionsListManager;
		this.listener = listener;
		if (!collectionFolderRelativeConfigPath.startsWith("/")) {
			this.collectionFolderRelativeConfigPath = "/" + collectionFolderRelativeConfigPath;
		} else {
			this.collectionFolderRelativeConfigPath = collectionFolderRelativeConfigPath;
		}

		collectionsListManager.registerCollectionsListener(this);
		for (String collection : collectionsListManager.getCollections()) {
			registerCollectionConfigAndLoad(collection);

		}
	}


	Object getFromCache(String key) {
		return cache.get(key);
	}

	void putInCache(String key, Object value, InsertionReason insertionReason) {
		cache.put(key, (Serializable) value, insertionReason);
	}

	void removeFromCache(String key) {
		cache.remove(key);
	}

	void clearCache() {
		cache.clear();
	}

	private void registerCollectionConfigAndLoad(String collection) {
		String configPath = getConfigPath(collection);
		configManager.registerListener(configPath, this);
		//TODO Francis : Retiré le 7 aout 2018 pour faire passer les tests de OneXMLConfigPerCollectionManagerAcceptanceTest
		try {
			load(collection, configPath, WAS_OBTAINED);
		} catch (MetadataSchemasManagerRuntimeException.MetadataSchemasManagerRuntimeException_NoSuchCollection e) {
			LOGGER.debug("Cannot load in cache yet", e);
		}
	}

	public String getConfigPath(String collectionCode) {
		return "/" + collectionCode + collectionFolderRelativeConfigPath;
	}

	public String getCollectionCode(String configPath) {
		return configPath.split("/")[1];
	}

	public void updateXML(String collection, DocumentAlteration documentAlteration) {
		String configPath = getConfigPath(collection);
		configManager.updateXML(configPath, documentAlteration);
	}

	public void update(String collection, String hash, Document document)
			throws OptimisticLockingConfiguration {
		String configPath = getConfigPath(collection);
		configManager.update(configPath, hash, document);
	}

	@SuppressWarnings("unchecked")
	public T get(String collection) {
		return (T) getFromCache(collection);
	}

	public void reload(String collection, InsertionReason insertionReason) {
		removeFromCache(collection);
		String configPath = getConfigPath(collection);
		load(collection, configPath, insertionReason);
	}

	T load(String collection, String configPath, InsertionReason insertionReason) {
		XMLConfiguration config = configManager.getXML(configPath);
		if (config == null && newDocumentAlteration != null) {
			configManager.createXMLDocumentIfInexistent(configPath, newDocumentAlteration);
			config = configManager.getXML(configPath);
		}

		if (config != null) {
			T value = parse(collection, config);
			putInCache(collection, value, insertionReason);
			return value;
		} else {
			return null;
		}
	}

	protected T parse(String collection, XMLConfiguration xmlConfiguration) {
		Document document = xmlConfiguration.getDocument();
		return configReader.read(collection, document);
	}

	@Override
	public void onConfigUpdated(String configPath) {
		String collection = getCollectionCode(configPath);
		load(collection, configPath, WAS_MODIFIED);
		listener.onValueModified(collection, get(collection));
	}

	@Override
	public void onCollectionCreated(final String collection) {
		String configPath = getConfigPath(collection);

		registerCollectionConfigAndLoad(collection);
	}

	@Override
	public void onCollectionDeleted(String collection) {
		removeFromCache(collection);
	}

	public void createCollectionFile(final String collection, DocumentAlteration documentAlteration) {
		String configPath = getConfigPath(collection);
		configManager.createXMLDocumentIfInexistent(configPath, documentAlteration);
		load(collection, configPath, WAS_MODIFIED);
	}

}
