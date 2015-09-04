/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
        Element propertiesElement = new Element(EmailConfigurationsManager.PROPERTIES);
        for(String key : emailServerConfiguration.getProperties().keySet()){
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
        if(collectionConfigElement == null){
            addEmailServerConfiguration(emailServerConfiguration, collection);
            return;
        }
        collectionConfigElement.getChild(EmailConfigurationsManager.USERNAME).setText(emailServerConfiguration.getUsername());
        collectionConfigElement.getChild(EmailConfigurationsManager.DEFAULT_SENDER_EMAIL).setText(emailServerConfiguration.getDefaultSenderEmail());
        collectionConfigElement.getChild(EmailConfigurationsManager.PASSWORD).setText(emailServerConfiguration.getPassword());
        Element propertiesElement = collectionConfigElement.getChild(EmailConfigurationsManager.PROPERTIES);
        propertiesElement.removeChildren(EmailConfigurationsManager.PROPERTY);

        for(String key : emailServerConfiguration.getProperties().keySet()){
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
