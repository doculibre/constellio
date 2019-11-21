package com.constellio.app.ui.acceptation.collection;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.management.authorizations.TransferPermissionPresenter;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

public class TransferPermissionPresenterAcceptanceTest extends ConstellioTest {
	TransferPermissionPresenter transferPermissionPresenter;
	@Mock RecordVO sourceUserVO;
	@Mock List<String> destinationUsers;
	@Mock SchemasRecordsServices schemasRecordsServices;
	@Mock UserServices userServices;

	@Before
	public void setUp() {
	}

	@Test
	public void whenCopyingAccessRightsThenSelectedUsersAccessRightsAreExactlyTheSameAsSourceUser() {

	}

	@Test
	public void whenTransferingAccessRightsThenSelectedUsersRolesAreExactlyTheSameAsSourceUser() {
		transferPermissionPresenter.copyUserAuthorizations(sourceUserVO, destinationUsers);
		User sourceUser = wrapUser(sourceUserVO.getRecord());
		for (String destUserId : destinationUsers) {
			//User destUser = userServices.		coreSchemas().getUser(destUserId);
			//assertThat(destUser.getUserRoles()).isEqualTo(sourceUser.getUserRoles());
		}
	}

	@Test
	public void givenMultipleDestinationUsersSelectedWhenTransferingAccessRightsThenCorrectConfirmMessage() {

	}

	@Test
	public void givenSingleDestinationUserSelectedWhenTransferingAccessRightsThenCorrectConfirmMessage() {

	}

	@Test
	public void givenRemoveAccessCheckboxCheckedWhenTransferingAccessRightsThenCorrectConfirmMessage() {

	}

	@Test
	public void givenRemoveAccessCheckboxCheckedWhenTransferingAccessRightsThenSourceUserAuthorizationsAreRemoved() {

	}

	@Test
	public void givenRemoveAccessCheckboxCheckedWhenTransferingAccessRightsThenSourceUserRolesAreRemoved() {

	}

	@Test
	public void givenNoDestinationUserSelectedDestinationUsersWhenTransferingAccessRightsThenErrorMessage() {

	}

	@Test
	public void givenSourceUserInSelectedDestinationUsersWhenTransferingAccessRightsThenErrorMessage() {

	}

	@Test
	public void givenDestinationUserWhenTransferingAccessRightsThenAuthorizationIsSameAsSourceUser() {

	}


	@Test
	public void givenDestinationUserWithSameAuthorizationAsSourceUserWhenTransferingAccessRightsThenDestinationUserNotAdddedToAuthorization() {

	}

	//dequoi avec si le bouton est disabled (admin)

}
