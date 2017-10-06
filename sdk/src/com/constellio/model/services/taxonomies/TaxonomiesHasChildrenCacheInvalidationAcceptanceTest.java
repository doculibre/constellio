package com.constellio.model.services.taxonomies;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.entities.security.global.AuthorizationModificationRequest.modifyAuthorizationOnRecord;
import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.ListAssert;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.AuthorizationDeleteRequest;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class TaxonomiesHasChildrenCacheInvalidationAcceptanceTest extends ConstellioTest {

	private static String FOLDER1 = "folder1";

	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	RMSchemasRecordsServices rm;
	AuthorizationsServices authServices;

	MemoryTaxonomiesSearchServicesCache cache;
	List<String> observedCacheIds = new ArrayList<>();

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withAllTest(users));
		//TODO Do not run this test with ignite cache
		cache = (MemoryTaxonomiesSearchServicesCache) getModelLayerFactory().getTaxonomiesSearchServicesCache();
		recordServices = getModelLayerFactory().newRecordServices();
		authServices = getModelLayerFactory().newAuthorizationsServices();

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		observedCacheIds.add(records.categoryId_X);
		observedCacheIds.add(records.categoryId_X100);
		observedCacheIds.add(records.categoryId_X110);
		observedCacheIds.add(records.categoryId_Z);
		observedCacheIds.add(records.categoryId_Z100);
		observedCacheIds.add(records.categoryId_Z110);

		observedCacheIds.add(records.unitId_10);
		observedCacheIds.add(records.unitId_10a);
		observedCacheIds.add(records.unitId_30);
		observedCacheIds.add(records.unitId_30c);

		Transaction tx = new Transaction();

		tx.add(rm.newFolderWithId(FOLDER1)
				.setAdministrativeUnitEntered(records.unitId_10a)
				.setCategoryEntered(records.categoryId_X110)
				.setRetentionRuleEntered(records.ruleId_1)
				.setOpenDate(aDate())
				.setTitle(FOLDER1));

		recordServices.execute(tx);

		authServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).givingReadAccess().on(FOLDER1));

		loadCache();

		assertThatInvalidatedEntriesSinceLastCheck();
	}

	@Test
	public void whenAFolderIsCreatedThenCacheIsInvalidatedForHasChildrenQueriesWithoutResultsInHierarchies()
			throws Exception {

		recordServices.add(rm.newFolderWithId("newFolder")
				.setAdministrativeUnitEntered(records.unitId_30)
				.setCategoryEntered(records.categoryId_Z100)
				.setRetentionRuleEntered(records.ruleId_1)
				.setOpenDate(aDate())
				.setTitle("New folder"));

		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_Z robin selecting-document false",
				"categoryId_Z robin selecting-folder false",
				"categoryId_Z robin visible false",
				"categoryId_Z sasquatch selecting-document false",
				"categoryId_Z sasquatch selecting-folder false",
				"categoryId_Z sasquatch visible false",
				"unitId_30 robin selecting-document false",
				"unitId_30 robin selecting-folder false",
				"unitId_30 robin visible false",
				"unitId_30 sasquatch selecting-document false",
				"unitId_30 sasquatch selecting-folder false",
				"unitId_30 sasquatch visible false"
		);

		recordServices.add(rm.newFolderWithId("newFolder2")
				.setAdministrativeUnitEntered(records.unitId_30c)
				.setCategoryEntered(records.categoryId_Z120)
				.setRetentionRuleEntered(records.ruleId_1)
				.setOpenDate(aDate())
				.setTitle("New folder 2"));

		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_Z robin selecting-document false",
				"categoryId_Z robin selecting-folder false",
				"categoryId_Z robin visible false",
				"categoryId_Z sasquatch selecting-document false",
				"categoryId_Z sasquatch selecting-folder false",
				"categoryId_Z sasquatch visible false",
				"unitId_30 robin selecting-document false",
				"unitId_30 robin selecting-folder false",
				"unitId_30 robin visible false",
				"unitId_30 sasquatch selecting-document false",
				"unitId_30 sasquatch selecting-folder false",
				"unitId_30 sasquatch visible false"
		);

		recordServices.add(rm.newFolderWithId("newFolder3")
				.setParentFolder("newFolder2")
				.setOpenDate(aDate())
				.setTitle("New folder 2"));

		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_Z robin selecting-document false",
				"categoryId_Z robin selecting-folder false",
				"categoryId_Z robin visible false",
				"categoryId_Z sasquatch selecting-document false",
				"categoryId_Z sasquatch selecting-folder false",
				"categoryId_Z sasquatch visible false",
				"unitId_30 robin selecting-document false",
				"unitId_30 robin selecting-folder false",
				"unitId_30 robin visible false",
				"unitId_30 sasquatch selecting-document false",
				"unitId_30 sasquatch selecting-folder false",
				"unitId_30 sasquatch visible false"
		);

	}

	@Test
	public void whenADocumentIsCreatedThenCacheIsInvalidatedForHasChildrenQueriesWithoutResultsInHierarchies()
			throws Exception {

		recordServices.add(rm.newDocumentWithId("newDocument").setTitle("New document").setFolder(FOLDER1));

		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-folder false",
				"categoryId_X robin visible false",
				"unitId_10 robin selecting-folder false",
				"unitId_10 robin visible false"
		);

	}

	@Test
	public void whenAFolderIsModifiedThenCacheIsInvalidatedForHasChildrenQueriesWithoutResultsInNewHierarchiesAndWithResultsInPreviousHierarchies()
			throws Exception {

		Folder folder1 = rm.getFolder(FOLDER1);

		recordServices.update(folder1.setCategoryEntered(records.categoryId_X120));

		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X110 sasquatch visible true"
		);

		recordServices.update(folder1.setCategoryEntered(records.categoryId_Z120)
				.setAdministrativeUnitEntered(records.unitId_30c));

		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible true",
				"categoryId_X100 sasquatch visible true",
				"categoryId_Z robin selecting-document false",
				"categoryId_Z robin selecting-folder false",
				"categoryId_Z robin visible false",
				"categoryId_Z sasquatch selecting-document false",
				"categoryId_Z sasquatch selecting-folder false",
				"categoryId_Z sasquatch visible false",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible true",
				"unitId_10a sasquatch visible true",
				"unitId_30 robin selecting-document false",
				"unitId_30 robin selecting-folder false",
				"unitId_30 robin visible false",
				"unitId_30 sasquatch selecting-document false",
				"unitId_30 sasquatch selecting-folder false",
				"unitId_30 sasquatch visible false"
		);

	}

	@Test
	public void whenAFolderIsLogicallyDeletedThenCacheIsInvalidatedForHasChildrenQueriesWithResultsInHierarchies()
			throws Exception {

		recordServices.logicallyDelete(record(FOLDER1), User.GOD);
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible true",
				"categoryId_X100 sasquatch visible true",
				"categoryId_X110 sasquatch visible true",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible true",
				"unitId_10a sasquatch visible true"
		);
	}

	@Test
	public void whenAFolderIsPhysicallyDeletedThenCacheIsInvalidatedForHasChildrenQueriesWithResultsInHierarchies()
			throws Exception {

		recordServices.physicallyDeleteNoMatterTheStatus(record(FOLDER1), User.GOD, new RecordPhysicalDeleteOptions());
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible true",
				"categoryId_X100 sasquatch visible true",
				"categoryId_X110 sasquatch visible true",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible true",
				"unitId_10a sasquatch visible true"
		);
	}

	@Test
	public void whenAFolderHasANewArchivisticStatusThenCacheIsInvalidatedForHasChildrenQueriesWithResultsInHierarchies()
			throws Exception {

		recordServices.update(rm.getFolder(FOLDER1).setActualTransferDate(date(2016, 1, 1)));
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible true",
				"categoryId_X100 sasquatch visible true",
				"categoryId_X110 sasquatch visible true",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible true",
				"unitId_10a sasquatch visible true"
		);

		recordServices.update(rm.getFolder(FOLDER1).setActualTransferDate(null));
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder false",
				"categoryId_X robin visible false",
				"categoryId_X sasquatch selecting-document false",
				"categoryId_X sasquatch visible false",
				"categoryId_X100 sasquatch visible false",
				"unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder false",
				"unitId_10 robin visible false",
				"unitId_10 sasquatch selecting-document false",
				"unitId_10 sasquatch visible false",
				"unitId_10a sasquatch visible false"
		);

	}

	@Test
	public void whenAFolderReceiveNewAuthorizationsThenInvalidated()
			throws Exception {

		String auth = authServices.add(authorizationForUsers(users.robinIn(zeCollection)).givingReadAccess().on(FOLDER1));
//		cache.invalidateWithoutChildren(FOLDER1);
		//		cache.invalidateWithoutChildren(records.categoryId_X110);
		//		cache.invalidateWithoutChildren(records.categoryId_X100);
		//		cache.invalidateWithoutChildren(records.categoryId_X);
		//		cache.invalidateWithoutChildren(records.unitId_10a);
		//		cache.invalidateWithoutChildren(records.unitId_10);
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder false",
				"categoryId_X robin visible false",
				"categoryId_X sasquatch selecting-document false",
				"unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder false",
				"unitId_10 robin visible false",
				"unitId_10 sasquatch selecting-document false"
		);

		authServices.execute(modifyAuthorizationOnRecord(auth, record(FOLDER1))
				.withNewPrincipalIds(users.sasquatchIn(zeCollection).getId()));
//		cache.invalidateWithChildren(FOLDER1);
//		cache.invalidateWithoutChildren(FOLDER1);
//		cache.invalidateWithChildren(records.categoryId_X110);
//		cache.invalidateWithChildren(records.categoryId_X100);
//		cache.invalidateWithChildren(records.categoryId_X);
//		cache.invalidateWithChildren(records.unitId_10a);
//		cache.invalidateWithChildren(records.unitId_10);
//		cache.invalidateWithoutChildren(records.categoryId_X110);
//		cache.invalidateWithoutChildren(records.categoryId_X100);
//		cache.invalidateWithoutChildren(records.categoryId_X);
//		cache.invalidateWithoutChildren(records.unitId_10a);
//		cache.invalidateWithoutChildren(records.unitId_10);
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder true",
				"categoryId_X robin visible true",
				"categoryId_X sasquatch selecting-document false",
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible true",
				"categoryId_X100 robin visible true",
				"categoryId_X100 sasquatch visible true",
				"categoryId_X110 robin visible true",
				"categoryId_X110 sasquatch visible true",
				"unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder true",
				"unitId_10 robin visible true",
				"unitId_10 sasquatch selecting-document false",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible true",
				"unitId_10a robin visible true",
				"unitId_10a sasquatch visible true"
		);

		authServices.execute(modifyAuthorizationOnRecord(auth, record(FOLDER1))
				.withNewPrincipalIds(users.robinIn(zeCollection).getId()));
//		cache.invalidateWithChildren(FOLDER1);
//		cache.invalidateWithoutChildren(FOLDER1);
//		cache.invalidateWithChildren(records.categoryId_X110);
//		cache.invalidateWithChildren(records.categoryId_X100);
//		cache.invalidateWithChildren(records.categoryId_X);
//		cache.invalidateWithChildren(records.unitId_10a);
//		cache.invalidateWithChildren(records.unitId_10);
//		cache.invalidateWithoutChildren(records.categoryId_X110);
//		cache.invalidateWithoutChildren(records.categoryId_X100);
//		cache.invalidateWithoutChildren(records.categoryId_X);
//		cache.invalidateWithoutChildren(records.unitId_10a);
//		cache.invalidateWithoutChildren(records.unitId_10);
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder false",
				"categoryId_X robin visible false",
				"categoryId_X sasquatch selecting-document false",
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible true",
				"categoryId_X100 sasquatch visible true",
				"categoryId_X110 sasquatch visible true",
				"unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder false",
				"unitId_10 robin visible false",
				"unitId_10 sasquatch selecting-document false",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible true",
				"unitId_10a sasquatch visible true"
		);

		authServices.execute(AuthorizationDeleteRequest.authorizationDeleteRequest(auth, zeCollection));
//		cache.invalidateWithChildren(FOLDER1);
//		cache.invalidateWithChildren(records.categoryId_X110);
//		cache.invalidateWithChildren(records.categoryId_X100);
//		cache.invalidateWithChildren(records.categoryId_X);
//		cache.invalidateWithChildren(records.unitId_10a);
//		cache.invalidateWithChildren(records.unitId_10);
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-folder true",
				"categoryId_X robin visible true",
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible true",
				"categoryId_X100 robin visible true",
				"categoryId_X100 sasquatch visible true",
				"categoryId_X110 robin visible true",
				"categoryId_X110 sasquatch visible true",
				"unitId_10 robin selecting-folder true",
				"unitId_10 robin visible true",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible true",
				"unitId_10a robin visible true",
				"unitId_10a sasquatch visible true"
		);
	}

	@Test
	public void givenDetachedFolderReceiveNewAuthorizationsThenInvalidated()
			throws Exception {

		authServices.detach(record(FOLDER1));

		String auth = authServices.add(authorizationForUsers(users.robinIn(zeCollection)).givingReadAccess().on(FOLDER1));
//		cache.invalidateWithoutChildren(FOLDER1);
//		cache.invalidateWithoutChildren(records.categoryId_X110);
//		cache.invalidateWithoutChildren(records.categoryId_X100);
//		cache.invalidateWithoutChildren(records.categoryId_X);
//		cache.invalidateWithoutChildren(records.unitId_10a);
//		cache.invalidateWithoutChildren(records.unitId_10);
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder false",
				"categoryId_X robin visible false",
				"categoryId_X sasquatch selecting-document false",
				"unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder false",
				"unitId_10 robin visible false",
				"unitId_10 sasquatch selecting-document false"
		);

		authServices.execute(modifyAuthorizationOnRecord(auth, record(FOLDER1))
				.withNewPrincipalIds(users.sasquatchIn(zeCollection).getId()));
//		cache.invalidateWithChildren(FOLDER1);
//		cache.invalidateWithoutChildren(FOLDER1);
//		cache.invalidateWithChildren(records.categoryId_X110);
//		cache.invalidateWithChildren(records.categoryId_X100);
//		cache.invalidateWithChildren(records.categoryId_X);
//		cache.invalidateWithChildren(records.unitId_10a);
//		cache.invalidateWithChildren(records.unitId_10);
//		cache.invalidateWithoutChildren(records.categoryId_X110);
//		cache.invalidateWithoutChildren(records.categoryId_X100);
//		cache.invalidateWithoutChildren(records.categoryId_X);
//		cache.invalidateWithoutChildren(records.unitId_10a);
//		cache.invalidateWithoutChildren(records.unitId_10);
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder true",
				"categoryId_X robin visible true",
				"categoryId_X sasquatch selecting-document false",
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible true",
				"categoryId_X100 robin visible true",
				"categoryId_X100 sasquatch visible true",
				"categoryId_X110 robin visible true",
				"categoryId_X110 sasquatch visible true",
				"unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder true",
				"unitId_10 robin visible true",
				"unitId_10 sasquatch selecting-document false",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible true",
				"unitId_10a robin visible true",
				"unitId_10a sasquatch visible true"
		);

		authServices.execute(modifyAuthorizationOnRecord(auth, record(FOLDER1))
				.withNewPrincipalIds(users.robinIn(zeCollection).getId()));
//		cache.invalidateWithChildren(FOLDER1);
//		cache.invalidateWithoutChildren(FOLDER1);
//		cache.invalidateWithChildren(records.categoryId_X110);
//		cache.invalidateWithChildren(records.categoryId_X100);
//		cache.invalidateWithChildren(records.categoryId_X);
//		cache.invalidateWithChildren(records.unitId_10a);
//		cache.invalidateWithChildren(records.unitId_10);
//		cache.invalidateWithoutChildren(records.categoryId_X110);
//		cache.invalidateWithoutChildren(records.categoryId_X100);
//		cache.invalidateWithoutChildren(records.categoryId_X);
//		cache.invalidateWithoutChildren(records.unitId_10a);
//		cache.invalidateWithoutChildren(records.unitId_10);
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder false",
				"categoryId_X robin visible false",
				"categoryId_X sasquatch selecting-document false",
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible true",
				"categoryId_X100 sasquatch visible true",
				"categoryId_X110 sasquatch visible true",
				"unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder false",
				"unitId_10 robin visible false",
				"unitId_10 sasquatch selecting-document false",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible true",
				"unitId_10a sasquatch visible true"
		);

		authServices.execute(AuthorizationDeleteRequest.authorizationDeleteRequest(auth, zeCollection));
//		cache.invalidateWithChildren(FOLDER1);
//		cache.invalidateWithChildren(records.categoryId_X110);
//		cache.invalidateWithChildren(records.categoryId_X100);
//		cache.invalidateWithChildren(records.categoryId_X);
//		cache.invalidateWithChildren(records.unitId_10a);
//		cache.invalidateWithChildren(records.unitId_10);
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-folder true",
				"categoryId_X robin visible true",
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible true",
				"categoryId_X100 robin visible true",
				"categoryId_X100 sasquatch visible true",
				"categoryId_X110 robin visible true",
				"categoryId_X110 sasquatch visible true",
				"unitId_10 robin selecting-folder true",
				"unitId_10 robin visible true",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible true",
				"unitId_10a robin visible true",
				"unitId_10a sasquatch visible true"
		);

		authServices.reset(record(FOLDER1));
//		cache.invalidateWithChildren(FOLDER1);
//		cache.invalidateWithoutChildren(FOLDER1);
//		cache.invalidateWithChildren(records.categoryId_X110);
//		cache.invalidateWithChildren(records.categoryId_X100);
//		cache.invalidateWithChildren(records.categoryId_X);
//		cache.invalidateWithChildren(records.unitId_10a);
//		cache.invalidateWithChildren(records.unitId_10);
//		cache.invalidateWithoutChildren(records.categoryId_X110);
//		cache.invalidateWithoutChildren(records.categoryId_X100);
//		cache.invalidateWithoutChildren(records.categoryId_X);
//		cache.invalidateWithoutChildren(records.unitId_10a);
//		cache.invalidateWithoutChildren(records.unitId_10);
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder false",
				"categoryId_X robin visible false",
				"categoryId_X sasquatch selecting-document false",
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible true",
				"categoryId_X100 sasquatch visible true",
				"categoryId_X110 sasquatch visible true",
				"unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder false",
				"unitId_10 robin visible false",
				"unitId_10 sasquatch selecting-document false",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible true",
				"unitId_10a sasquatch visible true"
		);
	}

	//--------------------------------------------------------------

	private void loadCacheForIds() {
	}

	private List<String> previousEntries = new ArrayList<>();

	private ListAssert<String> assertThatInvalidatedEntriesSinceLastCheck() {
		List<String> entriesNow = getCacheEntries();

		ListAssert<String> assertion = assertThat(LangUtils.compare(previousEntries, entriesNow).getRemovedItems());

		loadCache();
		previousEntries = getCacheEntries();
		return assertion;
	}

	private List<String> getCacheEntries() {

		List<String> entriesInfo = new ArrayList<>();

		for (String id : observedCacheIds) {
			Map<String, Map<String, Boolean>> recordCache = cache.getMemoryCache(id);
			for (Map.Entry<String, Map<String, Boolean>> entry : recordCache.entrySet()) {
				String user = entry.getKey();
				Map<String, Boolean> modes = entry.getValue();
				for (Map.Entry<String, Boolean> entry2 : modes.entrySet()) {
					entriesInfo.add(id + " " + user + " " + entry2.getKey() + " " + entry2.getValue());
				}
			}
		}
		Collections.sort(entriesInfo);

		for (String entry : entriesInfo) {
			System.out.println(entry);
		}

		return entriesInfo;
	}

	private void loadCache() {
		TaxonomiesManager taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		for (String username : asList(sasquatch, robin)) {
			User user = getModelLayerFactory().newUserServices().getUserInCollection(username, zeCollection);
			for (Taxonomy taxonomy : taxonomiesManager.getEnabledTaxonomies(zeCollection)) {

				TaxonomiesSearchOptions options = new TaxonomiesSearchOptions().setRows(100);
				for (TaxonomySearchRecord record : new TaxonomiesSearchServices(getModelLayerFactory())
						.getVisibleRootConcept(user, zeCollection, taxonomy.getCode(), options)) {
					navigateVisible(user, taxonomy.getCode(), record.getRecord(), options);
				}

				for (TaxonomySearchRecord record : new TaxonomiesSearchServices(getModelLayerFactory())
						.getLinkableRootConcept(user, zeCollection, taxonomy.getCode(), Folder.SCHEMA_TYPE, options)) {

					navigateLinkableSelectingAFolder(user, taxonomy.getCode(), record.getRecord(), options);
				}

				for (TaxonomySearchRecord record : new TaxonomiesSearchServices(getModelLayerFactory())
						.getLinkableRootConcept(user, zeCollection, taxonomy.getCode(), Document.SCHEMA_TYPE, options)) {

					navigateLinkableSelectingADocument(user, taxonomy.getCode(), record.getRecord(), options);
				}
			}
		}
	}

	private void navigateVisible(User user, String taxonomy, Record record, TaxonomiesSearchOptions options) {
		TaxonomiesSearchServices taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();

		for (TaxonomySearchRecord child : taxonomiesSearchServices
				.getVisibleChildConcept(user, taxonomy, record, options)) {
			navigateVisible(user, taxonomy, child.getRecord(), options);
		}

	}

	private void navigateLinkableSelectingAFolder(User user, String taxonomy, Record record, TaxonomiesSearchOptions options) {
		TaxonomiesSearchServices taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();

		for (TaxonomySearchRecord child : taxonomiesSearchServices
				.getVisibleChildConcept(user, taxonomy, record, options)) {
			navigateLinkableSelectingAFolder(user, taxonomy, child.getRecord(), options);
		}

	}

	private void navigateLinkableSelectingADocument(User user, String taxonomy, Record record, TaxonomiesSearchOptions options) {
		TaxonomiesSearchServices taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();

		for (TaxonomySearchRecord child : taxonomiesSearchServices
				.getVisibleChildConcept(user, taxonomy, record, options)) {
			navigateLinkableSelectingADocument(user, taxonomy, child.getRecord(), options);
		}

	}

}
