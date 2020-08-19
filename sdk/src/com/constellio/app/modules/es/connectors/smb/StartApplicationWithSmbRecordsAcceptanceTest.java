package com.constellio.app.modules.es.connectors.smb;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.setups.Users;
import org.assertj.core.api.ListAssert;
import org.junit.Before;

import static com.constellio.app.modules.es.constants.ESTaxonomies.SMB_FOLDERS;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@UiTest
public class StartApplicationWithSmbRecordsAcceptanceTest extends ConstellioTest {

	private Users users = new Users();

	private ConnectorInstance<?> connectorInstance;
	private ConnectorManager connectorManager;
	private RecordServices recordServices;
	private ESSchemasRecordsServices es;

	private String share, domain, username, password;

	private String folderA = "folderA";
	private String folderB = "folderB";
	private String folderAA = "folderAA";
	private String folderAB = "folderAB";
	private String documentA1 = "documentA1";
	private String documentA2 = "documentA2";
	private String documentB3 = "documentB3";
	private String documentAA4 = "documentAA4";
	private String documentAA5 = "documentAA5";

	private TaxonomiesSearchOptions defaultOptions = new TaxonomiesSearchOptions();

	private User userWithoutTokens;
	private User userWithToken1;
	private User userWithToken1And2;
	private User userWithCollectionReadAccess;

	@Before
	public void setUp()
			throws Exception {
		givenBackgroundThreadsEnabled();
		prepareSystem(withZeCollection().withConstellioESModule().withAllTestUsers());
		inCollection(zeCollection).giveReadAccessTo(gandalf);

		Users users = new Users().setUp(getModelLayerFactory().newUserServices(), zeCollection)
				.withPasswords(getModelLayerFactory().getPasswordFileAuthenticationService());

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();

		share = SDKPasswords.testSmbShare();
		domain = SDKPasswords.testSmbDomain();
		username = SDKPasswords.testSmbUsername();
		password = SDKPasswords.testSmbPassword();

		recordServices.update(users.bobIn(zeCollection)
				.setManualTokens("rtoken1"));
		recordServices.update(users.chuckNorrisIn(zeCollection)
				.setManualTokens("rtoken1", "rtoken2"));

		userWithoutTokens = users.sasquatchIn(zeCollection);
		userWithCollectionReadAccess = users.gandalfIn(zeCollection);
		userWithToken1 = users.bobIn(zeCollection);
		userWithToken1And2 = users.chuckNorrisIn(zeCollection);

		// Transaction transaction = new Transaction();
		// transaction.add(es.newConnectorSmbFolderWithId(folderA, connectorInstance))
		// .setTitle("A").setUrl("smb://A/");
		//
		// transaction.add(es.newConnectorSmbFolderWithId(folderB, connectorInstance))
		// .setTitle("B").setUrl("smb://B/");
		//
		// transaction.add(es.newConnectorSmbFolderWithId(folderAA, connectorInstance))
		// .setTitle("AA").setUrl("smb://A/A/").setParent(folderA);
		//
		// transaction.add(es.newConnectorSmbFolderWithId(folderAB, connectorInstance))
		// .setTitle("AB").setUrl("smb://A/B/");
		//
		// transaction.add(es.newConnectorSmbDocumentWithId(documentA1, connectorInstance))
		// .setTitle("1.txt").setUrl("smb://A/1.txt").setParent(folderA).setManualTokens(PUBLIC_TOKEN);
		//
		// transaction.add(es.newConnectorSmbDocumentWithId(documentA2, connectorInstance))
		// .setTitle("2.txt").setUrl("smb://A/2.txt").setParent(folderA).setManualTokens(PUBLIC_TOKEN);
		//
		// transaction.add(es.newConnectorSmbDocumentWithId(documentB3, connectorInstance))
		// .setTitle("3.txt").setUrl("smb://B/3.txt").setParent(folderB).setManualTokens("rtoken1");
		//
		// transaction.add(es.newConnectorSmbDocumentWithId(documentAA4, connectorInstance))
		// .setTitle("4.txt").setUrl("smb://A/A/4.txt").setParent(folderAA).setManualTokens(PUBLIC_TOKEN);
		//
		// transaction.add(es.newConnectorSmbDocumentWithId(documentAA5, connectorInstance))
		// .setTitle("5.txt").setUrl("smb://A/A/5.txt").setParent(folderAA).setManualTokens("rtoken2");
		//
		// recordServices.execute(transaction);
	}

	private void createConnector(String connectorCode) {
		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setCode(connectorCode)
				.setEnabled(false)
				.setSeeds(asList(share))
				.setUsername(username)
				.setPassword(password)
				.setDomain(domain)
				.setTraversalCode("")
				.setInclusions(asList(share))
				.setTitle("New Smb Connector"));
	}

	//	@Test
	//	@InDevelopmentTest
	//	public void givenAShareWhenTraversingThenAddUpdateDeleteContent()
	//			throws Exception {
	//
	//		createConnector("zeConnector");
	//
	//		newWebDriver();
	//		waitUntilICloseTheBrowsers();
	//	}


	//	@Test
	//	@InDevelopmentTest
	//	public void givenABigShareWhenTraversingThenAddUpdateDeleteContent()
	//			throws Exception {
	//		share = "smb://192.168.1.208/shareBig/";
	//		createConnector("zeConnector");
	//
	//		newWebDriver();
	//		waitUntilICloseTheBrowsers();
	//	}
	//
	//	@Test
	//	@InDevelopmentTest
	//	public void given2ConnectorsWhenRunningThenThereIsNoConflict()
	//			throws Exception {
	//
	//		createConnector("zeConnector");
	//
	//		share = "smb://192.168.1.208/shareBig/";
	//		createConnector("zeOtherConnector");
	//
	//		newWebDriver();
	//		waitUntilICloseTheBrowsers();
	//	}
	//
	//	@Test
	//	@InDevelopmentTest
	//	public void whenModifyingConfigurationThenTraversalUsesNewConfiguration() {
	//		fail("To implement!");
	//	}

	private ChildRecordAssertPreparator assertThatVisibleChildRecordsFor(User user) {
		return new ChildRecordAssertPreparator(user);
	}

	private ListAssert<Object> assertThatVisibleRootRecordsFor(User user) {
		TaxonomiesSearchServices taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();
		return assertThat(taxonomiesSearchServices.getVisibleRootConcept(user, zeCollection, SMB_FOLDERS, defaultOptions))
				.extracting("id");
	}

	private class ChildRecordAssertPreparator {
		private User user;

		private ChildRecordAssertPreparator(User user) {
			this.user = user;
		}

		private ListAssert<Object> in(String recordId) {
			Record record = getModelLayerFactory().newRecordServices()
					.getDocumentById(recordId);
			TaxonomiesSearchServices taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();
			return assertThat(taxonomiesSearchServices.getVisibleChildConcept(user, SMB_FOLDERS, record, defaultOptions))
					.extracting("id");
		}
	}

}
