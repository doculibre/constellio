package com.constellio.app.modules.robots.ui.pages;

import static com.constellio.model.entities.records.Record.PUBLIC_TOKEN;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.robots.ui.navigation.RobotsNavigationConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.robots.model.services.RobotsService;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.app.ui.pages.search.criteria.CriterionFactory;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;

/**
 * Created by Patrick on 2015-12-15.
 */
public class AddEditRobotPresenterAcceptTest extends ConstellioTest {

	@Mock CoreViews navigator;
	@Mock AddEditRobotView view;
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
		when(view.navigateTo()).thenReturn(navigator);

		smbClassifyServices = new SmbClassifyServices(zeCollection, getAppLayerFactory(), users.adminIn(zeCollection));

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		robotsService = new RobotsService(zeCollection, getAppLayerFactory());

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

		CriterionFactory criterionFactory = new CriterionFactory();
		Criterion criterion = criterionFactory.build(
				"{\"schemaType\":\"connectorSmbDocument\",\"metadataCode\":\"connectorSmbDocument_default_title\",\"metadataType\":\"STRING\",\"searchOperator\":\"EQUALS\",\"value\":\"1.txt\",\"leftParens\":false,\"rightParens\":false,\"booleanOperator\":\"AND\",\"dirty\":true,\"relativeCriteria\":{}}");
		robot = robotsService.newRobot((String) null)
				.setSearchCriteria(Arrays.asList(criterion))
				.setSchemaFilter("connectorSmbDocument")
				.setCode("robot1")
				.setTitle("robot1");
		recordServices.add(robot.getWrappedRecord());

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
