package com.constellio.model.services.taxonomies;

import static com.constellio.app.modules.rm.constants.RMTaxonomies.ADMINISTRATIVE_UNITS;
import static com.constellio.app.modules.rm.constants.RMTaxonomies.CLASSIFICATION_PLAN;
import static com.constellio.data.dao.dto.records.OptimisticLockingResolution.EXCEPTION;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.solr.common.params.SolrParams;
import org.assertj.core.api.BooleanAssert;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.joda.time.LocalDate;
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
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.services.idGenerator.ZeroPaddedSequentialUniqueIdGenerator;
import com.constellio.data.extensions.BigVaultServerExtension;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.ConditionTemplate;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServicesRuntimeException.TaxonomiesSearchServicesRuntimeException_CannotFilterNonPrincipalConceptWithWriteOrDeleteAccess;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.sun.research.ws.wadl.Doc;

public class TaxonomiesSearchServices_LinkableTreesAcceptTest extends ConstellioTest {

	User alice;
	DecommissioningService decommissioningService;
	TaxonomiesSearchServices service;
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	AuthorizationsServices authsServices;
	RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTestUsers().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		authsServices = getModelLayerFactory().newAuthorizationsServices();
		recordServices = getModelLayerFactory().newRecordServices();

		givenConfig(RMConfigs.LINKABLE_CATEGORY_MUST_NOT_BE_ROOT, false);
		givenConfig(RMConfigs.LINKABLE_CATEGORY_MUST_HAVE_APPROVED_RULES, false);

		givenRule3IsDisabled();

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		service = getModelLayerFactory().newTaxonomiesSearchService();
		decommissioningService = new DecommissioningService(zeCollection, getAppLayerFactory());

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
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X100)
				.has(numFoundAndListSize(1))
				.has(linkable(records.folder_A18))
				.has(itemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.folder_A18)
				.is(empty());

	}

	@Test
	public void givenUserHaveAuthorizationsOnSomeFoldersThenValidTreeForDocumentSelectionUsingPlanTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.folder_A18, records.folder_A08);

		assertThatRootWhenSelectingADocumentUsingPlanTaxonomy(withoutFilters)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(withoutFilters, records.categoryId_X)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100));

		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(withoutFilters, records.categoryId_X100)
				.has(numFoundAndListSize(1))
				.has(resultsInOrder(records.folder_A18))
				.has(unlinkable(records.folder_A18))
				.has(itemsWithChildren(records.folder_A18));

		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(withoutFilters, records.folder_A18)
				.has(numFoundAndListSize(3))
				.has(resultsInOrder(folder18Documents()))
				.has(linkable(folder18Documents()))
				.has(itemsWithChildren());

	}

	@Test
	public void givenUserHaveReadAuthorizationsOnSomeFoldersThenValidTreeForFolderSelectionUsingUnitsTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.folder_A18, records.folder_A08);

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_10)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10a))
				.has(itemsWithChildren(records.unitId_10a));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_10a)
				.has(numFoundAndListSize(2))
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
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_10, withWriteAccess)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10a))
				.has(itemsWithChildren(records.unitId_10a));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_10a, withWriteAccess)
				.has(numFoundAndListSize(2))
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
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_X))
				.has(unlinkable(records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_X)
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_X100, records.categoryId_X13))
				.has(resultsInOrder(records.categoryId_X13, records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_X100)
				.has(numFoundAndListSize(2))
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
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_Z100))
				.has(itemsWithChildren(records.categoryId_Z100));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z100)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_Z120))
				.has(itemsWithChildren(records.categoryId_Z120));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z120)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.folder_A20));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.folder_A20)
				.has(numFoundAndListSize(1))
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
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, withWriteAccess)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.folder_A20)
				.has(numFoundAndListSize(2))
				.has(linkable(subFolder1.getId(), subFolder2.getId()))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.folder_A20, withWriteAccess)
				.has(numFoundAndListSize(1))
				.has(linkable(subFolder2.getId()))
				.has(noItemsWithChildren());

	}

	@Test
	public void givenUserHaveWriteAuthorizationsOnDeletedSubFolderThenValidTreeForFolderSelectionUsingCategoryTaxonomy()
			throws Exception {

		Folder subFolder1 = decommissioningService.newSubFolderIn(records.getFolder_A20()).setTitle("Ze sub folder");
		Folder subFolder2 = decommissioningService.newSubFolderIn(records.getFolder_A20()).setTitle("Ze sub folder");
		getModelLayerFactory().newRecordServices().execute(new Transaction().addAll(subFolder1, subFolder2));

		getModelLayerFactory().newRecordServices().logicallyDelete(subFolder1.getWrappedRecord(), User.GOD);
		getModelLayerFactory().newRecordServices().logicallyDelete(subFolder2.getWrappedRecord(), User.GOD);

		//records.folder_A20,
		givenUserHasReadAccessTo(subFolder1.getId(), subFolder2.getId(), records.folder_C01);
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z).has(numFoundAndListSize(0));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z100).has(numFoundAndListSize(0));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z120).has(numFoundAndListSize(0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.folder_A20).has(numFoundAndListSize(0));

	}

	@Test
	public void givenInvisibleRecordsNotShownInLinkingModeThenInvisible()
			throws Exception {

		givenConfig(RMConfigs.DISPLAY_SEMI_ACTIVE_RECORDS_IN_TREES, false);
		givenConfig(RMConfigs.DISPLAY_SEMI_ACTIVE_RECORDS_IN_TREES, false);
		TaxonomiesSearchOptions defaultOptions = new TaxonomiesSearchOptions();
		TaxonomiesSearchOptions optionsWithNoInvisibleRecords = new TaxonomiesSearchOptions()
				.setShowInvisibleRecordsInLinkingMode(false);

		Folder subFolder1 = decommissioningService.newSubFolderIn(records.getFolder_A20()).setTitle("Ze sub folder")
				.setActualTransferDate(LocalDate.now()).setActualDestructionDate(LocalDate.now());
		Folder subFolder2 = decommissioningService.newSubFolderIn(records.getFolder_A20()).setTitle("Ze sub folder")
				.setActualTransferDate(LocalDate.now());
		getModelLayerFactory().newRecordServices().execute(new Transaction().addAll(subFolder1, subFolder2));

		assertThat(subFolder2.get(Schemas.VISIBLE_IN_TREES)).isEqualTo(Boolean.FALSE);

		givenUserHasReadAccessTo(subFolder1.getId(), subFolder2.getId(), records.folder_C01);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z, optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z100,
				optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z120,
				optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.folder_A20, optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0));

		// With default options

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, defaultOptions)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z, defaultOptions)
				.has(numFoundAndListSize(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z100, defaultOptions)
				.has(numFoundAndListSize(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z120, defaultOptions)
				.has(numFoundAndListSize(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.folder_A20, defaultOptions)
				.has(numFoundAndListSize(2));

	}

	@Test
	public void givenUserHaveAuthorizationsOnASubFolderThenValidTreeForFolderSelectionUsingUnitTaxonomy()
			throws Exception {

		Folder subFolder = decommissioningService.newSubFolderIn(records.getFolder_A20()).setTitle("Ze sub folder");
		getModelLayerFactory().newRecordServices().add(subFolder);

		givenUserHasReadAccessTo(subFolder.getId());

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_10)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10a))
				.has(itemsWithChildren(records.unitId_10a));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_10a)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.folder_A20))
				.has(itemsWithChildren(records.folder_A20));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.folder_A20)
				.has(numFoundAndListSize(1))
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
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X100)
				.has(numFoundAndListSize(5))
				.has(unlinkable(records.categoryId_X110, records.categoryId_X120))
				.has(linkable(records.folder_B06, records.folder_B32, records.folder_B52))
				.has(resultsInOrder("categoryId_X110", "categoryId_X120", "B52", "B06", "B32"))
				.has(itemsWithChildren("categoryId_X110", "categoryId_X120"));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X110)
				.has(numFoundAndListSize(4))
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
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_10)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_12))
				.has(itemsWithChildren(records.unitId_12));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_12)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_12b))
				.has(itemsWithChildren(records.unitId_12b));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_12b)
				.has(numFoundAndListSize(10))
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
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_X))
				.has(unlinkable(records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z)
				.has(numFoundAndListSize(4))
				.has(unlinkable(records.categoryId_Z100, records.categoryId_Z200))
				.has(linkable(records.categoryId_ZE42, records.categoryId_Z999))
				.has(resultsInOrder(records.categoryId_Z100, records.categoryId_Z200, records.categoryId_Z999,
						records.categoryId_ZE42))
				.has(itemsWithChildren(records.categoryId_Z100));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z100)
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_Z110, records.categoryId_Z120))
				.has(resultsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(itemsWithChildren(records.categoryId_Z110));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z110)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_Z111))
				.has(linkable(records.categoryId_Z112))
				.has(resultsInOrder(records.categoryId_Z111, records.categoryId_Z112))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z112)
				.is(empty());

	}

	@Test
	public void givenUserHaveAuthorizationsOnAnAdministrativeUnitThenAllCategoriesTreeVisibleForCategorySelectionWithInterval()
			throws Exception {

		givenUserHasReadAccessTo(records.unitId_12);

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters)
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_X))
				.has(unlinkable(records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z)
				.has(numFoundAndListSize(4))
				.has(unlinkable(records.categoryId_Z100, records.categoryId_Z200))
				.has(linkable(records.categoryId_ZE42, records.categoryId_Z999))
				.has(resultsInOrder(records.categoryId_Z100, records.categoryId_Z200, records.categoryId_Z999,
						records.categoryId_ZE42))
				.has(itemsWithChildren(records.categoryId_Z100));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z100)
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_Z110, records.categoryId_Z120))
				.has(resultsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(itemsWithChildren(records.categoryId_Z110));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z110)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_Z111))
				.has(linkable(records.categoryId_Z112))
				.has(resultsInOrder(records.categoryId_Z111, records.categoryId_Z112))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z112)
				.is(empty());

		// This test fails because numFound does not match the number of records in interval.
		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z110,
				new TaxonomiesSearchOptions().setStartRow(0).setRows(1))
				.has(resultsInOrder(records.categoryId_Z111))
				.has(noItemsWithChildren())
				.has(numFound(2)).has(listSize(1));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z110,
				new TaxonomiesSearchOptions().setStartRow(1).setRows(1))
				.has(resultsInOrder(records.categoryId_Z112))
				.has(noItemsWithChildren())
				.has(numFound(2)).has(listSize(1));

	}

	@Test
	public void givenUserHaveAuthorizationsOnAnAdministrativeUnitThenValidTreeForUnitSelectionUsingUnitTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.unitId_12);

		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_10)
				.has(numFoundAndListSize(1))
				.has(linkable(records.unitId_12))
				.has(itemsWithChildren(records.unitId_12));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_12)
				.has(numFoundAndListSize(2))
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
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_10, withWriteAccess)
				.has(numFoundAndListSize(1))
				.has(linkable(records.unitId_12))
				.has(itemsWithChildren(records.unitId_12));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_12, withWriteAccess)
				.has(numFoundAndListSize(2))
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
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, withWriteAccess)
				.is(empty());

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X)
				.has(numFoundAndListSize(1))
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
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X, withWriteAccess)
				.has(numFoundAndListSize(1))
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
	public void givenUserHaveWriteAccessOnSomeRecordWhenIteratingThenOnlySeeThoseRecords()
			throws Exception {

		for (String record : asList("A16", "A18", "A49", "A85", "A87", "B06", "B52", "C32", "C52")) {
			authsServices.add(authorizationForUsers(alice).givingReadWriteAccess().on(recordServices.getDocumentById(record)));
		}

		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		recordServices.refresh(alice);
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X100, withWriteAccess)
				.has(linkable("A16", "A18", "A49", "A85", "A87", "B06", "B52", "C32", "C52"))
				.has(resultsInOrder("B52", "A16", "A18", "B06", "A49", "C32", "A85", "A87", "C52"))
				.has(noItemsWithChildren());

	}

	@Test
	public void given10000FoldersAndUserHasOnlyAccessToTheLastOnesThenDoesNotIteratorOverAllNodesToFindThem()
			throws Exception {

		Folder folderNearEnd = null;
		Folder subFolderNearEnd = null;
		List<Folder> addedRecords = new ArrayList<>();

		int size = 4999;
		for (int i = 0; i < size; i++) {
			String paddedIndex = ZeroPaddedSequentialUniqueIdGenerator.zeroPaddedNumber(i);
			Folder folder = rm.newFolder().setTitle("Dossier #" + paddedIndex).setRetentionRuleEntered(records.ruleId_1)
					.setCategoryEntered(records.categoryId_X13).setOpenDate(LocalDate.now())
					.setAdministrativeUnitEntered(records.unitId_10a);
			addedRecords.add(folder);
			if (i == size - 2) {
				folderNearEnd = folder;
			}

			if (i == size - 1) {
				subFolderNearEnd = rm.newFolder().setTitle("Sub folder").setParentFolder(folder).setOpenDate(LocalDate.now());
				addedRecords.add(subFolderNearEnd);
			}
		}
		recordServices.execute(new Transaction().addAll(addedRecords).setOptimisticLockingResolution(EXCEPTION));

		authsServices.add(authorizationForUsers(alice).givingReadWriteAccess().on(folderNearEnd));
		authsServices.add(authorizationForUsers(alice).givingReadWriteAccess().on(subFolderNearEnd));

		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		final AtomicInteger queryCount = new AtomicInteger();
		getDataLayerFactory().getExtensions().getSystemWideExtensions().bigVaultServerExtension
				.add(new BigVaultServerExtension() {
					@Override
					public void afterQuery(SolrParams solrParams, long qtime) {
						queryCount.incrementAndGet();
					}
				});

		recordServices.refresh(alice);
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X13, withWriteAccess)
				.has(resultsInOrder(folderNearEnd.getId(), subFolderNearEnd.getParentFolder()))
				.has(linkable(folderNearEnd.getId()))
				.has(unlinkable(subFolderNearEnd.getParentFolder()));

		assertThat(queryCount.get()).isEqualTo(4);
	}

	@Test
	public void givenUserHaveSiteWriteAccessAndFoldersDeletedForAllTreeVisibleForFolderSelectionUsingCategoryTaxonomy()
			throws Exception {

		getModelLayerFactory().newRecordServices().update(alice.setCollectionWriteAccess(true));
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, withWriteAccess)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X, withWriteAccess)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X100, withWriteAccess)
				.has(unlinkable(records.categoryId_X110, records.categoryId_X120))
				.has(numFoundAndListSize(17))
				.has(linkable("A16", "A17", "A18", "A48", "A49", "A50", "A85", "A86", "A87", "B06", "B32", "B52", "C06", "C32",
						"C52"))
				.has(resultsInOrder("categoryId_X110", "categoryId_X120", "B52", "A16", "A17", "A18", "C06", "B06", "A48", "A49",
						"A50", "C32", "A85", "B32", "A86", "A87", "C52"))
				.has(itemsWithChildren("categoryId_X110", "categoryId_X120"));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.folder_A18, withWriteAccess)
				.is(empty());

		getModelLayerFactory().newRecordServices().logicallyDelete(records.getFolder_A16().getWrappedRecord(), User.GOD);

		// This test fails because numFound does not match the number of records in interval.
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X100,
				withWriteAccess.setStartRow(0).setRows(6))
				.has(unlinkable(records.categoryId_X110, records.categoryId_X120))
				.has(resultsInOrder("categoryId_X110", "categoryId_X120", "B52", "A17", "A18", "C06"))
				.has(itemsWithChildren("categoryId_X110", "categoryId_X120"))
				.has(numFound(16)).has(listSize(6));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X100,
				withWriteAccess.setStartRow(6).setRows(6))
				.has(resultsInOrder("B06", "A48", "A49", "A50", "C32", "A85"))
				.has(numFound(16)).has(listSize(6));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_X100,
				withWriteAccess.setStartRow(12).setRows(6))
				.has(resultsInOrder("B32", "A86", "A87", "C52"))
				.has(numFound(16)).has(listSize(4));

	}

	@Test
	public void givenPlethoraOfRootCategoriesInARubricThenValidGetRootResponse()
			throws Exception {

		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);
		getModelLayerFactory().newRecordServices().update(alice.setCollectionWriteAccess(true));

		Transaction transaction = new Transaction();
		for (int i = 1; i <= 300; i++) {
			String code = (i < 100 ? "0" : "") + (i < 10 ? "0" : "") + i;
			Category category = transaction.add(rm.newCategoryWithId("category_" + i)).setCode(code)
					.setTitle("Category #" + code);
			transaction.add(rm.newFolder().setTitle("A folder")
					.setCategoryEntered(category)
					.setRetentionRuleEntered(records.ruleId_1)
					.setAdministrativeUnitEntered(records.unitId_10a)
					.setOpenDate(new LocalDate(2014, 11, 1)));
		}
		getModelLayerFactory().newRecordServices().execute(transaction);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, withWriteAccess.setStartRow(0).setRows(20))
				.has(resultsInOrder("category_1", "category_2", "category_3", "category_4", "category_5", "category_6",
						"category_7", "category_8", "category_9", "category_10", "category_11", "category_12", "category_13",
						"category_14", "category_15", "category_16", "category_17", "category_18", "category_19", "category_20"))
				.has(numFound(25)).has(listSize(20));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, withWriteAccess.setStartRow(10).setRows(20))
				.has(resultsInOrder("category_11", "category_12", "category_13", "category_14", "category_15", "category_16",
						"category_17", "category_18", "category_19", "category_20", "category_21", "category_22", "category_23",
						"category_24", "category_25", "category_26", "category_27", "category_28", "category_29", "category_30"))
				.has(numFound(50)).has(listSize(20));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, withWriteAccess.setStartRow(0).setRows(30))
				.has(resultsInOrder("category_1", "category_2", "category_3", "category_4", "category_5", "category_6",
						"category_7", "category_8", "category_9", "category_10", "category_11", "category_12", "category_13",
						"category_14", "category_15", "category_16",
						"category_17", "category_18", "category_19", "category_20", "category_21", "category_22", "category_23",
						"category_24", "category_25", "category_26", "category_27", "category_28", "category_29", "category_30"))
				.has(numFound(50)).has(listSize(30));
	}

	@Test
	public void givenPlethoraOfFoldersInARubricThenValidGetChildrenResponse()
			throws Exception {

		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		getModelLayerFactory().newRecordServices().update(alice.setCollectionWriteAccess(true));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z999,
				withWriteAccess.setStartRow(0).setRows(20))
				.has(numFound(0)).has(listSize(0));

		Transaction transaction = new Transaction();
		for (int i = 1; i <= 300; i++) {
			String title = "Folder #" + (i < 100 ? "0" : "") + (i < 10 ? "0" : "") + i;
			transaction.add(rm.newFolderWithId("zeFolder" + i).setTitle(title)
					.setCategoryEntered(records.categoryId_Z999)
					.setRetentionRuleEntered(records.ruleId_1)
					.setAdministrativeUnitEntered(records.unitId_10a)
					.setOpenDate(new LocalDate(2014, 11, 1)));
		}
		getModelLayerFactory().newRecordServices().execute(transaction);

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z999,
				withWriteAccess.setStartRow(0).setRows(20))
				.has(resultsInOrder("zeFolder1", "zeFolder2", "zeFolder3", "zeFolder4", "zeFolder5", "zeFolder6",
						"zeFolder7", "zeFolder8", "zeFolder9", "zeFolder10", "zeFolder11", "zeFolder12", "zeFolder13",
						"zeFolder14", "zeFolder15", "zeFolder16", "zeFolder17", "zeFolder18", "zeFolder19", "zeFolder20"))
				.has(numFound(300)).has(listSize(20));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z999,
				withWriteAccess.setStartRow(0).setRows(25))
				.has(resultsInOrder("zeFolder1", "zeFolder2", "zeFolder3", "zeFolder4", "zeFolder5", "zeFolder6",
						"zeFolder7", "zeFolder8", "zeFolder9", "zeFolder10", "zeFolder11", "zeFolder12", "zeFolder13",
						"zeFolder14", "zeFolder15", "zeFolder16", "zeFolder17", "zeFolder18", "zeFolder19", "zeFolder20",
						"zeFolder21", "zeFolder22", "zeFolder23", "zeFolder24", "zeFolder25"))
				.has(numFound(300)).has(listSize(25));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z999,
				withWriteAccess.setStartRow(20).setRows(20))
				.has(numFound(300)).has(listSize(20));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z999,
				withWriteAccess.setStartRow(20).setRows(40))
				.has(numFound(300)).has(listSize(40));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z999,
				withWriteAccess.setStartRow(260).setRows(40))
				.has(numFound(300)).has(listSize(40));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(withoutFilters, records.categoryId_Z999,
				withWriteAccess.setStartRow(260).setRows(40))
				.has(resultsInOrder("zeFolder261", "zeFolder262", "zeFolder263", "zeFolder264", "zeFolder265", "zeFolder266",
						"zeFolder267", "zeFolder268", "zeFolder269", "zeFolder270", "zeFolder271", "zeFolder272",
						"zeFolder273",
						"zeFolder274", "zeFolder275", "zeFolder276", "zeFolder277", "zeFolder278", "zeFolder279",
						"zeFolder280",
						"zeFolder281", "zeFolder282", "zeFolder283", "zeFolder284", "zeFolder285", "zeFolder286",
						"zeFolder287",
						"zeFolder288", "zeFolder289", "zeFolder290", "zeFolder291", "zeFolder292", "zeFolder293",
						"zeFolder294",
						"zeFolder295", "zeFolder296", "zeFolder297", "zeFolder298", "zeFolder299", "zeFolder300"))
				.has(numFound(300)).has(listSize(40));
	}

	@Test
	public void givenUserHaveSiteReadAccessForAllTreeVisibleForFolderSelectionUsingUnitTaxonomy()
			throws Exception {

		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters)
				.has(numFoundAndListSize(2))
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
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_12b))
				.has(itemsWithChildren(records.unitId_12b));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_12b)
				.has(numFoundAndListSize(10))
				.has(linkable(records.folder_B02, records.folder_B04, records.folder_B06, records.folder_B08,
						records.folder_B30,
						records.folder_B32, records.folder_B34, records.folder_B50, records.folder_B52, records.folder_B54))
				.has(resultsInOrder("B52", "B02", "B04", "B06", "B08", "B54", "B30", "B32", "B34", "B50"))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.folder_B02)
				.is(empty());

	}

	@Test
	public void givenUserHaveSiteReadAccessAndFoldersDeletedForAllTreeVisibleForFolderSelectionUsingUnitTaxonomy()
			throws Exception {

		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters)
				.has(numFoundAndListSize(2))
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
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_12b))
				.has(itemsWithChildren(records.unitId_12b));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_12b)
				.has(numFoundAndListSize(10))
				.has(linkable(records.folder_B02, records.folder_B04, records.folder_B06, records.folder_B08,
						records.folder_B30,
						records.folder_B32, records.folder_B34, records.folder_B50, records.folder_B52, records.folder_B54))
				.has(resultsInOrder("B52", "B02", "B04", "B06", "B08", "B54", "B30", "B32", "B34", "B50"))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.folder_B02)
				.is(empty());

		getModelLayerFactory().newRecordServices().logicallyDelete(records.getFolder_B08().getWrappedRecord(), User.GOD);

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_12b, new TaxonomiesSearchOptions()
				.setStartRow(0).setRows(4))
				.has(resultsInOrder("B52", "B02", "B04", "B06"))
				.has(noItemsWithChildren())
				.has(numFound(9)).has(listSize(4));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_12b, new TaxonomiesSearchOptions()
				.setStartRow(4).setRows(4))
				.has(resultsInOrder("B54", "B30", "B32", "B34"))
				.has(noItemsWithChildren())
				.has(numFound(9)).has(listSize(4));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(withoutFilters, records.unitId_12b,
				new TaxonomiesSearchOptions().setStartRow(8).setRows(4))
				.has(resultsInOrder("B50"))
				.has(noItemsWithChildren())
				.has(numFound(9)).has(listSize(1));
	}

	@Test
	public void givenUserHaveSiteReadAccessForAllTreeVisibleForCategorySelectionUsingPlanTaxonomy()
			throws Exception {
		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters)
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_X))
				.has(unlinkable(records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z)
				.has(numFoundAndListSize(4))
				.has(unlinkable(records.categoryId_Z100, records.categoryId_Z200))
				.has(linkable(records.categoryId_ZE42, records.categoryId_Z999))
				.has(resultsInOrder(records.categoryId_Z100, records.categoryId_Z200, records.categoryId_Z999,
						records.categoryId_ZE42))
				.has(itemsWithChildren(records.categoryId_Z100));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z100)
				.has(numFoundAndListSize(2))
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
				.has(numFoundAndListSize(3))
				.has(linkable(records.unitId_10, records.unitId_20, records.unitId_30))
				.has(resultsInOrder(records.unitId_10, records.unitId_20, records.unitId_30))
				.has(itemsWithChildren(records.unitId_10, records.unitId_20, records.unitId_30));
		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, withWriteAccess)
				.is(empty());

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_10)
				.has(numFoundAndListSize(3))
				.has(linkable(records.unitId_11, records.unitId_12, records.unitId_10a))
				.has(resultsInOrder(records.unitId_10a, records.unitId_11, records.unitId_12))
				.has(itemsWithChildren(records.unitId_11, records.unitId_12));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_10, withWriteAccess)
				.is(empty());

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_12)
				.has(numFoundAndListSize(2))
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
				.has(numFoundAndListSize(3))
				.has(linkable(records.unitId_10, records.unitId_20, records.unitId_30))
				.has(resultsInOrder(records.unitId_10, records.unitId_20, records.unitId_30))
				.has(itemsWithChildren(records.unitId_10, records.unitId_20, records.unitId_30));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_10, withWriteAccess)
				.has(numFoundAndListSize(3))
				.has(linkable(records.unitId_11, records.unitId_12, records.unitId_10a))
				.has(resultsInOrder(records.unitId_10a, records.unitId_11, records.unitId_12))
				.has(itemsWithChildren(records.unitId_11, records.unitId_12));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withoutFilters, records.unitId_12, withWriteAccess)
				.has(numFoundAndListSize(2))
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
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z)
				.has(numFoundAndListSize(4))
				.has(linkable(records.categoryId_ZE42))
				.has(unlinkable(records.categoryId_Z100, records.categoryId_Z200, records.categoryId_Z999))
				.has(resultsInOrder(records.categoryId_Z100, records.categoryId_Z200, records.categoryId_Z999,
						records.categoryId_ZE42))
				.has(itemsWithChildren(records.categoryId_Z100));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z100)
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_Z110))
				.has(unlinkable(records.categoryId_Z120))
				.has(resultsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(itemsWithChildren(records.categoryId_Z110));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(withoutFilters, records.categoryId_Z110)
				.has(numFoundAndListSize(2))
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

	private Condition<? super LinkableTaxonomySearchResponse> numFoundAndListSize(final int expectedCount) {
		return new Condition<LinkableTaxonomySearchResponse>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponse value) {
				assertThat(value.getNumFound()).describedAs("NumFound").isEqualTo(expectedCount);
				assertThat(value.getRecords().size()).describedAs("records list size").isEqualTo(expectedCount);
				return true;
			}
		};
	}

	private Condition<? super LinkableTaxonomySearchResponse> numFound(final int expectedCount) {
		return new Condition<LinkableTaxonomySearchResponse>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponse value) {
				assertThat(value.getNumFound()).describedAs("NumFound").isEqualTo(expectedCount);
				return true;
			}
		};
	}

	private Condition<? super LinkableTaxonomySearchResponse> listSize(final int expectedCount) {
		return new Condition<LinkableTaxonomySearchResponse>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponse value) {
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
		for (String id : ids) {
			getModelLayerFactory().newAuthorizationsServices().add(authorizationForUsers(alice).on(id).givingReadAccess());
		}
		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
		alice = getModelLayerFactory().newUserServices().getUserInCollection(aliceWonderland, zeCollection);
	}

	private void givenUserHasWriteAccessTo(String... ids) {
		for (String id : ids) {
			getModelLayerFactory().newAuthorizationsServices()
					.add(authorizationForUsers(alice).on(id).givingReadWriteAccess());
		}
		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
		alice = getModelLayerFactory().newUserServices().getUserInCollection(aliceWonderland, zeCollection);
	}

	private ConditionTemplate withoutFilters = null;

	private ListAssert<Taxonomy> assertThatAvailableTaxonomiesForSelectionOf(String schemaTypeCode) {
		return assertThat(
				getModelLayerFactory().getTaxonomiesManager().getAvailableTaxonomiesForSelectionOfType(
						schemaTypeCode, alice, getModelLayerFactory().getMetadataSchemasManager()));
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingADocumentUsingPlanTaxonomy(
			ConditionTemplate template) {
		return assertThatRootWhenSelectingADocumentUsingPlanTaxonomy(template, new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingADocumentUsingPlanTaxonomy(
			ConditionTemplate template,
			TaxonomiesSearchOptions options) {
		return assertThat(
				service.getLinkableRootConceptResponse(alice, zeCollection, CLASSIFICATION_PLAN, Document.SCHEMA_TYPE, options));
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(
			ConditionTemplate template,
			String category) {
		return assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(template, category, new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(
			ConditionTemplate template,
			String category, TaxonomiesSearchOptions options) {
		Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
		LinkableTaxonomySearchResponse response = service.getLinkableChildConceptResponse(alice, inRecord,
				RMTaxonomies.CLASSIFICATION_PLAN, Document.SCHEMA_TYPE, options);
		return assertThat(response);
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

	private String[] folder18Documents() {
		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.document.schemaType())
				.where(rm.document.folder()).isEqualTo(records.getFolder_A18()));
		query.sortAsc(Schemas.TITLE);
		return getModelLayerFactory().newSearchServices().searchRecordIds(query).toArray(new String[0]);
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
