package com.constellio.model.conf.email;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;

public class EmailConfigurationManagerReader {
	private static final Logger LOGGER = LogManager.getLogger(EmailConfigurationManagerReader.class);
	Document document;

	public EmailConfigurationManagerReader(Document document) {
		this.document = document;
	}

	public EmailServerConfiguration readEmailServerConfiguration(String collection) {
		Element rootElement = document.getRootElement();
		Element serverConfigElement = rootElement.getChild(collection);
		if (serverConfigElement == null) {
			return null;
		}
		return buildConfiguration(serverConfigElement);
	}

	private EmailServerConfiguration buildConfiguration(Element serverConfigElement) {
		String username = serverConfigElement.getChild(EmailConfigurationsManager.USERNAME).getText();
		String password = serverConfigElement.getChild(EmailConfigurationsManager.PASSWORD).getText();
		String defaultSenderEmail = serverConfigElement.getChild(EmailConfigurationsManager.DEFAULT_SENDER_EMAIL).getText();
		Element enabledElement = serverConfigElement.getChild(EmailConfigurationsManager.ENABLED);
		boolean enabled = enabledElement == null || !"false".equals(enabledElement.getText());

		Map<String, String> propertiesMap = new HashMap<>();
		Element properties = serverConfigElement.getChild(EmailConfigurationsManager.PROPERTIES);
		for (Element property : properties.getChildren()) {
			String key = property.getAttributeValue(EmailConfigurationsManager.KEY);
			String value = property.getAttributeValue(EmailConfigurationsManager.VALUE);
			propertiesMap.put(key, value);
		}
		EmailServerConfiguration config;
		try {
			config = new EmailServerConfigurationFactory()
					.getServerConfiguration(username, defaultSenderEmail, password, propertiesMap, enabled);
		} catch (Throwable e) {
			config = new BaseEmailServerConfiguration(username, password, defaultSenderEmail, propertiesMap, false);
			LOGGER.error("Email server disabled because of :", e);
		}
		return config;
	}

}
