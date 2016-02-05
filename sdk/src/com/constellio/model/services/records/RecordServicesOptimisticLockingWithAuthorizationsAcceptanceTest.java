package com.constellio.model.services.records;

import static com.constellio.model.entities.security.CustomizedAuthorizationsBehavior.KEEP_ATTACHED;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.SecurityAcceptanceTestSetup;
import com.constellio.model.services.security.SecurityAcceptanceTestSetup.Records;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import com.constellio.sdk.tests.setups.Users;

@SlowTest
public class RecordServicesOptimisticLockingWithAuthorizationsAcceptanceTest extends ConstellioTest {

	String anotherCollection = "anotherCollection";
	SecurityAcceptanceTestSetup anothercollectionSetup = new SecurityAcceptanceTestSetup(anotherCollection);
	SecurityAcceptanceTestSetup setup = new SecurityAcceptanceTestSetup(zeCollection);
	MetadataSchemasManager schemasManager;
	SearchServices searchServices;
	RecordServices recordServices;
	TaxonomiesManager taxonomiesManager;
	CollectionsListManager collectionsListManager;
	AuthorizationsServices authorizationsServices;
	UserServices userServices;

	Records records;
	Records otherCollectionRecords;
	Users users = new Users();
	RolesManager roleManager;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withCollection(zeCollection).withAllTest(users),
				withCollection(anotherCollection).withAllTestUsers()
		);

		recordServices = getModelLayerFactory().newRecordServices();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		searchServices = getModelLayerFactory().newSearchServices();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		roleManager = getModelLayerFactory().getRolesManager();
		collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		userServices = getModelLayerFactory().newUserServices();

		defineSchemasManager().using(setup);
		taxonomiesManager.addTaxonomy(setup.getTaxonomy1(), schemasManager);
		taxonomiesManager.addTaxonomy(setup.getTaxonomy2(), schemasManager);
		taxonomiesManager.setPrincipalTaxonomy(setup.getTaxonomy1(), schemasManager);

		records = setup.givenRecords(recordServices);

		defineSchemasManager().using(anothercollectionSetup);
		taxonomiesManager.addTaxonomy(anothercollectionSetup.getTaxonomy1(), schemasManager);
		taxonomiesManager.addTaxonomy(anothercollectionSetup.getTaxonomy2(), schemasManager);
		otherCollectionRecords = anothercollectionSetup.givenRecords(recordServices);

	}

	@Test
	public void whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem()
			throws Exception {

		String aliceId = users.aliceIn(zeCollection).getId();
		String legendsId = users.legendsIn(zeCollection).getId();

		getDataLayerFactory().getDataLayerLogger().monitor("folder2");

		addAuthorizationWithoutDetaching("ZeFirst", asList(Role.READ), asList(users.legendsIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		addAuthorizationWithoutDetaching("ZeSecond", asList(Role.READ), asList(users.aliceIn(zeCollection).getId()),
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
	}

	@Test
	public void whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem_run01()
			throws Exception {
		whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem();
	}

	@Test
	public void whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem_run02()
			throws Exception {
		whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem();
	}

	@Test
	public void whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem_run03()
			throws Exception {
		whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem();
	}

	@Test
	public void whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem_run04()
			throws Exception {
		whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem();
	}

	@Test
	public void whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem_run05()
			throws Exception {
		whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem();
	}

	@Test
	public void whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem_run06()
			throws Exception {
		whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem();
	}

	@Test
	public void whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem_run07()
			throws Exception {
		whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem();
	}

	@Test
	public void whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem_run08()
			throws Exception {
		whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem();
	}

	@Test
	public void whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem_run09()
			throws Exception {
		whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem();
	}

	@Test
	public void whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem_run10()
			throws Exception {
		whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem();
	}

	@Test
	public void whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem_run11()
			throws Exception {
		whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem();
	}

	@Test
	public void whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem_run12()
			throws Exception {
		whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem();
	}

	@Test
	public void whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem_run13()
			throws Exception {
		whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem();
	}

	@Test
	public void whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem_run14()
			throws Exception {
		whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem();
	}

	@Test
	public void whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem_run15()
			throws Exception {
		whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem();
	}

	@Test
	public void whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem_run16()
			throws Exception {

		whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem();
	}

	@Test
	public void whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem_run17()
			throws Exception {
		whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem();
	}

	@Test
	public void whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem_run18()
			throws Exception {
		whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem();
	}

	@Test
	public void whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem_run19()
			throws Exception {
		whenAddingAuthorizationToAUserAndToOneOfItsGroupAtTheSameMomentThenNoProblem();
	}

	// ---------------------------------------------------------------------------------------------------------

	private Authorization addAuthorizationWithoutDetaching(String id, List<String> roles, List<String> grantedToPrincipals,
			List<String> grantedOnRecords) {
		AuthorizationDetails details = AuthorizationDetails.create(id, roles, null, null, zeCollection);
		Authorization authorization = new Authorization(details, grantedToPrincipals, grantedOnRecords);
		authorizationsServices.add(authorization, KEEP_ATTACHED, users.dakotaLIndienIn(zeCollection));
		return authorization;
	}

}
