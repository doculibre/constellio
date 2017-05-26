package com.constellio.app.modules.es.connectors.smb;

import static com.constellio.app.modules.es.constants.ESTaxonomies.SMB_FOLDERS;
import static com.constellio.model.entities.records.Record.PUBLIC_TOKEN;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.ListAssert;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.SecurityTokenManager.TokenProvider;
import com.constellio.model.services.security.SecurityTokenManager.UserTokens;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class SmbRecordsAcceptanceTest extends ConstellioTest {

	private Users users = new Users();

	private ConnectorInstance<?> connectorInstance;
	private ConnectorInstance<?> anotherConnectorInstance;
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

	private String fetchedFolderFromAnotherConnector = "fetchedFolderFromAnotherConnector";
	private String fetchedFolderOfPreviousTraversalFromAnotherConnector = "fetchedFolderOfPreviousTraversalFromAnotherConnector";
	private String unfetchedFolderFromAnotherConnector = "unfetchedFolderFromAnotherConnector";
	private String fetchedFolder = "fetchedFolder";
	private String fetchedFolderOfPreviousTraversal = "fetchedFolderOfPreviousTraversal";
	private String unfetchedFolder = "unfetchedFolder";
	private String fetchedDocument = "fetchedDocument";
	private String fetchedDocumentOfPreviousTraversal = "fetchedDocumentOfPreviousTraversal";
	private String unfetchedDocument = "unfetchedDocument";

	private TaxonomiesSearchOptions defaultOptions = new TaxonomiesSearchOptions();

	private User userWithoutTokens;
	private User userWithToken1;
	private User userWithToken1And2;
	private User userWithCollectionReadAccess;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioESModule().withAllTest(users));
		inCollection(zeCollection).giveReadAccessTo(gandalf);

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();

		share = SDKPasswords.testSmbShare();
		domain = SDKPasswords.testSmbDomain();
		username = SDKPasswords.testSmbUsername();
		password = SDKPasswords.testSmbPassword();

		userWithoutTokens = users.sasquatchIn(zeCollection);
		userWithCollectionReadAccess = users.gandalfIn(zeCollection);
		userWithToken1 = users.bobIn(zeCollection);
		userWithToken1And2 = users.chuckNorrisIn(zeCollection);

		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance().setCode("zeConnector").setEnabled(false)
				.setTitle("ze connector").setSeeds(asList(share)).setUsername(username).setPassword(password).setDomain(domain)
				.setTraversalCode("zeTraversal"));

		anotherConnectorInstance = connectorManager
				.createConnector(es.newConnectorSmbInstance().setCode("anotherConnector").setEnabled(false)
						.setTitle("another connector").setSeeds(asList(share)).setUsername(username).setPassword(password)
						.setDomain(domain)
						.setTraversalCode("anotherConnectorTraversal"));

	}

	private void givenFetchedFoldersAndDocuments()
			throws RecordServicesException {

		Transaction transaction = new Transaction();

		transaction.add(es.newConnectorSmbFolderWithId("root", connectorInstance))
				.setTitle("root").setUrl("smb://");

		transaction.add(es.newConnectorSmbFolderWithId(folderA, connectorInstance))
				.setTitle("A").setUrl("smb://A/").setParent("root");

		transaction.add(es.newConnectorSmbFolderWithId(folderB, connectorInstance))
				.setTitle("B").setUrl("smb://B/").setParent("root");

		transaction.add(es.newConnectorSmbFolderWithId(folderAA, connectorInstance))
				.setTitle("AA").setUrl("smb://A/A/").setParent(folderA);

		transaction.add(es.newConnectorSmbFolderWithId(folderAB, connectorInstance))
				.setTitle("AB").setUrl("smb://A/B/").setParent(folderA);

		transaction.add(es.newConnectorSmbDocumentWithId(documentA1, connectorInstance))
				.setTitle("1.txt").setUrl("smb://A/1.txt").setParent(folderA).setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentA2, connectorInstance))
				.setTitle("2.txt").setUrl("smb://A/2.txt").setParent(folderA).setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentB3, connectorInstance))
				.setTitle("3.txt").setUrl("smb://B/3.txt").setParent(folderB).setManualTokens("rtoken1");

		transaction.add(es.newConnectorSmbDocumentWithId(documentAA4, connectorInstance))
				.setTitle("4.txt").setUrl("smb://A/A/4.txt").setParent(folderAA).setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentAA5, connectorInstance))
				.setTitle("5.txt").setUrl("smb://A/A/5.txt").setParent(folderAA).setManualTokens("rtoken2");

		recordServices.execute(transaction);
	}

	@Test
	public void givenFetchedAndUnfetchedRecordsOfDifferentConnectorsAndTraversalThenReturnUnfetchedBeforeFetchedOfPreviousTraversals()
			throws Exception {

		recordServices.update(connectorInstance.setTraversalCode("current"));

		Transaction transaction = new Transaction();

		LocalDateTime shishOClock = new LocalDateTime();

		transaction.add(es.newConnectorSmbFolderWithId(fetchedFolderFromAnotherConnector, anotherConnectorInstance))
				.setTitle("A").setUrl("smb://A/").setFetched(true).setTraversalCode("current");

		transaction.add(es.newConnectorSmbFolderWithId(fetchedFolderOfPreviousTraversalFromAnotherConnector,
				anotherConnectorInstance))
				.setTitle("B").setUrl("smb://B/").setFetched(true).setTraversalCode("previous").setParent(share);

		transaction.add(es.newConnectorSmbFolderWithId(unfetchedFolderFromAnotherConnector, anotherConnectorInstance))
				.setTitle("AA").setUrl("smb://A/A/").setFetched(false).setParent(fetchedFolderFromAnotherConnector);

		transaction.add(es.newConnectorSmbFolderWithId("root", connectorInstance))
				.setTitle("test").setUrl("smb://").setFetched(true).setTraversalCode("current")
				.setModifiedOn(shishOClock.plusSeconds(0));

		transaction.add(es.newConnectorSmbFolderWithId(fetchedFolder, connectorInstance))
				.setTitle("A").setUrl("smb://A/").setFetched(true).setTraversalCode("current")
				.setModifiedOn(shishOClock.plusSeconds(1)).setParent("root");

		transaction.add(es.newConnectorSmbFolderWithId(fetchedFolderOfPreviousTraversal, connectorInstance))
				.setTitle("B").setUrl("smb://B/").setFetched(true).setTraversalCode("previous")
				.setModifiedOn(shishOClock.plusSeconds(2)).setParent("root");

		transaction.add(es.newConnectorSmbFolderWithId(unfetchedFolder, connectorInstance))
				.setTitle("AA").setUrl("smb://A/A/").setFetched(false)
				.setModifiedOn(shishOClock.plusSeconds(6)).setParent(fetchedFolder);

		transaction.add(es.newConnectorSmbDocumentWithId(fetchedDocument, connectorInstance))
				.setTitle("1.txt").setUrl("smb://A/1.txt").setParent(fetchedFolder).setFetched(true).setTraversalCode("current")
				.setModifiedOn(shishOClock.plusSeconds(3)).setParent(fetchedFolder);

		transaction.add(es.newConnectorSmbDocumentWithId(fetchedDocumentOfPreviousTraversal, connectorInstance))
				.setTitle("2.txt").setUrl("smb://A/2.txt").setParent(fetchedFolder).setFetched(true).setTraversalCode("previous")
				.setModifiedOn(shishOClock.plusSeconds(4)).setParent(fetchedFolder);

		transaction.add(es.newConnectorSmbDocumentWithId(unfetchedDocument, connectorInstance))
				.setTitle("3.txt").setUrl("smb://B/3.txt").setParent(fetchedFolder).setFetched(false)
				.setModifiedOn(shishOClock.plusSeconds(5)).setParent(fetchedFolderOfPreviousTraversal);
		recordServices.execute(transaction);

		List<ConnectorDocument<?>> documents = es.searchConnectorDocuments(es.connectorDocumentsToFetchQuery(connectorInstance));
		assertThat(documents).extracting("id").isEqualTo(asList(
				unfetchedDocument,
				unfetchedFolder,
				fetchedFolderOfPreviousTraversal,
				fetchedDocumentOfPreviousTraversal
		));
		assertThat(documents).extractingResultOf("getClass").isEqualTo(asList(
				ConnectorSmbDocument.class,
				ConnectorSmbFolder.class,
				ConnectorSmbFolder.class,
				ConnectorSmbDocument.class
		));

	}

	@Test
	public void givenFolderAndDocumentsThenAllAppearsInTaxonomyForAUserWithCollectionReadAccess()
			throws Exception {

		givenFetchedFoldersAndDocuments();

		assertThatVisibleRootRecordsFor(userWithCollectionReadAccess).containsOnly(folderA, folderB);
		assertThatVisibleChildRecordsFor(userWithCollectionReadAccess).in(folderA)
				.containsOnly(folderAA, documentA1, documentA2);
		assertThatVisibleChildRecordsFor(userWithCollectionReadAccess).in(folderB).containsOnly(documentB3);
		assertThatVisibleChildRecordsFor(userWithCollectionReadAccess).in(folderAA).containsOnly(documentAA4, documentAA5);
		assertThatVisibleChildRecordsFor(userWithCollectionReadAccess).in(folderAB).isEmpty();

	}

	@Test
	public void givenFolderAndDocumentsThenSecuredOnesAppearsInTaxonomyForAUserWithTokensAndWithoutReadAccess()
			throws Exception {

		givenFetchedFoldersAndDocuments();

		getModelLayerFactory().getSecurityTokenManager().registerProvider(new TokenProvider() {
			@Override
			public UserTokens getTokensFor(String username, String collection) {
				if (username.equals(bob)) {
					return new UserTokens(asList("rtoken1"));
				} else if (username.equals(chuck)) {
					return new UserTokens(asList("rtoken1", "rtoken2"));
				} else {
					return new UserTokens();
				}

				//				recordServices.update(users.bobIn(zeCollection).setManualTokens("rtoken1"));
				//				recordServices.update(users.chuckNorrisIn(zeCollection).setManualTokens("rtoken1", "rtoken2"));
			}
		});

		getDataLayerFactory().getDataLayerLogger().setPrintAllQueriesLongerThanMS(0);

		assertThatVisibleRootRecordsFor(userWithToken1).containsOnly(folderA, folderB);
		assertThatVisibleChildRecordsFor(userWithToken1).in(folderA).containsOnly(folderAA, documentA1, documentA2);
		assertThatVisibleChildRecordsFor(userWithToken1).in(folderB).containsOnly(documentB3);
		assertThatVisibleChildRecordsFor(userWithToken1).in(folderAA).containsOnly(documentAA4);
		assertThatVisibleChildRecordsFor(userWithToken1).in(folderAB).isEmpty();

		assertThatVisibleRootRecordsFor(userWithToken1And2).containsOnly(folderA, folderB);
		assertThatVisibleChildRecordsFor(userWithToken1And2).in(folderA).containsOnly(folderAA, documentA1, documentA2);
		assertThatVisibleChildRecordsFor(userWithToken1And2).in(folderB).contains(documentB3);
		assertThatVisibleChildRecordsFor(userWithToken1And2).in(folderAA).containsOnly(documentAA4, documentAA5);
		assertThatVisibleChildRecordsFor(userWithToken1And2).in(folderAB).isEmpty();
	}

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
			Record record = getModelLayerFactory().newRecordServices().getDocumentById(recordId);
			TaxonomiesSearchServices taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();
			return assertThat(taxonomiesSearchServices.getVisibleChildConcept(user, SMB_FOLDERS, record, defaultOptions))
					.extracting("id");
		}
	}

}
