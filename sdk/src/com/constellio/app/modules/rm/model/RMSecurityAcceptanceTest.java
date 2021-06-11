package com.constellio.app.modules.rm.model;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ExternalLink;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.constellio.app.modules.rm.constants.RMPermissionsTo.DELETE_CONTAINERS;
import static com.constellio.app.modules.rm.constants.RMPermissionsTo.DISPLAY_CONTAINERS;
import static com.constellio.app.modules.rm.constants.RMPermissionsTo.MANAGE_CONTAINERS;
import static com.constellio.app.modules.rm.constants.RMPermissionsTo.MANAGE_STORAGE_SPACES;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForGroups;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.services.search.query.logical.LogicalSearchQuery.query;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class RMSecurityAcceptanceTest extends ConstellioTest {

	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);
	RolesManager rolesManager;

	Role displayContainerRole, manageContainerRole, manageStorageSpace, deleteContainerRole, emptyRole;
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
		displayContainerRole = rolesManager.addRole(new Role(zeCollection, "displayContainers", singletonList(DISPLAY_CONTAINERS)));
		manageContainerRole = rolesManager.addRole(new Role(zeCollection, "manageContainers", singletonList(MANAGE_CONTAINERS)));
		manageStorageSpace = rolesManager.addRole(new Role(zeCollection, "manageStorageSpaces", singletonList(MANAGE_STORAGE_SPACES)));
		deleteContainerRole = rolesManager.addRole(new Role(zeCollection, "deleteContainers", singletonList(DELETE_CONTAINERS)));
		emptyRole = rolesManager.addRole(new Role(zeCollection, "emptyRole", Collections.emptyList()));

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		allContainers = from(singletonList(rm.containerRecord.schemaType())).returnAll();
		allStorageSpaces = from(singletonList(rm.storageSpace.schemaType())).returnAll();
		sasquatch = users.sasquatchIn(zeCollection);

		assertThat(searchServices.getResultsCount(query(allContainers))).isEqualTo(19);
		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserRead(sasquatch))).isEqualTo(0);
		assertThat(sasquatch.hasReadAccess().on(records.getContainerBac04())).isFalse();
		assertThat(sasquatch.hasWriteAccess().on(records.getContainerBac04())).isFalse();
		assertThat(sasquatch.hasDeleteAccess().on(records.getContainerBac04())).isFalse();

		assertThat(searchServices.getResultsCount(query(allStorageSpaces))).isEqualTo(6);
		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserRead(sasquatch))).isEqualTo(0);
		assertThat(sasquatch.hasReadAccess().on(records.getStorageSpaceS01_01())).isFalse();
		assertThat(sasquatch.hasWriteAccess().on(records.getStorageSpaceS01_01())).isFalse();
		assertThat(sasquatch.hasDeleteAccess().on(records.getStorageSpaceS01_01())).isFalse();
	}

	@Test
	public void givenUserWithDisplayContainerThenSeeContainerWhenSearchingRecordsButDoesNotHaveWriteDeleteAccess()
			throws Exception {

		recordServices.update(sasquatch.setUserRoles(singletonList(displayContainerRole.getCode())));

		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserRead(sasquatch))).isEqualTo(19);
		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserWrite(sasquatch))).isEqualTo(0);
		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserDelete(sasquatch))).isEqualTo(0);
		assertThat(sasquatch.hasReadAccess().on(records.getContainerBac04())).isTrue();
		assertThat(sasquatch.hasWriteAccess().on(records.getContainerBac04())).isFalse();
		assertThat(sasquatch.hasDeleteAccess().on(records.getContainerBac04())).isFalse();

		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserRead(sasquatch))).isEqualTo(0);
		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserWrite(sasquatch))).isEqualTo(0);
		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserDelete(sasquatch))).isEqualTo(0);
		assertThat(sasquatch.hasReadAccess().on(records.getStorageSpaceS01_01())).isFalse();
		assertThat(sasquatch.hasWriteAccess().on(records.getStorageSpaceS01_01())).isFalse();
		assertThat(sasquatch.hasDeleteAccess().on(records.getStorageSpaceS01_01())).isFalse();
	}

	@Test
	public void givenUserWithManagerContainerThenSeeContainerWhenSearchingRecordsButHasWriteAccess()
			throws Exception {

		recordServices.update(sasquatch.setUserRoles(asList(emptyRole.getCode())));
		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserRead(sasquatch))).isEqualTo(0);
		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserWrite(sasquatch))).isEqualTo(0);

		rolesManager.updateRole(rolesManager.getRole(zeCollection, "emptyRole").withNewPermissions(singletonList(DISPLAY_CONTAINERS)));
		sasquatch = users.sasquatchIn(zeCollection);

		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserRead(sasquatch))).isEqualTo(19);
	}

	@Test
	public void givenUserWithManagerContainerAndDeleteContainerThenSeeContainerWhenSearchingRecordsButHasWriteDeleteAccess()
			throws Exception {

		recordServices.update(sasquatch.setUserRoles(asList(manageContainerRole.getCode(), deleteContainerRole.getCode())));

		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserRead(sasquatch))).isEqualTo(19);
		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserWrite(sasquatch))).isEqualTo(19);
		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserDelete(sasquatch))).isEqualTo(19);
		assertThat(sasquatch.hasReadAccess().on(records.getContainerBac04())).isTrue();
		assertThat(sasquatch.hasWriteAccess().on(records.getContainerBac04())).isTrue();
		assertThat(sasquatch.hasDeleteAccess().on(records.getContainerBac04())).isTrue();

		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserRead(sasquatch))).isEqualTo(0);
		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserWrite(sasquatch))).isEqualTo(0);
		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserDelete(sasquatch))).isEqualTo(0);
		assertThat(sasquatch.hasReadAccess().on(records.getStorageSpaceS01_01())).isFalse();
		assertThat(sasquatch.hasWriteAccess().on(records.getStorageSpaceS01_01())).isFalse();
		assertThat(sasquatch.hasDeleteAccess().on(records.getStorageSpaceS01_01())).isFalse();
	}

	@Test
	public void givenUserWithManagerStorageSpacesThenSeeContainerWhenSearchingRecordsButHasWriteDeleteAccess()
			throws Exception {

		recordServices.update(sasquatch.setUserRoles(singletonList(manageStorageSpace.getCode())));

		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserRead(sasquatch))).isEqualTo(0);
		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserWrite(sasquatch))).isEqualTo(0);
		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserDelete(sasquatch))).isEqualTo(0);
		assertThat(sasquatch.hasReadAccess().on(records.getContainerBac04())).isFalse();
		assertThat(sasquatch.hasWriteAccess().on(records.getContainerBac04())).isFalse();
		assertThat(sasquatch.hasDeleteAccess().on(records.getContainerBac04())).isFalse();

		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserRead(sasquatch))).isEqualTo(6);
		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserWrite(sasquatch))).isEqualTo(6);
		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserDelete(sasquatch))).isEqualTo(6);
		assertThat(sasquatch.hasReadAccess().on(records.getStorageSpaceS01_01())).isTrue();
		assertThat(sasquatch.hasWriteAccess().on(records.getStorageSpaceS01_01())).isTrue();
		assertThat(sasquatch.hasDeleteAccess().on(records.getStorageSpaceS01_01())).isTrue();
	}

	@Test
	public void givenUserWithGlobalCollectionReadWriteDeleteAccessThenSeesAllStorageSpaceAndContainers()
			throws Exception {

		recordServices.update(sasquatch.setCollectionAllAccess(true));

		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserRead(sasquatch))).isEqualTo(19);
		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserWrite(sasquatch))).isEqualTo(19);
		assertThat(searchServices.getResultsCount(query(allContainers).filteredWithUserDelete(sasquatch))).isEqualTo(19);
		assertThat(sasquatch.hasReadAccess().on(records.getContainerBac04())).isTrue();
		assertThat(sasquatch.hasWriteAccess().on(records.getContainerBac04())).isTrue();
		assertThat(sasquatch.hasDeleteAccess().on(records.getContainerBac04())).isTrue();

		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserRead(sasquatch))).isEqualTo(6);
		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserWrite(sasquatch))).isEqualTo(6);
		assertThat(searchServices.getResultsCount(query(allStorageSpaces).filteredWithUserDelete(sasquatch))).isEqualTo(6);
		assertThat(sasquatch.hasReadAccess().on(records.getStorageSpaceS01_01())).isTrue();
		assertThat(sasquatch.hasWriteAccess().on(records.getStorageSpaceS01_01())).isTrue();
		assertThat(sasquatch.hasDeleteAccess().on(records.getStorageSpaceS01_01())).isTrue();
	}

	@Test
	public void givenUserAsAuthorizationOnAFolderThenAsAccessToItsExternalLinks() throws Exception{

		assertThat(users.sasquatchIn(zeCollection).hasReadAccess().on(records.getFolder_A15())).isFalse();

		ExternalLink oldLink = rm.newExternalLink().setTitle("1").setLinkedto(records.folder_A15);
		recordServices.add(oldLink);
		recordServices.update(records.getFolder_A15().addExternalLink(oldLink.getId()));

		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		authorizationsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection))
				.givingReadAccess().on(records.getFolder_A15()));

		assertThat(users.sasquatchIn(zeCollection).hasReadAccess().on(records.getFolder_A15())).isTrue();
		String sasquatchReadToken = "r_" + users.sasquatchIn(zeCollection).getId();
		assertThat(records.getFolder_A15().getList(Schemas.TOKENS)).contains(sasquatchReadToken);

		ExternalLink newLink = rm.newExternalLink().setTitle("1").setLinkedto(records.folder_A15);
		recordServices.add(newLink);
		recordServices.update(records.getFolder_A15().addExternalLink(newLink.getId()));


		Stream.of(oldLink.getId(), newLink.getId()).forEach(externalLinkId -> {
			ExternalLink externalLink = rm.getExternalLink(externalLinkId);

			assertThat(users.sasquatchIn(zeCollection).hasReadAccess().on(rm.getExternalLink(externalLink.getId()))).isTrue();

			assertExternalLinkSecurityWithFolder(externalLink, records.getFolder_A15().getWrappedRecord());

			assertThat(searchServices.searchRecordIds(new LogicalSearchQuery(from(rm.externalLink.schemaType()).returnAll())
					.filteredWithUserRead(users.sasquatchIn(zeCollection)))).contains(externalLink.getId());
		});


		authorizationsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).givingNegative(Role.READ_ROLE).on(records.getFolder_A15()));

		Stream.of(oldLink.getId(), newLink.getId()).forEach(externalLinkId -> {
			ExternalLink externalLink = rm.getExternalLink(externalLinkId);

			assertThat(users.sasquatchIn(zeCollection).hasReadAccess().on(externalLink)).isFalse();

			assertExternalLinkSecurityWithFolder(externalLink, records.getFolder_A15().getWrappedRecord());

			assertThat(searchServices.searchRecordIds(new LogicalSearchQuery(from(rm.externalLink.schemaType()).returnAll())
					.filteredWithUserRead(users.sasquatchIn(zeCollection)))).doesNotContain(externalLink.getId());
		});
	}

	@Test
	public void givenGroupAsAuthorizationOnAFolderThenAsAccessToItsExternalLinks() throws Exception {
		String groupId = users.sasquatchIn(zeCollection).getUserGroups().stream().findFirst().get();

		assertThat(users.sasquatchIn(zeCollection).hasReadAccess().on(records.getFolder_A15())).isFalse();

		ExternalLink oldLink = rm.newExternalLink().setTitle("1").setLinkedto(records.folder_A15);
		recordServices.add(oldLink);
		recordServices.update(records.getFolder_A15().addExternalLink(oldLink.getId()));

		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		authorizationsServices.add(authorizationForGroups(rm.getGroup(groupId))
				.givingReadAccess().on(records.getFolder_A15()));

		assertThat(users.sasquatchIn(zeCollection).hasReadAccess().on(records.getFolder_A15())).isTrue();
		String sasquatchReadToken = "r_" + groupId;
		assertThat(records.getFolder_A15().getList(Schemas.TOKENS)).contains(sasquatchReadToken);

		ExternalLink newLink = rm.newExternalLink().setTitle("1").setLinkedto(records.folder_A15);
		recordServices.add(newLink);
		recordServices.update(records.getFolder_A15().addExternalLink(newLink.getId()));


		Stream.of(oldLink.getId(), newLink.getId()).forEach(externalLinkId -> {
			ExternalLink externalLink = rm.getExternalLink(externalLinkId);

			assertThat(users.sasquatchIn(zeCollection).hasReadAccess().on(rm.getExternalLink(externalLink.getId()))).isTrue();

			assertExternalLinkSecurityWithFolder(externalLink, records.getFolder_A15().getWrappedRecord());

			assertThat(searchServices.searchRecordIds(new LogicalSearchQuery(from(rm.externalLink.schemaType()).returnAll())
					.filteredWithUserRead(users.sasquatchIn(zeCollection)))).contains(externalLink.getId());
		});


		authorizationsServices.add(authorizationForGroups(rm.getGroup(groupId)).givingNegative(Role.READ_ROLE).on(records.getFolder_A15()));

		Stream.of(oldLink.getId(), newLink.getId()).forEach(externalLinkId -> {
			ExternalLink externalLink = rm.getExternalLink(externalLinkId);

			assertThat(users.sasquatchIn(zeCollection).hasReadAccess().on(externalLink)).isFalse();

			assertExternalLinkSecurityWithFolder(externalLink, records.getFolder_A15().getWrappedRecord());

			assertThat(searchServices.searchRecordIds(new LogicalSearchQuery(from(rm.externalLink.schemaType()).returnAll())
					.filteredWithUserRead(users.sasquatchIn(zeCollection)))).doesNotContain(externalLink.getId());
		});
	}

	@Test
	public void givenUserAsAuthorizationOnOnAnAdminUnitThenAsAccessToItsExternalLinks() throws Exception {

		String administrativeUnitId = records.getFolder_A15().getAdministrativeUnit();

		assertThat(users.sasquatchIn(zeCollection).hasReadAccess().on(rm.getAdministrativeUnit(administrativeUnitId))).isFalse();

		ExternalLink oldLink = rm.newExternalLink().setTitle("1").setLinkedto(records.folder_A15);
		recordServices.add(oldLink);
		recordServices.update(records.getFolder_A15().addExternalLink(oldLink.getId()));

		assertThat(users.sasquatchIn(zeCollection).hasReadAccess().on(oldLink)).isFalse();

		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		authorizationsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection))
				.givingReadAccess().on(rm.getAdministrativeUnit(administrativeUnitId)));

		assertThat(users.sasquatchIn(zeCollection).hasReadAccess().on(rm.getAdministrativeUnit(administrativeUnitId))).isTrue();
		ExternalLink newLink = rm.newExternalLink().setTitle("1").setLinkedto(records.folder_A15);
		recordServices.add(newLink);
		recordServices.update(records.getFolder_A15().addExternalLink(newLink.getId()));

		waitForBatchProcess();

		Stream.of(oldLink.getId(), newLink.getId()).forEach(externalLinkId -> {
			ExternalLink externalLink = rm.getExternalLink(externalLinkId);

			assertThat(users.sasquatchIn(zeCollection).hasReadAccess().on(rm.getExternalLink(externalLink.getId()))).isTrue();

			assertExternalLinkSecurityWithFolder(externalLink, records.getFolder_A15().getWrappedRecord());

			assertThat(searchServices.searchRecordIds(new LogicalSearchQuery(from(rm.externalLink.schemaType()).returnAll())
					.filteredWithUserRead(users.sasquatchIn(zeCollection)))).contains(externalLink.getId());
		});
	}

	private void assertExternalLinkSecurityWithFolder(ExternalLink externalLink, Record record) {
		Stream.of(
				Schemas.SECONDARY_CONCEPTS_INT_IDS,
				Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS,
				Schemas.PRINCIPALS_ANCESTORS_INT_IDS,
				Schemas.PRINCIPAL_CONCEPTS_INT_IDS,
				Schemas.DETACHED_PRINCIPAL_ANCESTORS_INT_IDS,
				Schemas.TOKENS).forEach(metadata -> {
			Object externalLinkValue = externalLink.get(metadata);

			if (externalLinkValue instanceof List<?>) {
				List<Object> externalLinkValueIds = (List<Object>) externalLinkValue;
				List<Object> folderValues = record.get(metadata);

				assertThat(externalLinkValueIds).containsExactly(folderValues.stream().toArray(Object[]::new));
			} else {
				assertThat(externalLinkValue).isEqualTo(record.get(metadata));
			}
		});

		assertThat((List<String>) externalLink.get(Schemas.TOKENS_OF_HIERARCHY)).containsExactly(((List<String>) externalLink.get(Schemas.TOKENS)).stream().toArray(String[]::new));
	}
}
