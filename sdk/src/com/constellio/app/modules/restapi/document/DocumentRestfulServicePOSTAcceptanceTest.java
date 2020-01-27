package com.constellio.app.modules.restapi.document;

import com.constellio.app.modules.restapi.core.exception.CannotReadContentException;
import com.constellio.app.modules.restapi.core.exception.InvalidDateCombinationException;
import com.constellio.app.modules.restapi.core.exception.InvalidDateFormatException;
import com.constellio.app.modules.restapi.core.exception.InvalidMetadataValueException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterCombinationException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotFoundException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotMultivalueException;
import com.constellio.app.modules.restapi.core.exception.MetadataReferenceNotAllowedException;
import com.constellio.app.modules.restapi.core.exception.ParametersMustMatchException;
import com.constellio.app.modules.restapi.core.exception.RecordLogicallyDeletedException;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.restapi.core.exception.mapper.RestApiErrorResponse;
import com.constellio.app.modules.restapi.core.util.CustomHttpHeaders;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.HashingUtils;
import com.constellio.app.modules.restapi.core.util.HttpMethods;
import com.constellio.app.modules.restapi.document.dto.ContentDto;
import com.constellio.app.modules.restapi.document.dto.DocumentDto;
import com.constellio.app.modules.restapi.document.dto.DocumentTypeDto;
import com.constellio.app.modules.restapi.resource.dto.AceDto;
import com.constellio.app.modules.restapi.resource.dto.ExtendedAttributeDto;
import com.constellio.app.modules.restapi.resource.exception.ResourceTypeNotFoundException;
import com.constellio.app.modules.restapi.validation.exception.ExpiredSignedUrlException;
import com.constellio.app.modules.restapi.validation.exception.InvalidSignatureException;
import com.constellio.app.modules.restapi.validation.exception.UnallowedHostException;
import com.constellio.app.modules.restapi.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.validation.exception.UnauthorizedAccessException;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.constellio.app.modules.restapi.core.util.HttpMethods.POST;
import static com.constellio.app.modules.restapi.core.util.Permissions.READ;
import static com.constellio.app.modules.restapi.core.util.Permissions.WRITE;
import static com.constellio.app.modules.restapi.document.enumeration.VersionType.MAJOR;
import static com.constellio.app.modules.restapi.document.enumeration.VersionType.MINOR;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static com.constellio.sdk.tests.TestUtils.comparingListAnyOrder;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.util.Closeables.closeQuietly;

public class DocumentRestfulServicePOSTAcceptanceTest extends BaseDocumentRestfulServiceAcceptanceTest {

	private DocumentDto minDocumentToAdd, fullDocumentToAdd;
	private File fileToAdd;

	private DocumentDto firstDocumentToMerge, secondDocumentToMerge;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		minDocumentToAdd = DocumentDto.builder().folderId(records.folder_A20).title("title").build();
		fullDocumentToAdd = DocumentDto.builder().folderId(records.folder_A20).title("title").organization("org")
				.subject("sub").author("aut").keywords(singletonList("document"))
				.content(ContentDto.builder().versionType(MAJOR).filename("content.txt").build())
				.type(DocumentTypeDto.builder().id(records.documentTypeId_1).build())
				.directAces(asList(
						AceDto.builder().principals(singleton(alice)).permissions(newHashSet(READ, WRITE))
								.startDate(toDateString(new LocalDate()))
								.endDate(toDateString(new LocalDate().plusDays(365))).build(),
						AceDto.builder().principals(singleton(chuckNorris)).permissions(singleton(READ)).build()))
				.extendedAttributes(asList(
						ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList("value1")).build(),
						ExtendedAttributeDto.builder().key(fakeMetadata2).values(asList("value2a", "value2b")).build()))
				.build();
		fileToAdd = newTempFileWithContent("content.txt", fakeFileContentV1);

		firstDocumentToMerge = createDocumentWithTextContent("file1.txt", "This is the content of file 1.");
		secondDocumentToMerge = createDocumentWithTextContent("file2.txt", "This is the content of file 2.");
	}

	@Test
	public void testCreateMinimalDocument() throws Exception {
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		//assertThat(commitCounter.newCommitsCall()).isEmpty();

		DocumentDto documentDto = response.readEntity(DocumentDto.class);
		assertThat(documentDto.getId()).isNotNull().isNotEmpty();
		assertThat(documentDto.getTitle()).isEqualTo(minDocumentToAdd.getTitle());
		assertThat(documentDto.getFolderId()).isEqualTo(minDocumentToAdd.getFolderId()).isEqualTo(folderId);

		Record record = recordServices.getDocumentById(documentDto.getId());
		assertThat(record).isNotNull();
		assertThatRecord(record).extracting(Document.TITLE, Document.FOLDER).containsExactly(documentDto.getTitle(), documentDto.getFolderId());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + record.getVersion() + "\"");
	}

	@Test
	public void testCreateFullDocument() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(fullDocumentToAdd, fileToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		//assertThat(commitCounter.newCommitsCall()).hasSize(fullDocumentToAdd.getDirectAces().size());

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getId()).isNotNull().isNotEmpty();
		assertThat(doc.getTitle()).isEqualTo(fullDocumentToAdd.getTitle());
		assertThat(doc.getFolderId()).isEqualTo(fullDocumentToAdd.getFolderId()).isEqualTo(folderId);
		assertThat(doc.getOrganization()).isEqualTo(fullDocumentToAdd.getOrganization());
		assertThat(doc.getSubject()).isEqualTo(fullDocumentToAdd.getSubject());
		assertThat(doc.getKeywords()).isEqualTo(fullDocumentToAdd.getKeywords());
		assertThat(doc.getAuthor()).isEqualTo(fullDocumentToAdd.getAuthor());
		assertThat(doc.getDirectAces()).contains(fullDocumentToAdd.getDirectAces().toArray(new AceDto[0]));
		assertThat(doc.getExtendedAttributes()).isEqualTo(fullDocumentToAdd.getExtendedAttributes());
		assertThat(doc.getType().getId()).isEqualTo(fullDocumentToAdd.getType().getId());
		assertThat(doc.getType().getCode()).isNotNull().isNotEmpty();
		assertThat(doc.getType().getTitle()).isNotNull().isNotEmpty();
		assertThat(doc.getContent().getVersionType()).isEqualTo(fullDocumentToAdd.getContent().getVersionType());
		assertThat(doc.getContent().getFilename()).isEqualTo(fullDocumentToAdd.getContent().getFilename());
		assertThat(doc.getContent().getHash()).isNotNull().isNotEmpty();
		assertThat(doc.getContent().getVersion()).isEqualTo("1.0");

		InputStream stream = contentManager.getContentInputStream(doc.getContent().getHash(), doc.getContent().getFilename());
		assertThat(HashingUtils.md5(IOUtils.toByteArray(stream))).isEqualTo(expectedChecksumV1);
		closeQuietly(stream);

		Record record = recordServices.getDocumentById(doc.getId());
		assertThat(record).isNotNull();
		assertThatRecord(record).extracting(Document.TITLE, Document.FOLDER, Document.KEYWORDS, Document.AUTHOR, Document.COMPANY, Document.SUBJECT, Document.TYPE)
				.containsExactly(doc.getTitle(), doc.getFolderId(), doc.getKeywords(), doc.getAuthor(), doc.getOrganization(), doc.getSubject(), doc.getType().getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(doc.getExtendedAttributes().get(0).getValues());
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(doc.getExtendedAttributes().get(1).getValues());

		assertThat(response.getHeaderString("ETag")).isNull();

		Content content = record.get(rm.document.content());
		assertThat(content.getCurrentVersion().getVersion()).isEqualTo(doc.getContent().getVersion()).isEqualTo("1.0");
		assertThat(content.getCurrentVersion().getFilename()).isEqualTo(doc.getContent().getFilename());
		assertThat(content.getCurrentVersion().getHash()).isEqualTo(doc.getContent().getHash());

		List<Authorization> authorizations = filterInheritedAuthorizations(authorizationsServices.getRecordAuthorizations(record), record.getId());
		assertThat(authorizations).extracting("principals").usingElementComparator(comparingListAnyOrder).containsOnly(
				toPrincipalIds(fullDocumentToAdd.getDirectAces().get(0).getPrincipals()),
				toPrincipalIds(fullDocumentToAdd.getDirectAces().get(1).getPrincipals()));
		assertThat(authorizations).extracting("roles").usingElementComparator(comparingListAnyOrder).containsOnly(
				Lists.newArrayList(fullDocumentToAdd.getDirectAces().get(0).getPermissions()),
				Lists.newArrayList(fullDocumentToAdd.getDirectAces().get(1).getPermissions()));
		assertThat(authorizations).extracting("startDate").containsOnly(
				toLocalDate(fullDocumentToAdd.getDirectAces().get(0).getStartDate()), toLocalDate(fullDocumentToAdd.getDirectAces().get(1).getStartDate()));
		assertThat(authorizations).extracting("endDate").containsOnly(
				toLocalDate(fullDocumentToAdd.getDirectAces().get(0).getEndDate()), toLocalDate(fullDocumentToAdd.getDirectAces().get(1).getEndDate()));
	}

	@Test
	public void testCreateFullDocumentAllFilters() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		Response response = buildPostQuery()
				.queryParam("filter", "folderId", "type", "content", "title", "keywords", "author", "subject",
						"organization", "directAces", "inheritedAces", "extendedAttributes")
				.request().header("host", host).post(entity(buildMultiPart(fullDocumentToAdd, fileToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

		DocumentDto documentDto = response.readEntity(DocumentDto.class);
		assertThat(documentDto.getId()).isNotNull();
		assertThat(singletonList(documentDto)).extracting("folderId", "type", "content", "title",
				"keywords", "author", "subject", "organization", "directAces", "inheritedAces", "extendedAttributes")
				.containsOnly(tuple(null, null, null, null, null, null, null, null, null, null, null));
	}

	@Test
	public void testCreateFullDocumentSomeFilters() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		Response response = buildPostQuery()
				.queryParam("filter", "type", "content", "directAces", "inheritedAces", "extendedAttributes")
				.request().header("host", host).post(entity(buildMultiPart(fullDocumentToAdd, fileToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

		DocumentDto documentDto = response.readEntity(DocumentDto.class);
		assertThat(documentDto.getId()).isNotNull();
		assertThat(singletonList(documentDto)).extracting("folderId", "type", "content", "title",
				"keywords", "author", "subject", "organization", "directAces", "inheritedAces", "extendedAttributes")
				.containsOnly(tuple(fullDocumentToAdd.getFolderId(), null, null, fullDocumentToAdd.getTitle(), fullDocumentToAdd.getKeywords(),
						fullDocumentToAdd.getAuthor(), fullDocumentToAdd.getSubject(), fullDocumentToAdd.getOrganization(), null, null, null));
	}

	@Test
	public void testCreateDocumentInvalidFilter() throws Exception {
		Response response = buildPostQuery().queryParam("filter", "invalid")
				.request().header("host", host).post(entity(buildMultiPart(fullDocumentToAdd, fileToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("filter", "invalid").getValidationError()));
	}

	@Test
	public void testCreateDocumentWithTypeCode() throws Exception {
		Record documentType = recordServices.getDocumentById(records.documentTypeId_1);
		minDocumentToAdd.setType(DocumentTypeDto.builder().code((String) documentType.get(Schemas.CODE)).build());

		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getId()).isNotNull().isNotEmpty();
		assertThat(doc.getTitle()).isEqualTo(minDocumentToAdd.getTitle());
		assertThat(doc.getFolderId()).isEqualTo(minDocumentToAdd.getFolderId()).isEqualTo(folderId);
		assertThat(doc.getType().getId()).isEqualTo(documentType.getId());
		assertThat(doc.getType().getCode()).isEqualTo(minDocumentToAdd.getType().getCode());
		assertThat(doc.getType().getTitle()).isNotNull().isNotEmpty();

		Record record = recordServices.getDocumentById(doc.getId());
		assertThat(record).isNotNull();
		assertThatRecord(record).extracting(Document.TITLE, Document.FOLDER, Document.TYPE)
				.containsExactly(doc.getTitle(), doc.getFolderId(), doc.getType().getId());
	}

	@Test
	public void testCreateDocumentWithMinorVersionType() throws Exception {
		minDocumentToAdd.setContent(ContentDto.builder().versionType(MINOR).filename("minor.txt").build());
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd, fileToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getContent().getVersionType()).isEqualTo(minDocumentToAdd.getContent().getVersionType());
		assertThat(doc.getContent().getFilename()).isEqualTo(minDocumentToAdd.getContent().getFilename());
		assertThat(doc.getContent().getHash()).isNotNull().isNotEmpty();
		assertThat(doc.getContent().getVersion()).isEqualTo("0.1");

		InputStream stream = contentManager.getContentInputStream(doc.getContent().getHash(), doc.getContent().getFilename());
		assertThat(HashingUtils.md5(IOUtils.toByteArray(stream))).isEqualTo(expectedChecksumV1);
		closeQuietly(stream);

		Record record = recordServices.getDocumentById(doc.getId());
		assertThat(record).isNotNull();

		Content content = record.get(rm.document.content());
		assertThat(content.getCurrentVersion().getVersion()).isEqualTo(doc.getContent().getVersion());
		assertThat(content.getCurrentVersion().getFilename()).isEqualTo(doc.getContent().getFilename());
		assertThat(content.getCurrentVersion().getHash()).isEqualTo(doc.getContent().getHash());
	}

	@Test
	public void testCreateDocumentWithDateUsr() throws Exception {
		addUsrMetadata(MetadataValueType.DATE, null, null);

		List<String> value1 = singletonList("2017-07-21"), value2 = asList("2017-07-22", "2018-07-23");
		minDocumentToAdd.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

		DocumentDto doc = response.readEntity(DocumentDto.class);
		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(new LocalDate(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(new LocalDate(value2.get(0)), new LocalDate(value2.get(1))));
	}

	@Test
	public void testCreateDocumentWithDateUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("2017-07-21T00:00:00");
		addUsrMetadata(MetadataValueType.DATE, null, null);

		minDocumentToAdd.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidDateFormatException(value1.get(0), dateFormat).getValidationError()));
	}

	@Test
	public void testCreateDocumentWithDateTimeUsr() throws Exception {
		addUsrMetadata(MetadataValueType.DATE_TIME, null, null);

		List<String> value1 = singletonList(toDateString(new LocalDateTime()));
		List<String> value2 = asList(toDateString(fakeDate), toDateString(fakeDate.plusDays(5)));
		minDocumentToAdd.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

		DocumentDto doc = response.readEntity(DocumentDto.class);
		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(toLocalDateTime(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(toLocalDateTime(value2.get(0)), toLocalDateTime(value2.get(1))));
	}

	@Test
	public void testCreateDocumentWithDateTimeUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("2018-07-21T23:59:59.123-04:00");
		addUsrMetadata(MetadataValueType.DATE_TIME, null, null);

		minDocumentToAdd.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidDateFormatException(value1.get(0), dateTimeFormat).getValidationError()));
	}

	@Test
	public void testCreateDocumentWithNumberUsr() throws Exception {
		addUsrMetadata(MetadataValueType.NUMBER, null, null);

		List<String> value1 = singletonList("123.456"), value2 = asList("2018.24", "2018.25");
		minDocumentToAdd.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

		DocumentDto doc = response.readEntity(DocumentDto.class);
		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(Double.valueOf(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(Double.valueOf(value2.get(0)), Double.valueOf(value2.get(1))));
	}

	@Test
	public void testCreateDocumentWithNumberUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("not-a-number");
		addUsrMetadata(MetadataValueType.NUMBER, null, null);

		minDocumentToAdd.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidMetadataValueException(MetadataValueType.NUMBER.name(), value1.get(0)).getValidationError()));
	}

	@Test
	public void testCreateDocumentWithBooleanUsr() throws Exception {
		addUsrMetadata(MetadataValueType.BOOLEAN, null, null);

		List<String> value1 = singletonList("true"), value2 = asList("true", "false");
		minDocumentToAdd.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

		DocumentDto doc = response.readEntity(DocumentDto.class);
		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(Boolean.valueOf(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(Boolean.valueOf(value2.get(0)), Boolean.valueOf(value2.get(1))));
	}

	@Test
	public void testCreateDocumentWithBooleanUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("null");
		addUsrMetadata(MetadataValueType.BOOLEAN, null, null);

		minDocumentToAdd.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidMetadataValueException(MetadataValueType.BOOLEAN.name(), value1.get(0)).getValidationError()));
	}

	@Test
	public void testCreateDocumentWithTextUsr() throws Exception {
		addUsrMetadata(MetadataValueType.TEXT, null, null);

		List<String> value1 = singletonList("<html>"), value2 = asList("<b>bold", "test@test.com");
		minDocumentToAdd.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

		DocumentDto doc = response.readEntity(DocumentDto.class);
		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(value1);
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(value2);
	}

	@Test
	public void testCreateDocumentWithReferenceUsr() throws Exception {
		addUsrMetadata(MetadataValueType.REFERENCE, null, null);

		List<String> value1 = singletonList(records.getAlice().getId());
		List<String> value2 = asList(records.getChuckNorris().getId(), records.getAlice().getId());
		minDocumentToAdd.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

		DocumentDto doc = response.readEntity(DocumentDto.class);
		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(value1);
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(value2);
	}

	@Test
	public void testCreateDocumentWithReferenceUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("fake id");
		addUsrMetadata(MetadataValueType.REFERENCE, null, null);

		minDocumentToAdd.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(value1.get(0)).getValidationError()));
	}

	@Test
	public void testCreateDocumentWithReferenceUsrAndInvalidSchemaType() throws Exception {
		List<String> value1 = singletonList(records.folder_A18);
		addUsrMetadata(MetadataValueType.REFERENCE, null, null);

		minDocumentToAdd.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataReferenceNotAllowedException(rm.folderSchemaType().getCode(), fakeMetadata1).getValidationError()));
	}

	@Test
	public void testCreateDocumentWithMissingFolderId() throws Exception {
		Response response = buildPostQuery("folderId").request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "folderId"));
	}

	@Test
	public void testCreateDocumentWithInvalidFolderId() throws Exception {
		folderId = "fakeFolderId";
		minDocumentToAdd.setFolderId(folderId);
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(folderId).getValidationError()));
	}

	@Test
	public void testCreateDocumentWithMissingServiceKey() throws Exception {
		Response response = buildPostQuery("serviceKey").request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "serviceKey"));
	}

	@Test
	public void testCreateDocumentWithInvalidServiceKey() throws Exception {
		serviceKey = "fakeServiceKey";
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void testCreateDocumentWithMissingMethod() throws Exception {
		Response response = buildPostQuery("method").request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "method"));
	}

	@Test
	public void testCreateDocumentWithInvalidMethod() throws Exception {
		method = "fakeMethod";
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("method", method).getValidationError()));
	}

	@Test
	public void testCreateDocumentWithMissingDate() throws Exception {
		Response response = buildPostQuery("date").request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "date"));
	}

	@Test
	public void testCreateDocumentWithInvalidDate() throws Exception {
		date = "12345";
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("date", date).getValidationError()));
	}

	@Test
	public void testCreateDocumentWithMissingExpiration() throws Exception {
		Response response = buildPostQuery("expiration").request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "expiration"));
	}

	@Test
	public void testCreateDocumentWithInvalidExpiration() throws Exception {
		expiration = "111111111111111111111111111";
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(JERSEY_NOT_FOUND_MESSAGE);
	}

	@Test
	public void testCreateDocumentWithInvalidDateAndExpiration() throws Exception {
		date = DateUtils.formatIsoNoMillis(TimeProvider.getLocalDateTime().minusDays(365));
		expiration = "1";
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ExpiredSignedUrlException().getValidationError()));
	}

	@Test
	public void testCreateDocumentWithMissingSignature() throws Exception {
		Response response = buildPostQuery("signature").request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "signature"));
	}

	@Test
	public void testCreateDocumentWithInvalidSignature() throws Exception {
		signature = "fakeSignature";
		Response response = buildPostQuery(false).request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidSignatureException().getValidationError()));
	}

	@Test
	public void testCreateDocumentWithUserWithoutPermissions() throws Exception {
		serviceKey = sasquatchServiceKey;
		token = sasquatchToken;
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	@Test
	public void testCreateDocumentWithMissingDocumentFolderId() throws Exception {
		minDocumentToAdd.setFolderId(null);
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "document.folderId"));
	}

	@Test
	public void testCreateDocumentWithInvalidDocumentFolderId() throws Exception {
		minDocumentToAdd.setFolderId(records.folder_A42);
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ParametersMustMatchException("folderId", "document.folderId").getValidationError()));
	}

	@Test
	public void testCreateDocumentWithMissingDocumentTitle() throws Exception {
		minDocumentToAdd.setTitle(null);
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "document.title"));
	}

	@Test
	public void testCreateDocumentWithInvalidTypeId() throws Exception {
		minDocumentToAdd.setType(DocumentTypeDto.builder().id("fake").build());
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ResourceTypeNotFoundException("id", minDocumentToAdd.getType().getId()).getValidationError()));
	}

	@Test
	public void testCreateDocumentWithInvalidTypeCode() throws Exception {
		minDocumentToAdd.setType(DocumentTypeDto.builder().code("fake").build());
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ResourceTypeNotFoundException("code", minDocumentToAdd.getType().getCode()).getValidationError()));
	}

	@Test
	public void testCreateDocumentWithTypeIdAndTypeCode() throws Exception {
		minDocumentToAdd.setType(DocumentTypeDto.builder().id("id").code("code").build());
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterCombinationException("type.id", "type.code").getValidationError()));
	}

	@Test
	public void testCreateDocumentWithContentAndMissingFile() throws Exception {
		minDocumentToAdd.setContent(ContentDto.builder().filename("test.txt").versionType(MINOR).build());
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "file"));
	}

	@Test
	public void testCreateDocumentWithFileAndMissingContent() throws Exception {
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd, fileToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "document.content"));
	}

	@Test
	public void testCreateDocumentWithMissingContentVersionType() throws Exception {
		minDocumentToAdd.setContent(ContentDto.builder().filename("test.txt").build());
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd, fileToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "content.versionType"));
	}

	@Test
	public void testCreateDocumentWithMissingContentFilename() throws Exception {
		minDocumentToAdd.setContent(ContentDto.builder().versionType(MAJOR).build());
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd, fileToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "content.filename"));
	}

	@Test
	public void testCreateDocumentWithMissingAcePrincipals() throws Exception {
		minDocumentToAdd.setDirectAces(singletonList(AceDto.builder().permissions(singleton(READ)).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_EMPTY_MESSAGE, "directAces[0].principals"));
	}

	@Test
	public void testCreateDocumentWithEmptyAcePrincipals() throws Exception {
		minDocumentToAdd.setDirectAces(singletonList(AceDto.builder().principals(Collections.<String>emptySet()).permissions(singleton(READ)).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_EMPTY_MESSAGE, "directAces[0].principals"));
	}

	@Test
	public void testCreateDocumentWithInvalidAcePrincipals() throws Exception {
		minDocumentToAdd.setDirectAces(singletonList(AceDto.builder().principals(singleton("fake")).permissions(singleton(READ)).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException("fake").getValidationError()));
	}

	@Test
	public void testCreateDocumentWithMissingAcePermissions() throws Exception {
		minDocumentToAdd.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice)).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_EMPTY_MESSAGE, "directAces[0].permissions"));
	}

	@Test
	public void testCreateDocumentWithEmptyAcePermissions() throws Exception {
		minDocumentToAdd.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice)).permissions(Collections.<String>emptySet()).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_EMPTY_MESSAGE, "directAces[0].permissions"));
	}

	@Test
	public void testCreateDocumentWithStartDateGreaterThanEndDate() throws Exception {
		String start = toDateString(new LocalDate().plusDays(365));
		String end = toDateString(new LocalDate());
		minDocumentToAdd.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice))
				.permissions(singleton(READ)).startDate(start).endDate(end).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(new InvalidDateCombinationException(start, end).getValidationError()));
	}

	@Test
	public void testCreateDocumentWithStartDateOnly() throws Exception {
		minDocumentToAdd.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice))
				.permissions(singleton(READ)).startDate(toDateString(new LocalDate())).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getDirectAces().get(0).getStartDate()).isEqualTo(minDocumentToAdd.getDirectAces().get(0).getStartDate());
		assertThat(doc.getDirectAces().get(0).getEndDate()).isNull();

		Record record = recordServices.getDocumentById(doc.getId());
		List<Authorization> authorizations = filterInheritedAuthorizations(authorizationsServices.getRecordAuthorizations(record), record.getId());
		assertThat(authorizations).extracting("startDate").containsOnly(toLocalDate(minDocumentToAdd.getDirectAces().get(0).getStartDate()));
		assertThat(authorizations).extracting("endDate").containsNull();
	}

	@Test
	public void testCreateDocumentWithEndDateOnly() throws Exception {
		minDocumentToAdd.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice))
				.permissions(singleton(READ)).endDate(toDateString(new LocalDate())).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(new RequiredParameterException("ace.startDate").getValidationError()));
	}

	@Test
	public void testCreateDocumentWithInvalidAcePermissions() throws Exception {
		minDocumentToAdd.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice)).permissions(singleton("fake")).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("directAces[0].permissions", "fake").getValidationError()));
	}

	@Test
	public void testCreateDocumentWithInvalidExtAttributeKey() throws Exception {
		minDocumentToAdd.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key("fake").values(singletonList("123")).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataNotFoundException("fake").getValidationError()));
	}

	@Test
	public void testCreateDocumentWithInvalidExtAttributeMultiValue() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		minDocumentToAdd.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(asList("ab", "cd")).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataNotMultivalueException(fakeMetadata1).getValidationError()));
	}

	@Test
	public void testCreateDocumentWithEmptyExtAttributeValues() throws Exception {
		minDocumentToAdd.setExtendedAttributes(singletonList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(Collections.<String>emptyList()).build()));
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_EMPTY_MESSAGE, "extendedAttributes[0].values"));
	}

	@Test
	public void testCreateDocumentWithCustomSchema() throws Exception {
		minDocumentToAdd.setType(DocumentTypeDto.builder().id(records.documentTypeForm().getId()).build());
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getId()).isNotNull().isNotEmpty();
		assertThat(doc.getTitle()).isEqualTo(minDocumentToAdd.getTitle());
		assertThat(doc.getFolderId()).isEqualTo(minDocumentToAdd.getFolderId()).isEqualTo(folderId);
		assertThat(doc.getType().getId()).isEqualTo(minDocumentToAdd.getType().getId());
		assertThat(doc.getType().getCode()).isEqualTo(records.documentTypeForm().getCode());
		assertThat(doc.getType().getTitle()).isEqualTo(records.documentTypeForm().getTitle());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThat(record).isNotNull();
		assertThatRecord(record).extracting(Document.TITLE, Document.FOLDER, Document.TYPE)
				.containsExactly(doc.getTitle(), doc.getFolderId(), doc.getType().getId());
	}

	@Test
	public void testCreateDocumentDefaultFlushMode() throws Exception {
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		//assertThat(commitCounter.newCommitsCall()).isEmpty();

		DocumentDto newDocument = response.readEntity(DocumentDto.class);

		List<Record> documents = searchServices.search(new LogicalSearchQuery(
				from(rm.document.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(newDocument.getId())));
		assertThat(documents).hasSize(0);

		Record document = recordServices.realtimeGetRecordById(newDocument.getId());
		assertThat(document).isNotNull();

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + document.getVersion() + "\"");

		TimeUnit.MILLISECONDS.sleep(5250);

		documents = searchServices.search(new LogicalSearchQuery(
				from(rm.document.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(newDocument.getId())));
		assertThat(documents).hasSize(1);

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + documents.get(0).getVersion() + "\"");
	}

	@Test
	public void testCreateDocumentNowFlushMode() throws Exception {
		Response response = doPostQuery("NOW", minDocumentToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).isNotEmpty();

		DocumentDto newDocument = response.readEntity(DocumentDto.class);
		List<Record> documents = searchServices.search(new LogicalSearchQuery(
				from(rm.document.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(newDocument.getId())));
		assertThat(documents).hasSize(1);

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + documents.get(0).getVersion() + "\"");
	}

	@Test
	public void testCreateDocumentLaterFlushMode() throws Exception {
		Response response = doPostQuery("LATER", minDocumentToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		//assertThat(commitCounter.newCommitsCall()).isEmpty();

		DocumentDto newDocument = response.readEntity(DocumentDto.class);

		Record documentRecord = recordServices.getDocumentById(newDocument.getId());
		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + documentRecord.getVersion() + "\"");

		minDocumentToAdd.setId(newDocument.getId());
		minDocumentToAdd.setTitle("title2");
		id = newDocument.getId();
		response = doPutQuery("NOW", minDocumentToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		//assertThat(commitCounter.newCommitsCall()).hasSize(1);

		List<Record> documents = searchServices.search(new LogicalSearchQuery(from(rm.document.schemaType())
				.where(Schemas.IDENTIFIER).isEqualTo(id)));
		assertThat(documents.get(0).getTitle()).isEqualTo("title2");

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + documents.get(0).getVersion() + "\"");
	}

	@Test
	public void testCreateDocumentWithUnallowedHostHeader() throws Exception {
		host = "fakedns.com";
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(new UnallowedHostException(host).getValidationError()));
	}

	@Test
	public void testCreateDocumentWithAllowedHost() throws Exception {
		host = "localhost2";
		Response response = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
	}

	@Test
	public void testCreateDocumentAndGetDocument() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		minDocumentToAdd.setContent(ContentDto.builder().versionType(MAJOR).filename("content.txt").build());
		Response postResponse = buildPostQuery().request().header("host", host)
				.header(CustomHttpHeaders.FLUSH_MODE, "LATER")
				.post(entity(buildMultiPart(minDocumentToAdd, fileToAdd), MULTIPART_FORM_DATA_TYPE));

		assertThat(postResponse.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		//assertThat(commitCounter.newCommitsCall()).isEmpty();

		DocumentDto postDocument = postResponse.readEntity(DocumentDto.class);

		Response getResponse = doGetQuery(postDocument.getId());

		DocumentDto getDocument = getResponse.readEntity(DocumentDto.class);

		assertThat(getDocument.getContent()).isNotNull();
		assertThat(getDocument.getContent().getHash()).isNotNull();
	}

	@Test
	public void testCreateDocumentAndPatchDocumentAndGetDocument() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		Response postResponse = buildPostQuery().request().header("host", host)
				.header(CustomHttpHeaders.FLUSH_MODE, "LATER")
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));

		assertThat(postResponse.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		//assertThat(commitCounter.newCommitsCall()).isEmpty();

		DocumentDto postDocument = postResponse.readEntity(DocumentDto.class);

		DocumentDto documentToPatch = DocumentDto.builder().id(postDocument.getId())
				.content(ContentDto.builder().versionType(MAJOR).filename("content.txt").build())
				.build();
		Response patchResponse = doPatchQuery("LATER", documentToPatch, fileToAdd);

		assertThat(patchResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		//assertThat(commitCounter.newCommitsCall()).isEmpty();

		Response getResponse = doGetQuery(postDocument.getId());

		DocumentDto getDocument = getResponse.readEntity(DocumentDto.class);

		assertThat(getDocument.getContent()).isNotNull();
		assertThat(getDocument.getContent().getHash()).isNotNull();
	}

	@Test
	public void testCreateDocumentWithCalculatedUsr() throws Exception {
		addUserCalculatedMetadata(Document.DEFAULT_SCHEMA);

		Response postResponse = buildPostQuery().request().header("host", host)
				.post(entity(buildMultiPart(minDocumentToAdd), MULTIPART_FORM_DATA_TYPE));

		assertThat(postResponse.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
	}

	//
	// MERGE
	//

	@Test
	public void testCreateConsolidatedDocument() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		String mergeSourceIds = firstDocumentToMerge.getId() + "," +
								secondDocumentToMerge.getId();

		Response postResponse = buildPostQuery().request().header("host", host)
				.header(CustomHttpHeaders.MERGE_SOURCE, mergeSourceIds)
				.post(entity(buildMultiPart(minDocumentToAdd, null), MULTIPART_FORM_DATA_TYPE));
		DocumentDto result = postResponse.readEntity(DocumentDto.class);

		assertThat(postResponse.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		assertThat(result.getContent()).isNotNull();
	}

	@Test
	public void testCreateConsolidatedDocumentWithNoDocument() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		String mergeSourceIds = "";

		Response postResponse = buildPostQuery().request().header("host", host)
				.header(CustomHttpHeaders.MERGE_SOURCE, mergeSourceIds)
				.post(entity(buildMultiPart(minDocumentToAdd, null), MULTIPART_FORM_DATA_TYPE));
		DocumentDto result = postResponse.readEntity(DocumentDto.class);

		assertThat(postResponse.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		assertThat(result.getContent()).isNull();
	}

	@Test
	public void testCreateConsolidatedDocumentWithInvalidMergeSourceIds() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		String mergeSourceIds = firstDocumentToMerge.getId() + ", " +
								secondDocumentToMerge.getId();

		Response postResponse = buildPostQuery().request().header("host", host)
				.header(CustomHttpHeaders.MERGE_SOURCE, mergeSourceIds)
				.post(entity(buildMultiPart(minDocumentToAdd, null), MULTIPART_FORM_DATA_TYPE));

		assertThat(postResponse.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = postResponse.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(" " + secondDocumentToMerge.getId()).getValidationError()));
	}

	@Test
	public void testCreateConsolidatedDocumentWithEmptyContent() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		DocumentDto documentWithoutContent = createDocumentWithoutContent();

		String mergeSourceIds = firstDocumentToMerge.getId() + "," +
								secondDocumentToMerge.getId() + "," +
								documentWithoutContent.getId();

		Response postResponse = buildPostQuery().request().header("host", host)
				.header(CustomHttpHeaders.MERGE_SOURCE, mergeSourceIds)
				.post(entity(buildMultiPart(minDocumentToAdd, null), MULTIPART_FORM_DATA_TYPE));

		assertThat(postResponse.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = postResponse.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new CannotReadContentException(documentWithoutContent.getId()).getValidationError()));
	}

	@Test
	public void testCreateConsolidatedDocumentWithUnsupportedContent() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		DocumentDto documentWithZipContent = createDocumentWithZipContent();

		String mergeSourceIds = firstDocumentToMerge.getId() + "," +
								secondDocumentToMerge.getId() + "," +
								documentWithZipContent.getId();

		Response postResponse = buildPostQuery().request().header("host", host)
				.header(CustomHttpHeaders.MERGE_SOURCE, mergeSourceIds)
				.post(entity(buildMultiPart(minDocumentToAdd, null), MULTIPART_FORM_DATA_TYPE));

		assertThat(postResponse.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = postResponse.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new CannotReadContentException(documentWithZipContent.getId()).getValidationError()));
	}

	@Test
	public void testCreateConsolidatedDocumentWithNonExistingDocument() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		String fakeId = "fakeId";

		String mergeSourceIds = firstDocumentToMerge.getId() + "," +
								secondDocumentToMerge.getId() + "," +
								fakeId;

		Response postResponse = buildPostQuery().request().header("host", host)
				.header(CustomHttpHeaders.MERGE_SOURCE, mergeSourceIds)
				.post(entity(buildMultiPart(minDocumentToAdd, null), MULTIPART_FORM_DATA_TYPE));

		assertThat(postResponse.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = postResponse.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(fakeId).getValidationError()));
	}

	@Test
	public void testCreateConsolidatedDocumentWithoutDocumentAccess() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		Record record = recordServices.getDocumentById(firstDocumentToMerge.getId());
		User bobUser = userServices.getUserInCollection(bob, record.getCollection());
		authorizationsServices.add(authorizationForUsers(bobUser).on(record).givingNegativeReadWriteAccess());

		String mergeSourceIds = firstDocumentToMerge.getId() + "," +
								secondDocumentToMerge.getId();

		Response postResponse = buildPostQuery().request().header("host", host)
				.header(CustomHttpHeaders.MERGE_SOURCE, mergeSourceIds)
				.post(entity(buildMultiPart(minDocumentToAdd, null), MULTIPART_FORM_DATA_TYPE));

		assertThat(postResponse.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = postResponse.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	@Test
	public void testCreateConsolidatedDocumentWithLogicallyDeletedDocument() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		String secondDocumentToMergeId = secondDocumentToMerge.getId();
		String mergeSourceIds = firstDocumentToMerge.getId() + "," +
								secondDocumentToMergeId;

		Record record = recordServices.getDocumentById(secondDocumentToMergeId);
		recordServices.logicallyDelete(record, User.GOD);

		Response postResponse = buildPostQuery().request().header("host", host)
				.header(CustomHttpHeaders.MERGE_SOURCE, mergeSourceIds)
				.post(entity(buildMultiPart(minDocumentToAdd, null), MULTIPART_FORM_DATA_TYPE));

		assertThat(postResponse.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = postResponse.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordLogicallyDeletedException(secondDocumentToMergeId).getValidationError()));
	}

	@Test
	public void testCreateConsolidatedDocumentWithPhysicallyDeletedDocument() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		String secondDocumentToMergeId = secondDocumentToMerge.getId();
		String mergeSourceIds = firstDocumentToMerge.getId() + "," +
								secondDocumentToMergeId;

		Record record = recordServices.getDocumentById(secondDocumentToMergeId);
		recordServices.logicallyDelete(record, User.GOD);
		recordServices.physicallyDelete(record, User.GOD);

		Response postResponse = buildPostQuery().request().header("host", host)
				.header(CustomHttpHeaders.MERGE_SOURCE, mergeSourceIds)
				.post(entity(buildMultiPart(minDocumentToAdd, null), MULTIPART_FORM_DATA_TYPE));

		assertThat(postResponse.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = postResponse.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(secondDocumentToMergeId).getValidationError()));
	}

	@Test
	public void testCreateConsolidatedDocumentWithContent() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		String mergeSourceIds = firstDocumentToMerge.getId() + "," +
								secondDocumentToMerge.getId();

		Response postResponse = buildPostQuery().request().header("host", host)
				.header(CustomHttpHeaders.MERGE_SOURCE, mergeSourceIds)
				.post(entity(buildMultiPart(fullDocumentToAdd, null), MULTIPART_FORM_DATA_TYPE));

		assertThat(postResponse.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = postResponse.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterCombinationException("CustomHttpHeaders.MERGE_SOURCE", "document.content").getValidationError()));
	}

	@Test
	public void testCreateConsolidatedDocumentWithFile() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		String mergeSourceIds = firstDocumentToMerge.getId() + "," +
								secondDocumentToMerge.getId();

		Response postResponse = buildPostQuery().request().header("host", host)
				.header(CustomHttpHeaders.MERGE_SOURCE, mergeSourceIds)
				.post(entity(buildMultiPart(minDocumentToAdd, fileToAdd), MULTIPART_FORM_DATA_TYPE));

		assertThat(postResponse.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = postResponse.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterCombinationException("CustomHttpHeaders.MERGE_SOURCE", "file").getValidationError()));
	}

	//
	// PRIVATE FUNCTIONS
	//

	private DocumentDto createDocumentWithoutContent() throws Exception {
		DocumentDto document = DocumentDto.builder()
				.folderId(records.folder_A20)
				.title("title")
				.build();

		Response response = buildPostQuery().request().header("host", host)
				.header(CustomHttpHeaders.FLUSH_MODE, "NOW")
				.post(entity(buildMultiPart(document, null), MULTIPART_FORM_DATA_TYPE));
		return response.readEntity(DocumentDto.class);
	}

	private DocumentDto createDocumentWithTextContent(String filename, String content) throws Exception {
		DocumentDto document = DocumentDto.builder()
				.folderId(records.folder_A20)
				.title("title")
				.content(ContentDto.builder().versionType(MAJOR).filename(filename).build())
				.build();
		File file = newTempFileWithContent(filename, content);

		Response response = buildPostQuery().request().header("host", host)
				.header(CustomHttpHeaders.FLUSH_MODE, "NOW")
				.post(entity(buildMultiPart(document, file), MULTIPART_FORM_DATA_TYPE));
		return response.readEntity(DocumentDto.class);
	}

	private DocumentDto createDocumentWithZipContent() throws Exception {
		String filename = "zipTestFile.7z";

		DocumentDto document = DocumentDto.builder()
				.folderId(records.folder_A20)
				.title("title")
				.content(ContentDto.builder().versionType(MAJOR).filename(filename).build())
				.build();
		File file = getTestResourceFile("zipTestFile.7z");

		Response response = buildPostQuery().request().header("host", host)
				.header(CustomHttpHeaders.FLUSH_MODE, "NOW")
				.post(entity(buildMultiPart(document, file), MULTIPART_FORM_DATA_TYPE));
		return response.readEntity(DocumentDto.class);
	}

	private Response doPostQuery(String flushMode, DocumentDto document) throws Exception {
		return buildPostQuery().request().header("host", host).header(CustomHttpHeaders.FLUSH_MODE, flushMode)
				.post(entity(buildMultiPart(document), MULTIPART_FORM_DATA_TYPE));
	}

	private WebTarget buildPostQuery(String... excludedParam) throws Exception {
		return buildPostQuery(true, excludedParam);
	}

	private WebTarget buildPostQuery(boolean calculateSignature, String... excludedParam) throws Exception {
		method = !HttpMethods.contains(method) ? "fakeMethod" : POST;
		return buildQuery(webTarget, calculateSignature,
				asList("folderId", "serviceKey", "method", "date", "expiration", "signature"), excludedParam);
	}

	private Response doPutQuery(String flushMode, DocumentDto document) throws Exception {
		method = HttpMethods.PUT;
		return buildQuery(webTarget, true, asList("id", "serviceKey", "method", "date", "expiration", "signature"))
				.request().header("host", host).header(CustomHttpHeaders.FLUSH_MODE, flushMode)
				.put(entity(buildMultiPart(document), MULTIPART_FORM_DATA_TYPE));
	}

	private Response doPatchQuery(String flushMode, DocumentDto document, File file) throws Exception {
		id = document.getId();
		method = HttpMethods.PATCH;
		return buildQuery(webTarget, true, asList("id", "serviceKey", "method", "date", "expiration", "signature"))
				.request().header("host", host).header(CustomHttpHeaders.FLUSH_MODE, flushMode)
				.build("PATCH", entity(buildMultiPart(document, file), MULTIPART_FORM_DATA_TYPE)).invoke();
	}

	private Response doGetQuery(String documentId) throws Exception {
		id = documentId;
		method = HttpMethods.GET;
		return buildQuery(webTarget, true, asList("id", "serviceKey", "method", "date", "expiration", "signature"))
				.request().header("host", host).get();
	}

}
