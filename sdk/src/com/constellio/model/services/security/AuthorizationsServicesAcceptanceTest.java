package com.constellio.model.services.security;

import static com.constellio.model.entities.security.CustomizedAuthorizationsBehavior.KEEP_ATTACHED;
import static com.constellio.model.entities.security.Role.DELETE;
import static com.constellio.model.entities.security.Role.READ;
import static com.constellio.model.entities.security.Role.WRITE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER1_DOC1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER2;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER2_2_DOC1;
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
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.InvalidPrincipalsIds;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.InvalidTargetRecordsIds;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithId;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithIdOnRecord;
import com.constellio.model.services.security.roles.RolesManagerRuntimeException;

public class AuthorizationsServicesAcceptanceTest extends BaseAuthorizationsServicesAcceptanceTest {

	@After
	public void checkIfARecordHasAnInvalidAuthorization() {
		ensureNoRecordsHaveAnInvalidAuthorization();
	}

	@After
	public void checkIfChuckNorrisHasAccessToEverythingInZeCollection()
			throws Exception {

		List<String> foldersWithReadFound = findAllFoldersAndDocuments(users.chuckNorrisIn(zeCollection));
		List<String> foldersWithWriteFound = findAllFoldersAndDocumentsWithWritePermission(
				users.chuckNorrisIn(zeCollection));
		List<String> foldersWithDeleteFound = findAllFoldersAndDocumentsWithDeletePermission(
				users.chuckNorrisIn(zeCollection));

		assertThat(foldersWithReadFound).containsOnly(records.allFoldersAndDocumentsIds().toArray(new String[0]));
		assertThat(foldersWithWriteFound).containsOnly(records.allFoldersAndDocumentsIds().toArray(new String[0]));
		assertThat(foldersWithDeleteFound).containsOnly(records.allFoldersAndDocumentsIds().toArray(new String[0]));
	}

	@After
	public void checkIfAliceSeeAndCanModifyEverythingInCollection2()
			throws Exception {
		List<String> foldersWithReadFound = findAllFoldersAndDocuments(users.aliceIn(anotherCollection));
		List<String> foldersWithWriteFound = findAllFoldersAndDocumentsWithWritePermission(users.aliceIn(anotherCollection));
		List<String> foldersWithDeleteFound = findAllFoldersAndDocumentsWithDeletePermission(
				users.aliceIn(anotherCollection));

		assertThat(foldersWithReadFound).containsOnly(otherCollectionRecords.allFoldersAndDocumentsIds().toArray(new String[0]));
		assertThat(foldersWithWriteFound).containsOnly(otherCollectionRecords.allFoldersAndDocumentsIds().toArray(new String[0]));
		assertThat(foldersWithDeleteFound).hasSize(0);
	}

	@After
	public void checkIfBobSeeAndCanDeleteEverythingInCollection2()
			throws Exception {
		List<String> foldersWithReadFound = findAllFoldersAndDocuments(users.bobIn(anotherCollection));
		List<String> foldersWithWriteFound = findAllFoldersAndDocumentsWithWritePermission(users.bobIn(anotherCollection));
		List<String> foldersWithDeleteFound = findAllFoldersAndDocumentsWithDeletePermission(users.bobIn(anotherCollection));

		assertThat(foldersWithReadFound).containsOnly(otherCollectionRecords.allFoldersAndDocumentsIds().toArray(new String[0]));
		assertThat(foldersWithWriteFound).hasSize(0);
		assertThat(foldersWithDeleteFound)
				.containsOnly(otherCollectionRecords.allFoldersAndDocumentsIds().toArray(new String[0]));
	}

	@After
	public void checkIfDakotaSeeAndCanDeleteEverythingInCollection2()
			throws Exception {
		List<String> foldersWithReadFound = findAllFoldersAndDocuments(users.dakotaIn(anotherCollection));
		List<String> foldersWithWriteFound = findAllFoldersAndDocumentsWithWritePermission(users.dakotaIn(anotherCollection));
		List<String> foldersWithDeleteFound = findAllFoldersAndDocumentsWithDeletePermission(
				users.dakotaIn(anotherCollection));

		assertThat(foldersWithReadFound).containsOnly(otherCollectionRecords.allFoldersAndDocumentsIds().toArray(new String[0]));
		assertThat(foldersWithWriteFound).hasSize(0);
		assertThat(foldersWithDeleteFound).hasSize(0);
	}

	// TODO @Test Fix event logging
	public void givenBobHasReadAccessToCategory1ThenBobSeesFolder1AndFolder2()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		long eventsCount = fetchEventCount();
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.taxo1_category1().getId()));
		assertThat(fetchEventCount()).isEqualTo(eventsCount + 1);
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder1().getId(), records.folder2().getId(), records.folder2_1().getId(),
				records.folder2_2().getId(), records.folder1_doc1().getId(), records.folder2_2_doc1().getId(),
				records.folder2_2_doc2().getId());
	}

	//Notes :
	//TODO TestgetUsersWithPermission

	@Test
	//Case 1
	public void givenRoleAuthorizationsOnPrincipalConceptsThenInheritedInHierarchy()
			throws Exception {

		//Replacing
		// givenBobHasReadRoleOnCategory1WhenGettingUsersWithReadRoleOnRecordThenBobReturned
		// givenLegendsHaveReadRoleOnCategory1WhenGettingUsersWithReadRoleOnRecordThenAliceAndEdouardReturned
		givenTaxonomy1IsThePrincipalAndSomeRecords();

		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_CATEGORY2).giving(ROLE1));
		auth2 = addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).giving(ROLE1));
		auth3 = addKeepingAttached(authorizationForUser(alice).on(TAXO1_CATEGORY2_1).giving(ROLE1));

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRoles(ROLE1).forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingRoles(ROLE1).forPrincipals(heroes),
				authOnRecord(TAXO1_CATEGORY2_1).givingRoles(ROLE1).forPrincipals(alice)
		);

		//TODO Bug! Robin should have ROLE1
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
		assertThatBatchProcessDuringTest().hasSize(7);
	}

	//TODO Support this usecase @Test
	//Case 2
	public void givenRolesOfAuthorizationAreModifiedOnSameRecordOfAuthorizationThenNotDuplicatedAndInstantaneousEffectOnSecurity()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_CATEGORY2).giving(ROLE1));
		auth2 = addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).giving(ROLE1));

		assertThat(modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2).withNewAccessAndRoles(ROLE2)))
				.isNot(creatingACopy()).isNot(deleted());
		assertThat(modify(authorizationOnRecord(auth2, TAXO1_CATEGORY2).withNewAccessAndRoles(ROLE1, ROLE3)))
				.isNot(creatingACopy()).isNot(deleted());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRoles(ROLE2).forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingRoles(ROLE1, ROLE3).forPrincipals(heroes)
		);

		//TODO Bug! Robin should have ROLE1 and ROLE3
		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER3_DOC1, FOLDER4_1_DOC1, FOLDER4_2)) {
			verifyRecord.usersWithRole(ROLE1).containsOnly(bob, charles, dakota, gandalf);
			verifyRecord.usersWithRole(ROLE2).containsOnly(bob);
			verifyRecord.usersWithRole(ROLE3).containsOnly(bob, charles, dakota, gandalf);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		assertThatBatchProcessDuringTest().hasSize(7);
	}

	@Test
	//Case 4
	public void givenAccessAuthorizationsOnPrincipalConceptsThenInheritedInHierarchy()
			throws Exception {

		//Replacing
		//- givenBobHasReadRoleOnCategory1WhenGettingUsersWithReadRoleOnRecordThenBobReturned
		//- whenAddingAndRemovingAuthorizationToAGroupThenAppliedToAllUsers
		givenTaxonomy1IsThePrincipalAndSomeRecords();

		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth3 = addKeepingAttached(authorizationForUser(alice).on(TAXO1_CATEGORY2_1).givingReadWriteAccess());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(heroes),
				authOnRecord(TAXO1_CATEGORY2_1).givingReadWrite().forPrincipals(alice)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4, FOLDER4_1, FOLDER4_1_DOC1, FOLDER4_2, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, charles, dakota, gandalf, chuck, robin);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, alice, charles, dakota, chuck, robin, gandalf);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		assertThatBatchProcessDuringTest().hasSize(7);
	}

	//TODO Support this usecase @Test
	//Case 5
	public void givenAccessTypesOfAuthorizationAreModifiedOnSameRecordOfAuthorizationThenNotDuplicatedAndInstantaneousEffectOnSecurity()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadAccess());
		auth2 = addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadAccess());

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, TAXO1_CATEGORY2)) {
			verifyRecord.usersWithDeleteAccess().isEmpty();
			verifyRecord.usersWithWriteAccess().isEmpty();
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
			verifyRecord.usersWithWriteAccess().containsOnly(charles, dakota, gandalf, bob, robin);
			verifyRecord.usersWithDeleteAccess().containsOnly(bob);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		assertThatBatchProcessDuringTest().hasSize(7);
	}

	@Test
	//Case 7
	public void givenPrincipalsAreModifiedOnSameRecordOfAuthorizationThenNotDuplicatedAndInstantaneousEffectOnSecurity()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadAccess());

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
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadAccess());

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
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadAccess());
		auth2 = addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadAccess());

		request1 = modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2_1).withNewPrincipalIds(robin).detaching());

		try {
			modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2_1).withNewPrincipalIds(robin).detaching());
			fail("Exception expected");
		} catch (NoSuchAuthorizationWithIdOnRecord e) {
			//OK
		}

		try {
			modify(authorizationOnRecord(auth2, TAXO1_CATEGORY2_1).withNewPrincipalIds(robin).detaching());
			fail("Exception expected");
		} catch (NoSuchAuthorizationWithIdOnRecord e) {
			//OK
		}

		try {
			modify(authorizationOnRecord("invalidAuth", TAXO1_CATEGORY2_1).withNewPrincipalIds(robin).detaching());
			fail("Exception expected");
		} catch (NoSuchAuthorizationWithId e) {
			//OK
		}
	}

	@Test
	public void givenPrincipalsAreModifiedOnRecordOfAuthorizationDetachingThenDuplicatedAndInstantaneousEffectOnSecurity()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadAccess());
		auth2 = addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadAccess());

		request1 = modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2_1).withNewPrincipalIds(robin).detaching());
		String auth1CopyInCategory2_1 = request1.getIdOfAuthorizationCopy(auth1);
		String auth2CopyInCategory2_1 = request1.getIdOfAuthorizationCopy(auth2);
		assertThat(request1).is(creatingACopy()).isNot(deleted());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(heroes),
				authOnRecord(TAXO1_CATEGORY2_1).givingRead().forPrincipals(robin),
				authOnRecord(TAXO1_CATEGORY2_1).givingRead().forPrincipals(heroes)
		);

		request2 = modify(authorizationOnRecord(auth2CopyInCategory2_1, TAXO1_CATEGORY2_1)
				.withNewPrincipalIds(legends, bob).detaching());
		assertThat(request2).isNot(creatingACopy()).isNot(deleted());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(heroes),
				authOnRecord(TAXO1_CATEGORY2_1).givingRead().forPrincipals(robin),
				authOnRecord(TAXO1_CATEGORY2_1).givingRead().forPrincipals(legends, bob)
		);

		assertThatAuth(auth1).hasPrincipals(bob);
		assertThatAuth(auth2).hasPrincipals(heroes);
		assertThatAuth(auth1CopyInCategory2_1).hasPrincipals(robin);
		assertThatAuth(auth2CopyInCategory2_1).hasPrincipals(legends, bob);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(charles, dakota, gandalf, robin, bob, chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(sasquatch, gandalf, edouard, alice, bob, chuck, robin);
			verifyRecord.usersWithWriteAccess().containsOnly(chuck);
			if (verifyRecord.recordId.equals(TAXO1_CATEGORY2_1)) {
				verifyRecord.detachedAuthorizationFlag().isTrue();
			} else {
				verifyRecord.detachedAuthorizationFlag().isFalse();
			}
		}

	}

	@Test
	public void givenGroupAuthorizationsWhenAddOrRemoveUsersInGroupThenInstantaneousEffectOnSecurity()
			throws Exception {

		//Replacing
		//- whenAddingAndRemovingGroupToAUserThenHeReceivesAndLoseGroupAuthorizations
		//- whenAddingAndRemovingUserToAGroupThenHeReceivesAndLoseGroupAuthorizations

		givenTaxonomy1IsThePrincipalAndSomeRecords();

		addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY1).givingReadWriteAccess());
		addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY1).giving(ROLE1));
		addKeepingAttached(authorizationForGroup(heroes).on(FOLDER4).givingReadWriteDeleteAccess());
		addKeepingAttached(authorizationForGroup(heroes).on(FOLDER4).giving(ROLE2));

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER1, FOLDER2, FOLDER2_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(charles, dakota, gandalf, chuck, robin);
			//TODO Bug : Robin expected
			verifyRecord.usersWithRole(ROLE1).containsOnly(charles, dakota, gandalf);
		}

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithDeleteAccess().containsOnly(charles, dakota, gandalf, chuck, robin);

			//TODO Bug : Robin expected
			verifyRecord.usersWithRole(ROLE2).containsOnly(charles, dakota, gandalf);
		}

		assertThatBatchProcessDuringTest().hasSize(12);

		givenUser(charles).isRemovedFromGroup(heroes);
		givenUser(robin).isRemovedFromGroup(sidekicks);
		givenUser(sasquatch).isAddedInGroup(heroes);
		givenUser(edouard).isAddedInGroup(sidekicks);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER1, FOLDER2, FOLDER2_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(sasquatch, dakota, gandalf, chuck, edouard);
			//TODO Bug : Edouard expected
			verifyRecord.usersWithRole(ROLE1).containsOnly(sasquatch, dakota, gandalf);
		}

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithDeleteAccess().containsOnly(sasquatch, dakota, gandalf, chuck, edouard);

			//TODO Bug : Edouard expected
			verifyRecord.usersWithRole(ROLE2).containsOnly(sasquatch, dakota, gandalf);
		}

		assertThatBatchProcessDuringTest().hasSize(0);

	}

	@Test
	public void whenAddingAndRemovingAuthorizationToAGroupThenAppliedToAllUsers()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();

		GlobalGroup group = userServices.createGlobalGroup(
				"vilains", "Vilains", new ArrayList<String>(), null, GlobalGroupStatus.ACTIVE, true);
		userServices.addUpdateGlobalGroup(group);
		userServices.setGlobalGroupUsers("vilains", asList(users.bob()));

		assertThat(users.bobIn(zeCollection))
				.has(noAuthorizationsToRead(records.taxo1_category1(), records.folder1(), records.folder2()));

		List<String> roles = asList(READ);
		Authorization authorization = addAuthorizationWithoutDetaching(roles,
				asList(userServices.getGroupInCollection("vilains", zeCollection).getId()),
				asList(records.taxo1_category1().getId()));
		waitForBatchProcess();

		assertThat(users.bobIn(zeCollection))
				.has(authorizationsToRead(records.taxo1_category1(), records.folder1(), records.folder2()));

		services.delete(authorization.getDetail(), User.GOD);
		waitForBatchProcess();
		assertThat(users.bobIn(zeCollection))
				.has(noAuthorizationsToRead(records.taxo1_category1(), records.folder1(), records.folder2()));

	}

	@Test
	public void givenUserHasCollectionReadThenHasReadOnlyOnAnyRecord()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		User bob = users.bobIn(zeCollection);
		recordServices.update(bob.setCollectionReadAccess(true).getWrappedRecord());

		assertThat(services.canRead(bob, records.folder1())).isTrue();
		assertThat(services.canWrite(bob, records.folder1())).isFalse();
		assertThat(services.canDelete(bob, records.folder1())).isFalse();
	}

	@Test
	public void givenUserHasCollectionWriteThenHasReadAndWriteOnlyOnAnyRecord()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		User bob = users.bobIn(zeCollection);
		recordServices.update(bob.setCollectionWriteAccess(true).getWrappedRecord());

		assertThat(services.canRead(bob, records.folder1())).isTrue();
		assertThat(services.canWrite(bob, records.folder1())).isTrue();
		assertThat(services.canDelete(bob, records.folder1())).isFalse();
	}

	@Test
	public void givenUserHasCollectionDeleteThenHasReadAndDeleteOnAnyRecord()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		User bob = users.bobIn(zeCollection);
		recordServices.update(bob.setCollectionDeleteAccess(true).getWrappedRecord());

		assertThat(services.canRead(bob, records.folder1())).isTrue();
		assertThat(services.canWrite(bob, records.folder1())).isFalse();
		assertThat(services.canDelete(bob, records.folder1())).isTrue();
	}

	@Test
	public void givenUserHasCollectionWriteAndDeleteThenHasAllAuthsOnAnyRecord()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		User bob = users.bobIn(zeCollection);
		recordServices.update(bob.setCollectionWriteAccess(true).setCollectionDeleteAccess(true).getWrappedRecord());

		assertThat(services.canRead(bob, records.folder1())).isTrue();
		assertThat(services.canWrite(bob, records.folder1())).isTrue();
		assertThat(services.canDelete(bob, records.folder1())).isTrue();
	}

	@Test
	public void givenLegendsHaveReadAccessToStation2_1ThenTheySeeFolder2()
			throws Exception {
		givenTaxonomy2IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.legendsIn(zeCollection).getId()),
				asList(records.taxo2_station2_1().getId()));
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder2().getId(), records.folder2_1().getId(), records.folder2_2().getId(),
				records.folder2_2_doc1().getId(), records.folder2_2_doc2().getId());
		foundRecords = findAllFoldersAndDocuments(users.edouardLechatIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder2().getId(), records.folder2_1().getId(), records.folder2_2().getId(),
				records.folder2_2_doc1().getId(), records.folder2_2_doc2().getId());
	}

	@Test
	public void givenHeroesHaveReadAccessToCategory2ThenTheySeeFolder3AndFolder4()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);

		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));

		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder3().getId(), records.folder4().getId(), records.folder4_1().getId(),
				records.folder4_2().getId(), records.folder3_doc1().getId(), records.folder4_1_doc1().getId(),
				records.folder4_2_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.dakotaLIndienIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder3().getId(), records.folder4().getId(), records.folder4_1().getId(),
				records.folder4_2().getId(), records.folder3_doc1().getId(), records.folder4_1_doc1().getId(),
				records.folder4_2_doc1().getId());
	}

	@Test
	public void givenHeroesHaveReadAccessToCategory1WhenModifyingAuthorizationsReplacincingHeroesByBobThenOnlyBobSeesFoldersFromCategory1()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		Authorization authorization = addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category1().getId()));
		waitForBatchProcess();
		authorization.setGrantedToPrincipals(asList(users.bobIn(zeCollection).getId()));
		modifyAuthorizationWithoutDetaching(authorization);

		waitForBatchProcess();

		List<String> foundBobRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		List<String> foundXavierRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		assertThat(foundBobRecords)
				.containsOnly(records.folder1().getId(), records.folder1_doc1().getId(), records.folder2().getId(),
						records.folder2_1().getId(), records.folder2_2().getId(), records.folder2_2_doc1().getId(),
						records.folder2_2_doc2().getId());
		assertThat(foundXavierRecords).isEmpty();
	}

	@Test(expected = AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords.class)
	public void whenAddingAuthorizationWithoutPrincipalsAndTargetRecordsThenValidationException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);

		addAuthorizationWithoutDetaching(roles, new ArrayList<String>(), new ArrayList<String>());
	}

	@Test(expected = AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords.class)
	public void whenAddingAuthorizationWithoutPrincipalsThenValidationException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);

		addAuthorizationWithoutDetaching(roles, new ArrayList<String>(), asList(records.taxo1_category1().getId()));
	}

	@Test(expected = AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords.class)
	public void whenAddingAuthorizationWithoutTargetRecordsThenValidationException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);

		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()), new ArrayList<String>());
	}

	@Test(expected = InvalidPrincipalsIds.class)
	public void whenAddingAuthorizationWithInvalidPrincipalsThenValidationException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);

		addAuthorizationWithoutDetaching(roles, asList("inexistentId"), asList(records.taxo1_category1().getId()));
	}

	@Test(expected = InvalidTargetRecordsIds.class)
	public void whenAddingAuthorizationWithInvalidTargetRecordsThenValidationException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);

		addAuthorizationWithoutDetaching(roles, asList(users.aliceIn(zeCollection).getId()), asList("inexistentId"));
	}

	@Test(expected = AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords.class)
	public void givenAuthorizationWhenModifyingAuthorizationWithoutPrincipalsAndTargetRecordsThenValidationException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		AuthorizationDetails details = AuthorizationDetails.create(aString(), roles, zeCollection);
		Authorization authorization = new Authorization(details, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category1().getId()));
		authorization.setGrantedToPrincipals(new ArrayList<String>());
		authorization.setGrantedOnRecords(new ArrayList<String>());

		modifyAuthorizationWithoutDetaching(authorization);
	}

	@Test(expected = AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords.class)
	public void givenAuthorizationWhenModifyingAuthorizationWithoutTargetRecordsThenValidationException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		AuthorizationDetails details = AuthorizationDetails.create(aString(), roles, zeCollection);
		Authorization authorization = new Authorization(details, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category1().getId()));
		authorization.setGrantedOnRecords(new ArrayList<String>());

		modifyAuthorizationWithoutDetaching(authorization);
	}

	@Test(expected = AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords.class)
	public void givenAuthorizationWhenModifyingAuthorizationWithoutPrincipalsThenValidationException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		AuthorizationDetails details = AuthorizationDetails.create(aString(), roles, zeCollection);
		Authorization authorization = new Authorization(details, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category1().getId()));
		authorization.setGrantedToPrincipals(new ArrayList<String>());

		modifyAuthorizationWithoutDetaching(authorization);
	}

	@Test(expected = InvalidPrincipalsIds.class)
	public void givenAuthorizationWhenModifyingAuthorizationWithInvalidPrincipalsThenValidationException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		AuthorizationDetails details = AuthorizationDetails.create(aString(), roles, zeCollection);
		Authorization authorization = new Authorization(details, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category1().getId()));
		authorization.setGrantedToPrincipals(asList("inexistentId"));

		modifyAuthorizationWithoutDetaching(authorization);
	}

	@Test(expected = InvalidTargetRecordsIds.class)
	public void givenAuthorizationWhenModifyingAuthorizationWithInvalidTargetRecordsThenValidationException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		AuthorizationDetails details = AuthorizationDetails.create(aString(), roles, zeCollection);
		Authorization authorization = new Authorization(details, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category1().getId()));
		authorization.setGrantedOnRecords(asList("inexistentId"));

		modifyAuthorizationWithoutDetaching(authorization);
	}

	@Test
	public void whenHeroesHaveAccessToCategory1And2ThenDakotaAndXavierSeeAllFolders()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category1().getId(), records.taxo1_category2().getId()));
		waitForBatchProcess();

		List<String> foundDakotaRecords = findAllFoldersAndDocuments(users.dakotaLIndienIn(zeCollection));
		List<String> foundXavierRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		assertThat(foundDakotaRecords).containsOnly(records.folder1().getId(), records.folder1_doc1().getId(),
				records.folder2().getId(), records.folder2_1().getId(), records.folder2_2().getId(),
				records.folder2_2_doc1().getId(),
				records.folder2_2_doc2().getId(), records.folder3().getId(), records.folder3_doc1().getId(),
				records.folder4().getId(),
				records.folder4_1().getId(), records.folder4_1_doc1().getId(), records.folder4_2().getId(),
				records.folder4_2_doc1().getId());
		assertThat(foundXavierRecords).isEqualTo(foundDakotaRecords);
	}

	@Test
	public void whenHeroesAndAliceHaveAccessToCategory2_1AndFolder4_2ThenXavierDakotaAndAliceSeeFolder4_2AndFolder3()
			throws Exception {

		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId(), users.aliceIn(zeCollection).getId()),
				asList(records.taxo1_category2_1().getId(), records.folder4_2().getId()));
		waitForBatchProcess();

		List<String> foundDakotaRecords = findAllFoldersAndDocuments(users.dakotaLIndienIn(zeCollection));
		List<String> foundXavierRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		List<String> foundAliceRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		assertThat(foundDakotaRecords).containsOnly(records.folder3().getId(), records.folder3_doc1().getId(),
				records.folder4_2().getId(), records.folder4_2_doc1().getId());
		assertThat(foundDakotaRecords).isEqualTo(foundXavierRecords).isEqualTo(foundAliceRecords);
	}

	@Test
	public void givenHeroesHaveAccessToCategory1WhenModifyingAuhtorizationReplacingCategory1ByFolder4_2ThenXavierAndDakotaOnlySeeFolder4_2()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		Authorization authorization = addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category1().getId()));
		waitForBatchProcess();
		authorization.setGrantedOnRecords(asList(records.folder4_2().getId()));
		modifyAuthorizationWithoutDetaching(authorization);
		waitForBatchProcess();

		List<String> foundDakotaRecords = findAllFoldersAndDocuments(users.dakotaLIndienIn(zeCollection));
		List<String> foundXavierRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		assertThat(foundDakotaRecords).containsOnly(records.folder4_2().getId(), records.folder4_2_doc1().getId());
		assertThat(foundXavierRecords).isEqualTo(foundDakotaRecords);
	}

	@Test
	public void givenHeroesAndLegendHaveAccessToCategory2WhenAddBobAndRemoveHeroesFromFolder4AndResetItThenOnlyHeroesAndLegendHaveAccessToFolder4()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		Authorization authorizationHeroes = addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.legendsIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()), asList(records.folder4().getId()));
		waitForBatchProcess();
		services.removeAuthorizationOnRecord(authorizationHeroes, records.folder4(),
				KEEP_ATTACHED);
		waitForBatchProcess();
		services.reset(records.folder4());
		waitForBatchProcess();

		List<String> foundHeroesRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		List<String> foundLegendsRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		List<String> foundBobRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		assertThat(foundHeroesRecords).contains(records.folder4().getId(), records.folder4_1().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2().getId(), records.folder4_2_doc1().getId());
		assertThat(foundHeroesRecords).isEqualTo(foundLegendsRecords);
		assertThat(foundBobRecords).doesNotContain(records.folder4().getId(), records.folder4_1().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2().getId(), records.folder4_2_doc1().getId());
	}

	@Test
	public void givenHeroesAndLegendHaveAccessToCategory2WhenAddBobAndRemoveHeroesFromFolder4DetachingAndResetItThenOnlyHeroesAndLegendHaveAccessToFolder4()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		Authorization authorizationHeroes = addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		waitForBatchProcess();
		addAuthorizationWithoutDetaching(roles, asList(users.legendsIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		waitForBatchProcess();
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()), asList(records.folder4().getId()));
		waitForBatchProcess();

		services.removeAuthorizationOnRecord(authorizationHeroes, records.folder4(),
				CustomizedAuthorizationsBehavior.DETACH);
		waitForBatchProcess();
		services.reset(records.folder4());
		waitForBatchProcess();

		List<String> foundHeroesRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		List<String> foundLegendsRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		List<String> foundBobRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		assertThat(foundHeroesRecords).contains(records.folder4().getId(), records.folder4_1().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2().getId(), records.folder4_2_doc1().getId());
		assertThat(foundHeroesRecords).isEqualTo(foundLegendsRecords);
		assertThat(foundBobRecords).doesNotContain(records.folder4().getId(), records.folder4_1().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2().getId(), records.folder4_2_doc1().getId());
	}

	@Test
	public void givenMultipleAuthorizationAddedAtSameMomentThenAllOk()
			throws RolesManagerRuntimeException, InterruptedException {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		Authorization authorizationHeroes = addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.folder4().getId()));
		List<Record> folders = asList(records.folder1(), records.folder2(), records.folder3(), records.folder4(),
				records.folder5());
		for (Record folder : folders) {
			addAuthorizationWithoutDetaching(roles, asList(users.aliceIn(zeCollection).getId()), asList(folder.getId()));
			addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()), asList(folder.getId()));
			addAuthorizationWithoutDetaching(roles, asList(users.chuckNorrisIn(zeCollection).getId()), asList(folder.getId()));
			addAuthorizationWithoutDetaching(roles, asList(users.dakotaIn(zeCollection).getId()), asList(folder.getId()));
			addAuthorizationWithoutDetaching(roles, asList(users.edouardLechatIn(zeCollection).getId()), asList(folder.getId()));
			addAuthorizationWithoutDetaching(roles, asList(users.gandalfIn(zeCollection).getId()), asList(folder.getId()));
			addAuthorizationWithoutDetaching(roles, asList(users.charlesIn(zeCollection).getId()), asList(folder.getId()));
		}
		waitForBatchProcess();

		assertThat(findAllFoldersAndDocuments(users.aliceIn(zeCollection)))
				.contains(records.folder1().getId(), records.folder2().getId(),
						records.folder3().getId(), records.folder4().getId(), records.folder5().getId());
	}

	@Test
	public void givenHeroesAndLegendHaveAccessToFolder4AndBobToFolder4_1WhenRemoveHeroesFromFolder4_1AndResetFolder4AndAddHeroesToFolder4ThenHeroesSeeFolder4_1()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		Authorization authorizationHeroes = addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.folder4().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.legendsIn(zeCollection).getId()), asList(records.folder4().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()), asList(records.folder4_1().getId()));
		services.removeAuthorizationOnRecord(authorizationHeroes, records.folder4_1(),
				KEEP_ATTACHED);
		services.reset(records.folder4());
		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()), asList(records.folder4().getId()));
		waitForBatchProcess();

		List<String> foundHeroesRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		assertThat(foundHeroesRecords).contains(records.folder4().getId(), records.folder4_1().getId(),
				records.folder4_1_doc1().getId());
	}

	@Test
	public void givenHeroesAndLegendHaveAccessToFolder4AndBobToFolder4_1WhenRemoveHeroesDetachingFromFolder4_1AndResetFolder4AndAddHeroesToFolder4ThenHeroesDontSeeFolder4_1()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		Authorization authorizationHeroes = addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.folder4().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.legendsIn(zeCollection).getId()), asList(records.folder4().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()), asList(records.folder4_1().getId()));
		waitForBatchProcess();

		services.removeAuthorizationOnRecord(authorizationHeroes, records.folder4_1(),
				CustomizedAuthorizationsBehavior.DETACH);
		services.reset(records.folder4());
		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()), asList(records.folder4().getId()));
		waitForBatchProcess();

		List<String> foundHeroesRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		assertThat(foundHeroesRecords).doesNotContain(records.folder4_1().getId(), records.folder4_1_doc1().getId());
	}

	@Test(expected = AuthorizationsServicesRuntimeException.CannotDetachConcept.class)
	public void whenTryToDetachConceptThenException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);

		addAuthorizationDetaching(roles, asList(users.heroesIn(zeCollection).getId()), asList(records.taxo1_category2().getId()));
	}

	@Test
	public void givenHeroesAndLegendsHaveWriteAccessToFolder4AndHeroesAndBobHasReadAccessToFolder4WhenRemove1stAuthorizationsFromFolder4_1ThenHeroesAndBobOnlyHaveReadAccessAndLegendsHasNoAccess()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> writePermissionRoles = Arrays.asList(Role.WRITE);
		List<String> readPermissionRoles = Arrays.asList(READ);
		addAuthorizationWithoutDetaching(readPermissionRoles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.folder4().getId()));
		Authorization writeAuthorizationHeroesAndLegends = addAuthorizationWithoutDetaching(writePermissionRoles,
				asList(users.heroesIn(zeCollection).getId(), users.legendsIn(zeCollection).getId()),
				asList(records.folder4().getId()));
		addAuthorizationWithoutDetaching(readPermissionRoles,
				asList(users.heroesIn(zeCollection).getId(), users.bobIn(zeCollection).getId()),
				asList(records.folder4().getId()));
		waitForBatchProcess();

		services.removeAuthorizationOnRecord(writeAuthorizationHeroesAndLegends, records.folder4_1(),
				CustomizedAuthorizationsBehavior.DETACH);
		waitForBatchProcess();

		List<String> foundReadPermissionLegendsRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		List<String> foundReadPermissionHeroesRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		List<String> foundReadPermissionBobRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		List<String> foundWritePermissionLegendsRecords = findAllFoldersAndDocumentsWithWritePermission(
				users.aliceIn(zeCollection));
		List<String> foundWritePermissionHeroesRecords = findAllFoldersAndDocumentsWithWritePermission(
				users.charlesIn(zeCollection));
		List<String> foundWritePermissionBobRecords = findAllFoldersAndDocumentsWithWritePermission(users.bobIn(zeCollection));
		assertThat(foundReadPermissionLegendsRecords).containsOnly(records.folder4().getId(), records.folder4_2().getId(),
				records.folder4_2_doc1().getId());
		assertThat(foundWritePermissionLegendsRecords).containsOnly(records.folder4().getId(), records.folder4_2().getId(),
				records.folder4_2_doc1().getId());
		assertThat(foundWritePermissionHeroesRecords).containsOnly(records.folder4().getId(), records.folder4_2().getId(),
				records.folder4_2_doc1().getId());
		assertThat(foundWritePermissionBobRecords).isEmpty();
		assertThat(foundReadPermissionHeroesRecords).contains(records.folder4().getId(), records.folder4_1().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2().getId(), records.folder4_2_doc1().getId()).isEqualTo(
				foundReadPermissionBobRecords);
	}

	@Test
	public void givenAliceAndBobHaveAccessToFolder4AndBobHasNoLongerAccessToFolder4_2AndCharlesHasAccessToFolder4_2WithoutDetachingWhenAddingDakotaAndDetachingThenOnlyAliceBobAndDakotaHaveAccessToFolder4_2()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.aliceIn(zeCollection).getId()), asList(records.folder4().getId()));
		Authorization authorizationBob = addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.folder4().getId()));
		waitForBatchProcess();

		services.removeAuthorizationOnRecord(authorizationBob, records.folder4_2(),
				KEEP_ATTACHED);
		addAuthorizationWithoutDetaching(roles, asList(users.charlesIn(zeCollection).getId()),
				asList(records.folder4_2().getId()));
		addAuthorizationDetaching(roles, asList(users.dakotaIn(zeCollection).getId()), asList(records.folder4_2().getId()));
		waitForBatchProcess();

		List<String> foundAliceRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		List<String> foundBobRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		List<String> foundXavierRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		List<String> foundDakotaRecords = findAllFoldersAndDocuments(users.dakotaLIndienIn(zeCollection));
		assertThat(foundAliceRecords).contains(records.folder4_2().getId());
		assertThat(foundBobRecords).doesNotContain(records.folder4_2().getId());
		assertThat(foundXavierRecords).contains(records.folder4_2().getId());
		assertThat(foundDakotaRecords).contains(records.folder4_2().getId());
	}

	@Test
	@Ignore
	public void givenAliceAndBobHaveAccessToFolder4AndBobHasNoLongerAccessToFolder4_2AndXavierHasAccessToFolder4_2DetachingWhenRemovingAliceAndAddingDakotaToFolder4_2ThenXavierAndDakotaHaveAccessToFolder4_2()
			throws Exception {
		// This test used to rely on invalid behaviour of the AuthorizationServices when detaching
		// Also, I do not believe the case here represents anything we do (or want to do) in the application
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		Authorization authorizationAlice = addAuthorizationWithoutDetaching(roles, asList(users.aliceIn(zeCollection).getId()),
				asList(records.folder4().getId()));
		Authorization authorizationBob = addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.folder4().getId()));
		waitForBatchProcess();

		services.removeAuthorizationOnRecord(authorizationBob, records.folder4_2(),
				KEEP_ATTACHED);
		addAuthorizationDetaching(roles, asList(users.charlesIn(zeCollection).getId()), asList(records.folder4_2().getId()));
		waitForBatchProcess();

		services.removeAuthorizationOnRecord(authorizationAlice, records.folder4_2(),
				KEEP_ATTACHED);
		addAuthorizationWithoutDetaching(roles, asList(users.dakotaIn(zeCollection).getId()),
				asList(records.folder4_2().getId()));
		waitForBatchProcess();

		List<String> foundAliceRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		List<String> foundBobRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		List<String> foundXavierRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		List<String> foundDakotaRecords = findAllFoldersAndDocuments(users.dakotaLIndienIn(zeCollection));
		assertThat(foundAliceRecords).doesNotContain(records.folder4_2().getId());
		assertThat(foundBobRecords).doesNotContain(records.folder4_2().getId());
		assertThat(foundXavierRecords).contains(records.folder4_2().getId());
		assertThat(foundDakotaRecords).contains(records.folder4_2().getId());
	}

	@Test(expected = AuthorizationsServicesRuntimeException.CannotAddAuhtorizationInNonPrincipalTaxonomy.class)
	public void whenAddingAuthorizationOnAconceptOfASecondaryTaxonomyThenException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);

		addAuthorizationDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo2_station2_1().getId()));
	}

	@Test
	public void givenLegendsHaveAuthWhenAddingAuthToAliceThenAliceInheritsLegendsAuthsAlongsideHerOwn()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.legendsIn(zeCollection).getId()),
				asList(records.taxo1_category2_1().getId()));
		waitForBatchProcess();
		addAuthorizationWithoutDetaching(roles, asList(users.aliceIn(zeCollection).getId()), asList(records.folder1().getId()));
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		assertThat(foundRecords)
				.containsOnly(records.folder1().getId(), records.folder3().getId(), records.folder1_doc1().getId(),
						records.folder3_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.edouardLechatIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder3().getId(), records.folder3_doc1().getId());
	}

	@Test
	public void whenGetRecordsAuthorizationsThenObtainsAuthorizations()
			throws Exception {

		givenTaxonomy1IsThePrincipalAndSomeRecords();
		String legends = users.legendsIn(zeCollection).getId();
		String folder2 = records.folder2().getId();
		String folder4 = records.folder4().getId();
		String taxo1_category1 = records.taxo1_category1().getId();
		String heroes = users.heroesIn(zeCollection).getId();
		String dakota = users.dakotaIn(zeCollection).getId();
		String gandalf = users.gandalfIn(zeCollection).getId();
		String edouard = users.edouardLechatIn(zeCollection).getId();

		List<String> readRoles = asList(READ);
		List<String> writeRoles = asList(Role.WRITE);
		addAuthorizationWithoutDetaching(readRoles, asList(legends), asList(folder4));
		addAuthorizationWithoutDetaching(writeRoles, asList(heroes, dakota), asList(folder2, folder4));
		addAuthorizationWithoutDetaching(readRoles, asList(dakota), asList(taxo1_category1));
		waitForBatchProcess();

		assertThat(services.getRecordAuthorizations(get(folder2))).hasSize(2)
				.has(authorizationGrantingRolesOnTo(writeRoles, asList(folder2, folder4), asList(heroes, dakota)))
				.has(authorizationGrantingRolesOnTo(readRoles, asList(taxo1_category1), asList(dakota)));

		assertThat(services.getRecordAuthorizations(get(folder4))).hasSize(2)
				.has(authorizationGrantingRolesOnTo(readRoles, asList(folder4), asList(legends)))
				.has(authorizationGrantingRolesOnTo(writeRoles, asList(folder2, folder4), asList(heroes, dakota)));

		assertThat(services.getRecordAuthorizations(get(taxo1_category1))).hasSize(1)
				.has(authorizationGrantingRolesOnTo(readRoles, asList(taxo1_category1), asList(dakota)));

		assertThat(services.getRecordAuthorizations(get(legends))).hasSize(1)
				.has(authorizationGrantingRolesOnTo(readRoles, asList(folder4), asList(legends)));

		assertThat(services.getRecordAuthorizations(get(heroes))).hasSize(1)
				.has(authorizationGrantingRolesOnTo(writeRoles, asList(folder2, folder4), asList(heroes, dakota)));

		List<Authorization> authorizations = services.getRecordAuthorizations(get(dakota));
		System.out.println(authorizations);
		assertThat(services.getRecordAuthorizations(get(dakota))).hasSize(2)
				.has(authorizationGrantingRolesOnTo(writeRoles, asList(folder2, folder4), asList(heroes, dakota)))
				.has(authorizationGrantingRolesOnTo(readRoles, asList(taxo1_category1), asList(dakota)));

		assertThat(services.getRecordAuthorizations(get(gandalf))).hasSize(2)
				.has(authorizationGrantingRolesOnTo(readRoles, asList(folder4), asList(legends)))
				.has(authorizationGrantingRolesOnTo(writeRoles, asList(folder2, folder4), asList(heroes, dakota)));

		assertThat(services.getRecordAuthorizations(get(edouard))).hasSize(1)
				.has(authorizationGrantingRolesOnTo(readRoles, asList(folder4), asList(legends)));
	}

	@Test
	public void givenLegendsAndHeroesHaveAuthsWhenAddingAuthToGandalfThenGandalfInheritsBothGroupsAuthsAlongsideHisOwn()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.legendsIn(zeCollection).getId()), asList(records.folder4().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()), asList(records.folder2().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.gandalfIn(zeCollection).getId()),
				asList(records.taxo1_category1().getId()));
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.gandalfIn(zeCollection));

		assertThat(foundRecords).containsOnly(records.folder1().getId(), records.folder2().getId(), records.folder2_1().getId(),
				records.folder2_2().getId(), records.folder1_doc1().getId(), records.folder2_2_doc1().getId(),
				records.folder2_2_doc2().getId(), records.folder4().getId(), records.folder4_1().getId(),
				records.folder4_2().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2_doc1().getId());
	}

	@Test
	public void givenBobHasReadAccessToFolder2_2_doc2ThenBobSeesOnlyFolder2_2_doc2()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.folder2_2_doc2().getId()));
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder2_2_doc2().getId());
	}

	@Test
	public void givenLegendsHaveAuthWhenAddingAuthToAliceAndRemovingAliceFromLegendsThenAliceLosesLegendsAuthsButKeepsHerOwn()
			throws Exception {

		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);

		String aliceId = users.aliceIn(zeCollection).getId();
		String legendsId = users.legendsIn(zeCollection).getId();

		addAuthorizationWithoutDetaching("ZeFirst", roles, asList(users.legendsIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		addAuthorizationWithoutDetaching("ZeSecond", roles, asList(users.aliceIn(zeCollection).getId()),
				asList(records.folder1().getId()));
		waitForBatchProcess();
		assertThat(users.aliceIn(zeCollection).getUserTokens()).containsOnly(
				"r__ZeFirst",
				"r__ZeSecond",
				"r_" + aliceId,
				"w_" + aliceId,
				"d_" + aliceId,
				"r_" + legendsId,
				"w_" + legendsId,
				"d_" + legendsId);
		userServices.addUpdateUserCredential(userServices.getUserCredential("alice").withRemovedGlobalGroup("legends"));

		User alice = users.aliceIn(zeCollection);
		assertThat(users.aliceIn(zeCollection).getUserTokens()).containsOnly(
				"r__ZeSecond",
				"r_" + aliceId,
				"w_" + aliceId,
				"d_" + aliceId);

		List<String> foundRecords = findAllFoldersAndDocuments(alice);
		assertThat(foundRecords).containsOnly(records.folder1().getId(), records.folder1_doc1().getId());
	}

	@Test
	public void givenLegendsHaveAuthWhenAddingAuthToAliceAndMovingAliceToHeroesThenAliceLosesLegendsAuthsButKeepsHerOwn()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.legendsIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()), asList(records.folder2().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.aliceIn(zeCollection).getId()), asList(records.folder1().getId()));
		waitForBatchProcess();
		String heroes = users.heroesIn(zeCollection).getId();
		recordServices.update(users.aliceIn(zeCollection).setUserGroups(asList(heroes)));

		List<String> foundRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder1().getId(), records.folder2().getId(), records.folder2_1().getId(),
				records.folder2_2().getId(), records.folder1_doc1().getId(), records.folder2_2_doc1().getId(),
				records.folder2_2_doc2().getId());
	}

	@Test
	public void givenBobHasReadAccessToStation2WhenRemovingBobsAuthOnFolder2ThenBobSeesOnlyFolder1()
			throws Exception {
		givenTaxonomy2IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.taxo2_station2().getId()));
		waitForBatchProcess();

		for (Authorization recordAuth : services.getRecordAuthorizations(records.folder2())) {
			if (recordAuth.getGrantedToPrincipals().contains(users.bobIn(zeCollection).getId())) {
				services.removeAuthorizationOnRecord(recordAuth, records.folder2(),
						CustomizedAuthorizationsBehavior.DETACH);
			}
		}
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder1().getId(), records.folder1_doc1().getId());
	}

	@Test
	public void givenBobHasReadAccessToStation2WhenRemovingAndReaddingBobsAuthOnFolder2ThenBobSeesFolder1AndFolder2()
			throws Exception {
		givenTaxonomy2IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.taxo2_station2().getId()));
		waitForBatchProcess();

		for (Authorization recordAuth : services.getRecordAuthorizations(records.folder2())) {
			if (recordAuth.getGrantedToPrincipals().contains(users.bobIn(zeCollection).getId())) {
				services.removeAuthorizationOnRecord(recordAuth, records.folder2(),
						CustomizedAuthorizationsBehavior.DETACH);
			}
		}
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()), asList(records.folder2().getId()));
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder1().getId(), records.folder2().getId(), records.folder2_1().getId(),
				records.folder2_2().getId(), records.folder1_doc1().getId(), records.folder2_2_doc1().getId(),
				records.folder2_2_doc2().getId());
	}

	@Test
	public void givenAuthorizationsWithStartAndEndDateThenOnlyActiveDuringSpecifiedTimerange()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		givenTimeIs(date(2016, 4, 4));

		//A daily authorizaiton
		auth1 = addKeepingAttached(authorizationForUser(aliceWonderland).on(TAXO1_FOND1_1)
				.startingOn(date(2016, 4, 5)).endingOn(date(2016, 4, 5)).givingReadWriteAccess());

		//A 4 day authorizaiton
		auth2 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_FOND1_1)
				.startingOn(date(2016, 4, 5)).endingOn(date(2016, 4, 8)).givingReadWriteAccess());

		//A future authorization
		auth3 = addKeepingAttached(authorizationForUser(charles).on(TAXO1_FOND1_1)
				.startingOn(date(2016, 4, 7)).givingReadWriteAccess());

		//An authorization with an end
		auth4 = addKeepingAttached(authorizationForUser(dakota).on(TAXO1_FOND1_1)
				.endingOn(date(2016, 4, 6)).givingReadWriteAccess());

		auth5 = addKeepingAttached(authorizationForUser(edouard).on(TAXO1_FOND1_1).givingReadWriteAccess());

		//An authorization started in the past
		auth6 = addKeepingAttached(authorizationForUser(gandalf).on(TAXO1_FOND1_1)
				.during(date(2016, 4, 3), date(2016, 4, 6)).givingReadWriteAccess());

		//An authorization already finished
		try {
			auth7 = addKeepingAttached(authorizationForUser(sasquatch).on(TAXO1_FOND1_1)
					.during(date(2016, 4, 1), date(2016, 4, 3)).givingReadWriteAccess());
			fail("Exception expected");
		} catch (AuthorizationDetailsManagerRuntimeException.EndDateLessThanCurrentDate e) {
			//OK
		}

		givenTimeIs(date(2016, 4, 4));
		services.refreshActivationForAllAuths(collectionsListManager.getCollections());
		waitForBatchProcess();
		assertThatAllAuthorizationIds().containsOnly("-" + auth1, "-" + auth2, "-" + auth3, auth4, auth5, auth6);
		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, dakota, edouard, gandalf);
		}

		givenTimeIs(date(2016, 4, 5));
		services.refreshActivationForAllAuths(collectionsListManager.getCollections());
		waitForBatchProcess();
		assertThatAllAuthorizationIds().containsOnly(auth1, auth2, "-" + auth3, auth4, auth5, auth6);
		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, dakota, edouard, alice, bob, gandalf);
		}

		givenTimeIs(date(2016, 4, 6));
		services.refreshActivationForAllAuths(collectionsListManager.getCollections());
		waitForBatchProcess();
		assertThatAllAuthorizationIds().containsOnly(auth2, "-" + auth3, auth4, auth5, auth6);
		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, dakota, edouard, bob, gandalf);
		}

		givenTimeIs(date(2016, 4, 7));
		services.refreshActivationForAllAuths(collectionsListManager.getCollections());
		waitForBatchProcess();
		assertThatAllAuthorizationIds().containsOnly(auth2, auth3, auth5);
		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, edouard, bob, charles);
		}

		givenTimeIs(date(2016, 4, 8));
		waitForBatchProcess();
		services.refreshActivationForAllAuths(collectionsListManager.getCollections());
		assertThatAllAuthorizationIds().containsOnly(auth2, auth3, auth5);
		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, charles, edouard, bob);
		}

		givenTimeIs(date(2016, 4, 9));
		services.refreshActivationForAllAuths(collectionsListManager.getCollections());
		waitForBatchProcess();
		assertThatAllAuthorizationIds().containsOnly(auth3, auth5);
		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, charles, edouard);
		}

	}

	@Test
	public void givenUserWithCollectionAccessThenHasAccessNoMatterTheRecordsAuthorizationAndHasNoRolePermissions()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		//Valider que ça ne donne pas un accès permission

		Transaction transaction = new Transaction();
		transaction.add(users.edouardIn(zeCollection).setCollectionReadAccess(true).setSystemAdmin(true));
		transaction.add(users.charlesIn(zeCollection).setCollectionReadAccess(true));
		transaction.add(users.dakotaIn(zeCollection).setCollectionWriteAccess(true));
		transaction.add(users.aliceIn(zeCollection).setCollectionDeleteAccess(true));
		recordServices.execute(transaction);
		auth1 = addKeepingAttached(authorizationForUser(charles).on(TAXO1_FOND1_1).givingReadWriteAccess());
		auth2 = addKeepingAttached(authorizationForUser(dakota).on(TAXO1_FOND1_1).givingReadAccess());

		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER2)) {
			verifyRecord.usersWithReadAccess().containsOnly(charles, dakota, alice, chuck, edouard);
			verifyRecord.usersWithWriteAccess().containsOnly(charles, dakota, chuck);
			verifyRecord.usersWithDeleteAccess().containsOnly(alice, chuck);
			verifyRecord.usersWithPermission("aPermission").containsOnly(admin, edouard);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_FOND1, TAXO1_CATEGORY2_1, FOLDER3)) {
			verifyRecord.usersWithReadAccess().containsOnly(charles, dakota, alice, chuck, edouard);
			verifyRecord.usersWithWriteAccess().containsOnly(dakota, chuck);
			verifyRecord.usersWithDeleteAccess().containsOnly(alice, chuck);
			verifyRecord.usersWithPermission("aPermission").containsOnly(admin, edouard);
		}

	}

	@Test
	public void givenGroupWithCollectionAccessThenHasAccessNoMatterTheRecordsAuthorizationAndHasNoRolePermissions()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
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

		fail("todo");
	}

	@Test
	public void givenUserWithDeletePermissionOnRecordsThenCanOnlyDeleteRecordsIfHasPermissionOnWholeHierarchy()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();

		recordServices.logicallyDelete(records.folder2(), users.chuckNorrisIn(zeCollection));

		//Bob has no delete permission
		forUserInZeCollection(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER4).isFalse();
		forUserInZeCollection(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_FOND1_1).isFalse();
		forUserInZeCollection(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_CATEGORY1).isFalse();
		forUserInZeCollection(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER1).isFalse();
		forUserInZeCollection(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER2).isFalse();

		//Bob has a delete permission the whole hierarchy
		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_FOND1).givingReadWriteDeleteAccess());
		forUserInZeCollection(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER4).isTrue();
		forUserInZeCollection(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_FOND1_1).isTrue();
		forUserInZeCollection(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_CATEGORY1).isTrue();
		forUserInZeCollection(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER1).isTrue();
		forUserInZeCollection(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER2).isFalse();

		//Bob has a delete permission on folder 4
		modify(authorizationOnRecord(auth1, FOLDER4).removingItOnRecord());
		forUserInZeCollection(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER4).isFalse();
		forUserInZeCollection(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_FOND1_1).isTrue();
		forUserInZeCollection(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_CATEGORY1).isTrue();
		forUserInZeCollection(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER1).isTrue();
		forUserInZeCollection(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER2).isFalse();

		//Bob has a delete permission on category 1
		modify(authorizationOnRecord(auth1, TAXO1_CATEGORY1).removingItOnRecord());
		forUserInZeCollection(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER4).isFalse();
		forUserInZeCollection(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_FOND1_1).isFalse();
		forUserInZeCollection(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_CATEGORY1).isFalse();
		forUserInZeCollection(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER1).isFalse();
		forUserInZeCollection(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER2).isFalse();

	}

	@Test
	public void givenUserWithDeletePermissionOnRecordsThenCanOnlyRestoreRecordsIfHasPermissionOnWholeHierarchy()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();

		recordServices.logicallyDelete(records.folder1(), users.chuckNorrisIn(zeCollection));
		recordServices.logicallyDelete(records.folder4(), users.chuckNorrisIn(zeCollection));
		recordServices.logicallyDelete(records.taxo1_fond1_1(), users.chuckNorrisIn(zeCollection));

		//Bob has no delete permission
		forUserInZeCollection(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER4).isFalse();
		forUserInZeCollection(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_FOND1_1).isFalse();
		forUserInZeCollection(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_CATEGORY1).isFalse();
		forUserInZeCollection(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER1).isFalse();
		forUserInZeCollection(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER2).isFalse();

		//Bob has a delete permission the whole hierarchy
		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_FOND1).givingReadWriteDeleteAccess());
		forUserInZeCollection(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER4).isTrue();
		forUserInZeCollection(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_FOND1_1).isTrue();
		forUserInZeCollection(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_CATEGORY1).isTrue();
		forUserInZeCollection(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER1).isTrue();
		forUserInZeCollection(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER2).isFalse();

		//Bob has a delete permission on folder 4
		modify(authorizationOnRecord(auth1, FOLDER4).removingItOnRecord());
		forUserInZeCollection(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER4).isFalse();
		forUserInZeCollection(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_FOND1_1).isTrue();
		forUserInZeCollection(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_CATEGORY1).isTrue();
		forUserInZeCollection(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER1).isTrue();
		forUserInZeCollection(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER2).isFalse();

		//Bob has a delete permission on category 1
		modify(authorizationOnRecord(auth1, TAXO1_CATEGORY1).removingItOnRecord());
		forUserInZeCollection(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER4).isFalse();
		forUserInZeCollection(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_FOND1_1).isFalse();
		forUserInZeCollection(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_CATEGORY1).isFalse();
		forUserInZeCollection(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER1).isFalse();
		forUserInZeCollection(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER2).isFalse();

	}

	@Test
	public void givenUserWithDeletePermissionOnPrincipalConceptButNotOnSomeRecordsThenCanOnlyDeleteConceptIfExcludingRecords()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();

		//Bob has no delete permission
		forUserInZeCollection(bob).assertHasDeletePermissionOnPrincipalConceptExcludingRecords(TAXO1_CATEGORY2).isFalse();
		forUserInZeCollection(bob).assertHasDeletePermissionOnPrincipalConceptIncludingRecords(TAXO1_CATEGORY2).isFalse();

		//Bob has a delete permission the whole category2 hierarchy
		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadWriteDeleteAccess());
		forUserInZeCollection(bob).assertHasDeletePermissionOnPrincipalConceptExcludingRecords(TAXO1_CATEGORY2).isTrue();
		forUserInZeCollection(bob).assertHasDeletePermissionOnPrincipalConceptIncludingRecords(TAXO1_CATEGORY2).isTrue();

		//Bob has a delete permission on category2, but not the whole hierarchy
		modify(authorizationOnRecord(auth1, FOLDER4).removingItOnRecord());
		forUserInZeCollection(bob).assertHasDeletePermissionOnPrincipalConceptExcludingRecords(TAXO1_CATEGORY2).isTrue();
		forUserInZeCollection(bob).assertHasDeletePermissionOnPrincipalConceptIncludingRecords(TAXO1_CATEGORY2).isFalse();

		try {
			forUserInZeCollection(bob).assertHasDeletePermissionOnPrincipalConceptExcludingRecords(FOLDER1);
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.RecordIsNotAConceptOfPrincipalTaxonomy e) {
			//OK
		}

		try {
			forUserInZeCollection(bob).assertHasDeletePermissionOnPrincipalConceptExcludingRecords(TAXO2_STATION2);
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.RecordIsNotAConceptOfPrincipalTaxonomy e) {
			//OK
		}

	}

	@Test
	public void whenGetConceptsForWhichUserHasPermissionThenReturnTheGoodConcepts()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		recordServices.update(users.dakotaIn(zeCollection).setUserRoles(asList(ROLE3)));
		auth1 = addKeepingAttached(authorizationForUser(sasquatch).on(TAXO1_CATEGORY1).giving(ROLE1));
		auth2 = addKeepingAttached(authorizationForUser(sasquatch).on(TAXO1_CATEGORY2).giving(ROLE1));
		auth3 = addKeepingAttached(authorizationForUser(sasquatch).on(TAXO1_FOND1_1).giving(ROLE2));

		forUserInZeCollection(dakota).assertThatConceptsForWhichUserHas(PERMISSION_OF_NO_ROLE).isEmpty();
		forUserInZeCollection(dakota).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE1).isEmpty();
		forUserInZeCollection(dakota).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE1_AND_ROLE2).isEmpty();
		forUserInZeCollection(dakota).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE2).isEmpty();
		forUserInZeCollection(dakota).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE3).containsOnly(
				TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1);

		forUserInZeCollection(sasquatch).assertThatConceptsForWhichUserHas(PERMISSION_OF_NO_ROLE).isEmpty();
		forUserInZeCollection(sasquatch).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE1).containsOnly(
				TAXO1_CATEGORY1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1);
		forUserInZeCollection(sasquatch).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE1_AND_ROLE2).containsOnly(
				TAXO1_CATEGORY1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, TAXO1_FOND1_1);
		forUserInZeCollection(sasquatch).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE2).containsOnly(
				TAXO1_FOND1_1, TAXO1_CATEGORY1);
		forUserInZeCollection(sasquatch).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE3).isEmpty();

		for (String permission : asList(PERMISSION_OF_ROLE1, PERMISSION_OF_ROLE1_AND_ROLE2, PERMISSION_OF_ROLE2,
				PERMISSION_OF_ROLE3)) {
			forUserInAnotherCollection(sasquatch).assertThatConceptsForWhichUserHas(permission).isEmpty();
		}

	}

	@Test
	public void whenGetUsersWithGlobalPermissionThenReturnTheGoodUsers()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();

		Transaction transaction = new Transaction();
		transaction.add(users.sasquatchIn(zeCollection).setUserRoles(asList(ROLE1)));
		transaction.add(users.robinIn(zeCollection).setUserRoles(asList(ROLE2)));
		recordServices.execute(transaction);
		auth1 = addKeepingAttached(authorizationForUser(robin).on(TAXO1_FOND1).giving(ROLE3));

		assertThatUsersWithGlobalPermissionInZeCollection(PERMISSION_OF_NO_ROLE).isEmpty();
		assertThatUsersWithGlobalPermissionInZeCollection(PERMISSION_OF_ROLE1).containsOnly(sasquatch);
		assertThatUsersWithGlobalPermissionInZeCollection(PERMISSION_OF_ROLE1_AND_ROLE2).containsOnly(sasquatch, robin);
		assertThatUsersWithGlobalPermissionInZeCollection(PERMISSION_OF_ROLE2).containsOnly(robin);
		assertThatUsersWithGlobalPermissionInZeCollection(PERMISSION_OF_ROLE3).isEmpty();
	}

	@Test
	public void whenGetUsersWithPermissionOnConceptThenReturnTheGoodUsers()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();

		recordServices.update(users.aliceIn(zeCollection).setUserRoles(asList(ROLE3)));
		auth1 = addKeepingAttached(authorizationForUser(sasquatch).on(TAXO1_CATEGORY2).giving(ROLE2));
		auth2 = addKeepingAttached(authorizationForUser(robin).on(TAXO1_FOND1).giving(ROLE1));
		auth3 = addKeepingAttached(authorizationForUser(gandalf).on(FOLDER1).giving(ROLE1));

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
		givenTaxonomy1IsThePrincipalAndSomeRecords();

		recordServices.update(users.aliceIn(zeCollection).setUserRoles(asList(ROLE3)));
		auth1 = addKeepingAttached(authorizationForUser(sasquatch).on(TAXO1_CATEGORY2).giving(ROLE2));
		auth2 = addKeepingAttached(authorizationForUser(robin).on(TAXO1_FOND1).giving(ROLE1));
		auth3 = addKeepingAttached(authorizationForUser(gandalf).on(FOLDER1).giving(ROLE1));

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
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		roleManager.addRole(new Role(zeCollection, "roleA", "First role", asList("operation1", "operation2")));
		roleManager.addRole(new Role(zeCollection, "roleB", "Second role", asList("operation3", "operation4")));
		roleManager.addRole(new Role(zeCollection, "roleC", "Third role", asList("operation5", "operation6")));

		recordServices.update(users.sasquatchIn(zeCollection).setUserRoles(asList("roleA")));

		auth1 = addKeepingAttached(authorizationForUser(sasquatch).on(TAXO1_CATEGORY1).giving("roleB"));

		Record folder1Inside = refreshed(records.folder1());
		Record folder2Inside = refreshed(records.folder2());
		Record folder3Outside = refreshed(records.folder3());
		Record folder4Outside = refreshed(records.folder4());

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

}
