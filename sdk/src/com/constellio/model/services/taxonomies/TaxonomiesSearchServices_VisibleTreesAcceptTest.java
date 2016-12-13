package com.constellio.model.services.taxonomies;

import static com.constellio.app.modules.rm.constants.RMTaxonomies.ADMINISTRATIVE_UNITS;
import static com.constellio.app.modules.rm.constants.RMTaxonomies.CLASSIFICATION_PLAN;
import static com.constellio.model.entities.security.global.AuthorizationBuilder.authorizationForUsers;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.assertj.core.api.Condition;
import org.assertj.core.api.ObjectAssert;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.AuthorizationBuilder;
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
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class TaxonomiesSearchServices_VisibleTreesAcceptTest extends ConstellioTest {

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
	AuthorizationsServices authServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTest(users).withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		inCollection(zeCollection).giveReadAccessTo(admin);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		service = getModelLayerFactory().newTaxonomiesSearchService();
		decommissioningService = new DecommissioningService(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

		UserServices userServices = getModelLayerFactory().newUserServices();
		UserCredential userCredential = userServices.getUserCredential(aliceWonderland);
		userServices.addUserToCollection(userCredential, zeCollection);
		alice = userServices.getUserInCollection(aliceWonderland, zeCollection);
		manager = getModelLayerFactory().getMetadataSchemasManager();

		DecommissioningService service = new DecommissioningService(zeCollection, getModelLayerFactory());

		Folder subfolder = service.newSubFolderIn(records.getFolder_A16());
		subfolder.setTitle("Sous-dossier");
		recordServices.add(subfolder);
		subFolderId = subfolder.getId();

		List<String> documentsInA16 = getFolderDocuments(records.folder_A16);
		document1InA16 = documentsInA16.get(0);
		document2InA16 = documentsInA16.get(1);
		document3InA16 = documentsInA16.get(2);

		for (String documentId : getFolderDocuments(records.folder_A17)) {
			Record document = recordServices.getDocumentById(documentId);
			recordServices.logicallyDelete(document, User.GOD);
		}

		for (String documentId : getFolderDocuments(records.folder_A18)) {
			Record document = recordServices.getDocumentById(documentId);
			recordServices.logicallyDelete(document, User.GOD);
		}

		authServices = getModelLayerFactory().newAuthorizationsServices();
	}

	private List<String> getFolderDocuments(String id) {
		return getModelLayerFactory().newSearchServices().searchRecordIds(new LogicalSearchQuery()
				.sortAsc(Schemas.TITLE).setCondition(from(rm.documentSchemaType()).where(rm.documentFolder()).isEqualTo(id)));
	}

	//	@Test
	//	public void givenUserCreateNewTaxonomyAndAssignRecordsToItThenRecordsVisibleInTheTaxonomyTree()
	//			throws Exception {
	//
	//		getModelLayerFactory().newRecordServices().update(alice.setCollectionReadAccess(true));
	//
	//		ValueListServices services = new ValueListServices(getModelLayerFactory(), zeCollection);
	//		String type = services.createTaxonomy("ZE").getSchemaTypes().get(0);
	//
	//		MetadataSchemaTypesBuilder types = manager.modifyTo(zeCollection);
	//		types.getSchema(Folder.SCHEMA_TYPE).create("ZeMetadata").defineTaxonomyRelationshipToType(types.getSchemaType(type));
	//		manager.saveUpdateSchemaTypes(types);
	//
	//		HierarchicalValueListItem item = rm.newHierarchicalValueListItem(type + "_default");
	//
	//	}

	@Test
	public void whenDakotaIsNavigatingATaxonomyWithVisibleRecordsThenSeesRecords()
			throws Exception {

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getDakota_managerInA_userInB())
				.has(recordsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(recordsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(numFoundAndListSize(2));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getDakota_managerInA_userInB(), records.categoryId_X)
				.has(recordsInOrder(records.categoryId_X100))
				.has(recordsWithChildren(records.categoryId_X100))
				.has(numFoundAndListSize(1));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getDakota_managerInA_userInB(), records.categoryId_X100)
				.has(recordsInOrder(records.categoryId_X110, records.categoryId_X120, records.folder_A16, records.folder_A17,
						records.folder_A18, records.folder_B06, records.folder_B32))
				.has(recordsWithChildren(records.categoryId_X110, records.categoryId_X120, records.folder_A16,
						records.folder_B06, records.folder_B32))
				.has(numFoundAndListSize(7));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getDakota_managerInA_userInB(), records.folder_A16)
				.has(recordsInOrder(document1InA16, document2InA16, document3InA16, subFolderId))
				.has(noRecordsWithChildren())
				.has(numFoundAndListSize(4));
	}

	@Test
	public void whenAdminIsNavigatingATaxonomyWithVisibleRecordsThenSeesRecords()
			throws Exception {

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getAdmin())
				.has(recordsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(recordsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(numFoundAndListSize(2));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X)
				.has(recordsInOrder(records.categoryId_X100))
				.has(recordsWithChildren(records.categoryId_X100))
				.has(numFoundAndListSize(1));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100)
				.has(recordsInOrder("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06", "C32", "B32"))
				.has(recordsWithChildren("categoryId_X110", "categoryId_X120", "A16", "C06", "B06", "C32", "B32"))
				.has(numFoundAndListSize(9));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z)
				.has(recordsInOrder(records.categoryId_Z100))
				.has(recordsWithChildren(records.categoryId_Z100))
				.has(numFoundAndListSize(1));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z100)
				.has(recordsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(recordsWithChildren(records.categoryId_Z110, records.categoryId_Z120))
				.has(numFoundAndListSize(2));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z110)
				.has(recordsInOrder(records.categoryId_Z112))
				.has(recordsWithChildren(records.categoryId_Z112))
				.has(numFoundAndListSize(1));

	}

	@Test
	public void whenAdminIsNavigatingATaxonomyWithVisibleRecordsAlwaysDisplayingConceptsWithReadAccessThenSeesRecordsAndAllConcepts()
			throws Exception {

		recordServices.add(rm.newCategoryWithId("category_Y_id").setCode("Y").setTitle("Ze category Y"));

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions().setAlwaysReturnTaxonomyConceptsWithReadAccess(true);

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), options)
				.has(recordsInOrder(records.categoryId_X, "category_Y_id", records.categoryId_Z))
				.has(recordsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(numFoundAndListSize(3));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X, options)
				.has(recordsInOrder(records.categoryId_X13, records.categoryId_X100))
				.has(recordsWithChildren(records.categoryId_X100))
				.has(numFoundAndListSize(2));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100, options)
				.has(recordsInOrder("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06", "C32", "B32"))
				.has(recordsWithChildren("categoryId_X110", "categoryId_X120", "A16", "C06", "B06", "C32", "B32"))
				.has(numFoundAndListSize(9));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z, options)
				.has(recordsInOrder(records.categoryId_Z100, records.categoryId_Z200, records.categoryId_Z999,
						records.categoryId_ZE42))
				.has(recordsWithChildren(records.categoryId_Z100))
				.has(numFoundAndListSize(4));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z100, options)
				.has(recordsInOrder(records.categoryId_Z110, records.categoryId_Z120))
				.has(recordsWithChildren(records.categoryId_Z110, records.categoryId_Z120))
				.has(numFoundAndListSize(2));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_Z110, options)
				.has(recordsInOrder(records.categoryId_Z111, records.categoryId_Z112))
				.has(recordsWithChildren(records.categoryId_Z112))
				.has(numFoundAndListSize(2));

	}

	@Test
	public void whenAdminIsNavigatingAdminUnitTaxonomyWithVisibleRecordsThenSeesRecords()
			throws Exception {

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions();

		assertThatRootWhenUserNavigateUsingAdministrativeUnitsTaxonomy(records.getAdmin(), options)
				.has(recordsInOrder(records.unitId_10, records.unitId_30))
				.has(recordsWithChildren(records.unitId_10, records.unitId_30))
				.has(numFoundAndListSize(2));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(records.getAdmin(), records.unitId_12, options)
				.has(recordsInOrder(records.unitId_12b))
				.has(recordsWithChildren(records.unitId_12b))
				.has(numFoundAndListSize(1));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(records.getAdmin(), records.unitId_12b, options)
				.has(recordsInOrder("B02", "B04", "B06", "B08", "B32"))
				.has(recordsWithChildren("B02", "B04", "B06", "B08", "B32"))
				.has(numFoundAndListSize(5));

	}

	@Test
	public void whenUserIsNavigatingAdminUnitTaxonomyThenOnlySeeConceptsContainingAccessibleRecords()
			throws Exception {

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions();
		User sasquatch = users.sasquatchIn(zeCollection);
		User robin = users.robinIn(zeCollection);
		User admin = users.adminIn(zeCollection);
		authServices.add(authorizationForUsers(sasquatch).on("B06").givingReadAccess(), admin);
		authServices.add(authorizationForUsers(sasquatch).on(records.unitId_20d).givingReadAccess(), admin);

		authServices.add(authorizationForUsers(robin).on("B06").givingReadAccess(), admin);
		authServices.add(authorizationForUsers(robin).on(records.unitId_12c).givingReadAccess(), admin);
		authServices.add(authorizationForUsers(robin).on(records.unitId_30).givingReadAccess(), admin);
		recordServices.refresh(robin);
		recordServices.refresh(sasquatch);
		waitForBatchProcess();
		assertThat(robin.hasReadAccess().on(recordServices.getDocumentById("B06"))).isTrue();
		assertThat(sasquatch.hasReadAccess().on(recordServices.getDocumentById("B06"))).isTrue();

		//Sasquatch
		assertThatRootWhenUserNavigateUsingAdministrativeUnitsTaxonomy(sasquatch, options)
				.has(recordsInOrder(records.unitId_10))
				.has(recordsWithChildren(records.unitId_10))
				.has(numFoundAndListSize(1));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(sasquatch, records.unitId_10, options)
				.has(recordsInOrder(records.unitId_12))
				.has(recordsWithChildren(records.unitId_12))
				.has(numFoundAndListSize(1));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(sasquatch, records.unitId_12, options)
				.has(recordsInOrder(records.unitId_12b))
				.has(recordsWithChildren(records.unitId_12b))
				.has(numFoundAndListSize(1));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(sasquatch, records.unitId_12b, options)
				.has(recordsInOrder("B06"))
				.has(recordsWithChildren("B06"))
				.has(numFoundAndListSize(1));

		//Robin
		assertThatRootWhenUserNavigateUsingAdministrativeUnitsTaxonomy(robin, options)
				.has(recordsInOrder(records.unitId_10, records.unitId_30))
				.has(recordsWithChildren(records.unitId_10, records.unitId_30))
				.has(numFoundAndListSize(2));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(robin, records.unitId_10, options)
				.has(recordsInOrder(records.unitId_12))
				.has(recordsWithChildren(records.unitId_12))
				.has(numFoundAndListSize(1));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(robin, records.unitId_12, options)
				.has(recordsInOrder(records.unitId_12b))
				.has(recordsWithChildren(records.unitId_12b))
				.has(numFoundAndListSize(1));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(robin, records.unitId_12b, options)
				.has(recordsInOrder("B06"))
				.has(recordsWithChildren("B06"))
				.has(numFoundAndListSize(1));
	}

	@Test
	public void whenUserIsNavigatingAdminUnitTaxonomyAlwaysDisplayingConceptsWithReadAccessThenOnlySeeConceptsContainingAccessibleRecordsAndThoseWithReadAccess()
			throws Exception {

		getDataLayerFactory().getDataLayerLogger().setPrintAllQueriesLongerThanMS(0);

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions().setAlwaysReturnTaxonomyConceptsWithReadAccess(true);
		User sasquatch = users.sasquatchIn(zeCollection);
		User robin = users.robinIn(zeCollection);
		User admin = users.adminIn(zeCollection);
		authServices.add(authorizationForUsers(sasquatch).on("B06").givingReadAccess(), admin);
		authServices.add(authorizationForUsers(sasquatch).on(records.unitId_20d).givingReadAccess(), admin);

		authServices.add(authorizationForUsers(robin).on("B06").givingReadAccess(), admin);
		authServices.add(authorizationForUsers(robin).on(records.unitId_12c).givingReadAccess(), admin);
		authServices.add(authorizationForUsers(robin).on(records.unitId_30).givingReadAccess(), admin);

		recordServices.refresh(sasquatch);
		recordServices.refresh(robin);
		waitForBatchProcess();
		//Sasquatch
		assertThatRootWhenUserNavigateUsingAdministrativeUnitsTaxonomy(sasquatch, options)
				.has(recordsInOrder(records.unitId_10, records.unitId_20))
				.has(recordsWithChildren(records.unitId_10, records.unitId_20))
				.has(numFoundAndListSize(2));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(sasquatch, records.unitId_10, options)
				.has(recordsInOrder(records.unitId_12))
				.has(recordsWithChildren(records.unitId_12))
				.has(numFoundAndListSize(1));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(sasquatch, records.unitId_12, options)
				.has(recordsInOrder(records.unitId_12b))
				.has(recordsWithChildren(records.unitId_12b))
				.has(numFoundAndListSize(1));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(sasquatch, records.unitId_12b, options)
				.has(recordsInOrder("B06"))
				.has(recordsWithChildren("B06"))
				.has(numFoundAndListSize(1));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(sasquatch, records.unitId_12c, options)
				.has(numFoundAndListSize(0));

		//Robin
		assertThatRootWhenUserNavigateUsingAdministrativeUnitsTaxonomy(robin, options)
				.has(recordsInOrder(records.unitId_10, records.unitId_30))
				.has(recordsWithChildren(records.unitId_10, records.unitId_30))
				.has(numFoundAndListSize(2));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(robin, records.unitId_10, options)
				.has(recordsInOrder(records.unitId_12))
				.has(recordsWithChildren(records.unitId_12))
				.has(numFoundAndListSize(1));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(robin, records.unitId_12, options)
				.has(recordsInOrder(records.unitId_12b, records.unitId_12c))
				.has(recordsWithChildren(records.unitId_12b))
				.has(numFoundAndListSize(2));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(robin, records.unitId_30, options)
				.has(recordsInOrder(records.unitId_30c))
				.has(recordsWithChildren(records.unitId_30c))
				.has(numFoundAndListSize(1));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(robin, records.unitId_12b, options)
				.has(recordsInOrder("B06"))
				.has(recordsWithChildren("B06"))
				.has(numFoundAndListSize(1));
	}

	@Test
	public void whenAdminIsNavigatingAdminUnityWithVisibleRecordsAlwaysDisplayingConceptsWithReadAccessThenSeesRecordsAndAllConcepts()
			throws Exception {

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions().setAlwaysReturnTaxonomyConceptsWithReadAccess(true);

		assertThatRootWhenUserNavigateUsingAdministrativeUnitsTaxonomy(records.getAdmin(), options)
				.has(recordsInOrder(records.unitId_10, records.unitId_20, records.unitId_30))
				.has(recordsWithChildren(records.unitId_10, records.unitId_20, records.unitId_30))
				.has(numFoundAndListSize(3));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(records.getAdmin(), records.unitId_12, options)
				.has(recordsInOrder(records.unitId_12b, records.unitId_12c))
				.has(recordsWithChildren(records.unitId_12b))
				.has(numFoundAndListSize(2));

		assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(records.getAdmin(), records.unitId_12b, options)
				.has(recordsInOrder("B02", "B04", "B06", "B08", "B32"))
				.has(recordsWithChildren("B02", "B04", "B06", "B08", "B32"))
				.has(numFoundAndListSize(5));

	}

	@Test
	public void whenNavigatingByIntervalThenGetGoodResults()
			throws Exception {

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getAdmin())
				.has(recordsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(recordsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(numFoundAndListSize(2));

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), 0, 2)
				.has(recordsInOrder(records.categoryId_X, records.categoryId_Z))
				.has(recordsWithChildren(records.categoryId_X, records.categoryId_Z))
				.has(numFoundAndListSize(2));

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), 0, 1)
				.has(recordsInOrder(records.categoryId_X))
				.has(recordsWithChildren(records.categoryId_X))
				.has(listSize(1)).has(numFound(2));

		assertThatRootWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), 1, 1)
				.has(recordsInOrder(records.categoryId_Z))
				.has(recordsWithChildren(records.categoryId_Z))
				.has(listSize(1)).has(numFound(2));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100)
				.has(recordsInOrder("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06", "C32", "B32"))
				.has(recordsWithChildren("categoryId_X110", "categoryId_X120", "A16", "C06", "B06", "C32", "B32"))
				.has(listSize(9)).has(numFound(9));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100, 0, 10)
				.has(recordsInOrder("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06", "C32", "B32"))
				.has(recordsWithChildren("categoryId_X110", "categoryId_X120", "A16", "C06", "B06", "C32", "B32"))
				.has(listSize(9)).has(numFound(9));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100, 0, 7)
				.has(recordsInOrder("categoryId_X110", "categoryId_X120", "A16", "A17", "A18", "C06", "B06"))
				.has(recordsWithChildren("categoryId_X110", "categoryId_X120", "A16", "C06", "B06"))
				.has(listSize(7)).has(numFound(9));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100, 0, 3)
				.has(recordsInOrder("categoryId_X110", "categoryId_X120", "A16"))
				.has(recordsWithChildren("categoryId_X110", "categoryId_X120", "A16"))
				.has(listSize(3)).has(numFound(9));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.categoryId_X100, 1, 4)
				.has(recordsInOrder("categoryId_X120", "A16", "A17", "A18"))
				.has(recordsWithChildren("categoryId_X120", "A16"))
				.has(listSize(4)).has(numFound(9));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.folder_A16, 0, 5)
				.has(recordsInOrder(document1InA16, document2InA16, document3InA16, subFolderId))
				.has(noRecordsWithChildren())
				.has(listSize(4)).has(numFound(4));

		assertThatChildWhenUserNavigateUsingPlanTaxonomy(records.getAdmin(), records.folder_A16, 0, 1)
				.has(recordsInOrder(document1InA16))
				.has(noRecordsWithChildren())
				.has(listSize(1)).has(numFound(4));

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
					admin, zeCollection, CLASSIFICATION_PLAN, new TaxonomiesSearchOptions().setStartRow(i).setRows(25));
			List<String> expectedIds = new RecordUtils().toWrappedRecordIdsList(rootCategories.subList(i, i + 25));
			assertThat(response.getNumFound()).isEqualTo(rootCategories.size() + 2);
			assertThat(response.getRecords()).extracting("id").isEqualTo(expectedIds);
		}

		for (int i = 0; i < childCategories.size() - 25; i += 25) {
			LinkableTaxonomySearchResponse response = service.getVisibleChildConceptResponse(admin, CLASSIFICATION_PLAN,
					category42.getWrappedRecord(), new TaxonomiesSearchOptions().setStartRow(i).setRows(25));
			List<String> expectedIds = new RecordUtils().toWrappedRecordIdsList(childCategories.subList(i, i + 25));
			assertThat(response.getNumFound()).isEqualTo(childCategories.size());
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
					admin, zeCollection, ADMINISTRATIVE_UNITS, new TaxonomiesSearchOptions().setStartRow(2 + i).setRows(25));
			List<String> expectedIds = new RecordUtils().toWrappedRecordIdsList(rootAdministrativeUnits.subList(i, i + 25));
			assertThat(response.getNumFound()).isEqualTo(rootAdministrativeUnits.size() + 2);
			assertThat(response.getRecords()).extracting("id").isEqualTo(expectedIds);
		}

		for (int i = 0; i < childAdministrativeUnits.size() - 25; i += 25) {
			LinkableTaxonomySearchResponse response = service.getVisibleChildConceptResponse(admin, ADMINISTRATIVE_UNITS,
					unit42.getWrappedRecord(), new TaxonomiesSearchOptions().setStartRow(i).setRows(25));
			List<String> expectedIds = new RecordUtils().toWrappedRecordIdsList(childAdministrativeUnits.subList(i, i + 25));
			assertThat(response.getNumFound()).isEqualTo(childAdministrativeUnits.size());
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

	private Condition<? super LinkableTaxonomySearchResponse> numFoundAndListSize(final int expectedCount) {
		return new Condition<LinkableTaxonomySearchResponse>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponse value) {
				assertThat(value.getNumFound()).describedAs(description().toString() + " NumFound").isEqualTo(expectedCount);
				assertThat(value.getRecords().size()).describedAs(description().toString() + " records list size")
						.isEqualTo(expectedCount);
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

	private Condition<? super LinkableTaxonomySearchResponse> recordsInOrder(String... ids) {
		final List<String> idsList = asList(ids);
		return new Condition<LinkableTaxonomySearchResponse>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponse response) {
				List<String> valueIds = new ArrayList<>();
				for (TaxonomySearchRecord value : response.getRecords()) {
					valueIds.add(value.getRecord().getId());
				}
				assertThat(valueIds).describedAs(description().toString()).isEqualTo(idsList);
				return true;
			}
		};
	}

	private Condition<? super LinkableTaxonomySearchResponse> noRecordsWithChildren() {
		return recordsWithChildren();
	}

	private Condition<? super LinkableTaxonomySearchResponse> recordsWithChildren(String... ids) {
		final List<String> idsList = asList(ids);
		return new Condition<LinkableTaxonomySearchResponse>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponse response) {
				List<String> valueIds = new ArrayList<>();
				for (TaxonomySearchRecord value : response.getRecords()) {
					if (value.hasChildren()) {
						valueIds.add(value.getRecord().getId());
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

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenUserNavigateUsingPlanTaxonomy(User user) {
		return assertThatRootWhenUserNavigateUsingPlanTaxonomy(user, 0, Integer.MAX_VALUE);
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenUserNavigateUsingPlanTaxonomy(User user, int start,
			int rows) {
		LinkableTaxonomySearchResponse response = service.getVisibleRootConceptResponse(
				user, zeCollection, CLASSIFICATION_PLAN, new TaxonomiesSearchOptions().setStartRow(start).setRows(rows));

		if (rows == Integer.MAX_VALUE) {
			assertThat(response.getNumFound()).isEqualTo(response.getRecords().size());
		}
		return assertThat(response);
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenUserNavigateUsingPlanTaxonomy(User user,
			TaxonomiesSearchOptions options) {
		LinkableTaxonomySearchResponse response = service.getVisibleRootConceptResponse(
				user, zeCollection, CLASSIFICATION_PLAN, options);

		if (options.getRows() == Integer.MAX_VALUE) {
			assertThat(response.getNumFound()).isEqualTo(response.getRecords().size());
		}
		return assertThat(response);
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatRootWhenUserNavigateUsingAdministrativeUnitsTaxonomy(User user,
			TaxonomiesSearchOptions options) {
		LinkableTaxonomySearchResponse response = service.getVisibleRootConceptResponse(
				user, zeCollection, RMTaxonomies.ADMINISTRATIVE_UNITS, options);

		if (options.getRows() == Integer.MAX_VALUE) {
			assertThat(response.getNumFound()).isEqualTo(response.getRecords().size());
		}
		return assertThat(response);
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenUserNavigateUsingPlanTaxonomy(User user,
			String category) {
		return assertThatChildWhenUserNavigateUsingPlanTaxonomy(user, category, 0, Integer.MAX_VALUE);
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenUserNavigateUsingPlanTaxonomy(User user,
			String category,
			int start, int rows) {
		Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
		LinkableTaxonomySearchResponse response = service
				.getVisibleChildConceptResponse(user, CLASSIFICATION_PLAN, inRecord,
						new TaxonomiesSearchOptions().setStartRow(start).setRows(rows));

		if (rows == Integer.MAX_VALUE) {
			assertThat(response.getNumFound()).isEqualTo(response.getRecords().size());
		}
		return assertThat(response);
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenUserNavigateUsingPlanTaxonomy(User user,
			String category, TaxonomiesSearchOptions options) {
		Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
		LinkableTaxonomySearchResponse response = service
				.getVisibleChildConceptResponse(user, CLASSIFICATION_PLAN, inRecord, options);

		if (options.getRows() == Integer.MAX_VALUE) {
			assertThat(response.getNumFound()).isEqualTo(response.getRecords().size());
		}
		return assertThat(response);
	}

	private ObjectAssert<LinkableTaxonomySearchResponse> assertThatChildWhenUserNavigateUsingAdminUnitsTaxonomy(User user,
			String category, TaxonomiesSearchOptions options) {
		Record inRecord = getModelLayerFactory().newRecordServices().getDocumentById(category);
		LinkableTaxonomySearchResponse response = service
				.getVisibleChildConceptResponse(user, RMTaxonomies.ADMINISTRATIVE_UNITS, inRecord, options);

		if (options.getRows() == Integer.MAX_VALUE) {
			assertThat(response.getNumFound()).isEqualTo(response.getRecords().size());
		}
		return assertThat(response);
	}

}
