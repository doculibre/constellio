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
package com.constellio.app.modules.rm.services.decommissioning;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;

public class DecommissioningEmailServiceAcceptTest extends ConstellioTest {
	DecommissioningService decommissioningService;
	RMSchemasRecordsServices rm;
	RecordServices recordServices;
	RMTestRecords records = new RMTestRecords(zeCollection);
	DecommissioningEmailService service;

	//User chuckNorris, dakota, edouard;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		decommissioningService = new DecommissioningService(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		service = new DecommissioningEmailService(zeCollection, getModelLayerFactory());

	}

	@Test
	public void givenEverybodyHasAnEmailAddressThenFindCorrectUsers()
			throws Exception {

		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit10()).containsOnly(dakota, gandalf);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit10a()).containsOnly(dakota, gandalf);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit11()).containsOnly(edouard, gandalf);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit11b()).containsOnly(edouard, gandalf);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit12()).containsOnly(edouard, gandalf);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit12b()).containsOnly(edouard, gandalf);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit12c()).containsOnly(edouard, gandalf);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit20()).containsOnly(dakota, gandalf);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit30()).containsOnly(gandalf);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit30c()).containsOnly(gandalf);
	}

	@Test
	public void givenGandalfAndAdminHasNoEmailAddressThenFindCorrectUsers()
			throws Exception {
		recordServices.update(records.getAdmin().setEmail(null));
		recordServices.update(records.getGandalf_managerInABC().setEmail(null));

		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit10()).containsOnly(dakota);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit10a()).containsOnly(dakota);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit11()).containsOnly(edouard);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit11b()).containsOnly(edouard);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit12()).containsOnly(edouard);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit12b()).containsOnly(edouard);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit12c()).containsOnly(edouard);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit20()).containsOnly(dakota);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit30()).containsOnly(dakota);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit30c()).containsOnly(dakota);
	}

	@Test
	public void givenDakotaGandalfAndAdminHasNoEmailAddressThenFindCorrectUsers()
			throws Exception {
		recordServices.update(records.getAdmin().setEmail(null));
		recordServices.update(records.getGandalf_managerInABC().setEmail(null));
		recordServices.update(records.getDakota_managerInA_userInB().setEmail(null));

		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit10()).containsOnly(chuckNorris);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit10a()).containsOnly(chuckNorris);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit11()).containsOnly(edouard);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit11b()).containsOnly(edouard);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit12()).containsOnly(edouard);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit12b()).containsOnly(edouard);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit12c()).containsOnly(edouard);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit20()).containsOnly(chuckNorris);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit30()).containsOnly(chuckNorris);
		assertThatUsersWithEmailAddressAndDecomPermissionInConcept(records.getUnit30c()).containsOnly(chuckNorris);
	}

	private org.assertj.core.api.ListAssert<Object> assertThatUsersWithEmailAddressAndDecomPermissionInConcept(
			AdministrativeUnit unit) {
		return assertThat(service.getUsersWithEmailAddressAndDecommissioningPermissionInConcept(unit)).extracting("username");
	}

}
