package com.constellio.data.dao.managers.config;

import com.constellio.data.dao.managers.config.events.ConfigEventListener;
import com.constellio.data.dao.managers.config.values.BinaryConfiguration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.dao.managers.config.values.TextConfiguration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import org.jdom2.Document;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface ConfigManager {

	PropertiesAlteration EMPTY_PROPERTY_ALTERATION = new PropertiesAlteration() {
		@Override
		public void alter(Map<String, String> properties) {
		}
	};

	BinaryConfiguration getBinary(String path);

	XMLConfiguration getXML(String path);

	TextConfiguration getText(String path);

	PropertiesConfiguration getProperties(String path);

	/**
	 * Performance warning : there is no cache hit if the value if null. Prefer using getXML/getProperties in sensitive classes
	 */
	boolean exist(String path);

	boolean folderExist(String path);

	List<String> list(String path);

	void createXMLDocumentIfInexistent(String path, DocumentAlteration documentAlteration);

	void createPropertiesDocumentIfInexistent(String path, PropertiesAlteration propertiesAlteration);

	void delete(String path);

	void deleteFolder(String path);

	void delete(String path, String hash)
			throws ConfigManagerException.OptimisticLockingConfiguration;

	void updateXML(String path, DocumentAlteration documentAlteration);

	void updateProperties(String path, PropertiesAlteration propertiesAlteration);

	void add(String path, InputStream newBinaryStream);

	void add(String path, Document newDocument);

	void add(String path, Map<String, String> newProperties);

	void update(String path, String hash, InputStream newBinaryStream)
			throws ConfigManagerException.OptimisticLockingConfiguration;

	void update(String path, String hash, Document newDocument)
			throws ConfigManagerException.OptimisticLockingConfiguration;

	void update(String path, String hash, Map<String, String> newProperties)
			throws ConfigManagerException.OptimisticLockingConfiguration;

	void registerListener(String path, ConfigEventListener listener);

	void registerTopPriorityListener(String path, ConfigEventListener listener);

	void deleteAllConfigsIn(String collection);

	void copySettingsFrom(File setting);

	void move(String src, String dest);

	void importFrom(File settingsFolder);

	void exportTo(File settingsFolder);

	void keepInCache(String path);

	void notifyChanged(String path);

}
