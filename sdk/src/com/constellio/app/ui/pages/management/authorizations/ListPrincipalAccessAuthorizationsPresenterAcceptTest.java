package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static com.constellio.sdk.tests.FakeSessionContext.forRealUserIncollection;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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

	@Test
	public void whenCurrentUserHasGlobalSecurityPermissionThenCanGiveCollectionAccessOnUsers()
			throws Exception {

		sessionContext = forRealUserIncollection(users.adminIn(zeCollection));
		when(accessView.getSessionContext()).thenReturn(sessionContext);
		accessPresenter = new ListPrincipalAccessAuthorizationsPresenter(accessView);
		accessPresenter.forRequestParams(users.dakotaIn(zeCollection).getId());
		assertThat(accessPresenter.seeCollectionAccessField()).isTrue();
		assertThat(accessPresenter.getCollectionAccessChoicesModifiableByCurrentUser())
				.containsOnly(Role.READ, Role.DELETE, Role.WRITE);

		sessionContext = forRealUserIncollection(users.adminIn(zeCollection));
		when(accessView.getSessionContext()).thenReturn(sessionContext);
		accessPresenter = new ListPrincipalAccessAuthorizationsPresenter(accessView);
		accessPresenter.forRequestParams(users.legendsIn(zeCollection).getId());
		assertThat(accessPresenter.seeCollectionAccessField()).isFalse();

		sessionContext = forRealUserIncollection(users.adminIn(zeCollection));
		when(accessView.getSessionContext()).thenReturn(sessionContext);
		accessPresenter = new ListPrincipalAccessAuthorizationsPresenter(accessView);
		accessPresenter.forRequestParams(users.adminIn(zeCollection).getId());
		assertThat(accessPresenter.seeCollectionAccessField()).isTrue();

		sessionContext = forRealUserIncollection(users.chuckNorrisIn(zeCollection));
		when(accessView.getSessionContext()).thenReturn(sessionContext);
		accessPresenter = new ListPrincipalAccessAuthorizationsPresenter(accessView);
		accessPresenter.forRequestParams(users.dakotaIn(zeCollection).getId());
		assertThat(accessPresenter.seeCollectionAccessField()).isTrue();
		assertThat(accessPresenter.getCollectionAccessChoicesModifiableByCurrentUser())
				.containsOnly(Role.READ, Role.DELETE, Role.WRITE);

		sessionContext = forRealUserIncollection(users.gandalfIn(zeCollection));
		when(accessView.getSessionContext()).thenReturn(sessionContext);
		accessPresenter = new ListPrincipalAccessAuthorizationsPresenter(accessView);
		accessPresenter.forRequestParams(users.dakotaIn(zeCollection).getId());
		assertThat(accessPresenter.seeCollectionAccessField()).isFalse();
		assertThat(accessPresenter.getCollectionAccessChoicesModifiableByCurrentUser()).isEmpty();

		sessionContext = forRealUserIncollection(users.gandalfIn(zeCollection));
		when(accessView.getSessionContext()).thenReturn(sessionContext);
		accessPresenter = new ListPrincipalAccessAuthorizationsPresenter(accessView);
		accessPresenter.forRequestParams(users.dakotaIn(zeCollection).getId());
		assertThat(accessPresenter.seeCollectionAccessField()).isFalse();
		assertThat(accessPresenter.getCollectionAccessChoicesModifiableByCurrentUser()).isEmpty();

	}

	@Test
	public void whenModifyingCollectionAccessesThenCorrectlySaved()
			throws Exception {

		accessPresenter.forRequestParams(users.dakotaIn(zeCollection).getId());
		assertThat(accessPresenter.getUserGlobalAccess()).isEmpty();

		accessPresenter.accessCreationRequested(asList(Role.READ, Role.DELETE));

		assertThat(accessPresenter.getUserGlobalAccess()).containsOnly(Role.READ, Role.DELETE);

		accessPresenter.accessCreationRequested(asList(Role.WRITE));

		assertThat(accessPresenter.getUserGlobalAccess()).containsOnly(Role.READ, Role.WRITE);

		accessPresenter.accessCreationRequested(asList(Role.DELETE));

		assertThat(accessPresenter.getUserGlobalAccess()).containsOnly(Role.READ, Role.DELETE);

		accessPresenter.accessCreationRequested(asList(Role.READ));

		assertThat(accessPresenter.getUserGlobalAccess()).containsOnly(Role.READ);

		accessPresenter.accessCreationRequested(new ArrayList<String>());

		assertThat(accessPresenter.getUserGlobalAccess()).isEmpty();

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
				.usingElementComparatorIgnoringFields("authId", "userRolesTitles", "negative");
	}

	private org.assertj.core.api.ListAssert<AuthorizationVO> assertThatOwnAccessAuthorizations() {
		return assertThat(accessPresenter.getOwnAuthorizations())
				.usingElementComparatorIgnoringFields("authId", "userRolesTitles", "negative");
	}

	private void add(AuthorizationAddRequest authorization) {
		getModelLayerFactory().newAuthorizationsServices().add(authorization, User.GOD);
	}

	private AuthorizationAddRequest givenAuthorizationFor(String principalId) {
		return authorizationInCollection(zeCollection).forPrincipalsIds(asList(principalId));
	}

	private void givenAliceIsInLegendsGroup() {
		UserServices userServices = getModelLayerFactory().newUserServices();
		userServices.addUpdateUserCredential(users.aliceAddUpdateRequest().addGlobalGroup(users.legends().getCode()));
	}
}

