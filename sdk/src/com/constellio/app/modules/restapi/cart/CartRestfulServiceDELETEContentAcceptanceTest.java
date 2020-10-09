package com.constellio.app.modules.restapi.cart;

import com.constellio.app.modules.restapi.BaseRestfulServiceAcceptanceTest;
import com.constellio.app.modules.restapi.core.exception.InvalidAuthenticationException;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.core.exception.mapper.RestApiErrorResponse;
import com.constellio.app.modules.restapi.validation.exception.ExpiredTokenException;
import com.constellio.app.modules.restapi.validation.exception.UnallowedHostException;
import com.constellio.app.modules.restapi.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.validation.exception.UnauthorizedAccessException;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.utils.CartUtil;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.entities.records.Record;
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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class CartRestfulServiceDELETEContentAcceptanceTest extends BaseRestfulServiceAcceptanceTest {
	private RolesManager rolesManager;
	private CartUtil cartUtil;

	private Cart cart;

	private String roleWithPermissions = "roleWithGroupPermissions";
	private String roleWithoutPermission = "roleWithoutPermission";

	@Before
	public void setUp() throws Exception {
		setUpTest();

		rolesManager = getModelLayerFactory().getRolesManager();
		cartUtil = new CartUtil(zeCollection, getAppLayerFactory());

		webTarget = newWebTarget("v1/cart/content", new ObjectMapper());

		rolesManager.addRole(new Role(zeCollection, roleWithPermissions, "Role with permissions", new ArrayList<String>()));
		rolesManager.addRole(new Role(zeCollection, roleWithoutPermission, "Role without permission", new ArrayList<String>()));

		Role role = rolesManager.getRole(zeCollection, roleWithPermissions);
		role = role.withPermissions(asList(RMPermissionsTo.USE_GROUP_CART, RMPermissionsTo.USE_MY_CART));
		rolesManager.updateRole(role);

		recordServices.update(userServices.getUserRecordInCollection(bobGratton, zeCollection)
				.setUserRoles(asList(roleWithPermissions)));

		cart = createCart();

		commitCounter.reset();
		queryCounter.reset();
	}

	@Test
	public void validateServiceWithMyCartId()
			throws Exception {
		String myCartId = users.bobIn(zeCollection).getId();

		Folder folder = rm.getFolder(records.folder_A10);
		folder.addFavorite(myCartId);
		recordServices.update(folder);

		folder = rm.getFolder(records.folder_A11);
		folder.addFavorite(myCartId);
		recordServices.update(folder);

		folder = rm.getFolder(records.folder_A11);
		folder.addFavorite(myCartId);
		recordServices.update(folder);

		List<Record> favorites = cartUtil.getCartRecords(myCartId);
		assertThat(favorites).isNotEmpty();

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", myCartId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.delete();

		assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(4);
		assertThat(commitCounter.newCommitsCall()).isNotEmpty();

		folder = rm.getFolder(records.folder_A11);
		assertThat(folder.getFavorites()).isEmpty();

		favorites = cartUtil.getCartRecords(myCartId);
		assertThat(favorites).isEmpty();
	}

	@Test
	public void validateServiceWithCustomGroupCartId() {
		assertThat(cart.isLogicallyDeletedStatus()).isFalse();

		List<Record> records = cartUtil.getCartRecords(cart.getId());
		assertThat(records).isNotEmpty();

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", cart.getId()).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.delete();

		assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(4);
		assertThat(commitCounter.newCommitsCall()).isNotEmpty();

		cart = rm.getCart(cart.getId());
		assertThat(cart.isLogicallyDeletedStatus()).isFalse();

		records = cartUtil.getCartRecords(cart.getId());
		assertThat(records).isEmpty();
	}

	@Test
	public void whenCallingServiceWithoutAuthorizationHeader() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", cart.getId()).request()
				.header(HttpHeaders.HOST, host)
				.delete();
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
				.delete();
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
				.delete();
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
				.delete();
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
				.delete();
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
				.delete();
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
				.delete();
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
				.delete();
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
				.delete();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnallowedHostException(fakeHost).getValidationError()));
	}

	@Test
	public void whenCallingServiceWithMissingId() {
		Response response = webTarget.queryParam("serviceKey", serviceKey).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.delete();

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "cartId"));
	}

	@Test
	public void whenCallingServiceWithInvalidId() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", "fakeId").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.delete();

		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new RecordNotFoundException("fakeId").getValidationError()));
	}

	@Test
	public void whenCallingServiceWithoutPermission() throws Exception {
		recordServices.update(userServices.getUserRecordInCollection(bobGratton, zeCollection)
				.setUserRoles(asList(roleWithoutPermission)));

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", cart.getId()).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.delete();

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
