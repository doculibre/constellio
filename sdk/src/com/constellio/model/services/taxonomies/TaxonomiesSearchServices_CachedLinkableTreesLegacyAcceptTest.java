package com.constellio.model.services.taxonomies;

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
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException;
import com.constellio.data.dao.services.idGenerator.ZeroPaddedSequentialUniqueIdGenerator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.extensions.AfterQueryParams;
import com.constellio.data.extensions.BigVaultServerExtension;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.RecordCacheType;
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
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServicesRuntimeException.TaxonomiesSearchServicesRuntimeException_CannotFilterNonPrincipalConceptWithWriteOrDeleteAccess;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.assertj.core.api.BooleanAssert;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.app.modules.rm.constants.RMTaxonomies.ADMINISTRATIVE_UNITS;
import static com.constellio.app.modules.rm.constants.RMTaxonomies.CLASSIFICATION_PLAN;
import static com.constellio.app.modules.rm.constants.RMTaxonomies.STORAGES;
import static com.constellio.data.dao.dto.records.OptimisticLockingResolution.EXCEPTION;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.taxonomies.TaxonomiesSearchOptions.HasChildrenFlagCalculated.NEVER;
import static com.constellio.model.services.taxonomies.TaxonomiesTestsUtils.ajustIfBetterThanExpected;
import static com.constellio.model.services.taxonomies.TaxonomiesTestsUtils.createFoldersAndDocumentsWithNegativeAuths;
import static com.constellio.model.services.taxonomies.TaxonomiesTestsUtils.createFoldersAndSubFoldersWithNegativeAuths;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class TaxonomiesSearchServices_CachedLinkableTreesLegacyAcceptTest extends ConstellioTest {

	private static final boolean VALIDATE_SOLR_QUERIES_COUNT = true;

	Users users = new Users();
	User alice;
	User zeSasquatch;
	DecommissioningService decommissioningService;
	TaxonomiesSearchServices service;
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	AuthorizationsServices authsServices;
	RecordServices recordServices;

	AtomicInteger queriesCount = new AtomicInteger();
	AtomicInteger facetsCount = new AtomicInteger();
	AtomicInteger returnedDocumentsCount = new AtomicInteger();

	private static final Logger LOGGER = LoggerFactory.getLogger(TaxonomiesSearchServices_CachedLinkableTreesLegacyAcceptTest.class);

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withAllTest(users).withConstellioRMModule().withRMTest(records)
				.withFoldersAndContainersOfEveryStatus()
		);
		Toggle.TRY_USING_NEW_CACHE_BASED_TAXONOMIES_SEARCH_SERVICES_QUERY_HANDLER.disable();
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
		waitForBatchProcess();
		getDataLayerFactory().getExtensions().getSystemWideExtensions().bigVaultServerExtension
				.add(new BigVaultServerExtension() {
					@Override
					public void afterQuery(AfterQueryParams params) {

						//LOGGER.warn("Query " + params.getSolrParams().toQueryString() + ":", new RuntimeException());

						queriesCount.incrementAndGet();
						String[] facetQuery = params.getSolrParams().getParams("facet.query");
						if (facetQuery != null) {
							facetsCount.addAndGet(facetQuery.length);
						}

						returnedDocumentsCount.addAndGet(params.getReturnedResultsCount());
					}
				});
	}

	@Test
	public void givenStorageSpaceIsNotValidAndHasNoValidChildThenDoNotShowInTree()
			throws Exception {
		cleanStorageSpaces();
		StorageSpace ancestor = buildStorageSpace();
		StorageSpace parent = buildStorageSpace().setParentStorageSpace(ancestor);
		StorageSpace child = buildStorageSpace().setParentStorageSpace(parent).setCapacity(10);

		Transaction transaction = new Transaction();
		transaction.addAll(ancestor, parent, child);
		recordServices.execute(transaction);
		waitForBatchProcess();

		TaxonomiesSearchFilter taxonomiesSearchFilter = new TaxonomiesSearchFilter();
		taxonomiesSearchFilter.setLinkableConceptsFilter(new LinkableConceptFilter() {
			@Override
			public boolean isLinkable(LinkableConceptFilterParams params) {
				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

				StorageSpace storageSpace = rm.wrapStorageSpace(params.getRecord());

				return canStorageSpaceContainContainer(storageSpace, 20D);
			}
		});
		TaxonomiesSearchOptions taxonomiesSearchOptions = new TaxonomiesSearchOptions().setFilter(taxonomiesSearchFilter);

		List<TaxonomySearchRecord> visibleChild = getAppLayerFactory().getModelLayerFactory().newTaxonomiesSearchService()
				.getLinkableRootConcept(users.adminIn(zeCollection), zeCollection, STORAGES, StorageSpace.SCHEMA_TYPE,
						taxonomiesSearchOptions);
		assertThat(visibleChild).isEmpty();

		getAppLayerFactory().getModelLayerFactory().newTaxonomiesSearchService()
				.getLinkableChildConcept(users.adminIn(zeCollection), ancestor.getWrappedRecord(), STORAGES,
						StorageSpace.SCHEMA_TYPE, taxonomiesSearchOptions);
		assertThat(visibleChild).isEmpty();

		child.setCapacity(30);
		recordServices.update(child);

		visibleChild = getAppLayerFactory().getModelLayerFactory().newTaxonomiesSearchService()
				.getLinkableRootConcept(users.adminIn(zeCollection), zeCollection, STORAGES, StorageSpace.SCHEMA_TYPE,
						taxonomiesSearchOptions);
		assertThat(visibleChild).hasSize(1);

		visibleChild = getAppLayerFactory().getModelLayerFactory().newTaxonomiesSearchService()
				.getLinkableChildConcept(users.adminIn(zeCollection), ancestor.getWrappedRecord(), STORAGES,
						StorageSpace.SCHEMA_TYPE, taxonomiesSearchOptions);
		assertThat(visibleChild).hasSize(1);
	}

	@Test
	public void whenGetListOfCategoriesThenReturnNotDeactivatedTaxonomie()
			throws Exception {
		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Transaction transaction = new Transaction();
		transaction.update(records.getCategory_X().set(Category.DEACTIVATE, true).getWrappedRecord());

		recordServices.execute(transaction);

		waitForBatchProcess();

		MetadataSchemaType categoryType = getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(zeCollection).getSchemaType(Category.SCHEMA_TYPE);
		LogicalSearchCondition searchCondition = from(categoryType)
				.where(categoryType.getDefaultSchema().get(Category.DEACTIVATE)).isNotEqual(true);
		TaxonomiesSearchFilter taxonomiesSearchFilter = new TaxonomiesSearchFilter();
		taxonomiesSearchFilter.setLinkableConceptsFilter(new LinkableConceptFilter() {
			@Override
			public boolean isLinkable(LinkableConceptFilterParams params) {

				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(params.getRecord().getCollection(),
						getAppLayerFactory());

				Category category = rm.wrapCategory(params.getRecord());
				return LangUtils.isFalseOrNull(category.<Boolean>get(Category.DEACTIVATE));
			}
		});

		loadCaches();

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(taxonomiesSearchFilter)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X))
				.has(unlinkable(records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_X)
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_X100, records.categoryId_X13))
				.has(resultsInOrder(records.categoryId_X13, records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_X100)
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_X110, records.categoryId_X120))
				.has(resultsInOrder(records.categoryId_X110, records.categoryId_X120))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_X110)
				.is(empty())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

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
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X100)
				.has(numFoundAndListSize(1))
				.has(linkable(records.folder_A18))
				.has(itemsWithChildren())
				.has(solrQueryCounts(3, 1, 3))
				.has(secondSolrQueryCounts(2, 1, 1));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A18)
				.is(empty())
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

	}

	@Test
	public void givenUserHaveAuthorizationsOnSomeFoldersThenValidTreeForDocumentSelectionUsingPlanTaxonomy()
			throws Exception {


		assertThat(getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchemaType("folder")
				.getCacheType()).isEqualTo(RecordCacheType.SUMMARY_CACHED_WITH_VOLATILE);

		givenUserHasReadAccessTo(records.folder_A18, records.folder_A08);

		assertThatRootWhenSelectingADocumentUsingPlanTaxonomy()
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(records.categoryId_X)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(records.categoryId_X100)
				.has(numFoundAndListSize(1))
				.has(resultsInOrder(records.folder_A18))
				.has(unlinkable(records.folder_A18))
				.has(itemsWithChildren(records.folder_A18))
				.has(solrQueryCounts(3, 1, 3))
				.has(secondSolrQueryCounts(2, 1, 1));

		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(records.folder_A18)
				.has(numFoundAndListSize(3))
				.has(resultsInOrder(folder18Documents()))
				.has(linkable(folder18Documents()))
				.has(itemsWithChildren())
				.has(solrQueryCounts(2, 3, 3))
				.has(secondSolrQueryCounts(2, 3, 3));

	}

	@Test
	public void givenUserHaveReadAuthorizationsOnSomeFoldersThenValidTreeForFolderSelectionUsingUnitsTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.folder_A18, records.folder_A08);

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy()
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10))
				.has(solrQueryCounts(1, 0, 3))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10a))
				.has(itemsWithChildren(records.unitId_10a))
				.has(solrQueryCounts(2, 0, 3))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10a)
				.has(numFoundAndListSize(2))
				.has(linkable(records.folder_A18, records.folder_A08))
				.has(resultsInOrder(records.folder_A08, records.folder_A18))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(2, 2, 2))
				.has(secondSolrQueryCounts(2, 2, 2));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.folder_A18)
				.is(empty())
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

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
				.has(itemsWithChildren(records.unitId_10))
				.has(solrQueryCounts(1, 0, 3))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10, withWriteAccess)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10a))
				.has(itemsWithChildren(records.unitId_10a))
				.has(solrQueryCounts(2, 0, 3))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10a, withWriteAccess)
				.has(numFoundAndListSize(2))
				.has(linkable(records.folder_A18, records.folder_A08))
				.has(resultsInOrder(records.folder_A08, records.folder_A18))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(2, 2, 2))
				.has(secondSolrQueryCounts(2, 2, 2));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.folder_A18, withWriteAccess)
				.is(empty())
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

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
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_X)
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_X100, records.categoryId_X13))
				.has(resultsInOrder(records.categoryId_X13, records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_X100)
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_X110, records.categoryId_X120))
				.has(resultsInOrder(records.categoryId_X110, records.categoryId_X120))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_X110)
				.is(empty())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

	}


	@Test
	public void givenSpecialConditionWhenSelectingASecondaryConceptThenReturnRecordsBasedOnCondition()
			throws Exception {

		//givenUserHasReadAccessTo(records.folder_A18, records.folder_A08);

		givenConfig(RMConfigs.LINKABLE_CATEGORY_MUST_NOT_BE_ROOT, false);
		givenConfig(RMConfigs.LINKABLE_CATEGORY_MUST_HAVE_APPROVED_RULES, false);
		waitForBatchProcess();

		Transaction tx = new Transaction();

		Category category = tx.add(rm.newCategoryWithId("zeCategoryWithoutChildren").setCode("ZeCategoryWithoutChildren")
				.setTitle("ZeCategoryWithoutChildren").setRetentionRules(asList(records.ruleId_4)));

		recordServices.execute(tx);

		loadCaches();

		TaxonomiesSearchFilter filter = new TaxonomiesSearchFilter();
		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions().setFilter(filter);

		//		filter.setLinkableConceptsCondition(where(IDENTIFIER).isNot(containingText("Z"))
		//				.andWhere(IDENTIFIER).isContainingText("2"));
		filter.setLinkableConceptsFilter(new LinkableConceptFilter() {
			@Override
			public boolean isLinkable(LinkableConceptFilterParams params) {
				String id = params.getRecord().getId();
				return !id.contains("Z") && id.contains("2");
			}
		});

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(options)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X))
				.has(resultsInOrder(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X))
				.has(solrQueryCounts(1, 0, 3))
				.has(secondSolrQueryCounts(0, 0, 0));

		filter.setLinkableConceptsFilter(new LinkableConceptFilter() {
			@Override
			public boolean isLinkable(LinkableConceptFilterParams params) {
				String id = params.getRecord().getId();
				return id.contains("Z") || id.contains("2");
			}
		});

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(options)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_X, options)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X100))
				.has(resultsInOrder(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_X100, options)
				.has(numFoundAndListSize(1))
				.has(linkable(records.categoryId_X120))
				.has(resultsInOrder(records.categoryId_X120))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_X110, options)
				.is(empty())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		filter.setLinkableConceptsFilter(new LinkableConceptFilter() {
			@Override
			public boolean isLinkable(LinkableConceptFilterParams params) {
				String id = params.getRecord().getId();
				return id.contains("Z") && id.contains("2");
			}
		});

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z100, options)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_Z110))
				.has(linkable(records.categoryId_Z120))
				.has(resultsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(itemsWithChildren(records.categoryId_Z110))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z110, options)
				.has(numFoundAndListSize(1))
				.has(linkable(records.categoryId_Z112))
				.has(resultsInOrder(records.categoryId_Z112))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

	}

	private void loadCaches() {
		for (MetadataSchemaType aSchemaType : getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaTypes()) {
			if (recordServices.getRecordsCaches().getCache(zeCollection).isConfigured(aSchemaType)) {
				getModelLayerFactory().newSearchServices().cachedSearch(new LogicalSearchQuery(from(aSchemaType).returnAll()));
			}
		}
		queriesCount.set(0);
		facetsCount.set(0);
		returnedDocumentsCount.set(0);

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
				.is(empty())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

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
				.has(itemsWithChildren(records.categoryId_Z))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_Z100))
				.has(itemsWithChildren(records.categoryId_Z100))
				.has(solrQueryCounts(2, 0, 4))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z100)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_Z120))
				.has(itemsWithChildren(records.categoryId_Z120))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z120)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.folder_A20))
				.has(solrQueryCounts(2, 1, 1))
				.has(secondSolrQueryCounts(2, 1, 1));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20)
				.has(numFoundAndListSize(1))
				.has(linkable(subFolder.getId()))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(2, 1, 1))
				.has(secondSolrQueryCounts(2, 1, 1));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(subFolder.getId())
				.is(empty())
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

	}

	@Test
	public void givenUserHavePositiveAuthorizationsOnFoldersAndSubFoldersThenValidTreeForFolderSelectionUsingCategoryTaxonomy()
			throws Exception {


		createFoldersAndSubFoldersWithNegativeAuths(records.getUnit20(), records.getCategory_X13());
		waitForBatchProcess();


		assertThat(tokensOf("f1")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(hierarchyTokensOf("f1")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(tokensOf("f2")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(hierarchyTokensOf("f2")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(tokensOf("f3")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(hierarchyTokensOf("f3")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(tokensOf("f4")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(hierarchyTokensOf("f4")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(tokensOf("f5")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(hierarchyTokensOf("f5")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(tokensOf("f6")).containsOnly();
		assertThat(hierarchyTokensOf("f6")).containsOnly();
		assertThat(tokensOf("f7")).containsOnly("nd_legends", "nd_rumors", "nr_legends", "nr_rumors", "nw_legends", "nw_rumors");
		assertThat(hierarchyTokensOf("f7")).containsOnly("nd_legends", "nd_rumors", "nr_legends", "nr_rumors", "nw_legends", "nw_rumors");
		assertThat(tokensOf("f8")).containsOnly("nd_legends", "nd_rumors", "nr_legends", "nr_rumors", "nw_legends", "nw_rumors");
		assertThat(hierarchyTokensOf("f8")).containsOnly("nd_legends", "nd_rumors", "nr_legends", "nr_rumors", "nw_legends", "nw_rumors");
		assertThat(tokensOf("f9")).containsOnly("nd_heroes", "nd_legends", "nd_sidekicks", "nd_rumors", "nr_heroes", "nr_legends", "nr_sidekicks", "nr_rumors", "nw_heroes", "nw_legends", "nw_sidekicks", "nw_rumors");
		assertThat(hierarchyTokensOf("f9")).containsOnly("nd_heroes", "nd_legends", "nd_sidekicks", "nd_rumors", "nr_heroes", "nr_legends", "nr_sidekicks", "nr_rumors", "nw_heroes", "nw_legends", "nw_sidekicks", "nw_rumors");
		assertThat(tokensOf("f10")).containsOnly("nd_heroes", "nd_legends", "nd_sidekicks", "nd_rumors", "nr_heroes", "nr_legends", "nr_sidekicks", "nr_rumors", "nw_heroes", "nw_legends", "nw_sidekicks", "nw_rumors");
		assertThat(hierarchyTokensOf("f10")).containsOnly("nd_heroes", "nd_legends", "nd_sidekicks", "nd_rumors", "nr_heroes", "nr_legends", "nr_sidekicks", "nr_rumors", "nw_heroes", "nw_legends", "nw_sidekicks", "nw_rumors");
		assertThat(tokensOf("f11")).containsOnly("-nd_heroes", "-nd_sidekicks", "-nr_heroes", "-nr_sidekicks", "-nw_heroes", "-nw_sidekicks");
		assertThat(hierarchyTokensOf("f11")).containsOnly("-nd_heroes", "-nd_sidekicks", "-nr_heroes", "-nr_sidekicks", "-nw_heroes", "-nw_sidekicks");
		assertThat(tokensOf("f21")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(hierarchyTokensOf("f21")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(tokensOf("f31")).containsOnly("-nd_heroes", "-nd_sidekicks", "-nr_heroes", "-nr_sidekicks", "-nw_heroes", "-nw_sidekicks", "nd_chuck", "nr_chuck", "nw_chuck", "r_alice", "r_bob", "r_charles", "r_gandalf", "rf_alice", "rf_bob", "rf_charles", "rf_gandalf", "w_alice", "w_bob", "w_charles", "w_gandalf", "wf_alice", "wf_bob", "wf_charles", "wf_gandalf");
		assertThat(hierarchyTokensOf("f31")).containsOnly("-nd_heroes", "-nd_sidekicks", "-nr_heroes", "-nr_sidekicks", "-nw_heroes", "-nw_sidekicks", "nd_chuck", "nr_chuck", "nw_chuck", "r_alice", "r_bob", "r_charles", "r_gandalf", "rf_alice", "rf_bob", "rf_charles", "rf_gandalf", "w_alice", "w_bob", "w_charles", "w_gandalf", "wf_alice", "wf_bob", "wf_charles", "wf_gandalf");
		assertThat(tokensOf("f32")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(hierarchyTokensOf("f32")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(tokensOf("f41")).containsOnly("-nd_heroes", "-nd_sidekicks", "-nr_heroes", "-nr_sidekicks", "-nw_heroes", "-nw_sidekicks", "nd_legends", "nd_rumors", "nr_legends", "nr_rumors", "nw_legends", "nw_rumors", "r_alice", "r_bob", "r_charles", "r_gandalf", "rf_alice", "rf_bob", "rf_charles", "rf_gandalf", "w_alice", "w_bob", "w_charles", "w_gandalf", "wf_alice", "wf_bob", "wf_charles", "wf_gandalf");
		assertThat(hierarchyTokensOf("f41")).containsOnly("-nd_heroes", "-nd_sidekicks", "-nr_heroes", "-nr_sidekicks", "-nw_heroes", "-nw_sidekicks", "nd_legends", "nd_rumors", "nr_legends", "nr_rumors", "nw_legends", "nw_rumors", "r_alice", "r_bob", "r_charles", "r_gandalf", "rf_alice", "rf_bob", "rf_charles", "rf_gandalf", "w_alice", "w_bob", "w_charles", "w_gandalf", "wf_alice", "wf_bob", "wf_charles", "wf_gandalf");
		assertThat(tokensOf("f51")).containsOnly("-nd_heroes", "-nd_sidekicks", "-nr_heroes", "-nr_sidekicks", "-nw_heroes", "-nw_sidekicks", "r_heroes", "r_sidekicks", "rf_heroes", "rf_sidekicks");
		assertThat(hierarchyTokensOf("f51")).containsOnly("-nd_heroes", "-nd_sidekicks", "-nr_heroes", "-nr_sidekicks", "-nw_heroes", "-nw_sidekicks", "r_heroes", "r_sidekicks", "rf_heroes", "rf_sidekicks");
		assertThat(tokensOf("f61")).containsOnly("nd_legends", "nd_rumors", "nr_legends", "nr_rumors", "nw_legends", "nw_rumors");
		assertThat(hierarchyTokensOf("f61")).containsOnly("nd_legends", "nd_rumors", "nr_legends", "nr_rumors", "nw_legends", "nw_rumors");
		assertThat(tokensOf("f62")).containsOnly("r_legends", "r_rumors", "rf_legends", "rf_rumors");
		assertThat(hierarchyTokensOf("f62")).containsOnly("r_legends", "r_rumors", "rf_legends", "rf_rumors");
		assertThat(tokensOf("f71")).containsOnly("nd_legends", "nd_rumors", "nr_legends", "nr_rumors", "nw_legends", "nw_rumors");
		assertThat(hierarchyTokensOf("f71")).containsOnly("nd_legends", "nd_rumors", "nr_legends", "nr_rumors", "nw_legends", "nw_rumors");
		assertThat(tokensOf("f72")).containsOnly("-nd_legends", "-nd_rumors", "-nr_legends", "-nr_rumors", "-nw_legends", "-nw_rumors");
		assertThat(hierarchyTokensOf("f72")).containsOnly("-nd_legends", "-nd_rumors", "-nr_legends", "-nr_rumors", "-nw_legends", "-nw_rumors");
		assertThat(tokensOf("f81")).containsOnly("-nd_legends", "-nd_rumors", "-nr_legends", "-nr_rumors", "-nw_legends", "-nw_rumors", "nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(hierarchyTokensOf("f81")).containsOnly("-nd_legends", "-nd_rumors", "-nr_legends", "-nr_rumors", "-nw_legends", "-nw_rumors", "nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(tokensOf("f91")).containsOnly("nd_heroes", "nd_legends", "nd_sidekicks", "nd_rumors", "nr_heroes", "nr_legends", "nr_sidekicks", "nr_rumors", "nw_heroes", "nw_legends", "nw_sidekicks", "nw_rumors");
		assertThat(hierarchyTokensOf("f91")).containsOnly("nd_heroes", "nd_legends", "nd_sidekicks", "nd_rumors", "nr_heroes", "nr_legends", "nr_sidekicks", "nr_rumors", "nw_heroes", "nw_legends", "nw_sidekicks", "nw_rumors");
		assertThat(tokensOf("f101")).containsOnly("nd_heroes", "nd_legends", "nd_sidekicks", "nd_rumors", "nr_heroes", "nr_legends", "nr_sidekicks", "nr_rumors", "nw_heroes", "nw_legends", "nw_sidekicks", "nw_rumors", "r_alice", "r_bob", "r_charles", "r_gandalf", "rf_alice", "rf_bob", "rf_charles", "rf_gandalf", "w_alice", "w_bob", "w_charles", "w_gandalf", "wf_alice", "wf_bob", "wf_charles", "wf_gandalf");
		assertThat(hierarchyTokensOf("f101")).containsOnly("nd_heroes", "nd_legends", "nd_sidekicks", "nd_rumors", "nr_heroes", "nr_legends", "nr_sidekicks", "nr_rumors", "nw_heroes", "nw_legends", "nw_sidekicks", "nw_rumors", "r_alice", "r_bob", "r_charles", "r_gandalf", "rf_alice", "rf_bob", "rf_charles", "rf_gandalf", "w_alice", "w_bob", "w_charles", "w_gandalf", "wf_alice", "wf_bob", "wf_charles", "wf_gandalf");

		recordServices.update(users.aliceIn(zeCollection).setCollectionAllAccess(true));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X13, users.aliceIn(zeCollection))
				.has(linkable("f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "f10"))
				.has(numFound(10));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f9", users.aliceIn(zeCollection)).has(linkable("f91"));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f10", users.aliceIn(zeCollection)).has(linkable("f101"));

		recordServices.update(users.aliceIn(zeCollection).setCollectionAllAccess(false));


		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X13, users.aliceIn(zeCollection))
				.has(linkable("f1", "f2", "f3", "f4", "f5", "f6"))
				.has(unlinkable("f7", "f8"))
				.has(numFound(8));
		//				.has(solrQueryCounts(3, 4, 4))
		//				.has(secondSolrQueryCounts(1, 0, 0));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f1", users.aliceIn(zeCollection)).has(linkable("f11"));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f2", users.aliceIn(zeCollection)).has(linkable("f21"));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f3", users.aliceIn(zeCollection)).has(linkable("f31", "f32"));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f4", users.aliceIn(zeCollection)).has(noItemsWithChildren());
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f5", users.aliceIn(zeCollection)).has(linkable("f51"));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f6", users.aliceIn(zeCollection)).has(linkable("f62")).has(numFound(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f7", users.aliceIn(zeCollection)).has(linkable("f72")).has(numFound(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f8", users.aliceIn(zeCollection)).has(linkable("f81"));


		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X13, users.charlesIn(zeCollection))
				.has(linkable("f6", "f7", "f8"))
				.has(unlinkable("f1", "f3", "f4", "f5"));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f1", users.charlesIn(zeCollection)).has(linkable("f11")).has(numFound(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f3", users.charlesIn(zeCollection)).has(linkable("f31")).has(numFound(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f4", users.charlesIn(zeCollection)).has(linkable("f41")).has(numFound(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f5", users.charlesIn(zeCollection)).has(linkable("f51")).has(numFound(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f6", users.charlesIn(zeCollection)).has(linkable("f61", "f62")).has(numFound(2));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f7", users.charlesIn(zeCollection)).has(linkable("f71", "f72")).has(numFound(2));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f8", users.charlesIn(zeCollection)).has(noItemsWithChildren());


		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X13, users.gandalfIn(zeCollection))
				.has(linkable("f6"))
				.has(unlinkable("f1", "f3", "f4", "f5", "f7", "f8"));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f1", users.gandalfIn(zeCollection)).has(linkable("f11")).has(numFound(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f3", users.gandalfIn(zeCollection)).has(linkable("f31")).has(numFound(1));

		//This is an accepted problem (f41 should not be returned)
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f4", users.gandalfIn(zeCollection)).has(noItemsWithChildren());
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f5", users.gandalfIn(zeCollection)).has(linkable("f51")).has(numFound(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f6", users.gandalfIn(zeCollection)).has(linkable("f62")).has(numFound(1));
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f7", users.gandalfIn(zeCollection)).has(linkable("f72")).has(numFound(1));

		//This is an accepted problem (f81 should not be returned)
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("f8", users.gandalfIn(zeCollection)).has(noItemsWithChildren());

	}

	private List<String> tokensOf(String recordId) {
		return transformTokens(record(recordId).<String>getList(Schemas.TOKENS));
	}


	private List<String> hierarchyTokensOf(String recordId) {
		return transformTokens(record(recordId).<String>getList(Schemas.TOKENS));
	}

	private List<String> transformTokens(List<String> tokens) {

		UserServices userServices = getModelLayerFactory().newUserServices();
		List<String> returnedTokens = new ArrayList<>();

		for (String token : tokens) {
			String transformedToken = token;

			for (User user : userServices.getAllUsersInCollection(zeCollection)) {
				transformedToken = transformedToken.replace(user.getId(), user.getUsername());
			}

			for (Group group : userServices.getAllGroupsInCollections(zeCollection)) {
				transformedToken = transformedToken.replace(group.getId(), group.getCode());
			}
			returnedTokens.add(transformedToken);
		}

		return returnedTokens;
	}

	private void generateAsserts(String... ids) {
		for (String id : ids) {
			System.out.println("assertThat(tokensOf(\"" + id + "\")).containsOnly(" + toExpected(tokensOf(id)) + ");");
			System.out.println("assertThat(hierarchyTokensOf(\"" + id + "\")).containsOnly(" + toExpected(hierarchyTokensOf(id)) + ");");
		}

	}

	private String toExpected(List<String> values) {
		StringBuilder sb = new StringBuilder();

		for (String value : values) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append("\"" + value + "\"");
		}

		return sb.toString();
	}

	@Test
	public void givenUserHavePositiveAuthorizationsOnFoldersAndDocumentsThenValidTreeForFolderSelectionUsingCategoryTaxonomy()
			throws Exception {


		createFoldersAndDocumentsWithNegativeAuths(records.getUnit20(), records.getCategory_X13());

		waitForBatchProcess();

		assertThat(tokensOf("f1")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(hierarchyTokensOf("f1")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(tokensOf("f2")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(hierarchyTokensOf("f2")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(tokensOf("f3")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(hierarchyTokensOf("f3")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(tokensOf("f4")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(hierarchyTokensOf("f4")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(tokensOf("f5")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(hierarchyTokensOf("f5")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(tokensOf("f6")).containsOnly();
		assertThat(hierarchyTokensOf("f6")).containsOnly();
		assertThat(tokensOf("f7")).containsOnly("nd_legends", "nd_rumors", "nr_legends", "nr_rumors", "nw_legends", "nw_rumors");
		assertThat(hierarchyTokensOf("f7")).containsOnly("nd_legends", "nd_rumors", "nr_legends", "nr_rumors", "nw_legends", "nw_rumors");
		assertThat(tokensOf("f8")).containsOnly("nd_legends", "nd_rumors", "nr_legends", "nr_rumors", "nw_legends", "nw_rumors");
		assertThat(hierarchyTokensOf("f8")).containsOnly("nd_legends", "nd_rumors", "nr_legends", "nr_rumors", "nw_legends", "nw_rumors");
		assertThat(tokensOf("f9")).containsOnly("nd_heroes", "nd_legends", "nd_sidekicks", "nd_rumors", "nr_heroes", "nr_legends", "nr_sidekicks", "nr_rumors", "nw_heroes", "nw_legends", "nw_sidekicks", "nw_rumors");
		assertThat(hierarchyTokensOf("f9")).containsOnly("nd_heroes", "nd_legends", "nd_sidekicks", "nd_rumors", "nr_heroes", "nr_legends", "nr_sidekicks", "nr_rumors", "nw_heroes", "nw_legends", "nw_sidekicks", "nw_rumors");
		assertThat(tokensOf("f10")).containsOnly("nd_heroes", "nd_legends", "nd_sidekicks", "nd_rumors", "nr_heroes", "nr_legends", "nr_sidekicks", "nr_rumors", "nw_heroes", "nw_legends", "nw_sidekicks", "nw_rumors");
		assertThat(hierarchyTokensOf("f10")).containsOnly("nd_heroes", "nd_legends", "nd_sidekicks", "nd_rumors", "nr_heroes", "nr_legends", "nr_sidekicks", "nr_rumors", "nw_heroes", "nw_legends", "nw_sidekicks", "nw_rumors");
		assertThat(tokensOf("d11")).containsOnly("-nd_heroes", "-nd_sidekicks", "-nr_heroes", "-nr_sidekicks", "-nw_heroes", "-nw_sidekicks");
		assertThat(hierarchyTokensOf("d11")).containsOnly("-nd_heroes", "-nd_sidekicks", "-nr_heroes", "-nr_sidekicks", "-nw_heroes", "-nw_sidekicks");
		assertThat(tokensOf("d21")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(hierarchyTokensOf("d21")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(tokensOf("d31")).containsOnly("-nd_heroes", "-nd_sidekicks", "-nr_heroes", "-nr_sidekicks", "-nw_heroes", "-nw_sidekicks", "nd_chuck", "nr_chuck", "nw_chuck", "r_alice", "r_bob", "r_charles", "r_gandalf", "rd_alice", "rd_bob", "rd_charles", "rd_gandalf", "w_alice", "w_bob", "w_charles", "w_gandalf", "wd_alice", "wd_bob", "wd_charles", "wd_gandalf");
		assertThat(hierarchyTokensOf("d31")).containsOnly("-nd_heroes", "-nd_sidekicks", "-nr_heroes", "-nr_sidekicks", "-nw_heroes", "-nw_sidekicks", "nd_chuck", "nr_chuck", "nw_chuck", "r_alice", "r_bob", "r_charles", "r_gandalf", "rd_alice", "rd_bob", "rd_charles", "rd_gandalf", "w_alice", "w_bob", "w_charles", "w_gandalf", "wd_alice", "wd_bob", "wd_charles", "wd_gandalf");
		assertThat(tokensOf("d32")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(hierarchyTokensOf("d32")).containsOnly("nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(tokensOf("d41")).containsOnly("-nd_heroes", "-nd_sidekicks", "-nr_heroes", "-nr_sidekicks", "-nw_heroes", "-nw_sidekicks", "nd_legends", "nd_rumors", "nr_legends", "nr_rumors", "nw_legends", "nw_rumors", "r_alice", "r_bob", "r_charles", "r_gandalf", "rd_alice", "rd_bob", "rd_charles", "rd_gandalf", "w_alice", "w_bob", "w_charles", "w_gandalf", "wd_alice", "wd_bob", "wd_charles", "wd_gandalf");
		assertThat(hierarchyTokensOf("d41")).containsOnly("-nd_heroes", "-nd_sidekicks", "-nr_heroes", "-nr_sidekicks", "-nw_heroes", "-nw_sidekicks", "nd_legends", "nd_rumors", "nr_legends", "nr_rumors", "nw_legends", "nw_rumors", "r_alice", "r_bob", "r_charles", "r_gandalf", "rd_alice", "rd_bob", "rd_charles", "rd_gandalf", "w_alice", "w_bob", "w_charles", "w_gandalf", "wd_alice", "wd_bob", "wd_charles", "wd_gandalf");
		assertThat(tokensOf("d51")).containsOnly("-nd_heroes", "-nd_sidekicks", "-nr_heroes", "-nr_sidekicks", "-nw_heroes", "-nw_sidekicks", "r_heroes", "r_sidekicks", "rd_heroes", "rd_sidekicks");
		assertThat(hierarchyTokensOf("d51")).containsOnly("-nd_heroes", "-nd_sidekicks", "-nr_heroes", "-nr_sidekicks", "-nw_heroes", "-nw_sidekicks", "r_heroes", "r_sidekicks", "rd_heroes", "rd_sidekicks");
		assertThat(tokensOf("d61")).containsOnly("nd_legends", "nd_rumors", "nr_legends", "nr_rumors", "nw_legends", "nw_rumors");
		assertThat(hierarchyTokensOf("d61")).containsOnly("nd_legends", "nd_rumors", "nr_legends", "nr_rumors", "nw_legends", "nw_rumors");
		assertThat(tokensOf("d62")).containsOnly("r_legends", "r_rumors", "rd_legends", "rd_rumors");
		assertThat(hierarchyTokensOf("d62")).containsOnly("r_legends", "r_rumors", "rd_legends", "rd_rumors");
		assertThat(tokensOf("d71")).containsOnly("nd_legends", "nd_rumors", "nr_legends", "nr_rumors", "nw_legends", "nw_rumors");
		assertThat(hierarchyTokensOf("d71")).containsOnly("nd_legends", "nd_rumors", "nr_legends", "nr_rumors", "nw_legends", "nw_rumors");
		assertThat(tokensOf("d72")).containsOnly("-nd_legends", "-nd_rumors", "-nr_legends", "-nr_rumors", "-nw_legends", "-nw_rumors");
		assertThat(hierarchyTokensOf("d72")).containsOnly("-nd_legends", "-nd_rumors", "-nr_legends", "-nr_rumors", "-nw_legends", "-nw_rumors");
		assertThat(tokensOf("d81")).containsOnly("-nd_legends", "-nd_rumors", "-nr_legends", "-nr_rumors", "-nw_legends", "-nw_rumors", "nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(hierarchyTokensOf("d81")).containsOnly("-nd_legends", "-nd_rumors", "-nr_legends", "-nr_rumors", "-nw_legends", "-nw_rumors", "nd_heroes", "nd_sidekicks", "nr_heroes", "nr_sidekicks", "nw_heroes", "nw_sidekicks");
		assertThat(tokensOf("d91")).containsOnly("nd_heroes", "nd_legends", "nd_sidekicks", "nd_rumors", "nr_heroes", "nr_legends", "nr_sidekicks", "nr_rumors", "nw_heroes", "nw_legends", "nw_sidekicks", "nw_rumors");
		assertThat(hierarchyTokensOf("d91")).containsOnly("nd_heroes", "nd_legends", "nd_sidekicks", "nd_rumors", "nr_heroes", "nr_legends", "nr_sidekicks", "nr_rumors", "nw_heroes", "nw_legends", "nw_sidekicks", "nw_rumors");
		assertThat(tokensOf("d101")).containsOnly("nd_heroes", "nd_legends", "nd_sidekicks", "nd_rumors", "nr_heroes", "nr_legends", "nr_sidekicks", "nr_rumors", "nw_heroes", "nw_legends", "nw_sidekicks", "nw_rumors", "r_alice", "r_bob", "r_charles", "r_gandalf", "rd_alice", "rd_bob", "rd_charles", "rd_gandalf", "w_alice", "w_bob", "w_charles", "w_gandalf", "wd_alice", "wd_bob", "wd_charles", "wd_gandalf");
		assertThat(hierarchyTokensOf("d101")).containsOnly("nd_heroes", "nd_legends", "nd_sidekicks", "nd_rumors", "nr_heroes", "nr_legends", "nr_sidekicks", "nr_rumors", "nw_heroes", "nw_legends", "nw_sidekicks", "nw_rumors", "r_alice", "r_bob", "r_charles", "r_gandalf", "rd_alice", "rd_bob", "rd_charles", "rd_gandalf", "w_alice", "w_bob", "w_charles", "w_gandalf", "wd_alice", "wd_bob", "wd_charles", "wd_gandalf");

		recordServices.update(users.aliceIn(zeCollection).setCollectionAllAccess(true));

		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(records.categoryId_X13, users.aliceIn(zeCollection))
				.has(unlinkable("f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "f10"))
				.has(numFound(10));
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f9", users.aliceIn(zeCollection)).has(linkable("d91"));
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f10", users.aliceIn(zeCollection)).has(linkable("d101"));

		recordServices.update(users.aliceIn(zeCollection).setCollectionAllAccess(false));

		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(records.categoryId_X13, users.aliceIn(zeCollection))
				.has(unlinkable("f1", "f2", "f3", "f5", "f6", "f7", "f8"));
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f1", users.aliceIn(zeCollection)).has(linkable("d11"));
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f2", users.aliceIn(zeCollection)).has(linkable("d21"));
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f3", users.aliceIn(zeCollection)).has(linkable("d31", "d32"));
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f4", users.aliceIn(zeCollection)).has(noItemsWithChildren());
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f5", users.aliceIn(zeCollection)).has(linkable("d51"));
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f6", users.aliceIn(zeCollection)).has(linkable("d62")).has(numFound(1));
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f7", users.aliceIn(zeCollection)).has(linkable("d72")).has(numFound(1));
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f8", users.aliceIn(zeCollection)).has(linkable("d81"));


		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(records.categoryId_X13, users.charlesIn(zeCollection))
				.has(unlinkable("f1", "f3", "f4", "f5", "f6", "f7", "f8"));
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f1", users.charlesIn(zeCollection)).has(linkable("d11")).has(numFound(1));
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f3", users.charlesIn(zeCollection)).has(linkable("d31")).has(numFound(1));
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f4", users.charlesIn(zeCollection)).has(linkable("d41")).has(numFound(1));
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f5", users.charlesIn(zeCollection)).has(linkable("d51")).has(numFound(1));
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f6", users.charlesIn(zeCollection)).has(linkable("d61", "d62")).has(numFound(2));
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f7", users.charlesIn(zeCollection)).has(linkable("d71", "d72")).has(numFound(2));
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f8", users.charlesIn(zeCollection)).has(noItemsWithChildren());


		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(records.categoryId_X13, users.gandalfIn(zeCollection))
				.has(unlinkable("f1", "f3", "f4", "f5", "f6", "f7", "f8"));
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f1", users.gandalfIn(zeCollection)).has(linkable("d11")).has(numFound(1));
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f3", users.gandalfIn(zeCollection)).has(linkable("d31")).has(numFound(1));

		//This is an accepted problem (f41 should not be returned)
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f4", users.gandalfIn(zeCollection)).has(noItemsWithChildren());
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f5", users.gandalfIn(zeCollection)).has(linkable("d51")).has(numFound(1));
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f6", users.gandalfIn(zeCollection)).has(linkable("d62")).has(numFound(1));
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f7", users.gandalfIn(zeCollection)).has(linkable("d72")).has(numFound(1));

		//This is an accepted problem (f81 should not be returned)
		assertThatChildWhenSelectingADocumentUsingPlanTaxonomy("f8", users.gandalfIn(zeCollection)).has(noItemsWithChildren());

	}

	@Test
	public void givenUserHaveNegativeAuthorizationsOnASubFolderAndPositiveAuthorizationOnItsParentThenValidTreeForFolderSelectionUsingCategoryTaxonomy()
			throws Exception {

		Folder subFolder = decommissioningService.newSubFolderIn(records.getFolder_A20()).setTitle("Ze sub folder");
		getModelLayerFactory().newRecordServices().add(subFolder);

		givenUserHasReadAccessTo(records.getFolder_A20().getId());
		givenUserHasNegativeReadAccessTo(subFolder.getId());

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy()
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_Z))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_Z100))
				.has(itemsWithChildren(records.categoryId_Z100))
				.has(solrQueryCounts(2, 0, 4))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z100)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_Z120))
				.has(itemsWithChildren(records.categoryId_Z120))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z120)
				.has(numFoundAndListSize(1))
				.has(linkable(records.folder_A20))
				.has(solrQueryCounts(2, 1, 1))
				.has(secondSolrQueryCounts(2, 1, 1));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20)
				.has(numFoundAndListSize(0))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(subFolder.getId())
				.is(empty())
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

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
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_Z))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20)
				.has(numFoundAndListSize(2))
				.has(linkable(subFolder1.getId(), subFolder2.getId()))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(2, 2, 2))
				.has(secondSolrQueryCounts(2, 2, 2));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20, withWriteAccess)
				.has(numFoundAndListSize(1))
				.has(linkable(subFolder2.getId()))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(2, 1, 1))
				.has(secondSolrQueryCounts(2, 1, 1));

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

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z).has(numFoundAndListSize(0))
				.has(solrQueryCounts(2, 0, 4))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z100).has(numFoundAndListSize(0))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z120).has(numFoundAndListSize(0))
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20).has(numFoundAndListSize(0))
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

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

		assertThat(subFolder2.<Boolean>get(Schemas.VISIBLE_IN_TREES)).isEqualTo(Boolean.FALSE);

		givenUserHasReadAccessTo(subFolder1.getId(), subFolder2.getId(), records.folder_C01);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z, optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0))
				.has(solrQueryCounts(2, 0, 4))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z100,
				optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z120,
				optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0))
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20, optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0))
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

		// With default options

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(defaultOptions)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z, defaultOptions)
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(2, 0, 4))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z100, defaultOptions)
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z120, defaultOptions)
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(2, 1, 1))
				.has(secondSolrQueryCounts(2, 1, 1));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20, defaultOptions)
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(2, 2, 2))
				.has(secondSolrQueryCounts(2, 2, 2));

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

		assertThat(subFolder2.<Boolean>get(Schemas.VISIBLE_IN_TREES)).isEqualTo(Boolean.FALSE);

		givenUserHasReadAccessTo(records.folder_A20, records.folder_C01);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z, optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(2, 0, 4))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z100,
				optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z120,
				optionsWithNoInvisibleRecords)
				.has(noItemsWithChildren())
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(2, 1, 1))
				.has(secondSolrQueryCounts(2, 1, 1));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20, optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0))
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

		// With default options

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(defaultOptions)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z, defaultOptions)
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(2, 0, 4))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z100, defaultOptions)
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z120, defaultOptions)
				.has(numFoundAndListSize(1))
				.has(itemsWithChildren(records.folder_A20))
				.has(solrQueryCounts(2, 1, 1))
				.has(secondSolrQueryCounts(2, 1, 1));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20, defaultOptions)
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(2, 2, 2))
				.has(secondSolrQueryCounts(2, 2, 2));

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

		assertThat(records.getFolder_A20().<Boolean>get(Schemas.VISIBLE_IN_TREES)).isEqualTo(Boolean.FALSE);

		givenUserHasReadAccessTo(records.folder_A20, records.folder_C01);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z, optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0))
				.has(solrQueryCounts(2, 0, 4))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z100,
				optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z120,
				optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0))
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20, optionsWithNoInvisibleRecords)
				.has(numFoundAndListSize(0))
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

		// With default options

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(defaultOptions)
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z, defaultOptions)
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(2, 0, 4))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z100, defaultOptions)
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z120, defaultOptions)
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(2, 1, 1))
				.has(secondSolrQueryCounts(2, 1, 1));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20, defaultOptions)
				.has(numFoundAndListSize(0))
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

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
				.has(itemsWithChildren(records.categoryId_X))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z).has(numFoundAndListSize(0))
				.has(solrQueryCounts(2, 0, 4))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z100).has(numFoundAndListSize(0))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z120).has(numFoundAndListSize(0))
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A20).has(numFoundAndListSize(0))
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

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
				.has(itemsWithChildren(records.unitId_10))
				.has(solrQueryCounts(1, 0, 3))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10a))
				.has(itemsWithChildren(records.unitId_10a))
				.has(solrQueryCounts(2, 0, 3))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10a)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.folder_A20))
				.has(itemsWithChildren(records.folder_A20))
				.has(solrQueryCounts(2, 1, 1))
				.has(secondSolrQueryCounts(2, 1, 1));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.folder_A20)
				.has(numFoundAndListSize(1))
				.has(linkable(subFolder.getId()))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(2, 1, 1))
				.has(secondSolrQueryCounts(2, 1, 1));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(subFolder.getId())
				.is(empty())
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

	}

	@Test
	public void givenUserHaveAuthorizationsOnAnAdministrativeUnitThenValidTreeForFolderSelectionUsingCategoryTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.unitId_12);

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy()
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X))
				.has(itemsWithChildren(records.categoryId_X))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X100)
				.has(numFoundAndListSize(5))
				.has(unlinkable(records.categoryId_X110, records.categoryId_X120))
				.has(linkable(records.folder_B06, records.folder_B32, records.folder_B52))
				.has(resultsInOrder("categoryId_X110", "categoryId_X120", "B52", "B06", "B32"))
				.has(itemsWithChildren("categoryId_X110", "categoryId_X120"))
				.has(solrQueryCounts(3, 3, 5))
				.has(secondSolrQueryCounts(2, 3, 3));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X110)
				.has(numFoundAndListSize(4))
				.has(linkable(records.folder_B02, records.folder_B04, records.folder_B30, records.folder_B50))
				.has(resultsInOrder(records.folder_B02, records.folder_B04, records.folder_B30, records.folder_B50))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(2, 4, 4))
				.has(secondSolrQueryCounts(2, 4, 4));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_B02)
				.is(empty())
				.has(solrQueryCounts(2, 1, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

	}

	@Test
	public void givenUserHaveAuthorizationsOnAnAdministrativeUnitThenValidTreeForFolderSelectionUsingUnitTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.unitId_12);

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy()
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10))
				.has(solrQueryCounts(1, 0, 3))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_12))
				.has(itemsWithChildren(records.unitId_12))
				.has(solrQueryCounts(2, 0, 3))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_12)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_12b))
				.has(itemsWithChildren(records.unitId_12b))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_12b)
				.has(numFoundAndListSize(10))
				.has(linkable(records.folder_B02, records.folder_B04, records.folder_B06, records.folder_B08, records.folder_B30,
						records.folder_B32, records.folder_B34, records.folder_B50, records.folder_B52, records.folder_B54))
				.has(resultsInOrder("B52", "B02", "B04", "B06", "B08", "B54", "B30", "B32", "B34", "B50"))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(2, 10, 10))
				.has(secondSolrQueryCounts(2, 10, 10));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.folder_B02)
				.is(empty())
				.has(solrQueryCounts(2, 1, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

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
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z)
				.has(numFoundAndListSize(3))
				.has(unlinkable(records.categoryId_Z100))
				.has(linkable(records.categoryId_ZE42, records.categoryId_Z999))
				.has(resultsInOrder(records.categoryId_Z100, records.categoryId_Z999, records.categoryId_ZE42))
				.has(itemsWithChildren(records.categoryId_Z100))
				.has(solrQueryCounts(1, 0, 4))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z100)
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_Z110, records.categoryId_Z120))
				.has(resultsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(itemsWithChildren(records.categoryId_Z110))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z110)
				.has(numFoundAndListSize(1))
				.has(linkable(records.categoryId_Z112))
				.has(resultsInOrder(records.categoryId_Z112))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z112)
				.is(empty())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		// This test fails because numFound does not match the number of records in interval.
		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z100,
				new TaxonomiesSearchOptions().setStartRow(0).setRows(1))
				.has(resultsInOrder(records.categoryId_Z110))
				.has(itemsWithChildren(records.categoryId_Z110))
				.has(numFound(2)).has(listSize(1))
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z100,
				new TaxonomiesSearchOptions().setStartRow(1).setRows(1))
				.has(resultsInOrder(records.categoryId_Z120))
				.has(noItemsWithChildren())
				.has(numFound(2)).has(listSize(1))
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

	}

	@Test
	public void givenUserHaveAuthorizationsOnAnAdministrativeUnitThenValidTreeForUnitSelectionUsingUnitTaxonomy()
			throws Exception {

		givenUserHasReadAccessTo(records.unitId_12);

		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy()
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_10))
				.has(itemsWithChildren(records.unitId_10))
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_10)
				.has(numFoundAndListSize(1))
				.has(linkable(records.unitId_12))
				.has(itemsWithChildren(records.unitId_12))
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_12)
				.has(numFoundAndListSize(2))
				.has(linkable(records.unitId_12b, records.unitId_12c))
				.has(resultsInOrder(records.unitId_12b, records.unitId_12c))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_12b)
				.is(empty())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

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
				.has(itemsWithChildren(records.unitId_10))
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_10, withWriteAccess)
				.has(numFoundAndListSize(1))
				.has(linkable(records.unitId_12))
				.has(itemsWithChildren(records.unitId_12))
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_12, withWriteAccess)
				.has(numFoundAndListSize(2))
				.has(linkable(records.unitId_12b, records.unitId_12c))
				.has(resultsInOrder(records.unitId_12b, records.unitId_12c))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_12b, withWriteAccess)
				.is(empty())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

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
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess)
				.is(empty())
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X, withWriteAccess)
				.is(empty())
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X100)
				.has(unlinkable(records.categoryId_X110, records.categoryId_X120))
				.has(linkable("A16", "A17", "A18", "A48", "A49", "A50", "A85", "A86", "A87", "B06", "B32", "B52", "C06", "C32",
						"C52"))
				.has(resultsInOrder("categoryId_X110", "categoryId_X120", "B52", "A16", "A17", "A18", "C06", "B06", "A48", "A49",
						"A50", "C32", "A85", "B32", "A86", "A87", "C52"))
				.has(itemsWithChildren("categoryId_X110", "categoryId_X120"))
				.has(solrQueryCounts(3, 15, 17))
				.has(secondSolrQueryCounts(2, 15, 15));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A18)
				.is(empty())
				.has(solrQueryCounts(2, 1, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

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
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X, withWriteAccess)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X100, withWriteAccess)
				.has(unlinkable(records.categoryId_X110, records.categoryId_X120))
				.has(linkable("A16", "A17", "A18", "A48", "A49", "A50", "A85", "A86", "A87", "B06", "B32", "B52", "C06", "C32",
						"C52"))
				.has(resultsInOrder("categoryId_X110", "categoryId_X120", "B52", "A16", "A17", "A18", "C06", "B06", "A48", "A49",
						"A50", "C32", "A85", "B32", "A86", "A87", "C52"))
				.has(itemsWithChildren("categoryId_X110", "categoryId_X120"))
				.has(solrQueryCounts(3, 15, 17))
				.has(secondSolrQueryCounts(2, 15, 15));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A18, withWriteAccess)
				.is(empty())
				.has(solrQueryCounts(2, 1, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

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
				.has(noItemsWithChildren())
				.has(solrQueryCounts(3, 9, 11))
				.has(secondSolrQueryCounts(2, 9, 9));

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
				subFolderNearEnd = rm.newFolderWithId("zeSubFolder").setTitle("Sub folder").setParentFolder(folder).setOpenDate(LocalDate.now());
				addedRecords.add(subFolderNearEnd);
			}
		}
		recordServices.execute(new Transaction().addAll(addedRecords).setOptimisticLockingResolution(EXCEPTION));

		authsServices.add(authorizationForUsers(alice).givingReadWriteAccess().on(folderNearEnd));
		authsServices.add(authorizationForUsers(alice).givingReadWriteAccess().on(subFolderNearEnd));
		waitForBatchProcess();

		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		final AtomicInteger queryCount = new AtomicInteger();
		getDataLayerFactory().getExtensions().getSystemWideExtensions().bigVaultServerExtension
				.add(new BigVaultServerExtension() {

					@Override
					public void afterQuery(AfterQueryParams params) {

						if (params.getQueryName() == null || !params.getQueryName().contains("*SDK*")) {
							queryCount.incrementAndGet();
						}
					}
				});


		getDataLayerFactory().getDataLayerLogger().setPrintAllQueriesLongerThanMS(0).setQueryDebuggingMode(true);
		recordServices.refresh(alice);
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X13, withWriteAccess)
				.has(resultsInOrder(folderNearEnd.getId(), subFolderNearEnd.getParentFolder()))
				.has(linkable(folderNearEnd.getId()))
				.has(unlinkable(subFolderNearEnd.getParentFolder()))
				.has(solrQueryCounts(2, 2, 2))
				.has(secondSolrQueryCounts(2, 2, 2));
		getDataLayerFactory().getDataLayerLogger().setPrintAllQueriesLongerThanMS(10000).setQueryDebuggingMode(false);
		assertThat(queryCount.get()).isEqualTo(5);
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
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X, withWriteAccess)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X100))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X100, withWriteAccess)
				.has(unlinkable(records.categoryId_X110, records.categoryId_X120))
				.has(numFoundAndListSize(17))
				.has(linkable("A16", "A17", "A18", "A48", "A49", "A50", "A85", "A86", "A87", "B06", "B32", "B52", "C06", "C32",
						"C52"))
				.has(resultsInOrder("categoryId_X110", "categoryId_X120", "B52", "A16", "A17", "A18", "C06", "B06", "A48", "A49",
						"A50", "C32", "A85", "B32", "A86", "A87", "C52"))
				.has(itemsWithChildren("categoryId_X110", "categoryId_X120"))
				.has(solrQueryCounts(3, 15, 17))
				.has(secondSolrQueryCounts(2, 15, 15));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.folder_A18, withWriteAccess)
				.is(empty())
				.has(solrQueryCounts(2, 1, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

		getModelLayerFactory().newRecordServices().logicallyDelete(records.getFolder_A16().getWrappedRecord(), User.GOD);

		// This test fails because numFound does not match the number of records in interval.
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X100,
				withWriteAccess.setStartRow(0).setRows(6))
				.has(unlinkable(records.categoryId_X110, records.categoryId_X120))
				.has(resultsInOrder("categoryId_X110", "categoryId_X120", "B52", "A17", "A18", "C06"))
				.has(itemsWithChildren("categoryId_X110", "categoryId_X120"))
				.has(numFound(16)).has(listSize(6))
				.has(solrQueryCounts(2, 4, 4))
				.has(secondSolrQueryCounts(2, 4, 4));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X100,
				withWriteAccess.setStartRow(6).setRows(6))
				.has(resultsInOrder("B06", "A48", "A49", "A50", "C32", "A85"))
				.has(numFound(16)).has(listSize(6))
				.has(solrQueryCounts(2, 10, 6))
				.has(secondSolrQueryCounts(2, 10, 6));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_X100,
				withWriteAccess.setStartRow(12).setRows(6))
				.has(resultsInOrder("B32", "A86", "A87", "C52"))
				.has(numFound(16)).has(listSize(4))
				.has(solrQueryCounts(2, 14, 6))
				.has(secondSolrQueryCounts(2, 14, 6));

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
				.has(fastContinuationInfos(false, 20))
				.has(solrQueryCounts(1, 0, 25))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess.setStartRow(10).setRows(20)
				.setFastContinueInfos(null))
				.has(resultsInOrder("category_11", "category_12", "category_13", "category_14", "category_15", "category_16",
						"category_17", "category_18", "category_19", "category_20", "category_21", "category_22", "category_23",
						"category_24", "category_25", "category_26", "category_27", "category_28", "category_29", "category_30"))
				.has(numFound(50)).has(listSize(20))
				.has(fastContinuationInfos(false, 30))
				.has(solrQueryCounts(1, 0, 10))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess.setStartRow(10).setRows(20)
				.setFastContinueInfos(new FastContinueInfos(false, 10, new ArrayList<String>())))
				.has(resultsInOrder("category_11", "category_12", "category_13", "category_14", "category_15", "category_16",
						"category_17", "category_18", "category_19", "category_20", "category_21", "category_22", "category_23",
						"category_24", "category_25", "category_26", "category_27", "category_28", "category_29", "category_30"))
				.has(numFound(35)).has(listSize(20))
				.has(fastContinuationInfos(false, 30))
				.has(solrQueryCounts(1, 0, 10))
				.has(secondSolrQueryCounts(0, 0, 0));

		//Calling with an different fast continue (simulating that one of the first ten record was not returned)
		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess.setStartRow(10).setRows(20)
				.setFastContinueInfos(new FastContinueInfos(false, 11, new ArrayList<String>())))
				.has(resultsInOrder("category_12", "category_13", "category_14", "category_15", "category_16", "category_17",
						"category_18", "category_19", "category_20", "category_21", "category_22", "category_23", "category_24",
						"category_25", "category_26", "category_27", "category_28", "category_29", "category_30", "category_31"))
				.has(numFound(35)).has(listSize(20))
				.has(fastContinuationInfos(false, 31))
				.has(solrQueryCounts(1, 0, 1))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess.setStartRow(0).setRows(30).setFastContinueInfos(null))
				.has(resultsInOrder("category_1", "category_2", "category_3", "category_4", "category_5", "category_6",
						"category_7", "category_8", "category_9", "category_10", "category_11", "category_12", "category_13",
						"category_14", "category_15", "category_16",
						"category_17", "category_18", "category_19", "category_20", "category_21", "category_22", "category_23",
						"category_24", "category_25", "category_26", "category_27", "category_28", "category_29", "category_30"))
				.has(numFound(50)).has(listSize(30))
				.has(fastContinuationInfos(false, 30))
				.has(solrQueryCounts(1, 0, 14))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess.setStartRow(289).setRows(30)
				.setFastContinueInfos(null))
				.has(resultsInOrder("category_290", "category_291", "category_292", "category_293",
						"category_294", "category_295", "category_296", "category_297", "category_298", "category_299",
						"category_300", "categoryId_X", "categoryId_Z"))
				.has(numFound(302)).has(listSize(13))
				.has(fastContinuationInfos(true, 302))
				.has(solrQueryCounts(11, 0, 252))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess.setStartRow(289).setRows(30)
				.setFastContinueInfos(new FastContinueInfos(false, 289, new ArrayList<String>())))
				.has(resultsInOrder("category_290", "category_291", "category_292", "category_293",
						"category_294", "category_295", "category_296", "category_297", "category_298", "category_299",
						"category_300", "categoryId_X", "categoryId_Z"))
				.has(numFound(302)).has(listSize(13))
				.has(fastContinuationInfos(true, 302))
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(withWriteAccess.setStartRow(289).setRows(30)
				.setFastContinueInfos(new FastContinueInfos(false, 290, new ArrayList<String>())))
				.has(resultsInOrder("category_291", "category_292", "category_293",
						"category_294", "category_295", "category_296", "category_297", "category_298", "category_299",
						"category_300", "categoryId_X", "categoryId_Z"))
				.has(numFound(301)).has(listSize(12))
				.has(fastContinuationInfos(true, 302))
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

	}

	@Test
	public void givenPlethoraOfChildCategoriesThenValidGetRootResponse()
			throws Exception {

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);
		getModelLayerFactory().newRecordServices().update(alice.setCollectionWriteAccess(true));

		Transaction transaction = new Transaction();
		Category rootCategory = rm.newCategoryWithId("root").setCode("root").setTitle("root");

		for (int i = 1; i <= 300; i++) {
			String code = (i < 100 ? "0" : "") + (i < 10 ? "0" : "") + i;
			Category category = transaction.add(rm.newCategoryWithId("category_" + i)).setCode(code)
					.setTitle("Category #" + code).setParent(rootCategory);
			transaction.add(rm.newFolder().setTitle("A folder")
					.setCategoryEntered(category)
					.setRetentionRuleEntered(records.ruleId_1)
					.setAdministrativeUnitEntered(records.unitId_10a)
					.setOpenDate(new LocalDate(2014, 11, 1)));
		}
		transaction.add(rootCategory);
		getModelLayerFactory().newRecordServices().execute(transaction);

		User alice = users.aliceIn(zeCollection);
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("root",
				options.setStartRow(0).setRows(20).setFastContinueInfos(null))
				.has(resultsInOrder("category_1", "category_2", "category_3", "category_4", "category_5", "category_6",
						"category_7", "category_8", "category_9", "category_10", "category_11", "category_12", "category_13",
						"category_14", "category_15", "category_16", "category_17", "category_18", "category_19", "category_20"))
				.has(numFound(40)).has(listSize(20))
				.has(fastContinuationInfos(false, 20))
				.has(solrQueryCounts(3, 0, 40))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("root",
				options.setStartRow(0).setRows(20).setFastContinueInfos(null))
				.has(resultsInOrder("category_1", "category_2", "category_3", "category_4", "category_5", "category_6",
						"category_7", "category_8", "category_9", "category_10", "category_11", "category_12", "category_13",
						"category_14", "category_15", "category_16", "category_17", "category_18", "category_19", "category_20"))
				.has(numFound(40)).has(listSize(20))
				.has(fastContinuationInfos(false, 20))
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("root", options.setStartRow(10).setRows(20)
				.setFastContinueInfos(new FastContinueInfos(false, 10, new ArrayList<String>())))
				.has(resultsInOrder("category_11", "category_12", "category_13", "category_14", "category_15", "category_16",
						"category_17", "category_18", "category_19", "category_20", "category_21", "category_22", "category_23",
						"category_24", "category_25", "category_26", "category_27", "category_28", "category_29", "category_30"))
				.has(numFound(40)).has(listSize(20))
				.has(fastContinuationInfos(false, 30))
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

		//Calling with an different fast continue (simulating that one of the first ten record was not returned)
		//Nothing changed since the service is using a cache
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("root", options.setStartRow(10).setRows(20)
				.setFastContinueInfos(new FastContinueInfos(false, 11, new ArrayList<String>())))
				.has(resultsInOrder("category_11", "category_12", "category_13", "category_14", "category_15", "category_16",
						"category_17", "category_18", "category_19", "category_20", "category_21", "category_22", "category_23",
						"category_24", "category_25", "category_26", "category_27", "category_28", "category_29", "category_30"))
				.has(numFound(40)).has(listSize(20)).has(fastContinuationInfos(false, 30))
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("root",
				options.setStartRow(0).setRows(30).setFastContinueInfos(null))
				.has(resultsInOrder("category_1", "category_2", "category_3", "category_4", "category_5", "category_6",
						"category_7", "category_8", "category_9", "category_10", "category_11", "category_12", "category_13",
						"category_14", "category_15", "category_16",
						"category_17", "category_18", "category_19", "category_20", "category_21", "category_22", "category_23",
						"category_24", "category_25", "category_26", "category_27", "category_28", "category_29", "category_30"))
				.has(numFound(60)).has(listSize(30))
				.has(fastContinuationInfos(false, 30))
				.has(solrQueryCounts(2, 0, 20))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("root", options.setStartRow(289).setRows(30)
				.setFastContinueInfos(null))
				.has(resultsInOrder("category_290", "category_291", "category_292", "category_293",
						"category_294", "category_295", "category_296", "category_297", "category_298", "category_299",
						"category_300"))
				.has(numFound(300)).has(listSize(11))
				.has(fastContinuationInfos(true, 0))
				.has(solrQueryCounts(9, 0, 240))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("root", options.setStartRow(289).setRows(30)
				.setFastContinueInfos(new FastContinueInfos(false, 289, new ArrayList<String>())))
				.has(resultsInOrder("category_290", "category_291", "category_292", "category_293",
						"category_294", "category_295", "category_296", "category_297", "category_298", "category_299",
						"category_300"))
				.has(numFound(300)).has(listSize(11))
				.has(fastContinuationInfos(true, 0))
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

		//Calling with a bad fastContinueInfos, but no difference since the service is using a cache
		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy("root", options.setStartRow(289).setRows(30)
				.setFastContinueInfos(new FastContinueInfos(false, 290, new ArrayList<String>())))
				.has(resultsInOrder("category_290", "category_291", "category_292", "category_293",
						"category_294", "category_295", "category_296", "category_297", "category_298", "category_299",
						"category_300"))
				.has(numFound(300)).has(listSize(11))
				.has(fastContinuationInfos(true, 0))
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

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
		assertThatgetLinkableRootConceptResponseReturnValueInOrder(zeSasquatch, zeCollection, ADMINISTRATIVE_UNITS,
				AdministrativeUnit.SCHEMA_TYPE, options)
				.has(resultsInOrder("unit_2", "unit_4", "unit_6", "unit_8", "unit_10", "unit_12", "unit_14",
						"unit_16", "unit_18", "unit_20", "unit_22", "unit_24", "unit_26", "unit_28", "unit_30", "unit_32",
						"unit_34", "unit_36", "unit_38", "unit_40", "unit_42", "unit_44", "unit_46", "unit_48", "unit_50",
						"unit_52", "unit_54", "unit_56", "unit_58", "unit_60", "unit_62", "unit_64", "unit_66", "unit_68",
						"unit_70", "unit_72", "unit_74", "unit_76", "unit_78", "unit_80", "unit_82", "unit_84", "unit_86",
						"unit_88", "unit_90", "unit_92", "unit_94", "unit_96", "unit_98", "unit_100"))
				.has(numFound(150)).has(listSize(50)).has(noFastContinuationInfos())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		options.setStartRow(50);
		assertThatgetLinkableRootConceptResponseReturnValueInOrder(zeSasquatch, zeCollection, ADMINISTRATIVE_UNITS,
				AdministrativeUnit.SCHEMA_TYPE, options)
				.has(resultsInOrder("unit_102", "unit_104", "unit_106", "unit_108", "unit_110", "unit_112", "unit_114",
						"unit_116", "unit_118", "unit_120", "unit_122", "unit_124", "unit_126", "unit_128", "unit_130",
						"unit_132", "unit_134", "unit_136", "unit_138", "unit_140", "unit_142", "unit_144", "unit_146",
						"unit_148", "unit_150", "unit_152", "unit_154", "unit_156", "unit_158", "unit_160", "unit_162",
						"unit_164", "unit_166", "unit_168", "unit_170", "unit_172", "unit_174", "unit_176", "unit_178",
						"unit_180", "unit_182", "unit_184", "unit_186", "unit_188", "unit_190", "unit_192", "unit_194",
						"unit_196", "unit_198", "unit_200"))
				.has(numFound(150)).has(listSize(50)).has(noFastContinuationInfos())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

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
		assertThatGetLinkableChildConceptResponseHasResultInOrder(zeSasquatch, parent.getWrappedRecord(), ADMINISTRATIVE_UNITS,
				AdministrativeUnit.SCHEMA_TYPE, options)
				.has(resultsInOrder("unit_2", "unit_4", "unit_6", "unit_8", "unit_10", "unit_12", "unit_14",
						"unit_16", "unit_18", "unit_20", "unit_22", "unit_24", "unit_26", "unit_28", "unit_30", "unit_32",
						"unit_34", "unit_36", "unit_38", "unit_40", "unit_42", "unit_44", "unit_46", "unit_48", "unit_50",
						"unit_52", "unit_54", "unit_56", "unit_58", "unit_60", "unit_62", "unit_64", "unit_66", "unit_68",
						"unit_70", "unit_72", "unit_74", "unit_76", "unit_78", "unit_80", "unit_82", "unit_84", "unit_86",
						"unit_88", "unit_90", "unit_92", "unit_94", "unit_96", "unit_98", "unit_100"))
				.has(numFound(150)).has(listSize(50)).has(noFastContinuationInfos())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		options.setStartRow(50);
		assertThatGetLinkableChildConceptResponseHasResultInOrder(zeSasquatch, parent.getWrappedRecord(), ADMINISTRATIVE_UNITS,
				AdministrativeUnit.SCHEMA_TYPE, options)
				.has(resultsInOrder("unit_102", "unit_104", "unit_106", "unit_108", "unit_110", "unit_112", "unit_114",
						"unit_116", "unit_118", "unit_120", "unit_122", "unit_124", "unit_126", "unit_128", "unit_130",
						"unit_132", "unit_134", "unit_136", "unit_138", "unit_140", "unit_142", "unit_144", "unit_146",
						"unit_148", "unit_150", "unit_152", "unit_154", "unit_156", "unit_158", "unit_160", "unit_162",
						"unit_164", "unit_166", "unit_168", "unit_170", "unit_172", "unit_174", "unit_176", "unit_178",
						"unit_180", "unit_182", "unit_184", "unit_186", "unit_188", "unit_190", "unit_192", "unit_194",
						"unit_196", "unit_198", "unit_200"))
				.has(numFound(150)).has(listSize(50)).has(noFastContinuationInfos())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

	}

	//@Test
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
				.has(numFound(400)).has(listSize(20)).has(fastContinuationInfos(false, 90))
				.has(solrQueryCounts(2, 2, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(89).setRows(20)
				.setFastContinueInfos(null))
				.has(resultsInOrder("category_90", "category_91", "category_92", "category_93", "category_94", "category_95",
						"category_96", "category_97", "category_98", "category_99", "category_100", "zeFolder1", "zeFolder2",
						"zeFolder3", "zeFolder4", "zeFolder5", "zeFolder6", "zeFolder7", "zeFolder8", "zeFolder9"))
				.has(numFound(400)).has(listSize(20))
				.has(fastContinuationInfos(true, 9))
				.has(solrQueryCounts(2, 2, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(90).setRows(20)
				.setFastContinueInfos(new FastContinueInfos(false, 90, new ArrayList<String>())))
				.has(resultsInOrder("category_91", "category_92", "category_93", "category_94", "category_95", "category_96",
						"category_97", "category_98", "category_99", "category_100", "zeFolder1", "zeFolder2", "zeFolder3",
						"zeFolder4", "zeFolder5", "zeFolder6", "zeFolder7", "zeFolder8", "zeFolder9", "zeFolder10"))
				.has(numFound(400)).has(listSize(20))
				.has(fastContinuationInfos(true, 9, "zeFolder10"))
				.has(solrQueryCounts(2, 2, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(90).setRows(20)
				.setFastContinueInfos(new FastContinueInfos(false, 91, new ArrayList<String>())))
				.has(resultsInOrder("category_92", "category_93", "category_94", "category_95", "category_96", "category_97",
						"category_98", "category_99", "category_100", "zeFolder1", "zeFolder2", "zeFolder3", "zeFolder4",
						"zeFolder5", "zeFolder6", "zeFolder7", "zeFolder8", "zeFolder9", "zeFolder10", "zeFolder11"))
				.has(numFound(399)).has(listSize(20))
				.has(fastContinuationInfos(true, 10, "zeFolder10"))
				.has(solrQueryCounts(2, 2, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(90).setRows(20)
				.setFastContinueInfos(null))
				.has(resultsInOrder("category_91", "category_92", "category_93", "category_94", "category_95", "category_96",
						"category_97", "category_98", "category_99", "category_100", "zeFolder1", "zeFolder2", "zeFolder3",
						"zeFolder4", "zeFolder5", "zeFolder6", "zeFolder7", "zeFolder8", "zeFolder9", "zeFolder10"))
				.has(numFound(400)).has(listSize(20))
				.has(fastContinuationInfos(true, 9, "zeFolder10"))
				.has(solrQueryCounts(2, 2, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(91).setRows(20)
				.setFastContinueInfos(null))
				.has(resultsInOrder("category_92", "category_93", "category_94", "category_95", "category_96", "category_97",
						"category_98", "category_99", "category_100", "zeFolder1", "zeFolder2", "zeFolder3", "zeFolder4",
						"zeFolder5", "zeFolder6", "zeFolder7", "zeFolder8", "zeFolder9", "zeFolder10", "zeFolder11"))
				.has(numFound(400)).has(listSize(20))
				.has(fastContinuationInfos(true, 10, "zeFolder10"))
				.has(solrQueryCounts(2, 2, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(100).setRows(20)
				.setFastContinueInfos(null))
				.has(resultsInOrder("zeFolder1", "zeFolder2", "zeFolder3", "zeFolder4", "zeFolder5", "zeFolder6",
						"zeFolder7", "zeFolder8", "zeFolder9", "zeFolder10", "zeFolder11", "zeFolder12", "zeFolder13",
						"zeFolder14", "zeFolder15", "zeFolder16", "zeFolder17", "zeFolder18", "zeFolder19", "zeFolder20"))
				.has(numFound(400)).has(listSize(20)).has(fastContinuationInfos(true, 18, "zeFolder10", "zeFolder20"))
				.has(solrQueryCounts(2, 2, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(120).setRows(20)
				.setFastContinueInfos(new FastContinueInfos(true, 18, asList("zeFolder10", "zeFolder20"))))
				.has(resultsInOrder("zeFolder21", "zeFolder22", "zeFolder23", "zeFolder24", "zeFolder25", "zeFolder26",
						"zeFolder27", "zeFolder28", "zeFolder29", "zeFolder30", "zeFolder31", "zeFolder32", "zeFolder33",
						"zeFolder34", "zeFolder35", "zeFolder36", "zeFolder37", "zeFolder38", "zeFolder39", "zeFolder40"))
				.has(numFound(400)).has(listSize(20))
				.has(fastContinuationInfos(true, 36, "zeFolder10", "zeFolder20", "zeFolder30", "zeFolder40"))
				.has(solrQueryCounts(2, 2, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(100).setRows(25)
				.setFastContinueInfos(null))
				.has(resultsInOrder("zeFolder1", "zeFolder2", "zeFolder3", "zeFolder4", "zeFolder5", "zeFolder6",
						"zeFolder7", "zeFolder8", "zeFolder9", "zeFolder10", "zeFolder11", "zeFolder12", "zeFolder13",
						"zeFolder14", "zeFolder15", "zeFolder16", "zeFolder17", "zeFolder18", "zeFolder19", "zeFolder20",
						"zeFolder21", "zeFolder22", "zeFolder23", "zeFolder24", "zeFolder25"))
				.has(numFound(400)).has(listSize(25))
				.has(solrQueryCounts(2, 2, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(120).setRows(20)
				.setFastContinueInfos(null))
				.has(numFound(400)).has(listSize(20))
				.has(solrQueryCounts(2, 2, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(120).setRows(40)
				.setFastContinueInfos(null))
				.has(numFound(400)).has(listSize(40))
				.has(solrQueryCounts(2, 2, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(360).setRows(40)
				.setFastContinueInfos(null))
				.has(numFound(400)).has(listSize(40))
				.has(solrQueryCounts(2, 2, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(records.categoryId_Z999, options.setStartRow(360).setRows(40)
				.setFastContinueInfos(null))
				.has(resultsInOrder("zeFolder261", "zeFolder262", "zeFolder263", "zeFolder264", "zeFolder265", "zeFolder266",
						"zeFolder267", "zeFolder268", "zeFolder269", "zeFolder270", "zeFolder271", "zeFolder272", "zeFolder273",
						"zeFolder274", "zeFolder275", "zeFolder276", "zeFolder277", "zeFolder278", "zeFolder279", "zeFolder280",
						"zeFolder281", "zeFolder282", "zeFolder283", "zeFolder284", "zeFolder285", "zeFolder286", "zeFolder287",
						"zeFolder288", "zeFolder289", "zeFolder290", "zeFolder291", "zeFolder292", "zeFolder293", "zeFolder294",
						"zeFolder295", "zeFolder296", "zeFolder297", "zeFolder298", "zeFolder299", "zeFolder300"))
				.has(numFound(400)).has(listSize(40))
				.has(solrQueryCounts(2, 2, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatIterationWithAndWithoutFastContinueGivesSameResults(records.categoryId_Z999, 20);
		assertThatIterationWithAndWithoutFastContinueGivesSameResults(records.categoryId_Z999, 1);
	}

	@Test
	public void whenGetListOfRootAdministrativeUnitsUsingFilterThenReturnOnlyThoseNotFiltered() {
		givenUserHasReadAccessTo(records.unitId_12);

		TaxonomiesSearchFilter taxonomiesSearchFilter = new TaxonomiesSearchFilter();
		taxonomiesSearchFilter.setLinkableConceptsFilter(new LinkableConceptFilter() {
			@Override
			public boolean isLinkable(LinkableConceptFilterParams params) {
				return false;
			}
		});

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions();
		options.setFilter(taxonomiesSearchFilter);

		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(options)
				.has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_10, options)
				.has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		// -- New case : only units with a "2" in the code are linkable


		taxonomiesSearchFilter.setLinkableConceptsFilter(new LinkableConceptFilter() {
			@Override
			public boolean isLinkable(LinkableConceptFilterParams params) {
				return params.getRecord().<String>get(Schemas.CODE).contains("2");
			}
		});

		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(options)
				.has(resultsInOrder(records.unitId_10))
				.has(unlinkable(records.unitId_10))
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_10, options)
				.has(resultsInOrder(records.unitId_12))
				.has(linkable(records.unitId_12))
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_12, options)
				.has(resultsInOrder(records.unitId_12b, records.unitId_12c))
				.has(linkable(records.unitId_12b, records.unitId_12c))
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));
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
				.has(itemsWithChildren(records.unitId_10, records.unitId_30))
				.has(solrQueryCounts(1, 0, 3))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10)
				.has(unlinkable(records.unitId_11, records.unitId_12, records.unitId_10a))
				.has(resultsInOrder(records.unitId_10a, records.unitId_11, records.unitId_12))
				.has(itemsWithChildren(records.unitId_10a, records.unitId_11, records.unitId_12))
				.has(solrQueryCounts(2, 0, 3))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10a)
				.has(linkable(records.folder_A42, records.folder_A43, records.folder_A44))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(2, 63, 63))
				.has(secondSolrQueryCounts(2, 63, 63));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_12)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_12b))
				.has(itemsWithChildren(records.unitId_12b))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_12b)
				.has(numFoundAndListSize(10))
				.has(linkable(records.folder_B02, records.folder_B04, records.folder_B06, records.folder_B08,
						records.folder_B30,
						records.folder_B32, records.folder_B34, records.folder_B50, records.folder_B52, records.folder_B54))
				.has(resultsInOrder("B52", "B02", "B04", "B06", "B08", "B54", "B30", "B32", "B34", "B50"))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(2, 10, 10))
				.has(secondSolrQueryCounts(2, 10, 10));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.folder_B02)
				.is(empty())
				.has(solrQueryCounts(2, 1, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

	}

	@Test
	public void givenUserHaveSiteReadAccessAndFoldersDeletedForAllTreeVisibleForFolderSelectionUsingUnitTaxonomy()
			throws Exception {

		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));

		assertThatRootWhenSelectingAFolderUsingUnitTaxonomy()
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.unitId_10, records.unitId_30))
				.has(resultsInOrder(records.unitId_10, records.unitId_30))
				.has(itemsWithChildren(records.unitId_10, records.unitId_30))
				.has(solrQueryCounts(1, 0, 3))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10)
				.has(unlinkable(records.unitId_11, records.unitId_12, records.unitId_10a))
				.has(resultsInOrder(records.unitId_10a, records.unitId_11, records.unitId_12))
				.has(itemsWithChildren(records.unitId_10a, records.unitId_11, records.unitId_12))
				.has(solrQueryCounts(2, 0, 3))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_10a)
				.has(linkable(records.folder_A42, records.folder_A43, records.folder_A44))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(2, 63, 63))
				.has(secondSolrQueryCounts(2, 63, 63));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_12)
				.has(numFoundAndListSize(1))
				.has(unlinkable(records.unitId_12b))
				.has(itemsWithChildren(records.unitId_12b))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_12b)
				.has(numFoundAndListSize(10))
				.has(linkable(records.folder_B02, records.folder_B04, records.folder_B06, records.folder_B08,
						records.folder_B30,
						records.folder_B32, records.folder_B34, records.folder_B50, records.folder_B52, records.folder_B54))
				.has(resultsInOrder("B52", "B02", "B04", "B06", "B08", "B54", "B30", "B32", "B34", "B50"))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(2, 10, 10))
				.has(secondSolrQueryCounts(2, 10, 10));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.folder_B02)
				.is(empty())
				.has(solrQueryCounts(2, 1, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

		getModelLayerFactory().newRecordServices().logicallyDelete(records.getFolder_B08().getWrappedRecord(), User.GOD);

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_12b, new TaxonomiesSearchOptions()
				.setStartRow(0).setRows(4))
				.has(resultsInOrder("B52", "B02", "B04", "B06"))
				.has(noItemsWithChildren())
				.has(numFound(9)).has(listSize(4))
				.has(solrQueryCounts(2, 4, 4))
				.has(secondSolrQueryCounts(2, 4, 4));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_12b, new TaxonomiesSearchOptions()
				.setStartRow(4).setRows(4))
				.has(resultsInOrder("B54", "B30", "B32", "B34"))
				.has(noItemsWithChildren())
				.has(numFound(9)).has(listSize(4))
				.has(solrQueryCounts(2, 8, 4))
				.has(secondSolrQueryCounts(2, 8, 4));

		assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(records.unitId_12b,
				new TaxonomiesSearchOptions().setStartRow(8).setRows(4))
				.has(resultsInOrder("B50"))
				.has(noItemsWithChildren())
				.has(numFound(9)).has(listSize(1))
				.has(solrQueryCounts(2, 9, 4))
				.has(secondSolrQueryCounts(2, 9, 4));

	}

	@Test
	public void givenUserHaveSiteReadAccessForAllTreeVisibleForCategorySelectionUsingPlanTaxonomy()
			throws Exception {
		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy()
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_X))
				.has(unlinkable(records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z)
				.has(numFoundAndListSize(3))
				.has(unlinkable(records.categoryId_Z100))
				.has(linkable(records.categoryId_ZE42, records.categoryId_Z999))
				.has(resultsInOrder(records.categoryId_Z100, records.categoryId_Z999, records.categoryId_ZE42))
				.has(itemsWithChildren(records.categoryId_Z100))
				.has(solrQueryCounts(1, 0, 4))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z100)
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_Z110, records.categoryId_Z120))
				.has(resultsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(itemsWithChildren(records.categoryId_Z110))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z112)
				.is(empty())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

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
				.has(itemsWithChildren(records.unitId_10, records.unitId_20, records.unitId_30))
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(withWriteAccess)
				.is(empty())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_10)
				.has(numFoundAndListSize(3))
				.has(linkable(records.unitId_11, records.unitId_12, records.unitId_10a))
				.has(resultsInOrder(records.unitId_10a, records.unitId_11, records.unitId_12))
				.has(itemsWithChildren(records.unitId_11, records.unitId_12))
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_10, withWriteAccess)
				.is(empty())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_12)
				.has(numFoundAndListSize(2))
				.has(linkable(records.unitId_12b, records.unitId_12c))
				.has(resultsInOrder(records.unitId_12b, records.unitId_12c))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_12b)
				.is(empty())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

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
				.has(itemsWithChildren(records.unitId_10, records.unitId_20, records.unitId_30))
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_10, withWriteAccess)
				.has(numFoundAndListSize(3))
				.has(linkable(records.unitId_11, records.unitId_12, records.unitId_10a))
				.has(resultsInOrder(records.unitId_10a, records.unitId_11, records.unitId_12))
				.has(itemsWithChildren(records.unitId_11, records.unitId_12))
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_12, withWriteAccess)
				.has(numFoundAndListSize(2))
				.has(linkable(records.unitId_12b, records.unitId_12c))
				.has(resultsInOrder(records.unitId_12b, records.unitId_12c))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(records.unitId_12b, withWriteAccess)
				.is(empty())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

	}

	@Test
	public void givenLinkableCategoryCannotBeRootAndMustHaveApprovedRulesThenTheseCategoriesUnlinkable()
			throws Exception {
		givenConfig(RMConfigs.LINKABLE_CATEGORY_MUST_NOT_BE_ROOT, true);
		givenConfig(RMConfigs.LINKABLE_CATEGORY_MUST_HAVE_APPROVED_RULES, true);
		waitForBatchProcess();

		Transaction tx = new Transaction();
		tx.add(rm.newCategoryWithId("rootCategoryWithoutChild").setCode("rootCategoryWithoutChild")
				.setTitle("rootCategoryWithoutChild"));
		tx.add(rm.newCategoryWithId("rootCategoryWithChild").setCode("rootCategoryWithChild").setTitle("rootCategoryWithChild"));
		tx.add(rm.newCategoryWithId("childCategory").setCode("childCategory").setTitle("childCategory")
				.setParent("rootCategoryWithChild"));
		recordServices.execute(tx);

		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy()
				.has(numFoundAndListSize(2))
				.has(unlinkable(records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(solrQueryCounts(1, 0, 4))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z)
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_ZE42))
				.has(unlinkable(records.categoryId_Z100))
				.has(resultsInOrder(records.categoryId_Z100, records.categoryId_ZE42))
				.has(itemsWithChildren(records.categoryId_Z100))
				.has(solrQueryCounts(1, 0, 4))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z100)
				.has(numFoundAndListSize(1))
				.has(linkable(records.categoryId_Z110))
				.has(resultsInOrder(records.categoryId_Z110))
				.has(noItemsWithChildren())
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z112)
				.is(empty())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

	}

	@Test
	public void whenSelectingACategoryNoMatterItsLinkableStatusThenReturnGoodResults()
			throws Exception {
		givenConfig(RMConfigs.LINKABLE_CATEGORY_MUST_NOT_BE_ROOT, true);
		givenConfig(RMConfigs.LINKABLE_CATEGORY_MUST_HAVE_APPROVED_RULES, true);
		waitForBatchProcess();

		Transaction tx = new Transaction();
		tx.add(rm.newCategoryWithId("rootCategoryWithoutChild").setCode("rootCategoryWithoutChild")
				.setTitle("rootCategoryWithoutChild"));
		tx.add(rm.newCategoryWithId("rootCategoryWithChild").setCode("rootCategoryWithChild").setTitle("rootCategoryWithChild"));
		tx.add(rm.newCategoryWithId("childCategory").setCode("childCategory").setTitle("childCategory")
				.setParent("rootCategoryWithChild"));
		tx.add(rm.newCategoryWithId("childChildCategory").setCode("childChildCategory").setTitle("childChildCategory")
				.setParent("childCategory"));
		recordServices.execute(tx);

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions()
				.setAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable(true);

		assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(options)
				.has(numFoundAndListSize(4))
				.has(linkable("rootCategoryWithChild", "rootCategoryWithoutChild", records.categoryId_X, records.categoryId_Z))
				.has(resultsInOrder("rootCategoryWithChild", "rootCategoryWithoutChild", records.categoryId_X,
						records.categoryId_Z))
				.has(itemsWithChildren("rootCategoryWithChild", records.categoryId_X, records.categoryId_Z))
				.has(solrQueryCounts(1, 0, 4))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy("rootCategoryWithChild", options)
				.has(numFoundAndListSize(1))
				.has(resultsInOrder("childCategory"))
				.has(itemsWithChildren("childCategory"))
				.has(linkable("childCategory"))
				.has(solrQueryCounts(1, 0, 1))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy("childCategory", options)
				.has(numFoundAndListSize(1))
				.has(resultsInOrder("childChildCategory"))
				.has(noItemsWithChildren())
				.has(linkable("childChildCategory"))
				.has(solrQueryCounts(1, 0, 1))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z, options)
				.has(numFoundAndListSize(4))
				.has(resultsInOrder(records.categoryId_Z100, records.categoryId_Z200, records.categoryId_Z999,
						records.categoryId_ZE42))
				.has(itemsWithChildren(records.categoryId_Z100))
				.has(linkable(records.categoryId_Z100, records.categoryId_Z200, records.categoryId_Z999,
						records.categoryId_ZE42))
				.has(solrQueryCounts(1, 0, 4))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z100, options)
				.has(resultsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(itemsWithChildren(records.categoryId_Z110))
				.has(numFoundAndListSize(2))
				.has(linkable(records.categoryId_Z110))
				.has(solrQueryCounts(1, 0, 2))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(records.categoryId_Z112, options)
				.is(empty())
				.has(solrQueryCounts(0, 0, 0))
				.has(secondSolrQueryCounts(0, 0, 0));

	}

	@Test
	public void whenAdminIsSelectingAFolderAlwaysDisplayingConceptsWithReadAccessThenSeesRecordsAndAllConcepts()
			throws Exception {

		recordServices.add(rm.newCategoryWithId("category_Y_id").setCode("Y").setTitle("Ze category Y"));

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions()
				.setAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable(true)
				.setShowInvisibleRecordsInLinkingMode(false);

		assertThatRootWhenSelectingFolderUsingPlanTaxonomy(records.getAdmin(), options)
				.has(resultsInOrder(records.categoryId_X, "category_Y_id", records.categoryId_Z))
				.has(itemsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(numFoundAndListSize(3))
				.has(solrQueryCounts(1, 0, 3))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingFolderUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X, options)
				.has(resultsInOrder(records.categoryId_X13, records.categoryId_X100))
				.has(itemsWithChildren(records.categoryId_X13, records.categoryId_X100))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingFolderUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100, options)
				.has(resultsInOrder("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06", "C32", "B32"))
				.has(itemsWithChildren("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06", "C32", "B32"))
				.has(numFoundAndListSize(9))
				.has(solrQueryCounts(1, 7, 0))
				.has(secondSolrQueryCounts(1, 7, 0));

		assertThatChildWhenSelectingFolderUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z, options)
				.has(resultsInOrder(records.categoryId_Z100, records.categoryId_Z200, records.categoryId_Z999,
						records.categoryId_ZE42))
				.has(itemsWithChildren(records.categoryId_Z100, records.categoryId_Z200, records.categoryId_Z999,
						records.categoryId_ZE42))
				.has(numFoundAndListSize(4))
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingFolderUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z100, options)
				.has(resultsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(itemsWithChildren(records.categoryId_Z110, records.categoryId_Z120))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingFolderUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z110, options)
				.has(resultsInOrder(records.categoryId_Z111, records.categoryId_Z112))
				.has(itemsWithChildren(records.categoryId_Z111, records.categoryId_Z112))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

	}

	@Test
	public void whenUserIsNavigatingAdminUnitTaxonomyAlwaysDisplayingConceptsWithReadAccessThenOnlySeeConceptsContainingAccessibleRecordsAndThoseWithReadAccess()
			throws Exception {

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions()
				.setAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable(true);
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
		//Sasquatch
		assertThatRootWhenSelectingFolderUsingAdministrativeUnitsTaxonomy(sasquatch, options)
				.has(resultsInOrder(records.unitId_10, records.unitId_20))
				.has(itemsWithChildren(records.unitId_10, records.unitId_20))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(1, 0, 3))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingFolderUsingAdminUnitsTaxonomy(sasquatch, records.unitId_10, options)
				.has(resultsInOrder(records.unitId_12))
				.has(itemsWithChildren(records.unitId_12))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(2, 0, 3))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingFolderUsingAdminUnitsTaxonomy(sasquatch, records.unitId_12, options)
				.has(resultsInOrder(records.unitId_12b))
				.has(itemsWithChildren(records.unitId_12b))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingFolderUsingAdminUnitsTaxonomy(sasquatch, records.unitId_12b, options)
				.has(resultsInOrder("B06"))
				.has(noItemsWithChildren())
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(2, 1, 1))
				.has(secondSolrQueryCounts(2, 1, 1));

		assertThatChildWhenSelectingFolderUsingAdminUnitsTaxonomy(sasquatch, records.unitId_12c, options)
				.has(numFoundAndListSize(0))
				.has(solrQueryCounts(1, 0, 0))
				.has(secondSolrQueryCounts(1, 0, 0));

		//Robin
		assertThatRootWhenSelectingFolderUsingAdministrativeUnitsTaxonomy(robin, options)
				.has(resultsInOrder(records.unitId_10, records.unitId_30))
				.has(itemsWithChildren(records.unitId_10, records.unitId_30))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(1, 0, 3))
				.has(secondSolrQueryCounts(0, 0, 0));

		assertThatChildWhenSelectingFolderUsingAdminUnitsTaxonomy(robin, records.unitId_10, options)
				.has(resultsInOrder(records.unitId_12))
				.has(itemsWithChildren(records.unitId_12))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(2, 0, 3))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingFolderUsingAdminUnitsTaxonomy(robin, records.unitId_12, options)
				.has(resultsInOrder(records.unitId_12b, records.unitId_12c))
				.has(itemsWithChildren(records.unitId_12b))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(2, 0, 2))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingFolderUsingAdminUnitsTaxonomy(robin, records.unitId_30, options)
				.has(resultsInOrder(records.unitId_30c))
				.has(itemsWithChildren(records.unitId_30c))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(2, 0, 1))
				.has(secondSolrQueryCounts(1, 0, 0));

		assertThatChildWhenSelectingFolderUsingAdminUnitsTaxonomy(robin, records.unitId_12b, options)
				.has(resultsInOrder("B06"))
				.has(noItemsWithChildren())
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(2, 1, 1))
				.has(secondSolrQueryCounts(2, 1, 1));

	}

	// -------

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatChildWhenSelectingFolderUsingAdminUnitsTaxonomy(
			final User user,
			final String category, final TaxonomiesSearchOptions options) {

		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {

			@Override
			protected LinkableTaxonomySearchResponse call() {
				Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
				LinkableTaxonomySearchResponse response = service
						.getLinkableChildConceptResponse(user, inRecord, RMTaxonomies.ADMINISTRATIVE_UNITS, Folder.SCHEMA_TYPE,
								options);

				if (options.getRows() == 10000) {
					assertThat(response.getNumFound()).isEqualTo(response.getRecords().size());
				}
				return response;
			}
		});
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatRootWhenSelectingFolderUsingAdministrativeUnitsTaxonomy(
			final User user,
			final TaxonomiesSearchOptions options) {

		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {

			@Override
			protected LinkableTaxonomySearchResponse call() {
				LinkableTaxonomySearchResponse response = service.getLinkableRootConceptResponse(
						user, zeCollection, RMTaxonomies.ADMINISTRATIVE_UNITS, Folder.SCHEMA_TYPE, options);

				if (options.getRows() == 10000) {
					assertThat(response.getNumFound()).isEqualTo(response.getRecords().size());
				}
				return response;
			}
		});
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatRootWhenSelectingFolderUsingPlanTaxonomy(
			final User user,
			final TaxonomiesSearchOptions options) {
		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {
			@Override
			protected LinkableTaxonomySearchResponse call() {
				LinkableTaxonomySearchResponse response = service.getLinkableRootConceptResponse(
						user, zeCollection, CLASSIFICATION_PLAN, Folder.SCHEMA_TYPE, options);

				options.setHasChildrenFlagCalculated(NEVER);
				if (options.getRows() == 10000) {
					assertThat(response.getNumFound()).isEqualTo(response.getRecords().size());
				}
				return response;
			}
		});
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatChildWhenSelectingFolderUsingPlanTaxonomy(
			User user,
			String category, TaxonomiesSearchOptions options) {
		return assertThatChildWhenSelectingFolderUsingPlanTaxonomy(user, category, options, 0, 10000);
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatChildWhenSelectingFolderUsingPlanTaxonomy(
			final User user,
			final String category, final TaxonomiesSearchOptions options, final int start, final int rows) {

		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {
			@Override
			protected LinkableTaxonomySearchResponse call() {

				Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
				LinkableTaxonomySearchResponse response = service.getLinkableChildConceptResponse(
						user, inRecord, CLASSIFICATION_PLAN, Folder.SCHEMA_TYPE,
						new TaxonomiesSearchOptions(options).setStartRow(start).setRows(rows)
								.setHasChildrenFlagCalculated(NEVER));
				if (rows == 10000) {
					assertThat(response.getNumFound()).isEqualTo(response.getRecords().size());
				}
				return response;
			}
		});
	}

	private Condition<? super LinkableTaxonomySearchResponseCaller> empty() {
		return numFound(0);
	}

	private Condition<? super LinkableTaxonomySearchResponseCaller> numFoundAndListSize(final int expectedCount) {
		return new Condition<LinkableTaxonomySearchResponseCaller>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponseCaller value) {
				assertThat(value.firstAnswer().getNumFound()).describedAs("first answer NumFound").isEqualTo(expectedCount);
				assertThat(value.firstAnswer().getRecords().size()).describedAs("first answer records list size")
						.isEqualTo(expectedCount);

				assertThat(value.secondAnswer().getNumFound()).describedAs("second answer NumFound").isEqualTo(expectedCount);
				assertThat(value.secondAnswer().getRecords().size()).describedAs("second answer records list size")
						.isEqualTo(expectedCount);
				return true;
			}
		};
	}

	private Condition<? super LinkableTaxonomySearchResponseCaller> numFound(final int expectedCount) {
		return new Condition<LinkableTaxonomySearchResponseCaller>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponseCaller value) {
				assertThat(value.firstAnswer().getNumFound()).describedAs("first answer NumFound").isEqualTo(expectedCount);

				assertThat(value.secondAnswer().getNumFound()).describedAs("second answer NumFound").isEqualTo(expectedCount);
				return true;
			}
		};
	}

	private Condition<? super LinkableTaxonomySearchResponseCaller> listSize(final int expectedCount) {
		return new Condition<LinkableTaxonomySearchResponseCaller>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponseCaller value) {
				assertThat(value.firstAnswer().getRecords().size()).describedAs("first answer records list size")
						.isEqualTo(expectedCount);

				assertThat(value.secondAnswer().getRecords().size()).describedAs("second answer records list size")
						.isEqualTo(expectedCount);
				return true;
			}
		};
	}

	private Condition<? super LinkableTaxonomySearchResponseCaller> validOrder() {
		return new Condition<LinkableTaxonomySearchResponseCaller>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponseCaller response) {

				List<Record> actualRecords = new ArrayList<>();
				List<Record> recordsInExpectedOrder = new ArrayList<>();

				for (TaxonomySearchRecord value : response.firstAnswer().getRecords()) {
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

				assertThat(actualRecords).describedAs("second answer ").isEqualTo(recordsInExpectedOrder);

				List<Record> actualRecordsSecondAnswer = new ArrayList<>();
				List<Record> recordsInExpectedOrderSecondAnswer = new ArrayList<>();

				for (TaxonomySearchRecord value : response.firstAnswer().getRecords()) {
					actualRecordsSecondAnswer.add(value.getRecord());
					recordsInExpectedOrderSecondAnswer.add(value.getRecord());
				}

				final List<String> typesOrderSecondAnswer = asList(Category.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE,
						ContainerRecord.SCHEMA_TYPE, Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE);

				Collections.sort(recordsInExpectedOrderSecondAnswer, new Comparator<Record>() {
					@Override
					public int compare(Record r1, Record r2) {

						int r1TypeIndex = typesOrderSecondAnswer.indexOf(new SchemaUtils().getSchemaTypeCode(r1.getSchemaCode()));
						int r2TypeIndex = typesOrderSecondAnswer.indexOf(new SchemaUtils().getSchemaTypeCode(r2.getSchemaCode()));

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

				assertThat(actualRecordsSecondAnswer).describedAs("second answer ").isEqualTo(recordsInExpectedOrderSecondAnswer);
				return true;
			}
		};
	}

	private Condition<? super LinkableTaxonomySearchResponseCaller> noItemsWithChildren() {
		return itemsWithChildren();
	}

	private Condition<? super LinkableTaxonomySearchResponseCaller> itemsWithChildren(final String... ids) {
		final List<String> idsList = asList(ids);
		return new Condition<LinkableTaxonomySearchResponseCaller>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponseCaller response) {

				for (TaxonomySearchRecord record : response.firstAnswer().getRecords()) {
					BooleanAssert assertion = assertThat(record.hasChildren()).describedAs(
							"second answer Record '" + record.getRecord().getId() + "' has children");
					if (idsList.contains(record.getRecord().getId())) {
						assertion.isTrue();
					} else {
						assertion.isFalse();
					}
				}

				for (TaxonomySearchRecord record : response.secondAnswer().getRecords()) {
					BooleanAssert assertion = assertThat(record.hasChildren()).describedAs(
							"second answer Record '" + record.getRecord().getId() + "' has children");
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

	private Condition<? super LinkableTaxonomySearchResponseCaller> unlinkable(final String... ids) {
		return new Condition<LinkableTaxonomySearchResponseCaller>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponseCaller response) {

				for (String id : ids) {
					TaxonomySearchRecord foundRecord = null;
					for (TaxonomySearchRecord record : response.firstAnswer().getRecords()) {
						if (id.equals(record.getRecord().getId())) {
							if (foundRecord != null) {
								throw new RuntimeException("Same record found twice");
							}
							foundRecord = record;
						}
					}
					assertThat(foundRecord).describedAs("Result for first awnser '" + id + "'").isNotNull();
					assertThat(foundRecord.isLinkable()).describedAs("is record '" + id + "' linkable").isFalse();

				}

				for (String id : ids) {
					TaxonomySearchRecord foundRecord = null;
					for (TaxonomySearchRecord record : response.secondAnswer().getRecords()) {
						if (id.equals(record.getRecord().getId())) {
							if (foundRecord != null) {
								throw new RuntimeException("Same record found twice");
							}
							foundRecord = record;
						}
					}
					assertThat(foundRecord).describedAs("Result for second answer '" + id + "'").isNotNull();
					assertThat(foundRecord.isLinkable()).describedAs("is record '" + id + "' linkable").isFalse();

				}

				return true;
			}
		}.describedAs("unlinkable " + ids);
	}

	private Condition<? super LinkableTaxonomySearchResponseCaller> linkable(final String... ids) {
		return new Condition<LinkableTaxonomySearchResponseCaller>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponseCaller response) {

				for (String id : ids) {
					TaxonomySearchRecord foundRecord = null;
					for (TaxonomySearchRecord record : response.firstAnswer().getRecords()) {
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

				for (String id : ids) {
					TaxonomySearchRecord foundRecord = null;
					for (TaxonomySearchRecord record : response.secondAnswer().getRecords()) {
						if (id.equals(record.getRecord().getId())) {
							if (foundRecord != null) {
								throw new RuntimeException("Same record found twice");
							}
							foundRecord = record;
						}
					}
					assertThat(foundRecord).describedAs("second answer Result '" + id + "'").isNotNull();
					assertThat(foundRecord.isLinkable()).describedAs("second answer is record '" + id + "' linkable").isTrue();

				}

				return true;
			}
		}.describedAs("linkable " + ids);
	}

	private Condition<? super LinkableTaxonomySearchResponseCaller> resultsInOrder(final String... ids) {
		return new Condition<LinkableTaxonomySearchResponseCaller>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponseCaller response) {

				List<String> responseRecords = new ArrayList();
				List<String> codeOrTitle = new ArrayList<>();
				for (TaxonomySearchRecord record : response.firstAnswer().getRecords()) {
					responseRecords.add(record.getRecord().getId());
					if (record.getRecord().get(Schemas.CODE) == null) {
						codeOrTitle.add((String) record.getRecord().get(Schemas.TITLE));
					} else {
						codeOrTitle.add((String) record.getRecord().get(Schemas.CODE));
					}
				}

				assertThat(responseRecords).describedAs("second answer Results in correct order : " + codeOrTitle)
						.isEqualTo(asList(ids));

				List<String> responseRecordsSecondAnswer = new ArrayList();
				List<String> codeOrTitleSecondAnswer = new ArrayList<>();
				for (TaxonomySearchRecord record : response.secondAnswer().getRecords()) {
					responseRecordsSecondAnswer.add(record.getRecord().getId());
					if (record.getRecord().get(Schemas.CODE) == null) {
						codeOrTitleSecondAnswer.add((String) record.getRecord().get(Schemas.TITLE));
					} else {
						codeOrTitleSecondAnswer.add((String) record.getRecord().get(Schemas.CODE));
					}
				}

				assertThat(responseRecordsSecondAnswer)
						.describedAs("second answer Results in correct order : " + codeOrTitleSecondAnswer)
						.isEqualTo(asList(ids));

				return true;
			}
		};
	}

	private void givenUserHasReadAccessTo(String... ids) {
		for (String id : ids) {
			getModelLayerFactory().newAuthorizationsServices().add(authorizationForUsers(alice).on(id).givingReadAccess());
		}
		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		alice = getModelLayerFactory().newUserServices().getUserInCollection(aliceWonderland, zeCollection);
	}

	private void givenUserHasNegativeReadAccessTo(String... ids) {
		for (String id : ids) {
			getModelLayerFactory().newAuthorizationsServices().add(authorizationForUsers(alice).on(id).givingNegativeReadAccess());
		}
		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
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

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatRootWhenSelectingADocumentUsingPlanTaxonomy() {
		return assertThatRootWhenSelectingADocumentUsingPlanTaxonomy(new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatRootWhenSelectingADocumentUsingPlanTaxonomy(
			final TaxonomiesSearchOptions options) {
		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {

			@Override
			protected LinkableTaxonomySearchResponse call() {
				return service
						.getLinkableRootConceptResponse(alice, zeCollection, CLASSIFICATION_PLAN, Document.SCHEMA_TYPE, options);
			}
		});
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(
			String category) {
		return assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(category, new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(
			final String category, final TaxonomiesSearchOptions options) {

		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {
			@Override
			protected LinkableTaxonomySearchResponse call() {
				Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
				LinkableTaxonomySearchResponse response = service.getLinkableChildConceptResponse(alice, inRecord,
						CLASSIFICATION_PLAN, Document.SCHEMA_TYPE, options);
				return response;
			}
		});

	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatRootWhenSelectingAFolderUsingPlanTaxonomy() {
		return assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatRootWhenSelectingAFolderUsingPlanTaxonomy(
			final TaxonomiesSearchOptions options) {
		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {
			@Override
			protected LinkableTaxonomySearchResponse call() {
				return service
						.getLinkableRootConceptResponse(alice, zeCollection, CLASSIFICATION_PLAN, Folder.SCHEMA_TYPE, options);
			}
		});
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(
			String category) {
		return assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(category, new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(
			final String category, final TaxonomiesSearchOptions options) {
		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {
			@Override
			protected LinkableTaxonomySearchResponse call() {
				Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
				LinkableTaxonomySearchResponse response = service.getLinkableChildConceptResponse(alice, inRecord,
						CLASSIFICATION_PLAN, Folder.SCHEMA_TYPE, options);
				return response;
			}
		});
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatChildWhenSelectingAFolderUsingPlanTaxonomy(
			final String category, final User user) {
		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {
			@Override
			protected LinkableTaxonomySearchResponse call() {
				Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
				return service.getLinkableChildConceptResponse(user, inRecord,
						CLASSIFICATION_PLAN, Folder.SCHEMA_TYPE, new TaxonomiesSearchOptions());
			}
		});
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatChildWhenSelectingADocumentUsingPlanTaxonomy(
			final String category, final User user) {
		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {
			@Override
			protected LinkableTaxonomySearchResponse call() {
				Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
				return service.getLinkableChildConceptResponse(user, inRecord,
						CLASSIFICATION_PLAN, Document.SCHEMA_TYPE, new TaxonomiesSearchOptions());
			}
		});
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatRootWhenSelectingACategoryUsingPlanTaxonomy() {
		return assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(
			TaxonomiesSearchFilter taxonomiesSearchFilter) {
		return assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(
				new TaxonomiesSearchOptions().setFilter(taxonomiesSearchFilter));
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatRootWhenSelectingAUnitUsingAdministrativeUnitTaxonomy() {
		return assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatRootWhenSelectingACategoryUsingPlanTaxonomy(
			final TaxonomiesSearchOptions options) {
		LinkableTaxonomySearchResponseCaller caller = (LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {

			@Override
			protected LinkableTaxonomySearchResponse call() {
				return service
						.getLinkableRootConceptResponse(alice, zeCollection, CLASSIFICATION_PLAN, Category.SCHEMA_TYPE, options);
			}
		};
		caller.firstAnswer();
		return assertThat(caller);
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(
			String category) {
		return assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(category, new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatChildWhenSelectingACategoryUsingPlanTaxonomy(
			final String category, final TaxonomiesSearchOptions taxonomiesSearchOptions) {
		LinkableTaxonomySearchResponseCaller caller = (LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {
			@Override
			protected LinkableTaxonomySearchResponse call() {
				Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
				return service.getLinkableChildConceptResponse(alice, inRecord, CLASSIFICATION_PLAN, Category.SCHEMA_TYPE,
						taxonomiesSearchOptions);
			}
		};
		caller.firstAnswer();
		return assertThat(caller);
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatRootWhenSelectingAFolderUsingUnitTaxonomy() {
		return assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatRootWhenSelectingAFolderUsingUnitTaxonomy(
			final TaxonomiesSearchOptions options) {
		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {
			@Override
			protected LinkableTaxonomySearchResponse call() {

				LinkableTaxonomySearchResponse response = service
						.getLinkableRootConceptResponse(alice, zeCollection, ADMINISTRATIVE_UNITS, Folder.SCHEMA_TYPE, options);
				return response;
			}
		});
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(
			String admUnit) {
		return assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(admUnit, new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatChildWhenSelectingAFolderUsingUnitTaxonomy(
			final String admUnit, final TaxonomiesSearchOptions options) {
		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {
			@Override
			protected LinkableTaxonomySearchResponse call() {
				Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(admUnit);
				LinkableTaxonomySearchResponse response = service
						.getLinkableChildConceptResponse(alice, inRecord, RMTaxonomies.ADMINISTRATIVE_UNITS, Folder.SCHEMA_TYPE,
								options);
				return response;
			}
		});
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy() {
		return assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatRootWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(
			final TaxonomiesSearchOptions options) {
		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {

			@Override
			protected LinkableTaxonomySearchResponse call() {
				return service
						.getLinkableRootConceptResponse(alice, zeCollection, ADMINISTRATIVE_UNITS, AdministrativeUnit.SCHEMA_TYPE,
								options);
			}
		});
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatGetLinkableChildConceptResponseHasResultInOrder(
			final User user, final Record record,
			final String unit, final String schema, final TaxonomiesSearchOptions options) {
		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {
			@Override
			protected LinkableTaxonomySearchResponse call() {
				return service.getLinkableChildConceptResponse(user, record, unit, schema, options);
			}
		});
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(
			String admUnit) {
		return assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(admUnit,
				new TaxonomiesSearchOptions());
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatChildWhenSelectingAnAdministrativeUnitUsingUnitTaxonomy(
			final String admUnit, final TaxonomiesSearchOptions options) {
		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {
			@Override
			protected LinkableTaxonomySearchResponse call() {
				Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(admUnit);
				return service.getLinkableChildConceptResponse(alice, inRecord, RMTaxonomies.ADMINISTRATIVE_UNITS,
						AdministrativeUnit.SCHEMA_TYPE, options);
			}
		});
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatgetLinkableRootConceptResponseReturnValueInOrder(
			final User user, final String collection,
			final String unit, final String schema, final TaxonomiesSearchOptions options) {
		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {
			@Override
			protected LinkableTaxonomySearchResponse call() {
				return service.getLinkableRootConceptResponse(user, collection, unit,
						schema, options);
			}
		});
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

	private Condition<? super LinkableTaxonomySearchResponseCaller> fastContinuationInfos(
			final boolean expectedinishedIteratingOverConcepts,
			final int expectedLastReturnRecordIndex, String... ids) {

		final List<String> expectedIds = asList(ids);

		return new Condition<LinkableTaxonomySearchResponseCaller>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponseCaller value) {

				assertThat(value.firstAnswer().getFastContinueInfos().getShownRecordsWithVisibleChildren())
						.describedAs("notYetShownRecordsWithVisibleChildren").isEqualTo(expectedIds);

				assertThat(value.firstAnswer().getFastContinueInfos().finishedConceptsIteration)
						.describedAs("notYetShownRecordsWithVisibleChildren").isEqualTo(expectedinishedIteratingOverConcepts);

				assertThat(value.firstAnswer().getFastContinueInfos().getLastReturnRecordIndex())
						.describedAs("lastReturnRecordIndex").isEqualTo(expectedLastReturnRecordIndex);

				assertThat(value.secondAnswer().getFastContinueInfos().getShownRecordsWithVisibleChildren())
						.describedAs("secondAnswer notYetShownRecordsWithVisibleChildren").isEqualTo(expectedIds);

				assertThat(value.secondAnswer().getFastContinueInfos().finishedConceptsIteration)
						.describedAs("secondAnswer notYetShownRecordsWithVisibleChildren")
						.isEqualTo(expectedinishedIteratingOverConcepts);

				assertThat(value.secondAnswer().getFastContinueInfos().getLastReturnRecordIndex())
						.describedAs("secondAnswer lastReturnRecordIndex").isEqualTo(expectedLastReturnRecordIndex);
				return true;
			}
		};
	}

	private Condition<? super LinkableTaxonomySearchResponseCaller> noFastContinuationInfos() {

		return new Condition<LinkableTaxonomySearchResponseCaller>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponseCaller value) {

				assertThat(value.firstAnswer().getFastContinueInfos()).describedAs("first answer").isNull();

				assertThat(value.secondAnswer().getFastContinueInfos()).describedAs("second answer").isNull();
				return true;
			}
		};
	}

	private abstract class LinkableTaxonomySearchResponseCaller {

		private LinkableTaxonomySearchResponse firstCallAnswer;

		private LinkableTaxonomySearchResponse secondCallAnswer;

		private String firstCallSolrQueries;

		private String secondCallSolrQueries;

		public LinkableTaxonomySearchResponse firstAnswer() {
			if (firstCallAnswer == null) {
				queriesCount.set(0);
				returnedDocumentsCount.set(0);
				facetsCount.set(0);
				firstCallAnswer = call();
				firstCallSolrQueries = queriesCount.get() + "-" + returnedDocumentsCount.get() + "-" + facetsCount.get();
				queriesCount.set(0);
				returnedDocumentsCount.set(0);
				facetsCount.set(0);
			}
			return firstCallAnswer;
		}

		public LinkableTaxonomySearchResponse secondAnswer() {
			firstAnswer();
			if (secondCallAnswer == null) {
				queriesCount.set(0);
				returnedDocumentsCount.set(0);
				facetsCount.set(0);
				secondCallAnswer = call();
				secondCallSolrQueries = queriesCount.get() + "-" + returnedDocumentsCount.get() + "-" + facetsCount.get();
				queriesCount.set(0);
				returnedDocumentsCount.set(0);
				facetsCount.set(0);
			}
			return secondCallAnswer;
		}

		protected abstract LinkableTaxonomySearchResponse call();

		public String firstAnswerSolrQueries() {
			firstAnswer();
			return firstCallSolrQueries;
		}

		public String secondAnswerSolrQueries() {
			secondAnswer();
			return secondCallSolrQueries;
		}

	}

	private Condition<? super LinkableTaxonomySearchResponseCaller> solrQueryCounts(final int queries,
																					final int queryResults,
																					final int facets) {
		final Exception exception = new Exception();
		return new Condition<LinkableTaxonomySearchResponseCaller>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponseCaller value) {
				String expected = queries + "-" + queryResults + "-" + facets;
				String current = value.firstAnswerSolrQueries();

				if (VALIDATE_SOLR_QUERIES_COUNT && !ajustIfBetterThanExpected(exception.getStackTrace(), current, expected)) {
					assertThat(current).describedAs("First call Queries count - Query resuts count - Facets count")
							.isEqualTo(expected);
				}
				queriesCount.set(0);
				facetsCount.set(0);
				returnedDocumentsCount.set(0);
				System.out.println("--------- --------- --------- --------- --------- --------- ---------");
				return true;
			}
		};
	}

	private Condition<? super LinkableTaxonomySearchResponseCaller> secondSolrQueryCounts(final int queries,
																						  final int queryResults,
																						  final int facets) {
		final Exception exception = new Exception();
		return new Condition<LinkableTaxonomySearchResponseCaller>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponseCaller value) {
				String expected = queries + "-" + queryResults + "-" + facets;
				String current = value.secondAnswerSolrQueries();

				if (VALIDATE_SOLR_QUERIES_COUNT && !ajustIfBetterThanExpected(exception.getStackTrace(), current, expected)) {
					assertThat(current).describedAs("Second call Queries count - Query resuts count - Facets count")
							.isEqualTo(expected);
				}
				queriesCount.set(0);
				facetsCount.set(0);
				returnedDocumentsCount.set(0);
				System.out.println("--------- --------- --------- --------- --------- --------- ---------");
				return true;
			}
		};
	}

	public StorageSpace buildStorageSpace() {
		StorageSpace storageSpace = rm.newStorageSpace().setTitle("storageTest");
		storageSpace.setCode(storageSpace.getId());
		return storageSpace;
	}

	public static boolean canStorageSpaceContainContainer(StorageSpace storageSpace, Double containerCapacity) {
		return storageSpace.getTitle().equals("storageTest") && storageSpace.getCapacity() != null
			   && storageSpace.getCapacity() > containerCapacity;
	}

	public void cleanStorageSpaces() {
		RecordDao recordDao = getAppLayerFactory().getModelLayerFactory().getDataLayerFactory().newRecordDao();
		TransactionDTO transaction = new TransactionDTO(RecordsFlushing.NOW());
		ModifiableSolrParams modifiableSolrParams = new ModifiableSolrParams();
		modifiableSolrParams.set("q", "schema_s:storageSpace*");
		transaction = transaction.withDeletedByQueries(modifiableSolrParams);
		try {
			recordDao.execute(transaction);
		} catch (RecordDaoException.OptimisticLocking optimisticLocking) {
			optimisticLocking.printStackTrace();
		}
		getModelLayerFactory().getRecordsCaches().getCache(zeCollection).invalidateVolatileReloadPermanent(asList("storageSpace"));
	}

	@Override
	protected void givenConfig(SystemConfiguration config, Object value) {
		super.givenConfig(config, value);
		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		queriesCount.set(0);
		facetsCount.set(0);
		returnedDocumentsCount.set(0);

	}
}
