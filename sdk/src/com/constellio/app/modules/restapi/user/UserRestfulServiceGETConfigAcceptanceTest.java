package com.constellio.app.modules.restapi.user;

import com.constellio.app.modules.restapi.BaseRestfulServiceAcceptanceTest;
import com.constellio.app.modules.restapi.core.exception.InvalidAuthenticationException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotFoundException;
import com.constellio.app.modules.restapi.core.exception.mapper.RestApiErrorResponse;
import com.constellio.app.modules.restapi.user.dto.UserConfigDto;
import com.constellio.app.modules.restapi.user.exception.UserConfigNotSupportedException;
import com.constellio.app.modules.restapi.validation.exception.ExpiredTokenException;
import com.constellio.app.modules.restapi.validation.exception.UnallowedHostException;
import com.constellio.app.modules.restapi.validation.exception.UnauthenticatedUserException;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.entities.security.global.UserCredential;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

public class UserRestfulServiceGETConfigAcceptanceTest extends BaseRestfulServiceAcceptanceTest {

	private List<String> mails;

	@Before
	public void setUp() throws Exception {
		setUpTest();

		webTarget = newWebTarget("v1/user/credentials/config", new ObjectMapper());

		mails = asList("m1", "m2", "m3", "m4", "m5");

		UserCredential userCredentials = userServices.getUser(users.bobIn(zeCollection).getUsername());
		userCredentials.set(UserCredential.PERSONAL_EMAILS, mails);
		userServices.addUpdateUserCredential(userCredentials);

		queryCounter.reset();
		commitCounter.reset();
	}

	@Test
	public void validateService() {
		UserCredential userCredentials = userServices.getUser(users.bobIn(zeCollection).getUsername());
		List<String> favoritesDisplayOrder = userCredentials.getList(UserCredential.PERSONAL_EMAILS);
		assertThat(favoritesDisplayOrder).containsExactly("m1", "m2", "m3", "m4", "m5");

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("localCode", UserCredential.PERSONAL_EMAILS).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();

		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		UserConfigDto configDto = response.readEntity(UserConfigDto.class);
		assertThat(configDto.getLocalCode()).isEqualTo(UserCredential.PERSONAL_EMAILS);
		assertThat(configDto.getValue()).containsExactly("m1", "m2", "m3", "m4", "m5");
	}

	@Test
	public void validateServiceWithEmptyData() {
		UserCredential userCredentials = userServices.getUser(users.bobIn(zeCollection).getUsername());
		userCredentials.set(UserCredential.PERSONAL_EMAILS, null);
		userServices.addUpdateUserCredential(userCredentials);

		queryCounter.reset();
		commitCounter.reset();

		List<String> favoritesDisplayOrder = userCredentials.getList(UserCredential.PERSONAL_EMAILS);
		assertThat(favoritesDisplayOrder).isEmpty();

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("localCode", UserCredential.PERSONAL_EMAILS).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();

		assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());
	}

	@Test
	public void whenCallingServiceWithoutAuthorizationHeader() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("localCode", UserCredential.PERSONAL_EMAILS).request()
				.header(HttpHeaders.HOST, host).get();
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
				.queryParam("localCode", UserCredential.PERSONAL_EMAILS).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "").get();
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
				.queryParam("localCode", UserCredential.PERSONAL_EMAILS).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Basic ".concat(token)).get();
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
				.queryParam("localCode", UserCredential.PERSONAL_EMAILS).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, token).get();
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
				.queryParam("localCode", UserCredential.PERSONAL_EMAILS).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(expiredToken)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new ExpiredTokenException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithInvalidToken() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("localCode", UserCredential.PERSONAL_EMAILS).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(fakeToken)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithoutServiceKeyParam() {
		Response response = webTarget.queryParam("localCode", UserCredential.PERSONAL_EMAILS).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "serviceKey"));
	}

	@Test
	public void whenCallingServiceWithInvalidServiceKeyParam() {
		Response response = webTarget.queryParam("serviceKey", fakeServiceKey)
				.queryParam("localCode", UserCredential.PERSONAL_EMAILS).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithUnallowedHostHeader() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("localCode", UserCredential.PERSONAL_EMAILS).request()
				.header(HttpHeaders.HOST, fakeHost).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnallowedHostException(fakeHost).getValidationError()));
	}

	@Test
	public void whenCallingServiceWithMissingCode() {
		Response response = webTarget.queryParam("serviceKey", serviceKey).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "localCode"));
	}

	@Test
	public void whenCallingServiceWithNonExistingCode() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("localCode", "fakeCode").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new MetadataNotFoundException("fakeCode").getValidationError()));
	}

	@Test
	public void whenCallingServiceWithInvalidCode() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("localCode", UserCredential.FIRST_NAME).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UserConfigNotSupportedException().getValidationError()));
	}
}
