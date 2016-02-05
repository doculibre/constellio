package com.constellio.app.services.schemas.bulkImport;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.schemas.bulkImport.data.xml.XMLImportDataProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.LoadTest;
import com.constellio.sdk.tests.setups.Users;

@LoadTest
public class RecordsImportServicesLoadTest extends ConstellioTest {

	private Users users = new Users();
	private RMSchemasRecordsServices rm;
	private RMTestRecords records = new RMTestRecords(zeCollection);

	private BulkImportProgressionListener progressionListener = new LoggerBulkImportProgressionListener();
	private RecordsImportServices importServices;
	private User admin;
	private SearchServices searchServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
		);

		importServices = new RecordsImportServices(getModelLayerFactory());

		admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		searchServices = getModelLayerFactory().newSearchServices();
	}

	//@Test
	public void generateImportFile()
			throws Exception {

		List<String> ids = new ArrayList<>();
		for (int i = 0; i < 10_000_000; i++) {
			ids.add("" + i);
		}
		Collections.shuffle(ids);

		File file1 = new File(
				"/Users/francisbaril/Downloads/RecordsImportServicesAcceptanceTest-folder.xml");
		File file2 = new File(
				"/Users/francisbaril/Downloads/RecordsImportServicesAcceptanceTest-folder2.xml");

		file1.delete();
		BufferedWriter writer1 = new BufferedWriter(new FileWriter(file1));
		writer1.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer1.append("<records datePattern=\"yyyy-MM-dd\">");

		file2.delete();
		BufferedWriter writer2 = new BufferedWriter(new FileWriter(file2));
		writer2.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer2.append("<records datePattern=\"yyyy-MM-dd\">");

		int index = 0;

		for (int i = 0; i < 1000; i++) {
			String id = ids.get(index++);

			writer1.append("\n<record id=\"" + id + "\">");
			writer1.append("\n<title>Folder " + id + "</title>");
			writer1.append(
					"\n<administrativeUnitEntered>code:" + records.getUnit10a().getCode() + "</administrativeUnitEntered>");
			writer1.append("\n<categoryEntered>code:" + records.getCategory_X13().getCode() + "</categoryEntered>");
			writer1.append("\n<copyStatusEntered>P</copyStatusEntered>");
			writer1.append("\n<openingDate type=\"date\">2015-04-16</openingDate>");
			writer1.append("\n<keywords multivalue=\"true\">");
			writer1.append("\n<item>keyword1</item>");
			writer1.append("\n<item>keyword2</item>");
			writer1.append("\n</keywords>");
			writer1.append("\n<mediumTypes multivalue=\"true\">");
			writer1.append("\n<item>code:PA</item>");
			writer1.append("\n<item>code:DM</item>");
			writer1.append("\n</mediumTypes>");
			writer1.append("\n<actualTransferDate type=\"date\">2010-05-29</actualTransferDate>");
			writer1.append("\n<retentionRuleEntered>code:" + records.getRule1().getCode() + "</retentionRuleEntered>");
			writer1.append("\n</record>");

		}

		for (int parent = 0; parent < 500; parent++) {
			for (int i = 0; i < 200; i++) {
				String id = ids.get(index++);
				writer1.append("\n<record id=\"" + id + "\">");
				writer1.append("\n<title>Folder " + id + "</title>");
				writer1.append("\n<parentFolder>" + ids.get(parent) + "</parentFolder>");
				writer1.append("\n<openingDate type=\"date\">2015-04-16</openingDate>");
				writer1.append("\n<keywords></keywords>");
				writer1.append("\n</record>");
			}
		}
		for (int parent = 500; parent < 1000; parent++) {
			for (int i = 0; i < 200; i++) {
				String id = ids.get(index++);
				writer2.append("\n<record id=\"" + id + "\">");
				writer2.append("\n<title>Folder " + id + "</title>");
				writer2.append("\n<parentFolder>" + ids.get(parent) + "</parentFolder>");
				writer2.append("\n<openingDate type=\"date\">2015-04-16</openingDate>");
				writer2.append("\n<keywords></keywords>");
				writer2.append("\n</record>");
			}
		}

		writer1.append("</records>");
		writer1.close();

		writer2.append("</records>");
		writer2.close();
	}

	@Test
	@LoadTest
	public void testName()
			throws Exception {

		RecordsImportServices importServices = new RecordsImportServices(getModelLayerFactory());

		File zip1 = getTestResourceFile("folder1.xml.zip");
		XMLImportDataProvider dataProvider1 = XMLImportDataProvider.forZipFile(getModelLayerFactory(), zip1);
		BulkImportResults results1 = importServices.bulkImport(dataProvider1, new LoggerBulkImportProgressionListener(), admin);
		assertThat(searchServices.getResultsCount(from(rm.folderSchemaType()).returnAll())).isEqualTo(101_000);
		//		assertThat(rm.getFolderByLegacyId("8544312").getTitle()).isEqualTo("Folder 8544312");
		assertThat(results1.getImportErrors()).isEmpty();
		assertThat(results1.getInvalidIds()).isEmpty();

		File zip2 = getTestResourceFile("folder2.xml.zip");
		XMLImportDataProvider dataProvider2 = XMLImportDataProvider.forZipFile(getModelLayerFactory(), zip2);
		BulkImportResults results2 = importServices.bulkImport(dataProvider2, new LoggerBulkImportProgressionListener(), admin);
		assertThat(results2.getImportErrors()).isEmpty();
		assertThat(results2.getInvalidIds()).isEmpty();
		assertThat(searchServices.getResultsCount(from(rm.folderSchemaType()).returnAll())).isEqualTo(201_000);
		//		assertThat(rm.getFolderByLegacyId("8544312").getTitle()).isEqualTo("Folder 8544312 - modified");

		dataProvider1 = XMLImportDataProvider.forZipFile(getModelLayerFactory(), zip1);
		results1 = importServices.bulkImport(dataProvider1, new LoggerBulkImportProgressionListener(), admin);

		dataProvider2 = XMLImportDataProvider.forZipFile(getModelLayerFactory(), zip2);
		results2 = importServices.bulkImport(dataProvider2, new LoggerBulkImportProgressionListener(), admin);
	}

	// ----------------------------------------

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
