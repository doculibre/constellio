/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.management.authorizations;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.global.AuthorizationBuilder;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class ListPrincipalAccessAuthorizationsPresenterAcceptTest extends ConstellioTest {

	String alice;
	String legends;
	String rumors;

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();

	@Mock SessionContext sessionContext;
	@Mock ListPrincipalAccessAuthorizationsView accessView;
	ListPrincipalAccessAuthorizationsPresenter accessPresenter;

	@Test
	public void givenUserWithoutAuthorizationsWhenGetUserInheritedAndOwnAuthorizationsThenEmpty()
			throws Exception {

		accessPresenter.forRequestParams(users.aliceIn(zeCollection).getId());

		assertThatInheritedAccessAuthorizations().isEmpty();
		assertThatOwnAccessAuthorizations().isEmpty();
	}

	@Test
	public void givenUserWithInheritedAndOwnAuthorizationsWhenGetInheritedAuthorizationThenOk()
			throws Exception {

		givenAliceIsInLegendsGroup();
		add(givenAuthorizationFor(legends).on(records.folder_A04).givingReadAccess());
		add(givenAuthorizationFor(legends).on(records.folder_A04).giving(RMRoles.USER));
		add(givenAuthorizationFor(legends).on(records.unitId_10a).givingReadWriteDeleteAccess());
		add(givenAuthorizationFor(legends).on(records.unitId_10a).giving(RMRoles.MANAGER));
		add(givenAuthorizationFor(alice).on(records.folder_B01).givingReadWriteAccess());
		add(givenAuthorizationFor(alice).on(records.folder_B01).giving(RMRoles.USER));
		add(givenAuthorizationFor(alice).on(records.unitId_11b).givingReadWriteDeleteAccess());
		add(givenAuthorizationFor(alice).on(records.unitId_11b).giving(RMRoles.MANAGER));
		waitForBatchProcess();

		accessPresenter.forRequestParams(users.aliceIn(zeCollection).getId());

		assertThatInheritedAccessAuthorizations().containsOnly(
				AuthorizationVO.forGroups(legends).givingReadAccess().on(records.folder_A04),
				AuthorizationVO.forGroups(legends).givingReadWriteDeleteAccess().on(records.unitId_10a));
		assertThatOwnAccessAuthorizations().containsOnly(
				AuthorizationVO.forUsers(alice).givingReadWriteAccess().on(records.folder_B01),
				AuthorizationVO.forUsers(alice).givingReadWriteDeleteAccess().on(records.unitId_11b));
	}

	@Test
	public void givenGroupWithoutAuthorizationsWhenGetInheritedAndOwnAuthorizationsThenEmpty()
			throws Exception {

		accessPresenter.forRequestParams(users.rumorsIn(zeCollection).getId());

		assertThatInheritedAccessAuthorizations().isEmpty();
		assertThatOwnAccessAuthorizations().isEmpty();
	}

	@Test
	public void givenGroupWithInheritedAuthorizationsWhenGetInheritedAuthorizationThenOk()
			throws Exception {
		givenAliceIsInLegendsGroup();
		add(givenAuthorizationFor(rumors).on(records.folder_A04).givingReadWriteAccess());
		add(givenAuthorizationFor(rumors).on(records.folder_A04).giving(RMRoles.USER));
		add(givenAuthorizationFor(rumors).on(records.unitId_10a).givingReadWriteDeleteAccess());
		add(givenAuthorizationFor(rumors).on(records.unitId_10a).giving(RMRoles.MANAGER));
		add(givenAuthorizationFor(legends).on(records.folder_B01).givingReadWriteAccess());
		add(givenAuthorizationFor(legends).on(records.folder_B01).giving(RMRoles.USER));
		add(givenAuthorizationFor(legends).on(records.unitId_11b).givingReadWriteDeleteAccess());
		add(givenAuthorizationFor(legends).on(records.unitId_11b).giving(RMRoles.MANAGER));
		waitForBatchProcess();

		accessPresenter.forRequestParams(users.rumorsIn(zeCollection).getId());

		assertThatInheritedAccessAuthorizations().containsOnly(
				AuthorizationVO.forGroups(legends).givingReadWriteAccess().on(records.folder_B01),
				AuthorizationVO.forGroups(legends).givingReadWriteDeleteAccess().on(records.unitId_11b));
		assertThatOwnAccessAuthorizations().containsOnly(
				AuthorizationVO.forGroups(rumors).givingReadWriteAccess().on(records.folder_A04),
				AuthorizationVO.forGroups(rumors).givingReadWriteDeleteAccess().on(records.unitId_10a)
		);
	}

	// -------------------------------------------------------------------------

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus());
		when(accessView.getSessionContext()).thenReturn(sessionContext);
		when(sessionContext.getCurrentCollection()).thenReturn(zeCollection);
		accessPresenter = new ListPrincipalAccessAuthorizationsPresenter(accessView);

		alice = users.aliceIn(zeCollection).getId();
		legends = users.legendsIn(zeCollection).getId();
		rumors = users.rumorsIn(zeCollection).getId();

	}

	private org.assertj.core.api.ListAssert<AuthorizationVO> assertThatInheritedAccessAuthorizations() {
		return assertThat(accessPresenter.getInheritedAuthorizations())
				.usingElementComparatorIgnoringFields("authId", "userRolesTitles");
	}

	private org.assertj.core.api.ListAssert<AuthorizationVO> assertThatOwnAccessAuthorizations() {
		return assertThat(accessPresenter.getOwnAuthorizations())
				.usingElementComparatorIgnoringFields("authId", "userRolesTitles");
	}

	private void add(Authorization authorization) {
		getModelLayerFactory().newAuthorizationsServices().add(authorization, User.GOD);
	}

	private AuthorizationBuilder givenAuthorizationFor(String principalId) {
		return new AuthorizationBuilder(zeCollection).forPrincipalsIds(asList(principalId));
	}

	private void givenAliceIsInLegendsGroup() {
		UserServices userServices = getModelLayerFactory().newUserServices();
		userServices.addUpdateUserCredential(users.alice().withNewGlobalGroup(users.legends().getCode()));
	}
}

