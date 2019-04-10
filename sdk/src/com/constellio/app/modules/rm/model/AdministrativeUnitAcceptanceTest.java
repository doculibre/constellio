package com.constellio.app.modules.rm.model;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.UserFunction;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import static com.constellio.app.modules.rm.services.UserFunctionChecker.ParentAdministrativeUnitFunctionsInclusion.EXCLUDE_PARENT_USER_FUNCTIONS;
import static com.constellio.app.modules.rm.services.UserFunctionChecker.ParentAdministrativeUnitFunctionsInclusion.INCLUDE_PARENT_USER_FUNCTIONS;
import static com.constellio.app.modules.rm.services.UserFunctionChecker.ParentAdministrativeUnitFunctionsInclusion.ONLY_INCLUDE_PARENT_USER_FUNCTIONS_IF_NO_RESULTS;
import static com.constellio.app.modules.rm.wrappers.AdministrativeUnit.FUNCTIONS;
import static com.constellio.app.modules.rm.wrappers.AdministrativeUnit.FUNCTIONS_USERS;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class AdministrativeUnitAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	Users users = new Users();

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records).withAllTest(users)
		);
		assertThat(getModelLayerFactory().getTaxonomiesManager().getPrincipalTaxonomy(zeCollection).getCode())
				.isEqualTo(RMTaxonomies.ADMINISTRATIVE_UNITS);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();


	}

	@Test
	public void whenAssigningFunctionsToUserThenPersistedAndRetrievedToFindUsersByFunction()
			throws Exception {

		Transaction tx = new Transaction();
		UserFunction fonctionRecteur = tx.add(rm.newUserFunctionWithId("idRecteur")).setTitle("Recteur");
		UserFunction fonctionDoyen = tx.add(rm.newUserFunctionWithId("idDoyen")).setTitle("Doyen");
		UserFunction fonctionProfesseur = tx.add(rm.newUserFunctionWithId("idProfesseur")).setTitle("Professeur");
		UserFunction fonctionChargeCours = tx.add(rm.newUserFunctionWithId("idCharge")).setTitle("Chargé de cours");

		tx.add(records.getUnit10())
				.addFunction(fonctionRecteur, users.gandalfIn(zeCollection))
				.addFunction(fonctionDoyen, users.chuckNorrisIn(zeCollection))
				.addFunction(fonctionProfesseur, users.charlesIn(zeCollection))
				.addFunction(fonctionProfesseur, users.robinIn(zeCollection))
				.addFunction(fonctionProfesseur, users.robinIn(zeCollection));

		tx.add(records.getUnit10a())
				.addFunction(fonctionProfesseur, users.dakotaLIndienIn(zeCollection))
				.addFunction(fonctionDoyen, users.bobIn(zeCollection))
				.addFunction(fonctionChargeCours, users.aliceIn(zeCollection));

		rm.executeTransaction(tx);

		assertThatRecord(records.getUnit10()).extracting(FUNCTIONS, FUNCTIONS_USERS).containsOnly(
				asList("idRecteur", "idDoyen", "idProfesseur", "idProfesseur"),
				asList(users.gandalfIn(zeCollection).getId(), users.chuckNorrisIn(zeCollection).getId(),
						users.charlesIn(zeCollection).getId(), users.robinIn(zeCollection).getId())

		);

		assertThat(records.getUnit10().getUsersWithFunction(fonctionProfesseur)).containsOnly(
				users.charlesIn(zeCollection).getId(), users.robinIn(zeCollection).getId());


		assertThat(records.getUnit10().getFunctionsOfUser(users.robinIn(zeCollection))).containsOnly(
				fonctionProfesseur.getId());

		recordServices.update(records.getUnit10().removeFunction(fonctionProfesseur, users.robinIn(zeCollection)));

		assertThatRecord(records.getUnit10()).extracting(FUNCTIONS, FUNCTIONS_USERS).containsOnly(
				asList("idRecteur", "idDoyen", "idProfesseur"),
				asList(users.gandalfIn(zeCollection).getId(), users.chuckNorrisIn(zeCollection).getId(),
						users.charlesIn(zeCollection).getId())

		);


		assertThat(records.getUnit10().getUsersWithFunction(fonctionProfesseur)).containsOnly(
				users.charlesIn(zeCollection).getId());


		assertThat(records.getUnit10().getFunctionsOfUser(users.robinIn(zeCollection))).isEmpty();
	}

	@Test
	public void givenUsersWithFunctionsOnAdministrativeUnitsThenReturnedUsingUtilityMethods()
			throws Exception {

		Transaction tx = new Transaction();
		UserFunction fonctionRecteur = tx.add(rm.newUserFunctionWithId("idRecteur")).setTitle("Recteur");
		UserFunction fonctionDoyen = tx.add(rm.newUserFunctionWithId("idDoyen")).setTitle("Doyen");
		UserFunction fonctionProfesseur = tx.add(rm.newUserFunctionWithId("idProfesseur")).setTitle("Professeur");
		UserFunction fonctionChargeCours = tx.add(rm.newUserFunctionWithId("idCharge")).setTitle("Chargé de cours");

		tx.add(records.getUnit10())
				.addFunction(fonctionRecteur, users.gandalfIn(zeCollection))
				.addFunction(fonctionDoyen, users.chuckNorrisIn(zeCollection))
				.addFunction(fonctionProfesseur, users.charlesIn(zeCollection))
				.addFunction(fonctionProfesseur, users.robinIn(zeCollection))
				.addFunction(fonctionProfesseur, users.robinIn(zeCollection));

		tx.add(records.getUnit10a())
				.addFunction(fonctionProfesseur, users.dakotaLIndienIn(zeCollection))
				.addFunction(fonctionDoyen, users.bobIn(zeCollection))
				.addFunction(fonctionChargeCours, users.aliceIn(zeCollection));

		rm.executeTransaction(tx);

		//
		assertThatRecords(rm.getUsersWithFunction(fonctionDoyen).onAdministrativeUnit(records.getUnit10a(), INCLUDE_PARENT_USER_FUNCTIONS))
				.extractingMetadata(User.USERNAME).containsOnly(bob, chuckNorris);

		assertThatRecords(rm.getUsersWithFunction(fonctionDoyen).onAdministrativeUnit(records.getUnit10a(), EXCLUDE_PARENT_USER_FUNCTIONS))
				.extractingMetadata(User.USERNAME).containsOnly(bob);

		assertThatRecords(rm.getUsersWithFunction(fonctionDoyen).onAdministrativeUnit(records.getUnit10a(), ONLY_INCLUDE_PARENT_USER_FUNCTIONS_IF_NO_RESULTS))
				.extractingMetadata(User.USERNAME).containsOnly(bob);

		assertThatRecords(rm.getUsersWithFunction(fonctionDoyen).onAdministrativeUnit(records.getUnit10()))
				.extractingMetadata(User.USERNAME).containsOnly(chuckNorris);


		//
		assertThatRecords(rm.getUsersWithFunction(fonctionProfesseur).onAdministrativeUnit(records.getUnit10a(), INCLUDE_PARENT_USER_FUNCTIONS))
				.extractingMetadata(User.USERNAME).containsOnly(charles, robin, dakota);

		assertThatRecords(rm.getUsersWithFunction(fonctionProfesseur).onAdministrativeUnit(records.getUnit10a(), EXCLUDE_PARENT_USER_FUNCTIONS))
				.extractingMetadata(User.USERNAME).containsOnly(dakota);

		assertThatRecords(rm.getUsersWithFunction(fonctionProfesseur).onAdministrativeUnit(records.getUnit10a(), ONLY_INCLUDE_PARENT_USER_FUNCTIONS_IF_NO_RESULTS))
				.extractingMetadata(User.USERNAME).containsOnly(dakota);

		assertThatRecords(rm.getUsersWithFunction(fonctionProfesseur).onAdministrativeUnit(records.getUnit10()))
				.extractingMetadata(User.USERNAME).containsOnly(charles, robin);

		//
		assertThatRecords(rm.getUsersWithFunction(fonctionChargeCours).onAdministrativeUnit(records.getUnit10a(), INCLUDE_PARENT_USER_FUNCTIONS))
				.extractingMetadata(User.USERNAME).containsOnly(alice);

		assertThatRecords(rm.getUsersWithFunction(fonctionChargeCours).onAdministrativeUnit(records.getUnit10a(), EXCLUDE_PARENT_USER_FUNCTIONS))
				.extractingMetadata(User.USERNAME).containsOnly(alice);

		assertThatRecords(rm.getUsersWithFunction(fonctionChargeCours).onAdministrativeUnit(records.getUnit10a(), ONLY_INCLUDE_PARENT_USER_FUNCTIONS_IF_NO_RESULTS))
				.extractingMetadata(User.USERNAME).containsOnly(alice);

		assertThatRecords(rm.getUsersWithFunction(fonctionChargeCours).onAdministrativeUnit(records.getUnit10()))
				.extractingMetadata(User.USERNAME).isEmpty();

		//
		assertThatRecords(rm.getUsersWithFunction(fonctionRecteur).onAdministrativeUnit(records.getUnit10a(), INCLUDE_PARENT_USER_FUNCTIONS))
				.extractingMetadata(User.USERNAME).containsOnly(gandalf);

		assertThatRecords(rm.getUsersWithFunction(fonctionRecteur).onAdministrativeUnit(records.getUnit10a(), EXCLUDE_PARENT_USER_FUNCTIONS))
				.extractingMetadata(User.USERNAME).isEmpty();

		assertThatRecords(rm.getUsersWithFunction(fonctionRecteur).onAdministrativeUnit(records.getUnit10a(), ONLY_INCLUDE_PARENT_USER_FUNCTIONS_IF_NO_RESULTS))
				.extractingMetadata(User.USERNAME).containsOnly(gandalf);


		assertThatRecords(rm.getUsersWithFunction(fonctionRecteur).onAdministrativeUnit(records.getUnit10()))
				.extractingMetadata(User.USERNAME).containsOnly(gandalf);
	}

}
