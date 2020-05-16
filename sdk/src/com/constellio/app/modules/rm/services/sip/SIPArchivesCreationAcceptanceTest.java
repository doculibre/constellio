package com.constellio.app.modules.rm.services.sip;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.sip.bagInfo.DefaultSIPZipBagInfoFactory;
import com.constellio.app.services.sip.bagInfo.SIPZipBagInfoFactory;
import com.constellio.app.services.sip.mets.MetsContentFileReference;
import com.constellio.app.services.sip.mets.MetsDivisionInfo;
import com.constellio.app.services.sip.mets.MetsEADMetadataReference;
import com.constellio.app.services.sip.record.UnclassifiedDataSIPWriter;
import com.constellio.app.services.sip.zip.AutoSplittedSIPZipWriter;
import com.constellio.app.services.sip.zip.DefaultSIPFileNameProvider;
import com.constellio.app.services.sip.zip.FileSIPZipWriter;
import com.constellio.app.services.sip.zip.SIPFileHasher;
import com.constellio.app.services.sip.zip.SIPFileNameProvider;
import com.constellio.app.services.sip.zip.SIPZipWriter;
import com.constellio.data.dao.services.idGenerator.InMemorySequentialGenerator;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.Provider;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.enums.ParsingBehavior;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.contents.ContentImpl;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.event.EventTestUtil;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.utils.RecordCodeComparator;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils;
import com.constellio.sdk.tests.annotations.SlowTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.constellio.app.modules.rm.model.enums.DecommissioningType.DEPOSIT;
import static com.constellio.app.modules.rm.model.enums.DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE;
import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.atomicSet;
import static com.constellio.sdk.tests.TestUtils.zipFileWithSameContentExceptingFiles;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Locale.FRENCH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

public class SIPArchivesCreationAcceptanceTest extends ConstellioTest {

	public static final String DATE_1 = "10/01/2000 12:00:00";
	private RMTestRecords records = new RMTestRecords(zeCollection);
	private Users users = new Users();
	private RMSchemasRecordsServices rm;
	private IOServices ioServices;
	private RMSelectedFoldersAndDocumentsSIPBuilder constellioSIP;
	private RMSchemasRecordsServices rmSchemasRecordsServices;
	private Predicate<Metadata> metadataIgnore;

	@Before
	public void setUp() throws Exception {

		records.copyBuilder = new CopyRetentionRuleBuilder(new InMemorySequentialGenerator());

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records),
				withCollection("otherCollection").withConstellioRMModule().withAllTest(users));
		this.rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		givenConfig(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS);
		givenTimeIs(new LocalDateTime(2018, 1, 2, 3, 4, 5));

		Transaction tx = new Transaction();

		tx.add(records.getRule1().setCopyRetentionRules(records.getRule1().getCopyRetentionRules()));
		tx.add(records.getRule2().setCopyRetentionRules(records.getRule1().getCopyRetentionRules()));
		tx.add(records.getRule3().setCopyRetentionRules(records.getRule1().getCopyRetentionRules()));
		tx.add(records.getRule4().setCopyRetentionRules(records.getRule1().getCopyRetentionRules()));
		tx.add(records.getRule5().setCopyRetentionRules(records.getRule1().getCopyRetentionRules()));

		rm.executeTransaction(tx);
		ioServices = getModelLayerFactory().getIOServicesFactory().newIOServices();
		constellioSIP = new RMSelectedFoldersAndDocumentsSIPBuilder(zeCollection, getAppLayerFactory());
		rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		ignoreMetadatasWithLocalCode(asList("isCheckoutAlertSent", "markedForPreviewConversion"));
	}

	@Test
	public void givenSIPArchivesOfTwoDocumentsInSameFolderThenArchiveContainsAllMetadatasContentsAndManifests()
			throws Exception {

		Transaction tx = new Transaction();
		tx.add(rm.newFolderWithId("zeFolderId").setOpenDate(new LocalDate(2018, 1, 1))
				.setTitle("Ze folder")
				.setAdministrativeUnitEntered(records.unitId_10a).setCategoryEntered(records.categoryId_X13)
				.setRetentionRuleEntered(records.ruleId_1));

		tx.add(rm.newDocumentWithId("document1").setTitle("Document 1").setFolder("zeFolderId")
				.setContent(majorContent("content1.doc")));

		tx.add(rm.newDocumentWithId("document2").setTitle("Document 2").setFolder("zeFolderId"))
				.setContent(minorContent("content2.doc"));

		rm.executeTransaction(tx);

		File sipFile = buildSIPWithDocuments("document1", "document2");
		System.out.println(sipFile.getAbsolutePath());

		assertThat(sipFile).is(zipFileWithSameContentExceptingFiles(getTestResourceFile("sip1.zip"), "bag-info.txt"));

	}


	@Test
	public void givenSIPArchivesOfAnEmailThenAttachementsExtractedInSIP()
			throws Exception {

		Transaction tx = new Transaction();
		tx.add(rm.newFolderWithId("zeFolderId").setOpenDate(new LocalDate(2018, 1, 1))
				.setTitle("Ze folder")
				.setAdministrativeUnitEntered(records.unitId_10a).setCategoryEntered(records.categoryId_X13)
				.setRetentionRuleEntered(records.ruleId_1));

		tx.add(rm.newEmailWithId("theEmailId").setTitle("My important email").setFolder("zeFolderId"))
				.setContent(minorContent("testFile.msg"));


		rm.executeTransaction(tx);

		File sipFile = buildSIPWithDocuments("theEmailId");
		System.out.println(sipFile.getAbsolutePath());

		assertThat(sipFile).is(zipFileWithSameContentExceptingFiles(getTestResourceFile("sip2.zip"), "bag-info.txt"));

	}

	@Test
	public void whenExportingCollectionFoldersAndDocumentsThenAllExported()
			throws Exception {
		cacheIntegrityCheckedAfterTest = false;

		Transaction tx = new Transaction();

		int folderIndex = 0;
		for (Category category : rm.getAllCategories().stream().sorted(
				new RecordCodeComparator(emptyList())).collect(Collectors.toList())) {

			String folderId = "folder" + ++folderIndex;
			tx.add(rm.newFolderWithId(folderId).setOpenDate(new LocalDate(2018, 1, 1))
					.setTitle("Folder in category " + category)
					.setAdministrativeUnitEntered(records.unitId_10a).setCategoryEntered(category)
					.setRetentionRuleEntered(records.ruleId_1));

			tx.add(rm.newDocumentWithId(folderId + "_document1").setTitle("Document 1").setFolder(folderId)
					.setContent(majorContent("content1.doc")));

			tx.add(rm.newDocumentWithId(folderId + "_document2").setTitle("Document 2").setFolder(folderId))
					.setContent(minorContent("content2.doc"));

		}
		rm.executeTransaction(tx);
		createAnInvalidFolder666();
		movefolder1document2InAnotherCollection();

		File tempFolder = newTempFolder();
		RMCollectionExportSIPBuilder builder = new RMCollectionExportSIPBuilder(zeCollection, getAppLayerFactory(), tempFolder);

		builder.exportAllFoldersAndDocuments(new ProgressInfo(), new Provider<String, Boolean>() {
			@Override
			public Boolean get(String input) {
				return true;
			}
		});

		assertThat(tempFolder.list()).containsOnly("info", "foldersAndDocuments-001.zip");

		assertThat(new File(tempFolder, "info").list())
				.containsOnly("failedFolderExport.txt", "exportedFolders.txt", "exportedDocuments.txt");

		assertThat(contentOf(new File(tempFolder, "info" + File.separator + "failedFolderExport.txt")))
				.isEqualTo("folder666, folder666_document1, folder666_document2");

		assertThat(contentOf(new File(tempFolder, "info" + File.separator + "exportedFolders.txt")))
				.contains("folder12,").doesNotContain("folder12_document1,");

		assertThat(contentOf(new File(tempFolder, "info" + File.separator + "exportedDocuments.txt")))
				.doesNotContain("folder12,").contains("folder12_document1,");

		TestUtils.assertFilesInZip(new File(tempFolder, "foldersAndDocuments-001.zip")).contains(
				"bag-info.txt",
				"bagit.txt",
				"data/category-X/category-X100/category-X110/folder-folder4.xml",
				"data/category-X/category-X100/category-X110/folder-folder4/document-folder4_document1.xml",
				"data/category-X/category-X100/category-X110/folder-folder4/document-folder4_document2.xml",
				"data/category-X/category-X100/category-X120/folder-folder5.xml",
				"data/category-X/category-X100/category-X120/folder-folder5/document-folder5_document1.xml",
				"data/category-X/category-X100/category-X120/folder-folder5/document-folder5_document2.xml",
				"data/category-X/category-X100/folder-folder3.xml",
				"data/category-X/category-X100/folder-folder3/document-folder3_document1.xml",
				"data/category-X/category-X100/folder-folder3/document-folder3_document2.xml",
				"data/category-X/category-X13/folder-folder2.xml",
				"data/category-X/category-X13/folder-folder2/document-folder2_document1.xml",
				"data/category-X/category-X13/folder-folder2/document-folder2_document2.xml",
				"data/category-X/folder-folder1.xml",
				"data/category-X/folder-folder1/document-folder1_document1.xml",
				"data/category-X/folder-folder1/document-folder1_document2.xml",
				"data/category-Z/category-Z100/category-Z110/category-Z111/folder-folder9.xml",
				"data/category-Z/category-Z100/category-Z110/category-Z111/folder-folder9/document-folder9_document1.xml",
				"data/category-Z/category-Z100/category-Z110/category-Z111/folder-folder9/document-folder9_document2.xml",
				"data/category-Z/category-Z100/category-Z110/category-Z112/folder-folder10.xml",
				"data/category-Z/category-Z100/category-Z110/category-Z112/folder-folder10/document-folder10_document1.xml",
				"data/category-Z/category-Z100/category-Z110/category-Z112/folder-folder10/document-folder10_document2.xml",
				"data/category-Z/category-Z100/category-Z110/folder-folder8.xml",
				"data/category-Z/category-Z100/category-Z110/folder-folder8/document-folder8_document1.xml",
				"data/category-Z/category-Z100/category-Z110/folder-folder8/document-folder8_document2.xml",
				"data/category-Z/category-Z100/category-Z120/folder-folder11.xml",
				"data/category-Z/category-Z100/category-Z120/folder-folder11/document-folder11_document1.xml",
				"data/category-Z/category-Z100/category-Z120/folder-folder11/document-folder11_document2.xml",
				"data/category-Z/category-Z100/folder-folder7.xml",
				"data/category-Z/category-Z100/folder-folder7/document-folder7_document1.xml",
				"data/category-Z/category-Z100/folder-folder7/document-folder7_document2.xml",
				"data/category-Z/category-Z200/folder-folder12.xml",
				"data/category-Z/category-Z200/folder-folder12/document-folder12_document1.xml",
				"data/category-Z/category-Z200/folder-folder12/document-folder12_document2.xml",
				"data/category-Z/category-Z999/folder-folder13.xml",
				"data/category-Z/category-Z999/folder-folder13/document-folder13_document1.xml",
				"data/category-Z/category-Z999/folder-folder13/document-folder13_document2.xml",
				"data/category-Z/category-ZE42/folder-folder14.xml",
				"data/category-Z/category-ZE42/folder-folder14/document-folder14_document1.xml",
				"data/category-Z/category-ZE42/folder-folder14/document-folder14_document2.xml",
				"data/category-Z/folder-folder6.xml",
				"data/category-Z/folder-folder6/document-folder6_document1.xml",
				"data/category-Z/folder-folder6/document-folder6_document2.xml",
				"foldersAndDocuments-001.xml",
				"manifest-sha256.txt",
				"tagmanifest-sha256.txt");

	}

	private void createADocumentInAnInvalidFolder() throws Exception {
		getModelLayerFactory().newRecordServices().add(rm.newDocumentWithId("documentWithInexistingFolder")
				.setTitle("Document 2").setFolder("folder1")
				.setContent(minorContent("content2.doc")));

		SolrClient solrClient = getDataLayerFactory().getRecordsVaultServer().getNestedSolrServer();
		SolrInputDocument doc = new SolrInputDocument();
		doc.setField("id", "documentWithInexistingFolder");
		doc.setField("folderId_s", atomicSet("mouhahahaha"));
		solrClient.add(doc);
		solrClient.commit();
	}

	private void createADocumentWithoutFolder() throws Exception {
		getModelLayerFactory().newRecordServices().add(rm.newDocumentWithId("documentWithoutParent")
				.setTitle("Document 2").setFolder("folder1")
				.setContent(minorContent("content2.doc")));

		SolrClient solrClient = getDataLayerFactory().getRecordsVaultServer().getNestedSolrServer();
		SolrInputDocument doc = new SolrInputDocument();
		doc.setField("id", "documentWithoutParent");
		doc.setField("folderId_s", atomicSet(""));
		solrClient.add(doc);
		solrClient.commit();
	}

	private void movefolder1document2InAnotherCollection() throws IOException, SolrServerException {
		//

		SolrClient solrClient = getDataLayerFactory().getRecordsVaultServer().getNestedSolrServer();
		SolrInputDocument doc = new SolrInputDocument();
		doc.setField("id", "folder1_document2");
		doc.setField("collection_s", atomicSet("otherCollection"));
		solrClient.add(doc);
		solrClient.commit();
	}

	public void createEvents()
			throws RecordServicesException {
		LocalDateTime localDateTime = EventTestUtil.getLocalDateTimeFromString(DATE_1);

		Transaction tx = new Transaction();

		LocalDateTime event1LocalDateTime = localDateTime;
		Event event1 = createEvent(event1LocalDateTime.minusSeconds(6), "event-1");
		Event event2 = createEvent(event1LocalDateTime.minusSeconds(5), "event-2");
		Event event3 = createEvent(event1LocalDateTime.minusSeconds(4), "event-3");
		tx.add(event1);
		tx.add(event2);
		tx.add(event3);

		Event event4 = createEvent(event1LocalDateTime.plusDays(1), "event-4");
		Event event5 = createEvent(event1LocalDateTime.plusDays(2), "event-5");
		Event event6 = createEvent(event1LocalDateTime.plusMonths(3), "event-6");
		tx.add(event4);
		tx.add(event5);
		tx.add(event6);

		Event event7 = createEvent(event1LocalDateTime.plusMonths(4), "event-7");
		tx.add(event7);

		Event event8 = createEvent(event1LocalDateTime.plusMonths(5), "event-8");
		tx.add(event8);

		Event event9 = createEvent(event1LocalDateTime.plusYears(2), "event-9");
		tx.add(event9);

		rm.executeTransaction(tx);
	}

	private Event createEvent(LocalDateTime localDateTime, String id) {
		Event event = rmSchemasRecordsServices.newEventWithId(id);
		event.setTitle("Event1").setCreatedOn(localDateTime).setCreatedBy(users.adminIn(zeCollection).getId());
		event.setType("Type1");

		return event;
	}

	@Test
	public void givenEventsWriteThemInSipArchiveThenValidateIntegrity()
			throws Exception {
		createEvents();

		File tempFolder = newTempFolder();

		RMCollectionExportSIPBuilder builder = new RMCollectionExportSIPBuilder(zeCollection, getAppLayerFactory(), tempFolder) {
			@Override
			protected SIPZipWriter newFileSIPZipWriter(String sipName, Map<String, MetsDivisionInfo> divisionInfoMap,
													   final ProgressInfo progressInfo) throws IOException {
				SIPZipWriter sipZipWriter = super.newFileSIPZipWriter(sipName, divisionInfoMap, progressInfo);
				sipZipWriter.setSipFileHasher(SIPFileHasher());

				return sipZipWriter;
			}

			;
		};
		builder.exportAllEvents(new ProgressInfo(), new Provider<String, Boolean>() {
			@Override
			public Boolean get(String input) {
				return true;
			}
		});

		File tempFolder1 = new File(tempFolder, "events.zip");
		System.out.println(tempFolder1.getAbsolutePath());
		assertThat(tempFolder1).is(zipFileWithSameContentExceptingFiles(getTestResourceFile("events1.zip"), "bag-info.txt", "manifest-sha256.txt", "tagmanifest-sha256.txt"));
	}


	public void createStorageSpace()
			throws Exception {
		Transaction tx = new Transaction();

		tx.add(rm.newStorageSpaceWithId(records.storageSpaceId_S01).setCode(records.storageSpaceId_S01)
				.setTitle("Etagere 1"));
		tx.add(
				rm.newStorageSpaceWithId(records.storageSpaceId_S01_01).setCode(records.storageSpaceId_S01_01)
						.setTitle("Tablette 1").setParentStorageSpace(records.storageSpaceId_S01)).setDecommissioningType(
				TRANSFERT_TO_SEMI_ACTIVE);
		tx.add(
				rm.newStorageSpaceWithId(records.storageSpaceId_S01_02).setCode(records.storageSpaceId_S01_02)
						.setTitle("Tablette 2").setParentStorageSpace(records.storageSpaceId_S01)).setDecommissioningType(
				DEPOSIT);

		tx.add(
				rm.newStorageSpaceWithId("storageSpaceId_S01_02_01").setCode("storageSpaceId_S01_02_01")
						.setTitle("Tablette 2").setParentStorageSpace(records.storageSpaceId_S01_01)).setDecommissioningType(
				DEPOSIT);

		tx.add(rm.newContainerRecordTypeWithId(records.containerTypeId_boite22x22).setTitle("Boite 22X22")
				.setCode("B22x22"));

		rm.executeTransaction(tx);
	}

	public void createContainersWithMultipleStorageSpaceAndStorageSpaces()
			throws Exception {
		createStorageSpace();

		ContainerRecord containerRecord1 = rm.newContainerRecordWithId(records.containerId_bac13).setTemporaryIdentifier("10_A_06")
				.setFull(false).setAdministrativeUnit(records.unitId_10a)
				.setRealTransferDate(date(2008, 10, 31))
				.setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(records.containerTypeId_boite22x22);

		containerRecord1.set(ContainerRecord.STORAGE_SPACE, asList(records.storageSpaceId_S01_01, records.storageSpaceId_S01));

		Transaction tx = new Transaction();

		tx.add(containerRecord1);

		ContainerRecord containerRecord2 = rm.newContainerRecordWithId(records.containerId_bac12).setTemporaryIdentifier("10_A_05")
				.setFull(false).setAdministrativeUnit(records.unitId_10a)
				.setRealTransferDate(date(2006, 10, 31))
				.setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(records.containerTypeId_boite22x22);

		containerRecord2.set(ContainerRecord.STORAGE_SPACE, asList("storageSpaceId_S01_02_01", records.storageSpaceId_S01_01));

		tx.add(containerRecord2);

		rm.executeTransaction(tx);
	}

	public void createContainersInStorageSpacesOfOtherCollections()
			throws Exception {
		createStorageSpace();

		RMSchemasRecordsServices otherCollectionRM = new RMSchemasRecordsServices("otherCollection", getAppLayerFactory());
		StorageSpace storageSpace = otherCollectionRM.newStorageSpaceWithId("otherCollectionStorageSpace")
				.setCode("space").setTitle("Other collection storage space");
		otherCollectionRM.executeTransaction(new Transaction(storageSpace));

		ContainerRecord containerRecordProblematic = rm.newContainerRecordWithId(records.containerId_bac13)
				.setTemporaryIdentifier("problem").setFull(false).setAdministrativeUnit(records.unitId_10a)
				.setRealTransferDate(date(2008, 10, 31)).setStorageSpace(storageSpace)
				.setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(records.containerTypeId_boite22x22);

		Transaction tx = new Transaction(containerRecordProblematic);
		tx.getRecordUpdateOptions().setValidationsEnabled(false);

		rm.executeTransaction(tx);

	}

	@Test
	public void createContainersAndStorageSpace()
			throws Exception {

		createStorageSpace();

		Transaction tx = new Transaction();

		tx.add(rm.newContainerRecordWithId(records.containerId_bac13).setTemporaryIdentifier("10_A_06")
				.setFull(false).setStorageSpace(records.storageSpaceId_S01).setAdministrativeUnit(records.unitId_10a)
				.setRealTransferDate(date(2008, 10, 31)))
				.setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(records.containerTypeId_boite22x22);

		tx.add(rm.newContainerRecordWithId(records.containerId_bac12).setTemporaryIdentifier("10_A_05")
				.setFull(false).setStorageSpace(records.storageSpaceId_S01_01).setAdministrativeUnit(records.unitId_10a)
				.setRealTransferDate(date(2006, 10, 31)))
				.setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(records.containerTypeId_boite22x22);

		tx.add(rm.newContainerRecordWithId(records.containerId_bac11).setTemporaryIdentifier("10_A_04")
				.setFull(false).setStorageSpace(records.storageSpaceId_S01_02).setAdministrativeUnit(records.unitId_10a)
				.setRealTransferDate(date(2005, 10, 31)))
				.setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(records.containerTypeId_boite22x22);

		tx.add(rm.newContainerRecordWithId(records.containerId_bac11 + "extra1").setTemporaryIdentifier("10_A_04_extra1")
				.setFull(false).setStorageSpace(records.storageSpaceId_S01_01).setAdministrativeUnit(records.unitId_10a)
				.setRealTransferDate(date(2005, 10, 31)))
				.setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(records.containerTypeId_boite22x22);

		tx.add(rm.newContainerRecordWithId(records.containerId_bac11 + "extra2").setTemporaryIdentifier("10_A_04_extra2")
				.setFull(false).setStorageSpace("storageSpaceId_S01_02_01").setAdministrativeUnit(records.unitId_10a)
				.setRealTransferDate(date(2005, 10, 31)))
				.setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(records.containerTypeId_boite22x22);

		rm.executeTransaction(tx);
	}

	public void createOrphanContainersAndStorageSpace()
			throws Exception {
		Transaction tx = new Transaction();

		tx.add(rm.newContainerRecordWithId(records.containerId_bac12 + "_Orphan").setTemporaryIdentifier("10_A_05_Orphan")
				.setFull(false).setAdministrativeUnit(records.unitId_10a)
				.setRealTransferDate(date(2006, 10, 31)))
				.setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(records.containerTypeId_boite22x22);

		tx.add(rm.newContainerRecordWithId(records.containerId_bac11 + "_Orphan").setTemporaryIdentifier("10_A_04_Orphan")
				.setFull(false).setAdministrativeUnit(records.unitId_10a)
				.setRealTransferDate(date(2005, 10, 31)))
				.setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(records.containerTypeId_boite22x22);

		rm.executeTransaction(tx);
	}

	@Test
	public void whenExportingContainersOfCollectionThenValidateZipContent()
			throws Exception {
		File tempFolder = newTempFolder();

		createContainersAndStorageSpace();

		RMCollectionExportSIPBuilder builder = new RMCollectionExportSIPBuilder(zeCollection, getAppLayerFactory(), tempFolder);
		builder.exportAllContainersBySpace(new ProgressInfo());

		System.out.println(tempFolder.getAbsolutePath());
		File zipFile = new File(tempFolder, "warehouse.zip");

		assertThat(zipFile).is(zipFileWithSameContentExceptingFiles(getTestResourceFile("warehouse1.zip"), "bag-info.txt", "manifest-sha256.txt", "tagmanifest-sha256.txt"));
	}

	@Test
	public void whenExportingContainersOfCollectionThenOk()
			throws Exception {
		File tempFolder = newTempFolder();

		createContainersAndStorageSpace();

		RMCollectionExportSIPBuilder builder = new RMCollectionExportSIPBuilder(zeCollection, getAppLayerFactory(), tempFolder);
		builder.exportAllContainersBySpace(new ProgressInfo());

		assertThat(tempFolder.list()).containsOnly("info", "warehouse.zip");

		assertThat(new File(tempFolder, "info").list())
				.containsOnly("exportedContainers.txt", "failedContainersExport.txt");

		assertThat(contentOf(new File(tempFolder, "info" + File.separator + "failedContainersExport.txt")))
				.isEqualTo("");

		assertThat(contentOf(new File(tempFolder, "info" + File.separator + "exportedContainers.txt")))
				.isEqualTo("bac11, bac11extra1, bac11extra2, bac12, bac13");
	}


	@Test
	public void whenExportingContainersWithMultipleStorageSpace()
			throws Exception {

		File tempFolder = newTempFolder();
		givenConfig(RMConfigs.IS_CONTAINER_MULTIVALUE, true);

		createContainersWithMultipleStorageSpaceAndStorageSpaces();

		RMCollectionExportSIPBuilder builder = new RMCollectionExportSIPBuilder(zeCollection, getAppLayerFactory(), tempFolder) {
			@Override
			protected SIPZipWriter newFileSIPZipWriter(String sipName, Map<String, MetsDivisionInfo> divisionInfoMap,
													   final ProgressInfo progressInfo) throws IOException {
				SIPZipWriter sipZipWriter = super.newFileSIPZipWriter(sipName, divisionInfoMap, progressInfo);
				sipZipWriter.setSipFileHasher(SIPFileHasher());

				return sipZipWriter;
			}
		};

		builder.exportAllContainersBySpace(new ProgressInfo());

		File sipFile = new File(tempFolder, "warehouse.zip");

		assertThat(sipFile).is(zipFileWithSameContentExceptingFiles(getTestResourceFile("multipleStoragewarehouse.zip"), "bag-info.txt", "manifest-sha256.txt", "tagmanifest-sha256.txt"));
	}

	@Test
	public void whenExportingContainersWithSomeThatHaveNoStorageSpaceOfCollectionThenOk()
			throws Exception {
		File tempFolder = newTempFolder();

		createContainersAndStorageSpace();
		createOrphanContainersAndStorageSpace();

		RMCollectionExportSIPBuilder builder = new RMCollectionExportSIPBuilder(zeCollection, getAppLayerFactory(), tempFolder);
		builder.exportAllContainersBySpace(new ProgressInfo());

		assertThat(tempFolder.list()).containsOnly("info", "warehouse.zip");

		assertThat(new File(tempFolder, "info").list())
				.containsOnly("exportedContainers.txt", "failedContainersExport.txt");

		assertThat(contentOf(new File(tempFolder, "info" + File.separator + "failedContainersExport.txt")))
				.isEqualTo("");

		assertThat(contentOf(new File(tempFolder, "info" + File.separator + "exportedContainers.txt")))
				.isEqualTo("bac11, bac11_Orphan, bac11extra1, bac11extra2, bac12, bac12_Orphan, bac13");

		File zipFile = new File(tempFolder, "warehouse.zip");
		System.out.println(zipFile.getAbsolutePath());
		assertThat(zipFile).is(zipFileWithSameContentExceptingFiles(getTestResourceFile("warehouseSipOrphan.zip"), "bag-info.txt", "manifest-sha256.txt", "tagmanifest-sha256.txt"));
	}


	@Test
	public void whenExportingCollectionThenExportTasks()
			throws Exception {
		cacheIntegrityCheckedAfterTest = false;
		//		getIOLayerFactory().newZipService().zip(getTestResourceFile("sip1.zip"),
		//				asList(new File("/Users/francisbaril/Downloads/SIPArchivesCreationAcceptanceTest-sip1").listFiles()));

		Transaction tx = new Transaction();

		int folderIndex = 0;

		tx.add(rm.newRMTaskWithId("taskId").setAssigner(users.aliceIn(zeCollection).getId())
				.setAssignee(users.bobIn(zeCollection).getId()).setTitle("My task to ya")
				.setAssignedOn(new LocalDate(2012, 12, 12))
				.setCreatedOn(new LocalDateTime(2011, 11, 11, 11, 11, 1)));

		tx.add(rm.newRMTaskWithId("modelTaskId").setAssigner(users.aliceIn(zeCollection).getId())
				.setAssignee(users.bobIn(zeCollection).getId()).setTitle("My task to ya").setModel(true)
				.setAssignedOn(new LocalDate(2012, 12, 12))
				.setCreatedOn(new LocalDateTime(2011, 11, 11, 11, 11, 1)));

		rm.executeTransaction(tx);
		createAnInvalidTask666();

		File tempFolder = newTempFolder();
		RMCollectionExportSIPBuilder builder = new RMCollectionExportSIPBuilder(zeCollection, getAppLayerFactory(), tempFolder);


		builder.exportAllTasks(new ProgressInfo());

		assertThat(tempFolder.list()).containsOnly("info", "tasks-001.zip");

		assertThat(contentOf(new File(tempFolder, "info" + File.separator + "exportedTasks.txt")))
				.contains("taskId");


		assertThat(new File(tempFolder, "info").list())
				.containsOnly("failedTasksExport.txt", "exportedTasks.txt");

		assertThat(contentOf(new File(tempFolder, "info" + File.separator + "failedTasksExport.txt")))
				.isEqualTo("task666");


		TestUtils.assertFilesInZip(new File(tempFolder, "tasks-001.zip")).contains(
				"bag-info.txt",
				"bagit.txt",
				"data/_2011/_2011-11/_2011-11-11/userTask-taskId.xml",
				"manifest-sha256.txt", "tagmanifest-sha256.txt",
				"tasks-001.xml"
		);

	}


	@Test
	public void whenExportingUnclassifiedInfosThenExportUserFolderDocumentAndTemporaryRecords()
			throws Exception {

		tx = new Transaction();
		for (User user : asList(users.gandalfIn(zeCollection), users.bobIn(zeCollection), users.dakotaLIndienIn(zeCollection))) {
			UserFolder f1 = tx.add(rm.newUserFolderWithId(user.getUsername() + "userFolder1"))
					.setUser(user).setTitle("Ze dossier de " + user.getUsername());
			UserFolder f1_1 = tx.add(rm.newUserFolderWithId(user.getUsername() + "userFolder1Sub1"))
					.setUser(user).setTitle("Ze sous-dossier 1 de " + user.getUsername()).setParent(f1);
			UserFolder f1_2 = tx.add(rm.newUserFolderWithId(user.getUsername() + "userFolder1Sub2"))
					.setUser(user).setTitle("Ze sous-dossier 2 de " + user.getUsername()).setParent(f1);

			tx.add(rm.newUserDocumentWithId(f1.getId() + "_doc")).setUser(user).setUserFolder(f1)
					.setContent(majorContent("content1.doc"));

			tx.add(rm.newUserDocumentWithId(f1_1.getId() + "_doc")).setUser(user).setUserFolder(f1_1)
					.setContent(majorContent("content2.doc"));

			tx.add(rm.newUserDocumentWithId(f1_2.getId() + "_doc")).setUser(user).setUserFolder(f1_2)
					.setContent(majorContent("content1.doc"));

			tx.add(rm.newUserDocumentWithId(user.getUsername() + "doc1")).setUser(user).setContent(majorContent("content2.doc"));
			tx.add(rm.newUserDocumentWithId(user.getUsername() + "doc2")).setUser(user).setContent(majorContent("content2.doc"));
		}

		tx.add(rm.newTemporaryRecordWithId("tempRecord1").setTitle("Temporary record 1").setContent(majorContent("content1.doc"))
				.setCreatedBy(users.bobIn(zeCollection).getId())).setCreatedOn(TimeProvider.getLocalDateTime());

		tx.add(rm.newTemporaryRecordWithId("tempRecord2").setTitle("Temporary record 1").setContent(majorContent("content2.doc"))
				.setCreatedBy(users.aliceIn(zeCollection).getId())).setCreatedOn(TimeProvider.getLocalDateTime());

		rm.executeTransaction(tx);

		File sipFile = new File(newTempFolder(), "test.zip");


		SIPZipBagInfoFactory factory = new DefaultSIPZipBagInfoFactory(getAppLayerFactory(), Locale.FRENCH);
		SIPZipWriter writer = new FileSIPZipWriter(getAppLayerFactory(), sipFile, "unclassified.zip", factory);
		ProgressInfo progressInfo = new ProgressInfo();
		UnclassifiedDataSIPWriter builder = new UnclassifiedDataSIPWriter(zeCollection, getAppLayerFactory(), writer, Locale.FRENCH, progressInfo);

		builder.exportUnclassifiedData();
		builder.close();

		List<String> expectedFiles = new ArrayList<>();

		String aliceId = users.aliceIn(zeCollection).getId();
		String bobId = users.bobIn(zeCollection).getId();

		expectedFiles.add("data/user/user-" + bobId + "/temporaryRecord-tempRecord1.xml");
		expectedFiles.add("data/user/user-" + bobId + "/temporaryRecord-tempRecord1-content-1.0.doc");
		expectedFiles.add("data/user/user-" + aliceId + "/temporaryRecord-tempRecord2.xml");
		expectedFiles.add("data/user/user-" + aliceId + "/temporaryRecord-tempRecord2-content-1.0.doc");

		expectedFiles.add("data/user/user-" + bobId + "/userDocument-bobdoc1-content-1.0.doc");
		expectedFiles.add("data/user/user-" + bobId + "/userDocument-bobdoc1.xml");
		expectedFiles.add("data/user/user-" + bobId + "/userDocument-bobdoc2-content-1.0.doc");
		expectedFiles.add("data/user/user-" + bobId + "/userDocument-bobdoc2.xml");
		expectedFiles.add("data/user/user-" + bobId + "/userFolder-bobuserFolder1.xml");
		expectedFiles.add("data/user/user-" + bobId + "/userFolder-bobuserFolder1/userDocument-bobuserFolder1_doc-content-1.0.doc");
		expectedFiles.add("data/user/user-" + bobId + "/userFolder-bobuserFolder1/userDocument-bobuserFolder1_doc.xml");
		expectedFiles.add("data/user/user-" + bobId + "/userFolder-bobuserFolder1/userFolder-bobuserFolder1Sub1.xml");
		expectedFiles.add("data/user/user-" + bobId + "/userFolder-bobuserFolder1/userFolder-bobuserFolder1Sub1/userDocument-bobuserFolder1Sub1_doc-content-1.0.doc");
		expectedFiles.add("data/user/user-" + bobId + "/userFolder-bobuserFolder1/userFolder-bobuserFolder1Sub1/userDocument-bobuserFolder1Sub1_doc.xml");
		expectedFiles.add("data/user/user-" + bobId + "/userFolder-bobuserFolder1/userFolder-bobuserFolder1Sub2.xml");
		expectedFiles.add("data/user/user-" + bobId + "/userFolder-bobuserFolder1/userFolder-bobuserFolder1Sub2/userDocument-bobuserFolder1Sub2_doc-content-1.0.doc");
		expectedFiles.add("data/user/user-" + bobId + "/userFolder-bobuserFolder1/userFolder-bobuserFolder1Sub2/userDocument-bobuserFolder1Sub2_doc.xml");

		TestUtils.assertFilesInZip(sipFile).contains(expectedFiles.toArray(new String[0]));

	}

	protected void createAnInvalidFolder666() throws Exception {
		Transaction tx;
		tx = new Transaction();

		tx.add(rm.newFolderWithId("folder666").setOpenDate(new LocalDate(2018, 1, 1))
				.setTitle("Folder in an invalid category")
				.setAdministrativeUnitEntered(records.unitId_10a).setCategoryEntered(records.categoryId_X13)
				.setRetentionRuleEntered(records.ruleId_1));

		tx.add(rm.newDocumentWithId("folder666_document1").setTitle("Document 1").setFolder("folder666")
				.setContent(majorContent("content1.doc")));

		tx.add(rm.newDocumentWithId("folder666_document2").setTitle("Document 2").setFolder("folder666"))
				.setContent(minorContent("content2.doc"));

		rm.executeTransaction(tx);
		waitForBatchProcess();

		SolrClient solrClient = getDataLayerFactory().getRecordsVaultServer().getNestedSolrServer();
		SolrInputDocument doc = new SolrInputDocument();
		doc.setField("id", "folder666");
		doc.setField(rm.folder.categoryEntered().getDataStoreCode(), atomicSet("mouhahahaha"));
		doc.setField(rm.folder.category().getDataStoreCode(), atomicSet("mouhahahaha"));
		solrClient.add(doc);
		solrClient.commit();
	}

	protected void createAnInvalidTask666() throws Exception {
		Transaction tx;
		tx = new Transaction();


		tx.add(rm.newRMTaskWithId("task666").setAssigner(users.aliceIn(zeCollection).getId())
				.setAssignee(users.bobIn(zeCollection).getId()).setTitle("My task to ya")
				.setAssignedOn(new LocalDate(2012, 12, 12))
				.setCreatedOn(new LocalDateTime(2011, 11, 11, 11, 11, 1)));

		rm.executeTransaction(tx);
		waitForBatchProcess();

		TasksSchemasRecordsServices tasks = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());

		SolrClient solrClient = getDataLayerFactory().getRecordsVaultServer().getNestedSolrServer();
		SolrInputDocument doc = new SolrInputDocument();
		doc.setField("id", "task666");
		doc.setField(tasks.userTask.statusType().getDataStoreCode(), atomicSet("Mouahahahahaha"));
		solrClient.add(doc);
		solrClient.commit();

		getModelLayerFactory().getRecordsCaches().reloadAllSchemaTypes(zeCollection);
	}


	@Test
	@SlowTest
	public void whenExportingAllFoldersAndDocumentsSplittedBySizeThen()
			throws Exception {

		//		getIOLayerFactory().newZipService().zip(getTestResourceFile("sip2.zip"),
		//				asList(new File("/Users/francisbaril/Downloads/SIPArchivesCreationAcceptanceTest-sip2").listFiles()));


		Transaction tx = new Transaction();
		tx.add(rm.newFolderWithId("zeFolderId").setOpenDate(new LocalDate(2018, 1, 1))
				.setTitle("Ze folder")
				.setAdministrativeUnitEntered(records.unitId_10a).setCategoryEntered(records.categoryId_X13)
				.setRetentionRuleEntered(records.ruleId_1));

		List<String> ids = new ArrayList<>();
		for (int i = 0; i < 500; i++) {
			Document email = rm.newEmailWithId("email" + i).setTitle("My important email").setFolder("zeFolderId")
					.setContent(minorContent("testFile.msg"));
			tx.add(email);
			ids.add(email.getWrappedRecord().getId());
		}


		rm.executeTransaction(tx);

		File tempFolder = getModelLayerFactory().getDataLayerFactory().getIOServicesFactory().getTempFolder();

		List<String> tempFilesBeforeSIPCreation = LangUtils.listFilenames(tempFolder);

		File sipFilesFolder = buildSIPWithDocumentsWith1MegabytesLimit(ids);

		List<String> tempFilesAfterSIPCreation = new ArrayList<>(LangUtils.listFilenames(tempFolder));
		tempFilesAfterSIPCreation.removeAll(tempFilesBeforeSIPCreation);
		assertThat(tempFilesAfterSIPCreation).isEmpty();

		assertThat(sipFilesFolder.list()).containsOnly("test-001.zip", "test-002.zip", "test-003.zip", "test-004.zip",
				"test-005.zip", "test-006.zip", "test-007.zip", "test-008.zip", "test-009.zip", "test-010.zip",
				"test-011.zip", "test-012.zip", "test-013.zip", "test-014.zip", "test-015.zip", "test-016.zip", "test-017.zip");

	}

	private void unzipAllInDownloadFolder(File folder, String name) {
		File destFolder = new File("/Users/francisbaril/Downloads/" + name);
		FileUtils.deleteQuietly(destFolder);
		destFolder.mkdirs();


		for (File sipFile : folder.listFiles()) {
			File destUnzipFolder = new File(destFolder, StringUtils.substringBefore(sipFile.getName(), "."));
			destUnzipFolder.mkdirs();
			try {
				getIOLayerFactory().newZipService().unzip(sipFile, destUnzipFolder);
			} catch (ZipServiceException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void unzipInDownloadFolder(File sipFile, String name) {
		File folder = new File("/Users/francisbaril/Downloads/" + name);
		try {
			FileUtils.deleteDirectory(folder);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		folder.mkdirs();

		try {
			getIOLayerFactory().newZipService().unzip(sipFile, folder);
		} catch (ZipServiceException e) {
			throw new RuntimeException(e);
		}
	}

	//-------------------------------------------


	private File buildSIPWithDocuments(String... documentsIds) throws Exception {
		List<String> bagInfoLines = new ArrayList<>();
		bagInfoLines.add("This is the first bagInfo line");
		bagInfoLines.add("This is the second bagInfo line");
		bagInfoLines.add("This is the last bagInfo line");
		DefaultSIPZipBagInfoFactory bagInfoFactory = new DefaultSIPZipBagInfoFactory(getAppLayerFactory(), FRENCH);
		bagInfoFactory.setHeaderLines(bagInfoLines);


		File sipFile = new File(newTempFolder(), "test.sip");

		SIPZipWriter writer = new FileSIPZipWriter(getAppLayerFactory(), sipFile, "test", bagInfoFactory);
		writer.setSipFileHasher(SIPFileHasher());


		ValidationErrors errors = constellioSIP.buildWithFoldersAndDocuments(writer, new ArrayList<String>(), asList(documentsIds), null,
				metadataIgnore
		);

		if (!errors.isEmpty()) {
			assertThat(TestUtils.frenchMessages(errors)).describedAs("errors").isEmpty();
		}

		return sipFile;
	}

	private SIPFileHasher SIPFileHasher() {
		return new SIPFileHasher() {
			@Override
			public String computeHash(File input, String sipPath) throws IOException {
				return "CHECKSUM{{" + sipPath.replace("\\", "/ d") + "}}";
			}

			@Override
			public long length(File zipFile, List<MetsContentFileReference> contentFileReferences,
							   List<MetsEADMetadataReference> eadMetadataReferences) {
				return 42;
			}
		};
	}

	private File buildSIPWithDocumentsWith1MegabytesLimit(List<String> documentsIds) throws Exception {

		List<String> bagInfoLines = new ArrayList<>();
		bagInfoLines.add("This is the first bagInfo line");
		bagInfoLines.add("This is the second bagInfo line");
		bagInfoLines.add("This is the last bagInfo line");
		DefaultSIPZipBagInfoFactory bagInfoFactory = new DefaultSIPZipBagInfoFactory(getAppLayerFactory(), FRENCH);
		bagInfoFactory.setHeaderLines(bagInfoLines);

		final File tempFolder = newTempFolder();

		SIPFileNameProvider fileNameProvider = new DefaultSIPFileNameProvider(tempFolder, "test");
		AutoSplittedSIPZipWriter writer = new AutoSplittedSIPZipWriter(getAppLayerFactory(),
				fileNameProvider, 1000 * 1000, bagInfoFactory);

		writer.setSipFileHasher(new SIPFileHasher() {
			@Override
			public String computeHash(File input, String sipPath) throws IOException {
				return "CHECKSUM{{" + sipPath.replace("\\", "/ d") + "}}";
			}
		});

		RMSelectedFoldersAndDocumentsSIPBuilder constellioSIP = new RMSelectedFoldersAndDocumentsSIPBuilder(zeCollection, getAppLayerFactory());
		ValidationErrors errors = constellioSIP.buildWithFoldersAndDocuments(writer, new ArrayList<>(), documentsIds, null,
				metadataIgnore
		);

		if (!errors.isEmpty()) {
			assertThat(TestUtils.frenchMessages(errors)).describedAs("errors").isEmpty();
		}

		return tempFolder;
	}

	private Content majorContent(String filename) throws Exception {
		ContentVersionDataSummary dataSummary =
				getModelLayerFactory().getContentManager().upload(getTestResourceFile(filename));
		return ContentImpl.create("zeContent", users.adminIn(zeCollection), filename, dataSummary, true, false);
	}

	private Content minorContent(String filename) throws Exception {
		ContentVersionDataSummary dataSummary =
				getModelLayerFactory().getContentManager().upload(getTestResourceFile(filename));
		return ContentImpl.create("zeContent", users.adminIn(zeCollection), filename, dataSummary, false, false);
	}

	private void ignoreMetadatas(List<Metadata> ignoredMetadatas) {
		Predicate<Metadata> predicate = null;
		for (Metadata ignoredMetadata : ignoredMetadatas) {
			if (predicate != null) {
				predicate = predicate.or(metadata -> metadata.equals(ignoredMetadata));
			} else {
				predicate = metadata -> metadata.equals(ignoredMetadata);
			}
		}
		metadataIgnore = predicate;
	}

	private void ignoreMetadatasWithLocalCode(List<String> ignoredLocalCodes) {
		Predicate<Metadata> predicate = null;
		for (String ignoredLocalCode : ignoredLocalCodes) {
			if (predicate != null) {
				predicate = predicate.or(metadata -> metadata.isLocalCode(ignoredLocalCode));
			} else {
				predicate = metadata -> metadata.isLocalCode(ignoredLocalCode);
			}
		}
		metadataIgnore = predicate;
	}

	private void ignoreAllMetadatas() {
		metadataIgnore = metadata -> true;
	}
}
