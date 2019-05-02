package com.constellio.app.modules.restapi.folder;

import com.constellio.app.modules.restapi.core.exception.InvalidParameterException;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.core.exception.mapper.RestApiErrorResponse;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.folder.dto.FolderDto;
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

public class FolderRestfulServiceGETAcceptanceTest extends BaseFolderRestfulServiceAcceptanceTest {

	@Test
	public void testGetFolder() throws Exception {
		String value1 = "value1";
		List<String> value2 = asList("value2a", "value2b");
		addUsrMetadata(MetadataValueType.STRING, value1, value2);

		Response response = doGetQuery();

		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		Record folderRecord = recordServices.getDocumentById(id);
		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + folderRecord.getVersion() + "\"");

		FolderDto folderDto = response.readEntity(FolderDto.class);

		assertThat(folderDto).isNotNull();
		assertThat(folderDto.getId()).isEqualTo(fakeFolder.getId());
		assertThat(folderDto.getParentFolderId()).isEqualTo(fakeFolder.getParentFolder());
		assertThat(folderDto.getType()).isEqualTo(fakeFolder.getType());
		assertThat(folderDto.getRetentionRule()).isEqualTo(fakeFolder.getRetentionRule());
		assertThat(folderDto.getAdministrativeUnit()).isEqualTo(fakeFolder.getAdministrativeUnitEntered());
		assertThat(folderDto.getMainCopyRule()).isEqualTo(fakeFolder.getMainCopyRule().getId());
		assertThat(folderDto.getCopyStatus()).isEqualTo(fakeFolder.getCopyStatusEntered().getCode());
		assertThat(folderDto.getMediumTypes()).isEqualTo(fakeFolder.getMediumTypes());
		assertThat(folderDto.getMediaType()).isEqualTo(fakeFolder.getMediaType().getCode());
		assertThat(folderDto.getContainer()).isEqualTo(fakeFolder.getContainer());
		assertThat(folderDto.getTitle()).isEqualTo(fakeFolder.getTitle());
		assertThat(folderDto.getDescription()).isEqualTo(fakeFolder.getDescription());
		assertThat(folderDto.getKeywords()).isEqualTo(fakeFolder.getKeywords());
		assertThat(folderDto.getOpeningDate()).isEqualTo(fakeFolder.getOpenDate());
		assertThat(folderDto.getClosingDate()).isEqualTo(fakeFolder.getCloseDate());
		assertThat(folderDto.getActualTransferDate()).isEqualTo(fakeFolder.getActualTransferDate());
		assertThat(folderDto.getActualDepositDate()).isEqualTo(fakeFolder.getActualDepositDate());
		assertThat(folderDto.getActualDestructionDate()).isEqualTo(fakeFolder.getActualDestructionDate());
		assertThat(folderDto.getExpectedTransferDate()).isEqualTo(fakeFolder.getExpectedTransferDate());
		assertThat(folderDto.getExpectedDepositDate()).isEqualTo(fakeFolder.getExpectedDepositDate());
		assertThat(folderDto.getExpectedDestructionDate()).isEqualTo(fakeFolder.getExpectedDestructionDate());

		assertThat(folderDto.getDirectAces()).contains(
				AceDto.builder().principals(toPrincipals(authorization1.getPrincipals())).permissions(newHashSet(authorization1.getRoles())).build(),
				AceDto.builder().principals(toPrincipals(authorization2.getPrincipals())).permissions(newHashSet(authorization2.getRoles())).build());

		assertThat(folderDto.getExtendedAttributes()).containsOnly(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList(value1)).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build());
	}

	@Test
	public void testGetFolderAllFilters() throws Exception {
		Response response = doFilteredGetQuery("parentFolderId", "type", "category", "retentionRule",
				"administrativeUnit", "mainCopyRule", "copyStatus", "mediumTypes", "mediaType", "container",
				"title", "description", "keywords", "openingDate", "closingDate", "actualTransferDate",
				"actualDepositDate", "actualDestructionDate", "expectedTransferDate", "expectedDepositDate",
				"expectedDestructionDate", "directAces", "inheritedAces");
		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());

		assertThat(singletonList(response.readEntity(FolderDto.class))).extracting("id",
				"parentFolderId", "type", "category", "retentionRule",
				"administrativeUnit", "mainCopyRule", "copyStatus", "mediumTypes", "mediaType", "container",
				"title", "description", "keywords", "openingDate", "closingDate", "actualTransferDate",
				"actualDepositDate", "actualDestructionDate", "expectedTransferDate", "expectedDepositDate",
				"expectedDestructionDate", "directAces", "inheritedAces")
				.containsOnly(tuple(id, null, null, null, null, null, null, null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null, null));

		Record folderRecord = recordServices.getDocumentById(id);
		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + folderRecord.getVersion() + "\"");
	}

	@Test
	public void testGetFolderSomeFilters() throws Exception {
		Response response = doFilteredGetQuery("type", "copyStatus", "mediumTypes", "description", "openingDate",
				"actualDepositDate", "expectedTransferDate", "expectedDestructionDate");
		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());

		FolderDto folderDto = response.readEntity(FolderDto.class);
		assertThat(singletonList(folderDto)).extracting("id",
				"parentFolderId", "type", "category", "retentionRule",
				"administrativeUnit", "mainCopyRule", "copyStatus", "mediumTypes", "mediaType", "container",
				"title", "description", "keywords", "openingDate", "closingDate", "actualTransferDate",
				"actualDepositDate", "actualDestructionDate", "expectedTransferDate", "expectedDepositDate",
				"expectedDestructionDate")
				.containsOnly(tuple(id, fakeFolder.getParentFolder(), null, fakeFolder.getCategory(),
						fakeFolder.getRetentionRule(), fakeFolder.getAdministrativeUnitEntered(),
						fakeFolder.getMainCopyRule().getId(), null, null, fakeFolder.getMediaType().getCode(),
						fakeFolder.getContainer(), fakeFolder.getTitle(), null, fakeFolder.getKeywords(), null,
						fakeFolder.getCloseDate(), fakeFolder.getActualTransferDate(), null,
						fakeFolder.getActualDestructionDate(), fakeFolder.getExpectedTransferDate(), null, null));
		assertThat(folderDto.getDirectAces()).containsOnly(
				AceDto.builder().principals(toPrincipals(authorization1.getPrincipals())).permissions(newHashSet(authorization1.getRoles())).build(),
				AceDto.builder().principals(toPrincipals(authorization2.getPrincipals())).permissions(newHashSet(authorization2.getRoles())).build());

		Record folderRecord = recordServices.getDocumentById(id);
		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + folderRecord.getVersion() + "\"");
	}

	@Test
	public void testGetFolderInvalidFilter() throws Exception {
		Response response = doFilteredGetQuery("invalid");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("filter", "invalid").getValidationError()));
	}

	@Test
	public void testGetFolderWithDateUsr() throws Exception {
		final LocalDate value1 = TimeProvider.getLocalDate();
		final LocalDate value2a = TimeProvider.getLocalDate().minusDays(1), value2b = TimeProvider.getLocalDate().plusDays(1);
		addUsrMetadata(MetadataValueType.DATE, value1, asList(value2a, value2b));

		FolderDto folderDto = doGetQueryAndParseResponse();
		assertThat(folderDto.getExtendedAttributes()).containsOnly(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList(value1.toString())).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(asList(value2a.toString(), value2b.toString())).build());
	}

	@Test
	public void testGetFolderWithDateTimeUsr() throws Exception {
		final LocalDateTime value1 = TimeProvider.getLocalDateTime();
		final LocalDateTime value2a = TimeProvider.getLocalDateTime().minusDays(1), value2b = TimeProvider.getLocalDateTime().plusDays(1);
		addUsrMetadata(MetadataValueType.DATE_TIME, value1, asList(value2a, value2b));

		FolderDto folderDto = doGetQueryAndParseResponse();
		assertThat(folderDto.getExtendedAttributes()).containsOnly(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList(toDateString(value1))).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(asList(toDateString(value2a), toDateString(value2b))).build());
	}

	@Test
	public void testGetFolderWithTextUsr() throws Exception {
		final Text value1 = new Text("<b>value1");
		final String value2a = "<i>value2a", value2b = "<html>";
		addUsrMetadata(MetadataValueType.TEXT, value1, asList(value2a, value2b));

		FolderDto folderDto = doGetQueryAndParseResponse();
		assertThat(folderDto.getExtendedAttributes()).containsOnly(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList(String.valueOf(value1))).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(asList(value2a, value2b)).build());
	}

	@Test
	public void testGetFolderWithNumberUsr() throws Exception {
		final double value1 = 1.5, value2a = 2.1, value2b = 2.2;
		addUsrMetadata(MetadataValueType.NUMBER, value1, asList(value2a, value2b));

		FolderDto folderDto = doGetQueryAndParseResponse();
		assertThat(folderDto.getExtendedAttributes()).containsOnly(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList(String.valueOf(value1))).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2)
						.values(asList(String.valueOf(value2a), String.valueOf(value2b))).build());
	}

	@Test
	public void testGetFolderWithBooleanUsr() throws Exception {
		final boolean value1 = true, value2a = false, value2b = true;
		addUsrMetadata(MetadataValueType.BOOLEAN, value1, asList(value2a, value2b));

		FolderDto folderDto = doGetQueryAndParseResponse();
		assertThat(folderDto.getExtendedAttributes()).containsOnly(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList(String.valueOf(value1))).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2)
						.values(asList(String.valueOf(value2a), String.valueOf(value2b))).build());
	}

	@Test
	public void testGetFolderWithReferenceUsr() throws Exception {
		final String value1 = records.getAlice().getId();
		final String value2a = records.getChuckNorris().getId();
		final String value2b = records.getAlice().getId();
		addUsrMetadata(MetadataValueType.REFERENCE, value1, asList(value2a, value2b));

		FolderDto folderDto = doGetQueryAndParseResponse();
		assertThat(folderDto.getExtendedAttributes()).containsOnly(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList(String.valueOf(value1))).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(asList(value2a, value2b)).build());
	}

	@Test
	public void testGetFolderWithMissingId() throws Exception {
		Response response = doGetQuery("id");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, "id"));
	}

	@Test
	public void testGetFolderWithInvalidId() throws Exception {
		id = "fakeId";
		Response response = doGetQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(id).getValidationError()));
	}

	@Test
	public void testGetFolderWithMissingServiceKey() throws Exception {
		Response response = doGetQuery("serviceKey");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, "serviceKey"));
	}

	@Test
	public void testGetFolderWithInvalidServiceKey() throws Exception {
		serviceKey = "fakeServiceKey";
		Response response = doGetQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void testGetFolderWithMissingMethod() throws Exception {
		Response response = doGetQuery("method");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, "method"));
	}

	@Test
	public void testGetFolderWithInvalidMethod() throws Exception {
		method = "fakeMethod";
		Response response = doGetQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("method", method).getValidationError()));
	}

	@Test
	public void testGetFolderWithMissingDate() throws Exception {
		Response response = doGetQuery("date");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, "date"));
	}

	@Test
	public void testGetFolderWithMissingExpiration() throws Exception {
		Response response = doGetQuery("expiration");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, "expiration"));
	}

	@Test
	public void testGetFolderWithInvalidDateAndExpiration() throws Exception {
		date = DateUtils.formatIsoNoMillis(TimeProvider.getLocalDateTime().minusDays(365));
		expiration = "1";
		Response response = doGetQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ExpiredSignedUrlException().getValidationError()));
	}

	@Test
	public void testGetFolderWithMissingSignature() throws Exception {
		Response response = doGetQuery("signature");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, "signature"));
	}

	@Test
	public void testGetFolderWithInvalidSignature() throws Exception {
		Response response = doGetQuery(false);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidSignatureException().getValidationError()));
	}

	@Test
	public void testGetFolderWithUserWithoutPermissions() throws Exception {
		serviceKey = sasquatchServiceKey;
		token = sasquatchToken;
		Response response = doGetQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	@Test
	public void testGetFolderWithUnallowedHostHeader() throws Exception {
		host = "fakedns.com";
		Response response = doGetQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnallowedHostException(host).getValidationError()));
	}

	@Test
	public void testGetFolderWithAllowedHost() throws Exception {
		host = "localhost2";
		Response response = doGetQuery();
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
	}

	//
	// PRIVATE FUNCTIONS
	//

	private <T> void addUsrMetadata(final MetadataValueType type, T value1, T value2) throws Exception {
		addUsrMetadata(fakeFolder.getId(), fakeFolder.getSchemaCode(), type, value1, value2);
	}

	private Response doFilteredGetQuery(Object... filters) throws Exception {
		return buildGetQuery(true).queryParam("filter", filters)
				.request().header("host", host).get();
	}

	private Response doGetQuery(String... excludedParam) throws Exception {
		return doGetQuery(true, excludedParam);
	}

	private Response doGetQuery(boolean calculateSignature, String... excludedParam) throws Exception {
		return buildGetQuery(calculateSignature, excludedParam).request().header("host", host).get();
	}

	private FolderDto doGetQueryAndParseResponse(String... excludedParam) throws Exception {
		return buildGetQuery(true, excludedParam).request()
				.header("host", host).get(FolderDto.class);
	}

	private WebTarget buildGetQuery(boolean calculateSignature, String... excludedParam) throws Exception {
		method = method.equals("fakeMethod") ? "fakeMethod" : GET;
		return buildQuery(getClass(), webTarget, calculateSignature,
				asList("id", "serviceKey", "method", "date", "expiration", "signature"), excludedParam);
	}

}
