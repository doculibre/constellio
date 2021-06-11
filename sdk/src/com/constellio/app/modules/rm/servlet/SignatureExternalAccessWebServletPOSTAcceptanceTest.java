package com.constellio.app.modules.rm.servlet;

import com.constellio.app.api.pdf.signature.config.ESignatureConfigs;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.ExpiredTokenException;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthorizedAccessException;
import com.constellio.app.api.pdf.signature.config.ESignatureConfigs;
import com.constellio.app.modules.restapi.core.exception.AtLeastOneParameterRequiredException;
import com.constellio.app.modules.restapi.core.exception.InvalidAuthenticationException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterCombinationException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterException;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.conf.email.EmailConfigurationsManager;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.emails.SmtpServerTestConfig;
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

import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.HEADER_PARAM_AUTH;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.PARAM_DOCUMENT;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.PARAM_EXPIRATION_DATE;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.PARAM_EXTERNAL_USER_EMAIL;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.PARAM_EXTERNAL_USER_FULLNAME;
import static com.constellio.app.modules.rm.servlet.SignatureExternalAccessWebServlet.PARAM_INTERNAL_USER;
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
	private String misterXEmail = "noreply.doculibre2@gmail.com";

	protected WebTarget webTarget;

	private RMTestRecords records = new RMTestRecords(zeCollection);
	private Users users = new Users();

	private RecordServices recordServices;
	private UserServices userServices;
	private AuthorizationsServices authorizationsServices;
	private RolesManager rolesManager;
	private RMSchemasRecordsServices rm;
	private ContentManager contentManager;
	private EmailConfigurationsManager emailConfigurationsManager;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users)
				.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());

		givenConfig(ESignatureConfigs.SIGNING_KEYSTORE, getTestResourceFile("zipTestFile.7z"));

		webTarget = newWebTarget("signatureExternalAccess", new ObjectMapper(), false);

		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		rolesManager = getModelLayerFactory().getRolesManager();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		contentManager = getModelLayerFactory().getContentManager();
		emailConfigurationsManager = getModelLayerFactory().getEmailConfigurationsManager();

		emailConfigurationsManager.addEmailServerConfiguration(new SmtpServerTestConfig(), zeCollection);

		userServices.execute(users.bobAddUpdateRequest().setServiceKey(bobKey)
				.addAccessToken(bobAuth, TimeProvider.getLocalDateTime().plusYears(1))
				.addAccessToken(expiredAuth, TimeProvider.getLocalDateTime().minusDays(1)));

		rolesManager.addRole(new Role(zeCollection, roleWithPermission, "Role with permission",
				new ArrayList<String>()));
		rolesManager.addRole(new Role(zeCollection, roleWithoutPermission, "Role without permission",
				new ArrayList<String>()));

		Role role = rolesManager.getRole(zeCollection, roleWithPermission);
		role = role.withPermissions(asList(RMPermissionsTo.SEND_SIGNATURE_REQUEST));
		rolesManager.updateRole(role);

		recordServices.update(userServices.getUserRecordInCollection(bobGratton, zeCollection)
				.setUserRoles(asList(roleWithPermission)));
	}

	@Test
	public void validateServiceWithExternalUser() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
	}

	@Test
	public void validateServiceWithInternalUser() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_INTERNAL_USER, users.aliceIn(zeCollection).getId())
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
	}

	@Test
	public void whenCallingServiceWithoutAuthorizationHeader() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.post(null);

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new InvalidAuthenticationException().getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithEmptyAuthorizationHeader() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "")
				.post(null);

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new InvalidAuthenticationException().getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithInvalidSchemeInAuthorizationHeader() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Basic ".concat(bobAuth))
				.post(null);

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new InvalidAuthenticationException().getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithoutSchemeInAuthorizationHeader() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, bobAuth)
				.post(null);

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new InvalidAuthenticationException().getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithExpiredToken() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(expiredAuth))
				.post(null);

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new ExpiredTokenException().getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithInvalidToken() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat("fakeToken"))
				.post(null);

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithoutServiceKeyParam() {
		Response response = webTarget.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RequiredParameterException(PARAM_SERVICE_KEY).getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithEmptyServiceKeyParam() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, "")
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RequiredParameterException(PARAM_SERVICE_KEY).getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithInvalidServiceKeyParam() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, "fakeKey")
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithMissingDocument() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RequiredParameterException(PARAM_DOCUMENT).getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithEmptyDocument() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, "")
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RequiredParameterException(PARAM_DOCUMENT).getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithInvalidDocument() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.folder_A01)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new InvalidParameterException(PARAM_DOCUMENT, records.folder_A01).getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithNonExistingDocument() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, "fakeId")
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RecordNotFoundException("fakeId").getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithDocumentWithoutContent()
			throws Exception {
		Document docWithoutContent = createDocumentWithoutContent();

		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, docWithoutContent.getId())
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new InvalidParameterException(PARAM_DOCUMENT, docWithoutContent.getId()).getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithDocumentWithUnsupportedContent()
			throws Exception {
		Document docWithZipContent = createDocumentWithZipContent();

		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, docWithZipContent.getId())
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new InvalidParameterException(PARAM_DOCUMENT, docWithZipContent.getId()).getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithUserWithoutUrlGenerationPermission()
			throws Exception {
		recordServices.update(userServices.getUserRecordInCollection(bobGratton, zeCollection)
				.setUserRoles(asList(roleWithoutPermission)));

		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithUserWithoutWritePermission() {
		Record record = recordServices.getDocumentById(records.document_A19);
		User bobUser = userServices.getUserInCollection(bob, record.getCollection());
		authorizationsServices.add(authorizationForUsers(bobUser).on(record).givingNegativeReadWriteAccess());

		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithBothUser() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_INTERNAL_USER, users.aliceIn(zeCollection).getId())
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new InvalidParameterCombinationException(PARAM_INTERNAL_USER, PARAM_EXTERNAL_USER_FULLNAME).getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithMissingUser() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new AtLeastOneParameterRequiredException(PARAM_INTERNAL_USER, PARAM_EXTERNAL_USER_FULLNAME).getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithEmptyInternalUser() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_INTERNAL_USER, "")
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new AtLeastOneParameterRequiredException(PARAM_INTERNAL_USER, PARAM_EXTERNAL_USER_FULLNAME).getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithNonExistingInternalUser() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_INTERNAL_USER, "fakeUser")
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RecordNotFoundException("fakeUser").getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithInvalidInternalUser() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_INTERNAL_USER, records.folder_A01)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new InvalidParameterException(PARAM_INTERNAL_USER, records.folder_A01).getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithEmptyExternalUser() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, "")
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new AtLeastOneParameterRequiredException(PARAM_INTERNAL_USER, PARAM_EXTERNAL_USER_FULLNAME).getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithMissingEmail() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RequiredParameterException(PARAM_EXTERNAL_USER_EMAIL).getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithEmptyEmail() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, "")
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RequiredParameterException(PARAM_EXTERNAL_USER_EMAIL).getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithMissingExpirationDate() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RequiredParameterException(PARAM_EXPIRATION_DATE).getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithEmptyExpirationDate() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, "")
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RequiredParameterException(PARAM_EXPIRATION_DATE).getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithInvalidExpirationDate() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, "fakeDate")
				.queryParam(PARAM_LANGUAGE, validLanguage).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new InvalidParameterException(PARAM_EXPIRATION_DATE, "fakeDate").getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithMissingLang() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow()).request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RequiredParameterException(PARAM_LANGUAGE).getValidationError().getValidatorErrorCode()));
	}

	@Test
	public void whenCallingServiceWithEmptyLang() {
		Response response = webTarget.queryParam(PARAM_SERVICE_KEY, bobKey)
				.queryParam(PARAM_DOCUMENT, records.document_A19)
				.queryParam(PARAM_EXTERNAL_USER_FULLNAME, misterXFullname)
				.queryParam(PARAM_EXTERNAL_USER_EMAIL, misterXEmail)
				.queryParam(PARAM_EXPIRATION_DATE, getTomorrow())
				.queryParam(PARAM_LANGUAGE, "").request()
				.header(HEADER_PARAM_AUTH, "Bearer ".concat(bobAuth))
				.post(null);

		String error = response.readEntity(String.class);
		assertThat(error).isEqualTo(i18n.$(new RequiredParameterException(PARAM_LANGUAGE).getValidationError().getValidatorErrorCode()));
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
