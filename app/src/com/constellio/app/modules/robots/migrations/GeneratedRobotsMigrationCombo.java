package com.constellio.app.modules.robots.migrations;

import static java.util.Arrays.asList;

import java.util.ArrayList;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.pages.search.criteria.CriterionFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.calculators.AllAuthorizationsCalculator;
import com.constellio.model.services.schemas.calculators.InheritedAuthorizationsCalculator;
import com.constellio.model.services.schemas.calculators.ParentPathCalculator;
import com.constellio.model.services.schemas.calculators.PathCalculator;
import com.constellio.model.services.schemas.calculators.PathPartsCalculator;
import com.constellio.model.services.schemas.calculators.PrincipalPathCalculator;
import com.constellio.model.services.schemas.calculators.TokensCalculator2;
import com.constellio.model.services.schemas.validators.ManualTokenValidator;
import com.constellio.model.services.security.roles.RolesManager;

public final class GeneratedRobotsMigrationCombo {
	String collection;

	AppLayerFactory appLayerFactory;

	MigrationResourcesProvider resourcesProvider;

	GeneratedRobotsMigrationCombo(String collection, AppLayerFactory appLayerFactory,
			MigrationResourcesProvider resourcesProvider) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.resourcesProvider = resourcesProvider;
	}

	public void applyGeneratedSchemaAlteration(MetadataSchemaTypesBuilder typesBuilder) {
		MetadataSchemaTypeBuilder collectionSchemaType = typesBuilder.getSchemaType("collection");
		MetadataSchemaBuilder collectionSchema = collectionSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder groupSchemaType = typesBuilder.getSchemaType("group");
		MetadataSchemaBuilder groupSchema = groupSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder userSchemaType = typesBuilder.getSchemaType("user");
		MetadataSchemaBuilder userSchema = userSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder emailToSendSchemaType = typesBuilder.getSchemaType("emailToSend");
		MetadataSchemaBuilder emailToSendSchema = emailToSendSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder eventSchemaType = typesBuilder.getSchemaType("event");
		MetadataSchemaBuilder eventSchema = eventSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder facetSchemaType = typesBuilder.getSchemaType("facet");
		MetadataSchemaBuilder facet_fieldSchema = facetSchemaType.getCustomSchema("field");
		MetadataSchemaBuilder facet_querySchema = facetSchemaType.getCustomSchema("query");
		MetadataSchemaBuilder facetSchema = facetSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder reportSchemaType = typesBuilder.getSchemaType("report");
		MetadataSchemaBuilder reportSchema = reportSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder savedSearchSchemaType = typesBuilder.getSchemaType("savedSearch");
		MetadataSchemaBuilder savedSearchSchema = savedSearchSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder taskSchemaType = typesBuilder.getSchemaType("task");
		MetadataSchemaBuilder task_approvalSchema = taskSchemaType.getCustomSchema("approval");
		MetadataSchemaBuilder taskSchema = taskSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder userDocumentSchemaType = typesBuilder.getSchemaType("userDocument");
		MetadataSchemaBuilder userDocumentSchema = userDocumentSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder actionParametersSchemaType = typesBuilder.createNewSchemaType("actionParameters");
		MetadataSchemaBuilder actionParametersSchema = actionParametersSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder robotSchemaType = typesBuilder.createNewSchemaType("robot");
		MetadataSchemaBuilder robotSchema = robotSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder robotLogSchemaType = typesBuilder.createNewSchemaType("robotLog");
		MetadataSchemaBuilder robotLogSchema = robotLogSchemaType.getDefaultSchema();
		MetadataBuilder actionParameters_allauthorizations = actionParametersSchema.get("allauthorizations");
		actionParameters_allauthorizations.setMultivalue(true);
		actionParameters_allauthorizations.setSystemReserved(true);
		actionParameters_allauthorizations.setUndeletable(true);
		MetadataBuilder actionParameters_authorizations = actionParametersSchema.get("authorizations");
		actionParameters_authorizations.setMultivalue(true);
		actionParameters_authorizations.setSystemReserved(true);
		actionParameters_authorizations.setUndeletable(true);
		MetadataBuilder actionParameters_createdBy = actionParametersSchema.get("createdBy");
		actionParameters_createdBy.setSystemReserved(true);
		actionParameters_createdBy.setUndeletable(true);
		MetadataBuilder actionParameters_createdOn = actionParametersSchema.get("createdOn");
		actionParameters_createdOn.setSystemReserved(true);
		actionParameters_createdOn.setUndeletable(true);
		actionParameters_createdOn.setSortable(true);
		MetadataBuilder actionParameters_deleted = actionParametersSchema.get("deleted");
		actionParameters_deleted.setSystemReserved(true);
		actionParameters_deleted.setUndeletable(true);
		MetadataBuilder actionParameters_denyTokens = actionParametersSchema.get("denyTokens");
		actionParameters_denyTokens.setMultivalue(true);
		actionParameters_denyTokens.setSystemReserved(true);
		actionParameters_denyTokens.setUndeletable(true);
		actionParameters_denyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder actionParameters_detachedauthorizations = actionParametersSchema.get("detachedauthorizations");
		actionParameters_detachedauthorizations.setSystemReserved(true);
		actionParameters_detachedauthorizations.setUndeletable(true);
		MetadataBuilder actionParameters_errorOnPhysicalDeletion = actionParametersSchema.get("errorOnPhysicalDeletion");
		actionParameters_errorOnPhysicalDeletion.setSystemReserved(true);
		actionParameters_errorOnPhysicalDeletion.setUndeletable(true);
		MetadataBuilder actionParameters_followers = actionParametersSchema.get("followers");
		actionParameters_followers.setMultivalue(true);
		actionParameters_followers.setSystemReserved(true);
		actionParameters_followers.setUndeletable(true);
		actionParameters_followers.setSearchable(true);
		MetadataBuilder actionParameters_id = actionParametersSchema.get("id");
		actionParameters_id.setDefaultRequirement(true);
		actionParameters_id.setSystemReserved(true);
		actionParameters_id.setUndeletable(true);
		actionParameters_id.setSearchable(true);
		actionParameters_id.setSortable(true);
		actionParameters_id.setUniqueValue(true);
		actionParameters_id.setUnmodifiable(true);
		MetadataBuilder actionParameters_inheritedauthorizations = actionParametersSchema.get("inheritedauthorizations");
		actionParameters_inheritedauthorizations.setMultivalue(true);
		actionParameters_inheritedauthorizations.setSystemReserved(true);
		actionParameters_inheritedauthorizations.setUndeletable(true);
		MetadataBuilder actionParameters_legacyIdentifier = actionParametersSchema.get("legacyIdentifier");
		actionParameters_legacyIdentifier.setDefaultRequirement(true);
		actionParameters_legacyIdentifier.setSystemReserved(true);
		actionParameters_legacyIdentifier.setUndeletable(true);
		actionParameters_legacyIdentifier.setSearchable(true);
		actionParameters_legacyIdentifier.setUniqueValue(true);
		actionParameters_legacyIdentifier.setUnmodifiable(true);
		MetadataBuilder actionParameters_logicallyDeletedOn = actionParametersSchema.get("logicallyDeletedOn");
		actionParameters_logicallyDeletedOn.setSystemReserved(true);
		actionParameters_logicallyDeletedOn.setUndeletable(true);
		MetadataBuilder actionParameters_manualTokens = actionParametersSchema.get("manualTokens");
		actionParameters_manualTokens.setMultivalue(true);
		actionParameters_manualTokens.setSystemReserved(true);
		actionParameters_manualTokens.setUndeletable(true);
		actionParameters_manualTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder actionParameters_markedForPreviewConversion = actionParametersSchema.get("markedForPreviewConversion");
		actionParameters_markedForPreviewConversion.setSystemReserved(true);
		actionParameters_markedForPreviewConversion.setUndeletable(true);
		MetadataBuilder actionParameters_modifiedBy = actionParametersSchema.get("modifiedBy");
		actionParameters_modifiedBy.setSystemReserved(true);
		actionParameters_modifiedBy.setUndeletable(true);
		MetadataBuilder actionParameters_modifiedOn = actionParametersSchema.get("modifiedOn");
		actionParameters_modifiedOn.setSystemReserved(true);
		actionParameters_modifiedOn.setUndeletable(true);
		actionParameters_modifiedOn.setSortable(true);
		MetadataBuilder actionParameters_parentpath = actionParametersSchema.get("parentpath");
		actionParameters_parentpath.setMultivalue(true);
		actionParameters_parentpath.setSystemReserved(true);
		actionParameters_parentpath.setUndeletable(true);
		MetadataBuilder actionParameters_path = actionParametersSchema.get("path");
		actionParameters_path.setMultivalue(true);
		actionParameters_path.setSystemReserved(true);
		actionParameters_path.setUndeletable(true);
		MetadataBuilder actionParameters_pathParts = actionParametersSchema.get("pathParts");
		actionParameters_pathParts.setMultivalue(true);
		actionParameters_pathParts.setSystemReserved(true);
		actionParameters_pathParts.setUndeletable(true);
		MetadataBuilder actionParameters_principalpath = actionParametersSchema.get("principalpath");
		actionParameters_principalpath.setSystemReserved(true);
		actionParameters_principalpath.setUndeletable(true);
		MetadataBuilder actionParameters_removedauthorizations = actionParametersSchema.get("removedauthorizations");
		actionParameters_removedauthorizations.setMultivalue(true);
		actionParameters_removedauthorizations.setSystemReserved(true);
		actionParameters_removedauthorizations.setUndeletable(true);
		MetadataBuilder actionParameters_schema = actionParametersSchema.get("schema");
		actionParameters_schema.setDefaultRequirement(true);
		actionParameters_schema.setSystemReserved(true);
		actionParameters_schema.setUndeletable(true);
		MetadataBuilder actionParameters_searchable = actionParametersSchema.get("searchable");
		actionParameters_searchable.setSystemReserved(true);
		actionParameters_searchable.setUndeletable(true);
		MetadataBuilder actionParameters_shareDenyTokens = actionParametersSchema.get("shareDenyTokens");
		actionParameters_shareDenyTokens.setMultivalue(true);
		actionParameters_shareDenyTokens.setSystemReserved(true);
		actionParameters_shareDenyTokens.setUndeletable(true);
		actionParameters_shareDenyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder actionParameters_shareTokens = actionParametersSchema.get("shareTokens");
		actionParameters_shareTokens.setMultivalue(true);
		actionParameters_shareTokens.setSystemReserved(true);
		actionParameters_shareTokens.setUndeletable(true);
		actionParameters_shareTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder actionParameters_title = actionParametersSchema.get("title");
		actionParameters_title.setUndeletable(true);
		actionParameters_title.setEnabled(false);
		actionParameters_title.setSchemaAutocomplete(true);
		actionParameters_title.setSearchable(true);
		MetadataBuilder actionParameters_tokens = actionParametersSchema.get("tokens");
		actionParameters_tokens.setMultivalue(true);
		actionParameters_tokens.setSystemReserved(true);
		actionParameters_tokens.setUndeletable(true);
		MetadataBuilder actionParameters_visibleInTrees = actionParametersSchema.get("visibleInTrees");
		actionParameters_visibleInTrees.setSystemReserved(true);
		actionParameters_visibleInTrees.setUndeletable(true);
		MetadataBuilder robot_action = robotSchema.create("action").setType(MetadataValueType.STRING);
		robot_action.setUndeletable(true);
		robot_action.setEssential(true);
		MetadataBuilder robot_actionParameters = robotSchema.create("actionParameters").setType(MetadataValueType.REFERENCE);
		robot_actionParameters.setUndeletable(true);
		robot_actionParameters.setEssential(true);
		robot_actionParameters.defineReferencesTo(actionParametersSchemaType);
		MetadataBuilder robot_allauthorizations = robotSchema.get("allauthorizations");
		robot_allauthorizations.setMultivalue(true);
		robot_allauthorizations.setSystemReserved(true);
		robot_allauthorizations.setUndeletable(true);
		MetadataBuilder robot_authorizations = robotSchema.get("authorizations");
		robot_authorizations.setMultivalue(true);
		robot_authorizations.setSystemReserved(true);
		robot_authorizations.setUndeletable(true);
		MetadataBuilder robot_autoExecute = robotSchema.create("autoExecute").setType(MetadataValueType.BOOLEAN);
		robot_autoExecute.setDefaultRequirement(true);
		robot_autoExecute.setUndeletable(true);
		robot_autoExecute.setDefaultValue(false);
		MetadataBuilder robot_code = robotSchema.create("code").setType(MetadataValueType.STRING);
		robot_code.setDefaultRequirement(true);
		robot_code.setUndeletable(true);
		robot_code.setEssential(true);
		robot_code.setUniqueValue(true);
		MetadataBuilder robot_createdBy = robotSchema.get("createdBy");
		robot_createdBy.setSystemReserved(true);
		robot_createdBy.setUndeletable(true);
		MetadataBuilder robot_createdOn = robotSchema.get("createdOn");
		robot_createdOn.setSystemReserved(true);
		robot_createdOn.setUndeletable(true);
		robot_createdOn.setSortable(true);
		MetadataBuilder robot_deleted = robotSchema.get("deleted");
		robot_deleted.setSystemReserved(true);
		robot_deleted.setUndeletable(true);
		MetadataBuilder robot_denyTokens = robotSchema.get("denyTokens");
		robot_denyTokens.setMultivalue(true);
		robot_denyTokens.setSystemReserved(true);
		robot_denyTokens.setUndeletable(true);
		robot_denyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder robot_description = robotSchema.create("description").setType(MetadataValueType.TEXT);
		MetadataBuilder robot_detachedauthorizations = robotSchema.get("detachedauthorizations");
		robot_detachedauthorizations.setSystemReserved(true);
		robot_detachedauthorizations.setUndeletable(true);
		MetadataBuilder robot_errorOnPhysicalDeletion = robotSchema.get("errorOnPhysicalDeletion");
		robot_errorOnPhysicalDeletion.setSystemReserved(true);
		robot_errorOnPhysicalDeletion.setUndeletable(true);
		MetadataBuilder robot_excludeProcessedByChildren = robotSchema.create("excludeProcessedByChildren")
				.setType(MetadataValueType.BOOLEAN);
		robot_excludeProcessedByChildren.setDefaultRequirement(true);
		robot_excludeProcessedByChildren.setUndeletable(true);
		robot_excludeProcessedByChildren.setEssential(true);
		robot_excludeProcessedByChildren.setDefaultValue(false);
		MetadataBuilder robot_followers = robotSchema.get("followers");
		robot_followers.setMultivalue(true);
		robot_followers.setSystemReserved(true);
		robot_followers.setUndeletable(true);
		robot_followers.setSearchable(true);
		MetadataBuilder robot_id = robotSchema.get("id");
		robot_id.setDefaultRequirement(true);
		robot_id.setSystemReserved(true);
		robot_id.setUndeletable(true);
		robot_id.setSearchable(true);
		robot_id.setSortable(true);
		robot_id.setUniqueValue(true);
		robot_id.setUnmodifiable(true);
		MetadataBuilder robot_inheritedauthorizations = robotSchema.get("inheritedauthorizations");
		robot_inheritedauthorizations.setMultivalue(true);
		robot_inheritedauthorizations.setSystemReserved(true);
		robot_inheritedauthorizations.setUndeletable(true);
		MetadataBuilder robot_legacyIdentifier = robotSchema.get("legacyIdentifier");
		robot_legacyIdentifier.setDefaultRequirement(true);
		robot_legacyIdentifier.setSystemReserved(true);
		robot_legacyIdentifier.setUndeletable(true);
		robot_legacyIdentifier.setSearchable(true);
		robot_legacyIdentifier.setUniqueValue(true);
		robot_legacyIdentifier.setUnmodifiable(true);
		MetadataBuilder robot_logicallyDeletedOn = robotSchema.get("logicallyDeletedOn");
		robot_logicallyDeletedOn.setSystemReserved(true);
		robot_logicallyDeletedOn.setUndeletable(true);
		MetadataBuilder robot_manualTokens = robotSchema.get("manualTokens");
		robot_manualTokens.setMultivalue(true);
		robot_manualTokens.setSystemReserved(true);
		robot_manualTokens.setUndeletable(true);
		robot_manualTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder robot_markedForPreviewConversion = robotSchema.get("markedForPreviewConversion");
		robot_markedForPreviewConversion.setSystemReserved(true);
		robot_markedForPreviewConversion.setUndeletable(true);
		MetadataBuilder robot_modifiedBy = robotSchema.get("modifiedBy");
		robot_modifiedBy.setSystemReserved(true);
		robot_modifiedBy.setUndeletable(true);
		MetadataBuilder robot_modifiedOn = robotSchema.get("modifiedOn");
		robot_modifiedOn.setSystemReserved(true);
		robot_modifiedOn.setUndeletable(true);
		robot_modifiedOn.setSortable(true);
		MetadataBuilder robot_parent = robotSchema.create("parent").setType(MetadataValueType.REFERENCE);
		robot_parent.setUndeletable(true);
		robot_parent.setEssential(true);
		robot_parent.defineChildOfRelationshipToType(robotSchemaType);
		MetadataBuilder robot_parentpath = robotSchema.get("parentpath");
		robot_parentpath.setMultivalue(true);
		robot_parentpath.setSystemReserved(true);
		robot_parentpath.setUndeletable(true);
		MetadataBuilder robot_path = robotSchema.get("path");
		robot_path.setMultivalue(true);
		robot_path.setSystemReserved(true);
		robot_path.setUndeletable(true);
		MetadataBuilder robot_pathParts = robotSchema.get("pathParts");
		robot_pathParts.setMultivalue(true);
		robot_pathParts.setSystemReserved(true);
		robot_pathParts.setUndeletable(true);
		MetadataBuilder robot_principalpath = robotSchema.get("principalpath");
		robot_principalpath.setSystemReserved(true);
		robot_principalpath.setUndeletable(true);
		MetadataBuilder robot_removedauthorizations = robotSchema.get("removedauthorizations");
		robot_removedauthorizations.setMultivalue(true);
		robot_removedauthorizations.setSystemReserved(true);
		robot_removedauthorizations.setUndeletable(true);
		MetadataBuilder robot_schema = robotSchema.get("schema");
		robot_schema.setDefaultRequirement(true);
		robot_schema.setSystemReserved(true);
		robot_schema.setUndeletable(true);
		MetadataBuilder robot_schemaFilter = robotSchema.create("schemaFilter").setType(MetadataValueType.STRING);
		robot_schemaFilter.setDefaultRequirement(true);
		robot_schemaFilter.setUndeletable(true);
		robot_schemaFilter.setEssential(true);
		MetadataBuilder robot_searchCriteria = robotSchema.create("searchCriteria").setType(MetadataValueType.STRUCTURE);
		robot_searchCriteria.setMultivalue(true);
		robot_searchCriteria.setDefaultRequirement(true);
		robot_searchCriteria.setUndeletable(true);
		robot_searchCriteria.setEssential(true);
		robot_searchCriteria.defineStructureFactory(CriterionFactory.class);
		MetadataBuilder robot_searchable = robotSchema.get("searchable");
		robot_searchable.setSystemReserved(true);
		robot_searchable.setUndeletable(true);
		MetadataBuilder robot_shareDenyTokens = robotSchema.get("shareDenyTokens");
		robot_shareDenyTokens.setMultivalue(true);
		robot_shareDenyTokens.setSystemReserved(true);
		robot_shareDenyTokens.setUndeletable(true);
		robot_shareDenyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder robot_shareTokens = robotSchema.get("shareTokens");
		robot_shareTokens.setMultivalue(true);
		robot_shareTokens.setSystemReserved(true);
		robot_shareTokens.setUndeletable(true);
		robot_shareTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder robot_title = robotSchema.get("title");
		robot_title.setDefaultRequirement(true);
		robot_title.setUndeletable(true);
		robot_title.setSchemaAutocomplete(true);
		robot_title.setSearchable(true);
		MetadataBuilder robot_tokens = robotSchema.get("tokens");
		robot_tokens.setMultivalue(true);
		robot_tokens.setSystemReserved(true);
		robot_tokens.setUndeletable(true);
		MetadataBuilder robot_visibleInTrees = robotSchema.get("visibleInTrees");
		robot_visibleInTrees.setSystemReserved(true);
		robot_visibleInTrees.setUndeletable(true);
		MetadataBuilder robotLog_allauthorizations = robotLogSchema.get("allauthorizations");
		robotLog_allauthorizations.setMultivalue(true);
		robotLog_allauthorizations.setSystemReserved(true);
		robotLog_allauthorizations.setUndeletable(true);
		MetadataBuilder robotLog_authorizations = robotLogSchema.get("authorizations");
		robotLog_authorizations.setMultivalue(true);
		robotLog_authorizations.setSystemReserved(true);
		robotLog_authorizations.setUndeletable(true);
		MetadataBuilder robotLog_count = robotLogSchema.create("count").setType(MetadataValueType.NUMBER);
		robotLog_count.setUndeletable(true);
		MetadataBuilder robotLog_createdBy = robotLogSchema.get("createdBy");
		robotLog_createdBy.setSystemReserved(true);
		robotLog_createdBy.setUndeletable(true);
		MetadataBuilder robotLog_createdOn = robotLogSchema.get("createdOn");
		robotLog_createdOn.setSystemReserved(true);
		robotLog_createdOn.setUndeletable(true);
		robotLog_createdOn.setSortable(true);
		MetadataBuilder robotLog_deleted = robotLogSchema.get("deleted");
		robotLog_deleted.setSystemReserved(true);
		robotLog_deleted.setUndeletable(true);
		MetadataBuilder robotLog_denyTokens = robotLogSchema.get("denyTokens");
		robotLog_denyTokens.setMultivalue(true);
		robotLog_denyTokens.setSystemReserved(true);
		robotLog_denyTokens.setUndeletable(true);
		robotLog_denyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder robotLog_detachedauthorizations = robotLogSchema.get("detachedauthorizations");
		robotLog_detachedauthorizations.setSystemReserved(true);
		robotLog_detachedauthorizations.setUndeletable(true);
		MetadataBuilder robotLog_errorOnPhysicalDeletion = robotLogSchema.get("errorOnPhysicalDeletion");
		robotLog_errorOnPhysicalDeletion.setSystemReserved(true);
		robotLog_errorOnPhysicalDeletion.setUndeletable(true);
		MetadataBuilder robotLog_followers = robotLogSchema.get("followers");
		robotLog_followers.setMultivalue(true);
		robotLog_followers.setSystemReserved(true);
		robotLog_followers.setUndeletable(true);
		robotLog_followers.setSearchable(true);
		MetadataBuilder robotLog_id = robotLogSchema.get("id");
		robotLog_id.setDefaultRequirement(true);
		robotLog_id.setSystemReserved(true);
		robotLog_id.setUndeletable(true);
		robotLog_id.setSearchable(true);
		robotLog_id.setSortable(true);
		robotLog_id.setUniqueValue(true);
		robotLog_id.setUnmodifiable(true);
		MetadataBuilder robotLog_inheritedauthorizations = robotLogSchema.get("inheritedauthorizations");
		robotLog_inheritedauthorizations.setMultivalue(true);
		robotLog_inheritedauthorizations.setSystemReserved(true);
		robotLog_inheritedauthorizations.setUndeletable(true);
		MetadataBuilder robotLog_legacyIdentifier = robotLogSchema.get("legacyIdentifier");
		robotLog_legacyIdentifier.setDefaultRequirement(true);
		robotLog_legacyIdentifier.setSystemReserved(true);
		robotLog_legacyIdentifier.setUndeletable(true);
		robotLog_legacyIdentifier.setSearchable(true);
		robotLog_legacyIdentifier.setUniqueValue(true);
		robotLog_legacyIdentifier.setUnmodifiable(true);
		MetadataBuilder robotLog_logicallyDeletedOn = robotLogSchema.get("logicallyDeletedOn");
		robotLog_logicallyDeletedOn.setSystemReserved(true);
		robotLog_logicallyDeletedOn.setUndeletable(true);
		MetadataBuilder robotLog_manualTokens = robotLogSchema.get("manualTokens");
		robotLog_manualTokens.setMultivalue(true);
		robotLog_manualTokens.setSystemReserved(true);
		robotLog_manualTokens.setUndeletable(true);
		robotLog_manualTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder robotLog_markedForPreviewConversion = robotLogSchema.get("markedForPreviewConversion");
		robotLog_markedForPreviewConversion.setSystemReserved(true);
		robotLog_markedForPreviewConversion.setUndeletable(true);
		MetadataBuilder robotLog_modifiedBy = robotLogSchema.get("modifiedBy");
		robotLog_modifiedBy.setSystemReserved(true);
		robotLog_modifiedBy.setUndeletable(true);
		MetadataBuilder robotLog_modifiedOn = robotLogSchema.get("modifiedOn");
		robotLog_modifiedOn.setSystemReserved(true);
		robotLog_modifiedOn.setUndeletable(true);
		robotLog_modifiedOn.setSortable(true);
		MetadataBuilder robotLog_parentpath = robotLogSchema.get("parentpath");
		robotLog_parentpath.setMultivalue(true);
		robotLog_parentpath.setSystemReserved(true);
		robotLog_parentpath.setUndeletable(true);
		MetadataBuilder robotLog_path = robotLogSchema.get("path");
		robotLog_path.setMultivalue(true);
		robotLog_path.setSystemReserved(true);
		robotLog_path.setUndeletable(true);
		MetadataBuilder robotLog_pathParts = robotLogSchema.get("pathParts");
		robotLog_pathParts.setMultivalue(true);
		robotLog_pathParts.setSystemReserved(true);
		robotLog_pathParts.setUndeletable(true);
		MetadataBuilder robotLog_principalpath = robotLogSchema.get("principalpath");
		robotLog_principalpath.setSystemReserved(true);
		robotLog_principalpath.setUndeletable(true);
		MetadataBuilder robotLog_removedauthorizations = robotLogSchema.get("removedauthorizations");
		robotLog_removedauthorizations.setMultivalue(true);
		robotLog_removedauthorizations.setSystemReserved(true);
		robotLog_removedauthorizations.setUndeletable(true);
		MetadataBuilder robotLog_robot = robotLogSchema.create("robot").setType(MetadataValueType.REFERENCE);
		robotLog_robot.setDefaultRequirement(true);
		robotLog_robot.setUndeletable(true);
		robotLog_robot.setEssential(true);
		robotLog_robot.defineReferencesTo(robotSchemaType);
		MetadataBuilder robotLog_schema = robotLogSchema.get("schema");
		robotLog_schema.setDefaultRequirement(true);
		robotLog_schema.setSystemReserved(true);
		robotLog_schema.setUndeletable(true);
		MetadataBuilder robotLog_searchable = robotLogSchema.get("searchable");
		robotLog_searchable.setSystemReserved(true);
		robotLog_searchable.setUndeletable(true);
		MetadataBuilder robotLog_shareDenyTokens = robotLogSchema.get("shareDenyTokens");
		robotLog_shareDenyTokens.setMultivalue(true);
		robotLog_shareDenyTokens.setSystemReserved(true);
		robotLog_shareDenyTokens.setUndeletable(true);
		robotLog_shareDenyTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder robotLog_shareTokens = robotLogSchema.get("shareTokens");
		robotLog_shareTokens.setMultivalue(true);
		robotLog_shareTokens.setSystemReserved(true);
		robotLog_shareTokens.setUndeletable(true);
		robotLog_shareTokens.defineValidators().add(ManualTokenValidator.class);
		MetadataBuilder robotLog_title = robotLogSchema.get("title");
		robotLog_title.setUndeletable(true);
		robotLog_title.setSchemaAutocomplete(true);
		robotLog_title.setSearchable(true);
		MetadataBuilder robotLog_tokens = robotLogSchema.get("tokens");
		robotLog_tokens.setMultivalue(true);
		robotLog_tokens.setSystemReserved(true);
		robotLog_tokens.setUndeletable(true);
		MetadataBuilder robotLog_visibleInTrees = robotLogSchema.get("visibleInTrees");
		robotLog_visibleInTrees.setSystemReserved(true);
		robotLog_visibleInTrees.setUndeletable(true);
		actionParameters_allauthorizations.defineDataEntry().asCalculated(AllAuthorizationsCalculator.class);
		actionParameters_inheritedauthorizations.defineDataEntry().asCalculated(InheritedAuthorizationsCalculator.class);
		actionParameters_parentpath.defineDataEntry().asCalculated(ParentPathCalculator.class);
		actionParameters_path.defineDataEntry().asCalculated(PathCalculator.class);
		actionParameters_pathParts.defineDataEntry().asCalculated(PathPartsCalculator.class);
		actionParameters_principalpath.defineDataEntry().asCalculated(PrincipalPathCalculator.class);
		actionParameters_tokens.defineDataEntry().asCalculated(TokensCalculator2.class);
		robot_allauthorizations.defineDataEntry().asCalculated(AllAuthorizationsCalculator.class);
		robot_inheritedauthorizations.defineDataEntry().asCalculated(InheritedAuthorizationsCalculator.class);
		robot_parentpath.defineDataEntry().asCalculated(ParentPathCalculator.class);
		robot_path.defineDataEntry().asCalculated(PathCalculator.class);
		robot_pathParts.defineDataEntry().asCalculated(PathPartsCalculator.class);
		robot_principalpath.defineDataEntry().asCalculated(PrincipalPathCalculator.class);
		robot_tokens.defineDataEntry().asCalculated(TokensCalculator2.class);
		robotLog_allauthorizations.defineDataEntry().asCalculated(AllAuthorizationsCalculator.class);
		robotLog_inheritedauthorizations.defineDataEntry().asCalculated(InheritedAuthorizationsCalculator.class);
		robotLog_parentpath.defineDataEntry().asCalculated(ParentPathCalculator.class);
		robotLog_path.defineDataEntry().asCalculated(PathCalculator.class);
		robotLog_pathParts.defineDataEntry().asCalculated(PathPartsCalculator.class);
		robotLog_principalpath.defineDataEntry().asCalculated(PrincipalPathCalculator.class);
		robotLog_tokens.defineDataEntry().asCalculated(TokensCalculator2.class);
	}

	public void applySchemasDisplay(SchemasDisplayManager manager) {
		SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);
		SchemaTypesDisplayConfig typesConfig = manager.getTypes(collection);
		transaction.add(manager.getSchema(collection, "actionParameters_default").withFormMetadataCodes(new ArrayList<String>())
				.withDisplayMetadataCodes(asList("actionParameters_default_title"))
				.withSearchResultsMetadataCodes(asList("actionParameters_default_title", "actionParameters_default_modifiedOn"))
				.withTableMetadataCodes(asList("actionParameters_default_title", "actionParameters_default_modifiedOn")));
		transaction.add(manager.getType(collection, "robot").withSimpleSearchStatus(false).withAdvancedSearchStatus(false)
				.withManageableStatus(false).withMetadataGroup(resourcesProvider.getLanguageMap(
						asList("init.robot.tabs.criteria", "default:init.robot.tabs.definition", "init.robot.tabs.action"))));
		transaction.add(manager.getSchema(collection, "robot_default").withFormMetadataCodes(
				asList("robot_default_code", "robot_default_title", "robot_default_parent", "robot_default_schemaFilter",
						"robot_default_description", "robot_default_searchCriteria", "robot_default_action",
						"robot_default_actionParameters", "robot_default_excludeProcessedByChildren",
						"robot_default_autoExecute")).withDisplayMetadataCodes(
				asList("robot_default_code", "robot_default_title", "robot_default_createdBy", "robot_default_createdOn",
						"robot_default_modifiedBy", "robot_default_modifiedOn", "robot_default_action",
						"robot_default_actionParameters", "robot_default_excludeProcessedByChildren", "robot_default_parent",
						"robot_default_schemaFilter", "robot_default_description"))
				.withSearchResultsMetadataCodes(asList("robot_default_title", "robot_default_modifiedOn"))
				.withTableMetadataCodes(asList("robot_default_title", "robot_default_modifiedOn")));
		transaction.add(manager.getMetadata(collection, "robot_default_action").withMetadataGroup("init.robot.tabs.action")
				.withInputType(MetadataInputType.FIELD).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
		transaction
				.add(manager.getMetadata(collection, "robot_default_actionParameters").withMetadataGroup("init.robot.tabs.action")
						.withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false)
						.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getMetadata(collection, "robot_default_excludeProcessedByChildren")
				.withMetadataGroup("init.robot.tabs.action").withInputType(MetadataInputType.FIELD).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getMetadata(collection, "robot_default_parent").withMetadataGroup("")
				.withInputType(MetadataInputType.HIDDEN).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
		transaction
				.add(manager.getMetadata(collection, "robot_default_schemaFilter").withMetadataGroup("init.robot.tabs.criteria")
						.withInputType(MetadataInputType.FIELD).withHighlightStatus(false)
						.withVisibleInAdvancedSearchStatus(false));
		transaction
				.add(manager.getMetadata(collection, "robot_default_searchCriteria").withMetadataGroup("init.robot.tabs.criteria")
						.withInputType(MetadataInputType.FIELD).withHighlightStatus(false)
						.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getSchema(collection, "robotLog_default")
				.withFormMetadataCodes(asList("robotLog_default_title", "robotLog_default_count", "robotLog_default_robot"))
				.withDisplayMetadataCodes(
						asList("robotLog_default_title", "robotLog_default_createdBy", "robotLog_default_createdOn",
								"robotLog_default_modifiedBy", "robotLog_default_modifiedOn", "robotLog_default_count",
								"robotLog_default_robot"))
				.withSearchResultsMetadataCodes(asList("robotLog_default_title", "robotLog_default_modifiedOn"))
				.withTableMetadataCodes(
						asList("robotLog_default_title", "robotLog_default_modifiedOn", "robotLog_default_count")));
		manager.execute(transaction.build());
	}

	public void applyGeneratedRoles() {
		RolesManager rolesManager = appLayerFactory.getModelLayerFactory().getRolesManager();
		;
		rolesManager.updateRole(rolesManager.getRole(collection, "ADM").withNewPermissions(
				asList("core.deleteContentVersion", "core.ldapConfigurationManagement", "core.manageConnectors",
						"core.manageEmailServer", "core.manageFacets", "core.manageMetadataExtractor",
						"core.manageMetadataSchemas", "core.manageSearchEngine", "core.manageSearchReports",
						"core.manageSecurity", "core.manageSystemCollections", "core.manageSystemConfiguration",
						"core.manageSystemDataImports", "core.manageSystemGroups", "core.manageSystemModules",
						"core.manageSystemServers", "core.manageSystemUpdates", "core.manageSystemUsers", "core.manageTaxonomies",
						"core.manageTrash", "core.manageValueList", "core.useExternalAPIS", "core.viewEvents",
						"robots.manageRobots")));
	}
}
