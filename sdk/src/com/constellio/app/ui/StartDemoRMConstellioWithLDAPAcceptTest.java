package com.constellio.app.ui;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
@MainTest
public class StartDemoRMConstellioWithLDAPAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records = new RMTestRecords(zeCollection);
	DemoTestRecords records2 = new DemoTestRecords("LaCollectionDeRida");
	RMSchemasRecordsServices schemas;

	@Before
	public void setUp()
			throws Exception {

		givenBackgroundThreadsEnabled();
		givenTransactionLogIsEnabled();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(
						records).withFoldersAndContainersOfEveryStatus().withEvents(),
				withCollection("LaCollectionDeRida").withConstellioRMModule().withAllTestUsers().withRMTest(records2)
						.withFoldersAndContainersOfEveryStatus()
		);
		inCollection("LaCollectionDeRida").setCollectionTitleTo("Collection d'entreprise");
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();

		LDAPUserSyncConfiguration userSync = LDAPTestConfig.getLDAPUserSyncConfiguration();
		LDAPServerConfiguration serverConf = LDAPTestConfig.getLDAPServerConfiguration();
		getModelLayerFactory().getLdapConfigurationManager().saveLDAPConfiguration(serverConf, userSync);
		UserServices userServices = getModelLayerFactory().newUserServices();
		System.out.println(userServices.getAllUserCredentials().size());
		getModelLayerFactory().getLdapUserSyncManager().synchronize();

		System.out.println(userServices.getAllUserCredentials().size());
		//		UserCredential administrator = userServices.getUser("Administrator");
		//		userServices.addUserToCollection(administrator, zeCollection);
	}

	@Test
	@MainTestDefaultStart
	public void startOnLoginPage()
			throws Exception {
		driver = newWebDriver();
		waitUntilICloseTheBrowsers();

	}

}
