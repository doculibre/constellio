package com.constellio.app.modules.rm.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class RMSchemasRecordsServicesAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();

	RMSchemasRecordsServices rm;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus()
				.withAllTest(users));

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		Folder folder = records.getFolder_A02();

	}

	@Test
	public void validateLinkedSchemaUtilsMethods()
			throws Exception {

		assertThat(rm.getLinkedSchemaOf(rm.getFolderTypeWithCode("meetingFolder"))).isEqualTo("folder_meetingFolder");
		assertThat(rm.getLinkedSchemaOf(rm.getFolderTypeWithCode("employe"))).isEqualTo("folder_employe");
		assertThat(rm.getLinkedSchemaOf(rm.getFolderTypeWithCode("other"))).isEqualTo("folder_default");

		Folder folder = records.getFolder_A05();
		assertThat(rm.getLinkedSchemaOf(folder)).isEqualTo("folder_default");

		folder.setType(rm.getFolderTypeWithCode("meetingFolder"));
		assertThat(rm.getLinkedSchemaOf(folder)).isEqualTo("folder_meetingFolder");

		folder.setType(rm.getFolderTypeWithCode("employe"));
		assertThat(rm.getLinkedSchemaOf(folder)).isEqualTo("folder_employe");

		folder.setType(rm.getFolderTypeWithCode("other"));
		assertThat(rm.getLinkedSchemaOf(folder)).isEqualTo("folder_default");

	}
}
