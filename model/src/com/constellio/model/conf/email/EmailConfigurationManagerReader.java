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

import com.constellio.model.entities.records.wrappers.User;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmailConfigurationManagerReader {
    Document document;
    public EmailConfigurationManagerReader(Document document) {
        this.document = document;
    }

    public EmailServerConfiguration readEmailServerConfiguration(String collection) {
        Element rootElement = document.getRootElement();
        Element serverConfigElement = rootElement.getChild(collection);
        if(serverConfigElement == null){
            return null;
        }
        return buildConfiguration(serverConfigElement);
    }

    private EmailServerConfiguration buildConfiguration(Element serverConfigElement) {
        String username = serverConfigElement.getChild(EmailConfigurationsManager.USERNAME).getText();
        String password = serverConfigElement.getChild(EmailConfigurationsManager.PASSWORD).getText();
        String defaultSenderEmail = serverConfigElement.getChild(EmailConfigurationsManager.DEFAULT_SENDER_EMAIL).getText();
        Map<String, String> propertiesMap = new HashMap<>();
        Element properties = serverConfigElement.getChild(EmailConfigurationsManager.PROPERTIES);
        for(Element property : properties.getChildren()){
            String key = property.getAttributeValue(EmailConfigurationsManager.KEY);
            String value = property.getAttributeValue(EmailConfigurationsManager.VALUE);
            propertiesMap.put(key, value);
        }
        return new EmailServerConfigurationFactory().getServerConfiguration(username, defaultSenderEmail, password, propertiesMap);
    }

}
