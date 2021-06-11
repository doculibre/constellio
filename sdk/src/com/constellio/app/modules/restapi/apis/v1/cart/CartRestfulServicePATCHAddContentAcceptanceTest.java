package com.constellio.app.modules.restapi.apis.v1.cart;

import com.constellio.app.modules.restapi.apis.v1.BaseRestfulServiceAcceptanceTest;
import com.constellio.app.modules.restapi.apis.v1.cart.dto.CartContentUpdateDto;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.ExpiredTokenException;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnallowedHostException;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthorizedAccessException;
import com.constellio.app.modules.restapi.core.exception.AtLeastOneParameterRequiredException;
import com.constellio.app.modules.restapi.core.exception.InvalidAuthenticationException;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.core.exception.mapper.RestApiErrorResponse;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.utils.CartUtil;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.security.roles.RolesManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static java.util.Arrays.asList;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

public class CartRestfulServicePATCHAddContentAcceptanceTest extends BaseRestfulServiceAcceptanceTest {
	private RolesManager rolesManager;
	private CartUtil cartUtil;

	private Cart cart;
	private CartContentUpdateDto contentToAdd;

	private String roleWithGroupPermission = "roleWithGroupPermission";
	private String roleWithMyPermission = "roleWithMyPermission";

	@Before
	public void setUp() throws Exception {
		setUpTest();

		rolesManager = getModelLayerFactory().getRolesManager();
		cartUtil = new CartUtil(zeCollection, getAppLayerFactory());

		webTarget = newWebTarget("v1/cart/content", new ObjectMapper());

		rolesManager.addRole(new Role(zeCollection, roleWithGroupPermission, "Role with group permission", new ArrayList<String>()));
		rolesManager.addRole(new Role(zeCollection, roleWithMyPermission, "Role with my permission", new ArrayList<String>()));

		Role role = rolesManager.getRole(zeCollection, roleWithMyPermission);
		role = role.withPermissions(asList(RMPermissionsTo.USE_MY_CART));
		rolesManager.updateRole(role);

		role = rolesManager.getRole(zeCollection, roleWithGroupPermission);
		role = role.withPermissions(asList(RMPermissionsTo.USE_GROUP_CART));
		rolesManager.updateRole(role);

		recordServices.update(userServices.getUserRecordInCollection(bobGratton, zeCollection)
				.setUserRoles(asList(roleWithGroupPermission)));

		cart = createCart();
		contentToAdd = CartContentUpdateDto.builder().itemsToAdd(asList(records.folder_B02, records.folder_B02)).build();

		commitCounter.reset();
		queryCounter.reset();
	}

	@Test
	public void validateServiceWithMyCartId()
			throws Exception {
		recordServices.update(userServices.getUserRecordInCollection(bobGratton, zeCollection)
				.setUserRoles(asList(roleWithMyPermission)));

		String myCartId = users.bobIn(zeCollection).getId();

		Folder folder = rm.getFolder(records.folder_A10);
		folder.addFavorite(myCartId);
		recordServices.update(folder);

		folder = rm.getFolder(records.folder_A11);
		folder.addFavorite(myCartId);
		recordServices.update(folder);

		folder = rm.getFolder(records.folder_A12);
		folder.addFavorite(myCartId);
		recordServices.update(folder);

		List<Record> favorites = cartUtil.getCartRecords(myCartId);
		assertThat(favorites.size()).isEqualTo(3);

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", myCartId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.build("PATCH", entity(contentToAdd, APPLICATION_JSON_TYPE)).invoke();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(2);
		assertThat(commitCounter.newCommitsCall()).isNotEmpty();

		folder = rm.getFolder(records.folder_B02);
		assertThat(folder.getFavorites()).contains(myCartId);

		favorites = cartUtil.getCartRecords(myCartId);
		assertThat(favorites.size()).isEqualTo(4);
	}

	@Test
	public void validateServiceWithCustomGroupCartId() {
		assertThat(cart.isLogicallyDeletedStatus()).isFalse();

		List<Record> favorites = cartUtil.getCartRecords(cart.getId());
		assertThat(favorites.size()).isEqualTo(3);

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", cart.getId()).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.build("PATCH", entity(contentToAdd, APPLICATION_JSON_TYPE)).invoke();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(2);
		assertThat(commitCounter.newCommitsCall()).isNotEmpty();

		cart = rm.getCart(cart.getId());
		assertThat(cart.isLogicallyDeletedStatus()).isFalse();

		Folder folder = rm.getFolder(records.folder_B02);
		assertThat(folder.getFavorites()).contains(cart.getId());

		favorites = cartUtil.getCartRecords(cart.getId());
		assertThat(favorites.size()).isEqualTo(4);
	}

	@Test
	public void validateServiceWithoutWritePermissionOnFolder() throws Exception {
		Record record = recordServices.getDocumentById(records.folder_B02);
		User bobUser = userServices.getUserInCollection(bob, record.getCollection());
		authorizationsServices.add(authorizationForUsers(bobUser).on(record).givingNegativeWriteAccess());

		List<Record> favorites = cartUtil.getCartRecords(cart.getId());
		assertThat(favorites.size()).isEqualTo(3);

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", cart.getId()).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.build("PATCH", entity(contentToAdd, APPLICATION_JSON_TYPE)).invoke();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		favorites = cartUtil.getCartRecords(cart.getId());
		assertThat(favorites.size()).isEqualTo(4);
	}

	@Test
	public void validateServiceWithBothMode() {
		List<Record> favorites = cartUtil.getCartRecords(cart.getId());
		assertThat(favorites.size()).isEqualTo(3);

		CartContentUpdateDto updateDto = CartContentUpdateDto.builder()
				.itemsToAdd(asList(records.folder_B02, records.folder_A01))
				.itemsToRemove(asList(records.folder_A01, records.folder_C51)).build();

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", cart.getId()).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.build("PATCH", entity(updateDto, APPLICATION_JSON_TYPE)).invoke();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		cart = rm.getCart(cart.getId());
		assertThat(cart.isLogicallyDeletedStatus()).isFalse();

		Folder folder = rm.getFolder(records.folder_C51);
		assertThat(folder.getFavorites()).isEmpty();

		folder = rm.getFolder(records.folder_B02);
		assertThat(folder.getFavorites()).contains(cart.getId());

		folder = rm.getFolder(records.folder_A01);
		assertThat(folder.getFavorites()).contains(cart.getId());

		favorites = cartUtil.getCartRecords(cart.getId());
		assertThat(favorites.size()).isEqualTo(3);
	}

	@Test
	public void whenCallingServiceWithoutAuthorizationHeader() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", cart.getId()).request()
				.header(HttpHeaders.HOST, host)
				.build("PATCH", entity(contentToAdd, APPLICATION_JSON_TYPE)).invoke();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
		assertThat(response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Bearer");

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new InvalidAuthenticationException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithEmptyAuthorizationHeader() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", cart.getId()).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "")
				.build("PATCH", entity(contentToAdd, APPLICATION_JSON_TYPE)).invoke();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
		assertThat(response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Bearer");

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new InvalidAuthenticationException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithInvalidSchemeInAuthorizationHeader() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", cart.getId()).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Basic ".concat(token))
				.build("PATCH", entity(contentToAdd, APPLICATION_JSON_TYPE)).invoke();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
		assertThat(response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Bearer");

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new InvalidAuthenticationException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithoutSchemeInAuthorizationHeader() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", cart.getId()).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, token)
				.build("PATCH", entity(contentToAdd, APPLICATION_JSON_TYPE)).invoke();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
		assertThat(response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Bearer");

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new InvalidAuthenticationException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithExpiredToken() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", cart.getId()).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(expiredToken))
				.build("PATCH", entity(contentToAdd, APPLICATION_JSON_TYPE)).invoke();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new ExpiredTokenException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithInvalidToken() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", cart.getId()).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(fakeToken))
				.build("PATCH", entity(contentToAdd, APPLICATION_JSON_TYPE)).invoke();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithoutServiceKeyParam() {
		Response response = webTarget.queryParam("id", cart.getId()).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.build("PATCH", entity(contentToAdd, APPLICATION_JSON_TYPE)).invoke();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "serviceKey"));
	}

	@Test
	public void whenCallingServiceWithInvalidServiceKeyParam() {
		Response response = webTarget.queryParam("serviceKey", fakeServiceKey)
				.queryParam("id", cart.getId()).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.build("PATCH", entity(contentToAdd, APPLICATION_JSON_TYPE)).invoke();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithUnallowedHostHeader() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", cart.getId()).request()
				.header(HttpHeaders.HOST, fakeHost).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.build("PATCH", entity(contentToAdd, APPLICATION_JSON_TYPE)).invoke();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnallowedHostException(fakeHost).getValidationError()));
	}

	@Test
	public void whenCallingServiceWithMissingId() {
		Response response = webTarget.queryParam("serviceKey", serviceKey).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.build("PATCH", entity(contentToAdd, APPLICATION_JSON_TYPE)).invoke();

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "cartId"));
	}

	@Test
	public void whenCallingServiceWithInvalidId() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", "fakeId").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.build("PATCH", entity(contentToAdd, APPLICATION_JSON_TYPE)).invoke();

		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new RecordNotFoundException("fakeId").getValidationError()));
	}

	@Test
	public void whenCallingServiceWithMissingContent() {
		CartContentUpdateDto updateDto = CartContentUpdateDto.builder().build();
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", cart.getId()).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.build("PATCH", entity(updateDto, APPLICATION_JSON_TYPE)).invoke();

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new AtLeastOneParameterRequiredException("CartContentUpdateDto.itemsToAdd", "CartContentUpdateDto.itemsToRemove").getValidationError()));
	}

	@Test
	public void whenCallingServiceWithInvalidContent() {
		contentToAdd = CartContentUpdateDto.builder().itemsToAdd(asList("fakeId")).build();
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", cart.getId()).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.build("PATCH", entity(contentToAdd, APPLICATION_JSON_TYPE)).invoke();

		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new RecordNotFoundException("fakeId").getValidationError()));
	}

	@Test
	public void whenCallingServiceWithoutReadPermissionOnFolder() throws Exception {
		Record record = recordServices.getDocumentById(records.folder_B02);
		User bobUser = userServices.getUserInCollection(bob, record.getCollection());
		authorizationsServices.add(authorizationForUsers(bobUser).on(record).givingNegativeReadWriteAccess());

		List<Record> favorites = cartUtil.getCartRecords(cart.getId());
		assertThat(favorites.size()).isEqualTo(3);

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", cart.getId()).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.build("PATCH", entity(contentToAdd, APPLICATION_JSON_TYPE)).invoke();

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	@Test
	public void whenCallingServiceForMyCartWithoutPermission() throws Exception {
		String myCartId = users.bobIn(zeCollection).getId();

		Folder folder = rm.getFolder(records.folder_A10);
		folder.addFavorite(myCartId);
		recordServices.update(folder);

		folder = rm.getFolder(records.folder_A11);
		folder.addFavorite(myCartId);
		recordServices.update(folder);

		folder = rm.getFolder(records.folder_A12);
		folder.addFavorite(myCartId);
		recordServices.update(folder);

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", myCartId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.build("PATCH", entity(contentToAdd, APPLICATION_JSON_TYPE)).invoke();

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	@Test
	public void whenCallingServiceForCustomGroupCartWithoutPermission() throws Exception {
		recordServices.update(userServices.getUserRecordInCollection(bobGratton, zeCollection)
				.setUserRoles(asList(roleWithMyPermission)));

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", cart.getId()).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.build("PATCH", entity(contentToAdd, APPLICATION_JSON_TYPE)).invoke();

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	private Cart createCart() throws Exception {
		Cart cart = rm.newCartWithId("cartId");
		cart.setOwner(users.bobIn(zeCollection).getId());
		cart.setTitle("new cart");
		recordServices.add(cart);

		Folder folder = rm.getFolder(records.folder_A01);
		folder.addFavorite(cart.getId());
		recordServices.update(folder);

		folder = rm.getFolder(records.folder_C51);
		folder.addFavorite(cart.getId());
		recordServices.update(folder);

		folder = rm.getFolder(records.folder_A80);
		folder.addFavorite(cart.getId());
		recordServices.update(folder);

		return cart;
	}
}
