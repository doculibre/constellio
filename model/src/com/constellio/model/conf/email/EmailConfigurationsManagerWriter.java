package com.constellio.model.conf.email;

import org.jdom2.Document;
import org.jdom2.Element;

public class EmailConfigurationsManagerWriter {
	private Document document;

	public EmailConfigurationsManagerWriter(Document document) {
		this.document = document;
	}

	public void addEmailServerConfiguration(EmailServerConfiguration emailServerConfiguration, String collection) {
		Element rootElement = document.getRootElement();
		Element newConfig = new Element(collection);
		Element usernameElement = new Element(EmailConfigurationsManager.USERNAME);
		usernameElement.setText(emailServerConfiguration.getUsername());
		newConfig.addContent(usernameElement);
		Element defaultSenderEmailElement = new Element(EmailConfigurationsManager.DEFAULT_SENDER_EMAIL);
		defaultSenderEmailElement.setText(emailServerConfiguration.getDefaultSenderEmail());
		newConfig.addContent(defaultSenderEmailElement);
		Element passwordElement = new Element(EmailConfigurationsManager.PASSWORD);
		passwordElement.setText(emailServerConfiguration.getPassword());
		newConfig.addContent(passwordElement);
		Element enabledElement = new Element(EmailConfigurationsManager.ENABLED);
		enabledElement.setText(emailServerConfiguration.isEnabled() ? "true" : "false");
		newConfig.addContent(enabledElement);
		Element propertiesElement = new Element(EmailConfigurationsManager.PROPERTIES);
		for (String key : emailServerConfiguration.getProperties().keySet()) {
			String value = emailServerConfiguration.getProperties().get(key);
			Element property = new Element(EmailConfigurationsManager.PROPERTY);
			property.setAttribute(EmailConfigurationsManager.KEY, key);
			property.setAttribute(EmailConfigurationsManager.VALUE, value);
			propertiesElement.addContent(property);
		}
		newConfig.addContent(propertiesElement);
		rootElement.addContent(newConfig);
	}

	public void deleteEmailServerConfiguration(String collection) {
		Element rootElement = document.getRootElement();
		rootElement.removeChild(collection);
	}

	public void updateEmailServerConfiguration(EmailServerConfiguration emailServerConfiguration, String collection) {
		Element rootElement = document.getRootElement();
		Element collectionConfigElement = rootElement.getChild(collection);
		if (collectionConfigElement == null) {
			addEmailServerConfiguration(emailServerConfiguration, collection);
			return;
		}
		collectionConfigElement.getChild(EmailConfigurationsManager.USERNAME).setText(emailServerConfiguration.getUsername());
		collectionConfigElement.getChild(EmailConfigurationsManager.DEFAULT_SENDER_EMAIL)
				.setText(emailServerConfiguration.getDefaultSenderEmail());
		collectionConfigElement.getChild(EmailConfigurationsManager.PASSWORD).setText(emailServerConfiguration.getPassword());

		Element enabledElement = collectionConfigElement.getChild(EmailConfigurationsManager.ENABLED);
		if (enabledElement == null) {
			enabledElement = new Element(EmailConfigurationsManager.ENABLED);
			collectionConfigElement.addContent(enabledElement);
		}
		enabledElement.setText(emailServerConfiguration.isEnabled() ? "true" : "false");

		Element propertiesElement = collectionConfigElement.getChild(EmailConfigurationsManager.PROPERTIES);
		propertiesElement.removeChildren(EmailConfigurationsManager.PROPERTY);

		for (String key : emailServerConfiguration.getProperties().keySet()) {
			String value = emailServerConfiguration.getProperties().get(key);
			Element property = new Element(EmailConfigurationsManager.PROPERTY);
			property.setAttribute(EmailConfigurationsManager.KEY, key);
			property.setAttribute(EmailConfigurationsManager.VALUE, value);
			propertiesElement.addContent(property);
		}
	}

	public void createEmptyDocument() {
		Element rootElement = new Element("collectionsEmailServerConfigs");
		document.setRootElement(rootElement);
	}
}
