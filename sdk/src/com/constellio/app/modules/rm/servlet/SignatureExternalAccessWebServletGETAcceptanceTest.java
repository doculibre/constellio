package com.constellio.app.modules.rm.servlet;

import com.constellio.app.modules.restapi.apis.v1.validation.exception.ExpiredSignedUrlException;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthorizedAccessException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterException;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.SignatureExternalAccessUrl;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.entities.records.wrappers.structure.ExternalAccessUrlStatus;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.IntermittentFailureTest;
import com.constellio.sdk.tests.setups.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.UUID;

import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.PARAM_ID;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.PARAM_LANGUAGE;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.PARAM_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;

public class SignatureExternalAccessWebServletGETAcceptanceTest extends ConstellioTest {

	private String validLanguage = "fr";
	private String validAccessId = "validAccessId";
	private SignatureExternalAccessUrl validAccess;

	protected WebTarget webTarget;

	private RMTestRecords records = new RMTestRecords(zeCollection);
	private Users users = new Users();

	private RecordServices recordServices;
	private RMSchemasRecordsServices rm;

	@Before
	public void setUp() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users)
				.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());

		givenConfig(ConstellioEIMConfigs.CONSTELLIO_URL, "http://localhost:7070/constellio/");

		webTarget = newWebTarget("signatureExternalAccess", new ObjectMapper(), false);

		recordServices = getModelLayerFactory().newRecordServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		validAccess = createAccess();
	}

	@Test
	public void validateService() {
		Response response = webTarget.queryParam(PARAM_ID, validAccessId)
				.queryParam(PARAM_TOKEN, validAccess.getToken())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
	}

	@Test
	@IntermittentFailureTest
	public void validateServiceWithToCloseAccessStatus()
			throws Exception {
		validAccess.setStatus(ExternalAccessUrlStatus.TO_CLOSE);
		recordServices.update(validAccess);

		Response response = webTarget.queryParam(PARAM_ID, validAccessId)
				.queryParam(PARAM_TOKEN, validAccess.getToken())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
	}

	@Test
	public void whenCallingServiceWithClosedAccessStatus()
			throws Exception {
		validAccess.setStatus(ExternalAccessUrlStatus.CLOSED);
		recordServices.update(validAccess);

		Response response = webTarget.queryParam(PARAM_ID, validAccessId)
				.queryParam(PARAM_TOKEN, validAccess.getToken())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new ExpiredSignedUrlException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithExpiredAccessStatus()
			throws Exception {
		validAccess.setStatus(ExternalAccessUrlStatus.EXPIRED);
		recordServices.update(validAccess);

		Response response = webTarget.queryParam(PARAM_ID, validAccessId)
				.queryParam(PARAM_TOKEN, validAccess.getToken())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new ExpiredSignedUrlException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithExpiredAccess()
			throws Exception {
		validAccess.setExpirationDate(getYesterday());
		recordServices.update(validAccess);

		Response response = webTarget.queryParam(PARAM_ID, validAccessId)
				.queryParam(PARAM_TOKEN, validAccess.getToken())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new ExpiredSignedUrlException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithMissingId() {
		Response response = webTarget.queryParam(PARAM_TOKEN, validAccess.getToken())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RequiredParameterException(PARAM_ID).getValidationError()));
	}

	@Test
	public void whenCallingServiceWithEmptyId() {
		Response response = webTarget.queryParam(PARAM_ID, "")
				.queryParam(PARAM_TOKEN, validAccess.getToken())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RequiredParameterException(PARAM_ID).getValidationError()));
	}

	@Test
	public void whenCallingServiceWithNonExistingId() {
		Response response = webTarget.queryParam(PARAM_ID, "fakeId")
				.queryParam(PARAM_TOKEN, validAccess.getToken())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RecordNotFoundException("fakeId").getValidationError()));
	}

	@Test
	public void whenCallingServiceWithInvalidId() {
		Response response = webTarget.queryParam(PARAM_ID, records.document_A19)
				.queryParam(PARAM_TOKEN, validAccess.getToken())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new InvalidParameterException(PARAM_ID, records.document_A19).getValidationError()));
	}

	@Test
	public void whenCallingServiceWithMissingToken() {
		Response response = webTarget.queryParam(PARAM_ID, validAccessId)
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RequiredParameterException(PARAM_TOKEN).getValidationError()));
	}

	@Test
	public void whenCallingServiceWithEmptyToken() {
		Response response = webTarget.queryParam(PARAM_ID, validAccessId)
				.queryParam(PARAM_TOKEN, "")
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RequiredParameterException(PARAM_TOKEN).getValidationError()));
	}

	@Test
	public void whenCallingServiceWithInvalidToken() {
		Response response = webTarget.queryParam(PARAM_ID, validAccessId)
				.queryParam(PARAM_TOKEN, "fakeToken")
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	@Test
	public void whenCallingServiceWithMissingLang() {
		Response response = webTarget.queryParam(PARAM_ID, validAccessId)
				.queryParam(PARAM_TOKEN, validAccess.getToken()).request()
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RequiredParameterException(PARAM_LANGUAGE).getValidationError()));
	}

	@Test
	public void whenCallingServiceWithEmptyLang() {
		Response response = webTarget.queryParam(PARAM_ID, validAccessId)
				.queryParam(PARAM_TOKEN, validAccess.getToken())
				.queryParam(PARAM_LANGUAGE, "").request()
				.get();

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RequiredParameterException(PARAM_LANGUAGE).getValidationError()));
	}

	private SignatureExternalAccessUrl createAccess() throws Exception {
		SignatureExternalAccessUrl access = rm.newSignatureExternalAccessUrlWithId(validAccessId);
		access.setToken(UUID.randomUUID().toString());
		access.setAccessRecord(records.document_A19);
		access.setStatus(ExternalAccessUrlStatus.OPEN);
		access.setFullname("Mister X");
		access.setExpirationDate(getTomorrow());
		access.setCreatedBy(users.adminIn(zeCollection));
		access.setCreatedOn(new LocalDateTime());

		recordServices.add(access);
		return access;
	}

	private LocalDate getTomorrow() {
		return LocalDate.now().plusDays(1);
	}

	private LocalDate getYesterday() {
		return LocalDate.now().minusDays(1);
	}
}
