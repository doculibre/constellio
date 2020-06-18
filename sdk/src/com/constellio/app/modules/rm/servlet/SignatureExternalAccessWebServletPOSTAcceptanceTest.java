package com.constellio.app.modules.rm.servlet;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.SignatureExternalAccessUrl;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.users.UserServices;
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
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessDao.ACTION_IMPOSSIBLE;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessDao.INVALID_DATE_PARAM;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessDao.INVALID_DOCUMENT_PARAM;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessDao.MISSING_DATE_PARAM;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessDao.MISSING_DOCUMENT_PARAM;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessDao.MISSING_EXTERNAL_USER_FULLNAME_PARAM;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessDao.MISSING_LANGUAGE_PARAM;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessDao.UNAUTHORIZED;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.HEADER_PARAM_AUTH;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.PARAM_DOCUMENT;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.PARAM_EXPIRATION_DATE;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.PARAM_EXTERNAL_USER_FULLNAME;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.PARAM_LANGUAGE;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.PARAM_SERVICE_KEY;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class SignatureExternalAccessWebServletPOSTAcceptanceTest extends ConstellioTest {

	private String validLanguage = "fr";
	private String bobAuth = "bobAuth";
	private String expiredAuth = "expiredAuth";
	private String bobKey = "bobKey";
	private String roleWithPermission = "roleWithPermission";
	private String roleWithoutPermission = "roleWithoutPermission";
	private String misterXFullname = "Mister X";

	private RMTestRecords records = new RMTestRecords(zeCollection);
	private Users users = new Users();

	private RecordServices recordServices;
	private UserServices userServices;
	private AuthorizationsServices authorizationsServices;
	private RolesManager rolesManager;
	private RMSchemasRecordsServices rm;
	private ContentManager contentManager;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users)
				.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());
		startApplication();

		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		rolesManager = getModelLayerFactory().getRolesManager();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		contentManager = getModelLayerFactory().getContentManager();

		userServices.addUpdateUserCredential(users.bob().setServiceKey(bobKey)
				.addAccessToken(bobAuth, TimeProvider.getLocalDateTime().plusYears(1))
				.addAccessToken(expiredAuth, TimeProvider.getLocalDateTime().minusDays(1)));

		rolesManager.addRole(new Role(zeCollection, roleWithPermission, "Role with permission", new ArrayList<String>()));
		rolesManager.addRole(new Role(zeCollection, roleWithoutPermission, "Role without permission", new ArrayList<String>()));

		Role role = rolesManager.getRole(zeCollection, roleWithPermission);
		role = role.withPermissions(asList(RMPermissionsTo.GENERATE_EXTERNAL_SIGNATURE_URL));
		rolesManager.updateRole(role);

		recordServices.update(userServices.getUserRecordInCollection(bobGratton, zeCollection)
				.setUserRoles(asList(roleWithPermission)));
	}

	@After
	public void tearDown()
			throws Exception {
		stopApplication();
	}

	@Test
	public void validateWebService()
			throws Exception {
		WebResponse response = callWebservice(bobAuth, bobKey, records.document_A19, misterXFullname, getTomorrow(), validLanguage);

		assertThat(response.getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);

		String url = response.getContentAsString();
		String[] urlParts = url.split("\\?");
		String[] paramStrings = urlParts[1].split("&");

		Map<String, String> params = new HashMap<>();
		for (String param : paramStrings) {
			String[] paramParts = param.split("=");
			params.put(paramParts[0], paramParts[1]);
		}

		SignatureExternalAccessUrl signatureAccess = rm.getSignatureExternalAccessUrl(params.get("id"));
		assertThat(signatureAccess.getToken()).isEqualTo(params.get("token"));
	}

	@Test
	public void whenCallingServiceWithMissingAuth()
			throws Exception {
		try {
			callWebservice("", bobKey, records.document_A19, misterXFullname, getTomorrow(), validLanguage);
			fail("whenCallingServiceWithMissingAuth should throw an exception.");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
			assertThat(e.getStatusMessage()).isEqualTo(UNAUTHORIZED);
		}
	}

	@Test
	public void whenCallingServiceWithInvalidAuth()
			throws Exception {
		try {
			callWebservice("fakeToken", bobKey, records.document_A19, misterXFullname, getTomorrow(), validLanguage);
			fail("whenCallingServiceWithInvalidAuth should throw an exception.");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
			assertThat(e.getStatusMessage()).isEqualTo(UNAUTHORIZED);
		}
	}

	@Test
	public void whenCallingServiceWithExpiredAuth()
			throws Exception {
		try {
			callWebservice(expiredAuth, bobKey, records.document_A19, misterXFullname, getTomorrow(), validLanguage);
			fail("whenCallingServiceWithExpiredAuth should throw an exception.");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
			assertThat(e.getStatusMessage()).isEqualTo(UNAUTHORIZED);
		}
	}

	@Test
	public void whenCallingServiceWithMissingServiceKey()
			throws Exception {
		try {
			callWebservice(bobAuth, "", records.document_A19, misterXFullname, getTomorrow(), validLanguage);
			fail("whenCallingServiceWithMissingServiceKey should throw an exception.");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
			assertThat(e.getStatusMessage()).isEqualTo(UNAUTHORIZED);
		}
	}

	@Test
	public void whenCallingServiceWithInvalidServiceKey()
			throws Exception {
		try {
			callWebservice(bobAuth, "fakeKey", records.document_A19, misterXFullname, getTomorrow(), validLanguage);
			fail("whenCallingServiceWithInvalidServiceKey should throw an exception.");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
			assertThat(e.getStatusMessage()).isEqualTo(UNAUTHORIZED);
		}
	}

	@Test
	public void whenCallingServiceWithMissingDocument()
			throws Exception {
		try {
			callWebservice(bobAuth, bobKey, "", misterXFullname, getTomorrow(), validLanguage);
			fail("whenCallingServiceWithMissingDocument should throw an exception.");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
			assertThat(e.getStatusMessage()).isEqualTo(MISSING_DOCUMENT_PARAM);
		}
	}

	@Test
	public void whenCallingServiceWithNonExistingDocument()
			throws Exception {
		try {
			callWebservice(bobAuth, bobKey, "fakeDocument", misterXFullname, getTomorrow(), validLanguage);
			fail("whenCallingServiceWithNonExistingDocument should throw an exception.");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
			assertThat(e.getStatusMessage()).isEqualTo(INVALID_DOCUMENT_PARAM);
		}
	}

	@Test
	public void whenCallingServiceWithUserWithoutUrlGenerationPermission()
			throws Exception {
		try {
			recordServices.update(userServices.getUserRecordInCollection(bobGratton, zeCollection)
					.setUserRoles(asList(roleWithoutPermission)));

			callWebservice(bobAuth, bobKey, records.document_A19, misterXFullname, getTomorrow(), validLanguage);
			fail("whenCallingServiceWithUserWithoutUrlGenerationPermission should throw an exception.");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
			assertThat(e.getStatusMessage()).isEqualTo(UNAUTHORIZED);
		}
	}

	@Test
	public void whenCallingServiceWithUserWithoutWritePermission()
			throws Exception {
		try {
			Record record = recordServices.getDocumentById(records.document_A19);
			User bobUser = userServices.getUserInCollection(bob, record.getCollection());
			authorizationsServices.add(authorizationForUsers(bobUser).on(record).givingNegativeReadWriteAccess());

			callWebservice(bobAuth, bobKey, records.document_A19, misterXFullname, getTomorrow(), validLanguage);
			fail("whenCallingServiceWithUserWithoutWritePermission should throw an exception.");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
			assertThat(e.getStatusMessage()).isEqualTo(UNAUTHORIZED);
		}
	}

	@Test
	public void whenCallingServiceWithInvalidDocument()
			throws Exception {
		try {
			callWebservice(bobAuth, bobKey, records.folder_A01, misterXFullname, getTomorrow(), validLanguage);
			fail("whenCallingServiceWithInvalidDocument should throw an exception.");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
			assertThat(e.getStatusMessage()).isEqualTo(INVALID_DOCUMENT_PARAM);
		}
	}

	@Test
	public void whenCallingServiceWithDocumentWithoutContent()
			throws Exception {
		try {
			Document docWithoutContent = createDocumentWithoutContent();
			callWebservice(bobAuth, bobKey, docWithoutContent.getId(), misterXFullname, getTomorrow(), validLanguage);
			fail("whenCallingServiceWithDocumentWithoutContent should throw an exception.");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
			assertThat(e.getStatusMessage()).isEqualTo(ACTION_IMPOSSIBLE);
		}
	}

	@Test
	public void whenCallingServiceWithDocumentWithUnsupportedContent()
			throws Exception {
		try {
			Document docWithZipContent = createDocumentWithZipContent();
			callWebservice(bobAuth, bobKey, docWithZipContent.getId(), misterXFullname, getTomorrow(), validLanguage);
			fail("whenCallingServiceWithDocumentWithUnsupportedContent should throw an exception.");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
			assertThat(e.getStatusMessage()).isEqualTo(ACTION_IMPOSSIBLE);
		}
	}

	@Test
	public void whenCallingServiceWithMissingExternalUsername()
			throws Exception {
		try {
			callWebservice(bobAuth, bobKey, records.document_A19, "", getTomorrow(), validLanguage);
			fail("whenCallingServiceWithMissingExternalUsername should throw an exception.");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
			assertThat(e.getStatusMessage()).isEqualTo(MISSING_EXTERNAL_USER_FULLNAME_PARAM);
		}
	}

	@Test
	public void whenCallingServiceWithMissingExpirationDate()
			throws Exception {
		try {
			callWebservice(bobAuth, bobKey, records.document_A19, misterXFullname, "", validLanguage);
			fail("whenCallingServiceWithMissingExpirationDate should throw an exception.");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
			assertThat(e.getStatusMessage()).isEqualTo(MISSING_DATE_PARAM);
		}
	}

	@Test
	public void whenCallingServiceWithInvalidExpirationDate()
			throws Exception {
		try {
			callWebservice(bobAuth, bobKey, records.document_A19, misterXFullname, "fakeDate", validLanguage);
			fail("whenCallingServiceWithInvalidExpirationDate should throw an exception.");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
			assertThat(e.getStatusMessage()).isEqualTo(INVALID_DATE_PARAM);
		}
	}

	@Test
	public void whenCallingServiceWithMissignLanguage()
			throws Exception {
		try {
			callWebservice(bobAuth, bobKey, records.document_A19, misterXFullname, getTomorrow(), "");
			fail("whenCallingServiceWithMissignLanguage should throw an exception.");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
			assertThat(e.getStatusMessage()).isEqualTo(MISSING_LANGUAGE_PARAM);
		}
	}

	private WebResponse callWebservice(String authToken, String serviceKey, String document,
									   String externalUserFullname, String expirationDate, String language)
			throws Exception {
		WebClient webClient = new WebClient();
		WebRequest webRequest;

		String url = "http://localhost:7070/constellio/signatureExternalAccess";

		webRequest = new WebRequest(new URL(url));

		webRequest.setHttpMethod(HttpMethod.POST);
		webRequest.setAdditionalHeader(HEADER_PARAM_AUTH, "Bearer " + authToken);
		webRequest.setRequestParameters(Arrays.asList(new NameValuePair(PARAM_SERVICE_KEY, serviceKey),
				new NameValuePair(PARAM_DOCUMENT, document),
				new NameValuePair(PARAM_EXTERNAL_USER_FULLNAME, externalUserFullname),
				new NameValuePair(PARAM_EXPIRATION_DATE, expirationDate),
				new NameValuePair(PARAM_LANGUAGE, language)));

		Page page = webClient.getPage(webRequest);
		return page.getWebResponse();
	}

	private Document createDocumentWithoutContent() throws Exception {
		Document document = rm.newDocument()
				.setFolder(records.folder_A20)
				.setTitle("Document without content");

		recordServices.add(document);
		return document;
	}

	private Document createDocumentWithZipContent() throws Exception {
		String filename = "zipTestFile.7z";

		Document document = rm.newDocument()
				.setFolder(records.folder_A20)
				.setTitle("Document with zip content");

		File file = getTestResourceFile(filename);
		ContentVersionDataSummary versionDataSummary = contentManager.upload(file);

		Content content = contentManager.createMajor(users.gandalfLeblancIn(zeCollection), file.getName(), versionDataSummary);
		document.setContent(content);

		recordServices.add(document);
		return document;
	}

	private String getTomorrow() {
		return LocalDate.now().plusDays(1).toString();
	}
}
