package com.constellio.model.services.search;

import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.zipContents.ZipContentsService;
import com.constellio.model.services.search.zipContents.ZipContentsService.NoContentToZipRuntimeException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ZipContentsServicesAcceptanceTest extends ConstellioTest {
	private static final String TEST_ID = "ZipSearchResultsContentsServicesAcceptanceTest-inputStreams";
	Users users = new Users();
	DecommissioningService service;
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	SearchServices searchServices;
	TasksSchemasRecordsServices taskSchemas;
	ZipContentsService zipSearchResultsContentsServices;
	ZipService zipService;
	//test data
	Content content1_title1, content1_title2, content2_title1, content2_title2, content3_title1;
	File content1File, content2File, content3File;
	String title1 = "Chevreuil.odt";
	String title2 = "Grenouille.odt";
	String title3 = "Poire.odt";
	Document documentWithContent1HavingTitle1;
	Document document11WithContent1HavingTitle1InFolderA2;
	Document document12WithContent1HavingTitle2;
	Document document21WithContent2HavingTitle1;
	Document document22WithContent2HavingTitle2InSubFolder1;
	Document document31WithContent3HavingTitle1;
	Document documentWithoutContent;
	Folder folderA2WithDocument1AndSubFolder1WithContent2AndSubFolder2, subFolder1WithDocumentHavingContent2, subFolder2WithoutDocuments;
	Task taskWithContent1AndContent2;
	private ContentManager contentManager;
	File zippedContentsResult;
	List<String> selectedRecordIds;
	IOServices ioServices;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withAllTestUsers()
		);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		service = new DecommissioningService(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		taskSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		users.setUp(getModelLayerFactory().newUserServices(), zeCollection);
		zipSearchResultsContentsServices = new ZipContentsService(getModelLayerFactory(), zeCollection);
		zipService = new ZipService(new IOServices(newTempFolder()));
		contentManager = getModelLayerFactory().getContentManager();
		ioServices = getDataLayerFactory().getIOServicesFactory().newIOServices();

		initTestData();
	}

	@After
	public void cleanUp()
			throws Exception {
	}

	@Test(expected = NoContentToZipRuntimeException.class)
	public void givenFolderWithNoContentWhenZipThenNoContentToZipRuntimeException()
			throws Exception {
		selectedRecordIds = asList(subFolder2WithoutDocuments.getId());
		zipSearchResultsContentsServices.zipContentsOfRecords(selectedRecordIds, zippedContentsResult);
	}

	@Test
	public void givenFolderWithContentWhenZipThenOk()
			throws Exception {
		selectedRecordIds = asList(subFolder1WithDocumentHavingContent2.getId());
		zipSearchResultsContentsServices.zipContentsOfRecords(selectedRecordIds, zippedContentsResult);
		assertZipHasContents(zippedContentsResult, asList(content2File));
		File unzippedResult = newTempFolder();
		zipService.unzip(zippedContentsResult, unzippedResult);
		assertThat(unzippedResult.list()).containsOnly(subFolder1WithDocumentHavingContent2.getTitle());
	}

	@Test
	public void givenDocumentAndFolderContainingDocumentSelectedWhenZipThenDocumentAppearsOnlyInItsFolder()
			throws Exception {
		selectedRecordIds = asList(subFolder1WithDocumentHavingContent2.getId(),
				document22WithContent2HavingTitle2InSubFolder1.getId());
		zipSearchResultsContentsServices.zipContentsOfRecords(selectedRecordIds, zippedContentsResult);
		assertZipHasContents(zippedContentsResult, asList(content2File));
		File unzippedResult = newTempFolder();
		zipService.unzip(zippedContentsResult, unzippedResult);
		assertThat(unzippedResult.list()).containsOnly(subFolder1WithDocumentHavingContent2.getTitle());
	}

	@Test
	public void givenFolderAndItsSubFolderSelectedWhenZipThenSubFolderOnlyOnceInZip()
			throws Exception {
		selectedRecordIds = asList(folderA2WithDocument1AndSubFolder1WithContent2AndSubFolder2.getId(),
				subFolder1WithDocumentHavingContent2.getId());
		zipSearchResultsContentsServices.zipContentsOfRecords(selectedRecordIds, zippedContentsResult);
		assertZipHasContents(zippedContentsResult, asList(content1File, content2File));
		File unzippedResult = newTempFolder();
		zipService.unzip(zippedContentsResult, unzippedResult);
		assertThat(unzippedResult.list()).containsOnly(folderA2WithDocument1AndSubFolder1WithContent2AndSubFolder2.getTitle());
		File folderA2Unzipped = new File(unzippedResult, folderA2WithDocument1AndSubFolder1WithContent2AndSubFolder2.getTitle());
		assertThat(folderA2Unzipped.list()).containsOnly(subFolder1WithDocumentHavingContent2.getTitle(),
				content1_title1.getCurrentVersion().getFilename());
		File subFolder1WithDocumentHavingContent2File = new File(folderA2Unzipped,
				subFolder1WithDocumentHavingContent2.getTitle());
		assertThat(subFolder1WithDocumentHavingContent2File.list())
				.containsOnly(content2_title2.getCurrentVersion().getFilename());
	}

	@Test(expected = NoContentToZipRuntimeException.class)
	public void givenDocumentWithNoContentWhenZipThenNoContentToZipRuntimeException()
			throws Exception {
		selectedRecordIds = asList(documentWithoutContent.getId());
		zipSearchResultsContentsServices.zipContentsOfRecords(selectedRecordIds, zippedContentsResult);
	}

	@Test
	public void givenDocumentWithContentWhenZipThenOk()
			throws Exception {
		selectedRecordIds = asList(document12WithContent1HavingTitle2.getId());
		zipSearchResultsContentsServices.zipContentsOfRecords(selectedRecordIds, zippedContentsResult);
		assertZipHasContentsWithTitles(zippedContentsResult, asList(content1File),
				asList(title2));
	}

	@Test
	public void givenRecordsWithSameContentHashAndSameTitleWhenZipThenOk()
			throws Exception {
		selectedRecordIds = asList(document11WithContent1HavingTitle1InFolderA2.getId(),
				documentWithContent1HavingTitle1.getId());
		zipSearchResultsContentsServices.zipContentsOfRecords(selectedRecordIds, zippedContentsResult);
		assertZipHasContentsWithTitles(zippedContentsResult, asList(content1File, content1File),
				asList("Chevreuil(" + document11WithContent1HavingTitle1InFolderA2.getId() + ").odt",
						"Chevreuil(" + documentWithContent1HavingTitle1.getId() + ").odt"));
	}

	@Test
	public void givenRecordsWithSameContentHashAndDifferentTitleWhenZipThenTwoZippedFilesWithDifferentTitles()
			throws Exception {
		selectedRecordIds = asList(document11WithContent1HavingTitle1InFolderA2.getId(),
				document12WithContent1HavingTitle2.getId());
		zipSearchResultsContentsServices.zipContentsOfRecords(selectedRecordIds, zippedContentsResult);
		assertZipHasContentsWithTitles(zippedContentsResult, asList(content1File, content1File),
				asList(title1, title2));
	}

	@Test
	public void givenRecordsWithDifferentContentHashAndDifferentTitleWhenZipThenTwoZippedFilesWithDifferentTitles()
			throws Exception {
		selectedRecordIds = asList(document11WithContent1HavingTitle1InFolderA2.getId(),
				document22WithContent2HavingTitle2InSubFolder1.getId());
		zipSearchResultsContentsServices.zipContentsOfRecords(selectedRecordIds, zippedContentsResult);
		assertZipHasContentsWithTitles(zippedContentsResult, asList(content1File, content2File),
				asList(title1, title2));
	}

	@Test
	public void givenRecordsWithThreeDifferentContentHashAndSameTitleWhenZipThenThreeZippedFilesWithDifferentTitles()
			throws Exception {
		selectedRecordIds = asList(document11WithContent1HavingTitle1InFolderA2.getId(),
				document21WithContent2HavingTitle1.getId(),
				document31WithContent3HavingTitle1.getId());
		zipSearchResultsContentsServices.zipContentsOfRecords(selectedRecordIds, zippedContentsResult);
		assertZipHasContentsWithTitles(zippedContentsResult, asList(content1File, content2File, content3File),
				asList("Chevreuil(" + document11WithContent1HavingTitle1InFolderA2.getId() + ").odt",
						"Chevreuil(" + document21WithContent2HavingTitle1.getId() + ").odt",
						"Chevreuil(" + document31WithContent3HavingTitle1.getId() + ").odt"));
	}

	@Test
	public void givenTaskWithTwoContentsWhenZipThenTwoContentsZippedCorrectly()
			throws Exception {
		selectedRecordIds = asList(taskWithContent1AndContent2.getId());
		zipSearchResultsContentsServices.zipContentsOfRecords(selectedRecordIds, zippedContentsResult);
		assertZipHasContents(zippedContentsResult, asList(content1File, content2File));
		File unzippedResult = newTempFolder();
		zipService.unzip(zippedContentsResult, unzippedResult);
		assertThat(unzippedResult.list()).containsOnly(taskWithContent1AndContent2.getTitle());
		File taskUnzipped = new File(unzippedResult, taskWithContent1AndContent2.getTitle());
		assertThat(taskUnzipped.list()).containsOnly(title1, title2);
	}

	@Test
	public void whenCanHaveChildrenThenOk()
			throws Exception {
		MetadataSchemasManager metadataSchemaManager = getModelLayerFactory()
				.getMetadataSchemasManager();
		MetadataSchema schema = metadataSchemaManager.getSchemaTypes(zeCollection)
				.getSchema(Folder.SCHEMA_TYPE + "_default");
		assertThat(zipSearchResultsContentsServices.canHaveChildren(schema)).isTrue();
		schema = metadataSchemaManager.getSchemaTypes(zeCollection)
				.getSchema(Task.SCHEMA_TYPE + "_default");
		assertThat(zipSearchResultsContentsServices.canHaveChildren(schema)).isTrue();
		schema = metadataSchemaManager.getSchemaTypes(zeCollection)
				.getSchema(Document.SCHEMA_TYPE + "_default");
		assertThat(zipSearchResultsContentsServices.canHaveChildren(schema)).isFalse();
	}

	@Test
	public void givenDocumentWithContentWhenZipThenContentZippedCorrectly()
			throws Exception {
		selectedRecordIds = asList(document11WithContent1HavingTitle1InFolderA2.getId());
		zipSearchResultsContentsServices.zipContentsOfRecords(selectedRecordIds, zippedContentsResult);
		assertZipHasContentsWithTitles(zippedContentsResult, asList(content1File), asList(title1));
	}

	@Test
	public void givenFolderWithDocumentsInTrashNotContainedInZip()
			throws Exception {
		Folder folderA01 = records.getFolder_A01();
		selectedRecordIds = asList(folderA01.getId());

		zipSearchResultsContentsServices.zipContentsOfRecords(selectedRecordIds, zippedContentsResult);
		File unzippedResult = newTempFolder();
		zipService.unzip(zippedContentsResult, unzippedResult);
		assertThat(unzippedResult.list()).containsOnly(folderA01.getTitle());
		File taskUnzipped = new File(unzippedResult, folderA01.getTitle());
		assertThat(taskUnzipped.list()).containsOnly("Chevreuil(0).odt", "Chevreuil(1).odt", "Chevreuil(2).odt", title2);

		recordServices.logicallyDelete(documentWithContent1HavingTitle1, User.GOD);

		zipSearchResultsContentsServices.zipContentsOfRecords(selectedRecordIds, zippedContentsResult);
		unzippedResult = newTempFolder();
		zipService.unzip(zippedContentsResult, unzippedResult);
		assertThat(unzippedResult.list()).containsOnly(folderA01.getTitle());
		taskUnzipped = new File(unzippedResult, folderA01.getTitle());
		assertThat(taskUnzipped.list()).containsOnly("Chevreuil(0).odt", "Chevreuil(1).odt", title2);
	}

	private void initTestData()
			throws RecordServicesException {
		Transaction transaction = new Transaction();
		content1_title1 = createContent(title1, title1);
		content1_title2 = createContent(title1, title2);
		content2_title1 = createContent(title2, title1);
		content2_title2 = createContent(title2, title2);
		content3_title1 = createContent(title3, title1);
		File folder = newTempFolder();
		content1File = createFileFromContent(content1_title1, folder.getPath() + "/1");
		content2File = createFileFromContent(content2_title2, folder.getPath() + "/2");
		content3File = createFileFromContent(content3_title1, folder.getPath() + "/3");
		documentWithContent1HavingTitle1 = rm.newDocument().setType(records.documentTypeId_1)
				.setFolder(records.getFolder_A01().getId());
		transaction.add(documentWithContent1HavingTitle1.setContent(content1_title1).setTitle(title1));

		folderA2WithDocument1AndSubFolder1WithContent2AndSubFolder2 = records.getFolder_A02();
		document11WithContent1HavingTitle1InFolderA2 = rm.newDocument().setType(records.documentTypeId_1)
				.setFolder(folderA2WithDocument1AndSubFolder1WithContent2AndSubFolder2);
		transaction.add(document11WithContent1HavingTitle1InFolderA2.setContent(content1_title1).setTitle(title1));

		document12WithContent1HavingTitle2 = rm.newDocument().setType(records.documentTypeId_1).setFolder(
				records.getFolder_A01().getId());
		transaction.add(document12WithContent1HavingTitle2.setContent(content1_title2).setTitle(title2));

		document21WithContent2HavingTitle1 = rm.newDocument().setType(records.documentTypeId_1).setFolder(
				records.getFolder_A01().getId());
		transaction.add(document21WithContent2HavingTitle1.setContent(content2_title1).setTitle(title1));

		document22WithContent2HavingTitle2InSubFolder1 = rm.newDocument().setType(records.documentTypeId_1).setFolder(
				records.getFolder_A01().getId());
		transaction.add(document22WithContent2HavingTitle2InSubFolder1.setContent(content2_title2).setTitle(title2));

		documentWithoutContent = rm.newDocument().setType(records.documentTypeId_1).setTitle("withoutContent").setFolder(
				records.getFolder_A01().getId());
		transaction.add(documentWithoutContent);

		document31WithContent3HavingTitle1 = rm.newDocument().setType(records.documentTypeId_1).setFolder(
				records.getFolder_A01().getId());
		transaction.add(document31WithContent3HavingTitle1.setTitle(title1).setContent(content3_title1));

		taskWithContent1AndContent2 = taskSchemas.newTask().setTitle("zeTask")
				.setContent(asList(content1_title1, content2_title2));
		transaction.add(taskWithContent1AndContent2);

		subFolder1WithDocumentHavingContent2 = records.getFolder_A03();
		subFolder2WithoutDocuments = records.getFolder_A13();
		//FIXME Cis	car pas meme regle?	recordServices.add(records.getFolder_A04().setParentFolder(records.getFolder_A02()));
		transaction.add(document22WithContent2HavingTitle2InSubFolder1.setFolder(subFolder1WithDocumentHavingContent2));
		transaction.add(subFolder1WithDocumentHavingContent2
				.setParentFolder(folderA2WithDocument1AndSubFolder1WithContent2AndSubFolder2));
		transaction.add(subFolder2WithoutDocuments.setParentFolder(folderA2WithDocument1AndSubFolder1WithContent2AndSubFolder2));

		recordServices.execute(transaction);

		zippedContentsResult = new File(newTempFolder().getPath() + "result.zip");
	}

	private File createFileFromContent(Content content, String filePath) {
		InputStream inputStream = null;
		try {
			inputStream = contentManager.getContentInputStream(content.getCurrentVersion().getHash(), TEST_ID);
			FileUtils.copyInputStreamToFile(inputStream, new File(filePath));
			return new File(filePath);
		} catch (Exception e) {
			fail(e.getMessage());
			return null;
		} finally {
			ioServices.closeQuietly(inputStream);
		}
	}

	private void assertZipHasContentsWithTitles(File zippedContents, List<File> contents, List<String> titles)
			throws Exception {
		for (File content : contents) {
			assertThat(zipService.contains(zippedContents, content.getPath()));
		}
		File zipDestination = newTempFolder();
		zipService.unzip(zippedContents, zipDestination);
		String[] zippedFilesPaths = zipDestination.list();
		assertThat(zipDestination.list().length).isEqualTo(contents.size());
		assertFilesTitlesAreValid(zippedFilesPaths, titles);
	}

	private void assertZipHasContents(File zippedContents, List<File> contents)
			throws Exception {
		for (File content : contents) {
			assertThat(zipService.contains(zippedContents, content.getPath()));
		}
	}

	private void assertFilesTitlesAreValid(String[] filesPaths, List<String> titles) {
		List<String> filesTitles = new ArrayList<>();
		for (String filePath : filesPaths) {
			File file = new File(filePath);
			filesTitles.add(file.getName());
		}
		assertThat(filesTitles).containsAll(titles);
	}

	private Content createContent(String resource, String title) {
		User user = users.adminIn(zeCollection);
		ContentVersionDataSummary version01 = upload("Minor_" + resource);
		Content content = contentManager.createMinor(user, title, version01);
		ContentVersionDataSummary version10 = upload("Major_" + resource);
		content.updateContent(user, version10, true);
		return content;
	}

	private ContentVersionDataSummary upload(String resource) {
		InputStream inputStream = DemoTestRecords.class.getResourceAsStream("RMTestRecords_" + resource);
		return contentManager.upload(inputStream);
	}

}
