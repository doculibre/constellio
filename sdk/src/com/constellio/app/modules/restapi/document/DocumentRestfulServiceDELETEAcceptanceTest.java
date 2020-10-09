package com.constellio.app.modules.restapi.document;

import com.constellio.app.modules.restapi.core.exception.InvalidParameterException;
import com.constellio.app.modules.restapi.core.exception.RecordLogicallyDeletedException;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.core.exception.mapper.RestApiErrorResponse;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.HttpMethods;
import com.constellio.app.modules.restapi.validation.exception.ExpiredSignedUrlException;
import com.constellio.app.modules.restapi.validation.exception.InvalidSignatureException;
import com.constellio.app.modules.restapi.validation.exception.UnallowedHostException;
import com.constellio.app.modules.restapi.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.validation.exception.UnauthorizedAccessException;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static com.constellio.app.modules.restapi.core.util.HttpMethods.DELETE;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class DocumentRestfulServiceDELETEAcceptanceTest extends BaseDocumentRestfulServiceAcceptanceTest {

	@Test
	public void testDeleteDocument() throws Exception {
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());

		Record record = recordServices.getDocumentById(id);
		assertThat(record).isNotNull();
	}

	@Test
	public void testDeleteWithNonExistingDocument() throws Exception {
		id = "99999";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(id).getValidationError()));
	}

	@Test
	public void testDeleteDocumentAlreadyDeleted() throws Exception {
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
	public void testDeleteDocumentAlreadyLogicallyDeleted() throws Exception {
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
	public void testDeletePhysicallyDocument() throws Exception {
		physical = "true";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).hasSize(9);
		assertThat(queryCounter.newQueryCalls()).isEqualTo(2);

		try {
			recordServices.getDocumentById(id);
			fail("Record not deleted");
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			// ignore
		}
	}

	@Test
	public void testDeletePhysicallyDocumentAlreadyLogicallyDeleted() throws Exception {
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
	public void testLogicallyDeleteDocument() throws Exception {
		physical = "false";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());

		Record record = recordServices.getDocumentById(id);
		Boolean logicallyDeleted = record.get(Schemas.LOGICALLY_DELETED_STATUS);
		assertThat(logicallyDeleted).isTrue();
	}

	@Test
	public void testLogicallyDeleteDocumentNonExisting() throws Exception {
		id = "9999";
		physical = "false";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(id).getValidationError()));
	}

	@Test
	public void testDeleteLogicallyDocumentAlreadyLogicallyDeleted() throws Exception {
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
	public void testDeleteDocumentWithMissingId() throws Exception {
		Response response = doDeleteQuery("id");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "id"));
	}

	@Test
	public void testDeleteDocumentWithInvalidId() throws Exception {
		id = "fakeId";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(id).getValidationError()));
	}

	@Test
	public void testDeleteDocumentWithMissingServiceKey() throws Exception {
		Response response = doDeleteQuery("serviceKey");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "serviceKey"));
	}

	@Test
	public void testDeleteDocumentWithInvalidServiceKey() throws Exception {
		serviceKey = "fakeServiceKey";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void testDeleteDocumentWithMissingMethod() throws Exception {
		Response response = doDeleteQuery("method");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "method"));
	}

	@Test
	public void testDeleteDocumentWithInvalidMethod() throws Exception {
		method = "fakeMethod";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("method", method).getValidationError()));
	}

	@Test
	public void testDeleteDocumentWithMissingDate() throws Exception {
		Response response = doDeleteQuery("date");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "date"));
	}

	@Test
	public void testDeleteDocumentWithInvalidDate() throws Exception {
		date = "fakeDate";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("date", date).getValidationError()));
	}

	@Test
	public void testDeleteDocumentWithMissingExpiration() throws Exception {
		Response response = doDeleteQuery("expiration");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "expiration"));
	}

	@Test
	public void testDeleteDocumentWithInvalidExpiration() throws Exception {
		expiration = "1.5";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(JERSEY_NOT_FOUND_MESSAGE);
	}

	@Test
	public void testDeleteDocumentWithInvalidDateAndExpiration() throws Exception {
		date = DateUtils.formatIsoNoMillis(TimeProvider.getLocalDateTime().minusDays(365));
		expiration = "1";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ExpiredSignedUrlException().getValidationError()));
	}

	@Test
	public void testDeleteDocumentWithMissingSignature() throws Exception {
		Response response = doDeleteQuery("signature");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "signature"));
	}

	@Test
	public void testDeleteDocumentWithInvalidSignature() throws Exception {
		signature = "fakeSignature";
		Response response = doDeleteQuery(false);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidSignatureException().getValidationError()));
	}

	@Test
	public void testDeleteDocumentWithUserWithoutPermissions() throws Exception {
		serviceKey = sasquatchServiceKey;
		token = sasquatchToken;
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	@Test
	public void testDeleteDocumentWithInvalidPhysical() throws Exception {
		physical = "yes";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("physical", physical).getValidationError()));
	}

	@Test
	public void testDeleteDocumentWithUnallowedHostHeader() throws Exception {
		host = "localhost2:8080";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(new UnallowedHostException(host).getValidationError()));
	}

	@Test
	public void testDeleteDocumentWithAllowedHost() throws Exception {
		host = "localhost2";
		Response response = doDeleteQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
	}

	//
	// PRIVATE FUNCTIONS
	//

	private Response doDeleteQuery(String... excludedParam) throws Exception {
		return doDeleteQuery(true, excludedParam);
	}

	private Response doDeleteQuery(boolean calculateSignature, String... excludedParam) throws Exception {
		method = !HttpMethods.contains(method) ? "fakeMethod" : DELETE;
		return buildQuery(webTarget, calculateSignature, asList("id", "serviceKey", "method", "date", "expiration", "physical", "signature"), excludedParam)
				.request().header("host", host).delete();
	}

}
