package com.constellio.app.modules.restapi.folder;


import com.constellio.app.modules.restapi.core.exception.InvalidDateCombinationException;
import com.constellio.app.modules.restapi.core.exception.InvalidDateFormatException;
import com.constellio.app.modules.restapi.core.exception.InvalidMetadataValueException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterCombinationException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotFoundException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotMultivalueException;
import com.constellio.app.modules.restapi.core.exception.MetadataReferenceNotAllowedException;
import com.constellio.app.modules.restapi.core.exception.OptimisticLockRuntimeException;
import com.constellio.app.modules.restapi.core.exception.ParametersMustMatchException;
import com.constellio.app.modules.restapi.core.exception.RecordLogicallyDeletedException;
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
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.google.common.collect.Lists;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.constellio.app.modules.restapi.core.util.HttpMethods.PATCH;
import static com.constellio.app.modules.restapi.core.util.HttpMethods.PUT;
import static com.constellio.app.modules.restapi.core.util.Permissions.READ;
import static com.constellio.app.modules.restapi.core.util.Permissions.WRITE;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.asSet;
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

public class FolderRestfulServicePUTAcceptanceTest extends BaseFolderRestfulServiceAcceptanceTest {

	private FolderDto minFolderToUpdate, minFolderWithoutAcesToUpdate, fullFolderToUpdate;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		minFolderToUpdate = FolderDto.builder()
				.id(id)
				.title("New title")
				.administrativeUnit(AdministrativeUnitDto.builder().id(records.unitId_10a).build())
				.category(CategoryDto.builder().id(records.categoryId_X110).build())
				.retentionRule(RetentionRuleDto.builder().id(records.ruleId_1).build())
				.copyStatus(CopyType.PRINCIPAL.getCode())
				.openingDate(new LocalDate(2019, 4, 4))
				.parentFolderId(records.folder_A04)
				.build();
		minFolderWithoutAcesToUpdate = FolderDto.builder()
				.id(id)
				.title("New title")
				.administrativeUnit(AdministrativeUnitDto.builder().id(records.unitId_10a).build())
				.category(CategoryDto.builder().id(records.categoryId_X110).build())
				.retentionRule(RetentionRuleDto.builder().id(records.ruleId_1).build())
				.copyStatus(CopyType.PRINCIPAL.getCode())
				.openingDate(new LocalDate(2019, 4, 4))
				.parentFolderId(records.folder_A04)
				.directAces(asList(
						AceDto.builder().principals(singleton(bob)).permissions(asSet("READ", "WRITE", "DELETE")).build(),
						AceDto.builder().principals(singleton(alice)).permissions(asSet("READ", "WRITE")).build()))
				.build();
		fullFolderToUpdate = FolderDto.builder()
				.id(id)
				.title("title")
				.parentFolderId(records.folder_A04)
				.category(CategoryDto.builder().id(records.categoryId_X110).build())
				.retentionRule(RetentionRuleDto.builder().id(records.ruleId_1).build())
				.administrativeUnit(AdministrativeUnitDto.builder().id(records.unitId_10a).build())
				.mainCopyRule(records.getRule1().getPrincipalCopies().get(0).getId())
				.copyStatus(CopyType.PRINCIPAL.getCode())
				.mediumTypes(singletonList("PA"))
				.container(ContainerDto.builder().id(records.containerId_bac01).build())
				.description("description")
				.keywords(singletonList("folder"))
				.openingDate(new LocalDate(2019, 4, 4))
				.closingDate(new LocalDate(2019, 4, 4))
				.actualDepositDate(new LocalDate(2019, 4, 4))
				.actualDestructionDate(new LocalDate(2019, 4, 4))
				.actualTransferDate(new LocalDate(2019, 4, 4))
				.type(FolderTypeDto.builder().id(records.folderTypeEmploye().getId()).build())
				.directAces(asList(
						AceDto.builder().principals(singleton(alice)).permissions(newHashSet(READ, WRITE))
								.startDate(toDateString(new LocalDate()))
								.endDate(toDateString(new LocalDate().plusDays(365))).build(),
						AceDto.builder().principals(singleton(chuckNorris)).permissions(singleton(READ)).build()))
				.extendedAttributes(asList(
						ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList("value1")).build(),
						ExtendedAttributeDto.builder().key(fakeMetadata2).values(asList("value2a", "value2b")).build()))
				.build();
	}

	@Test
	public void testUpdateMinimalFolder() throws Exception {
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		FolderDto folderDto = response.readEntity(FolderDto.class);
		assertThat(folderDto.getId()).isNotNull().isNotEmpty();
		assertThat(folderDto.getTitle()).isEqualTo(minFolderToUpdate.getTitle());
		assertThat(folderDto.getParentFolderId()).isEqualTo(minFolderToUpdate.getParentFolderId());
		assertThat(folderDto.getAdministrativeUnit().getId()).isEqualTo(minFolderToUpdate.getAdministrativeUnit().getId());
		assertThat(folderDto.getAdministrativeUnit().getCode()).isNotNull();
		assertThat(folderDto.getAdministrativeUnit().getTitle()).isNotNull();
		assertThat(folderDto.getCategory().getId()).isEqualTo(minFolderToUpdate.getCategory().getId());
		assertThat(folderDto.getCategory().getTitle()).isNotNull();
		assertThat(folderDto.getRetentionRule().getId()).isEqualTo(minFolderToUpdate.getRetentionRule().getId());
		assertThat(folderDto.getRetentionRule().getCode()).isNotNull();
		assertThat(folderDto.getRetentionRule().getTitle()).isNotNull();
		assertThat(folderDto.getCopyStatus()).isEqualTo(minFolderToUpdate.getCopyStatus());
		assertThat(folderDto.getOpeningDate()).isEqualTo(minFolderToUpdate.getOpeningDate());

		Record record = recordServices.getDocumentById(folderDto.getId());
		assertThat(record).isNotNull();
		assertThatRecord(record).extracting(Folder.TITLE, Folder.ADMINISTRATIVE_UNIT, Folder.CATEGORY,
				Folder.RETENTION_RULE, Folder.COPY_STATUS, Folder.OPENING_DATE)
				.containsExactly(folderDto.getTitle(), folderDto.getAdministrativeUnit().getId(), folderDto.getCategory().getId(),
						folderDto.getRetentionRule().getId(), toCopyType(folderDto.getCopyStatus()), folderDto.getOpeningDate());

		assertThat(response.getHeaderString("ETag")).isNull();
	}

	@Test
	public void testUpdateMinimalFolderWithoutParentFolderId() throws Exception {
		folderId = null;
		minFolderToUpdate.setParentFolderId(null);

		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		FolderDto folderDto = response.readEntity(FolderDto.class);
		assertThat(folderDto.getId()).isNotNull().isNotEmpty();
		assertThat(folderDto.getTitle()).isEqualTo(minFolderToUpdate.getTitle());
		assertThat(folderDto.getParentFolderId()).isNull();
		assertThat(folderDto.getAdministrativeUnit().getId()).isEqualTo(minFolderToUpdate.getAdministrativeUnit().getId());
		assertThat(folderDto.getAdministrativeUnit().getCode()).isNotNull();
		assertThat(folderDto.getAdministrativeUnit().getTitle()).isNotNull();
		assertThat(folderDto.getCategory().getId()).isEqualTo(minFolderToUpdate.getCategory().getId());
		assertThat(folderDto.getCategory().getTitle()).isNotNull();
		assertThat(folderDto.getRetentionRule().getId()).isEqualTo(minFolderToUpdate.getRetentionRule().getId());
		assertThat(folderDto.getRetentionRule().getCode()).isNotNull();
		assertThat(folderDto.getRetentionRule().getTitle()).isNotNull();
		assertThat(folderDto.getCopyStatus()).isEqualTo(minFolderToUpdate.getCopyStatus());
		assertThat(folderDto.getOpeningDate()).isEqualTo(minFolderToUpdate.getOpeningDate());

		Record record = recordServices.getDocumentById(folderDto.getId());
		assertThat(record).isNotNull();
		assertThatRecord(record).extracting(Folder.TITLE, Folder.ADMINISTRATIVE_UNIT, Folder.CATEGORY,
				Folder.RETENTION_RULE, Folder.COPY_STATUS, Folder.OPENING_DATE)
				.containsExactly(folderDto.getTitle(), folderDto.getAdministrativeUnit().getId(), folderDto.getCategory().getId(),
						folderDto.getRetentionRule().getId(), toCopyType(folderDto.getCopyStatus()), folderDto.getOpeningDate());

		assertThat(response.getHeaderString("ETag")).isNull();
	}

	@Test
	public void testUpdateFullFolder() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, "value1", asList("value2a", "value2b"));
		resetCounters();

		Response response = doPutQuery(fullFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).hasSize(fullFolderToUpdate.getDirectAces().size());

		FolderDto folder = response.readEntity(FolderDto.class);
		assertThat(folder.getId()).isNotNull().isNotEmpty();
		assertThat(folder.getTitle()).isEqualTo(fullFolderToUpdate.getTitle());
		assertThat(folder.getParentFolderId()).isEqualTo(fullFolderToUpdate.getParentFolderId()).isEqualTo(folderId);
		assertThat(folder.getKeywords()).isEqualTo(fullFolderToUpdate.getKeywords());
		assertThat(folder.getDirectAces()).contains(fullFolderToUpdate.getDirectAces().toArray(new AceDto[0]));
		assertThat(folder.getExtendedAttributes()).isEqualTo(fullFolderToUpdate.getExtendedAttributes());
		assertThat(folder.getType().getId()).isEqualTo(fullFolderToUpdate.getType().getId());
		assertThat(folder.getType().getCode()).isNotNull().isNotEmpty();
		assertThat(folder.getType().getTitle()).isNotNull().isNotEmpty();

		Category category = rm.getCategory(fullFolderToUpdate.getCategory().getId());
		assertThat(folder.getCategory().getId()).isEqualTo(fullFolderToUpdate.getCategory().getId());
		assertThat(folder.getCategory().getTitle()).isEqualTo(category.getTitle());

		RetentionRule rule = rm.getRetentionRule(fullFolderToUpdate.getRetentionRule().getId());
		assertThat(folder.getRetentionRule().getId()).isEqualTo(fullFolderToUpdate.getRetentionRule().getId());
		assertThat(folder.getRetentionRule().getCode()).isEqualTo(rule.getCode());
		assertThat(folder.getRetentionRule().getTitle()).isEqualTo(rule.getTitle());

		AdministrativeUnit adminUnit = rm.getAdministrativeUnit(fullFolderToUpdate.getAdministrativeUnit().getId());
		assertThat(folder.getAdministrativeUnit().getId()).isEqualTo(fullFolderToUpdate.getAdministrativeUnit().getId());
		assertThat(folder.getAdministrativeUnit().getCode()).isEqualTo(adminUnit.getCode());
		assertThat(folder.getAdministrativeUnit().getTitle()).isEqualTo(adminUnit.getTitle());

		ContainerRecord container = rm.getContainerRecord(fullFolderToUpdate.getContainer().getId());
		assertThat(folder.getContainer().getId()).isEqualTo(fullFolderToUpdate.getContainer().getId());
		assertThat(folder.getContainer().getTitle()).isEqualTo(container.getTitle());

		assertThat(folder.getMainCopyRule()).isEqualTo(fullFolderToUpdate.getMainCopyRule());
		assertThat(folder.getCopyStatus()).isEqualTo(fullFolderToUpdate.getCopyStatus());
		assertThat(folder.getMediumTypes()).isEqualTo(fullFolderToUpdate.getMediumTypes());
		assertThat(folder.getMediaType()).isNotNull();
		assertThat(folder.getDescription()).isEqualTo(fullFolderToUpdate.getDescription());
		assertThat(folder.getOpeningDate()).isEqualTo(fullFolderToUpdate.getOpeningDate());
		assertThat(folder.getClosingDate()).isEqualTo(fullFolderToUpdate.getClosingDate());
		assertThat(folder.getActualTransferDate()).isEqualTo(fullFolderToUpdate.getActualTransferDate());
		assertThat(folder.getActualDepositDate()).isEqualTo(fullFolderToUpdate.getActualDepositDate());
		assertThat(folder.getActualDestructionDate()).isEqualTo(fullFolderToUpdate.getActualDestructionDate());
		assertThat(folder.getExpectedTransferDate()).isEqualTo(fullFolderToUpdate.getExpectedTransferDate());
		assertThat(folder.getExpectedDepositDate()).isEqualTo(fullFolderToUpdate.getExpectedDepositDate());
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(fullFolderToUpdate.getExpectedDestructionDate());

		Record record = recordServices.getDocumentById(folder.getId());
		assertThat(record).isNotNull();
		assertThatRecord(record).extracting(Folder.TITLE, Folder.PARENT_FOLDER, Folder.KEYWORDS, Folder.TYPE, Folder.RETENTION_RULE,
				Folder.CATEGORY, Folder.ADMINISTRATIVE_UNIT, Folder.MAIN_COPY_RULE, Folder.COPY_STATUS, Folder.MEDIUM_TYPES, Folder.CONTAINER, Folder.DESCRIPTION,
				Folder.OPENING_DATE, Folder.CLOSING_DATE, Folder.ACTUAL_TRANSFER_DATE, Folder.ACTUAL_DEPOSIT_DATE, Folder.ACTUAL_DESTRUCTION_DATE)
				.containsExactly(folder.getTitle(), folder.getParentFolderId(), folder.getKeywords(), folder.getType().getId(),
						folder.getRetentionRule().getId(), folder.getCategory().getId(), folder.getAdministrativeUnit().getId(),
						toMainCopyRule(folder.getRetentionRule().getId(), folder.getMainCopyRule()),
						toCopyType(folder.getCopyStatus()), toMediumTypeIds(folder.getMediumTypes()),
						folder.getContainer().getId(), folder.getDescription(), folder.getOpeningDate(), folder.getClosingDate(),
						folder.getActualTransferDate(), folder.getActualDepositDate(), folder.getActualDestructionDate());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(folder.getExtendedAttributes().get(0).getValues());
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(folder.getExtendedAttributes().get(1).getValues());

		assertThat(response.getHeaderString("ETag")).isNull();

		List<Authorization> authorizations = filterInheritedAuthorizations(authorizationsServices.getRecordAuthorizations(record), record.getId());
		assertThat(authorizations).extracting("principals").usingElementComparator(comparingListAnyOrder).containsOnly(
				toPrincipalIds(fullFolderToUpdate.getDirectAces().get(0).getPrincipals()),
				toPrincipalIds(fullFolderToUpdate.getDirectAces().get(1).getPrincipals()));
		assertThat(authorizations).extracting("roles").usingElementComparator(comparingListAnyOrder).containsOnly(
				Lists.newArrayList(fullFolderToUpdate.getDirectAces().get(0).getPermissions()),
				Lists.newArrayList(fullFolderToUpdate.getDirectAces().get(1).getPermissions()));
		assertThat(authorizations).extracting("startDate").containsOnly(
				toLocalDate(fullFolderToUpdate.getDirectAces().get(0).getStartDate()), toLocalDate(fullFolderToUpdate.getDirectAces().get(1).getStartDate()));
		assertThat(authorizations).extracting("endDate").containsOnly(
				toLocalDate(fullFolderToUpdate.getDirectAces().get(0).getEndDate()), toLocalDate(fullFolderToUpdate.getDirectAces().get(1).getEndDate()));
	}

	@Test
	public void testUpdateFullFolderWithAllFilters() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		Set<String> filters = newHashSet("parentFolderId", "type", "category", "retentionRule",
				"administrativeUnit", "mainCopyRule", "copyStatus", "mediumTypes", "mediaType", "container", "title",
				"description", "keywords", "openingDate", "closingDate", "actualTransferDate", "actualDepositDate",
				"actualDestructionDate", "expectedTransferDate", "expectedDepositDate", "expectedDestructionDate",
				"directAces", "inheritedAces", "extendedAttributes");
		Response response = doPutQuery(filters, fullFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		FolderDto folderDto = response.readEntity(FolderDto.class);
		assertThat(folderDto.getId()).isNotNull();
		assertThat(singletonList(folderDto)).extracting("parentFolderId", "type", "category", "retentionRule",
				"administrativeUnit", "mainCopyRule", "copyStatus", "mediumTypes", "mediaType", "container", "title",
				"description", "keywords", "openingDate", "closingDate", "actualTransferDate", "actualDepositDate",
				"actualDestructionDate", "expectedTransferDate", "expectedDepositDate", "expectedDestructionDate",
				"directAces", "inheritedAces", "extendedAttributes")
				.containsOnly(tuple(null, null, null, null, null, null, null, null, null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null));
	}

	@Test
	public void testUpdateFullFolderWithSomeFilters() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		Set<String> filters = newHashSet("type", "directAces", "inheritedAces", "extendedAttributes");
		Response response = doPutQuery(filters, fullFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		FolderDto folderDto = response.readEntity(FolderDto.class);
		assertThat(folderDto.getId()).isNotNull();
		assertThat(singletonList(folderDto)).extracting("parentFolderId", "type", "title",
				"keywords", "directAces", "inheritedAces", "extendedAttributes")
				.containsOnly(tuple(fullFolderToUpdate.getParentFolderId(), null, fullFolderToUpdate.getTitle(), fullFolderToUpdate.getKeywords(),
						null, null, null));
	}

	@Test
	public void testUpdateFolderWithInvalidFilter() throws Exception {
		Response response = doPutQuery(singleton("invalid"), fullFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("filter", "invalid").getValidationError()));
	}

	@Test
	public void testUpdateFolderWithDateUsr() throws Exception {
		addUsrMetadata(MetadataValueType.DATE, null, null);

		List<String> value1 = singletonList("2017-07-21"), value2 = asList("2017-07-22", "2018-07-23");
		minFolderToUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		FolderDto folder = response.readEntity(FolderDto.class);
		assertThat(folder.getExtendedAttributes()).isEqualTo(minFolderToUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(folder.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(new LocalDate(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(new LocalDate(value2.get(0)), new LocalDate(value2.get(1))));
	}

	@Test
	public void testUpdateFolderWithDateUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("2017-07-21T00:00:00");
		addUsrMetadata(MetadataValueType.DATE, null, null);

		minFolderToUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidDateFormatException(value1.get(0), dateFormat).getValidationError()));
	}

	@Test
	public void testUpdateFolderWithDateTimeUsr() throws Exception {
		addUsrMetadata(MetadataValueType.DATE_TIME, null, null);

		List<String> value1 = singletonList(toDateString(fakeDate)), value2 = asList(toDateString(fakeDate), toDateString(fakeDate));
		minFolderToUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		FolderDto folder = response.readEntity(FolderDto.class);
		assertThat(folder.getExtendedAttributes()).isEqualTo(minFolderToUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(folder.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(toLocalDateTime(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(toLocalDateTime(value2.get(0)), toLocalDateTime(value2.get(1))));
	}

	@Test
	public void testUpdateFolderWithDateTimeUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("2018-07-21T23:59:59.123-04:00");
		addUsrMetadata(MetadataValueType.DATE_TIME, null, null);

		minFolderToUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidDateFormatException(value1.get(0), dateTimeFormat).getValidationError()));
	}

	@Test
	public void testUpdateFolderWithNumberUsr() throws Exception {
		addUsrMetadata(MetadataValueType.NUMBER, null, null);

		List<String> value1 = singletonList("123.456"), value2 = asList("2018.24", "2018.25");
		minFolderToUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		FolderDto folder = response.readEntity(FolderDto.class);
		assertThat(folder.getExtendedAttributes()).isEqualTo(minFolderToUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(folder.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(Double.valueOf(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(Double.valueOf(value2.get(0)), Double.valueOf(value2.get(1))));
	}

	@Test
	public void testUpdateFolderWithNumberUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("not-a-number");
		addUsrMetadata(MetadataValueType.NUMBER, null, null);

		minFolderToUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidMetadataValueException(MetadataValueType.NUMBER.name(), value1.get(0)).getValidationError()));
	}

	@Test
	public void testUpdateFolderWithBooleanUsr() throws Exception {
		addUsrMetadata(MetadataValueType.BOOLEAN, null, null);

		List<String> value1 = singletonList("true"), value2 = asList("true", "false");
		minFolderToUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		FolderDto folder = response.readEntity(FolderDto.class);
		assertThat(folder.getExtendedAttributes()).isEqualTo(minFolderToUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(folder.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(Boolean.valueOf(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(Boolean.valueOf(value2.get(0)), Boolean.valueOf(value2.get(1))));
	}

	@Test
	public void testUpdateFolderWithBooleanUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("null");
		addUsrMetadata(MetadataValueType.BOOLEAN, null, null);

		minFolderToUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidMetadataValueException(MetadataValueType.BOOLEAN.name(), value1.get(0)).getValidationError()));
	}

	@Test
	public void testUpdateFolderWithTextUsr() throws Exception {
		addUsrMetadata(MetadataValueType.TEXT, null, null);

		List<String> value1 = singletonList("<html>"), value2 = asList("<b>bold", "test@test.com");
		minFolderToUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		FolderDto folder = response.readEntity(FolderDto.class);
		assertThat(folder.getExtendedAttributes()).isEqualTo(minFolderToUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(folder.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(value1);
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(value2);
	}

	@Test
	public void testUpdateFolderWithReferenceUsr() throws Exception {
		addUsrMetadata(MetadataValueType.REFERENCE, null, null);

		List<String> value1 = singletonList(records.getAlice().getId());
		List<String> value2 = asList(records.getChuckNorris().getId(), records.getAlice().getId());
		minFolderToUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		FolderDto folder = response.readEntity(FolderDto.class);
		assertThat(folder.getExtendedAttributes()).isEqualTo(minFolderToUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(folder.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(value1);
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(value2);
	}

	@Test
	public void testUpdateFolderWithReferenceUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("fake id");
		addUsrMetadata(MetadataValueType.REFERENCE, null, null);

		minFolderToUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(value1.get(0)).getValidationError()));
	}

	@Test
	public void testUpdateFolderWithReferenceUsrAndInvalidSchemaType() throws Exception {
		List<String> value1 = singletonList(records.folder_A18);
		addUsrMetadata(MetadataValueType.REFERENCE, null, null);

		minFolderToUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataReferenceNotAllowedException("folder", fakeMetadata1).getValidationError()));
	}

	@Test
	public void testUpdateFolderWithSchemaChangeByTypeId() throws Exception {
		minFolderToUpdate.setType(FolderTypeDto.builder().id(records.folderTypeEmploye().getId()).build());

		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		FolderDto folder = response.readEntity(FolderDto.class);
		assertThat(folder.getId()).isEqualTo(minFolderToUpdate.getId());

		Record folderTypeRecord = recordServices.getDocumentById(records.folderTypeEmploye().getId());
		assertThat(folder.getType().getId()).isEqualTo(folderTypeRecord.getId());
		assertThat(folder.getType().getCode()).isEqualTo(folderTypeRecord.get((Schemas.CODE)));
		assertThat(folder.getType().getTitle()).isEqualTo(folderTypeRecord.getTitle());

		Record folderRecord = recordServices.getDocumentById(folder.getId());
		assertThat(folderRecord.getId()).isEqualTo(folder.getId());
		assertThatRecord(folderRecord).extracting(Folder.TYPE).isEqualTo(singletonList(records.folderTypeEmploye().getId()));
	}

	@Test
	public void testUpdateFolderWithSchemaChangeByTypeCode() throws Exception {
		Record folderType = recordServices.getDocumentById(records.folderTypeEmploye().getId());
		minFolderToUpdate.setType(FolderTypeDto.builder().code(folderType.<String>get(Schemas.CODE)).build());

		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		FolderDto folder = response.readEntity(FolderDto.class);
		assertThat(folder.getId()).isEqualTo(minFolderToUpdate.getId());
		assertThat(folder.getType().getId()).isEqualTo(folderType.getId());
		assertThat(folder.getType().getCode()).isEqualTo(folderType.<String>get(Schemas.CODE));
		assertThat(folder.getType().getTitle()).isEqualTo(folderType.get(rm.ddvFolderType.title()));

		Record documentRecord = recordServices.getDocumentById(folder.getId());
		assertThat(documentRecord.getId()).isEqualTo(folder.getId());
		assertThatRecord(documentRecord).extracting(Folder.TYPE).isEqualTo(singletonList(records.folderTypeEmploye().getId()));
	}

	@Test
	public void testUpdateFolderWithSchemaChangeAndInvalidMetadataKey() throws Exception {
		switchToCustomSchema(id);
		addUsrMetadata(id, records.documentTypeForm().getLinkedSchema(), MetadataValueType.STRING, null, null);

		minFolderToUpdate.setType(FolderTypeDto.builder().id(records.folderTypeEmploye().getId()).build());
		minFolderToUpdate.setExtendedAttributes(singletonList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(singletonList("value1b")).build()));

		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataNotFoundException(fakeMetadata1).getValidationError()));
	}

	@Test
	public void testUpdateFolderWithMissingId() throws Exception {
		Response response = doPutQuery(minFolderToUpdate, "id");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, "id"));
	}

	@Test
	public void testUpdateFolderWithInvalidId() throws Exception {
		id = "fakeId";
		minFolderToUpdate.setId("fakeId");
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(id).getValidationError()));
	}

	@Test
	public void testUpdateFolderWithMissingServiceKey() throws Exception {
		Response response = doPutQuery(minFolderToUpdate, "serviceKey");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, "serviceKey"));
	}

	@Test
	public void testUpdateFolderWithInvalidServiceKey() throws Exception {
		serviceKey = "fakeServiceKey";
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void testUpdateFolderWithMissingMethod() throws Exception {
		Response response = doPutQuery(minFolderToUpdate, "method");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, "method"));
	}

	@Test
	public void testUpdateFolderWithInvalidMethod() throws Exception {
		method = "fakeMethod";
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("method", method).getValidationError()));
	}

	@Test
	public void testUpdateFolderWithMissingDate() throws Exception {
		Response response = doPutQuery(minFolderToUpdate, "date");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, "date"));
	}

	@Test
	public void testUpdateFolderWithInvalidDate() throws Exception {
		date = "fakeDate";
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("date", date).getValidationError()));
	}

	@Test
	public void testUpdateFolderWithMissingExpiration() throws Exception {
		Response response = doPutQuery(minFolderToUpdate, "expiration");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, "expiration"));
	}

	@Test
	public void testUpdateFolderWithInvalidExpiration() throws Exception {
		expiration = "fakeExpiration";
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(JERSEY_NOT_FOUND_MESSAGE);
	}

	@Test
	public void testUpdateFolderWithInvalidDateAndExpiration() throws Exception {
		date = DateUtils.formatIsoNoMillis(TimeProvider.getLocalDateTime().minusDays(365));
		expiration = "1";
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ExpiredSignedUrlException().getValidationError()));
	}

	@Test
	public void testUpdateFolderWithMissingSignature() throws Exception {
		Response response = doPutQuery(minFolderToUpdate, "signature");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, "signature"));
	}

	@Test
	public void testUpdateFolderWithInvalidSignature() throws Exception {
		signature = "fakeSignature";
		Response response = doPutQuery(minFolderToUpdate, false);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidSignatureException().getValidationError()));
	}

	@Test
	public void testUpdateFolderWithUserWithoutPermissions() throws Exception {
		serviceKey = sasquatchServiceKey;
		token = sasquatchToken;
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	@Test
	public void testUpdateFolderWithoutParentFolderPermissions() throws Exception {
		Record record = recordServices.getDocumentById(records.folder_C51);
		User bobUser = userServices.getUserInCollection(bob, record.getCollection());
		authorizationsServices.add(authorizationForUsers(bobUser).on(record).givingNegativeReadWriteAccess());

		minFolderToUpdate.setParentFolderId(records.folder_C51);
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	@Test
	public void testUpdateFolderWithMissingFolderId() throws Exception {
		minFolderToUpdate.setId(null);
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ParametersMustMatchException("id", "folder.id").getValidationError()));
	}

	@Test
	public void testUpdateFolderWithInvalidFolderId() throws Exception {
		minFolderToUpdate.setId(records.folder_C51);
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ParametersMustMatchException("id", "folder.id").getValidationError()));
	}

	@Test
	public void testUpdateFolderWithInvalidParentFolderId() throws Exception {
		minFolderToUpdate.setParentFolderId("fakeId");
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(minFolderToUpdate.getParentFolderId()).getValidationError()));
	}

	@Test
	public void testUpdateFolderWithMissingTitle() throws Exception {
		minFolderToUpdate.setTitle(null);
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, "folder.title"));
	}

	@Test
	public void testUpdateFolderWithInvalidTypeId() throws Exception {
		minFolderToUpdate.setType(FolderTypeDto.builder().id("fake").build());
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ResourceTypeNotFoundException("id", "fake").getValidationError()));
	}

	@Test
	public void testUpdateFolderWithInvalidTypeCode() throws Exception {
		minFolderToUpdate.setType(FolderTypeDto.builder().code("fake").build());
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ResourceTypeNotFoundException("code", "fake").getValidationError()));
	}

	@Test
	public void testUpdateFolderWithTypeIdAndTypeCode() throws Exception {
		minFolderToUpdate.setType(FolderTypeDto.builder().id("id").code("code").build());
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterCombinationException("type.id", "type.code").getValidationError()));
	}

	@Test
	public void testUpdateFolderWithMissingAcePrincipals() throws Exception {
		minFolderToUpdate.setDirectAces(singletonList(AceDto.builder().permissions(singleton(READ)).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_EMPTY_MESSAGE, "directAces[0].principals"));
	}

	@Test
	public void testUpdateFolderWithEmptyAcePrincipals() throws Exception {
		minFolderToUpdate.setDirectAces(singletonList(AceDto.builder().principals(Collections.<String>emptySet()).permissions(singleton(READ)).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_EMPTY_MESSAGE, "directAces[0].principals"));
	}

	@Test
	public void testUpdateFolderWithInvalidAcePrincipal() throws Exception {
		minFolderToUpdate.setDirectAces(singletonList(AceDto.builder().principals(singleton("fake")).permissions(singleton(READ)).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException("fake").getValidationError()));
	}

	@Test
	public void testUpdateFolderWithMissingAcePermissions() throws Exception {
		minFolderToUpdate.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice)).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_EMPTY_MESSAGE, "directAces[0].permissions"));
	}

	@Test
	public void testUpdateFolderWithEmptyAcePermissions() throws Exception {
		minFolderToUpdate.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice)).permissions(Collections.<String>emptySet()).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_EMPTY_MESSAGE, "directAces[0].permissions"));
	}

	@Test
	public void testUpdateFolderWithInvalidAcePermissions() throws Exception {
		minFolderToUpdate.setDirectAces(singletonList(AceDto.builder()
				.principals(singleton(alice)).permissions(singleton("fake")).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("directAces[0].permissions", "fake").getValidationError()));
	}

	@Test
	public void testUpdateFolderWithStartDateGreaterThanEndDate() throws Exception {
		String start = toDateString(new LocalDate().plusDays(365));
		String end = toDateString(new LocalDate());
		minFolderToUpdate.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice))
				.permissions(singleton(READ)).startDate(start).endDate(end).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidDateCombinationException(start, end).getValidationError()));
	}

	@Test
	public void testUpdateFolderWithStartDateOnly() throws Exception {
		minFolderToUpdate.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice))
				.permissions(singleton(READ)).startDate(toDateString(new LocalDate())).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		FolderDto folder = response.readEntity(FolderDto.class);
		assertThat(folder.getDirectAces().get(0).getStartDate()).isEqualTo(minFolderToUpdate.getDirectAces().get(0).getStartDate());
		assertThat(folder.getDirectAces().get(0).getEndDate()).isNull();

		Record record = recordServices.getDocumentById(folder.getId());
		List<Authorization> authorizations = filterInheritedAuthorizations(authorizationsServices.getRecordAuthorizations(record), record.getId());
		assertThat(authorizations).extracting("startDate").containsOnly(toLocalDate(minFolderToUpdate.getDirectAces().get(0).getStartDate()));
		assertThat(authorizations).extracting("endDate").containsNull();
	}

	@Test
	public void testUpdateFolderWithEndDateOnly() throws Exception {
		minFolderToUpdate.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice))
				.permissions(singleton(READ)).endDate(toDateString(new LocalDate())).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).describedAs(response.toString()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RequiredParameterException("ace.startDate").getValidationError()));
	}

	@Test
	public void testUpdateFolderWithInvalidExtAttributeKey() throws Exception {
		minFolderToUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key("fake").values(singletonList("123")).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataNotFoundException("fake").getValidationError()));
	}

	@Test
	public void testUpdateFolderWithInvalidExtAttributeMultiValue() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		minFolderToUpdate.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(asList("ab", "cd")).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataNotMultivalueException(fakeMetadata1).getValidationError()));
	}

	@Test
	public void testUpdateFolderWithEmptyExtAttributeValues() throws Exception {
		minFolderToUpdate.setExtendedAttributes(singletonList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(Collections.<String>emptyList()).build()));
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(NOT_EMPTY_MESSAGE, "extendedAttributes[0].values"));
	}

	@Test
	public void testUpdateFolderWithMissingAdministrativeUnitId() throws Exception {
		minFolderToUpdate.setAdministrativeUnit(AdministrativeUnitDto.builder().build());
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE)
				.doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "administrativeUnit.id"));
	}

	@Test
	public void testUpdateFolderWithMissingCategoryId() throws Exception {
		minFolderToUpdate.setCategory(CategoryDto.builder().build());
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE)
				.doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "category.id"));
	}

	@Test
	public void testUpdateFolderWithMissingContainerId() throws Exception {
		minFolderToUpdate.setContainer(ContainerDto.builder().build());
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE)
				.doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "container.id"));
	}

	@Test
	public void testUpdateFolderWithMissingRetentionRuleId() throws Exception {
		minFolderToUpdate.setRetentionRule(RetentionRuleDto.builder().build());
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE)
				.doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "retentionRule.id"));
	}

	@Test
	public void testUpdateFolderWithCustomSchema() throws Exception {
		switchToCustomSchema(id);
		addUsrMetadata(id, records.folderTypeEmploye().getLinkedSchema(), MetadataValueType.STRING, null, null);

		List<String> value1 = singletonList("value1b"), value2 = asList("value2c", "value2d");
		minFolderToUpdate.setType(FolderTypeDto.builder().id(records.folderTypeEmploye().getId()).build());
		minFolderToUpdate.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));

		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);

		FolderDto folder = response.readEntity(FolderDto.class);
		assertThat(folder.getExtendedAttributes()).isEqualTo(minFolderToUpdate.getExtendedAttributes());

		Record record = recordServices.getDocumentById(folder.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(value1);
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(value2);
	}

	@Test
	public void testUpdateFolderLogicallyDeleted() throws Exception {
		Record record = recordServices.getDocumentById(id);
		recordServices.logicallyDelete(record, User.GOD);

		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordLogicallyDeletedException(id).getValidationError()));
	}

	@Test
	public void testUpdateFolderPhysicallyDeleted() throws Exception {
		Record record = recordServices.getDocumentById(id);
		recordServices.logicallyDelete(record, User.GOD);
		recordServices.physicallyDelete(record, User.GOD);

		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(id).getValidationError()));
	}

	@Test
	public void testUpdateFolderWithParentLogicallyDeleted() throws Exception {
		Record record = recordServices.getDocumentById(records.folder_C51);
		recordServices.logicallyDelete(record, User.GOD);

		minFolderToUpdate.setParentFolderId(records.folder_C51);
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordLogicallyDeletedException(records.folder_C51).getValidationError()));
	}

	@Test
	public void testUpdateFolderWithParentPhysicallyDeleted() throws Exception {
		Record record = recordServices.getDocumentById(records.folder_A01);
		recordServices.logicallyDelete(record, User.GOD);
		recordServices.physicallyDelete(record, User.GOD, new RecordPhysicalDeleteOptions().setMostReferencesToNull(true));

		minFolderToUpdate.setParentFolderId(records.folder_A01);
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(records.folder_A01).getValidationError()));
	}

	@Test
	public void testUpdateFolderWithInvalidFlushMode() throws Exception {
		Response response = doPutQuery("ALL", minFolderWithoutAcesToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException(CustomHttpHeaders.FLUSH_MODE, "ALL").getValidationError()));
	}

	@Test
	public void testUpdateFolderWithin0SecondsFlushMode() throws Exception {
		Response response = doPutQuery("WITHIN_0_SECONDS", minFolderWithoutAcesToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException(CustomHttpHeaders.FLUSH_MODE, "WITHIN_0_SECONDS").getValidationError()));
	}

	@Test
	public void testUpdateFolderWithinInvalidIntegerSecondsFlushMode() throws Exception {
		String flushMode = "WITHIN_111111111111111111111_SECONDS";
		Response response = doPutQuery(flushMode, minFolderWithoutAcesToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException(CustomHttpHeaders.FLUSH_MODE, flushMode).getValidationError()));
	}

	@Test
	public void testUpdateFolderWithDefaultFlushMode() throws Exception {
		Response response = doPutQuery(minFolderWithoutAcesToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		List<Record> folders = searchServices.search(new LogicalSearchQuery(
				from(rm.folder.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(id)));
		assertThat(folders.get(0).getTitle()).isNotEqualTo(minFolderWithoutAcesToUpdate.getTitle());

		Record folder = recordServices.realtimeGetRecordById(minFolderWithoutAcesToUpdate.getId());
		assertThat(folder.getTitle()).isEqualTo(minFolderWithoutAcesToUpdate.getTitle());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + folder.getVersion() + "\"");

		TimeUnit.MILLISECONDS.sleep(5250);

		folders = searchServices.search(new LogicalSearchQuery(
				from(rm.folder.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(id)));
		assertThat(folders.get(0).getTitle()).isEqualTo(minFolderWithoutAcesToUpdate.getTitle());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + folders.get(0).getVersion() + "\"");
	}

	@Test
	public void testUpdateFolderWithin1SecondFlushMode() throws Exception {
		Response response = doPutQuery("WITHIN_1_SECONDS", minFolderWithoutAcesToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		Record record = recordServices.getDocumentById(minFolderWithoutAcesToUpdate.getId());
		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + record.getVersion() + "\"");

		TimeUnit.MILLISECONDS.sleep(1250);

		List<Record> folders = searchServices.search(new LogicalSearchQuery(
				from(rm.folder.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(id)));
		assertThat(folders.get(0).getTitle()).isEqualTo(minFolderWithoutAcesToUpdate.getTitle());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + folders.get(0).getVersion() + "\"");
	}

	@Test
	public void testUpdateFolderWithNowFlushMode() throws Exception {
		Response response = doPutQuery("NOW", minFolderWithoutAcesToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).isNotEmpty();

		List<Record> folders = searchServices.search(new LogicalSearchQuery(
				from(rm.folder.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(id)));
		assertThat(folders.get(0).getTitle()).isEqualTo(minFolderWithoutAcesToUpdate.getTitle());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + folders.get(0).getVersion() + "\"");
	}

	@Test
	public void testUpdateFolderWithLaterFlushMode() throws Exception {
		Response response = doPutQuery("LATER", minFolderWithoutAcesToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		Record record = recordServices.getDocumentById(minFolderWithoutAcesToUpdate.getId());
		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + record.getVersion() + "\"");

		minFolderToUpdate.setTitle(null);
		minFolderToUpdate.setDescription("newDescription2");
		response = doPatchQuery("NOW", minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).hasSize(1);

		List<Record> folders = searchServices.search(new LogicalSearchQuery(
				from(rm.folder.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(id)));
		assertThat(folders.get(0).getTitle()).isEqualTo(minFolderWithoutAcesToUpdate.getTitle());
		assertThat(folders.get(0).<String>get(rm.folder.description())).isEqualTo(minFolderToUpdate.getDescription());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + folders.get(0).getVersion() + "\"");
	}

	@Test
	public void testUpdateFolderWithEtag() throws Exception {
		Record folder = recordServices.getDocumentById(minFolderWithoutAcesToUpdate.getId());
		String eTag = String.valueOf(folder.getVersion());

		Response response = buildPutQuery().request().header("host", host).header(HttpHeaders.IF_MATCH, eTag)
				.put(entity(minFolderWithoutAcesToUpdate, APPLICATION_JSON));
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		FolderDto folderDto = response.readEntity(FolderDto.class);
		assertThat(folderDto.getTitle()).isEqualTo(minFolderWithoutAcesToUpdate.getTitle());
	}

	@Test
	public void testUpdateFolderWithQuotedEtag() throws Exception {
		Record folder = recordServices.getDocumentById(minFolderWithoutAcesToUpdate.getId());
		String eTag = "\"".concat(String.valueOf(folder.getVersion())).concat("\"");

		Response response = buildPutQuery().request().header("host", host).header(HttpHeaders.IF_MATCH, eTag)
				.put(entity(minFolderWithoutAcesToUpdate, APPLICATION_JSON));
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		FolderDto folderDto = response.readEntity(FolderDto.class);
		assertThat(folderDto.getTitle()).isEqualTo(minFolderWithoutAcesToUpdate.getTitle());
	}

	@Test
	public void testUpdateFolderWithOldEtag() throws Exception {
		Record folder = recordServices.getDocumentById(minFolderWithoutAcesToUpdate.getId());
		String eTag = String.valueOf(folder.getVersion());

		Response response = buildPutQuery().request().header("host", host).header(HttpHeaders.IF_MATCH, eTag)
				.put(entity(minFolderWithoutAcesToUpdate, APPLICATION_JSON));
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
		String validEtag = response.getEntityTag().getValue();

		minFolderWithoutAcesToUpdate.setTitle("aNewTitle2");
		response = buildPutQuery().request().header("host", host).header(HttpHeaders.IF_MATCH, eTag)
				.put(entity(minFolderWithoutAcesToUpdate, APPLICATION_JSON));
		assertThat(response.getStatus()).isEqualTo(Response.Status.PRECONDITION_FAILED.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new OptimisticLockRuntimeException(id, eTag, Long.valueOf(validEtag)).getValidationError()));
	}

	@Test
	public void testUpdateFolderWithInvalidEtag() throws Exception {
		Response response = buildPutQuery().request().header("host", host).header(HttpHeaders.IF_MATCH, "invalidEtag")
				.put(entity(minFolderWithoutAcesToUpdate, APPLICATION_JSON));
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("ETag", "invalidEtag").getValidationError()));
	}

	@Test
	public void testUpdateFolderWithUnallowedHostHeader() throws Exception {
		host = "fakedns.com";
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(new UnallowedHostException(host).getValidationError()));
	}

	@Test
	public void testUpdateFolderWithAllowedHost() throws Exception {
		host = "localhost2";
		Response response = doPutQuery(minFolderToUpdate);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
	}


	private Response doPutQuery(FolderDto folderDto, String... excludedParam) throws Exception {
		return doPutQuery(folderDto, true, excludedParam);
	}

	private Response doPutQuery(String flushMode, FolderDto folderDto, String... excludedParam)
			throws Exception {
		return doPutQuery(folderDto, flushMode, null, true, excludedParam);
	}

	private Response doPutQuery(Set<String> filters, FolderDto folderDto, String... excludedParam)
			throws Exception {
		return doPutQuery(folderDto, null, filters, true, excludedParam);
	}

	private Response doPutQuery(FolderDto folderDto, boolean calculateSignature, String... excludedParam)
			throws Exception {
		return doPutQuery(folderDto, null, null, calculateSignature, excludedParam);
	}

	private Response doPutQuery(FolderDto folderDto, String flushMode, Set<String> filters,
								boolean calculateSignature, String... excludedParam) throws Exception {
		WebTarget webTarget = buildPutQuery(calculateSignature, excludedParam);
		if (filters != null && !filters.isEmpty()) {
			webTarget = webTarget.queryParam("filter", filters.toArray());
		}

		Invocation.Builder webTargetBuilder = webTarget.request().header("host", host);
		if (flushMode != null) {
			webTargetBuilder.header(CustomHttpHeaders.FLUSH_MODE, flushMode);
		}
		return webTargetBuilder.build("PUT", Entity.entity(folderDto, APPLICATION_JSON)).invoke();
	}

	private WebTarget buildPutQuery(String... excludedParam) throws Exception {
		return buildPutQuery(true, excludedParam);
	}

	private WebTarget buildPutQuery(boolean calculateSignature, String... excludedParam) throws Exception {
		method = !HttpMethods.contains(method) ? "fakeMethod" : PUT;
		return buildQuery(webTarget, calculateSignature, asList("id", "serviceKey", "method", "date", "expiration", "signature"), excludedParam);
	}


	private Response doPatchQuery(String flushMode, FolderDto folderDto, String... excludedParam)
			throws Exception {
		return doPatchQuery(folderDto, flushMode, null, true, excludedParam);
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

	private WebTarget buildPatchQuery(boolean calculateSignature, String... excludedParam) throws Exception {
		method = !HttpMethods.contains(method) ? "fakeMethod" : PATCH;
		return buildQuery(webTarget, calculateSignature, asList("id", "serviceKey", "method", "date", "expiration", "signature"), excludedParam);
	}
}
