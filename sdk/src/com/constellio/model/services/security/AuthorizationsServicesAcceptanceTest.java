package com.constellio.model.services.security;

import static com.constellio.model.entities.records.wrappers.Event.PERMISSION_USERS;
import static com.constellio.model.entities.records.wrappers.Event.RECORD_ID;
import static com.constellio.model.entities.records.wrappers.Event.TYPE;
import static com.constellio.model.entities.records.wrappers.Event.USERNAME;
import static com.constellio.model.entities.schemas.Schemas.ALL_REMOVED_AUTHS;
import static com.constellio.model.entities.schemas.Schemas.ATTACHED_ANCESTORS;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.entities.schemas.Schemas.PRINCIPAL_PATH;
import static com.constellio.model.entities.security.Role.DELETE;
import static com.constellio.model.entities.security.Role.READ;
import static com.constellio.model.entities.security.Role.WRITE;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static com.constellio.model.entities.security.global.AuthorizationDeleteRequest.authorizationDeleteRequest;
import static com.constellio.model.entities.security.global.AuthorizationModificationRequest.modifyAuthorizationOnRecord;
import static com.constellio.model.entities.security.global.GlobalGroupStatus.ACTIVE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER1_DOC1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER2;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER2_1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER2_2;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER2_2_DOC1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER2_2_DOC2;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER3;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER3_DOC1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER4;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER4_1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER4_1_DOC1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER4_2;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER4_2_DOC1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.TAXO1_CATEGORY1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.TAXO1_CATEGORY2;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.TAXO1_CATEGORY2_1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.TAXO1_FOND1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.TAXO1_FOND1_1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.TAXO2_STATION2;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.TAXO2_STATION2_1;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.enums.GroupAuthorizationsInheritance;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.InvalidPrincipalsIds;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.InvalidTargetRecordId;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithId;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithIdOnRecord;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.NoSuchPrincipalWithUsername;
import com.constellio.sdk.tests.annotations.SlowTest;

public class AuthorizationsServicesAcceptanceTest extends BaseAuthorizationsServicesAcceptanceTest {

	//TODO Mieux tester la journalisation des modifications

	boolean checkIfChuckNorrisHasAccessToEverythingInZeCollection = true;

	LogicalSearchQuery recordsWithPrincipalPath = new LogicalSearchQuery(
			fromAllSchemasIn(zeCollection).where(PRINCIPAL_PATH).isNotNull());

	@Before
	public void enableAfterTestValidation() {
		checkIfChuckNorrisHasAccessToEverythingInZeCollection = true;
	}

	@After
	public void checkIfARecordHasAnInvalidAuthorization() {
		ensureNoRecordsHaveAnInvalidAuthorization();
	}

	@After
	public void checkIfChuckNorrisHasAccessToEverythingInZeCollection()
			throws Exception {

		if (records != null && checkIfChuckNorrisHasAccessToEverythingInZeCollection) {
			List<String> foldersWithReadFound = findAllFoldersAndDocuments(users.chuckNorrisIn(zeCollection));
			List<String> foldersWithWriteFound = findAllFoldersAndDocumentsWithWritePermission(
					users.chuckNorrisIn(zeCollection));
			List<String> foldersWithDeleteFound = findAllFoldersAndDocumentsWithDeletePermission(
					users.chuckNorrisIn(zeCollection));

			assertThat(foldersWithReadFound).containsOnly(records.allFoldersAndDocumentsIds().toArray(new String[0]));
			assertThat(foldersWithWriteFound).containsOnly(records.allFoldersAndDocumentsIds().toArray(new String[0]));
			assertThat(foldersWithDeleteFound).containsOnly(records.allFoldersAndDocumentsIds().toArray(new String[0]));
		}
	}

	@After
	public void checkIfAliceSeeAndCanModifyEverythingInCollection2()
			throws Exception {
		if (otherCollectionRecords != null) {
			List<String> foldersWithReadFound = findAllFoldersAndDocuments(users.aliceIn(anotherCollection));
			List<String> foldersWithWriteFound = findAllFoldersAndDocumentsWithWritePermission(users.aliceIn(anotherCollection));
			List<String> foldersWithDeleteFound = findAllFoldersAndDocumentsWithDeletePermission(
					users.aliceIn(anotherCollection));

			assertThat(foldersWithReadFound)
					.containsOnly(otherCollectionRecords.allFoldersAndDocumentsIds().toArray(new String[0]));
			assertThat(foldersWithWriteFound)
					.containsOnly(otherCollectionRecords.allFoldersAndDocumentsIds().toArray(new String[0]));
			assertThat(foldersWithDeleteFound).hasSize(0);
		}
	}

	@After
	public void checkIfBobSeeAndCanDeleteEverythingInCollection2()
			throws Exception {
		if (otherCollectionRecords != null) {
			List<String> foldersWithReadFound = findAllFoldersAndDocuments(users.bobIn(anotherCollection));
			List<String> foldersWithWriteFound = findAllFoldersAndDocumentsWithWritePermission(users.bobIn(anotherCollection));
			List<String> foldersWithDeleteFound = findAllFoldersAndDocumentsWithDeletePermission(users.bobIn(anotherCollection));

			assertThat(foldersWithReadFound)
					.containsOnly(otherCollectionRecords.allFoldersAndDocumentsIds().toArray(new String[0]));
			assertThat(foldersWithWriteFound).hasSize(0);
			assertThat(foldersWithDeleteFound)
					.containsOnly(otherCollectionRecords.allFoldersAndDocumentsIds().toArray(new String[0]));
		}
	}

	@After
	public void checkIfDakotaSeeAndCanDeleteEverythingInCollection2()
			throws Exception {
		if (otherCollectionRecords != null) {
			List<String> foldersWithReadFound = findAllFoldersAndDocuments(users.dakotaIn(anotherCollection));
			List<String> foldersWithWriteFound = findAllFoldersAndDocumentsWithWritePermission(users.dakotaIn(anotherCollection));
			List<String> foldersWithDeleteFound = findAllFoldersAndDocumentsWithDeletePermission(
					users.dakotaIn(anotherCollection));

			assertThat(foldersWithReadFound)
					.containsOnly(otherCollectionRecords.allFoldersAndDocumentsIds().toArray(new String[0]));
			assertThat(foldersWithWriteFound).hasSize(0);
			assertThat(foldersWithDeleteFound).hasSize(0);
		}
	}

	@Test
	public void whenRecordIsSecurizedThenHasAncestors()
			throws Exception {

		assertThatRecords(searchServices.search(recordsWithPrincipalPath))
				.extractingMetadatas(IDENTIFIER, ATTACHED_ANCESTORS).containsOnly(
				tuple(TAXO1_FOND1, asList(TAXO1_FOND1)),
				tuple(TAXO1_FOND1_1, asList(TAXO1_FOND1, TAXO1_FOND1_1)),
				tuple(FOLDER4_1, asList(TAXO1_FOND1, TAXO1_CATEGORY2, FOLDER4, FOLDER4_1)),
				tuple(FOLDER4_2, asList(TAXO1_FOND1, TAXO1_CATEGORY2, FOLDER4, FOLDER4_2)),
				tuple(FOLDER2, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER2)),
				tuple(FOLDER1, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER1)),
				tuple(TAXO1_CATEGORY1, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1)),
				tuple(FOLDER2_2_DOC2, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER2, FOLDER2_2, FOLDER2_2_DOC2)),
				tuple(FOLDER3, asList(TAXO1_FOND1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER3)),
				tuple(FOLDER4, asList(TAXO1_FOND1, TAXO1_CATEGORY2, FOLDER4)),
				tuple(FOLDER2_2_DOC1, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER2, FOLDER2_2, FOLDER2_2_DOC1)),
				tuple(FOLDER4_2_DOC1, asList(TAXO1_FOND1, TAXO1_CATEGORY2, FOLDER4, FOLDER4_2, FOLDER4_2_DOC1)),
				tuple(FOLDER1_DOC1, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER1, FOLDER1_DOC1)),
				tuple(FOLDER2_1, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER2, FOLDER2_1)),
				tuple(FOLDER2_2, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER2, FOLDER2_2)),
				tuple(TAXO1_CATEGORY2, asList(TAXO1_FOND1, TAXO1_CATEGORY2)),
				tuple(TAXO1_CATEGORY2_1, asList(TAXO1_FOND1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1)),
				tuple(FOLDER3_DOC1, asList(TAXO1_FOND1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1)),
				tuple(FOLDER4_1_DOC1, asList(TAXO1_FOND1, TAXO1_CATEGORY2, FOLDER4, FOLDER4_1, FOLDER4_1_DOC1))
		);

		detach(FOLDER4);
		detach(FOLDER2);

		assertThatRecords(searchServices.search(recordsWithPrincipalPath))
				.extractingMetadatas(IDENTIFIER, ATTACHED_ANCESTORS).containsOnly(
				tuple(TAXO1_FOND1, asList(TAXO1_FOND1)),
				tuple(TAXO1_FOND1_1, asList(TAXO1_FOND1, TAXO1_FOND1_1)),
				tuple(FOLDER4_1, asList(FOLDER4, FOLDER4_1)),
				tuple(FOLDER4_2, asList(FOLDER4, FOLDER4_2)),
				tuple(FOLDER2, asList(FOLDER2)),
				tuple(FOLDER1, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER1)),
				tuple(TAXO1_CATEGORY1, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1)),
				tuple(FOLDER2_2_DOC2, asList(FOLDER2, FOLDER2_2, FOLDER2_2_DOC2)),
				tuple(FOLDER3, asList(TAXO1_FOND1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER3)),
				tuple(FOLDER4, asList(FOLDER4)),
				tuple(FOLDER2_2_DOC1, asList(FOLDER2, FOLDER2_2, FOLDER2_2_DOC1)),
				tuple(FOLDER4_2_DOC1, asList(FOLDER4, FOLDER4_2, FOLDER4_2_DOC1)),
				tuple(FOLDER1_DOC1, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER1, FOLDER1_DOC1)),
				tuple(FOLDER2_1, asList(FOLDER2, FOLDER2_1)),
				tuple(FOLDER2_2, asList(FOLDER2, FOLDER2_2)),
				tuple(TAXO1_CATEGORY2, asList(TAXO1_FOND1, TAXO1_CATEGORY2)),
				tuple(TAXO1_CATEGORY2_1, asList(TAXO1_FOND1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1)),
				tuple(FOLDER3_DOC1, asList(TAXO1_FOND1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1)),
				tuple(FOLDER4_1_DOC1, asList(FOLDER4, FOLDER4_1, FOLDER4_1_DOC1))
		);

		reset(FOLDER2);
		detach(FOLDER4_1);

		assertThatRecords(searchServices.search(recordsWithPrincipalPath))
				.extractingMetadatas(IDENTIFIER, ATTACHED_ANCESTORS).containsOnly(
				tuple(TAXO1_FOND1, asList(TAXO1_FOND1)),
				tuple(TAXO1_FOND1_1, asList(TAXO1_FOND1, TAXO1_FOND1_1)),
				tuple(FOLDER4_1, asList(FOLDER4_1)),
				tuple(FOLDER4_2, asList(FOLDER4, FOLDER4_2)),
				tuple(FOLDER2, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER2)),
				tuple(FOLDER1, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER1)),
				tuple(TAXO1_CATEGORY1, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1)),
				tuple(FOLDER2_2_DOC2, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER2, FOLDER2_2, FOLDER2_2_DOC2)),
				tuple(FOLDER3, asList(TAXO1_FOND1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER3)),
				tuple(FOLDER4, asList(FOLDER4)),
				tuple(FOLDER2_2_DOC1, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER2, FOLDER2_2, FOLDER2_2_DOC1)),
				tuple(FOLDER4_2_DOC1, asList(FOLDER4, FOLDER4_2, FOLDER4_2_DOC1)),
				tuple(FOLDER1_DOC1, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER1, FOLDER1_DOC1)),
				tuple(FOLDER2_1, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER2, FOLDER2_1)),
				tuple(FOLDER2_2, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER2, FOLDER2_2)),
				tuple(TAXO1_CATEGORY2, asList(TAXO1_FOND1, TAXO1_CATEGORY2)),
				tuple(TAXO1_CATEGORY2_1, asList(TAXO1_FOND1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1)),
				tuple(FOLDER3_DOC1, asList(TAXO1_FOND1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1)),
				tuple(FOLDER4_1_DOC1, asList(FOLDER4_1, FOLDER4_1_DOC1))
		);
	}

	@Test
	public void whenRecordIsSecurizedThenHasInheritedRemovedAuths()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_FOND1).giving(ROLE1));
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).giving(ROLE1));

		LogicalSearchQuery query = new LogicalSearchQuery(
				fromAllSchemasIn(zeCollection).where(ALL_REMOVED_AUTHS).isNotNull());
		assertThatRecords(searchServices.search(query)).extractingMetadatas(IDENTIFIER, ALL_REMOVED_AUTHS).isEmpty();

		modify(authorizationOnRecord(auth1, TAXO1_CATEGORY1).removingItOnRecord());
		modify(authorizationOnRecord(auth2, FOLDER3).removingItOnRecord());
		assertThatRecords(searchServices.search(recordsWithPrincipalPath))
				.extractingMetadatas(IDENTIFIER, ALL_REMOVED_AUTHS).containsOnly(
				tuple(TAXO1_FOND1, new ArrayList<>()),
				tuple(TAXO1_FOND1_1, new ArrayList<>()),
				tuple(TAXO1_CATEGORY1, asList(auth1)),
				tuple(TAXO1_CATEGORY2, new ArrayList<>()),
				tuple(TAXO1_CATEGORY2_1, new ArrayList<>()),

				tuple(FOLDER1, asList(auth1)),
				tuple(FOLDER1_DOC1, asList(auth1)),
				tuple(FOLDER2, asList(auth1)),
				tuple(FOLDER2_1, asList(auth1)),
				tuple(FOLDER2_2, asList(auth1)),
				tuple(FOLDER2_2_DOC2, asList(auth1)),
				tuple(FOLDER2_2_DOC1, asList(auth1)),
				tuple(FOLDER3, asList(auth2)),
				tuple(FOLDER3_DOC1, asList(auth2)),
				tuple(FOLDER4, new ArrayList<>()),
				tuple(FOLDER4_1, new ArrayList<>()),
				tuple(FOLDER4_1_DOC1, new ArrayList<>()),
				tuple(FOLDER4_2, new ArrayList<>()),
				tuple(FOLDER4_2_DOC1, new ArrayList<>())
		);

		detach(FOLDER2_2);
		detach(FOLDER3_DOC1);

		assertThatRecords(searchServices.search(recordsWithPrincipalPath))
				.extractingMetadatas(IDENTIFIER, ALL_REMOVED_AUTHS).containsOnly(
				tuple(TAXO1_FOND1, new ArrayList<>()),
				tuple(TAXO1_FOND1_1, new ArrayList<>()),
				tuple(TAXO1_CATEGORY1, asList(auth1)),
				tuple(TAXO1_CATEGORY2, new ArrayList<>()),
				tuple(TAXO1_CATEGORY2_1, new ArrayList<>()),

				tuple(FOLDER1, asList(auth1)),
				tuple(FOLDER1_DOC1, asList(auth1)),
				tuple(FOLDER2, asList(auth1)),
				tuple(FOLDER2_1, asList(auth1)),
				tuple(FOLDER2_2, new ArrayList<>()),
				tuple(FOLDER2_2_DOC2, new ArrayList<>()),
				tuple(FOLDER2_2_DOC1, new ArrayList<>()),
				tuple(FOLDER3, asList(auth2)),
				tuple(FOLDER3_DOC1, new ArrayList<>()),
				tuple(FOLDER4, new ArrayList<>()),
				tuple(FOLDER4_1, new ArrayList<>()),
				tuple(FOLDER4_1_DOC1, new ArrayList<>()),
				tuple(FOLDER4_2, new ArrayList<>()),
				tuple(FOLDER4_2_DOC1, new ArrayList<>())
		);

	}

	@Test
	public void givenRoleAuthorizationsOnPrincipalConceptsThenInheritedInHierarchy()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).giving(ROLE1));
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).giving(ROLE1));
		auth3 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2_1).giving(ROLE1));
		auth4 = add(authorizationForUser(sasquatch).on(FOLDER1).giving(ROLE2));

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRoles(ROLE1).forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingRoles(ROLE1).forPrincipals(heroes),
				authOnRecord(TAXO1_CATEGORY2_1).givingRoles(ROLE1).forPrincipals(alice),
				authOnRecord(FOLDER1).givingRoles(ROLE2).forPrincipals(sasquatch)
		);

		//TODO Should be inherited in child groups : Robin would have ROLE1
		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4, FOLDER4_1, FOLDER4_1_DOC1, FOLDER4_2, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithRole(ROLE1).containsOnly(bob, charles, dakota, gandalf);
			verifyRecord.usersWithRole(ROLE2).isEmpty();
			verifyRecord.usersWithRole(ROLE3).isEmpty();
			verifyRecord.usersWithWriteAccess().containsOnly(chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithRole(ROLE1).containsOnly(bob, alice, charles, dakota, gandalf);
			verifyRecord.usersWithRole(ROLE2).isEmpty();
			verifyRecord.usersWithRole(ROLE3).isEmpty();
			verifyRecord.usersWithWriteAccess().containsOnly(chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(FOLDER1)) {
			verifyRecord.usersWithRole(ROLE2).containsOnly(sasquatch);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}
	}

	@Test
	public void givenAuthsOnChildGroupThenOnlyInheritedIfChildToParentMode()
			throws Exception {

		auth1 = add(authorizationForGroup(legends).on(TAXO1_CATEGORY2).giving(ROLE1));
		auth2 = add(authorizationForGroup(rumors).on(TAXO1_CATEGORY2).giving(ROLE2));

		//TODO Should be inherited in child groups : Robin would have ROLE1
		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2)) {
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).containsOnly(sasquatch, gandalf, admin, alice, edouard);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE2).containsOnly(sasquatch, admin);
		}

		givenConfig(ConstellioEIMConfigs.GROUP_AUTHORIZATIONS_INHERITANCE, GroupAuthorizationsInheritance.FROM_CHILD_TO_PARENT);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2)) {
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).containsOnly(gandalf, admin, alice, edouard);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE2).containsOnly(sasquatch, gandalf, admin, alice, edouard);
		}
	}

	@Test
	public void givenRolesOfAuthorizationAreModifiedOnSameRecordOfAuthorizationThenNotDuplicatedAndInstantaneousEffectOnSecurity()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).giving(ROLE1));
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).giving(ROLE1));

		assertThat(modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2).withNewAccessAndRoles(ROLE2, ROLE3)))
				.isNot(creatingACopy()).isNot(deleted());
		assertThat(modify(authorizationOnRecord(auth2, TAXO1_CATEGORY2).withNewAccessAndRoles(ROLE1, ROLE3)))
				.isNot(creatingACopy()).isNot(deleted());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRoles(ROLE2, ROLE3).forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingRoles(ROLE1, ROLE3).forPrincipals(heroes)
		);

		//TODO Should be inherited in child groups : Robin should have ROLE1 and ROLE3
		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER3_DOC1, FOLDER4_1_DOC1, FOLDER4_2)) {
			verifyRecord.usersWithRole(ROLE1).containsOnly(charles, dakota, gandalf);
			verifyRecord.usersWithRole(ROLE2).containsOnly(bob);
			verifyRecord.usersWithRole(ROLE3).containsOnly(bob, charles, dakota, gandalf);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

	}

	@Test
	public void givenAccessAuthorizationsOnPrincipalConceptsThenInheritedInHierarchy()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth3 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2_1).givingReadWriteAccess());
		auth4 = add(authorizationForUser(sasquatch).on(FOLDER1).givingReadWriteAccess());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(heroes),
				authOnRecord(TAXO1_CATEGORY2_1).givingReadWrite().forPrincipals(alice),
				authOnRecord(FOLDER1).givingReadWrite().forPrincipals(sasquatch)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4, FOLDER4_1, FOLDER4_1_DOC1, FOLDER4_2, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, charles, dakota, gandalf, chuck, robin);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, alice, charles, dakota, chuck, robin, gandalf);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(FOLDER1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(sasquatch, chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

	}

	@Test
	public void givenAccessTypesOfAuthorizationAreModifiedOnSameRecordOfAuthorizationThenNotDuplicatedAndInstantaneousEffectOnSecurity()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadAccess());
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadAccess());

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, TAXO1_CATEGORY2)) {
			verifyRecord.usersWithDeleteAccess().containsOnly(chuck);
			verifyRecord.usersWithWriteAccess().containsOnly(chuck);
		}

		assertThat(modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2).withNewAccessAndRoles(WRITE, DELETE)))
				.isNot(creatingACopy()).isNot(deleted());
		assertThat(modify(authorizationOnRecord(auth2, TAXO1_CATEGORY2).withNewAccessAndRoles(WRITE)))
				.isNot(creatingACopy()).isNot(deleted());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWriteDelete().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(heroes)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER4, FOLDER4_1_DOC1, FOLDER3_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(charles, dakota, gandalf, bob, robin, chuck);
			verifyRecord.usersWithDeleteAccess().containsOnly(bob, chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

	}

	@Test
	public void givenPrincipalsAreModifiedOnSameRecordOfAuthorizationThenNotDuplicatedAndInstantaneousEffectOnSecurity()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadAccess());

		assertThat(modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2).withNewPrincipalIds(robin)))
				.isNot(creatingACopy()).isNot(deleted());
		assertThat(modify(authorizationOnRecord(auth2, TAXO1_CATEGORY2).withNewPrincipalIds(legends, bob)))
				.isNot(creatingACopy()).isNot(deleted());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(robin),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(legends, bob)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4, FOLDER4_2_DOC1, TAXO1_CATEGORY2_1, FOLDER3,
				FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(sasquatch, gandalf, edouard, alice, bob, robin, chuck);
			verifyRecord.usersWithWriteAccess().containsOnly(robin, chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

	}

	@Test
	public void givenPrincipalsAreModifiedOnRecordOfAuthorizationKeepingAttachedThenDuplicatedAndInstantaneousEffectOnSecurity()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadAccess());

		assertThat(modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2_1).withNewPrincipalIds(robin)))
				.is(creatingACopy()).isNot(deleted());
		assertThat(modify(authorizationOnRecord(auth2, TAXO1_CATEGORY2_1).withNewPrincipalIds(legends, bob)))
				.is(creatingACopy()).isNot(deleted());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(bob).removedOnRecords(TAXO1_CATEGORY2_1),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(heroes).removedOnRecords(TAXO1_CATEGORY2_1),
				authOnRecord(TAXO1_CATEGORY2_1).givingReadWrite().forPrincipals(robin),
				authOnRecord(TAXO1_CATEGORY2_1).givingRead().forPrincipals(legends, bob)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(charles, dakota, gandalf, robin, bob, chuck);
			verifyRecord.usersWithWriteAccess().containsOnly(bob, chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(sasquatch, gandalf, edouard, alice, bob, robin, chuck);
			verifyRecord.usersWithWriteAccess().containsOnly(robin, chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

	}

	@Test
	public void whenModifyingAnInvalidAuthorizationOnRecordThenException()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(FOLDER4).givingReadAccess());
		auth2 = add(authorizationForGroup(heroes).on(FOLDER4).givingReadAccess());

		detach(FOLDER4_1);

		try {
			modify(authorizationOnRecord(auth1, FOLDER4_1).withNewPrincipalIds(robin));
			fail("Exception expected");
		} catch (NoSuchAuthorizationWithIdOnRecord e) {
			//OK
		}

		try {
			modify(authorizationOnRecord(auth2, FOLDER4_1).withNewPrincipalIds(robin));
			fail("Exception expected");
		} catch (NoSuchAuthorizationWithIdOnRecord e) {
			//OK
		}

		try {
			modify(authorizationOnRecord("invalidAuth", FOLDER4_1).withNewPrincipalIds(robin));
			fail("Exception expected");
		} catch (NoSuchAuthorizationWithId e) {
			//OK
		}
	}

	@Test
	public void givenPrincipalsAreModifiedOnRecordOfAuthorizationDetachingThenDuplicatedAndInstantaneousEffectOnSecurity()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadAccess());
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadAccess());

		Map<String, String> copies = detach(FOLDER3);
		String auth1CopyInCategory2_1 = copies.get(auth1);
		String auth2CopyInCategory2_1 = copies.get(auth2);

		request1 = modify(authorizationOnRecord(auth1CopyInCategory2_1, FOLDER3).withNewPrincipalIds(robin));

		assertThat(request1).isNot(creatingACopy()).isNot(deleted());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(heroes),
				authOnRecord(FOLDER3).givingRead().forPrincipals(robin),
				authOnRecord(FOLDER3).givingRead().forPrincipals(heroes)
		);

		request2 = modify(authorizationOnRecord(auth2CopyInCategory2_1, FOLDER3)
				.withNewPrincipalIds(legends, bob));
		assertThat(request2).isNot(creatingACopy()).isNot(deleted());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(heroes),
				authOnRecord(FOLDER3).givingRead().forPrincipals(robin),
				authOnRecord(FOLDER3).givingRead().forPrincipals(legends, bob)
		);

		assertThatAuth(auth1).hasPrincipals(bob);
		assertThatAuth(auth2).hasPrincipals(heroes);
		assertThatAuth(auth1CopyInCategory2_1).hasPrincipals(robin);
		assertThatAuth(auth2CopyInCategory2_1).hasPrincipals(legends, bob);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER4, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(charles, dakota, gandalf, robin, bob, chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(sasquatch, gandalf, edouard, alice, bob, chuck, robin);
			verifyRecord.usersWithWriteAccess().containsOnly(chuck);
			if (verifyRecord.recordId.equals(FOLDER3)) {
				verifyRecord.detachedAuthorizationFlag().isTrue();
			} else {
				verifyRecord.detachedAuthorizationFlag().isFalse();
			}
		}

	}

	@Test
	public void givenGroupAuthorizationsWhenAddOrRemoveUsersInGroupThenInstantaneousEffectOnSecurity()
			throws Exception {

		add(authorizationForGroup(heroes).on(TAXO1_CATEGORY1).givingReadWriteAccess());
		add(authorizationForGroup(heroes).on(TAXO1_CATEGORY1).giving(ROLE1));
		add(authorizationForGroup(heroes).on(FOLDER4).givingReadWriteDeleteAccess());
		add(authorizationForGroup(heroes).on(FOLDER4).giving(ROLE2));

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER1, FOLDER2, FOLDER2_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(charles, dakota, gandalf, chuck, robin);
			//TODO Should be inherited in child groups : Robin would be expected
			verifyRecord.usersWithRole(ROLE1).containsOnly(charles, dakota, gandalf);
		}

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithDeleteAccess().containsOnly(charles, dakota, gandalf, chuck, robin);

			//TODO Should be inherited in child groups : Robin would be expected
			verifyRecord.usersWithRole(ROLE2).containsOnly(charles, dakota, gandalf);
		}

		givenUser(charles).isRemovedFromGroup(heroes);
		givenUser(robin).isRemovedFromGroup(sidekicks);
		givenUser(sasquatch).isAddedInGroup(heroes);
		givenUser(edouard).isAddedInGroup(sidekicks);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER1, FOLDER2, FOLDER2_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(sasquatch, dakota, gandalf, chuck, edouard);
			//TODO Should be inherited in child groups : Edouard would be expected
			verifyRecord.usersWithRole(ROLE1).containsOnly(sasquatch, dakota, gandalf);
		}

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithDeleteAccess().containsOnly(sasquatch, dakota, gandalf, chuck, edouard);

			//TODO Should be inherited in child groups : Edouard would expected
			verifyRecord.usersWithRole(ROLE2).containsOnly(sasquatch, dakota, gandalf);
		}

		assertThatBatchProcessDuringTest().hasSize(0);

	}

	@Test
	public void whenAddingAndRemovingAuthorizationToAGroupThenAppliedToAllUsers()
			throws Exception {

		GlobalGroup group = userServices.createGlobalGroup("vilains", "Vilains", new ArrayList<String>(), null, ACTIVE, true);
		userServices.addUpdateGlobalGroup(group);
		userServices.setGlobalGroupUsers("vilains", asList(users.bob()));
		forUser(bob).assertThatRecordsWithReadAccess().isEmpty();

		auth1 = add(authorizationForGroup("vilains").on(TAXO1_CATEGORY1).givingReadAccess());
		forUser(bob).assertThatRecordsWithReadAccess().containsOnly(TAXO1_CATEGORY1, FOLDER1, FOLDER1_DOC1, FOLDER2, FOLDER2_1,
				FOLDER2_2, FOLDER2_2_DOC1, FOLDER2_2_DOC2);

		modify(authorizationOnRecord(auth1, TAXO1_CATEGORY1).removingItOnRecord());
		forUser(bob).assertThatRecordsWithReadAccess().isEmpty();

	}

	@Test
	public void givenAuthorizationWhenModifyingAuthorizationWithoutPrincipalsThenValidationException()
			throws Exception {

		String aliceId = users.aliceIn(zeCollection).getId();

		try {
			add(authorizationInCollection(zeCollection).givingReadAccess().on(FOLDER4));
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords e) {
			//OK
		}

		try {
			add(authorizationInCollection(zeCollection).givingReadAccess().forPrincipalsIds(new ArrayList<String>())
					.on(FOLDER4));
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords e) {
			//OK
		}

		try {
			add(authorizationInCollection(zeCollection).givingReadAccess().forPrincipalsIds(asList(aliceId)));
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords e) {
			//OK
		}

		auth1 = add(authorizationForUser(alice).on(FOLDER4).givingReadAccess());

		try {
			modify(authorizationOnRecord(auth1, FOLDER4).withNewPrincipalIds(new ArrayList<String>()));
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords e) {
			//OK
		}

		try {
			modify(authorizationOnRecord(auth1, FOLDER4_1).withNewPrincipalIds(new ArrayList<String>()));
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords e) {
			//OK
		}
	}

	@Test
	public void givenAuthorizationWhenModifyingAuthorizationWithInvalidPrincipalsThenValidationException()
			throws Exception {

		try {
			auth1 = add(authorizationInCollection(zeCollection).givingReadAccess().forPrincipalsIds("inexistentId1")
					.on(TAXO1_CATEGORY1));
			fail("Exception expected");
		} catch (InvalidPrincipalsIds e) {
			//OK
		}

		try {
			List<String> roles = asList(READ);
			addAuthorizationWithoutDetaching(roles, asList(users.aliceIn(zeCollection).getId()), "inexistentId2");
			fail("Exception expected");
		} catch (InvalidTargetRecordId e) {
			//OK
		}

		auth1 = add(authorizationForUser(alice).on(FOLDER4).givingReadAccess());

		//Cannot modify an authorization with an invalid principal id
		try {
			modify(authorizationOnRecord(auth1, FOLDER4).withNewPrincipalIds(asList("inexistentId3")));
			fail("Exception expected");
		} catch (NoSuchPrincipalWithUsername e) {
			//OK
		}
		try {
			modify(authorizationOnRecord(auth1, FOLDER4_1).withNewPrincipalIds(asList("inexistentId4")));
			fail("Exception expected");
		} catch (NoSuchPrincipalWithUsername e) {
			//OK
		}
		//Nothing changed
		assertThatAuth(auth1).hasPrincipals(alice);

	}

	@Test(expected = AuthorizationsServicesRuntimeException.CannotDetachConcept.class)
	public void whenTryToDetachConceptThenException()
			throws Exception {

		detach(TAXO1_CATEGORY2);
	}

	@Test
	public void whenDetachingASecurizedRecordThenCustomAuthKeptAndRemovedAuthNotCopied()
			throws Exception {

		auth1 = add(authorizationForUser(alice).on(FOLDER4).givingReadAccess());
		auth2 = add(authorizationForUser(bob).on(FOLDER4).givingReadAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER4).givingReadWriteDeleteAccess());
		auth4 = add(authorizationForUser(dakota).on(FOLDER4_1).givingReadWriteAccess());

		modify(authorizationOnRecord(auth1, FOLDER4_1).removingItOnRecord());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(FOLDER4).removedOnRecords(FOLDER4_1).givingRead().forPrincipals(alice),
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingReadWriteDelete().forPrincipals(charles),
				authOnRecord(FOLDER4_1).givingReadWrite().forPrincipals(dakota)
		);

		detach(FOLDER4_1);
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(alice),
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingReadWriteDelete().forPrincipals(charles),
				authOnRecord(FOLDER4_1).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4_1).givingReadWriteDelete().forPrincipals(charles),
				authOnRecord(FOLDER4_1).givingReadWrite().forPrincipals(dakota)
		);

		//Detaching it twice, nothing changes...
		detach(FOLDER4_1);
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(alice),
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingReadWriteDelete().forPrincipals(charles),
				authOnRecord(FOLDER4_1).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4_1).givingReadWriteDelete().forPrincipals(charles),
				authOnRecord(FOLDER4_1).givingReadWrite().forPrincipals(dakota)
		);

		reset(FOLDER4_1);
		verifyRecord(FOLDER4_1).detachedAuthorizationFlag().isFalse();
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(alice),
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingReadWriteDelete().forPrincipals(charles)
		);

		//Resetting it twice, nothing changes
		reset(FOLDER4_1);
		verifyRecord(FOLDER4_1).detachedAuthorizationFlag().isFalse();
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(alice),
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingReadWriteDelete().forPrincipals(charles)
		);
	}

	@Test
	public void whenDetachingARootSecurizedRecordThenCustomAuthKeptAndRemovedAuthNotCopied()
			throws Exception {

		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadAccess());
		auth2 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadAccess());
		auth3 = add(authorizationForUser(charles).on(TAXO1_CATEGORY2).givingReadWriteDeleteAccess());
		auth4 = add(authorizationForUser(dakota).on(FOLDER4).givingReadWriteAccess());

		modify(authorizationOnRecord(auth1, FOLDER4).removingItOnRecord());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).removedOnRecords(FOLDER4).givingRead().forPrincipals(alice),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingReadWriteDelete().forPrincipals(charles),
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(dakota)
		);

		detach(FOLDER4);
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(alice),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingReadWriteDelete().forPrincipals(charles),
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingReadWriteDelete().forPrincipals(charles),
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(dakota)
		);

		reset(FOLDER4);
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isFalse();
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(alice),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingReadWriteDelete().forPrincipals(charles)
		);
	}

	@Test
	public void whenResettingASecurizedRecordThenCustomAuthDeletedAndRemovedAuthReenabled()
			throws Exception {

		auth1 = add(authorizationForUser(alice).on(FOLDER4).givingReadAccess());
		auth2 = add(authorizationForUser(bob).on(FOLDER4).givingReadAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER4).givingReadWriteDeleteAccess());
		auth4 = add(authorizationForUser(dakota).on(FOLDER4_1).givingReadWriteAccess());

		modify(authorizationOnRecord(auth1, FOLDER4_1).removingItOnRecord());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(FOLDER4).removedOnRecords(FOLDER4_1).givingRead().forPrincipals(alice),
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingReadWriteDelete().forPrincipals(charles),
				authOnRecord(FOLDER4_1).givingReadWrite().forPrincipals(dakota)
		);

		reset(FOLDER4_1);

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(alice),
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingReadWriteDelete().forPrincipals(charles)
		);

		verifyRecord(FOLDER4_1).detachedAuthorizationFlag().isFalse();

	}

	@Test
	public void whenResettingARootSecurizedRecordThenCustomAuthDeletedAndRemovedAuthReenabled()
			throws Exception {

		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadAccess());
		auth2 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadAccess());
		auth3 = add(authorizationForUser(charles).on(TAXO1_CATEGORY2).givingReadWriteDeleteAccess());
		auth4 = add(authorizationForUser(dakota).on(FOLDER4).givingReadWriteAccess());

		modify(authorizationOnRecord(auth1, FOLDER4).removingItOnRecord());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).removedOnRecords(FOLDER4).givingRead().forPrincipals(alice),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingReadWriteDelete().forPrincipals(charles),
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(dakota)
		);

		reset(FOLDER4);

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(alice),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingReadWriteDelete().forPrincipals(charles)
		);

		verifyRecord(FOLDER4).detachedAuthorizationFlag().isFalse();

	}

	@Test
	public void givenDetachedRecordNoMatterWhatIsDoneOnItsAncestorsThenNotInheritedOnTheDetachedRecord()
			throws Exception {

		auth1 = add(authorizationForGroup(heroes).on(FOLDER4).givingReadAccess());
		auth2 = add(authorizationForGroup(legends).on(FOLDER4).givingReadAccess());
		auth3 = add(authorizationForUser(bob).on(FOLDER4_1).givingReadAccess());

		Map<String, String> newAuths = detach(FOLDER4_1);
		verifyRecord(records.folder4().getId()).detachedAuthorizationFlag().isFalse();
		verifyRecord(records.folder4_1().getId()).detachedAuthorizationFlag().isTrue();
		forUser(charles).assertThatAllFoldersAndDocuments().contains(FOLDER4_1, FOLDER4_1_DOC1);

		modify(authorizationOnRecord(newAuths.get(auth1), FOLDER4_1).removingItOnRecord());
		verifyRecord(records.folder4().getId()).detachedAuthorizationFlag().isFalse();
		verifyRecord(records.folder4_1().getId()).detachedAuthorizationFlag().isTrue();
		forUser(charles).assertThatAllFoldersAndDocuments().doesNotContain(FOLDER4_1, FOLDER4_1_DOC1);

		//Even if we reset folder4, still no access on folder4_1
		reset(FOLDER4);
		forUser(charles).assertThatAllFoldersAndDocuments().doesNotContain(FOLDER4_1, FOLDER4_1_DOC1);

		//Even if we add an auth on folder4 or category2, still no access on folder4_1
		auth4 = add(authorizationForUser(charles).on(TAXO1_CATEGORY2).givingReadAccess());
		auth5 = add(authorizationForUser(charles).on(FOLDER4).givingReadAccess());
		forUser(charles).assertThatAllFoldersAndDocuments().doesNotContain(FOLDER4_1, FOLDER4_1_DOC1);

		//If you we reattach the record, it gains the auths
		reset(FOLDER4_1);
		forUser(charles).assertThatAllFoldersAndDocuments().contains(FOLDER4_1, FOLDER4_1_DOC1);
	}

	private void reset(String id) {
		services.reset(get(id));
		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void whenModifyingAuthorizationPrincipalsThenCreateCopyIfInherited()
			throws Exception {

		auth1 = add(authorizationForUsers(alice, bob, charles).on(FOLDER4).givingReadAccess());

		assertThat(modify(authorizationOnRecord(auth1, FOLDER4_2).withNewPrincipalIds(alice, charles)))
				.is(creatingACopy()).isNot(deleted());

		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(chuck, alice, bob, charles);
		verifyRecord(FOLDER4_1).usersWithReadAccess().containsOnly(chuck, alice, bob, charles);
		verifyRecord(FOLDER4_2).usersWithReadAccess().containsOnly(chuck, alice, charles);

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(FOLDER4).removedOnRecords(FOLDER4_2).givingRead().forPrincipals(alice, bob, charles),
				authOnRecord(FOLDER4_2).givingRead().forPrincipals(alice, charles)
		);

		assertThat(modify(authorizationOnRecord(auth1, FOLDER4).withNewPrincipalIds(alice, bob, dakota)))
				.isNot(creatingACopy()).isNot(deleted());

		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(chuck, alice, bob, dakota);
		verifyRecord(FOLDER4_1).usersWithReadAccess().containsOnly(chuck, alice, bob, dakota);
		verifyRecord(FOLDER4_2).usersWithReadAccess().containsOnly(chuck, alice, charles);

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(FOLDER4).removedOnRecords(FOLDER4_2).givingRead().forPrincipals(alice, bob, dakota),
				authOnRecord(FOLDER4_2).givingRead().forPrincipals(alice, charles)
		);

	}

	@Test(expected = AuthorizationsServicesRuntimeException.CannotAddAuhtorizationInNonPrincipalTaxonomy.class)
	public void whenAddingAuthorizationOnAconceptOfASecondaryTaxonomyThenException()
			throws Exception {

		add(authorizationForGroups(heroes).on(TAXO2_STATION2_1).givingReadAccess());
	}

	@Test
	public void whenGetRecordsAuthorizationsThenObtainsAuthorizations()
			throws Exception {

		auth1 = add(authorizationForGroup(legends).on(FOLDER4).givingReadAccess());
		auth2 = add(authorizationForPrincipals(heroes, dakota).on(FOLDER2).givingReadWriteAccess());
		auth3 = add(authorizationForUser(dakota).on(TAXO1_CATEGORY1).givingReadAccess());

		assertThatAuthorizationsOn(FOLDER2).containsOnly(
				authOnRecord(FOLDER2).givingReadWrite().forPrincipals(heroes, dakota),
				authOnRecord(TAXO1_CATEGORY1).givingRead().forPrincipals(dakota));

		assertThatAuthorizationsOn(FOLDER4).containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(legends));

		assertThatAuthorizationsOn(TAXO1_CATEGORY1).containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingRead().forPrincipals(dakota));

		assertThatAuthorizationsFor(legends).containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(legends));

		assertThatAuthorizationsFor(heroes).containsOnly(
				authOnRecord(FOLDER2).givingReadWrite().forPrincipals(heroes, dakota));

		assertThatAuthorizationsFor(dakota).containsOnly(
				authOnRecord(FOLDER2).givingReadWrite().forPrincipals(heroes, dakota),
				authOnRecord(TAXO1_CATEGORY1).givingRead().forPrincipals(dakota));

		assertThatAuthorizationsFor(gandalf).containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(legends),
				authOnRecord(FOLDER2).givingReadWrite().forPrincipals(heroes, dakota));

		assertThatAuthorizationsFor(edouard).containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(legends));
	}

	@Test
	public void whenNewGroupsAreAssignedToAUserThenLostPreviousGroupAccessAndGainNewGroupAccess()
			throws Exception {

		List<String> roles = asList(READ);

		auth1 = add(authorizationForGroups(legends).on(TAXO1_CATEGORY2).givingReadAccess());
		auth2 = add(authorizationForGroups(heroes).on(FOLDER2).givingReadAccess());
		auth3 = add(authorizationForUser(alice).on(FOLDER1).givingReadAccess());
		forUser(alice).assertThatRecordsWithReadAccess().containsOnly(
				TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER1, FOLDER1_DOC1, FOLDER3, FOLDER3_DOC1, FOLDER4, FOLDER4_1,
				FOLDER4_1_DOC1, FOLDER4_2, FOLDER4_2_DOC1);

		givenUser(alice).isRemovedFromGroup(legends);
		forUser(alice).assertThatRecordsWithReadAccess().containsOnly(FOLDER1, FOLDER1_DOC1);

		givenUser(alice).isAddedInGroup(heroes);
		forUser(alice).assertThatRecordsWithReadAccess().containsOnly(
				FOLDER1, FOLDER1_DOC1, FOLDER2, FOLDER2_1, FOLDER2_2, FOLDER2_2_DOC1, FOLDER2_2_DOC2);
	}

	@Test
	public void givenAuthorizationsWithStartAndEndDateThenOnlyActiveDuringSpecifiedTimerange()
			throws Exception {

		givenTimeIs(date(2016, 4, 4));

		//A daily authorizaiton
		auth1 = add(authorizationForUser(aliceWonderland).on(TAXO1_FOND1_1)
				.startingOn(date(2016, 4, 5)).endingOn(date(2016, 4, 5)).givingReadWriteAccess());

		//A 4 day authorizaiton
		auth2 = add(authorizationForUser(bob).on(TAXO1_FOND1_1)
				.startingOn(date(2016, 4, 5)).endingOn(date(2016, 4, 8)).givingReadWriteAccess());

		//A future authorization
		auth3 = add(authorizationForUser(charles).on(TAXO1_FOND1_1)
				.startingOn(date(2016, 4, 7)).givingReadWriteAccess());

		//An authorization with an end
		auth4 = add(authorizationForUser(dakota).on(TAXO1_FOND1_1)
				.endingOn(date(2016, 4, 6)).givingReadWriteAccess());

		auth5 = add(authorizationForUser(edouard).on(TAXO1_FOND1_1).givingReadWriteAccess());

		//An authorization started in the past
		auth6 = add(authorizationForUser(gandalf).on(TAXO1_FOND1_1)
				.during(date(2016, 4, 3), date(2016, 4, 6)).givingReadWriteAccess());

		//An authorization already finished
		try {
			auth7 = add(authorizationForUser(sasquatch).on(TAXO1_FOND1_1)
					.during(date(2016, 4, 1), date(2016, 4, 3)).givingReadWriteAccess());
			fail("Exception expected");
		} catch (AuthorizationDetailsManagerRuntimeException.EndDateLessThanCurrentDate e) {
			//OK
		}

		services.refreshActivationForAllAuths(collectionsListManager.getCollections());

		givenTimeIs(date(2016, 4, 4));
		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, dakota, edouard, gandalf);
		}

		givenTimeIs(date(2016, 4, 5));
		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, dakota, edouard, alice, bob, gandalf);
		}

		givenTimeIs(date(2016, 4, 6));
		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, dakota, edouard, bob, gandalf);
		}

		givenTimeIs(date(2016, 4, 7));
		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, edouard, bob, charles);
		}

		givenTimeIs(date(2016, 4, 8));
		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, charles, edouard, bob);
		}

		givenTimeIs(date(2016, 4, 9));
		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, charles, edouard);
		}

		assertThatAllAuthorizationsIds().containsOnly(auth1, auth2, auth3, auth4, auth5, auth6);
		services.refreshActivationForAllAuths(collectionsListManager.getCollections());
		assertThatAllAuthorizationsIds().containsOnly(auth3, auth5);

	}

	@Test
	public void givenUserWithCollectionAccessThenHasAccessNoMatterTheRecordsAuthorizationAndHasNoRolePermissions()
			throws Exception {

		Transaction transaction = new Transaction();
		transaction.add(users.edouardIn(zeCollection).setCollectionReadAccess(true).setSystemAdmin(true));
		transaction.add(users.charlesIn(zeCollection).setCollectionReadAccess(true));
		transaction.add(users.dakotaIn(zeCollection).setCollectionWriteAccess(true));
		transaction.add(users.sasquatchIn(zeCollection).setCollectionWriteAccess(true).setCollectionDeleteAccess(true));
		transaction.add(users.aliceIn(zeCollection).setCollectionDeleteAccess(true));
		recordServices.execute(transaction);
		auth1 = add(authorizationForUser(charles).on(TAXO1_FOND1_1).givingReadWriteAccess());
		auth2 = add(authorizationForUser(dakota).on(TAXO1_FOND1_1).givingReadAccess());

		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER2)) {
			verifyRecord.usersWithReadAccess().containsOnly(charles, dakota, alice, chuck, edouard, sasquatch);
			verifyRecord.usersWithWriteAccess().containsOnly(charles, dakota, chuck, sasquatch);
			verifyRecord.usersWithDeleteAccess().containsOnly(alice, chuck, sasquatch);
			verifyRecord.usersWithPermission("aPermission").containsOnly(admin, edouard);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_FOND1, TAXO1_CATEGORY2_1, FOLDER3)) {
			verifyRecord.usersWithReadAccess().containsOnly(charles, dakota, alice, chuck, edouard, sasquatch);
			verifyRecord.usersWithWriteAccess().containsOnly(dakota, chuck, sasquatch);
			verifyRecord.usersWithDeleteAccess().containsOnly(alice, chuck, sasquatch);
			verifyRecord.usersWithPermission("aPermission").containsOnly(admin, edouard);
		}

		assertThat(users.edouardIn(zeCollection).hasReadAccess().globally()).isTrue();
		assertThat(users.edouardIn(zeCollection).hasWriteAccess().globally()).isFalse();
		assertThat(users.edouardIn(zeCollection).hasDeleteAccess().globally()).isFalse();

		assertThat(users.dakotaIn(zeCollection).hasReadAccess().globally()).isTrue();
		assertThat(users.dakotaIn(zeCollection).hasWriteAccess().globally()).isTrue();
		assertThat(users.dakotaIn(zeCollection).hasDeleteAccess().globally()).isFalse();

		assertThat(users.aliceIn(zeCollection).hasReadAccess().globally()).isTrue();
		assertThat(users.aliceIn(zeCollection).hasWriteAccess().globally()).isFalse();
		assertThat(users.aliceIn(zeCollection).hasDeleteAccess().globally()).isTrue();

		assertThat(users.sasquatchIn(zeCollection).hasReadAccess().globally()).isTrue();
		assertThat(users.sasquatchIn(zeCollection).hasWriteAccess().globally()).isTrue();
		assertThat(users.sasquatchIn(zeCollection).hasDeleteAccess().globally()).isTrue();
	}

	//@Test
	public void givenGroupWithCollectionAccessThenHasAccessNoMatterTheRecordsAuthorizationAndHasNoRolePermissions()
			throws Exception {

		//Valider que ça ne donne pas un accès permission

		//Donner au groupe Heroes, une permission globale RW
		//Donner au groupe Legends, une permission globale RD
		//Donner au groupe Rumors, une permission globale RW
		//Donner à Charles (groupe Heroes) une permission globale R
		//Donner à Dakota (groupe Heroes) une permission globale RD
		//Donner à Alice (groupe Legends) aucune permission

		//Sur quelques records :

		//Valider que Charles (groupe Heroes) a RW
		//Valider que Dakota (groupe Heroes) a RWD
		//Valider que Gandalf (groupe Heroes et Legends) a RWD

		//Valider que Alice (Legends) a RD
		//Valider que Édouard (Legends) a RD
		//Valider que Sasquatch (Rumors) a RWD - toutes les permissions globales données dans Legends sont héritées par le sous-groupe rumors
		//Valider que Bob n'a rien (aucun groupe)

		//fail("todo");
	}

	@Test
	public void givenUserWithDeletePermissionOnRecordsThenCanOnlyDeleteRecordsIfHasPermissionOnWholeHierarchy()
			throws Exception {

		recordServices.logicallyDelete(records.folder2(), users.chuckNorrisIn(zeCollection));

		//Bob has no delete permission
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER4).isFalse();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_FOND1_1).isFalse();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_CATEGORY1).isFalse();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER1).isFalse();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER2).isFalse();

		//Bob has a delete permission the whole hierarchy
		auth1 = add(authorizationForUser(bob).on(TAXO1_FOND1).givingReadWriteDeleteAccess());
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER4).isTrue();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_FOND1_1).isTrue();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_CATEGORY1).isTrue();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER1).isTrue();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER2).isFalse();

		//Bob has a delete permission on folder 4
		modify(authorizationOnRecord(auth1, FOLDER4).removingItOnRecord());
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER4).isFalse();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_FOND1_1).isTrue();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_CATEGORY1).isTrue();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER1).isTrue();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER2).isFalse();

		//Bob has a delete permission on category 1
		modify(authorizationOnRecord(auth1, TAXO1_CATEGORY1).removingItOnRecord());
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER4).isFalse();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_FOND1_1).isFalse();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_CATEGORY1).isFalse();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER1).isFalse();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER2).isFalse();

	}

	@Test
	public void givenUserWithDeletePermissionOnRecordsThenCanOnlyRestoreRecordsIfHasPermissionOnWholeHierarchy()
			throws Exception {

		recordServices.logicallyDelete(records.folder1(), users.chuckNorrisIn(zeCollection));
		recordServices.logicallyDelete(records.folder4(), users.chuckNorrisIn(zeCollection));
		recordServices.logicallyDelete(records.taxo1_fond1_1(), users.chuckNorrisIn(zeCollection));

		//Bob has no delete permission
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER4).isFalse();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_FOND1_1).isFalse();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_CATEGORY1).isFalse();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER1).isFalse();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER2).isFalse();

		//Bob has a delete permission the whole hierarchy
		auth1 = add(authorizationForUser(bob).on(TAXO1_FOND1).givingReadWriteDeleteAccess());
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER4).isTrue();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_FOND1_1).isTrue();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_CATEGORY1).isTrue();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER1).isTrue();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER2).isFalse();

		//Bob has a delete permission on folder 4
		modify(authorizationOnRecord(auth1, FOLDER4).removingItOnRecord());
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER4).isFalse();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_FOND1_1).isTrue();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_CATEGORY1).isTrue();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER1).isTrue();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER2).isFalse();

		//Bob has a delete permission on category 1
		modify(authorizationOnRecord(auth1, TAXO1_CATEGORY1).removingItOnRecord());
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER4).isFalse();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_FOND1_1).isFalse();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_CATEGORY1).isFalse();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER1).isFalse();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER2).isFalse();

	}

	@Test
	public void givenUserWithDeletePermissionOnPrincipalConceptButNotOnSomeRecordsThenCanOnlyDeleteConceptIfExcludingRecords()
			throws Exception {

		//Bob has no delete permission
		forUser(bob).assertHasDeletePermissionOnPrincipalConceptExcludingRecords(TAXO1_CATEGORY2).isFalse();
		forUser(bob).assertHasDeletePermissionOnPrincipalConceptIncludingRecords(TAXO1_CATEGORY2).isFalse();

		//Bob has a delete permission the whole category2 hierarchy
		auth1 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadWriteDeleteAccess());
		forUser(bob).assertHasDeletePermissionOnPrincipalConceptExcludingRecords(TAXO1_CATEGORY2).isTrue();
		forUser(bob).assertHasDeletePermissionOnPrincipalConceptIncludingRecords(TAXO1_CATEGORY2).isTrue();

		//Bob has a delete permission on category2, but not the whole hierarchy
		modify(authorizationOnRecord(auth1, FOLDER4).removingItOnRecord());
		forUser(bob).assertHasDeletePermissionOnPrincipalConceptExcludingRecords(TAXO1_CATEGORY2).isTrue();
		forUser(bob).assertHasDeletePermissionOnPrincipalConceptIncludingRecords(TAXO1_CATEGORY2).isFalse();

		try {
			forUser(bob).assertHasDeletePermissionOnPrincipalConceptExcludingRecords(FOLDER1);
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.RecordIsNotAConceptOfPrincipalTaxonomy e) {
			//OK
		}

		try {
			forUser(bob).assertHasDeletePermissionOnPrincipalConceptExcludingRecords(TAXO2_STATION2);
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.RecordIsNotAConceptOfPrincipalTaxonomy e) {
			//OK
		}

	}

	@Test
	public void whenGetConceptsForWhichUserHasPermissionThenReturnTheGoodConcepts()
			throws Exception {

		recordServices.update(users.dakotaIn(zeCollection).setUserRoles(asList(ROLE3)));
		auth1 = add(authorizationForUser(sasquatch).on(TAXO1_CATEGORY1).giving(ROLE1));
		auth2 = add(authorizationForUser(sasquatch).on(TAXO1_CATEGORY2).giving(ROLE1));
		auth3 = add(authorizationForUser(sasquatch).on(TAXO1_FOND1_1).giving(ROLE2));

		forUser(dakota).assertThatConceptsForWhichUserHas(PERMISSION_OF_NO_ROLE).isEmpty();
		forUser(dakota).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE1).isEmpty();
		forUser(dakota).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE1_AND_ROLE2).isEmpty();
		forUser(dakota).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE2).isEmpty();
		forUser(dakota).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE3).containsOnly(
				TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1);

		forUser(sasquatch).assertThatConceptsForWhichUserHas(PERMISSION_OF_NO_ROLE).isEmpty();
		forUser(sasquatch).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE1).containsOnly(
				TAXO1_CATEGORY1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1);
		forUser(sasquatch).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE1_AND_ROLE2).containsOnly(
				TAXO1_CATEGORY1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, TAXO1_FOND1_1);
		forUser(sasquatch).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE2).containsOnly(
				TAXO1_FOND1_1, TAXO1_CATEGORY1);
		forUser(sasquatch).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE3).isEmpty();

		for (String permission : asList(PERMISSION_OF_ROLE1, PERMISSION_OF_ROLE1_AND_ROLE2, PERMISSION_OF_ROLE2,
				PERMISSION_OF_ROLE3)) {
			forUserInAnotherCollection(sasquatch).assertThatConceptsForWhichUserHas(permission).isEmpty();
		}

	}

	@Test
	public void whenGetUsersWithGlobalPermissionThenReturnTheGoodUsers()
			throws Exception {

		Transaction transaction = new Transaction();
		transaction.add(users.sasquatchIn(zeCollection).setUserRoles(asList(ROLE1)));
		transaction.add(users.robinIn(zeCollection).setUserRoles(asList(ROLE2)));
		recordServices.execute(transaction);
		auth1 = add(authorizationForUser(robin).on(TAXO1_FOND1).giving(ROLE3));

		assertThatUsersWithGlobalPermissionInZeCollection(PERMISSION_OF_NO_ROLE).isEmpty();
		assertThatUsersWithGlobalPermissionInZeCollection(PERMISSION_OF_ROLE1).containsOnly(sasquatch);
		assertThatUsersWithGlobalPermissionInZeCollection(PERMISSION_OF_ROLE1_AND_ROLE2).containsOnly(sasquatch, robin);
		assertThatUsersWithGlobalPermissionInZeCollection(PERMISSION_OF_ROLE2).containsOnly(robin);
		assertThatUsersWithGlobalPermissionInZeCollection(PERMISSION_OF_ROLE3).isEmpty();
	}

	@Test
	public void whenGetUsersWithPermissionOnConceptThenReturnTheGoodUsers()
			throws Exception {

		recordServices.update(users.aliceIn(zeCollection).setUserRoles(asList(ROLE3)));
		auth1 = add(authorizationForUser(sasquatch).on(TAXO1_CATEGORY2).giving(ROLE2));
		auth2 = add(authorizationForUser(robin).on(TAXO1_FOND1).giving(ROLE1));
		auth3 = add(authorizationForUser(gandalf).on(FOLDER1).giving(ROLE1));

		for (RecordVerifier verifyRecord : $(FOLDER1, FOLDER1_DOC1)) {
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_NO_ROLE).isEmpty();
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE1).containsOnly(robin, gandalf);
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE1_AND_ROLE2).containsOnly(robin, gandalf);
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE3).containsOnly(alice);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER2_2_DOC1)) {
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_NO_ROLE).isEmpty();
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE1).containsOnly(robin);
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE1_AND_ROLE2).containsOnly(robin);
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE2).isEmpty();
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE3).containsOnly(alice);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER3_DOC1, FOLDER4_1)) {
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_NO_ROLE).isEmpty();
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE1).containsOnly(robin);
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE1_AND_ROLE2).containsOnly(robin, sasquatch);
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE2).containsOnly(sasquatch);
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE3).containsOnly(alice);
		}

	}

	@Test
	public void whenGetUsersWithPermissionOnConceptExcludingInheritedAuthorizationsThenReturnTheGoodUsers()
			throws Exception {

		recordServices.update(users.aliceIn(zeCollection).setUserRoles(asList(ROLE3)));
		auth1 = add(authorizationForUser(sasquatch).on(TAXO1_CATEGORY2).giving(ROLE2));
		auth2 = add(authorizationForUser(robin).on(TAXO1_FOND1).giving(ROLE1));
		auth3 = add(authorizationForUser(gandalf).on(FOLDER1).giving(ROLE1));

		for (RecordVerifier verifyRecord : $(TAXO1_FOND1)) {
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_NO_ROLE).isEmpty();
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE1)
					.containsOnly(robin);
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE1_AND_ROLE2)
					.containsOnly(robin);
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE2).isEmpty();
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE3).isEmpty();
		}

		for (RecordVerifier verifyRecord : $(FOLDER1)) {
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_NO_ROLE).isEmpty();
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE1)
					.containsOnly(gandalf);
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE1_AND_ROLE2)
					.containsOnly(gandalf);
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE2).isEmpty();
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE3).isEmpty();
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2)) {
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_NO_ROLE).isEmpty();
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE1).isEmpty();
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE1_AND_ROLE2)
					.containsOnly(sasquatch);
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE2)
					.containsOnly(sasquatch);
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE3).isEmpty();
		}

		for (RecordVerifier verifyRecord : $(FOLDER2, FOLDER1_DOC1, TAXO1_FOND1_1, TAXO1_CATEGORY1, TAXO1_CATEGORY2_1,
				FOLDER2_2_DOC1)) {
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_NO_ROLE).isEmpty();
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE1).isEmpty();
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE1_AND_ROLE2)
					.isEmpty();
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE3).isEmpty();
		}

	}

	@Test
	public void givenUserHasAuthorizationsWithRolesThenHasValidPermissionsOnRecords()
			throws Exception {

		roleManager.addRole(new Role(zeCollection, "roleA", "First role", asList("operation1", "operation2")));
		roleManager.addRole(new Role(zeCollection, "roleB", "Second role", asList("operation3", "operation4")));
		roleManager.addRole(new Role(zeCollection, "roleC", "Third role", asList("operation5", "operation6")));

		recordServices.update(users.sasquatchIn(zeCollection).setUserRoles(asList("roleA")));

		auth1 = add(authorizationForUser(sasquatch).on(TAXO1_CATEGORY1).giving("roleB"));

		Record folder1Inside = records.folder1();
		Record folder2Inside = records.folder2();
		Record folder3Outside = records.folder3();
		Record folder4Outside = records.folder4();

		User sasquatchInZeCollection = users.sasquatchIn(zeCollection);
		assertThat(sasquatchInZeCollection.has("operation1").onSomething()).isTrue();
		assertThat(sasquatchInZeCollection.has("operation3").onSomething()).isTrue();
		assertThat(sasquatchInZeCollection.has("operation5").onSomething()).isFalse();

		assertThat(sasquatchInZeCollection.has("operation1").on(folder1Inside)).isTrue();
		assertThat(sasquatchInZeCollection.has("operation1").on(folder3Outside)).isTrue();
		assertThat(sasquatchInZeCollection.has("operation3").on(folder1Inside)).isTrue();
		assertThat(sasquatchInZeCollection.has("operation3").on(folder3Outside)).isFalse();
		assertThat(sasquatchInZeCollection.has("operation5").on(folder1Inside)).isFalse();
		assertThat(sasquatchInZeCollection.has("operation5").on(folder3Outside)).isFalse();

		assertThat(sasquatchInZeCollection.has("operation1").onAll(folder1Inside, folder3Outside)).isTrue();
		assertThat(sasquatchInZeCollection.has("operation3").onAll(folder1Inside, folder2Inside)).isTrue();
		assertThat(sasquatchInZeCollection.has("operation3").onAll(folder1Inside, folder3Outside)).isFalse();
		assertThat(sasquatchInZeCollection.has("operation5").onAll(folder1Inside, folder2Inside)).isFalse();

		assertThat(sasquatchInZeCollection.has("operation1").onAny(folder1Inside, folder3Outside)).isTrue();
		assertThat(sasquatchInZeCollection.has("operation3").onAny(folder1Inside, folder2Inside)).isTrue();
		assertThat(sasquatchInZeCollection.has("operation3").onAny(folder1Inside, folder3Outside)).isTrue();
		assertThat(sasquatchInZeCollection.has("operation3").onAny(folder3Outside, folder4Outside)).isFalse();
		assertThat(sasquatchInZeCollection.has("operation5").onAny(folder1Inside, folder3Outside)).isFalse();

		assertThat(sasquatchInZeCollection.hasAny("operation1", "operation3").on(folder1Inside)).isTrue();
		assertThat(sasquatchInZeCollection.hasAny("operation1", "operation3").on(folder3Outside)).isTrue();
		assertThat(sasquatchInZeCollection.hasAny("operation3", "operation4").on(folder3Outside)).isFalse();
		assertThat(sasquatchInZeCollection.hasAny("operation3", "operation4").onAny(folder1Inside, folder3Outside)).isTrue();
		assertThat(sasquatchInZeCollection.hasAny("operation1", "operation3").onAll(folder1Inside, folder3Outside)).isTrue();
		assertThat(sasquatchInZeCollection.hasAny("operation3", "operation4").onAll(folder1Inside, folder3Outside)).isFalse();
		assertThat(sasquatchInZeCollection.hasAny("operation5", "operation6").onAny(folder1Inside, folder3Outside)).isFalse();

		assertThat(sasquatchInZeCollection.hasAll("operation1", "operation3").on(folder1Inside)).isTrue();
		assertThat(sasquatchInZeCollection.hasAll("operation1", "operation2").on(folder3Outside)).isTrue();
		assertThat(sasquatchInZeCollection.hasAll("operation1", "operation3").on(folder3Outside)).isFalse();
		assertThat(sasquatchInZeCollection.hasAll("operation3", "operation4").on(folder3Outside)).isFalse();
		assertThat(sasquatchInZeCollection.hasAll("operation3", "operation4").onAny(folder1Inside, folder3Outside)).isTrue();
		assertThat(sasquatchInZeCollection.hasAll("operation3", "operation6").onAny(folder1Inside, folder3Outside)).isFalse();
		assertThat(sasquatchInZeCollection.hasAll("operation3", "operation4").onAll(folder1Inside, folder2Inside)).isTrue();
		assertThat(sasquatchInZeCollection.hasAll("operation1", "operation4").onAll(folder1Inside, folder2Inside)).isTrue();
		assertThat(sasquatchInZeCollection.hasAll("operation3", "operation4").onAll(folder1Inside, folder3Outside)).isFalse();
		assertThat(sasquatchInZeCollection.hasAll("operation1", "operation4").onAll(folder1Inside, folder3Outside)).isFalse();
		assertThat(sasquatchInZeCollection.hasAll("operation1", "operation2").onAll(folder1Inside, folder3Outside)).isTrue();

		assertThat(users.sasquatchIn(anotherCollection).hasAny("operation1", "operation2", "operation3", "operation4",
				"operation5", "operation6", "otherPermission").globally()).isFalse();

		assertThat(userServices.has(sasquatch).globalPermissionInAnyCollection("operation1")).isTrue();
		assertThat(userServices.has(sasquatch).globalPermissionInAnyCollection("operation3")).isFalse();

	}

	@Test
	public void whenModifyingMultipleFieldsAtOnceOnAnAuthorizationOfARecordThenAllApplied()
			throws Exception {

		givenTimeIs(date(2012, 10, 1));
		auth1 = add(authorizationForUser(sasquatch).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(sasquatch));
		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(sasquatch, chuck);

		modify(modifyAuthorizationOnRecord(auth1, records.taxo1_category2())
				.withNewStartDate(date(2012, 10, 2))
				.withNewPrincipalIds(users.bobIn(zeCollection).getId())
		);

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(bob).startingOn(date(2012, 10, 2)));
		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(chuck);

		givenTimeIs(date(2012, 10, 2));
		services.refreshActivationForAllAuths(collectionsListManager.getCollections());
		waitForBatchProcess();

		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(bob, chuck);

	}

	@Test
	public void whenModifyingMultipleFieldsAtOnceOnAnAuthorizationInheritedByARecordThenAllApplied()
			throws Exception {

		givenTimeIs(date(2012, 10, 1));
		auth1 = add(authorizationForUser(sasquatch).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(sasquatch));
		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(sasquatch, chuck);
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(sasquatch, chuck);

		modify(modifyAuthorizationOnRecord(auth1, records.folder4())
				.withNewStartDate(date(2012, 10, 2))
				.withNewPrincipalIds(users.bobIn(zeCollection).getId())
		);

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).removedOnRecords(FOLDER4).givingReadWrite().forPrincipals(sasquatch),
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(bob).startingOn(date(2012, 10, 2)));
		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(chuck, sasquatch);
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(chuck);

		givenTimeIs(date(2012, 10, 2));
		services.refreshActivationForAllAuths(collectionsListManager.getCollections());
		waitForBatchProcess();

		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(chuck, sasquatch);
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(chuck, bob);
	}

	@Test
	public void whenDeleteAuthorizationThenDeletedFromEveryRecords()
			throws Exception {

		detach(FOLDER4);
		auth1 = addWithoutUser(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = addWithoutUser(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = addWithoutUser(authorizationForUser(charles).on(FOLDER4).givingReadWriteAccess());

		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(bob, charles, chuck);

		services.execute(authorizationDeleteRequest(auth2, zeCollection)
				.setReattachIfLastAuthDeleted(false)
				.setExecutedBy(users.chuckNorrisIn(zeCollection)));

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(alice),
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(charles));
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isTrue();
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(charles, chuck);

		services.execute(authorizationDeleteRequest(auth3, zeCollection)
				.setReattachIfLastAuthDeleted(false)
				.setExecutedBy(users.chuckNorrisIn(zeCollection)));

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(alice));
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isTrue();
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(chuck);

		services.execute(authorizationDeleteRequest(auth1, zeCollection)
				.setReattachIfLastAuthDeleted(false)
				.setExecutedBy(users.chuckNorrisIn(zeCollection)));

		assertThatAllAuthorizations().isEmpty();
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isTrue();
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(chuck);

		recordServices.flush();

		assertThatRecords(schemas.searchEvents(ALL)).extractingMetadatas(RECORD_ID, PERMISSION_USERS, TYPE, USERNAME)
				.containsOnly(
						tuple("folder4", "Bob 'Elvis' Gratton", "delete_permission_folder", "chuck"),
						tuple("taxo1_category2", "Alice Wonderland", "delete_permission_category", "chuck"),
						tuple("folder4", "Charles-François Xavier", "delete_permission_folder", "chuck")
				);
	}

	@Test
	public void whenDeleteLastAuthorizationOfAttachedRecordThenNoAuthorization()
			throws Exception {

		auth1 = addWithoutUser(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = addWithoutUser(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = addWithoutUser(authorizationForUser(charles).on(FOLDER4).givingReadWriteAccess());

		modify(authorizationOnRecord(auth1, FOLDER4).removingItOnRecord());

		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(bob, charles, chuck);

		services.execute(authorizationDeleteRequest(auth2, zeCollection)
				.setReattachIfLastAuthDeleted(true)
				.setExecutedBy(users.chuckNorrisIn(zeCollection)));

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).removedOnRecords(FOLDER4).givingReadWrite().forPrincipals(alice),
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(charles));
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isFalse();
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(charles, chuck);

		services.execute(authorizationDeleteRequest(auth3, zeCollection)
				.setReattachIfLastAuthDeleted(true)
				.setExecutedBy(users.chuckNorrisIn(zeCollection)));

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).removedOnRecords(FOLDER4).givingReadWrite().forPrincipals(alice));
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isFalse();
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(chuck);

		services.execute(authorizationDeleteRequest(auth1, zeCollection)
				.setReattachIfLastAuthDeleted(true)
				.setExecutedBy(users.chuckNorrisIn(zeCollection)));

		assertThatAllAuthorizations().isEmpty();
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isFalse();
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(chuck);

		recordServices.flush();

		assertThatRecords(schemas.searchEvents(ALL)).extractingMetadatas(RECORD_ID, PERMISSION_USERS, TYPE, USERNAME)
				.containsOnly(
						tuple("folder4", "Bob 'Elvis' Gratton", "delete_permission_folder", "chuck"),
						tuple("taxo1_category2", "Alice Wonderland", "delete_permission_category", "chuck"),
						tuple("folder4", "Charles-François Xavier", "delete_permission_folder", "chuck")
				);
	}

	@Test
	public void whenDeleteLastAuthorizationOfDetachedRecordThenReattachDependingOnOption()
			throws Exception {

		detach(FOLDER4);
		auth1 = addWithoutUser(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = addWithoutUser(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = addWithoutUser(authorizationForUser(charles).on(FOLDER4).givingReadWriteAccess());

		assertThatRecords(schemas.searchEvents(ALL)).isEmpty();
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(bob, charles, chuck);

		services.execute(authorizationDeleteRequest(auth2, zeCollection).setReattachIfLastAuthDeleted(true));

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(alice),
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(charles));
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isTrue();
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(charles, chuck);

		services.execute(authorizationDeleteRequest(auth3, zeCollection).setReattachIfLastAuthDeleted(true));

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(alice));
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isFalse();
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(alice, chuck);

		services.execute(authorizationDeleteRequest(auth1, zeCollection).setReattachIfLastAuthDeleted(true));

		assertThatAllAuthorizations().isEmpty();
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isFalse();
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(chuck);

		recordServices.flush();

		assertThatRecords(schemas.searchEvents(ALL)).isEmpty();
	}

	@Test
	public void whenCreatingAndModifyingAuthWithoutUserThenNoEventCreated()
			throws Exception {

		auth1 = addWithoutUser(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = addWithoutUser(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = addWithoutUser(authorizationForUser(charles).on(FOLDER4).givingReadWriteAccess());

		modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2).withNewPrincipalIds(users.dakotaIn(zeCollection).getId()));
		modify(authorizationOnRecord(auth1, FOLDER4).withNewPrincipalIds(users.edouardLechatIn(zeCollection).getId()));

		assertThatRecords(schemas.searchEvents(ALL)).isEmpty();
	}

	@Test
	public void whenCreatingAndModifyingAuthWithUserThenNoEventCreated()
			throws Exception {

		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER4).givingReadWriteAccess());

		modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2).withNewPrincipalIds(users.dakotaIn(zeCollection).getId())
				.setExecutedBy(users.gandalfIn(zeCollection)));
		modify(authorizationOnRecord(auth1, FOLDER4).withNewPrincipalIds(users.edouardLechatIn(zeCollection).getId())
				.setExecutedBy(users.gandalfIn(zeCollection)));
		modify(authorizationOnRecord(auth2, FOLDER4).withNewPrincipalIds(users.edouardLechatIn(zeCollection).getId())
				.setExecutedBy(users.gandalfIn(zeCollection)));

		assertThatRecords(schemas.searchEvents(ALL)).extractingMetadatas(RECORD_ID, PERMISSION_USERS, TYPE, USERNAME)
				.containsOnly(
						tuple("folder4", "Bob 'Elvis' Gratton", "grant_permission_folder", "dakota"),
						tuple("taxo1_category2", "Alice Wonderland", "grant_permission_category", "dakota"),
						tuple("folder4", "Charles-François Xavier", "grant_permission_folder", "dakota"),

						tuple("folder4", "Dakota L'Indien", "modify_permission_folder", "gandalf"),
						tuple("folder4", "Bob 'Elvis' Gratton", "modify_permission_folder", "gandalf"),
						tuple("taxo1_category2", "Alice Wonderland", "modify_permission_category", "gandalf")

				);

		Event event = schemas.searchEvents(where(schemas.eventType()).isEqualTo("modify_permission_category")).get(0);
		assertThat(event.getDelta().replace("\n", "")).isEqualTo("Utilisateurs :-[Dakota L'Indien]+[Alice Wonderland]");
	}

	@Test
	public void whenAConceptIsAssignedToANewParentThenTokensUpdated()
			throws Exception {

		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY1).givingReadWriteAccess());
		auth2 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth3 = add(authorizationForUser(charles).on(TAXO1_CATEGORY2_1).givingReadWriteAccess());

		verifyRecord(TAXO1_CATEGORY1).usersWithReadAccess().containsOnly(alice, chuck);
		verifyRecord(FOLDER2).usersWithReadAccess().containsOnly(alice, chuck);
		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(bob, chuck);
		verifyRecord(TAXO1_CATEGORY2_1).usersWithReadAccess().containsOnly(bob, charles, chuck);
		verifyRecord(FOLDER3).usersWithReadAccess().containsOnly(bob, charles, chuck);

		recordServices.update(records.taxo1_category2_1().set(setup.category.parentOfCategory(), TAXO1_CATEGORY1));
		verifyRecord(TAXO1_CATEGORY1).usersWithReadAccess().containsOnly(alice, chuck);
		verifyRecord(FOLDER2).usersWithReadAccess().containsOnly(alice, chuck);
		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(bob, chuck);
		verifyRecord(TAXO1_CATEGORY2_1).usersWithReadAccess().containsOnly(alice, charles, chuck);
		verifyRecord(FOLDER3).usersWithReadAccess().containsOnly(alice, charles, chuck);

		recordServices.update(records.taxo1_category2_1().set(setup.category.parentOfCategory(), null));
		verifyRecord(TAXO1_CATEGORY1).usersWithReadAccess().containsOnly(alice, chuck);
		verifyRecord(FOLDER2).usersWithReadAccess().containsOnly(alice, chuck);
		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(bob, chuck);
		verifyRecord(TAXO1_CATEGORY2_1).usersWithReadAccess().containsOnly(charles, chuck);
		verifyRecord(FOLDER3).usersWithReadAccess().containsOnly(charles, chuck);
	}

	@Test
	public void whenARecordIsAssignedToANewConceptThenTokensUpdated()
			throws Exception {

		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY1).givingReadWriteAccess());
		auth2 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER3).givingReadWriteAccess());
		auth4 = add(authorizationForUser(dakota).on(FOLDER4).givingReadWriteAccess());

		verifyRecord(TAXO1_CATEGORY1).usersWithReadAccess().containsOnly(alice, chuck);
		verifyRecord(FOLDER2).usersWithReadAccess().containsOnly(alice, chuck);
		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(bob, chuck);
		verifyRecord(FOLDER3).usersWithReadAccess().containsOnly(bob, charles, chuck);
		verifyRecord(FOLDER3_DOC1).usersWithReadAccess().containsOnly(bob, charles, chuck);

		recordServices.update(records.folder3().set(setup.folderSchema.taxonomy1(), TAXO1_CATEGORY1));
		verifyRecord(TAXO1_CATEGORY1).usersWithReadAccess().containsOnly(alice, chuck);
		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(bob, chuck);
		verifyRecord(FOLDER3).usersWithReadAccess().containsOnly(alice, charles, chuck);
		verifyRecord(FOLDER3_DOC1).usersWithReadAccess().containsOnly(alice, charles, chuck);

		recordServices.update(records.folder3()
				.set(setup.folderSchema.taxonomy1(), null)
				.set(setup.folderSchema.parent(), FOLDER4));
		verifyRecord(TAXO1_CATEGORY1).usersWithReadAccess().containsOnly(alice, chuck);
		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(bob, chuck);
		verifyRecord(FOLDER3).usersWithReadAccess().containsOnly(bob, charles, dakota, chuck);
		verifyRecord(FOLDER3_DOC1).usersWithReadAccess().containsOnly(bob, charles, dakota, chuck);
	}

	//@Test
	@SlowTest
	public void givenAGroupHasALotOfUsersThenBAtchProcessUsedWhenGivingAuth()
			throws Exception {

		createDummyUsersInLegendsGroup(1000);
		getModelLayerFactory().getBatchProcessesController().close();

		try {
			auth1 = services.add(authorizationForGroup(legends).on(TAXO1_CATEGORY1).givingReadWriteAccess());
			verifyRecord(TAXO1_CATEGORY1).usersWithReadAccess().hasSize(1);
		} finally {
			getModelLayerFactory().getBatchProcessesController().initialize();
		}
		waitForBatchProcess();
		verifyRecord(TAXO1_CATEGORY1).usersWithReadAccess().hasSize(1005);

		getModelLayerFactory().getBatchProcessesController().close();
		try {
			services.execute(authorizationDeleteRequest(auth1, zeCollection));
			verifyRecord(TAXO1_CATEGORY1).usersWithReadAccess().hasSize(1);
		} finally {
			getModelLayerFactory().getBatchProcessesController().initialize();
		}
		waitForBatchProcess();
		verifyRecord(TAXO1_CATEGORY1).usersWithReadAccess().hasSize(1);
	}

	@Test
	public void givenAuthorizationOnRecordWhenPhysicallyDeletingTheRecordThenAuthorizationDeleted()
			throws Exception {

		checkIfChuckNorrisHasAccessToEverythingInZeCollection = false;
		auth1 = add(authorizationForUser(bob).on(FOLDER1).givingReadAccess());
		auth2 = add(authorizationForGroup(heroes).on(FOLDER2_1).givingReadAccess());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(FOLDER1).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER2_1).givingRead().forPrincipals(heroes)
		);

		givenRecordIsLogicallyThenPhysicallyDeleted(FOLDER1);
		givenRecordIsLogicallyThenPhysicallyDeleted(FOLDER2);

		assertThatAllAuthorizations().isEmpty();

	}

	@Test
	public void whenRecordIsDetachedThenOnlyInheritedAuthsAreDetached()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_FOND1).givingReadAccess());
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_FOND1).givingReadAccess());

		assertThatAuthorizationsOn(FOLDER4).containsOnly(
				authOnRecord(TAXO1_FOND1).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_FOND1).givingRead().forPrincipals(heroes)
		).hasSize(2);

		assertThatAuthorizationsOn(FOLDER4_1).containsOnly(
				authOnRecord(TAXO1_FOND1).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_FOND1).givingRead().forPrincipals(heroes)
		).hasSize(2);

		assertThatAuthorizationsOn(FOLDER4_1_DOC1).containsOnly(
				authOnRecord(TAXO1_FOND1).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_FOND1).givingRead().forPrincipals(heroes)
		).hasSize(2);

		detach(FOLDER4);

		assertThatAuthorizationsOn(FOLDER4).containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingRead().forPrincipals(heroes)
		).hasSize(2);

		assertThatAuthorizationsOn(FOLDER4_1).containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingRead().forPrincipals(heroes)
		).hasSize(2);

		assertThatAuthorizationsOn(FOLDER4_1_DOC1).containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingRead().forPrincipals(heroes)
		).hasSize(2);

		detach(FOLDER4_1);

		assertThatAuthorizationsOn(FOLDER4).containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingRead().forPrincipals(heroes)
		).hasSize(2);

		assertThatAuthorizationsOn(FOLDER4_1).containsOnly(
				authOnRecord(FOLDER4_1).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4_1).givingRead().forPrincipals(heroes)
		).hasSize(2);

		assertThatAuthorizationsOn(FOLDER4_1_DOC1).containsOnly(
				authOnRecord(FOLDER4_1).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4_1).givingRead().forPrincipals(heroes)
		).hasSize(2);

	}

}
