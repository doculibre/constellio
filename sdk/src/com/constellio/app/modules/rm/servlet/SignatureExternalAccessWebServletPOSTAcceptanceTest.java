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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessDao.ACTION_IMPOSSIBLE;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessDao.DOCUMENT_NOT_FOUND;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessDao.FORBIDDEN;
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

public class SignatureExternalAccessWebServletPOSTAcceptanceTest extends ConstellioTest {

	private String validLanguage = "fr";
	private String bobAuth = "bobAuth";
	private String expiredAuth = "expiredAuth";
	private String bobKey = "bobKey";
	private String roleWithPermission = "roleWithPermission";
	private String roleWithoutPermission = "roleWithoutPermission";
	private String misterXFullname = "Mister X";

	protected WebTarget webTarget;

	private RMTestRecords records = new RMTestRecords(zeCollection);
	private Users users = new Users();

	private RecordServices recordServices;
	private UserServices userServices;
	private AuthorizationsServices authorizationsServices;
	private RolesManager rolesManager;
	private RMSchemasRecordsServices rm;
	private ContentManager contentManager;

	@Before
	public void setUp() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users)
				.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());

		webTarget = newWebTarget("signatureExternalAccess", new ObjectMapper(), false);

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

	@Test
	public void validateService() {
		Response response = callWebservice(bobAuth, bobKey, records.document_A19, misterXFullname, getTomorrow(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		String url = response.readEntity(String.class);
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
		Response response = callWebservice("", bobKey, records.document_A19, misterXFullname, getTomorrow(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(UNAUTHORIZED);
	}

	@Test
	public void whenCallingServiceWithInvalidAuth()
			throws Exception {
		Response response = callWebservice("fakeToken", bobKey, records.document_A19, misterXFullname, getTomorrow(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(UNAUTHORIZED);
	}

	@Test
	public void whenCallingServiceWithExpiredAuth()
			throws Exception {
		Response response = callWebservice(expiredAuth, bobKey, records.document_A19, misterXFullname, getTomorrow(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(UNAUTHORIZED);
	}

	@Test
	public void whenCallingServiceWithMissingServiceKey()
			throws Exception {
		Response response = callWebservice(bobAuth, "", records.document_A19, misterXFullname, getTomorrow(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(UNAUTHORIZED);
	}

	@Test
	public void whenCallingServiceWithInvalidServiceKey()
			throws Exception {
		Response response = callWebservice(bobAuth, "fakeKey", records.document_A19, misterXFullname, getTomorrow(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(UNAUTHORIZED);
	}

	@Test
	public void whenCallingServiceWithMissingDocument()
			throws Exception {
		Response response = callWebservice(bobAuth, bobKey, "", misterXFullname, getTomorrow(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(MISSING_DOCUMENT_PARAM);
	}

	@Test
	public void whenCallingServiceWithNonExistingDocument()
			throws Exception {
		Response response = callWebservice(bobAuth, bobKey, "fakeDocument", misterXFullname, getTomorrow(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(DOCUMENT_NOT_FOUND);
	}

	@Test
	public void whenCallingServiceWithUserWithoutUrlGenerationPermission()
			throws Exception {
		recordServices.update(userServices.getUserRecordInCollection(bobGratton, zeCollection)
				.setUserRoles(asList(roleWithoutPermission)));

		Response response = callWebservice(bobAuth, bobKey, records.document_A19, misterXFullname, getTomorrow(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(FORBIDDEN);
	}

	@Test
	public void whenCallingServiceWithUserWithoutWritePermission()
			throws Exception {
		Record record = recordServices.getDocumentById(records.document_A19);
		User bobUser = userServices.getUserInCollection(bob, record.getCollection());
		authorizationsServices.add(authorizationForUsers(bobUser).on(record).givingNegativeReadWriteAccess());

		Response response = callWebservice(bobAuth, bobKey, records.document_A19, misterXFullname, getTomorrow(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(FORBIDDEN);
	}

	@Test
	public void whenCallingServiceWithInvalidDocument()
			throws Exception {
		Response response = callWebservice(bobAuth, bobKey, records.folder_A01, misterXFullname, getTomorrow(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(INVALID_DOCUMENT_PARAM);
	}

	@Test
	public void whenCallingServiceWithDocumentWithoutContent()
			throws Exception {
		Document docWithoutContent = createDocumentWithoutContent();

		Response response = callWebservice(bobAuth, bobKey, docWithoutContent.getId(), misterXFullname, getTomorrow(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(ACTION_IMPOSSIBLE);
	}

	@Test
	public void whenCallingServiceWithDocumentWithUnsupportedContent()
			throws Exception {
		Document docWithZipContent = createDocumentWithZipContent();

		Response response = callWebservice(bobAuth, bobKey, docWithZipContent.getId(), misterXFullname, getTomorrow(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(ACTION_IMPOSSIBLE);
	}

	@Test
	public void whenCallingServiceWithMissingExternalUsername()
			throws Exception {
		Response response = callWebservice(bobAuth, bobKey, records.document_A19, "", getTomorrow(), validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(MISSING_EXTERNAL_USER_FULLNAME_PARAM);
	}

	@Test
	public void whenCallingServiceWithMissingExpirationDate()
			throws Exception {
		Response response = callWebservice(bobAuth, bobKey, records.document_A19, misterXFullname, "", validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(MISSING_DATE_PARAM);
	}

	@Test
	public void whenCallingServiceWithInvalidExpirationDate()
			throws Exception {
		Response response = callWebservice(bobAuth, bobKey, records.document_A19, misterXFullname, "fakeDate", validLanguage);

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(INVALID_DATE_PARAM);
	}

	@Test
	public void whenCallingServiceWithMissignLanguage()
			throws Exception {
		Response response = callWebservice(bobAuth, bobKey, records.document_A19, misterXFullname, getTomorrow(), "");

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(MISSING_LANGUAGE_PARAM);
	}

	private Response callWebservice(String authToken, String serviceKey, String document, String externalUserFullname,
									String expirationDate, String language) {
		return webTarget.queryParam(PARAM_SERVICE_KEY, serviceKey)
				.queryParam(PARAM_DOCUMENT, document)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, externalUserFullname)
				.queryParam(PARAM_EXPIRATION_DATE, expirationDate)
				.queryParam(PARAM_LANGUAGE, language).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(authToken))
				.post(null);
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
