package com.constellio.model.services.contents;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager.VaultScanResults;
import com.constellio.model.services.records.RecordLogicalDeleteOptions;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ContentManagerScanAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	ContentManager contentManager;

	@Before
	public void setUp() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
						.withDocumentsHavingContent()
		);
		users.setUp(getModelLayerFactory().newUserServices());
		contentManager = getModelLayerFactory().getContentManager();
	}

	@Test
	public void givenContentManagement() throws RecordServicesException {
		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		Document documentToBeDeleted = rm.newDocument().setTitle("documentToBeDeleted").setFolder(records.getFolder_A01())
				.setContent(createContent("documentToBeDeleted.txt"));
		Document documentToBeKept = rm.newDocument().setTitle("documentToBeDeleted").setFolder(records.getFolder_A01())
				.setContent(createContent("documentToBeKept.txt"));

		Transaction transaction = new Transaction(documentToBeDeleted, documentToBeKept);
		recordServices.execute(transaction);
		recordServices.physicallyDeleteNoMatterTheStatus(documentToBeDeleted.getWrappedRecord(), User.GOD, new RecordPhysicalDeleteOptions());
		recordServices.logicallyDelete(documentToBeKept.getWrappedRecord(), User.GOD, new RecordLogicalDeleteOptions());

		VaultScanResults vaultScanResults = new VaultScanResults();
		contentManager.scanVaultContentAndDeleteUnreferencedFiles(vaultScanResults);
		assertThat(vaultScanResults.getNumberOfDeletedContents()).isEqualTo(0);
		assertThat(vaultScanResults.getReportMessage()).doesNotContain(documentToBeDeleted.getContent().getCurrentVersion().getHash());
		assertThat(vaultScanResults.getReportMessage()).doesNotContain(documentToBeKept.getContent().getCurrentVersion().getHash());

		givenTimeIs(TimeProvider.getLocalDateTime().plusHours(35));

		vaultScanResults = new VaultScanResults();
		contentManager.scanVaultContentAndDeleteUnreferencedFiles(vaultScanResults);
		assertThat(vaultScanResults.getNumberOfDeletedContents()).isEqualTo(0);
		assertThat(vaultScanResults.getReportMessage()).doesNotContain(documentToBeDeleted.getContent().getCurrentVersion().getHash());
		assertThat(vaultScanResults.getReportMessage()).doesNotContain(documentToBeKept.getContent().getCurrentVersion().getHash());

		givenTimeIs(TimeProvider.getLocalDateTime().plusMinutes(61));

		vaultScanResults = new VaultScanResults();
		contentManager.scanVaultContentAndDeleteUnreferencedFiles(vaultScanResults);

		assertThat(vaultScanResults.getNumberOfDeletedContents()).isEqualTo(1);
		assertThat(vaultScanResults.getReportMessage()).contains(documentToBeDeleted.getContent().getCurrentVersion().getHash());
		assertThat(vaultScanResults.getReportMessage()).doesNotContain(documentToBeKept.getContent().getCurrentVersion().getHash());
	}

	private Content createContent(String filename) {
		User user = users.adminIn(zeCollection);
		ContentVersionDataSummary version01 = upload(filename);
		Content content = contentManager.createMinor(user, filename, version01);
		return content;
	}

	private ContentVersionDataSummary upload(String filename) {
		return contentManager.upload(getTestResourceInputStream(filename));
	}
}
