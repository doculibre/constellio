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
package com.constellio.app.modules.rm.imports;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.bulkImport.BulkImportProgressionListener;
import com.constellio.model.services.records.bulkImport.LoggerBulkImportProgressionListener;
import com.constellio.model.services.records.bulkImport.RecordsImportServices;
import com.constellio.model.services.records.bulkImport.data.ImportDataProvider;
import com.constellio.model.services.records.bulkImport.data.xml.XMLImportDataProvider;
import com.constellio.sdk.tests.ConstellioTest;

public class RMImportsAcceptanceTest extends ConstellioTest {

	RecordsImportServices importServices;
	RMSchemasRecordsServices rm;

	@Before
	public void setUp()
			throws Exception {
		importServices = new RecordsImportServices(getModelLayerFactory(), 1);
		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

	}

	@Test
	public void whenImportingDocumentTypesAndRetentionRulesAtTheSameMomentThenOK()
			throws Exception {

		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();

		ImportDataProvider importDataProvider = XMLImportDataProvider.forZipFile(
				getModelLayerFactory(), getTestResourceFile("data.zip"));

		BulkImportProgressionListener progressionListener = new LoggerBulkImportProgressionListener();

		User admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);

		importServices.bulkImport(importDataProvider, progressionListener, admin);

		RetentionRule rule1 = rm.getRetentionRuleByCode("111200");
		assertThat(rule1.getDocumentTypesDetails()).isNotEmpty();
		assertThat(rule1.getDocumentTypes()).isNotEmpty();

	}
}
