package com.constellio.app.modules.es.connectors.smb;

import static com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator.OR;
import static com.constellio.model.entities.records.Record.PUBLIC_TOKEN;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.tools.ant.filters.StringInputStream;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.complementary.esRmRobots.actions.ClassifyConnectorFolderInTaxonomyActionExecutor;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.enums.ActionAfterClassification;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorInstanciator;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.ui.pages.search.criteria.CriterionBuilder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.BulkRecordTransactionHandler;
import com.constellio.model.services.records.BulkRecordTransactionHandlerOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.setups.Users;

@UiTest
public class ClassifySmbDocumentActionLoadAcceptTest extends ConstellioTest {

	public static final String SMB = "smb://";
	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;

	private ConnectorInstance<?> connectorInstance;
	private ConnectorManager connectorManager;
	private ESSchemasRecordsServices es;
	private RobotSchemaRecordServices robotsSchemas;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioESModule().withRobotsModule().withAllTest(users)
				.withRMTest(records).withFoldersAndContainersOfEveryStatus());

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());

		es.getConnectorManager().setConnectorInstanciator(new ConnectorInstanciator() {
			@Override
			public Connector instanciate(ConnectorInstance connectorInstance) {
				return new ConnectorSmb() {
					@Override
					public InputStream getInputStream(ConnectorSmbDocument document, String resourceName) {
						return new StringInputStream("Test!");
					}
				};
			}
		});

		robotsSchemas = new RobotSchemaRecordServices(zeCollection, getAppLayerFactory());

		recordServices = getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();

		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance().setCode("zeConnector").setEnabled(false)
				.setTitle("ze connector").setSeeds(asList("share")).setUsername("username").setPassword("password").setDomain(
						"domain")
				.setTraversalCode("zeTraversal"));
	}

	@Test
	@InDevelopmentTest
	public void testName()
			throws Exception {

		List<Integer> numberOfFolders = asList(10, 10, 10, 10);
		int numberOfDocumentsInEachFolders = 5;
		createLevel(numberOfFolders, numberOfDocumentsInEachFolders);

		ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
				.wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
		parameters.setInTaxonomy(RMTaxonomies.CLASSIFICATION_PLAN);
		parameters.setActionAfterClassification(ActionAfterClassification.DO_NOTHING);

		//TODO Default value
		parameters.setDelimiter(" ");

		recordServices.add(parameters);
		recordServices.add(robotsSchemas.newRobot().setActionParameters(parameters)
				.setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriteria(asList(
						new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE).booleanOperator(OR)
								.where(es.connectorSmbFolder.url()).isContainingText("smb").build()
				)).setAction(ClassifyConnectorFolderInTaxonomyActionExecutor.ID).setCode("robocop").setTitle("robocop"));

		robotsSchemas.getRobotsManager().startAllRobotsExecution();
		waitForBatchProcess();

		newWebDriver();
		waitUntilICloseTheBrowsers();

	}

	public void createLevel(List<Integer> numberOfFoldersForEachLevel, int numberOfDocuments)
			throws Exception {

		Stack<Integer> numberOfFoldersForEachLevelStack = new Stack<>();
		for (int i = numberOfFoldersForEachLevel.size() - 1; i >= 0; i--) {
			numberOfFoldersForEachLevelStack.add(numberOfFoldersForEachLevel.get(i));
		}

		ConnectorSmbFolder x = (ConnectorSmbFolder) es.newConnectorSmbFolder(connectorInstance)
				.setTitle("X")
				.setUrl(SMB + "X/")
				.setFetched(true)
				.setManualTokens(PUBLIC_TOKEN);

		ConnectorSmbFolder x100 = (ConnectorSmbFolder) es.newConnectorSmbFolder(connectorInstance)
				.setTitle("X100")
				.setUrl(SMB + "X/X100/")
				.setFetched(true)
				.setParent(x)
				.setManualTokens(PUBLIC_TOKEN);

		ConnectorSmbFolder x110 = (ConnectorSmbFolder) es.newConnectorSmbFolder(connectorInstance)
				.setTitle("X110")
				.setUrl(SMB + "X/X100/X110/")
				.setFetched(true)
				.setParent(x100)
				.setManualTokens(PUBLIC_TOKEN);

		ConnectorSmbFolder baleine = (ConnectorSmbFolder) es.newConnectorSmbFolder(connectorInstance)
				.setTitle("Baleine")
				.setUrl(SMB + "X/X100/X110/Baleine/")
				.setFetched(true)
				.setParent(x110)
				.setManualTokens(PUBLIC_TOKEN);

		Folder baleineRMFolder = (Folder) records.getFolder_A04().setLegacyId("smb://X/X100/X110/Baleine/");

		recordServices.execute(new Transaction().addAll(x, x100, x110, baleine, baleineRMFolder));

		createLevel(singletonList(baleine), numberOfFoldersForEachLevelStack,
				numberOfDocuments);

	}

	public void createLevel(List<ConnectorSmbFolder> parents,
			Stack<Integer> numberOfFoldersForEachLevel, int numberOfDocuments) {

		BulkRecordTransactionHandlerOptions options = new BulkRecordTransactionHandlerOptions().withRecordsPerBatch(5000);
		BulkRecordTransactionHandler bulkTransactionsHandler = new BulkRecordTransactionHandler(recordServices,
				"SmbRecordLoad", options);

		int numberOfFolders = 0;
		if (!numberOfFoldersForEachLevel.isEmpty()) {
			numberOfFolders = numberOfFoldersForEachLevel.pop();
		}

		List<ConnectorSmbFolder> folders = new ArrayList<>();
		List<Record> recordsToPush = new ArrayList<>();
		for (ConnectorSmbFolder parent : parents) {

			for (int i = 1; i <= numberOfFolders; i++) {
				ConnectorSmbFolder folder = createFolder(parent, i);

				recordsToPush.add(folder.getWrappedRecord());
				if (numberOfFoldersForEachLevel.isEmpty()) {
					for (int j = 1; j <= numberOfDocuments; j++) {
						ConnectorSmbDocument document = createDocument(folder, j);
						recordsToPush.add(document.getWrappedRecord());
					}
				} else {
					folders.add(folder);
				}

				bulkTransactionsHandler.append(recordsToPush, singletonList(parent.getWrappedRecord()));
				recordsToPush.clear();
			}

			if (parent != null) {
				for (int i = 1; i <= numberOfDocuments; i++) {
					ConnectorSmbDocument document = createDocument(parent, i);
					bulkTransactionsHandler.append(
							singletonList(document.getWrappedRecord()),
							singletonList(parent.getWrappedRecord()));
				}
			}

		}

		bulkTransactionsHandler.closeAndJoin();
		if (!folders.isEmpty()) {
			createLevel(folders, numberOfFoldersForEachLevel, numberOfDocuments);
		}
	}

	private ConnectorSmbDocument createDocument(ConnectorSmbFolder parent, int position) {
		return (ConnectorSmbDocument) es.newConnectorSmbDocument(connectorInstance)
				.setTitle("Document #" + parent.getTitle() + "-" + position)
				.setUrl(parent.getUrl() + position + ".txt")
				.setParent(parent)
				.setFetched(true)
				.setSearchable(true)
				.setManualTokens(PUBLIC_TOKEN);
	}

	private ConnectorSmbFolder createFolder(ConnectorSmbFolder parent, int position) {
		if (parent == null) {
			return (ConnectorSmbFolder) es.newConnectorSmbFolder(connectorInstance)
					.setTitle("Root folder #" + position)
					.setUrl(SMB + position + "/")
					.setFetched(true)
					.setSearchable(true)
					.setManualTokens(PUBLIC_TOKEN);
		} else {
			return (ConnectorSmbFolder) es.newConnectorSmbFolder(connectorInstance)
					.setTitle("Folder #" + parent.getTitle() + "-" + position)
					.setUrl(parent.getUrl() + position + "/")
					.setParent(parent)
					.setFetched(true)
					.setSearchable(true)
					.setManualTokens(PUBLIC_TOKEN);
		}
	}

}
