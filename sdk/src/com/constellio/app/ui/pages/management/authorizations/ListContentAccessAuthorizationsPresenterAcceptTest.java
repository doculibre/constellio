package com.constellio.app.ui.pages.management.authorizations;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.global.AuthorizationBuilder;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

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

		alice = users.aliceIn(zeCollection).getId();
		legends = users.legendsIn(zeCollection).getId();
		rumors = users.rumorsIn(zeCollection).getId();

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
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
				.usingElementComparatorIgnoringFields("authId", "userRolesTitles");
	}

	private org.assertj.core.api.ListAssert<AuthorizationVO> assertThatOwnAccessAuthorizations() {
		return assertThat(accessPresenter.getOwnAuthorizations())
				.usingElementComparatorIgnoringFields("authId", "userRolesTitles");
	}

	private org.assertj.core.api.ListAssert<AuthorizationVO> assertThatInheritedRoleAuthorizations() {
		return assertThat(rolePresenter.getInheritedAuthorizations())
				.usingElementComparatorIgnoringFields("authId", "userRolesTitles");
	}

	private org.assertj.core.api.ListAssert<AuthorizationVO> assertThatOwnRoleAuthorizations() {
		return assertThat(rolePresenter.getOwnAuthorizations())
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

