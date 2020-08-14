package com.constellio.model.conf.email;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.security.roles.RolesManagerRuntimeException;
import com.constellio.model.utils.OneXMLConfigPerCollectionManager;
import com.constellio.model.utils.OneXMLConfigPerCollectionManagerListener;
import com.constellio.model.utils.XMLConfigReader;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;

import java.util.Map;

public class EmailConfigurationsManager
		implements StatefulService, OneXMLConfigPerCollectionManagerListener<EmailServerConfiguration> {
	private static final String EMAIL_CONFIGS = "/emailConfigs.xml";
	public static final String USERNAME = "username";
	public static final String DEFAULT_SENDER_EMAIL = "defaultSenderEmail";
	public static final String PASSWORD = "password";
	public static final String ENABLED = "enabled";
	public static final java.lang.String PROPERTIES = "properties";
	public static final java.lang.String PROPERTY = "property";
	public static final String KEY = "key";
	public static final String VALUE = "value";
	private OneXMLConfigPerCollectionManager<EmailServerConfiguration> oneXMLConfigPerCollectionManager;
	private ConfigManager configManager;
	private CollectionsListManager collectionsListManager;
	private ModelLayerFactory modelLayerFactory;
	private ConstellioCacheManager cacheManager;

	public EmailConfigurationsManager(ConfigManager configManager, CollectionsListManager collectionsListManager,
									  ModelLayerFactory modelLayerFactory, ConstellioCacheManager cacheManager) {
		this.configManager = configManager;
		this.collectionsListManager = collectionsListManager;
		this.modelLayerFactory = modelLayerFactory;
		this.cacheManager = cacheManager;
	}

	public EmailServerConfiguration addEmailServerConfiguration(EmailServerConfiguration emailServerConfiguration,
																final String collection) {
		validateEmailConfiguration(emailServerConfiguration);

		String encryptedPassword = (String) modelLayerFactory.newEncryptionServices().encryptWithAppKey(emailServerConfiguration.getPassword());
		final EmailServerConfiguration emailServerConfigurationToBeSaved = new BaseEmailServerConfiguration(
				emailServerConfiguration.getUsername(),
				encryptedPassword, emailServerConfiguration.getDefaultSenderEmail(),
				emailServerConfiguration.getProperties(), emailServerConfiguration.isEnabled());
		DocumentAlteration alteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				EmailConfigurationsManagerWriter writer = newEmailConfigurationsManagerWriter(document);
				writer.addEmailServerConfiguration(emailServerConfigurationToBeSaved, collection);
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

	public void updateEmailServerConfiguration(EmailServerConfiguration emailServerConfiguration,
											   final String collection,
											   boolean encryptPassword)
			throws RolesManagerRuntimeException {
		validateEmailConfiguration(emailServerConfiguration);
		final EmailServerConfiguration emailServerConfigurationToBeSaved;
		if (encryptPassword) {
			String encryptedPassword = (String) modelLayerFactory.newEncryptionServices().encryptWithAppKey(emailServerConfiguration.getPassword());
			emailServerConfigurationToBeSaved = new BaseEmailServerConfiguration(emailServerConfiguration.getUsername(),
					encryptedPassword, emailServerConfiguration.getDefaultSenderEmail(),
					emailServerConfiguration.getProperties(), emailServerConfiguration.isEnabled());
		} else {
			emailServerConfigurationToBeSaved = emailServerConfiguration;
		}
		DocumentAlteration alteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				EmailConfigurationsManagerWriter writer = newEmailConfigurationsManagerWriter(document);
				writer.updateEmailServerConfiguration(emailServerConfigurationToBeSaved, collection);
			}
		};
		oneXMLConfigPerCollectionManager.updateXML(collection, alteration);
	}

	public static void validateEmailConfiguration(EmailServerConfiguration emailServerConfig) {
		String username = emailServerConfig.getUsername();
		if (StringUtils.isBlank(username)) {
			throw new EmailServerConfigurationRuntimeException.InvalidBlankUsernameRuntimeException();
		}
		Map<String, String> serverProperties = emailServerConfig.getProperties();
		if (serverProperties.isEmpty()) {
			throw new EmailServerConfigurationRuntimeException.InvalidPropertiesRuntimeException();
		}
	}

	@Override
	public void onValueModified(String collection, EmailServerConfiguration newValue) {

	}

	@Override
	public void initialize() {
		ConstellioCache cache = cacheManager.getCache(EmailConfigurationsManager.class.getName());
		this.oneXMLConfigPerCollectionManager = new OneXMLConfigPerCollectionManager<>(configManager, collectionsListManager,
				EMAIL_CONFIGS, xmlConfigReader(), this, new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				EmailConfigurationsManagerWriter writer = newEmailConfigurationsManagerWriter(document);
				writer.createEmptyDocument();
			}
		}, cache);
	}

	public EmailServerConfiguration getEmailConfiguration(String collection, boolean decryptPassword) {
		EmailServerConfiguration config = oneXMLConfigPerCollectionManager.get(collection);
		if (!decryptPassword) {
			return config;
		} else if (config != null) {
			String password = config.getPassword();
			String decryptedPassword = (String) modelLayerFactory.newEncryptionServices().decryptWithAppKey(password);

			return new BaseEmailServerConfiguration(config.getUsername(), decryptedPassword, config.getDefaultSenderEmail(),
					config.getProperties(), config.isEnabled());
		}
		return null;
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
