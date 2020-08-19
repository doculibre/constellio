package com.constellio.app.modules.robots.ui.pages;

import com.constellio.app.modules.complementary.esRmRobots.services.SmbClassifyServices;
import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.ConnectorSmbRuntimeException.ConnectorSmbRuntimeException_CannotDownloadSmbDocument;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorInstanciator;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.robots.model.services.RobotsService;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.modules.robots.ui.navigation.RobotsNavigationConfiguration;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.app.ui.pages.search.criteria.CriterionFactory;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.model.entities.records.Record.PUBLIC_TOKEN;
import static com.constellio.model.entities.schemas.Schemas.IS_DETACHED_AUTHORIZATIONS;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by Patrick on 2015-12-15.
 */
public class AddEditRobotPresenterAcceptTest extends ConstellioTest {

	MockedNavigation navigator;
	@Mock AddEditRobotView view;
	SessionContext sessionContext;
	RobotsService robotsService;
	AddEditRobotPresenter presenter;
	Robot robot;
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

	private SmbClassifyServices smbClassifyServices;

	@Spy ConnectorSmb connectorSmb;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioESModule().withRobotsModule().withAllTest(users)
				.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.navigate()).thenReturn(navigator);

		smbClassifyServices = new SmbClassifyServices(zeCollection, getAppLayerFactory(), users.adminIn(zeCollection));

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		robotsService = new RobotsService(zeCollection, getAppLayerFactory());

		inCollection(zeCollection).giveReadAccessTo(gandalf);
		Users users = new Users().setUp(getModelLayerFactory().newUserServices(), zeCollection);

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

		CriterionFactory criterionFactory = new CriterionFactory();
		Criterion criterion = criterionFactory.build(
				"{\"schemaType\":\"connectorSmbDocument\",\"metadataCode\":\"connectorSmbDocument_default_title\",\"metadataType\":\"STRING\",\"searchOperator\":\"EQUALS\",\"value\":\"1.txt\",\"leftParens\":false,\"rightParens\":false,\"booleanOperator\":\"AND\",\"dirty\":true,\"relativeCriteria\":{}}");
		robot = robotsService.newRobot((String) null)
				.setSearchCriteria(Arrays.asList(criterion))
				.setSchemaFilter("connectorSmbDocument")
				.setCode("robot1")
				.setTitle("robot1");
		recordServices.add(robot.getWrappedRecord());

		sessionContext = FakeSessionContext.chuckNorrisInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());

		presenter = new AddEditRobotPresenter(view);
	}

	@Test
	public void givenARobotWithASmbDocAsCriterionWhenGetDataProviderThenOk()
			throws Exception {

		givenFetchedFoldersAndDocuments();
		Map<String, String> params = new HashMap<>();
		params.put("pageMode", AddEditRobotPresenter.EDIT);
		params.put("robotId", robot.getId());
		String viewPath = ParamUtils.addParams(RobotsNavigationConfiguration.ADD_EDIT_ROBOT, params);
		presenter.forParams(viewPath);

		List<Criterion> criteria = robot.getSearchCriteria();
		assertThat(presenter.getSearchResults(criteria).size()).isEqualTo(1);

	}

	@Test
	public void givenAdvanceSearchThenMetadataChoiceIsLimitedByUsedSchemas()
			throws RecordServicesException {
		connectWithAdmin();
		List<MetadataVO> baseMetadatas = presenter.getMetadataAllowedInCriteria();

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(Folder.SCHEMA_TYPE).createCustomSchema("customSchema").create("newSearchableMetadata")
						.setType(MetadataValueType.STRING).setSearchable(true);
			}
		});

		SchemasDisplayManager metadataSchemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		metadataSchemasDisplayManager
				.saveMetadata(metadataSchemasDisplayManager.getMetadata(zeCollection, "folder_customSchema_newSearchableMetadata")
						.withVisibleInAdvancedSearchStatus(true));

		assertThat(baseMetadatas).containsAll(presenter.getMetadataAllowedInCriteria());
		recordServices.add(newFolder("testFolder").changeSchemaTo("folder_customSchema"));
		recordServices.update(recordServices.getDocumentById("testFolder").set(IS_DETACHED_AUTHORIZATIONS, true));

		List<MetadataVO> newMetadatas = presenter.getMetadataAllowedInCriteria();
		newMetadatas.removeAll(baseMetadatas);
		assertThat(newMetadatas.size()).isEqualTo(1);
		assertThat(newMetadatas.get(0).getCode()).isEqualTo("folder_customSchema_newSearchableMetadata");

		connectWithBob();
		assertThat(baseMetadatas).containsAll(presenter.getMetadataAllowedInCriteria());
	}

	@Test
	public void givenAdvanceSearchWithTaxonomiesThenIsLimitedByPermission()
			throws RecordServicesException {
		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, "justeadmin");

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaTypeBuilder justeadmin = types.createNewSchemaType("justeadmin");
				justeadmin.getDefaultSchema().create("code").setType(MetadataValueType.STRING);
			}
		});

		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		Taxonomy hiddenInHomePage = Taxonomy.createHiddenInHomePage("justeadmin", labelTitle1, zeCollection,
				"justeadmin").withUserIds(asList(records.getAdmin().getId()));
		getModelLayerFactory().getTaxonomiesManager().addTaxonomy(hiddenInHomePage, metadataSchemasManager);

		recordServices.add((RecordWrapper) rm.newHierarchicalValueListItem("justeadmin_default").setCode("J01")
				.set(Schemas.TITLE, "J01"));

		connectWithAdmin();
		List<MetadataVO> baseMetadatas = presenter.getMetadataAllowedInCriteria();

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getDefaultSchema(Folder.SCHEMA_TYPE).create("newSearchableMetadata")
						.setType(MetadataValueType.REFERENCE).defineReferencesTo(types.getDefaultSchema("justeadmin"))
						.setSearchable(true);
			}
		});

		SchemasDisplayManager metadataSchemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		metadataSchemasDisplayManager
				.saveMetadata(metadataSchemasDisplayManager.getMetadata(zeCollection, "folder_default_newSearchableMetadata")
						.withVisibleInAdvancedSearchStatus(true));

		List<MetadataVO> newMetadatas = presenter.getMetadataAllowedInCriteria();
		newMetadatas.removeAll(baseMetadatas);
		assertThat(newMetadatas.size()).isEqualTo(1);
		assertThat(newMetadatas.get(0).getCode()).isEqualTo("folder_default_newSearchableMetadata");

		connectWithBob();
		assertThat(baseMetadatas).containsAll(presenter.getMetadataAllowedInCriteria());
	}

	@Test
	public void givenAdvanceSearchThenDoNotShowDisabledMetadatas()
			throws RecordServicesException {
		connectWithAdmin();
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getDefaultSchema(Folder.SCHEMA_TYPE).get(Folder.BORROWED)
						.setEnabled(false);
			}
		});

		List<MetadataVO> baseMetadatas = presenter.getMetadataAllowedInCriteria();

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getDefaultSchema(Folder.SCHEMA_TYPE).get(Folder.BORROWED)
						.setEnabled(true);
			}
		});

		List<MetadataVO> newMetadatas = presenter.getMetadataAllowedInCriteria();
		newMetadatas.removeAll(baseMetadatas);
		assertThat(newMetadatas.size()).isEqualTo(1);
		assertThat(newMetadatas.get(0).getCode()).isEqualTo("folder_default_" + Folder.BORROWED);
	}

	private void connectWithAdmin() {
		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(view.getSessionContext()).thenReturn(sessionContext);
		presenter = spy(new AddEditRobotPresenter(view));
		presenter.schemaFilterSelected(Folder.SCHEMA_TYPE);
	}

	private void connectWithBob() {
		sessionContext = FakeSessionContext.bobInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		when(view.getSessionContext()).thenReturn(sessionContext);
		presenter = spy(new AddEditRobotPresenter(view));
		presenter.schemaFilterSelected(Folder.SCHEMA_TYPE);
	}

	private Folder newFolder(String title) {
		return rm.newFolderWithId("testFolder").setTitle(title).setOpenDate(LocalDate.now())
				.setAdministrativeUnitEntered(records.unitId_10a)
				.setCategoryEntered(records.categoryId_X110)
				.setRetentionRuleEntered(records.getRule2())
				.setCopyStatusEntered(CopyType.PRINCIPAL)
				.set("aCustomRequiredMetadata", "test");
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
				.setTitle("AA").setUrl("smb://A/A/").setParentUrl("smb://A/");

		transaction.add(es.newConnectorSmbFolderWithId(folderAAA, connectorInstance))
				.setTitle("AAA").setUrl("smb://A/A/A/").setParentUrl("smb://A/A/");

		transaction.add(es.newConnectorSmbFolderWithId(folderAAB, connectorInstance))
				.setTitle("AAB").setUrl("smb://A/A/B/").setParentUrl("smb://A/A/");

		transaction.add(es.newConnectorSmbFolderWithId(folderAB, connectorInstance))
				.setTitle("AB").setUrl("smb://A/B/").setParentUrl("smb://A/");

		transaction.add(es.newConnectorSmbFolderWithId(folderABA, connectorInstance))
				.setTitle("ABA").setUrl("smb://A/B/A/").setParentUrl("smb://A/B/");

		//		SmbFile localfile;
		//		try {
		//			localfile = new SmbFile("C:\\Users\\Patrick\\Documents\\tests\\docx.docx");
		//		} catch (MalformedURLException e) {
		//			throw new RuntimeException(e);
		//		}

		String url = "smb://A/1.txt";
		//		String url = localfile.getURL().getFile();
		transaction.add(es.newConnectorSmbDocumentWithId(documentA1, connectorInstance))
				.setTitle("1.txt").setUrl(url).setParsedContent("Document A1 content").setParentUrl("smb://A/").setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentA2, connectorInstance))
				.setTitle("2.txt").setUrl("smb://A/2.txt").setParsedContent("Document A2 content").setParentUrl("smb://A/")
				.setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentB3, connectorInstance))
				.setTitle("3.txt").setUrl("smb://B/3.txt").setParsedContent("Document B3 content").setParentUrl("smb://B/")
				.setManualTokens("rtoken1");

		transaction.add(es.newConnectorSmbDocumentWithId(documentB3JustDeleted, connectorInstance))
				.setTitle("justDeleted.txt").setUrl("smb://B/justDeleted.txt").setParsedContent("Document B3")
				.setParentUrl("smb://B/")
				.setManualTokens("rtoken1");

		transaction.add(es.newConnectorSmbDocumentWithId(documentAA4, connectorInstance))
				.setTitle("4.txt").setUrl("smb://A/A/4.txt").setParsedContent("Document AA4 content").setParentUrl("smb://A/A/")
				.setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentAA5, connectorInstance))
				.setTitle("5.txt").setUrl("smb://A/A/5.txt").setParentUrl("smb://A/A/").setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentAAA6, connectorInstance))
				.setTitle("6.txt").setUrl("smb://A/A/A/6.txt").setParentUrl("smb://A/A/A/").setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentAAB7, connectorInstance))
				.setTitle("7.txt").setUrl("smb://A/A/B/7.txt").setParentUrl("smb://A/A/B").setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentAB8, connectorInstance))
				.setTitle("8.txt").setUrl("smb://A/B/8.txt").setParentUrl("smb://A/B/").setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentABA9, connectorInstance))
				.setTitle("9.txt").setUrl("smb://A/B/A/9.txt").setParentUrl("smb://A/B/A/").setManualTokens(PUBLIC_TOKEN);

		recordServices.execute(transaction);
	}

}
