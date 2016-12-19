package com.constellio.app.modules.complementary.esRmRobots.migrations;

import static java.util.Arrays.asList;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.complementary.esRmRobots.model.enums.ActionAfterClassification;
import com.constellio.app.modules.complementary.esRmRobots.validators.ClassifyConnectorTaxonomyActionParametersValidator;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.contents.ContentFactory;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;

public final class GeneratedESRMRobotsMigrationCombo {
	String collection;

	AppLayerFactory appLayerFactory;

	MigrationResourcesProvider resourcesProvider;

	GeneratedESRMRobotsMigrationCombo(String collection, AppLayerFactory appLayerFactory,
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
		MetadataSchemaTypeBuilder actionParametersSchemaType = typesBuilder.getSchemaType("actionParameters");
		MetadataSchemaBuilder actionParameters_classifyConnectorFolderDirectlyInThePlanSchema = actionParametersSchemaType
				.createCustomSchema("classifyConnectorFolderDirectlyInThePlan");
		MetadataSchemaBuilder actionParameters_classifyConnectorFolderInParentFolderSchema = actionParametersSchemaType
				.createCustomSchema("classifyConnectorFolderInParentFolder");
		MetadataSchemaBuilder actionParameters_classifyConnectorTaxonomySchema = actionParametersSchemaType
				.createCustomSchema("classifyConnectorTaxonomy");
		actionParameters_classifyConnectorTaxonomySchema.defineValidators()
				.add(ClassifyConnectorTaxonomyActionParametersValidator.class);
		MetadataSchemaBuilder actionParameters_classifySmbDocumentInFolderSchema = actionParametersSchemaType
				.createCustomSchema("classifySmbDocumentInFolder");
		MetadataSchemaBuilder actionParameters_classifySmbFolderInFolderSchema = actionParametersSchemaType
				.createCustomSchema("classifySmbFolderInFolder");
		MetadataSchemaBuilder actionParametersSchema = actionParametersSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder administrativeUnitSchemaType = typesBuilder.getSchemaType("administrativeUnit");
		MetadataSchemaBuilder administrativeUnitSchema = administrativeUnitSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder cartSchemaType = typesBuilder.getSchemaType("cart");
		MetadataSchemaBuilder cartSchema = cartSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder categorySchemaType = typesBuilder.getSchemaType("category");
		MetadataSchemaBuilder categorySchema = categorySchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder connectorHttpDocumentSchemaType = typesBuilder.getSchemaType("connectorHttpDocument");
		MetadataSchemaBuilder connectorHttpDocumentSchema = connectorHttpDocumentSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder connectorInstanceSchemaType = typesBuilder.getSchemaType("connectorInstance");
		MetadataSchemaBuilder connectorInstance_httpSchema = connectorInstanceSchemaType.getCustomSchema("http");
		MetadataSchemaBuilder connectorInstance_ldapSchema = connectorInstanceSchemaType.getCustomSchema("ldap");
		MetadataSchemaBuilder connectorInstance_smbSchema = connectorInstanceSchemaType.getCustomSchema("smb");
		MetadataSchemaBuilder connectorInstanceSchema = connectorInstanceSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder connectorLdapUserDocumentSchemaType = typesBuilder.getSchemaType("connectorLdapUserDocument");
		MetadataSchemaBuilder connectorLdapUserDocumentSchema = connectorLdapUserDocumentSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder connectorSmbDocumentSchemaType = typesBuilder.getSchemaType("connectorSmbDocument");
		MetadataSchemaBuilder connectorSmbDocumentSchema = connectorSmbDocumentSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder connectorSmbFolderSchemaType = typesBuilder.getSchemaType("connectorSmbFolder");
		MetadataSchemaBuilder connectorSmbFolderSchema = connectorSmbFolderSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder connectorTypeSchemaType = typesBuilder.getSchemaType("connectorType");
		MetadataSchemaBuilder connectorTypeSchema = connectorTypeSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder containerRecordSchemaType = typesBuilder.getSchemaType("containerRecord");
		MetadataSchemaBuilder containerRecordSchema = containerRecordSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder ddvContainerRecordTypeSchemaType = typesBuilder.getSchemaType("ddvContainerRecordType");
		MetadataSchemaBuilder ddvContainerRecordTypeSchema = ddvContainerRecordTypeSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder ddvDocumentTypeSchemaType = typesBuilder.getSchemaType("ddvDocumentType");
		MetadataSchemaBuilder ddvDocumentTypeSchema = ddvDocumentTypeSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder ddvFolderTypeSchemaType = typesBuilder.getSchemaType("ddvFolderType");
		MetadataSchemaBuilder ddvFolderTypeSchema = ddvFolderTypeSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder ddvMediumTypeSchemaType = typesBuilder.getSchemaType("ddvMediumType");
		MetadataSchemaBuilder ddvMediumTypeSchema = ddvMediumTypeSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder ddvStorageSpaceTypeSchemaType = typesBuilder.getSchemaType("ddvStorageSpaceType");
		MetadataSchemaBuilder ddvStorageSpaceTypeSchema = ddvStorageSpaceTypeSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder ddvTaskStatusSchemaType = typesBuilder.getSchemaType("ddvTaskStatus");
		MetadataSchemaBuilder ddvTaskStatusSchema = ddvTaskStatusSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder ddvTaskTypeSchemaType = typesBuilder.getSchemaType("ddvTaskType");
		MetadataSchemaBuilder ddvTaskTypeSchema = ddvTaskTypeSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder ddvVariablePeriodSchemaType = typesBuilder.getSchemaType("ddvVariablePeriod");
		MetadataSchemaBuilder ddvVariablePeriodSchema = ddvVariablePeriodSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder decommissioningListSchemaType = typesBuilder.getSchemaType("decommissioningList");
		MetadataSchemaBuilder decommissioningListSchema = decommissioningListSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder documentSchemaType = typesBuilder.getSchemaType("document");
		MetadataSchemaBuilder document_emailSchema = documentSchemaType.getCustomSchema("email");
		MetadataSchemaBuilder documentSchema = documentSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder emailToSendSchemaType = typesBuilder.getSchemaType("emailToSend");
		MetadataSchemaBuilder emailToSendSchema = emailToSendSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder eventSchemaType = typesBuilder.getSchemaType("event");
		MetadataSchemaBuilder eventSchema = eventSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder facetSchemaType = typesBuilder.getSchemaType("facet");
		MetadataSchemaBuilder facet_fieldSchema = facetSchemaType.getCustomSchema("field");
		MetadataSchemaBuilder facet_querySchema = facetSchemaType.getCustomSchema("query");
		MetadataSchemaBuilder facetSchema = facetSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder filingSpaceSchemaType = typesBuilder.getSchemaType("filingSpace");
		MetadataSchemaBuilder filingSpaceSchema = filingSpaceSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder folderSchemaType = typesBuilder.getSchemaType("folder");
		MetadataSchemaBuilder folderSchema = folderSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder reportSchemaType = typesBuilder.getSchemaType("report");
		MetadataSchemaBuilder reportSchema = reportSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder retentionRuleSchemaType = typesBuilder.getSchemaType("retentionRule");
		MetadataSchemaBuilder retentionRuleSchema = retentionRuleSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder robotSchemaType = typesBuilder.getSchemaType("robot");
		MetadataSchemaBuilder robotSchema = robotSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder robotLogSchemaType = typesBuilder.getSchemaType("robotLog");
		MetadataSchemaBuilder robotLogSchema = robotLogSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder savedSearchSchemaType = typesBuilder.getSchemaType("savedSearch");
		MetadataSchemaBuilder savedSearchSchema = savedSearchSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder storageSpaceSchemaType = typesBuilder.getSchemaType("storageSpace");
		MetadataSchemaBuilder storageSpaceSchema = storageSpaceSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder taskSchemaType = typesBuilder.getSchemaType("task");
		MetadataSchemaBuilder task_approvalSchema = taskSchemaType.getCustomSchema("approval");
		MetadataSchemaBuilder taskSchema = taskSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder uniformSubdivisionSchemaType = typesBuilder.getSchemaType("uniformSubdivision");
		MetadataSchemaBuilder uniformSubdivisionSchema = uniformSubdivisionSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder userDocumentSchemaType = typesBuilder.getSchemaType("userDocument");
		MetadataSchemaBuilder userDocumentSchema = userDocumentSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder userTaskSchemaType = typesBuilder.getSchemaType("userTask");
		MetadataSchemaBuilder userTaskSchema = userTaskSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder workflowSchemaType = typesBuilder.getSchemaType("workflow");
		MetadataSchemaBuilder workflowSchema = workflowSchemaType.getDefaultSchema();
		MetadataSchemaTypeBuilder workflowInstanceSchemaType = typesBuilder.getSchemaType("workflowInstance");
		MetadataSchemaBuilder workflowInstanceSchema = workflowInstanceSchemaType.getDefaultSchema();
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_actionAfterClassification = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.create("actionAfterClassification").setType(MetadataValueType.ENUM);
		actionParameters_classifyConnectorFolderDirectlyInThePlan_actionAfterClassification.setDefaultRequirement(true);
		actionParameters_classifyConnectorFolderDirectlyInThePlan_actionAfterClassification
				.setDefaultValue(ActionAfterClassification.DO_NOTHING);
		actionParameters_classifyConnectorFolderDirectlyInThePlan_actionAfterClassification
				.defineAsEnum(ActionAfterClassification.class);
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultAdminUnit = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.create("defaultAdminUnit").setType(MetadataValueType.REFERENCE);
		actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultAdminUnit.setDefaultRequirement(true);
		actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultAdminUnit
				.defineReferencesTo(administrativeUnitSchemaType);
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCategory = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.create("defaultCategory").setType(MetadataValueType.REFERENCE);
		actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCategory.setDefaultRequirement(true);
		actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCategory.defineReferencesTo(categorySchemaType);
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCopyStatus = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.create("defaultCopyStatus").setType(MetadataValueType.ENUM);
		actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCopyStatus.setDefaultRequirement(true);
		actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCopyStatus.defineAsEnum(CopyType.class);
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultOpenDate = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.create("defaultOpenDate").setType(MetadataValueType.DATE);
		actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultOpenDate.setDefaultRequirement(true);
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultRetentionRule = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.create("defaultRetentionRule").setType(MetadataValueType.REFERENCE);
		actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultRetentionRule.setDefaultRequirement(true);
		actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultRetentionRule
				.defineReferencesTo(retentionRuleSchemaType);
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_documentMapping = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.create("documentMapping").setType(MetadataValueType.CONTENT);
		actionParameters_classifyConnectorFolderDirectlyInThePlan_documentMapping.defineStructureFactory(ContentFactory.class);
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_folderMapping = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.create("folderMapping").setType(MetadataValueType.CONTENT);
		actionParameters_classifyConnectorFolderDirectlyInThePlan_folderMapping.defineStructureFactory(ContentFactory.class);
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_actionAfterClassification = actionParameters_classifyConnectorFolderInParentFolderSchema
				.create("actionAfterClassification").setType(MetadataValueType.ENUM);
		actionParameters_classifyConnectorFolderInParentFolder_actionAfterClassification.setDefaultRequirement(true);
		actionParameters_classifyConnectorFolderInParentFolder_actionAfterClassification
				.setDefaultValue(ActionAfterClassification.DO_NOTHING);
		actionParameters_classifyConnectorFolderInParentFolder_actionAfterClassification
				.defineAsEnum(ActionAfterClassification.class);
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_defaultOpenDate = actionParameters_classifyConnectorFolderInParentFolderSchema
				.create("defaultOpenDate").setType(MetadataValueType.DATE);
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_defaultParentFolder = actionParameters_classifyConnectorFolderInParentFolderSchema
				.create("defaultParentFolder").setType(MetadataValueType.REFERENCE);
		actionParameters_classifyConnectorFolderInParentFolder_defaultParentFolder.setUndeletable(true);
		actionParameters_classifyConnectorFolderInParentFolder_defaultParentFolder.defineReferencesTo(folderSchemaType);
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_documentMapping = actionParameters_classifyConnectorFolderInParentFolderSchema
				.create("documentMapping").setType(MetadataValueType.CONTENT);
		actionParameters_classifyConnectorFolderInParentFolder_documentMapping.defineStructureFactory(ContentFactory.class);
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_folderMapping = actionParameters_classifyConnectorFolderInParentFolderSchema
				.create("folderMapping").setType(MetadataValueType.CONTENT);
		actionParameters_classifyConnectorFolderInParentFolder_folderMapping.defineStructureFactory(ContentFactory.class);
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_actionAfterClassification = actionParameters_classifyConnectorTaxonomySchema
				.create("actionAfterClassification").setType(MetadataValueType.ENUM);
		actionParameters_classifyConnectorTaxonomy_actionAfterClassification.setDefaultRequirement(true);
		actionParameters_classifyConnectorTaxonomy_actionAfterClassification
				.setDefaultValue(ActionAfterClassification.DO_NOTHING);
		actionParameters_classifyConnectorTaxonomy_actionAfterClassification.defineAsEnum(ActionAfterClassification.class);
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_defaultAdminUnit = actionParameters_classifyConnectorTaxonomySchema
				.create("defaultAdminUnit").setType(MetadataValueType.REFERENCE);
		actionParameters_classifyConnectorTaxonomy_defaultAdminUnit.defineReferencesTo(administrativeUnitSchemaType);
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_defaultCategory = actionParameters_classifyConnectorTaxonomySchema
				.create("defaultCategory").setType(MetadataValueType.REFERENCE);
		actionParameters_classifyConnectorTaxonomy_defaultCategory.defineReferencesTo(categorySchemaType);
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_defaultCopyStatus = actionParameters_classifyConnectorTaxonomySchema
				.create("defaultCopyStatus").setType(MetadataValueType.ENUM);
		actionParameters_classifyConnectorTaxonomy_defaultCopyStatus.defineAsEnum(CopyType.class);
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_defaultOpenDate = actionParameters_classifyConnectorTaxonomySchema
				.create("defaultOpenDate").setType(MetadataValueType.DATE);
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_defaultParentFolder = actionParameters_classifyConnectorTaxonomySchema
				.create("defaultParentFolder").setType(MetadataValueType.REFERENCE);
		actionParameters_classifyConnectorTaxonomy_defaultParentFolder.setUndeletable(true);
		actionParameters_classifyConnectorTaxonomy_defaultParentFolder.defineReferencesTo(folderSchemaType);
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_defaultRetentionRule = actionParameters_classifyConnectorTaxonomySchema
				.create("defaultRetentionRule").setType(MetadataValueType.REFERENCE);
		actionParameters_classifyConnectorTaxonomy_defaultRetentionRule.defineReferencesTo(retentionRuleSchemaType);
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_delimiter = actionParameters_classifyConnectorTaxonomySchema
				.create("delimiter").setType(MetadataValueType.STRING);
		actionParameters_classifyConnectorTaxonomy_delimiter.setDefaultRequirement(true);
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_documentMapping = actionParameters_classifyConnectorTaxonomySchema
				.create("documentMapping").setType(MetadataValueType.CONTENT);
		actionParameters_classifyConnectorTaxonomy_documentMapping.defineStructureFactory(ContentFactory.class);
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_folderMapping = actionParameters_classifyConnectorTaxonomySchema
				.create("folderMapping").setType(MetadataValueType.CONTENT);
		actionParameters_classifyConnectorTaxonomy_folderMapping.defineStructureFactory(ContentFactory.class);
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_inTaxonomy = actionParameters_classifyConnectorTaxonomySchema
				.create("inTaxonomy").setType(MetadataValueType.STRING);
		actionParameters_classifyConnectorTaxonomy_inTaxonomy.setDefaultRequirement(true);
		actionParameters_classifyConnectorTaxonomy_inTaxonomy.setDefaultValue("admUnits");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_pathPrefix = actionParameters_classifyConnectorTaxonomySchema
				.create("pathPrefix").setType(MetadataValueType.STRING);
		actionParameters_classifyConnectorTaxonomy_pathPrefix.setDefaultRequirement(true);
		actionParameters_classifyConnectorTaxonomy_pathPrefix.setDefaultValue("smb://");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_actionAfterClassification = actionParameters_classifySmbDocumentInFolderSchema
				.create("actionAfterClassification").setType(MetadataValueType.ENUM);
		actionParameters_classifySmbDocumentInFolder_actionAfterClassification.setDefaultRequirement(true);
		actionParameters_classifySmbDocumentInFolder_actionAfterClassification.setUndeletable(true);
		actionParameters_classifySmbDocumentInFolder_actionAfterClassification
				.setDefaultValue(ActionAfterClassification.DO_NOTHING);
		actionParameters_classifySmbDocumentInFolder_actionAfterClassification.defineAsEnum(ActionAfterClassification.class);
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_documentType = actionParameters_classifySmbDocumentInFolderSchema
				.create("documentType").setType(MetadataValueType.REFERENCE);
		actionParameters_classifySmbDocumentInFolder_documentType.setUndeletable(true);
		actionParameters_classifySmbDocumentInFolder_documentType.defineReferencesTo(ddvDocumentTypeSchemaType);
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_inFolder = actionParameters_classifySmbDocumentInFolderSchema
				.create("inFolder").setType(MetadataValueType.REFERENCE);
		actionParameters_classifySmbDocumentInFolder_inFolder.setDefaultRequirement(true);
		actionParameters_classifySmbDocumentInFolder_inFolder.defineReferencesTo(folderSchemaType);
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_majorVersions = actionParameters_classifySmbDocumentInFolderSchema
				.create("majorVersions").setType(MetadataValueType.BOOLEAN);
		actionParameters_classifySmbDocumentInFolder_majorVersions.setDefaultRequirement(true);
		actionParameters_classifySmbDocumentInFolder_majorVersions.setDefaultValue(true);
		MetadataBuilder actionParameters_classifySmbFolderInFolder_inFolder = actionParameters_classifySmbFolderInFolderSchema
				.create("inFolder").setType(MetadataValueType.REFERENCE);
		actionParameters_classifySmbFolderInFolder_inFolder.setDefaultRequirement(true);
		actionParameters_classifySmbFolderInFolder_inFolder.defineReferencesTo(folderSchemaType);
		MetadataBuilder actionParameters_classifySmbFolderInFolder_majorVersions = actionParameters_classifySmbFolderInFolderSchema
				.create("majorVersions").setType(MetadataValueType.BOOLEAN);
		actionParameters_classifySmbFolderInFolder_majorVersions.setDefaultRequirement(true);
		actionParameters_classifySmbFolderInFolder_majorVersions.setDefaultValue(true);
		MetadataBuilder document_createdByRobot = documentSchema.create("createdByRobot").setType(MetadataValueType.STRING);
		document_createdByRobot.setSystemReserved(true);
		document_createdByRobot.setUndeletable(true);
		MetadataBuilder folder_createdByRobot = folderSchema.create("createdByRobot").setType(MetadataValueType.STRING);
		folder_createdByRobot.setSystemReserved(true);
		folder_createdByRobot.setUndeletable(true);
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_allReferences = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("allReferences");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_allauthorizations = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("allauthorizations");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_authorizations = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("authorizations");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_createdBy = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("createdBy");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_createdOn = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("createdOn");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_deleted = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("deleted");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_denyTokens = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("denyTokens");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_detachedauthorizations = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("detachedauthorizations");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_errorOnPhysicalDeletion = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("errorOnPhysicalDeletion");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_followers = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("followers");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_id = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("id");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_inheritedauthorizations = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("inheritedauthorizations");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_legacyIdentifier = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("legacyIdentifier");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_logicallyDeletedOn = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("logicallyDeletedOn");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_manualTokens = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("manualTokens");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_markedForPreviewConversion = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("markedForPreviewConversion");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_markedForReindexing = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("markedForReindexing");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_modifiedBy = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("modifiedBy");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_modifiedOn = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("modifiedOn");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_parentpath = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("parentpath");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_path = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("path");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_pathParts = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("pathParts");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_principalpath = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("principalpath");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_removedauthorizations = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("removedauthorizations");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_schema = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("schema");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_searchable = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("searchable");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_shareDenyTokens = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("shareDenyTokens");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_shareTokens = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("shareTokens");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_title = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("title");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_tokens = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("tokens");
		MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_visibleInTrees = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema
				.get("visibleInTrees");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_allReferences = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("allReferences");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_allauthorizations = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("allauthorizations");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_authorizations = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("authorizations");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_createdBy = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("createdBy");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_createdOn = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("createdOn");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_deleted = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("deleted");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_denyTokens = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("denyTokens");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_detachedauthorizations = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("detachedauthorizations");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_errorOnPhysicalDeletion = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("errorOnPhysicalDeletion");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_followers = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("followers");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_id = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("id");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_inheritedauthorizations = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("inheritedauthorizations");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_legacyIdentifier = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("legacyIdentifier");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_logicallyDeletedOn = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("logicallyDeletedOn");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_manualTokens = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("manualTokens");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_markedForPreviewConversion = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("markedForPreviewConversion");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_markedForReindexing = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("markedForReindexing");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_modifiedBy = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("modifiedBy");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_modifiedOn = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("modifiedOn");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_parentpath = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("parentpath");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_path = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("path");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_pathParts = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("pathParts");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_principalpath = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("principalpath");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_removedauthorizations = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("removedauthorizations");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_schema = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("schema");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_searchable = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("searchable");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_shareDenyTokens = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("shareDenyTokens");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_shareTokens = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("shareTokens");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_title = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("title");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_tokens = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("tokens");
		MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_visibleInTrees = actionParameters_classifyConnectorFolderInParentFolderSchema
				.get("visibleInTrees");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_allReferences = actionParameters_classifyConnectorTaxonomySchema
				.get("allReferences");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_allauthorizations = actionParameters_classifyConnectorTaxonomySchema
				.get("allauthorizations");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_authorizations = actionParameters_classifyConnectorTaxonomySchema
				.get("authorizations");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_createdBy = actionParameters_classifyConnectorTaxonomySchema
				.get("createdBy");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_createdOn = actionParameters_classifyConnectorTaxonomySchema
				.get("createdOn");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_deleted = actionParameters_classifyConnectorTaxonomySchema
				.get("deleted");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_denyTokens = actionParameters_classifyConnectorTaxonomySchema
				.get("denyTokens");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_detachedauthorizations = actionParameters_classifyConnectorTaxonomySchema
				.get("detachedauthorizations");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_errorOnPhysicalDeletion = actionParameters_classifyConnectorTaxonomySchema
				.get("errorOnPhysicalDeletion");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_followers = actionParameters_classifyConnectorTaxonomySchema
				.get("followers");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_id = actionParameters_classifyConnectorTaxonomySchema
				.get("id");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_inheritedauthorizations = actionParameters_classifyConnectorTaxonomySchema
				.get("inheritedauthorizations");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_legacyIdentifier = actionParameters_classifyConnectorTaxonomySchema
				.get("legacyIdentifier");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_logicallyDeletedOn = actionParameters_classifyConnectorTaxonomySchema
				.get("logicallyDeletedOn");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_manualTokens = actionParameters_classifyConnectorTaxonomySchema
				.get("manualTokens");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_markedForPreviewConversion = actionParameters_classifyConnectorTaxonomySchema
				.get("markedForPreviewConversion");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_markedForReindexing = actionParameters_classifyConnectorTaxonomySchema
				.get("markedForReindexing");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_modifiedBy = actionParameters_classifyConnectorTaxonomySchema
				.get("modifiedBy");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_modifiedOn = actionParameters_classifyConnectorTaxonomySchema
				.get("modifiedOn");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_parentpath = actionParameters_classifyConnectorTaxonomySchema
				.get("parentpath");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_path = actionParameters_classifyConnectorTaxonomySchema
				.get("path");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_pathParts = actionParameters_classifyConnectorTaxonomySchema
				.get("pathParts");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_principalpath = actionParameters_classifyConnectorTaxonomySchema
				.get("principalpath");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_removedauthorizations = actionParameters_classifyConnectorTaxonomySchema
				.get("removedauthorizations");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_schema = actionParameters_classifyConnectorTaxonomySchema
				.get("schema");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_searchable = actionParameters_classifyConnectorTaxonomySchema
				.get("searchable");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_shareDenyTokens = actionParameters_classifyConnectorTaxonomySchema
				.get("shareDenyTokens");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_shareTokens = actionParameters_classifyConnectorTaxonomySchema
				.get("shareTokens");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_title = actionParameters_classifyConnectorTaxonomySchema
				.get("title");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_tokens = actionParameters_classifyConnectorTaxonomySchema
				.get("tokens");
		MetadataBuilder actionParameters_classifyConnectorTaxonomy_visibleInTrees = actionParameters_classifyConnectorTaxonomySchema
				.get("visibleInTrees");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_allReferences = actionParameters_classifySmbDocumentInFolderSchema
				.get("allReferences");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_allauthorizations = actionParameters_classifySmbDocumentInFolderSchema
				.get("allauthorizations");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_authorizations = actionParameters_classifySmbDocumentInFolderSchema
				.get("authorizations");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_createdBy = actionParameters_classifySmbDocumentInFolderSchema
				.get("createdBy");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_createdOn = actionParameters_classifySmbDocumentInFolderSchema
				.get("createdOn");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_deleted = actionParameters_classifySmbDocumentInFolderSchema
				.get("deleted");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_denyTokens = actionParameters_classifySmbDocumentInFolderSchema
				.get("denyTokens");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_detachedauthorizations = actionParameters_classifySmbDocumentInFolderSchema
				.get("detachedauthorizations");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_errorOnPhysicalDeletion = actionParameters_classifySmbDocumentInFolderSchema
				.get("errorOnPhysicalDeletion");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_followers = actionParameters_classifySmbDocumentInFolderSchema
				.get("followers");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_id = actionParameters_classifySmbDocumentInFolderSchema
				.get("id");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_inheritedauthorizations = actionParameters_classifySmbDocumentInFolderSchema
				.get("inheritedauthorizations");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_legacyIdentifier = actionParameters_classifySmbDocumentInFolderSchema
				.get("legacyIdentifier");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_logicallyDeletedOn = actionParameters_classifySmbDocumentInFolderSchema
				.get("logicallyDeletedOn");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_manualTokens = actionParameters_classifySmbDocumentInFolderSchema
				.get("manualTokens");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_markedForPreviewConversion = actionParameters_classifySmbDocumentInFolderSchema
				.get("markedForPreviewConversion");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_markedForReindexing = actionParameters_classifySmbDocumentInFolderSchema
				.get("markedForReindexing");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_modifiedBy = actionParameters_classifySmbDocumentInFolderSchema
				.get("modifiedBy");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_modifiedOn = actionParameters_classifySmbDocumentInFolderSchema
				.get("modifiedOn");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_parentpath = actionParameters_classifySmbDocumentInFolderSchema
				.get("parentpath");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_path = actionParameters_classifySmbDocumentInFolderSchema
				.get("path");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_pathParts = actionParameters_classifySmbDocumentInFolderSchema
				.get("pathParts");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_principalpath = actionParameters_classifySmbDocumentInFolderSchema
				.get("principalpath");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_removedauthorizations = actionParameters_classifySmbDocumentInFolderSchema
				.get("removedauthorizations");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_schema = actionParameters_classifySmbDocumentInFolderSchema
				.get("schema");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_searchable = actionParameters_classifySmbDocumentInFolderSchema
				.get("searchable");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_shareDenyTokens = actionParameters_classifySmbDocumentInFolderSchema
				.get("shareDenyTokens");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_shareTokens = actionParameters_classifySmbDocumentInFolderSchema
				.get("shareTokens");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_title = actionParameters_classifySmbDocumentInFolderSchema
				.get("title");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_tokens = actionParameters_classifySmbDocumentInFolderSchema
				.get("tokens");
		MetadataBuilder actionParameters_classifySmbDocumentInFolder_visibleInTrees = actionParameters_classifySmbDocumentInFolderSchema
				.get("visibleInTrees");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_allReferences = actionParameters_classifySmbFolderInFolderSchema
				.get("allReferences");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_allauthorizations = actionParameters_classifySmbFolderInFolderSchema
				.get("allauthorizations");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_authorizations = actionParameters_classifySmbFolderInFolderSchema
				.get("authorizations");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_createdBy = actionParameters_classifySmbFolderInFolderSchema
				.get("createdBy");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_createdOn = actionParameters_classifySmbFolderInFolderSchema
				.get("createdOn");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_deleted = actionParameters_classifySmbFolderInFolderSchema
				.get("deleted");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_denyTokens = actionParameters_classifySmbFolderInFolderSchema
				.get("denyTokens");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_detachedauthorizations = actionParameters_classifySmbFolderInFolderSchema
				.get("detachedauthorizations");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_errorOnPhysicalDeletion = actionParameters_classifySmbFolderInFolderSchema
				.get("errorOnPhysicalDeletion");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_followers = actionParameters_classifySmbFolderInFolderSchema
				.get("followers");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_id = actionParameters_classifySmbFolderInFolderSchema
				.get("id");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_inheritedauthorizations = actionParameters_classifySmbFolderInFolderSchema
				.get("inheritedauthorizations");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_legacyIdentifier = actionParameters_classifySmbFolderInFolderSchema
				.get("legacyIdentifier");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_logicallyDeletedOn = actionParameters_classifySmbFolderInFolderSchema
				.get("logicallyDeletedOn");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_manualTokens = actionParameters_classifySmbFolderInFolderSchema
				.get("manualTokens");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_markedForPreviewConversion = actionParameters_classifySmbFolderInFolderSchema
				.get("markedForPreviewConversion");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_markedForReindexing = actionParameters_classifySmbFolderInFolderSchema
				.get("markedForReindexing");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_modifiedBy = actionParameters_classifySmbFolderInFolderSchema
				.get("modifiedBy");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_modifiedOn = actionParameters_classifySmbFolderInFolderSchema
				.get("modifiedOn");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_parentpath = actionParameters_classifySmbFolderInFolderSchema
				.get("parentpath");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_path = actionParameters_classifySmbFolderInFolderSchema
				.get("path");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_pathParts = actionParameters_classifySmbFolderInFolderSchema
				.get("pathParts");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_principalpath = actionParameters_classifySmbFolderInFolderSchema
				.get("principalpath");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_removedauthorizations = actionParameters_classifySmbFolderInFolderSchema
				.get("removedauthorizations");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_schema = actionParameters_classifySmbFolderInFolderSchema
				.get("schema");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_searchable = actionParameters_classifySmbFolderInFolderSchema
				.get("searchable");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_shareDenyTokens = actionParameters_classifySmbFolderInFolderSchema
				.get("shareDenyTokens");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_shareTokens = actionParameters_classifySmbFolderInFolderSchema
				.get("shareTokens");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_title = actionParameters_classifySmbFolderInFolderSchema
				.get("title");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_tokens = actionParameters_classifySmbFolderInFolderSchema
				.get("tokens");
		MetadataBuilder actionParameters_classifySmbFolderInFolder_visibleInTrees = actionParameters_classifySmbFolderInFolderSchema
				.get("visibleInTrees");
		MetadataBuilder connectorInstance_http_allReferences = connectorInstance_httpSchema.get("allReferences");
		MetadataBuilder connectorInstance_http_allauthorizations = connectorInstance_httpSchema.get("allauthorizations");
		MetadataBuilder connectorInstance_http_authorizations = connectorInstance_httpSchema.get("authorizations");
		MetadataBuilder connectorInstance_http_availableFields = connectorInstance_httpSchema.get("availableFields");
		MetadataBuilder connectorInstance_http_code = connectorInstance_httpSchema.get("code");
		MetadataBuilder connectorInstance_http_connectorType = connectorInstance_httpSchema.get("connectorType");
		MetadataBuilder connectorInstance_http_createdBy = connectorInstance_httpSchema.get("createdBy");
		MetadataBuilder connectorInstance_http_createdOn = connectorInstance_httpSchema.get("createdOn");
		MetadataBuilder connectorInstance_http_deleted = connectorInstance_httpSchema.get("deleted");
		MetadataBuilder connectorInstance_http_denyTokens = connectorInstance_httpSchema.get("denyTokens");
		MetadataBuilder connectorInstance_http_detachedauthorizations = connectorInstance_httpSchema
				.get("detachedauthorizations");
		MetadataBuilder connectorInstance_http_enabled = connectorInstance_httpSchema.get("enabled");
		MetadataBuilder connectorInstance_http_errorOnPhysicalDeletion = connectorInstance_httpSchema
				.get("errorOnPhysicalDeletion");
		MetadataBuilder connectorInstance_http_followers = connectorInstance_httpSchema.get("followers");
		MetadataBuilder connectorInstance_http_id = connectorInstance_httpSchema.get("id");
		MetadataBuilder connectorInstance_http_inheritedauthorizations = connectorInstance_httpSchema
				.get("inheritedauthorizations");
		MetadataBuilder connectorInstance_http_lastTraversalOn = connectorInstance_httpSchema.get("lastTraversalOn");
		MetadataBuilder connectorInstance_http_legacyIdentifier = connectorInstance_httpSchema.get("legacyIdentifier");
		MetadataBuilder connectorInstance_http_logicallyDeletedOn = connectorInstance_httpSchema.get("logicallyDeletedOn");
		MetadataBuilder connectorInstance_http_manualTokens = connectorInstance_httpSchema.get("manualTokens");
		MetadataBuilder connectorInstance_http_markedForPreviewConversion = connectorInstance_httpSchema
				.get("markedForPreviewConversion");
		MetadataBuilder connectorInstance_http_markedForReindexing = connectorInstance_httpSchema.get("markedForReindexing");
		MetadataBuilder connectorInstance_http_modifiedBy = connectorInstance_httpSchema.get("modifiedBy");
		MetadataBuilder connectorInstance_http_modifiedOn = connectorInstance_httpSchema.get("modifiedOn");
		MetadataBuilder connectorInstance_http_parentpath = connectorInstance_httpSchema.get("parentpath");
		MetadataBuilder connectorInstance_http_path = connectorInstance_httpSchema.get("path");
		MetadataBuilder connectorInstance_http_pathParts = connectorInstance_httpSchema.get("pathParts");
		MetadataBuilder connectorInstance_http_principalpath = connectorInstance_httpSchema.get("principalpath");
		MetadataBuilder connectorInstance_http_propertiesMapping = connectorInstance_httpSchema.get("propertiesMapping");
		MetadataBuilder connectorInstance_http_removedauthorizations = connectorInstance_httpSchema.get("removedauthorizations");
		MetadataBuilder connectorInstance_http_schema = connectorInstance_httpSchema.get("schema");
		MetadataBuilder connectorInstance_http_searchable = connectorInstance_httpSchema.get("searchable");
		MetadataBuilder connectorInstance_http_shareDenyTokens = connectorInstance_httpSchema.get("shareDenyTokens");
		MetadataBuilder connectorInstance_http_shareTokens = connectorInstance_httpSchema.get("shareTokens");
		MetadataBuilder connectorInstance_http_title = connectorInstance_httpSchema.get("title");
		MetadataBuilder connectorInstance_http_tokens = connectorInstance_httpSchema.get("tokens");
		MetadataBuilder connectorInstance_http_traversalCode = connectorInstance_httpSchema.get("traversalCode");
		MetadataBuilder connectorInstance_http_traversalSchedule = connectorInstance_httpSchema.get("traversalSchedule");
		MetadataBuilder connectorInstance_http_visibleInTrees = connectorInstance_httpSchema.get("visibleInTrees");
		MetadataBuilder connectorInstance_ldap_allReferences = connectorInstance_ldapSchema.get("allReferences");
		MetadataBuilder connectorInstance_ldap_allauthorizations = connectorInstance_ldapSchema.get("allauthorizations");
		MetadataBuilder connectorInstance_ldap_authorizations = connectorInstance_ldapSchema.get("authorizations");
		MetadataBuilder connectorInstance_ldap_availableFields = connectorInstance_ldapSchema.get("availableFields");
		MetadataBuilder connectorInstance_ldap_code = connectorInstance_ldapSchema.get("code");
		MetadataBuilder connectorInstance_ldap_connectorType = connectorInstance_ldapSchema.get("connectorType");
		MetadataBuilder connectorInstance_ldap_createdBy = connectorInstance_ldapSchema.get("createdBy");
		MetadataBuilder connectorInstance_ldap_createdOn = connectorInstance_ldapSchema.get("createdOn");
		MetadataBuilder connectorInstance_ldap_deleted = connectorInstance_ldapSchema.get("deleted");
		MetadataBuilder connectorInstance_ldap_denyTokens = connectorInstance_ldapSchema.get("denyTokens");
		MetadataBuilder connectorInstance_ldap_detachedauthorizations = connectorInstance_ldapSchema
				.get("detachedauthorizations");
		MetadataBuilder connectorInstance_ldap_enabled = connectorInstance_ldapSchema.get("enabled");
		MetadataBuilder connectorInstance_ldap_errorOnPhysicalDeletion = connectorInstance_ldapSchema
				.get("errorOnPhysicalDeletion");
		MetadataBuilder connectorInstance_ldap_followers = connectorInstance_ldapSchema.get("followers");
		MetadataBuilder connectorInstance_ldap_id = connectorInstance_ldapSchema.get("id");
		MetadataBuilder connectorInstance_ldap_inheritedauthorizations = connectorInstance_ldapSchema
				.get("inheritedauthorizations");
		MetadataBuilder connectorInstance_ldap_lastTraversalOn = connectorInstance_ldapSchema.get("lastTraversalOn");
		MetadataBuilder connectorInstance_ldap_legacyIdentifier = connectorInstance_ldapSchema.get("legacyIdentifier");
		MetadataBuilder connectorInstance_ldap_logicallyDeletedOn = connectorInstance_ldapSchema.get("logicallyDeletedOn");
		MetadataBuilder connectorInstance_ldap_manualTokens = connectorInstance_ldapSchema.get("manualTokens");
		MetadataBuilder connectorInstance_ldap_markedForPreviewConversion = connectorInstance_ldapSchema
				.get("markedForPreviewConversion");
		MetadataBuilder connectorInstance_ldap_markedForReindexing = connectorInstance_ldapSchema.get("markedForReindexing");
		MetadataBuilder connectorInstance_ldap_modifiedBy = connectorInstance_ldapSchema.get("modifiedBy");
		MetadataBuilder connectorInstance_ldap_modifiedOn = connectorInstance_ldapSchema.get("modifiedOn");
		MetadataBuilder connectorInstance_ldap_parentpath = connectorInstance_ldapSchema.get("parentpath");
		MetadataBuilder connectorInstance_ldap_path = connectorInstance_ldapSchema.get("path");
		MetadataBuilder connectorInstance_ldap_pathParts = connectorInstance_ldapSchema.get("pathParts");
		MetadataBuilder connectorInstance_ldap_principalpath = connectorInstance_ldapSchema.get("principalpath");
		MetadataBuilder connectorInstance_ldap_propertiesMapping = connectorInstance_ldapSchema.get("propertiesMapping");
		MetadataBuilder connectorInstance_ldap_removedauthorizations = connectorInstance_ldapSchema.get("removedauthorizations");
		MetadataBuilder connectorInstance_ldap_schema = connectorInstance_ldapSchema.get("schema");
		MetadataBuilder connectorInstance_ldap_searchable = connectorInstance_ldapSchema.get("searchable");
		MetadataBuilder connectorInstance_ldap_shareDenyTokens = connectorInstance_ldapSchema.get("shareDenyTokens");
		MetadataBuilder connectorInstance_ldap_shareTokens = connectorInstance_ldapSchema.get("shareTokens");
		MetadataBuilder connectorInstance_ldap_title = connectorInstance_ldapSchema.get("title");
		MetadataBuilder connectorInstance_ldap_tokens = connectorInstance_ldapSchema.get("tokens");
		MetadataBuilder connectorInstance_ldap_traversalCode = connectorInstance_ldapSchema.get("traversalCode");
		MetadataBuilder connectorInstance_ldap_traversalSchedule = connectorInstance_ldapSchema.get("traversalSchedule");
		MetadataBuilder connectorInstance_ldap_visibleInTrees = connectorInstance_ldapSchema.get("visibleInTrees");
		MetadataBuilder connectorInstance_smb_allReferences = connectorInstance_smbSchema.get("allReferences");
		MetadataBuilder connectorInstance_smb_allauthorizations = connectorInstance_smbSchema.get("allauthorizations");
		MetadataBuilder connectorInstance_smb_authorizations = connectorInstance_smbSchema.get("authorizations");
		MetadataBuilder connectorInstance_smb_availableFields = connectorInstance_smbSchema.get("availableFields");
		MetadataBuilder connectorInstance_smb_code = connectorInstance_smbSchema.get("code");
		MetadataBuilder connectorInstance_smb_connectorType = connectorInstance_smbSchema.get("connectorType");
		MetadataBuilder connectorInstance_smb_createdBy = connectorInstance_smbSchema.get("createdBy");
		MetadataBuilder connectorInstance_smb_createdOn = connectorInstance_smbSchema.get("createdOn");
		MetadataBuilder connectorInstance_smb_deleted = connectorInstance_smbSchema.get("deleted");
		MetadataBuilder connectorInstance_smb_denyTokens = connectorInstance_smbSchema.get("denyTokens");
		MetadataBuilder connectorInstance_smb_detachedauthorizations = connectorInstance_smbSchema.get("detachedauthorizations");
		MetadataBuilder connectorInstance_smb_enabled = connectorInstance_smbSchema.get("enabled");
		MetadataBuilder connectorInstance_smb_errorOnPhysicalDeletion = connectorInstance_smbSchema
				.get("errorOnPhysicalDeletion");
		MetadataBuilder connectorInstance_smb_followers = connectorInstance_smbSchema.get("followers");
		MetadataBuilder connectorInstance_smb_id = connectorInstance_smbSchema.get("id");
		MetadataBuilder connectorInstance_smb_inheritedauthorizations = connectorInstance_smbSchema
				.get("inheritedauthorizations");
		MetadataBuilder connectorInstance_smb_lastTraversalOn = connectorInstance_smbSchema.get("lastTraversalOn");
		MetadataBuilder connectorInstance_smb_legacyIdentifier = connectorInstance_smbSchema.get("legacyIdentifier");
		MetadataBuilder connectorInstance_smb_logicallyDeletedOn = connectorInstance_smbSchema.get("logicallyDeletedOn");
		MetadataBuilder connectorInstance_smb_manualTokens = connectorInstance_smbSchema.get("manualTokens");
		MetadataBuilder connectorInstance_smb_markedForPreviewConversion = connectorInstance_smbSchema
				.get("markedForPreviewConversion");
		MetadataBuilder connectorInstance_smb_markedForReindexing = connectorInstance_smbSchema.get("markedForReindexing");
		MetadataBuilder connectorInstance_smb_modifiedBy = connectorInstance_smbSchema.get("modifiedBy");
		MetadataBuilder connectorInstance_smb_modifiedOn = connectorInstance_smbSchema.get("modifiedOn");
		MetadataBuilder connectorInstance_smb_parentpath = connectorInstance_smbSchema.get("parentpath");
		MetadataBuilder connectorInstance_smb_path = connectorInstance_smbSchema.get("path");
		MetadataBuilder connectorInstance_smb_pathParts = connectorInstance_smbSchema.get("pathParts");
		MetadataBuilder connectorInstance_smb_principalpath = connectorInstance_smbSchema.get("principalpath");
		MetadataBuilder connectorInstance_smb_propertiesMapping = connectorInstance_smbSchema.get("propertiesMapping");
		MetadataBuilder connectorInstance_smb_removedauthorizations = connectorInstance_smbSchema.get("removedauthorizations");
		MetadataBuilder connectorInstance_smb_schema = connectorInstance_smbSchema.get("schema");
		MetadataBuilder connectorInstance_smb_searchable = connectorInstance_smbSchema.get("searchable");
		MetadataBuilder connectorInstance_smb_shareDenyTokens = connectorInstance_smbSchema.get("shareDenyTokens");
		MetadataBuilder connectorInstance_smb_shareTokens = connectorInstance_smbSchema.get("shareTokens");
		MetadataBuilder connectorInstance_smb_title = connectorInstance_smbSchema.get("title");
		MetadataBuilder connectorInstance_smb_tokens = connectorInstance_smbSchema.get("tokens");
		MetadataBuilder connectorInstance_smb_traversalCode = connectorInstance_smbSchema.get("traversalCode");
		MetadataBuilder connectorInstance_smb_traversalSchedule = connectorInstance_smbSchema.get("traversalSchedule");
		MetadataBuilder connectorInstance_smb_visibleInTrees = connectorInstance_smbSchema.get("visibleInTrees");
		MetadataBuilder document_email_actualDepositDate = document_emailSchema.get("actualDepositDate");
		MetadataBuilder document_email_actualDepositDateEntered = document_emailSchema.get("actualDepositDateEntered");
		MetadataBuilder document_email_actualDestructionDate = document_emailSchema.get("actualDestructionDate");
		MetadataBuilder document_email_actualDestructionDateEntered = document_emailSchema.get("actualDestructionDateEntered");
		MetadataBuilder document_email_actualTransferDate = document_emailSchema.get("actualTransferDate");
		MetadataBuilder document_email_actualTransferDateEntered = document_emailSchema.get("actualTransferDateEntered");
		MetadataBuilder document_email_administrativeUnit = document_emailSchema.get("administrativeUnit");
		MetadataBuilder document_email_alertUsersWhenAvailable = document_emailSchema.get("alertUsersWhenAvailable");
		MetadataBuilder document_email_allReferences = document_emailSchema.get("allReferences");
		MetadataBuilder document_email_allauthorizations = document_emailSchema.get("allauthorizations");
		MetadataBuilder document_email_applicableCopyRule = document_emailSchema.get("applicableCopyRule");
		MetadataBuilder document_email_archivisticStatus = document_emailSchema.get("archivisticStatus");
		MetadataBuilder document_email_author = document_emailSchema.get("author");
		MetadataBuilder document_email_authorizations = document_emailSchema.get("authorizations");
		MetadataBuilder document_email_borrowed = document_emailSchema.get("borrowed");
		MetadataBuilder document_email_category = document_emailSchema.get("category");
		MetadataBuilder document_email_closingDate = document_emailSchema.get("closingDate");
		MetadataBuilder document_email_comments = document_emailSchema.get("comments");
		MetadataBuilder document_email_company = document_emailSchema.get("company");
		MetadataBuilder document_email_content = document_emailSchema.get("content");
		MetadataBuilder document_email_copyStatus = document_emailSchema.get("copyStatus");
		MetadataBuilder document_email_createdBy = document_emailSchema.get("createdBy");
		MetadataBuilder document_email_createdByRobot = document_emailSchema.get("createdByRobot");
		MetadataBuilder document_email_createdOn = document_emailSchema.get("createdOn");
		MetadataBuilder document_email_deleted = document_emailSchema.get("deleted");
		MetadataBuilder document_email_denyTokens = document_emailSchema.get("denyTokens");
		MetadataBuilder document_email_description = document_emailSchema.get("description");
		MetadataBuilder document_email_detachedauthorizations = document_emailSchema.get("detachedauthorizations");
		MetadataBuilder document_email_documentType = document_emailSchema.get("documentType");
		MetadataBuilder document_email_errorOnPhysicalDeletion = document_emailSchema.get("errorOnPhysicalDeletion");
		MetadataBuilder document_email_expectedDepositDate = document_emailSchema.get("expectedDepositDate");
		MetadataBuilder document_email_expectedDestructionDate = document_emailSchema.get("expectedDestructionDate");
		MetadataBuilder document_email_expectedTransferDate = document_emailSchema.get("expectedTransferDate");
		MetadataBuilder document_email_filingSpace = document_emailSchema.get("filingSpace");
		MetadataBuilder document_email_folder = document_emailSchema.get("folder");
		MetadataBuilder document_email_followers = document_emailSchema.get("followers");
		MetadataBuilder document_email_formCreatedBy = document_emailSchema.get("formCreatedBy");
		MetadataBuilder document_email_formCreatedOn = document_emailSchema.get("formCreatedOn");
		MetadataBuilder document_email_formModifiedBy = document_emailSchema.get("formModifiedBy");
		MetadataBuilder document_email_formModifiedOn = document_emailSchema.get("formModifiedOn");
		MetadataBuilder document_email_id = document_emailSchema.get("id");
		MetadataBuilder document_email_inheritedRetentionRule = document_emailSchema.get("inheritedRetentionRule");
		MetadataBuilder document_email_inheritedauthorizations = document_emailSchema.get("inheritedauthorizations");
		MetadataBuilder document_email_keywords = document_emailSchema.get("keywords");
		MetadataBuilder document_email_legacyIdentifier = document_emailSchema.get("legacyIdentifier");
		MetadataBuilder document_email_logicallyDeletedOn = document_emailSchema.get("logicallyDeletedOn");
		MetadataBuilder document_email_mainCopyRule = document_emailSchema.get("mainCopyRule");
		MetadataBuilder document_email_mainCopyRuleIdEntered = document_emailSchema.get("mainCopyRuleIdEntered");
		MetadataBuilder document_email_manualTokens = document_emailSchema.get("manualTokens");
		MetadataBuilder document_email_markedForPreviewConversion = document_emailSchema.get("markedForPreviewConversion");
		MetadataBuilder document_email_markedForReindexing = document_emailSchema.get("markedForReindexing");
		MetadataBuilder document_email_modifiedBy = document_emailSchema.get("modifiedBy");
		MetadataBuilder document_email_modifiedOn = document_emailSchema.get("modifiedOn");
		MetadataBuilder document_email_openingDate = document_emailSchema.get("openingDate");
		MetadataBuilder document_email_parentpath = document_emailSchema.get("parentpath");
		MetadataBuilder document_email_path = document_emailSchema.get("path");
		MetadataBuilder document_email_pathParts = document_emailSchema.get("pathParts");
		MetadataBuilder document_email_principalpath = document_emailSchema.get("principalpath");
		MetadataBuilder document_email_published = document_emailSchema.get("published");
		MetadataBuilder document_email_removedauthorizations = document_emailSchema.get("removedauthorizations");
		MetadataBuilder document_email_retentionRule = document_emailSchema.get("retentionRule");
		MetadataBuilder document_email_sameInactiveFateAsFolder = document_emailSchema.get("sameInactiveFateAsFolder");
		MetadataBuilder document_email_sameSemiActiveFateAsFolder = document_emailSchema.get("sameSemiActiveFateAsFolder");
		MetadataBuilder document_email_schema = document_emailSchema.get("schema");
		MetadataBuilder document_email_searchable = document_emailSchema.get("searchable");
		MetadataBuilder document_email_shareDenyTokens = document_emailSchema.get("shareDenyTokens");
		MetadataBuilder document_email_shareTokens = document_emailSchema.get("shareTokens");
		MetadataBuilder document_email_subject = document_emailSchema.get("subject");
		MetadataBuilder document_email_title = document_emailSchema.get("title");
		MetadataBuilder document_email_tokens = document_emailSchema.get("tokens");
		MetadataBuilder document_email_type = document_emailSchema.get("type");
		MetadataBuilder document_email_version = document_emailSchema.get("version");
		MetadataBuilder document_email_visibleInTrees = document_emailSchema.get("visibleInTrees");
		MetadataBuilder facet_field_active = facet_fieldSchema.get("active");
		MetadataBuilder facet_field_allReferences = facet_fieldSchema.get("allReferences");
		MetadataBuilder facet_field_allauthorizations = facet_fieldSchema.get("allauthorizations");
		MetadataBuilder facet_field_authorizations = facet_fieldSchema.get("authorizations");
		MetadataBuilder facet_field_createdBy = facet_fieldSchema.get("createdBy");
		MetadataBuilder facet_field_createdOn = facet_fieldSchema.get("createdOn");
		MetadataBuilder facet_field_deleted = facet_fieldSchema.get("deleted");
		MetadataBuilder facet_field_denyTokens = facet_fieldSchema.get("denyTokens");
		MetadataBuilder facet_field_detachedauthorizations = facet_fieldSchema.get("detachedauthorizations");
		MetadataBuilder facet_field_elementPerPage = facet_fieldSchema.get("elementPerPage");
		MetadataBuilder facet_field_errorOnPhysicalDeletion = facet_fieldSchema.get("errorOnPhysicalDeletion");
		MetadataBuilder facet_field_facetType = facet_fieldSchema.get("facetType");
		MetadataBuilder facet_field_fieldDatastoreCode = facet_fieldSchema.get("fieldDatastoreCode");
		MetadataBuilder facet_field_followers = facet_fieldSchema.get("followers");
		MetadataBuilder facet_field_id = facet_fieldSchema.get("id");
		MetadataBuilder facet_field_inheritedauthorizations = facet_fieldSchema.get("inheritedauthorizations");
		MetadataBuilder facet_field_legacyIdentifier = facet_fieldSchema.get("legacyIdentifier");
		MetadataBuilder facet_field_logicallyDeletedOn = facet_fieldSchema.get("logicallyDeletedOn");
		MetadataBuilder facet_field_manualTokens = facet_fieldSchema.get("manualTokens");
		MetadataBuilder facet_field_markedForPreviewConversion = facet_fieldSchema.get("markedForPreviewConversion");
		MetadataBuilder facet_field_markedForReindexing = facet_fieldSchema.get("markedForReindexing");
		MetadataBuilder facet_field_modifiedBy = facet_fieldSchema.get("modifiedBy");
		MetadataBuilder facet_field_modifiedOn = facet_fieldSchema.get("modifiedOn");
		MetadataBuilder facet_field_openByDefault = facet_fieldSchema.get("openByDefault");
		MetadataBuilder facet_field_order = facet_fieldSchema.get("order");
		MetadataBuilder facet_field_orderResult = facet_fieldSchema.get("orderResult");
		MetadataBuilder facet_field_pages = facet_fieldSchema.get("pages");
		MetadataBuilder facet_field_parentpath = facet_fieldSchema.get("parentpath");
		MetadataBuilder facet_field_path = facet_fieldSchema.get("path");
		MetadataBuilder facet_field_pathParts = facet_fieldSchema.get("pathParts");
		MetadataBuilder facet_field_principalpath = facet_fieldSchema.get("principalpath");
		MetadataBuilder facet_field_removedauthorizations = facet_fieldSchema.get("removedauthorizations");
		MetadataBuilder facet_field_schema = facet_fieldSchema.get("schema");
		MetadataBuilder facet_field_searchable = facet_fieldSchema.get("searchable");
		MetadataBuilder facet_field_shareDenyTokens = facet_fieldSchema.get("shareDenyTokens");
		MetadataBuilder facet_field_shareTokens = facet_fieldSchema.get("shareTokens");
		MetadataBuilder facet_field_title = facet_fieldSchema.get("title");
		MetadataBuilder facet_field_tokens = facet_fieldSchema.get("tokens");
		MetadataBuilder facet_field_usedByModule = facet_fieldSchema.get("usedByModule");
		MetadataBuilder facet_field_visibleInTrees = facet_fieldSchema.get("visibleInTrees");
		MetadataBuilder facet_query_active = facet_querySchema.get("active");
		MetadataBuilder facet_query_allReferences = facet_querySchema.get("allReferences");
		MetadataBuilder facet_query_allauthorizations = facet_querySchema.get("allauthorizations");
		MetadataBuilder facet_query_authorizations = facet_querySchema.get("authorizations");
		MetadataBuilder facet_query_createdBy = facet_querySchema.get("createdBy");
		MetadataBuilder facet_query_createdOn = facet_querySchema.get("createdOn");
		MetadataBuilder facet_query_deleted = facet_querySchema.get("deleted");
		MetadataBuilder facet_query_denyTokens = facet_querySchema.get("denyTokens");
		MetadataBuilder facet_query_detachedauthorizations = facet_querySchema.get("detachedauthorizations");
		MetadataBuilder facet_query_elementPerPage = facet_querySchema.get("elementPerPage");
		MetadataBuilder facet_query_errorOnPhysicalDeletion = facet_querySchema.get("errorOnPhysicalDeletion");
		MetadataBuilder facet_query_facetType = facet_querySchema.get("facetType");
		MetadataBuilder facet_query_fieldDatastoreCode = facet_querySchema.get("fieldDatastoreCode");
		MetadataBuilder facet_query_followers = facet_querySchema.get("followers");
		MetadataBuilder facet_query_id = facet_querySchema.get("id");
		MetadataBuilder facet_query_inheritedauthorizations = facet_querySchema.get("inheritedauthorizations");
		MetadataBuilder facet_query_legacyIdentifier = facet_querySchema.get("legacyIdentifier");
		MetadataBuilder facet_query_logicallyDeletedOn = facet_querySchema.get("logicallyDeletedOn");
		MetadataBuilder facet_query_manualTokens = facet_querySchema.get("manualTokens");
		MetadataBuilder facet_query_markedForPreviewConversion = facet_querySchema.get("markedForPreviewConversion");
		MetadataBuilder facet_query_markedForReindexing = facet_querySchema.get("markedForReindexing");
		MetadataBuilder facet_query_modifiedBy = facet_querySchema.get("modifiedBy");
		MetadataBuilder facet_query_modifiedOn = facet_querySchema.get("modifiedOn");
		MetadataBuilder facet_query_openByDefault = facet_querySchema.get("openByDefault");
		MetadataBuilder facet_query_order = facet_querySchema.get("order");
		MetadataBuilder facet_query_orderResult = facet_querySchema.get("orderResult");
		MetadataBuilder facet_query_pages = facet_querySchema.get("pages");
		MetadataBuilder facet_query_parentpath = facet_querySchema.get("parentpath");
		MetadataBuilder facet_query_path = facet_querySchema.get("path");
		MetadataBuilder facet_query_pathParts = facet_querySchema.get("pathParts");
		MetadataBuilder facet_query_principalpath = facet_querySchema.get("principalpath");
		MetadataBuilder facet_query_removedauthorizations = facet_querySchema.get("removedauthorizations");
		MetadataBuilder facet_query_schema = facet_querySchema.get("schema");
		MetadataBuilder facet_query_searchable = facet_querySchema.get("searchable");
		MetadataBuilder facet_query_shareDenyTokens = facet_querySchema.get("shareDenyTokens");
		MetadataBuilder facet_query_shareTokens = facet_querySchema.get("shareTokens");
		MetadataBuilder facet_query_title = facet_querySchema.get("title");
		MetadataBuilder facet_query_tokens = facet_querySchema.get("tokens");
		MetadataBuilder facet_query_usedByModule = facet_querySchema.get("usedByModule");
		MetadataBuilder facet_query_visibleInTrees = facet_querySchema.get("visibleInTrees");
		MetadataBuilder task_approval_allReferences = task_approvalSchema.get("allReferences");
		MetadataBuilder task_approval_allauthorizations = task_approvalSchema.get("allauthorizations");
		MetadataBuilder task_approval_assignCandidates = task_approvalSchema.get("assignCandidates");
		MetadataBuilder task_approval_assignedOn = task_approvalSchema.get("assignedOn");
		MetadataBuilder task_approval_assignedTo = task_approvalSchema.get("assignedTo");
		MetadataBuilder task_approval_authorizations = task_approvalSchema.get("authorizations");
		MetadataBuilder task_approval_createdBy = task_approvalSchema.get("createdBy");
		MetadataBuilder task_approval_createdOn = task_approvalSchema.get("createdOn");
		MetadataBuilder task_approval_deleted = task_approvalSchema.get("deleted");
		MetadataBuilder task_approval_denyTokens = task_approvalSchema.get("denyTokens");
		MetadataBuilder task_approval_detachedauthorizations = task_approvalSchema.get("detachedauthorizations");
		MetadataBuilder task_approval_dueDate = task_approvalSchema.get("dueDate");
		MetadataBuilder task_approval_errorOnPhysicalDeletion = task_approvalSchema.get("errorOnPhysicalDeletion");
		MetadataBuilder task_approval_finishedBy = task_approvalSchema.get("finishedBy");
		MetadataBuilder task_approval_finishedOn = task_approvalSchema.get("finishedOn");
		MetadataBuilder task_approval_followers = task_approvalSchema.get("followers");
		MetadataBuilder task_approval_id = task_approvalSchema.get("id");
		MetadataBuilder task_approval_inheritedauthorizations = task_approvalSchema.get("inheritedauthorizations");
		MetadataBuilder task_approval_legacyIdentifier = task_approvalSchema.get("legacyIdentifier");
		MetadataBuilder task_approval_logicallyDeletedOn = task_approvalSchema.get("logicallyDeletedOn");
		MetadataBuilder task_approval_manualTokens = task_approvalSchema.get("manualTokens");
		MetadataBuilder task_approval_markedForPreviewConversion = task_approvalSchema.get("markedForPreviewConversion");
		MetadataBuilder task_approval_markedForReindexing = task_approvalSchema.get("markedForReindexing");
		MetadataBuilder task_approval_modifiedBy = task_approvalSchema.get("modifiedBy");
		MetadataBuilder task_approval_modifiedOn = task_approvalSchema.get("modifiedOn");
		MetadataBuilder task_approval_parentpath = task_approvalSchema.get("parentpath");
		MetadataBuilder task_approval_path = task_approvalSchema.get("path");
		MetadataBuilder task_approval_pathParts = task_approvalSchema.get("pathParts");
		MetadataBuilder task_approval_principalpath = task_approvalSchema.get("principalpath");
		MetadataBuilder task_approval_removedauthorizations = task_approvalSchema.get("removedauthorizations");
		MetadataBuilder task_approval_schema = task_approvalSchema.get("schema");
		MetadataBuilder task_approval_searchable = task_approvalSchema.get("searchable");
		MetadataBuilder task_approval_shareDenyTokens = task_approvalSchema.get("shareDenyTokens");
		MetadataBuilder task_approval_shareTokens = task_approvalSchema.get("shareTokens");
		MetadataBuilder task_approval_title = task_approvalSchema.get("title");
		MetadataBuilder task_approval_tokens = task_approvalSchema.get("tokens");
		MetadataBuilder task_approval_visibleInTrees = task_approvalSchema.get("visibleInTrees");
		MetadataBuilder task_approval_workflowIdentifier = task_approvalSchema.get("workflowIdentifier");
		MetadataBuilder task_approval_workflowRecordIdentifiers = task_approvalSchema.get("workflowRecordIdentifiers");
	}

	public void applySchemasDisplay(SchemasDisplayManager manager) {
		SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);
		SchemaTypesDisplayConfig typesConfig = manager.getTypes(collection);
		transaction.setModifiedCollectionTypes(manager.getTypes(collection).withFacetMetadataCodes(
				asList("folder_default_schema", "folder_default_archivisticStatus", "folder_default_category",
						"folder_default_administrativeUnit", "folder_default_filingSpace", "folder_default_mediumTypes",
						"folder_default_copyStatus")));
		transaction
				.add(manager.getType(collection, "actionParameters").withSimpleSearchStatus(false).withAdvancedSearchStatus(false)
						.withManageableStatus(false).withMetadataGroup(resourcesProvider.getLanguageMap(
								asList("default", "default:tab.taxonomy", "tab.options", "tab.mappings", "tab.defaultValues",
										"tab.advanced"))));
		transaction.add(manager.getSchema(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan")
				.withFormMetadataCodes(asList("actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultAdminUnit",
						"actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCategory",
						"actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultRetentionRule",
						"actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCopyStatus",
						"actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultOpenDate",
						"actionParameters_classifyConnectorFolderDirectlyInThePlan_actionAfterClassification",
						"actionParameters_classifyConnectorFolderDirectlyInThePlan_documentMapping",
						"actionParameters_classifyConnectorFolderDirectlyInThePlan_folderMapping")).withDisplayMetadataCodes(
						asList("actionParameters_classifyConnectorFolderDirectlyInThePlan_title",
								"actionParameters_classifyConnectorFolderDirectlyInThePlan_actionAfterClassification",
								"actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultAdminUnit",
								"actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCategory",
								"actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCopyStatus",
								"actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultOpenDate",
								"actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultRetentionRule",
								"actionParameters_classifyConnectorFolderDirectlyInThePlan_documentMapping",
								"actionParameters_classifyConnectorFolderDirectlyInThePlan_folderMapping"))
				.withSearchResultsMetadataCodes(asList("actionParameters_classifyConnectorFolderDirectlyInThePlan_title",
						"actionParameters_classifyConnectorFolderDirectlyInThePlan_modifiedOn")).withTableMetadataCodes(
						asList("actionParameters_classifyConnectorFolderDirectlyInThePlan_title",
								"actionParameters_classifyConnectorFolderDirectlyInThePlan_modifiedOn")));
		transaction.add(manager
				.getMetadata(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan_actionAfterClassification")
				.withMetadataGroup("tab.options").withInputType(MetadataInputType.RADIO_BUTTONS).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction
				.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultAdminUnit")
						.withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false)
						.withVisibleInAdvancedSearchStatus(false));
		transaction
				.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCategory")
						.withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false)
						.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager
				.getMetadata(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCopyStatus")
				.withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.RADIO_BUTTONS).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction
				.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultOpenDate")
						.withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.FIELD).withHighlightStatus(false)
						.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager
				.getMetadata(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultRetentionRule")
				.withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction
				.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan_documentMapping")
						.withMetadataGroup("tab.advanced").withInputType(MetadataInputType.CONTENT).withHighlightStatus(false)
						.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan_folderMapping")
				.withMetadataGroup("tab.advanced").withInputType(MetadataInputType.CONTENT).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getSchema(collection, "actionParameters_classifyConnectorFolderInParentFolder")
				.withFormMetadataCodes(asList("actionParameters_classifyConnectorFolderInParentFolder_defaultParentFolder",
						"actionParameters_classifyConnectorFolderInParentFolder_defaultOpenDate",
						"actionParameters_classifyConnectorFolderInParentFolder_actionAfterClassification",
						"actionParameters_classifyConnectorFolderInParentFolder_documentMapping",
						"actionParameters_classifyConnectorFolderInParentFolder_folderMapping")).withDisplayMetadataCodes(
						asList("actionParameters_classifyConnectorFolderInParentFolder_title",
								"actionParameters_classifyConnectorFolderInParentFolder_actionAfterClassification",
								"actionParameters_classifyConnectorFolderInParentFolder_defaultOpenDate",
								"actionParameters_classifyConnectorFolderInParentFolder_defaultParentFolder",
								"actionParameters_classifyConnectorFolderInParentFolder_documentMapping",
								"actionParameters_classifyConnectorFolderInParentFolder_folderMapping"))
				.withSearchResultsMetadataCodes(asList("actionParameters_classifyConnectorFolderInParentFolder_title",
						"actionParameters_classifyConnectorFolderInParentFolder_modifiedOn")).withTableMetadataCodes(
						asList("actionParameters_classifyConnectorFolderInParentFolder_title",
								"actionParameters_classifyConnectorFolderInParentFolder_modifiedOn")));
		transaction.add(manager
				.getMetadata(collection, "actionParameters_classifyConnectorFolderInParentFolder_actionAfterClassification")
				.withMetadataGroup("tab.options").withInputType(MetadataInputType.RADIO_BUTTONS).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderInParentFolder_defaultOpenDate")
				.withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.FIELD).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction
				.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderInParentFolder_defaultParentFolder")
						.withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false)
						.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderInParentFolder_documentMapping")
				.withMetadataGroup("tab.advanced").withInputType(MetadataInputType.CONTENT).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderInParentFolder_folderMapping")
				.withMetadataGroup("tab.advanced").withInputType(MetadataInputType.CONTENT).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getSchema(collection, "actionParameters_classifyConnectorTaxonomy").withFormMetadataCodes(
				asList("actionParameters_classifyConnectorTaxonomy_inTaxonomy",
						"actionParameters_classifyConnectorTaxonomy_pathPrefix",
						"actionParameters_classifyConnectorTaxonomy_delimiter",
						"actionParameters_classifyConnectorTaxonomy_defaultParentFolder",
						"actionParameters_classifyConnectorTaxonomy_defaultAdminUnit",
						"actionParameters_classifyConnectorTaxonomy_defaultCategory",
						"actionParameters_classifyConnectorTaxonomy_defaultRetentionRule",
						"actionParameters_classifyConnectorTaxonomy_defaultCopyStatus",
						"actionParameters_classifyConnectorTaxonomy_defaultOpenDate",
						"actionParameters_classifyConnectorTaxonomy_documentMapping",
						"actionParameters_classifyConnectorTaxonomy_folderMapping",
						"actionParameters_classifyConnectorTaxonomy_actionAfterClassification")).withDisplayMetadataCodes(
				asList("actionParameters_classifyConnectorTaxonomy_title",
						"actionParameters_classifyConnectorTaxonomy_actionAfterClassification",
						"actionParameters_classifyConnectorTaxonomy_defaultParentFolder",
						"actionParameters_classifyConnectorTaxonomy_defaultAdminUnit",
						"actionParameters_classifyConnectorTaxonomy_defaultCategory",
						"actionParameters_classifyConnectorTaxonomy_defaultCopyStatus",
						"actionParameters_classifyConnectorTaxonomy_defaultOpenDate",
						"actionParameters_classifyConnectorTaxonomy_defaultRetentionRule",
						"actionParameters_classifyConnectorTaxonomy_delimiter",
						"actionParameters_classifyConnectorTaxonomy_documentMapping",
						"actionParameters_classifyConnectorTaxonomy_folderMapping",
						"actionParameters_classifyConnectorTaxonomy_inTaxonomy",
						"actionParameters_classifyConnectorTaxonomy_pathPrefix")).withSearchResultsMetadataCodes(
				asList("actionParameters_classifyConnectorTaxonomy_title",
						"actionParameters_classifyConnectorTaxonomy_modifiedOn")).withTableMetadataCodes(
				asList("actionParameters_classifyConnectorTaxonomy_title",
						"actionParameters_classifyConnectorTaxonomy_modifiedOn")));
		transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_actionAfterClassification")
				.withMetadataGroup("tab.options").withInputType(MetadataInputType.RADIO_BUTTONS).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_defaultAdminUnit")
				.withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_defaultCategory")
				.withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_defaultCopyStatus")
				.withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.RADIO_BUTTONS).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_defaultOpenDate")
				.withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.FIELD).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_defaultParentFolder")
				.withMetadataGroup("Valeurs par défaut").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_defaultRetentionRule")
				.withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_delimiter")
				.withMetadataGroup("default:tab.taxonomy").withInputType(MetadataInputType.FIELD).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_documentMapping")
				.withMetadataGroup("tab.mappings").withInputType(MetadataInputType.CONTENT).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_folderMapping")
				.withMetadataGroup("tab.mappings").withInputType(MetadataInputType.CONTENT).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_inTaxonomy")
				.withMetadataGroup("default:tab.taxonomy").withInputType(MetadataInputType.FIELD).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_pathPrefix")
				.withMetadataGroup("default:tab.taxonomy").withInputType(MetadataInputType.FIELD).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getSchema(collection, "actionParameters_classifySmbDocumentInFolder").withFormMetadataCodes(
				asList("actionParameters_classifySmbDocumentInFolder_inFolder",
						"actionParameters_classifySmbDocumentInFolder_documentType",
						"actionParameters_classifySmbDocumentInFolder_majorVersions",
						"actionParameters_classifySmbDocumentInFolder_actionAfterClassification")).withDisplayMetadataCodes(
				asList("actionParameters_classifySmbDocumentInFolder_title",
						"actionParameters_classifySmbDocumentInFolder_actionAfterClassification",
						"actionParameters_classifySmbDocumentInFolder_inFolder",
						"actionParameters_classifySmbDocumentInFolder_majorVersions")).withSearchResultsMetadataCodes(
				asList("actionParameters_classifySmbDocumentInFolder_title",
						"actionParameters_classifySmbDocumentInFolder_modifiedOn")).withTableMetadataCodes(
				asList("actionParameters_classifySmbDocumentInFolder_title",
						"actionParameters_classifySmbDocumentInFolder_modifiedOn")));
		transaction.add(manager.getMetadata(collection, "actionParameters_classifySmbDocumentInFolder_actionAfterClassification")
				.withMetadataGroup("tab.options").withInputType(MetadataInputType.RADIO_BUTTONS).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getMetadata(collection, "actionParameters_classifySmbDocumentInFolder_documentType")
				.withMetadataGroup("Valeurs par défaut").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getMetadata(collection, "actionParameters_classifySmbDocumentInFolder_inFolder")
				.withMetadataGroup("Valeurs par défaut").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
		transaction.add(manager.getMetadata(collection, "actionParameters_classifySmbDocumentInFolder_majorVersions")
				.withMetadataGroup("Valeurs par défaut").withInputType(MetadataInputType.FIELD).withHighlightStatus(false)
				.withVisibleInAdvancedSearchStatus(false));
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
						"core.manageTrash", "core.manageValueList", "core.useExternalAPIS", "core.viewEvents", "rm.borrowFolder",
						"rm.createDocuments", "rm.createFolders", "rm.createInactiveDocuments", "rm.createSemiActiveDocuments",
						"rm.createSubFolders", "rm.createSubFoldersInInactiveFolders", "rm.createSubFoldersInSemiActiveFolders",
						"rm.decommissioning", "rm.deleteBorrowedDocuments", "rm.deleteInactiveDocuments",
						"rm.deleteInactiveFolders", "rm.deletePublishedDocuments", "rm.deleteSemiActiveDocuments",
						"rm.deleteSemiActiveFolders", "rm.duplicateInactiveFolders", "rm.duplicateSemiActiveFolders",
						"rm.editDecommissioningList", "rm.manageClassificationPlan", "rm.manageContainers",
						"rm.manageDocumentAuthorizations", "rm.manageFolderAuthorizations", "rm.manageReports",
						"rm.manageRetentionRule", "rm.manageStorageSpaces", "rm.manageUniformSubdivisions",
						"rm.modifyFolderDecomDate", "rm.modifyImportedDocuments", "rm.modifyImportedFolders",
						"rm.modifyInactiveBorrowedFolder", "rm.modifyInactiveDocuments", "rm.modifyInactiveFolders",
						"rm.modifyOpeningDateFolder", "rm.modifySemiActiveBorrowedFolder", "rm.modifySemiActiveDocuments",
						"rm.modifySemiActiveFolders", "rm.processDecommissioningList", "rm.publishAndUnpublishDocuments",
						"rm.returnOtherUsersDocuments", "rm.shareDocuments", "rm.shareFolders", "rm.shareImportedDocuments",
						"rm.shareImportedFolders", "rm.shareInactiveDocuments", "rm.shareInactiveFolders",
						"rm.shareSemiActiveDocuments", "rm.shareSemiActiveFolders", "rm.uploadInactiveDocuments",
						"rm.uploadSemiActiveDocuments", "rm.useCart", "robots.manageRobots", "tasks.manageWorkflows")));
		rolesManager.updateRole(rolesManager.getRole(collection, "U").withNewPermissions(
				asList("rm.borrowFolder", "rm.createDocuments", "rm.createFolders", "rm.createSubFolders",
						"rm.deletePublishedDocuments", "rm.deleteSemiActiveDocuments", "rm.deleteSemiActiveFolders",
						"rm.modifySemiActiveBorrowedFolder", "rm.publishAndUnpublishDocuments", "rm.shareDocuments",
						"rm.shareFolders", "rm.shareSemiActiveDocuments", "rm.shareSemiActiveFolders",
						"rm.uploadSemiActiveDocuments", "rm.useCart")));
		rolesManager.updateRole(rolesManager.getRole(collection, "M").withNewPermissions(
				asList("rm.borrowFolder", "rm.createDocuments", "rm.createFolders", "rm.createSubFolders", "rm.decommissioning",
						"rm.deletePublishedDocuments", "rm.deleteSemiActiveDocuments", "rm.deleteSemiActiveFolders",
						"rm.manageContainers", "rm.manageDocumentAuthorizations", "rm.manageFolderAuthorizations",
						"rm.modifyOpeningDateFolder", "rm.modifySemiActiveBorrowedFolder", "rm.publishAndUnpublishDocuments",
						"rm.shareDocuments", "rm.shareFolders", "rm.shareSemiActiveDocuments", "rm.shareSemiActiveFolders",
						"rm.uploadSemiActiveDocuments", "rm.useCart")));
		rolesManager.updateRole(rolesManager.getRole(collection, "RGD").withNewPermissions(
				asList("core.deleteContentVersion", "core.ldapConfigurationManagement", "core.manageConnectors",
						"core.manageEmailServer", "core.manageFacets", "core.manageMetadataExtractor",
						"core.manageMetadataSchemas", "core.manageSearchEngine", "core.manageSearchReports",
						"core.manageSecurity", "core.manageSystemCollections", "core.manageSystemConfiguration",
						"core.manageSystemDataImports", "core.manageSystemGroups", "core.manageSystemModules",
						"core.manageSystemServers", "core.manageSystemUpdates", "core.manageSystemUsers", "core.manageTaxonomies",
						"core.manageTrash", "core.manageValueList", "core.useExternalAPIS", "core.viewEvents", "rm.borrowFolder",
						"rm.createDocuments", "rm.createFolders", "rm.createInactiveDocuments", "rm.createSemiActiveDocuments",
						"rm.createSubFolders", "rm.createSubFoldersInInactiveFolders", "rm.createSubFoldersInSemiActiveFolders",
						"rm.decommissioning", "rm.deleteBorrowedDocuments", "rm.deleteInactiveDocuments",
						"rm.deleteInactiveFolders", "rm.deletePublishedDocuments", "rm.deleteSemiActiveDocuments",
						"rm.deleteSemiActiveFolders", "rm.duplicateInactiveFolders", "rm.duplicateSemiActiveFolders",
						"rm.editDecommissioningList", "rm.manageClassificationPlan", "rm.manageContainers",
						"rm.manageDocumentAuthorizations", "rm.manageFolderAuthorizations", "rm.manageReports",
						"rm.manageRetentionRule", "rm.manageStorageSpaces", "rm.manageUniformSubdivisions",
						"rm.modifyFolderDecomDate", "rm.modifyImportedDocuments", "rm.modifyImportedFolders",
						"rm.modifyInactiveBorrowedFolder", "rm.modifyInactiveDocuments", "rm.modifyInactiveFolders",
						"rm.modifyOpeningDateFolder", "rm.modifySemiActiveBorrowedFolder", "rm.modifySemiActiveDocuments",
						"rm.modifySemiActiveFolders", "rm.processDecommissioningList", "rm.publishAndUnpublishDocuments",
						"rm.returnOtherUsersDocuments", "rm.shareDocuments", "rm.shareFolders", "rm.shareImportedDocuments",
						"rm.shareImportedFolders", "rm.shareInactiveDocuments", "rm.shareInactiveFolders",
						"rm.shareSemiActiveDocuments", "rm.shareSemiActiveFolders", "rm.uploadInactiveDocuments",
						"rm.uploadSemiActiveDocuments", "rm.useCart", "tasks.manageWorkflows")));
	}
}
