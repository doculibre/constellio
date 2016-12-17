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

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		decommissioningService = new DecommissioningService(zeCollection, getAppLayerFactory());
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
