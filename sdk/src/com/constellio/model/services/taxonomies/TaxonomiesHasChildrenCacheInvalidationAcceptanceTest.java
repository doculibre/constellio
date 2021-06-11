package com.constellio.model.services.taxonomies;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils;
import com.constellio.sdk.tests.setups.Users;
import org.assertj.core.api.ListAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForGroups;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.entities.security.global.AuthorizationDeleteRequest.authorizationDeleteRequest;
import static com.constellio.model.entities.security.global.AuthorizationModificationRequest.modifyAuthorizationOnRecord;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class TaxonomiesHasChildrenCacheInvalidationAcceptanceTest extends ConstellioTest {

	private static String FOLDER1 = "folder1";

	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	RecordServices recordServicesOfOtherInstance;
	RMSchemasRecordsServices rm;
	AuthorizationsServices authServices;
	AuthorizationsServices authServicesOfOtherInstance;

	EventBusTaxonomiesSearchServicesCache cache;
	List<String> observedCacheIds = new ArrayList<>();

	String testCase;

	public TaxonomiesHasChildrenCacheInvalidationAcceptanceTest(String testCase) {
		this.testCase = testCase;
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> testCases() {
		return Arrays.asList(new Object[][]{
				{"testingInvalidationsOnLocalCache"}, {"testingInvalidationsOnRemoteCache"}});
	}

	private static boolean NO_TAXONOMIES_CACHE_INVALIDATION_VALUE, TRY_USING_NEW_CACHE_BASED_TAXONOMIES_SEARCH_SERVICES_QUERY_HANDLER_VALUE;

	@Before
	public void disableToggle() {

	}

	@After
	public void setToggle() {
		Toggle.NO_TAXONOMIES_CACHE_INVALIDATION.set(NO_TAXONOMIES_CACHE_INVALIDATION_VALUE);
		Toggle.TRY_USING_NEW_CACHE_BASED_TAXONOMIES_SEARCH_SERVICES_QUERY_HANDLER.set(TRY_USING_NEW_CACHE_BASED_TAXONOMIES_SEARCH_SERVICES_QUERY_HANDLER_VALUE);
	}

	@Before
	public void setUp()
			throws Exception {
		NO_TAXONOMIES_CACHE_INVALIDATION_VALUE = Toggle.NO_TAXONOMIES_CACHE_INVALIDATION.isEnabled();
		Toggle.NO_TAXONOMIES_CACHE_INVALIDATION.disable();

		TRY_USING_NEW_CACHE_BASED_TAXONOMIES_SEARCH_SERVICES_QUERY_HANDLER_VALUE = Toggle.TRY_USING_NEW_CACHE_BASED_TAXONOMIES_SEARCH_SERVICES_QUERY_HANDLER.isEnabled();
		Toggle.TRY_USING_NEW_CACHE_BASED_TAXONOMIES_SEARCH_SERVICES_QUERY_HANDLER.disable();

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withAllTest(users));

		ModelLayerFactory modelLayerFactoryOfOtherInstance = getModelLayerFactory("other-instance");
		TestUtils.linkEventBus(getModelLayerFactory(), modelLayerFactoryOfOtherInstance);

		//TODO Do not run this test with ignite cache
		cache = (EventBusTaxonomiesSearchServicesCache) getModelLayerFactory().getTaxonomiesSearchServicesCache();

		if (testCase.equals("testingInvalidationsOnLocalCache")) {
			recordServices = getModelLayerFactory().newRecordServices();
			authServices = getModelLayerFactory().newAuthorizationsServices();
		} else {
			recordServices = modelLayerFactoryOfOtherInstance.newRecordServices();
			authServices = modelLayerFactoryOfOtherInstance.newAuthorizationsServices();
		}

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
				"categoryId_Z robin visible-actives false",
				"categoryId_Z sasquatch selecting-document false",
				"categoryId_Z sasquatch selecting-folder false",
				"categoryId_Z sasquatch visible-actives false",
				"unitId_30 robin selecting-document false",
				"unitId_30 robin selecting-folder false",
				"unitId_30 robin visible-actives false",
				"unitId_30 sasquatch selecting-document false",
				"unitId_30 sasquatch selecting-folder false",
				"unitId_30 sasquatch visible-actives false"
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
				"categoryId_Z robin visible-actives false",
				"categoryId_Z sasquatch selecting-document false",
				"categoryId_Z sasquatch selecting-folder false",
				"categoryId_Z sasquatch visible-actives false",
				"unitId_30 robin selecting-document false",
				"unitId_30 robin selecting-folder false",
				"unitId_30 robin visible-actives false",
				"unitId_30 sasquatch selecting-document false",
				"unitId_30 sasquatch selecting-folder false",
				"unitId_30 sasquatch visible-actives false"
		);

		recordServices.add(rm.newFolderWithId("newFolder3")
				.setParentFolder("newFolder2")
				.setOpenDate(aDate())
				.setTitle("New folder 2"));

		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_Z robin selecting-document false",
				"categoryId_Z robin selecting-folder false",
				"categoryId_Z robin visible-actives false",
				"categoryId_Z sasquatch selecting-document false",
				"categoryId_Z sasquatch selecting-folder false",
				"categoryId_Z sasquatch visible-actives false",
				"unitId_30 robin selecting-document false",
				"unitId_30 robin selecting-folder false",
				"unitId_30 robin visible-actives false",
				"unitId_30 sasquatch selecting-document false",
				"unitId_30 sasquatch selecting-folder false",
				"unitId_30 sasquatch visible-actives false"
		);

	}

	@Test
	public void whenADocumentIsCreatedThenCacheIsInvalidatedForHasChildrenQueriesWithoutResultsInHierarchies()
			throws Exception {

		recordServices.add(rm.newDocumentWithId("newDocument").setTitle("New document").setFolder(FOLDER1));

		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-folder false",
				"categoryId_X robin visible-actives false",
				"unitId_10 robin selecting-folder false",
				"unitId_10 robin visible-actives false"
		);

	}

	@Test
	public void whenAFolderIsModifiedThenCacheIsInvalidatedForHasChildrenQueriesWithoutResultsInNewHierarchiesAndWithResultsInPreviousHierarchies()
			throws Exception {

		Folder folder1 = rm.getFolder(FOLDER1);

		recordServices.update(folder1.setCategoryEntered(records.categoryId_X120));

		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X110 sasquatch visible-actives true"
		);

		recordServices.update(folder1.setCategoryEntered(records.categoryId_Z120)
				.setAdministrativeUnitEntered(records.unitId_30c));

		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible-actives true",
				"categoryId_X100 sasquatch visible-actives true",
				"categoryId_Z robin selecting-document false",
				"categoryId_Z robin selecting-folder false",
				"categoryId_Z robin visible-actives false",
				"categoryId_Z sasquatch selecting-document false",
				"categoryId_Z sasquatch selecting-folder false",
				"categoryId_Z sasquatch visible-actives false",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible-actives true",
				"unitId_10a sasquatch visible-actives true",
				"unitId_30 robin selecting-document false",
				"unitId_30 robin selecting-folder false",
				"unitId_30 robin visible-actives false",
				"unitId_30 sasquatch selecting-document false",
				"unitId_30 sasquatch selecting-folder false",
				"unitId_30 sasquatch visible-actives false"
		);

	}

	@Test
	public void whenAFolderIsLogicallyDeletedThenCacheIsInvalidatedForHasChildrenQueriesWithResultsInHierarchies()
			throws Exception {

		recordServices.logicallyDelete(record(FOLDER1), User.GOD);
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible-actives true",
				"categoryId_X100 sasquatch visible-actives true",
				"categoryId_X110 sasquatch visible-actives true",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible-actives true",
				"unitId_10a sasquatch visible-actives true"
		);
	}

	@Test
	public void whenAFolderIsPhysicallyDeletedThenCacheIsInvalidatedForHasChildrenQueriesWithResultsInHierarchies()
			throws Exception {

		recordServices.physicallyDeleteNoMatterTheStatus(record(FOLDER1), User.GOD, new RecordPhysicalDeleteOptions());
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible-actives true",
				"categoryId_X100 sasquatch visible-actives true",
				"categoryId_X110 sasquatch visible-actives true",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible-actives true",
				"unitId_10a sasquatch visible-actives true"
		);
	}

	@Test
	public void whenAFolderHasANewArchivisticStatusThenCacheIsInvalidatedForHasChildrenQueriesWithResultsInHierarchies()
			throws Exception {

		recordServices.update(rm.getFolder(FOLDER1).setActualTransferDate(date(2016, 1, 1)));
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible-actives true",
				"categoryId_X100 sasquatch visible-actives true",
				"categoryId_X110 sasquatch visible-actives true",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible-actives true",
				"unitId_10a sasquatch visible-actives true"
		);

		recordServices.update(rm.getFolder(FOLDER1).setActualTransferDate(null));
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder false",
				"categoryId_X robin visible-actives false",
				"categoryId_X sasquatch selecting-document false",
				"categoryId_X sasquatch visible-actives false",
				"categoryId_X100 sasquatch visible-actives false",
				"unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder false",
				"unitId_10 robin visible-actives false",
				"unitId_10 sasquatch selecting-document false",
				"unitId_10 sasquatch visible-actives false",
				"unitId_10a sasquatch visible-actives false"
		);

	}

	@Test
	public void whenAnAdministrativeUnitReceiveNewUserAuthorizationsThenInvalidated()
			throws Exception {

		String auth = authServices
				.add(authorizationForUsers(users.robinIn(zeCollection)).givingReadAccess().on(records.unitId_10));
		cache.invalidateUser(robin);
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false", "categoryId_X robin selecting-folder false",
				"categoryId_X robin visible-actives false", "categoryId_Z robin selecting-document false",
				"categoryId_Z robin selecting-folder false", "categoryId_Z robin visible-actives false",
				"unitId_10 admin selecting-document false", "unitId_10 alice selecting-document false",
				"unitId_10 bob selecting-document false", "unitId_10 charles selecting-document false",
				"unitId_10 chuck selecting-document false", "unitId_10 dakota selecting-document false",
				"unitId_10 edouard selecting-document false", "unitId_10 edouard selecting-folder false",
				"unitId_10 edouard visible-actives false", "unitId_10 gandalf selecting-document false",
				"unitId_10 robin selecting-document false", "unitId_10 robin selecting-folder false",
				"unitId_10 robin visible-actives false", "unitId_10 sasquatch selecting-document false",
				"unitId_30 robin selecting-document false", "unitId_30 robin selecting-folder false",
				"unitId_30 robin visible-actives false"
		);

		authServices.execute(modifyAuthorizationOnRecord(auth, records.getUnit10())
				.withNewPrincipalIds(users.sasquatchIn(zeCollection).getId()));
		cache.invalidateUser(robin);
		cache.invalidateUser(sasquatch);
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false", "categoryId_X robin selecting-folder true",
				"categoryId_X robin visible-actives true", "categoryId_X sasquatch selecting-document false",
				"categoryId_X sasquatch selecting-folder true", "categoryId_X sasquatch visible-actives true",
				"categoryId_X100 robin visible-actives true", "categoryId_X100 sasquatch visible-actives true",
				"categoryId_X110 robin visible-actives true", "categoryId_X110 sasquatch visible-actives true",
				"categoryId_Z robin selecting-document false", "categoryId_Z robin selecting-folder false",
				"categoryId_Z robin visible-actives false", "categoryId_Z sasquatch selecting-document false",
				"categoryId_Z sasquatch selecting-folder false", "categoryId_Z sasquatch visible-actives false",
				"unitId_10 admin selecting-document false", "unitId_10 admin selecting-folder true",
				"unitId_10 admin visible-actives true", "unitId_10 alice selecting-document false",
				"unitId_10 alice selecting-folder true", "unitId_10 alice visible-actives true", "unitId_10 bob selecting-document false",
				"unitId_10 bob selecting-folder true", "unitId_10 bob visible-actives true", "unitId_10 charles selecting-document false",
				"unitId_10 charles selecting-folder true", "unitId_10 charles visible-actives true",
				"unitId_10 chuck selecting-document false", "unitId_10 chuck selecting-folder true",
				"unitId_10 chuck visible-actives true", "unitId_10 dakota selecting-document false",
				"unitId_10 dakota selecting-folder true", "unitId_10 dakota visible-actives true",
				"unitId_10 edouard selecting-document false", "unitId_10 edouard selecting-folder false",
				"unitId_10 edouard visible-actives false", "unitId_10 gandalf selecting-document false",
				"unitId_10 gandalf selecting-folder true", "unitId_10 gandalf visible-actives true",
				"unitId_10 robin selecting-document false", "unitId_10 robin selecting-folder true",
				"unitId_10 robin visible-actives true", "unitId_10 sasquatch selecting-document false",
				"unitId_10 sasquatch selecting-folder true", "unitId_10 sasquatch visible-actives true", "unitId_10a robin visible-actives true",
				"unitId_10a sasquatch visible-actives true", "unitId_30 robin selecting-document false",
				"unitId_30 robin selecting-folder false", "unitId_30 robin visible-actives false",
				"unitId_30 sasquatch selecting-document false", "unitId_30 sasquatch selecting-folder false",
				"unitId_30 sasquatch visible-actives false"
		);

		authServices.execute(modifyAuthorizationOnRecord(auth, record(FOLDER1))
				.withNewPrincipalIds(users.robinIn(zeCollection).getId()));
		cache.invalidateUser(robin);
		cache.invalidateUser(sasquatch);
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X admin selecting-document false", "categoryId_X admin selecting-folder true",
				"categoryId_X admin visible-actives true", "categoryId_X alice selecting-document false",
				"categoryId_X alice selecting-folder true", "categoryId_X alice visible-actives true",
				"categoryId_X bob selecting-document false", "categoryId_X bob selecting-folder true",
				"categoryId_X bob visible-actives true", "categoryId_X charles selecting-document false",
				"categoryId_X charles selecting-folder true", "categoryId_X charles visible-actives true",
				"categoryId_X chuck selecting-document false", "categoryId_X chuck selecting-folder true",
				"categoryId_X chuck visible-actives true", "categoryId_X dakota selecting-document false",
				"categoryId_X dakota selecting-folder true", "categoryId_X dakota visible-actives true",
				"categoryId_X edouard selecting-document false", "categoryId_X edouard selecting-folder false",
				"categoryId_X edouard visible-actives false", "categoryId_X gandalf selecting-document false",
				"categoryId_X gandalf selecting-folder true", "categoryId_X gandalf visible-actives true",
				"categoryId_X robin selecting-document false", "categoryId_X robin selecting-folder false",
				"categoryId_X robin visible-actives false", "categoryId_X sasquatch selecting-document false",
				"categoryId_X sasquatch selecting-folder true", "categoryId_X sasquatch visible-actives true",
				"categoryId_X100 admin visible-actives true", "categoryId_X100 alice visible-actives true", "categoryId_X100 bob visible-actives true",
				"categoryId_X100 charles visible-actives true", "categoryId_X100 chuck visible-actives true",
				"categoryId_X100 dakota visible-actives true", "categoryId_X100 gandalf visible-actives true",
				"categoryId_X100 sasquatch visible-actives true", "categoryId_X110 admin visible-actives true",
				"categoryId_X110 alice visible-actives true", "categoryId_X110 bob visible-actives true", "categoryId_X110 charles visible-actives true",
				"categoryId_X110 chuck visible-actives true", "categoryId_X110 dakota visible-actives true",
				"categoryId_X110 gandalf visible-actives true", "categoryId_X110 sasquatch visible-actives true",
				"categoryId_Z robin selecting-document false", "categoryId_Z robin selecting-folder false",
				"categoryId_Z robin visible-actives false", "categoryId_Z sasquatch selecting-document false",
				"categoryId_Z sasquatch selecting-folder false", "categoryId_Z sasquatch visible-actives false",
				"unitId_10 admin selecting-document false", "unitId_10 admin selecting-folder true",
				"unitId_10 admin visible-actives true", "unitId_10 alice selecting-document false",
				"unitId_10 alice selecting-folder true", "unitId_10 alice visible-actives true", "unitId_10 bob selecting-document false",
				"unitId_10 bob selecting-folder true", "unitId_10 bob visible-actives true", "unitId_10 charles selecting-document false",
				"unitId_10 charles selecting-folder true", "unitId_10 charles visible-actives true",
				"unitId_10 chuck selecting-document false", "unitId_10 chuck selecting-folder true",
				"unitId_10 chuck visible-actives true", "unitId_10 dakota selecting-document false",
				"unitId_10 dakota selecting-folder true", "unitId_10 dakota visible-actives true",
				"unitId_10 edouard selecting-document false", "unitId_10 edouard selecting-folder false",
				"unitId_10 edouard visible-actives false", "unitId_10 gandalf selecting-document false",
				"unitId_10 gandalf selecting-folder true", "unitId_10 gandalf visible-actives true",
				"unitId_10 robin selecting-document false", "unitId_10 robin selecting-folder false",
				"unitId_10 robin visible-actives false", "unitId_10 sasquatch selecting-document false",
				"unitId_10 sasquatch selecting-folder true", "unitId_10 sasquatch visible-actives true", "unitId_10a admin visible-actives true",
				"unitId_10a alice visible-actives true", "unitId_10a bob visible-actives true", "unitId_10a charles visible-actives true",
				"unitId_10a chuck visible-actives true", "unitId_10a dakota visible-actives true", "unitId_10a gandalf visible-actives true",
				"unitId_10a sasquatch visible-actives true", "unitId_30 robin selecting-document false",
				"unitId_30 robin selecting-folder false", "unitId_30 robin visible-actives false",
				"unitId_30 sasquatch selecting-document false", "unitId_30 sasquatch selecting-folder false",
				"unitId_30 sasquatch visible-actives false"
		);

		authServices.execute(authorizationDeleteRequest(auth, zeCollection));
		cache.invalidateUser(robin);
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false", "categoryId_X robin selecting-folder true",
				"categoryId_X robin visible-actives true", "categoryId_X100 robin visible-actives true", "categoryId_X110 robin visible-actives true",
				"categoryId_Z robin selecting-document false", "categoryId_Z robin selecting-folder false",
				"categoryId_Z robin visible-actives false", "unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder true", "unitId_10 robin visible-actives true", "unitId_10a robin visible-actives true",
				"unitId_30 robin selecting-document false", "unitId_30 robin selecting-folder false",
				"unitId_30 robin visible-actives false"
		);
	}

	@Test
	public void whenAnAdministrativeUnitReceiveNewGroupAuthorizationsThenInvalidated()
			throws Exception {

		String auth = authServices
				.add(authorizationForGroups(users.legendsIn(zeCollection)).givingReadAccess().on(records.unitId_10));
		cache.invalidateUser(alice);
		cache.invalidateUser(edouard);
		cache.invalidateUser(gandalf);
		cache.invalidateUser(sasquatch);
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X alice selecting-document false", "categoryId_X alice selecting-folder true",
				"categoryId_X alice visible-actives true", "categoryId_X edouard selecting-document false",
				"categoryId_X edouard selecting-folder false", "categoryId_X edouard visible-actives false",
				"categoryId_X gandalf selecting-document false", "categoryId_X gandalf selecting-folder true",
				"categoryId_X gandalf visible-actives true", "categoryId_X sasquatch selecting-document false",
				"categoryId_X sasquatch selecting-folder true", "categoryId_X sasquatch visible-actives true",
				"categoryId_X100 alice visible-actives true", "categoryId_X100 gandalf visible-actives true",
				"categoryId_X100 sasquatch visible-actives true", "categoryId_X110 alice visible-actives true",
				"categoryId_X110 gandalf visible-actives true", "categoryId_X110 sasquatch visible-actives true",
				"categoryId_Z alice selecting-document false", "categoryId_Z alice selecting-folder false",
				"categoryId_Z alice visible-actives false", "categoryId_Z edouard selecting-document false",
				"categoryId_Z edouard selecting-folder false", "categoryId_Z edouard visible-actives false",
				"categoryId_Z gandalf selecting-document false", "categoryId_Z gandalf selecting-folder false",
				"categoryId_Z gandalf visible-actives false", "categoryId_Z sasquatch selecting-document false",
				"categoryId_Z sasquatch selecting-folder false", "categoryId_Z sasquatch visible-actives false",
				"unitId_10 admin selecting-document false", "unitId_10 alice selecting-document false",
				"unitId_10 alice selecting-folder true", "unitId_10 alice visible-actives true", "unitId_10 bob selecting-document false",
				"unitId_10 charles selecting-document false", "unitId_10 chuck selecting-document false",
				"unitId_10 dakota selecting-document false", "unitId_10 edouard selecting-document false",
				"unitId_10 edouard selecting-folder false", "unitId_10 edouard visible-actives false",
				"unitId_10 gandalf selecting-document false", "unitId_10 gandalf selecting-folder true",
				"unitId_10 gandalf visible-actives true", "unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder false", "unitId_10 robin visible-actives false",
				"unitId_10 sasquatch selecting-document false", "unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible-actives true", "unitId_10a alice visible-actives true", "unitId_10a gandalf visible-actives true",
				"unitId_10a sasquatch visible-actives true", "unitId_30 alice selecting-document false",
				"unitId_30 alice selecting-folder false", "unitId_30 alice visible-actives false",
				"unitId_30 edouard selecting-document false", "unitId_30 edouard selecting-folder false",
				"unitId_30 edouard visible-actives false", "unitId_30 gandalf selecting-document false",
				"unitId_30 gandalf selecting-folder false", "unitId_30 gandalf visible-actives false",
				"unitId_30 sasquatch selecting-document false", "unitId_30 sasquatch selecting-folder false",
				"unitId_30 sasquatch visible-actives false"
		);

		authServices.execute(modifyAuthorizationOnRecord(auth, records.getUnit10())
				.withNewPrincipalIds(users.heroesIn(zeCollection).getId()));
		cache.invalidateUser(alice);
		cache.invalidateUser(edouard);
		cache.invalidateUser(sasquatch);
		cache.invalidateUser(gandalf);

		cache.invalidateUser(charles);
		cache.invalidateUser(dakota);
		cache.invalidateUser(robin);

		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X alice selecting-document false", "categoryId_X alice selecting-folder true",
				"categoryId_X alice visible-actives true", "categoryId_X charles selecting-document false",
				"categoryId_X charles selecting-folder true", "categoryId_X charles visible-actives true",
				"categoryId_X dakota selecting-document false", "categoryId_X dakota selecting-folder true",
				"categoryId_X dakota visible-actives true", "categoryId_X edouard selecting-document false",
				"categoryId_X edouard selecting-folder true", "categoryId_X edouard visible-actives true",
				"categoryId_X gandalf selecting-document false", "categoryId_X gandalf selecting-folder true",
				"categoryId_X gandalf visible-actives true", "categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder false", "categoryId_X robin visible-actives false",
				"categoryId_X sasquatch selecting-document false", "categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible-actives true", "categoryId_X100 alice visible-actives true",
				"categoryId_X100 charles visible-actives true", "categoryId_X100 dakota visible-actives true",
				"categoryId_X100 edouard visible-actives true", "categoryId_X100 gandalf visible-actives true",
				"categoryId_X100 sasquatch visible-actives true", "categoryId_X110 alice visible-actives true",
				"categoryId_X110 charles visible-actives true", "categoryId_X110 dakota visible-actives true",
				"categoryId_X110 edouard visible-actives true", "categoryId_X110 gandalf visible-actives true",
				"categoryId_X110 sasquatch visible-actives true", "categoryId_Z alice selecting-document false",
				"categoryId_Z alice selecting-folder false", "categoryId_Z alice visible-actives false",
				"categoryId_Z charles selecting-document false", "categoryId_Z charles selecting-folder false",
				"categoryId_Z charles visible-actives false", "categoryId_Z dakota selecting-document false",
				"categoryId_Z dakota selecting-folder false", "categoryId_Z dakota visible-actives false",
				"categoryId_Z edouard selecting-document false", "categoryId_Z edouard selecting-folder false",
				"categoryId_Z edouard visible-actives false", "categoryId_Z gandalf selecting-document false",
				"categoryId_Z gandalf selecting-folder false", "categoryId_Z gandalf visible-actives false",
				"categoryId_Z robin selecting-document false", "categoryId_Z robin selecting-folder false",
				"categoryId_Z robin visible-actives false", "categoryId_Z sasquatch selecting-document false",
				"categoryId_Z sasquatch selecting-folder false", "categoryId_Z sasquatch visible-actives false",
				"unitId_10 admin selecting-document false", "unitId_10 admin selecting-folder true",
				"unitId_10 admin visible-actives true", "unitId_10 alice selecting-document false",
				"unitId_10 alice selecting-folder true", "unitId_10 alice visible-actives true", "unitId_10 bob selecting-document false",
				"unitId_10 bob selecting-folder true", "unitId_10 bob visible-actives true", "unitId_10 charles selecting-document false",
				"unitId_10 charles selecting-folder true", "unitId_10 charles visible-actives true",
				"unitId_10 chuck selecting-document false", "unitId_10 chuck selecting-folder true",
				"unitId_10 chuck visible-actives true", "unitId_10 dakota selecting-document false",
				"unitId_10 dakota selecting-folder true", "unitId_10 dakota visible-actives true",
				"unitId_10 edouard selecting-document false", "unitId_10 edouard selecting-folder true",
				"unitId_10 edouard visible-actives true", "unitId_10 gandalf selecting-document false",
				"unitId_10 gandalf selecting-folder true", "unitId_10 gandalf visible-actives true",
				"unitId_10 robin selecting-document false", "unitId_10 robin selecting-folder false",
				"unitId_10 robin visible-actives false", "unitId_10 sasquatch selecting-document false",
				"unitId_10 sasquatch selecting-folder true", "unitId_10 sasquatch visible-actives true", "unitId_10a alice visible-actives true",
				"unitId_10a charles visible-actives true", "unitId_10a dakota visible-actives true", "unitId_10a edouard visible-actives true",
				"unitId_10a gandalf visible-actives true", "unitId_10a sasquatch visible-actives true",
				"unitId_30 alice selecting-document false", "unitId_30 alice selecting-folder false",
				"unitId_30 alice visible-actives false", "unitId_30 charles selecting-document false",
				"unitId_30 charles selecting-folder false", "unitId_30 charles visible-actives false",
				"unitId_30 dakota selecting-document false", "unitId_30 dakota selecting-folder false",
				"unitId_30 dakota visible-actives false", "unitId_30 edouard selecting-document false",
				"unitId_30 edouard selecting-folder false", "unitId_30 edouard visible-actives false",
				"unitId_30 gandalf selecting-document false", "unitId_30 gandalf selecting-folder false",
				"unitId_30 gandalf visible-actives false", "unitId_30 robin selecting-document false",
				"unitId_30 robin selecting-folder false", "unitId_30 robin visible-actives false",
				"unitId_30 sasquatch selecting-document false", "unitId_30 sasquatch selecting-folder false",
				"unitId_30 sasquatch visible-actives false"
		);

		authServices.execute(modifyAuthorizationOnRecord(auth, records.getUnit10())
				.withNewPrincipalIds(users.legendsIn(zeCollection).getId()));
		cache.invalidateUser(alice);
		cache.invalidateUser(edouard);
		cache.invalidateUser(sasquatch);
		cache.invalidateUser(gandalf);

		cache.invalidateUser(charles);
		cache.invalidateUser(dakota);
		cache.invalidateUser(robin);
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X alice selecting-document false", "categoryId_X alice selecting-folder true",
				"categoryId_X alice visible-actives true", "categoryId_X charles selecting-document false",
				"categoryId_X charles selecting-folder true", "categoryId_X charles visible-actives true",
				"categoryId_X dakota selecting-document false", "categoryId_X dakota selecting-folder true",
				"categoryId_X dakota visible-actives true", "categoryId_X edouard selecting-document false",
				"categoryId_X edouard selecting-folder false", "categoryId_X edouard visible-actives false",
				"categoryId_X gandalf selecting-document false", "categoryId_X gandalf selecting-folder true",
				"categoryId_X gandalf visible-actives true", "categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder true", "categoryId_X robin visible-actives true",
				"categoryId_X sasquatch selecting-document false", "categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible-actives true", "categoryId_X100 alice visible-actives true",
				"categoryId_X100 charles visible-actives true", "categoryId_X100 dakota visible-actives true",
				"categoryId_X100 gandalf visible-actives true", "categoryId_X100 robin visible-actives true",
				"categoryId_X100 sasquatch visible-actives true", "categoryId_X110 alice visible-actives true",
				"categoryId_X110 charles visible-actives true", "categoryId_X110 dakota visible-actives true",
				"categoryId_X110 gandalf visible-actives true", "categoryId_X110 robin visible-actives true",
				"categoryId_X110 sasquatch visible-actives true", "categoryId_Z alice selecting-document false",
				"categoryId_Z alice selecting-folder false", "categoryId_Z alice visible-actives false",
				"categoryId_Z charles selecting-document false", "categoryId_Z charles selecting-folder false",
				"categoryId_Z charles visible-actives false", "categoryId_Z dakota selecting-document false",
				"categoryId_Z dakota selecting-folder false", "categoryId_Z dakota visible-actives false",
				"categoryId_Z edouard selecting-document false", "categoryId_Z edouard selecting-folder false",
				"categoryId_Z edouard visible-actives false", "categoryId_Z gandalf selecting-document false",
				"categoryId_Z gandalf selecting-folder false", "categoryId_Z gandalf visible-actives false",
				"categoryId_Z robin selecting-document false", "categoryId_Z robin selecting-folder false",
				"categoryId_Z robin visible-actives false", "categoryId_Z sasquatch selecting-document false",
				"categoryId_Z sasquatch selecting-folder false", "categoryId_Z sasquatch visible-actives false",
				"unitId_10 admin selecting-document false", "unitId_10 admin selecting-folder true",
				"unitId_10 admin visible-actives true", "unitId_10 alice selecting-document false",
				"unitId_10 alice selecting-folder true", "unitId_10 alice visible-actives true", "unitId_10 bob selecting-document false",
				"unitId_10 bob selecting-folder true", "unitId_10 bob visible-actives true", "unitId_10 charles selecting-document false",
				"unitId_10 charles selecting-folder true", "unitId_10 charles visible-actives true",
				"unitId_10 chuck selecting-document false", "unitId_10 chuck selecting-folder true",
				"unitId_10 chuck visible-actives true", "unitId_10 dakota selecting-document false",
				"unitId_10 dakota selecting-folder true", "unitId_10 dakota visible-actives true",
				"unitId_10 edouard selecting-document false", "unitId_10 edouard selecting-folder false",
				"unitId_10 edouard visible-actives false", "unitId_10 gandalf selecting-document false",
				"unitId_10 gandalf selecting-folder true", "unitId_10 gandalf visible-actives true",
				"unitId_10 robin selecting-document false", "unitId_10 robin selecting-folder true",
				"unitId_10 robin visible-actives true", "unitId_10 sasquatch selecting-document false",
				"unitId_10 sasquatch selecting-folder true", "unitId_10 sasquatch visible-actives true", "unitId_10a alice visible-actives true",
				"unitId_10a charles visible-actives true", "unitId_10a dakota visible-actives true", "unitId_10a gandalf visible-actives true",
				"unitId_10a robin visible-actives true", "unitId_10a sasquatch visible-actives true", "unitId_30 alice selecting-document false",
				"unitId_30 alice selecting-folder false", "unitId_30 alice visible-actives false",
				"unitId_30 charles selecting-document false", "unitId_30 charles selecting-folder false",
				"unitId_30 charles visible-actives false", "unitId_30 dakota selecting-document false",
				"unitId_30 dakota selecting-folder false", "unitId_30 dakota visible-actives false",
				"unitId_30 edouard selecting-document false", "unitId_30 edouard selecting-folder false",
				"unitId_30 edouard visible-actives false", "unitId_30 gandalf selecting-document false",
				"unitId_30 gandalf selecting-folder false", "unitId_30 gandalf visible-actives false",
				"unitId_30 robin selecting-document false", "unitId_30 robin selecting-folder false",
				"unitId_30 robin visible-actives false", "unitId_30 sasquatch selecting-document false",
				"unitId_30 sasquatch selecting-folder false", "unitId_30 sasquatch visible-actives false"
		);

		authServices.execute(authorizationDeleteRequest(auth, zeCollection));
		cache.invalidateUser(alice);
		cache.invalidateUser(edouard);
		cache.invalidateUser(sasquatch);
		cache.invalidateUser(gandalf);
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X alice selecting-document false", "categoryId_X alice selecting-folder true",
				"categoryId_X alice visible-actives true", "categoryId_X edouard selecting-document false",
				"categoryId_X edouard selecting-folder true", "categoryId_X edouard visible-actives true",
				"categoryId_X gandalf selecting-document false", "categoryId_X gandalf selecting-folder true",
				"categoryId_X gandalf visible-actives true", "categoryId_X sasquatch selecting-document false",
				"categoryId_X sasquatch selecting-folder true", "categoryId_X sasquatch visible-actives true",
				"categoryId_X100 alice visible-actives true", "categoryId_X100 edouard visible-actives true",
				"categoryId_X100 gandalf visible-actives true", "categoryId_X100 sasquatch visible-actives true",
				"categoryId_X110 alice visible-actives true", "categoryId_X110 edouard visible-actives true",
				"categoryId_X110 gandalf visible-actives true", "categoryId_X110 sasquatch visible-actives true",
				"categoryId_Z alice selecting-document false", "categoryId_Z alice selecting-folder false",
				"categoryId_Z alice visible-actives false", "categoryId_Z edouard selecting-document false",
				"categoryId_Z edouard selecting-folder false", "categoryId_Z edouard visible-actives false",
				"categoryId_Z gandalf selecting-document false", "categoryId_Z gandalf selecting-folder false",
				"categoryId_Z gandalf visible-actives false", "categoryId_Z sasquatch selecting-document false",
				"categoryId_Z sasquatch selecting-folder false", "categoryId_Z sasquatch visible-actives false",
				"unitId_10 admin selecting-folder true", "unitId_10 admin visible-actives true",
				"unitId_10 alice selecting-document false", "unitId_10 alice selecting-folder true",
				"unitId_10 alice visible-actives true", "unitId_10 bob selecting-folder true", "unitId_10 bob visible-actives true",
				"unitId_10 charles selecting-folder true", "unitId_10 charles visible-actives true",
				"unitId_10 chuck selecting-folder true", "unitId_10 chuck visible-actives true", "unitId_10 dakota selecting-folder true",
				"unitId_10 dakota visible-actives true", "unitId_10 edouard selecting-document false",
				"unitId_10 edouard selecting-folder true", "unitId_10 edouard visible-actives true",
				"unitId_10 gandalf selecting-document false", "unitId_10 gandalf selecting-folder true",
				"unitId_10 gandalf visible-actives true", "unitId_10 sasquatch selecting-document false",
				"unitId_10 sasquatch selecting-folder true", "unitId_10 sasquatch visible-actives true", "unitId_10a alice visible-actives true",
				"unitId_10a edouard visible-actives true", "unitId_10a gandalf visible-actives true", "unitId_10a sasquatch visible-actives true",
				"unitId_30 alice selecting-document false", "unitId_30 alice selecting-folder false",
				"unitId_30 alice visible-actives false", "unitId_30 edouard selecting-document false",
				"unitId_30 edouard selecting-folder false", "unitId_30 edouard visible-actives false",
				"unitId_30 gandalf selecting-document false", "unitId_30 gandalf selecting-folder false",
				"unitId_30 gandalf visible-actives false", "unitId_30 sasquatch selecting-document false",
				"unitId_30 sasquatch selecting-folder false", "unitId_30 sasquatch visible-actives false"
		);
	}

	@Test
	public void whenAFolderReceiveNewAuthorizationsThenInvalidated()
			throws Exception {

		String auth = authServices.add(authorizationForUsers(users.robinIn(zeCollection)).givingReadAccess().on(FOLDER1));
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder false",
				"categoryId_X robin visible-actives false",
				"categoryId_X sasquatch selecting-document false",
				"unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder false",
				"unitId_10 robin visible-actives false",
				"unitId_10 sasquatch selecting-document false"
		);

		authServices.execute(modifyAuthorizationOnRecord(auth, record(FOLDER1))
				.withNewPrincipalIds(users.sasquatchIn(zeCollection).getId()));
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder true",
				"categoryId_X robin visible-actives true",
				"categoryId_X sasquatch selecting-document false",
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible-actives true",
				"categoryId_X100 robin visible-actives true",
				"categoryId_X100 sasquatch visible-actives true",
				"categoryId_X110 robin visible-actives true",
				"categoryId_X110 sasquatch visible-actives true",
				"unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder true",
				"unitId_10 robin visible-actives true",
				"unitId_10 sasquatch selecting-document false",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible-actives true",
				"unitId_10a robin visible-actives true",
				"unitId_10a sasquatch visible-actives true"
		);

		authServices.execute(modifyAuthorizationOnRecord(auth, record(FOLDER1))
				.withNewPrincipalIds(users.robinIn(zeCollection).getId()));
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder false",
				"categoryId_X robin visible-actives false",
				"categoryId_X sasquatch selecting-document false",
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible-actives true",
				"categoryId_X100 sasquatch visible-actives true",
				"categoryId_X110 sasquatch visible-actives true",
				"unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder false",
				"unitId_10 robin visible-actives false",
				"unitId_10 sasquatch selecting-document false",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible-actives true",
				"unitId_10a sasquatch visible-actives true"
		);

		authServices.execute(authorizationDeleteRequest(auth, zeCollection));
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-folder true",
				"categoryId_X robin visible-actives true",
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible-actives true",
				"categoryId_X100 robin visible-actives true",
				"categoryId_X100 sasquatch visible-actives true",
				"categoryId_X110 robin visible-actives true",
				"categoryId_X110 sasquatch visible-actives true",
				"unitId_10 robin selecting-folder true",
				"unitId_10 robin visible-actives true",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible-actives true",
				"unitId_10a robin visible-actives true",
				"unitId_10a sasquatch visible-actives true"
		);
	}

	@Test
	public void whenAUserReceiveOrLoseCollectionAccessThenInvalidated()
			throws Exception {

		UserServices userServices = getModelLayerFactory().newUserServices();
		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		recordServices.update(userServices.getUserInCollection(robin, zeCollection).setCollectionAllAccess(true));

		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder false",
				"categoryId_X robin visible-actives false",
				"categoryId_Z robin selecting-document false",
				"categoryId_Z robin selecting-folder false",
				"categoryId_Z robin visible-actives false",
				"unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder false",
				"unitId_10 robin visible-actives false",
				"unitId_30 robin selecting-document false",
				"unitId_30 robin selecting-folder false",
				"unitId_30 robin visible-actives false"
		);

		recordServices.update(userServices.getUserInCollection(robin, zeCollection).setAddress("2020 rue du Finfin"));
		assertThatInvalidatedEntriesSinceLastCheck().isEmpty();

		recordServices.update(userServices.getUserInCollection(robin, zeCollection).setCollectionAllAccess(false));

		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder true",
				"categoryId_X robin visible-actives true",
				"categoryId_X100 robin visible-actives true",
				"categoryId_X110 robin visible-actives true",
				"categoryId_Z robin selecting-document false",
				"categoryId_Z robin selecting-folder false",
				"categoryId_Z robin visible-actives false",
				"unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder true",
				"unitId_10 robin visible-actives true",
				"unitId_10a robin visible-actives true",
				"unitId_30 robin selecting-document false",
				"unitId_30 robin selecting-folder false",
				"unitId_30 robin visible-actives false"
		);

	}

	@Test
	public void whenAUserReceiveOrLoseGroupThenInvalidated()
			throws Exception {

		UserServices userServices = getModelLayerFactory().newUserServices();
		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		User robinUser = userServices.getUserInCollection(robin, zeCollection);

		recordServices.update(robinUser.addUserGroups(userServices.getGroupInCollection(legends, zeCollection).getId()));
		assertThatInvalidatedEntriesSinceLastCheck().containsOnly(
				"categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder false",
				"categoryId_X robin visible-actives false",
				"categoryId_Z robin selecting-document false",
				"categoryId_Z robin selecting-folder false",
				"categoryId_Z robin visible-actives false",
				"unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder false",
				"unitId_10 robin visible-actives false",
				"unitId_30 robin selecting-document false",
				"unitId_30 robin selecting-folder false",
				"unitId_30 robin visible-actives false"
		);

		recordServices.update(robinUser.setUserGroups(new ArrayList<String>()));
		assertThatInvalidatedEntriesSinceLastCheck().containsOnly(
				"categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder false",
				"categoryId_X robin visible-actives false",
				"categoryId_Z robin selecting-document false",
				"categoryId_Z robin selecting-folder false",
				"categoryId_Z robin visible-actives false",
				"unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder false",
				"unitId_10 robin visible-actives false",
				"unitId_30 robin selecting-document false",
				"unitId_30 robin selecting-folder false",
				"unitId_30 robin visible-actives false"
		);

	}

	@Test
	public void whenAGroupReceiveANewParentThenAllInvalidated()
			throws Exception {

		UserServices userServices = getModelLayerFactory().newUserServices();
		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Group legendsGroup = userServices.getGroupInCollection(legends, zeCollection);
		Group heroesGroup = userServices.getGroupInCollection(heroes, zeCollection);

		recordServices.update(legendsGroup.setParent(heroesGroup.getId()));
		assertThatCacheIsEntirelyInvalidated();

		recordServices.update(legendsGroup.setParent(null));
		assertThatCacheIsEntirelyInvalidated();

	}

	@Test
	public void givenDetachedFolderReceiveNewAuthorizationsThenInvalidated()
			throws Exception {

		authServices.detach(record(FOLDER1));

		String auth = authServices.add(authorizationForUsers(users.robinIn(zeCollection)).givingReadAccess().on(FOLDER1));
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder false",
				"categoryId_X robin visible-actives false",
				"categoryId_X sasquatch selecting-document false",
				"unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder false",
				"unitId_10 robin visible-actives false",
				"unitId_10 sasquatch selecting-document false"
		);

		authServices.execute(modifyAuthorizationOnRecord(auth, record(FOLDER1))
				.withNewPrincipalIds(users.sasquatchIn(zeCollection).getId()));
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder true",
				"categoryId_X robin visible-actives true",
				"categoryId_X sasquatch selecting-document false",
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible-actives true",
				"categoryId_X100 robin visible-actives true",
				"categoryId_X100 sasquatch visible-actives true",
				"categoryId_X110 robin visible-actives true",
				"categoryId_X110 sasquatch visible-actives true",
				"unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder true",
				"unitId_10 robin visible-actives true",
				"unitId_10 sasquatch selecting-document false",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible-actives true",
				"unitId_10a robin visible-actives true",
				"unitId_10a sasquatch visible-actives true"
		);

		authServices.execute(modifyAuthorizationOnRecord(auth, record(FOLDER1))
				.withNewPrincipalIds(users.robinIn(zeCollection).getId()));
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder false",
				"categoryId_X robin visible-actives false",
				"categoryId_X sasquatch selecting-document false",
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible-actives true",
				"categoryId_X100 sasquatch visible-actives true",
				"categoryId_X110 sasquatch visible-actives true",
				"unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder false",
				"unitId_10 robin visible-actives false",
				"unitId_10 sasquatch selecting-document false",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible-actives true",
				"unitId_10a sasquatch visible-actives true"
		);

		authServices.execute(authorizationDeleteRequest(auth, zeCollection));
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-folder true",
				"categoryId_X robin visible-actives true",
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible-actives true",
				"categoryId_X100 robin visible-actives true",
				"categoryId_X100 sasquatch visible-actives true",
				"categoryId_X110 robin visible-actives true",
				"categoryId_X110 sasquatch visible-actives true",
				"unitId_10 robin selecting-folder true",
				"unitId_10 robin visible-actives true",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible-actives true",
				"unitId_10a robin visible-actives true",
				"unitId_10a sasquatch visible-actives true"
		);

		authServices.reset(record(FOLDER1));
		assertThatInvalidatedEntriesSinceLastCheck().contains(
				"categoryId_X robin selecting-document false",
				"categoryId_X robin selecting-folder false",
				"categoryId_X robin visible-actives false",
				"categoryId_X sasquatch selecting-document false",
				"categoryId_X sasquatch selecting-folder true",
				"categoryId_X sasquatch visible-actives true",
				"categoryId_X100 sasquatch visible-actives true",
				"categoryId_X110 sasquatch visible-actives true",
				"unitId_10 robin selecting-document false",
				"unitId_10 robin selecting-folder false",
				"unitId_10 robin visible-actives false",
				"unitId_10 sasquatch selecting-document false",
				"unitId_10 sasquatch selecting-folder true",
				"unitId_10 sasquatch visible-actives true",
				"unitId_10a sasquatch visible-actives true"
		);
	}

	//--------------------------------------------------------------

	private void loadCacheForIds() {
	}

	private List<String> previousEntries = new ArrayList<>();

	private void assertThatCacheIsEntirelyInvalidated() {
		List<String> entriesNow = getCacheEntries();
		previousEntries = getCacheEntries();
		assertThat(entriesNow).isEmpty();
	}

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
			Map<String, Map<String, Boolean>> recordCache = ((MemoryTaxonomiesSearchServicesCache) cache.nestedCache)
					.getMemoryCache(id);
			for (Map.Entry<String, Map<String, Boolean>> entry : recordCache.entrySet()) {
				String user = entry.getKey();
				Map<String, Boolean> modes = entry.getValue();
				for (Map.Entry<String, Boolean> entry2 : modes.entrySet()) {
					entriesInfo.add(id + " " + user + " " + entry2.getKey() + " " + entry2.getValue());
				}
			}
		}
		Collections.sort(entriesInfo);

		return entriesInfo;
	}

	private void loadCache() {
		TaxonomiesManager taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		for (User user : getModelLayerFactory().newUserServices().getAllUsersInCollection(zeCollection)) {
			for (Taxonomy taxonomy : taxonomiesManager.getEnabledTaxonomies(zeCollection)) {

				TaxonomiesSearchOptions options = new TaxonomiesSearchOptions().setRows(100);
				for (TaxonomySearchRecord record : getModelLayerFactory().newTaxonomiesSearchService()
						.getVisibleRootConcept(user, zeCollection, taxonomy.getCode(), options)) {
					navigateVisible(user, taxonomy.getCode(), record.getRecord(), options);
				}

				for (TaxonomySearchRecord record : getModelLayerFactory().newTaxonomiesSearchService()
						.getLinkableRootConcept(user, zeCollection, taxonomy.getCode(), Folder.SCHEMA_TYPE, options)) {

					navigateLinkableSelectingAFolder(user, taxonomy.getCode(), record.getRecord(), options);
				}

				for (TaxonomySearchRecord record : getModelLayerFactory().newTaxonomiesSearchService()
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

	private void navigateLinkableSelectingAFolder(User user, String taxonomy, Record record,
												  TaxonomiesSearchOptions options) {
		TaxonomiesSearchServices taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();

		for (TaxonomySearchRecord child : taxonomiesSearchServices
				.getVisibleChildConcept(user, taxonomy, record, options)) {
			navigateLinkableSelectingAFolder(user, taxonomy, child.getRecord(), options);
		}

	}

	private void navigateLinkableSelectingADocument(User user, String taxonomy, Record record,
													TaxonomiesSearchOptions options) {
		TaxonomiesSearchServices taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();

		for (TaxonomySearchRecord child : taxonomiesSearchServices
				.getVisibleChildConcept(user, taxonomy, record, options)) {
			navigateLinkableSelectingADocument(user, taxonomy, child.getRecord(), options);
		}

	}

}
