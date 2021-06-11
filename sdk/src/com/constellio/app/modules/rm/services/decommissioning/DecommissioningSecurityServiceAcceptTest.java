package com.constellio.app.modules.rm.services.decommissioning;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningMainPresenter;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static com.constellio.sdk.tests.TestUtils.comparingRecordWrapperIds;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class DecommissioningSecurityServiceAcceptTest extends ConstellioTest {
	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);

	RecordServices recordServices;
	RMSchemasRecordsServices rm;
	DecommissioningSecurityService service;
	DecommissioningListQueryFactory queryFactory;

	User sasquatch, robin, bob;
	DecommissioningList alistIn10A, anotherListIn10A, aListIn20D, anotherListIn20D, aProcessedListIn10A, anotherProcessedListIn10A, listIn10ASentForValidation, anotherListIn10ASentForValidation;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus());

		recordServices = getModelLayerFactory().newRecordServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		service = new DecommissioningSecurityService(zeCollection, getAppLayerFactory());
		queryFactory = new DecommissioningListQueryFactory(zeCollection, getAppLayerFactory());

		refresh();
	}

	@Test
	public void givenUsersHasNoDecommissioningPermissionsAndNoListsToValidateThenHasNoAccessToPages()
			throws Exception {
		assertThat(service.hasAccessToDecommissioningMainPage(sasquatch)).isFalse();
		assertThat(service.hasAccessToDecommissioningListPage(alistIn10A, sasquatch)).isFalse();
	}

	@Test
	public void givenUsersHasNoDecommissioningPermissionsAndNoListsToValidateButIsSuperUserThenHasAccessToPages()
			throws Exception {
		alistIn10A.setSuperUser(users.sasquatchIn(zeCollection));
		recordServices.update(alistIn10A);

		assertThat(service.hasAccessToDecommissioningMainPage(sasquatch)).isTrue();
		assertThat(service.hasAccessToDecommissioningListPage(alistIn10A, sasquatch)).isTrue();
	}

	@Test
	public void givenUsersHasGlobalDecommissioningPermissionsThenCanDoAnything()
			throws Exception {
		recordServices.update(sasquatch.setUserRoles(asList(RMRoles.RGD)));
		refresh();

		assertThat(service.canCreateLists(sasquatch)).isTrue();
		assertThat(service.canCreateLists(robin)).isFalse();
		assertThat(service.hasAccessToDecommissioningMainPage(sasquatch)).isTrue();
		assertThat(service.hasAccessToDecommissioningListPage(alistIn10A, sasquatch)).isTrue();
		assertThat(service.hasAccessToDecommissioningListPage(aListIn20D, sasquatch)).isTrue();
		assertThat(service.getVisibleTabsInDecommissioningMainPage(sasquatch)).containsOnly(
				DecommissioningMainPresenter.CREATE, DecommissioningMainPresenter.GENERATED,
				DecommissioningMainPresenter.PENDING_VALIDATION, DecommissioningMainPresenter.TO_VALIDATE,
				DecommissioningMainPresenter.VALIDATED, DecommissioningMainPresenter.PENDING_APPROVAL,
				DecommissioningMainPresenter.TO_APPROVE, DecommissioningMainPresenter.APPROVED,
				DecommissioningMainPresenter.PROCESSED);

		assertThat(service.canAskApproval(alistIn10A, sasquatch)).isTrue();
		assertThat(service.canAskValidation(alistIn10A, sasquatch)).isTrue();
		assertThat(service.canDelete(alistIn10A, sasquatch)).isTrue();
		assertThat(service.canModify(alistIn10A, sasquatch)).isTrue();
		assertThat(service.canModifyFoldersAndContainers(alistIn10A, sasquatch)).isTrue();
		assertThat(service.canProcess(alistIn10A, sasquatch)).isTrue();
		assertThat(service.canValidate(alistIn10A, sasquatch)).isFalse();

		assertThat(service.canAskApproval(aListIn20D, sasquatch)).isTrue();
		assertThat(service.canAskValidation(aListIn20D, sasquatch)).isTrue();
		assertThat(service.canDelete(aListIn20D, sasquatch)).isTrue();
		assertThat(service.canModify(aListIn20D, sasquatch)).isTrue();
		assertThat(service.canModifyFoldersAndContainers(aListIn20D, sasquatch)).isTrue();
		assertThat(service.canProcess(aListIn20D, sasquatch)).isTrue();
		assertThat(service.canValidate(aListIn20D, sasquatch)).isFalse();

		assertThatResultsOf(queryFactory.getGeneratedListsQuery(sasquatch))
				.contains(alistIn10A, anotherListIn10A, aListIn20D, anotherListIn20D);

		assertThatResultsOf(queryFactory.getProcessedListsQuery(sasquatch))
				.contains(aProcessedListIn10A, anotherProcessedListIn10A)
				.doesNotContain(alistIn10A, anotherListIn10A, aListIn20D, anotherListIn20D);

		assertThatResultsOf(queryFactory.getListsToValidateQuery(sasquatch)).isEmpty();

		assertThatResultsOf(queryFactory.getListsPendingValidationQuery(sasquatch))
				.extracting("id").containsOnly(records.list_25);
	}

	@Test
	public void givenUsersHasDecommissioningPermissionsInASpecificAdministrativeUnitThenHasAccessToItsLists()
			throws Exception {
		save(authorization().forUsers(sasquatch).on(records.unitId_10).giving(RMRoles.RGD));
		save(authorization().forUsers(robin).on(records.unitId_20).giving(RMRoles.RGD));
		refresh();

		assertThat(service.canCreateLists(sasquatch)).isTrue();
		assertThat(service.canCreateLists(robin)).isTrue();
		assertThat(service.hasAccessToDecommissioningMainPage(sasquatch)).isTrue();
		assertThat(service.hasAccessToDecommissioningListPage(alistIn10A, sasquatch)).isTrue();
		assertThat(service.hasAccessToDecommissioningListPage(aListIn20D, sasquatch)).isFalse();
		assertThat(service.getVisibleTabsInDecommissioningMainPage(sasquatch)).containsOnly(
				DecommissioningMainPresenter.CREATE, DecommissioningMainPresenter.GENERATED,
				DecommissioningMainPresenter.PENDING_VALIDATION, DecommissioningMainPresenter.TO_VALIDATE,
				DecommissioningMainPresenter.VALIDATED, DecommissioningMainPresenter.PENDING_APPROVAL,
				DecommissioningMainPresenter.TO_APPROVE, DecommissioningMainPresenter.APPROVED,
				DecommissioningMainPresenter.PROCESSED);

		assertThat(service.canAskApproval(alistIn10A, sasquatch)).isTrue();
		assertThat(service.canAskValidation(alistIn10A, sasquatch)).isTrue();
		assertThat(service.canDelete(alistIn10A, sasquatch)).isTrue();
		assertThat(service.canModify(alistIn10A, sasquatch)).isTrue();
		assertThat(service.canModifyFoldersAndContainers(alistIn10A, sasquatch)).isTrue();
		assertThat(service.canProcess(alistIn10A, sasquatch)).isTrue();
		assertThat(service.canValidate(alistIn10A, sasquatch)).isFalse();

		assertThat(service.canAskApproval(aListIn20D, sasquatch)).isFalse();
		assertThat(service.canAskValidation(aListIn20D, sasquatch)).isFalse();
		assertThat(service.canDelete(aListIn20D, sasquatch)).isFalse();
		assertThat(service.canModify(aListIn20D, sasquatch)).isFalse();
		assertThat(service.canModifyFoldersAndContainers(aListIn20D, sasquatch)).isFalse();
		assertThat(service.canProcess(aListIn20D, sasquatch)).isFalse();
		assertThat(service.canValidate(aListIn20D, sasquatch)).isFalse();

		assertThatResultsOf(queryFactory.getGeneratedListsQuery(sasquatch))
				.contains(alistIn10A, anotherListIn10A)
				.doesNotContain(aListIn20D, anotherListIn20D);

		assertThatResultsOf(queryFactory.getGeneratedListsQuery(robin))

				.contains(aListIn20D, anotherListIn20D)
				.doesNotContain(alistIn10A, anotherListIn10A);

		assertThatResultsOf(queryFactory.getProcessedListsQuery(sasquatch))
				.contains(aProcessedListIn10A, anotherProcessedListIn10A)
				.doesNotContain(alistIn10A, anotherListIn10A, aListIn20D, anotherListIn20D);

		assertThatResultsOf(queryFactory.getProcessedListsQuery(robin)).isEmpty();

		assertThatResultsOf(queryFactory.getListsToValidateQuery(sasquatch)).isEmpty();

		assertThatResultsOf(queryFactory.getListsPendingValidationQuery(sasquatch))
				.extracting("id").containsOnly(records.list_25);

		assertThatResultsOf(queryFactory.getListsPendingValidationQuery(robin)).isEmpty();
	}

	@Test
	public void givenUsersHasDecommissioningPermissionsInASpecificAdministrativeUnitThenHasAccessToItsListsAndSuperUserList()
			throws Exception {
		save(authorization().forUsers(sasquatch).on(records.unitId_10).giving(RMRoles.RGD));
		save(authorization().forUsers(robin).on(records.unitId_20).giving(RMRoles.RGD));
		refresh();

		anotherListIn20D.setSuperUser(users.sasquatchIn(zeCollection));
		recordServices.update(anotherListIn20D);

		assertThat(service.canCreateLists(sasquatch)).isTrue();
		assertThat(service.canCreateLists(robin)).isTrue();
		assertThat(service.hasAccessToDecommissioningMainPage(sasquatch)).isTrue();
		assertThat(service.hasAccessToDecommissioningListPage(alistIn10A, sasquatch)).isTrue();
		assertThat(service.hasAccessToDecommissioningListPage(aListIn20D, sasquatch)).isFalse();
		assertThat(service.getVisibleTabsInDecommissioningMainPage(sasquatch)).containsOnly(
				DecommissioningMainPresenter.CREATE, DecommissioningMainPresenter.GENERATED,
				DecommissioningMainPresenter.PENDING_VALIDATION, DecommissioningMainPresenter.TO_VALIDATE,
				DecommissioningMainPresenter.VALIDATED, DecommissioningMainPresenter.PENDING_APPROVAL,
				DecommissioningMainPresenter.TO_APPROVE, DecommissioningMainPresenter.APPROVED,
				DecommissioningMainPresenter.PROCESSED);

		assertThat(service.canAskApproval(alistIn10A, sasquatch)).isTrue();
		assertThat(service.canAskValidation(alistIn10A, sasquatch)).isTrue();
		assertThat(service.canDelete(alistIn10A, sasquatch)).isTrue();
		assertThat(service.canModify(alistIn10A, sasquatch)).isTrue();
		assertThat(service.canModifyFoldersAndContainers(alistIn10A, sasquatch)).isTrue();
		assertThat(service.canProcess(alistIn10A, sasquatch)).isTrue();
		assertThat(service.canValidate(alistIn10A, sasquatch)).isFalse();

		assertThat(service.canAskApproval(aListIn20D, sasquatch)).isFalse();
		assertThat(service.canAskValidation(aListIn20D, sasquatch)).isFalse();
		assertThat(service.canDelete(aListIn20D, sasquatch)).isFalse();
		assertThat(service.canModify(aListIn20D, sasquatch)).isFalse();
		assertThat(service.canModifyFoldersAndContainers(aListIn20D, sasquatch)).isFalse();
		assertThat(service.canProcess(aListIn20D, sasquatch)).isFalse();
		assertThat(service.canValidate(aListIn20D, sasquatch)).isFalse();

		assertThatResultsOf(queryFactory.getGeneratedListsQuery(sasquatch))
				.contains(alistIn10A, anotherListIn10A, anotherListIn20D)
				.doesNotContain(aListIn20D);

		assertThatResultsOf(queryFactory.getGeneratedListsQuery(robin))
				.contains(aListIn20D, anotherListIn20D)
				.doesNotContain(alistIn10A, anotherListIn10A);

		assertThatResultsOf(queryFactory.getProcessedListsQuery(sasquatch))
				.contains(aProcessedListIn10A, anotherProcessedListIn10A)
				.doesNotContain(alistIn10A, anotherListIn10A, aListIn20D, anotherListIn20D);

		assertThatResultsOf(queryFactory.getProcessedListsQuery(robin)).isEmpty();

		assertThatResultsOf(queryFactory.getListsToValidateQuery(sasquatch)).isEmpty();

		assertThatResultsOf(queryFactory.getListsPendingValidationQuery(sasquatch))
				.extracting("id").containsOnly(records.list_25);

		assertThatResultsOf(queryFactory.getListsPendingValidationQuery(robin)).isEmpty();
	}

	@Test
	public void testUsingDefaulSetup()
			throws Exception {

		assertThat(service.hasAccessToDecommissioningListPage(records.getList25(), bob)).isTrue();
	}

	@Test
	public void givenUsersHasNoDecommissioningPermissionsButIsRequestedToValidateAListThenOnlyHasAccessToThatList()
			throws Exception {

		recordServices.update(alistIn10A.addValidationRequest(sasquatch.getId(), TimeProvider.getLocalDate()));

		assertThat(service.canCreateLists(sasquatch)).isFalse();
		assertThat(service.canCreateLists(robin)).isFalse();
		assertThat(service.hasAccessToDecommissioningMainPage(sasquatch)).isTrue();
		assertThat(service.hasAccessToDecommissioningListPage(alistIn10A, sasquatch)).isTrue();
		assertThat(service.hasAccessToDecommissioningListPage(aListIn20D, sasquatch)).isFalse();
		assertThat(service.getVisibleTabsInDecommissioningMainPage(sasquatch)).containsOnly(
				DecommissioningMainPresenter.TO_VALIDATE);

		assertThat(service.canAskApproval(alistIn10A, sasquatch)).isFalse();
		assertThat(service.canAskValidation(alistIn10A, sasquatch)).isFalse();
		assertThat(service.canDelete(alistIn10A, sasquatch)).isFalse();
		assertThat(service.canModify(alistIn10A, sasquatch)).isFalse();
		assertThat(service.canModifyFoldersAndContainers(alistIn10A, sasquatch)).isFalse();
		assertThat(service.canProcess(alistIn10A, sasquatch)).isFalse();
		assertThat(service.canValidate(alistIn10A, sasquatch)).isTrue();

		assertThat(service.canAskApproval(anotherListIn10A, sasquatch)).isFalse();
		assertThat(service.canAskValidation(anotherListIn10A, sasquatch)).isFalse();
		assertThat(service.canDelete(anotherListIn10A, sasquatch)).isFalse();
		assertThat(service.canModify(anotherListIn10A, sasquatch)).isFalse();
		assertThat(service.canModifyFoldersAndContainers(anotherListIn10A, sasquatch)).isFalse();
		assertThat(service.canProcess(anotherListIn10A, sasquatch)).isFalse();
		assertThat(service.canValidate(anotherListIn10A, sasquatch)).isFalse();

		assertThat(service.canAskApproval(aListIn20D, sasquatch)).isFalse();
		assertThat(service.canAskValidation(aListIn20D, sasquatch)).isFalse();
		assertThat(service.canDelete(aListIn20D, sasquatch)).isFalse();
		assertThat(service.canModify(aListIn20D, sasquatch)).isFalse();
		assertThat(service.canModifyFoldersAndContainers(aListIn20D, sasquatch)).isFalse();
		assertThat(service.canProcess(aListIn20D, sasquatch)).isFalse();
		assertThat(service.canValidate(aListIn20D, sasquatch)).isFalse();

		assertThatResultsOf(queryFactory.getGeneratedListsQuery(sasquatch))
				.isEmpty();

		assertThatResultsOf(queryFactory.getGeneratedListsQuery(robin))
				.isEmpty();

		assertThatResultsOf(queryFactory.getProcessedListsQuery(sasquatch))
				.isEmpty();

		assertThatResultsOf(queryFactory.getProcessedListsQuery(robin))
				.isEmpty();

		assertThatResultsOf(queryFactory.getListsToValidateQuery(sasquatch))
				.containsOnly(alistIn10A);

		assertThatResultsOf(queryFactory.getListsToValidateQuery(robin))
				.isEmpty();

		assertThatResultsOf(queryFactory.getListsPendingValidationQuery(sasquatch))
				.isEmpty();

		assertThatResultsOf(queryFactory.getListsPendingValidationQuery(robin))
				.isEmpty();
	}

	private void save(AuthorizationAddRequest authorization) {
		getModelLayerFactory().newAuthorizationsServices().add(authorization, User.GOD);
		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		refresh();
	}

	private void refresh() {
		bob = users.bobIn(zeCollection);
		sasquatch = users.sasquatchIn(zeCollection);
		robin = users.robinIn(zeCollection);
		alistIn10A = records.getList02();
		anotherListIn10A = records.getList03();
		aListIn20D = records.getList19();
		anotherListIn20D = records.getList18();
		aProcessedListIn10A = records.getList11();
		anotherProcessedListIn10A = records.getList12();
		listIn10ASentForValidation = records.getList24();
		anotherListIn10ASentForValidation = records.getList25();
	}

	private org.assertj.core.api.ListAssert<DecommissioningList> assertThatResultsOf(LogicalSearchQuery query) {
		List<Record> records = getModelLayerFactory().newSearchServices().search(query);
		return assertThat(rm.wrapDecommissioningLists(records)).usingElementComparator(comparingRecordWrapperIds);
	}

	private AuthorizationAddRequest authorization() {
		return authorizationInCollection(zeCollection);
	}
}
