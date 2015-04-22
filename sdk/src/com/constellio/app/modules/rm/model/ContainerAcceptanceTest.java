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

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;

public class ContainerAcceptanceTest extends ConstellioTest {
	RMSchemasRecordsServices schemas;
	RMTestRecords records;
	RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection).withConstellioRMModule();
		assertThat(getModelLayerFactory().getTaxonomiesManager().getPrincipalTaxonomy(zeCollection).getCode())
				.isEqualTo(RMTaxonomies.ADMINISTRATIVE_UNITS);

		schemas = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

	}

	@Test
	public void givenContainerWithTemporaryIdentifierThenUsedHasTitle()
			throws Exception {

		StorageSpace storage42 = schemas.newStorageSpaceWithId("42");
		storage42.setCode("Ze42");
		storage42.setTitle("Ze storage");
		storage42.setDescription("Ze description");
		storage42.setCapacity(42L);

		StorageSpace storage666 = schemas.newStorageSpaceWithId("666");
		storage666.setCode("Ze666");
		storage666.setTitle("Ze child storage");
		storage666.setDescription("Ze description");
		storage666.setCapacity(666L);
		storage666.setDecommissioningType(DecommissioningType.DEPOSIT);
		storage666.setParentStorageSpace("42");

		ContainerRecordType zeBoite = schemas.newContainerRecordTypeWithId("zeBoite");
		zeBoite.setTitle("Ze Boite");
		zeBoite.setCode("BOITE");

		ContainerRecord zeContainer = schemas.newContainerRecordWithId("zeContainer");
		zeContainer.setTemporaryIdentifier("Ze temp identifier");
		zeContainer.setDescription("Ze description");
		zeContainer.setFull(false);
		zeContainer.setDecommissioningType(DecommissioningType.DEPOSIT);
		zeContainer.setStorageSpace(storage666);
		zeContainer.setAdministrativeUnit(records.unitId_10);
		zeContainer.setFilingSpace(records.filingId_A);
		zeContainer.setType("zeBoite");

		ContainerRecord anotherContainer = schemas.newContainerRecordWithId("anotherContainer");
		anotherContainer.setTemporaryIdentifier("Ze temp identifier");
		anotherContainer.setIdentifier("Ze ultimate identifier");
		anotherContainer.setDescription("Ze description");
		anotherContainer.setFull(true);
		anotherContainer.setDecommissioningType(DecommissioningType.DEPOSIT);
		anotherContainer.setStorageSpace(storage666);
		anotherContainer.setAdministrativeUnit(records.unitId_20);
		anotherContainer.setFilingSpace(records.filingId_C);
		anotherContainer.setType("zeBoite");

		Transaction transaction = new Transaction();
		transaction.add(storage42);
		transaction.add(storage666);
		transaction.add(zeBoite);
		transaction.add(zeContainer);
		transaction.add(anotherContainer);
		recordServices.execute(transaction);

		storage42 = schemas.getStorageSpace("42");
		storage666 = schemas.getStorageSpace("666");
		zeContainer = schemas.getContainerRecord("zeContainer");
		anotherContainer = schemas.getContainerRecord("anotherContainer");

		assertThat(storage42.getCode()).isEqualTo("Ze42");
		assertThat(storage42.getTitle()).isEqualTo("Ze storage");
		assertThat(storage42.getDescription()).isEqualTo("Ze description");

		assertThat(storage666.getCode()).isEqualTo("Ze666");
		assertThat(storage666.getTitle()).isEqualTo("Ze child storage");
		assertThat(storage666.getDescription()).isEqualTo("Ze description");
		assertThat(storage666.getDecommissioningType()).isEqualTo(DecommissioningType.DEPOSIT);
		assertThat(storage666.getParentStorageSpace()).isEqualTo("42");

		assertThat(zeContainer.getTitle()).isEqualTo("Ze temp identifier");
		assertThat(zeContainer.getTemporaryIdentifier()).isEqualTo("Ze temp identifier");
		assertThat(zeContainer.getDescription()).isEqualTo("Ze description");
		assertThat(zeContainer.isFull()).isFalse();
		assertThat(zeContainer.getAdministrativeUnit()).isEqualTo(records.unitId_10);
		assertThat(zeContainer.getFilingSpace()).isEqualTo(records.filingId_A);
		assertThat(zeContainer.getType()).isEqualTo("zeBoite");

		assertThat(anotherContainer.getTitle()).isEqualTo("Ze ultimate identifier");
		assertThat(anotherContainer.getTemporaryIdentifier()).isEqualTo("Ze temp identifier");
		assertThat(anotherContainer.getIdentifier()).isEqualTo("Ze ultimate identifier");
		assertThat(anotherContainer.getDescription()).isEqualTo("Ze description");
		assertThat(anotherContainer.isFull()).isTrue();
		assertThat(anotherContainer.getAdministrativeUnit()).isEqualTo(records.unitId_20);
		assertThat(anotherContainer.getFilingSpace()).isEqualTo(records.filingId_C);
		assertThat(anotherContainer.getType()).isEqualTo("zeBoite");
	}
}
