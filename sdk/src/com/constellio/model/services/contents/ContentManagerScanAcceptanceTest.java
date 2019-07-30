package com.constellio.model.services.contents;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.contents.ContentManager.VaultScanResults;
import com.constellio.model.services.records.RecordLogicalDeleteOptions;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.FileTime;

import static org.assertj.core.api.Assertions.assertThat;

public class ContentManagerScanAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	ContentManager contentManager;
	MetadataSchemasManager metadataSchemasManager;
	RMSchemasRecordsServices rmSchemasRecordsServices;
	RecordServices recordServices;

	Document documentToBeDeleted;
	Document documentToBeKept;

	@Before
	public void setUp() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
						.withDocumentsHavingContent()
		);
		users.setUp(getModelLayerFactory().newUserServices());
		contentManager = getModelLayerFactory().getContentManager();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
	}

	@Test
	public void givenContentManagement() throws Exception {
		givenOneUnlinkedFileAndOneLinkedFileWhichAreNewlyCreated();

		VaultScanResults vaultScanResults = new VaultScanResults();
		contentManager.scanVaultContentAndDeleteUnreferencedFiles(vaultScanResults);
		assertThat(vaultScanResults.getNumberOfDeletedContents()).isEqualTo(0);

		givenOneUnlinkedFileAndOneLinkedWhichAreOlderThanThreeDays();

		vaultScanResults = new VaultScanResults();
		contentManager.scanVaultContentAndDeleteUnreferencedFiles(vaultScanResults);
		assertThat(vaultScanResults.getNumberOfDeletedContents()).isEqualTo(1);
		assertThat(vaultScanResults.getReportMessage()).contains(documentToBeDeleted.getContent().getCurrentVersion().getHash());
		assertThat(vaultScanResults.getReportMessage()).doesNotContain(documentToBeKept.getContent().getCurrentVersion().getHash());
	}

	private void givenOneUnlinkedFileAndOneLinkedFileWhichAreNewlyCreated() throws Exception {
		Document documentToBeDeleted = rmSchemasRecordsServices.newDocument().setTitle("documentToBeDeleted").setFolder(records.getFolder_A01())
				.setContent(createContent("documentToBeDeleted.txt"));
		Document documentToBeKept = rmSchemasRecordsServices.newDocument().setTitle("documentToBeDeleted").setFolder(records.getFolder_A01())
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

		givenTimeIs(TimeProvider.getLocalDateTime().plusDays(3));

		vaultScanResults = new VaultScanResults();
		contentManager.scanVaultContentAndDeleteUnreferencedFiles(vaultScanResults);


		assertThat(vaultScanResults.getNumberOfDeletedContents()).isEqualTo(1);
		assertThat(vaultScanResults.getReportMessage()).contains(documentToBeDeleted.getContent().getCurrentVersion().getHash());
		assertThat(vaultScanResults.getReportMessage()).doesNotContain(documentToBeKept.getContent().getCurrentVersion().getHash());
	}

	@Test
	public void givenTwoRecordReferencingSameContentThenDoNotDelete()
			throws RecordServicesException {
		metadataSchemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).createUndeletable("content").setType(MetadataValueType.CONTENT);
			}
		});

		Document documentToBeKept = rmSchemasRecordsServices.newDocument().setTitle("documentToBeDeleted").setFolder(records.getFolder_A01())
				.setContent(createContent("documentToBeKept.txt"));

		Folder folderWithContent = rmSchemasRecordsServices.getFolder(records.folder_A01);
		folderWithContent.set("content", createContent("documentToBeKept.txt"));

		Transaction transaction = new Transaction(documentToBeKept, folderWithContent);
		recordServices.execute(transaction);

		recordServices.physicallyDeleteNoMatterTheStatus(documentToBeKept.getWrappedRecord(),
				User.GOD, new RecordPhysicalDeleteOptions());

		VaultScanResults vaultScanResults = new VaultScanResults();
		contentManager.scanVaultContentAndDeleteUnreferencedFiles(vaultScanResults);

		Content contentOfFolder = folderWithContent.get("content");

		assertThat(vaultScanResults.getNumberOfDeletedContents()).isEqualTo(0);
		assertThat(vaultScanResults.getReportMessage()).doesNotContain(documentToBeKept.getContent().getCurrentVersion().getHash());
		assertThat(vaultScanResults.getReportMessage()).doesNotContain(contentOfFolder.getCurrentVersion().getHash());
	}

	private void givenOneUnlinkedFileAndOneLinkedWhichAreOlderThanThreeDays() throws Exception {
		File fileToBeDeleted = contentManager.getContentDao().getFileOf(documentToBeDeleted.getContent().getCurrentVersion().getHash());
		File fileToBeKept = contentManager.getContentDao().getFileOf(documentToBeKept.getContent().getCurrentVersion().getHash());
		Files.setAttribute(fileToBeDeleted.toPath(), "basic:creationTime", FileTime.fromMillis(System.currentTimeMillis() - 259200001), LinkOption.NOFOLLOW_LINKS);
		Files.setAttribute(fileToBeDeleted.toPath(), "basic:lastModifiedTime", FileTime.fromMillis(System.currentTimeMillis() - 259200001), LinkOption.NOFOLLOW_LINKS);
		Files.setAttribute(fileToBeKept.toPath(), "basic:creationTime", FileTime.fromMillis(System.currentTimeMillis() - 259200001), LinkOption.NOFOLLOW_LINKS);
		Files.setAttribute(fileToBeKept.toPath(), "basic:lastModifiedTime", FileTime.fromMillis(System.currentTimeMillis() - 259200001), LinkOption.NOFOLLOW_LINKS);
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
