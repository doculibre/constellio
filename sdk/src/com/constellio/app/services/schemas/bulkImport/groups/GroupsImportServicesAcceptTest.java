package com.constellio.app.services.schemas.bulkImport.groups;

import com.constellio.model.entities.security.global.SystemWideGroup;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class GroupsImportServicesAcceptTest extends ConstellioTest {

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection(), withCollection("anotherCollection")
		);
	}

	@Test
	public void whenReadingGroupsXMLFileThenReadCorrectly()
			throws Exception {
		importGroups();
		assertThat(getGroup("001").getName()).isEqualTo("group 001");
		assertThatGroupIsInAllCollections("001");
		assertThat(getGroup("002").getName()).isEqualTo("group 002");
		assertThat(getGroup("002").getParent()).isEqualTo("001");
		assertThatGroupIsInAllCollections("002");
		assertThat(getGroup("003").getName()).isEqualTo("group 003");
		assertThat(getGroup("003").getParent()).isEqualTo("002");
		assertThatGroupIsInAllCollections("003");
	}

	private void assertThatGroupIsInAllCollections(String code) {
		UserServices userServices = getModelLayerFactory().newUserServices();
		assertThat(userServices.getGroupInCollection(code, zeCollection)).isNotNull();
		assertThat(userServices.getGroupInCollection(code, "anotherCollection")).isNotNull();
	}

	private SystemWideGroup getGroup(String code) {
		return getModelLayerFactory().newUserServices().getGroup(code);
	}

	private void importGroups()
			throws Exception {
		File groupsFile = getTestResourceFile("groups.xml");
		new GroupsImportServices().bulkImport(groupsFile, Arrays.asList(zeCollection, "anotherCollection"),
				getModelLayerFactory());
	}
}
