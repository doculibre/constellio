package com.constellio.app.modules.complementary.esRmRobots.services;

import static com.constellio.app.modules.complementary.esRmRobots.model.enums.ActionAfterClassification.DO_NOTHING;
import static com.constellio.model.entities.records.Record.PUBLIC_TOKEN;
import static com.constellio.model.entities.schemas.Schemas.LEGACY_ID;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

import java.io.File;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.complementary.esRmRobots.actions.ClassifyConnectorFolderDirectlyInThePlanActionExecutor;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderDirectlyInThePlanActionParameters;
import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.ConnectorSmbRuntimeException.ConnectorSmbRuntimeException_CannotDownloadSmbDocument;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorInstanciator;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.ui.pages.search.criteria.CriterionBuilder;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class ESRMRobotsServicesAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;
	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	SearchServices searchServices;

	private ConnectorInstance<?> connectorInstance;
	private ConnectorInstance<?> anotherConnectorInstance;
	private ConnectorManager connectorManager;
	private ESSchemasRecordsServices es;
	private RobotSchemaRecordServices robotsSchemas;

	private String robotId = "zeTerminator";

	private String share, domain, username, password;

	private String adminUnit1 = "adminUnit1";
	private String adminUnit11 = "adminUnit11";
	private String adminUnit2 = "adminUnit2";
	private String adminUnit21 = "adminUnit21";
	private String adminUnit22 = "adminUnit22";
	private String folderA = "folderA";
	private String folderB = "folderB";
	private String folderAA = "folderAA";
	private String folderAAA = "folderAAA";
	private String folderAAB = "folderAAB";
	private String folderABA = "folderABA";
	private String folderAB = "folderAB";
	private String documentA1 = "documentA1";
	private String documentA2 = "documentA2";
	private String documentB3 = "documentB3";
	private String documentB7JustDeleted = "documentB7JustDeleted";
	private String documentAA4 = "documentAA4";
	private String documentAA5 = "documentAA5";
	private String documentAAA6 = "documentAAA6";
	private String documentAAB7 = "documentAAB7";
	private String documentAB8 = "documentAB8";
	private String documentABA9 = "documentABA9";

	private String documentA1TaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/1.txt";
	private String documentA2TaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/2.txt";
	private String documentB3TaxoURL = "smb://AU2 Admin Unit2/AU21 Admin Unit21/B/3.txt";
	private String documentAA4TaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/4.txt";
	private String documentAA5TaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/5.txt";
	private String documentAAA6TaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/AAA/6.txt";
	private String documentAAB7TaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/AAB/7.txt";
	private String documentB7JustDeletedTaxoURL = "smb://AU2 Admin Unit2/AU21 Admin Unit21/B/7.txt";
	private String documentAB8TaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AB/8.txt";

	//TODO Support major/minor
	//TODO Fix problem when running a test directly

	@Spy ConnectorSmb connectorSmb;
	private String folderATaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/";
	private String folderAATaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/";
	private String folderABTaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AB/";
	private String folderAAATaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/AAA/";
	private String folderAABTaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/AAB/";
	private String folderBTaxoURL = "smb://AU2 Admin Unit2/AU21 Admin Unit21/B/";

	private String folderANoTaxoURL = "smb://host.ext/section/A/";
	private String folderAANoTaxoURL = "smb://host.ext/section/A/AA/";
	private String folderABNoTaxoURL = "smb://host.ext/section/A/AB/";
	private String folderAAANoTaxoURL = "smb://host.ext/section/A/AA/AAA/";
	private String folderAABNoTaxoURL = "smb://host.ext/section/A/AA/AAB/";
	private String folderBNoTaxoURL = "smb://host.ext/section/B/";
	private String documentA1NoTaxoURL = "smb://host.ext/section/A/1.txt";
	private String documentA2NoTaxoURL = "smb://host.ext/section/A/2.txt";
	private String documentB3NoTaxoURL = "smb://host.ext/section/B/3.txt";
	private String documentAA4NoTaxoURL = "smb://host.ext/section/A/AA/4.txt";
	private String documentAA5NoTaxoURL = "smb://host.ext/section/A/AA/5.txt";
	private String documentAAA6NoTaxoURL = "smb://host.ext/section/AAA/6.txt";

	private ContentManager contentManager;

	LocalDate squatreNovembre = new LocalDate(2010, 11, 4);

	@Before
	public void setUp()
			throws Exception {

		notAUnitItest = true;
		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioESModule().withRobotsModule().withAllTest(users)
				.withRMTest(records).withFoldersAndContainersOfEveryStatus());

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		contentManager = getModelLayerFactory().getContentManager();

		inCollection(zeCollection).giveReadAccessTo(gandalf);
		Users users = new Users().setUp(getModelLayerFactory().newUserServices());

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();

		share = "zeShare";//SDKPasswords.testSmbShare();
		domain = "zeDomain";//SDKPasswords.testSmbDomain();
		username = "zeUsername";//SDKPasswords.testSmbUsername();
		password = "zePassword";//SDKPasswords.testSmbPassword();

		recordServices.update(users.bobIn(zeCollection).setManualTokens("rtoken1"));
		recordServices.update(users.chuckNorrisIn(zeCollection).setManualTokens("rtoken1", "rtoken2"));

		es.getConnectorManager().setConnectorInstanciator(new ConnectorInstanciator() {
			@Override
			public Connector instanciate(ConnectorInstance connectorInstance) {
				return connectorSmb;
			}
		});

		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation)
					throws Throwable {
				ConnectorSmbDocument connectorSmbDocument = (ConnectorSmbDocument) invocation.getArguments()[0];
				String resourceName = (String) invocation.getArguments()[1];
				String name = connectorSmbDocument.getTitle();
				try {
					File file = getTestResourceFile(name);

					return getIOLayerFactory().newIOServices().newFileInputStream(file, resourceName);
				} catch (RuntimeException e) {
					throw new ConnectorSmbRuntimeException_CannotDownloadSmbDocument(connectorSmbDocument, e);
				}
			}
		}).when(connectorSmb).getInputStream(any(ConnectorSmbDocument.class), anyString());

		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setTitle("zeConnector")
				.setCode("zeConnector")
				.setEnabled(false)
				.setSeeds(asList(share)).setUsername(username).setPassword(password).setDomain(domain)
				.setTraversalCode("zeTraversal"));

		anotherConnectorInstance = connectorManager
				.createConnector(es.newConnectorSmbInstance()
						.setTitle("anotherConnector")
						.setCode("anotherConnector")
						.setEnabled(false)
						.setSeeds(asList(share)).setUsername(username).setPassword(password).setDomain(domain)
						.setTraversalCode("anotherConnectorTraversal"));

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());

		robotsSchemas = new RobotSchemaRecordServices(zeCollection, getAppLayerFactory());
		notAUnitItest = false;

	}

	@Test
	public void whenDeletingFoldersAndDocumentsCreatedByRobotThenOnlyDeleteThoseRecords()
			throws Exception {
		notAUnitItest = true;
		givenFetchedFoldersAndDocumentsWithoutValidTaxonomyPath();
		ClassifyConnectorFolderDirectlyInThePlanActionParameters parameters = ClassifyConnectorFolderDirectlyInThePlanActionParameters
				.wrap(robotsSchemas
						.newActionParameters(ClassifyConnectorFolderDirectlyInThePlanActionParameters.SCHEMA_LOCAL_CODE));
		recordServices.add(parameters
				.setActionAfterClassification(DO_NOTHING)
				.setDefaultCategory(records.categoryId_X)
				.setDefaultAdminUnit(records.unitId_10)
				.setDefaultCopyStatus(CopyType.PRINCIPAL)
				.setDefaultRetentionRule(records.ruleId_1)
				.setDefaultOpenDate(squatreNovembre));

		recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
				.setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
						new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
								.where(es.connectorSmbFolder.url()).isContainingText("/"))
				.setAction(ClassifyConnectorFolderDirectlyInThePlanActionExecutor.ID).setCode("terminator")
				.setTitle("terminator"));

		int initialFoldersCount = rm.searchFolders(where(LEGACY_ID).isNull()).size();
		int initialDocumentsCount = rm.searchDocuments(where(LEGACY_ID).isNull()).size();

		//1- First execution
		robotsSchemas.getRobotsManager().startAllRobotsExecution();
		waitForBatchProcess();
		assertThat(rm.searchFolders(where(LEGACY_ID).isNotNull())).extracting("legacyId", "title").containsOnly(
				tuple(folderANoTaxoURL, "A"),
				tuple(folderAANoTaxoURL, "AA"),
				tuple(folderABNoTaxoURL, "AB"),
				tuple(folderAAANoTaxoURL, "AAA"),
				tuple(folderAABNoTaxoURL, "AAB"),
				tuple(folderBNoTaxoURL, "B")
		);

		assertThat(rm.searchDocuments(where(LEGACY_ID).isNotNull()))
				.extracting("title", "content.currentVersion.hash", "content.currentVersion.version")
				.containsOnly(
						tuple("1.txt", "F+roHxDf6G8Ks/bQjnaxc1fPjuw=", "1.0"),
						tuple("2.txt", "B/Y1uv947wtmT6zR294q3eAkHOs=", "1.0"),
						tuple("3.txt", "LhTJnquyaSPRtdZItiSx0UNkpcc=", "1.0"),
						tuple("4.txt", "fRNOVjfA/c+w6xobmII/eIPU6s4=", "1.0")
				);

		User user = users.adminIn(zeCollection);
		ProgressInfo progressInfo = new ProgressInfo();
		new ESRMRobotsServices(getModelLayerFactory()).deleteRobotFoldersAndDocuments(user, robotId, progressInfo);

		assertThat(rm.searchFolders(where(LEGACY_ID).isNotNull())).isEmpty();
		assertThat(rm.searchDocuments(where(LEGACY_ID).isNotNull())).isEmpty();

		assertThat(progressInfo.getEnd()).isEqualTo(10);
		assertThat(progressInfo.getCurrentState()).isEqualTo(10);
		assertThat(rm.searchFolders(where(LEGACY_ID).isNull())).hasSize(initialFoldersCount);
		assertThat(rm.searchDocuments(where(LEGACY_ID).isNull())).hasSize(initialDocumentsCount);

	}

	//When delete, given error in transaction, then not deleted
	//when delete, log

	// ---------------------------------------

	private void givenFetchedTaxonomyWithValidFoldersButNoDocuments()
			throws RecordServicesException {
		Transaction transaction = new Transaction();
		transaction.add(rm.newAdministrativeUnitWithId(adminUnit1)).setCode("AU1").setTitle(adminUnit1);
		transaction.add(rm.newAdministrativeUnitWithId(adminUnit11)).setCode("AU11").setTitle(adminUnit11).setParent(adminUnit1);
		transaction.add(rm.newAdministrativeUnitWithId(adminUnit2)).setCode("AU2").setTitle(adminUnit2);
		transaction.add(rm.newAdministrativeUnitWithId(adminUnit21)).setCode("AU21").setTitle(adminUnit21).setParent(adminUnit2);
		transaction.add(rm.newAdministrativeUnitWithId(adminUnit22)).setCode("AU22").setTitle(adminUnit22).setParent(adminUnit2);

		transaction.add(es.newConnectorSmbFolderWithId(folderA, connectorInstance)).setTitle("A").setUrl(folderATaxoURL);
		transaction.add(es.newConnectorSmbFolderWithId(folderAA, connectorInstance)).setTitle("AA").setUrl(folderAATaxoURL)
				.setParent(folderA);
		transaction.add(es.newConnectorSmbFolderWithId(folderAB, connectorInstance)).setTitle("AB").setUrl(folderABTaxoURL)
				.setParent(folderA);
		transaction.add(es.newConnectorSmbFolderWithId(folderAAA, connectorInstance)).setTitle("AAA").setUrl(folderAAATaxoURL)
				.setParent(folderAA);
		transaction.add(es.newConnectorSmbFolderWithId(folderAAB, connectorInstance)).setTitle("AAB").setUrl(folderAABTaxoURL)
				.setParent(folderAA);
		transaction.add(es.newConnectorSmbFolderWithId(folderB, connectorInstance)).setTitle("B").setUrl(folderBTaxoURL);

		recordServices.execute(transaction);
	}

	private void givenFetchedFoldersAndDocumentsWithoutValidTaxonomyPath()
			throws RecordServicesException {
		Transaction transaction = new Transaction();

		transaction.add(es.newConnectorSmbFolderWithId(folderA, connectorInstance)).setTitle("A").setUrl(folderANoTaxoURL);
		transaction.add(es.newConnectorSmbFolderWithId(folderAA, connectorInstance)).setTitle("AA").setUrl(folderAANoTaxoURL)
				.setParent(folderA);
		transaction.add(es.newConnectorSmbFolderWithId(folderAB, connectorInstance)).setTitle("AB").setUrl(folderABNoTaxoURL)
				.setParent(folderA);
		transaction.add(es.newConnectorSmbFolderWithId(folderAAA, connectorInstance)).setTitle("AAA").setUrl(folderAAANoTaxoURL)
				.setParent(folderAA);
		transaction.add(es.newConnectorSmbFolderWithId(folderAAB, connectorInstance)).setTitle("AAB").setUrl(folderAABNoTaxoURL)
				.setParent(folderAA);
		transaction.add(es.newConnectorSmbFolderWithId(folderB, connectorInstance)).setTitle("B").setUrl(folderBNoTaxoURL);
		transaction.add(es.newConnectorSmbDocumentWithId(documentA1, connectorInstance))
				.setTitle("1.txt").setUrl(documentA1NoTaxoURL).setParsedContent("Document A1 content").setParent(
				folderA).setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentA2, connectorInstance))
				.setTitle("2.txt").setUrl(documentA2NoTaxoURL).setParsedContent("Document A2 content").setParent(folderA)
				.setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentB3, connectorInstance))
				.setTitle("3.txt").setUrl(documentB3NoTaxoURL).setParsedContent("Document B3 content").setParent(folderB)
				.setManualTokens("rtoken1");

		transaction.add(es.newConnectorSmbDocumentWithId(documentAA4, connectorInstance))
				.setTitle("4.txt").setUrl(documentAA4NoTaxoURL).setParsedContent("Document AA4 content").setParent(folderAA)
				.setManualTokens(PUBLIC_TOKEN);

		recordServices.execute(transaction);
	}

}
