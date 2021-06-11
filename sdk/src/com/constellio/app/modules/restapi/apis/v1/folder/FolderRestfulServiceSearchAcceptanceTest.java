package com.constellio.app.modules.restapi.apis.v1.folder;

import com.constellio.app.modules.restapi.apis.v1.BaseRestfulServiceAcceptanceTest;
import com.constellio.app.modules.restapi.apis.v1.folder.dto.FolderDto;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.ExpiredTokenException;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnallowedHostException;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.core.exception.InvalidAuthenticationException;
import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.restapi.core.exception.mapper.RestApiErrorResponse;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.sdk.tests.QueryCounter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.sdk.tests.QueryCounter.ON_COLLECTION;
import static org.assertj.core.api.Assertions.assertThat;

public class FolderRestfulServiceSearchAcceptanceTest extends BaseRestfulServiceAcceptanceTest {

	private String searchExpression = "abeille";
	private int autoCompleteSize = 3;

	@Before
	public void setUp() throws Exception {
		setUpTest();

		webTarget = newWebTarget("v1/folders/search", new ObjectMapper());

		givenConfig(ConstellioEIMConfigs.AUTOCOMPLETE_SIZE, autoCompleteSize);

		queryCounter = new QueryCounter(getDataLayerFactory(), ON_COLLECTION(zeCollection));

		commitCounter.reset();
		queryCounter.reset();
	}

	@Test
	public void whenCallingService() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("collection", zeCollection)
				.queryParam("expression", searchExpression).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(1);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		List<FolderDto> folders = response.readEntity(newFolderList());
		assertThat(folders).isNotNull();
		assertThat(folders.size()).isEqualTo(1);
	}

	@Test
	public void whenCallingServiceWithoutWritePermission() {
		Record record = recordServices.getDocumentById(records.folder_A01);
		User bobUser = userServices.getUserInCollection(bob, record.getCollection());
		authorizationsServices.add(authorizationForUsers(bobUser).on(record).givingNegativeWriteAccess());

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("collection", zeCollection)
				.queryParam("expression", searchExpression).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(2);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		List<FolderDto> folders = response.readEntity(newFolderList());
		assertThat(folders).isNotNull();
		assertThat(folders.size()).isEqualTo(0);
	}

	@Test
	public void whenCallingServiceWithHighResultCount() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("collection", zeCollection)
				.queryParam("expression", "a").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(1);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		List<FolderDto> folders = response.readEntity(newFolderList());
		assertThat(folders).isNotNull();
		assertThat(folders.size()).isEqualTo(autoCompleteSize);
	}

	@Test
	public void whenCallingServiceWithoutAuthorizationHeader() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("collection", zeCollection)
				.queryParam("expression", searchExpression).request()
				.header(HttpHeaders.HOST, host)
				.get();
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
				.queryParam("collection", zeCollection)
				.queryParam("expression", searchExpression).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "")
				.get();
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
				.queryParam("collection", zeCollection)
				.queryParam("expression", searchExpression).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Basic ".concat(token))
				.get();
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
				.queryParam("collection", zeCollection)
				.queryParam("expression", searchExpression).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, token)
				.get();
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
				.queryParam("collection", zeCollection)
				.queryParam("expression", searchExpression).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(expiredToken))
				.get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new ExpiredTokenException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithInvalidToken() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("collection", zeCollection)
				.queryParam("expression", searchExpression).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(fakeToken))
				.get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithoutServiceKeyParam() {
		Response response = webTarget.queryParam("collection", zeCollection)
				.queryParam("expression", searchExpression).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "serviceKey"));
	}

	@Test
	public void whenCallingServiceWithEmptyServiceKeyParam() {
		Response response = webTarget.queryParam("serviceKey", "")
				.queryParam("collection", zeCollection)
				.queryParam("expression", searchExpression).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new RequiredParameterException("serviceKey").getValidationError()));
	}

	@Test
	public void whenCallingServiceWithInvalidServiceKeyParam() {
		Response response = webTarget.queryParam("serviceKey", fakeServiceKey)
				.queryParam("collection", zeCollection)
				.queryParam("expression", searchExpression).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithUnallowedHostHeader() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("collection", zeCollection)
				.queryParam("expression", searchExpression).request()
				.header(HttpHeaders.HOST, fakeHost).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnallowedHostException(fakeHost).getValidationError()));
	}

	@Test
	public void whenCallingServiceWithMissingCollection() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("expression", searchExpression).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "collection"));
	}

	@Test
	public void whenCallingServiceWithEmptyCollection() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("collection", "")
				.queryParam("expression", searchExpression).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new RequiredParameterException("collection").getValidationError()));
	}

	@Test
	public void whenCallingServiceWithInvalidCollection() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("collection", "fakeCollection")
				.queryParam("expression", searchExpression).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithMissingExpression() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "expression"));
	}

	@Test
	public void whenCallingServiceWithEmptyExpression() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("collection", zeCollection)
				.queryParam("expression", "").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "expression"));
	}

	private GenericType<List<FolderDto>> newFolderList() {
		return new GenericType<List<FolderDto>>() {
		};
	}
}
