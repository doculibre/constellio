package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.FolderMediaType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.model.entities.records.Content;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class RMRecordMediumTypeExtensionAcceptanceTest extends ConstellioTest {

	private Users users = new Users();
	private RMTestRecords records = new RMTestRecords(zeCollection);
	private RMSchemasRecordsServices rm;
	private RecordServices recordServices;

	private Content content;
	private MediumType digitalMediumType;

	@Before
	public void setup() throws Exception {
		givenBackgroundThreadsEnabled();
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus());

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

		digitalMediumType = rm.getMediumTypeByCode("DM");

		File file = newTempFileWithContent("test.txt", "This is a test");
		ContentManager contentManager = getModelLayerFactory().getContentManager();
		ContentVersionDataSummary data = contentManager.upload(file);
		content = contentManager.createMajor(users.adminIn(zeCollection), "test.txt", data);
	}

	@Test
	public void givenDigitalDocumentCreatedThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Document document = newDigitalDocumentWithId("digitalDocument").setFolder(folder);
		recordServices.add(document);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).contains(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenAnalogDocumentCreatedThenUnknownParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		Document document = newAnalogDocumentWithId("analogDocument").setFolder(folder);
		recordServices.add(document);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);
	}

	@Test
	public void givenDigitalDocumentAndAnalogDocumentCreatedThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newDigitalDocumentWithId("digitalDocument").setFolder(folder));

		Document document = newAnalogDocumentWithId("analogDocument").setFolder(folder);
		recordServices.add(document);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenAnalogDocumentAndDigitalDocumentCreatedThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newAnalogDocumentWithId("analogDocument").setFolder(folder));

		Document document = newDigitalDocumentWithId("digitalDocument").setFolder(folder);
		recordServices.add(document);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenDigitalDocumentAndDigitalDocumentCreatedThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newDigitalDocumentWithId("digitalDocument").setFolder(folder));

		Document document = newDigitalDocumentWithId("digitalDocument2").setFolder(folder);
		recordServices.add(document);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenAnalogDocumentChangedToDigitalThenDigitalParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newAnalogDocumentWithId("analogDocument").setFolder(folder));

		Document document = rm.getDocument("analogDocument");
		document.setContent(content);
		recordServices.update(document);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenDigitalDocumentChangedToAnalogThenUnknownParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newDigitalDocumentWithId("digitalDocument").setFolder(folder));

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

		recordServices.add(newAnalogDocumentWithId("analogDocument").setFolder(folder));
		recordServices.add(newAnalogDocumentWithId("analogDocument1").setFolder(folder));

		Document document = rm.getDocument("analogDocument");
		document.setContent(content);
		recordServices.update(document);

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).containsOnly(digitalMediumType.getId());
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.ELECTRONIC);
	}

	@Test
	public void givenDigitalDocumentAndDigitalDocumentChangedToAnalogThenUnknownParentFolder() throws Exception {
		Folder folder = newFolderWithId("parentFolder");
		recordServices.add(folder);

		recordServices.add(newDigitalDocumentWithId("digitalDocument").setFolder(folder));
		recordServices.add(newDigitalDocumentWithId("digitalDocument1").setFolder(folder));

		Document document = rm.getDocument("digitalDocument");
		document.setContent(null);
		recordServices.update(document);
		waitForBatchProcess();

		folder = rm.getFolder("parentFolder");
		assertThat(folder.getMediumTypes()).isEmpty();
		assertThat(folder.getMediaType()).isEqualTo(FolderMediaType.UNKNOWN);
	}

	public void givenDigitalDocumentDeletedThenParentFolderHasNoSupportTypes() {

	}

	public void givenAnalogDocumentDeletedThenParentFolderHasNoSupportTypes() {

	}

	public void givenAnalogDocumentAndDigitalDocumentDeletedThenAnalogParentFolder() {

	}

	public void givenDigitalDocumentAndAnalogDocumentDeletedThenDigitalParentFolder() {

	}

	public void givenDigitalDocumentAndDigitalDocumentDeletedThenDigitalParentFolder() {

	}

	public void givenAnalogDocumentAndAnalogDocumentDeletedThenAnalogParentFolder() {

	}

	private Folder newFolderWithId(String id) {
		return rm.newFolderWithId(id).setAdministrativeUnitEntered(records.unitId_10a)
				.setCategoryEntered(records.categoryId_X13).setRetentionRuleEntered(records.ruleId_1)
				.setTitle("Title").setOpenDate(new LocalDate()).setCopyStatusEntered(CopyType.PRINCIPAL);
	}

	private Document newDigitalDocumentWithId(String id) {
		return rm.newDocumentWithId(id).setTitle("title").setContent(content);
	}

	private Document newAnalogDocumentWithId(String id) {
		return rm.newDocumentWithId(id).setTitle("title");
	}

}
