package com.constellio.app.modules.rm.services.sip;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.sip.bagInfo.DefaultSIPZipBagInfoFactory;
import com.constellio.app.services.sip.zip.AutoSplittedSIPZipWriter;
import com.constellio.app.services.sip.zip.DefaultSIPFileNameProvider;
import com.constellio.app.services.sip.zip.FileSIPZipWriter;
import com.constellio.app.services.sip.zip.SIPFileHasher;
import com.constellio.app.services.sip.zip.SIPFileNameProvider;
import com.constellio.app.services.sip.zip.SIPZipWriter;
import com.constellio.data.dao.services.idGenerator.InMemorySequentialGenerator;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.contents.ContentImpl;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.modules.rm.model.enums.DecommissioningType.DEPOSIT;
import static com.constellio.app.modules.rm.model.enums.DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE;
import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.atomicSet;
import static com.constellio.sdk.tests.TestUtils.zipFileWithSameContentExceptingFiles;
import static java.util.Arrays.asList;
import static java.util.Locale.FRENCH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

public class SIPArchivesCreationAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	RMSchemasRecordsServices rm;
	IOServices ioServices;
	RMSelectedFoldersAndDocumentsSIPBuilder constellioSIP;

	@Before
	public void setUp() throws Exception {

		records.copyBuilder = new CopyRetentionRuleBuilder(new InMemorySequentialGenerator());

		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records));
		this.rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
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
	}

	@Test
	public void givenSIPArchivesOfTwoDocumentsInSameFolderThenArchiveContainsAllMetadatasContentsAndManifests()
			throws Exception {

		getIOLayerFactory().newZipService().zip(getTestResourceFile("sip1.zip"),
						asList(new File("/Users/francisbaril/Downloads/SIPArchivesCreationAcceptanceTest-sip1").listFiles()));
//		getIOLayerFactory().newZipService().zip(getTestResourceFile("sip1.zip"),
//				asList(new File("C:\\Users\\constellios\\Downloads\\SIPArchivesCreationAcceptanceTest-sip1").listFiles()));

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
//		unzipInDownloadFolder(sipFile, "testSIP");

		assertThat(sipFile).is(zipFileWithSameContentExceptingFiles(getTestResourceFile("sip1.zip")));

	}

	@Test
	public void givenSIPArchivesOfAnEmailThenAttachementsExtractedInSIP()
			throws Exception {

		//				getIOLayerFactory().newZipService().zip(getTestResourceFile("sip2.zip"),
		//						asList(new File("/Users/francisbaril/Downloads/SIPArchivesCreationAcceptanceTest-sip2").listFiles()));

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
//		unzipInDownloadFolder(sipFile, "testSIP");

		assertThat(sipFile).is(zipFileWithSameContentExceptingFiles(getTestResourceFile("sip2.zip")));

	}

	@Test
	public void whenExportingCollectionThenAll()
			throws Exception {


		Transaction tx = new Transaction();

		int folderIndex = 0;
		for (Category category : rm.getAllCategories()) {

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

		File tempFolder = newTempFolder();
		RMCollectionExportSIPBuilder builder = new RMCollectionExportSIPBuilder(zeCollection, getAppLayerFactory(), tempFolder);


		builder.exportAllFoldersAndDocuments(new ProgressInfo());

		assertThat(tempFolder.list()).containsOnly("info", "foldersAndDocuments-001.zip");

		assertThat(new File(tempFolder, "info").list())
				.containsOnly("failedFolderExport.txt", "exportedFolders.txt", "exportedDocuments.txt");

		assertThat(contentOf(new File(tempFolder, "info" + File.separator + "failedFolderExport.txt")))
				.isEqualTo("folder666, folder666_document1, folder666_document2");

		assertThat(contentOf(new File(tempFolder, "info" + File.separator + "exportedFolders.txt")))
				.contains("folder12,").doesNotContain("folder12_document1,");

		assertThat(contentOf(new File(tempFolder, "info" + File.separator + "exportedDocuments.txt")))
				.doesNotContain("folder12,").contains("folder12_document1,");

	}

	@Test
	public void createContainersAndStorageSpace()
			throws RecordServicesException {
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

	@Test
	public void whenExportingContainersOfCollectionThenValidateZipContent()
			throws IOException, RecordServicesException {
		File tempFolder = newTempFolder();

		createContainersAndStorageSpace();

		RMCollectionExportSIPBuilder builder = new RMCollectionExportSIPBuilder(zeCollection, getAppLayerFactory(), tempFolder);
		builder.exportAllContainersBySpace(new ProgressInfo());

		System.out.println(tempFolder.getAbsolutePath());
		File tempFolder1 = new File(tempFolder, "containerByBoxes-001.zip");

		assertThat(tempFolder1).is(zipFileWithSameContentExceptingFiles(getTestResourceFile("containerByBoxesSip1.zip")));
	}

	@Test
	public void whenExportingContainersOfCollectionThenOk()
			throws IOException, RecordServicesException {
		File tempFolder = newTempFolder();

		createContainersAndStorageSpace();

		RMCollectionExportSIPBuilder builder = new RMCollectionExportSIPBuilder(zeCollection, getAppLayerFactory(), tempFolder);
		builder.exportAllContainersBySpace(new ProgressInfo());

		assertThat(tempFolder.list()).containsOnly("info", "containerByBoxes-001.zip");

		assertThat(new File(tempFolder, "info").list())
				.containsOnly("exportedContainers.txt", "failedContainersExport.txt");

		assertThat(contentOf(new File(tempFolder, "info" + File.separator + "failedContainersExport.txt")))
				.isEqualTo("");

		assertThat(contentOf(new File(tempFolder, "info" + File.separator + "exportedContainers.txt")))
				.isEqualTo("bac11, bac11extra1, bac11extra2, bac12, bac13");
	}


	@Test
	public void whenExportingCollectionThenExportTasks()
			throws Exception {

		//		getIOLayerFactory().newZipService().zip(getTestResourceFile("sip1.zip"),
		//				asList(new File("/Users/francisbaril/Downloads/SIPArchivesCreationAcceptanceTest-sip1").listFiles()));

		Transaction tx = new Transaction();

		int folderIndex = 0;

		tx.add(rm.newRMTask().setAssigner(users.aliceIn(zeCollection).getId())
				.setAssignee(users.bobIn(zeCollection).getId()).setTitle("My task to ya")
				.setAssignedOn(new LocalDate(2012, 12, 12))
				.setCreatedOn(new LocalDateTime(2011, 11, 11, 11, 11, 1)));

		rm.executeTransaction(tx);
		//createAnInvalidFolder666();

		File tempFolder = newTempFolder();
		RMCollectionExportSIPBuilder builder = new RMCollectionExportSIPBuilder(zeCollection, getAppLayerFactory(), tempFolder);


		builder.exportAllTasks(new ProgressInfo());

		assertThat(tempFolder.list()).containsOnly("info", "tasks-001.zip");

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

	@Test
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

		File sipFilesFolder = buildSIPWithDocumentsWith10MegabytesLimit(ids);
		System.out.println(sipFilesFolder.getAbsolutePath());

		System.out.println(getModelLayerFactory().getDataLayerFactory().getIOServicesFactory().getTempFolder());
		assertThat(getModelLayerFactory().getDataLayerFactory().getIOServicesFactory().getTempFolder().list()).isNull();

		//unzipAllInDownloadFolder(sipFilesFolder, "testSIP");

		//assertThat(sipFile).is(zipFileWithSameContentExceptingFiles(getTestResourceFile("sip2.zip"), "bag-info.txt"));

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
		writer.setSipFileHasher(new SIPFileHasher() {
			@Override
			public String computeHash(File input, String sipPath) throws IOException {
				return "CHECKSUM{{" + sipPath.replace("\\", "/ d") + "}}";
			}
		});


		ValidationErrors errors = constellioSIP.buildWithFoldersAndDocuments(writer, new ArrayList<String>(), asList(documentsIds), null);

		if (!errors.isEmpty()) {
			assertThat(TestUtils.frenchMessages(errors)).describedAs("errors").isEmpty();
		}

		return sipFile;
	}

	private File buildSIPWithDocumentsWith10MegabytesLimit(List<String> documentsIds) throws Exception {

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
		ValidationErrors errors = constellioSIP.buildWithFoldersAndDocuments(writer, new ArrayList<String>(), documentsIds, null
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
}
