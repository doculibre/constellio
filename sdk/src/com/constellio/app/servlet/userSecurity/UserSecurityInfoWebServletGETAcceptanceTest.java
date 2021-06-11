package com.constellio.app.servlet.userSecurity;

import com.constellio.app.modules.restapi.apis.v1.validation.exception.ExpiredTokenException;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.core.exception.InvalidAuthenticationException;
import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.servlet.userSecurity.dto.UserCollectionAccessDto;
import com.constellio.app.servlet.userSecurity.dto.UserCollectionPermissionDto;
import com.constellio.app.servlet.userSecurity.dto.UserSecurityInfoDto;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.app.servlet.userSecurity.UserSecurityInfoWebServlet.HEADER_PARAM_AUTH;
import static com.constellio.app.servlet.userSecurity.UserSecurityInfoWebServlet.PARAM_SERVICE_KEY;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForGroups;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class UserSecurityInfoWebServletGETAcceptanceTest extends ConstellioTest {

	private String testCollection = "test";
	private String testGroup = "testGroup";
	private String folderInUnit20 = "testFolder";
	private String bobAuth = "bobAuth";
	private String expiredAuth = "expiredAuth";
	private String bobKey = "bobKey";

	protected WebTarget webTarget;

	private RMTestRecords records;
	private Users zeUsers = new Users();

	private RecordServices recordServices;
	private UserServices userServices;
	private RolesManager rolesManager;
	private AuthorizationsServices authorizationsServices;
	private RMSchemasRecordsServices rm;

	@Before
	public void setUp()
			throws Exception {
		records = new RMTestRecords(zeCollection);

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus()
						.withAllTestUsers().withAllTest(zeUsers),
				withCollection(testCollection).withConstellioRMModule().withFoldersAndContainersOfEveryStatus()
						.withAllTestUsers());

		webTarget = newWebTarget("userSecurityInfo", new ObjectMapper(), false);

		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();
		rolesManager = getModelLayerFactory().getRolesManager();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		userServices.execute(zeUsers.bobAddUpdateRequest().setServiceKey(bobKey)
				.addAccessToken(bobAuth, TimeProvider.getLocalDateTime().plusYears(1))
				.addAccessToken(expiredAuth, TimeProvider.getLocalDateTime().minusDays(1)));

		setupRolePermissions();
		setupGroupAndUnit();
	}

	@Test
	public void whenCallingServiceForAUserWithUserRole() throws Exception {
		setUserRoles(zeCollection, bobGratton, asList(RMRoles.USER));

		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		UserSecurityInfoDto info = response.readEntity(UserSecurityInfoDto.class);
		List<UserCollectionPermissionDto> collectionPermissions = info.getCollectionPermissions().stream()
				.filter(perm -> perm.getCollection().equals(zeCollection)).collect(Collectors.toList());
		assertThat(collectionPermissions.size()).isEqualTo(1);
		validatePermissionsForRole(collectionPermissions.get(0).getPermissionCodes(), zeCollection, asList(RMRoles.USER));
	}

	@Test
	public void whenCallingServiceForAUserWithRGDRole() throws Exception {
		setUserRoles(zeCollection, bobGratton, asList(RMRoles.RGD));

		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		UserSecurityInfoDto info = response.readEntity(UserSecurityInfoDto.class);
		List<UserCollectionPermissionDto> collectionPermissions = info.getCollectionPermissions().stream()
				.filter(perm -> perm.getCollection().equals(zeCollection)).collect(Collectors.toList());
		assertThat(collectionPermissions.size()).isEqualTo(1);
		validatePermissionsForRole(collectionPermissions.get(0).getPermissionCodes(), zeCollection, asList(RMRoles.RGD));
	}

	@Test
	public void whenCallingServiceForAUserWithBothRole() throws Exception {
		setUserRoles(zeCollection, bobGratton, asList(RMRoles.USER, RMRoles.RGD));

		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		UserSecurityInfoDto info = response.readEntity(UserSecurityInfoDto.class);
		List<UserCollectionPermissionDto> collectionPermissions = info.getCollectionPermissions().stream()
				.filter(perm -> perm.getCollection().equals(zeCollection)).collect(Collectors.toList());
		assertThat(collectionPermissions.size()).isEqualTo(1);
		validatePermissionsForRole(collectionPermissions.get(0).getPermissionCodes(), zeCollection, asList(RMRoles.USER, RMRoles.RGD));
	}

	@Test
	public void whenCallingServiceForAUserWithDifferentRolesInDifferentCollections() throws Exception {
		setUserRoles(zeCollection, bobGratton, asList(RMRoles.RGD));
		setUserRoles(testCollection, bobGratton, asList(RMRoles.USER));

		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		UserSecurityInfoDto info = response.readEntity(UserSecurityInfoDto.class);
		assertThat(info.getCollectionPermissions().stream().map(UserCollectionPermissionDto::getCollection).collect(Collectors.toList()))
				.contains(zeCollection, testCollection);
		for (UserCollectionPermissionDto perms : info.getCollectionPermissions()) {
			if (perms.getCollection().equals(zeCollection)) {
				validatePermissionsForRole(perms.getPermissionCodes(), perms.getCollection(), asList(RMRoles.RGD));
			} else {
				validatePermissionsForRole(perms.getPermissionCodes(), perms.getCollection(), asList(RMRoles.USER));
			}
		}
	}

	@Test
	public void whenCallingServiceForAUserWithUserRoleInRGDGroup() throws Exception {
		setUserRoles(zeCollection, bobGratton, asList(RMRoles.USER));
		userServices.execute(zeUsers.bobAddUpdateRequest().addToGroupInCollection(testGroup, zeCollection));
		assertThat(userServices.getUserInCollection(bobGratton, zeCollection).getUserGroups().stream()
				.map(id -> recordServices.getDocumentById(id).get(Schemas.CODE)).collect(Collectors.toList())).contains(testGroup);

		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		UserSecurityInfoDto info = response.readEntity(UserSecurityInfoDto.class);
		List<UserCollectionPermissionDto> collectionPermissions = info.getCollectionPermissions().stream()
				.filter(perm -> perm.getCollection().equals(zeCollection)).collect(Collectors.toList());
		assertThat(collectionPermissions.size()).isEqualTo(1);
		validatePermissionsForRole(collectionPermissions.get(0).getPermissionCodes(), zeCollection, asList(RMRoles.USER, RMRoles.RGD));
	}

	@Test
	public void whenCallingServiceForAUserWithUserRoleAndRGDAuthOnUnit() throws Exception {
		setUserRoles(zeCollection, bobGratton, asList(RMRoles.USER));

		AdministrativeUnit unit = rm.getAdministrativeUnit(records.unitId_10);
		authorizationsServices.add(authorizationForUsers(zeUsers.bobIn(zeCollection)).on(unit)
				.giving(rolesManager.getRole(zeCollection, RMRoles.RGD)));

		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		UserSecurityInfoDto info = response.readEntity(UserSecurityInfoDto.class);
		List<UserCollectionPermissionDto> collectionPermissions = info.getCollectionPermissions().stream()
				.filter(perm -> perm.getCollection().equals(zeCollection)).collect(Collectors.toList());
		assertThat(collectionPermissions.size()).isEqualTo(1);
		validatePermissionsForRole(collectionPermissions.get(0).getPermissionCodes(), zeCollection, asList(RMRoles.USER));
	}

	@Test
	public void whenCallingServiceForAUserWithReadAccess() throws Exception {
		User user = zeUsers.bobIn(zeCollection).setCollectionReadAccess(true);
		recordServices.update(user);

		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		UserSecurityInfoDto info = response.readEntity(UserSecurityInfoDto.class);
		List<UserCollectionAccessDto> collectionAccess = info.getCollectionAccess().stream()
				.filter(perm -> perm.getCollection().equals(zeCollection)).collect(Collectors.toList());
		assertThat(collectionAccess.size()).isEqualTo(1);
		assertThat(collectionAccess.get(0).isHasReadAccess()).isTrue();
		assertThat(collectionAccess.get(0).isHasWriteAccess()).isFalse();
	}

	@Test
	public void whenCallingServiceForAUserWithWriteAccess() throws Exception {
		User user = zeUsers.bobIn(zeCollection).setCollectionWriteAccess(true);
		recordServices.update(user);

		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		UserSecurityInfoDto info = response.readEntity(UserSecurityInfoDto.class);
		List<UserCollectionAccessDto> collectionAccess = info.getCollectionAccess().stream()
				.filter(perm -> perm.getCollection().equals(zeCollection)).collect(Collectors.toList());
		assertThat(collectionAccess.size()).isEqualTo(1);
		assertThat(collectionAccess.get(0).isHasReadAccess()).isFalse();
		assertThat(collectionAccess.get(0).isHasWriteAccess()).isTrue();
	}

	@Test
	public void whenCallingServiceForAUserWithBothAccess() throws Exception {
		User user = zeUsers.bobIn(zeCollection).setCollectionReadAccess(true).setCollectionWriteAccess(true);
		recordServices.update(user);

		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		UserSecurityInfoDto info = response.readEntity(UserSecurityInfoDto.class);
		List<UserCollectionAccessDto> collectionAccess = info.getCollectionAccess().stream()
				.filter(perm -> perm.getCollection().equals(zeCollection)).collect(Collectors.toList());
		assertThat(collectionAccess.size()).isEqualTo(1);
		assertThat(collectionAccess.get(0).isHasReadAccess()).isTrue();
		assertThat(collectionAccess.get(0).isHasWriteAccess()).isTrue();
	}

	@Test
	public void whenCallingServiceForAUserWithDifferentAccessInDifferentCollections() throws Exception {
		User user = zeUsers.bobIn(zeCollection).setCollectionReadAccess(true).setCollectionWriteAccess(true);
		recordServices.update(user);

		user = zeUsers.bobIn(testCollection).setCollectionReadAccess(true);
		recordServices.update(user);

		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		UserSecurityInfoDto info = response.readEntity(UserSecurityInfoDto.class);
		List<UserCollectionAccessDto> collectionAccess = info.getCollectionAccess().stream()
				.filter(perm -> perm.getCollection().equals(zeCollection)).collect(Collectors.toList());
		assertThat(collectionAccess.size()).isEqualTo(1);
		assertThat(collectionAccess.get(0).isHasReadAccess()).isTrue();
		assertThat(collectionAccess.get(0).isHasWriteAccess()).isTrue();

		assertThat(info.getCollectionAccess().stream().map(UserCollectionAccessDto::getCollection).collect(Collectors.toList()))
				.contains(zeCollection, testCollection);
		for (UserCollectionAccessDto access : info.getCollectionAccess()) {
			if (access.getCollection().equals(zeCollection)) {
				assertThat(access.isHasReadAccess()).isTrue();
				assertThat(access.isHasWriteAccess()).isTrue();
			} else {
				assertThat(access.isHasReadAccess()).isTrue();
				assertThat(access.isHasWriteAccess()).isFalse();
			}
		}
	}

	@Test
	public void whenCallingServiceForAUserWithReadAccessOnUnit() throws Exception {
		AdministrativeUnit unit = rm.getAdministrativeUnit(records.unitId_20);
		authorizationsServices.add(authorizationForUsers(zeUsers.bobIn(zeCollection)).on(unit)
				.givingReadAccess());

		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		UserSecurityInfoDto info = response.readEntity(UserSecurityInfoDto.class);
		List<UserCollectionAccessDto> collectionAccess = info.getCollectionAccess().stream()
				.filter(perm -> perm.getCollection().equals(zeCollection)).collect(Collectors.toList());
		assertThat(collectionAccess.size()).isEqualTo(1);

		assertThat(collectionAccess.get(0).getUnitsWithReadAccess()).contains(records.unitId_20);
		assertThat(collectionAccess.get(0).getUnitsWithWriteAccess()).doesNotContain(records.unitId_20);
	}

	@Test
	public void whenCallingServiceForAUserWithWriteAccessOnUnit() throws Exception {
		AdministrativeUnit unit = rm.getAdministrativeUnit(records.unitId_20);
		authorizationsServices.add(authorizationForUsers(zeUsers.bobIn(zeCollection)).on(unit)
				.givingWriteAccess());

		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		UserSecurityInfoDto info = response.readEntity(UserSecurityInfoDto.class);
		List<UserCollectionAccessDto> collectionAccess = info.getCollectionAccess().stream()
				.filter(perm -> perm.getCollection().equals(zeCollection)).collect(Collectors.toList());
		assertThat(collectionAccess.size()).isEqualTo(1);

		assertThat(collectionAccess.get(0).getUnitsWithReadAccess()).contains(records.unitId_20);
		assertThat(collectionAccess.get(0).getUnitsWithWriteAccess()).contains(records.unitId_20);
	}

	@Test
	public void whenCallingServiceForAUserWithBothAccessOnUnit() throws Exception {
		AdministrativeUnit unit = rm.getAdministrativeUnit(records.unitId_20);
		authorizationsServices.add(authorizationForUsers(zeUsers.bobIn(zeCollection)).on(unit)
				.givingReadWriteAccess());

		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		UserSecurityInfoDto info = response.readEntity(UserSecurityInfoDto.class);
		List<UserCollectionAccessDto> collectionAccess = info.getCollectionAccess().stream()
				.filter(perm -> perm.getCollection().equals(zeCollection)).collect(Collectors.toList());
		assertThat(collectionAccess.size()).isEqualTo(1);

		assertThat(collectionAccess.get(0).getUnitsWithReadAccess()).contains(records.unitId_20);
		assertThat(collectionAccess.get(0).getUnitsWithWriteAccess()).contains(records.unitId_20);
	}

	@Test
	public void whenCallingServiceForAUserWithNoAccessInReadWriteGroupOnUnit() throws Exception {
		userServices.execute(zeUsers.bobAddUpdateRequest().addToGroupInCollection(testGroup, zeCollection));
		assertThat(userServices.getUserInCollection(bobGratton, zeCollection).getUserGroups().stream()
				.map(id -> recordServices.getDocumentById(id).get(Schemas.CODE)).collect(Collectors.toList())).contains(testGroup);

		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		UserSecurityInfoDto info = response.readEntity(UserSecurityInfoDto.class);
		List<UserCollectionAccessDto> collectionAccess = info.getCollectionAccess().stream()
				.filter(perm -> perm.getCollection().equals(zeCollection)).collect(Collectors.toList());
		assertThat(collectionAccess.size()).isEqualTo(1);

		assertThat(collectionAccess.get(0).getUnitsWithReadAccess()).contains(records.unitId_20);
		assertThat(collectionAccess.get(0).getUnitsWithWriteAccess()).contains(records.unitId_20);
	}

	@Test
	public void whenCallingServiceForAUserWithNoAccessOnUnitAndReadWriteAuthOnFolder() throws Exception {
		Folder folder = rm.getFolder(folderInUnit20);
		assertThat(folder.getAdministrativeUnit()).isEqualTo(records.unitId_20);
		authorizationsServices.add(authorizationForUsers(zeUsers.bobIn(zeCollection)).on(folder)
				.givingReadWriteAccess());

		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		UserSecurityInfoDto info = response.readEntity(UserSecurityInfoDto.class);
		List<UserCollectionAccessDto> collectionAccess = info.getCollectionAccess().stream()
				.filter(perm -> perm.getCollection().equals(zeCollection)).collect(Collectors.toList());
		assertThat(collectionAccess.size()).isEqualTo(1);

		assertThat(collectionAccess.get(0).getUnitsWithReadAccess()).doesNotContain(records.unitId_20);
		assertThat(collectionAccess.get(0).getUnitsWithWriteAccess()).doesNotContain(records.unitId_20);
	}

	@Test
	public void whenCallingServiceWithoutAuthorizationHeader() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request().get();

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new InvalidAuthenticationException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithEmptyAuthorizationHeader() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request()
				.header(HEADER_PARAM_AUTH, "").get();

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new InvalidAuthenticationException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithInvalidSchemeInAuthorizationHeader() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request()
				.header(HEADER_PARAM_AUTH, "Basic ".concat(bobAuth)).get();

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new InvalidAuthenticationException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithoutSchemeInAuthorizationHeader() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request()
				.header(HEADER_PARAM_AUTH, bobAuth).get();

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new InvalidAuthenticationException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithExpiredToken() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(expiredAuth)).get();

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new ExpiredTokenException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithInvalidToken() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat("fakeToken")).get();

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithoutServiceKeyParam() {
		Response response = webTarget.request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth)).get();

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RequiredParameterException(PARAM_SERVICE_KEY).getValidationError()));
	}

	@Test
	public void whenCallingServiceWithEmptyServiceKeyParam() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, "").request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth)).get();

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RequiredParameterException(PARAM_SERVICE_KEY).getValidationError()));
	}

	@Test
	public void whenCallingServiceWithInvalidServiceKeyParam() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, "fakeKey").request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth)).get();

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	private void setupRolePermissions() {
		Role role = rolesManager.getRole(zeCollection, RMRoles.USER);
		role = role.withPermissions(asList(RMPermissionsTo.SEND_SIGNATURE_REQUEST, RMPermissionsTo.USE_MY_CART));
		rolesManager.updateRole(role);

		role = rolesManager.getRole(zeCollection, RMRoles.RGD);
		role = role.withPermissions(asList(RMPermissionsTo.SEND_SIGNATURE_REQUEST, RMPermissionsTo.USE_GROUP_CART));
		rolesManager.updateRole(role);

		role = rolesManager.getRole(testCollection, RMRoles.USER);
		role = role.withPermissions(asList(RMPermissionsTo.SEND_SIGNATURE_REQUEST, RMPermissionsTo.BORROW_FOLDER));
		rolesManager.updateRole(role);

		role = rolesManager.getRole(testCollection, RMRoles.RGD);
		role = role.withPermissions(asList(RMPermissionsTo.SEND_SIGNATURE_REQUEST, RMPermissionsTo.BORROW_CONTAINER));
		rolesManager.updateRole(role);
	}

	private void setUserRoles(String collection, String username, List<String> roles) throws Exception {
		recordServices.update(userServices.getUserRecordInCollection(username, collection).setUserRoles(roles));
	}

	private void validatePermissionsForRole(List<String> permissions, String collection, List<String> roles) {
		if (roles.size() == 1) {
			assertThat(permissions.size()).isEqualTo(2);
		} else {
			assertThat(permissions.size()).isEqualTo(3);
		}

		if (collection.equals(zeCollection)) {
			if (roles.contains(RMRoles.USER)) {
				assertThat(permissions).contains(RMPermissionsTo.SEND_SIGNATURE_REQUEST, RMPermissionsTo.USE_MY_CART);
			}
			if (roles.contains(RMRoles.RGD)) {
				assertThat(permissions).contains(RMPermissionsTo.SEND_SIGNATURE_REQUEST, RMPermissionsTo.USE_GROUP_CART);
			}
		} else if (collection.equals(testCollection)) {
			if (roles.contains(RMRoles.USER)) {
				assertThat(permissions).contains(RMPermissionsTo.SEND_SIGNATURE_REQUEST, RMPermissionsTo.BORROW_FOLDER);
			}
			if (roles.contains(RMRoles.RGD)) {
				assertThat(permissions).contains(RMPermissionsTo.SEND_SIGNATURE_REQUEST, RMPermissionsTo.BORROW_CONTAINER);
			}
		} else {
			fail("Should not contains '" + collection + "' collection.");
		}
	}

	private void setupGroupAndUnit() throws Exception {
		userServices.createGroup(testGroup, (req) -> req.setName("Group test").addCollections(zeCollection, testCollection));
		recordServices.update(userServices.getGroupInCollection(testGroup, zeCollection).setRoles(asList(RMRoles.RGD)));

		AdministrativeUnit unit = rm.getAdministrativeUnit(records.unitId_20);
		authorizationsServices.add(authorizationForGroups(userServices.getGroupInCollection(testGroup, zeCollection)).on(unit)
				.givingReadWriteAccess());

		Folder folder = rm.newFolderWithId(folderInUnit20).setTitle("Test").setAdministrativeUnitEntered(unit)
				.setCategoryEntered(records.categoryId_X).setRetentionRuleEntered(records.ruleId_1)
				.setMediumTypes(records.PA).setOpenDate(new LocalDate());
		recordServices.add(folder);
	}
}