package com.constellio.app.modules.complementary.esRmRobots.services;

import static com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator.OR;
import static com.constellio.model.entities.records.Record.PUBLIC_TOKEN;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.constellio.app.modules.complementary.esRmRobots.actions.ClassifyConnectorDocumentInFolderActionExecutor;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorDocumentInFolderActionParameters;
import com.constellio.app.modules.es.connectors.ConnectorServicesFactory;
import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.ConnectorSmbRuntimeException.ConnectorSmbRuntimeException_CannotDownloadSmbDocument;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorInstanciator;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.ui.pages.search.criteria.CriterionBuilder;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.setups.Users;

public class ClassifyServicesAcceptanceTest extends ConstellioTest {

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

	private String share, domain, username, password;

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

	private SmbClassifyServices classifyServices;

	@Spy ConnectorSmb connectorSmb;
	@Mock ConnectorServicesFactory connectorServicesFactory;

	ConstellioWebDriver driver;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioESModule().withRobotsModule().withAllTest(users)
				.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());

		classifyServices = new SmbClassifyServices(zeCollection, getAppLayerFactory(), users.adminIn(zeCollection));

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();

		inCollection(zeCollection).giveReadAccessTo(gandalf);
		Users users = new Users().setUp(getModelLayerFactory().newUserServices());

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();
		connectorManager.setConnectorInstanciator(new ConnectorInstanciator() {
			@Override
			public Connector instanciate(ConnectorInstance connectorInstance) {
				return connectorSmb;
			}
		});

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

		robotsSchemas = new RobotSchemaRecordServices(zeCollection, getAppLayerFactory());
		notAUnitItest = false;
	}

	@Test
	public void givenConnectorSmbDocumentsWhenClassifyThenCreateRMDocumentsAndExcludeConnector()
			throws Exception {

		givenFetchedFoldersAndDocuments();

		ConnectorSmbDocument connectorSmbDocumentA1 = es.getConnectorSmbDocument(documentA1);
		ConnectorSmbDocument connectorSmbDocumentA2 = es.getConnectorSmbDocument(documentA2);
		ConnectorSmbDocument connectorSmbDocumentAA4 = es.getConnectorSmbDocument(documentAA4);
		ConnectorSmbDocument connectorSmbDocumentAA5 = es.getConnectorSmbDocument(documentAA5);
		ConnectorSmbDocument connectorSmbDocumentB3 = es.getConnectorSmbDocument(documentB3);

		List<String> smbConnectorDocumentsIds = new ArrayList<>();
		smbConnectorDocumentsIds.add(connectorSmbDocumentA1.getId());
		smbConnectorDocumentsIds.add(connectorSmbDocumentA2.getId());
		smbConnectorDocumentsIds.add(connectorSmbDocumentAA4.getId());
		smbConnectorDocumentsIds.add(connectorSmbDocumentAA5.getId());
		smbConnectorDocumentsIds.add(connectorSmbDocumentB3.getId());

		assertThat(classifyServices.classifyConnectorDocuments(records.folder_A01, smbConnectorDocumentsIds, true, true))
				.hasSize(5);
		validateAAAndB3SmbDocumentsAreClassifiedIn(records.folder_A01);
	}

	private void validateAAAndB3SmbDocumentsAreClassifiedIn(String folder) {

		ConnectorSmbDocument connectorSmbDocumentA1 = es.getConnectorSmbDocument(documentA1);
		ConnectorSmbDocument connectorSmbDocumentA2 = es.getConnectorSmbDocument(documentA2);
		ConnectorSmbDocument connectorSmbDocumentAA4 = es.getConnectorSmbDocument(documentAA4);
		ConnectorSmbDocument connectorSmbDocumentAA5 = es.getConnectorSmbDocument(documentAA5);
		ConnectorSmbDocument connectorSmbDocumentB3 = es.getConnectorSmbDocument(documentB3);

		List<String> smbConnectorDocumentsTitles = new ArrayList<>();
		smbConnectorDocumentsTitles.add(connectorSmbDocumentA1.getTitle());
		smbConnectorDocumentsTitles.add(connectorSmbDocumentA2.getTitle());
		smbConnectorDocumentsTitles.add(connectorSmbDocumentAA4.getTitle());
		smbConnectorDocumentsTitles.add(connectorSmbDocumentAA5.getTitle());
		smbConnectorDocumentsTitles.add(connectorSmbDocumentB3.getTitle());

		MetadataSchemaType documentSchemaType = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(Document.SCHEMA_TYPE);
		LogicalSearchCondition condition = from(documentSchemaType)
				.where(Schemas.TITLE).isIn(smbConnectorDocumentsTitles);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		List<Record> newDocumentsRecords = searchServices.search(query.sortAsc(Schemas.TITLE));
		List<Document> newDocumentsWrappers = rm.wrapDocuments(newDocumentsRecords);

		assertThat(es.getConnectorSmbInstance(es.getConnectorSmbDocument(documentA2).getConnector()).getExclusions())
				.contains("smb://A/1.txt", "smb://A/2.txt", "smb://B/3.txt", "smb://A/A/4.txt", "smb://A/A/5.txt");
		assertThat(recordServices.getDocumentById(documentA1).get(Schemas.FETCHED)).isEqualTo(false);
		assertThat(recordServices.getDocumentById(documentA2).get(Schemas.FETCHED)).isEqualTo(false);
		assertThat(recordServices.getDocumentById(documentB3).get(Schemas.FETCHED)).isEqualTo(false);
		assertThat(recordServices.getDocumentById(documentAA4).get(Schemas.FETCHED)).isEqualTo(false);
		assertThat(recordServices.getDocumentById(documentAA5).get(Schemas.FETCHED)).isEqualTo(false);
		assertThat(newDocumentsWrappers).hasSize(5);

		assertThat(newDocumentsWrappers.get(0).getTitle()).isEqualTo(connectorSmbDocumentA1.getTitle());
		assertThat(strContentOf(newDocumentsWrappers.get(0))).isEqualTo("Document 1");
		assertThat(newDocumentsWrappers.get(0).getFolder()).isEqualTo(folder);

		assertThat(newDocumentsWrappers.get(1).getTitle()).isEqualTo(connectorSmbDocumentA2.getTitle());
		assertThat(strContentOf(newDocumentsWrappers.get(1))).isEqualTo("Document 2");
		assertThat(newDocumentsWrappers.get(1).getFolder()).isEqualTo(folder);

		assertThat(newDocumentsWrappers.get(2).getTitle()).isEqualTo(connectorSmbDocumentB3.getTitle());
		assertThat(strContentOf(newDocumentsWrappers.get(2))).isEqualTo("Document 3");
		assertThat(newDocumentsWrappers.get(2).getFolder()).isEqualTo(folder);

		assertThat(newDocumentsWrappers.get(3).getTitle()).isEqualTo(connectorSmbDocumentAA4.getTitle());
		assertThat(strContentOf(newDocumentsWrappers.get(3))).isEqualTo("Document 4");
		assertThat(newDocumentsWrappers.get(3).getFolder()).isEqualTo(folder);

		assertThat(newDocumentsWrappers.get(4).getTitle()).isEqualTo(connectorSmbDocumentAA5.getTitle());
		assertThat(strContentOf(newDocumentsWrappers.get(4))).isEqualTo("Document 5");
		assertThat(newDocumentsWrappers.get(4).getFolder()).isEqualTo(folder);
	}

	@Test
	public void givenConnectorSmbDocumentsWhenClassifyUsingARobotThenCreateRMDocumentsAndExcludeConnector()
			throws Exception {

		givenFetchedFoldersAndDocuments();

		ConnectorSmbDocument connectorSmbDocumentA1 = es.getConnectorSmbDocument(documentA1);
		ConnectorSmbDocument connectorSmbDocumentA2 = es.getConnectorSmbDocument(documentA2);
		ConnectorSmbDocument connectorSmbDocumentAA4 = es.getConnectorSmbDocument(documentAA4);
		ConnectorSmbDocument connectorSmbDocumentAA5 = es.getConnectorSmbDocument(documentAA5);
		ConnectorSmbDocument connectorSmbDocumentB3 = es.getConnectorSmbDocument(documentB3);

		List<String> smbConnectorDocumentsIds = new ArrayList<>();
		smbConnectorDocumentsIds.add(connectorSmbDocumentA1.getId());
		smbConnectorDocumentsIds.add(connectorSmbDocumentA2.getId());
		smbConnectorDocumentsIds.add(connectorSmbDocumentAA4.getId());
		smbConnectorDocumentsIds.add(connectorSmbDocumentAA5.getId());
		smbConnectorDocumentsIds.add(connectorSmbDocumentB3.getId());

		ClassifyConnectorDocumentInFolderActionParameters parameters = ClassifyConnectorDocumentInFolderActionParameters
				.wrap(robotsSchemas.newActionParameters(ClassifyConnectorDocumentInFolderActionParameters.SCHEMA_LOCAL_CODE));
		recordServices.add(parameters.setInFolder(records.folder_A01).setMajorVersions(false));

		recordServices.add(robotsSchemas.newRobot().setActionParameters(parameters)
				.setSchemaFilter(ConnectorSmbDocument.SCHEMA_TYPE).setSearchCriteria(asList(
						new CriterionBuilder(ConnectorSmbDocument.SCHEMA_TYPE).booleanOperator(OR)
								.where(es.connectorSmbDocument.url()).isContainingText("smb").build(),
						new CriterionBuilder(ConnectorSmbDocument.SCHEMA_TYPE)
								.where(es.connectorSmbDocument.url()).isContainingText("smb://B/3").build()
				)).setAction(ClassifyConnectorDocumentInFolderActionExecutor.ID).setCode("robocop").setTitle("robocop"));

		robotsSchemas.getRobotsManager().startAllRobotsExecution();
		waitForBatchProcess();

		List<String> documents = classifyServices.classifyConnectorDocuments(records.folder_A01, smbConnectorDocumentsIds, false,
				true);
		assertThat(documents).hasSize(5);
		assertThat(rm.wrapDocument(record(documents.get(0))).getContent().getCurrentVersion().isMajor()).isFalse();
		validateAAAndB3SmbDocumentsAreClassifiedIn(records.folder_A01);
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

	@Test
	public void givenConnectorSmbFolderWhenClassifyThenCreateRMFolderAndExcludeConnector()
			throws Exception {

		givenFetchedFoldersAndDocuments();

		Folder zefolder = createZeFolder();

		recordServices.add(zefolder.getWrappedRecord());

		assertThat(classifyServices.classifySmbFolder(folderB, "zefolder", true)).hasSize(1);
		validateThatFolderBIsClassifiedIn(zefolder);

	}

	private void validateThatFolderBIsClassifiedIn(Folder zefolder) {
		ConnectorSmbDocument connectorSmbDocumentB3 = es.getConnectorSmbDocument(documentB3);
		Metadata folderMetadata = rm.documentFolder();
		MetadataSchemaType documentSchemaType = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(Document.SCHEMA_TYPE);
		LogicalSearchCondition condition = from(documentSchemaType)
				.where(folderMetadata).is(zefolder.getId());
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		List<Record> newDocumentsRecords = searchServices.search(query.sortAsc(Schemas.TITLE));
		List<Document> newDocumentsWrappers = rm.wrapDocuments(newDocumentsRecords);

		zefolder = rm.getFolder("zefolder");

		assertThat(es.getConnectorSmbInstance(es.getConnectorSmbFolder(folderB).getConnector()).getExclusions())
				.contains("smb://B/", "smb://B/3.txt");
		assertThat(recordServices.getDocumentById(folderB).get(Schemas.FETCHED)).isEqualTo(false);
		assertThat(recordServices.getDocumentById(documentB3).get(Schemas.FETCHED)).isEqualTo(false);
		assertThat(newDocumentsWrappers).hasSize(1);
		assertThat(newDocumentsWrappers.get(0).getTitle()).isEqualTo(connectorSmbDocumentB3.getTitle());
		assertThat(newDocumentsWrappers.get(0).getFolder()).isEqualTo(zefolder.getId());
		assertThat(newDocumentsWrappers.get(0).getContent().getCurrentVersion().isMajor()).isTrue();
	}

	@Test
	public void givenConnectorSmbHierarchyWhenClassifyThenCreateRMDocumentsAndExcludeConnector()
			throws Exception {

		givenFetchedFoldersAndDocuments();
		createZeFolder();

		assertThat(classifyServices.classifySmbFolder(folderA, "zefolder", true)).hasSize(8);

		rm.getFolder("zefolder");

		assertThat(es.getConnectorSmbInstance(es.getConnectorSmbFolder(folderA).getConnector()).getExclusions())
				.containsOnly("smb://A/",
						"smb://A/A/",
						"smb://A/B/",
						"smb://A/B/A/",
						"smb://A/A/A/",
						"smb://A/A/B/",
						"smb://A/1.txt",
						"smb://A/2.txt",
						"smb://A/A/4.txt",
						"smb://A/A/5.txt",
						"smb://A/A/A/6.txt",
						"smb://A/A/B/7.txt",
						"smb://A/B/8.txt",
						"smb://A/B/A/9.txt"
				);
		assertThat(recordServices.getDocumentById(folderA).get(Schemas.FETCHED)).isEqualTo(false);
		assertThat(recordServices.getDocumentById(folderAA).get(Schemas.FETCHED)).isEqualTo(false);
		assertThat(recordServices.getDocumentById(folderAAA).get(Schemas.FETCHED)).isEqualTo(false);
		assertThat(recordServices.getDocumentById(folderAAB).get(Schemas.FETCHED)).isEqualTo(false);
		assertThat(recordServices.getDocumentById(folderAB).get(Schemas.FETCHED)).isEqualTo(false);
		assertThat(recordServices.getDocumentById(folderABA).get(Schemas.FETCHED)).isEqualTo(false);
		assertThat(recordServices.getDocumentById(documentA1).get(Schemas.FETCHED)).isEqualTo(false);
		assertThat(recordServices.getDocumentById(documentA2).get(Schemas.FETCHED)).isEqualTo(false);
		assertThat(recordServices.getDocumentById(documentAA4).get(Schemas.FETCHED)).isEqualTo(false);
		assertThat(recordServices.getDocumentById(documentAA5).get(Schemas.FETCHED)).isEqualTo(false);
		assertThat(recordServices.getDocumentById(documentAAA6).get(Schemas.FETCHED)).isEqualTo(false);
		assertThat(recordServices.getDocumentById(documentAAB7).get(Schemas.FETCHED)).isEqualTo(false);
		assertThat(recordServices.getDocumentById(documentAB8).get(Schemas.FETCHED)).isEqualTo(false);
		assertThat(recordServices.getDocumentById(documentABA9).get(Schemas.FETCHED)).isEqualTo(false);
	}

	private Folder createZeFolder()
			throws RecordServicesException {
		Folder zefolder = rm.newFolderWithId("zefolder");
		zefolder.setAdministrativeUnitEntered(records.unitId_10a);
		zefolder.setCategoryEntered(records.categoryId_X110);
		zefolder.setTitle("Ze zefolder");
		zefolder.set("aCustomRequiredMetadata", "aValue");
		zefolder.setRetentionRuleEntered(records.ruleId_1);
		LocalDate localDate = TimeProvider.getLocalDate();
		zefolder.setOpenDate(localDate);

		recordServices.add(zefolder.getWrappedRecord());
		return zefolder;
	}

	// ---------------------------------------

	private void givenFetchedFoldersAndDocuments()
			throws RecordServicesException {

		Transaction transaction = new Transaction();
		transaction.add(es.newConnectorSmbFolderWithId(folderA, connectorInstance))
				.setTitle("A").setUrl("smb://A/");

		transaction.add(es.newConnectorSmbFolderWithId(folderB, connectorInstance))
				.setTitle("B").setUrl("smb://B/");

		transaction.add(es.newConnectorSmbFolderWithId(folderAA, connectorInstance))
				.setTitle("AA").setUrl("smb://A/A/").setParent(folderA);

		transaction.add(es.newConnectorSmbFolderWithId(folderAAA, connectorInstance))
				.setTitle("AAA").setUrl("smb://A/A/A/").setParent(folderAA);

		transaction.add(es.newConnectorSmbFolderWithId(folderAAB, connectorInstance))
				.setTitle("AAB").setUrl("smb://A/A/B/").setParent(folderAA);

		transaction.add(es.newConnectorSmbFolderWithId(folderAB, connectorInstance))
				.setTitle("AB").setUrl("smb://A/B/").setParent(folderA);

		transaction.add(es.newConnectorSmbFolderWithId(folderABA, connectorInstance))
				.setTitle("ABA").setUrl("smb://A/B/A/").setParent(folderAB);

		//		SmbFile localfile;
		//		try {
		//			localfile = new SmbFile("C:\\Users\\Patrick\\Documents\\tests\\docx.docx");
		//		} catch (MalformedURLException e) {
		//			throw new RuntimeException(e);
		//		}

		String url = "smb://A/1.txt";
		//		String url = localfile.getURL().getFile();
		transaction.add(es.newConnectorSmbDocumentWithId(documentA1, connectorInstance))
				.setTitle("1.txt").setUrl(url).setParsedContent("Document A1 content").setParent(
				folderA).setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentA2, connectorInstance))
				.setTitle("2.txt").setUrl("smb://A/2.txt").setParsedContent("Document A2 content").setParent(folderA)
				.setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentB3, connectorInstance))
				.setTitle("3.txt").setUrl("smb://B/3.txt").setParsedContent("Document B3 content").setParent(folderB)
				.setManualTokens("rtoken1");

		transaction.add(es.newConnectorSmbDocumentWithId(documentB3JustDeleted, connectorInstance))
				.setTitle("justDeleted.txt").setUrl("smb://B/justDeleted.txt").setParsedContent("Document B3")
				.setParent(folderB)
				.setManualTokens("rtoken1");

		transaction.add(es.newConnectorSmbDocumentWithId(documentAA4, connectorInstance))
				.setTitle("4.txt").setUrl("smb://A/A/4.txt").setParsedContent("Document AA4 content").setParent(folderAA)
				.setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentAA5, connectorInstance))
				.setTitle("5.txt").setUrl("smb://A/A/5.txt").setParent(folderAA).setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentAAA6, connectorInstance))
				.setTitle("6.txt").setUrl("smb://A/A/A/6.txt").setParent(folderAAA).setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentAAB7, connectorInstance))
				.setTitle("7.txt").setUrl("smb://A/A/B/7.txt").setParent(folderAAB).setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentAB8, connectorInstance))
				.setTitle("8.txt").setUrl("smb://A/B/8.txt").setParent(folderAB).setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentABA9, connectorInstance))
				.setTitle("9.txt").setUrl("smb://A/B/A/9.txt").setParent(folderABA).setManualTokens(PUBLIC_TOKEN);

		recordServices.execute(transaction);
	}

}
