package com.constellio.data.dao.managers.config;

import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.ConfigManagerRuntimeException.ConfigurationAlreadyExists;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.data.utils.ImpossibleRuntimeException;
import org.jdom2.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ConfigManagerHelper {

	ConfigManager configManager;
	Logger LOGGER = LoggerFactory.getLogger(ConfigManagerHelper.class);

	public ConfigManagerHelper(ConfigManager configManager) {
		this.configManager = configManager;
	}

	public void updateXML(String path, DocumentAlteration documentAlteration) {
		updateXML(path, documentAlteration, 0);
	}

	public void updateProperties(String path, PropertiesAlteration propertiesAlteration) {
		updateProperties(path, propertiesAlteration, 0);
	}

	public void createXMLDocumentIfInexistent(String path, DocumentAlteration documentAlteration) {
		if (!configManager.exist(path)) {
			Document document = new Document();
			if (documentAlteration != null) {
				documentAlteration.alter(document);
			}
			try {
				configManager.add(path, document);
			} catch (ConfigurationAlreadyExists e) {
				LOGGER.info("Configuration was created by another instance", e);
			}
		}
	}

	public void createPropertiesDocumentIfInexistent(String path, PropertiesAlteration propertiesAlteration) {
		if (!configManager.exist(path)) {
			Map<String, String> properties = new HashMap<>();
			propertiesAlteration.alter(properties);
			try {
				configManager.add(path, properties);
			} catch (ConfigurationAlreadyExists e) {
				LOGGER.info("Configuration was created by another instance", e);
			}
		}
	}

	public void updateXML(String path, DocumentAlteration documentAlteration, int attempt) {
		//Read/update extension called when calling getXML and update
		XMLConfiguration xmlConfiguration = configManager.getXML(path);

		Document doc = xmlConfiguration.getDocument();
		documentAlteration.alter(doc);

		try {
			configManager.update(path, xmlConfiguration.getHash(), doc);
		} catch (OptimisticLockingConfiguration e) {
			if (attempt > 2) {
				throw new ImpossibleRuntimeException(e);
			} else {
				updateXML(path, documentAlteration, attempt + 1);
			}
		}

	}

	public void updateProperties(String path, PropertiesAlteration propertiesAlteration, int attempt) {
		//Read/update extension called when calling getProperties and update
		PropertiesConfiguration propertiesConfiguration = configManager.getProperties(path);

		Map<String, String> doc = propertiesConfiguration.getProperties();
		propertiesAlteration.alter(doc);

		try {
			configManager.update(path, propertiesConfiguration.getHash(), doc);
		} catch (OptimisticLockingConfiguration e) {
			if (attempt > 2) {
				throw new ImpossibleRuntimeException(e);
			} else {
				updateProperties(path, propertiesAlteration, attempt + 1);
			}
		}

	}

}
