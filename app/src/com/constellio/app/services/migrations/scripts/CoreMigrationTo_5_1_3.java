package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.conf.email.EmailConfigurationsManager;
import com.constellio.model.conf.email.EmailServerConfiguration;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.services.encrypt.EncryptionKeyFactory;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.SolrUserCredentialsManager;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class CoreMigrationTo_5_1_3 implements MigrationScript {
	@Override
	public String getVersion() {
		return "5.1.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		initEncryption(collection, provider, appLayerFactory);
		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
	}

	public static void initEncryption(String collection, MigrationResourcesProvider provider,
									  AppLayerFactory appLayerFactory) {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		DataLayerFactory dataLayerFactory = modelLayerFactory.getDataLayerFactory();
		EncryptionServices encryptionServices;
		try {
			if (isFirstInit(modelLayerFactory)) {
				try {
					createKeyFile(modelLayerFactory.getConfiguration(), dataLayerFactory.getDataLayerConfiguration());
				} catch (Exception e) {
					if (!Toggle.LOST_PRIVATE_KEY.isEnabled()) {
						throw new RuntimeException(e);
					}
				}
				try {
					createKeyDocument(dataLayerFactory);
				} catch (Exception e) {
					if (!Toggle.LOST_PRIVATE_KEY.isEnabled()) {
						throw new RuntimeException(e);
					}
				}
				encryptionServices = modelLayerFactory.newEncryptionServices();
				encryptLdapPassword(modelLayerFactory, encryptionServices);
				encryptUserTokens(modelLayerFactory.getUserCredentialsManager());
			} else {
				encryptionServices = modelLayerFactory.newEncryptionServices();
			}
			encryptEmailServerPassword(modelLayerFactory.getEmailConfigurationsManager(), collection);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void encryptEmailServerPassword(EmailConfigurationsManager emailManager,
												   String collection) {
		EmailServerConfiguration config = emailManager.getEmailConfiguration(collection, false);
		if (config != null) {
			emailManager.updateEmailServerConfiguration(config, collection, true);
		}
	}

	private static void encryptUserTokens(SolrUserCredentialsManager userCredentialsManager) {
		userCredentialsManager.rewrite();
	}

	private static void encryptLdapPassword(ModelLayerFactory modelLayerFactory,
											EncryptionServices encryptionServices) {
		LDAPConfigurationManager ldapConfigManager = modelLayerFactory
				.getLdapConfigurationManager();
		LDAPServerConfiguration serverConfiguration = ldapConfigManager.getLDAPServerConfiguration();
		LDAPUserSyncConfiguration userSynchConfiguration = ldapConfigManager
				.getLDAPUserSyncConfiguration(false);
		if (userSynchConfiguration != null && userSynchConfiguration.getUser() != null) {
			ldapConfigManager.saveLDAPConfiguration(serverConfiguration, userSynchConfiguration, false);
		}
	}

	private static void createKeyDocument(DataLayerFactory dataLayerFactory) {
		dataLayerFactory.saveEncryptionKey();
	}

	private static void createKeyFile(ModelLayerConfiguration modelLayerConfiguration,
									  DataLayerConfiguration dataLayerConfiguration)
			throws IOException {
		File encryptionFile = modelLayerConfiguration.getConstellioEncryptionFile();

		Random random = new Random();
		String fileKeyPart =
				"constellio_" + dataLayerConfiguration.createRandomUniqueKey() + "_ext";
		FileUtils.writeByteArrayToFile(encryptionFile, fileKeyPart.getBytes());
	}

	private static boolean isFirstInit(ModelLayerFactory modelLayerFactory) {
		try {
			EncryptionKeyFactory.getApplicationKey(modelLayerFactory);

			return false;
		} catch (Exception e) {
			return true;
		}
	}

}