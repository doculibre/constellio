package com.constellio.app.modules.restapi.document;

import com.constellio.app.modules.restapi.core.exception.InvalidParameterException;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.core.exception.mapper.RestApiErrorResponse;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.HashingUtils;
import com.constellio.app.modules.restapi.document.dto.DocumentDto;
import com.constellio.app.modules.restapi.document.exception.DocumentContentNotFoundException;
import com.constellio.app.modules.restapi.resource.dto.AceDto;
import com.constellio.app.modules.restapi.resource.dto.ExtendedAttributeDto;
import com.constellio.app.modules.restapi.validation.exception.ExpiredSignedUrlException;
import com.constellio.app.modules.restapi.validation.exception.InvalidSignatureException;
import com.constellio.app.modules.restapi.validation.exception.UnallowedHostException;
import com.constellio.app.modules.restapi.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.validation.exception.UnauthorizedAccessException;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.utils.MimeTypes;
import org.jdom2.Text;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.constellio.app.modules.restapi.core.util.HttpMethods.GET;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.util.Sets.newHashSet;

public class DocumentRestfulServiceGETAcceptanceTest extends BaseDocumentRestfulServiceAcceptanceTest {

	@Test
	public void testGetDocument() throws Exception {
		String value1 = "value1";
		List<String> value2 = asList("value2a", "value2b");
		addUsrMetadata(MetadataValueType.STRING, value1, value2);

		Response response = buildGetQuery().request().header("host", host).get();

		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		Record documentRecord = recordServices.getDocumentById(id);
		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + documentRecord.getVersion() + "\"");

		DocumentDto documentDto = response.readEntity(DocumentDto.class);

		assertThat(documentDto).isNotNull();
		assertThat(documentDto.getId()).isEqualTo(fakeDocument.getId());
		assertThat(documentDto.getFolderId()).isEqualTo(fakeDocument.getFolder());
		assertThat(documentDto.getType()).isNotNull();
		assertThat(documentDto.getType().getId()).isEqualTo(fakeDocumentType.getId());
		assertThat(documentDto.getType().getCode()).isEqualTo(fakeDocumentType.getCode());
		assertThat(documentDto.getType().getTitle()).isEqualTo(fakeDocumentType.getTitle());
		assertThat(documentDto.getTitle()).isEqualTo(fakeDocument.getTitle());
		assertThat(documentDto.getKeywords()).contains(fakeDocument.getKeywords().toArray(new String[0]));
		assertThat(documentDto.getAuthor()).isEqualTo(fakeDocument.getAuthor());
		assertThat(documentDto.getSubject()).isEqualTo(fakeDocument.getSubject());
		assertThat(documentDto.getOrganization()).isEqualTo(fakeDocument.getCompany());

		assertThat(documentDto.getDirectAces()).contains(
				AceDto.builder().principals(toPrincipals(authorization1.getPrincipals())).permissions(newHashSet(authorization1.getRoles())).build(),
				AceDto.builder().principals(toPrincipals(authorization2.getPrincipals())).permissions(newHashSet(authorization2.getRoles())).build());

		assertThat(documentDto.getExtendedAttributes()).containsOnly(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList(value1)).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build());
	}

	@Test
	public void testGetDocumentAllFilters() throws Exception {
		Response response = buildGetQuery()
				.queryParam("filter", "folderId", "type", "content", "title", "keywords", "author",
						"subject", "organization", "directAces", "inheritedAces", "extendedAttributes")
				.request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());

		assertThat(singletonList(response.readEntity(DocumentDto.class))).extracting("id", "folderId", "type", "content", "title",
				"keywords", "author", "subject", "organization", "directAces", "inheritedAces", "extendedAttributes")
				.containsOnly(tuple(id, null, null, null, null, null, null, null, null, null, null, null));

		Record documentRecord = recordServices.getDocumentById(id);
		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + documentRecord.getVersion() + "\"");
	}

	@Test
	public void testGetDocumentSomeFilters() throws Exception {
		Response response = buildGetQuery()
				.queryParam("filter", "type", "content", "inheritedAces", "extendedAttributes")
				.request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());

		DocumentDto documentDto = response.readEntity(DocumentDto.class);
		assertThat(singletonList(documentDto)).extracting("id", "folderId", "type", "content", "title",
				"keywords", "author", "subject", "organization", "inheritedAces", "extendedAttributes")
				.containsOnly(tuple(id, fakeDocument.getFolder(), null, null, fakeDocument.getTitle(), fakeDocument.getKeywords(),
						fakeDocument.getAuthor(), fakeDocument.getSubject(), fakeDocument.getCompany(), null, null));
		assertThat(documentDto.getDirectAces()).containsOnly(
				AceDto.builder().principals(toPrincipals(authorization1.getPrincipals())).permissions(newHashSet(authorization1.getRoles())).build(),
				AceDto.builder().principals(toPrincipals(authorization2.getPrincipals())).permissions(newHashSet(authorization2.getRoles())).build());

		Record documentRecord = recordServices.getDocumentById(id);
		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + documentRecord.getVersion() + "\"");
	}

	@Test
	public void testGetDocumentInvalidFilter() throws Exception {
		Response response = buildGetQuery().queryParam("filter", "invalid").request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("filter", "invalid").getValidationError()));
	}

	@Test
	public void testGetDocumentWithDateUsr() throws Exception {
		final LocalDate value1 = TimeProvider.getLocalDate();
		final LocalDate value2a = TimeProvider.getLocalDate().minusDays(1), value2b = TimeProvider.getLocalDate().plusDays(1);
		addUsrMetadata(MetadataValueType.DATE, value1, asList(value2a, value2b));

		DocumentDto documentDto = buildGetQuery().request().header("host", host).get(DocumentDto.class);

		assertThat(documentDto.getExtendedAttributes()).containsOnly(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList(value1.toString())).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(asList(value2a.toString(), value2b.toString())).build());
	}

	@Test
	public void testGetDocumentWithDateTimeUsr() throws Exception {
		final LocalDateTime value1 = TimeProvider.getLocalDateTime();
		final LocalDateTime value2a = TimeProvider.getLocalDateTime().minusDays(1), value2b = TimeProvider.getLocalDateTime().plusDays(1);
		addUsrMetadata(MetadataValueType.DATE_TIME, value1, asList(value2a, value2b));

		DocumentDto documentDto = buildGetQuery().request().header("host", host).get(DocumentDto.class);

		assertThat(documentDto.getExtendedAttributes()).containsOnly(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList(toDateString(value1))).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(asList(toDateString(value2a), toDateString(value2b))).build());
	}

	@Test
	public void testGetDocumentWithTextUsr() throws Exception {
		final Text value1 = new Text("<b>value1");
		final String value2a = "<i>value2a", value2b = "<html>";
		addUsrMetadata(MetadataValueType.TEXT, value1, asList(value2a, value2b));

		DocumentDto documentDto = buildGetQuery().request().header("host", host).get(DocumentDto.class);

		assertThat(documentDto.getExtendedAttributes()).containsOnly(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList(String.valueOf(value1))).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(asList(value2a, value2b)).build());
	}

	@Test
	public void testGetDocumentWithNumberUsr() throws Exception {
		final double value1 = 1.5, value2a = 2.1, value2b = 2.2;
		addUsrMetadata(MetadataValueType.NUMBER, value1, asList(value2a, value2b));

		DocumentDto documentDto = buildGetQuery().request().header("host", host).get(DocumentDto.class);

		assertThat(documentDto.getExtendedAttributes()).containsOnly(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList(String.valueOf(value1))).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2)
						.values(asList(String.valueOf(value2a), String.valueOf(value2b))).build());
	}

	@Test
	public void testGetDocumentWithBooleanUsr() throws Exception {
		final boolean value1 = true, value2a = false, value2b = true;
		addUsrMetadata(MetadataValueType.BOOLEAN, value1, asList(value2a, value2b));

		DocumentDto documentDto = buildGetQuery().request().header("host", host).get(DocumentDto.class);

		assertThat(documentDto.getExtendedAttributes()).containsOnly(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList(String.valueOf(value1))).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2)
						.values(asList(String.valueOf(value2a), String.valueOf(value2b))).build());
	}

	@Test
	public void testGetDocumentWithReferenceUsr() throws Exception {
		final String value1 = records.getAlice().getId();
		final String value2a = records.getChuckNorris().getId();
		final String value2b = records.getAlice().getId();
		addUsrMetadata(MetadataValueType.REFERENCE, value1, asList(value2a, value2b));

		DocumentDto documentDto = buildGetQuery().request().header("host", host).get(DocumentDto.class);

		assertThat(documentDto.getExtendedAttributes()).containsOnly(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList(String.valueOf(value1))).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(asList(value2a, value2b)).build());
	}

	@Test
	public void testGetDocumentWithMissingId() throws Exception {
		Response response = buildGetQuery("id").request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "id"));
	}

	@Test
	public void testGetDocumentWithInvalidId() throws Exception {
		id = "fakeId";
		Response response = buildGetQuery().request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(id).getValidationError()));
	}

	@Test
	public void testGetDocumentWithMissingServiceKey() throws Exception {
		Response response = buildGetQuery("serviceKey").request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "serviceKey"));
	}

	@Test
	public void testGetDocumentWithInvalidServiceKey() throws Exception {
		serviceKey = "fakeServiceKey";
		Response response = buildGetQuery().request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void testGetDocumentWithMissingMethod() throws Exception {
		Response response = buildGetQuery("method").request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "method"));
	}

	@Test
	public void testGetDocumentWithInvalidMethod() throws Exception {
		method = "fakeMethod";
		Response response = buildGetQuery().request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("method", method).getValidationError()));
	}

	@Test
	public void testGetDocumentWithMissingDate() throws Exception {
		Response response = buildGetQuery("date").request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "date"));
	}

	@Test
	public void testGetDocumentWithMissingExpiration() throws Exception {
		Response response = buildGetQuery("expiration").request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "expiration"));
	}

	@Test
	public void testGetDocumentWithMissingSignature() throws Exception {
		Response response = buildGetQuery("signature").request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "signature"));
	}

	@Test
	public void testGetDocumentWithInvalidSignature() throws Exception {
		Response response = buildGetQuery(false).request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidSignatureException().getValidationError()));
	}

	@Test
	public void testGetDocumentWithInvalidDateAndExpiration() throws Exception {
		date = DateUtils.formatIsoNoMillis(TimeProvider.getLocalDateTime().minusDays(365));
		expiration = "1";
		Response response = buildGetQuery().request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ExpiredSignedUrlException().getValidationError()));
	}

	@Test
	public void testGetDocumentWithUserWithoutPermissions() throws Exception {
		serviceKey = sasquatchServiceKey;
		token = sasquatchToken;
		Response response = buildGetQuery().request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	@Test
	public void testGetDocumentWithUnallowedHostHeader() throws Exception {
		host = "fakedns.com";
		Response response = buildGetQuery().request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(new UnallowedHostException(host).getValidationError()));
	}

	@Test
	public void testGetDocumentWithAllowedHost() throws Exception {
		host = "localhost2";
		Response response = buildGetQuery().request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
	}

	//
	// GET CONTENT
	//

	@Test
	public void testGetDocumentContentVersion1() throws Exception {
		version = "1.0";
		Response response = buildGetContentQuery().request().header("host", host).get();

		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
		assertThat(response.getMediaType().toString()).contains(expectedMimeType);
		assertThat(response.getHeaderString("Content-Disposition")).isEqualTo("attachment; filename=\"" + fakeFilename + "\"");

		String checksum = HashingUtils.md5(readStreamEntity(response));
		assertThat(checksum).isEqualTo(expectedChecksumV1);
	}

	@Test
	public void testGetDocumentContentVersion2() throws Exception {
		version = "2.0";
		Response response = buildGetContentQuery().request().header("host", host).get();

		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
		assertThat(response.getMediaType().toString()).contains(MimeTypes.MIME_TEXT_PLAIN);
		assertThat(response.getHeaderString("Content-Disposition")).isEqualTo("attachment; filename=\"" + fakeFilename + "\"");

		String checksum = HashingUtils.md5(readStreamEntity(response));
		assertThat(checksum).isEqualTo(expectedChecksumV2);
	}

	@Test
	public void testGetDocumentContentLatestVersion() throws Exception {
		version = "last";
		Response response = buildGetContentQuery().request().header("host", host).get();

		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
		assertThat(response.getMediaType().toString()).contains(expectedMimeType);
		assertThat(response.getHeaderString("Content-Disposition")).isEqualTo("attachment; filename=\"" + fakeFilename + "\"");

		String checksum = HashingUtils.md5(readStreamEntity(response));
		assertThat(checksum).isEqualTo(expectedChecksumV2);
	}

	@Test
	public void testGetDocumentContentWithMissingId() throws Exception {
		version = "1.0";
		Response response = buildGetContentQuery("id").request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "id"));
	}

	@Test
	public void testGetDocumentContentWithInvalidId() throws Exception {
		id = "fakeId";
		version = "1.0";
		Response response = buildGetContentQuery().request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(id).getValidationError()));
	}

	@Test
	public void testGetDocumentContentWithMissingServiceKey() throws Exception {
		version = "1.0";
		Response response = buildGetContentQuery("serviceKey").request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "serviceKey"));
	}

	@Test
	public void testGetDocumentContentWithInvalidServiceKey() throws Exception {
		serviceKey = "fakeServiceKey";
		version = "1.0";
		Response response = buildGetContentQuery().request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void testGetDocumentContentWithMissingMethod() throws Exception {
		version = "1.0";
		Response response = buildGetContentQuery("method").request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "method"));
	}

	@Test
	public void testGetDocumentContentWithInvalidMethod() throws Exception {
		method = "fakeMethod";
		version = "1.0";
		Response response = buildGetContentQuery().request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("method", method).getValidationError()));
	}

	@Test
	public void testGetDocumentContentWithMissingDate() throws Exception {
		version = "1.0";
		Response response = buildGetContentQuery("date").request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "date"));
	}

	@Test
	public void testGetDocumentContentWithMissingExpiration() throws Exception {
		version = "1.0";
		Response response = buildGetContentQuery("expiration").request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "expiration"));
	}

	@Test
	public void testGetDocumentContentWithMissingVersion() throws Exception {
		version = null;
		Response response = buildGetContentQuery("version").request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "version"));
	}

	@Test
	public void testGetDocumentContentWithInvalidVersion() throws Exception {
		version = "3.0";
		Response response = buildGetContentQuery().request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new DocumentContentNotFoundException(id, version).getValidationError()));
	}

	@Test
	public void testGetDocumentContentWithMissingSignature() throws Exception {
		version = "1.0";
		Response response = buildGetContentQuery("signature").request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "signature"));
	}

	@Test
	public void testGetDocumentContentWithInvalidSignature() throws Exception {
		version = "1.0";
		Response response = buildGetContentQuery(false).request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidSignatureException().getValidationError()));
	}

	@Test
	public void testGetDocumentContentWithInvalidDateAndExpiration() throws Exception {
		date = DateUtils.formatIsoNoMillis(TimeProvider.getLocalDateTime().minusDays(365));
		expiration = "1";
		version = "1.0";
		Response response = buildGetContentQuery().request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ExpiredSignedUrlException().getValidationError()));
	}

	@Test
	public void testGetDocumentContentWithUserWithoutPermissions() throws Exception {
		version = "1.0";
		serviceKey = sasquatchServiceKey;
		token = sasquatchToken;
		Response response = buildGetContentQuery().request().header("host", host).get();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	//
	// PRIVATE FUNCTIONS
	//

	private WebTarget buildGetQuery(String... excludedParam) throws Exception {
		return buildGetQuery(true, excludedParam);
	}

	private WebTarget buildGetQuery(boolean calculateSignature, String... excludedParam) throws Exception {
		method = method.equals("fakeMethod") ? "fakeMethod" : GET;
		return buildQuery(webTarget, calculateSignature,
				asList("id", "serviceKey", "method", "date", "expiration", "signature"), excludedParam);
	}

	private WebTarget buildGetContentQuery(String... excludedParam) throws Exception {
		return buildGetContentQuery(true, excludedParam);
	}

	private WebTarget buildGetContentQuery(boolean calculateSignature, String... excludedParam) throws Exception {
		method = method.equals("fakeMethod") ? "fakeMethod" : GET;
		return buildQuery(newWebTarget("v1/documents/content"), calculateSignature,
				asList("id", "serviceKey", "method", "date", "expiration", "version", "signature"), excludedParam);
	}

}
