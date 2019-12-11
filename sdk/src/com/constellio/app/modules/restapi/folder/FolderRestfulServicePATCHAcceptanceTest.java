package com.constellio.app.modules.restapi.folder;

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
import com.constellio.app.modules.restapi.folder.dto.AdministrativeUnitDto;
import com.constellio.app.modules.restapi.folder.dto.CategoryDto;
import com.constellio.app.modules.restapi.folder.dto.ContainerDto;
import com.constellio.app.modules.restapi.folder.dto.FolderDto;
import com.constellio.app.modules.restapi.folder.dto.FolderTypeDto;
import com.constellio.app.modules.restapi.folder.dto.RetentionRuleDto;
import com.constellio.app.modules.restapi.resource.dto.AceDto;
import com.constellio.app.modules.restapi.resource.dto.ExtendedAttributeDto;
import com.constellio.app.modules.restapi.resource.exception.ResourceTypeNotFoundException;
import com.constellio.app.modules.restapi.validation.exception.ExpiredSignedUrlException;
import com.constellio.app.modules.restapi.validation.exception.InvalidSignatureException;
import com.constellio.app.modules.restapi.validation.exception.UnallowedHostException;
import com.constellio.app.modules.restapi.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.validation.exception.UnauthorizedAccessException;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.utils.TimeProvider;
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

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.constellio.app.modules.restapi.core.util.HttpMethods.PATCH;
import static com.constellio.app.modules.restapi.core.util.Permissions.READ;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static com.constellio.sdk.tests.TestUtils.comparingListAnyOrder;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class FolderRestfulServicePATCHAcceptanceTest extends BaseFolderRestfulServiceAcceptanceTest {
	private FolderDto folderToPatialUpdate;


	@Before
	public void setUp() throws Exception {
		super.setUp();

		folderToPatialUpdate = FolderDto.builder().id(id).build();
	}

	@Test
	public void testPartialUpdateFolderWithParentFolder() throws Exception {
		String value1 = "value1";
		List<String> value2 = asList("value2a", "value2b");
		addUsrMetadata(MetadataValueType.STRING, value1, value2);

		resetCounters();

		LocalDate localDate = new LocalDate(2000, 1, 1);

		FolderDto folderUpdate = FolderDto.builder().id(id).title("aNewTitle").parentFolderId(records.folder_A42).description("description")
				.openingDate(localDate).build();

		Response response = doPatchQuery(folderUpdate);

		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(commitCounter.newCommitsCall()).isEmpty();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);

		FolderDto folder = response.readEntity(FolderDto.class);

		assertThat(singletonList(folder)).extracting("id", "title", "parentFolderId", "description", "openingDate")
				.containsExactly(tuple(folderUpdate.getId(), folderUpdate.getTitle(), folderUpdate.getParentFolderId(), folderUpdate.getDescription(), folderUpdate.getOpeningDate()));

		assertFolderType(folder);

		assertThat(folder.getDirectAces()).extracting("principals", "permissions").contains(
				tuple(toPrincipals(authorization1.getPrincipals()), Sets.newHashSet(authorization1.getRoles())),
				tuple(toPrincipals(authorization2.getPrincipals()), Sets.newHashSet(authorization2.getRoles())));


		assertThat(folder.getExtendedAttributes()).extracting("key", "values")
				.containsOnly(tuple(fakeMetadata1, singletonList(value1)), tuple(fakeMetadata2, value2));

		Record record = recordServices.getDocumentById(folderUpdate.getId());
		assertThatRecord(record).extracting(Folder.TITLE, Folder.PARENT_FOLDER, Folder.KEYWORDS, Folder.TYPE)
				.containsExactly(folder.getTitle(), folder.getParentFolderId(), folder.getKeywords(), folder.getType().getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(folder.getExtendedAttributes().get(0).getValues());
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(folder.getExtendedAttributes().get(1).getValues());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + record.getVersion() + "\"");

		List<Authorization> authorizations = filterInheritedAuthorizations(authorizationsServices.getRecordAuthorizations(record), record.getId());
		assertThat(authorizations).extracting("principals").containsOnly(
				singletonList(authorization1.getPrincipals().get(0)), singletonList(authorization2.getPrincipals().get(0)));
		assertThat(authorizations).extracting("roles", "startDate", "endDate").containsOnly(
				tuple(authorization1.getRoles(), authorization1.getStart(), authorization1.getEnd()),
				tuple(authorization2.getRoles(), authorization2.getStart(), authorization2.getEnd()));
	}

	private void assertFolderType(FolderDto folder) {
		assertThat(singletonList(folder.getType())).extracting("id", "code", "title")
				.containsExactly(tuple(fakeFolderType.getId(), fakeFolderType.getCode(), fakeFolderType.getTitle()));
	}

	@Test
	public void testPartialUpdateFolderWithoutParentFolder() throws Exception {
		String value1 = "value1";
		List<String> value2 = asList("value2a", "value2b");
		addUsrMetadata(MetadataValueType.STRING, value1, value2);

		resetCounters();

		LocalDate localDate = new LocalDate(2000, 1, 1);

		FolderDto folderUpdate = FolderDto.builder().id(id).title("aNewTitle").description("description").category(CategoryDto.builder().id(records.categoryId_X120).build())
				.retentionRule(RetentionRuleDto.builder().id(records.ruleId_4).build()).administrativeUnit(AdministrativeUnitDto.builder().id(records.unitId_10a).build())
				.copyStatus(CopyType.PRINCIPAL.getCode()).mediumTypes(getMediumTypesCode(asList(rm.PA(), rm.DM()))).keywords(asList("keyword1", "keyword2"))
				.container(ContainerDto.builder().id(records.containerId_bac01).build()).closingDate(fakeDate2.toLocalDate())
				.openingDate(localDate).build();

		Response response = doPatchQuery(folderUpdate);

		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(commitCounter.newCommitsCall()).isEmpty();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);

		FolderDto folder = response.readEntity(FolderDto.class);

		assertFolderMetadata(folderUpdate, folder);
		assertFolderType(folder);

		assertThat(folder.getMainCopyRule()).isNotNull();
	}

	@Test
	public void testPartialUpdateFolderAceOnly() throws Exception {
		String value1 = "value1";
		List<String> value2 = asList("value2a", "value2b");
		addUsrMetadata(MetadataValueType.STRING, value1, value2);
		resetCounters();

		FolderDto folderUpdate = FolderDto.builder().id(id)
				.directAces(singletonList(AceDto.builder().principals(singleton(chuckNorris)).permissions(singleton(READ)).build()))
				.build();

		Response response = doPatchQuery(folderUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).hasSize(4);

		FolderDto folder = response.readEntity(FolderDto.class);

		assertFolderMetadata(folder, fakeFolder);
		assertFolderType(folder);

		assertThat(folder.getDirectAces()).extracting("principals", "permissions").contains(
				tuple(Sets.newHashSet(folderUpdate.getDirectAces().get(0).getPrincipals()), Sets.newHashSet(folderUpdate.getDirectAces().get(0).getPermissions())));
		assertThat(folder.getExtendedAttributes()).extracting("key", "values")
				.containsOnly(tuple(fakeMetadata1, singletonList(value1)), tuple(fakeMetadata2, value2));

		Folder folderFromSolr = rm.getFolder(folderUpdate.getId());

		assertFolderMetadata(folder, folderFromSolr);


		assertThatRecord(folderFromSolr.getWrappedRecord()).extracting(fakeMetadata1).isEqualTo(folder.getExtendedAttributes().get(0).getValues());
		assertThatRecord(folderFromSolr.getWrappedRecord()).extracting(fakeMetadata2).containsExactly(folder.getExtendedAttributes().get(1).getValues());

		assertThat(response.getHeaderString("ETag")).isNull();

		List<Authorization> authorizations = filterInheritedAuthorizations(authorizationsServices.getRecordAuthorizations(folderFromSolr.getWrappedRecord()), folderFromSolr.getId());
		assertThat(authorizations).extracting("principals").usingElementComparator(comparingListAnyOrder).containsOnly(
				toPrincipalIds(folderUpdate.getDirectAces().get(0).getPrincipals()));
		assertThat(authorizations).extracting("roles").usingElementComparator(comparingListAnyOrder).containsOnly(
				Lists.newArrayList(folderUpdate.getDirectAces().get(0).getPermissions()));
		assertThat(authorizations).extracting("startDate", "endDate").containsOnly(
				tuple(authorization1.getStart(), authorization1.getEnd()),
				tuple(authorization2.getStart(), authorization2.getEnd()));
	}

	@Test
	public void testPartialUpdateFolderExtendedAttributesOnly() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, "value1", asList("value2a", "value2b"));

		List<String> newValue = singletonList("value3");
		FolderDto folderUpdate = FolderDto.builder().id(id).extendedAttributes(
				singletonList(ExtendedAttributeDto.builder().key(fakeMetadata2).values(newValue).build())).build();
		Response response = doPatchQuery(folderUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		FolderDto folder = response.readEntity(FolderDto.class);

		assertFolderMetadata(folder, fakeFolder);

		assertFolderType(folder);

		assertThat(folder.getDirectAces()).extracting("principals", "permissions").contains(
				tuple(toPrincipals(authorization1.getPrincipals()), Sets.newHashSet(authorization1.getRoles())),
				tuple(toPrincipals(authorization2.getPrincipals()), Sets.newHashSet(authorization2.getRoles())));
		assertThat(folder.getExtendedAttributes()).extracting("key", "values").containsOnly(tuple(fakeMetadata2, newValue));

		Record record = recordServices.getDocumentById(folderUpdate.getId());
		assertFolderMetadata(folder, rm.wrapFolder(record));
		assertThatRecord(record).extracting(fakeMetadata1).containsNull();
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(newValue);

		List<Authorization> authorizations = filterInheritedAuthorizations(authorizationsServices.getRecordAuthorizations(record), record.getId());
		assertThat(authorizations).extracting("principals").containsOnly(
				singletonList(authorization1.getPrincipals().get(0)), singletonList(authorization2.getPrincipals().get(0)));
		assertThat(authorizations).extracting("roles", "startDate", "endDate").containsOnly(
				tuple(authorization1.getRoles(), authorization1.getStart(), authorization1.getEnd()),
				tuple(authorization2.getRoles(), authorization2.getStart(), authorization2.getEnd()));
	}

	@Test
	public void testPartialUpdateFolderAllFilters() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		folderToPatialUpdate.setTitle("title2");

		Set<String> filters = newHashSet("parentFolderId",
				"category",
				"retentionRule",
				"administrativeUnit",
				"mainCopyRule",
				"copyStatus",
				"mediumTypes",
				"mediaType",
				"container",
				"title",
				"description",
				"keywords",
				"openingDate",
				"closingDate",
				"actualDepositDate",
				"actualDestructionDate",
				"actualTransferDate",
				"expectedDepositDate",
				"expectedDestructionDate",
				"expectedTransferDate");

		Response response = doPatchQuery(filters, folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		FolderDto folderDto = response.readEntity(FolderDto.class);
		assertThat(folderDto.getId()).isNotNull();
		assertThat(singletonList(folderDto)).extracting("parentFolderId", "category",
				"retentionRule", "administrativeUnit", "mainCopyRule", "copyStatus", "mediumTypes", "mediaType", "container",
				"title", "description", "keywords", "openingDate", "closingDate", "actualDepositDate", "actualDestructionDate", "actualTransferDate",
				"expectedDepositDate", "expectedDestructionDate", "expectedTransferDate")
				.containsOnly(tuple(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null));
	}

	@Test
	public void testPartialUpdateFolderSomeFilters() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		folderToPatialUpdate.setTitle("title2");
		Set<String> filters = newHashSet("parentFolderId",
				"retentionRule", "mainCopyRule", "copyStatus", "mediumTypes", "mediaType", "container",
				"title", "keywords", "openingDate", "closingDate", "actualDepositDate", "actualDestructionDate", "actualTransferDate",
				"expectedDepositDate", "expectedDestructionDate", "expectedTransferDate");
		Response response = doPatchQuery(filters, folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		FolderDto folderDto = response.readEntity(FolderDto.class);
		assertThat(folderDto.getId()).isNotNull();
		assertThat(singletonList(folderDto)).extracting("parentFolderId", "retentionRule",
				"mainCopyRule", "copyStatus", "mediumTypes", "mediaType", "container",
				"title", "description", "keywords", "openingDate", "closingDate", "actualDepositDate", "actualDestructionDate", "actualTransferDate",
				"expectedDepositDate", "expectedDestructionDate", "expectedTransferDate")
				.containsOnly(tuple(null, null, null, null, null, null, null, null, fakeFolder.getDescription(), null, null, null, null, null, null, null, null, null));

		assertThat(folderDto.getCategory().getId()).isEqualTo(fakeFolder.getCategory());

		Category category = rm.getCategory(fakeFolder.getCategory());
		assertThat(folderDto.getCategory().getTitle()).isEqualTo(category.getTitle());
		assertThat(folderDto.getAdministrativeUnit().getId()).isEqualTo(fakeFolder.getAdministrativeUnit());
		AdministrativeUnit administrativeUnit = rm.getAdministrativeUnit(fakeFolder.getAdministrativeUnit());
		assertThat(folderDto.getAdministrativeUnit().getTitle()).isEqualTo(administrativeUnit.getTitle());
	}

	@Test
	public void testPartialUpdateFolderInvalidFilter() throws Exception {
		Response response = doPatchQuery(singleton("invalid"), folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("filter", "invalid").getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithDateUsr() throws Exception {
		addUsrMetadata(MetadataValueType.DATE, null, null);

		List<String> value1 = singletonList("2017-07-21"), value2 = asList("2017-07-22", "2018-07-23");
		folderToPatialUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		FolderDto folder = response.readEntity(FolderDto.class);
		assertThat(folder.getExtendedAttributes()).isEqualTo(folderToPatialUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(folder.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(new LocalDate(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(new LocalDate(value2.get(0)), new LocalDate(value2.get(1))));
	}

	@Test
	public void testPartialUpdateFolderWithDateUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("2017-07-21T00:00:00");
		addUsrMetadata(MetadataValueType.DATE, null, null);

		folderToPatialUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidDateFormatException(value1.get(0), dateFormat).getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithDateTimeUsr() throws Exception {
		addUsrMetadata(MetadataValueType.DATE_TIME, null, null);

		List<String> value1 = singletonList(toDateString(fakeDate));
		List<String> value2 = asList(toDateString(fakeDate), toDateString(fakeDate.plusDays(1)));
		folderToPatialUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		FolderDto doc = response.readEntity(FolderDto.class);
		assertThat(doc.getExtendedAttributes()).isEqualTo(folderToPatialUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(toLocalDateTime(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(toLocalDateTime(value2.get(0)), toLocalDateTime(value2.get(1))));
	}

	@Test
	public void testPartialUpdateFolderWithDateTimeUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("2018-07-21T23:59:59.123-04:00");
		addUsrMetadata(MetadataValueType.DATE_TIME, null, null);

		folderToPatialUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidDateFormatException(value1.get(0), dateTimeFormat).getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithNumberUsr() throws Exception {
		addUsrMetadata(MetadataValueType.NUMBER, null, null);

		List<String> value1 = singletonList("123.456"), value2 = asList("2018.24", "2018.25");
		folderToPatialUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		FolderDto doc = response.readEntity(FolderDto.class);
		assertThat(doc.getExtendedAttributes()).isEqualTo(folderToPatialUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(Double.valueOf(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(Double.valueOf(value2.get(0)), Double.valueOf(value2.get(1))));
	}


	@Test
	public void testPartialUpdateFolderWithNumberUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("not-a-number");
		addUsrMetadata(MetadataValueType.NUMBER, null, null);

		folderToPatialUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidMetadataValueException(MetadataValueType.NUMBER.name(), value1.get(0)).getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithBooleanUsr() throws Exception {
		addUsrMetadata(MetadataValueType.BOOLEAN, null, null);

		List<String> value1 = singletonList("true"), value2 = asList("true", "false");
		folderToPatialUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		FolderDto doc = response.readEntity(FolderDto.class);
		assertThat(doc.getExtendedAttributes()).isEqualTo(folderToPatialUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(Boolean.valueOf(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(Boolean.valueOf(value2.get(0)), Boolean.valueOf(value2.get(1))));
	}

	@Test
	public void testPartialUpdateFolderWithBooleanUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("null");
		addUsrMetadata(MetadataValueType.BOOLEAN, null, null);

		folderToPatialUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidMetadataValueException(MetadataValueType.BOOLEAN.name(), value1.get(0)).getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithTextUsr() throws Exception {
		addUsrMetadata(MetadataValueType.TEXT, null, null);

		List<String> value1 = singletonList("<html>"), value2 = asList("<b>bold", "test@test.com");
		folderToPatialUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		FolderDto doc = response.readEntity(FolderDto.class);
		assertThat(doc.getExtendedAttributes()).isEqualTo(folderToPatialUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(value1);
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(value2);
	}

	@Test
	public void testPartialUpdateFolderWithReferenceUsr() throws Exception {
		addUsrMetadata(MetadataValueType.REFERENCE, null, null);

		List<String> value1 = singletonList(records.getAlice().getId());
		List<String> value2 = asList(records.getChuckNorris().getId(), records.getAlice().getId());
		folderToPatialUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		FolderDto doc = response.readEntity(FolderDto.class);
		assertThat(doc.getExtendedAttributes()).isEqualTo(folderToPatialUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(value1);
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(value2);
	}

	@Test
	public void testPartialUpdateFolderWithReferenceUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("fake id");
		addUsrMetadata(MetadataValueType.REFERENCE, null, null);

		folderToPatialUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(value1.get(0)).getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithReferenceUsrAndInvalidSchemaType() throws Exception {
		List<String> value1 = singletonList(records.folder_A18);
		addUsrMetadata(MetadataValueType.REFERENCE, null, null);

		folderToPatialUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataReferenceNotAllowedException("folder", fakeMetadata1).getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithSchemaChangeByTypeCode() throws Exception {
		FolderType folderType = records.folderTypeEmploye();
		folderToPatialUpdate.setType(FolderTypeDto.builder().code(folderType.<String>get(Schemas.CODE)).build());

		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		FolderDto folder = response.readEntity(FolderDto.class);
		assertThat(folder.getId()).isEqualTo(folderToPatialUpdate.getId());
		assertThat(folder.getType().getId()).isEqualTo(folderType.getId());
		assertThat(folder.getType().getCode()).isEqualTo(folderType.<String>get(Schemas.CODE));
		assertThat(folder.getType().getTitle()).isEqualTo(folderType.get(rm.ddvFolderType.title()));

		Record folderRecord = recordServices.getDocumentById(folder.getId());
		assertThat(folderRecord.getId()).isEqualTo(folder.getId());
		assertThatRecord(folderRecord).extracting(Folder.TYPE).isEqualTo(singletonList(folderType.getId()));
	}

	@Test
	public void testPartialUpdateFolderWithSchemaChangeAndInvalidMetadataKey() throws Exception {
		switchToCustomSchema(fakeFolder.getId());

		FolderType folderType = records.folderTypeEmploye();

		addUsrMetadata(id, records.documentTypeForm().getLinkedSchema(), MetadataValueType.STRING, null, null);

		folderToPatialUpdate.setType(FolderTypeDto.builder().id(folderType.getId()).build());
		folderToPatialUpdate.setExtendedAttributes(singletonList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList("value1b")).build()));

		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataNotFoundException(fakeMetadata1).getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithMissingId() throws Exception {
		Response response = doPatchQuery(folderToPatialUpdate, "id");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "id"));
	}

	@Test
	public void testPartialUpdateFolderWithInvalidId() throws Exception {
		id = "fakeId";
		folderToPatialUpdate.setId(id);
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(id).getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithMissingMethod() throws Exception {
		Response response = doPatchQuery(folderToPatialUpdate, "method");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "method"));
	}

	@Test
	public void testPartialUpdateFolderWithInvalidMethod() throws Exception {
		method = "fakeMethod";
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("method", method).getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithMissingDate() throws Exception {
		Response response = doPatchQuery(folderToPatialUpdate, "date");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "date"));
	}

	@Test
	public void testPartialUpdateFolderWithMissingServiceKey() throws Exception {
		Response response = doPatchQuery(folderToPatialUpdate, "serviceKey");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "serviceKey"));
	}

	@Test
	public void testPartialUpdateFolderWithInvalidServiceKey() throws Exception {
		serviceKey = "fakeServiceKey";
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithInvalidDate() throws Exception {
		date = "fakeDate";
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("date", date).getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithMissingExpiration() throws Exception {
		Response response = doPatchQuery(folderToPatialUpdate, "expiration");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "expiration"));
	}

	@Test
	public void testPartialUpdateFolderWithInvalidExpiration() throws Exception {
		expiration = "fakeExpiration";
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(JERSEY_NOT_FOUND_MESSAGE);
	}


	@Test
	public void testPartialUpdateFolderWithInvalidDateAndExpiration() throws Exception {
		date = DateUtils.formatIsoNoMillis(TimeProvider.getLocalDateTime().minusDays(365));
		expiration = "1";
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ExpiredSignedUrlException().getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithMissingSignature() throws Exception {
		Response response = doPatchQuery(folderToPatialUpdate, "signature");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "signature"));
	}

	@Test
	public void testPartialUpdateFolderWithInvalidSignature() throws Exception {
		signature = "fakeSignature";
		Response response = doPatchQuery(folderToPatialUpdate, false);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidSignatureException().getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithUserWithoutPermissions() throws Exception {
		serviceKey = sasquatchServiceKey;
		token = sasquatchToken;
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithMissingFolderId() throws Exception {
		folderToPatialUpdate.setId(null);
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ParametersMustMatchException("id", "folder.id").getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithInvalidFolderId() throws Exception {
		folderToPatialUpdate.setId(records.folder_A01);
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ParametersMustMatchException("id", "folder.id").getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithInvalidTypeId() throws Exception {
		folderToPatialUpdate.setType(FolderTypeDto.builder().id("fake").build());
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ResourceTypeNotFoundException("id", "fake").getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithInvalidTypeCode() throws Exception {
		folderToPatialUpdate.setType(FolderTypeDto.builder().code("fake").build());
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ResourceTypeNotFoundException("code", "fake").getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithTypeIdAndTypeCode() throws Exception {
		folderToPatialUpdate.setType(FolderTypeDto.builder().id("id").code("code").build());
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterCombinationException("type.id", "type.code").getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithInvalidAcePrincipal() throws Exception {
		folderToPatialUpdate.setDirectAces(singletonList(AceDto.builder().principals(singleton("fake")).permissions(singleton(READ)).build()));
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException("fake").getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithInvalidAcePermissions() throws Exception {
		folderToPatialUpdate.setDirectAces(singletonList(AceDto.builder()
				.principals(singleton(records.getAlice().getId())).permissions(singleton("fake")).build()));
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("directAces[0].permissions", "fake").getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithStartDateGreaterThanEndDate() throws Exception {
		String start = toDateString(new LocalDate().plusDays(365));
		String end = toDateString(new LocalDate());
		folderToPatialUpdate.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice))
				.permissions(singleton(READ)).startDate(start).endDate(end).build()));
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(new InvalidDateCombinationException(start, end).getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithStartDateOnly() throws Exception {
		folderToPatialUpdate.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice))
				.permissions(singleton(READ)).startDate(toDateString(new LocalDate())).build()));
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		FolderDto doc = response.readEntity(FolderDto.class);
		assertThat(doc.getDirectAces().get(0).getStartDate()).isEqualTo(folderToPatialUpdate.getDirectAces().get(0).getStartDate());
		assertThat(doc.getDirectAces().get(0).getEndDate()).isNull();

		Record record = recordServices.getDocumentById(doc.getId());
		List<Authorization> authorizations = filterInheritedAuthorizations(authorizationsServices.getRecordAuthorizations(record), record.getId());
		assertThat(authorizations).extracting("startDate").containsOnly(toLocalDate(folderToPatialUpdate.getDirectAces().get(0).getStartDate()));
		assertThat(authorizations).extracting("endDate").containsNull();
	}

	@Test
	public void testPartialUpdateFolderWithEndDateOnly() throws Exception {
		folderToPatialUpdate.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice))
				.permissions(singleton(READ)).endDate(toDateString(new LocalDate())).build()));
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(new RequiredParameterException("ace.startDate").getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithInvalidExtAttributeKey() throws Exception {
		folderToPatialUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key("fake").values(singletonList("123")).build()));
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataNotFoundException("fake").getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithInvalidExtAttributeMultiValue() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		folderToPatialUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(asList("ab", "cd")).build()));
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataNotMultivalueException(fakeMetadata1).getValidationError()));
	}

	@Test
	public void testPartialUpdateFolderWithEmptyExtAttributeValues() throws Exception {
		folderToPatialUpdate.setExtendedAttributes(singletonList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(Collections.<String>emptyList()).build()));
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_EMPTY_MESSAGE, "extendedAttributes[0].values"));
	}

	@Test
	public void testPartialUpdateFolderWithMissingAdministrativeUnitId() throws Exception {
		folderToPatialUpdate.setAdministrativeUnit(AdministrativeUnitDto.builder().build());
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE)
				.doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "administrativeUnit.id"));
	}

	@Test
	public void testPartialUpdateFolderWithMissingCategoryId() throws Exception {
		folderToPatialUpdate.setCategory(CategoryDto.builder().build());
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE)
				.doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "category.id"));
	}

	@Test
	public void testPartialUpdateFolderWithMissingContainerId() throws Exception {
		folderToPatialUpdate.setContainer(ContainerDto.builder().build());
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE)
				.doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "container.id"));
	}

	@Test
	public void testPartialUpdateFolderWithMissingRetentionRuleId() throws Exception {
		folderToPatialUpdate.setRetentionRule(RetentionRuleDto.builder().build());
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE)
				.doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "retentionRule.id"));
	}

	@Test
	public void testPartialUpdateFolderWithCustomSchema() throws Exception {
		switchToCustomSchema(fakeFolder.getId());
		addUsrMetadata(id, records.folderTypeEmploye().getLinkedSchema(), MetadataValueType.STRING, null, null);

		List<String> value1 = singletonList("value11"), value2 = asList("value21", "value22");
		folderToPatialUpdate.setType(FolderTypeDto.builder().id(records.folderTypeEmploye().getId()).build());
		folderToPatialUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));

		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);

		FolderDto doc = response.readEntity(FolderDto.class);
		assertThat(doc.getExtendedAttributes()).isEqualTo(folderToPatialUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(doc.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(value1);
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(value2);
	}

	@Test
	public void testPartialUpdateDefaultFlushMode() throws Exception {
		folderToPatialUpdate.setTitle("title2");
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		List<Record> Folders = searchServices.search(new LogicalSearchQuery(
				from(rm.folder.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(id)));
		assertThat(Folders.get(0).getTitle()).isNotEqualTo(folderToPatialUpdate.getTitle());

		Record Folder = recordServices.realtimeGetRecordById(folderToPatialUpdate.getId());
		assertThat(Folder.getTitle()).isEqualTo(folderToPatialUpdate.getTitle());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + Folder.getVersion() + "\"");

		TimeUnit.MILLISECONDS.sleep(5250);

		Folders = searchServices.search(new LogicalSearchQuery(
				from(rm.folder.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(id)));
		assertThat(Folders.get(0).getTitle()).isEqualTo(folderToPatialUpdate.getTitle());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + Folders.get(0).getVersion() + "\"");
	}

	@Test
	public void testPartialUpdateNowFlushMode() throws Exception {
		folderToPatialUpdate.setTitle("title2");
		Response response = doPatchQuery("NOW", folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).isNotEmpty();

		List<Record> Folders = searchServices.search(new LogicalSearchQuery(
				from(rm.folder.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(id)));
		assertThat(Folders.get(0).getTitle()).isEqualTo(folderToPatialUpdate.getTitle());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + Folders.get(0).getVersion() + "\"");
	}

	@Test
	public void testPartialUpdateLaterFlushMode() throws Exception {
		folderToPatialUpdate.setTitle("title2");
		Response response = doPatchQuery("LATER", folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		Record Folder = recordServices.getDocumentById(folderToPatialUpdate.getId());
		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + Folder.getVersion() + "\"");

		folderToPatialUpdate.setTitle(null);
		folderToPatialUpdate.setDescription("an other fake description");
		response = doPatchQuery("NOW", folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).hasSize(1);

		List<Record> Folders = searchServices.search(new LogicalSearchQuery(
				from(rm.folder.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(id)));
		assertThat(Folders.get(0).getTitle()).isEqualTo("title2");
		assertThat(Folders.get(0).<String>get(rm.folder.description())).isEqualTo("an other fake description");

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + Folders.get(0).getVersion() + "\"");
	}

	@Test
	public void testPartialUpdateWithEtag() throws Exception {
		Record Folder = recordServices.getDocumentById(folderToPatialUpdate.getId());
		String eTag = String.valueOf(Folder.getVersion());

		folderToPatialUpdate.setTitle("aNewTitle");
		Response response = buildPatchQuery().request().header("host", host).header(HttpHeaders.IF_MATCH, eTag)
				.build("PATCH", entity(folderToPatialUpdate, APPLICATION_JSON)).invoke();
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		FolderDto folderDto = response.readEntity(FolderDto.class);
		assertThat(folderDto.getTitle()).isEqualTo(folderToPatialUpdate.getTitle());
	}

	@Test
	public void testPartialUpdateWithQuotedEtag() throws Exception {
		Record Folder = recordServices.getDocumentById(folderToPatialUpdate.getId());
		String eTag = "\"".concat(String.valueOf(Folder.getVersion())).concat("\"");

		folderToPatialUpdate.setTitle("aNewTitle");
		Response response = buildPatchQuery().request().header("host", host).header(HttpHeaders.IF_MATCH, eTag)
				.build("PATCH", entity(folderToPatialUpdate, APPLICATION_JSON)).invoke();
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		FolderDto folderDto = response.readEntity(FolderDto.class);
		assertThat(folderDto.getTitle()).isEqualTo(folderToPatialUpdate.getTitle());
	}

	@Test
	public void testPartialUpdateWithOldEtag() throws Exception {
		Record Folder = recordServices.getDocumentById(folderToPatialUpdate.getId());
		String eTag = String.valueOf(Folder.getVersion());

		folderToPatialUpdate.setTitle("aNewTitle");
		Response response = buildPatchQuery().request().header("host", host).header(HttpHeaders.IF_MATCH, eTag)
				.build("PATCH", entity(folderToPatialUpdate, APPLICATION_JSON)).invoke();
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		String validEtag = response.getEntityTag().getValue();

		folderToPatialUpdate.setTitle("aNewTitle2");
		response = buildPatchQuery().request().header("host", host).header(HttpHeaders.IF_MATCH, eTag)
				.build("PATCH", entity(folderToPatialUpdate, APPLICATION_JSON)).invoke();
		assertThat(response.getStatus()).isEqualTo(Response.Status.PRECONDITION_FAILED.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new OptimisticLockException(id, eTag, Long.valueOf(validEtag)).getValidationError()));
	}

	@Test
	public void testPartialUpdateWithInvalidEtag() throws Exception {
		Response response = buildPatchQuery().request().header("host", host).header(HttpHeaders.IF_MATCH, "invalidEtag")
				.build("PATCH", entity(folderToPatialUpdate, APPLICATION_JSON)).invoke();
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("ETag", "invalidEtag").getValidationError()));
	}

	@Test
	public void testPartialUpdateWithUnallowedHostHeader() throws Exception {
		host = "fakedns.com";
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(new UnallowedHostException(host).getValidationError()));
	}

	@Test
	public void testPartialUpdateWithAllowedHost() throws Exception {
		host = "localhost2";
		Response response = doPatchQuery(folderToPatialUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
	}

	private void assertFolderMetadata(Folder folder, Folder folderToExtract) {
		assertThat(singletonList(folderToExtract)).extracting("id", "parentFolderId", "title", "description", "category", "retentionRule", "administrativeUnit",
				"copyStatus", "keywords", "mediumTypes", "container", "closeDate", "openingDate")
				.containsExactly(tuple(folder.getId(), folder.getParentFolder(), folder.getTitle(), folder.getDescription(), folder.getCategory(), folder.getRetentionRule(),
						folder.getAdministrativeUnit(), folder.getCopyStatus().getCode(), folder.getKeywords(), folder.getMediumTypes(), folder.getContainer(),
						folder.getCloseDate(), folder.getOpeningDate()));
	}

	private void assertFolderMetadata(FolderDto folder, Folder folderToExtract) {
		assertThat(singletonList(folderToExtract)).extracting("id", "parentFolder", "title", "description", "category", "retentionRule", "administrativeUnit",
				"keywords", "mediumTypes", "container", "closeDate", "openingDate")
				.containsExactly(tuple(folder.getId(), folder.getParentFolderId(), folder.getTitle(), folder.getDescription(), folder.getCategory().getId(), folder.getRetentionRule().getId(),
						folder.getAdministrativeUnit().getId(), folder.getKeywords(), getIdOfMediumType(folder.getMediumTypes()), folder.getContainer().getId(),
						folder.getClosingDate(), folder.getOpeningDate()));

		assertThat(folder.getCopyStatus()).isEqualTo(folderToExtract.getCopyStatus().getCode());
	}

	private List<String> getIdOfMediumType(List<String> codeList) {

		if (codeList == null) {
			return codeList;
		}

		List<String> idList = new ArrayList<>();

		for (String currentCode : codeList) {
			idList.add(rm.getMediumTypeByCode(currentCode).getId());
		}

		return idList;
	}

	private void assertFolderMetadata(FolderDto folderUpdate, FolderDto folderToExtract) {
		assertThat(singletonList(folderToExtract)).extracting("id", "title", "description", "category.id", "retentionRule.id", "administrativeUnit.id",
				"copyStatus", "keywords", "mediumTypes", "container.id", "closingDate", "openingDate")
				.containsExactly(tuple(folderUpdate.getId(), folderUpdate.getTitle(), folderUpdate.getDescription(), folderUpdate.getCategory().getId(), folderUpdate.getRetentionRule().getId(),
						folderUpdate.getAdministrativeUnit().getId(), folderUpdate.getCopyStatus(), folderUpdate.getKeywords(), folderUpdate.getMediumTypes(), folderUpdate.getContainer().getId(),
						folderUpdate.getClosingDate(), folderUpdate.getOpeningDate()));

		Category category = rm.getCategory(folderUpdate.getCategory().getId());
		AdministrativeUnit administrativeUnit = rm.getAdministrativeUnit(folderUpdate.getAdministrativeUnit().getId());
		RetentionRule retentionRule = rm.getRetentionRule(folderUpdate.getRetentionRule().getId());
		ContainerRecord container = rm.getContainerRecord(folderUpdate.getContainer().getId());

		assertThat(category.getTitle()).isEqualTo(folderToExtract.getCategory().getTitle());
		assertThat(administrativeUnit.getTitle()).isEqualTo(folderToExtract.getAdministrativeUnit().getTitle());
		assertThat(administrativeUnit.getCode()).isEqualTo(folderToExtract.getAdministrativeUnit().getCode());
		assertThat(retentionRule.getTitle()).isEqualTo(folderToExtract.getRetentionRule().getTitle());
		assertThat(retentionRule.getCode()).isEqualTo(folderToExtract.getRetentionRule().getCode());
		assertThat(container.getTitle()).isEqualTo(folderToExtract.getContainer().getTitle());
	}

	private Response doPatchQuery(FolderDto folderDto, String... excludedParam) throws Exception {
		return doPatchQuery(folderDto, true, excludedParam);
	}

	private Response doPatchQuery(String flushMode, FolderDto folderDto, String... excludedParam)
			throws Exception {
		return doPatchQuery(folderDto, flushMode, null, true, excludedParam);
	}

	private Response doPatchQuery(Set<String> filters, FolderDto folderDto, String... excludedParam)
			throws Exception {
		return doPatchQuery(folderDto, null, filters, true, excludedParam);
	}

	private Response doPatchQuery(FolderDto folderDto, boolean calculateSignature, String... excludedParam)
			throws Exception {
		return doPatchQuery(folderDto, null, null, calculateSignature, excludedParam);
	}

	private Response doPatchQuery(FolderDto folderDto, String flushMode, Set<String> filters,
								  boolean calculateSignature, String... excludedParam) throws Exception {
		WebTarget webTarget = buildPatchQuery(calculateSignature, excludedParam);
		if (filters != null && !filters.isEmpty()) {
			webTarget = webTarget.queryParam("filter", filters.toArray());
		}

		Invocation.Builder webTargetBuilder = webTarget.request().header("host", host);
		if (flushMode != null) {
			webTargetBuilder.header(CustomHttpHeaders.FLUSH_MODE, flushMode);
		}
		return webTargetBuilder.build("PATCH", Entity.entity(folderDto, APPLICATION_JSON)).invoke();
	}


	private WebTarget buildPatchQuery(String... excludedParam) throws Exception {
		return buildPatchQuery(true, excludedParam);
	}


	private WebTarget buildPatchQuery(boolean calculateSignature, String... excludedParam) throws Exception {
		method = !HttpMethods.contains(method) ? "fakeMethod" : PATCH;
		return buildQuery(webTarget, calculateSignature, asList("id", "serviceKey", "method", "date", "expiration", "signature"), excludedParam);
	}
}
