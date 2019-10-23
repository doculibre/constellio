package com.constellio.app.modules.restapi.document;

import com.constellio.app.modules.restapi.core.exception.InvalidDateCombinationException;
import com.constellio.app.modules.restapi.core.exception.InvalidDateFormatException;
import com.constellio.app.modules.restapi.core.exception.InvalidMetadataValueException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterCombinationException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotFoundException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotMultivalueException;
import com.constellio.app.modules.restapi.core.exception.MetadataReferenceNotAllowedException;
import com.constellio.app.modules.restapi.core.exception.OptimisticLockException;
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
import com.constellio.model.utils.MimeTypes;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.constellio.app.modules.restapi.core.util.HttpMethods.PUT;
import static com.constellio.app.modules.restapi.core.util.Permissions.READ;
import static com.constellio.app.modules.restapi.core.util.Permissions.WRITE;
import static com.constellio.app.modules.restapi.document.enumeration.VersionType.MAJOR;
import static com.constellio.app.modules.restapi.document.enumeration.VersionType.MINOR;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.asSet;
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

public class DocumentRestfulServicePUTAcceptanceTest extends BaseDocumentRestfulServiceAcceptanceTest {

	private DocumentDto minDocumentToUpdate, minDocumentWithoutAcesToUpdate, fullDocumentToUpdate;
	private File fileToUpdate;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		minDocumentToUpdate = DocumentDto.builder().id(fakeDocument.getId()).title("New title").folderId(records.folder_C01).build();
		minDocumentWithoutAcesToUpdate = DocumentDto.builder().id(fakeDocument.getId()).title("New title").folderId(fakeDocument.getFolder())
				.directAces(asList(
						AceDto.builder().principals(singleton(bob)).permissions(asSet("READ", "WRITE", "DELETE")).build(),
						AceDto.builder().principals(singleton(alice)).permissions(asSet("READ", "WRITE")).build())).build();
		fullDocumentToUpdate = DocumentDto.builder().id(fakeDocument.getId()).folderId(fakeDocument.getFolder()).title("newTitle")
				.organization("newOrg").subject("newSub").author("newAut").keywords(asList("document", "document2"))
				.content(ContentDto.builder().versionType(MINOR).filename(fakeFilename).build())
				.type(DocumentTypeDto.builder().id(fakeDocumentType.getId()).build())
				.directAces(singletonList(
						AceDto.builder().principals(singleton(alice)).permissions(newHashSet(READ, WRITE))
								.startDate(toDateString(new LocalDate()))
								.endDate(toDateString(new LocalDate().plusDays(365))).build()))
				.extendedAttributes(singletonList(
						ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList("value1b")).build()))
				.build();
		fileToUpdate = newTempFileWithContent(fakeFilename, fakeFileContentV1);
	}

	@Test
	public void testUpdateMinimalDocument() throws Exception {
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getId()).isEqualTo(minDocumentToUpdate.getId());
		assertThat(doc.getTitle()).isEqualTo(minDocumentToUpdate.getTitle());
		assertThat(doc.getFolderId()).isEqualTo(minDocumentToUpdate.getFolderId());
		assertThat(doc.getSubject()).isNull();
		assertThat(doc.getKeywords()).isEmpty();
		assertThat(doc.getAuthor()).isNull();
		assertThat(doc.getDirectAces()).isEmpty();
		assertThat(doc.getInheritedAces()).isNotEmpty();
		assertThat(doc.getExtendedAttributes()).isEmpty();
		assertThat(doc.getType()).isNull();
		assertThat(doc.getContent().getVersionType()).isEqualTo(MAJOR);
		assertThat(doc.getContent().getFilename()).isEqualTo(fakeFilename);
		assertThat(doc.getContent().getHash()).isEqualTo(dataSummaryV2.getHash());
		assertThat(doc.getContent().getVersion()).isEqualTo("2.0");

		Record record = recordServices.getDocumentById(doc.getId());
		assertThat(record).isNotNull();
		assertThat(record.getId()).isEqualTo(doc.getId());
		assertThatRecord(record).extracting(Document.TITLE, Document.FOLDER).containsExactly(doc.getTitle(), doc.getFolderId());
		assertThatRecord(record).extracting(Document.TYPE, Document.SUBJECT, Document.KEYWORDS, Document.AUTHOR)
				.containsExactly(null, null, Collections.emptyList(), null);

		assertThat(response.getHeaderString("ETag")).isNull();
	}

	@Test
	public void testUpdateFullDocument() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, "value1", asList("value2a", "value2b"));
		resetCounters();

		Response response = doPutQuery(fullDocumentToUpdate, fileToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).hasSize(4);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getId()).isEqualTo(fullDocumentToUpdate.getId());
		assertThat(doc.getTitle()).isEqualTo(fullDocumentToUpdate.getTitle());
		assertThat(doc.getFolderId()).isEqualTo(fullDocumentToUpdate.getFolderId());
		assertThat(doc.getOrganization()).isEqualTo(fullDocumentToUpdate.getOrganization());
		assertThat(doc.getSubject()).isEqualTo(fullDocumentToUpdate.getSubject());
		assertThat(doc.getKeywords()).isEqualTo(fullDocumentToUpdate.getKeywords());
		assertThat(doc.getAuthor()).isEqualTo(fullDocumentToUpdate.getAuthor());
		assertThat(doc.getDirectAces()).containsOnly(fullDocumentToUpdate.getDirectAces().toArray(new AceDto[0]));
		assertThat(doc.getExtendedAttributes()).isEqualTo(fullDocumentToUpdate.getExtendedAttributes());
		assertThat(doc.getType().getId()).isEqualTo(fullDocumentToUpdate.getType().getId()).isEqualTo(fakeDocumentType.getId());
		assertThat(doc.getType().getCode()).isEqualTo(fakeDocumentType.getCode());
		assertThat(doc.getType().getTitle()).isEqualTo(fakeDocumentType.getTitle());
		assertThat(doc.getContent().getVersionType()).isEqualTo(fullDocumentToUpdate.getContent().getVersionType());
		assertThat(doc.getContent().getFilename()).isEqualTo(fullDocumentToUpdate.getContent().getFilename());
		assertThat(doc.getContent().getHash()).isNotNull().isNotEmpty();
		assertThat(doc.getContent().getVersion()).isEqualTo("2.1");

		InputStream stream = contentManager.getContentInputStream(doc.getContent().getHash(), doc.getContent().getFilename());
		assertThat(HashingUtils.md5(IOUtils.toByteArray(stream))).isEqualTo(expectedChecksumV1);
		closeQuietly(stream);

		Record record = recordServices.getDocumentById(doc.getId());
		assertThat(record).isNotNull();
		assertThatRecord(record).extracting(Document.TITLE, Document.FOLDER, Document.KEYWORDS, Document.AUTHOR, Document.COMPANY, Document.SUBJECT, Document.TYPE)
				.containsExactly(doc.getTitle(), doc.getFolderId(), doc.getKeywords(), doc.getAuthor(), doc.getOrganization(), doc.getSubject(), doc.getType().getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(doc.getExtendedAttributes().get(0).getValues());
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(Collections.emptyList());

		assertThat(response.getHeaderString("ETag")).isNull();

		Content content = record.get(rm.document.content());
		assertThat(content.getCurrentVersion().getVersion()).isEqualTo(doc.getContent().getVersion());
		assertThat(content.getCurrentVersion().getFilename()).isEqualTo(doc.getContent().getFilename());
		assertThat(content.getCurrentVersion().getHash()).isEqualTo(doc.getContent().getHash());

		List<Authorization> authorizations = filterInheritedAuthorizations(authorizationsServices.getRecordAuthorizations(record), record.getId());
		assertThat(authorizations).extracting("principals").usingElementComparator(comparingListAnyOrder).containsOnly(
				toPrincipalIds(fullDocumentToUpdate.getDirectAces().get(0).getPrincipals()));
		assertThat(authorizations).extracting("roles").usingElementComparator(comparingListAnyOrder).containsOnly(
				Lists.newArrayList(fullDocumentToUpdate.getDirectAces().get(0).getPermissions()));
		assertThat(authorizations).extracting("startDate", "endDate").containsOnly(
				tuple(toLocalDate(fullDocumentToUpdate.getDirectAces().get(0).getStartDate()), toLocalDate(fullDocumentToUpdate.getDirectAces().get(0).getEndDate())));
	}

	@Test
	public void testUpdateFullDocumentAllFilters() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		Set<String> filters = newHashSet("folderId", "type", "content", "title", "keywords", "author", "subject",
				"organization", "directAces", "inheritedAces", "extendedAttributes");
		Response response = doPutQuery(filters, fullDocumentToUpdate, fileToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		DocumentDto documentDto = response.readEntity(DocumentDto.class);
		assertThat(documentDto.getId()).isNotNull();
		assertThat(singletonList(documentDto)).extracting("folderId", "type", "content", "title",
				"keywords", "author", "subject", "organization", "directAces", "inheritedAces", "extendedAttributes")
				.containsOnly(tuple(null, null, null, null, null, null, null, null, null, null, null));
	}

	@Test
	public void testUpdateFullDocumentSomeFilters() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		Set<String> filters = newHashSet("type", "content", "directAces", "inheritedAces", "extendedAttributes");
		Response response = doPutQuery(filters, fullDocumentToUpdate, fileToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		DocumentDto documentDto = response.readEntity(DocumentDto.class);
		assertThat(documentDto.getId()).isNotNull();
		assertThat(singletonList(documentDto)).extracting("folderId", "type", "content", "title",
				"keywords", "author", "subject", "organization", "directAces", "inheritedAces", "extendedAttributes")
				.containsOnly(tuple(fullDocumentToUpdate.getFolderId(), null, null, fullDocumentToUpdate.getTitle(),
						fullDocumentToUpdate.getKeywords(), fullDocumentToUpdate.getAuthor(), fullDocumentToUpdate.getSubject(),
						fullDocumentToUpdate.getOrganization(), null, null, null));
	}

	@Test
	public void testUpdateDocumentInvalidFilter() throws Exception {
		Response response = doPutQuery(singleton("invalid"), fullDocumentToUpdate, fileToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("filter", "invalid").getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithDifferentFilename() throws Exception {
		minDocumentToUpdate.setContent(ContentDto.builder().versionType(MINOR).filename("newFilename.txt").build());

		Response response = doPutQuery(minDocumentToUpdate, fileToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getContent().getFilename()).isEqualTo(minDocumentToUpdate.getContent().getFilename());
		assertThat(doc.getContent().getVersion()).isEqualTo("2.1");

		Record record = recordServices.getDocumentById(doc.getId());
		Content content = record.get(rm.document.content());
		assertThat(content.getCurrentVersion().getFilename()).isEqualTo(doc.getContent().getFilename());
		assertThat(content.getCurrentVersion().getVersion()).isEqualTo(doc.getContent().getVersion());
	}

	@Test
	public void testUpdateDocumentWithDifferentMimeType() throws Exception {
		minDocumentToUpdate.setContent(ContentDto.builder().versionType(MINOR).filename("content.png").build());
		File file = getTestResourceFile("image.png");

		Response response = doPutQuery(minDocumentToUpdate, file);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getContent().getFilename()).isEqualTo(minDocumentToUpdate.getContent().getFilename());
		assertThat(doc.getContent().getVersion()).isEqualTo("2.1");

		Record record = recordServices.getDocumentById(doc.getId());
		Content content = record.get(rm.document.content());
		assertThat(content.getCurrentVersion().getFilename()).isEqualTo(doc.getContent().getFilename());
		assertThat(content.getCurrentVersion().getVersion()).isEqualTo(doc.getContent().getVersion());
		assertThat(content.getCurrentVersion().getMimetype()).isEqualTo(MimeTypes.MIME_IMAGE_PNG);
	}

	@Test
	public void testUpdateDocumentOnDocumentWithoutContent() throws Exception {
		Document newDoc = rm.newDocument().setFolder(folderId).setTitle("Title");
		recordServices.add(newDoc);

		id = newDoc.getId();
		DocumentDto docUpdate = DocumentDto.builder().id(id).title(newDoc.getTitle()).folderId(newDoc.getFolder())
				.content(ContentDto.builder().filename(fakeFilename).versionType(MAJOR).build()).build();
		Response response = doPutQuery(docUpdate, fileToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(singletonList(doc.getContent())).extracting("versionType", "filename", "hash", "version")
				.containsExactly(tuple(MAJOR, fakeFilename, dataSummaryV1.getHash(), "1.0"));

		Record record = recordServices.getDocumentById(docUpdate.getId());
		Content content = record.get(rm.document.content());
		assertThat(singletonList(content.getCurrentVersion())).extracting("version", "filename", "hash", "major")
				.containsExactly(tuple(doc.getContent().getVersion(), doc.getContent().getFilename(), doc.getContent().getHash(), true));
	}

	@Test
	public void testUpdateDocumentWithoutFilenameOnDocumentWithoutContent() throws Exception {
		Document newDoc = rm.newDocument().setFolder(folderId).setTitle("Title");
		recordServices.add(newDoc);

		id = newDoc.getId();
		DocumentDto docUpdate = DocumentDto.builder().id(id).title(newDoc.getTitle()).folderId(newDoc.getFolder())
				.content(ContentDto.builder().versionType(MAJOR).build()).build();
		Response response = doPutQuery(docUpdate, fileToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RequiredParameterException("content.filename").getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithDateUsr() throws Exception {
		addUsrMetadata(MetadataValueType.DATE, null, null);

		List<String> value1 = singletonList("2017-07-21"), value2 = asList("2017-07-22", "2018-07-23");
		minDocumentToUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getExtendedAttributes()).isEqualTo(minDocumentToUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(new LocalDate(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(new LocalDate(value2.get(0)), new LocalDate(value2.get(1))));
	}

	@Test
	public void testUpdateDocumentWithDateUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("2017-07-21T00:00:00");
		addUsrMetadata(MetadataValueType.DATE, null, null);

		minDocumentToUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidDateFormatException(value1.get(0), dateFormat).getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithDateTimeUsr() throws Exception {
		addUsrMetadata(MetadataValueType.DATE_TIME, null, null);

		List<String> value1 = singletonList(toDateString(fakeDate)), value2 = asList(toDateString(fakeDate), toDateString(fakeDate));
		minDocumentToUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getExtendedAttributes()).isEqualTo(minDocumentToUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(toLocalDateTime(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(toLocalDateTime(value2.get(0)), toLocalDateTime(value2.get(1))));
	}

	@Test
	public void testUpdateDocumentWithDateTimeUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("2018-07-21T23:59:59.123-04:00");
		addUsrMetadata(MetadataValueType.DATE_TIME, null, null);

		minDocumentToUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidDateFormatException(value1.get(0), dateTimeFormat).getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithNumberUsr() throws Exception {
		addUsrMetadata(MetadataValueType.NUMBER, null, null);

		List<String> value1 = singletonList("123.456"), value2 = asList("2018.24", "2018.25");
		minDocumentToUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getExtendedAttributes()).isEqualTo(minDocumentToUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(Double.valueOf(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(Double.valueOf(value2.get(0)), Double.valueOf(value2.get(1))));
	}

	@Test
	public void testUpdateDocumentWithNumberUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("not-a-number");
		addUsrMetadata(MetadataValueType.NUMBER, null, null);

		minDocumentToUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidMetadataValueException(MetadataValueType.NUMBER.name(), value1.get(0)).getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithBooleanUsr() throws Exception {
		addUsrMetadata(MetadataValueType.BOOLEAN, null, null);

		List<String> value1 = singletonList("true"), value2 = asList("true", "false");
		minDocumentToUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getExtendedAttributes()).isEqualTo(minDocumentToUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(Boolean.valueOf(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(Boolean.valueOf(value2.get(0)), Boolean.valueOf(value2.get(1))));
	}

	@Test
	public void testUpdateDocumentWithBooleanUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("null");
		addUsrMetadata(MetadataValueType.BOOLEAN, null, null);

		minDocumentToUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidMetadataValueException(MetadataValueType.BOOLEAN.name(), value1.get(0)).getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithTextUsr() throws Exception {
		addUsrMetadata(MetadataValueType.TEXT, null, null);

		List<String> value1 = singletonList("<html>"), value2 = asList("<b>bold", "test@test.com");
		minDocumentToUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getExtendedAttributes()).isEqualTo(minDocumentToUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(value1);
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(value2);
	}

	@Test
	public void testUpdateDocumentWithReferenceUsr() throws Exception {
		addUsrMetadata(MetadataValueType.REFERENCE, null, null);

		List<String> value1 = singletonList(records.getAlice().getId());
		List<String> value2 = asList(records.getChuckNorris().getId(), records.getAlice().getId());
		minDocumentToUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getExtendedAttributes()).isEqualTo(minDocumentToUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(value1);
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(value2);
	}

	@Test
	public void testUpdateDocumentWithReferenceUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("fake id");
		addUsrMetadata(MetadataValueType.REFERENCE, null, null);

		minDocumentToUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(value1.get(0)).getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithReferenceUsrAndInvalidSchemaType() throws Exception {
		List<String> value1 = singletonList(records.folder_A18);
		addUsrMetadata(MetadataValueType.REFERENCE, null, null);

		minDocumentToUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataReferenceNotAllowedException("folder", fakeMetadata1).getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithSchemaChangeByTypeId() throws Exception {
		minDocumentToUpdate.setType(DocumentTypeDto.builder().id(records.documentTypeId_9).build());

		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getId()).isEqualTo(fullDocumentToUpdate.getId());

		Record documentTypeRecord = recordServices.getDocumentById(records.documentTypeId_9);
		assertThat(doc.getType().getId()).isEqualTo(documentTypeRecord.getId());
		assertThat(doc.getType().getCode()).isEqualTo(documentTypeRecord.get((Schemas.CODE)));
		assertThat(doc.getType().getTitle()).isEqualTo(documentTypeRecord.getTitle());

		Record documentRecord = recordServices.getDocumentById(doc.getId());
		assertThat(documentRecord.getId()).isEqualTo(doc.getId());
		assertThatRecord(documentRecord).extracting(Document.TYPE).isEqualTo(singletonList(records.documentTypeId_9));
	}

	@Test
	public void testUpdateDocumentWithSchemaChangeByTypeCode() throws Exception {
		Record documentType = recordServices.getDocumentById(records.documentTypeId_10);
		minDocumentToUpdate.setType(DocumentTypeDto.builder().code(documentType.<String>get(Schemas.CODE)).build());

		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getId()).isEqualTo(fullDocumentToUpdate.getId());
		assertThat(doc.getType().getId()).isEqualTo(documentType.getId());
		assertThat(doc.getType().getCode()).isEqualTo(documentType.<String>get(Schemas.CODE));
		assertThat(doc.getType().getTitle()).isEqualTo(documentType.get(rm.ddvDocumentType.title()));

		Record documentRecord = recordServices.getDocumentById(doc.getId());
		assertThat(documentRecord.getId()).isEqualTo(doc.getId());
		assertThatRecord(documentRecord).extracting(Document.TYPE).isEqualTo(singletonList(records.documentTypeId_10));
	}

	@Test
	public void testUpdateDocumentWithSchemaChangeAndInvalidMetadataKey() throws Exception {
		switchToCustomSchema(fakeDocument.getId());
		addUsrMetadata(id, records.documentTypeForm().getLinkedSchema(), MetadataValueType.STRING, null, null);

		minDocumentToUpdate.setType(DocumentTypeDto.builder().id(records.documentTypeId_1).build());
		minDocumentToUpdate.setExtendedAttributes(singletonList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList("value1b")).build()));

		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataNotFoundException(fakeMetadata1).getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithMissingId() throws Exception {
		Response response = doPutQuery(minDocumentToUpdate, null, "id");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "id"));
	}

	@Test
	public void testUpdateDocumentWithInvalidId() throws Exception {
		id = "fakeId";
		minDocumentToUpdate.setId("fakeId");
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(id).getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithMissingServiceKey() throws Exception {
		Response response = doPutQuery(minDocumentToUpdate, null, "serviceKey");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "serviceKey"));
	}

	@Test
	public void testUpdateDocumentWithInvalidServiceKey() throws Exception {
		serviceKey = "fakeServiceKey";
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithMissingMethod() throws Exception {
		Response response = doPutQuery(minDocumentToUpdate, null, "method");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "method"));
	}

	@Test
	public void testUpdateDocumentWithInvalidMethod() throws Exception {
		method = "fakeMethod";
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("method", method).getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithMissingDate() throws Exception {
		Response response = doPutQuery(minDocumentToUpdate, null, "date");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "date"));
	}

	@Test
	public void testUpdateDocumentWithInvalidDate() throws Exception {
		date = "fakeDate";
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("date", date).getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithMissingExpiration() throws Exception {
		Response response = doPutQuery(minDocumentToUpdate, null, "expiration");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "expiration"));
	}

	@Test
	public void testUpdateDocumentWithInvalidExpiration() throws Exception {
		expiration = "fakeExpiration";
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(JERSEY_NOT_FOUND_MESSAGE);
	}

	@Test
	public void testUpdateDocumentWithInvalidDateAndExpiration() throws Exception {
		date = DateUtils.formatIsoNoMillis(TimeProvider.getLocalDateTime().minusDays(365));
		expiration = "1";
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ExpiredSignedUrlException().getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithMissingSignature() throws Exception {
		Response response = doPutQuery(minDocumentToUpdate, null, "signature");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "signature"));
	}

	@Test
	public void testUpdateDocumentWithInvalidSignature() throws Exception {
		signature = "fakeSignature";
		Response response = doPutQuery(minDocumentToUpdate, null, false);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidSignatureException().getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithUserWithoutPermissions() throws Exception {
		serviceKey = sasquatchServiceKey;
		token = sasquatchToken;
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithMissingDocumentId() throws Exception {
		minDocumentToUpdate.setId(null);
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ParametersMustMatchException("id", "document.id").getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithInvalidDocumentId() throws Exception {
		minDocumentToUpdate.setId(records.document_B33);
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ParametersMustMatchException("id", "document.id").getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithMissingDocumentFolderId() throws Exception {
		minDocumentToUpdate.setFolderId(null);
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "document.folderId"));
	}

	@Test
	public void testUpdateDocumentWithInvalidDocumentFolderId() throws Exception {
		minDocumentToUpdate.setFolderId("fakeId");
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(minDocumentToUpdate.getFolderId()).getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithMissingDocumentTitle() throws Exception {
		minDocumentToUpdate.setTitle(null);
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "document.title"));
	}

	@Test
	public void testUpdateDocumentWithInvalidTypeId() throws Exception {
		minDocumentToUpdate.setType(DocumentTypeDto.builder().id("fake").build());
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ResourceTypeNotFoundException("id", "fake").getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithInvalidTypeCode() throws Exception {
		minDocumentToUpdate.setType(DocumentTypeDto.builder().code("fake").build());
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ResourceTypeNotFoundException("code", "fake").getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithTypeIdAndTypeCode() throws Exception {
		minDocumentToUpdate.setType(DocumentTypeDto.builder().id("id").code("code").build());
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterCombinationException("type.id", "type.code").getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithContentAndMissingFile() throws Exception {
		minDocumentToUpdate.setContent(ContentDto.builder().versionType(MAJOR).filename("test.txt").build());
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "file"));
	}

	@Test
	public void testUpdateDocumentWithFileAndMissingContent() throws Exception {
		Response response = doPutQuery(minDocumentToUpdate, fileToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "document.content"));
	}

	@Test
	public void testUpdateDocumentWithMissingContentVersionType() throws Exception {
		minDocumentToUpdate.setContent(ContentDto.builder().filename("test.txt").build());
		Response response = doPutQuery(minDocumentToUpdate, fileToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "content.versionType"));
	}

	@Test
	public void testUpdateDocumentWithMissingContentFilename() throws Exception {
		minDocumentToUpdate.setContent(ContentDto.builder().versionType(MAJOR).build());
		Response response = doPutQuery(minDocumentToUpdate, fileToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getContent().getFilename()).isEqualTo(fakeFilename);
		assertThat(doc.getContent().getVersion()).isEqualTo("3.0");

		Record record = recordServices.getDocumentById(doc.getId());
		Content content = record.get(rm.document.content());
		assertThat(content.getCurrentVersion().getFilename()).isEqualTo(fakeFilename);
		assertThat(content.getCurrentVersion().getVersion()).isEqualTo(doc.getContent().getVersion());
	}

	@Test
	public void testUpdateDocumentWithInvalidAcePrincipal() throws Exception {
		minDocumentToUpdate.setDirectAces(singletonList(AceDto.builder().principals(singleton("fake")).permissions(singleton(READ)).build()));
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException("fake").getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithInvalidAcePermissions() throws Exception {
		minDocumentToUpdate.setDirectAces(singletonList(AceDto.builder()
				.principals(singleton(alice)).permissions(singleton("fake")).build()));
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("directAces[0].permissions", "fake").getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithStartDateGreaterThanEndDate() throws Exception {
		String start = toDateString(new LocalDate().plusDays(365));
		String end = toDateString(new LocalDate());
		minDocumentToUpdate.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice))
				.permissions(singleton(READ)).startDate(start).endDate(end).build()));
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(new InvalidDateCombinationException(start, end).getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithStartDateOnly() throws Exception {
		minDocumentToUpdate.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice))
				.permissions(singleton(READ)).startDate(toDateString(new LocalDate())).build()));
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getDirectAces().get(0).getStartDate()).isEqualTo(minDocumentToUpdate.getDirectAces().get(0).getStartDate());
		assertThat(doc.getDirectAces().get(0).getEndDate()).isNull();

		Record record = recordServices.getDocumentById(doc.getId());
		List<Authorization> authorizations = filterInheritedAuthorizations(authorizationsServices.getRecordAuthorizations(record), record.getId());
		assertThat(authorizations).extracting("startDate").containsOnly(toLocalDate(minDocumentToUpdate.getDirectAces().get(0).getStartDate()));
		assertThat(authorizations).extracting("endDate").containsNull();
	}

	@Test
	public void testUpdateDocumentWithEndDateOnly() throws Exception {
		minDocumentToUpdate.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice))
				.permissions(singleton(READ)).endDate(toDateString(new LocalDate())).build()));
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(new RequiredParameterException("ace.startDate").getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithInvalidExtAttributeKey() throws Exception {
		minDocumentToUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key("fake").values(singletonList("123")).build()));
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataNotFoundException("fake").getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithInvalidExtAttributeMultiValue() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		minDocumentToUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(asList("ab", "cd")).build()));
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataNotMultivalueException(fakeMetadata1).getValidationError()));
	}

	@Test
	public void testUpdateDocumentWithEmptyExtAttributeValues() throws Exception {
		minDocumentToUpdate.setExtendedAttributes(singletonList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(Collections.<String>emptyList()).build()));
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_EMPTY_MESSAGE, "extendedAttributes[0].values"));
	}

	@Test
	public void testUpdateDocumentWithCustomSchema() throws Exception {
		switchToCustomSchema(fakeDocument.getId());
		addUsrMetadata(id, records.documentTypeForm().getLinkedSchema(), MetadataValueType.STRING, null, null);

		List<String> value1 = singletonList("value1b"), value2 = asList("value2c", "value2d");
		minDocumentToUpdate.setType(DocumentTypeDto.builder().id(records.documentTypeForm().getId()).build());
		minDocumentToUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));

		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getExtendedAttributes()).isEqualTo(minDocumentToUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(value1);
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(value2);
	}

	@Test
	public void testUpdateDocumentLogicallyDeleted() throws Exception {
		Record record = recordServices.getDocumentById(id);
		recordServices.logicallyDelete(record, User.GOD);

		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordLogicallyDeletedException(id).getValidationError()));
	}

	@Test
	public void testUpdateInvalidFlushMode() throws Exception {
		Response response = doPutQuery("ALL", minDocumentWithoutAcesToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException(CustomHttpHeaders.FLUSH_MODE, "ALL").getValidationError()));
	}

	@Test
	public void testUpdateWithin0SecondsFlushMode() throws Exception {
		Response response = doPutQuery("WITHIN_0_SECONDS", minDocumentWithoutAcesToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException(CustomHttpHeaders.FLUSH_MODE, "WITHIN_0_SECONDS").getValidationError()));
	}

	@Test
	public void testUpdateWithinInvalidIntegerSecondsFlushMode() throws Exception {
		String flushMode = "WITHIN_111111111111111111111_SECONDS";
		Response response = doPutQuery(flushMode, minDocumentWithoutAcesToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException(CustomHttpHeaders.FLUSH_MODE, flushMode).getValidationError()));
	}

	@Test
	public void testUpdateDefaultFlushMode() throws Exception {
		Response response = doPutQuery(minDocumentWithoutAcesToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		List<Record> documents = searchServices.search(new LogicalSearchQuery(
				from(rm.document.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(id)));
		assertThat(documents.get(0).getTitle()).isNotEqualTo(minDocumentWithoutAcesToUpdate.getTitle());

		Record document = recordServices.realtimeGetRecordById(minDocumentWithoutAcesToUpdate.getId());
		assertThat(document.getTitle()).isEqualTo(minDocumentWithoutAcesToUpdate.getTitle());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + document.getVersion() + "\"");

		TimeUnit.MILLISECONDS.sleep(5250);

		documents = searchServices.search(new LogicalSearchQuery(
				from(rm.document.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(id)));
		assertThat(documents.get(0).getTitle()).isEqualTo(minDocumentWithoutAcesToUpdate.getTitle());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + documents.get(0).getVersion() + "\"");
	}

	@Test
	public void testUpdateWithin1SecondFlushMode() throws Exception {
		Response response = doPutQuery("WITHIN_1_SECONDS", minDocumentWithoutAcesToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		Record record = recordServices.getDocumentById(minDocumentWithoutAcesToUpdate.getId());
		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + record.getVersion() + "\"");

		TimeUnit.MILLISECONDS.sleep(1250);

		List<Record> documents = searchServices.search(new LogicalSearchQuery(
				from(rm.document.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(id)));
		assertThat(documents.get(0).getTitle()).isEqualTo(minDocumentWithoutAcesToUpdate.getTitle());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + documents.get(0).getVersion() + "\"");
	}

	@Test
	public void testUpdateNowFlushMode() throws Exception {
		Response response = doPutQuery("NOW", minDocumentWithoutAcesToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).isNotEmpty();

		List<Record> documents = searchServices.search(new LogicalSearchQuery(
				from(rm.document.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(id)));
		assertThat(documents.get(0).getTitle()).isEqualTo(minDocumentWithoutAcesToUpdate.getTitle());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + documents.get(0).getVersion() + "\"");
	}

	@Test
	public void testUpdateLaterFlushMode() throws Exception {
		Response response = doPutQuery("LATER", minDocumentWithoutAcesToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		Record record = recordServices.getDocumentById(minDocumentWithoutAcesToUpdate.getId());
		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + record.getVersion() + "\"");

		minDocumentToUpdate.setTitle(null);
		minDocumentToUpdate.setAuthor("newAuthor2");
		response = doPatchQuery("NOW", minDocumentToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).hasSize(1);

		List<Record> documents = searchServices.search(new LogicalSearchQuery(
				from(rm.document.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(id)));
		assertThat(documents.get(0).getTitle()).isEqualTo(minDocumentWithoutAcesToUpdate.getTitle());
		assertThat(documents.get(0).get(rm.document.author())).isEqualTo(minDocumentToUpdate.getAuthor());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + documents.get(0).getVersion() + "\"");
	}

	@Test
	public void testUpdateWithEtag() throws Exception {
		Record document = recordServices.getDocumentById(minDocumentWithoutAcesToUpdate.getId());
		String eTag = String.valueOf(document.getVersion());

		Response response = buildPutQuery().request().header("host", host).header(HttpHeaders.IF_MATCH, eTag)
				.put(entity(buildMultiPart(minDocumentWithoutAcesToUpdate), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		DocumentDto documentDto = response.readEntity(DocumentDto.class);
		assertThat(documentDto.getTitle()).isEqualTo(minDocumentWithoutAcesToUpdate.getTitle());
	}

	@Test
	public void testUpdateWithQuotedEtag() throws Exception {
		Record document = recordServices.getDocumentById(minDocumentWithoutAcesToUpdate.getId());
		String eTag = "\"".concat(String.valueOf(document.getVersion())).concat("\"");

		Response response = buildPutQuery().request().header("host", host).header(HttpHeaders.IF_MATCH, eTag)
				.put(entity(buildMultiPart(minDocumentWithoutAcesToUpdate), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		DocumentDto documentDto = response.readEntity(DocumentDto.class);
		assertThat(documentDto.getTitle()).isEqualTo(minDocumentWithoutAcesToUpdate.getTitle());
	}

	@Test
	public void testUpdateWithOldEtag() throws Exception {
		Record document = recordServices.getDocumentById(minDocumentWithoutAcesToUpdate.getId());
		String eTag = String.valueOf(document.getVersion());

		Response response = buildPutQuery().request().header("host", host).header(HttpHeaders.IF_MATCH, eTag)
				.put(entity(buildMultiPart(minDocumentWithoutAcesToUpdate), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		String validEtag = response.getEntityTag().getValue();

		minDocumentWithoutAcesToUpdate.setTitle("aNewTitle2");
		response = buildPutQuery().request().header("host", host).header(HttpHeaders.IF_MATCH, eTag)
				.put(entity(buildMultiPart(minDocumentWithoutAcesToUpdate), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.PRECONDITION_FAILED.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new OptimisticLockException(id, eTag, Long.valueOf(validEtag)).getValidationError()));
	}

	@Test
	public void testUpdateWithInvalidEtag() throws Exception {
		Response response = buildPutQuery().request().header("host", host).header(HttpHeaders.IF_MATCH, "invalidEtag")
				.put(entity(buildMultiPart(minDocumentWithoutAcesToUpdate), MULTIPART_FORM_DATA_TYPE));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("ETag", "invalidEtag").getValidationError()));
	}

	@Test
	public void testUpdateWithUnallowedHostHeader() throws Exception {
		host = "fakedns.com";
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(new UnallowedHostException(host).getValidationError()));
	}

	@Test
	public void testUpdateWithAllowedHost() throws Exception {
		host = "localhost2";
		Response response = doPutQuery(minDocumentToUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
	}

	//
	// PRIVATE FUNCTIONS
	//

	private Response doPutQuery(DocumentDto document, File file, String... excludedParam) throws Exception {
		return doPutQuery(document, file, true, excludedParam);
	}

	private Response doPutQuery(String flushMode, DocumentDto document, File file, String... excludedParam)
			throws Exception {
		return doPutQuery(document, file, flushMode, null, true, excludedParam);
	}

	private Response doPutQuery(Set<String> filters, DocumentDto document, File file, String... excludedParam)
			throws Exception {
		return doPutQuery(document, file, null, filters, true, excludedParam);
	}

	private Response doPutQuery(DocumentDto document, File file, boolean calculateSignature, String... excludedParam)
			throws Exception {
		return doPutQuery(document, file, null, null, calculateSignature, excludedParam);
	}

	private Response doPutQuery(DocumentDto document, File file, String flushMode, Set<String> filters,
								boolean calculateSignature, String... excludedParam) throws Exception {
		WebTarget webTarget = buildPutQuery(calculateSignature, excludedParam);
		if (filters != null && !filters.isEmpty()) {
			webTarget = webTarget.queryParam("filter", filters.toArray());
		}

		Invocation.Builder webTargetBuilder = webTarget.request().header("host", host);
		if (flushMode != null) {
			webTargetBuilder.header(CustomHttpHeaders.FLUSH_MODE, flushMode);
		}
		return webTargetBuilder.put(entity(buildMultiPart(document, file), MULTIPART_FORM_DATA_TYPE));
	}

	private WebTarget buildPutQuery(String... excludedParam) throws Exception {
		return buildPutQuery(true, excludedParam);
	}

	private WebTarget buildPutQuery(boolean calculateSignature, String... excludedParam) throws Exception {
		method = !HttpMethods.contains(method) ? "fakeMethod" : PUT;
		return buildQuery(webTarget, calculateSignature, asList("id", "serviceKey", "method", "date", "expiration", "signature"), excludedParam);
	}

	private Response doPatchQuery(String flushMode, DocumentDto document) throws Exception {
		method = HttpMethods.PATCH;
		return buildQuery(webTarget, true, asList("id", "serviceKey", "method", "date", "expiration", "signature"))
				.request().header("host", host).header(CustomHttpHeaders.FLUSH_MODE, flushMode)
				.build("PATCH", entity(buildMultiPart(document), MULTIPART_FORM_DATA_TYPE)).invoke();
	}

}
