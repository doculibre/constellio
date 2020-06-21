package com.constellio.app.modules.restapi.record;

import com.constellio.app.modules.restapi.BaseRestfulServiceAcceptanceTest;
import com.constellio.app.modules.restapi.core.exception.InvalidAuthenticationException;
import com.constellio.app.modules.restapi.core.exception.InvalidDateFormatException;
import com.constellio.app.modules.restapi.core.exception.InvalidMetadataValueException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotFoundException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotManualException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotMultivalueException;
import com.constellio.app.modules.restapi.core.exception.MetadataReferenceNotAllowedException;
import com.constellio.app.modules.restapi.core.exception.RecordLogicallyDeletedException;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.core.exception.UnsupportedMetadataTypeException;
import com.constellio.app.modules.restapi.core.exception.mapper.RestApiErrorResponse;
import com.constellio.app.modules.restapi.record.dto.MetadataDto;
import com.constellio.app.modules.restapi.validation.exception.ExpiredTokenException;
import com.constellio.app.modules.restapi.validation.exception.UnallowedHostException;
import com.constellio.app.modules.restapi.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.validation.exception.UnauthorizedAccessException;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
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
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

public class RecordRestfulServicePOSTMetadataAcceptanceTest extends BaseRestfulServiceAcceptanceTest {

	protected SystemConfigurationsManager systemConfigurationsManager;

	private String folderId;
	private MetadataDto stringMetadata, referenceMetadata, dateMetadata, dateTimeMetadata, numberMetadata,
			booleanMetadata, multivalueMetadata, emptyMetadata;

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
		stringMetadata = MetadataDto.builder().code(Folder.TITLE).values(asList("My Title")).build();
		referenceMetadata = MetadataDto.builder().code(Folder.PARENT_FOLDER).values(asList(records.folder_A04)).build();
		dateMetadata = MetadataDto.builder().code(Folder.MANUAL_EXPECTED_DEPOSIT_DATE).values(asList(localDate.toString(dateFormat))).build();
		dateTimeMetadata = MetadataDto.builder().code(Folder.FORM_CREATED_ON).values(asList(localDateTime.toString(dateTimeFormat))).build();
		numberMetadata = MetadataDto.builder().code(Folder.LINEAR_SIZE).values(asList(String.valueOf(33))).build();
		booleanMetadata = MetadataDto.builder().code(Folder.BORROWED).values(asList(String.valueOf(true))).build();
		multivalueMetadata = MetadataDto.builder().code(Folder.KEYWORDS).values(asList("11", "12", "13")).build();
		emptyMetadata = MetadataDto.builder().build();

		Folder folder = rm.getFolder(folderId);
		folder.setTitle("Default title");
		folder.setParentFolder(records.folder_A20);
		folder.setManualExpectedDepositDate(localDate.minusDays(1));
		folder.setFormCreatedOn(localDateTime.minusDays(1));
		folder.setLinearSize(28.0);
		folder.setBorrowed(false);
		folder.setKeywords(asList("F1", "F2", "F3"));
		recordServices.update(folder.getWrappedRecord());

		commitCounter.reset();
		queryCounter.reset();
	}

	@Test
	public void validateService() {
		Folder folder = rm.getFolder(folderId);
		assertThat(folder.getTitle()).isNotEqualTo("My Title");

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(stringMetadata, APPLICATION_JSON_TYPE));

		assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).isNotEmpty();

		folder = rm.getFolder(folderId);
		assertThat(folder.getTitle()).isEqualTo("My Title");
	}

	@Test
	public void whenCallingServiceWithoutAuthorizationHeader() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host)
				.post(entity(stringMetadata, APPLICATION_JSON_TYPE));
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
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "")
				.post(entity(stringMetadata, APPLICATION_JSON_TYPE));
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
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Basic ".concat(token))
				.post(entity(stringMetadata, APPLICATION_JSON_TYPE));
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
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, token)
				.post(entity(stringMetadata, APPLICATION_JSON_TYPE));
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
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(expiredToken))
				.post(entity(stringMetadata, APPLICATION_JSON_TYPE));
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new ExpiredTokenException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithInvalidToken() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(fakeToken))
				.post(entity(stringMetadata, APPLICATION_JSON_TYPE));
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithoutServiceKeyParam() {
		Response response = webTarget.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(stringMetadata, APPLICATION_JSON_TYPE));
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "serviceKey"));
	}

	@Test
	public void whenCallingServiceWithInvalidServiceKeyParam() {
		Response response = webTarget.queryParam("serviceKey", fakeServiceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(stringMetadata, APPLICATION_JSON_TYPE));
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithUnallowedHostHeader() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, fakeHost).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(stringMetadata, APPLICATION_JSON_TYPE));
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
				.post(entity(stringMetadata, APPLICATION_JSON_TYPE));
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "id"));
	}

	@Test
	public void whenCallingServiceWithInvalidId() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", "fakeId").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(stringMetadata, APPLICATION_JSON_TYPE));
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new RecordNotFoundException("fakeId").getValidationError()));
	}

	@Test
	public void whenCallingServiceWithoutPermission() {
		Record record = recordServices.getDocumentById(folderId);
		User bobUser = userServices.getUserInCollection(bob, record.getCollection());
		authorizationsServices.add(authorizationForUsers(bobUser).on(record).givingNegativeReadWriteAccess());

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(stringMetadata, APPLICATION_JSON_TYPE));
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
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
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(stringMetadata, APPLICATION_JSON_TYPE));
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new RecordLogicallyDeletedException(folderId).getValidationError()));
	}

	@Test
	public void whenCallingServiceWithMissingDto() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(null, APPLICATION_JSON_TYPE));
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "metadata"));
	}

	@Test
	public void whenCallingServiceWithEmptyDto() {
		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(emptyMetadata, APPLICATION_JSON_TYPE));
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "metadata.code"));
	}

	@Test
	public void whenCallingServiceWithInvalidCode() {
		emptyMetadata.setCode("fakeCode");

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(emptyMetadata, APPLICATION_JSON_TYPE));
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new MetadataNotFoundException("fakeCode").getValidationError()));
	}

	@Test
	public void validateServiceWithNullValue() {
		Folder folder = rm.getFolder(folderId);
		assertThat(folder.getKeywords()).isNotEmpty();

		multivalueMetadata.setValues(null);

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(multivalueMetadata, APPLICATION_JSON_TYPE));

		assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).isNotEmpty();

		folder = rm.getFolder(folderId);
		assertThat(folder.getKeywords()).isEmpty();
	}

	@Test
	public void validateServiceForRefMetadata() {
		Folder folder = rm.getFolder(folderId);
		assertThat(folder.getParentFolder()).isEqualTo(records.folder_A20);

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(referenceMetadata, APPLICATION_JSON_TYPE));

		assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).isNotEmpty();

		folder = rm.getFolder(folderId);
		assertThat(folder.getParentFolder()).isEqualTo(records.folder_A04);
	}

	@Test
	public void validateServiceForRefMetadataWithInvalidRefValue() {
		referenceMetadata.setValues(asList("fakeId"));

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(referenceMetadata, APPLICATION_JSON_TYPE));

		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new RecordNotFoundException("fakeId").getValidationError()));
	}

	@Test
	public void validateServiceForRefMetadataWithWrongTypeRefValue() {
		referenceMetadata.setValues(asList(records.document_A19));

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(referenceMetadata, APPLICATION_JSON_TYPE));

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		Record record = recordServices.getDocumentById(records.document_A19);
		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new MetadataReferenceNotAllowedException(record.getTypeCode(),
				"folder_default_" + referenceMetadata.getCode()).getValidationError()));
	}

	@Test
	public void validateServiceForDateMetadata() {
		Folder folder = rm.getFolder(folderId);
		assertThat(folder.getManualExpectedDepositDate().toString(dateFormat)).isEqualTo(localDate.minusDays(1).toString(dateFormat));

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(dateMetadata, APPLICATION_JSON_TYPE));

		assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).isNotEmpty();

		folder = rm.getFolder(folderId);
		assertThat(folder.getManualExpectedDepositDate().toString(dateFormat)).isEqualTo(localDate.toString(dateFormat));
	}

	@Test
	public void validateServiceForDateMetadataWithInvalidDateValue() {
		dateMetadata.setValues(asList("fakeDate"));

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(dateMetadata, APPLICATION_JSON_TYPE));

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new InvalidDateFormatException("fakeDate", dateFormat).getValidationError()));
	}

	@Test
	public void validateServiceForDateTimeMetadata() {
		Folder folder = rm.getFolder(folderId);
		assertThat(folder.getFormCreatedOn().toString(dateTimeFormat)).isEqualTo(localDateTime.minusDays(1).toString(dateTimeFormat));

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(dateTimeMetadata, APPLICATION_JSON_TYPE));

		assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).isNotEmpty();

		folder = rm.getFolder(folderId);
		assertThat(folder.getFormCreatedOn().toString(dateTimeFormat)).isEqualTo(localDateTime.toString(dateTimeFormat));
	}

	@Test
	public void validateServiceForDateTimeMetadataWithInvalidDateValue() {
		dateTimeMetadata.setValues(asList("fakeDate"));

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(dateTimeMetadata, APPLICATION_JSON_TYPE));

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new InvalidDateFormatException("fakeDate", dateTimeFormat).getValidationError()));
	}

	@Test
	public void validateServiceForNumberMetadata() {
		Folder folder = rm.getFolder(folderId);
		assertThat(folder.getLinearSize()).isEqualTo(28);

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(numberMetadata, APPLICATION_JSON_TYPE));

		assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).isNotEmpty();

		folder = rm.getFolder(folderId);
		assertThat(folder.getLinearSize()).isEqualTo(33);
	}

	@Test
	public void validateServiceForNumberMetadataWithInvalidNumberValue() {
		numberMetadata.setValues(asList("fakeNumber"));

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(numberMetadata, APPLICATION_JSON_TYPE));

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new InvalidMetadataValueException(MetadataValueType.NUMBER.name(),
				"fakeNumber").getValidationError()));
	}

	@Test
	public void validateServiceForBoolMetadata() {
		Folder folder = rm.getFolder(folderId);
		assertThat(folder.getBorrowed()).isFalse();

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(booleanMetadata, APPLICATION_JSON_TYPE));

		assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).isNotEmpty();

		folder = rm.getFolder(folderId);
		assertThat(folder.getBorrowed()).isTrue();
	}

	@Test
	public void validateServiceForBoolMetadataWithInvalidBoolValue() {
		booleanMetadata.setValues(asList("fakeBoolean"));

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(booleanMetadata, APPLICATION_JSON_TYPE));

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new InvalidMetadataValueException(MetadataValueType.BOOLEAN.name(),
				"fakeBoolean").getValidationError()));
	}

	@Test
	public void validateServiceForSinglevalueMetadataWithMultivalueValue() {
		stringMetadata.setValues(asList("Title 1", "Title 2"));

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(stringMetadata, APPLICATION_JSON_TYPE));

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new MetadataNotMultivalueException(
				"folder_default_" + stringMetadata.getCode()).getValidationError()));
	}

	@Test
	public void validateServiceForMultivalueMetadata() {
		Folder folder = rm.getFolder(folderId);
		assertThat(folder.getKeywords()).containsExactly("F1", "F2", "F3");

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(multivalueMetadata, APPLICATION_JSON_TYPE));

		assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).isNotEmpty();

		folder = rm.getFolder(folderId);
		assertThat(folder.getKeywords()).containsExactly("11", "12", "13");
	}

	@Test
	public void validateServiceWithNonManualMetadata() {
		emptyMetadata.setCode(Folder.CLOSING_DATE);

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(emptyMetadata, APPLICATION_JSON_TYPE));

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new MetadataNotManualException(
				"folder_default_" + emptyMetadata.getCode()).getValidationError()));
	}

	@Test
	public void validateServiceWithUnsupportedTypeMetadata() {
		emptyMetadata.setCode(Folder.MANUAL_DISPOSAL_TYPE);

		Response response = webTarget.queryParam("serviceKey", serviceKey)
				.queryParam("id", folderId).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token))
				.post(entity(emptyMetadata, APPLICATION_JSON_TYPE));

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnsupportedMetadataTypeException(MetadataValueType.ENUM.name()).getValidationError()));
	}
}
