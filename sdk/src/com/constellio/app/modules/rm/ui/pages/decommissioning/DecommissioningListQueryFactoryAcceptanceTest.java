package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningListQueryFactory;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DecommissioningListQueryFactoryAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);

	private DecommissioningListQueryFactory queryFactory;

	private RecordServices recordServices;
	private SearchServices searchServices;

	@Before
	public void setUp()
			throws Exception {

		//		configure(new AppLayerConfigurationAlteration() {
		//			@Override
		//			public void alter(InMemoryAppLayerConfiguration configuration) {
		//				configuration.setFastMigrationsEnabled(false);
		//			}
		//		});

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();

		queryFactory = new DecommissioningListQueryFactory(zeCollection, getAppLayerFactory());
	}

	@Test
	public void givenUserHasNoPermissionWhenSearchingGeneratedListsThenReturnNone() {
		LogicalSearchQuery query = queryFactory.getGeneratedListsQuery(records.getAlice());
		assertThat(searchServices.hasResults(query)).isFalse();
	}

	@Test
	public void givenUserHasApprovalPermissionWhenSearchingGeneratedListsThenReturnNoneWithProcessPermission() {
		User gandalf_managerInABC = records.getGandalf_managerInABC();
		LogicalSearchQuery query = queryFactory.getGeneratedListsQuery(gandalf_managerInABC);
		List<Record> decommissioningListsProcessable = searchServices.search(query);
		assertThat(decommissioningListsProcessable).extracting("id").containsOnly(
				records.list_01, records.list_02, records.list_03, records.list_04, records.list_05, records.list_06,
				records.list_07, records.list_08, records.list_09, records.list_10, records.list_16, records.list_17,
				records.list_18, records.list_19, records.list_20, records.list_21);
		for (Record decommissioningList : decommissioningListsProcessable) {
			assertThat(gandalf_managerInABC.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).on(decommissioningList)).isFalse();
			assertThat(gandalf_managerInABC.has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).on(decommissioningList)).isTrue();
		}
	}

	@Test
	public void givenUserHasProcessPermissionWhenSearchingGeneratedListsThenReturnGeneratedLists() {
		LogicalSearchQuery query = queryFactory.getGeneratedListsQuery(records.getChuckNorris());
		assertThat(searchServices.search(query)).extracting("id").containsOnly(
				records.list_01, records.list_02, records.list_03, records.list_04, records.list_05, records.list_06,
				records.list_07, records.list_08, records.list_09, records.list_10, records.list_16, records.list_17,
				records.list_18, records.list_19, records.list_20, records.list_21);
	}

	@Test
	public void givenUserHasNoPermissionWhenSearchingListsPendingValidationThenReturnNone()
			throws RecordServicesException {
		LogicalSearchQuery query = queryFactory.getListsPendingValidationQuery(records.getAlice());
		assertThat(searchServices.hasResults(query)).isFalse();
	}

	@Test
	public void givenUserHasApprovalPermissionWhenSearchingListsPendingValidationThenReturnListsPendingApprovalAndValidation()
			throws RecordServicesException {
		LogicalSearchQuery query = queryFactory.getListsPendingValidationQuery(records.getGandalf_managerInABC());
		assertThat(searchServices.search(query)).extracting("id").containsOnly(records.list_25);
	}

	// TODO: validator = user when user is manager

	@Test
	public void givenUserHasProcessPermissionWhenSearchingListsPendingValidationThenReturnListsPendingValidationNotApproval()
			throws RecordServicesException {
		DecommissioningList list = addListWithValidationRequestToBobAndChuck();
		User admin = records.getAdmin();
		LogicalSearchQuery query = queryFactory.getListsPendingValidationQuery(admin);
		List<Record> decommissioningListsProcessable = searchServices.search(query);
		assertThat(decommissioningListsProcessable).extracting("id").containsOnly(list.getId(), records.list_25);
		for (Record decommissioningList : decommissioningListsProcessable) {
			assertThat(admin.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).on(decommissioningList)).isTrue();
			assertThat(admin.has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).on(decommissioningList)).isTrue();
		}

	}

	@Test
	public void givenUserHasProcessPermissionWhenSearchingListsPendingValidationThenReturnListsTheUserMustValidate()
			throws RecordServicesException {
		addListWithValidationRequestToBobAndChuck();
		LogicalSearchQuery query = queryFactory.getListsPendingValidationQuery(records.getChuckNorris());
		assertThat(searchServices.search(query)).extracting("id").containsOnly(records.list_25);
	}

	@Test
	public void givenNoValidationRequestWhenSearchingListsToValidateThenReturn()
			throws RecordServicesException {
		LogicalSearchQuery query = queryFactory.getListsPendingValidationQuery(records.getChuckNorris());
		assertThat(searchServices.search(query)).extracting("id").containsOnly(records.list_25);
	}

	@Test
	public void givenValidationRequestWhenSearchingListsToValidateThenReturnList()
			throws RecordServicesException {
		DecommissioningList newList = addListWithValidationRequestToBobAndChuck();
		LogicalSearchQuery query = queryFactory.getListsToValidateQuery(records.getChuckNorris());
		assertThat(searchServices.search(query)).extracting("id").containsOnly(newList.getId());
	}

	@Test
	public void givenValidationRequestAlreadyValidatedBySameUserWhenSearchingListsToValidateThenReturnNone()
			throws RecordServicesException {
		addUserValidationToList(addListWithValidationRequestToBobAndChuck(), records.getChuckNorris());
		LogicalSearchQuery query = queryFactory.getListsToValidateQuery(records.getChuckNorris());
		assertThat(searchServices.hasResults(query)).isFalse();
	}

	@Test
	public void givenValidationRequestAlreadyValidatedByAnotherUserWhenSearchingListsToValidateThenReturnList()
			throws RecordServicesException {
		DecommissioningList newList = addListWithValidationRequestToBobAndChuck();
		addUserValidationToList(newList, records.getBob_userInAC());
		LogicalSearchQuery query = queryFactory.getListsToValidateQuery(records.getChuckNorris());
		assertThat(searchServices.search(query)).extracting("id").containsOnly(newList.getId());
	}

	@Test
	public void givenUserWithNoPermissionWhenSearchingValidatedListsThenReturnValidatedLists()
			throws RecordServicesException {
		DecommissioningList newList = addListWithValidationRequestToBobAndChuck();
		addUserValidationToList(newList, records.getBob_userInAC());
		addUserValidationToList(newList, records.getChuckNorris());
		LogicalSearchQuery query = queryFactory.getValidatedListsQuery(records.getAlice());
		assertThat(searchServices.hasResults(query)).isFalse();
	}

	@Test
	public void givenUserWithApprovePermissionWhenSearchingValidatedListsThenReturnValidatedLists()
			throws RecordServicesException {
		DecommissioningList newList = addListWithValidationRequestToBobAndChuck();
		addUserValidationToList(newList, records.getBob_userInAC());
		addUserValidationToList(newList, records.getChuckNorris());
		User gandalf_managerInABC = records.getGandalf_managerInABC();
		LogicalSearchQuery query = queryFactory.getValidatedListsQuery(gandalf_managerInABC);
		List<Record> decommissioningListsProcessable = searchServices.search(query);
		assertThat(decommissioningListsProcessable).extracting("id").containsOnly(records.list_01);
		for (Record decommissioningList : decommissioningListsProcessable) {
			assertThat(gandalf_managerInABC.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).on(decommissioningList)).isFalse();
			assertThat(gandalf_managerInABC.has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).on(decommissioningList)).isTrue();
		}
	}

	@Test
	public void givenUserWithProcessPermissionWhenSearchingValidatedListsThenReturnValidatedLists()
			throws RecordServicesException {
		DecommissioningList newList = addListWithValidationRequestToBobAndChuck();
		addUserValidationToList(newList, records.getBob_userInAC());
		addUserValidationToList(newList, records.getChuckNorris());
		LogicalSearchQuery query = queryFactory.getValidatedListsQuery(records.getChuckNorris());
		assertThat(searchServices.search(query)).extracting("id").containsOnly(newList.getId());
	}

	@Test
	public void givenUserWithNoPermissionWhenSearchingListsPendingApprovalThenReturnNone() {
		LogicalSearchQuery query = queryFactory.getListsPendingApprovalQuery(records.getAlice());
		assertThat(searchServices.hasResults(query)).isFalse();
	}

	@Test
	public void givenUserWithApprovePermissionWhenSearchingListsPendingApprovalThenReturnNone() {

		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		User gandalf_managerInABC = records.getGandalf_managerInABC();
		LogicalSearchQuery query = queryFactory.getListsPendingApprovalQuery(gandalf_managerInABC);
		List<Record> decommissioningListsProcessable = searchServices.search(query);
		assertThat(decommissioningListsProcessable).extracting("id").containsOnly(records.list_23, records.list_25);
		for (Record decommissioningList : decommissioningListsProcessable) {
			assertThat(gandalf_managerInABC.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).on(decommissioningList)).isFalse();
			assertThat(gandalf_managerInABC.has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).on(decommissioningList)).isTrue();
		}
	}

	@Test
	public void givenUserWithProcessPermissionWhenSearchingListsPendingApprovalThenReturnLists() {
		LogicalSearchQuery query = queryFactory.getListsPendingApprovalQuery(records.getChuckNorris());
		assertThat(searchServices.search(query)).extracting("id").containsOnly(records.list_23, records.list_25);
	}

	@Test
	public void givenUserWithNoPermissionWhenSearchingListsToApproveThenReturnNone() {
		LogicalSearchQuery query = queryFactory.getListsToApproveQuery(records.getAlice());
		assertThat(searchServices.hasResults(query)).isFalse();
	}

	@Test
	public void givenUserWithProcessPermissionWhenSearchingListsToApproveThenReturnNone() {
		LogicalSearchQuery query = queryFactory.getListsToApproveQuery(records.getAdmin());
		assertThat(searchServices.hasResults(query)).isFalse();
	}

	@Test
	public void givenUserWithApprovePermissionWhenSearchingListsToApproveThenReturnLists() {
		LogicalSearchQuery query = queryFactory.getListsToApproveQuery(records.getGandalf_managerInABC());
		assertThat(searchServices.search(query)).extracting("id").containsOnly(records.list_23);
	}

	@Test
	public void givenUserWithNoPermissionWhenSearchingApprovedListsThenReturnNone() {
		LogicalSearchQuery query = queryFactory.getApprovedListsQuery(records.getAlice());
		assertThat(searchServices.hasResults(query)).isFalse();
	}

	@Test
	public void givenUserWithApprovePermissionWhenSearchingApprovedListsThenReturnNone() {
		User gandalf_managerInABC = records.getGandalf_managerInABC();
		LogicalSearchQuery query = queryFactory.getApprovedListsQuery(gandalf_managerInABC);
		List<Record> decommissioningListsProcessable = searchServices.search(query);
		assertThat(decommissioningListsProcessable).extracting("id").containsOnly(records.list_24);
		for (Record decommissioningList : decommissioningListsProcessable) {
			assertThat(gandalf_managerInABC.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).on(decommissioningList)).isFalse();
			assertThat(gandalf_managerInABC.has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).on(decommissioningList)).isTrue();
		}
	}

	@Test
	public void givenUserWithProcessPermissionWhenSearchingApprovedListsThenReturnApprovedLists() {
		LogicalSearchQuery query = queryFactory.getApprovedListsQuery(records.getChuckNorris());
		assertThat(searchServices.search(query)).extracting("id").containsOnly(records.list_24);
	}

	@Test
	public void givenUserWithNoPermissionWhenSearchingProcessedListsThenReturnNone() {
		LogicalSearchQuery query = queryFactory.getProcessedListsQuery(records.getAlice());
		assertThat(searchServices.hasResults(query)).isFalse();
	}

	@Test
	public void givenUserWithApprovePermissionWhenSearchingProcessedListsThenReturnNone() {
		User gandalf_managerInABC = records.getGandalf_managerInABC();
		LogicalSearchQuery query = queryFactory.getProcessedListsQuery(gandalf_managerInABC);
		List<Record> decommissioningListsProcessable = searchServices.search(query);
		assertThat(decommissioningListsProcessable).extracting("id").containsOnly(
				records.list_11, records.list_12, records.list_13, records.list_14, records.list_15);
		for (Record decommissioningList : decommissioningListsProcessable) {
			assertThat(gandalf_managerInABC.has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).on(decommissioningList)).isFalse();
			assertThat(gandalf_managerInABC.has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).on(decommissioningList)).isTrue();
		}
	}

	@Test
	public void givenUserWithProcessPermissionWhenSearchingProcessedListsThenReturnProcessedLists() {
		LogicalSearchQuery query = queryFactory.getProcessedListsQuery(records.getChuckNorris());
		assertThat(searchServices.search(query)).extracting("id").containsOnly(
				records.list_11, records.list_12, records.list_13, records.list_14, records.list_15);
	}

	void addUserValidationToList(DecommissioningList list, User user)
			throws RecordServicesException {
		list.getValidationFor(user.getId()).setValidationDate(TimeProvider.getLocalDate());
		recordServices.add(list);
	}

	private DecommissioningList addListWithValidationRequestToBobAndChuck()
			throws RecordServicesException {
		String bob = records.getBob_userInAC().getId();
		String chuck = records.getChuckNorris().getId();
		DecommissioningList list = records.getList01()
				.addValidationRequest(bob, TimeProvider.getLocalDate())
				.addValidationRequest(chuck, TimeProvider.getLocalDate());
		recordServices.add(list);
		return list;
	}
}
