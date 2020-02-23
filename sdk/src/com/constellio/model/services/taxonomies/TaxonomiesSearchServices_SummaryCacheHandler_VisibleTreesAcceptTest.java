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
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.dao.services.idGenerator.ZeroPaddedSequentialUniqueIdGenerator;
import com.constellio.data.extensions.AfterQueryParams;
import com.constellio.data.extensions.BigVaultServerExtension;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.ConditionTemplate;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.setups.Users;
import org.apache.solr.common.params.SolrParams;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ObjectAssert;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.app.modules.rm.constants.RMTaxonomies.ADMINISTRATIVE_UNITS;
import static com.constellio.app.modules.rm.constants.RMTaxonomies.CLASSIFICATION_PLAN;
import static com.constellio.data.dao.dto.records.OptimisticLockingResolution.EXCEPTION;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.taxonomies.TaxonomiesSearchOptions.HasChildrenFlagCalculated.NEVER;
import static com.constellio.model.services.taxonomies.TaxonomiesTestsUtils.createFoldersAndDocumentsWithNegativeAuths;
import static com.constellio.model.services.taxonomies.TaxonomiesTestsUtils.createFoldersAndSubFoldersWithNegativeAuths;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class TaxonomiesSearchServices_SummaryCacheHandler_VisibleTreesAcceptTest extends AbstractTaxonomiesSearchServicesAcceptanceTest {


	String subFolderId;

	Users users = new Users();
	User alice;
	DecommissioningService decommissioningService;
	TaxonomiesSearchServices service;
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	MetadataSchemasManager manager;
	RecordServices recordServices;
	String document1InA16, document2InA16, document3InA16;
	AuthorizationsServices authsServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTest(users).withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);
		Toggle.TRY_USING_NEW_CACHE_BASED_TAXONOMIES_SEARCH_SERVICES_QUERY_HANDLER.enable();
		Toggle.FORCE_USING_NEW_CACHE_BASED_TAXONOMIES_SEARCH_SERVICES_QUERY_HANDLER.enable();

		inCollection(zeCollection).giveReadAccessTo(admin);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		service = getModelLayerFactory().newTaxonomiesSearchService();
		decommissioningService = new DecommissioningService(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

		UserServices userServices = getModelLayerFactory().newUserServices();
		UserCredential userCredential = userServices.getUserCredential(aliceWonderland);
		userServices.addUserToCollection(userCredential, zeCollection);
		alice = userServices.getUserInCollection(aliceWonderland, zeCollection);
		manager = getModelLayerFactory().getMetadataSchemasManager();

		DecommissioningService service = new DecommissioningService(zeCollection, getAppLayerFactory());

		Folder subfolder = service.newSubFolderIn(records.getFolder_A16());
		subfolder.setTitle("Sous-dossier");
		recordServices.add(subfolder);
		subFolderId = subfolder.getId();

		List<String> documentsInA16 = getFolderDocuments(records.folder_A16);
		document1InA16 = documentsInA16.get(0);
		document2InA16 = documentsInA16.get(1);
		document3InA16 = documentsInA16.get(2);

		waitForBatchProcess();
		configureQueryCounter();

		for (String documentId : getFolderDocuments(records.folder_A17)) {
			Record document = recordServices.getDocumentById(documentId);
			recordServices.logicallyDelete(document, User.GOD);
		}

		for (String documentId : getFolderDocuments(records.folder_A18)) {
			Record document = recordServices.getDocumentById(documentId);
			recordServices.logicallyDelete(document, User.GOD);
		}

		authsServices = getModelLayerFactory().newAuthorizationsServices();
	}

	private List<String> getFolderDocuments(String id) {
		return getModelLayerFactory().newSearchServices().searchRecordIds(new LogicalSearchQuery()
				.sortAsc(Schemas.TITLE).setCondition(from(rm.documentSchemaType()).where(rm.documentFolder()).isEqualTo(id)));
	}

	@Test
	public void whenDakotaIsNavigatingATaxonomyWithVisibleRecordsThenSeesRecords()
			throws Exception {

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getDakota_managerInA_userInB())
				.has(recordsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(recordsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getDakota_managerInA_userInB(), records.categoryId_X)
				.has(recordsInOrder(records.categoryId_X100))
				.has(recordsWithChildren(records.categoryId_X100))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getDakota_managerInA_userInB(), records.categoryId_X100)
				.has(recordsInOrder(records.categoryId_X110, records.categoryId_X120, records.folder_A16, records.folder_A17,
						records.folder_A18, records.folder_B06, records.folder_B32))
				.has(recordsWithChildren(records.categoryId_X110, records.categoryId_X120, records.folder_A16, records.folder_A17,
						records.folder_A18, records.folder_B06, records.folder_B32))
				.has(numFoundAndListSize(7))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getDakota_managerInA_userInB(), records.folder_A16)
				.has(recordsInOrder(document1InA16, document2InA16, document3InA16, subFolderId))
				.has(recordsWithChildren(subFolderId))
				.has(numFoundAndListSize(4))
				.has(solrQueryCounts(0, 0, 0));

	}

	@Test
	public void whenAdminIsNavigatingATaxonomyWithVisibleRecordsThenSeesRecords()
			throws Exception {

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getAdmin())
				.has(recordsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(recordsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X)
				.has(recordsInOrder(records.categoryId_X100))
				.has(recordsWithChildren(records.categoryId_X100))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100)
				.has(recordsInOrder("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06", "C32", "B32"))
				.has(recordsWithChildren("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06", "C32", "B32"))
				.has(numFoundAndListSize(9))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z)
				.has(recordsInOrder(records.categoryId_Z100))
				.has(recordsWithChildren(records.categoryId_Z100))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z100)
				.has(recordsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(recordsWithChildren(records.categoryId_Z110, records.categoryId_Z120))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z110)
				.has(recordsInOrder(records.categoryId_Z112))
				.has(recordsWithChildren(records.categoryId_Z112))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

	}

	@Test
	public void whenAdminIsNavigatingATaxonomyWithVisibleRecordsAlwaysDisplayingConceptsWithReadAccessThenSeesRecordsAndAllConcepts()
			throws Exception {

		recordServices.add(rm.newCategoryWithId("category_Y_id").setCode("Y").setTitle("Ze category Y"));

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions()
				.setAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable(true);

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), options)
				.has(recordsInOrder(records.categoryId_X, "category_Y_id", records.categoryId_Z))
				.has(recordsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(numFoundAndListSize(3))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X, options)
				.has(recordsInOrder(records.categoryId_X13, records.categoryId_X100))
				.has(recordsWithChildren(records.categoryId_X100))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100, options)
				.has(recordsInOrder("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06", "C32", "B32"))
				.has(recordsWithChildren("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06", "C32", "B32"))
				.has(numFoundAndListSize(9))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z, options)
				.has(recordsInOrder(records.categoryId_Z100, records.categoryId_Z200, records.categoryId_Z999,
						records.categoryId_ZE42))
				.has(recordsWithChildren(records.categoryId_Z100))
				.has(numFoundAndListSize(4))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z100, options)
				.has(recordsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(recordsWithChildren(records.categoryId_Z110, records.categoryId_Z120))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z110, options)
				.has(recordsInOrder(records.categoryId_Z111, records.categoryId_Z112))
				.has(recordsWithChildren(records.categoryId_Z112))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(0, 0, 0));

	}

	@Test
	public void whenAdminIsNavigatingAdminUnitTaxonomyWithVisibleRecordsThenSeesRecords()
			throws Exception {

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions();

		assertThatRootWhenUserNavigateUsingAdministrativeUnitsTaxonomy(records.getAdmin(), options)
				.has(recordsInOrder(records.unitId_10, records.unitId_30))
				.has(recordsWithChildren(records.unitId_10, records.unitId_30))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(records.getAdmin(), records.unitId_12, options)
				.has(recordsInOrder(records.unitId_12b))
				.has(recordsWithChildren(records.unitId_12b))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(records.getAdmin(), records.unitId_12b, options)
				.has(recordsInOrder("B02", "B04", "B06", "B08", "B32"))
				.has(recordsWithChildren("B02", "B04", "B06", "B08", "B32"))
				.has(numFoundAndListSize(5))
				.has(solrQueryCounts(0, 0, 0));

	}

	@Test
	public void whenUserIsNavigatingAdminUnitTaxonomyThenOnlySeeConceptsContainingAccessibleRecords()
			throws Exception {

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions();
		User sasquatch = users.sasquatchIn(zeCollection);
		User robin = users.robinIn(zeCollection);
		User admin = users.adminIn(zeCollection);
		authsServices.add(authorizationForUsers(sasquatch).on("B06").givingReadAccess(), admin);
		authsServices.add(authorizationForUsers(sasquatch).on(records.unitId_20d).givingReadAccess(), admin);

		authsServices.add(authorizationForUsers(robin).on("B06").givingReadAccess(), admin);
		authsServices.add(authorizationForUsers(robin).on(records.unitId_12c).givingReadAccess(), admin);
		authsServices.add(authorizationForUsers(robin).on(records.unitId_30).givingReadAccess(), admin);
		recordServices.refresh(robin);
		recordServices.refresh(sasquatch);
		waitForBatchProcess();
		assertThat(robin.hasReadAccess().on(recordServices.getDocumentById("B06"))).isTrue();
		assertThat(sasquatch.hasReadAccess().on(recordServices.getDocumentById("B06"))).isTrue();

		//Sasquatch
		assertThatRootWhenUserNavigateUsingAdministrativeUnitsTaxonomy(sasquatch, options)
				.has(recordsInOrder(records.unitId_10))
				.has(recordsWithChildren(records.unitId_10))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(sasquatch, records.unitId_10, options)
				.has(recordsInOrder(records.unitId_12))
				.has(recordsWithChildren(records.unitId_12))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(sasquatch, records.unitId_12, options)
				.has(recordsInOrder(records.unitId_12b))
				.has(recordsWithChildren(records.unitId_12b))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(sasquatch, records.unitId_12b, options)
				.has(recordsInOrder("B06"))
				.has(recordsWithChildren("B06"))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

		//Robin
		assertThatRootWhenUserNavigateUsingAdministrativeUnitsTaxonomy(robin, options)
				.has(recordsInOrder(records.unitId_10, records.unitId_30))
				.has(recordsWithChildren(records.unitId_10, records.unitId_30))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(robin, records.unitId_10, options)
				.has(recordsInOrder(records.unitId_12))
				.has(recordsWithChildren(records.unitId_12))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(robin, records.unitId_12, options)
				.has(recordsInOrder(records.unitId_12b))
				.has(recordsWithChildren(records.unitId_12b))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(robin, records.unitId_12b, options)
				.has(recordsInOrder("B06"))
				.has(recordsWithChildren("B06"))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

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
		assertThatRootWhenUserNavigateUsingAdministrativeUnitsTaxonomy(sasquatch, options)
				.has(recordsInOrder(records.unitId_10, records.unitId_20))
				.has(recordsWithChildren(records.unitId_10, records.unitId_20))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(sasquatch, records.unitId_10, options)
				.has(recordsInOrder(records.unitId_12))
				.has(recordsWithChildren(records.unitId_12))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(sasquatch, records.unitId_12, options)
				.has(recordsInOrder(records.unitId_12b))
				.has(recordsWithChildren(records.unitId_12b))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(sasquatch, records.unitId_12b, options)
				.has(recordsInOrder("B06"))
				.has(recordsWithChildren("B06"))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(sasquatch, records.unitId_12c, options)
				.has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0));

		//Robin
		assertThatRootWhenUserNavigateUsingAdministrativeUnitsTaxonomy(robin, options)
				.has(recordsInOrder(records.unitId_10, records.unitId_30))
				.has(recordsWithChildren(records.unitId_10, records.unitId_30))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(robin, records.unitId_10, options)
				.has(recordsInOrder(records.unitId_12))
				.has(recordsWithChildren(records.unitId_12))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(robin, records.unitId_12, options)
				.has(recordsInOrder(records.unitId_12b, records.unitId_12c))
				.has(recordsWithChildren(records.unitId_12b))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(robin, records.unitId_30, options)
				.has(recordsInOrder(records.unitId_30c))
				.has(recordsWithChildren(records.unitId_30c))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(robin, records.unitId_12b, options)
				.has(recordsInOrder("B06"))
				.has(recordsWithChildren("B06"))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

	}

	@Test
	public void whenAdminIsNavigatingAdminUnityWithVisibleRecordsAlwaysDisplayingConceptsWithReadAccessThenSeesRecordsAndAllConcepts()
			throws Exception {

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions()
				.setAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable(true);

		assertThatRootWhenUserNavigateUsingAdministrativeUnitsTaxonomy(records.getAdmin(), options)
				.has(recordsInOrder(records.unitId_10, records.unitId_20, records.unitId_30))
				.has(recordsWithChildren(records.unitId_10, records.unitId_20, records.unitId_30))
				.has(numFoundAndListSize(3))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(records.getAdmin(), records.unitId_12, options)
				.has(recordsInOrder(records.unitId_12b, records.unitId_12c))
				.has(recordsWithChildren(records.unitId_12b))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(records.getAdmin(), records.unitId_12b, options)
				.has(recordsInOrder("B02", "B04", "B06", "B08", "B32"))
				.has(recordsWithChildren("B02", "B04", "B06", "B08", "B32"))
				.has(numFoundAndListSize(5))
				.has(solrQueryCounts(0, 0, 0));

	}

	@Test
	public void whenNavigatingByIntervalThenGetGoodResults()
			throws Exception {

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getAdmin())
				.has(recordsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(recordsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(0, 0, 0));

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), 0, 2)
				.has(recordsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(recordsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(0, 0, 0));

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), 0, 1)
				.has(recordsInOrder(records.categoryId_X))
				.has(recordsWithChildren(records.categoryId_X))
				.has(listSize(1)).has(numFound(2))
				.has(solrQueryCounts(0, 0, 0));

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), 1, 1)
				.has(recordsInOrder(records.categoryId_Z))
				.has(recordsWithChildren(records.categoryId_Z))
				.has(listSize(1)).has(numFound(2))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100)
				.has(recordsInOrder("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06", "C32", "B32"))
				.has(recordsWithChildren("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06", "C32", "B32"))
				.has(listSize(9)).has(numFound(9))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100, 0, 10)
				.has(recordsInOrder("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06", "C32", "B32"))
				.has(recordsWithChildren("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06", "C32", "B32"))
				.has(listSize(9)).has(numFound(9))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100, 0, 7)
				.has(recordsInOrder("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06"))
				.has(recordsWithChildren("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06"))
				.has(listSize(7)).has(numFound(9))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100, 0, 3)
				.has(recordsInOrder("categoryId_X110", "categoryId_X120", "A16"))
				.has(recordsWithChildren("categoryId_X110", "categoryId_X120", "A16"))
				.has(listSize(3)).has(numFound(9))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100, 1, 4)
				.has(recordsInOrder("categoryId_X120", "A16", "A17", "A18"))
				.has(recordsWithChildren("categoryId_X120", "A16", "A17", "A18"))
				.has(listSize(4)).has(numFound(9))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.folder_A16, 0, 5)
				.has(recordsInOrder(document1InA16, document2InA16, document3InA16, subFolderId))
				.has(recordsWithChildren(subFolderId))
				.has(listSize(4)).has(numFound(4))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.folder_A16, 0, 1)
				.has(recordsInOrder(document1InA16))
				.has(noRecordsWithChildren())
				.has(listSize(1)).has(numFoundGreaterThan(1))
				.has(solrQueryCounts(0, 0, 0));

	}

	@Test
	public void givenHugeClassificationPlanContainingMultipleFoldersThenValidSearchResponses()
			throws Exception {
		User admin = users.adminIn(zeCollection);
		admin.setCollectionReadAccess(true);

		List<Category> rootCategories = new ArrayList<>();
		for (int i = 1; i <= 100; i++) {
			String code = toTitle(i);
			rootCategories.add(rm.newCategoryWithId(code).setCode(code).setTitle("Title " + toTitle((20000 - i)))
					.setRetentionRules(asList(records.ruleId_1)));
		}
		Category category42 = rootCategories.get(41);
		addRecordsInRandomOrder(rootCategories);

		List<Category> childCategories = new ArrayList<>();
		for (int i = 1; i <= 100; i++) {
			String code = "42_" + toTitle(i);
			childCategories.add(rm.newCategoryWithId(code).setRetentionRules(asList(records.ruleId_1))
					.setParent(category42).setCode(code).setTitle("Title " + toTitle((20000 - i))));
		}
		Category category42_42 = childCategories.get(41);
		addRecordsInRandomOrder(childCategories);

		List<Folder> category42_42_folders = new ArrayList<>();
		for (int i = 1; i <= 100; i++) {
			category42_42_folders.add(newFolderInCategory(category42_42, "Folder " + toTitle(i)));
		}
		addRecordsInRandomOrder(category42_42_folders);

		List<Folder> otherCategoriesFolder = new ArrayList<>();
		for (Category category : rootCategories) {
			if (!category.getId().equals(category42.getId())) {
				otherCategoriesFolder.add(newFolderInCategory(category, "A folder"));
			}
		}
		for (Category category : childCategories) {
			if (!category.getId().equals(category42_42.getId())) {
				otherCategoriesFolder.add(newFolderInCategory(category, "A folder"));
			}
		}
		addRecordsInRandomOrder(otherCategoriesFolder);

		for (int i = 0; i < rootCategories.size() - 25; i += 25) {
			LinkableTaxonomySearchResponse response = service.getVisibleRootConceptResponse(
					admin, zeCollection, CLASSIFICATION_PLAN, new TaxonomiesSearchOptions().setStartRow(i).setRows(25), null);
			List<String> expectedIds = new RecordUtils().toWrappedRecordIdsList(rootCategories.subList(i, i + 25));
			assertThat(response.getRecords()).extracting("id").isEqualTo(expectedIds);
		}

		for (int i = 0; i < childCategories.size() - 25; i += 25) {
			LinkableTaxonomySearchResponse response = service.getVisibleChildConceptResponse(admin, CLASSIFICATION_PLAN,
					category42.getWrappedRecord(), new TaxonomiesSearchOptions().setStartRow(i).setRows(25));
			List<String> expectedIds = new RecordUtils().toWrappedRecordIdsList(childCategories.subList(i, i + 25));
			assertThat(response.getRecords()).extracting("id").isEqualTo(expectedIds);
		}

		for (int i = 0; i < category42_42_folders.size() - 25; i += 25) {
			LinkableTaxonomySearchResponse response = service.getVisibleChildConceptResponse(admin, CLASSIFICATION_PLAN,
					category42_42.getWrappedRecord(), new TaxonomiesSearchOptions().setStartRow(i).setRows(25));
			List<String> expectedIds = new RecordUtils().toWrappedRecordIdsList(category42_42_folders.subList(i, i + 25));
			assertThat(response.getNumFound()).isEqualTo(category42_42_folders.size());
			assertThat(response.getRecords()).extracting("id").isEqualTo(expectedIds);
		}

	}

	@Test
	public void givenHugeAdministrativeUnitsContainingMultipleFoldersThenValidSearchResponses()
			throws Exception {
		User admin = users.adminIn(zeCollection);
		admin.setCollectionReadAccess(true);

		List<AdministrativeUnit> rootAdministrativeUnits = new ArrayList<>();
		for (int i = 1; i <= 100; i++) {
			String code = toTitle(1000 + i);
			rootAdministrativeUnits.add(rm.newAdministrativeUnitWithId(code).setCode(code)
					.setTitle("Title " + toTitle(20000 - i)));
		}
		AdministrativeUnit unit42 = rootAdministrativeUnits.get(41);
		addRecordsInRandomOrder(rootAdministrativeUnits);

		List<AdministrativeUnit> childAdministrativeUnits = new ArrayList<>();
		for (int i = 1; i <= 100; i++) {
			String code = "42_" + toTitle(i);
			childAdministrativeUnits.add(rm.newAdministrativeUnitWithId(code)
					.setParent(unit42).setCode(code).setTitle("Title " + toTitle((20000 - i))));
		}
		AdministrativeUnit unit42_666 = childAdministrativeUnits.get(41);
		addRecordsInRandomOrder(childAdministrativeUnits);

		List<Folder> unit42_666_folders = new ArrayList<>();
		for (int i = 1; i <= 100; i++) {
			unit42_666_folders.add(newFolderInUnit(unit42_666, "Folder " + toTitle(i)));
		}
		addRecordsInRandomOrder(unit42_666_folders);

		List<Folder> otherUnitsFolder = new ArrayList<>();
		for (AdministrativeUnit unit : rootAdministrativeUnits) {
			if (!unit.getId().equals(unit42.getId())) {
				otherUnitsFolder.add(newFolderInUnit(unit, "A folder"));
			}
		}
		for (AdministrativeUnit unit : childAdministrativeUnits) {
			if (!unit.getId().equals(unit42_666.getId())) {
				otherUnitsFolder.add(newFolderInUnit(unit, "A folder"));
			}
		}
		addRecordsInRandomOrder(otherUnitsFolder);

		for (int i = 0; i < rootAdministrativeUnits.size() - 25; i += 25) {
			LinkableTaxonomySearchResponse response = service.getVisibleRootConceptResponse(
					admin, zeCollection, ADMINISTRATIVE_UNITS, new TaxonomiesSearchOptions().setStartRow(2 + i).setRows(25),
					null);
			List<String> expectedIds = new RecordUtils().toWrappedRecordIdsList(rootAdministrativeUnits.subList(i, i + 25));
			assertThat(response.getRecords()).extracting("id").isEqualTo(expectedIds);
		}

		for (int i = 0; i < childAdministrativeUnits.size() - 25; i += 25) {
			LinkableTaxonomySearchResponse response = service.getVisibleChildConceptResponse(admin, ADMINISTRATIVE_UNITS,
					unit42.getWrappedRecord(), new TaxonomiesSearchOptions().setStartRow(i).setRows(25));
			List<String> expectedIds = new RecordUtils().toWrappedRecordIdsList(childAdministrativeUnits.subList(i, i + 25));

			assertThat(response.getRecords()).extracting("id").isEqualTo(expectedIds);
		}

		for (int i = 0; i < unit42_666_folders.size() - 25; i += 25) {
			LinkableTaxonomySearchResponse response = service.getVisibleChildConceptResponse(admin, ADMINISTRATIVE_UNITS,
					unit42_666.getWrappedRecord(), new TaxonomiesSearchOptions().setStartRow(i).setRows(25));
			List<String> expectedIds = new RecordUtils().toWrappedRecordIdsList(unit42_666_folders.subList(i, i + 25));
			assertThat(response.getNumFound()).isEqualTo(unit42_666_folders.size());
			assertThat(response.getRecords()).extracting("id").isEqualTo(expectedIds);
		}

	}

	@Test
	public void givenLogicallyDeletedRecordsInVisibleRecordsThenNotShownInTree()
			throws Exception {

		Folder subFolder1 = decommissioningService.newSubFolderIn(records.getFolder_A20()).setTitle("Ze sub folder");
		Folder subFolder2 = decommissioningService.newSubFolderIn(records.getFolder_A20()).setTitle("Ze sub folder");
		getModelLayerFactory().newRecordServices().execute(new Transaction().addAll(subFolder1, subFolder2));

		getModelLayerFactory().newRecordServices().logicallyDelete(subFolder1.getWrappedRecord(), User.GOD);
		getModelLayerFactory().newRecordServices().logicallyDelete(subFolder2.getWrappedRecord(), User.GOD);

		authsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).on(subFolder1.getId()).givingReadAccess());
		authsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).on(subFolder2.getId()).givingReadAccess());
		authsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).on(records.folder_C01).givingReadAccess());

		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);
		User sasquatch = users.sasquatchIn(zeCollection);
		assertThatRootWhenUserNavigateUsingPlanTaxonomy(sasquatch)
				.has(numFoundAndListSize(1))
				.has(recordsWithChildren(records.categoryId_X));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(sasquatch, records.categoryId_Z).has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(sasquatch, records.categoryId_Z100).has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(sasquatch, records.categoryId_Z120).has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(sasquatch, records.folder_A20).has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0));

	}

	@Test
	public void givenInvisibleInTreeRecordsInVisibleRecordThenNotShownInTree()
			throws Exception {

		givenConfig(RMConfigs.DISPLAY_SEMI_ACTIVE_RECORDS_IN_TREES, false);
		givenConfig(RMConfigs.DISPLAY_SEMI_ACTIVE_RECORDS_IN_TREES, false);

		Folder subFolder1 = decommissioningService.newSubFolderIn(records.getFolder_A20()).setTitle("Ze sub folder")
				.setActualTransferDate(LocalDate.now()).setActualDestructionDate(LocalDate.now());
		Folder subFolder2 = decommissioningService.newSubFolderIn(records.getFolder_A20()).setTitle("Ze sub folder")
				.setActualTransferDate(LocalDate.now());
		getModelLayerFactory().newRecordServices().execute(new Transaction().addAll(subFolder1, subFolder2));

		assertThat(subFolder2.<Boolean>get(Schemas.VISIBLE_IN_TREES)).isEqualTo(Boolean.FALSE);

		authsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).on(subFolder1.getId()).givingReadAccess());
		authsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).on(subFolder2.getId()).givingReadAccess());
		authsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).on(records.folder_C01).givingReadAccess());

		User sasquatch = users.sasquatchIn(zeCollection);
		assertThatRootWhenUserNavigateUsingPlanTaxonomy(sasquatch)
				.has(numFoundAndListSize(1))
				.has(recordsWithChildren(records.categoryId_X));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(sasquatch, records.categoryId_Z).has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(sasquatch, records.categoryId_Z100).has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(sasquatch, records.categoryId_Z120).has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(sasquatch, records.folder_A20).has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0));

	}

	@Test
	public void givenInvisibleInTreeRecordsThenNotShownInTree()
			throws Exception {

		getDataLayerFactory().getDataLayerLogger().setMonitoredIds(asList("00000000309", "00000000310", "00000000311", "00000000312"));

		givenConfig(RMConfigs.DISPLAY_SEMI_ACTIVE_RECORDS_IN_TREES, false);
		givenConfig(RMConfigs.DISPLAY_SEMI_ACTIVE_RECORDS_IN_TREES, false);

		getModelLayerFactory().newRecordServices()
				.execute(new Transaction().addAll(records.getFolder_A20().setActualTransferDate(LocalDate.now())));

		authsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).on(records.folder_A20).givingReadAccess());
		authsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).on(records.folder_C01).givingReadAccess());

		User sasquatch = users.sasquatchIn(zeCollection);

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(sasquatch)
				.has(numFoundAndListSize(1))
				.has(recordsWithChildren(records.categoryId_X));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(sasquatch, records.categoryId_Z).has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(sasquatch, records.categoryId_Z100).has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(sasquatch, records.categoryId_Z120).has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(sasquatch, records.folder_A20).has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0));

	}

	@Test
	public void givenLogicallyDeletedRecordsThenNotShownInTree()
			throws Exception {

		authsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).on(records.folder_A20).givingReadAccess());
		authsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).on(records.folder_C01).givingReadAccess());

		getModelLayerFactory().newRecordServices().logicallyDelete(records.getFolder_A20().getWrappedRecord(), User.GOD);

		User sasquatch = users.sasquatchIn(zeCollection);
		assertThatRootWhenUserNavigateUsingPlanTaxonomy(sasquatch)
				.has(numFoundAndListSize(1))
				.has(recordsWithChildren(records.categoryId_X));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(sasquatch, records.categoryId_Z).has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(sasquatch, records.categoryId_Z100).has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(sasquatch, records.categoryId_Z120).has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(sasquatch, records.folder_A20).has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0));

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

		authsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).givingReadWriteAccess().on(folderNearEnd));
		authsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).givingReadWriteAccess().on(subFolderNearEnd));
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

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.sasquatchIn(zeCollection), records.categoryId_X13, withWriteAccess)
				.has(recordsInOrder(folderNearEnd.getId(), subFolderNearEnd.getParentFolder()))
				.has(solrQueryCounts(1, 2, 0))
				.has(secondSolrQueryCounts(1, 2, 0));

		assertThat(queryCount.get()).isEqualTo(0);
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
		getModelLayerFactory().getRecordsCaches().updateRecordsMainSortValue();

		User alice = users.aliceIn(zeCollection);
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(alice, "root",
				options.setStartRow(0).setRows(20))
				.has(recordsInOrder("category_1", "category_2", "category_3", "category_4", "category_5", "category_6",
						"category_7", "category_8", "category_9", "category_10", "category_11", "category_12", "category_13",
						"category_14", "category_15", "category_16", "category_17", "category_18", "category_19", "category_20"))
				.has(numFoundGreaterThan(20)).has(listSize(20))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(alice, "root",
				options.setStartRow(0).setRows(20))
				.has(recordsInOrder("category_1", "category_2", "category_3", "category_4", "category_5", "category_6",
						"category_7", "category_8", "category_9", "category_10", "category_11", "category_12", "category_13",
						"category_14", "category_15", "category_16", "category_17", "category_18", "category_19", "category_20"))
				.has(numFoundGreaterThan(20)).has(listSize(20))
				.has(solrQueryCounts(0, 0, 0));


		assertThatChildWhenUserNavigateUsingPlanTaxonomy(alice, "root",
				options.setStartRow(0).setRows(30))
				.has(recordsInOrder("category_1", "category_2", "category_3", "category_4", "category_5", "category_6",
						"category_7", "category_8", "category_9", "category_10", "category_11", "category_12", "category_13",
						"category_14", "category_15", "category_16",
						"category_17", "category_18", "category_19", "category_20", "category_21", "category_22", "category_23",
						"category_24", "category_25", "category_26", "category_27", "category_28", "category_29", "category_30"))
				.has(numFoundGreaterThan(30)).has(listSize(30))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(alice, "root", options.setStartRow(289).setRows(30)
				.setFastContinueInfos(null))
				.has(recordsInOrder("category_290", "category_291", "category_292", "category_293",
						"category_294", "category_295", "category_296", "category_297", "category_298", "category_299",
						"category_300"))
				.has(numFound(300)).has(listSize(11))
				.has(solrQueryCounts(0, 0, 0));

	}

	@Test
	public void givenPlethoraOfRootCategoriesThenValidGetRootResponse()
			throws Exception {

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);
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
		getModelLayerFactory().getRecordsCaches().updateRecordsMainSortValue();

		User alice = users.aliceIn(zeCollection);
		assertThatRootWhenUserNavigateUsingPlanTaxonomy(alice, options.setStartRow(0).setRows(20).setFastContinueInfos(null))
				.has(recordsInOrder("category_1", "category_2", "category_3", "category_4", "category_5", "category_6",
						"category_7", "category_8", "category_9", "category_10", "category_11", "category_12", "category_13",
						"category_14", "category_15", "category_16", "category_17", "category_18", "category_19", "category_20"))
				.has(numFoundGreaterThan(20)).has(listSize(20))
				.has(solrQueryCounts(0, 0, 0));

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(alice, options.setStartRow(0).setRows(20).setFastContinueInfos(null))
				.has(recordsInOrder("category_1", "category_2", "category_3", "category_4", "category_5", "category_6",
						"category_7", "category_8", "category_9", "category_10", "category_11", "category_12", "category_13",
						"category_14", "category_15", "category_16", "category_17", "category_18", "category_19", "category_20"))
				.has(numFoundGreaterThan(20)).has(listSize(20))
				.has(solrQueryCounts(0, 0, 0));

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(alice, options.setStartRow(0).setRows(30))
				.has(recordsInOrder("category_1", "category_2", "category_3", "category_4", "category_5", "category_6",
						"category_7", "category_8", "category_9", "category_10", "category_11", "category_12", "category_13",
						"category_14", "category_15", "category_16",
						"category_17", "category_18", "category_19", "category_20", "category_21", "category_22", "category_23",
						"category_24", "category_25", "category_26", "category_27", "category_28", "category_29", "category_30"))
				.has(numFoundGreaterThan(30)).has(listSize(30))
				.has(solrQueryCounts(0, 0, 0));

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(alice, options.setStartRow(289).setRows(30))
				.has(recordsInOrder("category_290", "category_291", "category_292", "category_293",
						"category_294", "category_295", "category_296", "category_297", "category_298", "category_299",
						"category_300", "categoryId_X", "categoryId_Z"))
				.has(numFound(302)).has(listSize(13))
				.has(solrQueryCounts(0, 0, 0));

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(alice, options.setStartRow(289).setRows(30))
				.has(recordsInOrder("category_290", "category_291", "category_292", "category_293",
						"category_294", "category_295", "category_296", "category_297", "category_298", "category_299",
						"category_300", "categoryId_X", "categoryId_Z"))
				.has(numFound(302)).has(listSize(13))
				.has(solrQueryCounts(0, 0, 0));

	}

	@Test
	public void whenAdminIsNavigatingATaxonomyWithVisibleRecordsAlwaysDisplayingConceptsWithReadAccessAndNoChildCalculatedThenSeesRecordsAndAllConcepts()
			throws Exception {

		recordServices.add(rm.newCategoryWithId("category_Y_id").setCode("Y").setTitle("Ze category Y"));

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions()
				.setAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable(true)
				.setHasChildrenFlagCalculated(NEVER);

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), options)
				.has(recordsInOrder(records.categoryId_X, "category_Y_id", records.categoryId_Z))
				.has(recordsWithChildren(records.categoryId_X, "category_Y_id", records.categoryId_Z))
				.has(numFoundAndListSize(3))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X, options)
				.has(recordsInOrder(records.categoryId_X13, records.categoryId_X100))
				.has(recordsWithChildren(records.categoryId_X13, records.categoryId_X100))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100, options)
				.has(recordsInOrder("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06", "C32", "B32"))
				.has(recordsWithChildren("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06", "C32", "B32"))
				.has(numFoundAndListSize(9))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z, options)
				.has(recordsInOrder(records.categoryId_Z100, records.categoryId_Z200, records.categoryId_Z999,
						records.categoryId_ZE42))
				.has(recordsWithChildren(records.categoryId_Z100, records.categoryId_Z200, records.categoryId_Z999,
						records.categoryId_ZE42))
				.has(numFoundAndListSize(4))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z100, options)
				.has(recordsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(recordsWithChildren(records.categoryId_Z110, records.categoryId_Z120))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z110, options)
				.has(recordsInOrder(records.categoryId_Z111, records.categoryId_Z112))
				.has(recordsWithChildren(records.categoryId_Z111, records.categoryId_Z112))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(0, 0, 0));

	}


	@Test
	public void given5000FoldersAndUserHasOnlyAccessToTheLastOnesThenDoesNotIteratorOverAllNodesToFindThemAndUseSolr()
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

		authsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).givingReadWriteAccess().on(folderNearEnd));
		authsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).givingReadWriteAccess().on(subFolderNearEnd));
		waitForBatchProcess();
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		final AtomicInteger queryCount = new AtomicInteger();
		getDataLayerFactory().getExtensions().getSystemWideExtensions().bigVaultServerExtension
				.add(new BigVaultServerExtension() {
					@Override
					public void afterQuery(SolrParams solrParams, long qtime) {
						queryCount.incrementAndGet();
					}
				});

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.sasquatchIn(zeCollection), records.categoryId_X13, withWriteAccess)
				.has(recordsInOrder(folderNearEnd.getId(), subFolderNearEnd.getParentFolder()))
				.has(solrQueryCounts(1, 2, 0))
				.has(secondCallQueryCounts(1, 2, 0));

	}


	@Test
	public void given1000FoldersAndUserHasOnlyAccessToTheLastOnesThenDoesNotIteratorOverAllNodesToFindThemAndUseCache()
			throws Exception {

		Folder folderNearEnd = null;
		Folder subFolderNearEnd = null;
		List<Folder> addedRecords = new ArrayList<>();

		int size = 999;
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

		authsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).givingReadWriteAccess().on(folderNearEnd));
		authsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).givingReadWriteAccess().on(subFolderNearEnd));
		waitForBatchProcess();
		TaxonomiesSearchOptions withWriteAccess = new TaxonomiesSearchOptions().setRequiredAccess(Role.WRITE);

		final AtomicInteger queryCount = new AtomicInteger();
		getDataLayerFactory().getExtensions().getSystemWideExtensions().bigVaultServerExtension
				.add(new BigVaultServerExtension() {
					@Override
					public void afterQuery(SolrParams solrParams, long qtime) {
						queryCount.incrementAndGet();
					}
				});

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.sasquatchIn(zeCollection), records.categoryId_X13, withWriteAccess)
				.has(recordsInOrder(folderNearEnd.getId(), subFolderNearEnd.getParentFolder()))
				.has(solrQueryCounts(0, 0, 0))
				.has(secondCallQueryCounts(0, 0, 0));

	}


	@Test
	public void givenUserHavePositiveAuthorizationsOnFoldersAndSubFoldersThenValidTreeForFolderSelectionUsingCategoryTaxonomy()
			throws Exception {


		createFoldersAndSubFoldersWithNegativeAuths(records.getUnit20(), records.getCategory_X13());
		waitForBatchProcess();

		//		assertThatTokensOf("f11").containsOnly("-nd_heroes", "-nr_heroes", "-nw_sidekicks", "-nw_heroes", "-nd_sidekicks", "-nr_sidekicks");
		//		assertThatTokensOf("f1").isEmpty();
		//
		//		assertThatTokensOf("f21").containsOnly("nd_heroes", "nr_heroes", "nw_sidekicks", "nw_heroes", "nd_sidekicks", "nr_sidekicks");
		//		assertThatTokensOf("f2").containsOnly("nd_heroes", "nr_heroes", "nw_sidekicks", "nw_heroes", "nd_sidekicks", "nr_sidekicks");
		//
		//		assertThatTokensOf("f31").containsOnly("-nd_heroes", "-nr_heroes", "-nw_sidekicks", "-nw_heroes", "-nd_sidekicks", "-nr_sidekicks");
		//		assertThatTokensOf("f32").containsOnly("nd_heroes", "nr_heroes", "nw_sidekicks", "nw_heroes", "nd_sidekicks", "nr_sidekicks");
		//		assertThatTokensOf("f3").isEmpty();
		//
		//		assertThatTokensOf("f41").containsOnly("-nd_heroes", "-nr_heroes", "-nw_sidekicks", "-nw_heroes", "-nd_sidekicks", "-nr_sidekicks",
		//				"nd_legends", "nr_legends", "nw_rumors", "nw_legends", "nd_rumors", "nr_rumors");
		//		assertThatTokensOf("f4").isEmpty();

		//		assertThatTokensOf("f51").containsOnly("-nd_heroes", "-nr_heroes", "-nw_sidekicks", "-nw_heroes", "-nd_sidekicks", "-nr_sidekicks");
		//		assertThatTokensOf("f5").isEmpty();
		//
		//		assertThatTokensOf("f61").containsOnly("-nd_heroes", "-nr_heroes", "-nw_sidekicks", "-nw_heroes", "-nd_sidekicks", "-nr_sidekicks");
		//		assertThatTokensOf("f62").containsOnly("-nd_heroes", "-nr_heroes", "-nw_sidekicks", "-nw_heroes", "-nd_sidekicks", "-nr_sidekicks");
		//		assertThatTokensOf("f6").isEmpty();
		//
		//		assertThatTokensOf("f71").containsOnly("-nd_heroes", "-nr_heroes", "-nw_sidekicks", "-nw_heroes", "-nd_sidekicks", "-nr_sidekicks");
		//		assertThatTokensOf("f72").containsOnly("-nd_heroes", "-nr_heroes", "-nw_sidekicks", "-nw_heroes", "-nd_sidekicks", "-nr_sidekicks");
		//		assertThatTokensOf("f7").isEmpty();

		//		assertThatTokensOf("f81").containsOnly("-nd_heroes", "-nr_heroes", "-nw_sidekicks", "-nw_heroes", "-nd_sidekicks", "-nr_sidekicks");
		//		assertThatTokensOf("f8").isEmpty();

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), records.categoryId_X13)
				.has(recordsInOrder("f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "f10"))
				.has(recordsWithChildren("f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "f10"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), "f9").has(recordsInOrder("f91"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), "f10").has(recordsInOrder("f101"));

		recordServices.update(users.aliceIn(zeCollection).setCollectionAllAccess(false));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), records.categoryId_X13)
				.has(recordsInOrder("f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8"))
				.has(recordsWithChildren("f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8"));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), "f1").has(recordsInOrder("f11"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), "f2").has(recordsInOrder("f21"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), "f3").has(recordsInOrder("f31", "f32"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), "f4").has(recordsInOrder());
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), "f5").has(recordsInOrder("f51"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), "f6").has(recordsInOrder("f62"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), "f7").has(recordsInOrder("f72"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), "f8").has(recordsInOrder("f81"));


		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.charlesIn(zeCollection), records.categoryId_X13)
				.has(recordsInOrder("f1", "f3", "f4", "f5", "f6", "f7", "f8"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.charlesIn(zeCollection), "f1").has(recordsInOrder("f11"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.charlesIn(zeCollection), "f3").has(recordsInOrder("f31"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.charlesIn(zeCollection), "f4").has(recordsInOrder("f41"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.charlesIn(zeCollection), "f5").has(recordsInOrder("f51"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.charlesIn(zeCollection), "f6").has(recordsInOrder("f61", "f62"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.charlesIn(zeCollection), "f7").has(recordsInOrder("f71", "f72"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.charlesIn(zeCollection), "f8").has(recordsInOrder());


		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.gandalfIn(zeCollection), records.categoryId_X13)
				.has(recordsInOrder("f1", "f3", "f4", "f5", "f6", "f7", "f8"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.gandalfIn(zeCollection), "f1").has(recordsInOrder("f11"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.gandalfIn(zeCollection), "f3").has(recordsInOrder("f31"));

		//This is an accepted problem (f41 should not be returned)
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.gandalfIn(zeCollection), "f4").has(recordsInOrder());
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.gandalfIn(zeCollection), "f5").has(recordsInOrder("f51"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.gandalfIn(zeCollection), "f6").has(recordsInOrder("f62"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.gandalfIn(zeCollection), "f7").has(recordsInOrder("f72"));

		//This is an accepted problem (f81 should not be returned)
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.gandalfIn(zeCollection), "f8").has(recordsInOrder());

	}

	@Test
	public void givenUserHavePositiveAuthorizationsOnFoldersAndDocumentsThenValidTreeForFolderSelectionUsingCategoryTaxonomy()
			throws Exception {

		createFoldersAndDocumentsWithNegativeAuths(records.getUnit20(), records.getCategory_X13());
		waitForBatchProcess();

		//		assertThatTokensOf("f11").containsOnly("-nd_heroes", "-nr_heroes", "-nw_sidekicks", "-nw_heroes", "-nd_sidekicks", "-nr_sidekicks");
		//		assertThatTokensOf("f1").isEmpty();
		//
		//		assertThatTokensOf("f21").containsOnly("nd_heroes", "nr_heroes", "nw_sidekicks", "nw_heroes", "nd_sidekicks", "nr_sidekicks");
		//		assertThatTokensOf("f2").containsOnly("nd_heroes", "nr_heroes", "nw_sidekicks", "nw_heroes", "nd_sidekicks", "nr_sidekicks");
		//
		//		assertThatTokensOf("f31").containsOnly("-nd_heroes", "-nr_heroes", "-nw_sidekicks", "-nw_heroes", "-nd_sidekicks", "-nr_sidekicks");
		//		assertThatTokensOf("f32").containsOnly("nd_heroes", "nr_heroes", "nw_sidekicks", "nw_heroes", "nd_sidekicks", "nr_sidekicks");
		//		assertThatTokensOf("f3").isEmpty();
		//
		//		assertThatTokensOf("f41").containsOnly("-nd_heroes", "-nr_heroes", "-nw_sidekicks", "-nw_heroes", "-nd_sidekicks", "-nr_sidekicks",
		//				"nd_legends", "nr_legends", "nw_rumors", "nw_legends", "nd_rumors", "nr_rumors");
		//		assertThatTokensOf("f4").isEmpty();

		//		assertThatTokensOf("f51").containsOnly("-nd_heroes", "-nr_heroes", "-nw_sidekicks", "-nw_heroes", "-nd_sidekicks", "-nr_sidekicks");
		//		assertThatTokensOf("f5").isEmpty();
		//
		//		assertThatTokensOf("f61").containsOnly("-nd_heroes", "-nr_heroes", "-nw_sidekicks", "-nw_heroes", "-nd_sidekicks", "-nr_sidekicks");
		//		assertThatTokensOf("f62").containsOnly("-nd_heroes", "-nr_heroes", "-nw_sidekicks", "-nw_heroes", "-nd_sidekicks", "-nr_sidekicks");
		//		assertThatTokensOf("f6").isEmpty();
		//
		//		assertThatTokensOf("f71").containsOnly("-nd_heroes", "-nr_heroes", "-nw_sidekicks", "-nw_heroes", "-nd_sidekicks", "-nr_sidekicks");
		//		assertThatTokensOf("f72").containsOnly("-nd_heroes", "-nr_heroes", "-nw_sidekicks", "-nw_heroes", "-nd_sidekicks", "-nr_sidekicks");
		//		assertThatTokensOf("f7").isEmpty();

		//		assertThatTokensOf("f81").containsOnly("-nd_heroes", "-nr_heroes", "-nw_sidekicks", "-nw_heroes", "-nd_sidekicks", "-nr_sidekicks");
		//		assertThatTokensOf("f8").isEmpty();


		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), records.categoryId_X13)
				.has(recordsInOrder("f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "f10"))
				.has(recordsWithChildren("f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "f10"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), "f9").has(recordsInOrder("d91"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), "f10").has(recordsInOrder("d101"));

		recordServices.update(users.aliceIn(zeCollection).setCollectionAllAccess(false));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), records.categoryId_X13)
				.has(recordsInOrder("f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8"))
				.has(recordsWithChildren("f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8"));


		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), "f1").has(recordsInOrder("d11"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), "f2").has(recordsInOrder("d21"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), "f3").has(recordsInOrder("d31", "d32"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), "f4").has(recordsInOrder());
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), "f5").has(recordsInOrder("d51"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), "f6").has(recordsInOrder("d62"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), "f7").has(recordsInOrder("d72"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.aliceIn(zeCollection), "f8").has(recordsInOrder("d81"));


		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.charlesIn(zeCollection), records.categoryId_X13)
				.has(recordsInOrder("f1", "f3", "f4", "f5", "f6", "f7", "f8"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.charlesIn(zeCollection), "f1").has(recordsInOrder("d11"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.charlesIn(zeCollection), "f3").has(recordsInOrder("d31"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.charlesIn(zeCollection), "f4").has(recordsInOrder("d41"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.charlesIn(zeCollection), "f5").has(recordsInOrder("d51"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.charlesIn(zeCollection), "f6").has(recordsInOrder("d61", "d62"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.charlesIn(zeCollection), "f7").has(recordsInOrder("d71", "d72"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.charlesIn(zeCollection), "f8").has(recordsInOrder());


		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.gandalfIn(zeCollection), records.categoryId_X13)
				.has(recordsInOrder("f1", "f3", "f4", "f5", "f6", "f7", "f8"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.gandalfIn(zeCollection), "f1").has(recordsInOrder("d11"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.gandalfIn(zeCollection), "f3").has(recordsInOrder("d31"));

		//This is an accepted problem (f41 should not be returned)
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.gandalfIn(zeCollection), "f4").has(recordsInOrder());
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.gandalfIn(zeCollection), "f5").has(recordsInOrder("d51"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.gandalfIn(zeCollection), "f6").has(recordsInOrder("d62"));
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.gandalfIn(zeCollection), "f7").has(recordsInOrder("d72"));

		//This is an accepted problem (f81 should not be returned)
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(users.gandalfIn(zeCollection), "f8").has(recordsInOrder());

	}

	@Test
	public void whenUserIsNavigatingDocumentLinkedFoldersThenOnlySeesAccessibleFolders()
			throws Exception {
		MetadataSchema schema = rm.schemaType("document").getDefaultSchema();
		Metadata metadata = schema.getMetadata("linkedTo");
		User bob = users.bobIn(zeCollection);
		User charles = users.charlesIn(zeCollection);

		AdministrativeUnit au1 = rm.getAdministrativeUnit(records.unitId_10);
		AdministrativeUnit au2 = rm.newAdministrativeUnitWithId(records.unitId_20);

		Folder folder1 = newFolderInUnit(au1, "folder1");
		Folder folder2 = newFolderInUnit(au1, "folder2");
		Folder folder3 = newFolderInUnit(au1, "folder3");
		Folder folder4 = newFolderInUnit(au1, "folder4");
		Folder folder5 = newFolderInUnit(au1, "folder5");
		Folder folderZ = newFolderInUnit(au2, "folderZ");

		Document doc0 = rm.newDocumentWithId("doc0").setFolder(folder4).setTitle("Beta");
		Document doc1 = rm.newDocumentWithId("doc1").setFolder(folderZ).setTitle("Zeta");
		Document doc2 = rm.newDocumentWithId("doc2").setFolder(folderZ).setTitle("Gamma");
		Document doc3 = rm.newDocumentWithId("doc3").setFolder(folderZ).setTitle("Alpha");
		Document doc4 = rm.newDocumentWithId("doc4").setFolder(folder4).setTitle("Delta");

		// Setting document links to folders
		List<Folder> doc0Refs = new ArrayList<>();
		doc0Refs.add(folder4);
		doc0.set(metadata, doc0Refs);

		List<Folder> doc1Refs = new ArrayList<>();
		doc1Refs.add(folder2);
		doc1Refs.add(folder4);
		doc1.set(metadata, doc1Refs);

		List<Folder> doc2Refs = new ArrayList<>();
		doc2Refs.add(folder3);
		doc2Refs.add(folder4);
		doc2.set(metadata, doc2Refs);

		List<Folder> doc3Refs = new ArrayList<>();
		doc3Refs.add(folder3);
		doc3Refs.add(folder5);
		doc3.set(metadata, doc3Refs);

		List<Folder> doc4Refs = new ArrayList<>();
		doc4Refs.add(folder4);
		doc4.set(metadata, doc4Refs);

		recordServices.execute(new Transaction(
				folder1, folder2, folder3, folder4, folder5, folderZ, doc0, doc1, doc2, doc3, doc4
		));

		assertThat((Object) rm.wrapDocument(recordServices.getDocumentById(doc1.getId())).get(metadata)).isInstanceOf(List.class);
		assertThat((List) rm.wrapDocument(recordServices.getDocumentById(doc1.getId())).getLinkedTo()).isEqualTo(doc1.getLinkedTo());

		authsServices.add(authorizationForUsers(users.bobIn(zeCollection)).on(folder1.getId()).givingReadAccess());
		authsServices.add(authorizationForUsers(users.bobIn(zeCollection)).on(folder2.getId()).givingReadAccess());
		authsServices.add(authorizationForUsers(users.bobIn(zeCollection)).on(folder3.getId()).givingReadAccess());
		authsServices.add(authorizationForUsers(users.bobIn(zeCollection)).on(folder4.getId()).givingReadAccess());

		authsServices.add(authorizationForUsers(users.bobIn(zeCollection)).on(doc1.getId()).givingReadAccess());
		authsServices.add(authorizationForUsers(users.bobIn(zeCollection)).on(doc2.getId()).givingReadAccess());

		recordServices.update(charles.setCollectionReadAccess(true));

		// Bob
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(bob, folder1.getId())
				.has(recordsInOrder())
				.has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(bob, folder2.getId())
				.has(recordsInOrder(doc1.getId()))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(bob, folder3.getId())
				.has(recordsInOrder(doc2.getId()))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

		// Should be sorted by title in alphabetical order
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(bob, folder4.getId())
				.has(recordsInOrder(doc0.getId(), doc4.getId(), doc2.getId(), doc1.getId()))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(bob, folder5.getId())
				.has(recordsInOrder())
				.has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0));

		// Charles
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(charles, folder1.getId())
				.has(recordsInOrder())
				.has(numFoundAndListSize(0))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(charles, folder2.getId())
				.has(recordsInOrder(doc1.getId()))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(charles, folder3.getId())
				.has(recordsInOrder(doc2.getId(), doc3.getId()))
				.has(numFoundAndListSize(2))
				.has(solrQueryCounts(0, 0, 0));

		// Should be sorted by title in alphabetical order
		assertThatChildWhenUserNavigateUsingPlanTaxonomy(charles, folder4.getId())
				.has(recordsInOrder(doc0.getId(), doc4.getId(), doc2.getId(), doc1.getId())).has(numFoundAndListSize(2)) // 4 once linkedTo included in taxonomies
				.has(solrQueryCounts(0, 0, 0));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(charles, folder5.getId())
				.has(recordsInOrder(doc3.getId()))
				.has(numFoundAndListSize(1))
				.has(solrQueryCounts(0, 0, 0));
	}


	//------ ------- ------ ------ ------- ------


	private Folder newFolderInCategory(Category category, String title) {
		return rm.newFolder().setCategoryEntered(category).setTitle(title).setOpenDate(new LocalDate())
				.setRetentionRuleEntered(records.ruleId_1).setAdministrativeUnitEntered(records.unitId_10a);
	}

	private Folder newFolderInUnit(AdministrativeUnit unit, String title) {
		return rm.newFolder().setCategoryEntered(records.categoryId_X100).setTitle(title).setOpenDate(new LocalDate())
				.setRetentionRuleEntered(records.ruleId_1).setAdministrativeUnitEntered(unit);
	}

	private String toTitle(int i) {
		String value = "0000" + i;
		return value.substring(value.length() - 5, value.length());
	}

	// -------

	private void addRecordsInRandomOrder(List<? extends RecordWrapper> records) {
		List<RecordWrapper> copy = new ArrayList<>(records);

		RecordWrapper addedBefore = copy.remove(23);
		RecordWrapper addedAfter = copy.remove(24);

		try {
			recordServices.add(addedBefore);
			Transaction transaction = new Transaction();
			transaction.addUpdate(new RecordUtils().unwrap(copy));
			recordServices.execute(transaction);
			recordServices.add(addedAfter);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

	}

	private Condition<? super LinkableTaxonomySearchResponseCaller> numFoundAndListSize(final int expectedCount) {
		return new Condition<LinkableTaxonomySearchResponseCaller>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponseCaller value) {
				assertThat(value.firstAnswer().getNumFound()).describedAs(description().toString() + " NumFound on first call")
						.isEqualTo(expectedCount);
				assertThat(value.firstAnswer().getRecords().size())
						.describedAs(description().toString() + " records list size on first call")
						.isEqualTo(expectedCount);

				assertThat(value.secondAnswer().getNumFound()).describedAs(description().toString() + " NumFound on second call")
						.isEqualTo(expectedCount);
				assertThat(value.secondAnswer().getRecords().size())
						.describedAs(description().toString() + " records list size on second call")
						.isEqualTo(expectedCount);
				return true;
			}
		};
	}

	private Condition<? super LinkableTaxonomySearchResponseCaller> numFound(final int expectedCount) {
		return new Condition<LinkableTaxonomySearchResponseCaller>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponseCaller value) {
				assertThat(value.firstAnswer().getNumFound()).describedAs("first call NumFound").isEqualTo(expectedCount);

				assertThat(value.secondAnswer().getNumFound()).describedAs("second call NumFound").isEqualTo(expectedCount);
				return true;
			}
		};
	}

	private Condition<? super LinkableTaxonomySearchResponseCaller> numFoundGreaterThan(final int expectedCount) {
		return new Condition<LinkableTaxonomySearchResponseCaller>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponseCaller value) {
				assertThat(value.firstAnswer().getNumFound()).describedAs("first call NumFound").isGreaterThan(expectedCount);

				assertThat(value.secondAnswer().getNumFound()).describedAs("second call NumFound").isGreaterThan(expectedCount);
				return true;
			}
		};
	}


	private Condition<? super LinkableTaxonomySearchResponseCaller> listSize(final int expectedCount) {
		return new Condition<LinkableTaxonomySearchResponseCaller>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponseCaller value) {
				assertThat(value.firstAnswer().getRecords().size()).describedAs("first call records list size")
						.isEqualTo(expectedCount);
				assertThat(value.secondAnswer().getRecords().size()).describedAs("second call records list size")
						.isEqualTo(expectedCount);
				return true;
			}
		};
	}

	private Condition<? super LinkableTaxonomySearchResponseCaller> recordsInOrder(String... ids) {
		final List<String> idsList = asList(ids);
		return new Condition<LinkableTaxonomySearchResponseCaller>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponseCaller response) {
				List<String> valueIds = new ArrayList<>();
				for (TaxonomySearchRecord value : response.firstAnswer().getRecords()) {
					valueIds.add(value.getRecord().getId());
				}
				assertThat(valueIds).describedAs(description().toString()).isEqualTo(idsList);

				List<String> valueIds1 = new ArrayList<>();
				for (TaxonomySearchRecord value : response.secondAnswer().getRecords()) {
					valueIds1.add(value.getRecord().getId());
				}
				assertThat(valueIds1).describedAs(description().toString()).isEqualTo(idsList);
				return true;
			}
		};
	}

	private Condition<? super LinkableTaxonomySearchResponseCaller> noRecordsWithChildren() {
		return recordsWithChildren();
	}

	private Condition<? super LinkableTaxonomySearchResponseCaller> recordsWithChildren(String... ids) {
		final List<String> idsList = asList(ids);
		return new Condition<LinkableTaxonomySearchResponseCaller>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponseCaller response) {
				List<String> valueIds = new ArrayList<>();
				for (TaxonomySearchRecord value : response.firstAnswer().getRecords()) {
					if (value.hasChildren()) {
						valueIds.add(value.getRecord().getId());
					}
				}
				assertThat(valueIds).describedAs(description().toString()).isEqualTo(idsList);

				List<String> valueIdsSecondCall = new ArrayList<>();
				for (TaxonomySearchRecord value : response.secondAnswer().getRecords()) {
					if (value.hasChildren()) {
						valueIdsSecondCall.add(value.getRecord().getId());
					}
				}
				assertThat(valueIds).describedAs(description().toString()).isEqualTo(idsList);
				return true;
			}
		};
	}

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

				List<Record> actualRecordsSecondCall = new ArrayList<>();
				List<Record> recordsInExpectedOrderSecondCall = new ArrayList<>();

				for (TaxonomySearchRecord value : values) {
					actualRecordsSecondCall.add(value.getRecord());
					recordsInExpectedOrderSecondCall.add(value.getRecord());
				}

				final List<String> typesOrderSecondCall = asList(Category.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE,
						ContainerRecord.SCHEMA_TYPE, Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE);

				Collections.sort(recordsInExpectedOrder, new Comparator<Record>() {
					@Override
					public int compare(Record r1, Record r2) {

						int r1TypeIndex = typesOrderSecondCall.indexOf(new SchemaUtils().getSchemaTypeCode(r1.getSchemaCode()));
						int r2TypeIndex = typesOrderSecondCall.indexOf(new SchemaUtils().getSchemaTypeCode(r2.getSchemaCode()));

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

				assertThat(actualRecordsSecondCall).isEqualTo(recordsInExpectedOrderSecondCall);
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
						assertThat(foundRecord.isLinkable()).isFalse();
					}

				}

				return true;
			}
		};
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
						assertThat(foundRecord.isLinkable()).isTrue();
					}

				}

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

	private ConditionTemplate withoutFilters = null;

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatRootWhenUserNavigateUsingPlanTaxonomy(
			User user) {
		return assertThatRootWhenUserNavigateUsingPlanTaxonomy(user, 0, 10000);
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatRootWhenUserNavigateUsingPlanTaxonomy(
			final User user,
			final int start,
			final int rows) {
		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {
			@Override
			protected LinkableTaxonomySearchResponse call() {
				LinkableTaxonomySearchResponse response = service.getVisibleRootConceptResponse(
						user, zeCollection, CLASSIFICATION_PLAN, new TaxonomiesSearchOptions().setStartRow(start).setRows(rows),
						null);

				if (rows == 10000) {
					assertThat(response.getNumFound()).isEqualTo(response.getRecords().size());
				}
				return response;
			}
		});
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatRootWhenUserNavigateUsingPlanTaxonomy(
			final User user,
			final TaxonomiesSearchOptions options) {
		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {
			@Override
			protected LinkableTaxonomySearchResponse call() {
				LinkableTaxonomySearchResponse response = service.getVisibleRootConceptResponse(
						user, zeCollection, CLASSIFICATION_PLAN, options, null);
				if (options.getRows() == 10000) {
					assertThat(response.getNumFound()).isEqualTo(response.getRecords().size());
				}
				return response;
			}
		});
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatRootWhenUserNavigateUsingAdministrativeUnitsTaxonomy(
			final User user,
			final TaxonomiesSearchOptions options) {

		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {

			@Override
			protected LinkableTaxonomySearchResponse call() {
				LinkableTaxonomySearchResponse response = service.getVisibleRootConceptResponse(
						user, zeCollection, RMTaxonomies.ADMINISTRATIVE_UNITS, options, null);

				if (options.getRows() == 10000) {
					assertThat(response.getNumFound()).isEqualTo(response.getRecords().size());
				}
				return response;
			}
		});
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatChildWhenUserNavigateUsingPlanTaxonomy(
			User user,
			String category) {
		return assertThatChildWhenUserNavigateUsingPlanTaxonomy(user, category, 0, 10000);
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatChildWhenUserNavigateUsingPlanTaxonomy(
			final User user,
			final String category,
			final int start, final int rows) {

		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {

			@Override
			protected LinkableTaxonomySearchResponse call() {
				Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
				LinkableTaxonomySearchResponse response = service
						.getVisibleChildConceptResponse(user, CLASSIFICATION_PLAN, inRecord,
								new TaxonomiesSearchOptions().setStartRow(start).setRows(rows));

				if (rows == 10000) {
					assertThat(response.getNumFound()).isEqualTo(response.getRecords().size());
				}
				return response;
			}
		});
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatChildWhenUserNavigateUsingPlanTaxonomy(
			final User user,
			final String category, final TaxonomiesSearchOptions options) {

		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {

			@Override
			protected LinkableTaxonomySearchResponse call() {
				Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
				LinkableTaxonomySearchResponse response = service
						.getVisibleChildConceptResponse(user, CLASSIFICATION_PLAN, inRecord, options);

				if (options.getRows() == 10000) {
					assertThat(response.getNumFound()).isEqualTo(response.getRecords().size());
				}
				return response;
			}
		});
	}

	private ObjectAssert<LinkableTaxonomySearchResponseCaller> assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(
			final User user,
			final String category, final TaxonomiesSearchOptions options) {

		return assertThat((LinkableTaxonomySearchResponseCaller) new LinkableTaxonomySearchResponseCaller() {

			@Override
			protected LinkableTaxonomySearchResponse call() {
				Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
				LinkableTaxonomySearchResponse response = service
						.getVisibleChildConceptResponse(user, RMTaxonomies.ADMINISTRATIVE_UNITS, inRecord, options);

				if (options.getRows() == 10000) {
					assertThat(response.getNumFound()).isEqualTo(response.getRecords().size());
				}
				return response;
			}
		});
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
				return true;
			}
		};
	}

}
