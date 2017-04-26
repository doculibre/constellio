package com.constellio.model.services.taxonomies;

import static com.constellio.app.modules.rm.constants.RMTaxonomies.ADMINISTRATIVE_UNITS;
import static com.constellio.app.modules.rm.constants.RMTaxonomies.CLASSIFICATION_PLAN;
import static com.constellio.data.dao.dto.records.OptimisticLockingResolution.EXCEPTION;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.containingText;
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

public class TaxonomiesSearchServices_LinkableTreesAcceptTest extends ConstellioTest {

	User alice;
	User zeSasquatch;
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
		zeSasquatch = userServices.getUserInCollection(sasquatch, zeCollection);
		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(false));
	}

	@Test
	public void whenGetListOfTaxonomyForRecordSelectionThenReturnValidTaxonomies() {

		TaxonomiesManager taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		Taxonomy storageSpaceTaxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, RMTaxonomies.STORAGES);
		Taxonomy planTaxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, CLASSIFICATION_PLAN);
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
		Taxonomy planTaxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, CLASSIFICATION_PLAN);
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

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy()
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X100)
				.has(numFoundAndListSize(1))
				.has(linkable(records.folder_A18))
				.has(itemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A18)
				.is(empty());

	}

	@Test
	public void givenUserHaveAuthorizationsOnSomeFoldersThenValidTreeForDocumentSelectionUsingPlanTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.folder_A18, records.folder_A08);

		assertThatRootWhenSelectingADocumentUsingPlanTaxonomy()
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(records.categoryId_X)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100));

		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(records.categoryId_X100)
				.has(numFoundAndListSize(1))
				.has(resultsInOrder(records.folder_A18))
				.has(unlinkable(records.folder_A18))
				.has(itemsWithChildren(records.folder_A18));

		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(records.folder_A18)
				.has(numFoundAndListSize(3))
				.has(resultsInOrder(folder18Documents()))
				.has(linkable(folder18Documents()))
				.has(itemsWithChildren());

	}

	@Test
	public void givenUserHaveReadAuthorizationsOnSomeFoldersThenValidTreeForFolderSelectionUsingUnitsTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.folder_A18, records.folder_A08);

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy()
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10a))
				.has(itemsWithChildren(records.unitId_10a));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10a)
				.has(numFoundAndListSize(2))
				.has(linkable(records.folder_A18, records.folder_A08))
				.has(resultsInOrder(records.folder_A08, records.folder_A18))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.folder_A18)
				.is(empty());

	}

	@Test
	public void givenUserHaveReadAndWriteAuthorizationsOnSomeFoldersThenValidTreeForFolderSelectionWithWriteUsingUnitsTaxonomy()
			throws Exception {

		givenUserHasWriteAccessTo(records.folder_A18, records.folder_A08);
		givenUserHasReadAccessTo(records.folder_C02, records.folder_A17);
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(withWriteAccess)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10, withWriteAccess)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10a))
				.has(itemsWithChildren(records.unitId_10a));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10a, withWriteAccess)
				.has(numFoundAndListSize(2))
				.has(linkable(records.folder_A18, records.folder_A08))
				.has(resultsInOrder(records.folder_A08, records.folder_A18))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.folder_A18, withWriteAccess)
				.is(empty());

	}

	@Test
	public void givenUserHaveAuthorizationsOnSomeFoldersThenValidTreeForCategorySelectionUsingPlanTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.folder_A18, records.folder_A08);

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy()
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_X))
				.has(unlinkable(records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_X)
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_X100, records.categoryId_X13))
				.has(resultsInOrder(records.categoryId_X13, records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_X100)
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_X110, records.categoryId_X120))
				.has(resultsInOrder(records.categoryId_X110, records.categoryId_X120))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_X110)
				.is(empty());
	}

	@Test
	public void givenSpecialConditionWhenSelectingASecondaryConceptThenReturnRecordsBasedOnCondition()
			throws Exception {

		//givenUserHasReadAccessTo(records.folder_A18, records.folder_A08);

		TaxonomiesSearchFilter filter = new TaxonomiesSearchFilter();
		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions().setFilter(filter);

		filter.setLinkableConceptsCondition(where(IDENTIFIER).isNot(containingText("Z"))
				.andWhere(IDENTIFIER).isContainingText("2"));

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(options)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X))
				.has(resultsInOrder(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X));

		filter.setLinkableConceptsCondition(where(IDENTIFIER).is(containingText("Z"))
				.orWhere(IDENTIFIER).isContainingText("2"));

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(options)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_X, options)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X100))
				.has(resultsInOrder(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_X100, options)
				.has(numFoundAndListSize(1))
				.has(linkable(records.categoryId_X120))
				.has(resultsInOrder(records.categoryId_X120))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_X110, options)
				.is(empty());

		filter.setLinkableConceptsCondition(where(IDENTIFIER).is(containingText("Z"))
				.andWhere(IDENTIFIER).isContainingText("2"));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z100, options)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_Z110))
				.has(linkable(records.categoryId_Z120))
				.has(resultsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(itemsWithChildren(records.categoryId_Z110));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z110, options)
				.has(numFoundAndListSize(1))
				.has(linkable(records.categoryId_Z112))
				.has(resultsInOrder(records.categoryId_Z112))
				.has(noItemsWithChildren());
	}

	@Test
	public void whenSelectingNonPrincipalConceptWithWriteOrDeleteAccessThenException()
			throws Exception {

		givenUserHasReadAccessTo(records.folder_A18, records.folder_A08);
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);
		TaxonomiesSearchOptions withDeleteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.DELETE);

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy();

		try {
			assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(withWriteAccess);
			fail("Exception expected");
		} catch (TaxonomiesSearchServicesRuntimeException_CannotFilterNonPrincipalConceptWithWriteOrDeleteAccess e) {
			//OK
		}

		try {
			assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(withDeleteAccess);
			fail("Exception expected");
		} catch (TaxonomiesSearchServicesRuntimeException_CannotFilterNonPrincipalConceptWithWriteOrDeleteAccess e) {
			//OK
		}

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_X);

		try {
			assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_X, withWriteAccess);
			fail("Exception expected");
		} catch (TaxonomiesSearchServicesRuntimeException_CannotFilterNonPrincipalConceptWithWriteOrDeleteAccess e) {
			//OK
		}

		try {
			assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_X, withDeleteAccess);
			fail("Exception expected");
		} catch (TaxonomiesSearchServicesRuntimeException_CannotFilterNonPrincipalConceptWithWriteOrDeleteAccess e) {
			//OK
		}

	}

	@Test
	public void givenUserHaveAuthorizationsOnSomeFoldersThenEmptyTreeForUnitSelectionUsingUnitTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.folder_A18, records.folder_A08);

		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy()
				.is(empty());

	}

	@Test
	public void givenUserHaveAuthorizationsOnASubFolderThenValidTreeForFolderSelectionUsingCategoryTaxonomy()
			throws Exception {

		Folder subFolder = decommissioningService.newSubFolderIn(records.getFolder_A20()).setTitle("Ze sub folder");
		getModelLayerFactory().newRecordServices().add(subFolder);

		givenUserHasReadAccessTo(subFolder.getId());

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy()
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_Z100))
				.has(itemsWithChildren(records.categoryId_Z100));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z100)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_Z120))
				.has(itemsWithChildren(records.categoryId_Z120));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z120)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.folder_A20));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20)
				.has(numFoundAndListSize(1))
				.has(linkable(subFolder.getId()))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(subFolder.getId())
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

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy()
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20)
				.has(numFoundAndListSize(2))
				.has(linkable(subFolder1.getId(), subFolder2.getId()))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20, withWriteAccess)
				.has(numFoundAndListSize(1))
				.has(linkable(subFolder2.getId()))
				.has(noItemsWithChildren());

	}

	@Test
	public void givenLogicallyDeletedRecordsInVisibleRecordThenNotVisible()
			throws Exception {

		Folder subFolder1 = decommissioningService.newSubFolderIn(records.getFolder_A20()).setTitle("Ze sub folder");
		Folder subFolder2 = decommissioningService.newSubFolderIn(records.getFolder_A20()).setTitle("Ze sub folder");
		getModelLayerFactory().newRecordServices().execute(new Transaction().addAll(subFolder1, subFolder2));

		getModelLayerFactory().newRecordServices().logicallyDelete(subFolder1.getWrappedRecord(), User.GOD);
		getModelLayerFactory().newRecordServices().logicallyDelete(subFolder2.getWrappedRecord(), User.GOD);

		//records.folder_A20,
		givenUserHasReadAccessTo(subFolder1.getId(), subFolder2.getId(), records.folder_C01);
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy()
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z).has(numFoundAndListSize(0));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z100).has(numFoundAndListSize(0));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z120).has(numFoundAndListSize(0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20).has(numFoundAndListSize(0));

	}

	@Test
	public void givenInvisibleRecordsInVisibleRecordNotShownInLinkingModeThenInvisible()
			throws Exception {

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

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z, optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z100,
				optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z120,
				optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20, optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0));

		// With default options

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(defaultOptions)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z, defaultOptions)
				.has(numFoundAndListSize(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z100, defaultOptions)
				.has(numFoundAndListSize(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z120, defaultOptions)
				.has(numFoundAndListSize(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20, defaultOptions)
				.has(numFoundAndListSize(2));

	}

	@Test
	public void givenInvisibleRecordsInVisibleRecordWithSecurityThenShownDependingOnMode()
			throws Exception {

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

		givenUserHasReadAccessTo(records.folder_A20, records.folder_C01);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z, optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z100,
				optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z120,
				optionsWithNoInvisibleRecords)
				.has(noItemsWithChildren())
				.has(numFoundAndListSize(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20, optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0));

		// With default options

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(defaultOptions)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z, defaultOptions)
				.has(numFoundAndListSize(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z100, defaultOptions)
				.has(numFoundAndListSize(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z120, defaultOptions)
				.has(numFoundAndListSize(1))
				.has(itemsWithChildren(records.folder_A20));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20, defaultOptions)
				.has(numFoundAndListSize(2));

	}

	@Test
	public void givenInvisibleRecordsNotShownInLinkingModeThenInvisible()
			throws Exception {

		givenConfig(RMConfigs.DISPLAY_SEMI_ACTIVE_RECORDS_IN_TREES, false);
		TaxonomiesSearchOptions defaultOptions = new TaxonomiesSearchOptions();
		TaxonomiesSearchOptions optionsWithNoInvisibleRecords = new TaxonomiesSearchOptions()
				.setShowInvisibleRecordsInLinkingMode(false);

		getModelLayerFactory().newRecordServices().execute(new Transaction().addAll(records.getFolder_A20()
				.setActualTransferDate(LocalDate.now())));

		assertThat(records.getFolder_A20().get(Schemas.VISIBLE_IN_TREES)).isEqualTo(Boolean.FALSE);

		givenUserHasReadAccessTo(records.folder_A20, records.folder_C01);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z, optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z100,
				optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z120,
				optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20, optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0));

		// With default options

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(defaultOptions)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z, defaultOptions)
				.has(numFoundAndListSize(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z100, defaultOptions)
				.has(numFoundAndListSize(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z120, defaultOptions)
				.has(numFoundAndListSize(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20, defaultOptions)
				.has(numFoundAndListSize(0));

	}

	@Test
	public void givenLogicallyDeletedRecordsThenNotVisible()
			throws Exception {

		getModelLayerFactory().newRecordServices().logicallyDelete(records.getFolder_A20().getWrappedRecord(), User.GOD);

		//records.folder_A20,
		givenUserHasReadAccessTo(records.folder_A20, records.folder_C01);
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy()
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z).has(numFoundAndListSize(0));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z100).has(numFoundAndListSize(0));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z120).has(numFoundAndListSize(0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20).has(numFoundAndListSize(0));

	}

	@Test
	public void givenUserHaveAuthorizationsOnASubFolderThenValidTreeForFolderSelectionUsingUnitTaxonomy()
			throws Exception {

		Folder subFolder = decommissioningService.newSubFolderIn(records.getFolder_A20()).setTitle("Ze sub folder");
		getModelLayerFactory().newRecordServices().add(subFolder);

		givenUserHasReadAccessTo(subFolder.getId());

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy()
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10a))
				.has(itemsWithChildren(records.unitId_10a));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10a)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.folder_A20))
				.has(itemsWithChildren(records.folder_A20));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.folder_A20)
				.has(numFoundAndListSize(1))
				.has(linkable(subFolder.getId()))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(subFolder.getId())
				.is(empty());
	}

	@Test
	public void givenUserHaveAuthorizationsOnAnAdministrativeUnitThenValidTreeForFolderSelectionUsingCategoryTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.unitId_12);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy()
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X100)
				.has(numFoundAndListSize(5))
				.has(unlinkable(records.categoryId_X110, records.categoryId_X120))
				.has(linkable(records.folder_B06, records.folder_B32, records.folder_B52))
				.has(resultsInOrder("categoryId_X110", "categoryId_X120", "B52", "B06", "B32"))
				.has(itemsWithChildren("categoryId_X110", "categoryId_X120"));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X110)
				.has(numFoundAndListSize(4))
				.has(linkable(records.folder_B02, records.folder_B04, records.folder_B30, records.folder_B50))
				.has(resultsInOrder(records.folder_B02, records.folder_B04, records.folder_B30, records.folder_B50))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_B02)
				.is(empty());
	}

	@Test
	public void givenUserHaveAuthorizationsOnAnAdministrativeUnitThenValidTreeForFolderSelectionUsingUnitTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.unitId_12);

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy()
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_12))
				.has(itemsWithChildren(records.unitId_12));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_12)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_12b))
				.has(itemsWithChildren(records.unitId_12b));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_12b)
				.has(numFoundAndListSize(10))
				.has(linkable(records.folder_B02, records.folder_B04, records.folder_B06, records.folder_B08, records.folder_B30,
						records.folder_B32, records.folder_B34, records.folder_B50, records.folder_B52, records.folder_B54))
				.has(resultsInOrder("B52", "B02", "B04", "B06", "B08", "B54", "B30", "B32", "B34", "B50"))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.folder_B02)
				.is(empty());

	}

	@Test
	public void givenUserHaveAuthorizationsOnAnAdministrativeUnitThenAllCategoriesTreeVisibleForCategorySelection()
			throws Exception {

		givenUserHasReadAccessTo(records.unitId_12);

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy()
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_X))
				.has(unlinkable(records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z)
				.has(numFoundAndListSize(3))
				.has(unlinkable(records.categoryId_Z100))
				.has(linkable(records.categoryId_ZE42, records.categoryId_Z999))
				.has(resultsInOrder(records.categoryId_Z100, records.categoryId_Z999, records.categoryId_ZE42))
				.has(itemsWithChildren(records.categoryId_Z100));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z100)
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_Z110, records.categoryId_Z120))
				.has(resultsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(itemsWithChildren(records.categoryId_Z110));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z110)
				.has(numFoundAndListSize(1))
				.has(linkable(records.categoryId_Z112))
				.has(resultsInOrder(records.categoryId_Z112))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z112)
				.is(empty());

		// This test fails because numFound does not match the number of records in interval.
		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z100,
				new TaxonomiesSearchOptions().setStartRow(0).setRows(1))
				.has(resultsInOrder(records.categoryId_Z110))
				.has(itemsWithChildren(records.categoryId_Z110))
				.has(numFound(2)).has(listSize(1));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z100,
				new TaxonomiesSearchOptions().setStartRow(1).setRows(1))
				.has(resultsInOrder(records.categoryId_Z120))
				.has(noItemsWithChildren())
				.has(numFound(2)).has(listSize(1));
	}

	@Test
	public void givenUserHaveAuthorizationsOnAnAdministrativeUnitThenValidTreeForUnitSelectionUsingUnitTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.unitId_12);

		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy()
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_10)
				.has(numFoundAndListSize(1))
				.has(linkable(records.unitId_12))
				.has(itemsWithChildren(records.unitId_12));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_12)
				.has(numFoundAndListSize(2))
				.has(linkable(records.unitId_12b, records.unitId_12c))
				.has(resultsInOrder(records.unitId_12b, records.unitId_12c))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_12b)
				.is(empty());

	}

	@Test
	public void givenUserReadAndWriteHaveAuthorizationsOnAnAdministrativeUnitThenValidTreeForUnitSelectionWithWriteAccessUsingUnitTaxonomy()
			throws Exception {

		givenUserHasWriteAccessTo(records.unitId_12);
		givenUserHasReadAccessTo(records.unitId_11, records.unitId_20);
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withWriteAccess)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_10, withWriteAccess)
				.has(numFoundAndListSize(1))
				.has(linkable(records.unitId_12))
				.has(itemsWithChildren(records.unitId_12));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_12, withWriteAccess)
				.has(numFoundAndListSize(2))
				.has(linkable(records.unitId_12b, records.unitId_12c))
				.has(resultsInOrder(records.unitId_12b, records.unitId_12c))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_12b, withWriteAccess)
				.is(empty());

	}

	@Test
	public void givenUserHaveSiteReadAccessForAllTreeVisibleForFolderSelectionUsingCategoryTaxonomy()
			throws Exception {

		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy()
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess)
				.is(empty());

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X, withWriteAccess)
				.is(empty());

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X100)
				.has(unlinkable(records.categoryId_X110, records.categoryId_X120))
				.has(linkable("A16", "A17", "A18", "A48", "A49", "A50", "A85", "A86", "A87", "B06", "B32", "B52", "C06", "C32",
						"C52"))
				.has(resultsInOrder("categoryId_X110", "categoryId_X120", "B52", "A16", "A17", "A18", "C06", "B06", "A48", "A49",
						"A50", "C32", "A85", "B32", "A86", "A87", "C52"))
				.has(itemsWithChildren("categoryId_X110", "categoryId_X120"));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A18)
				.is(empty());

	}

	@Test
	public void givenUserHaveSiteWriteAccessForAllTreeVisibleForFolderSelectionUsingCategoryTaxonomy()
			throws Exception {

		getModelLayerFactory().newRecordServices().update(alice.setCollectionWriteAccess(true));
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X, withWriteAccess)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X100, withWriteAccess)
				.has(unlinkable(records.categoryId_X110, records.categoryId_X120))
				.has(linkable("A16", "A17", "A18", "A48", "A49", "A50", "A85", "A86", "A87", "B06", "B32", "B52", "C06", "C32",
						"C52"))
				.has(resultsInOrder("categoryId_X110", "categoryId_X120", "B52", "A16", "A17", "A18", "C06", "B06", "A48", "A49",
						"A50", "C32", "A85", "B32", "A86", "A87", "C52"))
				.has(itemsWithChildren("categoryId_X110", "categoryId_X120"));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A18, withWriteAccess)
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
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X100, withWriteAccess)
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
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X13, withWriteAccess)
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

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X, withWriteAccess)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X100, withWriteAccess)
				.has(unlinkable(records.categoryId_X110, records.categoryId_X120))
				.has(numFoundAndListSize(17))
				.has(linkable("A16", "A17", "A18", "A48", "A49", "A50", "A85", "A86", "A87", "B06", "B32", "B52", "C06", "C32",
						"C52"))
				.has(resultsInOrder("categoryId_X110", "categoryId_X120", "B52", "A16", "A17", "A18", "C06", "B06", "A48", "A49",
						"A50", "C32", "A85", "B32", "A86", "A87", "C52"))
				.has(itemsWithChildren("categoryId_X110", "categoryId_X120"));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A18, withWriteAccess)
				.is(empty());

		getModelLayerFactory().newRecordServices().logicallyDelete(records.getFolder_A16().getWrappedRecord(), User.GOD);

		// This test fails because numFound does not match the number of records in interval.
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X100,
				withWriteAccess.setStartRow(0).setRows(6))
				.has(unlinkable(records.categoryId_X110, records.categoryId_X120))
				.has(resultsInOrder("categoryId_X110", "categoryId_X120", "B52", "A17", "A18", "C06"))
				.has(itemsWithChildren("categoryId_X110", "categoryId_X120"))
				.has(numFound(16)).has(listSize(6));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X100,
				withWriteAccess.setStartRow(6).setRows(6))
				.has(resultsInOrder("B06", "A48", "A49", "A50", "C32", "A85"))
				.has(numFound(16)).has(listSize(6));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X100,
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

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess.setStartRow(0).setRows(20).setFastContinueInfos(null))
				.has(resultsInOrder("category_1", "category_2", "category_3", "category_4", "category_5", "category_6",
						"category_7", "category_8", "category_9", "category_10", "category_11", "category_12", "category_13",
						"category_14", "category_15", "category_16", "category_17", "category_18", "category_19", "category_20"))
				.has(numFound(25)).has(listSize(20))
				.has(fastContinuationInfos(false, 20));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess.setStartRow(0).setRows(20).setFastContinueInfos(null))
				.has(resultsInOrder("category_1", "category_2", "category_3", "category_4", "category_5", "category_6",
						"category_7", "category_8", "category_9", "category_10", "category_11", "category_12", "category_13",
						"category_14", "category_15", "category_16", "category_17", "category_18", "category_19", "category_20"))
				.has(numFound(25)).has(listSize(20))
				.has(fastContinuationInfos(false, 20));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess.setStartRow(10).setRows(20)
				.setFastContinueInfos(new FastContinueInfos(false, 10, new ArrayList<String>())))
				.has(resultsInOrder("category_11", "category_12", "category_13", "category_14", "category_15", "category_16",
						"category_17", "category_18", "category_19", "category_20", "category_21", "category_22", "category_23",
						"category_24", "category_25", "category_26", "category_27", "category_28", "category_29", "category_30"))
				.has(numFound(35)).has(listSize(20))
				.has(fastContinuationInfos(false, 30));

		//Calling with an different fast continue (simulating that one of the first ten record was not returned)
		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess.setStartRow(10).setRows(20)
				.setFastContinueInfos(new FastContinueInfos(false, 11, new ArrayList<String>())))
				.has(resultsInOrder("category_12", "category_13", "category_14", "category_15", "category_16", "category_17",
						"category_18", "category_19", "category_20", "category_21", "category_22", "category_23", "category_24",
						"category_25", "category_26", "category_27", "category_28", "category_29", "category_30", "category_31"))
				.has(numFound(35)).has(listSize(20))
				.has(fastContinuationInfos(false, 31));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess.setStartRow(0).setRows(30).setFastContinueInfos(null))
				.has(resultsInOrder("category_1", "category_2", "category_3", "category_4", "category_5", "category_6",
						"category_7", "category_8", "category_9", "category_10", "category_11", "category_12", "category_13",
						"category_14", "category_15", "category_16",
						"category_17", "category_18", "category_19", "category_20", "category_21", "category_22", "category_23",
						"category_24", "category_25", "category_26", "category_27", "category_28", "category_29", "category_30"))
				.has(numFound(50)).has(listSize(30))
				.has(fastContinuationInfos(false, 30));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess.setStartRow(289).setRows(30)
				.setFastContinueInfos(null))
				.has(resultsInOrder("category_290", "category_291", "category_292", "category_293",
						"category_294", "category_295", "category_296", "category_297", "category_298", "category_299",
						"category_300", "categoryId_X", "categoryId_Z"))
				.has(numFound(302)).has(listSize(13))
				.has(fastContinuationInfos(true, 302));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess.setStartRow(289).setRows(30)
				.setFastContinueInfos(new FastContinueInfos(false, 289, new ArrayList<String>())))
				.has(resultsInOrder("category_290", "category_291", "category_292", "category_293",
						"category_294", "category_295", "category_296", "category_297", "category_298", "category_299",
						"category_300", "categoryId_X", "categoryId_Z"))
				.has(numFound(302)).has(listSize(13))
				.has(fastContinuationInfos(true, 302));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess.setStartRow(289).setRows(30)
				.setFastContinueInfos(new FastContinueInfos(false, 290, new ArrayList<String>())))
				.has(resultsInOrder("category_291", "category_292", "category_293",
						"category_294", "category_295", "category_296", "category_297", "category_298", "category_299",
						"category_300", "categoryId_X", "categoryId_Z"))
				.has(numFound(301)).has(listSize(12))
				.has(fastContinuationInfos(true, 302));
	}

	@Test
	public void givenPlethoraOfRootAdministrativeUnitsThenValidGetRootResponse()
			throws Exception {

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE).setRows(50);
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);
		getModelLayerFactory().newRecordServices().update(alice.setCollectionWriteAccess(true));

		List<String> ids = new ArrayList<>();

		Transaction transaction = new Transaction();
		for (int i = 1; i <= 300; i++) {
			String code = (i < 100 ? "0" : "") + (i < 10 ? "0" : "") + i;
			AdministrativeUnit unit = transaction.add(rm.newAdministrativeUnitWithId("unit_" + i)).setCode(code)
					.setTitle("Administrative unit #" + code);
			transaction.add(unit);

			if (i % 2 == 0) {
				ids.add("unit_" + i);
			}

		}

		getModelLayerFactory().newRecordServices().execute(transaction);

		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		for (String id : ids) {
			authorizationsServices
					.add(authorizationInCollection(zeCollection).givingReadWriteAccess().on(id).forUsers(zeSasquatch));
		}
		recordServices.refresh(zeSasquatch);

		options = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE).setRows(50);
		assertThat(service.getLinkableRootConceptResponse(zeSasquatch, zeCollection, ADMINISTRATIVE_UNITS,
				AdministrativeUnit.SCHEMA_TYPE, options))
				.has(resultsInOrder("unit_2", "unit_4", "unit_6", "unit_8", "unit_10", "unit_12", "unit_14",
						"unit_16", "unit_18", "unit_20", "unit_22", "unit_24", "unit_26", "unit_28", "unit_30", "unit_32",
						"unit_34", "unit_36", "unit_38", "unit_40", "unit_42", "unit_44", "unit_46", "unit_48", "unit_50",
						"unit_52", "unit_54", "unit_56", "unit_58", "unit_60", "unit_62", "unit_64", "unit_66", "unit_68",
						"unit_70", "unit_72", "unit_74", "unit_76", "unit_78", "unit_80", "unit_82", "unit_84", "unit_86",
						"unit_88", "unit_90", "unit_92", "unit_94", "unit_96", "unit_98", "unit_100"))
				.has(numFound(150)).has(listSize(50)).has(noFastContinuationInfos());

		options.setStartRow(50);
		assertThat(service.getLinkableRootConceptResponse(zeSasquatch, zeCollection, ADMINISTRATIVE_UNITS,
				AdministrativeUnit.SCHEMA_TYPE, options))
				.has(resultsInOrder("unit_102", "unit_104", "unit_106", "unit_108", "unit_110", "unit_112", "unit_114",
						"unit_116", "unit_118", "unit_120", "unit_122", "unit_124", "unit_126", "unit_128", "unit_130",
						"unit_132", "unit_134", "unit_136", "unit_138", "unit_140", "unit_142", "unit_144", "unit_146",
						"unit_148", "unit_150", "unit_152", "unit_154", "unit_156", "unit_158", "unit_160", "unit_162",
						"unit_164", "unit_166", "unit_168", "unit_170", "unit_172", "unit_174", "unit_176", "unit_178",
						"unit_180", "unit_182", "unit_184", "unit_186", "unit_188", "unit_190", "unit_192", "unit_194",
						"unit_196", "unit_198", "unit_200"))
				.has(numFound(150)).has(listSize(50)).has(noFastContinuationInfos());
	}

	@Test
	public void givenPlethoraOfChildAdministrativeUnitsThenValidGetRootResponse()
			throws Exception {

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE).setRows(50);
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);
		getModelLayerFactory().newRecordServices().update(alice.setCollectionWriteAccess(true));

		List<String> ids = new ArrayList<>();

		Transaction transaction = new Transaction();
		AdministrativeUnit parent = transaction.add(rm.newAdministrativeUnitWithId("zeParent")).setCode("zeParent")
				.setTitle("Ze ultimate parent adm unit");

		for (int i = 1; i <= 300; i++) {
			String code = (i < 100 ? "0" : "") + (i < 10 ? "0" : "") + i;
			AdministrativeUnit unit = transaction.add(rm.newAdministrativeUnitWithId("unit_" + i)).setCode(code)
					.setTitle("Administrative unit #" + code).setParent("zeParent");
			transaction.add(unit);

			if (i % 2 == 0) {
				ids.add("unit_" + i);
			}

		}

		getModelLayerFactory().newRecordServices().execute(transaction);

		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		for (String id : ids) {
			authorizationsServices
					.add(authorizationInCollection(zeCollection).givingReadWriteAccess().on(id).forUsers(zeSasquatch));
		}
		recordServices.refresh(zeSasquatch);

		options = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE).setRows(50);
		assertThat(service.getLinkableChildConceptResponse(zeSasquatch, parent.getWrappedRecord(), ADMINISTRATIVE_UNITS,
				AdministrativeUnit.SCHEMA_TYPE, options))
				.has(resultsInOrder("unit_2", "unit_4", "unit_6", "unit_8", "unit_10", "unit_12", "unit_14",
						"unit_16", "unit_18", "unit_20", "unit_22", "unit_24", "unit_26", "unit_28", "unit_30", "unit_32",
						"unit_34", "unit_36", "unit_38", "unit_40", "unit_42", "unit_44", "unit_46", "unit_48", "unit_50",
						"unit_52", "unit_54", "unit_56", "unit_58", "unit_60", "unit_62", "unit_64", "unit_66", "unit_68",
						"unit_70", "unit_72", "unit_74", "unit_76", "unit_78", "unit_80", "unit_82", "unit_84", "unit_86",
						"unit_88", "unit_90", "unit_92", "unit_94", "unit_96", "unit_98", "unit_100"))
				.has(numFound(150)).has(listSize(50)).has(noFastContinuationInfos());

		options.setStartRow(50);
		assertThat(service.getLinkableChildConceptResponse(zeSasquatch, parent.getWrappedRecord(), ADMINISTRATIVE_UNITS,
				AdministrativeUnit.SCHEMA_TYPE, options))
				.has(resultsInOrder("unit_102", "unit_104", "unit_106", "unit_108", "unit_110", "unit_112", "unit_114",
						"unit_116", "unit_118", "unit_120", "unit_122", "unit_124", "unit_126", "unit_128", "unit_130",
						"unit_132", "unit_134", "unit_136", "unit_138", "unit_140", "unit_142", "unit_144", "unit_146",
						"unit_148", "unit_150", "unit_152", "unit_154", "unit_156", "unit_158", "unit_160", "unit_162",
						"unit_164", "unit_166", "unit_168", "unit_170", "unit_172", "unit_174", "unit_176", "unit_178",
						"unit_180", "unit_182", "unit_184", "unit_186", "unit_188", "unit_190", "unit_192", "unit_194",
						"unit_196", "unit_198", "unit_200"))
				.has(numFound(150)).has(listSize(50)).has(noFastContinuationInfos());
	}

	@Test
	public void givenPlethoraOfFoldersInARubricThenValidGetChildrenResponse()
			throws Exception {

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999,
				options.setStartRow(0).setRows(20))
				.has(numFound(0)).has(listSize(0));

		authsServices.add(AuthorizationAddRequest.authorizationForUsers(alice).givingReadWriteAccess().on(records.unitId_10));

		Transaction transaction = new Transaction();
		for (int i = 1; i <= 100; i++) {
			String code = (i < 100 ? "0" : "") + (i < 10 ? "0" : "") + i;
			Category category = transaction.add(rm.newCategoryWithId("category_" + i)).setCode(code)
					.setTitle("Category #" + code).setParent(records.categoryId_Z999);
			transaction.add(rm.newFolder().setTitle("A folder")
					.setCategoryEntered(category)
					.setRetentionRuleEntered(records.ruleId_1)
					.setAdministrativeUnitEntered(records.unitId_10a)
					.setOpenDate(new LocalDate(2014, 11, 1)));
		}
		getModelLayerFactory().newRecordServices().execute(transaction);

		List<String> recordsToGiveAliceWriteAccess = new ArrayList<>();

		for (int i = 1; i <= 300; i++) {
			String title = "Folder #" + (i < 100 ? "0" : "") + (i < 10 ? "0" : "") + i;
			String id = "zeFolder" + i;
			if (i % 10 == 0) {
				String subFolderId = id + "_subFolder";
				recordsToGiveAliceWriteAccess.add(subFolderId);
				transaction.add(rm.newFolderWithId(id).setTitle(title)
						.setCategoryEntered(records.categoryId_Z999)
						.setRetentionRuleEntered(records.ruleId_1)
						.setAdministrativeUnitEntered(records.unitId_20)
						.setOpenDate(new LocalDate(2014, 11, 1)));
				transaction.add(rm.newFolderWithId(subFolderId).setTitle(title)
						.setParentFolder(id)
						.setOpenDate(new LocalDate(2014, 11, 1)));

			} else if (i % 3 == 1) {
				recordsToGiveAliceWriteAccess.add(id);
				transaction.add(rm.newFolderWithId(id).setTitle(title)
						.setCategoryEntered(records.categoryId_Z999)
						.setRetentionRuleEntered(records.ruleId_1)
						.setAdministrativeUnitEntered(records.unitId_20)
						.setOpenDate(new LocalDate(2014, 11, 1)));

			} else {
				transaction.add(rm.newFolderWithId(id).setTitle(title)
						.setCategoryEntered(records.categoryId_Z999)
						.setRetentionRuleEntered(records.ruleId_1)
						.setAdministrativeUnitEntered(records.unitId_10a)
						.setOpenDate(new LocalDate(2014, 11, 1)));
			}
		}

		getModelLayerFactory().newRecordServices().execute(transaction);

		for (String id : recordsToGiveAliceWriteAccess) {
			authsServices.add(AuthorizationAddRequest.authorizationForUsers(alice).givingReadWriteAccess().on(id));
		}

		recordServices.refresh(alice);

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(70).setRows(20)
				.setFastContinueInfos(null))
				.has(resultsInOrder("category_71", "category_72", "category_73", "category_74", "category_75", "category_76",
						"category_77", "category_78", "category_79", "category_80", "category_81", "category_82", "category_83",
						"category_84", "category_85", "category_86", "category_87", "category_88", "category_89", "category_90"))
				.has(numFound(400)).has(listSize(20)).has(fastContinuationInfos(false, 90));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(89).setRows(20)
				.setFastContinueInfos(null))
				.has(resultsInOrder("category_90", "category_91", "category_92", "category_93", "category_94", "category_95",
						"category_96", "category_97", "category_98", "category_99", "category_100", "zeFolder1", "zeFolder2",
						"zeFolder3", "zeFolder4", "zeFolder5", "zeFolder6", "zeFolder7", "zeFolder8", "zeFolder9"))
				.has(numFound(400)).has(listSize(20))
				.has(fastContinuationInfos(true, 9));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(90).setRows(20)
				.setFastContinueInfos(new FastContinueInfos(false, 90, new ArrayList<String>())))
				.has(resultsInOrder("category_91", "category_92", "category_93", "category_94", "category_95", "category_96",
						"category_97", "category_98", "category_99", "category_100", "zeFolder1", "zeFolder2", "zeFolder3",
						"zeFolder4", "zeFolder5", "zeFolder6", "zeFolder7", "zeFolder8", "zeFolder9", "zeFolder10"))
				.has(numFound(400)).has(listSize(20))
				.has(fastContinuationInfos(true, 9, "zeFolder10"));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(90).setRows(20)
				.setFastContinueInfos(new FastContinueInfos(false, 91, new ArrayList<String>())))
				.has(resultsInOrder("category_92", "category_93", "category_94", "category_95", "category_96", "category_97",
						"category_98", "category_99", "category_100", "zeFolder1", "zeFolder2", "zeFolder3", "zeFolder4",
						"zeFolder5", "zeFolder6", "zeFolder7", "zeFolder8", "zeFolder9", "zeFolder10", "zeFolder11"))
				.has(numFound(399)).has(listSize(20))
				.has(fastContinuationInfos(true, 10, "zeFolder10"));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(90).setRows(20)
				.setFastContinueInfos(null))
				.has(resultsInOrder("category_91", "category_92", "category_93", "category_94", "category_95", "category_96",
						"category_97", "category_98", "category_99", "category_100", "zeFolder1", "zeFolder2", "zeFolder3",
						"zeFolder4", "zeFolder5", "zeFolder6", "zeFolder7", "zeFolder8", "zeFolder9", "zeFolder10"))
				.has(numFound(400)).has(listSize(20))
				.has(fastContinuationInfos(true, 9, "zeFolder10"));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(91).setRows(20)
				.setFastContinueInfos(null))
				.has(resultsInOrder("category_92", "category_93", "category_94", "category_95", "category_96", "category_97",
						"category_98", "category_99", "category_100", "zeFolder1", "zeFolder2", "zeFolder3", "zeFolder4",
						"zeFolder5", "zeFolder6", "zeFolder7", "zeFolder8", "zeFolder9", "zeFolder10", "zeFolder11"))
				.has(numFound(400)).has(listSize(20))
				.has(fastContinuationInfos(true, 10, "zeFolder10"));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(100).setRows(20)
				.setFastContinueInfos(null))
				.has(resultsInOrder("zeFolder1", "zeFolder2", "zeFolder3", "zeFolder4", "zeFolder5", "zeFolder6",
						"zeFolder7", "zeFolder8", "zeFolder9", "zeFolder10", "zeFolder11", "zeFolder12", "zeFolder13",
						"zeFolder14", "zeFolder15", "zeFolder16", "zeFolder17", "zeFolder18", "zeFolder19", "zeFolder20"))
				.has(numFound(400)).has(listSize(20)).has(fastContinuationInfos(true, 18, "zeFolder10", "zeFolder20"));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(120).setRows(20)
				.setFastContinueInfos(new FastContinueInfos(true, 18, asList("zeFolder10", "zeFolder20"))))
				.has(resultsInOrder("zeFolder21", "zeFolder22", "zeFolder23", "zeFolder24", "zeFolder25", "zeFolder26",
						"zeFolder27", "zeFolder28", "zeFolder29", "zeFolder30", "zeFolder31", "zeFolder32", "zeFolder33",
						"zeFolder34", "zeFolder35", "zeFolder36", "zeFolder37", "zeFolder38", "zeFolder39", "zeFolder40"))
				.has(numFound(400)).has(listSize(20))
				.has(fastContinuationInfos(true, 36, "zeFolder10", "zeFolder20", "zeFolder30", "zeFolder40"));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(100).setRows(25)
				.setFastContinueInfos(null))
				.has(resultsInOrder("zeFolder1", "zeFolder2", "zeFolder3", "zeFolder4", "zeFolder5", "zeFolder6",
						"zeFolder7", "zeFolder8", "zeFolder9", "zeFolder10", "zeFolder11", "zeFolder12", "zeFolder13",
						"zeFolder14", "zeFolder15", "zeFolder16", "zeFolder17", "zeFolder18", "zeFolder19", "zeFolder20",
						"zeFolder21", "zeFolder22", "zeFolder23", "zeFolder24", "zeFolder25"))
				.has(numFound(400)).has(listSize(25));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(120).setRows(20)
				.setFastContinueInfos(null))
				.has(numFound(400)).has(listSize(20));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(120).setRows(40)
				.setFastContinueInfos(null))
				.has(numFound(400)).has(listSize(40));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(360).setRows(40)
				.setFastContinueInfos(null))
				.has(numFound(400)).has(listSize(40));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(360).setRows(40)
				.setFastContinueInfos(null))
				.has(resultsInOrder("zeFolder261", "zeFolder262", "zeFolder263", "zeFolder264", "zeFolder265", "zeFolder266",
						"zeFolder267", "zeFolder268", "zeFolder269", "zeFolder270", "zeFolder271", "zeFolder272", "zeFolder273",
						"zeFolder274", "zeFolder275", "zeFolder276", "zeFolder277", "zeFolder278", "zeFolder279", "zeFolder280",
						"zeFolder281", "zeFolder282", "zeFolder283", "zeFolder284", "zeFolder285", "zeFolder286", "zeFolder287",
						"zeFolder288", "zeFolder289", "zeFolder290", "zeFolder291", "zeFolder292", "zeFolder293", "zeFolder294",
						"zeFolder295", "zeFolder296", "zeFolder297", "zeFolder298", "zeFolder299", "zeFolder300"))
				.has(numFound(400)).has(listSize(40));

		assertThatIterationWithAndWithoutFastContinueGivesSameResults(records.categoryId_Z999, 20);
		assertThatIterationWithAndWithoutFastContinueGivesSameResults(records.categoryId_Z999, 1);
	}

	private void assertThatIterationWithAndWithoutFastContinueGivesSameResults(String conceptId, int rows) {
		RecordUtils utils = new RecordUtils();
		boolean hasMore = true;
		int start = 0;
		FastContinueInfos lastInfos = null;
		int iterationCount = 0;
		while (hasMore && ++iterationCount < 120) {

			Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(conceptId);
			LinkableTaxonomySearchResponse responseWithLastInfos = service.getLinkableChildConceptResponse(alice, inRecord,
					CLASSIFICATION_PLAN, Folder.SCHEMA_TYPE, new TaxonomiesSearchOptions()
							.setStartRow(start).setRows(rows).setFastContinueInfos(lastInfos));

			LinkableTaxonomySearchResponse responseWithoutLastInfos = service.getLinkableChildConceptResponse(alice, inRecord,
					CLASSIFICATION_PLAN, Folder.SCHEMA_TYPE, new TaxonomiesSearchOptions().setStartRow(start).setRows(rows));

			String desc = "batch [" + start + "-" + (start + rows) + "]";
			System.out.println("Testing " + desc);
			assertThat(responseWithLastInfos.getNumFound()).describedAs(desc).isEqualTo(responseWithoutLastInfos.getNumFound());
			assertThat(responseWithLastInfos.getRecords())
					.usingElementComparatorOnFields("record.id", "linkable", "hasChildren")
					.describedAs(desc).isEqualTo(responseWithoutLastInfos.getRecords());
			assertThat(responseWithLastInfos.getFastContinueInfos().isFinishedConceptsIteration()).describedAs(desc)
					.isEqualTo(responseWithoutLastInfos.getFastContinueInfos().isFinishedConceptsIteration());
			assertThat(responseWithLastInfos.getFastContinueInfos().getLastReturnRecordIndex()).describedAs(desc)
					.isEqualTo(responseWithoutLastInfos.getFastContinueInfos().getLastReturnRecordIndex());
			assertThat(responseWithLastInfos.getFastContinueInfos().getShownRecordsWithVisibleChildren())
					.usingElementComparatorOnFields("id").isEqualTo(
					responseWithoutLastInfos.getFastContinueInfos().getShownRecordsWithVisibleChildren());
			//			assertThat(utils.toIdList(responseWithLastInfos.getFastContinueInfos().getNotYetShownRecordsWithVisibleChildren()))
			//					.describedAs(desc).isEqualTo(
			//					utils.toIdList(responseWithoutLastInfos.getFastContinueInfos().getNotYetShownRecordsWithVisibleChildren()));
			hasMore = responseWithLastInfos.getNumFound() > start + rows;
			start += rows;
		}
	}

	@Test
	public void givenUserHaveSiteReadAccessForAllTreeVisibleForFolderSelectionUsingUnitTaxonomy()
			throws Exception {

		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy()
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.unitId_10, records.unitId_30))
				.has(resultsInOrder(records.unitId_10, records.unitId_30))
				.has(itemsWithChildren(records.unitId_10, records.unitId_30));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10)
				.has(unlinkable(records.unitId_11, records.unitId_12, records.unitId_10a))
				.has(resultsInOrder(records.unitId_10a, records.unitId_11, records.unitId_12))
				.has(itemsWithChildren(records.unitId_10a, records.unitId_11, records.unitId_12));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10a)
				.has(linkable(records.folder_A42, records.folder_A43, records.folder_A44))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_12)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_12b))
				.has(itemsWithChildren(records.unitId_12b));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_12b)
				.has(numFoundAndListSize(10))
				.has(linkable(records.folder_B02, records.folder_B04, records.folder_B06, records.folder_B08,
						records.folder_B30,
						records.folder_B32, records.folder_B34, records.folder_B50, records.folder_B52, records.folder_B54))
				.has(resultsInOrder("B52", "B02", "B04", "B06", "B08", "B54", "B30", "B32", "B34", "B50"))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.folder_B02)
				.is(empty());

	}

	@Test
	public void givenUserHaveSiteReadAccessAndFoldersDeletedForAllTreeVisibleForFolderSelectionUsingUnitTaxonomy()
			throws Exception {

		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy()
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.unitId_10, records.unitId_30))
				.has(resultsInOrder(records.unitId_10, records.unitId_30))
				.has(itemsWithChildren(records.unitId_10, records.unitId_30));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10)
				.has(unlinkable(records.unitId_11, records.unitId_12, records.unitId_10a))
				.has(resultsInOrder(records.unitId_10a, records.unitId_11, records.unitId_12))
				.has(itemsWithChildren(records.unitId_10a, records.unitId_11, records.unitId_12));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10a)
				.has(linkable(records.folder_A42, records.folder_A43, records.folder_A44))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_12)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_12b))
				.has(itemsWithChildren(records.unitId_12b));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_12b)
				.has(numFoundAndListSize(10))
				.has(linkable(records.folder_B02, records.folder_B04, records.folder_B06, records.folder_B08,
						records.folder_B30,
						records.folder_B32, records.folder_B34, records.folder_B50, records.folder_B52, records.folder_B54))
				.has(resultsInOrder("B52", "B02", "B04", "B06", "B08", "B54", "B30", "B32", "B34", "B50"))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.folder_B02)
				.is(empty());

		getModelLayerFactory().newRecordServices().logicallyDelete(records.getFolder_B08().getWrappedRecord(), User.GOD);

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_12b, new TaxonomiesSearchOptions()
				.setStartRow(0).setRows(4))
				.has(resultsInOrder("B52", "B02", "B04", "B06"))
				.has(noItemsWithChildren())
				.has(numFound(9)).has(listSize(4));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_12b, new TaxonomiesSearchOptions()
				.setStartRow(4).setRows(4))
				.has(resultsInOrder("B54", "B30", "B32", "B34"))
				.has(noItemsWithChildren())
				.has(numFound(9)).has(listSize(4));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_12b,
				new TaxonomiesSearchOptions().setStartRow(8).setRows(4))
				.has(resultsInOrder("B50"))
				.has(noItemsWithChildren())
				.has(numFound(9)).has(listSize(1));
	}

	@Test
	public void givenUserHaveSiteReadAccessForAllTreeVisibleForCategorySelectionUsingPlanTaxonomy()
			throws Exception {
		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy()
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_X))
				.has(unlinkable(records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z)
				.has(numFoundAndListSize(3))
				.has(unlinkable(records.categoryId_Z100))
				.has(linkable(records.categoryId_ZE42, records.categoryId_Z999))
				.has(resultsInOrder(records.categoryId_Z100, records.categoryId_Z999, records.categoryId_ZE42))
				.has(itemsWithChildren(records.categoryId_Z100));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z100)
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_Z110, records.categoryId_Z120))
				.has(resultsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(itemsWithChildren(records.categoryId_Z110));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z112)
				.is(empty());

	}

	@Test
	public void givenUserHaveSiteReadAccessForAllTreeVisibleForUnitSelectionUsingUnitTaxonomy()
			throws Exception {
		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy()
				.has(numFoundAndListSize(3))
				.has(linkable(records.unitId_10, records.unitId_20, records.unitId_30))
				.has(resultsInOrder(records.unitId_10, records.unitId_20, records.unitId_30))
				.has(itemsWithChildren(records.unitId_10, records.unitId_20, records.unitId_30));
		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withWriteAccess)
				.is(empty());

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_10)
				.has(numFoundAndListSize(3))
				.has(linkable(records.unitId_11, records.unitId_12, records.unitId_10a))
				.has(resultsInOrder(records.unitId_10a, records.unitId_11, records.unitId_12))
				.has(itemsWithChildren(records.unitId_11, records.unitId_12));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_10, withWriteAccess)
				.is(empty());

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_12)
				.has(numFoundAndListSize(2))
				.has(linkable(records.unitId_12b, records.unitId_12c))
				.has(resultsInOrder(records.unitId_12b, records.unitId_12c))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_12b)
				.is(empty());

	}

	@Test
	public void givenUserHaveSiteWriteAccessForAllTreeVisibleForUnitSelectionWithWriteAccessUsingUnitTaxonomy()
			throws Exception {
		getModelLayerFactory().newRecordServices().update(alice.setCollectionWriteAccess(true));
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withWriteAccess)
				.has(numFoundAndListSize(3))
				.has(linkable(records.unitId_10, records.unitId_20, records.unitId_30))
				.has(resultsInOrder(records.unitId_10, records.unitId_20, records.unitId_30))
				.has(itemsWithChildren(records.unitId_10, records.unitId_20, records.unitId_30));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_10, withWriteAccess)
				.has(numFoundAndListSize(3))
				.has(linkable(records.unitId_11, records.unitId_12, records.unitId_10a))
				.has(resultsInOrder(records.unitId_10a, records.unitId_11, records.unitId_12))
				.has(itemsWithChildren(records.unitId_11, records.unitId_12));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_12, withWriteAccess)
				.has(numFoundAndListSize(2))
				.has(linkable(records.unitId_12b, records.unitId_12c))
				.has(resultsInOrder(records.unitId_12b, records.unitId_12c))
				.has(noItemsWithChildren());

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_12b, withWriteAccess)
				.is(empty());

	}

	@Test
	public void givenLinkableCategoryCannotBeRootAndMustHaveApprovedRulesThenTheseCategoriesUnlinkable()
			throws Exception {
		givenConfig(RMConfigs.LINKABLE_CATEGORY_MUST_NOT_BE_ROOT, true);
		givenConfig(RMConfigs.LINKABLE_CATEGORY_MUST_HAVE_APPROVED_RULES, true);
		waitForBatchProcess();

		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy()
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z)
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_ZE42))
				.has(unlinkable(records.categoryId_Z100))
				.has(resultsInOrder(records.categoryId_Z100, records.categoryId_ZE42))
				.has(itemsWithChildren(records.categoryId_Z100));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z100)
				.has(numFoundAndListSize(1))
				.has(linkable(records.categoryId_Z110))
				.has(resultsInOrder(records.categoryId_Z110))
				.has(noItemsWithChildren());

		//		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z110)
		//				.has(numFoundAndListSize(2))
		//				.has(unlinkable(records.categoryId_Z111, records.categoryId_Z112))
		//				.has(resultsInOrder(records.categoryId_Z111, records.categoryId_Z112))
		//				.has(noItemsWithChildren());

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z112)
				.is(empty());

	}

	@Test
	public void whenSelectingACategoryNoMatterItsLinkableStatusThenReturnGoodResults()
			throws Exception {
		givenConfig(RMConfigs.LINKABLE_CATEGORY_MUST_NOT_BE_ROOT, true);
		givenConfig(RMConfigs.LINKABLE_CATEGORY_MUST_HAVE_APPROVED_RULES, true);
		waitForBatchProcess();

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions()
				.setAlwaysReturnTaxonomyConceptsWithReadAccess(true);

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(options)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z, options)
				.has(numFoundAndListSize(4))
				.has(resultsInOrder(records.categoryId_Z100, records.categoryId_Z200,
						records.categoryId_Z999, records.categoryId_ZE42))
				.has(itemsWithChildren(records.categoryId_Z100))
				.has(linkable(records.categoryId_Z100, records.categoryId_Z200, records.categoryId_Z999,
						records.categoryId_ZE42));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z100, options)
				.has(resultsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(itemsWithChildren(records.categoryId_Z110))
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_Z110));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z112, options)
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

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingADocumentUsingPlanTaxonomy() {
		return assertThatRootWhenSelectingADocumentUsingPlanTaxonomy(new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingADocumentUsingPlanTaxonomy(
			TaxonomiesSearchOptions options) {
		return assertThat(
				service.getLinkableRootConceptResponse(alice, zeCollection, CLASSIFICATION_PLAN, Document.SCHEMA_TYPE, options));
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(
			String category) {
		return assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(category, new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(
			String category, TaxonomiesSearchOptions options) {
		Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
		LinkableTaxonomySearchResponse response = service.getLinkableChildConceptResponse(alice, inRecord,
				CLASSIFICATION_PLAN, Document.SCHEMA_TYPE, options);
		return assertThat(response);
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingAFolderUsingPlanTaxonomy() {
		return assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(
			TaxonomiesSearchOptions options) {
		return assertThat(
				service.getLinkableRootConceptResponse(alice, zeCollection, CLASSIFICATION_PLAN, Folder.SCHEMA_TYPE, options));
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(
			String category) {
		return assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(category, new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(
			String category, TaxonomiesSearchOptions options) {
		Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
		LinkableTaxonomySearchResponse response = service.getLinkableChildConceptResponse(alice, inRecord,
				CLASSIFICATION_PLAN, Folder.SCHEMA_TYPE, options);
		return assertThat(response);
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingACategoryUsingPlanTaxonomy() {
		return assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingAUnitUsingAdministrativeUnitTaxonomy() {
		return assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(
			TaxonomiesSearchOptions options) {
		return assertThat(
				service.getLinkableRootConceptResponse(alice, zeCollection, CLASSIFICATION_PLAN, Category.SCHEMA_TYPE, options));
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(String category) {
		return assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(category, new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(
			String category, TaxonomiesSearchOptions taxonomiesSearchOptions) {
		Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
		return assertThat(service.getLinkableChildConceptResponse(alice, inRecord, CLASSIFICATION_PLAN,
				Category.SCHEMA_TYPE, taxonomiesSearchOptions));
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingAFolderUsingUnitTaxonomy() {
		return assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(
			TaxonomiesSearchOptions options) {

		LinkableTaxonomySearchResponse response = service
				.getLinkableRootConceptResponse(alice, zeCollection, ADMINISTRATIVE_UNITS, Folder.SCHEMA_TYPE, options);

		return assertThat(response);
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(
			String admUnit) {
		return assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(admUnit, new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(
			String admUnit, TaxonomiesSearchOptions options) {
		Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(admUnit);
		LinkableTaxonomySearchResponse response = service
				.getLinkableChildConceptResponse(alice, inRecord, RMTaxonomies.ADMINISTRATIVE_UNITS, Folder.SCHEMA_TYPE, options);
		return assertThat(response);
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy() {
		return assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(
			TaxonomiesSearchOptions options) {
		return assertThat(
				service.getLinkableRootConceptResponse(alice, zeCollection, ADMINISTRATIVE_UNITS, AdministrativeUnit.SCHEMA_TYPE,
						options));
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(
			String admUnit) {
		return assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(admUnit,
				new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(
			String admUnit, TaxonomiesSearchOptions options) {
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

	private Condition<? super LinkableTaxonomySearchResponse> fastContinuationInfos(
			final boolean expectedinishedIteratingOverConcepts,
			final int expectedLastReturnRecordIndex, String... ids) {

		final List<String> expectedIds = asList(ids);

		return new Condition<LinkableTaxonomySearchResponse>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponse value) {

				assertThat(value.getFastContinueInfos().getShownRecordsWithVisibleChildren())
						.describedAs("notYetShownRecordsWithVisibleChildren").isEqualTo(expectedIds);

				assertThat(value.getFastContinueInfos().finishedConceptsIteration)
						.describedAs("notYetShownRecordsWithVisibleChildren").isEqualTo(expectedinishedIteratingOverConcepts);

				assertThat(value.getFastContinueInfos().getLastReturnRecordIndex())
						.describedAs("lastReturnRecordIndex").isEqualTo(expectedLastReturnRecordIndex);
				return true;
			}
		};
	}

	private Condition<? super LinkableTaxonomySearchResponse> noFastContinuationInfos() {

		return new Condition<LinkableTaxonomySearchResponse>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponse value) {

				assertThat(value.getFastContinueInfos()).isNull();
				return true;
			}
		};
	}
}
