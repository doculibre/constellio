package com.constellio.model.services.records.cache.cacheIndexHook.impl;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.records.RecordId;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.services.records.RecordId.id;
import static com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHookKey.attachedRecordInPrincipalConcept;
import static com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHookKey.principalAccessOnRecordInConcept;
import static com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHookKey.principalConceptAuthGivingAccessToRecordInSecondaryConceptKey;
import static com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHookKey.recordInSecondaryConcept;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.apache.curator.shaded.com.google.common.primitives.Booleans.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.LocalDate.now;

public class TaxonomyCacheHookAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();

	@Test
	public void givenUserWithAccessToAnAdministrativeUnitThenValidTokens() throws Exception {
		Toggle.DEBUG_TAXONOMY_RECORDS_HOOK.enable();
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTestSuingStandardIds(records)
				.withFoldersAndContainersOfEveryStatus());

		//Improved story telling
		String abeille = records.folder_A01;
		AuthorizationsServices authServices = getModelLayerFactory().newAuthorizationsServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		Folder casquetteExpo = rm.newFolder().setParentFolder(abeille).setOpenDate(now()).setTitle("Casquette Expo");
		Folder gabriel = rm.newFolder().setParentFolder(casquetteExpo).setOpenDate(now()).setTitle("Gabriel");
		getModelLayerFactory().newRecordServices().execute(new Transaction(casquetteExpo, gabriel));
		getModelLayerFactory().newRecordServices().refresh(casquetteExpo, gabriel);
		RecordId admin = users.adminIn(zeCollection).getWrappedRecordId();
		RecordId bob = users.bobIn(zeCollection).getWrappedRecordId();
		RecordId charles = users.charlesIn(zeCollection).getWrappedRecordId();
		RecordId dakota = users.dakotaIn(zeCollection).getWrappedRecordId();
		RecordId gandalf = users.gandalfIn(zeCollection).getWrappedRecordId();
		RecordId legends = users.legendsIn(zeCollection).getWrappedRecordId();
		RecordId heroes = users.heroesIn(zeCollection).getWrappedRecordId();
		RecordId rumors = users.rumorsIn(zeCollection).getWrappedRecordId();
		RecordId sidekicks = users.sidekicksIn(zeCollection).getWrappedRecordId();

		getModelLayerFactory().getRecordsCaches().stream(rm.administrativeUnit.schemaType()).sorted(
				(c1, c2) -> c1.getId().compareTo(c2.getId())).forEach((ua) -> {
			System.out.println("Admin units | " + ua.getIdTitle());
		});

		getModelLayerFactory().getRecordsCaches().stream(rm.category.schemaType()).sorted(
				(c1, c2) -> c1.getId().compareTo(c2.getId())).forEach((c) -> {
			System.out.println("Categories | " + c.getIdTitle());
		});

		getModelLayerFactory().getRecordsCaches().stream(rm.folder.schemaType()).sorted(
				(c1, c2) -> c1.getId().compareTo(c2.getId())).forEach((f) -> {
			System.out.println("Folders | " + f.getIdTitle());
		});

		getModelLayerFactory().getRecordsCaches().stream(rm.user.schemaType()).sorted(
				(c1, c2) -> c1.getId().compareTo(c2.getId())).forEach((f) -> {
			System.out.println("Users | " + f.getIdTitle());
		});

		getModelLayerFactory().getRecordsCaches().stream(rm.group.schemaType()).sorted(
				(c1, c2) -> c1.getId().compareTo(c2.getId())).forEach((f) -> {
			System.out.println("Groups | " + f.getIdTitle());
		});

		//A01 is visible (active)

		System.out.println("SECONDARY_CONCEPTS_INT_IDS:" + casquetteExpo.getList(Schemas.SECONDARY_CONCEPTS_INT_IDS));
		System.out.println("PRINCIPAL ANCESTORS:" + casquetteExpo.getList(Schemas.PRINCIPALS_ANCESTORS_INT_IDS));
		System.out.println("ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS:" + casquetteExpo.getList(Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS));
		System.out.println("PRINCIPAL_CONCEPTS:" + casquetteExpo.getList(Schemas.PRINCIPAL_CONCEPTS_INT_IDS));

		TaxonomyRecordsHook hook = new TaxonomyRecordsHook(zeCollection, getModelLayerFactory());
		assertThat(hook.getKeys(record(records.folder_A01))).containsOnly(
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X), true),
				attachedRecordInPrincipalConcept(id(records.unitId_10a), true),
				attachedRecordInPrincipalConcept(id(records.unitId_10), true),
				recordInSecondaryConcept(id(records.categoryId_X110), true),
				recordInSecondaryConcept(id(records.categoryId_X100), true),
				recordInSecondaryConcept(id(records.categoryId_X), true));

		assertThat(hook.getKeys(record(records.folder_A08))).containsOnly(
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_Z112), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_Z112), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_Z110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_Z110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_Z100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_Z100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_Z), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_Z), true),
				attachedRecordInPrincipalConcept(id(records.unitId_10a), true),
				attachedRecordInPrincipalConcept(id(records.unitId_10), true),
				recordInSecondaryConcept(id(records.categoryId_Z112), true),
				recordInSecondaryConcept(id(records.categoryId_Z110), true),
				recordInSecondaryConcept(id(records.categoryId_Z100), true),
				recordInSecondaryConcept(id(records.categoryId_Z), true));

		//A42 is not visible (semi-active)
		assertThat(hook.getKeys(record(records.folder_A42))).containsOnly(
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X110), false),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X110), false),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X100), false),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X100), false),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X), false),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X), false),
				attachedRecordInPrincipalConcept(id(records.unitId_10a), false),
				attachedRecordInPrincipalConcept(id(records.unitId_10), false),
				recordInSecondaryConcept(id(records.categoryId_X110), false),
				recordInSecondaryConcept(id(records.categoryId_X100), false),
				recordInSecondaryConcept(id(records.categoryId_X), false));

		//B03 is visible in another administrative unit and another category
		assertThat(hook.getKeys(record(records.folder_B03))).containsOnly(
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_11b), id(records.categoryId_Z112), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_11), id(records.categoryId_Z112), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_Z112), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_11b), id(records.categoryId_Z110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_11), id(records.categoryId_Z110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_Z110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_11b), id(records.categoryId_Z100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_11), id(records.categoryId_Z100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_Z100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_11b), id(records.categoryId_Z), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_11), id(records.categoryId_Z), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_Z), true),
				attachedRecordInPrincipalConcept(id(records.unitId_11b), true),
				attachedRecordInPrincipalConcept(id(records.unitId_11), true),
				attachedRecordInPrincipalConcept(id(records.unitId_10), true),
				recordInSecondaryConcept(id(records.categoryId_Z112), true),
				recordInSecondaryConcept(id(records.categoryId_Z110), true),
				recordInSecondaryConcept(id(records.categoryId_Z100), true),
				recordInSecondaryConcept(id(records.categoryId_Z), true));

		assertThat(hook.getKeys(casquetteExpo.get())).containsOnly(
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X), true),
				attachedRecordInPrincipalConcept(id(records.unitId_10a), true),
				attachedRecordInPrincipalConcept(id(records.unitId_10), true),
				recordInSecondaryConcept(id(records.categoryId_X110), true),
				recordInSecondaryConcept(id(records.categoryId_X100), true),
				recordInSecondaryConcept(id(records.categoryId_X), true)

		);

		assertThat(hook.getKeys(gabriel.get())).containsOnly(
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X), true),
				attachedRecordInPrincipalConcept(id(records.unitId_10a), true),
				attachedRecordInPrincipalConcept(id(records.unitId_10), true),
				recordInSecondaryConcept(id(records.categoryId_X110), true),
				recordInSecondaryConcept(id(records.categoryId_X100), true),
				recordInSecondaryConcept(id(records.categoryId_X), true)

		);

		authServices.detach(casquetteExpo);
		getModelLayerFactory().newRecordServices().refresh(casquetteExpo, gabriel);
		System.out.println("--------");
		System.out.println("SECONDARY_CONCEPTS_INT_IDS:" + casquetteExpo.getList(Schemas.SECONDARY_CONCEPTS_INT_IDS));
		System.out.println("PRINCIPAL ANCESTORS:" + casquetteExpo.getList(Schemas.PRINCIPALS_ANCESTORS_INT_IDS));
		System.out.println("ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS:" + casquetteExpo.getList(Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS));
		System.out.println("PRINCIPAL_CONCEPTS:" + casquetteExpo.getList(Schemas.PRINCIPAL_CONCEPTS_INT_IDS));

		//bob_userInAC, charles_userInA, admin_userIdWithAllAccess);
		//		List<String> managerInUnit10 = asList(dakota_managerInA_userInB, gandalf_managerInABC
		assertThat(hook.getKeys(casquetteExpo.get())).containsOnly(
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(admin, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(admin, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(bob, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(bob, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(bob, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(bob, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(bob, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(charles, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(charles, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(charles, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(charles, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(charles, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.unitId_10), true, true),

				recordInSecondaryConcept(id(records.categoryId_X110), true),
				recordInSecondaryConcept(id(records.categoryId_X100), true),
				recordInSecondaryConcept(id(records.categoryId_X), true)

		);


		assertThat(hook.getKeys(gabriel.get())).containsOnly(
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(admin, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(admin, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(bob, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(bob, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(bob, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(bob, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(bob, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(charles, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(charles, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(charles, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(charles, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(charles, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.unitId_10), true, true),

				recordInSecondaryConcept(id(records.categoryId_X110), true),
				recordInSecondaryConcept(id(records.categoryId_X100), true),
				recordInSecondaryConcept(id(records.categoryId_X), true)
		);

		authServices.add(AuthorizationAddRequest.authorizationForGroups(users.legendsIn(zeCollection)).on(casquetteExpo).givingReadAccess());
		authServices.add(AuthorizationAddRequest.authorizationForGroups(users.heroesIn(zeCollection)).on(gabriel).givingWriteAccess());

		getModelLayerFactory().newRecordServices().refresh(casquetteExpo, gabriel);

		assertThat(hook.getKeys(casquetteExpo.get())).containsOnly(
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(admin, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(admin, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(bob, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(bob, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(bob, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(bob, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(bob, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(charles, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(charles, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(charles, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(charles, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(charles, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.unitId_10), true, true),


				principalAccessOnRecordInConcept(legends, id(records.categoryId_X110), false, true),
				principalAccessOnRecordInConcept(legends, id(records.categoryId_X100), false, true),
				principalAccessOnRecordInConcept(legends, id(records.categoryId_X), false, true),
				principalAccessOnRecordInConcept(legends, id(records.unitId_10a), false, true),
				principalAccessOnRecordInConcept(legends, id(records.unitId_10), false, true),

				principalAccessOnRecordInConcept(rumors, id(records.categoryId_X110), false, true),
				principalAccessOnRecordInConcept(rumors, id(records.categoryId_X100), false, true),
				principalAccessOnRecordInConcept(rumors, id(records.categoryId_X), false, true),
				principalAccessOnRecordInConcept(rumors, id(records.unitId_10a), false, true),
				principalAccessOnRecordInConcept(rumors, id(records.unitId_10), false, true),

				recordInSecondaryConcept(id(records.categoryId_X110), true),
				recordInSecondaryConcept(id(records.categoryId_X100), true),
				recordInSecondaryConcept(id(records.categoryId_X), true)

		);


		assertThat(hook.getKeys(gabriel.get())).containsOnly(
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(admin, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(admin, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(bob, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(bob, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(bob, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(bob, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(bob, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(charles, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(charles, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(charles, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(charles, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(charles, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(legends, id(records.categoryId_X110), false, true),
				principalAccessOnRecordInConcept(legends, id(records.categoryId_X100), false, true),
				principalAccessOnRecordInConcept(legends, id(records.categoryId_X), false, true),
				principalAccessOnRecordInConcept(legends, id(records.unitId_10a), false, true),
				principalAccessOnRecordInConcept(legends, id(records.unitId_10), false, true),

				principalAccessOnRecordInConcept(rumors, id(records.categoryId_X110), false, true),
				principalAccessOnRecordInConcept(rumors, id(records.categoryId_X100), false, true),
				principalAccessOnRecordInConcept(rumors, id(records.categoryId_X), false, true),
				principalAccessOnRecordInConcept(rumors, id(records.unitId_10a), false, true),
				principalAccessOnRecordInConcept(rumors, id(records.unitId_10), false, true),

				principalAccessOnRecordInConcept(heroes, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(heroes, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(heroes, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(heroes, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(heroes, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(sidekicks, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(sidekicks, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(sidekicks, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(sidekicks, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(sidekicks, id(records.unitId_10), true, true),

				recordInSecondaryConcept(id(records.categoryId_X110), true),
				recordInSecondaryConcept(id(records.categoryId_X100), true),
				recordInSecondaryConcept(id(records.categoryId_X), true)

		);
	}

	@Test
	public void givenNonStandardIdsAndUserWithAccessToAnAdministrativeUnitThenValidTokens() throws Exception {
		Toggle.DEBUG_TAXONOMY_RECORDS_HOOK.enable();
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records).withFoldersAndContainersOfEveryStatus());

		Set<Integer> hashCodes = new HashSet<>();
		getModelLayerFactory().getRecordsCaches().stream().forEach((r) -> {
			if (!r.getRecordId().isInteger()) {
				if (!hashCodes.add(r.getRecordId().hashCode())) {
					System.out.println("Duplicate  : " + r.getRecordId().hashCode());
				}
			}
		});


		//Improved story telling
		String abeille = records.folder_A01;
		AuthorizationsServices authServices = getModelLayerFactory().newAuthorizationsServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		Folder casquetteExpo = rm.newFolder().setParentFolder(abeille).setOpenDate(now()).setTitle("Casquette Expo");
		Folder gabriel = rm.newFolder().setParentFolder(casquetteExpo).setOpenDate(now()).setTitle("Gabriel");
		getModelLayerFactory().newRecordServices().execute(new Transaction(casquetteExpo, gabriel));
		getModelLayerFactory().newRecordServices().refresh(casquetteExpo, gabriel);
		RecordId admin = users.adminIn(zeCollection).getWrappedRecordId();
		RecordId bob = users.bobIn(zeCollection).getWrappedRecordId();
		RecordId charles = users.charlesIn(zeCollection).getWrappedRecordId();
		RecordId dakota = users.dakotaIn(zeCollection).getWrappedRecordId();
		RecordId gandalf = users.gandalfIn(zeCollection).getWrappedRecordId();
		RecordId legends = users.legendsIn(zeCollection).getWrappedRecordId();
		RecordId heroes = users.heroesIn(zeCollection).getWrappedRecordId();
		RecordId rumors = users.rumorsIn(zeCollection).getWrappedRecordId();
		RecordId sidekicks = users.sidekicksIn(zeCollection).getWrappedRecordId();

		getModelLayerFactory().getRecordsCaches().stream(rm.administrativeUnit.schemaType()).sorted(
				(c1, c2) -> c1.getId().compareTo(c2.getId())).forEach((ua) -> {
			System.out.println("Admin units | " + ua.getIdTitle());
		});

		getModelLayerFactory().getRecordsCaches().stream(rm.category.schemaType()).sorted(
				(c1, c2) -> c1.getId().compareTo(c2.getId())).forEach((c) -> {
			System.out.println("Categories | " + c.getIdTitle());
		});

		getModelLayerFactory().getRecordsCaches().stream(rm.folder.schemaType()).sorted(
				(c1, c2) -> c1.getId().compareTo(c2.getId())).forEach((f) -> {
			System.out.println("Folders | " + f.getIdTitle());
		});

		getModelLayerFactory().getRecordsCaches().stream(rm.user.schemaType()).sorted(
				(c1, c2) -> c1.getId().compareTo(c2.getId())).forEach((f) -> {
			System.out.println("Users | " + f.getIdTitle());
		});

		getModelLayerFactory().getRecordsCaches().stream(rm.group.schemaType()).sorted(
				(c1, c2) -> c1.getId().compareTo(c2.getId())).forEach((f) -> {
			System.out.println("Groups | " + f.getIdTitle());
		});

		//A01 is visible (active)

		System.out.println("SECONDARY_CONCEPTS_INT_IDS:" + casquetteExpo.getList(Schemas.SECONDARY_CONCEPTS_INT_IDS));
		System.out.println("PRINCIPAL ANCESTORS:" + casquetteExpo.getList(Schemas.PRINCIPALS_ANCESTORS_INT_IDS));
		System.out.println("ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS:" + casquetteExpo.getList(Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS));

		TaxonomyRecordsHook hook = new TaxonomyRecordsHook(zeCollection, getModelLayerFactory());
		assertThat(hook.getKeys(record(records.folder_A01))).containsOnly(
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X), true),
				attachedRecordInPrincipalConcept(id(records.unitId_10a), true),
				attachedRecordInPrincipalConcept(id(records.unitId_10), true),
				recordInSecondaryConcept(id(records.categoryId_X110), true),
				recordInSecondaryConcept(id(records.categoryId_X100), true),
				recordInSecondaryConcept(id(records.categoryId_X), true));

		assertThat(hook.getKeys(record(records.folder_A08))).containsOnly(
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_Z112), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_Z112), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_Z110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_Z110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_Z100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_Z100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_Z), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_Z), true),
				attachedRecordInPrincipalConcept(id(records.unitId_10a), true),
				attachedRecordInPrincipalConcept(id(records.unitId_10), true),
				recordInSecondaryConcept(id(records.categoryId_Z112), true),
				recordInSecondaryConcept(id(records.categoryId_Z110), true),
				recordInSecondaryConcept(id(records.categoryId_Z100), true),
				recordInSecondaryConcept(id(records.categoryId_Z), true));

		//A42 is not visible (semi-active)
		assertThat(hook.getKeys(record(records.folder_A42))).containsOnly(
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X110), false),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X110), false),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X100), false),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X100), false),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X), false),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X), false),
				attachedRecordInPrincipalConcept(id(records.unitId_10a), false),
				attachedRecordInPrincipalConcept(id(records.unitId_10), false),
				recordInSecondaryConcept(id(records.categoryId_X110), false),
				recordInSecondaryConcept(id(records.categoryId_X100), false),
				recordInSecondaryConcept(id(records.categoryId_X), false));

		//B03 is visible in another administrative unit and another category
		assertThat(hook.getKeys(record(records.folder_B03))).containsOnly(
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_11b), id(records.categoryId_Z112), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_11), id(records.categoryId_Z112), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_Z112), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_11b), id(records.categoryId_Z110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_11), id(records.categoryId_Z110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_Z110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_11b), id(records.categoryId_Z100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_11), id(records.categoryId_Z100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_Z100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_11b), id(records.categoryId_Z), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_11), id(records.categoryId_Z), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_Z), true),
				attachedRecordInPrincipalConcept(id(records.unitId_11b), true),
				attachedRecordInPrincipalConcept(id(records.unitId_11), true),
				attachedRecordInPrincipalConcept(id(records.unitId_10), true),
				recordInSecondaryConcept(id(records.categoryId_Z112), true),
				recordInSecondaryConcept(id(records.categoryId_Z110), true),
				recordInSecondaryConcept(id(records.categoryId_Z100), true),
				recordInSecondaryConcept(id(records.categoryId_Z), true));

		assertThat(hook.getKeys(casquetteExpo.get())).containsOnly(
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X), true),
				attachedRecordInPrincipalConcept(id(records.unitId_10a), true),
				attachedRecordInPrincipalConcept(id(records.unitId_10), true),
				recordInSecondaryConcept(id(records.categoryId_X110), true),
				recordInSecondaryConcept(id(records.categoryId_X100), true),
				recordInSecondaryConcept(id(records.categoryId_X), true)
		);

		assertThat(hook.getKeys(gabriel.get())).containsOnly(
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X110), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10a), id(records.categoryId_X), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X), true),
				attachedRecordInPrincipalConcept(id(records.unitId_10a), true),
				attachedRecordInPrincipalConcept(id(records.unitId_10), true),
				recordInSecondaryConcept(id(records.categoryId_X110), true),
				recordInSecondaryConcept(id(records.categoryId_X100), true),
				recordInSecondaryConcept(id(records.categoryId_X), true)

		);

		authServices.detach(casquetteExpo);
		getModelLayerFactory().newRecordServices().refresh(casquetteExpo, gabriel);

		System.out.println("SECONDARY_CONCEPTS_INT_IDS:" + casquetteExpo.getList(Schemas.SECONDARY_CONCEPTS_INT_IDS));
		System.out.println("PRINCIPAL ANCESTORS:" + casquetteExpo.getList(Schemas.PRINCIPALS_ANCESTORS_INT_IDS));
		System.out.println("ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS:" + casquetteExpo.getList(Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS));

		//bob_userInAC, charles_userInA, admin_userIdWithAllAccess);
		//		List<String> managerInUnit10 = asList(dakota_managerInA_userInB, gandalf_managerInABC
		assertThat(hook.getKeys(casquetteExpo.get())).containsOnly(
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(admin, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(admin, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(bob, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(bob, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(bob, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(bob, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(bob, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(charles, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(charles, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(charles, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(charles, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(charles, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.unitId_10), true, true),

				recordInSecondaryConcept(id(records.categoryId_X110), true),
				recordInSecondaryConcept(id(records.categoryId_X100), true),
				recordInSecondaryConcept(id(records.categoryId_X), true)

		);


		assertThat(hook.getKeys(gabriel.get())).containsOnly(
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(admin, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(admin, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(bob, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(bob, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(bob, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(bob, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(bob, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(charles, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(charles, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(charles, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(charles, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(charles, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.unitId_10), true, true),

				recordInSecondaryConcept(id(records.categoryId_X110), true),
				recordInSecondaryConcept(id(records.categoryId_X100), true),
				recordInSecondaryConcept(id(records.categoryId_X), true)
		);

		authServices.add(AuthorizationAddRequest.authorizationForGroups(users.legendsIn(zeCollection)).on(casquetteExpo).givingReadAccess());
		authServices.add(AuthorizationAddRequest.authorizationForGroups(users.heroesIn(zeCollection)).on(gabriel).givingWriteAccess());

		getModelLayerFactory().newRecordServices().refresh(casquetteExpo, gabriel);

		assertThat(hook.getKeys(casquetteExpo.get())).containsOnly(
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(admin, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(admin, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(bob, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(bob, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(bob, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(bob, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(bob, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(charles, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(charles, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(charles, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(charles, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(charles, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.unitId_10), true, true),


				principalAccessOnRecordInConcept(legends, id(records.categoryId_X110), false, true),
				principalAccessOnRecordInConcept(legends, id(records.categoryId_X100), false, true),
				principalAccessOnRecordInConcept(legends, id(records.categoryId_X), false, true),
				principalAccessOnRecordInConcept(legends, id(records.unitId_10a), false, true),
				principalAccessOnRecordInConcept(legends, id(records.unitId_10), false, true),

				principalAccessOnRecordInConcept(rumors, id(records.categoryId_X110), false, true),
				principalAccessOnRecordInConcept(rumors, id(records.categoryId_X100), false, true),
				principalAccessOnRecordInConcept(rumors, id(records.categoryId_X), false, true),
				principalAccessOnRecordInConcept(rumors, id(records.unitId_10a), false, true),
				principalAccessOnRecordInConcept(rumors, id(records.unitId_10), false, true),

				recordInSecondaryConcept(id(records.categoryId_X110), true),
				recordInSecondaryConcept(id(records.categoryId_X100), true),
				recordInSecondaryConcept(id(records.categoryId_X), true)

		);


		assertThat(hook.getKeys(gabriel.get())).containsOnly(
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(admin, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(admin, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(admin, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(bob, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(bob, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(bob, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(bob, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(bob, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(charles, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(charles, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(charles, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(charles, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(charles, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(dakota, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(gandalf, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(legends, id(records.categoryId_X110), false, true),
				principalAccessOnRecordInConcept(legends, id(records.categoryId_X100), false, true),
				principalAccessOnRecordInConcept(legends, id(records.categoryId_X), false, true),
				principalAccessOnRecordInConcept(legends, id(records.unitId_10a), false, true),
				principalAccessOnRecordInConcept(legends, id(records.unitId_10), false, true),

				principalAccessOnRecordInConcept(rumors, id(records.categoryId_X110), false, true),
				principalAccessOnRecordInConcept(rumors, id(records.categoryId_X100), false, true),
				principalAccessOnRecordInConcept(rumors, id(records.categoryId_X), false, true),
				principalAccessOnRecordInConcept(rumors, id(records.unitId_10a), false, true),
				principalAccessOnRecordInConcept(rumors, id(records.unitId_10), false, true),

				principalAccessOnRecordInConcept(heroes, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(heroes, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(heroes, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(heroes, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(heroes, id(records.unitId_10), true, true),

				principalAccessOnRecordInConcept(sidekicks, id(records.categoryId_X110), true, true),
				principalAccessOnRecordInConcept(sidekicks, id(records.categoryId_X100), true, true),
				principalAccessOnRecordInConcept(sidekicks, id(records.categoryId_X), true, true),
				principalAccessOnRecordInConcept(sidekicks, id(records.unitId_10a), true, true),
				principalAccessOnRecordInConcept(sidekicks, id(records.unitId_10), true, true),

				recordInSecondaryConcept(id(records.categoryId_X110), true),
				recordInSecondaryConcept(id(records.categoryId_X100), true),
				recordInSecondaryConcept(id(records.categoryId_X), true)

		);
	}

	@Test
	public void validateForAllTestUsersOnAllTestConceptsWithStandardIds() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTestSuingStandardIds(records)
				.withFoldersAndContainersOfEveryStatus());
		runTestRecordsValidation();

	}

	@Test
	public void validateForAllTestUsersOnAllTestConceptsWithNonStandardIds() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users)
				.withRMTest(records).withFoldersAndContainersOfEveryStatus());
		runTestRecordsValidation();

	}

	@Test
	public void test() throws Exception {
		Toggle.DEBUG_TAXONOMY_RECORDS_HOOK.enable();
		prepareSystem(
				withZeCollection().withAllTest(users).withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		inCollection(zeCollection).giveReadAccessTo(admin);

		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		UserServices userServices = getModelLayerFactory().newUserServices();
		UserCredential userCredential = userServices.getUserCredential(aliceWonderland);
		userServices.addUserToCollection(userCredential, zeCollection);

		AuthorizationsServices authsServices = getModelLayerFactory().newAuthorizationsServices();
		authsServices = getModelLayerFactory().newAuthorizationsServices();
		waitForBatchProcess();

		User sasquatch = users.sasquatchIn(zeCollection);
		User robin = users.robinIn(zeCollection);
		User admin = users.adminIn(zeCollection);


		authsServices.add(authorizationForUsers(sasquatch).on("B06").givingReadAccess(), admin);
		authsServices.add(authorizationForUsers(sasquatch).on(records.unitId_20d).givingReadAccess(), admin);

		authsServices.add(authorizationForUsers(robin).on("B06").givingReadAccess(), admin);
		authsServices.add(authorizationForUsers(robin).on(records.unitId_12c).givingReadAccess(), admin);
		authsServices.add(authorizationForUsers(robin).on(records.unitId_30).givingReadAccess(), admin);

		recordServices.refresh(sasquatch);
		recordServices.refresh(robin);

		waitForBatchProcess();

		TaxonomyRecordsHook hook = new TaxonomyRecordsHook(zeCollection, getModelLayerFactory());
		assertThat(hook.getKeys(record("B06"))).containsOnly(

				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_12b), id(records.categoryId_X100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_12b), id(records.categoryId_X), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_12), id(records.categoryId_X100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_12), id(records.categoryId_X), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X100), true),
				principalConceptAuthGivingAccessToRecordInSecondaryConceptKey(id(records.unitId_10), id(records.categoryId_X), true),

				principalAccessOnRecordInConcept(sasquatch.getWrappedRecordId(), id(records.unitId_12b), false, true),
				principalAccessOnRecordInConcept(sasquatch.getWrappedRecordId(), id(records.unitId_12), false, true),
				principalAccessOnRecordInConcept(sasquatch.getWrappedRecordId(), id(records.unitId_10), false, true),

				principalAccessOnRecordInConcept(sasquatch.getWrappedRecordId(), id(records.categoryId_X100), false, true),
				principalAccessOnRecordInConcept(sasquatch.getWrappedRecordId(), id(records.categoryId_X), false, true),

				principalAccessOnRecordInConcept(robin.getWrappedRecordId(), id(records.unitId_12b), false, true),
				principalAccessOnRecordInConcept(robin.getWrappedRecordId(), id(records.unitId_12), false, true),
				principalAccessOnRecordInConcept(robin.getWrappedRecordId(), id(records.unitId_10), false, true),

				principalAccessOnRecordInConcept(robin.getWrappedRecordId(), id(records.categoryId_X100), false, true),
				principalAccessOnRecordInConcept(robin.getWrappedRecordId(), id(records.categoryId_X), false, true),

				recordInSecondaryConcept(id(records.categoryId_X100), true),
				recordInSecondaryConcept(id(records.categoryId_X), true),

				attachedRecordInPrincipalConcept(id(records.unitId_12b), true),
				attachedRecordInPrincipalConcept(id(records.unitId_12), true),
				attachedRecordInPrincipalConcept(id(records.unitId_10), true)

		);


	}

	private void runTestRecordsValidation() throws RecordServicesException {

		TaxonomyRecordsHookRetriever retriever = getModelLayerFactory().getTaxonomyRecordsHookRetriever(zeCollection);
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		getModelLayerFactory().getRecordsCaches().stream(rm.administrativeUnit.schemaType()).sorted(
				(c1, c2) -> c1.getId().compareTo(c2.getId())).forEach((ua) -> {
			System.out.println("Admin units | " + ua.getIdTitle());
		});

		getModelLayerFactory().getRecordsCaches().stream(rm.category.schemaType()).sorted(
				(c1, c2) -> c1.getId().compareTo(c2.getId())).forEach((c) -> {
			System.out.println("Categories | " + c.getIdTitle());
		});

		getModelLayerFactory().getRecordsCaches().stream(rm.folder.schemaType()).sorted(
				(c1, c2) -> c1.getId().compareTo(c2.getId())).forEach((f) -> {
			System.out.println("Folders | " + f.getIdTitle());
		});

		getModelLayerFactory().getRecordsCaches().stream(rm.user.schemaType()).sorted(
				(c1, c2) -> c1.getId().compareTo(c2.getId())).forEach((f) -> {
			System.out.println("Users | " + f.getIdTitle());
		});

		getModelLayerFactory().getRecordsCaches().stream(rm.group.schemaType()).sorted(
				(c1, c2) -> c1.getId().compareTo(c2.getId())).forEach((f) -> {
			System.out.println("Groups | " + f.getIdTitle());
		});


		for (Category category : rm.categoryStream().collect(Collectors.toList())) {
			for (User user : rm.getAllUsers()) {
				for (boolean onlyVisible : asList(true, false)) {
					for (boolean writeAccess : asList(true, false)) {
						boolean hasAccessToCategory = retriever.hasUserAccessToSomethingInSecondaryConcept(
								user, category.getWrappedRecordId(), writeAccess, onlyVisible);
						boolean expectedHasAccessToCategory = visibleRecordsUsingSolr(category, user, writeAccess, onlyVisible);

						System.out.println(hasAccessToCategory + " - " + expectedHasAccessToCategory);

						if (hasAccessToCategory != expectedHasAccessToCategory) {
							hasAccessToCategory = retriever.hasUserAccessToSomethingInSecondaryConcept(
									user, category.getWrappedRecordId(), writeAccess, onlyVisible);
							assertThat(hasAccessToCategory).describedAs("user '" + user.getUsername() + "' " + (writeAccess ? "write" : "read") + " access to something " + (onlyVisible ? "visible " : "") + "in category '" + category.getCode() + "'")
									.isEqualTo(expectedHasAccessToCategory);
						}
					}
				}
			}
		}

		Transaction tx = new Transaction();
		Category childrenUnusedCategory = tx.add(rm.newCategory().setCode("1").setTitle("1").setParent(records.categoryId_Z112).setRetentionRules(Arrays.asList(records.ruleId_2)));
		Category rootUnusedCategory = tx.add(rm.newCategory().setCode("2").setTitle("2").setRetentionRules(Arrays.asList(records.ruleId_1)));
		Category rootCategoryWithFolder = tx.add(rm.newCategory().setCode("3").setTitle("3").setRetentionRules(Arrays.asList(records.ruleId_1)));
		AdministrativeUnit childrenUnusedAdministrativeUnit = tx.add(rm.newAdministrativeUnit().setCode("1").setTitle("1").setParent(records.unitId_10a));
		AdministrativeUnit rootUnusedAdministrativeUnit = tx.add(rm.newAdministrativeUnit().setCode("2").setTitle("2"));
		AdministrativeUnit rootAdministrativeUnit = tx.add(rm.newAdministrativeUnit().setCode("3").setTitle("3"));
		tx.add(rm.newFolder().setAdministrativeUnitEntered(rootAdministrativeUnit).setCategoryEntered(rootCategoryWithFolder)
				.setRetentionRuleEntered(records.ruleId_1).setOpenDate(new LocalDate())).setTitle("Folder at root of taxonomies");
		getModelLayerFactory().newRecordServices().execute(tx);

		AuthorizationsServices authServices = getModelLayerFactory().newAuthorizationsServices();
		authServices.add(AuthorizationAddRequest.authorizationForUsers(users.sasquatchIn(zeCollection)).on(childrenUnusedAdministrativeUnit).givingReadAccess());
		authServices.add(AuthorizationAddRequest.authorizationForUsers(users.sasquatchIn(zeCollection)).on(rootUnusedAdministrativeUnit).givingReadAccess());
		authServices.add(AuthorizationAddRequest.authorizationForUsers(users.sasquatchIn(zeCollection)).on(rootAdministrativeUnit).givingReadAccess());

		assertThat(records.getUnit10a().<Integer>getList(Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS))
				.containsOnly(RecordId.toIntId(records.unitId_10), RecordId.toIntId(records.unitId_10a));


		for (AdministrativeUnit administrativeUnit : rm.administrativeUnitStream().collect(Collectors.toList())) {
			for (User user : rm.getAllUsers()) {
				for (boolean onlyVisible : asList(true, false)) {
					for (boolean writeAccess : asList(true, false)) {
						boolean hasAccessToAdministrativeUnit = retriever.hasUserAccessToSomethingInPrincipalConcept(
								user, administrativeUnit.getWrappedRecord(), writeAccess, onlyVisible);

						boolean expectedHasAccessToUnit = visibleRecordsUsingSolr(administrativeUnit, user, writeAccess, onlyVisible);

						System.out.println(hasAccessToAdministrativeUnit + " - " + expectedHasAccessToUnit);

						if (hasAccessToAdministrativeUnit != expectedHasAccessToUnit) {
							hasAccessToAdministrativeUnit = retriever.hasUserAccessToSomethingInPrincipalConcept(
									user, administrativeUnit.getWrappedRecord(), writeAccess, onlyVisible);
							assertThat(hasAccessToAdministrativeUnit).describedAs("user '" + user.getUsername() + "' " + (writeAccess ? "write" : "read") + " access to something " + (onlyVisible ? "visible " : "") + "in adm. unit '" + administrativeUnit.getCode() + "'")
									.isEqualTo(expectedHasAccessToUnit);
						}
					}
				}
			}
		}
	}

	boolean visibleRecordsUsingSolr(Category category, User user, boolean write, boolean visible) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());


		LogicalSearchQuery query = new LogicalSearchQuery();

		if (visible) {
			query.setCondition(from(rm.folder.schemaType()).where(Schemas.PATH_PARTS).isEqualTo(category.getId())
					.andWhere(Schemas.VISIBLE_IN_TREES).isTrue());
		} else {
			query.setCondition(from(rm.folder.schemaType()).where(Schemas.PATH_PARTS).isEqualTo(category.getId()));
		}
		if (write) {
			query.filteredWithUserWrite(user);
		} else {
			query.filteredWithUser(user);
		}


		return searchServices.hasResults(query);
	}

	boolean visibleRecordsUsingSolr(AdministrativeUnit unit, User user, boolean write, boolean visible) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		SearchServices searchServices = getModelLayerFactory().newSearchServices();

		LogicalSearchQuery query = new LogicalSearchQuery();
		if (visible) {
			query.setCondition(from(rm.folder.schemaType()).where(Schemas.PATH_PARTS).isEqualTo(unit.getId())
					.andWhere(Schemas.VISIBLE_IN_TREES).isTrue());
		} else {
			query.setCondition(from(rm.folder.schemaType()).where(Schemas.PATH_PARTS).isEqualTo(unit.getId()));
		}
		query.filteredWithUser(user);

		if (write) {
			query.filteredWithUserWrite(user);
		} else {
			query.filteredWithUser(user);
		}

		return searchServices.hasResults(query);
	}
}
