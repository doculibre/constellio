package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.FolderMediaType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

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
		givenBackgroundThreadsEnabled();
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus());

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

		digitalMediumType = rm.getMediumTypeByCode("DM");
		paperMediumType = rm.getMediumTypeByCode("PA");

		File file = newTempFileWithContent("test.txt", "This is a test");
		ContentManager contentManager = getModelLayerFactory().getContentManager();
		ContentVersionDataSummary data = contentManager.upload(file);
		content = contentManager.createMajor(users.adminIn(zeCollection), "test.txt", data);
	}

	// TODO for each type of event, test with parent folder with existing medium types
	// FIXME some tests fail sometimes, figure out why

	@Test
	public void whenCheckingIfMediumTypeLogicallyDeletableThenFalse() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTestUsers());
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Record mediumType = rm.getMediumTypeByCode("DM").getWrappedRecord();
		assertThat(recordServices.isLogicallyDeletable(mediumType, User.GOD)).isFalse();

		mediumType = rm.getMediumTypeByCode("FI").getWrappedRecord();
		assertThat(recordServices.isLogicallyDeletable(mediumType, User.GOD)).isTrue();
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
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);

		folder2 = rm.getFolder("parentFolder");
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
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);

		folder2 = rm.getFolder("parentFolder");
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
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);

		folder2 = rm.getFolder("parentFolder");
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

		Document document = rm.getDocument("analogDocument");
		document.setFolder(folder2);

		folder = rm.getFolder("oldParentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);

		folder2 = rm.getFolder("parentFolder");
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
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);

		folder2 = rm.getFolder("parentFolder");
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
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);

		folder2 = rm.getFolder("parentFolder");
		assertThat(folder2.getMediumTypes()).isEmpty();
		assertThat(folder2.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);
	}

	// TODO test hybrid moved

	//
	// Folder
	//

	@Test
	public void givenDigitalFolderCreatedThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolderWithId("folder").setParentFolder(folder).setMediumTypes(digitalMediumType.getId());
		recordServices.add(childFolder);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenPaperFolderCreatedThenAnalogParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolderWithId("folder").setParentFolder(folder).setMediumTypes(paperMediumType.getId());
		recordServices.add(childFolder);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(paperMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ANALOG);
	}

	@Test
	public void givenHybridFolderCreatedThenHybridParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, paperMediumType, digitalMediumType);
		recordServices.add(childFolder);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(paperMediumType.getId(), digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.HYBRID);
	}

	@Test
	public void givenDigitalFolderAndDigitalFolderCreatedThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolderWithId("folder").setParentFolder(folder).setMediumTypes(digitalMediumType.getId());
		recordServices.add(childFolder);
		Folder childFolder2 = newFolderWithId("folder2").setParentFolder(folder).setMediumTypes(digitalMediumType.getId());
		recordServices.add(childFolder2);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenPaperFolderAndDigitalFolderCreatedThenHybridParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, paperMediumType);
		recordServices.add(childFolder);
		Folder childFolder2 = newFolder("folder2", folder, digitalMediumType);
		recordServices.add(childFolder2);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId(), paperMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.HYBRID);
	}

	@Test
	public void givenDigitalFolderAndPaperFolderCreatedThenHybridParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, digitalMediumType);
		recordServices.add(childFolder);
		Folder childFolder2 = newFolder("folder2", folder, paperMediumType);
		recordServices.add(childFolder2);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId(), paperMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.HYBRID);
	}

	@Test
	public void givenDigitalFolderAndHybridFolderCreatedThenHybridParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, digitalMediumType);
		recordServices.add(childFolder);
		Folder childFolder2 = newFolder("folder2", folder, paperMediumType, digitalMediumType);
		recordServices.add(childFolder2);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId(), paperMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.HYBRID);
	}

	@Test
	public void givenPaperFolderAndHybridFolderCreatedThenHybridParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, paperMediumType);
		recordServices.add(childFolder);
		Folder childFolder2 = newFolder("folder2", folder, paperMediumType, digitalMediumType);
		recordServices.add(childFolder2);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId(), paperMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.HYBRID);
	}

	@Test
	public void givenPaperFolderChangedToDigitalThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, paperMediumType);
		recordServices.add(childFolder);

		childFolder = rm.getFolder("folder");
		childFolder.setMediumTypes(Collections.singletonList(digitalMediumType.getId()));
		recordServices.update(childFolder);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenDigitalFolderChangedToPaperThenAnalogParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, digitalMediumType);
		recordServices.add(childFolder);

		childFolder = rm.getFolder("folder");
		childFolder.setMediumTypes(Collections.singletonList(paperMediumType.getId()));
		recordServices.update(childFolder);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(paperMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ANALOG);
	}

	@Test
	public void givenDigitalFolderChangedToHybridThenHybridParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, digitalMediumType);
		recordServices.add(childFolder);

		childFolder = rm.getFolder("folder");
		childFolder.setMediumTypes(asList(paperMediumType.getId(), digitalMediumType.getId()));
		recordServices.update(childFolder);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(paperMediumType.getId(), digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.HYBRID);
	}

	@Test
	public void givenDigitalFolderAndDigitalFolderChangedToPaperThenHybridParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, digitalMediumType);
		Folder childFolder2 = newFolder("folder2", folder, digitalMediumType);
		recordServices.execute(new Transaction().addAll(childFolder, childFolder2));

		childFolder = rm.getFolder("folder");
		childFolder.setMediumTypes(Collections.singletonList(paperMediumType.getId()));
		recordServices.update(childFolder);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId(), paperMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.HYBRID);
	}

	@Test
	public void givenPaperFolderAndPaperFolderChangedToDigitalThenHybridParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, paperMediumType);
		Folder childFolder2 = newFolder("folder2", folder, paperMediumType);
		recordServices.execute(new Transaction().addAll(childFolder, childFolder2));

		childFolder = rm.getFolder("folder");
		childFolder.setMediumTypes(Collections.singletonList(digitalMediumType.getId()));
		recordServices.update(childFolder);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId(), paperMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.HYBRID);
	}

	@Test
	public void givenDigitalFolderAndPaperFolderChangedToDigitalThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, paperMediumType);
		Folder childFolder2 = newFolder("folder2", folder, digitalMediumType);
		recordServices.execute(new Transaction().addAll(childFolder, childFolder2));

		childFolder = rm.getFolder("folder");
		childFolder.setMediumTypes(Collections.singletonList(digitalMediumType.getId()));
		recordServices.update(childFolder);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenPaperFolderAndDigitalFolderChangedToPaperThenAnalogParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, digitalMediumType);
		Folder childFolder2 = newFolder("folder2", folder, paperMediumType);
		recordServices.execute(new Transaction().addAll(childFolder, childFolder2));

		childFolder = rm.getFolder("folder");
		childFolder.setMediumTypes(Collections.singletonList(paperMediumType.getId()));
		recordServices.update(childFolder);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(paperMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ANALOG);
	}

	@Test
	public void givenDigitalFolderDeletedThenUnknownParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, digitalMediumType);
		recordServices.add(childFolder);
		recordServices.logicallyDelete(rm.get("folder"), User.GOD);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);
	}

	@Test
	public void givenPaperFolderDeletedThenUnknownParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, paperMediumType);
		recordServices.add(childFolder);
		recordServices.logicallyDelete(rm.get("folder"), User.GOD);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);
	}

	@Test
	public void givenPaperFolderAndPaperFolderDeletedThenAnalogParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, paperMediumType);
		Folder childFolder2 = newFolder("anotherFolder", folder, paperMediumType);
		recordServices.execute(new Transaction().addAll(childFolder, childFolder2));
		recordServices.logicallyDelete(rm.get("folder"), User.GOD);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(paperMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ANALOG);
	}

	@Test
	public void givenDigitalFolderAndDigitalFolderDeletedThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, digitalMediumType);
		Folder childFolder2 = newFolder("anotherFolder", folder, digitalMediumType);
		recordServices.execute(new Transaction().addAll(childFolder, childFolder2));
		recordServices.logicallyDelete(rm.get("folder"), User.GOD);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenDigitalFolderAndAnalogFolderDeletedThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, paperMediumType);
		Folder childFolder2 = newFolder("anotherFolder", folder, digitalMediumType);
		recordServices.execute(new Transaction().addAll(childFolder, childFolder2));
		recordServices.logicallyDelete(rm.get("folder"), User.GOD);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenPaperFolderAndDigitalFolderDeletedThenAnalogParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, digitalMediumType);
		Folder childFolder2 = newFolder("anotherFolder", folder, paperMediumType);
		recordServices.execute(new Transaction().addAll(childFolder, childFolder2));
		recordServices.logicallyDelete(rm.get("folder"), User.GOD);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(paperMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ANALOG);
	}

	@Test
	public void givenDigitalFolderRestoredThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, digitalMediumType);
		recordServices.add(childFolder);
		recordServices.logicallyDelete(rm.get("folder"), User.GOD);
		recordServices.restore(rm.get("folder"), User.GOD);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenAnalogFolderRestoredThenAnalogParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, paperMediumType);
		recordServices.add(childFolder);
		recordServices.logicallyDelete(rm.get("folder"), User.GOD);
		recordServices.restore(rm.get("folder"), User.GOD);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(paperMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ANALOG);
	}

	@Test
	public void givenHybridFolderRestoredThenHybridParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, paperMediumType, digitalMediumType);
		recordServices.add(childFolder);
		recordServices.logicallyDelete(rm.get("folder"), User.GOD);
		recordServices.restore(rm.get("folder"), User.GOD);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(paperMediumType.getId(), digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.HYBRID);
	}

	@Test
	public void givenDigitalFolderAndAnalogFolderRestoredThenHybridParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, paperMediumType);
		Folder childFolder2 = newFolder("folder2", folder, digitalMediumType);
		recordServices.execute(new Transaction().addAll(childFolder, childFolder2));
		recordServices.logicallyDelete(rm.get("folder"), User.GOD);
		recordServices.restore(rm.get("folder"), User.GOD);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(paperMediumType.getId(), digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.HYBRID);
	}

	@Test
	public void givenDigitalFolderAndDigitalFolderRestoredThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, digitalMediumType);
		Folder childFolder2 = newFolder("folder2", folder, digitalMediumType);
		recordServices.execute(new Transaction().addAll(childFolder, childFolder2));
		recordServices.logicallyDelete(rm.get("folder"), User.GOD);
		recordServices.restore(rm.get("folder"), User.GOD);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenAnalogFolderAndDigitalFolderRestoredThenHybridParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, digitalMediumType);
		Folder childFolder2 = newFolder("folder2", folder, paperMediumType);
		recordServices.execute(new Transaction().addAll(childFolder, childFolder2));
		recordServices.logicallyDelete(rm.get("folder"), User.GOD);
		recordServices.restore(rm.get("folder"), User.GOD);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId(), paperMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.HYBRID);
	}

	@Test
	public void givenAnalogFolderAndAnalogFolderRestoredThenAnalogParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, paperMediumType);
		Folder childFolder2 = newFolder("folder2", folder, paperMediumType);
		recordServices.execute(new Transaction().addAll(childFolder, childFolder2));
		recordServices.logicallyDelete(rm.get("folder"), User.GOD);
		recordServices.restore(rm.get("folder"), User.GOD);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(paperMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ANALOG);
	}

	@Test
	public void givenDigitalFolderAndHybridFolderRestoredThenHybridParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, digitalMediumType, paperMediumType);
		Folder childFolder2 = newFolder("folder2", folder, digitalMediumType);
		recordServices.execute(new Transaction().addAll(childFolder, childFolder2));
		recordServices.logicallyDelete(rm.get("folder"), User.GOD);
		recordServices.restore(rm.get("folder"), User.GOD);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(paperMediumType.getId(), digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.HYBRID);
	}

	@Test
	public void givenAnalogFolderAndHybridFolderRestoredThenHybridParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", folder, digitalMediumType, paperMediumType);
		Folder childFolder2 = newFolder("folder2", folder, paperMediumType);
		recordServices.execute(new Transaction().addAll(childFolder, childFolder2));
		recordServices.logicallyDelete(rm.get("folder"), User.GOD);
		recordServices.restore(rm.get("folder"), User.GOD);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(paperMediumType.getId(), digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.HYBRID);
	}

	@Test
	public void givenAnalogFolderMovedThenUnknownOldParentFolderAndAnalogParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		Folder oldFolder = newFolderWithId("oldParentFolder");
		recordServices.execute(new Transaction().addAll(folder, oldFolder));

		Folder childFolder = newFolder("folder", oldFolder, paperMediumType);
		recordServices.add(childFolder);

		childFolder = rm.getFolder("folder");
		childFolder.setParentFolder(folder);
		recordServices.update(childFolder);
		waitForBatchProcess();

		oldFolder = rm.getFolder("oldParentFolder");
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(paperMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ANALOG);
	}

	@Test
	public void givenDigitalFolderMovedThenUnknownOldParentFolderAndDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		Folder oldFolder = newFolderWithId("oldParentFolder");
		recordServices.execute(new Transaction().addAll(folder, oldFolder));

		Folder childFolder = newFolder("folder", oldFolder, digitalMediumType);
		recordServices.add(childFolder);

		childFolder = rm.getFolder("folder");
		childFolder.setParentFolder(folder);
		recordServices.update(childFolder);
		waitForBatchProcess();

		oldFolder = rm.getFolder("oldParentFolder");
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenAnalogFolderAndPaperFolderMovedThenAnalogOldParentFolderAndAnalogParentFolder()
			throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		Folder oldFolder = newFolderWithId("oldParentFolder");
		recordServices.execute(new Transaction().addAll(folder, oldFolder));

		Folder childFolder = newFolder("folder", oldFolder, paperMediumType);
		Folder childFolder2 = newFolder("folder2", oldFolder, paperMediumType);
		recordServices.execute(new Transaction().addAll(childFolder, childFolder2));

		childFolder = rm.getFolder("folder");
		childFolder.setParentFolder(folder);
		recordServices.update(childFolder);
		waitForBatchProcess();

		oldFolder = rm.getFolder("oldParentFolder");
		assertThat(oldFolder.getMediumTypes()).containsOnly(paperMediumType.getId());
		assertThat(oldFolder.getMediaType()).isEqualTo(FolderMediaType.ANALOG);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(paperMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ANALOG);
	}

	@Test
	public void givenDigitalFolderAndDigitalFolderMovedThenDigitalOldParentFolderAndDigitalParentFolder()
			throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		Folder oldFolder = newFolderWithId("oldParentFolder");
		recordServices.execute(new Transaction().addAll(folder, oldFolder));

		Folder childFolder = newFolder("folder", oldFolder, digitalMediumType);
		Folder childFolder2 = newFolder("folder2", oldFolder, digitalMediumType);
		recordServices.execute(new Transaction().addAll(childFolder, childFolder2));

		childFolder = rm.getFolder("folder");
		childFolder.setParentFolder(folder);
		recordServices.update(childFolder);
		waitForBatchProcess();

		oldFolder = rm.getFolder("oldParentFolder");
		assertThat(oldFolder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(oldFolder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenAnalogFolderAndDigitalFolderMovedThenAnalogOldParentFolderAndDigitalParentFolder()
			throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		Folder oldFolder = newFolderWithId("oldParentFolder");
		recordServices.execute(new Transaction().addAll(folder, oldFolder));

		Folder childFolder = newFolder("folder", oldFolder, digitalMediumType);
		Folder childFolder2 = newFolder("folder2", oldFolder, paperMediumType);
		recordServices.execute(new Transaction().addAll(childFolder, childFolder2));

		childFolder = rm.getFolder("folder");
		childFolder.setParentFolder(folder);
		recordServices.update(childFolder);
		waitForBatchProcess();

		oldFolder = rm.getFolder("oldParentFolder");
		assertThat(oldFolder.getMediumTypes()).containsOnly(paperMediumType.getId());
		assertThat(oldFolder.getMediaType()).isEqualTo(FolderMediaType.ANALOG);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenDigitalFolderAndAnologFolderMovedThenDigitalOldParentFolderAndAnalogParentFolder()
			throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		Folder oldFolder = newFolderWithId("oldParentFolder");
		recordServices.execute(new Transaction().addAll(folder, oldFolder));

		Folder childFolder = newFolder("folder", oldFolder, paperMediumType);
		Folder childFolder2 = newFolder("folder2", oldFolder, digitalMediumType);
		recordServices.execute(new Transaction().addAll(childFolder, childFolder2));

		childFolder = rm.getFolder("folder");
		childFolder.setParentFolder(folder);
		recordServices.update(childFolder);
		waitForBatchProcess();

		oldFolder = rm.getFolder("oldParentFolder");
		assertThat(oldFolder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(oldFolder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(paperMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ANALOG);
	}

	@Test
	public void givenDigitalFolderMovedToAnalogParentFolderThenHybridParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder").setMediumTypes(paperMediumType.getId());
		recordServices.add(folder);

		Folder childFolder = newFolder("folder", null, digitalMediumType);
		recordServices.add(childFolder);

		childFolder = rm.getFolder("folder");
		childFolder.setParentFolder(folder);
		recordServices.update(childFolder);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(paperMediumType.getId(), digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.HYBRID);
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
