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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
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
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;

public class TaxonomiesSearchServices_LinkableTreesAcceptTest extends ConstellioTest {

	User alice;
	DecommissioningService decommissioningService;
	TaxonomiesSearchServices service;
	RMSchemasRecordsServices rm;
	RMTestRecords test = new RMTestRecords(zeCollection);

	@Before
	public void setUp()
			throws Exception {

		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		givenConfig(RMConfigs.LINKABLE_CATEGORY_MUST_NOT_BE_ROOT, false);
		givenConfig(RMConfigs.LINKABLE_CATEGORY_MUST_HAVE_APPROVED_RULES, false);

		test.setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();
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

		String charlesId = test.getCharles_userInA().getId();
		String heroesId = test.getHeroes().getId();
		String legendsId = test.getLegends().getId();

		TaxonomiesManager taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		Taxonomy storageSpaceTaxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, RMTaxonomies.STORAGES);
		Taxonomy planTaxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, RMTaxonomies.CLASSIFICATION_PLAN);
		Taxonomy unitsTaxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, RMTaxonomies.ADMINISTRATIVE_UNITS);

		assertThat(taxonomiesManager.getAvailableTaxonomiesInHomePage(test.getCharles_userInA()))
				.containsOnly(planTaxonomy, unitsTaxonomy);
		assertThat(taxonomiesManager.getAvailableTaxonomiesInHomePage(test.getAdmin())).containsOnly(planTaxonomy, unitsTaxonomy);

		//Disable plan taxonomy + enable storage space taxonomy
		taxonomiesManager.editTaxonomy(planTaxonomy = planTaxonomy.withVisibleInHomeFlag(false));
		taxonomiesManager.editTaxonomy(storageSpaceTaxonomy = storageSpaceTaxonomy.withVisibleInHomeFlag(true));

		assertThat(taxonomiesManager.getAvailableTaxonomiesInHomePage(test.getCharles_userInA()))
				.containsOnly(storageSpaceTaxonomy, unitsTaxonomy);
		assertThat(taxonomiesManager.getAvailableTaxonomiesInHomePage(test.getAdmin())).containsOnly(storageSpaceTaxonomy,
				unitsTaxonomy);

		//Allow charles to view storage taxonomy
		taxonomiesManager.editTaxonomy(storageSpaceTaxonomy = storageSpaceTaxonomy.withUserIds(asList(charlesId)));

		assertThat(taxonomiesManager.getAvailableTaxonomiesInHomePage(test.getCharles_userInA()))
				.containsOnly(storageSpaceTaxonomy, unitsTaxonomy);
		assertThat(taxonomiesManager.getAvailableTaxonomiesInHomePage(test.getAdmin())).containsOnly(unitsTaxonomy);

		//Allow legends to view storage and units taxonomy
		taxonomiesManager.editTaxonomy(unitsTaxonomy = unitsTaxonomy.withGroupIds(asList(legendsId)));

		assertThat(taxonomiesManager.getAvailableTaxonomiesInHomePage(test.getCharles_userInA()))
				.containsOnly(storageSpaceTaxonomy);
		assertThat(taxonomiesManager.getAvailableTaxonomiesInHomePage(test.getAdmin())).isEmpty();

		//Allow heroes to view units taxonomy
		taxonomiesManager.editTaxonomy(unitsTaxonomy = unitsTaxonomy.withGroupIds(asList(heroesId, legendsId)));

		assertThat(taxonomiesManager.getAvailableTaxonomiesInHomePage(test.getCharles_userInA()))
				.containsOnly(storageSpaceTaxonomy, unitsTaxonomy);
		assertThat(taxonomiesManager.getAvailableTaxonomiesInHomePage(test.getAdmin())).isEmpty();

	}

	@Test
	public void givenUserHaveAuthorizationsOnSomeFoldersThenValidTreeForFolderSelectionUsingPlanTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(test.folder_A18, test.folder_A08);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters)
				.hasSize(2)
				.has(unlinkable(test.categoryId_X, test.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, test.categoryId_X)
				.hasSize(1)
				.has(unlinkable(test.categoryId_X100));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, test.categoryId_X100)
				.hasSize(1)
				.has(linkable(test.folder_A18));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, test.folder_A18)
				.isEmpty();

	}

	@Test
	public void givenUserHaveAuthorizationsOnSomeFoldersThenValidTreeForFolderSelectionUsingUnitsTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(test.folder_A18, test.folder_A08);

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters)
				.hasSize(1)
				.has(unlinkable(test.unitId_10));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, test.unitId_10)
				.hasSize(2)
				.has(linkable(test.folder_A18, test.folder_A08));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, test.folder_A18)
				.isEmpty();

	}

	@Test
	public void givenUserHaveAuthorizationsOnSomeFoldersThenValidTreeForCategorySelectionUsingPlanTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(test.folder_A18, test.folder_A08);

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters)
				.hasSize(2)
				.has(linkable(test.categoryId_X))
				.has(unlinkable(test.categoryId_Z));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, test.categoryId_X)
				.hasSize(2)
				.has(linkable(test.categoryId_X100, test.categoryId_X13));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, test.categoryId_X100)
				.hasSize(2)
				.has(linkable(test.categoryId_X110, test.categoryId_X120));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, test.categoryId_X110)
				.isEmpty();

	}

	@Test
	public void givenUserHaveAuthorizationsOnSomeFoldersThenEmptyTreeForUnitSelectionUsingUnitTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(test.folder_A18, test.folder_A08);

		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters)
				.isEmpty();

	}

	@Test
	public void givenUserHaveAuthorizationsOnASubFolderThenValidTreeForFolderSelectionUsingCategoryTaxonomy()
			throws Exception {

		Folder subFolder = decommissioningService.newSubFolderIn(test.getFolder_A20()).setTitle("Ze sub folder");
		getModelLayerFactory().newRecordServices().add(subFolder);

		givenUserHasReadAccessTo(subFolder.getId());

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters)
				.hasSize(1)
				.has(unlinkable(test.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, test.categoryId_Z)
				.hasSize(1)
				.has(unlinkable(test.categoryId_Z100));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, test.categoryId_Z100)
				.hasSize(1)
				.has(unlinkable(test.categoryId_Z120));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, test.categoryId_Z120)
				.hasSize(1)
				.has(unlinkable(test.folder_A20));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, test.folder_A20)
				.hasSize(1)
				.has(linkable(subFolder.getId()));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, subFolder.getId())
				.isEmpty();

	}

	@Test
	public void givenUserHaveAuthorizationsOnASubFolderThenValidTreeForFolderSelectionUsingUnitTaxonomy()
			throws Exception {

		Folder subFolder = decommissioningService.newSubFolderIn(test.getFolder_A20()).setTitle("Ze sub folder");
		getModelLayerFactory().newRecordServices().add(subFolder);

		givenUserHasReadAccessTo(subFolder.getId());

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters)
				.hasSize(1)
				.has(unlinkable(test.unitId_10));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, test.unitId_10)
				.hasSize(1)
				.has(unlinkable(test.folder_A20));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, test.folder_A20)
				.hasSize(1)
				.has(linkable(subFolder.getId()));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, subFolder.getId())
				.isEmpty();
	}

	@Test
	public void givenUserHaveAuthorizationsOnAnAdministrativeUnitThenValidTreeForFolderSelectionUsingCategoryTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(test.unitId_12);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters)
				.hasSize(1)
				.has(unlinkable(test.categoryId_X));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, test.categoryId_X)
				.hasSize(1)
				.has(unlinkable(test.categoryId_X100));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, test.categoryId_X100)
				.hasSize(5)
				.has(unlinkable(test.categoryId_X110, test.categoryId_X120))
				.has(linkable(test.folder_B06, test.folder_B32, test.folder_B52));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, test.categoryId_X110)
				.hasSize(4)
				.has(linkable(test.folder_B02, test.folder_B04, test.folder_B30, test.folder_B50));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, test.folder_B02)
				.isEmpty();
	}

	@Test
	public void givenUserHaveAuthorizationsOnAnAdministrativeUnitThenValidTreeForFolderSelectionUsingUnitTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(test.unitId_12);

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters)
				.hasSize(1)
				.has(unlinkable(test.unitId_10));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, test.unitId_10)
				.hasSize(1)
				.has(unlinkable(test.unitId_12));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, test.unitId_12)
				.hasSize(10)
				.has(linkable(test.folder_B02, test.folder_B04, test.folder_B06, test.folder_B08, test.folder_B30,
						test.folder_B32, test.folder_B34, test.folder_B50, test.folder_B52, test.folder_B54));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, test.folder_B02)
				.isEmpty();

	}

	@Test
	public void givenUserHaveAuthorizationsOnAnAdministrativeUnitThenAllCategoriesTreeVisibleForCategorySelection()
			throws Exception {

		givenUserHasReadAccessTo(test.unitId_12);

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters)
				.hasSize(2)
				.has(linkable(test.categoryId_X))
				.has(unlinkable(test.categoryId_Z));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, test.categoryId_Z)
				.hasSize(3)
				.has(unlinkable(test.categoryId_Z100, test.categoryId_Z200))
				.has(linkable(test.categoryId_ZE42));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, test.categoryId_Z100)
				.hasSize(2)
				.has(linkable(test.categoryId_Z110, test.categoryId_Z120));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, test.categoryId_Z110)
				.hasSize(2)
				.has(unlinkable(test.categoryId_Z111))
				.has(linkable(test.categoryId_Z112));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, test.categoryId_Z112)
				.isEmpty();

	}

	@Test
	public void givenUserHaveAuthorizationsOnAnAdministrativeUnitThenValidTreeForUnitSelectionUsingUnitTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(test.unitId_12);

		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters)
				.hasSize(1)
				.has(unlinkable(test.unitId_10));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, test.unitId_10)
				.hasSize(1)
				.has(linkable(test.unitId_12));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, test.unitId_12)
				.isEmpty();

	}

	@Test
	public void givenUserHaveSiteReadAccessForAllTreeVisibleForFolderSelectionUsingCategoryTaxonomy()
			throws Exception {

		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters)
				.hasSize(2)
				.has(unlinkable(test.categoryId_X, test.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, test.categoryId_X)
				.hasSize(1)
				.has(unlinkable(test.categoryId_X100));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, test.categoryId_X100)
				.has(unlinkable(test.categoryId_X110, test.categoryId_X120))
				.has(linkable(test.folder_A17, test.folder_A18));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, test.folder_A18)
				.isEmpty();

	}

	@Test
	public void givenUserHaveSiteReadAccessForAllTreeVisibleForFolderSelectionUsingUnitTaxonomy()
			throws Exception {

		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters)
				.hasSize(2)
				.has(unlinkable(test.unitId_10, test.unitId_30));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, test.unitId_10)
				.has(unlinkable(test.unitId_11, test.unitId_12))
				.has(linkable(test.folder_A42, test.folder_A43, test.folder_A44));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, test.unitId_12)
				.hasSize(10)
				.has(linkable(test.folder_B02, test.folder_B04, test.folder_B06, test.folder_B08, test.folder_B30,
						test.folder_B32, test.folder_B34, test.folder_B50, test.folder_B52, test.folder_B54));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, test.folder_B02)
				.isEmpty();

	}

	@Test
	public void givenUserHaveSiteReadAccessForAllTreeVisibleForCategorySelectionUsingPlanTaxonomy()
			throws Exception {
		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters)
				.hasSize(2)
				.has(linkable(test.categoryId_X))
				.has(unlinkable(test.categoryId_Z));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, test.categoryId_Z)
				.hasSize(3)
				.has(unlinkable(test.categoryId_Z100, test.categoryId_Z200))
				.has(linkable(test.categoryId_ZE42));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, test.categoryId_Z100)
				.hasSize(2)
				.has(linkable(test.categoryId_Z110, test.categoryId_Z120));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, test.categoryId_Z112)
				.isEmpty();

	}

	@Test
	public void givenUserHaveSiteReadAccessForAllTreeVisibleForUnitSelectionUsingUnitTaxonomy()
			throws Exception {
		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));

		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters)
				.hasSize(3)
				.has(linkable(test.unitId_10, test.unitId_20, test.unitId_30));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, test.unitId_10)
				.hasSize(2)
				.has(linkable(test.unitId_11, test.unitId_12));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, test.unitId_12)
				.isEmpty();

	}

	@Test
	public void givenLinkableCategoryCannotBeRootAndMustHaveApprovedRulesThenTheseCategoriesUnlinkable()
			throws Exception {
		givenConfig(RMConfigs.LINKABLE_CATEGORY_MUST_NOT_BE_ROOT, true);
		givenConfig(RMConfigs.LINKABLE_CATEGORY_MUST_HAVE_APPROVED_RULES, true);
		waitForBatchProcess();

		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters)
				.hasSize(2)
				.has(unlinkable(test.categoryId_X, test.categoryId_Z));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, test.categoryId_Z)
				.hasSize(3)
				.has(linkable(test.categoryId_ZE42))
				.has(unlinkable(test.categoryId_Z100, test.categoryId_Z200));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, test.categoryId_Z100)
				.hasSize(2)
				.has(linkable(test.categoryId_Z110))
				.has(unlinkable(test.categoryId_Z120));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, test.categoryId_Z110)
				.hasSize(2)
				.has(unlinkable(test.categoryId_Z111, test.categoryId_Z112));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, test.categoryId_Z112)
				.isEmpty();

	}

	// -------

	private Condition<? super List<TaxonomySearchRecord>> validOrder() {
		return new Condition<List<TaxonomySearchRecord>>() {
			@Override
			public boolean matches(List<TaxonomySearchRecord> values) {

				List<Record> actualRecords = new ArrayList<>();
				List<Record> recordsInExpectedOrder = new ArrayList<>();

				for (TaxonomySearchRecord value : values) {
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

	private Condition<? super List<TaxonomySearchRecord>> unlinkable(final String... ids) {
		return new Condition<List<TaxonomySearchRecord>>() {
			@Override
			public boolean matches(List<TaxonomySearchRecord> records) {

				for (String id : ids) {
					TaxonomySearchRecord foundRecord = null;
					for (TaxonomySearchRecord record : records) {
						if (id.equals(record.getRecord().getId())) {
							if (foundRecord != null) {
								throw new RuntimeException("Same record found twice");
							}
							foundRecord = record;
						}
					}
					if (foundRecord == null) {
						throw new RuntimeException("Record not found : " + id);
					} else {
						assertThat(foundRecord.isLinkable()).describedAs("is record '" + id + "' linkable").isFalse();
					}

				}

				return true;
			}
		}.describedAs("unlinkable " + ids);
	}

	private Condition<? super List<TaxonomySearchRecord>> linkable(final String... ids) {
		return new Condition<List<TaxonomySearchRecord>>() {
			@Override
			public boolean matches(List<TaxonomySearchRecord> records) {

				for (String id : ids) {
					TaxonomySearchRecord foundRecord = null;
					for (TaxonomySearchRecord record : records) {
						if (id.equals(record.getRecord().getId())) {
							if (foundRecord != null) {
								throw new RuntimeException("Same record found twice");
							}
							foundRecord = record;
						}
					}
					if (foundRecord == null) {
						throw new RuntimeException("Record not found : " + id);
					} else {
						assertThat(foundRecord.isLinkable()).describedAs("is record '" + id + "' linkable").isTrue();
					}

				}

				return true;
			}
		}.describedAs("linkable " + ids);
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

	private ConditionTemplate withoutFilters = null;

	private ListAssert<Taxonomy> assertThatAvailableTaxonomiesForSelectionOf(String schemaTypeCode) {
		return assertThat(
				getModelLayerFactory().getTaxonomiesManager().getAvailableTaxonomiesForSelectionOfType(
						schemaTypeCode, alice, getModelLayerFactory().getMetadataSchemasManager()));
	}

	private ListAssert<TaxonomySearchRecord> assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(ConditionTemplate template) {
		return assertThat(
				service.getLinkableRootConcept(alice, zeCollection, CLASSIFICATION_PLAN, Folder.SCHEMA_TYPE,
						new TaxonomiesSearchOptions()));
	}

	private ListAssert<TaxonomySearchRecord> assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(ConditionTemplate template,
			String category) {
		Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
		List<TaxonomySearchRecord> records = service.getLinkableChildConcept(alice, inRecord, RMTaxonomies.CLASSIFICATION_PLAN,
				Folder.SCHEMA_TYPE, new TaxonomiesSearchOptions());
		return assertThat(records);
	}

	private ListAssert<TaxonomySearchRecord> assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(ConditionTemplate template) {
		return assertThat(
				service.getLinkableRootConcept(alice, zeCollection, CLASSIFICATION_PLAN, Category.SCHEMA_TYPE,
						new TaxonomiesSearchOptions()));
	}

	private ListAssert<TaxonomySearchRecord> assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(ConditionTemplate template,
			String category) {
		Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
		return assertThat(service.getLinkableChildConcept(alice, inRecord, RMTaxonomies.CLASSIFICATION_PLAN,
				Category.SCHEMA_TYPE, new TaxonomiesSearchOptions()));
	}

	private ListAssert<TaxonomySearchRecord> assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(ConditionTemplate template) {

		List<TaxonomySearchRecord> records = service
				.getLinkableRootConcept(alice, zeCollection, ADMINISTRATIVE_UNITS, Folder.SCHEMA_TYPE,
						new TaxonomiesSearchOptions());

		return assertThat(records);
	}

	private ListAssert<TaxonomySearchRecord> assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(ConditionTemplate template,
			String admUnit) {
		Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(admUnit);
		List<TaxonomySearchRecord> taxonomySearchRecords = service
				.getLinkableChildConcept(alice, inRecord, RMTaxonomies.ADMINISTRATIVE_UNITS, Folder.SCHEMA_TYPE,
						new TaxonomiesSearchOptions());
		return assertThat(taxonomySearchRecords);
	}

	private ListAssert<TaxonomySearchRecord> assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(
			ConditionTemplate template) {
		return assertThat(
				service.getLinkableRootConcept(alice, zeCollection, ADMINISTRATIVE_UNITS, AdministrativeUnit.SCHEMA_TYPE,
						new TaxonomiesSearchOptions()));
	}

	private ListAssert<TaxonomySearchRecord> assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(
			ConditionTemplate template, String admUnit) {
		Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(admUnit);
		return assertThat(service.getLinkableChildConcept(alice, inRecord, RMTaxonomies.ADMINISTRATIVE_UNITS,
				AdministrativeUnit.SCHEMA_TYPE, new TaxonomiesSearchOptions()));
	}

	private void givenRule3IsDisabled() {
		RetentionRule rule3 = test.getRule3();
		rule3.setApproved(false);
		try {
			getModelLayerFactory().newRecordServices().update(rule3);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}
}
