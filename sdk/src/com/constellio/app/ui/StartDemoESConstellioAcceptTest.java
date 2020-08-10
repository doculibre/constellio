package com.constellio.app.ui;

import com.constellio.app.modules.es.connectors.http.utils.WebsitesUtils;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.setups.Users;
import org.eclipse.jetty.server.Server;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static com.constellio.app.modules.es.connectors.http.utils.WebsitesUtils.startWebsite;
import static java.util.Arrays.asList;

@UiTest
@MainTest
public class StartDemoESConstellioAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records = new RMTestRecords(zeCollection);
	DemoTestRecords records2 = new DemoTestRecords("LaCollectionDeRida");
	RMSchemasRecordsServices schemas;
	Users users = new Users();
	UserServices userServices;
	ESSchemasRecordsServices es;
	ConnectorManager connectorManager;
	ConnectorInstance connectorInstance, anotherConnectorInstace;

	@Before
	public void setUp()
			throws Exception {

		givenBackgroundThreadsEnabled();
		givenTransactionLogIsEnabled();
		prepareSystem(
				withZeCollection().withConstellioESModule().withConstellioRMModule().withRobotsModule().withAllTestUsers().withRMTest(records).withFoldersAndContainersOfEveryStatus()
		);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();
		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		users.setUp(getModelLayerFactory().newUserServices(), zeCollection);

		Transaction transaction = new Transaction();
		transaction.add(users.adminIn(zeCollection).setCollectionReadAccess(true));
		getModelLayerFactory().newRecordServices().execute(transaction);
	}

	@Test
	@MainTestDefaultStart
	public void startOnHomePageAsAdmin()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsAdminWithSmbConnectorAndPermissions()
			throws Exception {
		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		connectorManager = es.getConnectorManager();
		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode("zeConnectorCode")
				.setEnabled(true)
				.setSeeds(Arrays.asList(SDKPasswords.testSmbServer()))
				.setUsername(SDKPasswords.testSmbUsername())
				.setPassword(SDKPasswords.testSmbPassword())
				.setDomain(SDKPasswords.testSmbDomain())
				.setTraversalCode("")
				.setInclusions(Arrays.asList(SDKPasswords.testSmbServer()))
				.setTitle("New Smb Connector")
				.setSkipShareAccessControl(true));

		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));

		waitUntilICloseTheBrowsers();
	}


	//
	@Test
	public void startWithConnectorInstanceOnHomePageAsAdmin()
			throws Exception {
		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices, zeCollection);
		connectorManager = es.getConnectorManager();

		configureConnectorsInstances();

		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startWithHttpConnectorFetchingGouvQuebec()
			throws Exception {
		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices, zeCollection);
		connectorManager = es.getConnectorManager();

		connectorManager
				.createConnector(es.newConnectorHttpInstance()
						.setCode("wikipedia")
						.setTitle("Wikipedia")
						.setEnabled(true)
						.setSeeds("http://www.servicecanada.gc.ca/")
						.setDocumentsPerJobs(20)
						.setNumberOfJobsInParallel(20)
						.setIncludePatterns(".*"));

		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startWithHttpConnectorFetchingWikipedia()
			throws Exception {
		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices, zeCollection);
		connectorManager = es.getConnectorManager();

		connectorManager
				.createConnector(es.newConnectorHttpInstance()
						.setCode("wikipedia")
						.setTitle("Wikipedia")
						.setEnabled(true)
						.setSeeds("https://fr.wikipedia.org/wiki/Wikipédia:Accueil_principal")
						.setIncludePatterns("https://fr.wikipedia.org/wiki/"));

		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startWithThreeConnectors()
			throws Exception {
		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices, zeCollection);
		connectorManager = es.getConnectorManager();

		connectorManager
				.createConnector(es.newConnectorLDAPInstance()
						.setNumberOfJobsInParallel(15)
						.setNumberOfJobsInParallel(30)
						.setUrls(LDAPTestConfig.getUrls())
						.setUsersBaseContextList(LDAPTestConfig.getUsersWithoutGroupsBaseContextList())
						.setConnectionUsername(LDAPTestConfig.getUser() + "@" + LDAPTestConfig.getDomains().get(0))
						.setPassword(LDAPTestConfig.getPassword())
						.setCode("ldap")
						.setTitle("users")
						.setEnabled(true)
				);

		connectorManager
				.createConnector(es.newConnectorHttpInstance()
						.setCode("wikipedia")
						.setTitle("Wikipedia")
						.setEnabled(true)
						.setSeeds("https://fr.wikipedia.org/wiki/Wikipédia:Accueil_principal")
						.setIncludePatterns("https://fr.wikipedia.org/wiki/"));

		String host = SDKPasswords.testSmbServer();
		String share = SDKPasswords.testSmbShare();
		String domain = SDKPasswords.testSmbDomain();
		String username = SDKPasswords.testSmbUsername();
		String password = SDKPasswords.testSmbPassword();

		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()

				.setCode("zeConnectorCode")
				.setEnabled(true)
				.setSeeds(asList(host + share))
				.setUsername(username)
				.setPassword(password)
				.setDomain(domain)
				.setInclusions(Arrays.asList("smb://127.0.0.1/"))
				.setExclusions(new ArrayList<String>())
				.setTitle("zeConnectorTitle"));

		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		waitUntilICloseTheBrowsers();

	}

	@Test
	public void startWithLDAPConnector()
			throws Exception {

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices, zeCollection);
		connectorManager = es.getConnectorManager();

		connectorManager
				.createConnector(es.newConnectorLDAPInstance()
						.setNumberOfJobsInParallel(15)
						.setNumberOfJobsInParallel(30)
						.setUrls(LDAPTestConfig.getUrls())
						.setUsersBaseContextList(LDAPTestConfig.getUsersWithoutGroupsBaseContextList())
						.setConnectionUsername(LDAPTestConfig.getUser() + "@" + LDAPTestConfig.getDomains().get(0))
						.setPassword(LDAPTestConfig.getPassword())
						.setCode("ldap")
						.setTitle("users")
						.setEnabled(true)
				);

		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		waitUntilICloseTheBrowsers();

	}

	@Test
	public void startWithHttpConnectorFetchingLocalWikipedia()
			throws Exception {

		Server server = startWebsite(new File("/Volumes/Raid 1/wiki-extract/en/"));
		//Server server = startWebsite(new File("/Users/francisbaril/IdeaProjects/constellio-dev/sdk/sdk-resources/com/constellio/app/modules/es/connectors/http/utils/WebsitesUtils/animalsState1"));

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices, zeCollection);
		connectorManager = es.getConnectorManager();

		connectorManager
				.createConnector(es.newConnectorHttpInstance()
						.setCode("wikipedia")
						.setTitle("Wikipedia")
						.setEnabled(true)
						.setDocumentsPerJobs(15)
						.setNumberOfJobsInParallel(30)
						.setSeeds("http://localhost:4242/index.html"));

		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		waitUntilICloseTheBrowsers();

		//server.stop();
	}

	@Test
	public void startWithHttpConnectorFetchingAnimalWebsite()
			throws Exception {

		Server server = WebsitesUtils.startWebsiteInState1();

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices, zeCollection);
		connectorManager = es.getConnectorManager();

		connectorManager
				.createConnector(es.newConnectorHttpInstance()
						.setCode("wikipedia")
						.setTitle("Wikipedia")
						.setEnabled(true)
						.setSeeds("http://localhost:4242/index.html")
						.setIncludePatterns("http://localhost:4242/"));

		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		waitUntilICloseTheBrowsers();

		//server.stop();
	}

	@Test
	public void withAnSmbConnector()
			throws Exception {

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices, zeCollection);
		connectorManager = es.getConnectorManager();

		String host = SDKPasswords.testSmbServer();
		String share = SDKPasswords.testSmbShare();
		String domain = SDKPasswords.testSmbDomain();
		String username = SDKPasswords.testSmbUsername();
		String password = SDKPasswords.testSmbPassword();

		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()

				.setCode("zeConnectorCode")
				.setEnabled(true)
				.setSeeds(asList(host + share))
				.setUsername(username)
				.setPassword(password)
				.setDomain(domain)
				.setInclusions(Arrays.asList("smb://127.0.0.1/"))
				.setExclusions(new ArrayList<String>())
				.setTitle("zeConnectorTitle"));

		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		waitUntilICloseTheBrowsers();

	}

	@Test
	public void startApplicationWithSaveState()
			throws Exception {

		givenTransactionLogIsEnabled();
		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(
				new File("/path/to/the/saveState.zip")).withPasswordsReset();

		newWebDriver(loggedAsUserInCollection("zeUser", "myCollection"));
		waitUntilICloseTheBrowsers();

	}

	@Test
	public void startOnHomePageAsChuckNorris()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(chuckNorris, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsDakota()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(dakota, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsRida()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(admin, "LaCollectionDeRida"));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsGandalf()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(gandalf, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsBob()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(bobGratton, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsCharles()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(charlesFrancoisXavier, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsEdouard()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(edouard, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	//

	private void configureConnectorsInstances() {
		connectorInstance = connectorManager
				.createConnector(es.newConnectorHttpInstance()
						.setCode("zeConnector")
						.setTitle("Ze Connector")
						.setTraversalCode("traversalCode")
						.setEnabled(true)
						.setSeeds("http://constellio.com"));

		anotherConnectorInstace = connectorManager
				.createConnector(es.newConnectorHttpInstance()
						.setCode("anotherConnector")
						.setTitle("Another Connector")
						.setTraversalCode("anotherTraversalCode")
						.setEnabled(true)
						.setSeeds("http://constellio.com"));
	}
}


