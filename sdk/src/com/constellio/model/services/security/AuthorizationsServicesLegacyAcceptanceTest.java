package com.constellio.model.services.security;

import static com.constellio.model.entities.security.CustomizedAuthorizationsBehavior.DETACH;
import static com.constellio.model.entities.security.Role.READ;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.roles.RolesManagerRuntimeException;
import com.constellio.sdk.tests.annotations.SlowTest;

@SlowTest
public class AuthorizationsServicesLegacyAcceptanceTest extends BaseAuthorizationsServicesAcceptanceTest {

	@Test
	//Basic security test
	public void givenHeroesHaveReadAccessToCategory2ThenTheySeeFolder3AndFolder4()
			throws Exception {

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
	//Basic security test
	public void givenLegendsAndHeroesHaveAuthsWhenAddingAuthToGandalfThenGandalfInheritsBothGroupsAuthsAlongsideHisOwn()
			throws Exception {

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
	//Basic security test
	public void givenBobHasReadAccessToFolder2_2_doc2ThenBobSeesOnlyFolder2_2_doc2()
			throws Exception {

		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.folder2_2_doc2().getId()));
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder2_2_doc2().getId());
	}

	@Test
	public void givenLegendsHaveReadAuthToFolder2WhenGivingWriteAuthToAliceThenEdouardReadsFolder2AndAliceWritesFolder2()
			throws Exception {

		List<String> roles = Arrays.asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.legendsIn(zeCollection).getId()), asList(records.folder2().getId()));
		roles = Arrays.asList(Role.WRITE);
		detach(records.folder2().getId());
		addAuthorizationWithoutDetaching(roles, asList(users.aliceIn(zeCollection).getId()), asList(records.folder2().getId()));
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.edouardLechatIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder2().getId(), records.folder2_1().getId(), records.folder2_2().getId(),
				records.folder2_2_doc1().getId(), records.folder2_2_doc2().getId());
		foundRecords = findAllFoldersAndDocumentsWithWritePermission(users.aliceIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder2().getId(), records.folder2_1().getId(), records.folder2_2().getId(),
				records.folder2_2_doc1().getId(), records.folder2_2_doc2().getId());
	}

	@Test
	//Basic security test
	public void givenHeroesAndAliceHaveAuthToCategory2AndEdouardHasAuthToCategory2_1ThenAllButBobSeeFolder3()
			throws Exception {

		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId(), users.aliceIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.edouardLechatIn(zeCollection).getId()),
				asList(records.taxo1_category2_1().getId()));
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		assertThat(foundRecords).contains(records.folder3().getId(), records.folder3_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.edouardLechatIn(zeCollection));
		assertThat(foundRecords).contains(records.folder3().getId(), records.folder3_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		assertThat(foundRecords).contains(records.folder3().getId(), records.folder3_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.dakotaLIndienIn(zeCollection));
		assertThat(foundRecords).contains(records.folder3().getId(), records.folder3_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.gandalfIn(zeCollection));
		assertThat(foundRecords).contains(records.folder3().getId(), records.folder3_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		assertThat(foundRecords).isEmpty();
	}

	@Test
	//Basic security test
	public void givenHeroesAndAliceHaveAuthToCategory2AndEdouardHasAuthToFolder4ThenAllButBobSeeFolder4()
			throws Exception {

		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId(), users.aliceIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.edouardLechatIn(zeCollection).getId()),
				asList(records.folder4().getId()));
		waitForBatchProcess();

		// List<String> foundRecords = findAllFoldersAndDocuments(users.chuckNorrisIn(zeCollection));
		// assertThat(foundRecords).containsOnly(records.folder4().getId(), records.folder4_1().getId(), records.folder4_2().getId(),
		// records.folder4_1_doc1().getId(), records.folder4_2_doc1().getId());

		List<String> foundRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		assertThat(foundRecords).contains(records.folder4().getId(), records.folder4_1().getId(), records.folder4_2().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.edouardLechatIn(zeCollection));
		assertThat(foundRecords).contains(records.folder4().getId(), records.folder4_1().getId(), records.folder4_2().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		assertThat(foundRecords).contains(records.folder4().getId(), records.folder4_1().getId(), records.folder4_2().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.dakotaLIndienIn(zeCollection));
		assertThat(foundRecords).contains(records.folder4().getId(), records.folder4_1().getId(), records.folder4_2().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.gandalfIn(zeCollection));
		assertThat(foundRecords).contains(records.folder4().getId(), records.folder4_1().getId(), records.folder4_2().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		assertThat(foundRecords).isEmpty();
	}

	@Test
	public void givenBobHasAnAuthorizationGivingHimARoleWithReadAccessHasTheHasTheRolePermissionsOnTargetRecordsAndTheirDescendants
			()
			throws RolesManagerRuntimeException, InterruptedException, RecordServicesException {

		User bob = users.bobIn(zeCollection);
		addAuthorizationWithoutDetaching(asList("zeRole", READ), asList(bob.getId()),
				asList(records.taxo1_category1().getId()));
		waitForBatchProcess();

		bob = users.bobIn(zeCollection);
		assertThat(services.canRead(bob, records.folder1())).isTrue();
		assertThat(services.canRead(bob, records.folder2())).isTrue();

		assertThat(services.canRead(bob, records.taxo1_category1())).isTrue();
	}

	@Test
	public void givenBobHasAnAuthorizationOnAGroupGivingHimARoleWithReadAccessHasTheRolePermissionsOnTargetRecordsAndTheirDescendants
			()
			throws RolesManagerRuntimeException, InterruptedException, RecordServicesException {

		Group group = createGroup("HEROES");

		User bob = users.bobIn(zeCollection);
		bob.setCollectionReadAccess(true);
		bob.setUserGroups(asList(group.getId()));

		addAuthorizationWithoutDetaching(asList("zeRole"), asList(group.getId()),
				asList(records.taxo1_category1().getId()));
		waitForBatchProcess();

		assertThat(services.canRead(bob, records.folder1())).isTrue();
		assertThat(services.canRead(bob, records.folder2())).isTrue();

	}

	@Test
	public void givenBobHasAnAuthorizationGivingHimARoleWithoutReadAccessThenHasNoPermissionsOnTheCollectionRecords()
			throws RolesManagerRuntimeException, InterruptedException, RecordServicesException {

		User bob = users.bobIn(zeCollection);

		addAuthorizationWithoutDetaching(asList("zeRole"), asList(bob.getId()),
				asList(records.taxo1_category1().getId()));
		waitForBatchProcess();

		assertThat(services.canRead(bob, records.taxo1_category1())).isFalse();
	}

	@Test
	public void givenBobHasAnAuthorizationOnAGroupGivingHimARoleWithoutReadAccessThenHasNoPermissionsOnTheCollectionRecords()
			throws RolesManagerRuntimeException, InterruptedException, RecordServicesException {

		Group group = createGroup("HEROES");

		User bob = users.bobIn(zeCollection);
		// bob.setCollectionReadAccess(true);
		bob.setUserGroups(asList(group.getId()));

		addAuthorizationWithoutDetaching(asList("zeRole"), asList(group.getId()),
				asList(records.taxo1_category1().getId()));
		waitForBatchProcess();

		assertThat(services.canRead(bob, records.taxo1_category1())).isFalse();
	}

	@Test
	public void givenGroupHasAuthsThenAuthsInheritedToSubGroupAndItsUsers()
			throws InterruptedException {

		List<String> roles = asList(READ, Role.WRITE);

		assertThat(users.robinIn(zeCollection)).is(notAllowedToWrite(records.taxo1_category2()));

		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));

		waitForBatchProcess();

		assertThat(users.robinIn(zeCollection)).is(allowedToWrite(records.taxo1_category2()));

	}

	@Test
	public void givenAddAuthorizationWhenGetAuthorizationThenReturnIt()
			throws Exception {

		List<String> roles = asList(READ);

		Authorization authorization = addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		waitForBatchProcess();
		String authId = authorization.getDetail().getId();

		Authorization retrievedAuthorization = services
				.getAuthorization(authorization.getDetail().getCollection(),
						authId);

		assertThat(authorization.getDetail()).isEqualToComparingFieldByField(retrievedAuthorization.getDetail());
		assertThat(authorization.getGrantedOnRecords()).isEqualTo(retrievedAuthorization.getGrantedOnRecords());
		assertThat(authorization.getGrantedToPrincipals()).isEqualTo(retrievedAuthorization.getGrantedToPrincipals());
	}

}
