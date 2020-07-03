package com.constellio.app.modules.rm.servlet;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.menu.behaviors.DocumentMenuItemActionBehaviors;
import com.constellio.app.modules.rm.wrappers.SignatureExternalAccessUrl;
import com.constellio.model.entities.records.wrappers.structure.ExternalAccessUrlStatus;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.UUID;

import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessDao.UNAUTHORIZED;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.PARAM_ID;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.PARAM_LANGUAGE;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.PARAM_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;

public class SignatureExternalAccessWebServletGETAcceptanceTest extends ConstellioTest {

	private String validLanguage = "fr";
	private String validAccessId = "validAccessId";
	private SignatureExternalAccessUrl validAccess;

	private String validUrl;
	private String expiredUrl;

	protected WebTarget webTarget;

	private RMTestRecords records = new RMTestRecords(zeCollection);
	private Users users = new Users();

	private RecordServices recordServices;
	private RMSchemasRecordsServices rm;
	private DocumentMenuItemActionBehaviors documentMenuItemActionBehaviors;

	@Before
	public void setUp() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users)
				.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());
		givenConfig(ConstellioEIMConfigs.CONSTELLIO_URL, "http://localhost:7070/constellio/");
		startApplication();

		webTarget = newWebTarget("signatureExternalAccess", new ObjectMapper(), false);

		recordServices = getModelLayerFactory().newRecordServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		documentMenuItemActionBehaviors = new DocumentMenuItemActionBehaviors(zeCollection, getAppLayerFactory());

		validUrl = documentMenuItemActionBehaviors.createExternalSignatureUrl(records.document_A19, "Constellio Test", getTomorrow(), validLanguage, users.adminIn(zeCollection));
		expiredUrl = documentMenuItemActionBehaviors.createExternalSignatureUrl(records.document_A19, "Constellio Test", getYesterday(), validLanguage, users.adminIn(zeCollection));
		validAccess = createAccess();
	}

	@After
	public void tearDown()
			throws Exception {
		stopApplication();
	}

	@Test
	public void validateWebService()
			throws Exception {
		Response response = callWebservice(validUrl);

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
	}

	@Test
	public void whenCallingServiceWithValidAccess()
			throws Exception {
		Response response = callWebservice(validAccessId, validAccess.getToken(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
	}

	@Test
	public void whenCallingServiceWithToClosedAccessStatus()
			throws Exception {

		validAccess.setStatus(ExternalAccessUrlStatus.TO_CLOSE);
		recordServices.update(validAccess);

		Response response = callWebservice(validAccessId, validAccess.getToken(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
	}

	@Test
	public void whenCallingServiceWithMissingId()
			throws Exception {
		Response response = callWebservice("", validAccess.getToken(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(UNAUTHORIZED);
	}

	@Test
	public void whenCallingServiceWitNonExistingId()
			throws Exception {
		Response response = callWebservice("fakeId", validAccess.getToken(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(UNAUTHORIZED);
	}

	@Test
	public void whenCallingServiceWithInvalidId()
			throws Exception {
		Response response = callWebservice(records.document_A19, validAccess.getToken(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(UNAUTHORIZED);
	}

	@Test
	public void whenCallingServiceWithMissingToken()
			throws Exception {
		Response response = callWebservice(validAccessId, "", validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(UNAUTHORIZED);
	}

	@Test
	public void whenCallingServiceWithInvalidToken()
			throws Exception {
		Response response = callWebservice(validAccessId, "fakeToken", validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(UNAUTHORIZED);
	}

	@Test
	public void whenCallingServiceWithClosedAccessStatus()
			throws Exception {
		validAccess.setStatus(ExternalAccessUrlStatus.CLOSED);
		recordServices.update(validAccess);

		Response response = callWebservice(validAccessId, validAccess.getToken(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(UNAUTHORIZED);
	}

	@Test
	public void whenCallingServiceWithExpiredAccessStatus()
			throws Exception {
		validAccess.setStatus(ExternalAccessUrlStatus.EXPIRED);
		recordServices.update(validAccess);

		Response response = callWebservice(validAccessId, validAccess.getToken(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(UNAUTHORIZED);
	}

	@Test
	public void whenCallingServiceWithMissingLanguage()
			throws Exception {
		Response response = callWebservice(validAccessId, validAccess.getToken(), "");

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(UNAUTHORIZED);
	}

	@Test
	public void whenCallingServiceWithExpiredAccess()
			throws Exception {
		Response response = callWebservice(expiredUrl);

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(UNAUTHORIZED);
	}

	private Response callWebservice(String url) {
		int index = url.indexOf("signatureExternalAccess");
		String path = url.substring(index);
		webTarget = newWebTarget(path, new ObjectMapper(), false);

		return webTarget.request().get();
	}

	private Response callWebservice(String id, String token, String language) {
		return webTarget.queryParam(PARAM_ID, id)
				.queryParam(PARAM_TOKEN, token)
				.queryParam(PARAM_LANGUAGE, language).request()
				.get();
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
