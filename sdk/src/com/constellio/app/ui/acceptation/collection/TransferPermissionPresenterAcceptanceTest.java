package com.constellio.app.ui.acceptation.collection;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.collection.CollectionUserPresenter;
import com.constellio.app.ui.pages.collection.CollectionUserView;
import com.constellio.app.ui.pages.management.authorizations.TransferPermissionPresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;
import com.google.common.collect.Lists;
import com.vaadin.ui.Window;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class TransferPermissionPresenterAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	private Users users;

	@Mock Window window;
	@Mock CollectionUserView collectionUserView;
	private SessionContext sessionContext;
	TransferPermissionPresenter transferPermissionPresenter;
	private AuthorizationsServices authorizationsServices;
	private UserServices userServices;

	private User sourceUser;
	private User destUser1;
	private User destUser2;

	@Before
	public void setUp() {
		users = new Users();

		prepareSystem(withZeCollection()
				.withConstellioRMModule()
				.withAllTestUsers()
				.withRMTest(records)
				.withFoldersAndContainersOfEveryStatus());
		sessionContext = FakeSessionContext.gandalfInCollection(zeCollection);
		when(collectionUserView.getSessionContext()).thenReturn(sessionContext);

		userServices = getModelLayerFactory().newUserServices();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();

		initOrUpdateTestUsers();

		transferPermissionPresenter = spy(new CollectionUserPresenter(collectionUserView));
	}


	@Test
	public void whenCopyingAccessRightsThenAllSelectedUsersAccessRightsAreExactlyTheSameAsSourceUser() {
		List<String> destinationUsers = new ArrayList<>();
		destinationUsers.add(destUser1.getId());
		destinationUsers.add(destUser2.getId());

		List<Authorization> sourceUserAuthorizations = authorizationsServices.getRecordAuthorizations(sourceUser);
		for (Authorization authorization : sourceUserAuthorizations) {
			assertThat(authorization.getPrincipals()).contains(sourceUser.getId());
			assertThat(authorization.getPrincipals()).doesNotContain(destUser1.getId(), destUser2.getId());
		}

		transferPermissionPresenter.transferAccessSaveButtonClicked(toVO(sourceUser), destinationUsers, window);

		for (Authorization authorization : sourceUserAuthorizations) {
			assertThat(authorization.getPrincipals()).contains(destUser1.getId(), destUser2.getId());
		}
	}

	@Test
	public void givenRemoveAccessCheckboxCheckedWhenTransferringAccessRightsThenSourceUserAuthorizationsAreRemoved() {
		Record sourceUserRecord = sourceUser.getWrappedRecord();
		List<String> destinationUsers = new ArrayList<>();
		destinationUsers.add(destUser1.getId());
		destinationUsers.add(destUser2.getId());

		transferPermissionPresenter.setRemoveUserAccess(true);
		transferPermissionPresenter.transferAccessSaveButtonClicked(toVO(sourceUser), destinationUsers, window);

		List<Authorization> sourceUserAuthorizations = authorizationsServices.getRecordAuthorizations(sourceUserRecord);
		for (Authorization authorization : sourceUserAuthorizations) {
			assertThat(authorization.getPrincipals()).doesNotContain(sourceUser.getId());
		}
	}

	@Test
	public void givenRemoveAccessCheckboxUncheckedWhenTransferringAccessRightsThenSourceUserAuthorizationsRemain() {
		Record sourceUserRecord = sourceUser.getWrappedRecord();
		List<String> destinationUsers = new ArrayList<>();
		destinationUsers.add(destUser1.getId());
		destinationUsers.add(destUser2.getId());

		transferPermissionPresenter.setRemoveUserAccess(false);
		transferPermissionPresenter.transferAccessSaveButtonClicked(toVO(sourceUser), destinationUsers, window);
		List<Authorization> sourceUserAuthorizations = authorizationsServices.getRecordAuthorizations(sourceUserRecord);
		for (Authorization authorization : sourceUserAuthorizations) {
			assertThat(authorization.getPrincipals()).contains(sourceUser.getId());
		}
	}

	@Test        //TODO
	public void givenNoDestinationUserSelectedDestinationUsersWhenTransferringAccessRightsThenErrorMessage() {
		List<String> destinationsUsersList = new ArrayList<>();
		transferPermissionPresenter.transferAccessSaveButtonClicked((UserVO) sourceUser.getWrappedRecord(), destinationsUsersList, window);
	}

	@Test        //TODO
	public void givenSourceUserInSelectedDestinationUsersWhenTransferringAccessRightsThenErrorMessage() {
		Record sourceUserRecord = sourceUser.getWrappedRecord();

		List<String> destinationUsers = new ArrayList<>();
		destinationUsers.add(destUser1.getId());
		destinationUsers.add(destUser2.getId());
		destinationUsers.add(sourceUser.getId());
		transferPermissionPresenter.transferAccessSaveButtonClicked(toVO(sourceUser), destinationUsers, window);
		//assertThat(collectionUserView)
	}

	@Test
	public void givenDestinationUsersWhenTransferringAccessRightsThenGroupsSameAsSourceUser() {
		List<String> destinationIds = new ArrayList<String>();
		destinationIds.add(destUser1.getId());
		destinationIds.add(destUser2.getId());

		assertThat(destUser1.getUserGroups()).isNotEqualTo(sourceUser.getUserGroups());
		assertThat(destUser2.getUserGroups()).isNotEqualTo(sourceUser.getUserGroups());

		transferPermissionPresenter.transferAccessSaveButtonClicked(toVO(sourceUser), destinationIds, window);
		initOrUpdateTestUsers();

		assertThat(destUser1.getUserGroups()).isEqualTo(sourceUser.getUserGroups());
		assertThat(destUser2.getUserGroups()).isEqualTo(sourceUser.getUserGroups());
	}


	@Test
	public void givenDestinationUserWithSameAuthorizationAsSourceUserWhenTransferringAccessRightsThenDestinationUserNotAddedAgainToAuthorization() {
		List<String> destinationUsers = new ArrayList<>();
		destinationUsers.add(sourceUser.getId());

		transferPermissionPresenter.transferAccessSaveButtonClicked(toVO(sourceUser), destinationUsers, window);
		List<Authorization> sourceUserAuthorizations = authorizationsServices.getRecordAuthorizations(sourceUser);

		for (Authorization authorization : sourceUserAuthorizations) {
			assertThat(authorization.getPrincipals()).containsOnlyOnce(sourceUser.getId());
		}
	}

	@Test
		//TODO
	void givenAdminUserAndRemoveRightsCheckboxCheckedWhenTransferringAccessRightThenErrorMessage() {

	}


	public void initOrUpdateTestUsers() {
		sourceUser = userServices.getUserInCollection(gandalf, zeCollection);
		destUser1 = userServices.getUserInCollection(aliceWonderland, zeCollection);
		destUser2 = userServices.getUserInCollection(bobGratton, zeCollection);
	}

	public AuthorizationAddRequest givenAuthorizationFor(String principalId) {
		return authorizationInCollection(zeCollection).forPrincipalsIds(asList(principalId));
	}

	public RecordVO toVO(User user) {
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		return voBuilder.build(user.getWrappedRecord(), VIEW_MODE.FORM, sessionContext);
	}

	protected List<Authorization> filterInheritedAuthorizations(List<Authorization> authorizations, String recordId) {
		List<Authorization> filteredAuthorizations = Lists.newArrayList();
		for (Authorization authorization : authorizations) {
			if (authorization.getTarget().equals(recordId)) {
				filteredAuthorizations.add(authorization);
			}
		}
		return filteredAuthorizations;
	}

}
