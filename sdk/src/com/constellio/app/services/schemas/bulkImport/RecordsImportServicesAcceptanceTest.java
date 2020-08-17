package com.constellio.app.services.schemas.bulkImport;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailStatus;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.excel.Excel2003ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.excel.Excel2007ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.xml.XMLImportDataProvider;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InternetTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.modules.rm.model.enums.DisposalType.DEPOSIT;
import static com.constellio.app.modules.rm.model.enums.DisposalType.DESTRUCTION;
import static com.constellio.data.conf.HashingEncoding.BASE64_URL_ENCODED;
import static com.constellio.model.entities.schemas.Schemas.LEGACY_ID;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static com.constellio.sdk.tests.TestUtils.frenchMessages;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.Assert.fail;

public class RecordsImportServicesAcceptanceTest extends ConstellioTest {

	private Users users = new Users();
	private RMSchemasRecordsServices rm;
	private RMTestRecords records = new RMTestRecords(zeCollection);

	private BulkImportProgressionListener progressionListener = new LoggerBulkImportProgressionListener();
	private RecordsImportServices importServices;
	private User admin;

	private Folder folder1;
	private Folder folder2;
	private Folder folder3;
	private Folder folder4;
	private Folder folder5;
	private Folder folder6;
	private Folder folder7;
	private Folder folder8;

	private LocalDateTime now = new LocalDateTime().minusHours(3);

	@Before
	public void setUp()
			throws Exception {
		givenHashingEncodingIs(BASE64_URL_ENCODED);
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
		);

		givenTimeIs(now);

		importServices = new RecordsImportServices(getModelLayerFactory());

		admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

	}

	@Test
	public void whenImportingZipOfXMLFilesDecommissioningListThenImportedCorrectly()
			throws Exception {

		File zipFile = buildZipWith("administrativeUnit.xml", "category.xml", "containerRecord.xml", "ddvContainerRecordType.xml",
				"folder.xml", "document.xml",
				"retentionRule.xml", "ddvDocumentType.xml", "decommissioningList.xml");

		importServices.bulkImport(XMLImportDataProvider.forZipFile(getModelLayerFactory(), zipFile), progressionListener, admin);

		DecommissioningList decomList;

		decomList = rm.getDecommissioningListWithLegacyId("40");
		List<DecomListFolderDetail> decomListFolderDetails = decomList.getFolderDetails();
		assertThat(decomList.getDescription()).isNull();
		assertThat(decomList.getTitle()).isEqualTo("test");
		assertThat(decomListFolderDetails.get(0).getFolderId()).isEqualTo(rm.getFolderWithLegacyId("660").getId());
		assertThat(decomListFolderDetails.get(0).getContainerRecordId())
				.isEqualTo(rm.getContainerRecordWithLegacyId("412903").getId());
		assertThat(decomListFolderDetails.get(0).getFolderDetailStatus()).isEqualTo(FolderDetailStatus.EXCLUDED);
		assertThat(decomListFolderDetails.get(0).isReversedSort()).isTrue();
		assertThat(decomListFolderDetails.get(0).getFolderLinearSize()).isEqualTo(42.0);

		assertThat(decomListFolderDetails.get(1).getFolderId()).isEqualTo(rm.getFolderWithLegacyId("670").getId());
		assertThat(decomListFolderDetails.get(1).getContainerRecordId())
				.isEqualTo(rm.getContainerRecordWithLegacyId("412904").getId());
		assertThat(decomListFolderDetails.get(1).getFolderDetailStatus()).isEqualTo(FolderDetailStatus.INCLUDED);
		assertThat(decomListFolderDetails.get(1).isReversedSort()).isFalse();
		assertThat(decomListFolderDetails.get(1).getFolderLinearSize()).isEqualTo(0.0);

		decomList = rm.getDecommissioningListWithLegacyId("41");
		decomListFolderDetails = decomList.getFolderDetails();
		List<DecomListContainerDetail> decomListContainerDetails = decomList.getContainerDetails();
		assertThat(decomList.getDescription()).isNull();
		assertThat(decomList.getTitle()).isEqualTo("test41");
		assertThat(decomListFolderDetails.size()).isEqualTo(0);

		assertThat(decomListContainerDetails.get(0).getContainerRecordId())
				.isEqualTo(rm.getContainerRecordWithLegacyId("412903").getId());
		assertThat(decomListContainerDetails.get(0).isFull()).isTrue();

		decomList = rm.getDecommissioningListWithLegacyId("42");
		List<Comment> decomListComments = decomList.getComments();
		assertThat(decomList.getTitle()).isEqualTo("test42");
		assertThat(decomListComments.get(0).getMessage()).isEqualTo("message test");
		assertThat(decomListComments.get(0).getCreationDateTime()).isInstanceOf(LocalDateTime.class);
		assertThat(decomListComments.get(0).getUsername()).isEqualTo("admin");

		assertThat(decomListComments.get(1).getMessage()).isEqualTo("super message");
	}

	@Test
	@InternetTest
	public void whenImportingZipOfXMLFilesThenImportedCorrectly()
			throws Exception {

		File zipFile = buildZipWith("administrativeUnit.xml", "category.xml", "folder.xml", "document.xml",
				"retentionRule.xml", "ddvDocumentType.xml", "event.xml", "containerRecord.xml", "ddvContainerRecordType.xml");

		importServices.bulkImport(XMLImportDataProvider.forZipFile(getModelLayerFactory(), zipFile), progressionListener, admin);

		importAndValidate();
		importAndValidateWithRetentionRules();
		importAndValidateDocumentType();
		importAndValidateDocumentWithVersions();
		importAndValidateEvents();
	}

	@Test
	@InternetTest
	public void whenImportingXMLFilesSeparatelyThenImportedCorrectly()
			throws Exception {

		XMLImportDataProvider administrativeUnit = toXMLFile("administrativeUnit.xml");
		XMLImportDataProvider category = toXMLFile("category.xml");
		XMLImportDataProvider folder = toXMLFile("folder.xml");
		XMLImportDataProvider document = toXMLFile("document.xml");
		XMLImportDataProvider retentionRule = toXMLFile("retentionRule.xml");
		XMLImportDataProvider ddvDocumentType = toXMLFile("ddvDocumentType.xml");
		XMLImportDataProvider userTask = toXMLFile("userTask.xml");
		XMLImportDataProvider[] files = new XMLImportDataProvider[]{
				ddvDocumentType, category, administrativeUnit, retentionRule, folder, document, userTask,};

		for (ImportDataProvider importDataProvider : files) {
			importServices.bulkImport(importDataProvider, progressionListener, admin);
		}

		assertThat(administrativeUnit.size("administrativeUnit")).isEqualTo(3);
		assertThat(category.size("category")).isEqualTo(3);
		assertThat(folder.size("folder")).isEqualTo(8);
		assertThat(document.size("document")).isEqualTo(3);
		assertThat(retentionRule.size("retentionRule")).isEqualTo(2);
		assertThat(ddvDocumentType.size("ddvDocumentType")).isEqualTo(2);
		assertThat(userTask.size("userTask")).isEqualTo(1);

		importAndValidate();
		importAndValidateWithRetentionRules();
		importAndValidateDocumentType();
		importAndValidateDocumentWithVersions();

		RetentionRule rule111201Id = rm.getRetentionRuleWithCode("111201");
		String copy123Id = rule111201Id.getCopyRetentionRuleWithCode("123").getId();
		String copy456Id = rule111201Id.getCopyRetentionRuleWithCode("456").getId();
		String copy789Id = rule111201Id.getCopyRetentionRuleWithCode("789").getId();
		String copy000Id = rule111201Id.getCopyRetentionRuleWithCode("000").getId();

		folder5 = rm.wrapFolder(expectedRecordWithLegacyId("671"));
		assertThat(folder5.getRetentionRule()).isEqualTo(rule111201Id.getId());
		assertThat(folder5.getMainCopyRule().getId()).isEqualTo(copy000Id);

		folder6 = rm.wrapFolder(expectedRecordWithLegacyId("672"));
		assertThat(folder6.getRetentionRule()).isEqualTo(rule111201Id.getId());
		assertThat(folder6.getMainCopyRule().getId()).isEqualTo(copy123Id);

		folder7 = rm.wrapFolder(expectedRecordWithLegacyId("673"));
		assertThat(folder7.getRetentionRule()).isEqualTo(rule111201Id.getId());
		assertThat(folder7.getMainCopyRule().getId()).isEqualTo(copy456Id);

		folder8 = rm.wrapFolder(expectedRecordWithLegacyId("674"));
		assertThat(folder8.getRetentionRule()).isEqualTo(rule111201Id.getId());
		assertThat(folder8.getMainCopyRule().getId()).isEqualTo(copy789Id);

		Task task = rm.wrapRMTask(expectedRecordWithLegacyId("001"));
		assertThat(task.getTitle()).isEqualTo("An unexpected Journey task");
		assertThat(task.getContent().get(0).getCurrentVersion().getFilename()).isEqualTo("An unexpected Journey");
	}

	@Test
	public void whenImportingActiveFoldersWithManualDepositOrDestructionDateBeforeExpectedTransferDateThenInactiveDatesNotSetted()
			throws Exception {

		File excelFile = getTestResourceFile("dataWithInvalidManuallyEnteredDates.xls");

		importServices.bulkImport(Excel2003ImportDataProvider.fromFile(excelFile), progressionListener, admin);

		assertThatRecords(rm.searchFolders(ALL))
				.extractingMetadatas(LEGACY_ID.getLocalCode(), Folder.EXPECTED_TRANSFER_DATE, Folder.MANUAL_EXPECTED_DEPOSIT_DATE,
						Folder.MANUAL_EXPECTED_DESTRUCTION_DATE).containsOnly(
				tuple("1", date(2017, 12, 31), date(2012, 5, 29), date(2012, 5, 29)),
				tuple("2", date(2017, 12, 31), date(2012, 5, 29), date(2012, 5, 29)),
				tuple("3", null, date(2012, 5, 29), date(2012, 5, 29)),
				tuple("4", null, date(2012, 5, 29), date(2012, 5, 29)),

				tuple("5", date(2017, 12, 31), null, null),
				tuple("6", date(2017, 12, 31), null, null),
				tuple("7", null, null, null),
				tuple("8", null, null, null),

				tuple("9", date(2017, 12, 31), date(2010, 5, 29), date(2010, 5, 29)),
				tuple("10", date(2017, 12, 31), date(2010, 5, 29), date(2010, 5, 29)),
				tuple("11", null, date(2010, 5, 29), date(2010, 5, 29)),
				tuple("12", null, date(2010, 5, 29), date(2010, 5, 29))
		);

	}

	@Test
	public void whenImportingAnExcelFileThenImportedCorrectly()
			throws Exception {

		ContentManager contentManager = getModelLayerFactory().getContentManager();
		String hash1 = contentManager.upload(newTempFileWithContent("file.txt", "I am the first value")).getHash();
		String hash2 = contentManager.upload(newTempFileWithContent("file.txt", "I am the second value")).getHash();
		String hash3 = contentManager.upload(newTempFileWithContent("file.txt", "I am the third value")).getHash();
		String hash4 = contentManager.upload(newTempFileWithContent("file.txt", "I am the fourth value")).getHash();
		String hash5 = contentManager.upload(newTempFileWithContent("file.txt", "I am the fifth value")).getHash();
		String hash6 = contentManager.upload(newTempFileWithContent("file.txt", "I am the sixth value")).getHash();

		File excelFile = getTestResourceFile("datas.xls");
		File excelFileModified = getTestResourceFile("datasModified.xls");

		importServices.bulkImport(Excel2003ImportDataProvider.fromFile(excelFile), progressionListener, admin);

		importAndValidate();
		Document document1 = rm.getDocumentByLegacyId("1");
		assertThat(document1.getContent()).isNotNull();
		assertThat(document1.getContent().getCurrentVersion().getHash()).isEqualTo(hash1);
		assertThat(document1.getContent().getCurrentVersion().getFilename()).isEqualTo("fichier1.txt");

		Document document2 = rm.getDocumentByLegacyId("2");
		assertThat(document2.getContent()).isNotNull();
		assertThat(document2.getContent().getCurrentVersion().getHash()).isEqualTo(hash2);
		assertThat(document2.getContent().getCurrentVersion().getFilename()).isEqualTo("fichier2.txt");

		Document document3 = rm.getDocumentByLegacyId("3");
		assertThat(document3.getContent()).isNotNull();
		assertThat(document3.getContent().getCurrentVersion().getHash()).isEqualTo(hash3);
		assertThat(document3.getContent().getCurrentVersion().getFilename()).isEqualTo("fichier3.txt");


		importAndValidateWithModifications(Excel2003ImportDataProvider.fromFile(excelFileModified));
		document1 = rm.getDocumentByLegacyId("1");
		assertThat(document1.getContent()).isNotNull();
		assertThat(document1.getContent().getCurrentVersion().getHash()).isEqualTo(hash4);
		assertThat(document1.getContent().getCurrentVersion().getFilename()).isEqualTo("fichier4.txt");

		document2 = rm.getDocumentByLegacyId("2");
		assertThat(document2.getContent()).isNotNull();
		assertThat(document2.getContent().getCurrentVersion().getHash()).isEqualTo(hash5);
		assertThat(document2.getContent().getCurrentVersion().getFilename()).isEqualTo("fichier5.txt");

		document3 = rm.getDocumentByLegacyId("3");
		assertThat(document3.getContent()).isNotNull();
		assertThat(document3.getContent().getCurrentVersion().getHash()).isEqualTo(hash6);
		assertThat(document3.getContent().getCurrentVersion().getFilename()).isEqualTo("fichier6.txt");

	}


	@Test
	public void whenImportingAnExcel2007FileThenImportedCorrectly()
			throws Exception {

		ContentManager contentManager = getModelLayerFactory().getContentManager();
		String hash1 = contentManager.upload(newTempFileWithContent("file.txt", "I am the first value")).getHash();
		String hash2 = contentManager.upload(newTempFileWithContent("file.txt", "I am the second value")).getHash();
		String hash3 = contentManager.upload(newTempFileWithContent("file.txt", "I am the third value")).getHash();
		String hash4 = contentManager.upload(newTempFileWithContent("file.txt", "I am the fourth value")).getHash();
		String hash5 = contentManager.upload(newTempFileWithContent("file.txt", "I am the fifth value")).getHash();
		String hash6 = contentManager.upload(newTempFileWithContent("file.txt", "I am the sixth value")).getHash();

		File excelFile = getTestResourceFile("datas.xlsx");
		File excelFileModified = getTestResourceFile("datasModified.xlsx");

		importServices.bulkImport(Excel2007ImportDataProvider.fromFile(excelFile), progressionListener, admin);

		importAndValidate();
		Document document1 = rm.getDocumentByLegacyId("1");
		assertThat(document1.getContent()).isNotNull();
		assertThat(document1.getContent().getCurrentVersion().getHash()).isEqualTo(hash1);
		assertThat(document1.getContent().getCurrentVersion().getFilename()).isEqualTo("fichier1.txt");

		Document document2 = rm.getDocumentByLegacyId("2");
		assertThat(document2.getContent()).isNotNull();
		assertThat(document2.getContent().getCurrentVersion().getHash()).isEqualTo(hash2);
		assertThat(document2.getContent().getCurrentVersion().getFilename()).isEqualTo("fichier2.txt");

		Document document3 = rm.getDocumentByLegacyId("3");
		assertThat(document3.getContent()).isNotNull();
		assertThat(document3.getContent().getCurrentVersion().getHash()).isEqualTo(hash3);
		assertThat(document3.getContent().getCurrentVersion().getFilename()).isEqualTo("fichier3.txt");


		importAndValidateWithModifications(Excel2007ImportDataProvider.fromFile(excelFileModified));
		document1 = rm.getDocumentByLegacyId("1");
		assertThat(document1.getContent()).isNotNull();
		assertThat(document1.getContent().getCurrentVersion().getHash()).isEqualTo(hash4);
		assertThat(document1.getContent().getCurrentVersion().getFilename()).isEqualTo("fichier4.txt");

		document2 = rm.getDocumentByLegacyId("2");
		assertThat(document2.getContent()).isNotNull();
		assertThat(document2.getContent().getCurrentVersion().getHash()).isEqualTo(hash5);
		assertThat(document2.getContent().getCurrentVersion().getFilename()).isEqualTo("fichier5.txt");

		document3 = rm.getDocumentByLegacyId("3");
		assertThat(document3.getContent()).isNotNull();
		assertThat(document3.getContent().getCurrentVersion().getHash()).isEqualTo(hash6);
		assertThat(document3.getContent().getCurrentVersion().getFilename()).isEqualTo("fichier6.txt");

	}

	@Test
	public void whenImportingAnExcel2007FileWithMissingContentThenErrors()
			throws Exception {

		ContentManager contentManager = getModelLayerFactory().getContentManager();
		String hash1 = contentManager.upload(newTempFileWithContent("file.txt", "I am the first value")).getHash();
		String hash2 = contentManager.upload(newTempFileWithContent("file.txt", "I am the second value")).getHash();

		File excelFile = getTestResourceFile("datas.xlsx");
		try {
			importServices.bulkImport(Excel2007ImportDataProvider.fromFile(excelFile), progressionListener, admin);
			fail("Exception expected");
		} catch (ValidationException e) {
			assertThat(frenchMessages(e)).containsOnly("Document 3 : Le contenu «PDnNE4i3IjJ9FFN1HzE_5FXROBs=» n'existe pas dans la voûte");

		}

		importAndValidate();
		Document document1 = rm.getDocumentByLegacyId("1");
		assertThat(document1).isNull();

	}


	@Test
	public void givenNonEmptyDataFolderWhenImportingFromZipThenFolderContentUploadedToVault() throws Exception {
		ContentManager contentManager = getModelLayerFactory().getContentManager();
		File uploadedFile1 = newTempFileWithContent("file1.txt", "I am the value");


		String uploadedFile1Hash = "xRSHnQtqTxAt4hDUJ7aLCbo9on8=";//contentManager.upload(uploadedFile1).getHash();

		File zipFile = buildZipWithContent(Arrays.asList(uploadedFile1));

		importServices.bulkImport(XMLImportDataProvider.forZipFile(getModelLayerFactory(), zipFile), progressionListener, admin);
		assertThat(contentManager.getParsedContent(uploadedFile1Hash) != null);
		assertThat(contentManager.getParsedContent(uploadedFile1Hash).getParsedContent().equals("I am the value"));
	}

	@Test
	public void givenNonEmptyDataFolderWithOtherRecordsWhenImportingFromZipThenFolderContentUploadedToVault()
			throws Exception {
		ContentManager contentManager = getModelLayerFactory().getContentManager();
		File uploadedFile1 = newTempFileWithContent("file1.txt", "I am the value");
		File uploadedFile2 = newTempFileWithContent("file2.txt", "I am another value");


		String uploadedFile1Hash = "xRSHnQtqTxAt4hDUJ7aLCbo9on8=";//contentManager.upload(uploadedFile1).getHash();
		String uploadedFile2Hash = "6IbfpsIQP8-ZhYcfY0JEm_xbss4=";

		File zipFile = buildZipWithContent(Arrays.asList(uploadedFile1, uploadedFile2), "administrativeUnit.xml",
				"category.xml", "containerRecord.xml", "ddvContainerRecordType.xml", "folder.xml", "document.xml",
				"retentionRule.xml", "ddvDocumentType.xml", "decommissioningList.xml");
		File destination = new File("C:\\Users\\Constellio\\Desktop\\constellio\\dev\\workflows", zipFile.getName());
		FileUtils.copyFile(zipFile, destination);

		importServices.bulkImport(XMLImportDataProvider.forZipFile(getModelLayerFactory(), zipFile), progressionListener, admin);
		assertThat(contentManager.getParsedContent(uploadedFile1Hash) != null);
		assertThat(contentManager.getParsedContent(uploadedFile2Hash) != null);

		assertThat(contentManager.getParsedContent(uploadedFile1Hash).getParsedContent().equals("I am the value"));
		assertThat(contentManager.getParsedContent(uploadedFile1Hash).getParsedContent().equals("I am another value"));
	}



	private void importAndValidate() {
		Category category1 = rm.wrapCategory(expectedRecordWithLegacyId("22200"));
		assertThat(category1.getId()).isNotEqualTo("22200");
		assertThat(category1.getCode()).isEqualTo("X2222");
		assertThat(category1.getTitle()).isEqualTo("Element Category");
		assertThat(category1.getDescription()).isEqualTo("earth, water, fire and wind");
		assertThat(category1.getKeywords()).isEqualTo(asList("grass", "aqua", "torch", "gas"));
		assertThat(category1.getRententionRules()).isEqualTo(asList(records.ruleId_1, records.ruleId_2));
		assertThat(category1.getParent()).isNullOrEmpty();

		Category category2 = rm.wrapCategory(expectedRecordWithLegacyId("22230"));
		assertThat(category2.getCode()).isEqualTo("X2223");
		assertThat(category2.getTitle()).isEqualTo("Water Category");
		assertThat(category2.getDescription()).isEqualTo("It is so cold here...");
		assertThat(category2.getKeywords()).isEqualTo(asList("aqua", "wet", "rain"));
		assertThat(category2.getRententionRules()).isEmpty();
		assertThat(category2.getParent()).isEqualTo(category1.getId());

		Category category3 = rm.wrapCategory(expectedRecordWithLegacyId("22231"));
		assertThat(category3.getCode()).isEqualTo("X22231");
		assertThat(category3.getTitle()).isEqualTo("Tsunami Category");
		assertThat(category3.getDescription()).isNull();
		assertThat(category3.getKeywords()).isEqualTo(asList("àqûä", "%wet%", "a_p_o_c_a_l_y_p_s_e"));
		assertThat(category3.getRententionRules()).isEqualTo(asList(records.ruleId_1));
		assertThat(category3.getParent()).isEqualTo(category2.getId());

		AdministrativeUnit administrativeUnit1 = rm.wrapAdministrativeUnit(expectedRecordWithLegacyId("40"));
		assertThat(administrativeUnit1.getId()).isNotEqualTo("40");
		assertThat(administrativeUnit1.getCode()).isEqualTo("2014AKA");
		assertThat(administrativeUnit1.getDescription()).isEqualTo("I am a very wonderful Administrative Unit !");
		assertThat(administrativeUnit1.getTitle()).isEqualTo("Administrative Unit Wonderful");
		assertThat(administrativeUnit1.getParent()).isNull();

		AdministrativeUnit administrativeUnit2 = rm.wrapAdministrativeUnit(expectedRecordWithLegacyId("41"));
		assertThat(administrativeUnit2.getCode()).isEqualTo("2014AKA1");
		assertThat(administrativeUnit2.getDescription()).isNullOrEmpty();
		assertThat(administrativeUnit2.getTitle()).isEqualTo("Administrative Unit Badass");
		assertThat(administrativeUnit2.getParent()).isEqualTo(administrativeUnit1.getId());

		AdministrativeUnit administrativeUnit3 = rm.wrapAdministrativeUnit(expectedRecordWithLegacyId("42"));
		assertThat(administrativeUnit3.getCode()).isEqualTo("2014AKA2");
		assertThat(administrativeUnit3.getDescription()).isNull();
		assertThat(administrativeUnit3.getTitle()).isEqualTo("Administrative Unit with magical poney inside");
		assertThat(administrativeUnit3.getParent()).isEqualTo(administrativeUnit1.getId());

		folder1 = rm.wrapFolder(expectedRecordWithLegacyId("660"));
		assertThat(folder1.getAdministrativeUnitEntered()).isEqualTo(administrativeUnit1.getId());
		assertThat(folder1.getCategoryEntered()).isEqualTo(category3.getId());
		assertThat(folder1.getCopyStatusEntered()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folder1.getKeywords()).isEqualTo(asList("frozen", "wonderland"));
		// TODO vprigent: Fix to only test in the XML import
		//assertThat(folder1.getWrappedRecord().get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		assertThat(folder1.getMediumTypes()).isEqualTo(
				asList(rm.PA(), rm.DM()));
		assertThat(folder1.getActualTransferDate()).isEqualTo(new LocalDate(2010, 5, 29));
		assertThat(folder1.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folder1.getTitle()).isEqualTo("A Wonderful and Cold Folder");

		folder2 = rm.wrapFolder(expectedRecordWithLegacyId("670"));
		assertThat(folder2.getAdministrativeUnitEntered()).isEqualTo(administrativeUnit1.getId());
		assertThat(folder2.getCategoryEntered()).isEqualTo(category2.getId());
		assertThat(folder2.getCopyStatusEntered()).isEqualTo(CopyType.SECONDARY);
		assertThat(folder2.getKeywords()).isNullOrEmpty();
		assertThat(folder2.getMediumTypes()).isNullOrEmpty();
		assertThat(folder2.getActualTransferDate()).isEqualTo(new LocalDate(2010, 5, 29));
		assertThat(folder2.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folder2.getTitle()).isEqualTo("A Wonderful |#!/$%?*()_+ Folder");

		folder3 = rm.wrapFolder(expectedRecordWithLegacyId("661"));
		assertThat(folder3.getParentFolder()).isEqualTo(folder1.getId());
		assertThat(folder3.getTitle()).isEqualTo("A Wonderful and Cold (Sub)Folder");
		assertThat(folder3.getKeywords()).isEmpty();

		folder4 = rm.wrapFolder(expectedRecordWithLegacyId("662"));
		assertThat(folder4.getParentFolder()).isEqualTo(folder1.getId());
		assertThat(folder4.getTitle()).isEqualTo("A Wonderful and Cold (Sub)Folder Again");
		assertThat(folder4.getKeywords()).isEmpty();

	}

	private void importAndValidateEvents() {
		Folder folder660 = rm.getFolderWithLegacyId("660");
		Folder folder670 = rm.getFolderWithLegacyId("670");
		ContainerRecord containerRecord412903 = rm.getContainerRecordWithLegacyId("412903");
		Event event1 = rm.wrapEvent(expectedRecordWithLegacyId("event1"));
		assertThat(event1.getType()).isEqualTo(EventType.CREATE_FOLDER);
		assertThat(event1.getRecordId()).isEqualTo(folder660.getId());
		assertThat(event1.getTitle()).isEqualTo("A Wonderful and Cold Folder");
		assertThat(event1.getCreatedOn()).isEqualTo(dateTime(2002, 2, 2, 2, 2, 2));

		Event event2 = rm.wrapEvent(expectedRecordWithLegacyId("event2"));
		assertThat(event2.getType()).isEqualTo(EventType.MODIFY_FOLDER);
		assertThat(event2.getRecordId()).isEqualTo(folder670.getId());
		assertThat(event2.getTitle()).isEqualTo("A Wonderful |#!/$%?*()_+ Folder");
		assertThat(event2.getCreatedOn()).isEqualTo(dateTime(2003, 3, 3, 3, 3, 3));

		Event event3 = rm.wrapEvent(expectedRecordWithLegacyId("event3"));
		assertThat(event3.getType()).isEqualTo(EventType.BORROW_CONTAINER);
		assertThat(event3.getRecordId()).isEqualTo(containerRecord412903.getId());
		assertThat(event3.getTitle()).isEqualTo("00001");
		assertThat(event3.getCreatedOn()).isEqualTo(dateTime(2004, 4, 4, 4, 4, 4));
	}

	private void importAndValidateDocumentWithVersions() {
		String testResourceHash = "jLWaqQbCOSAPT4G3P75XnJJOmmo=";
		String testSecondResourceHash = "I_9qXqJxoU3dKHeM8bM_S4j8eIE=";

		Document document1 = rm.wrapDocument(expectedRecordWithLegacyId("00000000001"));
		Content content1 = document1.getContent();
		assertThat(document1.getTitle()).isEqualTo("The wonderful life of Dakota : An unexpected Journey");
		assertThat(document1.getFolder()).isEqualTo(folder4.getId());
		assertThat(content1).isNotNull();
		assertThat(content1.getCurrentVersion().getHash()).isEqualTo(testResourceHash);
		assertThat(content1.getCurrentVersion().getFilename()).isEqualTo("An unexpected Journey");
		assertThat(content1.getCurrentVersion().getVersion()).isEqualTo("1.0");

		Document document2 = rm.wrapDocument(expectedRecordWithLegacyId("00000000002"));
		Content content2 = document2.getContent();
		assertThat(document2.getTitle()).isEqualTo("The wonderful life of Dakota : The Return of the Jedi");
		assertThat(document2.getFolder()).isEqualTo(folder4.getId());
		assertThat(content2).isNotNull();
		assertThat(content2.getCurrentVersion().getHash()).isEqualTo(testResourceHash);
		assertThat(content2.getCurrentVersion().getFilename()).isEqualTo("The Return of the Jedi");
		assertThat(content2.getCurrentVersion().getVersion()).isEqualTo("0.1");

		Document document3 = rm.wrapDocument(expectedRecordWithLegacyId("00000000003"));
		Content content3 = document3.getContent();
		assertThat(document3.getTitle()).isEqualTo("The wonderful life of Dakota : The Kings Return");
		assertThat(document3.getFolder()).isEqualTo(folder4.getId());
		assertThat(content3).isNotNull();

		assertThat(content3.getVersions()).extracting("filename", "version", "comment")
				.isEqualTo(asList(
						tuple("The Kings Return1", "1.0", "DVD #1"),
						tuple("The Kings Return2", "1.1", "DVD #2"),
						tuple("The Kings Return3", "2.0", "DVD #3 : extras")
				));

		assertThat(content3.getCurrentVersion().getHash()).isEqualTo(testSecondResourceHash);
		assertThat(content3.getCurrentVersion().getFilename()).isEqualTo("The Kings Return3");
		assertThat(content3.getCurrentVersion().getVersion()).isEqualTo("2.0");
	}

	private void importAndValidateWithModifications(ImportDataProvider modifiedDatas)
			throws ValidationException {
		importServices.bulkImport(modifiedDatas, progressionListener, admin);

		Category category1 = rm.wrapCategory(expectedRecordWithLegacyId("22200"));
		assertThat(category1.getCode()).isEqualTo("X2222");
		assertThat(category1.getTitle()).isEqualTo("Element Category");
		assertThat(category1.getDescription()).isEqualTo("earth, water, fire and wind");
		assertThat(category1.getKeywords()).isEqualTo(asList("grass", "aqua", "torch", "gas"));
		assertThat(category1.getRententionRules()).isEqualTo(asList(records.ruleId_1, records.ruleId_2));
		assertThat(category1.getParent()).isNullOrEmpty();

		Category category2 = rm.wrapCategory(expectedRecordWithLegacyId("22230"));
		assertThat(category2.getCode()).isEqualTo("X2223");
		assertThat(category2.getTitle()).isEqualTo("Water Category");
		assertThat(category2.getDescription()).isEqualTo("It is so cold here...");
		assertThat(category2.getKeywords()).isEqualTo(emptyList());
		assertThat(category2.getRententionRules()).isEmpty();
		assertThat(category2.getParent()).isEqualTo(category1.getId());

		Category category3 = rm.wrapCategory(expectedRecordWithLegacyId("22231"));
		assertThat(category3.getCode()).isEqualTo("X22231");
		assertThat(category3.getTitle()).isEqualTo("Tsunami Category");
		assertThat(category3.getDescription()).isNull();
		assertThat(category3.getKeywords()).isEqualTo(asList("àqûä", "%wet%", "a_p_o_c_a_l_y_p_s_e"));
		assertThat(category3.getRententionRules()).isEqualTo(asList(records.ruleId_1));
		assertThat(category3.getParent()).isEqualTo(category2.getId());

		AdministrativeUnit administrativeUnit1 = rm.wrapAdministrativeUnit(expectedRecordWithLegacyId("40"));
		assertThat(administrativeUnit1.getCode()).isEqualTo("2014AKA");
		assertThat(administrativeUnit1.getDescription()).isEqualTo("I am a very wonderful Administrative Unit !");
		assertThat(administrativeUnit1.getTitle()).isEqualTo("Administrative Unit Wonderful");
		assertThat(administrativeUnit1.getParent()).isNull();

		AdministrativeUnit administrativeUnit2 = rm.wrapAdministrativeUnit(expectedRecordWithLegacyId("41"));
		assertThat(administrativeUnit2.getCode()).isEqualTo("2014AKA1");
		assertThat(administrativeUnit2.getDescription()).isNullOrEmpty();
		assertThat(administrativeUnit2.getTitle()).isEqualTo("Administrative Unit Badass");
		assertThat(administrativeUnit2.getParent()).isEqualTo(administrativeUnit1.getId());

		AdministrativeUnit administrativeUnit3 = rm.wrapAdministrativeUnit(expectedRecordWithLegacyId("42"));
		assertThat(administrativeUnit3.getCode()).isEqualTo("2014AKA2");
		assertThat(administrativeUnit3.getDescription()).isNull();
		assertThat(administrativeUnit3.getTitle()).isEqualTo("Administrative Unit with magical poney inside");
		assertThat(administrativeUnit3.getParent()).isEqualTo(administrativeUnit2.getId());

		Folder folder1 = rm.wrapFolder(expectedRecordWithLegacyId("660"));
		assertThat(folder1.getAdministrativeUnitEntered()).isEqualTo(administrativeUnit1.getId());
		assertThat(folder1.getCategoryEntered()).isEqualTo(category3.getId());
		assertThat(folder1.getCopyStatusEntered()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folder1.getKeywords()).isEqualTo(asList("frozen", "wonderland"));
		assertThat(folder1.getMediumTypes()).isEqualTo(asList(rm.PA(), rm.DM()));
		assertThat(folder1.getActualTransferDate()).isEqualTo(new LocalDate(2010, 6, 30));
		assertThat(folder1.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folder1.getTitle()).isEqualTo("A Wonderful and Cold Folder");

		Folder folder2 = rm.wrapFolder(expectedRecordWithLegacyId("670"));
		assertThat(folder2.getAdministrativeUnitEntered()).isEqualTo(administrativeUnit1.getId());
		assertThat(folder2.getCategoryEntered()).isEqualTo(category2.getId());
		assertThat(folder2.getCopyStatusEntered()).isEqualTo(CopyType.SECONDARY);
		assertThat(folder2.getKeywords()).isNullOrEmpty();
		assertThat(folder2.getMediumTypes()).isNullOrEmpty();
		assertThat(folder2.getActualTransferDate()).isEqualTo(new LocalDate(2010, 5, 29));
		assertThat(folder2.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folder2.getTitle()).isEqualTo("A Wonderful |#!/$%?*()_+ Folder");

		Folder folder3 = rm.wrapFolder(expectedRecordWithLegacyId("661"));
		assertThat(folder3.getParentFolder()).isEqualTo(folder1.getId());
		assertThat(folder3.getTitle()).isEqualTo("A Wonderful and Cold (Sub)Folder");
		assertThat(folder3.getKeywords()).isEmpty();

		Folder folder4 = rm.wrapFolder(expectedRecordWithLegacyId("662"));
		assertThat(folder4.getParentFolder()).isEqualTo(folder1.getId());
		assertThat(folder4.getTitle()).isEqualTo("A Wonderful and Cold (Sub)Folder");
		assertThat(folder4.getKeywords()).isEqualTo(asList("again and again", "again"));
		assertThat(folder4.getActualTransferDate()).isEqualTo(new LocalDate(2010, 8, 30));

	}

	private void importAndValidateWithRetentionRules()
			throws RecordServicesException {

		LocalDate localDate = new LocalDate(2015, 5, 1);

		RetentionRule rule1 = rm.wrapRetentionRule(expectedRecordWithLegacyId("1"));
		assertThat(rule1.isApproved()).isTrue();
		assertThat(rule1.getApprovalDate()).isEqualTo(localDate);
		assertThat(rule1.getTitle()).isEqualTo("Rule#1");
		assertThat(rule1.getCorpus()).isNull();
		assertThat(rule1.getCorpusRuleNumber()).isNull();
		assertThat(rule1.getAdministrativeUnits()).isEqualTo(
				asList(expectedRecordWithLegacyId("42").getId(), expectedRecordWithLegacyId("41").getId()));
		assertThat(rule1.getKeywords()).isEqualTo(asList("Rule #1"));
		assertThat(rule1.getHistory()).isNull();
		assertThat(rule1.isEssentialDocuments()).isTrue();
		assertThat(rule1.isConfidentialDocuments()).isFalse();
		assertThat(rule1.getDocumentTypes().contains(rm.getDocumentTypeByCode("2").getId())).isTrue();
		assertThat(rule1.getSecondaryCopy().getMediumTypeIds()).isEqualTo(asList(rm.getMediumTypeByCode("DM").getId()));
		assertThat(rule1.getSecondaryCopy().getInactiveDisposalType()).isEqualTo(DESTRUCTION);
		assertThat(rule1.getPrincipalCopies().size()).isEqualTo(1);
		assertThat(rule1.getPrincipalCopies().get(0).getInactiveDisposalType()).isEqualTo(DEPOSIT);

		update(rm.getCategoryWithCode("Z999").setRetentionRules(asList(rule1)).getWrappedRecord());
		assertThat((retentionRulesFromCategory("Z999")).contains(rule1.getId())).isTrue();

		update(rm.getUniformSubdivision("subdivId_1").setRetentionRules(asList(rule1.getId())).getWrappedRecord());
		assertThat(retentionRulesFromSubdivion("subdivId_1").contains(rule1.getId())).isTrue();

		RetentionRule ruleAdministration = rm.wrapRetentionRule(expectedRecordWithLegacyId("2"));
		assertThat(ruleAdministration.isApproved()).isTrue();
		assertThat(ruleAdministration.getApprovalDate()).isEqualTo(localDate);
		assertThat(ruleAdministration.getTitle()).isEqualTo("Conseil d'administration");
		assertThat(ruleAdministration.getCorpus()).isNull();
		assertThat(ruleAdministration.getCorpusRuleNumber()).isEqualTo(
				"Hello, i'm just some text from the corpus, trying to test the feature");
		assertThat(ruleAdministration.getAdministrativeUnits()).isEqualTo(asList(expectedRecordWithLegacyId("40").getId()));
		assertThat(ruleAdministration.getKeywords()).isEmpty();
		assertThat(ruleAdministration.getHistory()).isNull();
		assertThat(ruleAdministration.isEssentialDocuments()).isFalse();
		assertThat(ruleAdministration.isConfidentialDocuments()).isTrue();
		assertThat(ruleAdministration.getDocumentTypes().contains(rm.getDocumentTypeByCode("1").getId())).isTrue();
		assertThat(ruleAdministration.getSecondaryCopy().getMediumTypeIds())
				.isEqualTo(asList(rm.getMediumTypeByCode("DM").getId(), rm.getMediumTypeByCode("PA").getId()));
		assertThat(ruleAdministration.getSecondaryCopy().getInactiveDisposalType()).isEqualTo(DESTRUCTION);
		assertThat(ruleAdministration.getPrincipalCopies().size()).isEqualTo(3);

		assertThat(ruleAdministration.getPrincipalCopies()).extracting("code").containsOnly("123", "456", "789");

		CopyRetentionRule copy1 = ruleAdministration.getPrincipalCopies().get(0);
		CopyRetentionRule copy2 = ruleAdministration.getPrincipalCopies().get(1);

		//		assertThat(copy1.getInactiveDisposalType()).isEqualTo(DEPOSIT);
		//		assertThat(copy1.getMediumTypeIds())
		//				.isEqualTo(asList(rm.getMediumTypeByCode("PA").getId()));
		//		assertThat(copy1.getActiveRetentionPeriod()).isEqualTo(RetentionPeriod.OPEN_999);
		//		assertThat(copy1.getActiveRetentionComment()).isEqualTo("R1");
		//		assertThat(copy1.getSemiActiveRetentionPeriod()).isEqualTo(
		//				RetentionPeriod.fixed(1));
		//		assertThat(copy1.getSemiActiveRetentionComment()).isNullOrEmpty();
		//
		//		assertThat(copy2.getInactiveDisposalType()).isEqualTo(SORT);
		//		assertThat(copy2.getCode()).isEqualTo("123");
		//		assertThat(copy2.getCopyType()).isEqualTo(CopyType.PRINCIPAL);
		//		assertThat(copy2.getContentTypesComment()).isEqualTo("Some content");
		//		assertThat(copy2.getActiveRetentionPeriod()).isEqualTo(RetentionPeriod.fixed(3));
		//		assertThat(copy2.getActiveRetentionComment()).isEqualTo("R2");
		//		assertThat(copy2.getSemiActiveRetentionPeriod())
		//				.isEqualTo(RetentionPeriod.fixed(5));
		//		assertThat(copy2.getSemiActiveRetentionComment()).isEqualTo("R3");
		//		assertThat(copy2.getInactiveDisposalType()).isEqualTo(DisposalType.SORT);
		//		assertThat(copy2.getInactiveDisposalComment()).isNullOrEmpty();
		//		assertThat(copy2.getMediumTypeIds())
		//				.isEqualTo(asList(rm.getMediumTypeByCode("DM").getId()));

		update(rm.getCategoryWithCode("Z999").setRetentionRules(asList(ruleAdministration, rule1)).getWrappedRecord());
		assertThat(retentionRulesFromCategory("Z999").containsAll(asList(rule1.getId(), ruleAdministration.getId()))).isTrue();

		update(rm.getUniformSubdivision("subdivId_2").setRetentionRules(asList(ruleAdministration.getId()))
				.getWrappedRecord());
		assertThat(retentionRulesFromSubdivion("subdivId_2").contains(ruleAdministration.getId())).isTrue();
	}

	private void importAndValidateDocumentType() {

		DocumentType one = rm.wrapDocumentType(expectedRecordWithLegacyId("1010"));

		assertThat(one.getTitle()).isEqualTo("Table de pierre");
		assertThat(one.getCode()).isEqualTo("TP");

		DocumentType two = rm.wrapDocumentType(expectedRecordWithLegacyId("2020"));

		assertThat(two.getTitle()).isEqualTo("Verre");
		assertThat(two.getCode()).isEqualTo("VE");

	}

	// ----------------------------------------

	private File buildZipWith(String... files)
			throws Exception {

		File tempFolder = newTempFolder();
		File zipFile = new File(newTempFolder(), StringUtils.replace(StringUtils.join(files, "_"), ":", "-") + "testdata.zip");

		for (String file : files) {
			String filenameInTempFolder = file;
			String resourceFilename = file;
			if (file.contains(":")) {
				resourceFilename = file.split(":")[0];
				filenameInTempFolder = file.split(":")[1];
			}

			File fileInTempFolder = new File(tempFolder, filenameInTempFolder);
			File resourceFile = getTestResourceFile(resourceFilename);
			FileUtils.copyFile(resourceFile, fileInTempFolder);
		}

		getIOLayerFactory().newZipService().zip(zipFile, asList(tempFolder.listFiles()));

		return zipFile;
	}

	private File buildZipWithContent(List<File> contentFiles, String... templateFiles)
			throws Exception {

		File tempFolder = newTempFolder();
		File zipFile = new File(newTempFolder(), StringUtils.replace(StringUtils.join(templateFiles, "_"), ":", "-") + "testdata.zip");

		for (String file : templateFiles) {
			String filenameInTempFolder = file;
			String resourceFilename = file;
			if (file.contains(":")) {
				resourceFilename = file.split(":")[0];
				filenameInTempFolder = file.split(":")[1];
			}

			File fileInTempFolder = new File(tempFolder, filenameInTempFolder);
			File resourceFile = getTestResourceFile(resourceFilename);
			FileUtils.copyFile(resourceFile, fileInTempFolder);
		}

		if (!contentFiles.isEmpty()) {
			File dataFolder = new File(tempFolder, "data");
			for (File contentFile : contentFiles) {
				File destination = new File(dataFolder, contentFile.getName());
				FileUtils.copyFile(contentFile, destination);
			}
		}


		getIOLayerFactory().newZipService().zip(zipFile, asList(tempFolder.listFiles()));

		return zipFile;
	}

	private XMLImportDataProvider toXMLFile(String name) {
		File resourceFile = getTestResourceFile(name);
		File tempFile = new File(newTempFolder(), name);
		try {
			FileUtils.copyFile(resourceFile, tempFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return XMLImportDataProvider.forSingleXMLFile(getModelLayerFactory(), tempFile);
	}

	private Record expectedRecordWithLegacyId(String legacyId) {
		Record record = recordWithLegacyId(legacyId);
		assertThat(record).describedAs("Record with legacy id '" + legacyId + "' should exist");
		return record;
	}

	private Record recordWithLegacyId(String legacyId) {
		return getModelLayerFactory().newSearchServices().searchSingleResult(
				fromAllSchemasIn(zeCollection).where(LEGACY_ID).isEqualTo(legacyId));
	}

	private List<String> retentionRulesFromCategory(String code) {
		return rm.getCategoryWithCode(code).getRententionRules();
	}

	private List<String> retentionRulesFromSubdivion(String subdivId) {
		return rm.getUniformSubdivision(subdivId).getRetentionRules();
	}

	private void update(Record record)
			throws RecordServicesException {
		getModelLayerFactory().newRecordServices().update(record);
	}
}
