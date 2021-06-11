package com.constellio.app.modules.restapi.apis.v1.record;

import com.constellio.app.modules.restapi.apis.v1.BaseRestfulServiceAcceptanceTest;
import com.constellio.app.modules.restapi.apis.v1.record.dto.MetadataDto;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.ExpiredTokenException;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnallowedHostException;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthorizedAccessException;
import com.constellio.app.modules.restapi.core.exception.InvalidAuthenticationException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotFoundException;
import com.constellio.app.modules.restapi.core.exception.RecordLogicallyDeletedException;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.core.exception.UnsupportedMetadataTypeException;
import com.constellio.app.modules.restapi.core.exception.mapper.RestApiErrorResponse;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RecordRestfulServiceGETMetadataAcceptanceTest extends BaseRestfulServiceAcceptanceTest {

	protected SystemConfigurationsManager systemConfigurationsManager;

	private String folderId;

	private LocalDate localDate;
	private LocalDateTime localDateTime;
	private String dateFormat;
	private String dateTimeFormat;

	@Before
	public void setUp() throws Exception {
		setUpTest();

		localDate = LocalDate.now();
		localDateTime = LocalDateTime.now();

		systemConfigurationsManager = getModelLayerFactory().getSystemConfigurationsManager();
		dateFormat = systemConfigurationsManager.getValue(ConstellioEIMConfigs.DATE_FORMAT);

		dateTimeFormat = systemConfigurationsManager.getValue(ConstellioEIMConfigs.DATE_TIME_FORMAT);

		webTarget = newWebTarget("v1/record/metadata", new ObjectMapper());

		folderId = records.folder_A01;

		Folder folder = rm.getFolder(folderId);
		folder.setTitle("Default title");
		folder.setParentFolder(records.folder_A20);
		folder.setManualExpectedDepositDate(localDate.minusDays(1));
		folder.setFormCreatedOn(localDateTime.minusDays(1));
		folder.setLinearSize(28.0);
		folder.setBorrowed(false);
		folder.setKeywords(asList("F1", "F2", "F3"));
		recordServices.update(folder.getWrappedRecord());

		UserCredential credentials = userServices.getUserCredential(bob);
		credentials.set(UserCredential.TEAMS_HIDDEN_FAVORITES, asList("P1", "P2", "P3"));
		recordServices.update(credentials);

		commitCounter.reset();
		queryCounter.reset();
	}

	@Test
	public void validateService() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId)
				.queryParam("metadataCode", Folder.TITLE).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		MetadataDto dto = response.readEntity(MetadataDto.class);
		Folder folder = rm.getFolder(folderId);

		assertThat(dto.getCode()).isEqualTo(Folder.TITLE);
		assertThat(dto.getValues().get(0)).isEqualTo(folder.getTitle());
	}

	@Test
	public void validateServiceForUserCredentials() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", userServices.getUserCredential(bob).getId())
				.queryParam("metadataCode", UserCredential.TEAMS_HIDDEN_FAVORITES).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		MetadataDto dto = response.readEntity(MetadataDto.class);

		assertThat(dto.getCode()).isEqualTo(UserCredential.TEAMS_HIDDEN_FAVORITES);
		assertThat(dto.getValues()).containsAll(userServices.getUserCredential(bob).getList(UserCredential.TEAMS_HIDDEN_FAVORITES));
	}

	@Test
	public void whenCallingServiceForOtherUserCredentials() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", userServices.getUserCredential(sasquatch).getId())
				.queryParam("metadataCode", UserCredential.PERSONAL_EMAILS).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithoutAuthorizationHeader() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId)
				.queryParam("metadataCode", Folder.TITLE).request()
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
				.queryParam("id", folderId)
				.queryParam("metadataCode", Folder.TITLE).request()
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
				.queryParam("id", folderId)
				.queryParam("metadataCode", Folder.TITLE).request()
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
				.queryParam("id", folderId)
				.queryParam("metadataCode", Folder.TITLE).request()
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
				.queryParam("id", folderId)
				.queryParam("metadataCode", Folder.TITLE).request()
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
				.queryParam("id", folderId)
				.queryParam("metadataCode", Folder.TITLE).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(fakeToken)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithoutServiceKeyParam() {
		Response response = webTarget.queryParam("id", folderId)
				.queryParam("metadataCode", Folder.TITLE).request()
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
				.queryParam("id", folderId)
				.queryParam("metadataCode", Folder.TITLE).request()
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
				.queryParam("id", folderId)
				.queryParam("metadataCode", Folder.TITLE).request()
				.header(HttpHeaders.HOST, fakeHost).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnallowedHostException(fakeHost).getValidationError()));
	}

	@Test
	public void whenCallingServiceWithMissingId() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("metadataCode", Folder.TITLE).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "id"));
	}

	@Test
	public void whenCallingServiceWithInvalidId() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", "fakeId")
				.queryParam("metadataCode", Folder.TITLE).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new RecordNotFoundException("fakeId").getValidationError()));
	}

	@Test
	public void whenCallingServiceWithMissingCode() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "metadataCode"));
	}

	@Test
	public void whenCallingServiceWithInvalidCode() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId)
				.queryParam("metadataCode", "fakeCode").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new MetadataNotFoundException("fakeCode").getValidationError()));
	}

	@Test
	public void whenCallingServiceWithoutPermission() {
		Record record = recordServices.getDocumentById(folderId);
		User bobUser = userServices.getUserInCollection(bob, record.getCollection());
		authorizationsServices.add(authorizationForUsers(bobUser).on(record).givingNegativeReadWriteAccess());

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId)
				.queryParam("metadataCode", Folder.TITLE).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(1);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithLogicallyDeletedRecord() {
		Record record = recordServices.getDocumentById(folderId);
		recordServices.logicallyDelete(record, User.GOD);

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId)
				.queryParam("metadataCode", Folder.TITLE).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new RecordLogicallyDeletedException(folderId).getValidationError()));
	}

	@Test
	public void validateServiceWithUnsupportedTypeMetadata() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId)
				.queryParam("metadataCode", Folder.MANUAL_DISPOSAL_TYPE).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnsupportedMetadataTypeException(MetadataValueType.ENUM.name()).getValidationError()));
	}

	@Test
	public void validateServiceForRefMetadata() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId)
				.queryParam("metadataCode", Folder.PARENT_FOLDER).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		MetadataDto dto = response.readEntity(MetadataDto.class);
		Folder folder = rm.getFolder(folderId);

		assertThat(dto.getCode()).isEqualTo(Folder.PARENT_FOLDER);
		assertThat(dto.getValues().get(0)).isEqualTo(folder.getParentFolder());
	}

	@Test
	public void validateServiceForDateMetadata() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId)
				.queryParam("metadataCode", Folder.MANUAL_EXPECTED_DEPOSIT_DATE).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		MetadataDto dto = response.readEntity(MetadataDto.class);
		Folder folder = rm.getFolder(folderId);

		assertThat(dto.getCode()).isEqualTo(Folder.MANUAL_EXPECTED_DEPOSIT_DATE);
		assertThat(dto.getValues().get(0)).isEqualTo(folder.getManualExpectedDepositDate().toString(dateFormat));
	}

	@Test
	public void validateServiceForDateTimeMetadata() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId)
				.queryParam("metadataCode", Folder.FORM_CREATED_ON).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		MetadataDto dto = response.readEntity(MetadataDto.class);
		Folder folder = rm.getFolder(folderId);

		assertThat(dto.getCode()).isEqualTo(Folder.FORM_CREATED_ON);
		assertThat(dto.getValues().get(0)).isEqualTo(folder.getFormCreatedOn().toString(dateTimeFormat));
	}

	@Test
	public void validateServiceForNumberMetadata() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId)
				.queryParam("metadataCode", Folder.LINEAR_SIZE).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		MetadataDto dto = response.readEntity(MetadataDto.class);
		Folder folder = rm.getFolder(folderId);

		assertThat(dto.getCode()).isEqualTo(Folder.LINEAR_SIZE);
		assertThat(Double.valueOf(dto.getValues().get(0))).isEqualTo(folder.getLinearSize());
	}

	@Test
	public void validateServiceForBoolMetadata() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId)
				.queryParam("metadataCode", Folder.BORROWED).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		MetadataDto dto = response.readEntity(MetadataDto.class);
		Folder folder = rm.getFolder(folderId);

		assertThat(dto.getCode()).isEqualTo(Folder.BORROWED);
		assertThat(Boolean.valueOf(dto.getValues().get(0))).isEqualTo(folder.getBorrowed());
	}

	@Test
	public void validateServiceForMultivalueMetadata() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId)
				.queryParam("metadataCode", Folder.KEYWORDS).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		MetadataDto dto = response.readEntity(MetadataDto.class);
		Folder folder = rm.getFolder(folderId);

		assertThat(dto.getCode()).isEqualTo(Folder.KEYWORDS);
		assertThat(dto.getValues()).containsAll(folder.getKeywords());
	}

	@Test
	public void validateServiceWithNullValue() throws Exception {
		Folder folder = rm.getFolder(folderId);
		folder.setKeywords(null);
		recordServices.update(folder);
		commitCounter.reset();

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId)
				.queryParam("metadataCode", Folder.KEYWORDS).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		MetadataDto dto = response.readEntity(MetadataDto.class);

		assertThat(dto.getCode()).isEqualTo(Folder.KEYWORDS);
		assertThat(dto.getValues()).isNullOrEmpty();
	}
}