package com.constellio.app.services.migrations;

import static java.util.Arrays.asList;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.calculators.UserTitleCalculator;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.calculators.AllAuthorizationsCalculator;
import com.constellio.model.services.schemas.calculators.AllUserAuthorizationsCalculator;
import com.constellio.model.services.schemas.calculators.InheritedAuthorizationsCalculator;
import com.constellio.model.services.schemas.calculators.ParentPathCalculator;
import com.constellio.model.services.schemas.calculators.PathCalculator;
import com.constellio.model.services.schemas.calculators.PathPartsCalculator;
import com.constellio.model.services.schemas.calculators.PrincipalPathCalculator;
import com.constellio.model.services.schemas.calculators.RolesCalculator;
import com.constellio.model.services.schemas.calculators.TokensCalculator2;
import com.constellio.model.services.schemas.calculators.UserTokensCalculator2;
import com.constellio.model.services.schemas.validators.DecisionValidator;
import com.constellio.model.services.schemas.validators.EmailValidator;
import com.constellio.model.services.schemas.validators.ManualTokenValidator;
import com.constellio.model.services.security.roles.RolesManager;

public final class GeneratedCoreMigrationCombo {
	String collection;

	AppLayerFactory appLayerFactory;

	MigrationResourcesProvider resourcesProvider;

	GeneratedCoreMigrationCombo(String collection, AppLayerFactory appLayerFactory,
			MigrationResourcesProvider resourcesProvider) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.resourcesProvider = resourcesProvider;
	}

	public void applyGeneratedSchemaDisplay(SchemasDisplayManager manager,
			SchemaTypesDisplayTransactionBuilder typesDisplayTransaction) {
		//Metadata
		//typesDisplayTransaction.add(manager.getType(collection, "zeType").withAdvancedSearchStatus())

	}

	public void applyGeneratedSchemaAlteration(MetadataSchemaTypesBuilder typesBuilder) {
		MetadataSchemaTypeBuilder collectionSchemaType = typesBuilder.createNewSchemaType("collection").setSecurity(false);
		MetadataSchemaBuilder collectionSchema = collectionSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder groupSchemaType = typesBuilder.createNewSchemaType("group").setSecurity(false);
		MetadataSchemaBuilder groupSchema = groupSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder userSchemaType = typesBuilder.createNewSchemaType("user").setSecurity(false);
		MetadataSchemaBuilder userSchema = userSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder emailToSendSchemaType = typesBuilder.createNewSchemaType("emailToSend").setSecurity(false);
		MetadataSchemaBuilder emailToSendSchema = emailToSendSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder eventSchemaType = typesBuilder.createNewSchemaType("event").setSecurity(false);
		MetadataSchemaBuilder eventSchema = eventSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder facetSchemaType = typesBuilder.createNewSchemaType("facet").setSecurity(false);
		MetadataSchemaBuilder facet_fieldSchema = facetSchemaType.createCustomSchema("field");
		MetadataSchemaBuilder facet_querySchema = facetSchemaType.createCustomSchema("query");
		MetadataSchemaBuilder facetSchema = facetSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder reportSchemaType = typesBuilder.createNewSchemaType("report").setSecurity(false);
		MetadataSchemaBuilder reportSchema = reportSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder savedSearchSchemaType = typesBuilder.createNewSchemaType("savedSearch").setSecurity(false);
		MetadataSchemaBuilder savedSearchSchema = savedSearchSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder taskSchemaType = typesBuilder.createNewSchemaType("task").setSecurity(false);
		MetadataSchemaBuilder task_approvalSchema = taskSchemaType.createCustomSchema("approval");
		MetadataSchemaBuilder taskSchema = taskSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder userDocumentSchemaType = typesBuilder.createNewSchemaType("userDocument").setSecurity(false);
		MetadataSchemaBuilder userDocumentSchema = userDocumentSchemaType.getDefaultSchema();
		MetadataBuilder collection_allauthorizations = collectionSchema.get("allauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder collection_authorizations = collectionSchema.get("authorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder collection_code = collectionSchema.create("code").setType(MetadataValueType.STRING).setUndeletable(true)
				.setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder collection_createdOn = collectionSchema.get("createdOn").setSystemReserved(true).setUndeletable(true)
				.setSortable(true);
		MetadataBuilder collection_deleted = collectionSchema.get("deleted").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder collection_denyTokens = collectionSchema.get("denyTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		collection_denyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder collection_detachedauthorizations = collectionSchema.get("detachedauthorizations").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder collection_followers = collectionSchema.get("followers").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true).setSearchable(true);
		MetadataBuilder collection_id = collectionSchema.get("id").setDefaultRequirement(true).setSystemReserved(true)
				.setUndeletable(true).setSearchable(true).setSortable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder collection_inheritedauthorizations = collectionSchema.get("inheritedauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder collection_languages = collectionSchema.create("languages").setType(MetadataValueType.STRING)
				.setMultivalue(true).setUndeletable(true).setUnmodifiable(true);
		MetadataBuilder collection_legacyIdentifier = collectionSchema.get("legacyIdentifier").setDefaultRequirement(true)
				.setSystemReserved(true).setUndeletable(true).setSearchable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder collection_manualTokens = collectionSchema.get("manualTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		collection_manualTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder collection_markedForPreviewConversion = collectionSchema.get("markedForPreviewConversion")
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder collection_modifiedOn = collectionSchema.get("modifiedOn").setSystemReserved(true).setUndeletable(true)
				.setSortable(true);
		MetadataBuilder collection_name = collectionSchema.create("name").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder collection_parentpath = collectionSchema.get("parentpath").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder collection_path = collectionSchema.get("path").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder collection_pathParts = collectionSchema.get("pathParts").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder collection_principalpath = collectionSchema.get("principalpath").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder collection_removedauthorizations = collectionSchema.get("removedauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder collection_schema = collectionSchema.get("schema").setDefaultRequirement(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder collection_searchable = collectionSchema.get("searchable").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder collection_shareDenyTokens = collectionSchema.get("shareDenyTokens").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		collection_shareDenyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder collection_shareTokens = collectionSchema.get("shareTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		collection_shareTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder collection_title = collectionSchema.get("title").setUndeletable(true).setSchemaAutocomplete(true)
				.setSearchable(true);
		MetadataBuilder collection_tokens = collectionSchema.get("tokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder collection_visibleInTrees = collectionSchema.get("visibleInTrees").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder group_allauthorizations = groupSchema.get("allauthorizations").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder group_authorizations = groupSchema.get("authorizations").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder group_code = groupSchema.create("code").setType(MetadataValueType.STRING).setUndeletable(true)
				.setSchemaAutocomplete(true).setUniqueValue(true);
		MetadataBuilder group_createdOn = groupSchema.get("createdOn").setSystemReserved(true).setUndeletable(true)
				.setSortable(true);
		MetadataBuilder group_deleted = groupSchema.get("deleted").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder group_denyTokens = groupSchema.get("denyTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		group_denyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder group_detachedauthorizations = groupSchema.get("detachedauthorizations").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder group_followers = groupSchema.get("followers").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true).setSearchable(true);
		MetadataBuilder group_id = groupSchema.get("id").setDefaultRequirement(true).setSystemReserved(true).setUndeletable(true)
				.setSearchable(true).setSortable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder group_inheritedauthorizations = groupSchema.get("inheritedauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder group_isGlobal = groupSchema.create("isGlobal").setType(MetadataValueType.BOOLEAN).setUndeletable(true);
		MetadataBuilder group_legacyIdentifier = groupSchema.get("legacyIdentifier").setDefaultRequirement(true)
				.setSystemReserved(true).setUndeletable(true).setSearchable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder group_manualTokens = groupSchema.get("manualTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		group_manualTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder group_markedForPreviewConversion = groupSchema.get("markedForPreviewConversion").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder group_modifiedOn = groupSchema.get("modifiedOn").setSystemReserved(true).setUndeletable(true)
				.setSortable(true);
		MetadataBuilder group_parent = groupSchema.create("parent").setType(MetadataValueType.REFERENCE).setUndeletable(true)
				.defineReferencesTo(asList(groupSchema));
		MetadataBuilder group_parentpath = groupSchema.get("parentpath").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder group_path = groupSchema.get("path").setMultivalue(true).setSystemReserved(true).setUndeletable(true);
		MetadataBuilder group_pathParts = groupSchema.get("pathParts").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder group_principalpath = groupSchema.get("principalpath").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder group_removedauthorizations = groupSchema.get("removedauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder group_roles = groupSchema.create("roles").setType(MetadataValueType.STRING).setMultivalue(true)
				.setUndeletable(true);
		MetadataBuilder group_schema = groupSchema.get("schema").setDefaultRequirement(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder group_searchable = groupSchema.get("searchable").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder group_shareDenyTokens = groupSchema.get("shareDenyTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		group_shareDenyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder group_shareTokens = groupSchema.get("shareTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		group_shareTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder group_title = groupSchema.get("title").setUndeletable(true).setSchemaAutocomplete(true)
				.setSearchable(true);
		MetadataBuilder group_tokens = groupSchema.get("tokens").setMultivalue(true).setSystemReserved(true).setUndeletable(true);
		MetadataBuilder group_visibleInTrees = groupSchema.get("visibleInTrees").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder user_allauthorizations = userSchema.get("allauthorizations").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder user_allroles = userSchema.create("allroles").setType(MetadataValueType.STRING).setMultivalue(true)
				.setUndeletable(true);
		MetadataBuilder user_alluserauthorizations = userSchema.create("alluserauthorizations").setType(MetadataValueType.STRING)
				.setMultivalue(true).setUndeletable(true);
		MetadataBuilder user_authorizations = userSchema.get("authorizations").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder user_collectionDeleteAccess = userSchema.create("collectionDeleteAccess")
				.setType(MetadataValueType.BOOLEAN).setUndeletable(true);
		MetadataBuilder user_collectionReadAccess = userSchema.create("collectionReadAccess").setType(MetadataValueType.BOOLEAN)
				.setUndeletable(true);
		MetadataBuilder user_collectionWriteAccess = userSchema.create("collectionWriteAccess").setType(MetadataValueType.BOOLEAN)
				.setUndeletable(true);
		MetadataBuilder user_createdOn = userSchema.get("createdOn").setSystemReserved(true).setUndeletable(true)
				.setSortable(true);
		MetadataBuilder user_defaultTabInFolderDisplay = userSchema.create("defaultTabInFolderDisplay")
				.setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder user_defaultTaxonomy = userSchema.create("defaultTaxonomy").setType(MetadataValueType.STRING)
				.setUndeletable(true);
		MetadataBuilder user_deleted = userSchema.get("deleted").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder user_denyTokens = userSchema.get("denyTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		user_denyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder user_detachedauthorizations = userSchema.get("detachedauthorizations").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder user_email = userSchema.create("email").setType(MetadataValueType.STRING).setUndeletable(true);
		user_email.defineValidators().add(EmailValidator.class);
		MetadataBuilder user_firstname = userSchema.create("firstname").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder user_followers = userSchema.get("followers").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true).setSearchable(true);
		MetadataBuilder user_groups = userSchema.create("groups").setType(MetadataValueType.REFERENCE).setMultivalue(true)
				.setUndeletable(true).defineReferencesTo(groupSchemaType);
		MetadataBuilder user_groupsauthorizations = userSchema.create("groupsauthorizations").setType(MetadataValueType.STRING)
				.setMultivalue(true).setUndeletable(true);
		MetadataBuilder user_id = userSchema.get("id").setDefaultRequirement(true).setSystemReserved(true).setUndeletable(true)
				.setSearchable(true).setSortable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder user_inheritedauthorizations = userSchema.get("inheritedauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder user_jobTitle = userSchema.create("jobTitle").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder user_lastIPAddress = userSchema.create("lastIPAddress").setType(MetadataValueType.STRING)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder user_lastLogin = userSchema.create("lastLogin").setType(MetadataValueType.DATE_TIME)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder user_lastname = userSchema.create("lastname").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder user_legacyIdentifier = userSchema.get("legacyIdentifier").setDefaultRequirement(true)
				.setSystemReserved(true).setUndeletable(true).setSearchable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder user_loginLanguageCode = userSchema.create("loginLanguageCode").setType(MetadataValueType.STRING)
				.setUndeletable(true);
		MetadataBuilder user_manualTokens = userSchema.get("manualTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		user_manualTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder user_markedForPreviewConversion = userSchema.get("markedForPreviewConversion").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder user_modifiedOn = userSchema.get("modifiedOn").setSystemReserved(true).setUndeletable(true)
				.setSortable(true);
		MetadataBuilder user_parentpath = userSchema.get("parentpath").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder user_path = userSchema.get("path").setMultivalue(true).setSystemReserved(true).setUndeletable(true);
		MetadataBuilder user_pathParts = userSchema.get("pathParts").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder user_phone = userSchema.create("phone").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder user_principalpath = userSchema.get("principalpath").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder user_removedauthorizations = userSchema.get("removedauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder user_schema = userSchema.get("schema").setDefaultRequirement(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder user_searchable = userSchema.get("searchable").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder user_shareDenyTokens = userSchema.get("shareDenyTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		user_shareDenyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder user_shareTokens = userSchema.get("shareTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		user_shareTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder user_signature = userSchema.create("signature").setType(MetadataValueType.TEXT).setUndeletable(true);
		MetadataBuilder user_startTab = userSchema.create("startTab").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder user_status = userSchema.create("status").setType(MetadataValueType.ENUM).setUndeletable(true)
				.defineAsEnum(com.constellio.model.entities.security.global.UserCredentialStatus.class);
		MetadataBuilder user_systemAdmin = userSchema.create("systemAdmin").setType(MetadataValueType.BOOLEAN)
				.setUndeletable(true);
		MetadataBuilder user_title = userSchema.get("title").setUndeletable(true).setSchemaAutocomplete(true).setSearchable(true);
		MetadataBuilder user_tokens = userSchema.get("tokens").setMultivalue(true).setSystemReserved(true).setUndeletable(true);
		MetadataBuilder user_username = userSchema.create("username").setType(MetadataValueType.STRING).setUndeletable(true)
				.setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder user_userroles = userSchema.create("userroles").setType(MetadataValueType.STRING).setMultivalue(true)
				.setUndeletable(true);
		MetadataBuilder user_usertokens = userSchema.create("usertokens").setType(MetadataValueType.STRING).setMultivalue(true)
				.setUndeletable(true);
		MetadataBuilder user_visibleInTrees = userSchema.get("visibleInTrees").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder emailToSend_BCC = emailToSendSchema.create("BCC").setType(MetadataValueType.STRUCTURE).setMultivalue(true)
				.setUndeletable(true).defineStructureFactory(com.constellio.model.entities.structures.EmailAddressFactory.class);
		MetadataBuilder emailToSend_CC = emailToSendSchema.create("CC").setType(MetadataValueType.STRUCTURE).setMultivalue(true)
				.setUndeletable(true).defineStructureFactory(com.constellio.model.entities.structures.EmailAddressFactory.class);
		MetadataBuilder emailToSend_allauthorizations = emailToSendSchema.get("allauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder emailToSend_authorizations = emailToSendSchema.get("authorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder emailToSend_createdBy = emailToSendSchema.get("createdBy").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder emailToSend_createdOn = emailToSendSchema.get("createdOn").setSystemReserved(true).setUndeletable(true)
				.setSortable(true);
		MetadataBuilder emailToSend_deleted = emailToSendSchema.get("deleted").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder emailToSend_denyTokens = emailToSendSchema.get("denyTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		emailToSend_denyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder emailToSend_detachedauthorizations = emailToSendSchema.get("detachedauthorizations")
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder emailToSend_error = emailToSendSchema.create("error").setType(MetadataValueType.STRING)
				.setUndeletable(true);
		MetadataBuilder emailToSend_followers = emailToSendSchema.get("followers").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true).setSearchable(true);
		MetadataBuilder emailToSend_from = emailToSendSchema.create("from").setType(MetadataValueType.STRUCTURE)
				.setUndeletable(true).defineStructureFactory(com.constellio.model.entities.structures.EmailAddressFactory.class);
		MetadataBuilder emailToSend_id = emailToSendSchema.get("id").setDefaultRequirement(true).setSystemReserved(true)
				.setUndeletable(true).setSearchable(true).setSortable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder emailToSend_inheritedauthorizations = emailToSendSchema.get("inheritedauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder emailToSend_legacyIdentifier = emailToSendSchema.get("legacyIdentifier").setDefaultRequirement(true)
				.setSystemReserved(true).setUndeletable(true).setSearchable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder emailToSend_manualTokens = emailToSendSchema.get("manualTokens").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		emailToSend_manualTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder emailToSend_markedForPreviewConversion = emailToSendSchema.get("markedForPreviewConversion")
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder emailToSend_modifiedBy = emailToSendSchema.get("modifiedBy").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder emailToSend_modifiedOn = emailToSendSchema.get("modifiedOn").setSystemReserved(true).setUndeletable(true)
				.setSortable(true);
		MetadataBuilder emailToSend_parameters = emailToSendSchema.create("parameters").setType(MetadataValueType.STRING)
				.setMultivalue(true).setUndeletable(true);
		MetadataBuilder emailToSend_parentpath = emailToSendSchema.get("parentpath").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder emailToSend_path = emailToSendSchema.get("path").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder emailToSend_pathParts = emailToSendSchema.get("pathParts").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder emailToSend_principalpath = emailToSendSchema.get("principalpath").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder emailToSend_removedauthorizations = emailToSendSchema.get("removedauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder emailToSend_schema = emailToSendSchema.get("schema").setDefaultRequirement(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder emailToSend_searchable = emailToSendSchema.get("searchable").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder emailToSend_sendOn = emailToSendSchema.create("sendOn").setType(MetadataValueType.DATE_TIME)
				.setUndeletable(true);
		MetadataBuilder emailToSend_shareDenyTokens = emailToSendSchema.get("shareDenyTokens").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		emailToSend_shareDenyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder emailToSend_shareTokens = emailToSendSchema.get("shareTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		emailToSend_shareTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder emailToSend_subject = emailToSendSchema.create("subject").setType(MetadataValueType.STRING)
				.setUndeletable(true);
		MetadataBuilder emailToSend_template = emailToSendSchema.create("template").setType(MetadataValueType.STRING)
				.setUndeletable(true);
		MetadataBuilder emailToSend_title = emailToSendSchema.get("title").setUndeletable(true).setSchemaAutocomplete(true)
				.setSearchable(true);
		MetadataBuilder emailToSend_to = emailToSendSchema.create("to").setType(MetadataValueType.STRUCTURE).setMultivalue(true)
				.setUndeletable(true).defineStructureFactory(com.constellio.model.entities.structures.EmailAddressFactory.class);
		MetadataBuilder emailToSend_tokens = emailToSendSchema.get("tokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder emailToSend_tryingCount = emailToSendSchema.create("tryingCount").setType(MetadataValueType.NUMBER)
				.setDefaultRequirement(true).setUndeletable(true).setDefaultValue(0.0);
		MetadataBuilder emailToSend_visibleInTrees = emailToSendSchema.get("visibleInTrees").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder event_allauthorizations = eventSchema.get("allauthorizations").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder event_authorizations = eventSchema.get("authorizations").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder event_createdBy = eventSchema.get("createdBy").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder event_createdOn = eventSchema.get("createdOn").setSystemReserved(true).setUndeletable(true)
				.setSortable(true);
		MetadataBuilder event_deleted = eventSchema.get("deleted").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder event_delta = eventSchema.create("delta").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder event_denyTokens = eventSchema.get("denyTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		event_denyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder event_detachedauthorizations = eventSchema.get("detachedauthorizations").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder event_eventPrincipalPath = eventSchema.create("eventPrincipalPath").setType(MetadataValueType.STRING)
				.setUndeletable(true);
		MetadataBuilder event_followers = eventSchema.get("followers").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true).setSearchable(true);
		MetadataBuilder event_id = eventSchema.get("id").setDefaultRequirement(true).setSystemReserved(true).setUndeletable(true)
				.setSearchable(true).setSortable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder event_inheritedauthorizations = eventSchema.get("inheritedauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder event_ip = eventSchema.create("ip").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder event_legacyIdentifier = eventSchema.get("legacyIdentifier").setDefaultRequirement(true)
				.setSystemReserved(true).setUndeletable(true).setSearchable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder event_manualTokens = eventSchema.get("manualTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		event_manualTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder event_markedForPreviewConversion = eventSchema.get("markedForPreviewConversion").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder event_modifiedBy = eventSchema.get("modifiedBy").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder event_modifiedOn = eventSchema.get("modifiedOn").setSystemReserved(true).setUndeletable(true)
				.setSortable(true);
		MetadataBuilder event_parentpath = eventSchema.get("parentpath").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder event_path = eventSchema.get("path").setMultivalue(true).setSystemReserved(true).setUndeletable(true);
		MetadataBuilder event_pathParts = eventSchema.get("pathParts").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder event_permissionDateRange = eventSchema.create("permissionDateRange").setType(MetadataValueType.STRING)
				.setUndeletable(true);
		MetadataBuilder event_permissionRoles = eventSchema.create("permissionRoles").setType(MetadataValueType.STRING)
				.setUndeletable(true);
		MetadataBuilder event_permissionUsers = eventSchema.create("permissionUsers").setType(MetadataValueType.STRING)
				.setUndeletable(true);
		MetadataBuilder event_principalpath = eventSchema.get("principalpath").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder event_reason = eventSchema.create("reason").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder event_recordIdentifier = eventSchema.create("recordIdentifier").setType(MetadataValueType.STRING)
				.setUndeletable(true);
		MetadataBuilder event_removedauthorizations = eventSchema.get("removedauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder event_schema = eventSchema.get("schema").setDefaultRequirement(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder event_searchable = eventSchema.get("searchable").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder event_shareDenyTokens = eventSchema.get("shareDenyTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		event_shareDenyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder event_shareTokens = eventSchema.get("shareTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		event_shareTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder event_title = eventSchema.get("title").setUndeletable(true).setSchemaAutocomplete(true)
				.setSearchable(true);
		MetadataBuilder event_tokens = eventSchema.get("tokens").setMultivalue(true).setSystemReserved(true).setUndeletable(true);
		MetadataBuilder event_type = eventSchema.create("type").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder event_userRoles = eventSchema.create("userRoles").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder event_username = eventSchema.create("username").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder event_visibleInTrees = eventSchema.get("visibleInTrees").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder facet_field_fieldValuesLabel = facet_fieldSchema.create("fieldValuesLabel")
				.setType(MetadataValueType.STRUCTURE).setUndeletable(true)
				.defineStructureFactory(com.constellio.model.entities.structures.MapStringStringStructureFactory.class);
		MetadataBuilder facet_query_listQueries = facet_querySchema.create("listQueries").setType(MetadataValueType.STRUCTURE)
				.setUndeletable(true)
				.defineStructureFactory(com.constellio.model.entities.structures.MapStringStringStructureFactory.class);
		MetadataBuilder facet_active = facetSchema.create("active").setType(MetadataValueType.BOOLEAN).setUndeletable(true)
				.setDefaultValue(true);
		MetadataBuilder facet_allauthorizations = facetSchema.get("allauthorizations").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder facet_authorizations = facetSchema.get("authorizations").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder facet_createdBy = facetSchema.get("createdBy").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder facet_createdOn = facetSchema.get("createdOn").setSystemReserved(true).setUndeletable(true)
				.setSortable(true);
		MetadataBuilder facet_deleted = facetSchema.get("deleted").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder facet_denyTokens = facetSchema.get("denyTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		facet_denyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder facet_detachedauthorizations = facetSchema.get("detachedauthorizations").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder facet_elementPerPage = facetSchema.create("elementPerPage").setType(MetadataValueType.NUMBER)
				.setDefaultRequirement(true).setUndeletable(true).setDefaultValue(5);
		MetadataBuilder facet_facetType = facetSchema.create("facetType").setType(MetadataValueType.ENUM)
				.setDefaultRequirement(true).setUndeletable(true)
				.defineAsEnum(com.constellio.model.entities.records.wrappers.structure.FacetType.class);
		MetadataBuilder facet_fieldDatastoreCode = facetSchema.create("fieldDatastoreCode").setType(MetadataValueType.STRING)
				.setUndeletable(true).setEssential(true);
		MetadataBuilder facet_followers = facetSchema.get("followers").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true).setSearchable(true);
		MetadataBuilder facet_id = facetSchema.get("id").setDefaultRequirement(true).setSystemReserved(true).setUndeletable(true)
				.setSearchable(true).setSortable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder facet_inheritedauthorizations = facetSchema.get("inheritedauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder facet_legacyIdentifier = facetSchema.get("legacyIdentifier").setDefaultRequirement(true)
				.setSystemReserved(true).setUndeletable(true).setSearchable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder facet_manualTokens = facetSchema.get("manualTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		facet_manualTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder facet_markedForPreviewConversion = facetSchema.get("markedForPreviewConversion").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder facet_modifiedBy = facetSchema.get("modifiedBy").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder facet_modifiedOn = facetSchema.get("modifiedOn").setSystemReserved(true).setUndeletable(true)
				.setSortable(true);
		MetadataBuilder facet_openByDefault = facetSchema.create("openByDefault").setType(MetadataValueType.BOOLEAN)
				.setUndeletable(true).setDefaultValue(true);
		MetadataBuilder facet_order = facetSchema.create("order").setType(MetadataValueType.NUMBER).setUndeletable(true);
		MetadataBuilder facet_orderResult = facetSchema.create("orderResult").setType(MetadataValueType.ENUM)
				.setDefaultRequirement(true).setUndeletable(true)
				.setDefaultValue(com.constellio.model.entities.records.wrappers.structure.FacetOrderType.RELEVANCE)
				.defineAsEnum(com.constellio.model.entities.records.wrappers.structure.FacetOrderType.class);
		MetadataBuilder facet_pages = facetSchema.create("pages").setType(MetadataValueType.NUMBER).setUndeletable(true)
				.setDefaultValue(1);
		MetadataBuilder facet_parentpath = facetSchema.get("parentpath").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder facet_path = facetSchema.get("path").setMultivalue(true).setSystemReserved(true).setUndeletable(true);
		MetadataBuilder facet_pathParts = facetSchema.get("pathParts").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder facet_principalpath = facetSchema.get("principalpath").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder facet_removedauthorizations = facetSchema.get("removedauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder facet_schema = facetSchema.get("schema").setDefaultRequirement(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder facet_searchable = facetSchema.get("searchable").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder facet_shareDenyTokens = facetSchema.get("shareDenyTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		facet_shareDenyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder facet_shareTokens = facetSchema.get("shareTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		facet_shareTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder facet_title = facetSchema.get("title").setDefaultRequirement(true).setUndeletable(true)
				.setSchemaAutocomplete(true).setSearchable(true);
		MetadataBuilder facet_tokens = facetSchema.get("tokens").setMultivalue(true).setSystemReserved(true).setUndeletable(true);
		MetadataBuilder facet_usedByModule = facetSchema.create("usedByModule").setType(MetadataValueType.STRING)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder facet_visibleInTrees = facetSchema.get("visibleInTrees").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder report_allauthorizations = reportSchema.get("allauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder report_authorizations = reportSchema.get("authorizations").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder report_columnsCount = reportSchema.create("columnsCount").setType(MetadataValueType.NUMBER)
				.setUndeletable(true);
		MetadataBuilder report_createdBy = reportSchema.get("createdBy").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder report_createdOn = reportSchema.get("createdOn").setSystemReserved(true).setUndeletable(true)
				.setSortable(true);
		MetadataBuilder report_deleted = reportSchema.get("deleted").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder report_denyTokens = reportSchema.get("denyTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		report_denyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder report_detachedauthorizations = reportSchema.get("detachedauthorizations").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder report_followers = reportSchema.get("followers").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true).setSearchable(true);
		MetadataBuilder report_id = reportSchema.get("id").setDefaultRequirement(true).setSystemReserved(true)
				.setUndeletable(true).setSearchable(true).setSortable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder report_inheritedauthorizations = reportSchema.get("inheritedauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder report_legacyIdentifier = reportSchema.get("legacyIdentifier").setDefaultRequirement(true)
				.setSystemReserved(true).setUndeletable(true).setSearchable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder report_linesCount = reportSchema.create("linesCount").setType(MetadataValueType.NUMBER)
				.setDefaultRequirement(true).setUndeletable(true);
		MetadataBuilder report_manualTokens = reportSchema.get("manualTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		report_manualTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder report_markedForPreviewConversion = reportSchema.get("markedForPreviewConversion").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder report_modifiedBy = reportSchema.get("modifiedBy").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder report_modifiedOn = reportSchema.get("modifiedOn").setSystemReserved(true).setUndeletable(true)
				.setSortable(true);
		MetadataBuilder report_parentpath = reportSchema.get("parentpath").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder report_path = reportSchema.get("path").setMultivalue(true).setSystemReserved(true).setUndeletable(true);
		MetadataBuilder report_pathParts = reportSchema.get("pathParts").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder report_principalpath = reportSchema.get("principalpath").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder report_removedauthorizations = reportSchema.get("removedauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder report_reportedMetadata = reportSchema.create("reportedMetadata").setType(MetadataValueType.STRUCTURE)
				.setMultivalue(true).setUndeletable(true)
				.defineStructureFactory(com.constellio.model.entities.records.wrappers.structure.ReportedMetadataFactory.class);
		MetadataBuilder report_schema = reportSchema.get("schema").setDefaultRequirement(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder report_schemaTypeCode = reportSchema.create("schemaTypeCode").setType(MetadataValueType.STRING)
				.setDefaultRequirement(true).setUndeletable(true);
		MetadataBuilder report_searchable = reportSchema.get("searchable").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder report_separator = reportSchema.create("separator").setType(MetadataValueType.STRING)
				.setUndeletable(true);
		MetadataBuilder report_shareDenyTokens = reportSchema.get("shareDenyTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		report_shareDenyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder report_shareTokens = reportSchema.get("shareTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		report_shareTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder report_title = reportSchema.get("title").setUndeletable(true).setSchemaAutocomplete(true)
				.setSearchable(true);
		MetadataBuilder report_tokens = reportSchema.get("tokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder report_username = reportSchema.create("username").setType(MetadataValueType.STRING).setUndeletable(true);
		MetadataBuilder report_visibleInTrees = reportSchema.get("visibleInTrees").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder savedSearch_advancedSearch = savedSearchSchema.create("advancedSearch")
				.setType(MetadataValueType.STRUCTURE).setMultivalue(true).setUndeletable(true)
				.defineStructureFactory(com.constellio.app.ui.pages.search.criteria.CriterionFactory.class);
		MetadataBuilder savedSearch_allauthorizations = savedSearchSchema.get("allauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder savedSearch_authorizations = savedSearchSchema.get("authorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder savedSearch_createdBy = savedSearchSchema.get("createdBy").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder savedSearch_createdOn = savedSearchSchema.get("createdOn").setSystemReserved(true).setUndeletable(true)
				.setSortable(true);
		MetadataBuilder savedSearch_deleted = savedSearchSchema.get("deleted").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder savedSearch_denyTokens = savedSearchSchema.get("denyTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		savedSearch_denyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder savedSearch_detachedauthorizations = savedSearchSchema.get("detachedauthorizations")
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder savedSearch_facetSelections = savedSearchSchema.create("facetSelections")
				.setType(MetadataValueType.STRUCTURE).setMultivalue(true).setUndeletable(true)
				.defineStructureFactory(com.constellio.app.ui.pages.search.criteria.FacetSelectionsFactory.class);
		MetadataBuilder savedSearch_followers = savedSearchSchema.get("followers").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true).setSearchable(true);
		MetadataBuilder savedSearch_freeTextSearch = savedSearchSchema.create("freeTextSearch").setType(MetadataValueType.STRING)
				.setUndeletable(true);
		MetadataBuilder savedSearch_id = savedSearchSchema.get("id").setDefaultRequirement(true).setSystemReserved(true)
				.setUndeletable(true).setSearchable(true).setSortable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder savedSearch_inheritedauthorizations = savedSearchSchema.get("inheritedauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder savedSearch_legacyIdentifier = savedSearchSchema.get("legacyIdentifier").setDefaultRequirement(true)
				.setSystemReserved(true).setUndeletable(true).setSearchable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder savedSearch_manualTokens = savedSearchSchema.get("manualTokens").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		savedSearch_manualTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder savedSearch_markedForPreviewConversion = savedSearchSchema.get("markedForPreviewConversion")
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder savedSearch_modifiedBy = savedSearchSchema.get("modifiedBy").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder savedSearch_modifiedOn = savedSearchSchema.get("modifiedOn").setSystemReserved(true).setUndeletable(true)
				.setSortable(true);
		MetadataBuilder savedSearch_pageNumber = savedSearchSchema.create("pageNumber").setType(MetadataValueType.NUMBER)
				.setUndeletable(true);
		MetadataBuilder savedSearch_parentpath = savedSearchSchema.get("parentpath").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder savedSearch_path = savedSearchSchema.get("path").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder savedSearch_pathParts = savedSearchSchema.get("pathParts").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder savedSearch_principalpath = savedSearchSchema.get("principalpath").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder savedSearch_public = savedSearchSchema.create("public").setType(MetadataValueType.BOOLEAN)
				.setUndeletable(true);
		MetadataBuilder savedSearch_removedauthorizations = savedSearchSchema.get("removedauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder savedSearch_schema = savedSearchSchema.get("schema").setDefaultRequirement(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder savedSearch_schemaFilter = savedSearchSchema.create("schemaFilter").setType(MetadataValueType.STRING)
				.setUndeletable(true);
		MetadataBuilder savedSearch_searchType = savedSearchSchema.create("searchType").setType(MetadataValueType.STRING)
				.setUndeletable(true);
		MetadataBuilder savedSearch_searchable = savedSearchSchema.get("searchable").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder savedSearch_shareDenyTokens = savedSearchSchema.get("shareDenyTokens").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		savedSearch_shareDenyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder savedSearch_shareTokens = savedSearchSchema.get("shareTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		savedSearch_shareTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder savedSearch_sortField = savedSearchSchema.create("sortField").setType(MetadataValueType.STRING)
				.setUndeletable(true);
		MetadataBuilder savedSearch_sortOrder = savedSearchSchema.create("sortOrder").setType(MetadataValueType.ENUM)
				.setUndeletable(true).defineAsEnum(com.constellio.model.entities.records.wrappers.SavedSearch.SortOrder.class);
		MetadataBuilder savedSearch_temporary = savedSearchSchema.create("temporary").setType(MetadataValueType.BOOLEAN)
				.setUndeletable(true);
		MetadataBuilder savedSearch_title = savedSearchSchema.get("title").setUndeletable(true).setSchemaAutocomplete(true)
				.setSearchable(true);
		MetadataBuilder savedSearch_tokens = savedSearchSchema.get("tokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder savedSearch_user = savedSearchSchema.create("user").setType(MetadataValueType.REFERENCE)
				.setUndeletable(true).defineReferencesTo(userSchemaType);
		MetadataBuilder savedSearch_visibleInTrees = savedSearchSchema.get("visibleInTrees").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder task_approval_decision = task_approvalSchema.create("decision").setType(MetadataValueType.STRING)
				.setUndeletable(true);
		task_approval_decision.defineValidators().add(DecisionValidator.class);
		MetadataBuilder task_allauthorizations = taskSchema.get("allauthorizations").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder task_assignCandidates = taskSchema.create("assignCandidates").setType(MetadataValueType.REFERENCE)
				.setMultivalue(true).setUndeletable(true).defineReferencesTo(userSchemaType);
		MetadataBuilder task_assignedOn = taskSchema.create("assignedOn").setType(MetadataValueType.DATE_TIME)
				.setUndeletable(true);
		MetadataBuilder task_assignedTo = taskSchema.create("assignedTo").setType(MetadataValueType.REFERENCE)
				.setUndeletable(true).defineReferencesTo(userSchemaType);
		MetadataBuilder task_authorizations = taskSchema.get("authorizations").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder task_createdBy = taskSchema.get("createdBy").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder task_createdOn = taskSchema.get("createdOn").setSystemReserved(true).setUndeletable(true)
				.setSortable(true);
		MetadataBuilder task_deleted = taskSchema.get("deleted").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder task_denyTokens = taskSchema.get("denyTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		task_denyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder task_detachedauthorizations = taskSchema.get("detachedauthorizations").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder task_dueDate = taskSchema.create("dueDate").setType(MetadataValueType.DATE_TIME).setUndeletable(true);
		MetadataBuilder task_finishedBy = taskSchema.create("finishedBy").setType(MetadataValueType.REFERENCE)
				.setUndeletable(true).defineReferencesTo(userSchemaType);
		MetadataBuilder task_finishedOn = taskSchema.create("finishedOn").setType(MetadataValueType.DATE_TIME)
				.setUndeletable(true);
		MetadataBuilder task_followers = taskSchema.get("followers").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true).setSearchable(true);
		MetadataBuilder task_id = taskSchema.get("id").setDefaultRequirement(true).setSystemReserved(true).setUndeletable(true)
				.setSearchable(true).setSortable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder task_inheritedauthorizations = taskSchema.get("inheritedauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder task_legacyIdentifier = taskSchema.get("legacyIdentifier").setDefaultRequirement(true)
				.setSystemReserved(true).setUndeletable(true).setSearchable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder task_manualTokens = taskSchema.get("manualTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		task_manualTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder task_markedForPreviewConversion = taskSchema.get("markedForPreviewConversion").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder task_modifiedBy = taskSchema.get("modifiedBy").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder task_modifiedOn = taskSchema.get("modifiedOn").setSystemReserved(true).setUndeletable(true)
				.setSortable(true);
		MetadataBuilder task_parentpath = taskSchema.get("parentpath").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder task_path = taskSchema.get("path").setMultivalue(true).setSystemReserved(true).setUndeletable(true);
		MetadataBuilder task_pathParts = taskSchema.get("pathParts").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder task_principalpath = taskSchema.get("principalpath").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder task_removedauthorizations = taskSchema.get("removedauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder task_schema = taskSchema.get("schema").setDefaultRequirement(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder task_searchable = taskSchema.get("searchable").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder task_shareDenyTokens = taskSchema.get("shareDenyTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		task_shareDenyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder task_shareTokens = taskSchema.get("shareTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		task_shareTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder task_title = taskSchema.get("title").setUndeletable(true).setSchemaAutocomplete(true).setSearchable(true);
		MetadataBuilder task_tokens = taskSchema.get("tokens").setMultivalue(true).setSystemReserved(true).setUndeletable(true);
		MetadataBuilder task_visibleInTrees = taskSchema.get("visibleInTrees").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder task_workflowIdentifier = taskSchema.create("workflowIdentifier").setType(MetadataValueType.STRING)
				.setUndeletable(true);
		MetadataBuilder task_workflowRecordIdentifiers = taskSchema.create("workflowRecordIdentifiers")
				.setType(MetadataValueType.STRING).setMultivalue(true).setUndeletable(true);
		MetadataBuilder userDocument_allauthorizations = userDocumentSchema.get("allauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder userDocument_authorizations = userDocumentSchema.get("authorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder userDocument_content = userDocumentSchema.create("content").setType(MetadataValueType.CONTENT)
				.setUndeletable(true).setSearchable(true)
				.defineStructureFactory(com.constellio.model.services.contents.ContentFactory.class);
		MetadataBuilder userDocument_createdBy = userDocumentSchema.get("createdBy").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder userDocument_createdOn = userDocumentSchema.get("createdOn").setSystemReserved(true).setUndeletable(true)
				.setSortable(true);
		MetadataBuilder userDocument_deleted = userDocumentSchema.get("deleted").setSystemReserved(true).setUndeletable(true);
		MetadataBuilder userDocument_denyTokens = userDocumentSchema.get("denyTokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		userDocument_denyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder userDocument_detachedauthorizations = userDocumentSchema.get("detachedauthorizations")
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder userDocument_followers = userDocumentSchema.get("followers").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true).setSearchable(true);
		MetadataBuilder userDocument_id = userDocumentSchema.get("id").setDefaultRequirement(true).setSystemReserved(true)
				.setUndeletable(true).setSearchable(true).setSortable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder userDocument_inheritedauthorizations = userDocumentSchema.get("inheritedauthorizations")
				.setMultivalue(true).setSystemReserved(true).setUndeletable(true);
		MetadataBuilder userDocument_legacyIdentifier = userDocumentSchema.get("legacyIdentifier").setDefaultRequirement(true)
				.setSystemReserved(true).setUndeletable(true).setSearchable(true).setUniqueValue(true).setUnmodifiable(true);
		MetadataBuilder userDocument_manualTokens = userDocumentSchema.get("manualTokens").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		userDocument_manualTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder userDocument_markedForPreviewConversion = userDocumentSchema.get("markedForPreviewConversion")
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder userDocument_modifiedBy = userDocumentSchema.get("modifiedBy").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder userDocument_modifiedOn = userDocumentSchema.get("modifiedOn").setSystemReserved(true)
				.setUndeletable(true).setSortable(true);
		MetadataBuilder userDocument_parentpath = userDocumentSchema.get("parentpath").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder userDocument_path = userDocumentSchema.get("path").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder userDocument_pathParts = userDocumentSchema.get("pathParts").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder userDocument_principalpath = userDocumentSchema.get("principalpath").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder userDocument_removedauthorizations = userDocumentSchema.get("removedauthorizations").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		MetadataBuilder userDocument_schema = userDocumentSchema.get("schema").setDefaultRequirement(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder userDocument_searchable = userDocumentSchema.get("searchable").setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder userDocument_shareDenyTokens = userDocumentSchema.get("shareDenyTokens").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		userDocument_shareDenyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder userDocument_shareTokens = userDocumentSchema.get("shareTokens").setMultivalue(true)
				.setSystemReserved(true).setUndeletable(true);
		userDocument_shareTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder userDocument_title = userDocumentSchema.get("title").setUndeletable(true).setSchemaAutocomplete(true)
				.setSearchable(true);
		MetadataBuilder userDocument_tokens = userDocumentSchema.get("tokens").setMultivalue(true).setSystemReserved(true)
				.setUndeletable(true);
		MetadataBuilder userDocument_user = userDocumentSchema.create("user").setType(MetadataValueType.REFERENCE)
				.setUndeletable(true).defineReferencesTo(userSchemaType);
		MetadataBuilder userDocument_visibleInTrees = userDocumentSchema.get("visibleInTrees").setSystemReserved(true)
				.setUndeletable(true);
		collection_allauthorizations.defineDataEntry().asCalculated(AllAuthorizationsCalculator.class);
		collection_inheritedauthorizations.defineDataEntry().asCalculated(InheritedAuthorizationsCalculator.class);
		collection_parentpath.defineDataEntry().asCalculated(ParentPathCalculator.class);
		collection_path.defineDataEntry().asCalculated(PathCalculator.class);
		collection_pathParts.defineDataEntry().asCalculated(PathPartsCalculator.class);
		collection_principalpath.defineDataEntry().asCalculated(PrincipalPathCalculator.class);
		collection_tokens.defineDataEntry().asCalculated(TokensCalculator2.class);
		group_allauthorizations.defineDataEntry().asCalculated(AllAuthorizationsCalculator.class);
		group_inheritedauthorizations.defineDataEntry().asCopied(group_parent, group_allauthorizations);
		group_parentpath.defineDataEntry().asCalculated(ParentPathCalculator.class);
		group_path.defineDataEntry().asCalculated(PathCalculator.class);
		group_pathParts.defineDataEntry().asCalculated(PathPartsCalculator.class);
		group_principalpath.defineDataEntry().asCalculated(PrincipalPathCalculator.class);
		group_tokens.defineDataEntry().asCalculated(TokensCalculator2.class);
		user_allauthorizations.defineDataEntry().asCalculated(AllAuthorizationsCalculator.class);
		user_allroles.defineDataEntry().asCalculated(RolesCalculator.class);
		user_alluserauthorizations.defineDataEntry().asCalculated(AllUserAuthorizationsCalculator.class);
		user_groupsauthorizations.defineDataEntry().asCopied(user_groups, group_allauthorizations);
		user_inheritedauthorizations.defineDataEntry().asCalculated(InheritedAuthorizationsCalculator.class);
		user_parentpath.defineDataEntry().asCalculated(ParentPathCalculator.class);
		user_path.defineDataEntry().asCalculated(PathCalculator.class);
		user_pathParts.defineDataEntry().asCalculated(PathPartsCalculator.class);
		user_principalpath.defineDataEntry().asCalculated(PrincipalPathCalculator.class);
		user_title.defineDataEntry().asCalculated(UserTitleCalculator.class);
		user_tokens.defineDataEntry().asCalculated(TokensCalculator2.class);
		user_usertokens.defineDataEntry().asCalculated(UserTokensCalculator2.class);
		emailToSend_allauthorizations.defineDataEntry().asCalculated(AllAuthorizationsCalculator.class);
		emailToSend_inheritedauthorizations.defineDataEntry().asCalculated(InheritedAuthorizationsCalculator.class);
		emailToSend_parentpath.defineDataEntry().asCalculated(ParentPathCalculator.class);
		emailToSend_path.defineDataEntry().asCalculated(PathCalculator.class);
		emailToSend_pathParts.defineDataEntry().asCalculated(PathPartsCalculator.class);
		emailToSend_principalpath.defineDataEntry().asCalculated(PrincipalPathCalculator.class);
		emailToSend_tokens.defineDataEntry().asCalculated(TokensCalculator2.class);
		event_allauthorizations.defineDataEntry().asCalculated(AllAuthorizationsCalculator.class);
		event_inheritedauthorizations.defineDataEntry().asCalculated(InheritedAuthorizationsCalculator.class);
		event_parentpath.defineDataEntry().asCalculated(ParentPathCalculator.class);
		event_path.defineDataEntry().asCalculated(PathCalculator.class);
		event_pathParts.defineDataEntry().asCalculated(PathPartsCalculator.class);
		event_principalpath.defineDataEntry().asCalculated(PrincipalPathCalculator.class);
		event_tokens.defineDataEntry().asCalculated(TokensCalculator2.class);
		facet_allauthorizations.defineDataEntry().asCalculated(AllAuthorizationsCalculator.class);
		facet_inheritedauthorizations.defineDataEntry().asCalculated(InheritedAuthorizationsCalculator.class);
		facet_parentpath.defineDataEntry().asCalculated(ParentPathCalculator.class);
		facet_path.defineDataEntry().asCalculated(PathCalculator.class);
		facet_pathParts.defineDataEntry().asCalculated(PathPartsCalculator.class);
		facet_principalpath.defineDataEntry().asCalculated(PrincipalPathCalculator.class);
		facet_tokens.defineDataEntry().asCalculated(TokensCalculator2.class);
		report_allauthorizations.defineDataEntry().asCalculated(AllAuthorizationsCalculator.class);
		report_inheritedauthorizations.defineDataEntry().asCalculated(InheritedAuthorizationsCalculator.class);
		report_parentpath.defineDataEntry().asCalculated(ParentPathCalculator.class);
		report_path.defineDataEntry().asCalculated(PathCalculator.class);
		report_pathParts.defineDataEntry().asCalculated(PathPartsCalculator.class);
		report_principalpath.defineDataEntry().asCalculated(PrincipalPathCalculator.class);
		report_tokens.defineDataEntry().asCalculated(TokensCalculator2.class);
		savedSearch_allauthorizations.defineDataEntry().asCalculated(AllAuthorizationsCalculator.class);
		savedSearch_inheritedauthorizations.defineDataEntry().asCalculated(InheritedAuthorizationsCalculator.class);
		savedSearch_parentpath.defineDataEntry().asCalculated(ParentPathCalculator.class);
		savedSearch_path.defineDataEntry().asCalculated(PathCalculator.class);
		savedSearch_pathParts.defineDataEntry().asCalculated(PathPartsCalculator.class);
		savedSearch_principalpath.defineDataEntry().asCalculated(PrincipalPathCalculator.class);
		savedSearch_tokens.defineDataEntry().asCalculated(TokensCalculator2.class);
		task_allauthorizations.defineDataEntry().asCalculated(AllAuthorizationsCalculator.class);
		task_inheritedauthorizations.defineDataEntry().asCalculated(InheritedAuthorizationsCalculator.class);
		task_parentpath.defineDataEntry().asCalculated(ParentPathCalculator.class);
		task_path.defineDataEntry().asCalculated(PathCalculator.class);
		task_pathParts.defineDataEntry().asCalculated(PathPartsCalculator.class);
		task_principalpath.defineDataEntry().asCalculated(PrincipalPathCalculator.class);
		task_tokens.defineDataEntry().asCalculated(TokensCalculator2.class);
		userDocument_allauthorizations.defineDataEntry().asCalculated(AllAuthorizationsCalculator.class);
		userDocument_inheritedauthorizations.defineDataEntry().asCalculated(InheritedAuthorizationsCalculator.class);
		userDocument_parentpath.defineDataEntry().asCalculated(ParentPathCalculator.class);
		userDocument_path.defineDataEntry().asCalculated(PathCalculator.class);
		userDocument_pathParts.defineDataEntry().asCalculated(PathPartsCalculator.class);
		userDocument_principalpath.defineDataEntry().asCalculated(PrincipalPathCalculator.class);
		userDocument_tokens.defineDataEntry().asCalculated(TokensCalculator2.class);
	}

	public void applyGeneratedRoles() {
		RolesManager rolesManager = appLayerFactory.getModelLayerFactory().getRolesManager();
		;
		rolesManager.addRole(new Role(collection, "ADM", "Administrateur",
				asList("core.deleteContentVersion", "core.viewEvents", "core.manageFacets", "core.manageTaxonomies",
						"core.manageValueList", "core.manageMetadataSchemas", "core.manageSecurity",
						"core.manageMetadataExtractor", "core.manageConnectors", "core.manageSearchEngine", "core.manageTrash",
						"core.manageSearchReports", "core.manageEmailServer", "core.manageSystemConfiguration",
						"core.manageSystemGroups", "core.manageSystemUsers", "core.manageSystemCollections",
						"core.manageSystemModules", "core.manageSystemDataImports", "core.manageSystemServers",
						"core.manageSystemUpdates", "core.ldapConfigurationManagement")));
	}
}
