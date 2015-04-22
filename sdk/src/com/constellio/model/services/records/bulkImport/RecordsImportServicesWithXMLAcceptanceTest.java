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
import org.joda.time.LocalDateTime;
import org.junit.Before;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.bulkImport.data.ImportDataProvider;
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

	//@Test
	public void whenImportingZipOfXMLFilesThenImportedCorrectly()
			throws Exception {

		//File zipFile = getTestResourceFile("testdata.zip");
		File zipFile = buildZipWith("administrativeUnit.xml", "category.xml", "filingSpace.xml", "folder.xml");

		ImportDataProvider importDataProvider = XMLImportDataProvider.forZipFile(getModelLayerFactory(), zipFile);
		importServices.bulkImport(importDataProvider, progressionListener, admin);

		Folder folder1 = rm.wrapFolder(expectedRecordWithLegacyId("1"));
		assertThat(folder1.getRetentionRule()).isEqualTo(records.ruleId_3);

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
