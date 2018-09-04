package com.constellio.app.modules.rm.wrappers.utils;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentUtilAcceptanceTest extends ConstellioTest {
	protected RMTestRecords records = new RMTestRecords(zeCollection);
	protected RecordServices recordServices;
	protected RMSchemasRecordsServices rm;
	protected Users users = new Users();

	@Before
	public void setUp() {
		prepareSystem(
				withZeCollection()
						.withConstellioRMModule()
						.withAllTestUsers()
						.withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
						.withDocumentsDecommissioningList()
		);

		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();
	}

	@Test
	public void givenDocumentInFolderWhenGettingDocumentsInTheFolderThenDocumentIsFound()
			throws RecordServicesException {
		Transaction tx = new Transaction();
		Folder parent = records.getFolder_A02();
		Document document = tx.add(records.newDocumentIn(parent));
		recordServices.execute(tx);
		assertThat(document).isNotNull();
		assertThat(document.getId()).isNotNull();
		assertThat(document.getFolder()).isEqualTo(parent.getId());

		List<Document> documentsInFolder = DocumentUtil.getDocumentsInFolder(parent, getAppLayerFactory());
		assertThat(documentsInFolder).isNotEmpty();
		assertThat(documentsInFolder).extracting("id").contains(document.getId());
	}

	@Test
	public void givenDocumentWhenCopiedThenManualPropertiesEqual() {
		Document newDocument = DocumentUtil.createNewDocument(zeCollection, getAppLayerFactory());
		assertThat(newDocument).isNotNull();
		assertThat(newDocument.getId()).isNotNull();

		newDocument.setTitle("Document title");
		newDocument.setType(records.documentTypeOther());
		newDocument.setCompany("Document company");

		Document copyFrom = DocumentUtil.createCopyFrom(newDocument, getAppLayerFactory());
		assertThat(copyFrom).isNotNull();
		assertThat(copyFrom.getId()).isNotNull().isNotEqualTo(newDocument.getId());
		assertThat(copyFrom.getType()).isEqualTo(newDocument.getType());
		assertThat(copyFrom.getCompany()).isEqualTo(newDocument.getCompany());
	}

	@Test
	public void whenNewDocumentCreatedThenIdIsNotNull() {
		Document newDocument = DocumentUtil.createNewDocument(zeCollection, getAppLayerFactory());
		assertThat(newDocument).isNotNull();
		assertThat(newDocument.getId()).isNotNull();
	}
}
