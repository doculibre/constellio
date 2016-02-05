package com.constellio.app.services.schemas.bulkImport;

import static com.constellio.app.modules.rm.model.enums.DisposalType.DEPOSIT;
import static com.constellio.app.modules.rm.model.enums.DisposalType.DESTRUCTION;
import static com.constellio.app.modules.rm.model.enums.DisposalType.SORT;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.excel.Excel2003ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.xml.XMLImportDataProvider;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

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

	private LocalDateTime now = new LocalDateTime().minusHours(3);

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
		);

		givenTimeIs(now);

		importServices = new RecordsImportServices(getModelLayerFactory());

		admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
	}

	@Test
	public void whenImportingZipOfXMLFilesThenImportedCorrectly()
			throws Exception {

		File zipFile = buildZipWith("administrativeUnit.xml", "category.xml", "folder.xml", "document.xml",
				"retentionRule.xml", "ddvDocumentType.xml");

		importServices.bulkImport(XMLImportDataProvider.forZipFile(getModelLayerFactory(), zipFile), progressionListener, admin);

		importAndValidate();
		importAndValidateWithRetentionRules();
		importAndValidateDocumentType();
		importAndValidateDocumentWithVersions();
	}

	@Test
	public void whenImportingXMLFilesWithCreationModificationInfosThenImportedCorrectly()
			throws Exception {

		File zipFile = buildZipWith("administrativeUnit.xml", "category.xml",
				"folderWithCreationModificationInfos.xml:folder.xml",
				"documentWithCreationModificationInfos.xml:document.xml", "retentionRule.xml", "ddvDocumentType.xml");

		importServices.bulkImport(XMLImportDataProvider.forZipFile(getModelLayerFactory(), zipFile), progressionListener, admin);

		LocalDateTime shishOClock = new LocalDateTime().minusDays(1);
		givenTimeIs(shishOClock);

		Folder folder660 = rm.getFolderByLegacyId("660");
		assertThat(folder660.getFormCreatedBy()).isEqualTo(users.aliceIn(zeCollection).getId());
		assertThat(folder660.getFormCreatedOn()).isEqualTo(new LocalDateTime(2001, 1, 1, 1, 1, 1));
		assertThat(folder660.getFormModifiedBy()).isEqualTo(users.bobIn(zeCollection).getId());
		assertThat(folder660.getFormModifiedOn()).isEqualTo(new LocalDateTime(2002, 2, 2, 2, 2, 2));

		Folder folder670 = rm.getFolderByLegacyId("670");
		assertThat(folder670.getFormCreatedBy()).isEqualTo(users.charlesIn(zeCollection).getId());
		assertThat(folder670.getFormCreatedOn()).isEqualTo(new LocalDateTime(2001, 1, 1, 1, 1, 1));
		assertThat(folder670.getFormModifiedBy()).isEqualTo(users.dakotaLIndienIn(zeCollection).getId());
		assertThat(folder670.getFormModifiedOn()).isEqualTo(new LocalDateTime(2002, 2, 2, 2, 2, 2));

		Folder folder661 = rm.getFolderByLegacyId("661");
		assertThat(folder661.getFormCreatedBy()).isEqualTo(users.edouardIn(zeCollection).getId());
		assertThat(folder661.getFormCreatedOn()).isEqualTo(new LocalDateTime(2003, 3, 3, 3, 3, 3));
		assertThat(folder661.getFormModifiedBy()).isEqualTo(users.gandalfIn(zeCollection).getId());
		assertThat(folder661.getFormModifiedOn()).isEqualTo(new LocalDateTime(2004, 4, 4, 4, 4, 4));

		Folder folder662 = rm.getFolderByLegacyId("662");
		assertThat(folder662.getFormCreatedBy()).isNull();
		assertThat(folder662.getFormCreatedOn()).isNull();
		assertThat(folder662.getFormModifiedBy()).isNull();
		assertThat(folder662.getFormModifiedOn()).isNull();

		Document document1 = rm.getDocumentByLegacyId("00000000001");
		assertThat(document1.getFormCreatedBy()).isEqualTo(users.aliceIn(zeCollection).getId());
		assertThat(document1.getFormCreatedOn()).isEqualTo(new LocalDateTime(2001, 1, 1, 1, 1, 1));
		assertThat(document1.getFormModifiedBy()).isEqualTo(users.bobIn(zeCollection).getId());
		assertThat(document1.getFormModifiedOn()).isEqualTo(new LocalDateTime(2002, 2, 2, 2, 2, 2));

		Document document3 = rm.getDocumentByLegacyId("00000000003");
		assertThat(document3.getFormCreatedBy()).isEqualTo(users.charlesIn(zeCollection).getId());
		assertThat(document3.getFormCreatedOn()).isEqualTo(new LocalDateTime(2001, 1, 1, 1, 1, 1));
		assertThat(document3.getFormModifiedBy()).isEqualTo(users.dakotaIn(zeCollection).getId());
		assertThat(document3.getFormModifiedOn()).isEqualTo(new LocalDateTime(2002, 2, 2, 2, 2, 2));

		Document document2 = rm.getDocumentByLegacyId("00000000002");
		assertThat(document2.getFormCreatedBy()).isNull();
		assertThat(document2.getFormCreatedOn()).isNull();
		assertThat(document2.getFormModifiedBy()).isNull();
		assertThat(document2.getFormModifiedOn()).isNull();

		//Update
		zipFile = buildZipWith("administrativeUnit.xml", "category.xml",
				"folderWithCreationModificationInfos2.xml:folder.xml",
				"documentWithCreationModificationInfos2.xml:document.xml", "retentionRule.xml", "ddvDocumentType.xml");
		importServices.bulkImport(XMLImportDataProvider.forZipFile(getModelLayerFactory(), zipFile), progressionListener, admin);

		folder660 = rm.getFolderByLegacyId("660");
		assertThat(folder660.getFormCreatedBy()).isEqualTo(users.aliceIn(zeCollection).getId());
		assertThat(folder660.getFormCreatedOn()).isEqualTo(new LocalDateTime(2001, 1, 1, 1, 1, 1));
		assertThat(folder660.getFormModifiedBy()).isEqualTo(users.bobIn(zeCollection).getId());
		assertThat(folder660.getFormModifiedOn()).isEqualTo(new LocalDateTime(2002, 2, 2, 2, 2, 2));

		folder670 = rm.getFolderByLegacyId("670");
		assertThat(folder670.getFormCreatedBy()).isEqualTo(users.chuckNorrisIn(zeCollection).getId());
		assertThat(folder670.getFormCreatedOn()).isEqualTo(new LocalDateTime(3001, 1, 1, 1, 1, 1));
		assertThat(folder670.getFormModifiedBy()).isEqualTo(users.chuckNorrisIn(zeCollection).getId());
		assertThat(folder670.getFormModifiedOn()).isEqualTo(new LocalDateTime(3002, 2, 2, 2, 2, 2));

		folder661 = rm.getFolderByLegacyId("661");
		assertThat(folder661.getFormCreatedBy()).isEqualTo(users.chuckNorrisIn(zeCollection).getId());
		assertThat(folder661.getFormCreatedOn()).isEqualTo(new LocalDateTime(3003, 3, 3, 3, 3, 3));
		assertThat(folder661.getFormModifiedBy()).isEqualTo(users.chuckNorrisIn(zeCollection).getId());
		assertThat(folder661.getFormModifiedOn()).isEqualTo(new LocalDateTime(3004, 4, 4, 4, 4, 4));

		folder662 = rm.getFolderByLegacyId("662");
		assertThat(folder662.getFormCreatedBy()).isEqualTo(users.aliceIn(zeCollection).getId());
		assertThat(folder662.getFormCreatedOn()).isEqualTo(new LocalDateTime(2001, 1, 1, 1, 1, 1));
		assertThat(folder662.getFormModifiedBy()).isEqualTo(users.bobIn(zeCollection).getId());
		assertThat(folder662.getFormModifiedOn()).isEqualTo(new LocalDateTime(2002, 2, 2, 2, 2, 2));

		document1 = rm.getDocumentByLegacyId("00000000001");
		assertThat(document1.getFormCreatedBy()).isEqualTo(users.aliceIn(zeCollection).getId());
		assertThat(document1.getFormCreatedOn()).isEqualTo(new LocalDateTime(2001, 1, 1, 1, 1, 1));
		assertThat(document1.getFormModifiedBy()).isEqualTo(users.bobIn(zeCollection).getId());
		assertThat(document1.getFormModifiedOn()).isEqualTo(new LocalDateTime(2002, 2, 2, 2, 2, 2));

		document3 = rm.getDocumentByLegacyId("00000000003");
		assertThat(document3.getFormCreatedBy()).isEqualTo(users.chuckNorrisIn(zeCollection).getId());
		assertThat(document3.getFormCreatedOn()).isEqualTo(new LocalDateTime(3001, 1, 1, 1, 1, 1));
		assertThat(document3.getFormModifiedBy()).isEqualTo(users.chuckNorrisIn(zeCollection).getId());
		assertThat(document3.getFormModifiedOn()).isEqualTo(new LocalDateTime(3002, 2, 2, 2, 2, 2));

		document2 = rm.getDocumentByLegacyId("00000000002");
		assertThat(document2.getFormCreatedBy()).isEqualTo(users.aliceIn(zeCollection).getId());
		assertThat(document2.getFormCreatedOn()).isEqualTo(new LocalDateTime(2001, 1, 1, 1, 1, 1));
		assertThat(document2.getFormModifiedBy()).isEqualTo(users.bobIn(zeCollection).getId());
		assertThat(document2.getFormModifiedOn()).isEqualTo(new LocalDateTime(2002, 2, 2, 2, 2, 2));

	}

	@Test
	public void whenImportingXMLFilesSeparatelyThenImportedCorrectly()
			throws Exception {

		XMLImportDataProvider administrativeUnit = toXMLFile("administrativeUnit.xml");
		XMLImportDataProvider category = toXMLFile("category.xml");
		XMLImportDataProvider folder = toXMLFile("folder.xml");
		XMLImportDataProvider document = toXMLFile("document.xml");
		XMLImportDataProvider retentionRule = toXMLFile("retentionRule.xml");
		XMLImportDataProvider ddvDocumentType = toXMLFile("ddvDocumentType.xml");
		XMLImportDataProvider[] files = new XMLImportDataProvider[] {
				ddvDocumentType, category, administrativeUnit, retentionRule, folder, document, };

		for (ImportDataProvider importDataProvider : files) {
			importServices.bulkImport(importDataProvider, progressionListener, admin);
		}

		assertThat(administrativeUnit.size("administrativeUnit")).isEqualTo(3);
		assertThat(category.size("category")).isEqualTo(3);
		assertThat(folder.size("folder")).isEqualTo(4);
		assertThat(document.size("document")).isEqualTo(3);
		assertThat(retentionRule.size("retentionRule")).isEqualTo(2);
		assertThat(ddvDocumentType.size("ddvDocumentType")).isEqualTo(2);

		importAndValidate();
		importAndValidateWithRetentionRules();
		importAndValidateDocumentType();
		importAndValidateDocumentWithVersions();
	}

	@Test
	public void whenImportingAnExcelFileThenImportedCorrectly()
			throws Exception {

		File excelFile = getTestResourceFile("datas.xls");
		File excelFileModified = getTestResourceFile("datasModified.xls");

		importServices.bulkImport(Excel2003ImportDataProvider.fromFile(excelFile), progressionListener, admin);

		importAndValidate();
		importAndValidateWithModifications(Excel2003ImportDataProvider.fromFile(excelFileModified));
	}

	private void importAndValidate() {
		Category category1 = rm.wrapCategory(expectedRecordWithLegacyId("22200"));
		assertThat(category1.getCode()).isEqualTo("X2222");
		assertThat(category1.getTitle()).isEqualTo("Element Category");
		assertThat(category1.getDescription()).isEqualTo("earth, water, fire and wind");
		assertThat(category1.getKeywords()).isEqualTo(asList("grass", "aqua", "torch", "gas"));
		assertThat(category1.getRententionRules()).isEqualTo(asList(records.ruleId_1, records.ruleId_2));
		assertThat(category1.getParentCategory()).isNullOrEmpty();

		Category category2 = rm.wrapCategory(expectedRecordWithLegacyId("22230"));
		assertThat(category2.getCode()).isEqualTo("X2223");
		assertThat(category2.getTitle()).isEqualTo("Water Category");
		assertThat(category2.getDescription()).isEqualTo("It is so cold here...");
		assertThat(category2.getKeywords()).isEqualTo(asList("aqua", "wet", "rain"));
		assertThat(category2.getRententionRules()).isEmpty();
		assertThat(category2.getParentCategory()).isEqualTo(category1.getId());

		Category category3 = rm.wrapCategory(expectedRecordWithLegacyId("22231"));
		assertThat(category3.getCode()).isEqualTo("X22231");
		assertThat(category3.getTitle()).isEqualTo("Tsunami Category");
		assertThat(category3.getDescription()).isNull();
		assertThat(category3.getKeywords()).isEqualTo(asList("àqûä", "%wet%", "a_p_o_c_a_l_y_p_s_e"));
		assertThat(category3.getRententionRules()).isEqualTo(asList(records.ruleId_1));
		assertThat(category3.getParentCategory()).isEqualTo(category2.getId());

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

	private void importAndValidateDocumentWithVersions() {
		String testResourceHash = "jLWaqQbCOSAPT4G3P75XnJJOmmo=";
		String testSecondResourceHash = "I/9qXqJxoU3dKHeM8bM/S4j8eIE=";

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

		assertThat(content3.getVersions()).extracting("filename", "version", "comment", "lastModificationDateTime")
				.isEqualTo(asList(
						tuple("The Kings Return1", "1.0", "DVD #1", new LocalDateTime(2014, 6, 9, 23, 38, 45)),
						tuple("The Kings Return2", "1.1", "DVD #2", new LocalDateTime(2015, 6, 9, 23, 38, 45)),
						tuple("The Kings Return3", "2.0", "DVD #3 : extras", now)
				));

		assertThat(content3.getCurrentVersion().getHash()).isEqualTo(testSecondResourceHash);
		assertThat(content3.getCurrentVersion().getFilename()).isEqualTo("The Kings Return3");
		assertThat(content3.getCurrentVersion().getVersion()).isEqualTo("2.0");
	}

	private void importAndValidateWithModifications(ImportDataProvider modifiedDatas) {
		importServices.bulkImport(modifiedDatas, progressionListener, admin);

		Category category1 = rm.wrapCategory(expectedRecordWithLegacyId("22200"));
		assertThat(category1.getCode()).isEqualTo("X2222");
		assertThat(category1.getTitle()).isEqualTo("Element Category");
		assertThat(category1.getDescription()).isEqualTo("earth, water, fire and wind");
		assertThat(category1.getKeywords()).isEqualTo(asList("grass", "aqua", "torch", "gas"));
		assertThat(category1.getRententionRules()).isEqualTo(asList(records.ruleId_1, records.ruleId_2));
		assertThat(category1.getParentCategory()).isNullOrEmpty();

		Category category2 = rm.wrapCategory(expectedRecordWithLegacyId("22230"));
		assertThat(category2.getCode()).isEqualTo("X2223");
		assertThat(category2.getTitle()).isEqualTo("Water Category");
		assertThat(category2.getDescription()).isEqualTo("It is so cold here...");
		assertThat(category2.getKeywords()).isEqualTo(emptyList());
		assertThat(category2.getRententionRules()).isEmpty();
		assertThat(category2.getParentCategory()).isEqualTo(category1.getId());

		Category category3 = rm.wrapCategory(expectedRecordWithLegacyId("22231"));
		assertThat(category3.getCode()).isEqualTo("X22231");
		assertThat(category3.getTitle()).isEqualTo("Tsunami Category");
		assertThat(category3.getDescription()).isNull();
		assertThat(category3.getKeywords()).isEqualTo(asList("àqûä", "%wet%", "a_p_o_c_a_l_y_p_s_e"));
		assertThat(category3.getRententionRules()).isEqualTo(asList(records.ruleId_1));
		assertThat(category3.getParentCategory()).isEqualTo(category2.getId());

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
		assertThat(ruleAdministration.getPrincipalCopies().size()).isEqualTo(2);

		assertThat(ruleAdministration.getPrincipalCopies().get(0).getInactiveDisposalType()).isEqualTo(DEPOSIT);
		assertThat(ruleAdministration.getPrincipalCopies().get(0).getMediumTypeIds())
				.isEqualTo(asList(rm.getMediumTypeByCode("PA").getId()));
		assertThat(ruleAdministration.getPrincipalCopies().get(0).getActiveRetentionPeriod()).isEqualTo(RetentionPeriod.OPEN_999);
		assertThat(ruleAdministration.getPrincipalCopies().get(0).getActiveRetentionComment()).isEqualTo("R1");
		assertThat(ruleAdministration.getPrincipalCopies().get(0).getSemiActiveRetentionPeriod()).isEqualTo(
				RetentionPeriod.fixed(1));
		assertThat(ruleAdministration.getPrincipalCopies().get(0).getSemiActiveRetentionComment()).isNullOrEmpty();

		assertThat(ruleAdministration.getPrincipalCopies().get(1).getInactiveDisposalType()).isEqualTo(SORT);
		assertThat(ruleAdministration.getPrincipalCopies().get(1).getCode()).isEqualTo("123");
		assertThat(ruleAdministration.getPrincipalCopies().get(1).getCopyType()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(ruleAdministration.getPrincipalCopies().get(1).getContentTypesComment()).isEqualTo("Some content");
		assertThat(ruleAdministration.getPrincipalCopies().get(1).getActiveRetentionPeriod()).isEqualTo(RetentionPeriod.fixed(3));
		assertThat(ruleAdministration.getPrincipalCopies().get(1).getActiveRetentionComment()).isEqualTo("R2");
		assertThat(ruleAdministration.getPrincipalCopies().get(1).getSemiActiveRetentionPeriod())
				.isEqualTo(RetentionPeriod.fixed(5));
		assertThat(ruleAdministration.getPrincipalCopies().get(1).getSemiActiveRetentionComment()).isEqualTo("R3");
		assertThat(ruleAdministration.getPrincipalCopies().get(1).getInactiveDisposalType()).isEqualTo(DisposalType.SORT);
		assertThat(ruleAdministration.getPrincipalCopies().get(1).getInactiveDisposalComment()).isNullOrEmpty();
		assertThat(ruleAdministration.getPrincipalCopies().get(1).getMediumTypeIds())
				.isEqualTo(asList(rm.getMediumTypeByCode("DM").getId()));

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

		File zipFile = new File(newTempFolder(), Arrays.toString(files) + "testdata.zip");
		File tempFolder = newTempFolder();

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
				fromAllSchemasIn(zeCollection).where(Schemas.LEGACY_ID).isEqualTo(legacyId));
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
