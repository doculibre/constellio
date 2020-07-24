package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKViewNavigation;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ListContentAccessAuthorizationsPresenterAcceptTest extends ConstellioTest {

	String alice;
	String legends;
	String rumors;

	String zeRootConcept = "zeRootConcept";
	String zeConcept = "zeConcept";
	String zeFolder = "zeFolder";

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();

	@Mock SessionContext sessionContext;
	@Mock ListContentAccessAuthorizationsView accessView;
	@Mock ListContentRoleAuthorizationsView roleView;
	ListContentAccessAuthorizationsPresenter accessPresenter;
	ListContentRoleAuthorizationsPresenter rolePresenter;

	RMSchemasRecordsServices rm;

	@Test
	public void givenDetachRequestedIsClickedThenDetachCorrectlyAndDoNotThrowException() {
		accessPresenter.forRequestParams(records.folder_A01);
		rolePresenter.forRequestParams(records.folder_A01);

		accessPresenter.detachRequested();
		rolePresenter.detachRequested();
	}

	@Test
	public void givenFolderWithoutOwnAndInheritedAuthorizationsWhenGetAuthorizationsThenEmpty()
			throws Exception {
		accessPresenter.forRequestParams(zeFolder);
		rolePresenter.forRequestParams(zeFolder);

		assertThatInheritedAccessAuthorizations().isEmpty();
		assertThatOwnAccessAuthorizations().isEmpty();
	}

	@Test
	public void givenFolderWithInheritedAuthorizationsWhenGetInheritedAuthorizationThenOk()
			throws Exception {

		givenAliceIsInLegendsGroup();
		add(givenAuthorizationFor(alice).on(zeFolder).givingReadAccess());
		add(givenAuthorizationFor(alice).on(zeFolder).giving(RMRoles.USER));
		add(givenAuthorizationFor(legends).on(zeFolder).givingReadWriteDeleteAccess());
		add(givenAuthorizationFor(legends).on(zeFolder).giving(RMRoles.MANAGER));
		add(givenAuthorizationFor(alice).on(zeConcept).givingReadWriteAccess());
		add(givenAuthorizationFor(alice).on(zeConcept).giving(RMRoles.USER));
		add(givenAuthorizationFor(legends).on(zeConcept).givingReadWriteDeleteAccess());
		add(givenAuthorizationFor(legends).on(zeConcept).giving(RMRoles.MANAGER));
		waitForBatchProcess();

		accessPresenter.forRequestParams(zeFolder);
		rolePresenter.forRequestParams(zeFolder);

		assertThatInheritedAccessAuthorizations().containsOnly(
				AuthorizationVO.forUsers(alice).givingReadWriteAccess().on(zeConcept),
				AuthorizationVO.forGroups(legends).givingReadWriteDeleteAccess().on(zeConcept));
		assertThatOwnAccessAuthorizations().containsOnly(
				AuthorizationVO.forUsers(alice).givingReadAccess().on(zeFolder),
				AuthorizationVO.forGroups(legends).givingReadWriteDeleteAccess().on(zeFolder));

		assertThatInheritedRoleAuthorizations().containsOnly(
				AuthorizationVO.forUsers(alice).giving(RMRoles.USER).on(zeConcept),
				AuthorizationVO.forGroups(legends).giving(RMRoles.MANAGER).on(zeConcept));
		assertThatOwnRoleAuthorizations().containsOnly(
				AuthorizationVO.forUsers(alice).giving(RMRoles.USER).on(zeFolder),
				AuthorizationVO.forGroups(legends).giving(RMRoles.MANAGER).on(zeFolder));
	}

	// -------------------------------------------------------------------------

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus());
		when(accessView.getSessionContext()).thenReturn(sessionContext);
		when(roleView.getSessionContext()).thenReturn(sessionContext);
		when(sessionContext.getCurrentCollection()).thenReturn(zeCollection);
		accessPresenter = new ListContentAccessAuthorizationsPresenter(accessView);
		rolePresenter = new ListContentRoleAuthorizationsPresenter(roleView);

		new SDKViewNavigation(accessView);
		new SDKViewNavigation(roleView);

		alice = users.aliceIn(zeCollection).getId();
		legends = users.legendsIn(zeCollection).getId();
		rumors = users.rumorsIn(zeCollection).getId();

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		Transaction transaction = new Transaction();
		transaction.add(rm.newAdministrativeUnitWithId(zeRootConcept).setCode("Ze root unit").setTitle("Ze root unit!"));
		transaction
				.add(rm.newAdministrativeUnitWithId(zeConcept).setCode("Ze unit").setTitle("Ze unit!").setParent(zeRootConcept));
		transaction.add(rm.newFolderWithId(zeFolder).setTitle("Ze folder!").setAdministrativeUnitEntered(zeConcept)
				.setCategoryEntered(records.categoryId_ZE42).setRetentionRuleEntered(records.ruleId_1)
				.setOpenDate(new LocalDate()));
		getModelLayerFactory().newRecordServices().execute(transaction);

		waitForBatchProcess();

		//		givenLegendsGroupHasAuthorizations();
	}

	//	private void givenLegendsGroupHasAuthorizations() {
	//
	//		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
	//
	//		authorizationsServices.add(new Authorization())
	//
	//		BatchProcessesManager batchProcessesManager = getModelLayerFactory().getBatchProcessesManager();
	//		batchProcessesManager.waitUntilAllFinished();
	//	}

	private org.assertj.core.api.ListAssert<AuthorizationVO> assertThatInheritedAccessAuthorizations() {
		return assertThat(accessPresenter.getInheritedAuthorizations())
				.usingElementComparatorIgnoringFields("authId", "userRolesTitles", "negative");
	}

	private org.assertj.core.api.ListAssert<AuthorizationVO> assertThatOwnAccessAuthorizations() {
		return assertThat(accessPresenter.getOwnAuthorizations())
				.usingElementComparatorIgnoringFields("authId", "userRolesTitles", "negative");
	}

	private org.assertj.core.api.ListAssert<AuthorizationVO> assertThatInheritedRoleAuthorizations() {
		return assertThat(rolePresenter.getInheritedAuthorizations())
				.usingElementComparatorIgnoringFields("authId", "userRolesTitles", "negative");
	}

	private org.assertj.core.api.ListAssert<AuthorizationVO> assertThatOwnRoleAuthorizations() {
		return assertThat(rolePresenter.getOwnAuthorizations())
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
		userServices.execute(users.aliceAddUpdateRequest().addToGroupInEachCollection(users.legends().getCode()));
	}
}

