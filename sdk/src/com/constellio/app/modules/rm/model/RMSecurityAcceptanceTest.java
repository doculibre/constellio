package com.constellio.app.modules.rm.model;

import static com.constellio.app.modules.rm.constants.RMPermissionsTo.DISPLAY_CONTAINERS;
import static com.constellio.app.modules.rm.constants.RMPermissionsTo.MANAGE_CONTAINERS;
import static com.constellio.app.modules.rm.constants.RMPermissionsTo.MANAGE_STORAGE_SPACES;
import static com.constellio.model.services.search.query.logical.LogicalSearchQuery.query;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class RMSecurityAcceptanceTest extends ConstellioTest {

	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);
	RolesManager rolesManager;

	Role displayContainerRole, manageContainerRole, manageStorageSpace;
	UserServices userServices;
	RecordServices recordServices;
	SearchServices searchServices;
	LogicalSearchCondition allContainers;
	LogicalSearchCondition allStorageSpaces;
	RMSchemasRecordsServices rm;
	User sasquatch;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		rolesManager = getModelLayerFactory().getRolesManager();
		userServices = getModelLayerFactory().newUserServices();
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		displayContainerRole = rolesManager.addRole(new Role(zeCollection, "displayContainers", asList(DISPLAY_CONTAINERS)));
		manageContainerRole = rolesManager.addRole(new Role(zeCollection, "manageContainers", asList(MANAGE_CONTAINERS)));
		manageStorageSpace = rolesManager.addRole(new Role(zeCollection, "manageStorageSpaces", asList(MANAGE_STORAGE_SPACES)));
		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		allContainers = from(asList(rm.containerRecord.schemaType())).returnAll();
		allStorageSpaces = from(asList(rm.storageSpace.schemaType())).returnAll();
		sasquatch = users.sasquatchIn(zeCollection);

		assertThat(searchServices.getResultsCount(query(allContainers))).isEqualTo(19);
		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUser(sasquatch))).isEqualTo(0);
		assertThat(sasquatch.hasReadAccess().on(records.getContainerBac04())).isFalse();
		assertThat(sasquatch.hasWriteAccess().on(records.getContainerBac04())).isFalse();
		assertThat(sasquatch.hasDeleteAccess().on(records.getContainerBac04())).isFalse();

		assertThat(searchServices.getResultsCount(query(allStorageSpaces))).isEqualTo(6);
		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUser(sasquatch))).isEqualTo(0);
		assertThat(sasquatch.hasReadAccess().on(records.getStorageSpaceS01_01())).isFalse();
		assertThat(sasquatch.hasWriteAccess().on(records.getStorageSpaceS01_01())).isFalse();
		assertThat(sasquatch.hasDeleteAccess().on(records.getStorageSpaceS01_01())).isFalse();
	}

	@Test
	public void givenUserWithDisplayContainerThenSeeContainerWhenSearchingRecordsButDoesNotHaveWriteDeleteAccess()
			throws Exception {

		recordServices.update(sasquatch.setUserRoles(asList(displayContainerRole.getCode())));

		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUser(sasquatch))).isEqualTo(19);
		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserWrite(sasquatch))).isEqualTo(0);
		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserDelete(sasquatch))).isEqualTo(0);
		assertThat(sasquatch.hasReadAccess().on(records.getContainerBac04())).isTrue();
		assertThat(sasquatch.hasWriteAccess().on(records.getContainerBac04())).isFalse();
		assertThat(sasquatch.hasDeleteAccess().on(records.getContainerBac04())).isFalse();

		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUser(sasquatch))).isEqualTo(0);
		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserWrite(sasquatch))).isEqualTo(0);
		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserDelete(sasquatch))).isEqualTo(0);
		assertThat(sasquatch.hasReadAccess().on(records.getStorageSpaceS01_01())).isFalse();
		assertThat(sasquatch.hasWriteAccess().on(records.getStorageSpaceS01_01())).isFalse();
		assertThat(sasquatch.hasDeleteAccess().on(records.getStorageSpaceS01_01())).isFalse();
	}

	@Test
	public void givenUserWithManagerContainerThenSeeContainerWhenSearchingRecordsButHasWriteDeleteAccess()
			throws Exception {

		recordServices.update(sasquatch.setUserRoles(asList(manageContainerRole.getCode())));

		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUser(sasquatch))).isEqualTo(19);
		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserWrite(sasquatch))).isEqualTo(19);
		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserDelete(sasquatch))).isEqualTo(19);
		assertThat(sasquatch.hasReadAccess().on(records.getContainerBac04())).isTrue();
		assertThat(sasquatch.hasWriteAccess().on(records.getContainerBac04())).isTrue();
		assertThat(sasquatch.hasDeleteAccess().on(records.getContainerBac04())).isTrue();

		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUser(sasquatch))).isEqualTo(0);
		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserWrite(sasquatch))).isEqualTo(0);
		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserDelete(sasquatch))).isEqualTo(0);
		assertThat(sasquatch.hasReadAccess().on(records.getStorageSpaceS01_01())).isFalse();
		assertThat(sasquatch.hasWriteAccess().on(records.getStorageSpaceS01_01())).isFalse();
		assertThat(sasquatch.hasDeleteAccess().on(records.getStorageSpaceS01_01())).isFalse();
	}

	@Test
	public void givenUserWithManagerStorageSpacesThenSeeContainerWhenSearchingRecordsButHasWriteDeleteAccess()
			throws Exception {

		recordServices.update(sasquatch.setUserRoles(asList(manageStorageSpace.getCode())));

		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUser(sasquatch))).isEqualTo(0);
		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserWrite(sasquatch))).isEqualTo(0);
		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserDelete(sasquatch))).isEqualTo(0);
		assertThat(sasquatch.hasReadAccess().on(records.getContainerBac04())).isFalse();
		assertThat(sasquatch.hasWriteAccess().on(records.getContainerBac04())).isFalse();
		assertThat(sasquatch.hasDeleteAccess().on(records.getContainerBac04())).isFalse();

		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUser(sasquatch))).isEqualTo(6);
		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserWrite(sasquatch))).isEqualTo(6);
		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserDelete(sasquatch))).isEqualTo(6);
		assertThat(sasquatch.hasReadAccess().on(records.getStorageSpaceS01_01())).isTrue();
		assertThat(sasquatch.hasWriteAccess().on(records.getStorageSpaceS01_01())).isTrue();
		assertThat(sasquatch.hasDeleteAccess().on(records.getStorageSpaceS01_01())).isTrue();
	}

	//@Test
	public void givenUserWithGlobalCollectionReadWriteDeleteAccessThenSeesNoStorageSpaceAndContainers()
			throws Exception {

		recordServices.update(sasquatch.setCollectionAllAccess(true));

		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUser(sasquatch))).isEqualTo(0);
		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserWrite(sasquatch))).isEqualTo(0);
		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserDelete(sasquatch))).isEqualTo(0);
		assertThat(sasquatch.hasReadAccess().on(records.getContainerBac04())).isFalse();
		assertThat(sasquatch.hasWriteAccess().on(records.getContainerBac04())).isFalse();
		assertThat(sasquatch.hasDeleteAccess().on(records.getContainerBac04())).isFalse();

		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUser(sasquatch))).isEqualTo(0);
		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserWrite(sasquatch))).isEqualTo(0);
		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserDelete(sasquatch))).isEqualTo(0);
		assertThat(sasquatch.hasReadAccess().on(records.getStorageSpaceS01_01())).isFalse();
		assertThat(sasquatch.hasWriteAccess().on(records.getStorageSpaceS01_01())).isFalse();
		assertThat(sasquatch.hasDeleteAccess().on(records.getStorageSpaceS01_01())).isFalse();
	}
}
