package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.FolderMediaType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.model.entities.enums.ParsingBehavior;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RMMediumTypeRecordExtensionAcceptanceTest extends ConstellioTest {

	private Users users = new Users();
	private RMTestRecords records = new RMTestRecords(zeCollection);
	private RMSchemasRecordsServices rm;
	private RecordServices recordServices;

	private Content content;
	private MediumType digitalMediumType, paperMediumType;

	@Before
	public void setup() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus());
		givenBackgroundThreadsEnabled();
		givenConfig(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

		paperMediumType = rm.getMediumTypeByCode("PA");
		digitalMediumType = rm.getMediumTypeByCode("DM").setActivatedOnContent(true);
		recordServices.update(digitalMediumType);

		File file = newTempFileWithContent("test.txt", "This is a test");
		ContentManager contentManager = getModelLayerFactory().getContentManager();
		ContentVersionDataSummary data = contentManager.upload(file);
		content = contentManager.createMajor(users.adminIn(zeCollection), "test.txt", data);
	}

	@Test
	public void whenCheckingIfMediumTypeLogicallyDeletableThenFalse() {
		Record mediumType = rm.getMediumTypeByCode("DM").getWrappedRecord();
		assertThat(recordServices.validateLogicallyDeletable(mediumType, User.GOD).isEmpty()).isFalse();

		mediumType = rm.getMediumTypeByCode("FI").getWrappedRecord();
		assertThat(recordServices.validateLogicallyDeletable(mediumType, User.GOD).isEmpty()).isTrue();
	}

	//
	// Document
	//

	@Test
	public void givenDigitalDocumentCreatedThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Document document = newDigitalDocumentWithId("digitalDocument", folder);
		recordServices.add(document);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).contains(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenAnalogDocumentCreatedThenUnknownParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Document document = newAnalogDocumentWithId("analogDocument", folder);
		recordServices.add(document);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isFalse();
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);
	}

	@Test
	public void givenDigitalDocumentAndAnalogDocumentCreatedThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newDigitalDocumentWithId("digitalDocument", folder));

		Document document = newAnalogDocumentWithId("analogDocument", folder);
		recordServices.add(document);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenAnalogDocumentAndDigitalDocumentCreatedThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newAnalogDocumentWithId("analogDocument", folder));

		Document document = newDigitalDocumentWithId("digitalDocument", folder);
		recordServices.add(document);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenDigitalDocumentAndDigitalDocumentCreatedThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newDigitalDocumentWithId("digitalDocument", folder));

		Document document = newDigitalDocumentWithId("digitalDocument2", folder);
		recordServices.add(document);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenAnalogDocumentChangedToDigitalThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newAnalogDocumentWithId("analogDocument", folder));

		Document document = rm.getDocument("analogDocument");
		document.setContent(content);
		recordServices.update(document);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenDigitalDocumentChangedToAnalogThenUnknownParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newDigitalDocumentWithId("digitalDocument", folder));

		Document document = rm.getDocument("digitalDocument");
		document.setContent(null);
		recordServices.update(document);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isFalse();
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);
	}

	@Test
	public void givenAnalogDocumentAndAnalogDocumentChangedToDigitalThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newAnalogDocumentWithId("analogDocument", folder));
		recordServices.add(newAnalogDocumentWithId("analogDocument1", folder));

		Document document = rm.getDocument("analogDocument");
		document.setContent(content);
		recordServices.update(document);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenDigitalDocumentAndDigitalDocumentChangedToAnalogThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newDigitalDocumentWithId("digitalDocument", folder));
		recordServices.add(newDigitalDocumentWithId("digitalDocument1", folder));

		Document document = rm.getDocument("digitalDocument");
		document.setContent(null);
		recordServices.update(document);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenDigitalDocumentDeletedThenUnknownParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newDigitalDocumentWithId("digitalDocument", folder));

		recordServices.logicallyDelete(rm.get("digitalDocument"), User.GOD);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isFalse();
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);
	}

	@Test
	public void givenAnalogDocumentDeletedThenUnknownParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newAnalogDocumentWithId("analogDocument", folder));

		recordServices.logicallyDelete(rm.get("analogDocument"), User.GOD);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isFalse();
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);
	}

	@Test
	public void givenAnalogDocumentAndDigitalDocumentDeletedThenUnknownParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newAnalogDocumentWithId("analogDocument", folder));
		recordServices.add(newDigitalDocumentWithId("digitalDocument", folder));

		recordServices.logicallyDelete(rm.get("digitalDocument"), User.GOD);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isFalse();
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);
	}

	@Test
	public void givenDigitalDocumentAndAnalogDocumentDeletedThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newAnalogDocumentWithId("analogDocument", folder));
		recordServices.add(newDigitalDocumentWithId("digitalDocument", folder));

		recordServices.logicallyDelete(rm.get("analogDocument"), User.GOD);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenDigitalDocumentAndDigitalDocumentDeletedThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newDigitalDocumentWithId("digitalDocument", folder));
		recordServices.add(newDigitalDocumentWithId("anotherDigitalDocument", folder));

		recordServices.logicallyDelete(rm.get("digitalDocument"), User.GOD);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenAnalogDocumentAndAnalogDocumentDeletedThenAnalogParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newAnalogDocumentWithId("analogDocument", folder));
		recordServices.add(newAnalogDocumentWithId("analogDocument2", folder));

		recordServices.logicallyDelete(rm.get("analogDocument"), User.GOD);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isFalse();
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);
	}

	@Test
	public void givenAnalogDocumentRestoredThenUnknownParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newAnalogDocumentWithId("analogDocument", folder));
		recordServices.logicallyDelete(rm.get("analogDocument"), User.GOD);
		recordServices.restore(rm.get("analogDocument"), User.GOD);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isFalse();
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);
	}

	@Test
	public void givenDigitalDocumentRestoredThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newDigitalDocumentWithId("digitalDocument", folder));
		recordServices.logicallyDelete(rm.get("digitalDocument"), User.GOD);
		waitForBatchProcess();
		recordServices.restore(rm.get("digitalDocument"), User.GOD);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenAnalogDocumentAndDigitalDocumentRestoredThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newAnalogDocumentWithId("analogDocument", folder));
		recordServices.add(newDigitalDocumentWithId("digitalDocument", folder));
		recordServices.logicallyDelete(rm.get("digitalDocument"), User.GOD);
		waitForBatchProcess();
		recordServices.restore(rm.get("digitalDocument"), User.GOD);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenDigitalDocumentAndAnalogDocumentRestoredThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newAnalogDocumentWithId("analogDocument", folder));
		recordServices.add(newDigitalDocumentWithId("digitalDocument", folder));
		recordServices.logicallyDelete(rm.get("analogDocument"), User.GOD);
		recordServices.restore(rm.get("analogDocument"), User.GOD);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenDigitalDocumentAndDigitalDocumentRestoredThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newDigitalDocumentWithId("digitalDocument", folder));
		recordServices.add(newDigitalDocumentWithId("digitalDocument2", folder));
		recordServices.logicallyDelete(rm.get("digitalDocument"), User.GOD);
		waitForBatchProcess();
		recordServices.restore(rm.get("digitalDocument"), User.GOD);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenAnalogDocumentAndAnalogDocumentRestoredThenUnknownParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newAnalogDocumentWithId("analogDocument", folder));
		recordServices.add(newAnalogDocumentWithId("analogDocument2", folder));
		recordServices.logicallyDelete(rm.get("analogDocument"), User.GOD);
		recordServices.restore(rm.get("analogDocument"), User.GOD);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isFalse();
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);
	}

	@Test
	public void givenAnalogDocumentMovedThenUnknownOldParentFolderAndUnknownParentFolder() throws Exception {
		Folder folder = newFolderWithId("oldParentFolder");
		Folder folder2 = newFolderWithId("parentFolder");
		recordServices.execute(new Transaction().addAll(folder, folder2));

		recordServices.add(newAnalogDocumentWithId("analogDocument", folder));

		Document document = rm.getDocument("analogDocument");
		document.setFolder(folder2);

		folder = rm.getFolder("oldParentFolder");
		assertThat(folder.hasContent()).isFalse();
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);

		folder2 = rm.getFolder("parentFolder");
		assertThat(folder2.hasContent()).isFalse();
		assertThat(folder2.getMediumTypes()).isEmpty();
		assertThat(folder2.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);
	}

	@Test
	public void givenDigitalDocumentMovedThenAnalogOldParentFolderAndDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("oldParentFolder");
		Folder folder2 = newFolderWithId("parentFolder");
		recordServices.execute(new Transaction().addAll(folder, folder2));

		recordServices.add(newDigitalDocumentWithId("digitalDocument", folder));

		Document document = rm.getDocument("digitalDocument");
		document.setFolder(folder2);
		recordServices.update(document);
		waitForBatchProcess();

		folder = rm.getFolder("oldParentFolder");
		assertThat(folder.hasContent()).isFalse();
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);

		folder2 = rm.getFolder("parentFolder");
		assertThat(folder2.hasContent()).isTrue();
		assertThat(folder2.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder2.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenDigitalDocumentAndDigitalDocumentMovedThenDigitalOldParentFolderAndDigitalParentFolder()
			throws Exception {
		Folder folder = newFolderWithId("oldParentFolder");
		Folder folder2 = newFolderWithId("parentFolder");
		recordServices.execute(new Transaction().addAll(folder, folder2));

		recordServices.execute(new Transaction().addAll(
				newDigitalDocumentWithId("digitalDocument", folder),
				newDigitalDocumentWithId("anotherDigitalDocument", folder)));

		Document document = rm.getDocument("digitalDocument");
		document.setFolder(folder2);
		recordServices.update(document);
		waitForBatchProcess();

		folder = rm.getFolder("oldParentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);

		folder2 = rm.getFolder("parentFolder");
		assertThat(folder2.hasContent()).isTrue();
		assertThat(folder2.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder2.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenDigitalDocumentAndAnalogDocumentMovedThenDigitalOldParentFolderAndUnknownParentFolder()
			throws Exception {
		Folder folder = newFolderWithId("oldParentFolder");
		Folder folder2 = newFolderWithId("parentFolder");
		recordServices.execute(new Transaction().addAll(folder, folder2));

		recordServices.execute(new Transaction().addAll(
				newAnalogDocumentWithId("analogDocument", folder),
				newDigitalDocumentWithId("digitalDocument", folder)));
		waitForBatchProcess();

		Document document = rm.getDocument("analogDocument");
		document.setFolder(folder2);
		recordServices.update(document);
		waitForBatchProcess();

		folder = rm.getFolder("oldParentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);

		folder2 = rm.getFolder("parentFolder");
		assertThat(folder2.hasContent()).isFalse();
		assertThat(folder2.getMediumTypes()).isEmpty();
		assertThat(folder2.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);
	}

	@Test
	public void givenAnalogDocumentAndDigitalDocumentMovedThenUnknownOldParentFolderAndDigitalParentFolder()
			throws Exception {
		Folder folder = newFolderWithId("oldParentFolder");
		Folder folder2 = newFolderWithId("parentFolder");
		recordServices.execute(new Transaction().addAll(folder, folder2));

		recordServices.execute(new Transaction().addAll(
				newDigitalDocumentWithId("digitalDocument", folder),
				newAnalogDocumentWithId("analogDocument", folder)));

		Document document = rm.getDocument("digitalDocument");
		document.setFolder(folder2);
		recordServices.update(document);
		waitForBatchProcess();

		folder = rm.getFolder("oldParentFolder");
		assertThat(folder.hasContent()).isFalse();
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);

		folder2 = rm.getFolder("parentFolder");
		assertThat(folder2.hasContent()).isTrue();
		assertThat(folder2.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder2.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenAnalogDocumentAndAnalogDocumentMovedThenUnknownOldParentFolderAndUnknownParentFolder()
			throws Exception {
		Folder folder = newFolderWithId("oldParentFolder");
		Folder folder2 = newFolderWithId("parentFolder");
		recordServices.execute(new Transaction().addAll(folder, folder2));

		recordServices.execute(new Transaction().addAll(
				newAnalogDocumentWithId("analogDocument", folder),
				newAnalogDocumentWithId("analogDocument2", folder)));

		Document document = rm.getDocument("analogDocument");
		document.setFolder(folder2);

		folder = rm.getFolder("oldParentFolder");
		assertThat(folder.hasContent()).isFalse();
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);

		folder2 = rm.getFolder("parentFolder");
		assertThat(folder2.hasContent()).isFalse();
		assertThat(folder2.getMediumTypes()).isEmpty();
		assertThat(folder2.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);
	}

	@Test
	public void givenPaperParentFolderWithParentFolderAndDigitalDocumentCreatedThenHybridParentFolderWithDigitalParentFolder()
			throws Exception {
		Folder folder = newFolderWithId("grandParentFolder");
		Folder folder2 = newFolderWithId("parentFolder").setMediumTypes(paperMediumType.getId()).setParentFolder(folder);
		recordServices.execute(new Transaction().addAll(folder, folder2));
		waitForBatchProcess();

		Document document = newDigitalDocumentWithId("digitalDocument", folder2);
		recordServices.add(document);
		waitForBatchProcess();

		folder2 = rm.getFolder("parentFolder");
		assertThat(folder2.hasContent()).isTrue();
		assertThat(folder2.getMediumTypes()).containsOnly(digitalMediumType.getId(), paperMediumType.getId());
		assertThat(folder2.getMediaType()).isEqualTo(FolderMediaType.HYBRID);

		folder = rm.getFolder("grandParentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenPaperParentFolderWithParentFolderAndAnalogDocumentChangedToDigitalThenHybridParentFolderWithDigitalParentFolder()
			throws Exception {
		Folder folder = newFolderWithId("grandParentFolder");
		Folder folder2 = newFolderWithId("parentFolder").setMediumTypes(paperMediumType.getId()).setParentFolder(folder);
		Document document = newAnalogDocumentWithId("analogDocument", folder2);
		recordServices.execute(new Transaction().addAll(folder, folder2, document));

		recordServices.update(rm.getDocument("analogDocument").setContent(content));
		waitForBatchProcess();

		folder = rm.getFolder("grandParentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);

		folder2 = rm.getFolder("parentFolder");
		assertThat(folder2.hasContent()).isTrue();
		assertThat(folder2.getMediumTypes()).containsOnly(digitalMediumType.getId(), paperMediumType.getId());
		assertThat(folder2.getMediaType()).isEqualTo(FolderMediaType.HYBRID);
	}

	@Test
	public void givenHybridParentFolderWithDigitalParentFolderAndDigitalDocumentChangedToAnalogThenPaperParentFolderWithUnknownParentFolder()
			throws Exception {
		Folder folder = newFolderWithId("grandParentFolder");
		Folder folder2 = newFolderWithId("parentFolder").setMediumTypes(paperMediumType.getId()).setParentFolder(folder);
		Document document = newDigitalDocumentWithId("digitalDocument", folder2);
		recordServices.execute(new Transaction().addAll(folder, folder2, document));
		waitForBatchProcess();

		folder = rm.getFolder("grandParentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);

		folder2 = rm.getFolder("parentFolder");
		assertThat(folder2.hasContent()).isTrue();
		assertThat(folder2.getMediumTypes()).containsOnly(paperMediumType.getId(), digitalMediumType.getId());
		assertThat(folder2.getMediaType()).isEqualTo(FolderMediaType.HYBRID);

		recordServices.update(rm.getDocument("digitalDocument").setContent(null));
		waitForBatchProcess();

		folder = rm.getFolder("grandParentFolder");
		assertThat(folder.hasContent()).isFalse();
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);

		folder2 = rm.getFolder("parentFolder");
		assertThat(folder2.hasContent()).isFalse();
		assertThat(folder2.getMediumTypes()).containsOnly(paperMediumType.getId());
		assertThat(folder2.getMediaType()).isEqualTo(FolderMediaType.ANALOG);
	}

	@Test
	public void givenHybridParentFolderWithDigitalParentFolderAndDigitalDocumentDeletedThenPaperParentFolderWithUnknownParentFolder()
			throws Exception {
		Folder folder = newFolderWithId("grandParentFolder");
		Folder folder2 = newFolderWithId("parentFolder").setMediumTypes(paperMediumType.getId())
				.setParentFolder(folder);
		Document document = newDigitalDocumentWithId("digitalDocument", folder2);
		recordServices.execute(new Transaction().addAll(folder, folder2, document));
		waitForBatchProcess();

		folder = rm.getFolder("grandParentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);

		folder2 = rm.getFolder("parentFolder");
		assertThat(folder2.hasContent()).isTrue();
		assertThat(folder2.getMediumTypes()).containsOnly(paperMediumType.getId(), digitalMediumType.getId());
		assertThat(folder2.getMediaType()).isEqualTo(FolderMediaType.HYBRID);

		recordServices.logicallyDelete(rm.get("digitalDocument"), User.GOD);
		waitForBatchProcess();

		folder = rm.getFolder("grandParentFolder");
		assertThat(folder.hasContent()).isFalse();
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);

		folder2 = rm.getFolder("parentFolder");
		assertThat(folder2.hasContent()).isFalse();
		assertThat(folder2.getMediumTypes()).containsOnly(paperMediumType.getId());
		assertThat(folder2.getMediaType()).isEqualTo(FolderMediaType.ANALOG);
	}

	@Test
	public void givenPaperParentFolderWithParentFolderAndDigitalDocumentRestoredThenHybridParentFolderWithDigitalParentFolder()
			throws Exception {
		Folder folder = newFolderWithId("grandParentFolder");
		Folder folder2 = newFolderWithId("parentFolder").setMediumTypes(paperMediumType.getId()).setParentFolder(folder);
		Document document = newDigitalDocumentWithId("digitalDocument", folder2);
		recordServices.execute(new Transaction().addAll(folder, folder2, document));
		waitForBatchProcess();

		recordServices.logicallyDelete(rm.get("digitalDocument"), User.GOD);
		waitForBatchProcess();
		recordServices.restore(rm.get("digitalDocument"), User.GOD);
		waitForBatchProcess();

		folder = rm.getFolder("grandParentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);

		folder2 = rm.getFolder("parentFolder");
		assertThat(folder2.hasContent()).isTrue();
		assertThat(folder2.getMediumTypes()).containsOnly(digitalMediumType.getId(), paperMediumType.getId());
		assertThat(folder2.getMediaType()).isEqualTo(FolderMediaType.HYBRID);
	}

	@Test
	public void givenPaperParentFolderWithParentFolderAndDigitalDocumentMovedThenHybridParentFolderWithDigitalParentFolder()
			throws Exception {
		Folder folder = newFolderWithId("grandParentFolder");
		Folder folder2 = newFolderWithId("parentFolder").setMediumTypes(paperMediumType.getId()).setParentFolder(folder);
		Folder folder3 = newFolderWithId("otherFolder");
		Document document = newDigitalDocumentWithId("digitalDocument", folder3);
		recordServices.execute(new Transaction().addAll(folder, folder2, folder3, document));

		recordServices.update(rm.getDocument("digitalDocument").setFolder(folder2));
		waitForBatchProcess();

		folder = rm.getFolder("grandParentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);

		folder2 = rm.getFolder("parentFolder");
		assertThat(folder2.hasContent()).isTrue();
		assertThat(folder2.getMediumTypes()).containsOnly(digitalMediumType.getId(), paperMediumType.getId());
		assertThat(folder2.getMediaType()).isEqualTo(FolderMediaType.HYBRID);
	}

	//
	// Folder
	//

	@Test
	public void givenDigitalFolderCreatedThenUnknownParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolderWithId("folder").setParentFolder(folder).setMediumTypes(digitalMediumType.getId());
		recordServices.add(childFolder);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isFalse();
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);
	}

	@Test
	public void givenPaperFolderCreatedThenUnknownParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolderWithId("folder").setParentFolder(folder).setMediumTypes(paperMediumType.getId());
		recordServices.add(childFolder);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isFalse();
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);
	}

	@Test
	public void givenTwoActivatedDigitalMediumTypesThenBothMediumTypesPropagated() throws Exception {
		MediumType digitalMediumType2 = rm.newMediumTypeWithId("DM2").setCode("DM2").setTitle("title")
				.setAnalogical(false).setActivatedOnContent(true);
		recordServices.add(digitalMediumType2.getWrappedRecord());

		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Document document = newDigitalDocumentWithId("digitalDocument", folder);
		recordServices.add(document);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).contains(digitalMediumType.getId(), digitalMediumType2.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenInactivatedDigitalMediumTypeAndActivationThenCorrectStateAfterSystemReindexation()
			throws Exception {
		recordServices.update(digitalMediumType.setActivatedOnContent(false));

		Folder folder = newFolderWithId("folder");
		Document document = newDigitalDocumentWithId("document", folder);
		Folder folder2 = newFolderWithId("folder2").setMediumTypes(digitalMediumType.getId());
		recordServices.execute(new Transaction().addAll(folder, document, folder2));

		assertThat(rm.getFolder("folder").getMediumTypes()).isEmpty();
		assertThat(rm.getFolder("folder2").getMediumTypes()).containsOnly(digitalMediumType.getId());

		recordServices.update(digitalMediumType.setActivatedOnContent(true));

		getModelLayerFactory().newReindexingServices().createLockFile();
		getModelLayerFactory().newReindexingServices().reindexCollections(ReindexationMode.RECALCULATE);
		getModelLayerFactory().newReindexingServices().removeLockFile();
		waitForBatchProcess();

		folder = rm.getFolder("folder");
		assertThat(folder.hasContent()).isTrue();
		assertThat(folder.getMediumTypes()).contains(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);

		folder2 = rm.getFolder("folder2");
		assertThat(folder2.hasContent()).isFalse();
		assertThat(folder2.getMediumTypes()).isEmpty();
		assertThat(folder2.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);
	}

	@Test
	public void givenInactivatedDigitalMediumTypeAndActivationThenNotUpdatedAfterReindexationWithoutLockFile()
			throws Exception {
		recordServices.update(digitalMediumType.setActivatedOnContent(false));

		Folder folder2 = newFolderWithId("folder2").setMediumTypes(digitalMediumType.getId());
		recordServices.execute(new Transaction().addAll(folder2));

		folder2 = rm.getFolder("folder2");
		assertThat(folder2.hasContent()).isFalse();
		assertThat(folder2.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder2.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);

		recordServices.update(digitalMediumType.setActivatedOnContent(true));

		getModelLayerFactory().newReindexingServices().reindexCollections(ReindexationMode.RECALCULATE);
		waitForBatchProcess();

		folder2 = rm.getFolder("folder2");
		assertThat(folder2.hasContent()).isFalse();
		assertThat(folder2.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder2.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	private Folder newFolderWithId(String id) {
		return rm.newFolderWithId(id).setAdministrativeUnitEntered(records.unitId_10a)
				.setCategoryEntered(records.categoryId_X13).setRetentionRuleEntered(records.ruleId_1)
				.setTitle("Title").setOpenDate(new LocalDate()).setCopyStatusEntered(CopyType.PRINCIPAL);
	}

	private Folder newFolder(String id, Folder parentFolder, MediumType... mediumTypes) {
		return newFolderWithId(id).setParentFolder(parentFolder).setMediumTypes(asList(mediumTypes));
	}

	private Document newDigitalDocumentWithId(String id, Folder folder) {
		return rm.newDocumentWithId(id).setTitle("title").setFolder(folder).setContent(content);
	}

	private Document newAnalogDocumentWithId(String id, Folder folder) {
		return rm.newDocumentWithId(id).setTitle("title").setFolder(folder);
	}
}
