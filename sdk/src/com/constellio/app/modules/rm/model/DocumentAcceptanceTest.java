package com.constellio.app.modules.rm.model;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.TasksSearchServices;
import com.constellio.model.entities.enums.ParsingBehavior;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import static com.constellio.model.entities.enums.TitleMetadataPopulatePriority.PROPERTIES_FILENAME_STYLES;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.LocalDate.now;

public class DocumentAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	RecordServices recordServices;

	User dakota;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
								  .withFoldersAndContainersOfEveryStatus().withAllTest(users)
		);

		givenConfig(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS);

		assertThat(getModelLayerFactory().getTaxonomiesManager().getPrincipalTaxonomy(zeCollection).getCode())
				.isEqualTo(RMTaxonomies.ADMINISTRATIVE_UNITS);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

		dakota = users.dakotaLIndienIn(zeCollection);
	}

	@Test
	public void whenCreatingADocumentWithoutDescriptionThenOK()
			throws Exception {

		Document document = rm.newDocument().setTitle("My document").setDescription("test").setFolder(records.folder_A03);
		recordServices.add(document);
		document.setDescription(null).setTitle("Z");
		recordServices.update(document);

	}

	@Test
	public void givenFolderIsMovedThenDocumentPathIsUpdated()
			throws Exception {

		Document document = rm.newDocumentWithId("zeDocument").setTitle("My document").setDescription("test")
							  .setFolder(records.folder_A03);
		recordServices.add(document);

		assertThat(document.getList(Schemas.PATH)).containsOnly(
				"/admUnits/unitId_10/unitId_10a/A03/zeDocument",
				"/plan/categoryId_X/categoryId_X100/categoryId_X110/A03/zeDocument");

		recordServices.update(records.getFolder_A03().setCategoryEntered(records.categoryId_X120));

		recordServices.refresh(document);
		assertThat(document.getList(Schemas.PATH)).containsOnly(
				"/admUnits/unitId_10/unitId_10a/A03/zeDocument",
				"/plan/categoryId_X/categoryId_X100/categoryId_X120/A03/zeDocument");
	}

	@Test
	public void whenCreatingADocumentFromAMSGFileThenExtractMetadatas()
			throws Exception {
		ContentManager contentManager = rm.getModelLayerFactory().getContentManager();
		ContentVersionDataSummary datasummary = contentManager.upload(
				getTestResourceInputStreamFactory("test.msg").create(SDK_STREAM));

		Document document = rm.newDocumentWithId("zeId")
							  .setFolder(records.folder_A05)
							  .setTitle("a dummy title")
							  .setContent(contentManager.createMajor(records.getAdmin(), "test.msg", datasummary));

		recordServices.add(document);

		Email email = rm.getEmail("zeId");
		assertThat(email.getSchemaCode()).isEqualTo(Email.SCHEMA);
		assertThat(email.getTitle()).isEqualTo("a dummy title");
		assertThat(email.getEmailFrom()).isEqualTo("Addin");
		assertThat(email.getEmailTo()).isEqualTo(asList("ff@doculibre.com", "ll@doculibre.com"));
		assertThat(email.getEmailBCCTo()).isEqualTo(asList("rccr@doculibre.com", "hcch@doculibre.com"));
		assertThat(email.getEmailCCTo()).isEqualTo(asList("rr@doculibre.com", "hh@doculibre.com"));
		assertThat(email.getEmailObject()).isEqualTo("broullion2");
	}

	@Test
	public void whenCreatingADocumentFromAMSGFileUsingACustomSchemaThenDoesNotExtractMetadatas()
			throws Exception {

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(Document.SCHEMA_TYPE).createCustomSchema("aCustomSchema");
			}
		});

		ContentManager contentManager = rm.getModelLayerFactory().getContentManager();
		ContentVersionDataSummary datasummary = contentManager.upload(
				getTestResourceInputStreamFactory("test.msg").create(SDK_STREAM));

		Document document = rm.newDocumentWithId("zeId")
							  .setFolder(records.folder_A05)
							  .setTitle("a dummy title")
							  .setContent(contentManager.createMajor(records.getAdmin(), "test.msg", datasummary));
		document.changeSchemaTo("aCustomSchema");
		recordServices.add(document);

		Document email = rm.getDocument("zeId");
		assertThat(email.getSchemaCode()).isEqualTo("document_aCustomSchema");
		assertThat(email.getTitle()).isEqualTo("a dummy title");

	}

	@Test
	public void whenCreatingAUserDocumentFromAMSGFileThenExtractMetadatas()
			throws Exception {
		ContentManager contentManager = rm.getModelLayerFactory().getContentManager();
		ContentVersionDataSummary datasummary = contentManager.upload(
				getTestResourceInputStreamFactory("test.msg").create(SDK_STREAM));

		UserDocument userDocument = rm.newUserDocumentWithId("zeId")
									  .setContent(contentManager.createMajor(records.getAdmin(), "test.msg", datasummary));

		recordServices.add(userDocument);

		UserDocument email = rm.getUserDocument("zeId");
		assertThat(email.getSchemaCode()).isEqualTo(UserDocument.DEFAULT_SCHEMA);
		assertThat(email.getTitle()).isEqualTo("test.msg");

	}

	@Test
	public void givenPropertyArePreferedToFileNameWhenAddingMsgThenHasSubjectAsTitle()
			throws Exception {
		givenConfig(ConstellioEIMConfigs.TITLE_METADATA_POPULATE_PRIORITY, PROPERTIES_FILENAME_STYLES);

		ContentManager contentManager = rm.getModelLayerFactory().getContentManager();
		ContentVersionDataSummary datasummary = contentManager.upload(
				getTestResourceInputStreamFactory("test.msg").create(SDK_STREAM));

		UserDocument userDocument = rm.newUserDocumentWithId("zeId")
									  .setContent(contentManager.createMajor(records.getAdmin(), "test.msg", datasummary));

		recordServices.add(userDocument);

		UserDocument email = rm.getUserDocument("zeId");
		assertThat(email.getSchemaCode()).isEqualTo(UserDocument.DEFAULT_SCHEMA);
		assertThat(email.getTitle()).isEqualTo("broullion2");

	}

	@Test
	public void givenACheckedOutDocumentThenNotLogicallyDeletable()
			throws Exception {

		ContentManager contentManager = getModelLayerFactory().getContentManager();

		ContentVersionDataSummary version1 = contentManager.upload(getTestResourceFile("test.docx"));
		ContentVersionDataSummary version2 = contentManager.upload(getTestResourceFile("test2.docx"));
		Content content = contentManager.createMajor(dakota, "test.docx", version1);

		Folder childFolder = rm.newFolder().setParentFolder(records.getFolder_A03()).setTitle("childFolder").setOpenDate(now());
		Document wordDocument = rm.newDocument().setFolder(childFolder).setTitle("ze title").setContent(content);
		wordDocument.getContent().checkOut(dakota);
		recordServices.execute(new Transaction(childFolder, wordDocument));

		assertThat(recordServices.isLogicallyDeletable(wordDocument.getWrappedRecord(), User.GOD)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(childFolder.getWrappedRecord(), User.GOD)).isFalse();
		assertThat(recordServices.isLogicallyDeletable(records.getFolder_A03().getWrappedRecord(), User.GOD)).isFalse();

		wordDocument.getContent().checkIn();
		recordServices.update(wordDocument);

		assertThat(recordServices.isLogicallyDeletable(wordDocument.getWrappedRecord(), User.GOD)).isTrue();
		assertThat(recordServices.isLogicallyDeletable(childFolder.getWrappedRecord(), User.GOD)).isTrue();
		assertThat(recordServices.isLogicallyDeletable(records.getFolder_A03().getWrappedRecord(), User.GOD)).isTrue();
	}

	@Test
	public void whenCreatingADocumentWithAMicrosofContentThenConverted()
			throws Exception {

		ContentManager contentManager = getModelLayerFactory().getContentManager();

		ContentVersionDataSummary version1 = contentManager.upload(getTestResourceFile("test.docx"));
		ContentVersionDataSummary version2 = contentManager.upload(getTestResourceFile("test2.docx"));

		Document wordDocument = newDocumentWithContent(contentManager.createMajor(dakota, "test.docx", version1));
		recordServices.add(wordDocument);

		assertThat(wordDocument.isMarkedForPreviewConversion()).isTrue();
		assertThat(contentManager.hasContentPreview(wordDocument.getContent().getCurrentVersion().getHash())).isFalse();
		contentManager.convertPendingContentForPreview();
		recordServices.flush();
		recordServices.refresh(wordDocument);
		assertThat(wordDocument.isMarkedForPreviewConversion()).isFalse();
		assertThat(contentManager.hasContentPreview(wordDocument.getContent().getCurrentVersion().getHash())).isTrue();

		wordDocument.getContent().updateContent(dakota, version2, false);
		recordServices.update(wordDocument);

		assertThat(wordDocument.isMarkedForPreviewConversion()).isTrue();
		assertThat(contentManager.hasContentPreview(wordDocument.getContent().getCurrentVersion().getHash())).isFalse();
		contentManager.convertPendingContentForPreview();
		recordServices.flush();
		recordServices.refresh(wordDocument);

		assertThat(wordDocument.isMarkedForPreviewConversion()).isFalse();
		assertThat(contentManager.hasContentPreview(wordDocument.getContent().getCurrentVersion().getHash())).isTrue();

		wordDocument.setContent(contentManager.createMajor(dakota, "test.docx", version1));
		recordServices.update(wordDocument);

		assertThat(wordDocument.isMarkedForPreviewConversion()).isTrue();
		assertThat(contentManager.hasContentPreview(wordDocument.getContent().getCurrentVersion().getHash())).isTrue();
		contentManager.convertPendingContentForPreview();
		recordServices.flush();
		recordServices.refresh(wordDocument);
		assertThat(wordDocument.isMarkedForPreviewConversion()).isFalse();
		assertThat(contentManager.hasContentPreview(wordDocument.getContent().getCurrentVersion().getHash())).isTrue();

	}

	@Test
	public void givenDocumentInNonCompletedAndNotDeletedTaskThenCannotDelete()
			throws RecordServicesException {
		Document document = rm.newDocument().setFolder(records.getFolder_A03()).setTitle("ze title");
		recordServices.add(document);

		Task task = rm.newRMTask().setLinkedDocuments(asList(document.getId())).setTitle("Task");
		recordServices.add(task);
		assertThat(recordServices.isLogicallyDeletable(document.getWrappedRecord(), users.adminIn(zeCollection))).isFalse();

		recordServices.logicallyDelete(task.getWrappedRecord(), users.adminIn(zeCollection));
		assertThat(recordServices.isLogicallyDeletable(document.getWrappedRecord(), users.adminIn(zeCollection))).isTrue();

		recordServices.restore(task.getWrappedRecord(), users.adminIn(zeCollection));
		assertThat(recordServices.isLogicallyDeletable(document.getWrappedRecord(), users.adminIn(zeCollection))).isFalse();

		TasksSchemasRecordsServices tasksSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		TasksSearchServices taskSearchServices = new TasksSearchServices(tasksSchemas);
		recordServices.update(task.setStatus(taskSearchServices.getFirstFinishedStatus().getId()));
		assertThat(recordServices.isLogicallyDeletable(document.getWrappedRecord(), users.adminIn(zeCollection))).isTrue();
	}

	@Test
	public void whenCreatingADocumentThenOnlySupportedTypesAreMarkedForConversion()
			throws Exception {

		ContentManager contentManager = getModelLayerFactory().getContentManager();

		ContentVersionDataSummary version = contentManager.upload(getTestResourceInputStream("test.docx"));

		Transaction transaction = new Transaction();
		Document docWithDoc = transaction.add(newDocumentWithContent(contentManager.createMajor(dakota, "test.doc", version)));
		Document docWithXls = transaction.add(newDocumentWithContent(contentManager.createMajor(dakota, "test.xls", version)));
		Document doxWithPpt = transaction.add(newDocumentWithContent(contentManager.createMajor(dakota, "test.ppt", version)));
		Document docWithDocx = transaction.add(newDocumentWithContent(contentManager.createMajor(dakota, "test.docx", version)));
		Document docWithXlsx = transaction.add(newDocumentWithContent(contentManager.createMajor(dakota, "test.xlsx", version)));
		Document docWithPptx = transaction.add(newDocumentWithContent(contentManager.createMajor(dakota, "test.pptx", version)));
		Document docWithPdf = transaction.add(newDocumentWithContent(contentManager.createMajor(dakota, "test.pdf", version)));
		Document docWithDot = transaction.add(newDocumentWithContent(contentManager.createMajor(dakota, "test.dot", version)));
		Document docWithOdt = transaction.add(newDocumentWithContent(contentManager.createMajor(dakota, "test.odt", version)));
		Document docWithMp4 = transaction.add(newDocumentWithContent(contentManager.createMajor(dakota, "test.mp4", version)));
		recordServices.execute(transaction);

		assertThat(docWithDoc.isMarkedForPreviewConversion()).isTrue();
		assertThat(docWithXls.isMarkedForPreviewConversion()).isTrue();
		assertThat(doxWithPpt.isMarkedForPreviewConversion()).isTrue();
		assertThat(docWithDocx.isMarkedForPreviewConversion()).isTrue();
		assertThat(docWithXlsx.isMarkedForPreviewConversion()).isTrue();
		assertThat(docWithPptx.isMarkedForPreviewConversion()).isTrue();
		assertThat(docWithPdf.isMarkedForPreviewConversion()).isFalse();
		assertThat(docWithDot.isMarkedForPreviewConversion()).isTrue();
		assertThat(docWithOdt.isMarkedForPreviewConversion()).isTrue();
		assertThat(docWithMp4.isMarkedForPreviewConversion()).isFalse();

	}

	private Document newDocumentWithContent(Content content) {
		return rm.newDocument().setFolder(records.getFolder_A03()).setTitle("ze title").setContent(content);

	}
}