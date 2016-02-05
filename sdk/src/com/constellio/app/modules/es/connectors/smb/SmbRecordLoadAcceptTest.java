package com.constellio.app.modules.es.connectors.smb;

import static com.constellio.model.entities.records.Record.PUBLIC_TOKEN;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.BulkRecordTransactionHandler;
import com.constellio.model.services.records.BulkRecordTransactionHandlerOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.setups.Users;

@UiTest
public class SmbRecordLoadAcceptTest extends ConstellioTest {

	public static final String SMB = "smb://";
	public static final String FOLDER = "folder";
	public static final String DOCUMENT = "document";
	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;

	private ConnectorInstance<?> connectorInstance;
	private ConnectorManager connectorManager;
	private ESSchemasRecordsServices es;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioESModule().withRobotsModule().withAllTest(users)
				.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());

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

		createTree(10, 10, 3, 2);

		newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		waitUntilICloseTheBrowsers();

	}

	public void createTree(int numberOfFolders, int numberOfSubFolders, int numberOfLevel, int numberOfDocuments) {
		List<Record> smbRecords = new ArrayList<>();
		List<Record> referenceRecords = new ArrayList<>();

		BulkRecordTransactionHandlerOptions options = new BulkRecordTransactionHandlerOptions().withRecordsPerBatch(5000);
		BulkRecordTransactionHandler bulkTransactionsHandler = new BulkRecordTransactionHandler(recordServices,
				"SmbRecordLoad", options);

		for (int folderNumber = 0; folderNumber < numberOfFolders; folderNumber++) {
			ConnectorSmbFolder connectorSmbFolder = createConnectorSmbFolder(smbRecords, folderNumber);
			for (int documentNumber = 0; documentNumber < numberOfDocuments; documentNumber++) {
				createDocuments(smbRecords, referenceRecords, folderNumber, connectorSmbFolder);
			}

			List<ConnectorSmbFolder> parents = new ArrayList<>();
			List<ConnectorSmbFolder> newParents = new ArrayList<>();
			parents.add(connectorSmbFolder);
			referenceRecords.add(connectorSmbFolder.getWrappedRecord());
			for (int level = 0; level < numberOfLevel; level++) {
				if (level != 0) {
					parents.clear();
					parents.addAll(newParents);
					newParents.clear();
				}
				for (ConnectorSmbFolder parentFolder : parents) {
					for (int subFolderNumber = 0; subFolderNumber < numberOfSubFolders; subFolderNumber++) {
						ConnectorSmbFolder newConnectorSmbFolder = createSubFolder(folderNumber, level, parentFolder,
								subFolderNumber);
						smbRecords.add(newConnectorSmbFolder.getWrappedRecord());

						for (int documentNumber = 0; documentNumber < numberOfDocuments; documentNumber++) {
							createDocuments(smbRecords, referenceRecords, folderNumber, newConnectorSmbFolder);
						}
						newParents.add(newConnectorSmbFolder);
						if (!referenceRecords.contains(parentFolder.getWrappedRecord())) {
							referenceRecords.add(parentFolder.getWrappedRecord());
						}
					}
				}
				System.out.println("================");
				System.out.println(FOLDER + " " + folderNumber);
				System.out.println("Level " + level);
				System.out.println("================");
			}
			System.out.println(FOLDER + " " + folderNumber);
		}
		try {
			bulkTransactionsHandler.append(smbRecords, referenceRecords);
			smbRecords.clear();
			referenceRecords.clear();
		} finally {
			bulkTransactionsHandler.closeAndJoin();
		}
	}

	private ConnectorSmbFolder createSubFolder(int folderNumber, int level, ConnectorSmbFolder parentFolder, int k) {
		String folderName;
		String folderId;
		String folderUrl;
		folderName = FOLDER + folderNumber + level + k;
		folderId = UUID.randomUUID().toString();
		folderUrl = parentFolder.getUrl() + folderId + "/";
		System.out.println(folderUrl);
		ConnectorSmbFolder newConnectorSmbFolder = es.newConnectorSmbFolderWithId(folderId, connectorInstance);
		newConnectorSmbFolder.setTitle(folderName).setUrl(folderUrl).setParent(parentFolder.getId())
				.setManualTokens(
						PUBLIC_TOKEN);
		return newConnectorSmbFolder;
	}

	private void createDocuments(List<Record> smbRecords, List<Record> referenceRecords, int folderNumber,
			ConnectorSmbFolder connectorSmbFolder) {
		String documentName = connectorSmbFolder.getTitle() + DOCUMENT + folderNumber;
		String documentId = UUID.randomUUID().toString();
		String documentUrl = connectorSmbFolder.getUrl() + documentName + ".txt";

		ConnectorSmbDocument connectorSmbDocument = es.newConnectorSmbDocumentWithId(documentId, connectorInstance);
		connectorSmbDocument.setTitle(documentName).setUrl(documentUrl).setParent(connectorSmbFolder.getId())
				.setManualTokens(
						PUBLIC_TOKEN);
		smbRecords.add(connectorSmbDocument.getWrappedRecord());
		if (!referenceRecords.contains(connectorSmbFolder.getWrappedRecord())) {
			referenceRecords.add(connectorSmbFolder.getWrappedRecord());
		}
	}

	private ConnectorSmbFolder createConnectorSmbFolder(List<Record> smbRecords, int folderNumber) {
		Record parent = null;
		String folderName = FOLDER + folderNumber;
		String folderId = UUID.randomUUID().toString();
		String folderUrl = SMB + folderId + "/";
		ConnectorSmbFolder connectorSmbFolder = es.newConnectorSmbFolderWithId(folderId, connectorInstance);
		connectorSmbFolder.setTitle(folderName).setUrl(folderUrl).setParent(parent).setManualTokens(
				PUBLIC_TOKEN);
		smbRecords.add(connectorSmbFolder.getWrappedRecord());
		return connectorSmbFolder;
	}
}
