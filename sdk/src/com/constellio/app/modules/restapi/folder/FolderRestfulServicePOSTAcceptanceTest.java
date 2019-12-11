package com.constellio.app.modules.restapi.folder;

import com.constellio.app.modules.restapi.core.exception.InvalidDateCombinationException;
import com.constellio.app.modules.restapi.core.exception.InvalidDateFormatException;
import com.constellio.app.modules.restapi.core.exception.InvalidMetadataValueException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterCombinationException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotFoundException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotManualException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotMultivalueException;
import com.constellio.app.modules.restapi.core.exception.MetadataReferenceNotAllowedException;
import com.constellio.app.modules.restapi.core.exception.ParametersMustMatchException;
import com.constellio.app.modules.restapi.core.exception.RecordCopyNotPermittedException;
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
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.FolderExtension;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.schemas.bulkImport.DummyCalculator;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.google.common.collect.Lists;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.constellio.app.modules.restapi.core.util.HttpMethods.POST;
import static com.constellio.app.modules.restapi.core.util.Permissions.READ;
import static com.constellio.app.modules.restapi.core.util.Permissions.WRITE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static com.constellio.sdk.tests.TestUtils.comparingListAnyOrder;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class FolderRestfulServicePOSTAcceptanceTest extends BaseFolderRestfulServiceAcceptanceTest {

	private FolderDto minFolderToAdd, fullFolderToAdd;
	private String copySource;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		minFolderToAdd = FolderDto.builder()
				.administrativeUnit(AdministrativeUnitDto.builder().id(records.unitId_10a).build())
				.category(CategoryDto.builder().id(records.categoryId_X110).build())
				.retentionRule(RetentionRuleDto.builder().id(records.ruleId_1).build())
				.title("folder")
				.copyStatus(CopyType.PRINCIPAL.getCode())
				.openingDate(new LocalDate(2019, 4, 4))
				.parentFolderId(records.folder_A04).build();
		fullFolderToAdd = FolderDto.builder().parentFolderId(records.folder_A04)
				.category(CategoryDto.builder().id(records.categoryId_X110).build())
				.retentionRule(RetentionRuleDto.builder().id(records.ruleId_1).build())
				.administrativeUnit(AdministrativeUnitDto.builder().id(records.unitId_10a).build())
				.mainCopyRule(records.getRule1().getPrincipalCopies().get(0).getId())
				.copyStatus(CopyType.PRINCIPAL.getCode())
				.mediumTypes(singletonList("PA")).container(ContainerDto.builder().id(records.containerId_bac01).build())
				.title("title").description("description").keywords(singletonList("folder"))
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

	//
	// CREATE
	//

	@Test
	public void testCreateMinimalFolder() throws Exception {
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		FolderDto folderDto = response.readEntity(FolderDto.class);
		assertThat(folderDto.getId()).isNotNull().isNotEmpty();
		assertThat(folderDto.getTitle()).isEqualTo(minFolderToAdd.getTitle());
		assertThat(folderDto.getParentFolderId()).isEqualTo(minFolderToAdd.getParentFolderId());
		assertThat(folderDto.getAdministrativeUnit().getId()).isEqualTo(minFolderToAdd.getAdministrativeUnit().getId());
		assertThat(folderDto.getAdministrativeUnit().getCode()).isNotNull();
		assertThat(folderDto.getAdministrativeUnit().getTitle()).isNotNull();
		assertThat(folderDto.getCategory().getId()).isEqualTo(minFolderToAdd.getCategory().getId());
		assertThat(folderDto.getCategory().getTitle()).isNotNull();
		assertThat(folderDto.getRetentionRule().getId()).isEqualTo(minFolderToAdd.getRetentionRule().getId());
		assertThat(folderDto.getRetentionRule().getCode()).isNotNull();
		assertThat(folderDto.getRetentionRule().getTitle()).isNotNull();
		assertThat(folderDto.getCopyStatus()).isEqualTo(minFolderToAdd.getCopyStatus());
		assertThat(folderDto.getOpeningDate()).isEqualTo(minFolderToAdd.getOpeningDate());

		Record record = recordServices.getDocumentById(folderDto.getId());
		assertThat(record).isNotNull();
		assertThatRecord(record).extracting(Folder.TITLE, Folder.ADMINISTRATIVE_UNIT, Folder.CATEGORY,
				Folder.RETENTION_RULE, Folder.COPY_STATUS, Folder.OPENING_DATE)
				.containsExactly(folderDto.getTitle(), folderDto.getAdministrativeUnit().getId(), folderDto.getCategory().getId(),
						folderDto.getRetentionRule().getId(), toCopyType(folderDto.getCopyStatus()), folderDto.getOpeningDate());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + record.getVersion() + "\"");
	}

	@Test
	public void testCreateMinimalFolderWithoutParentFolderId() throws Exception {
		folderId = null;
		minFolderToAdd.setParentFolderId(null);

		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		FolderDto folderDto = response.readEntity(FolderDto.class);
		assertThat(folderDto.getId()).isNotNull().isNotEmpty();
		assertThat(folderDto.getTitle()).isEqualTo(minFolderToAdd.getTitle());
		assertThat(folderDto.getParentFolderId()).isNull();
		assertThat(folderDto.getAdministrativeUnit().getId()).isEqualTo(minFolderToAdd.getAdministrativeUnit().getId());
		assertThat(folderDto.getAdministrativeUnit().getCode()).isNotNull();
		assertThat(folderDto.getAdministrativeUnit().getTitle()).isNotNull();
		assertThat(folderDto.getCategory().getId()).isEqualTo(minFolderToAdd.getCategory().getId());
		assertThat(folderDto.getCategory().getTitle()).isNotNull();
		assertThat(folderDto.getRetentionRule().getId()).isEqualTo(minFolderToAdd.getRetentionRule().getId());
		assertThat(folderDto.getRetentionRule().getCode()).isNotNull();
		assertThat(folderDto.getRetentionRule().getTitle()).isNotNull();
		assertThat(folderDto.getCopyStatus()).isEqualTo(minFolderToAdd.getCopyStatus());
		assertThat(folderDto.getOpeningDate()).isEqualTo(minFolderToAdd.getOpeningDate());

		Record record = recordServices.getDocumentById(folderDto.getId());
		assertThat(record).isNotNull();
		assertThatRecord(record).extracting(Folder.TITLE, Folder.ADMINISTRATIVE_UNIT, Folder.CATEGORY,
				Folder.RETENTION_RULE, Folder.COPY_STATUS, Folder.OPENING_DATE)
				.containsExactly(folderDto.getTitle(), folderDto.getAdministrativeUnit().getId(), folderDto.getCategory().getId(),
						folderDto.getRetentionRule().getId(), toCopyType(folderDto.getCopyStatus()), folderDto.getOpeningDate());

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + record.getVersion() + "\"");
	}

	@Test
	public void testCreateFullFolder() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		Response response = doPostQuery(fullFolderToAdd);
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall()).hasSize(fullFolderToAdd.getDirectAces().size());

		FolderDto folder = response.readEntity(FolderDto.class);
		assertThat(folder.getId()).isNotNull().isNotEmpty();
		assertThat(folder.getTitle()).isEqualTo(fullFolderToAdd.getTitle());
		assertThat(folder.getParentFolderId()).isEqualTo(fullFolderToAdd.getParentFolderId()).isEqualTo(folderId);
		assertThat(folder.getKeywords()).isEqualTo(fullFolderToAdd.getKeywords());
		assertThat(folder.getDirectAces()).contains(fullFolderToAdd.getDirectAces().toArray(new AceDto[0]));
		assertThat(folder.getExtendedAttributes()).isEqualTo(fullFolderToAdd.getExtendedAttributes());
		assertThat(folder.getType().getId()).isEqualTo(fullFolderToAdd.getType().getId());
		assertThat(folder.getType().getCode()).isNotNull().isNotEmpty();
		assertThat(folder.getType().getTitle()).isNotNull().isNotEmpty();

		Category category = rm.getCategory(fullFolderToAdd.getCategory().getId());
		assertThat(folder.getCategory().getId()).isEqualTo(fullFolderToAdd.getCategory().getId());
		assertThat(folder.getCategory().getTitle()).isEqualTo(category.getTitle());

		RetentionRule rule = rm.getRetentionRule(fullFolderToAdd.getRetentionRule().getId());
		assertThat(folder.getRetentionRule().getId()).isEqualTo(fullFolderToAdd.getRetentionRule().getId());
		assertThat(folder.getRetentionRule().getCode()).isEqualTo(rule.getCode());
		assertThat(folder.getRetentionRule().getTitle()).isEqualTo(rule.getTitle());

		AdministrativeUnit adminUnit = rm.getAdministrativeUnit(fullFolderToAdd.getAdministrativeUnit().getId());
		assertThat(folder.getAdministrativeUnit().getId()).isEqualTo(fullFolderToAdd.getAdministrativeUnit().getId());
		assertThat(folder.getAdministrativeUnit().getCode()).isEqualTo(adminUnit.getCode());
		assertThat(folder.getAdministrativeUnit().getTitle()).isEqualTo(adminUnit.getTitle());

		ContainerRecord container = rm.getContainerRecord(fullFolderToAdd.getContainer().getId());
		assertThat(folder.getContainer().getId()).isEqualTo(fullFolderToAdd.getContainer().getId());
		assertThat(folder.getContainer().getTitle()).isEqualTo(container.getTitle());

		assertThat(folder.getMainCopyRule()).isEqualTo(fullFolderToAdd.getMainCopyRule());
		assertThat(folder.getCopyStatus()).isEqualTo(fullFolderToAdd.getCopyStatus());
		assertThat(folder.getMediumTypes()).isEqualTo(fullFolderToAdd.getMediumTypes());
		assertThat(folder.getMediaType()).isNotNull();
		assertThat(folder.getDescription()).isEqualTo(fullFolderToAdd.getDescription());
		assertThat(folder.getOpeningDate()).isEqualTo(fullFolderToAdd.getOpeningDate());
		assertThat(folder.getClosingDate()).isEqualTo(fullFolderToAdd.getClosingDate());
		assertThat(folder.getActualTransferDate()).isEqualTo(fullFolderToAdd.getActualTransferDate());
		assertThat(folder.getActualDepositDate()).isEqualTo(fullFolderToAdd.getActualDepositDate());
		assertThat(folder.getActualDestructionDate()).isEqualTo(fullFolderToAdd.getActualDestructionDate());
		assertThat(folder.getExpectedTransferDate()).isEqualTo(fullFolderToAdd.getExpectedTransferDate());
		assertThat(folder.getExpectedDepositDate()).isEqualTo(fullFolderToAdd.getExpectedDepositDate());
		assertThat(folder.getExpectedDestructionDate()).isEqualTo(fullFolderToAdd.getExpectedDestructionDate());

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
				toPrincipalIds(fullFolderToAdd.getDirectAces().get(0).getPrincipals()),
				toPrincipalIds(fullFolderToAdd.getDirectAces().get(1).getPrincipals()));
		assertThat(authorizations).extracting("roles").usingElementComparator(comparingListAnyOrder).containsOnly(
				Lists.newArrayList(fullFolderToAdd.getDirectAces().get(0).getPermissions()),
				Lists.newArrayList(fullFolderToAdd.getDirectAces().get(1).getPermissions()));
		assertThat(authorizations).extracting("startDate").containsOnly(
				toLocalDate(fullFolderToAdd.getDirectAces().get(0).getStartDate()), toLocalDate(fullFolderToAdd.getDirectAces().get(1).getStartDate()));
		assertThat(authorizations).extracting("endDate").containsOnly(
				toLocalDate(fullFolderToAdd.getDirectAces().get(0).getEndDate()), toLocalDate(fullFolderToAdd.getDirectAces().get(1).getEndDate()));
	}

	@Test
	public void testCreateFullFolderAllFilters() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		Response response = doFilteredPostQuery(fullFolderToAdd, "parentFolderId", "type", "category", "retentionRule",
				"administrativeUnit", "mainCopyRule", "copyStatus", "mediumTypes", "mediaType", "container", "title",
				"description", "keywords", "openingDate", "closingDate", "actualTransferDate", "actualDepositDate",
				"actualDestructionDate", "expectedTransferDate", "expectedDepositDate", "expectedDestructionDate",
				"directAces", "inheritedAces", "extendedAttributes");
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

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
	public void testCreateFullFolderSomeFilters() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		Response response = doFilteredPostQuery(fullFolderToAdd, "type", "directAces", "inheritedAces", "extendedAttributes");
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

		FolderDto folderDto = response.readEntity(FolderDto.class);
		assertThat(folderDto.getId()).isNotNull();
		assertThat(singletonList(folderDto)).extracting("parentFolderId", "type", "title",
				"keywords", "directAces", "inheritedAces", "extendedAttributes")
				.containsOnly(tuple(fullFolderToAdd.getParentFolderId(), null, fullFolderToAdd.getTitle(), fullFolderToAdd.getKeywords(),
						null, null, null));
	}

	@Test
	public void testCreateFolderInvalidFilter() throws Exception {
		Response response = doFilteredPostQuery(minFolderToAdd, "invalid");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("filter", "invalid").getValidationError()));
	}

	@Test
	public void testCreateFolderWithoutFolder() throws Exception {
		Response response = doPostQuery(null);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RequiredParameterException("folder").getValidationError()));
	}

	@Test
	public void testCreateFolderWithTypeCode() throws Exception {
		Record folderType = recordServices.getDocumentById(records.folderTypeEmploye().getId());
		minFolderToAdd.setType(FolderTypeDto.builder().code((String) folderType.get(Schemas.CODE)).build());

		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

		FolderDto folder = response.readEntity(FolderDto.class);
		assertThat(folder.getId()).isNotNull().isNotEmpty();
		assertThat(folder.getTitle()).isEqualTo(minFolderToAdd.getTitle());
		assertThat(folder.getParentFolderId()).isEqualTo(minFolderToAdd.getParentFolderId()).isEqualTo(folderId);
		assertThat(folder.getType().getId()).isEqualTo(folderType.getId());
		assertThat(folder.getType().getCode()).isEqualTo(minFolderToAdd.getType().getCode());
		assertThat(folder.getType().getTitle()).isNotNull().isNotEmpty();

		Record record = recordServices.getDocumentById(folder.getId());
		assertThat(record).isNotNull();
		assertThatRecord(record).extracting(Folder.TITLE, Folder.PARENT_FOLDER, Folder.TYPE)
				.containsExactly(folder.getTitle(), folder.getParentFolderId(), folder.getType().getId());
	}

	@Test
	public void testCreateFolderWithDateUsr() throws Exception {
		addUsrMetadata(MetadataValueType.DATE, null, null);

		List<String> value1 = singletonList("2017-07-21"), value2 = asList("2017-07-22", "2018-07-23");
		minFolderToAdd.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

		FolderDto folder = response.readEntity(FolderDto.class);
		Record record = recordServices.getDocumentById(folder.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(new LocalDate(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(new LocalDate(value2.get(0)), new LocalDate(value2.get(1))));
	}

	@Test
	public void testCreateFolderWithDateUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("2017-07-21T00:00:00");
		addUsrMetadata(MetadataValueType.DATE, null, null);

		minFolderToAdd.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidDateFormatException(value1.get(0), dateFormat).getValidationError()));
	}

	@Test
	public void testCreateFolderWithDateTimeUsr() throws Exception {
		addUsrMetadata(MetadataValueType.DATE_TIME, null, null);

		List<String> value1 = singletonList(toDateString(new LocalDateTime()));
		List<String> value2 = asList(toDateString(fakeDate), toDateString(fakeDate.plusDays(5)));
		minFolderToAdd.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

		FolderDto folder = response.readEntity(FolderDto.class);
		Record record = recordServices.getDocumentById(folder.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(toLocalDateTime(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(toLocalDateTime(value2.get(0)), toLocalDateTime(value2.get(1))));
	}

	@Test
	public void testCreateFolderWithDateTimeUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("2018-07-21T23:59:59.123-04:00");
		addUsrMetadata(MetadataValueType.DATE_TIME, null, null);

		minFolderToAdd.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidDateFormatException(value1.get(0), dateTimeFormat).getValidationError()));
	}

	@Test
	public void testCreateFolderWithNumberUsr() throws Exception {
		addUsrMetadata(MetadataValueType.NUMBER, null, null);

		List<String> value1 = singletonList("123.456"), value2 = asList("2018.24", "2018.25");
		minFolderToAdd.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

		FolderDto folder = response.readEntity(FolderDto.class);
		Record record = recordServices.getDocumentById(folder.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(Double.valueOf(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(Double.valueOf(value2.get(0)), Double.valueOf(value2.get(1))));
	}

	@Test
	public void testCreateFolderWithNumberUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("not-a-number");
		addUsrMetadata(MetadataValueType.NUMBER, null, null);

		minFolderToAdd.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidMetadataValueException(MetadataValueType.NUMBER.name(), value1.get(0)).getValidationError()));
	}

	@Test
	public void testCreateFolderWithBooleanUsr() throws Exception {
		addUsrMetadata(MetadataValueType.BOOLEAN, null, null);

		List<String> value1 = singletonList("true"), value2 = asList("true", "false");
		minFolderToAdd.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

		FolderDto folder = response.readEntity(FolderDto.class);
		Record record = recordServices.getDocumentById(folder.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(singletonList(Boolean.valueOf(value1.get(0))));
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(
				asList(Boolean.valueOf(value2.get(0)), Boolean.valueOf(value2.get(1))));
	}

	@Test
	public void testCreateFolderWithBooleanUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("null");
		addUsrMetadata(MetadataValueType.BOOLEAN, null, null);

		minFolderToAdd.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidMetadataValueException(MetadataValueType.BOOLEAN.name(), value1.get(0)).getValidationError()));
	}

	@Test
	public void testCreateFolderWithTextUsr() throws Exception {
		addUsrMetadata(MetadataValueType.TEXT, null, null);

		List<String> value1 = singletonList("<html>"), value2 = asList("<b>bold", "test@test.com");
		minFolderToAdd.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

		FolderDto folder = response.readEntity(FolderDto.class);
		Record record = recordServices.getDocumentById(folder.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(value1);
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(value2);
	}

	@Test
	public void testCreateFolderWithReferenceUsr() throws Exception {
		addUsrMetadata(MetadataValueType.REFERENCE, null, null);

		List<String> value1 = singletonList(records.getAlice().getId());
		List<String> value2 = asList(records.getChuckNorris().getId(), records.getAlice().getId());
		minFolderToAdd.setExtendedAttributes(asList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build(),
				ExtendedAttributeDto.builder().key(fakeMetadata2).values(value2).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

		FolderDto folder = response.readEntity(FolderDto.class);
		Record record = recordServices.getDocumentById(folder.getId());
		assertThatRecord(record).extracting(fakeMetadata1).isEqualTo(value1);
		assertThatRecord(record).extracting(fakeMetadata2).containsExactly(value2);
	}

	@Test
	public void testCreateFolderWithReferenceUsrAndInvalidValue() throws Exception {
		List<String> value1 = singletonList("fake id");
		addUsrMetadata(MetadataValueType.REFERENCE, null, null);

		minFolderToAdd.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(value1.get(0)).getValidationError()));
	}

	@Test
	public void testCreateFolderWithReferenceUsrAndInvalidSchemaType() throws Exception {
		List<String> value1 = singletonList(records.folder_A18);
		addUsrMetadata(MetadataValueType.REFERENCE, null, null);

		minFolderToAdd.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(value1).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataReferenceNotAllowedException(rm.folderSchemaType().getCode(), fakeMetadata1).getValidationError()));
	}

	@Test
	public void testCreateFolderWithInvalidFolderId() throws Exception {
		folderId = "fakeFolderId";
		minFolderToAdd.setParentFolderId(folderId);
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException(folderId).getValidationError()));
	}

	@Test
	public void testCreateFolderWithMissingServiceKey() throws Exception {
		Response response = doPostQuery(minFolderToAdd, "serviceKey");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "serviceKey"));
	}

	@Test
	public void testCreateFolderWithInvalidServiceKey() throws Exception {
		serviceKey = "fakeKey";
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void testCreateFolderWithMissingMethod() throws Exception {
		Response response = doPostQuery(minFolderToAdd, "method");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "method"));
	}

	@Test
	public void testCreateFolderWithInvalidMethod() throws Exception {
		method = "fakeMethod";
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("method", method).getValidationError()));
	}

	@Test
	public void testCreateFolderWithMissingDate() throws Exception {
		Response response = doPostQuery(minFolderToAdd, "date");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "date"));
	}

	@Test
	public void testCreateFolderWithInvalidDate() throws Exception {
		date = "12345";
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("date", date).getValidationError()));
	}

	@Test
	public void testCreateFolderWithMissingExpiration() throws Exception {
		Response response = doPostQuery(minFolderToAdd, "expiration");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "expiration"));
	}

	@Test
	public void testCreateFolderWithInvalidExpiration() throws Exception {
		expiration = "111111111111111111111111111";
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(JERSEY_NOT_FOUND_MESSAGE);
	}

	@Test
	public void testCreateFolderWithInvalidDateAndExpiration() throws Exception {
		date = DateUtils.formatIsoNoMillis(TimeProvider.getLocalDateTime().minusDays(365));
		expiration = "1";
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ExpiredSignedUrlException().getValidationError()));
	}

	@Test
	public void testCreateFolderWithMissingSignature() throws Exception {
		Response response = doPostQuery(minFolderToAdd, "signature");
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "signature"));
	}

	@Test
	public void testCreateFolderWithInvalidSignature() throws Exception {
		signature = "fakeSignature";
		Response response = doPostQuery(minFolderToAdd, false);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidSignatureException().getValidationError()));
	}

	@Test
	public void testCreateFolderWithUserWithoutPermissions() throws Exception {
		serviceKey = sasquatchServiceKey;
		token = sasquatchToken;
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	@Test
	public void testCreateFolderWithInvalidParentFolderId() throws Exception {
		minFolderToAdd.setParentFolderId(records.folder_A42);
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ParametersMustMatchException("folderId", "folder.parentFolderId").getValidationError()));
	}

	@Test
	public void testCreateFolderWithMissingFolderTitle() throws Exception {
		minFolderToAdd.setTitle(null);
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "folder.title"));
	}

	@Test
	public void testCreateFolderWithInvalidTypeId() throws Exception {
		minFolderToAdd.setType(FolderTypeDto.builder().id("fake").build());
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ResourceTypeNotFoundException("id", minFolderToAdd.getType().getId()).getValidationError()));
	}

	@Test
	public void testCreateFolderWithInvalidTypeCode() throws Exception {
		minFolderToAdd.setType(FolderTypeDto.builder().code("fake").build());
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new ResourceTypeNotFoundException("code", minFolderToAdd.getType().getCode()).getValidationError()));
	}

	@Test
	public void testCreateFolderWithTypeIdAndTypeCode() throws Exception {
		minFolderToAdd.setType(FolderTypeDto.builder().id("id").code("code").build());
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterCombinationException("type.id", "type.code").getValidationError()));
	}

	@Test
	public void testCreateFolderWithMissingAcePrincipals() throws Exception {
		minFolderToAdd.setDirectAces(singletonList(AceDto.builder().permissions(singleton(READ)).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_EMPTY_MESSAGE, "directAces[0].principals"));
	}

	@Test
	public void testCreateFolderWithEmptyAcePrincipals() throws Exception {
		minFolderToAdd.setDirectAces(singletonList(AceDto.builder().principals(Collections.<String>emptySet()).permissions(singleton(READ)).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_EMPTY_MESSAGE, "directAces[0].principals"));
	}

	@Test
	public void testCreateFolderWithInvalidAcePrincipals() throws Exception {
		minFolderToAdd.setDirectAces(singletonList(AceDto.builder().principals(singleton("fake")).permissions(singleton(READ)).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RecordNotFoundException("fake").getValidationError()));
	}

	@Test
	public void testCreateFolderWithMissingAcePermissions() throws Exception {
		minFolderToAdd.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice)).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_EMPTY_MESSAGE, "directAces[0].permissions"));
	}

	@Test
	public void testCreateFolderWithEmptyAcePermissions() throws Exception {
		minFolderToAdd.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice)).permissions(Collections.<String>emptySet()).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_EMPTY_MESSAGE, "directAces[0].permissions"));
	}

	@Test
	public void testCreateFolderWithStartDateGreaterThanEndDate() throws Exception {
		String start = toDateString(new LocalDate().plusDays(365));
		String end = toDateString(new LocalDate());
		minFolderToAdd.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice))
				.permissions(singleton(READ)).startDate(start).endDate(end).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(new InvalidDateCombinationException(start, end).getValidationError()));
	}

	@Test
	public void testCreateFolderWithStartDateOnly() throws Exception {
		minFolderToAdd.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice))
				.permissions(singleton(READ)).startDate(toDateString(new LocalDate())).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

		FolderDto folder = response.readEntity(FolderDto.class);
		assertThat(folder.getDirectAces().get(0).getStartDate()).isEqualTo(minFolderToAdd.getDirectAces().get(0).getStartDate());
		assertThat(folder.getDirectAces().get(0).getEndDate()).isNull();

		Record record = recordServices.getDocumentById(folder.getId());
		List<Authorization> authorizations = filterInheritedAuthorizations(authorizationsServices.getRecordAuthorizations(record), record.getId());
		assertThat(authorizations).extracting("startDate").containsOnly(toLocalDate(minFolderToAdd.getDirectAces().get(0).getStartDate()));
		assertThat(authorizations).extracting("endDate").containsNull();
	}

	@Test
	public void testCreateFolderWithEndDateOnly() throws Exception {
		minFolderToAdd.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice))
				.permissions(singleton(READ)).endDate(toDateString(new LocalDate())).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new RequiredParameterException("ace.startDate").getValidationError()));
	}

	@Test
	public void testCreateFolderWithInvalidAcePermissions() throws Exception {
		minFolderToAdd.setDirectAces(singletonList(AceDto.builder().principals(singleton(alice)).permissions(singleton("fake")).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new InvalidParameterException("directAces[0].permissions", "fake").getValidationError()));
	}

	@Test
	public void testCreateFolderWithInvalidExtAttributeKey() throws Exception {
		minFolderToAdd.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key("fake").values(singletonList("123")).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataNotFoundException("fake").getValidationError()));
	}

	@Test
	public void testCreateFolderWithInvalidExtAttributeMultiValue() throws Exception {
		addUsrMetadata(MetadataValueType.STRING, null, null);

		minFolderToAdd.setExtendedAttributes(singletonList(ExtendedAttributeDto.builder().key(fakeMetadata1).values(asList("ab", "cd")).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE).doesNotContain(CLOSE_BRACE)
				.isEqualTo(i18n.$(new MetadataNotMultivalueException(fakeMetadata1).getValidationError()));
	}

	@Test
	public void testCreateFolderWithEmptyExtAttributeValues() throws Exception {
		minFolderToAdd.setExtendedAttributes(singletonList(
				ExtendedAttributeDto.builder().key(fakeMetadata1).values(Collections.<String>emptyList()).build()));
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE)
				.doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_EMPTY_MESSAGE, "extendedAttributes[0].values"));
	}

	@Test
	public void testCreateFolderWithMissingAdministrativeUnitId() throws Exception {
		minFolderToAdd.setAdministrativeUnit(AdministrativeUnitDto.builder().build());
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE)
				.doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "administrativeUnit.id"));
	}

	@Test
	public void testCreateFolderWithMissingCategoryId() throws Exception {
		minFolderToAdd.setCategory(CategoryDto.builder().build());
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE)
				.doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "category.id"));
	}

	@Test
	public void testCreateFolderWithMissingContainerId() throws Exception {
		minFolderToAdd.setContainer(ContainerDto.builder().build());
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE)
				.doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "container.id"));
	}

	@Test
	public void testCreateFolderWithMissingRetentionRuleId() throws Exception {
		minFolderToAdd.setRetentionRule(RetentionRuleDto.builder().build());
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain(OPEN_BRACE)
				.doesNotContain(CLOSE_BRACE).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "retentionRule.id"));
	}

	@Test
	public void testCreateFolderWithCustomSchema() throws Exception {
		minFolderToAdd.setType(FolderTypeDto.builder().id(records.folderTypeMeeting().getId()).build());
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);

		FolderDto folder = response.readEntity(FolderDto.class);
		assertThat(folder.getId()).isNotNull().isNotEmpty();
		assertThat(folder.getTitle()).isEqualTo(minFolderToAdd.getTitle());
		assertThat(folder.getType().getId()).isEqualTo(minFolderToAdd.getType().getId());
		assertThat(folder.getType().getCode()).isEqualTo(records.folderTypeMeeting().getCode());
		assertThat(folder.getType().getTitle()).isEqualTo(records.folderTypeMeeting().getTitle());

		Record record = recordServices.getDocumentById(folder.getId());
		assertThat(record).isNotNull();
		assertThatRecord(record).extracting(Folder.TITLE, Folder.TYPE)
				.containsExactly(folder.getTitle(), folder.getType().getId());
	}

	@Test
	public void testCreateFolderDefaultFlushMode() throws Exception {
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		FolderDto newFolder = response.readEntity(FolderDto.class);

		Record folderRecord = searchFolderById(newFolder.getId());
		assertThat(folderRecord).isNull();

		Record folder = recordServices.realtimeGetRecordById(newFolder.getId());
		assertThat(folder).isNotNull();

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + folder.getVersion() + "\"");

		TimeUnit.MILLISECONDS.sleep(5250);

		folderRecord = searchFolderById(newFolder.getId());
		assertThat(folderRecord).isNotNull();
		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + folderRecord.getVersion() + "\"");
	}

	@Test
	public void testCreateFolderNowFlushMode() throws Exception {
		Response response = doPostQuery("NOW", minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).isNotEmpty();

		FolderDto newFolder = response.readEntity(FolderDto.class);
		Record folder = searchFolderById(newFolder.getId());
		assertThat(folder).isNotNull();

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + folder.getVersion() + "\"");
	}

	@Test
	public void testCreateFolderLaterFlushMode() throws Exception {
		Response response = doPostQuery("LATER", minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		assertThat(commitCounter.newCommitsCall()).isEmpty();

		FolderDto newFolder = response.readEntity(FolderDto.class);

		Record folderRecord = recordServices.getDocumentById(newFolder.getId());
		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + folderRecord.getVersion() + "\"");

		recordServices.update(rm.wrapFolder(folderRecord).setTitle("title2"));
		assertThat(commitCounter.newCommitsCall()).hasSize(1);

		Record folder = searchFolderById(newFolder.getId());
		assertThat(folder).isNotNull();
		assertThat(folder.getTitle()).isEqualTo("title2");
	}

	@Test
	public void testCreateFolderWithUnallowedHostHeader() throws Exception {
		host = "fakedns.com";
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(new UnallowedHostException(host).getValidationError()));
	}

	@Test
	public void testCreateFolderWithAllowedHost() throws Exception {
		host = "localhost2";
		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
	}

	//
	// COPY
	//

	@Test
	public void testCopyWithEmptyFolder() throws Exception {
		copySource = records.folder_A01;

		Response response = doPostQuery(null);
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);

		Folder sourceFolder = records.getFolder_A01();
		FolderDto folderDto = response.readEntity(FolderDto.class);
		assertThat(folderDto.getId()).isNotNull().isNotEmpty();
		assertThat(folderDto.getTitle().startsWith(sourceFolder.getTitle())).isTrue();
		assertThat(folderDto.getAdministrativeUnit().getId()).isEqualTo(sourceFolder.getAdministrativeUnit());
		assertThat(folderDto.getAdministrativeUnit().getCode()).isNotNull();
		assertThat(folderDto.getAdministrativeUnit().getTitle()).isNotNull();
		assertThat(folderDto.getParentFolderId()).isEqualTo(sourceFolder.getParentFolder());
		assertThat(folderDto.getCategory().getId()).isEqualTo(sourceFolder.getCategory());
		assertThat(folderDto.getCategory().getTitle()).isNotNull();
		assertThat(folderDto.getRetentionRule().getId()).isEqualTo(sourceFolder.getRetentionRule());
		assertThat(folderDto.getRetentionRule().getCode()).isNotNull();
		assertThat(folderDto.getRetentionRule().getTitle()).isNotNull();
		assertThat(folderDto.getCopyStatus()).isEqualTo(sourceFolder.getCopyStatus().getCode());
		assertThat(folderDto.getOpeningDate()).isEqualTo(sourceFolder.getOpeningDate());

		Record record = recordServices.getDocumentById(folderDto.getId());
		assertThat(record).isNotNull();
		assertThatRecord(record).extracting(Folder.TITLE, Folder.ADMINISTRATIVE_UNIT, Folder.CATEGORY,
				Folder.RETENTION_RULE, Folder.COPY_STATUS, Folder.OPENING_DATE)
				.containsExactly(folderDto.getTitle(), folderDto.getAdministrativeUnit().getId(), folderDto.getCategory().getId(),
						folderDto.getRetentionRule().getId(), toCopyType(folderDto.getCopyStatus()), folderDto.getOpeningDate());

		long count = searchServices.getResultsCount(from(rm.document.schema())
				.where(rm.document.folder()).is(copySource));
		long copyCount = searchServices.getResultsCount(from(rm.document.schema())
				.where(rm.document.folder()).is(folderDto.getId()));
		assertThat(copyCount).isEqualTo(count);

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + record.getVersion() + "\"");
	}

	@Test
	public void testCopyFolderWithParentFolderIdOnly() throws Exception {
		copySource = records.folder_C35;

		FolderDto minFolderToCopy = FolderDto.builder().parentFolderId(minFolderToAdd.getParentFolderId())
				.title(minFolderToAdd.getTitle()).build();

		Response response = doPostQuery(minFolderToCopy);
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);

		FolderDto folderDto = response.readEntity(FolderDto.class);
		assertThat(folderDto.getId()).isNotNull().isNotEmpty();
		assertThat(folderDto.getTitle()).isEqualTo(minFolderToCopy.getTitle());
		assertThat(folderDto.getParentFolderId()).isEqualTo(minFolderToAdd.getParentFolderId());
		assertThat(folderDto.getAdministrativeUnit().getId()).isEqualTo(minFolderToAdd.getAdministrativeUnit().getId());
		assertThat(folderDto.getAdministrativeUnit().getCode()).isNotNull();
		assertThat(folderDto.getAdministrativeUnit().getTitle()).isNotNull();
		assertThat(folderDto.getCategory().getId()).isEqualTo(minFolderToAdd.getCategory().getId());
		assertThat(folderDto.getCategory().getTitle()).isNotNull();
		assertThat(folderDto.getRetentionRule().getId()).isEqualTo(minFolderToAdd.getRetentionRule().getId());
		assertThat(folderDto.getRetentionRule().getCode()).isNotNull();
		assertThat(folderDto.getRetentionRule().getTitle()).isNotNull();
		assertThat(folderDto.getCopyStatus()).isEqualTo(minFolderToAdd.getCopyStatus());

		Record record = recordServices.getDocumentById(folderDto.getId());
		assertThat(record).isNotNull();
		assertThatRecord(record).extracting(Folder.TITLE, Folder.ADMINISTRATIVE_UNIT, Folder.CATEGORY,
				Folder.RETENTION_RULE, Folder.COPY_STATUS, Folder.OPENING_DATE)
				.containsExactly(folderDto.getTitle(), folderDto.getAdministrativeUnit().getId(), folderDto.getCategory().getId(),
						folderDto.getRetentionRule().getId(), toCopyType(folderDto.getCopyStatus()), folderDto.getOpeningDate());

		long count = searchServices.getResultsCount(from(rm.document.schema())
				.where(rm.document.folder()).is(copySource));
		long copyCount = searchServices.getResultsCount(from(rm.document.schema())
				.where(rm.document.folder()).is(folderDto.getId()));
		assertThat(copyCount).isEqualTo(count);

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + record.getVersion() + "\"");
	}

	@Test
	public void testCopyMinimalFolderWithoutParentFolderId() throws Exception {
		folderId = null;
		copySource = records.folder_C35;
		minFolderToAdd.setParentFolderId(null);
		minFolderToAdd.setOpeningDate(null);

		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);

		FolderDto folderDto = response.readEntity(FolderDto.class);
		assertThat(folderDto.getId()).isNotNull().isNotEmpty();
		assertThat(folderDto.getTitle()).isEqualTo(minFolderToAdd.getTitle());
		assertThat(folderDto.getCategory()).isEqualTo(minFolderToAdd.getCategory());
		assertThat(folderDto.getAdministrativeUnit().getId()).isEqualTo(minFolderToAdd.getAdministrativeUnit().getId());
		assertThat(folderDto.getAdministrativeUnit().getCode()).isNotNull();
		assertThat(folderDto.getAdministrativeUnit().getTitle()).isNotNull();
		assertThat(folderDto.getParentFolderId()).isEqualTo(minFolderToAdd.getParentFolderId());
		assertThat(folderDto.getRetentionRule().getId()).isEqualTo(minFolderToAdd.getRetentionRule().getId());
		assertThat(folderDto.getRetentionRule().getCode()).isNotNull();
		assertThat(folderDto.getRetentionRule().getTitle()).isNotNull();
		assertThat(folderDto.getCopyStatus()).isEqualTo(minFolderToAdd.getCopyStatus());

		Record record = recordServices.getDocumentById(folderDto.getId());
		assertThat(record).isNotNull();
		assertThatRecord(record).extracting(Folder.TITLE, Folder.ADMINISTRATIVE_UNIT, Folder.CATEGORY,
				Folder.RETENTION_RULE, Folder.COPY_STATUS, Folder.OPENING_DATE)
				.containsExactly(folderDto.getTitle(), folderDto.getAdministrativeUnit().getId(),
						folderDto.getCategory().getId(), folderDto.getRetentionRule().getId(),
						toCopyType(folderDto.getCopyStatus()), folderDto.getOpeningDate());

		long count = searchServices.getResultsCount(from(rm.document.schema())
				.where(rm.document.folder()).is(copySource));
		long copyCount = searchServices.getResultsCount(from(rm.document.schema())
				.where(rm.document.folder()).is(folderDto.getId()));
		assertThat(copyCount).isEqualTo(count);

		assertThat(response.getHeaderString("ETag")).isEqualTo("\"" + record.getVersion() + "\"");
	}

	@Test
	public void testCopyFolderWithSubfolders() throws Exception {
		recordServices.update(records.getFolder_C34().setParentFolder(records.folder_C35));

		folderId = null;
		copySource = records.folder_C35;

		Response response = doPostQuery(null);
		assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);

		FolderDto folderDto = response.readEntity(FolderDto.class);
		assertThat(folderDto.getId()).isNotNull().isNotEmpty();

		long count = searchServices.getResultsCount(from(rm.folder.schema())
				.where(rm.folder.parentFolder()).is(copySource));
		long copyCount = searchServices.getResultsCount(from(rm.folder.schema())
				.where(rm.folder.parentFolder()).is(folderDto.getId()));
		assertThat(copyCount).isEqualTo(count);
	}

	@Test
	public void testCopyFolderWhenTryingToSetCalculatedMetadata() throws Exception {
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaBuilder schemaBuilder = types.getSchema(Folder.DEFAULT_SCHEMA);
				schemaBuilder.create("USRcalculated").setType(MetadataValueType.DATE)
						.defineDataEntry().asCalculated(DummyCalculator.class);
			}
		});

		copySource = records.folder_C35;

		FolderDto minFolderToCopy = FolderDto.builder().parentFolderId(minFolderToAdd.getParentFolderId())
				.title(minFolderToAdd.getTitle())
				.extendedAttributes(singletonList(ExtendedAttributeDto.builder()
						.key("USRcalculated").values(singletonList("2019-01-01")).build()))
				.build();

		Response response = doPostQuery(minFolderToCopy);
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(new MetadataNotManualException("USRcalculated").getValidationError()));
	}

	@Test
	public void testCopyFolderWhenCopyIsNotPermittedByExtension() throws Exception {
		RMModuleExtensions rmModuleExtensions = getAppLayerFactory().getExtensions().forCollection(zeCollection)
				.forModule(ConstellioRMModule.ID);
		rmModuleExtensions.getFolderExtensions().add(
				new FolderExtension() {
					@Override
					public ExtensionBooleanResult isCopyActionPossible(FolderExtensionActionPossibleParams params) {
						return ExtensionBooleanResult.FALSE;
					}
				});

		copySource = records.folder_C35;

		Response response = doPostQuery(null);
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(new RecordCopyNotPermittedException(copySource).getValidationError()));
	}

	@Test
	public void testCreateDocumentWithCalculatedUsr() throws Exception {
		addUserCalculatedMetadata(Folder.DEFAULT_SCHEMA);

		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
	}

	@Test
	public void testCreateDocumentWithoutRetentionRule() throws Exception {
		folderId = null;

		minFolderToAdd.setParentFolderId(null);
		minFolderToAdd.setCopyStatus(null);
		minFolderToAdd.setRetentionRule(null);

		Response response = doPostQuery(minFolderToAdd);
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
	}

	//
	// PRIVATE FUNCTIONS
	//

	private Response doPostQuery(FolderDto folder, String... excludedParam) throws Exception {
		return doPostQuery(null, true, folder, excludedParam);
	}

	private Response doPostQuery(FolderDto folder, boolean calculateSignature, String... excludedParam)
			throws Exception {
		return doPostQuery(null, calculateSignature, folder, excludedParam);
	}

	private Response doFilteredPostQuery(FolderDto folder, Object... filters) throws Exception {
		return buildPostQuery().queryParam("filter", filters)
				.request().header("host", host).post(entity(folder, APPLICATION_JSON_TYPE));
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
		if (copySource != null) {
			query = query.header(CustomHttpHeaders.COPY_SOURCE, copySource);
		}
		return query.post(entity(folder, APPLICATION_JSON_TYPE));
	}

	private WebTarget buildPostQuery(String... excludedParam) throws Exception {
		return buildPostQuery(true, excludedParam);
	}

	private WebTarget buildPostQuery(boolean calculateSignature, String... excludedParam) throws Exception {
		method = !HttpMethods.contains(method) ? "fakeMethod" : POST;
		return buildQuery(getClass(), webTarget, calculateSignature,
				asList("folderId", "serviceKey", "method", "date", "expiration", "copySource", "signature"), excludedParam);
	}

	private Record searchFolderById(String folderId) {
		List<Record> folders = searchServices.search(new LogicalSearchQuery(
				from(rm.folder.schemaType()).where(Schemas.IDENTIFIER).isEqualTo(folderId)));
		if (folders.isEmpty()) {
			return null;
		}
		return folders.get(0);
	}

}
