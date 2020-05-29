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
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.Arrays;
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

	private RMTestRecords records = new RMTestRecords(zeCollection);
	private Users users = new Users();

	private RecordServices recordServices;
	private RMSchemasRecordsServices rm;
	private DocumentMenuItemActionBehaviors documentMenuItemActionBehaviors;

	@Before
	public void setUp() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users)
				.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());
		startApplication();

		givenConfig(ConstellioEIMConfigs.CONSTELLIO_URL, "http://localhost:7070/constellio/");

		recordServices = getModelLayerFactory().newRecordServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		documentMenuItemActionBehaviors = new DocumentMenuItemActionBehaviors(zeCollection, getAppLayerFactory());

		validUrl = documentMenuItemActionBehaviors.createExternalSignatureUrl(records.document_A19, "Constellio Test", getTomorrow(), validLanguage);
		expiredUrl = documentMenuItemActionBehaviors.createExternalSignatureUrl(records.document_A19, "Constellio Test", getYesterday(), validLanguage);
		validAccess = createAccess();
	}

	@After
	public void tearDown() throws Exception {
		stopApplication();
	}

	/* TODO --> Hadle redirection without crashing test?
	@Test
	public void validateWebService()
			throws Exception {
		WebResponse response = callWebservice(validUrl);

		assertThat(response.getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);
	}*/

	/* TODO --> Hadle redirection without crashing test?
	@Test
	public void whenCallingServiceWithValidAccess()
			throws Exception {
		WebResponse response = callWebservice(validAccessId, validAccess.getToken(), validLanguage);

		assertThat(response.getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);
	}*/

	/* TODO --> Hadle redirection without crashing test?
	@Test
	public void whenCallingServiceWithToClosedAccessStatus()
			throws Exception {

		validAccess.setStatus(ExternalAccessUrlStatus.TO_CLOSE);
		recordServices.update(validAccess);

		WebResponse response = callWebservice(validAccessId, validAccess.getToken(), validLanguage);

		assertThat(response.getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);
	}*/

	@Test
	public void whenCallingServiceWithMissingId()
			throws Exception {
		try {
			callWebservice("", validAccess.getToken(), validLanguage);
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
			assertThat(e.getStatusMessage()).isEqualTo(UNAUTHORIZED);
		}
	}

	@Test
	public void whenCallingServiceWitNonExistingId()
			throws Exception {
		try {
			callWebservice("fakeId", validAccess.getToken(), validLanguage);
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
			assertThat(e.getStatusMessage()).isEqualTo(UNAUTHORIZED);
		}
	}

	@Test
	public void whenCallingServiceWithInvalidId()
			throws Exception {
		try {
			callWebservice(records.document_A19, validAccess.getToken(), validLanguage);
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
			assertThat(e.getStatusMessage()).isEqualTo(UNAUTHORIZED);
		}
	}

	@Test
	public void whenCallingServiceWithMissingToken()
			throws Exception {
		try {
			callWebservice(validAccessId, "", validLanguage);
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
			assertThat(e.getStatusMessage()).isEqualTo(UNAUTHORIZED);
		}
	}

	@Test
	public void whenCallingServiceWithInvalidToken()
			throws Exception {
		try {
			callWebservice(validAccessId, "fakeToken", validLanguage);
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
			assertThat(e.getStatusMessage()).isEqualTo(UNAUTHORIZED);
		}
	}

	@Test
	public void whenCallingServiceWithClosedAccessStatus()
			throws Exception {
		try {
			validAccess.setStatus(ExternalAccessUrlStatus.CLOSED);
			recordServices.update(validAccess);

			callWebservice(validAccessId, validAccess.getToken(), validLanguage);
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
			assertThat(e.getStatusMessage()).isEqualTo(UNAUTHORIZED);
		}
	}

	@Test
	public void whenCallingServiceWithExpiredAccessStatus()
			throws Exception {
		try {
			validAccess.setStatus(ExternalAccessUrlStatus.EXPIRED);
			recordServices.update(validAccess);

			callWebservice(validAccessId, validAccess.getToken(), validLanguage);
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
			assertThat(e.getStatusMessage()).isEqualTo(UNAUTHORIZED);
		}
	}

	@Test
	public void whenCallingServiceWithMissingLanguage()
			throws Exception {
		try {
			callWebservice(validAccessId, validAccess.getToken(), "");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
			assertThat(e.getStatusMessage()).isEqualTo(UNAUTHORIZED);
		}
	}

	@Test
	public void whenCallingServiceWithExpiredAccess()
			throws Exception {
		try {
			callWebservice(expiredUrl);
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
			assertThat(e.getStatusMessage()).isEqualTo(UNAUTHORIZED);
		}
	}

	private WebResponse callWebservice(String url)
			throws Exception {
		WebClient webClient = new WebClient();
		WebRequest webRequest;

		webRequest = new WebRequest(new URL(url));

		Page page = webClient.getPage(webRequest);
		return page.getWebResponse();
	}

	private WebResponse callWebservice(String id, String token, String language)
			throws Exception {
		WebClient webClient = new WebClient();
		WebRequest webRequest;

		String url = "http://localhost:7070/constellio/signatureExternalAccess";

		webRequest = new WebRequest(new URL(url));

		webRequest.setHttpMethod(HttpMethod.GET);
		webRequest.setRequestParameters(Arrays.asList(new NameValuePair(PARAM_ID, id),
				new NameValuePair(PARAM_TOKEN, token), new NameValuePair(PARAM_LANGUAGE, language)));

		Page page = webClient.getPage(webRequest);
		return page.getWebResponse();
	}

	private SignatureExternalAccessUrl createAccess() throws Exception {
		SignatureExternalAccessUrl access = rm.newSignatureExternalAccessUrlWithId(validAccessId);
		access.setToken(UUID.randomUUID().toString());
		access.setAccessRecord(records.document_A19);
		access.setStatus(ExternalAccessUrlStatus.OPEN);
		access.setFullname("Mister X");
		access.setExpirationDate(getTomorrow());

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
