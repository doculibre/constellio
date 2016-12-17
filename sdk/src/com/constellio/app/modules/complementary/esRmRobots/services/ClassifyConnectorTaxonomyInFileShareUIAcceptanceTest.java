package com.constellio.app.modules.complementary.esRmRobots.services;

import static com.constellio.app.modules.complementary.esRmRobots.model.enums.ActionAfterClassification.EXCLUDE_DOCUMENTS;
import static com.constellio.app.modules.rm.constants.RMTaxonomies.ADMINISTRATIVE_UNITS;
import static com.constellio.model.entities.records.Record.PUBLIC_TOKEN;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.constellio.app.modules.complementary.esRmRobots.actions.ClassifyConnectorFolderInTaxonomyActionExecutor;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters;
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
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.robots.model.wrappers.RobotLog;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.ui.pages.search.criteria.CriterionBuilder;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.setups.Users;

@UiTest
public class ClassifyConnectorTaxonomyInFileShareUIAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;
	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	SearchServices searchServices;

	private String robotId = "zeTerminator";
	private ConnectorInstance<?> connectorInstance;
	private ConnectorInstance<?> anotherConnectorInstance;
	private ConnectorManager connectorManager;
	private ESSchemasRecordsServices es;
	private RobotSchemaRecordServices robotsSchemas;

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
	private String documentB3JustDeleted = "documentB3JustDeleted";
	private String documentAA4 = "documentAA4";
	private String documentAA5 = "documentAA5";
	private String documentAAA6 = "documentAAA6";
	private String documentAAB7 = "documentAAB7";
	private String documentAB8 = "documentAB8";
	private String documentABA9 = "documentABA9";

	//TODO Support major/minor
	//TODO Fix problem when running a test directly

	@Spy ConnectorSmb connectorSmb;
	private String folderATaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/";
	private String folderAATaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/";
	private String folderABTaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AB/";
	private String folderAAATaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/AAA/";
	private String folderAABTaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/AAB/";
	private String folderBTaxoURL = "smb://AU2 Admin Unit2/AU21 Admin Unit21/B/";
	private ContentManager contentManager;

	@Before
	public void setUp()
			throws Exception {

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

		share = SDKPasswords.testSmbShare();
		domain = SDKPasswords.testSmbDomain();
		username = SDKPasswords.testSmbUsername();
		password = SDKPasswords.testSmbPassword();

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

		waitUntilICloseTheBrowsers();
	}

	private void withACustomRequiredMetadataInSchemas() {
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create("aCustomRequiredMetadata").setType(MetadataValueType.STRING)
						.setDefaultRequirement(
								true);
				types.getSchema(Document.DEFAULT_SCHEMA).create("aCustomRequiredMetadata").setType(MetadataValueType.STRING)
						.setDefaultRequirement(true);
			}
		});
	}

	@Test
	@InDevelopmentTest
	public void whenRobotClassifyingValidFoldersWithDocumentsAndDocumentMappingThenAllDocumentsMapped()
			throws Exception {
		notAUnitItest = true;
		givenFetchedTaxonomyWithFoldersAndDocuments();
		Content folderMapping = contentManager.createMajor(users.adminIn(zeCollection), "testFolderMapping.csv",
				contentManager.upload(getTestResourceInputStream("testFolderMapping.csv")));
		Content documentMapping = contentManager.createMajor(users.adminIn(zeCollection), "testDocumentMapping.csv",
				contentManager.upload(getTestResourceInputStream("testDocumentMapping.csv")));
		ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
				.wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
		recordServices.add(parameters.setInTaxonomy(ADMINISTRATIVE_UNITS).setActionAfterClassification(EXCLUDE_DOCUMENTS)
				.setDelimiter(" ").setDocumentMapping(documentMapping).setFolderMapping(folderMapping)
				.setDefaultCategory(records.categoryId_X));

		recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
				.setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
						new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
								.where(es.connectorSmbFolder.url()).isContainingText("smb://"))
				.setAction(ClassifyConnectorFolderInTaxonomyActionExecutor.ID).setCode("terminator").setTitle("terminator"));

		newWebDriver().logUserInCollection("admin", zeCollection);
		waitUntilICloseTheBrowsers();
	}

	//When delete, given error in transaction, then not deleted
	//when delete, log

	// ---------------------------------------

	private Document getDocumentByLegacyId(String path) {
		Metadata legacyIdMetadata = rm.defaultDocumentSchema().get(Schemas.LEGACY_ID.getLocalCode());
		return rm.wrapDocument(recordServices.getRecordByMetadata(legacyIdMetadata, path));
	}

	private List<Record> getDocumentsInFolder(Folder folder) {
		LogicalSearchCondition condition = from(rm.defaultDocumentSchema())
				.where(rm.defaultDocumentSchema().getMetadata(Document.FOLDER)).isEqualTo(folder.getId());
		return searchServices.search(new LogicalSearchQuery(condition));
	}

	private List<RobotLog> getRobotLogsForRobot(String robotId) {
		MetadataSchema robotLogSchema = robotsSchemas.robotLog.schema();
		LogicalSearchCondition condition = from(robotLogSchema)
				.where(robotLogSchema.getMetadata(RobotLog.ROBOT)).isEqualTo(robotId);
		return robotsSchemas.wrapRobotLogs(searchServices.search(new LogicalSearchQuery(condition)));
	}

	private void givenFetchedTaxonomyWithFoldersAndDocuments()
			throws RecordServicesException {
		givenFetchedTaxonomyWithValidFoldersButNoDocuments();

		Transaction transaction = new Transaction();
		transaction.add(es.newConnectorSmbDocumentWithId(documentA1, connectorInstance))
				.setTitle("1.txt").setUrl(folderATaxoURL + "1.txt").setParsedContent("Document A1 content").setParent(
				folderA).setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentA2, connectorInstance))
				.setTitle("2.txt").setUrl(folderATaxoURL + "2.txt").setParsedContent("Document A2 content").setParent(folderA)
				.setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentB3, connectorInstance))
				.setTitle("3.txt").setUrl(folderBTaxoURL + "3.txt").setParsedContent("Document B3 content").setParent(folderB)
				.setManualTokens("rtoken1");

		transaction.add(es.newConnectorSmbDocumentWithId(documentB3JustDeleted, connectorInstance))
				.setTitle("7.txt").setUrl(folderBTaxoURL + "7.txt").setParsedContent("Document B7")
				.setParent(folderB)
				.setManualTokens("rtoken1");

		transaction.add(es.newConnectorSmbDocumentWithId(documentAA4, connectorInstance))
				.setTitle("4.txt").setUrl(folderAATaxoURL + "4.txt").setParsedContent("Document AA4 content").setParent(folderAA)
				.setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentAA5, connectorInstance))
				.setTitle("5.txt").setUrl(folderAATaxoURL + "5.txt").setParent(folderAA).setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentAAA6, connectorInstance))
				.setTitle("6.txt").setUrl(folderAAATaxoURL + "6.txt").setParent(folderAAA).setManualTokens(PUBLIC_TOKEN);
		recordServices.execute(transaction);
	}

	private Folder getFolderByLegacyId(String path) {
		Metadata legacyIdMetadata = rm.defaultFolderSchema().get(Schemas.LEGACY_ID.getLocalCode());
		return rm.wrapFolder(recordServices.getRecordByMetadata(legacyIdMetadata, path));
	}

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

	private String strContentOf(Document document) {
		String hash = document.getContent().getCurrentVersion().getHash();
		InputStream inputStream = getModelLayerFactory().getContentManager().getContentInputStream(hash, SDK_STREAM);
		try {
			return IOUtils.toString(inputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	void classifyConnectorFolderInTaxonomy(Record connectorFolder, ClassifyConnectorFolderInTaxonomyActionParameters params) {
		ClassifyConnectorRecordInTaxonomyExecutor builder = new ClassifyConnectorRecordInTaxonomyExecutor(
				connectorFolder, params, es.getAppLayerFactory(), users.adminIn(zeCollection), robotId, new ArrayList<Record>());
		builder.execute();

	}
}
