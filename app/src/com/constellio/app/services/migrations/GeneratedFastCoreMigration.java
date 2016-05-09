package com.constellio.app.services.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class GeneratedFastCoreMigration {

	String collection;
	AppLayerFactory appLayerFactory;
	MigrationResourcesProvider resourcesProvider;

	public GeneratedFastCoreMigration(String collection, AppLayerFactory appLayerFactory,
			MigrationResourcesProvider resourcesProvider) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.resourcesProvider = resourcesProvider;
	}

	@SuppressWarnings("unused")
	public void applyGeneratedSchemaAlteration(MetadataSchemaTypesBuilder typesBuilder) {
		MetadataSchemaTypeBuilder collectionSchemaType = typesBuilder.createNewSchemaType("collection").setSecurity(false);
		MetadataSchemaBuilder collectionSchema = collectionSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder emailToSendSchemaType = typesBuilder.createNewSchemaType("emailToSend").setSecurity(false);
		MetadataSchemaBuilder emailToSendSchema = emailToSendSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder eventSchemaType = typesBuilder.createNewSchemaType("event").setSecurity(false);
		MetadataSchemaBuilder eventSchema = eventSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder facetSchemaType = typesBuilder.createNewSchemaType("facet").setSecurity(false);
		MetadataSchemaBuilder facet_fieldSchema = facetSchemaType.createCustomSchema("field");
		MetadataSchemaBuilder facet_querySchema = facetSchemaType.createCustomSchema("query");
		MetadataSchemaBuilder facetSchema = facetSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder groupSchemaType = typesBuilder.createNewSchemaType("group").setSecurity(false);
		MetadataSchemaBuilder groupSchema = groupSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder reportSchemaType = typesBuilder.createNewSchemaType("report").setSecurity(false);
		MetadataSchemaBuilder reportSchema = reportSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder savedSearchSchemaType = typesBuilder.createNewSchemaType("savedSearch").setSecurity(false);
		MetadataSchemaBuilder savedSearchSchema = savedSearchSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder taskSchemaType = typesBuilder.createNewSchemaType("task").setSecurity(false);
		MetadataSchemaBuilder task_approvalSchema = taskSchemaType.createCustomSchema("approval");
		MetadataSchemaBuilder taskSchema = taskSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder userSchemaType = typesBuilder.createNewSchemaType("user").setSecurity(false);
		MetadataSchemaBuilder userSchema = userSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder userDocumentSchemaType = typesBuilder.createNewSchemaType("userDocument").setSecurity(false);
		MetadataSchemaBuilder userDocumentSchema = userDocumentSchemaType.getDefaultSchema();
		MetadataBuilder collection_code = collectionSchema.create("code").setType(MetadataValueType.STRING).setUndeletable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder collection_languages = collectionSchema.create("languages").setType(MetadataValueType.STRING).setMultivalue(true).setUndeletable(true).setUnmodifiable(true);
		MetadataBuilder collection_name = collectionSchema.create("name").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder emailToSend_BCC = emailToSendSchema.create("BCC").setType(MetadataValueType.STRUCTURE).setMultivalue(true).setUndeletable(true);
		MetadataBuilder emailToSend_CC = emailToSendSchema.create("CC").setType(MetadataValueType.STRUCTURE).setMultivalue(true).setUndeletable(true);
		MetadataBuilder emailToSend_error = emailToSendSchema.create("error").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder emailToSend_from = emailToSendSchema.create("from").setType(MetadataValueType.STRUCTURE).setUndeletable(true);
		MetadataBuilder emailToSend_parameters = emailToSendSchema.create("parameters").setType(MetadataValueType.STRING).setMultivalue(true).setUndeletable(true);
		MetadataBuilder emailToSend_sendOn = emailToSendSchema.create("sendOn").setType(MetadataValueType.DATE_TIME).setUndeletable(true);
		MetadataBuilder emailToSend_subject = emailToSendSchema.create("subject").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder emailToSend_template = emailToSendSchema.create("template").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder emailToSend_to = emailToSendSchema.create("to").setType(MetadataValueType.STRUCTURE).setMultivalue(true).setUndeletable(true);
		MetadataBuilder emailToSend_tryingCount = emailToSendSchema.create("tryingCount").setType(MetadataValueType.NUMBER).setDefaultRequirement(true).setUndeletable(true);
		MetadataBuilder event_delta = eventSchema.create("delta").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder event_eventPrincipalPath = eventSchema.create("eventPrincipalPath").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder event_ip = eventSchema.create("ip").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder event_permissionDateRange = eventSchema.create("permissionDateRange").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder event_permissionRoles = eventSchema.create("permissionRoles").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder event_permissionUsers = eventSchema.create("permissionUsers").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder event_reason = eventSchema.create("reason").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder event_recordIdentifier = eventSchema.create("recordIdentifier").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder event_type = eventSchema.create("type").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder event_userRoles = eventSchema.create("userRoles").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder event_username = eventSchema.create("username").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder facet_field_fieldValuesLabel = facet_fieldSchema.create("fieldValuesLabel").setType(MetadataValueType.STRUCTURE).setUndeletable(true);
		MetadataBuilder facet_query_listQueries = facet_querySchema.create("listQueries").setType(MetadataValueType.STRUCTURE).setUndeletable(true);
		MetadataBuilder facet_active = facetSchema.create("active").setType(MetadataValueType.BOOLEAN).setUndeletable(true);
		MetadataBuilder facet_elementPerPage = facetSchema.create("elementPerPage").setType(MetadataValueType.NUMBER).setDefaultRequirement(true).setUndeletable(true);
		MetadataBuilder facet_facetType = facetSchema.create("facetType").setType(MetadataValueType.ENUM).setDefaultRequirement(true).setUndeletable(true);
		MetadataBuilder facet_fieldDatastoreCode = facetSchema.create("fieldDatastoreCode").setType(MetadataValueType.STRING).setUndeletable(true).setEssential(true);
		MetadataBuilder facet_openByDefault = facetSchema.create("openByDefault").setType(MetadataValueType.BOOLEAN).setUndeletable(true);
		MetadataBuilder facet_order = facetSchema.create("order").setType(MetadataValueType.NUMBER).setUndeletable(true);
		MetadataBuilder facet_orderResult = facetSchema.create("orderResult").setType(MetadataValueType.ENUM).setDefaultRequirement(true).setUndeletable(true);
		MetadataBuilder facet_pages = facetSchema.create("pages").setType(MetadataValueType.NUMBER).setUndeletable(true);
		MetadataBuilder facet_usedByModule = facetSchema.create("usedByModule").setType(MetadataValueType.STRING).setSystemReserved(true).setUndeletable(true);
		MetadataBuilder group_code = groupSchema.create("code").setType(MetadataValueType.STRING).setUndeletable(true).setSchemaAutocomplete(true).setUniqueValue(true);
		MetadataBuilder group_isGlobal = groupSchema.create("isGlobal").setType(MetadataValueType.BOOLEAN).setUndeletable(true);
		MetadataBuilder group_parent = groupSchema.create("parent").setType(MetadataValueType.REFERENCE).setUndeletable(true).defineReferencesTo(groupSchemaType);
		MetadataBuilder group_roles = groupSchema.create("roles").setType(MetadataValueType.STRING).setMultivalue(true).setUndeletable(true);
		MetadataBuilder report_columnsCount = reportSchema.create("columnsCount").setType(MetadataValueType.NUMBER).setUndeletable(true);
		MetadataBuilder report_linesCount = reportSchema.create("linesCount").setType(MetadataValueType.NUMBER).setDefaultRequirement(true).setUndeletable(true);
		MetadataBuilder report_reportedMetadata = reportSchema.create("reportedMetadata").setType(MetadataValueType.STRUCTURE).setMultivalue(true).setUndeletable(true);
		MetadataBuilder report_schemaTypeCode = reportSchema.create("schemaTypeCode").setType(MetadataValueType.STRING).setDefaultRequirement(true).setUndeletable(true);
		MetadataBuilder report_separator = reportSchema.create("separator").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder report_username = reportSchema.create("username").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder savedSearch_advancedSearch = savedSearchSchema.create("advancedSearch").setType(MetadataValueType.STRUCTURE).setMultivalue(true).setUndeletable(true);
		MetadataBuilder savedSearch_facetSelections = savedSearchSchema.create("facetSelections").setType(MetadataValueType.STRUCTURE).setMultivalue(true).setUndeletable(true);
		MetadataBuilder savedSearch_freeTextSearch = savedSearchSchema.create("freeTextSearch").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder savedSearch_pageNumber = savedSearchSchema.create("pageNumber").setType(MetadataValueType.NUMBER).setUndeletable(true);
		MetadataBuilder savedSearch_public = savedSearchSchema.create("public").setType(MetadataValueType.BOOLEAN).setUndeletable(true);
		MetadataBuilder savedSearch_schemaFilter = savedSearchSchema.create("schemaFilter").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder savedSearch_searchType = savedSearchSchema.create("searchType").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder savedSearch_sortField = savedSearchSchema.create("sortField").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder savedSearch_sortOrder = savedSearchSchema.create("sortOrder").setType(MetadataValueType.ENUM).setUndeletable(true);
		MetadataBuilder savedSearch_temporary = savedSearchSchema.create("temporary").setType(MetadataValueType.BOOLEAN).setUndeletable(true);
		MetadataBuilder savedSearch_user = savedSearchSchema.create("user").setType(MetadataValueType.REFERENCE).setUndeletable(true).defineReferencesTo(userSchemaType);
		MetadataBuilder task_approval_decision = task_approvalSchema.create("decision").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder task_assignCandidates = taskSchema.create("assignCandidates").setType(MetadataValueType.REFERENCE).setMultivalue(true).setUndeletable(true).defineReferencesTo(userSchemaType);
		MetadataBuilder task_assignedOn = taskSchema.create("assignedOn").setType(MetadataValueType.DATE_TIME).setUndeletable(true);
		MetadataBuilder task_assignedTo = taskSchema.create("assignedTo").setType(MetadataValueType.REFERENCE).setUndeletable(true).defineReferencesTo(userSchemaType);
		MetadataBuilder task_dueDate = taskSchema.create("dueDate").setType(MetadataValueType.DATE_TIME).setUndeletable(true);
		MetadataBuilder task_finishedBy = taskSchema.create("finishedBy").setType(MetadataValueType.REFERENCE).setUndeletable(true).defineReferencesTo(userSchemaType);
		MetadataBuilder task_finishedOn = taskSchema.create("finishedOn").setType(MetadataValueType.DATE_TIME).setUndeletable(true);
		MetadataBuilder task_workflowIdentifier = taskSchema.create("workflowIdentifier").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder task_workflowRecordIdentifiers = taskSchema.create("workflowRecordIdentifiers").setType(MetadataValueType.STRING).setMultivalue(true).setUndeletable(true);
		MetadataBuilder user_allroles = userSchema.create("allroles").setType(MetadataValueType.STRING).setMultivalue(true).setUndeletable(true);
		MetadataBuilder user_alluserauthorizations = userSchema.create("alluserauthorizations").setType(MetadataValueType.STRING).setMultivalue(true).setUndeletable(true);
		MetadataBuilder user_collectionDeleteAccess = userSchema.create("collectionDeleteAccess").setType(MetadataValueType.BOOLEAN).setUndeletable(true);
		MetadataBuilder user_collectionReadAccess = userSchema.create("collectionReadAccess").setType(MetadataValueType.BOOLEAN).setUndeletable(true);
		MetadataBuilder user_collectionWriteAccess = userSchema.create("collectionWriteAccess").setType(MetadataValueType.BOOLEAN).setUndeletable(true);
		MetadataBuilder user_defaultTabInFolderDisplay = userSchema.create("defaultTabInFolderDisplay").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder user_defaultTaxonomy = userSchema.create("defaultTaxonomy").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder user_email = userSchema.create("email").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder user_firstname = userSchema.create("firstname").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder user_groups = userSchema.create("groups").setType(MetadataValueType.REFERENCE).setMultivalue(true).setUndeletable(true).defineReferencesTo(groupSchemaType);
		MetadataBuilder user_groupsauthorizations = userSchema.create("groupsauthorizations").setType(MetadataValueType.STRING).setMultivalue(true).setUndeletable(true);
		MetadataBuilder user_jobTitle = userSchema.create("jobTitle").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder user_lastIPAddress = userSchema.create("lastIPAddress").setType(MetadataValueType.STRING).setSystemReserved(true).setUndeletable(true);
		MetadataBuilder user_lastLogin = userSchema.create("lastLogin").setType(MetadataValueType.DATE_TIME).setSystemReserved(true).setUndeletable(true);
		MetadataBuilder user_lastname = userSchema.create("lastname").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder user_loginLanguageCode = userSchema.create("loginLanguageCode").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder user_phone = userSchema.create("phone").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder user_signature = userSchema.create("signature").setType(MetadataValueType.TEXT).setUndeletable(true);
		MetadataBuilder user_startTab = userSchema.create("startTab").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder user_status = userSchema.create("status").setType(MetadataValueType.ENUM).setUndeletable(true);
		MetadataBuilder user_systemAdmin = userSchema.create("systemAdmin").setType(MetadataValueType.BOOLEAN).setUndeletable(true);
		MetadataBuilder user_username = userSchema.create("username").setType(MetadataValueType.STRING).setUndeletable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder user_userroles = userSchema.create("userroles").setType(MetadataValueType.STRING).setMultivalue(true).setUndeletable(true);
		MetadataBuilder user_usertokens = userSchema.create("usertokens").setType(MetadataValueType.STRING).setMultivalue(true).setUndeletable(true);
		MetadataBuilder userDocument_content = userDocumentSchema.create("content").setType(MetadataValueType.CONTENT).setUndeletable(true).setSearchable(true);
		MetadataBuilder userDocument_user = userDocumentSchema.create("user").setType(MetadataValueType.REFERENCE).setUndeletable(true).defineReferencesTo(userSchemaType);
	}

}
