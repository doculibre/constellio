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
						"rm.decommissioning", "rm.deleteInactiveDocuments", "rm.deleteInactiveFolders",
						"rm.deleteSemiActiveDocuments", "rm.deleteSemiActiveFolders", "rm.duplicateInactiveFolders",
						"rm.duplicateSemiActiveFolders", "rm.editDecommissioningList", "rm.manageClassificationPlan",
						"rm.manageContainers", "rm.manageDocumentAuthorizations", "rm.manageFolderAuthorizations",
						"rm.manageReports", "rm.manageRetentionRule", "rm.manageStorageSpaces", "rm.manageUniformSubdivisions",
						"rm.modifyFolderDecomDate", "rm.modifyImportedDocuments", "rm.modifyImportedFolders",
						"rm.modifyInactiveBorrowedFolder", "rm.modifyInactiveDocuments", "rm.modifyInactiveFolders",
						"rm.modifyOpeningDateFolder", "rm.modifySemiActiveBorrowedFolder", "rm.modifySemiActiveDocuments",
						"rm.modifySemiActiveFolders", "rm.processDecommissioningList", "rm.returnOtherUsersDocuments",
						"rm.shareDocuments", "rm.shareFolders", "rm.shareImportedDocuments", "rm.shareImportedFolders",
						"rm.shareInactiveDocuments", "rm.shareInactiveFolders", "rm.shareSemiActiveDocuments",
						"rm.shareSemiActiveFolders", "rm.uploadInactiveDocuments", "rm.uploadSemiActiveDocuments",
						"robots.manageRobots", "tasks.manageWorkflows")));
		rolesManager.updateRole(rolesManager.getRole(collection, "U").withNewPermissions(
				asList("rm.borrowFolder", "rm.createDocuments", "rm.createFolders", "rm.createSubFolders",
						"rm.deleteSemiActiveDocuments", "rm.deleteSemiActiveFolders", "rm.modifySemiActiveBorrowedFolder",
						"rm.shareDocuments", "rm.shareFolders", "rm.shareSemiActiveDocuments", "rm.shareSemiActiveFolders",
						"rm.uploadSemiActiveDocuments")));
		rolesManager.updateRole(rolesManager.getRole(collection, "M").withNewPermissions(
				asList("rm.borrowFolder", "rm.createDocuments", "rm.createFolders", "rm.createSubFolders", "rm.decommissioning",
						"rm.deleteSemiActiveDocuments", "rm.deleteSemiActiveFolders", "rm.manageContainers",
						"rm.manageDocumentAuthorizations", "rm.manageFolderAuthorizations", "rm.modifyOpeningDateFolder",
						"rm.modifySemiActiveBorrowedFolder", "rm.shareDocuments", "rm.shareFolders",
						"rm.shareSemiActiveDocuments", "rm.shareSemiActiveFolders", "rm.uploadSemiActiveDocuments")));
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
						"rm.decommissioning", "rm.deleteInactiveDocuments", "rm.deleteInactiveFolders",
						"rm.deleteSemiActiveDocuments", "rm.deleteSemiActiveFolders", "rm.duplicateInactiveFolders",
						"rm.duplicateSemiActiveFolders", "rm.editDecommissioningList", "rm.manageClassificationPlan",
						"rm.manageContainers", "rm.manageDocumentAuthorizations", "rm.manageFolderAuthorizations",
						"rm.manageReports", "rm.manageRetentionRule", "rm.manageStorageSpaces", "rm.manageUniformSubdivisions",
						"rm.modifyFolderDecomDate", "rm.modifyImportedDocuments", "rm.modifyImportedFolders",
						"rm.modifyInactiveBorrowedFolder", "rm.modifyInactiveDocuments", "rm.modifyInactiveFolders",
						"rm.modifyOpeningDateFolder", "rm.modifySemiActiveBorrowedFolder", "rm.modifySemiActiveDocuments",
						"rm.modifySemiActiveFolders", "rm.processDecommissioningList", "rm.returnOtherUsersDocuments",
						"rm.shareDocuments", "rm.shareFolders", "rm.shareImportedDocuments", "rm.shareImportedFolders",
						"rm.shareInactiveDocuments", "rm.shareInactiveFolders", "rm.shareSemiActiveDocuments",
						"rm.shareSemiActiveFolders", "rm.uploadInactiveDocuments", "rm.uploadSemiActiveDocuments",
						"tasks.manageWorkflows")));
	}
}
