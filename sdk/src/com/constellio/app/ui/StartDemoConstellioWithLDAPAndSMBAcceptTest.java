package com.constellio.app.ui;

import static java.util.Arrays.asList;

import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.connectors.smb.testutils.LDAPTokenTestConfig;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
@MainTest
public class StartDemoConstellioWithLDAPAndSMBAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices schemas;
	private ESSchemasRecordsServices es;
	private ConnectorManager connectorManager;
	private ConnectorInstance<?> connectorInstance;

	@Before
	public void setUp()
			throws Exception {
		givenBackgroundThreadsEnabled();
		givenTransactionLogIsEnabled();
		prepareSystem(withZeCollection().withAllTestUsers()
				.withConstellioESModule());
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();

		// SMB
		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		connectorManager = es.getConnectorManager();

		// LDAP
		LDAPServerConfiguration ldapServerConfiguration = LDAPTokenTestConfig.getLDAPServerConfiguration();

		LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTokenTestConfig.getLDAPUserSyncConfiguration();

		ldapUserSyncConfiguration.setDurationBetweenExecution(new Duration(3600_000));

		getModelLayerFactory().getLdapConfigurationManager()
				.saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);

		// UserServices userServices = getModelLayerFactory().newUserServices();
		// System.out.println(userServices.getAllUserCredentials().size());
		getModelLayerFactory().getLdapUserSyncManager()
				.synchronizeIfPossible();

		// System.out.println(userServices.getAllUserCredentials().size());

		UserServices userServices = getModelLayerFactory().newUserServices();
		User user = userServices.getUserInCollection("admin", zeCollection);
		try {
			getModelLayerFactory().newRecordServices()
					.update(user.setCollectionAllAccess(true));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	@MainTestDefaultStart
	public void startOnLoginPage()
			throws Exception {
		String share = "shareBig/";
		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode("zeConnectorCode")
				.setEnabled(false)
				.setSeeds(asList(SDKPasswords.testSmbServer() + share))
				.setUsername(SDKPasswords.testSmbUsername())
				.setPassword(SDKPasswords.testSmbPassword())
				.setDomain(SDKPasswords.testSmbDomain())
				.setTraversalCode("")
				.setInclusions(asList(SDKPasswords.testSmbServer() + share))
				.setTitle("New Smb Connector"));

		driver = newWebDriver();
		waitUntilICloseTheBrowsers();

	}

	@Test
	@MainTestDefaultStart
	public void startOnLoginPageWithSeedBeingInvalidDocumentUrl()
			throws Exception {
		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode("zeConnectorCode")
				.setEnabled(false)
				.setSeeds(asList(SDKPasswords.testSmbServer() + "invalidShareBig/file"))
				.setUsername(SDKPasswords.testSmbUsername())
				.setPassword(SDKPasswords.testSmbPassword())
				.setDomain(SDKPasswords.testSmbDomain())
				.setTraversalCode("")
				.setInclusions(asList(SDKPasswords.testSmbServer() + "invalidShareBig/file"))
				.setTitle("New Smb Connector"));

		driver = newWebDriver();
		waitUntilICloseTheBrowsers();

	}

	@Test
	@MainTestDefaultStart
	public void startOnLoginPageWithInvalidPassword()
			throws Exception {
		String share = "shareBig/";
		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode("zeConnectorCode")
				.setEnabled(false)
				.setSeeds(asList(SDKPasswords.testSmbServer() + share + "3D-Modelling.pdf"))
				.setUsername(SDKPasswords.testSmbUsername())
				.setPassword("invalidPassword")
				.setDomain(SDKPasswords.testSmbDomain())
				.setTraversalCode("")
				.setInclusions(asList(SDKPasswords.testSmbServer() + share + "3D-Modelling.pdf"))
				.setTitle("New Smb Connector"));

		driver = newWebDriver();
		waitUntilICloseTheBrowsers();

	}

	@Test
	public void startOnLoginPageWithSeedWithSpace()
			throws Exception {
		String share = "share With Space/";
		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode("zeConnectorCode")
				.setEnabled(false)
				.setSeeds(asList(SDKPasswords.testSmbServer() + share))
				.setUsername(SDKPasswords.testSmbUsername())
				.setPassword(SDKPasswords.testSmbPassword())
				.setDomain(SDKPasswords.testSmbDomain())
				.setTraversalCode("")
				.setInclusions(asList(SDKPasswords.testSmbServer() + share))
				.setTitle("New Smb Connector"));

		driver = newWebDriver();
		waitUntilICloseTheBrowsers();

	}
}
