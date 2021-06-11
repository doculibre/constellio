package com.constellio.app.modules.restapi.apis.v2.folder;

import com.constellio.app.modules.restapi.apis.v1.validation.exception.ExpiredTokenException;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.apis.v2.BaseRestfulServiceV2AcceptanceTest;
import com.constellio.app.modules.restapi.apis.v2.record.dto.FilterMode;
import com.constellio.app.modules.restapi.apis.v2.record.dto.RecordDtoV2;
import com.constellio.app.modules.restapi.core.exception.InvalidAuthenticationException;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.core.exception.mapper.RestApiErrorResponse;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.schemas.SchemaUtils;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static com.constellio.app.ui.i18n.i18n.$;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

public class FolderRestfulServiceV2AcceptanceTest extends BaseRestfulServiceV2AcceptanceTest {

	private static String recordId;
	private static String authorizationHeaderValue;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		recordId = records.folder_A01;
		authorizationHeaderValue = "Bearer ".concat(token);
	}

	@Test
	public void givenIdThenSummaryRecordReturned() {
		Response response = doGetByIdQuery(recordId, authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
		assertNoSolrQueryAndCommitCalls();

		RecordDtoV2 recordDto = response.readEntity(RecordDtoV2.class);
		assertThat(recordDto.getId()).isEqualTo(recordId);
		assertThat(recordDto.getSchemaType()).isEqualTo(Folder.SCHEMA_TYPE);
		assertThat(recordDto.getMetadatas()).isNotEmpty();

		boolean hasNonSummaryMetadata = recordDto.getMetadatas().keySet().stream()
				.anyMatch(metadataCode -> !SchemaUtils.isSummary(rm.defaultFolderSchema().get(metadataCode)));
		assertThat(hasNonSummaryMetadata).isFalse();

		Record folderRecord = recordServices.getDocumentById(recordId);
		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + folderRecord.getVersion() + "\"");
	}

	@Test
	public void givenIdAndFilterModeAllThenFullRecordReturned() {
		Response response = doGetByIdQuery(recordId, authorizationHeaderValue, FilterMode.ALL);
		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isLessThanOrEqualTo(1);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		RecordDtoV2 recordDto = response.readEntity(RecordDtoV2.class);
		assertThat(recordDto.getId()).isEqualTo(recordId);
		assertThat(recordDto.getSchemaType()).isEqualTo(Folder.SCHEMA_TYPE);
		assertThat(recordDto.getMetadatas()).isNotEmpty();

		boolean hasNonSummaryMetadata = recordDto.getMetadatas().keySet().stream()
				.anyMatch(metadataCode -> !SchemaUtils.isSummary(rm.defaultFolderSchema().get(metadataCode)));
		assertThat(hasNonSummaryMetadata).isTrue();

		Record folderRecord = recordServices.getDocumentById(recordId);
		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + folderRecord.getVersion() + "\"");
	}

	@Test
	public void givenMissingAuthHeaderThenUnauthorizedException() {
		Response response = doGetByIdQuery(recordId, null);
		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
		assertThat(response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Bearer");
		assertNoSolrQueryAndCommitCalls();

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo($(new InvalidAuthenticationException().getValidationError()));
	}

	@Test
	public void givenWrongAuthHeaderThenUnauthorizedException() {
		Response response = doGetByIdQuery(recordId, token);
		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
		assertThat(response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Bearer");
		assertNoSolrQueryAndCommitCalls();

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo($(new InvalidAuthenticationException().getValidationError()));
	}

	@Test
	public void givenExpiredTokenThenException() {
		Response response = doGetByIdQuery(recordId, "Bearer ".concat(expiredToken));
		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
		assertNoSolrQueryAndCommitCalls();

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo($(new ExpiredTokenException().getValidationError()));
	}

	@Test
	public void givenInvalidTokenThenException() {
		Response response = doGetByIdQuery(recordId, "Bearer ".concat(fakeToken));
		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
		assertNoSolrQueryAndCommitCalls();

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo($(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void givenUnexistingIdThenRecordNotFoundExceptionException() {
		Response response = doGetByIdQuery("fakeId", authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(NOT_FOUND.getStatusCode());
		assertNoSolrQueryAndCommitCalls();

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo($(new RecordNotFoundException("fakeId").getValidationError()));
	}

	// TODO etag header tests

	// TODO host header tests

	private Response doGetByIdQuery(String id, String authHeaderValue) {
		return doGetByIdQuery(id, authHeaderValue, null);
	}

	private Response doGetByIdQuery(String id, String authHeaderValue, FilterMode filterMode) {
		WebTarget target = newWebTarget("v2/folders/{id}").resolveTemplate("id", id);
		target = filterMode != null ? target.queryParam("filterMode", filterMode.name()) : target;
		return authHeaderValue != null ?
			   target.request().header(HttpHeaders.AUTHORIZATION, authHeaderValue).get() :
			   target.request().get();
	}
}
