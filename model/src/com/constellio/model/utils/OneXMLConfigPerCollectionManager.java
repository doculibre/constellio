package com.constellio.model.utils;

import java.util.HashMap;
import java.util.Map;

import org.jdom2.Document;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.events.ConfigUpdatedEventListener;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.collections.CollectionsListManagerListener;

public class OneXMLConfigPerCollectionManager<T> implements ConfigUpdatedEventListener, CollectionsListManagerListener {

	private final String collectionFolderRelativeConfigPath;

	private final XMLConfigReader<T> configReader;

	private final ConfigManager configManager;

	private final CollectionsListManager collectionsListManager;

	private final Map<String, T> cache = new HashMap<>();

	private final OneXMLConfigPerCollectionManagerListener<T> listener;

	private final DocumentAlteration newDocumentAlteration;

	public OneXMLConfigPerCollectionManager(
			ConfigManager configManager, CollectionsListManager collectionsListManager, String collectionFolderRelativeConfigPath,
			XMLConfigReader<T> configReader, OneXMLConfigPerCollectionManagerListener<T> listener) {
		this(configManager, collectionsListManager, collectionFolderRelativeConfigPath, configReader, listener, null);
	}

	public OneXMLConfigPerCollectionManager(
			ConfigManager configManager, CollectionsListManager collectionsListManager, String collectionFolderRelativeConfigPath,
			XMLConfigReader<T> configReader, OneXMLConfigPerCollectionManagerListener<T> listener,
			DocumentAlteration newDocumentAlteration) {

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

	private void registerCollectionConfigAndLoad(String collection) {
		String configPath = getConfigPath(collection);
		configManager.registerListener(configPath, this);
		load(collection, configPath);
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
		load(collection, configPath);
	}

	public void update(String collection, String hash, Document document)
			throws OptimisticLockingConfiguration {
		String configPath = getConfigPath(collection);
		configManager.update(configPath, hash, document);
		//load(collection, configPath);
	}

	public T get(String collection) {
		return cache.get(collection);
	}

	public void reload(String collection) {
		cache.remove(collection);
		String configPath = getConfigPath(collection);
		load(collection, configPath);
	}

	void load(String collection, String configPath) {
		XMLConfiguration config = configManager.getXML(configPath);
		if (config == null) {
			configManager.createXMLDocumentIfInexistent(configPath, newDocumentAlteration);
			config = configManager.getXML(configPath);
		}

		T value = parse(collection, config);
		cache.put(collection, value);
	}

	protected T parse(String collection, XMLConfiguration xmlConfiguration) {
		Document document = xmlConfiguration.getDocument();
		return configReader.read(collection, document);
	}

	@Override
	public void onConfigUpdated(String configPath) {
		String collection = getCollectionCode(configPath);
		load(collection, configPath);
		listener.onValueModified(collection, get(collection));
	}

	@Override
	public void onCollectionCreated(final String collection) {
		String configPath = getConfigPath(collection);

		registerCollectionConfigAndLoad(collection);
	}

	public void createCollectionFile(final String collection, DocumentAlteration documentAlteration) {
		String configPath = getConfigPath(collection);
		configManager.createXMLDocumentIfInexistent(configPath, documentAlteration);
		load(collection, configPath);
	}

}
