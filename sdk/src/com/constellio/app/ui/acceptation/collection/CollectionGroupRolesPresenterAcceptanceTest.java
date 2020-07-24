package com.constellio.app.ui.acceptation.collection;

import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.ui.entities.RoleAuthVO;
import com.constellio.app.ui.pages.collection.CollectionGroupRolesPresenter;
import com.constellio.app.ui.pages.collection.CollectionGroupRolesView;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class CollectionGroupRolesPresenterAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup setup = new TestsSchemasSetup(zeCollection);

	Users users = new Users();
	String legends;
	String aliceId;
	String legendsId;

	private UserServices userServices;
	private RecordServices recordServices;

	String zeConcept = "zeConcept";

	CollectionGroupRolesPresenter presenter;
	@Mock CollectionGroupRolesView collectionGroupRolesView;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withCollection(zeCollection).withConstellioRMModule().withAllTestUsers());

		when(collectionGroupRolesView.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		defineSchemasManager().using(setup);

		userServices = getModelLayerFactory().newUserServices();
		recordServices = getModelLayerFactory().newRecordServices();

		recordServices.add(recordServices.newRecordWithSchema(setup.zeDefaultSchema(), zeConcept));

		users.setUp(userServices);
		aliceId = users.aliceIn(zeCollection).getId();
		legendsId = users.legendsIn(zeCollection).getId();
		legends = users.legends().getCode();

		presenter = new CollectionGroupRolesPresenter(collectionGroupRolesView);
		presenter.forRequestParams(legendsId);
	}

	// @Test I'll have to adjust my setup to test group inheritance
	public void givenUserWithInheritedRolesAndAuthsWhenGettingInheritedRolesThenAllRolesThere()
			throws Exception {
		givenAliceIsInLegendsGroup();
		add(givenAuthorizationFor(legendsId).on(zeConcept).giving(RMRoles.USER));
		givenLegendsAreManagerGlobally();
		waitForBatchProcess();

		List<RoleAuthVO> aliceInheritedRoles = presenter.getInheritedRoles();
		assertThat(aliceInheritedRoles).hasSize(2);
		verifyThat(aliceInheritedRoles).containsGlobalRole(RMRoles.MANAGER);
		verifyThat(aliceInheritedRoles).containsRoleAuths(RMRoles.USER).onTarget(zeConcept);
	}

	@Test
	public void givenGroupWithSpecificRolesAndAuthsWhenGettingSpecificRolesThenAllRolesThere()
			throws Exception {
		add(givenAuthorizationFor(legendsId).on(zeConcept).giving(RMRoles.USER));
		givenLegendsAreManagerGlobally();
		waitForBatchProcess();

		List<RoleAuthVO> legendsSpecificRoles = presenter.getSpecificRoles();
		assertThat(legendsSpecificRoles).hasSize(2);
		verifyThat(legendsSpecificRoles).containsGlobalRole(RMRoles.MANAGER);
		verifyThat(legendsSpecificRoles).containsRoleAuths(RMRoles.USER).onTarget(zeConcept);
	}

	@Test
	public void givenGroupWithGlobalRoleWhenDeletingItThenRoleIsDeleted()
			throws Exception {
		add(givenAuthorizationFor(legendsId).on(zeConcept).giving(RMRoles.USER));
		givenLegendsAreManagerGlobally();
		waitForBatchProcess();

		presenter.deleteRoleButtonClicked(
				new RoleAuthVO(null, null, asList(RMRoles.MANAGER)));
		waitForBatchProcess();

		List<RoleAuthVO> legendsSpecificRoles = presenter.getSpecificRoles();
		verifyThat(legendsSpecificRoles).doesNotContainGlobalRole(RMRoles.MANAGER);
		verifyThat(legendsSpecificRoles).containsRoleAuths(RMRoles.USER).onTarget(zeConcept);
	}

	@Test
	public void givenGroupWithRoleAuthWhenDeletingItThenAuthDeleted()
			throws Exception {
		AuthorizationAddRequest authorization = givenAuthorizationFor(legendsId).on(zeConcept).giving(RMRoles.USER);
		String id = add(authorization);
		waitForBatchProcess();

		presenter.deleteRoleButtonClicked(new RoleAuthVO(id, authorization.getTarget(), asList(RMRoles.USER)));
		waitForBatchProcess();

		List<RoleAuthVO> legendsSpecificRoles = presenter.getSpecificRoles();
		assertThat(legendsSpecificRoles).isEmpty();
		verifyThat(legendsSpecificRoles).doesNotContainRoleAuths(RMRoles.USER).onTarget(zeConcept);
	}

	@Test
	public void whenAddingGlobalRoleThenRoleAddedToGroup()
			throws Exception {
		presenter.addRoleButtonClicked(new RoleAuthVO(null, null, asList(RMRoles.MANAGER)));
		waitForBatchProcess();

		List<RoleAuthVO> legendsSpecificRoles = presenter.getSpecificRoles();
		assertThat(legendsSpecificRoles).hasSize(1);
		verifyThat(legendsSpecificRoles).containsGlobalRole(RMRoles.MANAGER);
	}

	@Test
	public void whenAddingRoleAuthThenAuthorizationAdded()
			throws Exception {
		presenter.addRoleButtonClicked(new RoleAuthVO(null, zeConcept, asList(RMRoles.MANAGER)));
		waitForBatchProcess();

		List<RoleAuthVO> aliceSpecificRoles = presenter.getSpecificRoles();
		assertThat(aliceSpecificRoles).hasSize(1);
		verifyThat(aliceSpecificRoles).containsRoleAuths(RMRoles.MANAGER).onTarget(zeConcept);
	}

	// ==================================================================================================
	// ==================================================================================================

	private void givenLegendsAreManagerGlobally()
			throws RecordServicesException {
		Group legendsGroup = userServices.getGroupInCollection(legends, zeCollection);
		List<String> roles = new ArrayList<>(legendsGroup.getRoles());
		roles.add(RMRoles.MANAGER);
		legendsGroup.setRoles(roles);
		recordServices.update(legendsGroup.getWrappedRecord());
	}

	private void givenAliceIsManagerGlobally()
			throws RecordServicesException {
		User aliceUser = userServices.getUserInCollection(aliceWonderland, zeCollection);
		List<String> roles = new ArrayList<>(aliceUser.getUserRoles());
		roles.add(RMRoles.MANAGER);
		aliceUser.setUserRoles(roles);
		recordServices.update(aliceUser.getWrappedRecord());
	}

	private String add(AuthorizationAddRequest authorization) {
		return getModelLayerFactory().newAuthorizationsServices().add(authorization, User.GOD);
	}

	private AuthorizationAddRequest givenAuthorizationFor(String principalId) {
		return authorizationInCollection(zeCollection).forPrincipalsIds(asList(principalId));
	}

	private void givenAliceIsInLegendsGroup() {
		UserServices userServices = getModelLayerFactory().newUserServices();
		userServices.execute(users.aliceAddUpdateRequest().addToGroupInEachCollection(users.legends().getCode()));
	}

	private RoleAuthVOListVerifier verifyThat(List<RoleAuthVO> roleAuthVOs) {
		return new RoleAuthVOListVerifier(roleAuthVOs);
	}

	private class RoleAuthVOListVerifier {
		private List<RoleAuthVO> roleAuthVOs;
		private List<String> wantedRoles = new ArrayList<>();
		private boolean expectedToContain = true;

		public RoleAuthVOListVerifier(List<RoleAuthVO> roleAuthVOs) {
			this.roleAuthVOs = roleAuthVOs;
		}

		public RoleAuthVOListVerifier containsRoleAuths(String... roles) {
			wantedRoles.clear();
			wantedRoles.addAll(asList(roles));
			return this;
		}

		public RoleAuthVOListVerifier doesNotContainRoleAuths(String... roles) {
			wantedRoles.clear();
			wantedRoles.addAll(asList(roles));
			expectedToContain = false;
			return this;
		}

		public void onTarget(String target) {
			boolean constainsWantedRolesOnTarget = false;
			for (RoleAuthVO roleAuthVO : roleAuthVOs) {
				if (target.equals(roleAuthVO.getTarget()) && roleAuthVO.getRoles().containsAll(wantedRoles)) {
					constainsWantedRolesOnTarget = true;
					break;
				}
			}
			assertThat(constainsWantedRolesOnTarget == expectedToContain).isTrue();
		}

		public void containsGlobalRole(String role) {
			boolean constainsRole = false;
			for (RoleAuthVO roleAuthVO : roleAuthVOs) {
				if (roleAuthVO.getId() == null && roleAuthVO.getRoles().contains(role)) {
					constainsRole = true;
					break;
				}
			}
			assertThat(constainsRole).isTrue();
		}

		public void doesNotContainGlobalRole(String role) {
			boolean constainsRole = false;
			for (RoleAuthVO roleAuthVO : roleAuthVOs) {
				if (roleAuthVO.getId() == null && roleAuthVO.getRoles().contains(role)) {
					constainsRole = true;
					break;
				}
			}
			assertThat(constainsRole).isFalse();
		}
	}
}
