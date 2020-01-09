package com.constellio.app.modules.restapi.folder;

import com.constellio.app.modules.restapi.core.exception.InvalidParameterException;
import com.constellio.app.modules.restapi.core.exception.RecordLogicallyDeletedException;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.core.exception.mapper.RestApiErrorResponse;
import com.constellio.app.modules.restapi.core.util.CustomHttpHeaders;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.HttpMethods;
import com.constellio.app.modules.restapi.folder.dto.AdministrativeUnitDto;
import com.constellio.app.modules.restapi.folder.dto.CategoryDto;
import com.constellio.app.modules.restapi.folder.dto.FolderDto;
import com.constellio.app.modules.restapi.folder.dto.RetentionRuleDto;
import com.constellio.app.modules.restapi.validation.exception.ExpiredSignedUrlException;
import com.constellio.app.modules.restapi.validation.exception.InvalidSignatureException;
import com.constellio.app.modules.restapi.validation.exception.UnallowedHostException;
import com.constellio.app.modules.restapi.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.validation.exception.UnauthorizedAccessException;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static com.constellio.app.modules.restapi.core.util.HttpMethods.DELETE;
import static com.constellio.app.modules.restapi.core.util.HttpMethods.POST;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static java.util.Arrays.asList;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class FolderRestfulServiceDELETEAcceptanceTest extends BaseFolderRestfulServiceAcceptanceTest {
	protected String physical, copySource;

	private FolderDto rootFolder, subFolder1, subFolder2;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		rootFolder = createFolder(records.folder_A04, "rootFolder");
		subFolder1 = createFolder(rootFolder.getId(), "subFolder1");
		subFolder2 = createFolder(rootFolder.getId(), "subFolder2");
	}

	@Test
	public void testDeleteFolder() throws Exception {
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());

		Record record = recordServices.getDocumentById(id);
		assertThat(record).isNotNull();
	}

	@Test
	public void testDeleteFolderWithSubFolder() throws Exception {
		id = rootFolder.getId();
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());

		Record record = recordServices.getDocumentById(rootFolder.getId());
		assertThat(record).isNotNull();

		record = recordServices.getDocumentById(subFolder1.getId());
		assertThat(record).isNotNull();

		record = recordServices.getDocumentById(subFolder2.getId());
		assertThat(record).isNotNull();
	}

	@Test
	public void testDeleteWithNonExistingFolder() throws Exception {
		id = "99999";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(id).getValidationError()));
	}

	@Test
	public void testDeleteFolderAlreadyDeleted() throws Exception {
		Record record = recordServices.getDocumentById(id);
		recordServices.logicallyDelete(record, User.GOD);
		recordServices.physicallyDelete(record, User.GOD);

		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(id).getValidationError()));
	}

	@Test
	public void testDeleteFolderAlreadyLogicallyDeleted() throws Exception {
		physical = "true";
		Record record = recordServices.getDocumentById(id);
		recordServices.logicallyDelete(record, User.GOD);

		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());

		try {
			recordServices.getDocumentById(id);
			fail("Record not deleted");
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			// ignore
		}
	}

	@Test
	public void testDeletePhysicallyFolder() throws Exception {
		physical = "true";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);

		try {
			recordServices.getDocumentById(id);
			fail("Record not deleted");
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			// ignore
		}
	}

	@Test
	public void testDeletePhysicallyFolderAlreadyLogicallyDeleted() throws Exception {
		physical = "true";
		Record record = recordServices.getDocumentById(id);
		recordServices.logicallyDelete(record, User.GOD);

		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());

		try {
			recordServices.getDocumentById(id);
			fail("Record not deleted");
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			// ignore
		}
	}

	@Test
	public void testLogicallyDeleteFolder() throws Exception {
		physical = "false";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());

		Record record = recordServices.getDocumentById(id);
		Boolean logicallyDeleted = record.get(Schemas.LOGICALLY_DELETED_STATUS);
		assertThat(logicallyDeleted).isTrue();
	}

	@Test
	public void testLogicallyDeleteFolderNonExisting() throws Exception {
		id = "99999";
		physical = "false";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(id).getValidationError()));
	}

	@Test
	public void testDeleteLogicallyFolderAlreadyLogicallyDeleted() throws Exception {
		physical = "false";
		Record record = recordServices.getDocumentById(id);
		recordServices.logicallyDelete(record, User.GOD);

		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordLogicallyDeletedException(id).getValidationError()));
	}

	@Test
	public void testDeleteFolderWithMissingId() throws Exception {
		Response response = doDeleteQuery("id");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "id"));
	}

	@Test
	public void testDeleteFolderWithInvalidId() throws Exception {
		id = "fakeId";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(id).getValidationError()));
	}

	@Test
	public void testDeleteFolderWithMissingServiceKey() throws Exception {
		Response response = doDeleteQuery("serviceKey");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "serviceKey"));
	}

	@Test
	public void testDeleteFolderWithInvalidServiceKey() throws Exception {
		serviceKey = "fakeServiceKey";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void testDeleteFolderWithMissingMethod() throws Exception {
		Response response = doDeleteQuery("method");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "method"));
	}

	@Test
	public void testDeleteFolderWithInvalidMethod() throws Exception {
		method = "fakeMethod";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("method", method).getValidationError()));
	}

	@Test
	public void testDeleteFolderWithMissingDate() throws Exception {
		Response response = doDeleteQuery("date");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "date"));
	}

	@Test
	public void testDeleteFolderWithInvalidDate() throws Exception {
		date = "fakeDate";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("date", date).getValidationError()));
	}

	@Test
	public void testDeleteFolderWithMissingExpiration() throws Exception {
		Response response = doDeleteQuery("expiration");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "expiration"));
	}

	@Test
	public void testDeleteFolderWithInvalidExpiration() throws Exception {
		expiration = "1.5";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(JERSEY_NOT_FOUND_MESSAGE);
	}

	@Test
	public void testDeleteFolderWithInvalidDateAndExpiration() throws Exception {
		date = DateUtils.formatIsoNoMillis(TimeProvider.getLocalDateTime().minusDays(365));
		expiration = "1";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ExpiredSignedUrlException().getValidationError()));
	}

	@Test
	public void testDeleteFolderWithMissingSignature() throws Exception {
		Response response = doDeleteQuery("signature");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "signature"));
	}

	@Test
	public void testDeleteFolderWithInvalidSignature() throws Exception {
		signature = "fakeSignature";
		Response response = doDeleteQuery(false);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidSignatureException().getValidationError()));
	}

	@Test
	public void testDeleteFolderWithUserWithoutPermissions() throws Exception {
		serviceKey = sasquatchServiceKey;
		token = sasquatchToken;
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	@Test
	public void testDeleteFolderWithoutSubFolderPermissions() throws Exception {
		Record record = recordServices.getDocumentById(subFolder1.getId());
		User bobUser = userServices.getUserInCollection(bob, record.getCollection());
		authorizationsServices.add(authorizationForUsers(bobUser).on(record).givingNegativeDeleteAccess());

		id = rootFolder.getId();
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	@Test
	public void testDeleteFolderWithInvalidPhysical() throws Exception {
		physical = "yes";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("physical", physical).getValidationError()));
	}

	@Test
	public void testDeleteFolderWithUnallowedHostHeader() throws Exception {
		host = "localhost2:8080";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(new UnallowedHostException(host).getValidationError()));
	}

	@Test
	public void testDeleteFolderWithAllowedHost() throws Exception {
		host = "localhost2";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
	}

	//
	// PRIVATE FUNCTIONS
	//

	private FolderDto createFolder(String parentId, String title) throws Exception {
		FolderDto newfolder = FolderDto.builder()
				.administrativeUnit(AdministrativeUnitDto.builder().id(records.unitId_10a).build())
				.category(CategoryDto.builder().id(records.categoryId_X110).build())
				.retentionRule(RetentionRuleDto.builder().id(records.ruleId_1).build())
				.title(title)
				.copyStatus(CopyType.PRINCIPAL.getCode())
				.openingDate(new LocalDate(2019, 4, 4))
				.parentFolderId(parentId).build();

		folderId = parentId;
		Response response = doPostQuery("NOW", newfolder);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		FolderDto folderDto = response.readEntity(FolderDto.class);
		newfolder.setId(folderDto.getId());

		Record record = recordServices.getDocumentById(newfolder.getId());
		User bobUser = userServices.getUserInCollection(bob, record.getCollection());
		authorizationsServices.add(authorizationForUsers(bobUser).on(record).givingReadWriteDeleteAccess());

		return newfolder;
	}

	private Response doDeleteQuery(String... excludedParam) throws Exception {
		return doDeleteQuery(true, excludedParam);
	}

	private Response doDeleteQuery(boolean calculateSignature, String... excludedParam) throws Exception {
		method = !HttpMethods.contains(method) ? "fakeMethod" : DELETE;
		return buildQuery(webTarget, calculateSignature, asList("id", "serviceKey", "method", "date", "expiration", "physical", "signature"), excludedParam)
				.request().header("host", host).delete();
	}

	private Response doPostQuery(String flushMode, FolderDto folder,
								 String... excludedParam) throws Exception {
		return doPostQuery(flushMode, true, folder, excludedParam);
	}

	private Response doPostQuery(String flushMode, boolean calculateSignature, FolderDto folder,
								 String... excludedParam) throws Exception {
		Invocation.Builder query = buildPostQuery(calculateSignature, excludedParam).request().header("host", host);
		if (flushMode != null) {
			query = query.header(CustomHttpHeaders.FLUSH_MODE, flushMode);
		}
		return query.post(entity(folder, APPLICATION_JSON_TYPE));
	}

	private WebTarget buildPostQuery(boolean calculateSignature, String... excludedParam) throws Exception {
		method = !HttpMethods.contains(method) ? "fakeMethod" : POST;
		return buildQuery(getClass(), webTarget, calculateSignature,
				asList("folderId", "serviceKey", "method", "date", "expiration", "copySource", "signature"), excludedParam);
	}
}
