package com.constellio.app.modules.rm.services.sip;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
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
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.contents.ContentImpl;
import com.constellio.model.services.contents.ContentVersionDataSummary;
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
		unzipInDownloadFolder(sipFile, "testSIP");
		assertThat(sipFile).is(zipFileWithSameContentExceptingFiles(getTestResourceFile("sip2.zip")));

	}

	@Test
	public void whenExportingCollectionFoldersAndDocumentsThenAllExported()
			throws Exception {

		//		getIOLayerFactory().newZipService().zip(getTestResourceFile("sip1.zip"),
		//				asList(new File("/Users/francisbaril/Downloads/SIPArchivesCreationAcceptanceTest-sip1").listFiles()));

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

		TestUtils.assertFilesInZip(new File(tempFolder, "foldersAndDocuments-001.zip")).contains(
				"bag-info.txt",
				"data/category-X/folder-folder1.xml",
				"data/category-X/folder-folder1/document-folder1_document1.xml",
				"data/category-X/folder-folder1/document-folder1_document2.xml",
				"data/category-X/category-X100/category-X110/folder-folder9.xml",
				"data/category-X/category-X100/category-X110/folder-folder9/document-folder9_document1.xml",
				"data/category-X/category-X100/category-X110/folder-folder9/document-folder9_document2.xml",
				"data/category-X/category-X100/category-X120/folder-folder10.xml",
				"data/category-X/category-X100/category-X120/folder-folder10/document-folder10_document1.xml",
				"data/category-X/category-X100/category-X120/folder-folder10/document-folder10_document2.xml",
				"foldersAndDocuments-001.xml", "manifest-sha256.txt", "tagmanifest-sha256.txt"
		);

	}


	@Test
	public void whenExportingCollectionThenExportTasks()
			throws Exception {

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

		File tempFolder = getModelLayerFactory().getDataLayerFactory().getIOServicesFactory().getTempFolder();

		List<String> tempFilesBeforeSIPCreation = LangUtils.listFilenames(tempFolder);

		File sipFilesFolder = buildSIPWithDocumentsWith10MegabytesLimit(ids);

		List<String> tempFilesAfterSIPCreation = new ArrayList<>(LangUtils.listFilenames(tempFolder));
		tempFilesAfterSIPCreation.removeAll(tempFilesBeforeSIPCreation);
		assertThat(tempFilesAfterSIPCreation).isEmpty();

		assertThat(sipFilesFolder.list()).containsOnly("test-001.zip", "test-002.zip", "test-003.zip", "test-004.zip",
				"test-005.zip", "test-006.zip", "test-007.zip", "test-008.zip", "test-009.zip", "test-010.zip",
				"test-011.zip", "test-012.zip", "test-013.zip", "test-014.zip", "test-015.zip", "test-016.zip",
				"test-017.zip");

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
