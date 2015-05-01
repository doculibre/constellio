/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.records.bulkImport;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.bulkImport.data.ImportDataProvider;
import com.constellio.model.services.records.bulkImport.data.excel.ExcelImportDataProvider;
import com.constellio.model.services.records.bulkImport.data.xml.XMLImportDataProvider;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;

public class RecordsImportServicesWithXMLAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;
	RMTestRecords records;
	LocalDateTime shishOClock = new LocalDateTime().minusHours(1);

	BulkImportProgressionListener progressionListener = new LoggerBulkImportProgressionListener();
	RecordsImportServices importServices, importServicesWithTransactionsOf1Record;
	SearchServices searchServices;
	User admin;

	@Before
	public void setUp()
			throws Exception {

		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();

		progressionListener = new LoggerBulkImportProgressionListener();
		importServices = new RecordsImportServices(getModelLayerFactory());
		importServicesWithTransactionsOf1Record = new RecordsImportServices(getModelLayerFactory(), 1);
		searchServices = getModelLayerFactory().newSearchServices();

		admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory());
	}

	@Test
	public void whenImportingZipOfXMLFilesThenImportedCorrectly()
			throws Exception {

		File zipFile = buildZipWith("administrativeUnit.xml", "category.xml", "filingSpace.xml", "folder.xml", "document.xml");
		importAndValidate(XMLImportDataProvider.forZipFile(getModelLayerFactory(), zipFile));
	}

	@Test
	public void whenImportingAnExcelFileThenImportedCorrectly()
			throws Exception {

		File excelFile = getTestResourceFile("datas.xls");
		File excelFileModified = getTestResourceFile("datasModified.xls");

		importAndValidate(ExcelImportDataProvider.fromFile(excelFile));
		importAndValidateWithModifications(ExcelImportDataProvider.fromFile(excelFile), ExcelImportDataProvider.fromFile(excelFileModified));
	}

	private void importAndValidate(ImportDataProvider importDataProvider) {
		importServices.bulkImport(importDataProvider, progressionListener, admin);

		FilingSpace filingSpace1 = rm.wrapFilingSpace(expectedRecordWithLegacyId("22"));
		assertThat(filingSpace1.getCode()).isEqualTo("HOT");
		assertThat(filingSpace1.getDescription()).isEqualTo("I am a Hot Box.");
		assertThat(filingSpace1.getUsers()).isEqualTo(asList(records.getAlice().getId(), records.getChuckNorris().getId()));
		assertThat(filingSpace1.getAdministrators())
				.isEqualTo(asList(records.getAdmin().getId(), records.getBob_userInAC().getId()));

		FilingSpace filingSpace2 = rm.wrapFilingSpace(expectedRecordWithLegacyId("23"));
		assertThat(filingSpace2.getCode()).isEqualTo("COLD");
		assertThat(filingSpace2.getDescription()).isEqualTo("I am a Cold Box.");
		assertThat(filingSpace2.getUsers()).isEqualTo(
				asList(records.getAlice().getId(), records.getChuckNorris().getId(), records.getBob_userInAC().getId()));
		assertThat(filingSpace2.getAdministrators()).isEqualTo(asList(records.getCharles_userInA().getId()));

		FilingSpace filingSpace3 = rm.wrapFilingSpace(expectedRecordWithLegacyId("00"));
		assertThat(filingSpace3.getCode()).isEqualTo("3RR0R_B0X : S@S");
		assertThat(filingSpace3.getDescription()).isEqualTo("I am a Sick Box.");
		assertThat(filingSpace3.getUsers()).isEmpty();
		assertThat(filingSpace3.getAdministrators()).isNullOrEmpty();

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
		assertThat(administrativeUnit1.getFilingSpaces()).isEqualTo(asList(filingSpace2.getId()));

		AdministrativeUnit administrativeUnit2 = rm.wrapAdministrativeUnit(expectedRecordWithLegacyId("41"));
		assertThat(administrativeUnit2.getCode()).isEqualTo("2014AKA1");
		assertThat(administrativeUnit2.getDescription()).isNullOrEmpty();
		assertThat(administrativeUnit2.getTitle()).isEqualTo("Administrative Unit Badass");
		assertThat(administrativeUnit2.getParent()).isEqualTo(administrativeUnit1.getId());
		assertThat(administrativeUnit2.getFilingSpaces()).isEqualTo(asList(filingSpace1.getId(), filingSpace2.getId()));

		AdministrativeUnit administrativeUnit3 = rm.wrapAdministrativeUnit(expectedRecordWithLegacyId("42"));
		assertThat(administrativeUnit3.getCode()).isEqualTo("2014AKA2");
		assertThat(administrativeUnit3.getDescription()).isNull();
		assertThat(administrativeUnit3.getTitle()).isEqualTo("Administrative Unit with magical poney inside");
		assertThat(administrativeUnit3.getParent()).isEqualTo(administrativeUnit1.getId());
		assertThat(administrativeUnit3.getFilingSpaces()).isNullOrEmpty();

		Folder folder1 = rm.wrapFolder(expectedRecordWithLegacyId("660"));
		assertThat(folder1.getAdministrativeUnitEntered()).isEqualTo(administrativeUnit1.getId());
		assertThat(folder1.getCategoryEntered()).isEqualTo(category3.getId());
		assertThat(folder1.getCopyStatusEntered()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folder1.getFilingSpaceEntered()).isEqualTo(filingSpace2.getId());
		assertThat(folder1.getKeywords()).isEqualTo(asList("frozen", "wonderland"));
		if (importDataProvider instanceof ExcelImportDataProvider) {
			assertThat(folder1.getMediumTypes()).isEqualTo(asList("00000000001", "00000000003")); // codes des supports PA et DM, TODO mediumTypes non géré par xml
		}
		assertThat(folder1.getActualTransferDate()).isEqualTo(new LocalDate(2010, 05, 29));
		assertThat(folder1.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folder1.getTitle()).isEqualTo("A Wonderful and Cold Folder");

		Folder folder2 = rm.wrapFolder(expectedRecordWithLegacyId("670"));
		assertThat(folder2.getAdministrativeUnitEntered()).isEqualTo(administrativeUnit1.getId());
		assertThat(folder2.getCategoryEntered()).isEqualTo(category2.getId());
		assertThat(folder2.getCopyStatusEntered()).isEqualTo(CopyType.SECONDARY);
		assertThat(folder2.getFilingSpaceEntered()).isEqualTo(filingSpace3.getId());
		assertThat(folder2.getKeywords()).isNullOrEmpty();
		assertThat(folder2.getMediumTypes()).isNullOrEmpty();
		assertThat(folder2.getActualTransferDate()).isEqualTo(new LocalDate(2010, 05, 29));
		assertThat(folder2.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folder2.getTitle()).isEqualTo("A Wonderful |#!/$%?*()_+ Folder");

		Folder folder3 = rm.wrapFolder(expectedRecordWithLegacyId("661"));
		assertThat(folder3.getParentFolder()).isEqualTo(folder1.getId());
		assertThat(folder3.getTitle()).isEqualTo("A Wonderful and Cold (Sub)Folder");
		assertThat(folder3.getKeywords()).isEmpty();

		Folder folder4 = rm.wrapFolder(expectedRecordWithLegacyId("662"));
		assertThat(folder4.getParentFolder()).isEqualTo(folder1.getId());
		assertThat(folder4.getTitle()).isEqualTo("A Wonderful and Cold (Sub)Folder Again");
		assertThat(folder4.getKeywords()).isEmpty();

		String testResourceHash = "jLWaqQbCOSAPT4G3P75XnJJOmmo=";

		if (importDataProvider instanceof XMLImportDataProvider) {
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
			assertThat(content3.getCurrentVersion().getHash()).isEqualTo(testResourceHash);
			assertThat(content3.getCurrentVersion().getFilename()).isEqualTo("The Kings Return");
			assertThat(content3.getCurrentVersion().getVersion()).isEqualTo("0.1");
		}
	}

	private void importAndValidateWithModifications(ExcelImportDataProvider firstDatas,
			ExcelImportDataProvider modifiedDatas) {
		importServices.bulkImport(firstDatas, progressionListener, admin);
		importServices.bulkImport(modifiedDatas, progressionListener, admin);

		FilingSpace filingSpace1 = rm.wrapFilingSpace(expectedRecordWithLegacyId("22"));
		assertThat(filingSpace1.getCode()).isEqualTo("NOT HOT");
		assertThat(filingSpace1.getDescription()).isEqualTo("I am not anymore a Hot Box.");
		assertThat(filingSpace1.getUsers()).isEqualTo(asList(records.getAlice().getId(), records.getChuckNorris().getId()));
		assertThat(filingSpace1.getAdministrators())
				.isEqualTo(asList(records.getAdmin().getId(), records.getBob_userInAC().getId()));

		FilingSpace filingSpace2 = rm.wrapFilingSpace(expectedRecordWithLegacyId("23"));
		assertThat(filingSpace2.getCode()).isEqualTo("COLD");
		assertThat(filingSpace2.getDescription()).isEqualTo("I am a Cold Box.");
		assertThat(filingSpace2.getUsers()).isEqualTo(
				asList(records.getAlice().getId(), records.getChuckNorris().getId(), records.getBob_userInAC().getId()));
		assertThat(filingSpace2.getAdministrators()).isEqualTo(asList(records.getCharles_userInA().getId()));

		FilingSpace filingSpace3 = rm.wrapFilingSpace(expectedRecordWithLegacyId("00"));
		assertThat(filingSpace3.getCode()).isEqualTo("3RR0R_B0X : S@S");
		assertThat(filingSpace3.getDescription()).isEqualTo("I am a Sick Box.");
		assertThat(filingSpace3.getUsers()).isEmpty();
		assertThat(filingSpace3.getAdministrators()).isNullOrEmpty();

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
		assertThat(category2.getKeywords()).isEqualTo(asList());
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
		assertThat(administrativeUnit1.getFilingSpaces()).isEqualTo(asList(filingSpace2.getId()));

		AdministrativeUnit administrativeUnit2 = rm.wrapAdministrativeUnit(expectedRecordWithLegacyId("41"));
		assertThat(administrativeUnit2.getCode()).isEqualTo("2014AKA1");
		assertThat(administrativeUnit2.getDescription()).isNullOrEmpty();
		assertThat(administrativeUnit2.getTitle()).isEqualTo("Administrative Unit Badass");
		assertThat(administrativeUnit2.getParent()).isEqualTo(administrativeUnit1.getId());
		assertThat(administrativeUnit2.getFilingSpaces()).isEqualTo(asList(filingSpace1.getId(), filingSpace2.getId()));

		AdministrativeUnit administrativeUnit3 = rm.wrapAdministrativeUnit(expectedRecordWithLegacyId("42"));
		assertThat(administrativeUnit3.getCode()).isEqualTo("2014AKA2");
		assertThat(administrativeUnit3.getDescription()).isNull();
		assertThat(administrativeUnit3.getTitle()).isEqualTo("Administrative Unit with magical poney inside");
		assertThat(administrativeUnit3.getParent()).isEqualTo(administrativeUnit2.getId());
		assertThat(administrativeUnit3.getFilingSpaces()).isNullOrEmpty();

		Folder folder1 = rm.wrapFolder(expectedRecordWithLegacyId("660"));
		assertThat(folder1.getAdministrativeUnitEntered()).isEqualTo(administrativeUnit1.getId());
		assertThat(folder1.getCategoryEntered()).isEqualTo(category3.getId());
		assertThat(folder1.getCopyStatusEntered()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folder1.getFilingSpaceEntered()).isEqualTo(filingSpace2.getId());
		assertThat(folder1.getKeywords()).isEqualTo(asList("frozen", "wonderland"));
		//assertThat(folder1.getMediumTypes()).isEqualTo(asList("PA", "MD"));
		assertThat(folder1.getActualTransferDate()).isEqualTo(new LocalDate(2010, 06, 30));
		assertThat(folder1.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folder1.getTitle()).isEqualTo("A Wonderful and Cold Folder");

		Folder folder2 = rm.wrapFolder(expectedRecordWithLegacyId("670"));
		assertThat(folder2.getAdministrativeUnitEntered()).isEqualTo(administrativeUnit1.getId());
		assertThat(folder2.getCategoryEntered()).isEqualTo(category2.getId());
		assertThat(folder2.getCopyStatusEntered()).isEqualTo(CopyType.SECONDARY);
		assertThat(folder2.getFilingSpaceEntered()).isEqualTo(filingSpace3.getId());
		assertThat(folder2.getKeywords()).isNullOrEmpty();
		assertThat(folder2.getMediumTypes()).isNullOrEmpty();
		assertThat(folder2.getActualTransferDate()).isEqualTo(new LocalDate(2010, 05, 29));
		assertThat(folder2.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folder2.getTitle()).isEqualTo("A Wonderful |#!/$%?*()_+ Folder");

		Folder folder3 = rm.wrapFolder(expectedRecordWithLegacyId("661"));
		assertThat(folder3.getParentFolder()).isEqualTo(folder1.getId());
		assertThat(folder3.getTitle()).isEqualTo("A Wonderful and Cold (Sub)Folder");
		assertThat(folder3.getKeywords()).isEmpty();

		Folder folder4 = rm.wrapFolder(expectedRecordWithLegacyId("662"));
		assertThat(folder4.getParentFolder()).isEqualTo(folder1.getId());
		assertThat(folder4.getTitle()).isEqualTo("A Wonderful and Cold (Sub)Folder");
		assertThat(folder4.getKeywords()).isEqualTo(asList("again and again","again"));
		assertThat(folder4.getActualTransferDate()).isEqualTo(new LocalDate(2010, 8, 30));

	}

	private File buildZipWith(String... files)
			throws Exception {

		File zipFile = new File(newTempFolder(), "testdata.zip");
		File tempFolder = newTempFolder();

		for (String file : files) {
			File fileInTempFolder = new File(tempFolder, file);
			File resourceFile = getTestResourceFile(file);
			FileUtils.copyFile(resourceFile, fileInTempFolder);
		}

		getIOLayerFactory().newZipService().zip(zipFile, asList(tempFolder.listFiles()));

		return zipFile;
	}

	private Record expectedRecordWithLegacyId(String legacyId) {
		Record record = recordWithLegacyId(legacyId);
		assertThat(record).describedAs("Record with legacy id '" + legacyId + "' shold exist");
		return record;
	}

	private Record recordWithLegacyId(String legacyId) {
		return getModelLayerFactory().newSearchServices().searchSingleResult(
				fromAllSchemasIn(zeCollection).where(Schemas.LEGACY_ID).isEqualTo(legacyId));
	}

}
