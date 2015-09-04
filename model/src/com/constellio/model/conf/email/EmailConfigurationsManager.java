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

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.security.roles.RolesManagerRuntimeException;
import com.constellio.model.utils.OneXMLConfigPerCollectionManager;
import com.constellio.model.utils.OneXMLConfigPerCollectionManagerListener;
import com.constellio.model.utils.XMLConfigReader;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;

import java.util.Map;

public class EmailConfigurationsManager implements StatefulService, OneXMLConfigPerCollectionManagerListener<EmailServerConfiguration> {
    private static final String EMAIL_CONFIGS = "/emailConfigs.xml";
    public static final String USERNAME = "username";
    public static final String DEFAULT_SENDER_EMAIL = "defaultSenderEmail";
    public static final String PASSWORD = "password";
    public static final java.lang.String PROPERTIES = "properties";
    public static final java.lang.String PROPERTY = "property";
    public static final String KEY = "key";
    public static final String VALUE = "value";
    private OneXMLConfigPerCollectionManager<EmailServerConfiguration> oneXMLConfigPerCollectionManager;
    private ConfigManager configManager;
    private CollectionsListManager collectionsListManager;

    public EmailConfigurationsManager(ConfigManager configManager, CollectionsListManager collectionsListManager) {
        this.configManager = configManager;
        this.collectionsListManager = collectionsListManager;
    }

    public EmailServerConfiguration addEmailServerConfiguration(final EmailServerConfiguration emailServerConfiguration, final String collection) {
        validateEmailConfiguration(emailServerConfiguration);
        DocumentAlteration alteration = new DocumentAlteration() {
            @Override
            public void alter(Document document) {
                EmailConfigurationsManagerWriter writer = newEmailConfigurationsManagerWriter(document);
                writer.addEmailServerConfiguration(emailServerConfiguration, collection);
            }
        };
        oneXMLConfigPerCollectionManager.updateXML(collection, alteration);
        return emailServerConfiguration;
    }

    public void deleteEmailServerConfiguration(final String collection)
            throws RolesManagerRuntimeException {
        DocumentAlteration alteration = new DocumentAlteration() {
            @Override
            public void alter(Document document) {
                EmailConfigurationsManagerWriter writer = newEmailConfigurationsManagerWriter(document);
                writer.deleteEmailServerConfiguration(collection);
            }
        };
        oneXMLConfigPerCollectionManager.updateXML(collection, alteration);
    }

    public void updateEmailServerConfiguration(final EmailServerConfiguration emailServerConfiguration, final String collection)
            throws RolesManagerRuntimeException {
        validateEmailConfiguration(emailServerConfiguration);
        DocumentAlteration alteration = new DocumentAlteration() {
            @Override
            public void alter(Document document) {
                EmailConfigurationsManagerWriter writer = newEmailConfigurationsManagerWriter(document);
                writer.updateEmailServerConfiguration(emailServerConfiguration, collection);
            }
        };
        oneXMLConfigPerCollectionManager.updateXML(collection, alteration);
    }


    public static void validateEmailConfiguration(EmailServerConfiguration emailServerConfig) {
        String username = emailServerConfig.getUsername();
        if(StringUtils.isBlank(username)){
            throw new EmailServerConfigurationRuntimeException.InvalidBlankUsernameRuntimeException();
        }
        String password = emailServerConfig.getPassword();
        if(StringUtils.isBlank(password)){
            throw new EmailServerConfigurationRuntimeException.InvalidBlankPasswordRuntimeException();
        }
        Map<String, String> serverProperties = emailServerConfig.getProperties();
        if(serverProperties.isEmpty()){
            throw new EmailServerConfigurationRuntimeException.InvalidPropertiesRuntimeException();
        }
    }

    @Override
    public void onValueModified(String collection, EmailServerConfiguration newValue) {

    }

    @Override
    public void initialize() {
        this.oneXMLConfigPerCollectionManager = new OneXMLConfigPerCollectionManager<>(configManager, collectionsListManager,
                EMAIL_CONFIGS, xmlConfigReader(), this, new DocumentAlteration() {
            @Override
            public void alter(Document document) {
                EmailConfigurationsManagerWriter writer = newEmailConfigurationsManagerWriter(document);
                writer.createEmptyDocument();
            }
        });
    }

    public EmailServerConfiguration getEmailConfiguration(String collection) {
        return oneXMLConfigPerCollectionManager.get(collection);
    }

    private XMLConfigReader<EmailServerConfiguration> xmlConfigReader() {
        return new XMLConfigReader<EmailServerConfiguration>() {
            @Override
            public EmailServerConfiguration read(String collection, Document document) {
                return newEmailConfigurationReader(document).readEmailServerConfiguration(collection);
            }
        };
    }

    private EmailConfigurationManagerReader newEmailConfigurationReader(Document document) {
        return new EmailConfigurationManagerReader(document);
    }

    private EmailConfigurationsManagerWriter newEmailConfigurationsManagerWriter(Document document) {
        return new EmailConfigurationsManagerWriter(document);
    }

    @Override
    public void close() {

    }

    public void createCollectionEmailConfiguration(String collection) {
        DocumentAlteration createConfigAlteration = new DocumentAlteration() {
            @Override
            public void alter(Document document) {
                EmailConfigurationsManagerWriter writer = newEmailConfigurationsManagerWriter(document);
                writer.createEmptyDocument();
            }
        };
        oneXMLConfigPerCollectionManager.createCollectionFile(collection, createConfigAlteration);
    }
}
