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
package com.constellio.app.modules.rm.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;

public class EmailAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		assertThat(getModelLayerFactory().getTaxonomiesManager().getPrincipalTaxonomy(zeCollection).getCode())
				.isEqualTo(RMTaxonomies.ADMINISTRATIVE_UNITS);

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

	}

	@Test
	public void whenCreatingAnEmailWithoutDescriptionThenOK()
			throws Exception {

		Email email = rm.newEmail();
		email.setTitle("My email").setDescription("test").setFolder(records.folder_A03);
		email.setEmailTo(Arrays.asList("dest1", "dest2"));
		recordServices.add(email);
		email.setDescription(null).setTitle("Z");
		recordServices.update(email);
		assertThat(email.getEmailTo()).containsOnly("dest1", "dest2");
	}

	@Test
	public void givenEmailThenInheritFolderMetadatas()
			throws Exception {

		Email email = rm.newEmail();
		email.setTitle("My email").setDescription("test").setFolder(records.folder_A03);
		email.setEmailTo(Arrays.asList("dest1", "dest2"));
		recordServices.add(email);

		assertThat(email.getFolderAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(email.getFolderCategory()).isEqualTo(records.categoryId_X110);

		Folder folder = records.getFolder_A03()
				.setCategoryEntered(records.categoryId_X13)
				.setAdministrativeUnitEntered(records.unitId_11b);

		recordServices.execute(new Transaction(folder.getWrappedRecord()));
		waitForBatchProcess();
		recordServices.refresh(email);
		assertThat(email.getFolderAdministrativeUnit()).isEqualTo(records.unitId_11b);
		assertThat(email.getFolderCategory()).isEqualTo(records.categoryId_X13);

	}
}
