package com.constellio.app.services.migrations.scripts;

import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.conf.email.EmailConfigurationsManager;
import com.constellio.model.conf.email.EmailServerConfiguration;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.users.SolrUserCredentialsManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class CoreMigrationTo_9_2AcceptanceTest extends ConstellioTest {
	@Test
	public void whenMigratingTo9_2ThenEncryptedFieldsAreDecryptableWithNewWay() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_9.2.zip");

		getCurrentTestSession().getFactoriesTestFeatures()
				.givenSystemInState(state);

		LDAPConfigurationManager ldapConfigurationManager = getModelLayerFactory().getLdapConfigurationManager();

		EncryptionServices encryptionServices = this.getModelLayerFactory().newEncryptionServices();

		LDAPUserSyncConfiguration ldapUserSyncConfiguration = ldapConfigurationManager.getLDAPUserSyncConfiguration(false);

		String ldapUserSync = (String) encryptionServices.decryptWithAppKey(ldapUserSyncConfiguration.getPassword());

		ldapUserSync.equals("FLKJDjkgfkdljg");

		EmailConfigurationsManager emailConfigurationsManager = getModelLayerFactory().getEmailConfigurationsManager();

		EmailServerConfiguration emailServerConfigurationzeCollection = emailConfigurationsManager.getEmailConfiguration(zeCollection, false);


		String decryptedKeyZeCollection = (String) encryptionServices.decryptWithAppKey(emailServerConfigurationzeCollection.getPassword());

		EmailServerConfiguration emailServerConfigurationCollection2 = emailConfigurationsManager.getEmailConfiguration("zeCollection2", false);

		String decryptedKeyCollection2 = (String) encryptionServices.decryptWithAppKey(emailServerConfigurationCollection2.getPassword());

		assertThat(decryptedKeyZeCollection).isEqualTo("wjggfwxvbpgkgokw");
		assertThat(decryptedKeyCollection2).isEqualTo("LKF:JDsghfhjkbdfg");

		ESSchemasRecordsServices schemasRecordsServices = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());

		ConnectorHttpInstance connectorHttpInstance = schemasRecordsServices.getConnectorHttpInstanceWithCode("perdu");
		assertThat(connectorHttpInstance.getPasssword()).isEqualTo("gdfsklgkljsfd");
	}


	@Test
	public void whenMigratingTo9_2ThenUserCredentialIsRetrivableByServiceKey() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_9.2.zip");

		getCurrentTestSession().getFactoriesTestFeatures()
				.givenSystemInState(state);

		SolrUserCredentialsManager solrUserCredentialsManager = getModelLayerFactory().getUserCredentialsManager();

		UserCredential userCredential = solrUserCredentialsManager.getUserCredentialByServiceKey("1ab6764a-d5ba-11ea-8ae5-633d2f7118c5");

		assertThat(userCredential).isNotNull();
		assertThat(userCredential.getUsername()).isEqualTo("admin");
	}

	@Test
	public void whenMigratingTo9_2ThenUserCredentialIsRetrivableByToken() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_9.2.zip");

		getCurrentTestSession().getFactoriesTestFeatures()
				.givenSystemInState(state);

		SolrUserCredentialsManager solrUserCredentialsManager = getModelLayerFactory().getUserCredentialsManager();

		UserCredential userCredential = solrUserCredentialsManager.getUserCredentialByToken("26dc0c12-d5ba-11ea-8ae5-d521396b0a57");

		assertThat(userCredential).isNotNull();
		assertThat(userCredential.getUsername()).isEqualTo("admin");

	}

}
