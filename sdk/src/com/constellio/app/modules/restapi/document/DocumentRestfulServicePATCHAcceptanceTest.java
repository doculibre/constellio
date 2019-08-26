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
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.restapi.core.exception.mapper.RestApiErrorResponse;
import com.constellio.app.modules.restapi.core.util.CustomHttpHeaders;
import com.constellio.app.modules.restapi.core.util.DateUtils;
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
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.constellio.app.modules.restapi.core.util.HttpMethods.PATCH;
import static com.constellio.app.modules.restapi.core.util.Permissions.READ;
import static com.constellio.app.modules.restapi.document.enumeration.VersionType.MAJOR;
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

public class DocumentRestfulServicePATCHAcceptanceTest extends BaseDocumentRestfulServiceAcceptanceTest {

	private DocumentDto documentToPartialUpdate;
	private File fileToUpdate;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		documentToPartialUpdate = DocumentDto.builder().id(id).build();
		fileToUpdate = newTempFileWithContent(fakeFilename, fakeFileContentV1);
	}

	@Test
	public void testPartialUpdateDocument() throws Exception {
		String value1 = "value1";
		List<String> value2 = asList("value2a", "value2b");
		addUsrMetadata(MetadataValueType.STRING, value1, value2);
		resetCounters();

		DocumentDto docUpdate = DocumentDto.builder().id(id).title("aNewTitle").folderId(records.folder_A42)
				.author("aNewAut").organization("aNewOrg").subject("aNewSub").keywords(asList("z", "x")).build();
		Response response = doPatchQuery(docUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(commitCounter.newCommitsCall()).isEmpty();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(singletonList(doc)).extracting("id", "title", "folderId", "author", "keywords", "subject", "organization")
				.containsExactly(tuple(docUpdate.getId(), docUpdate.getTitle(), docUpdate.getFolderId(), docUpdate.getAuthor(),
						docUpdate.getKeywords(), docUpdate.getSubject(), docUpdate.getOrganization()));
		assertThat(singletonList(doc.getType())).extracting("id", "code", "title")
				.containsExactly(tuple(fakeDocumentType.getId(), fakeDocumentType.getCode(), fakeDocumentType.getTitle()));
		assertThat(singletonList(doc.getContent())).extracting("versionType", "filename", "hash", "version")
				.containsExactly(tuple(MAJOR, fakeFilename, dataSummaryV2.getHash(), "2.0"));
		assertThat(doc.getDirectAces()).extracting("principals", "permissions").contains(
				tuple(toPrincipals(authorization1.getPrincipals()), Sets.newHashSet(authorization1.getRoles())),
				tuple(toPrincipals(authorization2.getPrincipals()), Sets.newHashSet(authorization2.getRoles())));
		assertThat(doc.getExtendedAttributes()).extracting("key", "values")
				.containsOnly(tuple(fakeMetadata1, singletonList(value1)), tuple(fakeMetadata2, value2));

		Record record = recordServices.getDocumentById(docUpdate.getId());
		assertThatRecord(record).extracting(Document.TITLE, Document.FOLDER, Document.KEYWORDS, Document.AUTHOR, Document.COMPANY, Document.SUBJECT, Document.TYPE)
				.containsExactly(doc.getTitle(), doc.getFolderId(), doc.getKeywords(), doc.getAuthor(), doc.getOrganization(), doc.getSubject(), doc.getType().getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(doc.getExtendedAttributes().get(0).getValues());
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(doc.getExtendedAttributes().get(1).getValues());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + record.getVersion() + "\"");

		Content content = record.get(rm.document.content());
		assertThat(singletonList(content.getCurrentVersion())).extracting("version", "filename", "hash", "major")
				.containsExactly(tuple(doc.getContent().getVersion(), doc.getContent().getFilename(), doc.getContent().getHash(),
						doc.getContent().getVersionType() == MAJOR));

		List<Authorization> authorizations = filterInheritedAuthorizations(authorizationsServices.getRecordAuthorizations(record), record.getId());
		assertThat(authorizations).extracting("principals").containsOnly(
				singletonList(authorization1.getPrincipals().get(0)), singletonList(authorization2.getPrincipals().get(0)));
		assertThat(authorizations).extracting("roles", "startDate", "endDate").containsOnly(
				tuple(authorization1.getRoles(), authorization1.getStart(), authorization1.getEnd()),
				tuple(authorization2.getRoles(), authorization2.getStart(), authorization2.getEnd()));
	}

	@Test
	public void testPartialUpdateDocumentTypeOnly() throws Exception {
		String value1 = "value1";
		List<String> value2 = asList("value2a", "value2b");
		addUsrMetadata(MetadataValueType.STRING, value1, value2);

		DocumentDto docUpdate = DocumentDto.builder().id(id).type(DocumentTypeDto.builder().id(records.documentTypeId_4).build()).build();
		Response response = doPatchQuery(docUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentType documentType = rm.wrapDocumentType(recordServices.getDocumentById(records.documentTypeId_4));

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(singletonList(doc)).extracting("id", "title", "folderId", "author", "keywords", "subject", "organization")
				.containsExactly(tuple(fakeDocument.getId(), fakeDocument.getTitle(), fakeDocument.getFolder(), fakeDocument.getAuthor(),
						fakeDocument.getKeywords(), fakeDocument.getSubject(), fakeDocument.getCompany()));
		assertThat(singletonList(doc.getType())).extracting("id", "code", "title")
				.containsExactly(tuple(records.documentTypeId_4, documentType.getCode(), documentType.getTitle()));
		assertThat(singletonList(doc.getContent())).extracting("versionType", "filename", "hash", "version")
				.containsExactly(tuple(MAJOR, fakeFilename, dataSummaryV2.getHash(), "2.0"));
		assertThat(doc.getDirectAces()).extracting("principals", "permissions").contains(
				tuple(toPrincipals(authorization1.getPrincipals()), Sets.newHashSet(authorization1.getRoles())),
				tuple(toPrincipals(authorization2.getPrincipals()), Sets.newHashSet(authorization2.getRoles())));
		assertThat(doc.getExtendedAttributes()).extracting("key", "values")
				.containsOnly(tuple(fakeMetadata1, singletonList(value1)), tuple(fakeMetadata2, value2));

		Record record = recordServices.getDocumentById(docUpdate.getId());
		assertThatRecord(record).extracting(Document.TITLE, Document.FOLDER, Document.KEYWORDS, Document.AUTHOR, Document.COMPANY, Document.SUBJECT, Document.TYPE)
				.containsExactly(doc.getTitle(), doc.getFolderId(), doc.getKeywords(), doc.getAuthor(), doc.getOrganization(), doc.getSubject(), doc.getType().getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(doc.getExtendedAttributes().get(0).getValues());
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(doc.getExtendedAttributes().get(1).getValues());

		Content content = record.get(rm.document.content());
		assertThat(singletonList(content.getCurrentVersion())).extracting("version", "filename", "hash", "major")
				.containsExactly(tuple(doc.getContent().getVersion(), doc.getContent().getFilename(), doc.getContent().getHash(),
						doc.getContent().getVersionType() == MAJOR));

		List<Authorization> authorizations = filterInheritedAuthorizations(authorizationsServices.getRecordAuthorizations(record), record.getId());
		assertThat(authorizations).extracting("principals").containsOnly(
				singletonList(authorization1.getPrincipals().get(0)), singletonList(authorization2.getPrincipals().get(0)));
		assertThat(authorizations).extracting("roles", "startDate", "endDate").containsOnly(
				tuple(authorization1.getRoles(), authorization1.getStart(), authorization1.getEnd()),
				tuple(authorization2.getRoles(), authorization2.getStart(), authorization2.getEnd()));
	}

	@Test
	public void testPartialUpdateDocumentContentOnly() throws Exception {
		String value1 = "value1";
		List<String> value2 = asList("value2a", "value2b");
		addUsrMetadata(MetadataValueType.STRING, value1, value2);

		DocumentDto docUpdate = DocumentDto.builder().id(id).content(ContentDto.builder().versionType(MAJOR).build()).build();
		Response response = doPatchQuery(docUpdate, fileToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(singletonList(doc)).extracting("id", "title", "folderId", "author", "keywords", "subject", "organization")
				.containsExactly(tuple(fakeDocument.getId(), fakeDocument.getTitle(), fakeDocument.getFolder(), fakeDocument.getAuthor(),
						fakeDocument.getKeywords(), fakeDocument.getSubject(), fakeDocument.getCompany()));
		assertThat(singletonList(doc.getType())).extracting("id", "code", "title")
				.containsExactly(tuple(fakeDocumentType.getId(), fakeDocumentType.getCode(), fakeDocumentType.getTitle()));
		assertThat(singletonList(doc.getContent())).extracting("versionType", "filename", "hash", "version")
				.containsExactly(tuple(MAJOR, fakeFilename, dataSummaryV1.getHash(), "3.0"));
		assertThat(doc.getDirectAces()).extracting("principals", "permissions").contains(
				tuple(toPrincipals(authorization1.getPrincipals()), Sets.newHashSet(authorization1.getRoles())),
				tuple(toPrincipals(authorization2.getPrincipals()), Sets.newHashSet(authorization2.getRoles())));
		assertThat(doc.getExtendedAttributes()).extracting("key", "values")
				.containsOnly(tuple(fakeMetadata1, singletonList(value1)), tuple(fakeMetadata2, value2));

		Record record = recordServices.getDocumentById(docUpdate.getId());
		assertThatRecord(record).extracting(Document.TITLE, Document.FOLDER, Document.KEYWORDS, Document.AUTHOR, Document.COMPANY, Document.SUBJECT, Document.TYPE)
				.containsExactly(doc.getTitle(), doc.getFolderId(), doc.getKeywords(), doc.getAuthor(), doc.getOrganization(), doc.getSubject(), doc.getType().getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(doc.getExtendedAttributes().get(0).getValues());
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(doc.getExtendedAttributes().get(1).getValues());

		Content content = record.get(rm.document.content());
		assertThat(singletonList(content.getCurrentVersion())).extracting("version", "filename", "hash", "major")
				.containsExactly(tuple(doc.getContent().getVersion(), doc.getContent().getFilename(), doc.getContent().getHash(), true));

		List<Authorization> authorizations = filterInheritedAuthorizations(authorizationsServices.getRecordAuthorizations(record), record.getId());
		assertThat(authorizations).extracting("principals").containsOnly(
				singletonList(authorization1.getPrincipals().get(0)), singletonList(authorization2.getPrincipals().get(0)));
		assertThat(authorizations).extracting("roles", "startDate", "endDate").containsOnly(
				tuple(authorization1.getRoles(), authorization1.getStart(), authorization1.getEnd()),
				tuple(authorization2.getRoles(), authorization2.getStart(), authorization2.getEnd()));
	}

	@Test
	public void testPartialUpdateDocumentAceOnly() throws Exception {
		String value1 = "value1";
		List<String> value2 = asList("value2a", "value2b");
		addUsrMetadata(MetadataValueType.STRING, value1, value2);
		resetCounters();

		DocumentDto docUpdate = DocumentDto.builder().id(id)
				.directAces(singletonList(AceDto.builder().principals(singleton(chuckNorris)).permissions(singleton(READ)).build()))
				.build();
		Response response = doPatchQuery(docUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).hasSize(4);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(singletonList(doc)).extracting("id", "title", "folderId", "author", "keywords", "subject", "organization")
				.containsExactly(tuple(fakeDocument.getId(), fakeDocument.getTitle(), fakeDocument.getFolder(), fakeDocument.getAuthor(),
						fakeDocument.getKeywords(), fakeDocument.getSubject(), fakeDocument.getCompany()));
		assertThat(singletonList(doc.getType())).extracting("id", "code", "title")
				.containsExactly(tuple(fakeDocumentType.getId(), fakeDocumentType.getCode(), fakeDocumentType.getTitle()));
		assertThat(singletonList(doc.getContent())).extracting("versionType", "filename", "hash", "version")
				.containsExactly(tuple(MAJOR, fakeFilename, dataSummaryV2.getHash(), "2.0"));

		assertThat(doc.getDirectAces()).extracting("principals", "permissions").contains(
				tuple(Sets.newHashSet(docUpdate.getDirectAces().get(0).getPrincipals()), Sets.newHashSet(docUpdate.getDirectAces().get(0).getPermissions())));
		assertThat(doc.getExtendedAttributes()).extracting("key", "values")
				.containsOnly(tuple(fakeMetadata1, singletonList(value1)), tuple(fakeMetadata2, value2));

		Record record = recordServices.getDocumentById(docUpdate.getId());
		assertThatRecord(record).extracting(Document.TITLE, Document.FOLDER, Document.KEYWORDS, Document.AUTHOR, Document.COMPANY, Document.SUBJECT, Document.TYPE)
				.containsExactly(doc.getTitle(), doc.getFolderId(), doc.getKeywords(), doc.getAuthor(), doc.getOrganization(), doc.getSubject(), doc.getType().getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(doc.getExtendedAttributes().get(0).getValues());
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(doc.getExtendedAttributes().get(1).getValues());

		assertThat(response.getHeaderString("ETag")).isNull();

		Content content = record.get(rm.document.content());
		assertThat(singletonList(content.getCurrentVersion())).extracting("version", "filename", "hash", "major")
				.containsExactly(tuple(doc.getContent().getVersion(), doc.getContent().getFilename(), doc.getContent().getHash(), true));

		List<Authorization> authorizations = filterInheritedAuthorizations(authorizationsServices.getRecordAuthorizations(record), record.getId());
		assertThat(authorizations).extracting("principals").usingElementComparator(comparingListAnyOrder).containsOnly(
				toPrincipalIds(docUpdate.getDirectAces().get(0).getPrincipals()));
		assertThat(authorizations).extracting("roles").usingElementComparator(comparingListAnyOrder).containsOnly(
				Lists.newArrayList(docUpdate.getDirectAces().get(0).getPermissions()));
		assertThat(authorizations).extracting("startDate", "endDate").containsOnly(
				tuple(authorization1.getStart(), authorization1.getEnd()),
				tuple(authorization2.getStart(), authorization2.getEnd()));
	}

	@Test
	public void testPartialUpdateDocumentExtendedAttributesOnly() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, "value1", asList("value2a", "value2b"));

		List<String> newValue = singletonList("value3");
		DocumentDto docUpdate = DocumentDto.builder().id(id).extendedAttributes(
				singletonList(ExtendedAttributeDto.builder().key(fakeMetadata2).values(newValue).build())).build();
		Response response = doPatchQuery(docUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(singletonList(doc)).extracting("id", "title", "folderId", "author", "keywords", "subject", "organization")
				.containsExactly(tuple(fakeDocument.getId(), fakeDocument.getTitle(), fakeDocument.getFolder(), fakeDocument.getAuthor(),
						fakeDocument.getKeywords(), fakeDocument.getSubject(), fakeDocument.getCompany()));
		assertThat(singletonList(doc.getType())).extracting("id", "code", "title")
				.containsExactly(tuple(fakeDocumentType.getId(), fakeDocumentType.getCode(), fakeDocumentType.getTitle()));
		assertThat(singletonList(doc.getContent())).extracting("versionType", "filename", "hash", "version")
				.containsExactly(tuple(MAJOR, fakeFilename, dataSummaryV2.getHash(), "2.0"));
		assertThat(doc.getDirectAces()).extracting("principals", "permissions").contains(
				tuple(toPrincipals(authorization1.getPrincipals()), Sets.newHashSet(authorization1.getRoles())),
				tuple(toPrincipals(authorization2.getPrincipals()), Sets.newHashSet(authorization2.getRoles())));
		assertThat(doc.getExtendedAttributes()).extracting("key", "values").containsOnly(tuple(fakeMetadata2, newValue));

		Record record = recordServices.getDocumentById(docUpdate.getId());
		assertThatRecord(record).extracting(Document.TITLE, Document.FOLDER, Document.KEYWORDS, Document.AUTHOR, Document.COMPANY, Document.SUBJECT, Document.TYPE)
				.containsExactly(doc.getTitle(), doc.getFolderId(), doc.getKeywords(), doc.getAuthor(), doc.getOrganization(), doc.getSubject(), doc.getType().getId());
		assertThatRecord(record).extracting(fakeMetadata1).containsNull();
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(newValue);

		Content content = record.get(rm.document.content());
		assertThat(singletonList(content.getCurrentVersion())).extracting("version", "filename", "hash", "major")
				.containsExactly(tuple(doc.getContent().getVersion(), doc.getContent().getFilename(), doc.getContent().getHash(),
						doc.getContent().getVersionType() == MAJOR));

		List<Authorization> authorizations = filterInheritedAuthorizations(authorizationsServices.getRecordAuthorizations(record), record.getId());
		assertThat(authorizations).extracting("principals").containsOnly(
				singletonList(authorization1.getPrincipals().get(0)), singletonList(authorization2.getPrincipals().get(0)));
		assertThat(authorizations).extracting("roles", "startDate", "endDate").containsOnly(
				tuple(authorization1.getRoles(), authorization1.getStart(), authorization1.getEnd()),
				tuple(authorization2.getRoles(), authorization2.getStart(), authorization2.getEnd()));
	}

	@Test
	public void testPartialUpdateDocumentContentOnlyOnDocumentWithoutContent() throws Exception {
		Document newDoc = rm.newDocument().setFolder(folderId).setTitle("Title");
		recordServices.add(newDoc);

		id = newDoc.getId();
		DocumentDto docUpdate = DocumentDto.builder().id(id).content(ContentDto.builder().filename(fakeFilename).versionType(MAJOR).build()).build();
		Response response = doPatchQuery(docUpdate, fileToUpdate);
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
	public void testPartialUpdateDocumentContentOnlyWithoutFilenameOnDocumentWithoutContent() throws Exception {
		Document newDoc = rm.newDocument().setFolder(folderId).setTitle("Title");
		recordServices.add(newDoc);

		id = newDoc.getId();
		DocumentDto docUpdate = DocumentDto.builder().id(id).content(ContentDto.builder().versionType(MAJOR).build()).build();
		Response response = doPatchQuery(docUpdate, fileToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RequiredParameterException("content.filename").getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentAllFilters() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		documentToPartialUpdate.setTitle("title2");
		documentToPartialUpdate.setContent(ContentDto.builder().versionType(MAJOR).build());
		Set<String> filters = newHashSet("folderId", "type", "content", "title", "keywords", "author", "subject",
				"organization", "directAces", "inheritedAces", "extendedAttributes");
		Response response = doPatchQuery(filters, documentToPartialUpdate, fileToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		DocumentDto documentDto = response.readEntity(DocumentDto.class);
		assertThat(documentDto.getId()).isNotNull();
		assertThat(singletonList(documentDto)).extracting("folderId", "type", "content", "title",
				"keywords", "author", "subject", "organization", "directAces", "inheritedAces", "extendedAttributes")
				.containsOnly(tuple(null, null, null, null, null, null, null, null, null, null, null));
	}

	@Test
	public void testPartialUpdateDocumentSomeFilters() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		documentToPartialUpdate.setTitle("title2");
		Set<String> filters = newHashSet("type", "content", "directAces", "inheritedAces", "extendedAttributes");
		Response response = doPatchQuery(filters, documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		DocumentDto documentDto = response.readEntity(DocumentDto.class);
		assertThat(documentDto.getId()).isNotNull();
		assertThat(singletonList(documentDto)).extracting("folderId", "type", "content", "title",
				"keywords", "author", "subject", "organization", "directAces", "inheritedAces", "extendedAttributes")
				.containsOnly(tuple(fakeDocument.getFolder(), null, null, documentToPartialUpdate.getTitle(), fakeDocument.getKeywords(),
						fakeDocument.getAuthor(), fakeDocument.getSubject(), fakeDocument.getCompany(), null, null, null));
	}

	@Test
	public void testPartialUpdateDocumentInvalidFilter() throws Exception {
		Response response = doPatchQuery(singleton("invalid"), documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("filter", "invalid").getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithDateUsr() throws Exception {
		addUsrMetadata(MetadataValueType.DATE, null, null);

		List<String> value1 = singletonList("2017-07-21"), value2 = asList("2017-07-22", "2018-07-23");
		documentToPartialUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getExtendedAttributes()).isEqualTo(documentToPartialUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(new LocalDate(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(new LocalDate(value2.get(0)), new LocalDate(value2.get(1))));
	}

	@Test
	public void testPartialUpdateDocumentWithDateUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("2017-07-21T00:00:00");
		addUsrMetadata(MetadataValueType.DATE, null, null);

		documentToPartialUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidDateFormatException(value1.get(0), dateFormat).getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithDateTimeUsr() throws Exception {
		addUsrMetadata(MetadataValueType.DATE_TIME, null, null);

		List<String> value1 = singletonList(toDateString(fakeDate));
		List<String> value2 = asList(toDateString(fakeDate), toDateString(fakeDate.plusDays(1)));
		documentToPartialUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getExtendedAttributes()).isEqualTo(documentToPartialUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(toLocalDateTime(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(toLocalDateTime(value2.get(0)), toLocalDateTime(value2.get(1))));
	}

	@Test
	public void testPartialUpdateDocumentWithDateTimeUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("2018-07-21T23:59:59.123-04:00");
		addUsrMetadata(MetadataValueType.DATE_TIME, null, null);

		documentToPartialUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidDateFormatException(value1.get(0), dateTimeFormat).getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithNumberUsr() throws Exception {
		addUsrMetadata(MetadataValueType.NUMBER, null, null);

		List<String> value1 = singletonList("123.456"), value2 = asList("2018.24", "2018.25");
		documentToPartialUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getExtendedAttributes()).isEqualTo(documentToPartialUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(Double.valueOf(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(Double.valueOf(value2.get(0)), Double.valueOf(value2.get(1))));
	}

	@Test
	public void testPartialUpdateDocumentWithNumberUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("not-a-number");
		addUsrMetadata(MetadataValueType.NUMBER, null, null);

		documentToPartialUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidMetadataValueException(MetadataValueType.NUMBER.name(), value1.get(0)).getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithBooleanUsr() throws Exception {
		addUsrMetadata(MetadataValueType.BOOLEAN, null, null);

		List<String> value1 = singletonList("true"), value2 = asList("true", "false");
		documentToPartialUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getExtendedAttributes()).isEqualTo(documentToPartialUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(Boolean.valueOf(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(Boolean.valueOf(value2.get(0)), Boolean.valueOf(value2.get(1))));
	}

	@Test
	public void testPartialUpdateDocumentWithBooleanUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("null");
		addUsrMetadata(MetadataValueType.BOOLEAN, null, null);

		documentToPartialUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidMetadataValueException(MetadataValueType.BOOLEAN.name(), value1.get(0)).getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithTextUsr() throws Exception {
		addUsrMetadata(MetadataValueType.TEXT, null, null);

		List<String> value1 = singletonList("<html>"), value2 = asList("<b>bold", "test@test.com");
		documentToPartialUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getExtendedAttributes()).isEqualTo(documentToPartialUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(value1);
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(value2);
	}

	@Test
	public void testPartialUpdateDocumentWithReferenceUsr() throws Exception {
		addUsrMetadata(MetadataValueType.REFERENCE, null, null);

		List<String> value1 = singletonList(records.getAlice().getId());
		List<String> value2 = asList(records.getChuckNorris().getId(), records.getAlice().getId());
		documentToPartialUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getExtendedAttributes()).isEqualTo(documentToPartialUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(value1);
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(value2);
	}

	@Test
	public void testPartialUpdateDocumentWithReferenceUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("fake id");
		addUsrMetadata(MetadataValueType.REFERENCE, null, null);

		documentToPartialUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(value1.get(0)).getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithReferenceUsrAndInvalidSchemaType() throws Exception {
		List<String> value1 = singletonList(records.folder_A18);
		addUsrMetadata(MetadataValueType.REFERENCE, null, null);

		documentToPartialUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataReferenceNotAllowedException("folder", fakeMetadata1).getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithSchemaChangeByTypeCode() throws Exception {
		Record documentType = recordServices.getDocumentById(records.documentTypeId_10);
		documentToPartialUpdate.setType(DocumentTypeDto.builder().code(documentType.<String>get(Schemas.CODE)).build());

		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getId()).isEqualTo(documentToPartialUpdate.getId());
		assertThat(doc.getType().getId()).isEqualTo(documentType.getId());
		assertThat(doc.getType().getCode()).isEqualTo(documentType.<String>get(Schemas.CODE));
		assertThat(doc.getType().getTitle()).isEqualTo(documentType.get(rm.ddvDocumentType.title()));

		Record documentRecord = recordServices.getDocumentById(doc.getId());
		assertThat(documentRecord.getId()).isEqualTo(doc.getId());
		assertThatRecord(documentRecord).extracting(Document.TYPE).isEqualTo(singletonList(records.documentTypeId_10));
	}

	@Test
	public void testPartialUpdateDocumentWithSchemaChangeAndInvalidMetadataKey() throws Exception {
		switchToCustomSchema(fakeDocument.getId());
		addUsrMetadata(id, records.documentTypeForm().getLinkedSchema(), MetadataValueType.STRING, null, null);

		documentToPartialUpdate.setType(DocumentTypeDto.builder().id(records.documentTypeId_9).build());
		documentToPartialUpdate.setExtendedAttributes(singletonList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList("value1b")).build()));

		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataNotFoundException(fakeMetadata1).getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithMissingId() throws Exception {
		Response response = doPatchQuery(documentToPartialUpdate, null, "id");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "id"));
	}

	@Test
	public void testPartialUpdateDocumentWithInvalidId() throws Exception {
		id = "fakeId";
		documentToPartialUpdate.setId(id);
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(id).getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithMissingServiceKey() throws Exception {
		Response response = doPatchQuery(documentToPartialUpdate, null, "serviceKey");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "serviceKey"));
	}

	@Test
	public void testPartialUpdateDocumentWithInvalidServiceKey() throws Exception {
		serviceKey = "fakeServiceKey";
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithMissingMethod() throws Exception {
		Response response = doPatchQuery(documentToPartialUpdate, null, "method");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "method"));
	}

	@Test
	public void testPartialUpdateDocumentWithInvalidMethod() throws Exception {
		method = "fakeMethod";
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("method", method).getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithMissingDate() throws Exception {
		Response response = doPatchQuery(documentToPartialUpdate, null, "date");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "date"));
	}

	@Test
	public void testPartialUpdateDocumentWithInvalidDate() throws Exception {
		date = "fakeDate";
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("date", date).getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithMissingExpiration() throws Exception {
		Response response = doPatchQuery(documentToPartialUpdate, null, "expiration");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "expiration"));
	}

	@Test
	public void testPartialUpdateDocumentWithInvalidExpiration() throws Exception {
		expiration = "fakeExpiration";
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(JERSEY_NOT_FOUND_MESSAGE);
	}

	@Test
	public void testPartialUpdateDocumentWithInvalidDateAndExpiration() throws Exception {
		date = DateUtils.formatIsoNoMillis(TimeProvider.getLocalDateTime().minusDays(365));
		expiration = "1";
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ExpiredSignedUrlException().getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithMissingSignature() throws Exception {
		Response response = doPatchQuery(documentToPartialUpdate, null, "signature");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "signature"));
	}

	@Test
	public void testPartialUpdateDocumentWithInvalidSignature() throws Exception {
		signature = "fakeSignature";
		Response response = doPatchQuery(documentToPartialUpdate, null, false);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidSignatureException().getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithUserWithoutPermissions() throws Exception {
		serviceKey = sasquatchServiceKey;
		token = sasquatchToken;
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithMissingDocumentId() throws Exception {
		documentToPartialUpdate.setId(null);
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ParametersMustMatchException("id", "document.id").getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithInvalidDocumentId() throws Exception {
		documentToPartialUpdate.setId(records.document_B33);
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ParametersMustMatchException("id", "document.id").getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithInvalidTypeId() throws Exception {
		documentToPartialUpdate.setType(DocumentTypeDto.builder().id("fake").build());
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ResourceTypeNotFoundException("id", "fake").getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithInvalidTypeCode() throws Exception {
		documentToPartialUpdate.setType(DocumentTypeDto.builder().code("fake").build());
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ResourceTypeNotFoundException("code", "fake").getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithTypeIdAndTypeCode() throws Exception {
		documentToPartialUpdate.setType(DocumentTypeDto.builder().id("id").code("code").build());
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterCombinationException("type.id", "type.code").getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithContentAndMissingFile() throws Exception {
		documentToPartialUpdate.setContent(ContentDto.builder().versionType(MAJOR).filename("test.txt").build());
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "file"));
	}

	@Test
	public void testPartialUpdateDocumentWithFileAndMissingContent() throws Exception {
		Response response = doPatchQuery(documentToPartialUpdate, fileToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "document.content"));
	}

	@Test
	public void testPartialUpdateDocumentWithMissingContentVersionType() throws Exception {
		documentToPartialUpdate.setContent(ContentDto.builder().filename("test.txt").build());
		Response response = doPatchQuery(documentToPartialUpdate, fileToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "content.versionType"));
	}

	@Test
	public void testPartialUpdateDocumentWithMissingContentFilename() throws Exception {
		documentToPartialUpdate.setContent(ContentDto.builder().versionType(MAJOR).build());
		Response response = doPatchQuery(documentToPartialUpdate, fileToUpdate);
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
	public void testPartialUpdateDocumentWithInvalidAcePrincipal() throws Exception {
		documentToPartialUpdate.setDirectAces(singletonList(AceDto.builder().principals(singleton("fake")).permissions(singleton(READ)).build()));
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException("fake").getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithInvalidAcePermissions() throws Exception {
		documentToPartialUpdate.setDirectAces(singletonList(AceDto.builder()
				.principals(singleton(records.getAlice().getId())).permissions(singleton("fake")).build()));
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("directAces[0].permissions", "fake").getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithStartDateGreaterThanEndDate() throws Exception {
		String start = toDateString(new LocalDate().plusDays(365));
		String end = toDateString(new LocalDate());
		documentToPartialUpdate.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice))
				.permissions(singleton(READ)).startDate(start).endDate(end).build()));
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(new InvalidDateCombinationException(start, end).getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithStartDateOnly() throws Exception {
		documentToPartialUpdate.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice))
				.permissions(singleton(READ)).startDate(toDateString(new LocalDate())).build()));
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getDirectAces().get(0).getStartDate()).isEqualTo(documentToPartialUpdate.getDirectAces().get(0).getStartDate());
		assertThat(doc.getDirectAces().get(0).getEndDate()).isNull();

		Record record = recordServices.getDocumentById(doc.getId());
		List<Authorization> authorizations = filterInheritedAuthorizations(authorizationsServices.getRecordAuthorizations(record), record.getId());
		assertThat(authorizations).extracting("startDate").containsOnly(toLocalDate(documentToPartialUpdate.getDirectAces().get(0).getStartDate()));
		assertThat(authorizations).extracting("endDate").containsNull();
	}

	@Test
	public void testPartialUpdateDocumentWithEndDateOnly() throws Exception {
		documentToPartialUpdate.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice))
				.permissions(singleton(READ)).endDate(toDateString(new LocalDate())).build()));
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(new RequiredParameterException("ace.startDate").getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithInvalidExtAttributeKey() throws Exception {
		documentToPartialUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key("fake").values(singletonList("123")).build()));
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataNotFoundException("fake").getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithInvalidExtAttributeMultiValue() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		documentToPartialUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(asList("ab", "cd")).build()));
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataNotMultivalueException(fakeMetadata1).getValidationError()));
	}

	@Test
	public void testPartialUpdateDocumentWithEmptyExtAttributeValues() throws Exception {
		documentToPartialUpdate.setExtendedAttributes(singletonList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(Collections.<String>emptyList()).build()));
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_EMPTY_MESSAGE, "extendedAttributes[0].values"));
	}

	@Test
	public void testPartialUpdateDocumentWithCustomSchema() throws Exception {
		switchToCustomSchema(fakeDocument.getId());
		addUsrMetadata(id, records.documentTypeForm().getLinkedSchema(), MetadataValueType.STRING, null, null);

		List<String> value1 = singletonList("value11"), value2 = asList("value21", "value22");
		documentToPartialUpdate.setType(DocumentTypeDto.builder().id(records.documentTypeForm().getId()).build());
		documentToPartialUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));

		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);

		DocumentDto doc = response.readEntity(DocumentDto.class);
		assertThat(doc.getExtendedAttributes()).isEqualTo(documentToPartialUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(value1);
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(value2);
	}

	@Test
	public void testPartialUpdateDefaultFlushMode() throws Exception {
		documentToPartialUpdate.setTitle("title2");
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		List<Record> documents = searchServices.search(new LogicalSearchQuery(
				from(rm.document.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(id)));
		assertThat(documents.get(0).getTitle()).isNotEqualTo(documentToPartialUpdate.getTitle());

		Record document = recordServices.realtimeGetRecordById(documentToPartialUpdate.getId());
		assertThat(document.getTitle()).isEqualTo(documentToPartialUpdate.getTitle());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + document.getVersion() + "\"");

		TimeUnit.MILLISECONDS.sleep(5250);

		documents = searchServices.search(new LogicalSearchQuery(
				from(rm.document.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(id)));
		assertThat(documents.get(0).getTitle()).isEqualTo(documentToPartialUpdate.getTitle());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + documents.get(0).getVersion() + "\"");
	}

	@Test
	public void testPartialUpdateNowFlushMode() throws Exception {
		documentToPartialUpdate.setTitle("title2");
		Response response = doPatchQuery("NOW", documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).isNotEmpty();

		List<Record> documents = searchServices.search(new LogicalSearchQuery(
				from(rm.document.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(id)));
		assertThat(documents.get(0).getTitle()).isEqualTo(documentToPartialUpdate.getTitle());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + documents.get(0).getVersion() + "\"");
	}

	@Test
	public void testPartialUpdateLaterFlushMode() throws Exception {
		documentToPartialUpdate.setTitle("title2");
		Response response = doPatchQuery("LATER", documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		Record document = recordServices.getDocumentById(documentToPartialUpdate.getId());
		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + document.getVersion() + "\"");

		documentToPartialUpdate.setTitle(null);
		documentToPartialUpdate.setAuthor("author2");
		response = doPatchQuery("NOW", documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).hasSize(1);

		List<Record> documents = searchServices.search(new LogicalSearchQuery(
				from(rm.document.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(id)));
		assertThat(documents.get(0).getTitle()).isEqualTo("title2");
		assertThat(documents.get(0).get(rm.document.author())).isEqualTo("author2");

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + documents.get(0).getVersion() + "\"");
	}

	@Test
	public void testPartialUpdateWithEtag() throws Exception {
		Record document = recordServices.getDocumentById(documentToPartialUpdate.getId());
		String eTag = String.valueOf(document.getVersion());

		documentToPartialUpdate.setTitle("aNewTitle");
		Response response = buildPatchQuery().request().header("host", host).header(HttpHeaders.IF_MATCH, eTag)
				.build("PATCH", entity(buildMultiPart(documentToPartialUpdate), MULTIPART_FORM_DATA_TYPE)).invoke();
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		DocumentDto documentDto = response.readEntity(DocumentDto.class);
		assertThat(documentDto.getTitle()).isEqualTo(documentToPartialUpdate.getTitle());
	}

	@Test
	public void testPartialUpdateWithOldEtag() throws Exception {
		Record document = recordServices.getDocumentById(documentToPartialUpdate.getId());
		String eTag = String.valueOf(document.getVersion());

		documentToPartialUpdate.setTitle("aNewTitle");
		Response response = buildPatchQuery().request().header("host", host).header(HttpHeaders.IF_MATCH, eTag)
				.build("PATCH", entity(buildMultiPart(documentToPartialUpdate), MULTIPART_FORM_DATA_TYPE)).invoke();
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		String validEtag = response.getEntityTag().getValue();

		documentToPartialUpdate.setTitle("aNewTitle2");
		response = buildPatchQuery().request().header("host", host).header(HttpHeaders.IF_MATCH, eTag)
				.build("PATCH", entity(buildMultiPart(documentToPartialUpdate), MULTIPART_FORM_DATA_TYPE)).invoke();
		assertThat(response.getStatus()).isEqualTo(Response.Status.PRECONDITION_FAILED.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new OptimisticLockException(id, eTag, Long.valueOf(validEtag)).getValidationError()));
	}

	@Test
	public void testPartialUpdateWithInvalidEtag() throws Exception {
		Response response = buildPatchQuery().request().header("host", host).header(HttpHeaders.IF_MATCH, "invalidEtag")
				.build("PATCH", entity(buildMultiPart(documentToPartialUpdate), MULTIPART_FORM_DATA_TYPE)).invoke();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("ETag", "invalidEtag").getValidationError()));
	}

	@Test
	public void testPartialUpdateWithUnallowedHostHeader() throws Exception {
		host = "fakedns.com";
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(new UnallowedHostException(host).getValidationError()));
	}

	@Test
	public void testPartialUpdateWithAllowedHost() throws Exception {
		host = "localhost2";
		Response response = doPatchQuery(documentToPartialUpdate, null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
	}

	//
	// PRIVATE FUNCTIONS
	//

	private Response doPatchQuery(DocumentDto document, File file, String... excludedParam) throws Exception {
		return doPatchQuery(document, file, true, excludedParam);
	}

	private Response doPatchQuery(String flushMode, DocumentDto document, File file, String... excludedParam)
			throws Exception {
		return doPatchQuery(document, file, flushMode, null, true, excludedParam);
	}

	private Response doPatchQuery(Set<String> filters, DocumentDto document, File file, String... excludedParam)
			throws Exception {
		return doPatchQuery(document, file, null, filters, true, excludedParam);
	}

	private Response doPatchQuery(DocumentDto document, File file, boolean calculateSignature, String... excludedParam)
			throws Exception {
		return doPatchQuery(document, file, null, null, calculateSignature, excludedParam);
	}

	private Response doPatchQuery(DocumentDto document, File file, String flushMode, Set<String> filters,
								  boolean calculateSignature, String... excludedParam) throws Exception {
		WebTarget webTarget = buildPatchQuery(calculateSignature, excludedParam);
		if (filters != null && !filters.isEmpty()) {
			webTarget = webTarget.queryParam("filter", filters.toArray());
		}

		Invocation.Builder webTargetBuilder = webTarget.request().header("host", host);
		if (flushMode != null) {
			webTargetBuilder.header(CustomHttpHeaders.FLUSH_MODE, flushMode);
		}
		return webTargetBuilder.build("PATCH", entity(buildMultiPart(document, file), MULTIPART_FORM_DATA_TYPE)).invoke();
	}

	private WebTarget buildPatchQuery(String... excludedParam) throws Exception {
		return buildPatchQuery(true, excludedParam);
	}

	private WebTarget buildPatchQuery(boolean calculateSignature, String... excludedParam) throws Exception {
		method = !HttpMethods.contains(method) ? "fakeMethod" : PATCH;
		return buildQuery(webTarget, calculateSignature, asList("id", "serviceKey", "method", "date", "expiration", "signature"), excludedParam);
	}

}
