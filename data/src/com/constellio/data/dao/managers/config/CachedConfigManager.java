package com.constellio.data.dao.managers.config;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;

import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.events.ConfigEventListener;
import com.constellio.data.dao.managers.config.values.BinaryConfiguration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.dao.managers.config.values.TextConfiguration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.data.dao.services.cache.ConstellioCache;

public class CachedConfigManager implements ConfigManager {

	Set<String> cachedPaths = new HashSet<>();
	ConstellioCache constellioCache;
	ConfigManager configManager;
	ConfigManagerHelper configManagerHelper;

	public CachedConfigManager(ConfigManager configManager, ConstellioCache constellioCache) {
		this.constellioCache = constellioCache;
		this.configManager = configManager;
		this.configManagerHelper = new ConfigManagerHelper(this);
	}

	@Override
	public BinaryConfiguration getBinary(String path) {
		return configManager.getBinary(path);
	}

	@Override
	public XMLConfiguration getXML(String path) {
		XMLConfiguration xmlConfiguration = getFromCache(path);

		if (xmlConfiguration == null) {
			xmlConfiguration = configManager.getXML(path);
			constellioCache.put(path, xmlConfiguration);
		}

		return xmlConfiguration;
	}

	@Override
	public TextConfiguration getText(String path) {
		TextConfiguration textConfiguration = getFromCache(path);

		if (textConfiguration == null) {
			textConfiguration = configManager.getText(path);
			constellioCache.put(path, textConfiguration);
		}

		return textConfiguration;
	}

	@Override
	public PropertiesConfiguration getProperties(String path) {
		PropertiesConfiguration propertiesConfiguration = getFromCache(path);

		if (propertiesConfiguration == null) {
			propertiesConfiguration = configManager.getProperties(path);
			constellioCache.put(path, propertiesConfiguration);
		}

		return propertiesConfiguration;
	}

	@Override
	public boolean exist(String path) {
		return getFromCache(path) != null || configManager.exist(path);
	}

	@Override
	public boolean folderExist(String path) {
		return configManager.folderExist(path);
	}

	@Override
	public List<String> list(String path) {
		return configManager.list(path);
	}

	@Override
	public void delete(String path) {
		configManager.delete(path);
		constellioCache.remove(path);
	}

	@Override
	public void deleteFolder(String path) {
		configManager.deleteFolder(path);
		constellioCache.remove(path);
	}

	@Override
	public void delete(String path, String hash)
			throws OptimisticLockingConfiguration {
		configManager.delete(path, hash);
		constellioCache.remove(path);
	}

	@Override
	public void add(String path, InputStream newBinaryStream) {
		configManager.add(path, newBinaryStream);
	}

	@Override
	public void add(String path, Document newDocument) {
		configManager.add(path, newDocument);
		constellioCache.remove(path);
	}

	@Override
	public void add(String path, Map<String, String> newProperties) {
		configManager.add(path, newProperties);
		constellioCache.remove(path);
	}

	@Override
	public void update(String path, String hash, InputStream newBinaryStream)
			throws OptimisticLockingConfiguration {
		configManager.update(path, hash, newBinaryStream);
	}

	@Override
	public void update(String path, String hash, Document newDocument)
			throws OptimisticLockingConfiguration {
		configManager.update(path, hash, newDocument);
		removeFromCache(path);
	}

	@Override
	public void update(String path, String hash, Map<String, String> newProperties)
			throws OptimisticLockingConfiguration {
		configManager.update(path, hash, newProperties);
		constellioCache.remove(path);
	}

	@Override
	public void registerListener(String path, ConfigEventListener listener) {
		configManager.registerListener(path, listener);
		constellioCache.clear();
	}

	@Override
	public void deleteAllConfigsIn(String collection) {
		configManager.deleteAllConfigsIn(collection);
		constellioCache.clear();
	}

	@Override
	public void copySettingsFrom(File setting) {
		configManager.copySettingsFrom(setting);
		constellioCache.clear();
	}

	@Override
	public void move(String src, String dest) {
		configManager.move(src, dest);
		constellioCache.remove(src);
		constellioCache.remove(dest);
	}

	@Override
	public void importFrom(File settingsFolder) {
		configManager.importFrom(settingsFolder);
		constellioCache.clear();
	}

	@Override
	public void exportTo(File settingsFolder) {
		configManager.exportTo(settingsFolder);
		constellioCache.clear();
	}

	@Override
	public void keepInCache(String path) {
		cachedPaths.add(path);
	}

	private <T> T getFromCache(String path) {
		if (cachedPaths.contains(path)) {
			return constellioCache.get(path);
		} else {
			return null;
		}
	}

	private void removeFromCache(String path) {
		if (cachedPaths.contains(path)) {
			constellioCache.remove(path);
		}
	}

	@Override
	public synchronized void updateXML(String path, DocumentAlteration documentAlteration) {
		configManagerHelper.updateXML(path, documentAlteration);
	}

	@Override
	public synchronized void updateProperties(String path, PropertiesAlteration propertiesAlteration) {
		configManagerHelper.updateProperties(path, propertiesAlteration);
	}

	@Override
	public void createXMLDocumentIfInexistent(String path, DocumentAlteration documentAlteration) {
		configManagerHelper.createXMLDocumentIfInexistent(path, documentAlteration);
	}

	@Override
	public void createPropertiesDocumentIfInexistent(String path, PropertiesAlteration propertiesAlteration) {
		configManagerHelper.createPropertiesDocumentIfInexistent(path, propertiesAlteration);
	}
}
