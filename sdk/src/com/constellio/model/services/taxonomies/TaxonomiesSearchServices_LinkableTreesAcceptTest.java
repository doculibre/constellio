/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.taxonomies;

import static com.constellio.app.modules.rm.constants.RMTaxonomies.ADMINISTRATIVE_UNITS;
import static com.constellio.app.modules.rm.constants.RMTaxonomies.CLASSIFICATION_PLAN;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.assertj.core.api.BooleanAssert;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.query.logical.condition.ConditionTemplate;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServicesRuntimeException.TaxonomiesSearchServicesRuntimeException_CannotFilterNonPrincipalConceptWithWriteOrDeleteAccess;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;

public class TaxonomiesSearchServices_LinkableTreesAcceptTest extends ConstellioTest {

	User alice;
	DecommissioningService decommissioningService;
	TaxonomiesSearchServices service;
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTestUsers().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		givenConfig(RMConfigs.LINKABLE_CATEGORY_MUST_NOT_BE_ROOT, false);
		givenConfig(RMConfigs.LINKABLE_CATEGORY_MUST_HAVE_APPROVED_RULES, false);

		givenRule3IsDisabled();

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		service = getModelLayerFactory().newTaxonomiesSearchService();
		decommissioningService = new DecommissioningService(zeCollection, getModelLayerFactory());

		UserServices userServices = getModelLayerFactory().newUserServices();
		UserCredential userCredential = userServices.getUserCredential(aliceWonderland);
		userServices.addUserToCollection(userCredential, zeCollection);
		alice = userServices.getUserInCollection(aliceWonderland, zeCollection);
		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(false));
	}

	@Test
	public void whenGetListOfTaxonomyForRecordSelectionThenReturnValidTaxonomies() {

		TaxonomiesManager taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		Taxonomy storageSpaceTaxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, RMTaxonomies.STORAGES);
		Taxonomy planTaxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, RMTaxonomies.CLASSIFICATION_PLAN);
		Taxonomy unitsTaxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, RMTaxonomies.ADMINISTRATIVE_UNITS);

		assertThatAvailableTaxonomiesForSelectionOf(Folder.SCHEMA_TYPE)
				.containsOnlyOnce(planTaxonomy, unitsTaxonomy);

		assertThatAvailableTaxonomiesForSelectionOf(Document.SCHEMA_TYPE)
				.containsOnlyOnce(planTaxonomy, unitsTaxonomy);

		assertThatAvailableTaxonomiesForSelectionOf(ContainerRecord.SCHEMA_TYPE)
				.containsOnlyOnce(storageSpaceTaxonomy, unitsTaxonomy);

		assertThatAvailableTaxonomiesForSelectionOf(StorageSpace.SCHEMA_TYPE)
				.containsOnlyOnce(storageSpaceTaxonomy);

		assertThatAvailableTaxonomiesForSelectionOf(Category.SCHEMA_TYPE)
				.containsOnlyOnce(planTaxonomy);

		assertThatAvailableTaxonomiesForSelectionOf(AdministrativeUnit.SCHEMA_TYPE)
				.containsOnlyOnce(unitsTaxonomy);

		assertThatAvailableTaxonomiesForSelectionOf(FilingSpace.SCHEMA_TYPE).isEmpty();

		assertThatAvailableTaxonomiesForSelectionOf(FolderType.SCHEMA_TYPE).isEmpty();
	}

	@Test
	public void whenGetListOfTaxonomiesVisibleInHomePageThenReturnOnlyVisiblesInHomePage() {

		String charlesId = records.getCharles_userInA().getId();
		String heroesId = records.getHeroes().getId();
		String legendsId = records.getLegends().getId();

		TaxonomiesManager taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		Taxonomy storageSpaceTaxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, RMTaxonomies.STORAGES);
		Taxonomy planTaxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, RMTaxonomies.CLASSIFICATION_PLAN);
		Taxonomy unitsTaxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, RMTaxonomies.ADMINISTRATIVE_UNITS);

		assertThat(taxonomiesManager.getAvailableTaxonomiesInHomePage(records.getCharles_userInA()))
				.containsOnly(planTaxonomy, unitsTaxonomy);
		assertThat(taxonomiesManager.getAvailableTaxonomiesInHomePage(records.getAdmin()))
				.containsOnly(planTaxonomy, unitsTaxonomy);

		//Disable plan taxonomy + enable storage space taxonomy
		taxonomiesManager.editTaxonomy(planTaxonomy = planTaxonomy.withVisibleInHomeFlag(false));
		taxonomiesManager.editTaxonomy(storageSpaceTaxonomy = storageSpaceTaxonomy.withVisibleInHomeFlag(true));

		assertThat(taxonomiesManager.getAvailableTaxonomiesInHomePage(records.getCharles_userInA()))
				.containsOnly(storageSpaceTaxonomy, unitsTaxonomy);
		assertThat(taxonomiesManager.getAvailableTaxonomiesInHomePage(records.getAdmin())).containsOnly(storageSpaceTaxonomy,
				unitsTaxonomy);

		//Allow charles to view storage taxonomy
		taxonomiesManager.editTaxonomy(storageSpaceTaxonomy = storageSpaceTaxonomy.withUserIds(asList(charlesId)));

		assertThat(taxonomiesManager.getAvailableTaxonomiesInHomePage(records.getCharles_userInA()))
				.containsOnly(storageSpaceTaxonomy, unitsTaxonomy);
		assertThat(taxonomiesManager.getAvailableTaxonomiesInHomePage(records.getAdmin())).containsOnly(unitsTaxonomy);

		//Allow legends to view storage and units taxonomy
		taxonomiesManager.editTaxonomy(unitsTaxonomy = unitsTaxonomy.withGroupIds(asList(legendsId)));

		assertThat(taxonomiesManager.getAvailableTaxonomiesInHomePage(records.getCharles_userInA()))
				.containsOnly(storageSpaceTaxonomy);
		assertThat(taxonomiesManager.getAvailableTaxonomiesInHomePage(records.getAdmin())).isEmpty();

		//Allow heroes to view units taxonomy
		taxonomiesManager.editTaxonomy(unitsTaxonomy = unitsTaxonomy.withGroupIds(asList(heroesId, legendsId)));

		assertThat(taxonomiesManager.getAvailableTaxonomiesInHomePage(records.getCharles_userInA()))
				.containsOnly(storageSpaceTaxonomy, unitsTaxonomy);
		assertThat(taxonomiesManager.getAvailableTaxonomiesInHomePage(records.getAdmin())).isEmpty();

	}

	@Test
	public void givenUserHaveAuthorizationsOnSomeFoldersThenValidTreeForFolderSelectionUsingPlanTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.folder_A18, records.folder_A08);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters)
				.has(numFound(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X)
				.has(numFound(1))
				.has(unlinkable(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X100)
				.has(numFound(1))
				.has(linkable(records.folder_A18))
				.has(itemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.folder_A18)
				.is(empty());

	}

	@Test
	public void givenUserHaveReadAuthorizationsOnSomeFoldersThenValidTreeForFolderSelectionUsingUnitsTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.folder_A18, records.folder_A08);

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters)
				.has(numFound(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_10)
				.has(numFound(1))
				.has(unlinkable(records.unitId_10a))
				.has(itemsWithChildren(records.unitId_10a));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_10a)
				.has(numFound(2))
				.has(linkable(records.folder_A18, records.folder_A08))
				.has(resultsInOrder(records.folder_A08, records.folder_A18))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.folder_A18)
				.is(empty());

	}

	@Test
	public void givenUserHaveReadAndWriteAuthorizationsOnSomeFoldersThenValidTreeForFolderSelectionWithWriteUsingUnitsTaxonomy()
			throws Exception {

		givenUserHasWriteAccessTo(records.folder_A18, records.folder_A08);
		givenUserHasReadAccessTo(records.folder_C02, records.folder_A17);
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, withWriteAccess)
				.has(numFound(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_10, withWriteAccess)
				.has(numFound(1))
				.has(unlinkable(records.unitId_10a))
				.has(itemsWithChildren(records.unitId_10a));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_10a, withWriteAccess)
				.has(numFound(2))
				.has(linkable(records.folder_A18, records.folder_A08))
				.has(resultsInOrder(records.folder_A08, records.folder_A18))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.folder_A18, withWriteAccess)
				.is(empty());

	}

	@Test
	public void givenUserHaveAuthorizationsOnSomeFoldersThenValidTreeForCategorySelectionUsingPlanTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.folder_A18, records.folder_A08);

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters)
				.has(numFound(2))
				.has(linkable(records.categoryId_X))
				.has(unlinkable(records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_X)
				.has(numFound(2))
				.has(linkable(records.categoryId_X100, records.categoryId_X13))
				.has(resultsInOrder(records.categoryId_X100, records.categoryId_X13))
				.has(itemsWithChildren(records.categoryId_X100));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_X100)
				.has(numFound(2))
				.has(linkable(records.categoryId_X110, records.categoryId_X120))
				.has(resultsInOrder(records.categoryId_X110, records.categoryId_X120))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_X110)
				.is(empty());

	}

	@Test
	public void whenSelectingNonPrincipalConceptWithWriteOrDeleteAccessThenException()
			throws Exception {

		givenUserHasReadAccessTo(records.folder_A18, records.folder_A08);
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);
		TaxonomiesSearchOptions withDeleteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.DELETE);

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters);

		try {
			assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, withWriteAccess);
			fail("Exception expected");
		} catch (TaxonomiesSearchServicesRuntimeException_CannotFilterNonPrincipalConceptWithWriteOrDeleteAccess e) {
			//OK
		}

		try {
			assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, withDeleteAccess);
			fail("Exception expected");
		} catch (TaxonomiesSearchServicesRuntimeException_CannotFilterNonPrincipalConceptWithWriteOrDeleteAccess e) {
			//OK
		}

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_X);

		try {
			assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_X, withWriteAccess);
			fail("Exception expected");
		} catch (TaxonomiesSearchServicesRuntimeException_CannotFilterNonPrincipalConceptWithWriteOrDeleteAccess e) {
			//OK
		}

		try {
			assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_X, withDeleteAccess);
			fail("Exception expected");
		} catch (TaxonomiesSearchServicesRuntimeException_CannotFilterNonPrincipalConceptWithWriteOrDeleteAccess e) {
			//OK
		}

	}

	@Test
	public void givenUserHaveAuthorizationsOnSomeFoldersThenEmptyTreeForUnitSelectionUsingUnitTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.folder_A18, records.folder_A08);

		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters)
				.is(empty());

	}

	@Test
	public void givenUserHaveAuthorizationsOnASubFolderThenValidTreeForFolderSelectionUsingCategoryTaxonomy()
			throws Exception {

		Folder subFolder = decommissioningService.newSubFolderIn(records.getFolder_A20()).setTitle("Ze sub folder");
		getModelLayerFactory().newRecordServices().add(subFolder);

		givenUserHasReadAccessTo(subFolder.getId());

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters)
				.has(numFound(1))
				.has(unlinkable(records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z)
				.has(numFound(1))
				.has(unlinkable(records.categoryId_Z100))
				.has(itemsWithChildren(records.categoryId_Z100));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z100)
				.has(numFound(1))
				.has(unlinkable(records.categoryId_Z120))
				.has(itemsWithChildren(records.categoryId_Z120));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z120)
				.has(numFound(1))
				.has(unlinkable(records.folder_A20));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.folder_A20)
				.has(numFound(1))
				.has(linkable(subFolder.getId()))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, subFolder.getId())
				.is(empty());

	}

	@Test
	public void givenUserHaveWriteAuthorizationsOnASubFolderThenValidTreeForFolderSelectionUsingCategoryTaxonomy()
			throws Exception {

		Folder subFolder1 = decommissioningService.newSubFolderIn(records.getFolder_A20()).setTitle("Ze sub folder");
		Folder subFolder2 = decommissioningService.newSubFolderIn(records.getFolder_A20()).setTitle("Ze sub folder");
		getModelLayerFactory().newRecordServices().execute(new Transaction().addAll(subFolder1, subFolder2));

		givenUserHasReadAccessTo(records.folder_A20, records.folder_C01);
		givenUserHasWriteAccessTo(subFolder2.getId());
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters)
				.has(numFound(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, withWriteAccess)
				.has(numFound(1))
				.has(unlinkable(records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.folder_A20)
				.has(numFound(2))
				.has(linkable(subFolder1.getId(), subFolder2.getId()))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.folder_A20, withWriteAccess)
				.has(numFound(1))
				.has(linkable(subFolder2.getId()))
				.has(noItemsWithChildren());

	}

	@Test
	public void givenUserHaveAuthorizationsOnASubFolderThenValidTreeForFolderSelectionUsingUnitTaxonomy()
			throws Exception {

		Folder subFolder = decommissioningService.newSubFolderIn(records.getFolder_A20()).setTitle("Ze sub folder");
		getModelLayerFactory().newRecordServices().add(subFolder);

		givenUserHasReadAccessTo(subFolder.getId());

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters)
				.has(numFound(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_10)
				.has(numFound(1))
				.has(unlinkable(records.unitId_10a))
				.has(itemsWithChildren(records.unitId_10a));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_10a)
				.has(numFound(1))
				.has(unlinkable(records.folder_A20))
				.has(itemsWithChildren(records.folder_A20));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.folder_A20)
				.has(numFound(1))
				.has(linkable(subFolder.getId()))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, subFolder.getId())
				.is(empty());
	}

	@Test
	public void givenUserHaveAuthorizationsOnAnAdministrativeUnitThenValidTreeForFolderSelectionUsingCategoryTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.unitId_12);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters)
				.has(numFound(1))
				.has(unlinkable(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X)
				.has(numFound(1))
				.has(unlinkable(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X100)
				.has(numFound(5))
				.has(unlinkable(records.categoryId_X110, records.categoryId_X120))
				.has(linkable(records.folder_B06, records.folder_B32, records.folder_B52))
				.has(resultsInOrder("categoryId_X110", "categoryId_X120", "B52", "B06", "B32"))
				.has(itemsWithChildren("categoryId_X110", "categoryId_X120"));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X110)
				.has(numFound(4))
				.has(linkable(records.folder_B02, records.folder_B04, records.folder_B30, records.folder_B50))
				.has(resultsInOrder(records.folder_B02, records.folder_B04, records.folder_B30, records.folder_B50))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.folder_B02)
				.is(empty());
	}

	@Test
	public void givenUserHaveAuthorizationsOnAnAdministrativeUnitThenValidTreeForFolderSelectionUsingUnitTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.unitId_12);

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters)
				.has(numFound(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_10)
				.has(numFound(1))
				.has(unlinkable(records.unitId_12))
				.has(itemsWithChildren(records.unitId_12));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_12)
				.has(numFound(1))
				.has(unlinkable(records.unitId_12b))
				.has(itemsWithChildren(records.unitId_12b));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_12b)
				.has(numFound(10))
				.has(linkable(records.folder_B02, records.folder_B04, records.folder_B06, records.folder_B08, records.folder_B30,
						records.folder_B32, records.folder_B34, records.folder_B50, records.folder_B52, records.folder_B54))
				.has(resultsInOrder("B52", "B02", "B04", "B06", "B08", "B54", "B30", "B32", "B34", "B50"))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.folder_B02)
				.is(empty());

	}

	@Test
	public void givenUserHaveAuthorizationsOnAnAdministrativeUnitThenAllCategoriesTreeVisibleForCategorySelection()
			throws Exception {

		givenUserHasReadAccessTo(records.unitId_12);

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters)
				.has(numFound(2))
				.has(linkable(records.categoryId_X))
				.has(unlinkable(records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z)
				.has(numFound(4))
				.has(unlinkable(records.categoryId_Z100, records.categoryId_Z200))
				.has(linkable(records.categoryId_ZE42, records.categoryId_Z999))
				.has(resultsInOrder(records.categoryId_Z100, records.categoryId_Z200, records.categoryId_Z999,
						records.categoryId_ZE42))
				.has(itemsWithChildren(records.categoryId_Z100));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z100)
				.has(numFound(2))
				.has(linkable(records.categoryId_Z110, records.categoryId_Z120))
				.has(resultsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(itemsWithChildren(records.categoryId_Z110));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z110)
				.has(numFound(2))
				.has(unlinkable(records.categoryId_Z111))
				.has(linkable(records.categoryId_Z112))
				.has(resultsInOrder(records.categoryId_Z111, records.categoryId_Z112))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z112)
				.is(empty());

	}

	@Test
	public void givenUserHaveAuthorizationsOnAnAdministrativeUnitThenValidTreeForUnitSelectionUsingUnitTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.unitId_12);

		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters)
				.has(numFound(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_10)
				.has(numFound(1))
				.has(linkable(records.unitId_12))
				.has(itemsWithChildren(records.unitId_12));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_12)
				.has(numFound(2))
				.has(linkable(records.unitId_12b, records.unitId_12c))
				.has(resultsInOrder(records.unitId_12b, records.unitId_12c))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_12b)
				.is(empty());

	}

	@Test
	public void givenUserReadAndWriteHaveAuthorizationsOnAnAdministrativeUnitThenValidTreeForUnitSelectionWithWriteAccessUsingUnitTaxonomy()
			throws Exception {

		givenUserHasWriteAccessTo(records.unitId_12);
		givenUserHasReadAccessTo(records.unitId_11, records.unitId_20);
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, withWriteAccess)
				.has(numFound(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_10, withWriteAccess)
				.has(numFound(1))
				.has(linkable(records.unitId_12))
				.has(itemsWithChildren(records.unitId_12));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_12, withWriteAccess)
				.has(numFound(2))
				.has(linkable(records.unitId_12b, records.unitId_12c))
				.has(resultsInOrder(records.unitId_12b, records.unitId_12c))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_12b, withWriteAccess)
				.is(empty());

	}

	@Test
	public void givenUserHaveSiteReadAccessForAllTreeVisibleForFolderSelectionUsingCategoryTaxonomy()
			throws Exception {

		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters)
				.has(numFound(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, withWriteAccess)
				.is(empty());

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X)
				.has(numFound(1))
				.has(unlinkable(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X, withWriteAccess)
				.is(empty());

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X100)
				.has(unlinkable(records.categoryId_X110, records.categoryId_X120))
				.has(linkable("A16", "A17", "A18", "A48", "A49", "A50", "A85", "A86", "A87", "B06", "B32", "B52", "C06", "C32",
						"C52"))
				.has(resultsInOrder("categoryId_X110", "categoryId_X120", "B52", "A16", "A17", "A18", "C06", "B06", "A48", "A49",
						"A50", "C32", "A85", "B32", "A86", "A87", "C52"))
				.has(itemsWithChildren("categoryId_X110", "categoryId_X120"));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.folder_A18)
				.is(empty());

	}

	@Test
	public void givenUserHaveSiteWriteAccessForAllTreeVisibleForFolderSelectionUsingCategoryTaxonomy()
			throws Exception {

		getModelLayerFactory().newRecordServices().update(alice.setCollectionWriteAccess(true));
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, withWriteAccess)
				.has(numFound(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X, withWriteAccess)
				.has(numFound(1))
				.has(unlinkable(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X100, withWriteAccess)
				.has(unlinkable(records.categoryId_X110, records.categoryId_X120))
				.has(linkable("A16", "A17", "A18", "A48", "A49", "A50", "A85", "A86", "A87", "B06", "B32", "B52", "C06", "C32",
						"C52"))
				.has(resultsInOrder("categoryId_X110", "categoryId_X120", "B52", "A16", "A17", "A18", "C06", "B06", "A48", "A49",
						"A50", "C32", "A85", "B32", "A86", "A87", "C52"))
				.has(itemsWithChildren("categoryId_X110", "categoryId_X120"));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.folder_A18, withWriteAccess)
				.is(empty());

	}

	@Test
	public void givenUserHaveSiteReadAccessForAllTreeVisibleForFolderSelectionUsingUnitTaxonomy()
			throws Exception {

		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters)
				.has(numFound(2))
				.has(unlinkable(records.unitId_10, records.unitId_30))
				.has(resultsInOrder(records.unitId_10, records.unitId_30))
				.has(itemsWithChildren(records.unitId_10, records.unitId_30));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_10)
				.has(unlinkable(records.unitId_11, records.unitId_12, records.unitId_10a))
				.has(resultsInOrder(records.unitId_10a, records.unitId_11, records.unitId_12))
				.has(itemsWithChildren(records.unitId_10a, records.unitId_11, records.unitId_12));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_10a)
				.has(linkable(records.folder_A42, records.folder_A43, records.folder_A44))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_12)
				.has(numFound(1))
				.has(unlinkable(records.unitId_12b))
				.has(itemsWithChildren(records.unitId_12b));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_12b)
				.has(numFound(10))
				.has(linkable(records.folder_B02, records.folder_B04, records.folder_B06, records.folder_B08, records.folder_B30,
						records.folder_B32, records.folder_B34, records.folder_B50, records.folder_B52, records.folder_B54))
				.has(resultsInOrder("B52", "B02", "B04", "B06", "B08", "B54", "B30", "B32", "B34", "B50"))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.folder_B02)
				.is(empty());

	}

	@Test
	public void givenUserHaveSiteReadAccessForAllTreeVisibleForCategorySelectionUsingPlanTaxonomy()
			throws Exception {
		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters)
				.has(numFound(2))
				.has(linkable(records.categoryId_X))
				.has(unlinkable(records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z)
				.has(numFound(4))
				.has(unlinkable(records.categoryId_Z100, records.categoryId_Z200))
				.has(linkable(records.categoryId_ZE42, records.categoryId_Z999))
				.has(resultsInOrder(records.categoryId_Z100, records.categoryId_Z200, records.categoryId_Z999,
						records.categoryId_ZE42))
				.has(itemsWithChildren(records.categoryId_Z100));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z100)
				.has(numFound(2))
				.has(linkable(records.categoryId_Z110, records.categoryId_Z120))
				.has(resultsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(itemsWithChildren(records.categoryId_Z110));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z112)
				.is(empty());

	}

	@Test
	public void givenUserHaveSiteReadAccessForAllTreeVisibleForUnitSelectionUsingUnitTaxonomy()
			throws Exception {
		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters)
				.has(numFound(3))
				.has(linkable(records.unitId_10, records.unitId_20, records.unitId_30))
				.has(resultsInOrder(records.unitId_10, records.unitId_20, records.unitId_30))
				.has(itemsWithChildren(records.unitId_10, records.unitId_20, records.unitId_30));
		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, withWriteAccess)
				.is(empty());

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_10)
				.has(numFound(3))
				.has(linkable(records.unitId_11, records.unitId_12, records.unitId_10a))
				.has(resultsInOrder(records.unitId_10a, records.unitId_11, records.unitId_12))
				.has(itemsWithChildren(records.unitId_11, records.unitId_12));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_10, withWriteAccess)
				.is(empty());

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_12)
				.has(numFound(2))
				.has(linkable(records.unitId_12b, records.unitId_12c))
				.has(resultsInOrder(records.unitId_12b, records.unitId_12c))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_12b)
				.is(empty());

	}

	@Test
	public void givenUserHaveSiteWriteAccessForAllTreeVisibleForUnitSelectionWithWriteAccessUsingUnitTaxonomy()
			throws Exception {
		getModelLayerFactory().newRecordServices().update(alice.setCollectionWriteAccess(true));
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, withWriteAccess)
				.has(numFound(3))
				.has(linkable(records.unitId_10, records.unitId_20, records.unitId_30))
				.has(resultsInOrder(records.unitId_10, records.unitId_20, records.unitId_30))
				.has(itemsWithChildren(records.unitId_10, records.unitId_20, records.unitId_30));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_10, withWriteAccess)
				.has(numFound(3))
				.has(linkable(records.unitId_11, records.unitId_12, records.unitId_10a))
				.has(resultsInOrder(records.unitId_10a, records.unitId_11, records.unitId_12))
				.has(itemsWithChildren(records.unitId_11, records.unitId_12));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_12, withWriteAccess)
				.has(numFound(2))
				.has(linkable(records.unitId_12b, records.unitId_12c))
				.has(resultsInOrder(records.unitId_12b, records.unitId_12c))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_12b, withWriteAccess)
				.is(empty());

	}

	@Test
	public void givenLinkableCategoryCannotBeRootAndMustHaveApprovedRulesThenTheseCategoriesUnlinkable()
			throws Exception {
		givenConfig(RMConfigs.LINKABLE_CATEGORY_MUST_NOT_BE_ROOT, true);
		givenConfig(RMConfigs.LINKABLE_CATEGORY_MUST_HAVE_APPROVED_RULES, true);
		waitForBatchProcess();

		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters)
				.has(numFound(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z)
				.has(numFound(4))
				.has(linkable(records.categoryId_ZE42))
				.has(unlinkable(records.categoryId_Z100, records.categoryId_Z200, records.categoryId_Z999))
				.has(resultsInOrder(records.categoryId_Z100, records.categoryId_Z200, records.categoryId_Z999,
						records.categoryId_ZE42))
				.has(itemsWithChildren(records.categoryId_Z100));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z100)
				.has(numFound(2))
				.has(linkable(records.categoryId_Z110))
				.has(unlinkable(records.categoryId_Z120))
				.has(resultsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(itemsWithChildren(records.categoryId_Z110));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z110)
				.has(numFound(2))
				.has(unlinkable(records.categoryId_Z111, records.categoryId_Z112))
				.has(resultsInOrder(records.categoryId_Z111, records.categoryId_Z112))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z112)
				.is(empty());

	}

	// -------

	private Condition<? super LinkableTaxonomySearchResponse> empty() {
		return numFound(0);
	}

	private Condition<? super LinkableTaxonomySearchResponse> numFound(final int expectedCount) {
		return new Condition<LinkableTaxonomySearchResponse>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponse value) {
				assertThat(value.getNumFound()).describedAs("NumFound").isEqualTo(expectedCount);
				assertThat(value.getRecords().size()).describedAs("records list size").isEqualTo(expectedCount);
				return true;
			}
		};
	}

	private Condition<? super LinkableTaxonomySearchResponse> validOrder() {
		return new Condition<LinkableTaxonomySearchResponse>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponse response) {

				List<Record> actualRecords = new ArrayList<>();
				List<Record> recordsInExpectedOrder = new ArrayList<>();

				for (TaxonomySearchRecord value : response.getRecords()) {
					actualRecords.add(value.getRecord());
					recordsInExpectedOrder.add(value.getRecord());
				}

				final List<String> typesOrder = asList(Category.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE,
						ContainerRecord.SCHEMA_TYPE, Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE);

				Collections.sort(recordsInExpectedOrder, new Comparator<Record>() {
					@Override
					public int compare(Record r1, Record r2) {

						int r1TypeIndex = typesOrder.indexOf(new SchemaUtils().getSchemaTypeCode(r1.getSchemaCode()));
						int r2TypeIndex = typesOrder.indexOf(new SchemaUtils().getSchemaTypeCode(r2.getSchemaCode()));

						if (r1TypeIndex != r2TypeIndex) {
							return new Integer(r1TypeIndex).compareTo(r2TypeIndex);

						} else {
							String code1 = r1.get(Schemas.CODE);
							String code2 = r2.get(Schemas.CODE);
							if (code1 != null && code2 != null) {
								return code1.compareTo(code2);

							} else if (code1 != null && code2 == null) {
								return 1;
							} else if (code1 == null && code2 != null) {
								return -1;
							} else {

								String title1 = r1.get(Schemas.TITLE);
								String title2 = r2.get(Schemas.TITLE);
								if (title1 == null) {
									return -1;
								} else {
									return title1.compareTo(title2);
								}
							}

						}

					}
				});

				assertThat(actualRecords).isEqualTo(recordsInExpectedOrder);
				return true;
			}
		};
	}

	private Condition<? super LinkableTaxonomySearchResponse> noItemsWithChildren() {
		return itemsWithChildren();
	}

	private Condition<? super LinkableTaxonomySearchResponse> itemsWithChildren(final String... ids) {
		final List<String> idsList = asList(ids);
		return new Condition<LinkableTaxonomySearchResponse>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponse response) {

				for (TaxonomySearchRecord record : response.getRecords()) {
					BooleanAssert assertion = assertThat(record.hasChildren()).describedAs(
							"Record '" + record.getRecord().getId() + "' has children");
					if (idsList.contains(record.getRecord().getId())) {
						assertion.isTrue();
					} else {
						assertion.isFalse();
					}
				}

				return true;
			}
		};
	}

	private Condition<? super LinkableTaxonomySearchResponse> unlinkable(final String... ids) {
		return new Condition<LinkableTaxonomySearchResponse>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponse response) {

				for (String id : ids) {
					TaxonomySearchRecord foundRecord = null;
					for (TaxonomySearchRecord record : response.getRecords()) {
						if (id.equals(record.getRecord().getId())) {
							if (foundRecord != null) {
								throw new RuntimeException("Same record found twice");
							}
							foundRecord = record;
						}
					}
					assertThat(foundRecord).describedAs("Result '" + id + "'").isNotNull();
					assertThat(foundRecord.isLinkable()).describedAs("is record '" + id + "' linkable").isFalse();

				}

				return true;
			}
		}.describedAs("unlinkable " + ids);
	}

	private Condition<? super LinkableTaxonomySearchResponse> linkable(final String... ids) {
		return new Condition<LinkableTaxonomySearchResponse>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponse response) {

				for (String id : ids) {
					TaxonomySearchRecord foundRecord = null;
					for (TaxonomySearchRecord record : response.getRecords()) {
						if (id.equals(record.getRecord().getId())) {
							if (foundRecord != null) {
								throw new RuntimeException("Same record found twice");
							}
							foundRecord = record;
						}
					}
					assertThat(foundRecord).describedAs("Result '" + id + "'").isNotNull();
					assertThat(foundRecord.isLinkable()).describedAs("is record '" + id + "' linkable").isTrue();

				}

				return true;
			}
		}.describedAs("linkable " + ids);
	}

	private Condition<? super LinkableTaxonomySearchResponse> resultsInOrder(final String... ids) {
		return new Condition<LinkableTaxonomySearchResponse>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponse response) {

				List<String> responseRecords = new ArrayList();
				List<String> codeOrTitle = new ArrayList<>();
				for (TaxonomySearchRecord record : response.getRecords()) {
					responseRecords.add(record.getRecord().getId());
					if (record.getRecord().get(Schemas.CODE) == null) {
						codeOrTitle.add((String) record.getRecord().get(Schemas.TITLE));
					} else {
						codeOrTitle.add((String) record.getRecord().get(Schemas.CODE));
					}
				}

				assertThat(responseRecords).describedAs("Results in correct order : " + codeOrTitle).isEqualTo(asList(ids));

				return true;
			}
		};
	}

	private void givenUserHasReadAccessTo(String... ids) {

		Authorization authorization = new Authorization();
		authorization.setDetail(AuthorizationDetails.create("zeAuthorization", asList(Role.READ), zeCollection));
		authorization.setGrantedOnRecords(asList(ids));
		authorization.setGrantedToPrincipals(asList(alice.getId()));
		getModelLayerFactory().newAuthorizationsServices().add(
				authorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);

		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
		alice = getModelLayerFactory().newUserServices().getUserInCollection(aliceWonderland, zeCollection);
		System.out.println(alice.getTokens());
		System.out.println(alice.getTokens());
	}

	private void givenUserHasWriteAccessTo(String... ids) {
		Authorization authorization = new Authorization();
		authorization.setDetail(AuthorizationDetails.create("writeAuthorization", asList(Role.WRITE), zeCollection));
		authorization.setGrantedOnRecords(asList(ids));
		authorization.setGrantedToPrincipals(asList(alice.getId()));
		getModelLayerFactory().newAuthorizationsServices().add(
				authorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);

		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
		alice = getModelLayerFactory().newUserServices().getUserInCollection(aliceWonderland, zeCollection);
		System.out.println(alice.getTokens());
		System.out.println(alice.getTokens());
	}

	private ConditionTemplate withoutFilters = null;

	private ListAssert<Taxonomy> assertThatAvailableTaxonomiesForSelectionOf(String schemaTypeCode) {
		return assertThat(
				getModelLayerFactory().getTaxonomiesManager().getAvailableTaxonomiesForSelectionOfType(
						schemaTypeCode, alice, getModelLayerFactory().getMetadataSchemasManager()));
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(
			ConditionTemplate template) {
		return assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(template, new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(
			ConditionTemplate template,
			TaxonomiesSearchOptions options) {
		return assertThat(
				service.getLinkableRootConceptResponse(alice, zeCollection, CLASSIFICATION_PLAN, Folder.SCHEMA_TYPE, options));
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(
			ConditionTemplate template,
			String category) {
		return assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(template, category, new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(
			ConditionTemplate template,
			String category, TaxonomiesSearchOptions options) {
		Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
		LinkableTaxonomySearchResponse response = service.getLinkableChildConceptResponse(alice, inRecord,
				RMTaxonomies.CLASSIFICATION_PLAN, Folder.SCHEMA_TYPE, options);
		return assertThat(response);
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(
			ConditionTemplate template) {
		return assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(template, new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(ConditionTemplate
			template, TaxonomiesSearchOptions options) {
		return assertThat(
				service.getLinkableRootConceptResponse(alice, zeCollection, CLASSIFICATION_PLAN, Category.SCHEMA_TYPE, options));
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(ConditionTemplate
			template, String category) {
		return assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(template, category, new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(
			ConditionTemplate
					template, String category, TaxonomiesSearchOptions taxonomiesSearchOptions) {
		Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
		return assertThat(service.getLinkableChildConceptResponse(alice, inRecord, RMTaxonomies.CLASSIFICATION_PLAN,
				Category.SCHEMA_TYPE, taxonomiesSearchOptions));
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(
			ConditionTemplate template) {
		return assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(template, new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(
			ConditionTemplate template,
			TaxonomiesSearchOptions options) {

		LinkableTaxonomySearchResponse response = service
				.getLinkableRootConceptResponse(alice, zeCollection, ADMINISTRATIVE_UNITS, Folder.SCHEMA_TYPE, options);

		return assertThat(response);
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(
			ConditionTemplate template,
			String admUnit) {
		return assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(template, admUnit, new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(
			ConditionTemplate template,
			String admUnit, TaxonomiesSearchOptions options) {
		Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(admUnit);
		LinkableTaxonomySearchResponse response = service
				.getLinkableChildConceptResponse(alice, inRecord, RMTaxonomies.ADMINISTRATIVE_UNITS, Folder.SCHEMA_TYPE, options);
		return assertThat(response);
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(
			ConditionTemplate template) {
		return assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(template, new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(
			ConditionTemplate template, TaxonomiesSearchOptions options) {
		return assertThat(
				service.getLinkableRootConceptResponse(alice, zeCollection, ADMINISTRATIVE_UNITS, AdministrativeUnit.SCHEMA_TYPE,
						options));
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(
			ConditionTemplate template, String admUnit) {
		return assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(template, admUnit,
				new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(
			ConditionTemplate template, String admUnit, TaxonomiesSearchOptions options) {
		Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(admUnit);
		return assertThat(service.getLinkableChildConceptResponse(alice, inRecord, RMTaxonomies.ADMINISTRATIVE_UNITS,
				AdministrativeUnit.SCHEMA_TYPE, options));
	}

	private void givenRule3IsDisabled() {
		RetentionRule rule3 = records.getRule3();
		rule3.setApproved(false);
		try {
			getModelLayerFactory().newRecordServices().update(rule3);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}
}
