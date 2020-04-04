package com.constellio.app.modules.complementary.esRmRobots.migrations;

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

import static java.util.Arrays.asList;

public final class GeneratedESRMRobotsMigrationCombo {
  String collection;

  AppLayerFactory appLayerFactory;

  MigrationResourcesProvider resourcesProvider;

  GeneratedESRMRobotsMigrationCombo(String collection, AppLayerFactory appLayerFactory, MigrationResourcesProvider resourcesProvider) {
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
    MetadataSchemaBuilder actionParameters_classifyConnectorFolderDirectlyInThePlanSchema = actionParametersSchemaType.createCustomSchema("classifyConnectorFolderDirectlyInThePlan");
    MetadataSchemaBuilder actionParameters_classifyConnectorFolderInParentFolderSchema = actionParametersSchemaType.createCustomSchema("classifyConnectorFolderInParentFolder");
    MetadataSchemaBuilder actionParameters_classifyConnectorTaxonomySchema = actionParametersSchemaType.createCustomSchema("classifyConnectorTaxonomy");
    actionParameters_classifyConnectorTaxonomySchema.defineValidators().add(ClassifyConnectorTaxonomyActionParametersValidator.class);
    MetadataSchemaBuilder actionParameters_classifySmbDocumentInFolderSchema = actionParametersSchemaType.createCustomSchema("classifySmbDocumentInFolder");
    MetadataSchemaBuilder actionParameters_classifySmbFolderInFolderSchema = actionParametersSchemaType.createCustomSchema("classifySmbFolderInFolder");
    MetadataSchemaBuilder actionParametersSchema = actionParametersSchemaType.getDefaultSchema();
    MetadataSchemaTypeBuilder administrativeUnitSchemaType = typesBuilder.getSchemaType("administrativeUnit");
    MetadataSchemaBuilder administrativeUnitSchema = administrativeUnitSchemaType.getDefaultSchema();
    MetadataSchemaTypeBuilder authorizationDetailsSchemaType = typesBuilder.getSchemaType("authorizationDetails");
    MetadataSchemaBuilder authorizationDetailsSchema = authorizationDetailsSchemaType.getDefaultSchema();
    MetadataSchemaTypeBuilder bagInfoSchemaType = typesBuilder.getSchemaType("bagInfo");
    MetadataSchemaBuilder bagInfoSchema = bagInfoSchemaType.getDefaultSchema();
    MetadataSchemaTypeBuilder capsuleSchemaType = typesBuilder.getSchemaType("capsule");
    MetadataSchemaBuilder capsuleSchema = capsuleSchemaType.getDefaultSchema();
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
    MetadataSchemaTypeBuilder ddvCapsuleLanguageSchemaType = typesBuilder.getSchemaType("ddvCapsuleLanguage");
    MetadataSchemaBuilder ddvCapsuleLanguageSchema = ddvCapsuleLanguageSchemaType.getDefaultSchema();
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
    MetadataSchemaTypeBuilder ddvUserFunctionSchemaType = typesBuilder.getSchemaType("ddvUserFunction");
    MetadataSchemaBuilder ddvUserFunctionSchema = ddvUserFunctionSchemaType.getDefaultSchema();
    MetadataSchemaTypeBuilder ddvVariablePeriodSchemaType = typesBuilder.getSchemaType("ddvVariablePeriod");
    MetadataSchemaBuilder ddvVariablePeriodSchema = ddvVariablePeriodSchemaType.getDefaultSchema();
    MetadataSchemaTypeBuilder ddvYearTypeSchemaType = typesBuilder.getSchemaType("ddvYearType");
    MetadataSchemaBuilder ddvYearTypeSchema = ddvYearTypeSchemaType.getDefaultSchema();
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
    MetadataSchemaTypeBuilder printableSchemaType = typesBuilder.getSchemaType("printable");
    MetadataSchemaBuilder printable_labelSchema = printableSchemaType.getCustomSchema("label");
    MetadataSchemaBuilder printable_reportSchema = printableSchemaType.getCustomSchema("report");
    MetadataSchemaBuilder printableSchema = printableSchemaType.getDefaultSchema();
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
    MetadataSchemaTypeBuilder searchEventSchemaType = typesBuilder.getSchemaType("searchEvent");
    MetadataSchemaBuilder searchEventSchema = searchEventSchemaType.getDefaultSchema();
    MetadataSchemaTypeBuilder storageSpaceSchemaType = typesBuilder.getSchemaType("storageSpace");
    MetadataSchemaBuilder storageSpaceSchema = storageSpaceSchemaType.getDefaultSchema();
    MetadataSchemaTypeBuilder taskSchemaType = typesBuilder.getSchemaType("task");
    MetadataSchemaBuilder task_approvalSchema = taskSchemaType.getCustomSchema("approval");
    MetadataSchemaBuilder taskSchema = taskSchemaType.getDefaultSchema();
    MetadataSchemaTypeBuilder temporaryRecordSchemaType = typesBuilder.getSchemaType("temporaryRecord");
    MetadataSchemaBuilder temporaryRecord_ConsolidatedPdfSchema = temporaryRecordSchemaType.getCustomSchema("ConsolidatedPdf");
    MetadataSchemaBuilder temporaryRecord_batchProcessReportSchema = temporaryRecordSchemaType.getCustomSchema("batchProcessReport");
    MetadataSchemaBuilder temporaryRecord_exportAuditSchema = temporaryRecordSchemaType.getCustomSchema("exportAudit");
    MetadataSchemaBuilder temporaryRecord_importAuditSchema = temporaryRecordSchemaType.getCustomSchema("importAudit");
    MetadataSchemaBuilder temporaryRecord_scriptReportSchema = temporaryRecordSchemaType.getCustomSchema("scriptReport");
    MetadataSchemaBuilder temporaryRecord_sipArchiveSchema = temporaryRecordSchemaType.getCustomSchema("sipArchive");
    MetadataSchemaBuilder temporaryRecord_vaultScanReportSchema = temporaryRecordSchemaType.getCustomSchema("vaultScanReport");
    MetadataSchemaBuilder temporaryRecordSchema = temporaryRecordSchemaType.getDefaultSchema();
    MetadataSchemaTypeBuilder thesaurusConfigSchemaType = typesBuilder.getSchemaType("thesaurusConfig");
    MetadataSchemaBuilder thesaurusConfigSchema = thesaurusConfigSchemaType.getDefaultSchema();
    MetadataSchemaTypeBuilder uniformSubdivisionSchemaType = typesBuilder.getSchemaType("uniformSubdivision");
    MetadataSchemaBuilder uniformSubdivisionSchema = uniformSubdivisionSchemaType.getDefaultSchema();
    MetadataSchemaTypeBuilder userDocumentSchemaType = typesBuilder.getSchemaType("userDocument");
    MetadataSchemaBuilder userDocumentSchema = userDocumentSchemaType.getDefaultSchema();
    MetadataSchemaTypeBuilder userFolderSchemaType = typesBuilder.getSchemaType("userFolder");
    MetadataSchemaBuilder userFolderSchema = userFolderSchemaType.getDefaultSchema();
    MetadataSchemaTypeBuilder userTaskSchemaType = typesBuilder.getSchemaType("userTask");
    MetadataSchemaBuilder userTask_borrowExtensionRequestSchema = userTaskSchemaType.getCustomSchema("borrowExtensionRequest");
    MetadataSchemaBuilder userTask_borrowRequestSchema = userTaskSchemaType.getCustomSchema("borrowRequest");
    MetadataSchemaBuilder userTask_reactivationRequestSchema = userTaskSchemaType.getCustomSchema("reactivationRequest");
    MetadataSchemaBuilder userTask_returnRequestSchema = userTaskSchemaType.getCustomSchema("returnRequest");
    MetadataSchemaBuilder userTaskSchema = userTaskSchemaType.getDefaultSchema();
    MetadataSchemaTypeBuilder workflowSchemaType = typesBuilder.getSchemaType("workflow");
    MetadataSchemaBuilder workflowSchema = workflowSchemaType.getDefaultSchema();
    MetadataSchemaTypeBuilder workflowInstanceSchemaType = typesBuilder.getSchemaType("workflowInstance");
    MetadataSchemaBuilder workflowInstanceSchema = workflowInstanceSchemaType.getDefaultSchema();
    createCollectionSchemaTypeMetadatas(typesBuilder,collectionSchemaType, collectionSchema);
    createGroupSchemaTypeMetadatas(typesBuilder,groupSchemaType, groupSchema);
    createUserSchemaTypeMetadatas(typesBuilder,userSchemaType, userSchema);
    createActionParametersSchemaTypeMetadatas(typesBuilder,actionParametersSchemaType, actionParameters_classifyConnectorFolderDirectlyInThePlanSchema, actionParameters_classifyConnectorFolderInParentFolderSchema, actionParameters_classifyConnectorTaxonomySchema, actionParameters_classifySmbDocumentInFolderSchema, actionParameters_classifySmbFolderInFolderSchema, actionParametersSchema);
    createAdministrativeUnitSchemaTypeMetadatas(typesBuilder,administrativeUnitSchemaType, administrativeUnitSchema);
    createAuthorizationDetailsSchemaTypeMetadatas(typesBuilder,authorizationDetailsSchemaType, authorizationDetailsSchema);
    createBagInfoSchemaTypeMetadatas(typesBuilder,bagInfoSchemaType, bagInfoSchema);
    createCapsuleSchemaTypeMetadatas(typesBuilder,capsuleSchemaType, capsuleSchema);
    createCartSchemaTypeMetadatas(typesBuilder,cartSchemaType, cartSchema);
    createCategorySchemaTypeMetadatas(typesBuilder,categorySchemaType, categorySchema);
    createConnectorHttpDocumentSchemaTypeMetadatas(typesBuilder,connectorHttpDocumentSchemaType, connectorHttpDocumentSchema);
    createConnectorInstanceSchemaTypeMetadatas(typesBuilder,connectorInstanceSchemaType, connectorInstance_httpSchema, connectorInstance_ldapSchema, connectorInstance_smbSchema, connectorInstanceSchema);
    createConnectorLdapUserDocumentSchemaTypeMetadatas(typesBuilder,connectorLdapUserDocumentSchemaType, connectorLdapUserDocumentSchema);
    createConnectorSmbDocumentSchemaTypeMetadatas(typesBuilder,connectorSmbDocumentSchemaType, connectorSmbDocumentSchema);
    createConnectorSmbFolderSchemaTypeMetadatas(typesBuilder,connectorSmbFolderSchemaType, connectorSmbFolderSchema);
    createConnectorTypeSchemaTypeMetadatas(typesBuilder,connectorTypeSchemaType, connectorTypeSchema);
    createContainerRecordSchemaTypeMetadatas(typesBuilder,containerRecordSchemaType, containerRecordSchema);
    createDdvCapsuleLanguageSchemaTypeMetadatas(typesBuilder,ddvCapsuleLanguageSchemaType, ddvCapsuleLanguageSchema);
    createDdvContainerRecordTypeSchemaTypeMetadatas(typesBuilder,ddvContainerRecordTypeSchemaType, ddvContainerRecordTypeSchema);
    createDdvDocumentTypeSchemaTypeMetadatas(typesBuilder,ddvDocumentTypeSchemaType, ddvDocumentTypeSchema);
    createDdvFolderTypeSchemaTypeMetadatas(typesBuilder,ddvFolderTypeSchemaType, ddvFolderTypeSchema);
    createDdvMediumTypeSchemaTypeMetadatas(typesBuilder,ddvMediumTypeSchemaType, ddvMediumTypeSchema);
    createDdvStorageSpaceTypeSchemaTypeMetadatas(typesBuilder,ddvStorageSpaceTypeSchemaType, ddvStorageSpaceTypeSchema);
    createDdvTaskStatusSchemaTypeMetadatas(typesBuilder,ddvTaskStatusSchemaType, ddvTaskStatusSchema);
    createDdvTaskTypeSchemaTypeMetadatas(typesBuilder,ddvTaskTypeSchemaType, ddvTaskTypeSchema);
    createDdvUserFunctionSchemaTypeMetadatas(typesBuilder,ddvUserFunctionSchemaType, ddvUserFunctionSchema);
    createDdvVariablePeriodSchemaTypeMetadatas(typesBuilder,ddvVariablePeriodSchemaType, ddvVariablePeriodSchema);
    createDdvYearTypeSchemaTypeMetadatas(typesBuilder,ddvYearTypeSchemaType, ddvYearTypeSchema);
    createDecommissioningListSchemaTypeMetadatas(typesBuilder,decommissioningListSchemaType, decommissioningListSchema);
    createDocumentSchemaTypeMetadatas(typesBuilder,documentSchemaType, document_emailSchema, documentSchema);
    createEmailToSendSchemaTypeMetadatas(typesBuilder,emailToSendSchemaType, emailToSendSchema);
    createEventSchemaTypeMetadatas(typesBuilder,eventSchemaType, eventSchema);
    createFacetSchemaTypeMetadatas(typesBuilder,facetSchemaType, facet_fieldSchema, facet_querySchema, facetSchema);
    createFilingSpaceSchemaTypeMetadatas(typesBuilder,filingSpaceSchemaType, filingSpaceSchema);
    createFolderSchemaTypeMetadatas(typesBuilder,folderSchemaType, folderSchema);
    createPrintableSchemaTypeMetadatas(typesBuilder,printableSchemaType, printable_labelSchema, printable_reportSchema, printableSchema);
    createReportSchemaTypeMetadatas(typesBuilder,reportSchemaType, reportSchema);
    createRetentionRuleSchemaTypeMetadatas(typesBuilder,retentionRuleSchemaType, retentionRuleSchema);
    createRobotSchemaTypeMetadatas(typesBuilder,robotSchemaType, robotSchema);
    createRobotLogSchemaTypeMetadatas(typesBuilder,robotLogSchemaType, robotLogSchema);
    createSavedSearchSchemaTypeMetadatas(typesBuilder,savedSearchSchemaType, savedSearchSchema);
    createSearchEventSchemaTypeMetadatas(typesBuilder,searchEventSchemaType, searchEventSchema);
    createStorageSpaceSchemaTypeMetadatas(typesBuilder,storageSpaceSchemaType, storageSpaceSchema);
    createTaskSchemaTypeMetadatas(typesBuilder,taskSchemaType, task_approvalSchema, taskSchema);
    createTemporaryRecordSchemaTypeMetadatas(typesBuilder,temporaryRecordSchemaType, temporaryRecord_ConsolidatedPdfSchema, temporaryRecord_batchProcessReportSchema, temporaryRecord_exportAuditSchema, temporaryRecord_importAuditSchema, temporaryRecord_scriptReportSchema, temporaryRecord_sipArchiveSchema, temporaryRecord_vaultScanReportSchema, temporaryRecordSchema);
    createThesaurusConfigSchemaTypeMetadatas(typesBuilder,thesaurusConfigSchemaType, thesaurusConfigSchema);
    createUniformSubdivisionSchemaTypeMetadatas(typesBuilder,uniformSubdivisionSchemaType, uniformSubdivisionSchema);
    createUserDocumentSchemaTypeMetadatas(typesBuilder,userDocumentSchemaType, userDocumentSchema);
    createUserFolderSchemaTypeMetadatas(typesBuilder,userFolderSchemaType, userFolderSchema);
    createUserTaskSchemaTypeMetadatas(typesBuilder,userTaskSchemaType, userTask_borrowExtensionRequestSchema, userTask_borrowRequestSchema, userTask_reactivationRequestSchema, userTask_returnRequestSchema, userTaskSchema);
    createWorkflowSchemaTypeMetadatas(typesBuilder,workflowSchemaType, workflowSchema);
    createWorkflowInstanceSchemaTypeMetadatas(typesBuilder,workflowInstanceSchemaType, workflowInstanceSchema);
  }

  private void createDdvTaskTypeSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder ddvTaskTypeSchemaType, MetadataSchemaBuilder ddvTaskTypeSchema) {
  }

  private void createConnectorSmbFolderSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder connectorSmbFolderSchemaType, MetadataSchemaBuilder connectorSmbFolderSchema) {
  }

  private void createCartSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder cartSchemaType, MetadataSchemaBuilder cartSchema) {
  }

  private void createDdvYearTypeSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder ddvYearTypeSchemaType, MetadataSchemaBuilder ddvYearTypeSchema) {
  }

  private void createRobotSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder robotSchemaType, MetadataSchemaBuilder robotSchema) {
  }

  private void createActionParametersSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder actionParametersSchemaType, MetadataSchemaBuilder actionParameters_classifyConnectorFolderDirectlyInThePlanSchema, MetadataSchemaBuilder actionParameters_classifyConnectorFolderInParentFolderSchema, MetadataSchemaBuilder actionParameters_classifyConnectorTaxonomySchema, MetadataSchemaBuilder actionParameters_classifySmbDocumentInFolderSchema, MetadataSchemaBuilder actionParameters_classifySmbFolderInFolderSchema, MetadataSchemaBuilder actionParametersSchema) {
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_actionAfterClassification = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.create("actionAfterClassification").setType(MetadataValueType.ENUM);
    actionParameters_classifyConnectorFolderDirectlyInThePlan_actionAfterClassification.setDefaultRequirement(true);
    actionParameters_classifyConnectorFolderDirectlyInThePlan_actionAfterClassification.setDefaultValue(ActionAfterClassification.DO_NOTHING);
    actionParameters_classifyConnectorFolderDirectlyInThePlan_actionAfterClassification.defineAsEnum(ActionAfterClassification.class);
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultAdminUnit = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.create("defaultAdminUnit").setType(MetadataValueType.REFERENCE);
    actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultAdminUnit.setDefaultRequirement(true);
    actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultAdminUnit.defineReferencesTo(types.getSchemaType("administrativeUnit"));
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCategory = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.create("defaultCategory").setType(MetadataValueType.REFERENCE);
    actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCategory.setDefaultRequirement(true);
    actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCategory.defineReferencesTo(types.getSchemaType("category"));
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCopyStatus = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.create("defaultCopyStatus").setType(MetadataValueType.ENUM);
    actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCopyStatus.setDefaultRequirement(true);
    actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCopyStatus.defineAsEnum(CopyType.class);
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultOpenDate = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.create("defaultOpenDate").setType(MetadataValueType.DATE);
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultRetentionRule = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.create("defaultRetentionRule").setType(MetadataValueType.REFERENCE);
    actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultRetentionRule.setDefaultRequirement(true);
    actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultRetentionRule.defineReferencesTo(types.getSchemaType("retentionRule"));
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultUniformSubdivision = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.create("defaultUniformSubdivision").setType(MetadataValueType.REFERENCE);
    actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultUniformSubdivision.defineReferencesTo(types.getSchemaType("uniformSubdivision"));
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_documentMapping = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.create("documentMapping").setType(MetadataValueType.CONTENT);
    actionParameters_classifyConnectorFolderDirectlyInThePlan_documentMapping.defineStructureFactory(ContentFactory.class);
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_documentType = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.create("documentType").setType(MetadataValueType.REFERENCE);
    actionParameters_classifyConnectorFolderDirectlyInThePlan_documentType.setUndeletable(true);
    actionParameters_classifyConnectorFolderDirectlyInThePlan_documentType.defineReferencesTo(types.getSchemaType("ddvDocumentType"));
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_folderMapping = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.create("folderMapping").setType(MetadataValueType.CONTENT);
    actionParameters_classifyConnectorFolderDirectlyInThePlan_folderMapping.defineStructureFactory(ContentFactory.class);
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_folderType = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.create("folderType").setType(MetadataValueType.REFERENCE);
    actionParameters_classifyConnectorFolderDirectlyInThePlan_folderType.setUndeletable(true);
    actionParameters_classifyConnectorFolderDirectlyInThePlan_folderType.defineReferencesTo(types.getSchemaType("ddvFolderType"));
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_actionAfterClassification = actionParameters_classifyConnectorFolderInParentFolderSchema.create("actionAfterClassification").setType(MetadataValueType.ENUM);
    actionParameters_classifyConnectorFolderInParentFolder_actionAfterClassification.setDefaultRequirement(true);
    actionParameters_classifyConnectorFolderInParentFolder_actionAfterClassification.setDefaultValue(ActionAfterClassification.DO_NOTHING);
    actionParameters_classifyConnectorFolderInParentFolder_actionAfterClassification.defineAsEnum(ActionAfterClassification.class);
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_defaultOpenDate = actionParameters_classifyConnectorFolderInParentFolderSchema.create("defaultOpenDate").setType(MetadataValueType.DATE);
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_defaultParentFolder = actionParameters_classifyConnectorFolderInParentFolderSchema.create("defaultParentFolder").setType(MetadataValueType.REFERENCE);
    actionParameters_classifyConnectorFolderInParentFolder_defaultParentFolder.setUndeletable(true);
    actionParameters_classifyConnectorFolderInParentFolder_defaultParentFolder.defineReferencesTo(types.getSchemaType("folder"));
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_documentMapping = actionParameters_classifyConnectorFolderInParentFolderSchema.create("documentMapping").setType(MetadataValueType.CONTENT);
    actionParameters_classifyConnectorFolderInParentFolder_documentMapping.defineStructureFactory(ContentFactory.class);
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_documentType = actionParameters_classifyConnectorFolderInParentFolderSchema.create("documentType").setType(MetadataValueType.REFERENCE);
    actionParameters_classifyConnectorFolderInParentFolder_documentType.setUndeletable(true);
    actionParameters_classifyConnectorFolderInParentFolder_documentType.defineReferencesTo(types.getSchemaType("ddvDocumentType"));
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_folderMapping = actionParameters_classifyConnectorFolderInParentFolderSchema.create("folderMapping").setType(MetadataValueType.CONTENT);
    actionParameters_classifyConnectorFolderInParentFolder_folderMapping.defineStructureFactory(ContentFactory.class);
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_folderType = actionParameters_classifyConnectorFolderInParentFolderSchema.create("folderType").setType(MetadataValueType.REFERENCE);
    actionParameters_classifyConnectorFolderInParentFolder_folderType.setUndeletable(true);
    actionParameters_classifyConnectorFolderInParentFolder_folderType.defineReferencesTo(types.getSchemaType("ddvFolderType"));
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_actionAfterClassification = actionParameters_classifyConnectorTaxonomySchema.create("actionAfterClassification").setType(MetadataValueType.ENUM);
    actionParameters_classifyConnectorTaxonomy_actionAfterClassification.setDefaultRequirement(true);
    actionParameters_classifyConnectorTaxonomy_actionAfterClassification.setDefaultValue(ActionAfterClassification.DO_NOTHING);
    actionParameters_classifyConnectorTaxonomy_actionAfterClassification.defineAsEnum(ActionAfterClassification.class);
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_defaultAdminUnit = actionParameters_classifyConnectorTaxonomySchema.create("defaultAdminUnit").setType(MetadataValueType.REFERENCE);
    actionParameters_classifyConnectorTaxonomy_defaultAdminUnit.defineReferencesTo(types.getSchemaType("administrativeUnit"));
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_defaultCategory = actionParameters_classifyConnectorTaxonomySchema.create("defaultCategory").setType(MetadataValueType.REFERENCE);
    actionParameters_classifyConnectorTaxonomy_defaultCategory.defineReferencesTo(types.getSchemaType("category"));
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_defaultCopyStatus = actionParameters_classifyConnectorTaxonomySchema.create("defaultCopyStatus").setType(MetadataValueType.ENUM);
    actionParameters_classifyConnectorTaxonomy_defaultCopyStatus.defineAsEnum(CopyType.class);
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_defaultOpenDate = actionParameters_classifyConnectorTaxonomySchema.create("defaultOpenDate").setType(MetadataValueType.DATE);
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_defaultParentFolder = actionParameters_classifyConnectorTaxonomySchema.create("defaultParentFolder").setType(MetadataValueType.REFERENCE);
    actionParameters_classifyConnectorTaxonomy_defaultParentFolder.setUndeletable(true);
    actionParameters_classifyConnectorTaxonomy_defaultParentFolder.defineReferencesTo(types.getSchemaType("folder"));
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_defaultRetentionRule = actionParameters_classifyConnectorTaxonomySchema.create("defaultRetentionRule").setType(MetadataValueType.REFERENCE);
    actionParameters_classifyConnectorTaxonomy_defaultRetentionRule.defineReferencesTo(types.getSchemaType("retentionRule"));
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_defaultUniformSubdivision = actionParameters_classifyConnectorTaxonomySchema.create("defaultUniformSubdivision").setType(MetadataValueType.REFERENCE);
    actionParameters_classifyConnectorTaxonomy_defaultUniformSubdivision.defineReferencesTo(types.getSchemaType("uniformSubdivision"));
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_delimiter = actionParameters_classifyConnectorTaxonomySchema.create("delimiter").setType(MetadataValueType.STRING);
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_documentMapping = actionParameters_classifyConnectorTaxonomySchema.create("documentMapping").setType(MetadataValueType.CONTENT);
    actionParameters_classifyConnectorTaxonomy_documentMapping.defineStructureFactory(ContentFactory.class);
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_documentType = actionParameters_classifyConnectorTaxonomySchema.create("documentType").setType(MetadataValueType.REFERENCE);
    actionParameters_classifyConnectorTaxonomy_documentType.setUndeletable(true);
    actionParameters_classifyConnectorTaxonomy_documentType.defineReferencesTo(types.getSchemaType("ddvDocumentType"));
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_folderMapping = actionParameters_classifyConnectorTaxonomySchema.create("folderMapping").setType(MetadataValueType.CONTENT);
    actionParameters_classifyConnectorTaxonomy_folderMapping.defineStructureFactory(ContentFactory.class);
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_folderType = actionParameters_classifyConnectorTaxonomySchema.create("folderType").setType(MetadataValueType.REFERENCE);
    actionParameters_classifyConnectorTaxonomy_folderType.setUndeletable(true);
    actionParameters_classifyConnectorTaxonomy_folderType.defineReferencesTo(types.getSchemaType("ddvFolderType"));
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_inTaxonomy = actionParameters_classifyConnectorTaxonomySchema.create("inTaxonomy").setType(MetadataValueType.STRING);
    actionParameters_classifyConnectorTaxonomy_inTaxonomy.setDefaultRequirement(true);
    actionParameters_classifyConnectorTaxonomy_inTaxonomy.setDefaultValue("admUnits");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_pathPrefix = actionParameters_classifyConnectorTaxonomySchema.create("pathPrefix").setType(MetadataValueType.STRING);
    actionParameters_classifyConnectorTaxonomy_pathPrefix.setDefaultRequirement(true);
    actionParameters_classifyConnectorTaxonomy_pathPrefix.setDefaultValue("smb://");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_actionAfterClassification = actionParameters_classifySmbDocumentInFolderSchema.create("actionAfterClassification").setType(MetadataValueType.ENUM);
    actionParameters_classifySmbDocumentInFolder_actionAfterClassification.setDefaultRequirement(true);
    actionParameters_classifySmbDocumentInFolder_actionAfterClassification.setUndeletable(true);
    actionParameters_classifySmbDocumentInFolder_actionAfterClassification.setDefaultValue(ActionAfterClassification.DO_NOTHING);
    actionParameters_classifySmbDocumentInFolder_actionAfterClassification.defineAsEnum(ActionAfterClassification.class);
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_documentType = actionParameters_classifySmbDocumentInFolderSchema.create("documentType").setType(MetadataValueType.REFERENCE);
    actionParameters_classifySmbDocumentInFolder_documentType.setUndeletable(true);
    actionParameters_classifySmbDocumentInFolder_documentType.defineReferencesTo(types.getSchemaType("ddvDocumentType"));
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_inFolder = actionParameters_classifySmbDocumentInFolderSchema.create("inFolder").setType(MetadataValueType.REFERENCE);
    actionParameters_classifySmbDocumentInFolder_inFolder.setDefaultRequirement(true);
    actionParameters_classifySmbDocumentInFolder_inFolder.defineReferencesTo(types.getSchemaType("folder"));
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_majorVersions = actionParameters_classifySmbDocumentInFolderSchema.create("majorVersions").setType(MetadataValueType.BOOLEAN);
    actionParameters_classifySmbDocumentInFolder_majorVersions.setDefaultRequirement(true);
    actionParameters_classifySmbDocumentInFolder_majorVersions.setDefaultValue(true);
    MetadataBuilder actionParameters_classifySmbFolderInFolder_inFolder = actionParameters_classifySmbFolderInFolderSchema.create("inFolder").setType(MetadataValueType.REFERENCE);
    actionParameters_classifySmbFolderInFolder_inFolder.setDefaultRequirement(true);
    actionParameters_classifySmbFolderInFolder_inFolder.defineReferencesTo(types.getSchemaType("folder"));
    MetadataBuilder actionParameters_classifySmbFolderInFolder_majorVersions = actionParameters_classifySmbFolderInFolderSchema.create("majorVersions").setType(MetadataValueType.BOOLEAN);
    actionParameters_classifySmbFolderInFolder_majorVersions.setDefaultRequirement(true);
    actionParameters_classifySmbFolderInFolder_majorVersions.setDefaultValue(true);
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_allReferences = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("allReferences");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_allRemovedAuths = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("allRemovedAuths");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_attachedAncestors = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("attachedAncestors");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_autocomplete = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("autocomplete");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_caption = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("caption");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_createdBy = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("createdBy");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_createdOn = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("createdOn");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_deleted = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("deleted");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_denyTokens = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("denyTokens");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_detachedauthorizations = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("detachedauthorizations");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_errorOnPhysicalDeletion = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_estimatedSize = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("estimatedSize");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_hidden = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("hidden");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_id = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("id");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_legacyIdentifier = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("legacyIdentifier");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_logicallyDeletedOn = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("logicallyDeletedOn");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_manualTokens = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("manualTokens");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_markedForParsing = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("markedForParsing");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_markedForPreviewConversion = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("markedForPreviewConversion");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_markedForReindexing = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("markedForReindexing");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_migrationDataVersion = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("migrationDataVersion");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_modifiedBy = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("modifiedBy");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_modifiedOn = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("modifiedOn");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_path = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("path");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_pathParts = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("pathParts");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_principalpath = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("principalpath");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_removedauthorizations = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("removedauthorizations");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_schema = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("schema");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_shareDenyTokens = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("shareDenyTokens");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_shareTokens = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("shareTokens");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_title = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("title");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_tokens = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("tokens");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_tokensHierarchy = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("tokensHierarchy");
    MetadataBuilder actionParameters_classifyConnectorFolderDirectlyInThePlan_visibleInTrees = actionParameters_classifyConnectorFolderDirectlyInThePlanSchema.get("visibleInTrees");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_allReferences = actionParameters_classifyConnectorFolderInParentFolderSchema.get("allReferences");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_allRemovedAuths = actionParameters_classifyConnectorFolderInParentFolderSchema.get("allRemovedAuths");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_attachedAncestors = actionParameters_classifyConnectorFolderInParentFolderSchema.get("attachedAncestors");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_autocomplete = actionParameters_classifyConnectorFolderInParentFolderSchema.get("autocomplete");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_caption = actionParameters_classifyConnectorFolderInParentFolderSchema.get("caption");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_createdBy = actionParameters_classifyConnectorFolderInParentFolderSchema.get("createdBy");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_createdOn = actionParameters_classifyConnectorFolderInParentFolderSchema.get("createdOn");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_deleted = actionParameters_classifyConnectorFolderInParentFolderSchema.get("deleted");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_denyTokens = actionParameters_classifyConnectorFolderInParentFolderSchema.get("denyTokens");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_detachedauthorizations = actionParameters_classifyConnectorFolderInParentFolderSchema.get("detachedauthorizations");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_errorOnPhysicalDeletion = actionParameters_classifyConnectorFolderInParentFolderSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_estimatedSize = actionParameters_classifyConnectorFolderInParentFolderSchema.get("estimatedSize");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_hidden = actionParameters_classifyConnectorFolderInParentFolderSchema.get("hidden");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_id = actionParameters_classifyConnectorFolderInParentFolderSchema.get("id");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_legacyIdentifier = actionParameters_classifyConnectorFolderInParentFolderSchema.get("legacyIdentifier");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_logicallyDeletedOn = actionParameters_classifyConnectorFolderInParentFolderSchema.get("logicallyDeletedOn");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_manualTokens = actionParameters_classifyConnectorFolderInParentFolderSchema.get("manualTokens");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_markedForParsing = actionParameters_classifyConnectorFolderInParentFolderSchema.get("markedForParsing");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_markedForPreviewConversion = actionParameters_classifyConnectorFolderInParentFolderSchema.get("markedForPreviewConversion");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_markedForReindexing = actionParameters_classifyConnectorFolderInParentFolderSchema.get("markedForReindexing");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_migrationDataVersion = actionParameters_classifyConnectorFolderInParentFolderSchema.get("migrationDataVersion");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_modifiedBy = actionParameters_classifyConnectorFolderInParentFolderSchema.get("modifiedBy");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_modifiedOn = actionParameters_classifyConnectorFolderInParentFolderSchema.get("modifiedOn");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_path = actionParameters_classifyConnectorFolderInParentFolderSchema.get("path");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_pathParts = actionParameters_classifyConnectorFolderInParentFolderSchema.get("pathParts");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_principalpath = actionParameters_classifyConnectorFolderInParentFolderSchema.get("principalpath");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_removedauthorizations = actionParameters_classifyConnectorFolderInParentFolderSchema.get("removedauthorizations");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_schema = actionParameters_classifyConnectorFolderInParentFolderSchema.get("schema");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_shareDenyTokens = actionParameters_classifyConnectorFolderInParentFolderSchema.get("shareDenyTokens");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_shareTokens = actionParameters_classifyConnectorFolderInParentFolderSchema.get("shareTokens");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_title = actionParameters_classifyConnectorFolderInParentFolderSchema.get("title");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_tokens = actionParameters_classifyConnectorFolderInParentFolderSchema.get("tokens");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_tokensHierarchy = actionParameters_classifyConnectorFolderInParentFolderSchema.get("tokensHierarchy");
    MetadataBuilder actionParameters_classifyConnectorFolderInParentFolder_visibleInTrees = actionParameters_classifyConnectorFolderInParentFolderSchema.get("visibleInTrees");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_allReferences = actionParameters_classifyConnectorTaxonomySchema.get("allReferences");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_allRemovedAuths = actionParameters_classifyConnectorTaxonomySchema.get("allRemovedAuths");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_attachedAncestors = actionParameters_classifyConnectorTaxonomySchema.get("attachedAncestors");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_autocomplete = actionParameters_classifyConnectorTaxonomySchema.get("autocomplete");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_caption = actionParameters_classifyConnectorTaxonomySchema.get("caption");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_createdBy = actionParameters_classifyConnectorTaxonomySchema.get("createdBy");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_createdOn = actionParameters_classifyConnectorTaxonomySchema.get("createdOn");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_deleted = actionParameters_classifyConnectorTaxonomySchema.get("deleted");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_denyTokens = actionParameters_classifyConnectorTaxonomySchema.get("denyTokens");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_detachedauthorizations = actionParameters_classifyConnectorTaxonomySchema.get("detachedauthorizations");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_errorOnPhysicalDeletion = actionParameters_classifyConnectorTaxonomySchema.get("errorOnPhysicalDeletion");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_estimatedSize = actionParameters_classifyConnectorTaxonomySchema.get("estimatedSize");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_hidden = actionParameters_classifyConnectorTaxonomySchema.get("hidden");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_id = actionParameters_classifyConnectorTaxonomySchema.get("id");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_legacyIdentifier = actionParameters_classifyConnectorTaxonomySchema.get("legacyIdentifier");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_logicallyDeletedOn = actionParameters_classifyConnectorTaxonomySchema.get("logicallyDeletedOn");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_manualTokens = actionParameters_classifyConnectorTaxonomySchema.get("manualTokens");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_markedForParsing = actionParameters_classifyConnectorTaxonomySchema.get("markedForParsing");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_markedForPreviewConversion = actionParameters_classifyConnectorTaxonomySchema.get("markedForPreviewConversion");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_markedForReindexing = actionParameters_classifyConnectorTaxonomySchema.get("markedForReindexing");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_migrationDataVersion = actionParameters_classifyConnectorTaxonomySchema.get("migrationDataVersion");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_modifiedBy = actionParameters_classifyConnectorTaxonomySchema.get("modifiedBy");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_modifiedOn = actionParameters_classifyConnectorTaxonomySchema.get("modifiedOn");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_path = actionParameters_classifyConnectorTaxonomySchema.get("path");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_pathParts = actionParameters_classifyConnectorTaxonomySchema.get("pathParts");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_principalpath = actionParameters_classifyConnectorTaxonomySchema.get("principalpath");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_removedauthorizations = actionParameters_classifyConnectorTaxonomySchema.get("removedauthorizations");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_schema = actionParameters_classifyConnectorTaxonomySchema.get("schema");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_shareDenyTokens = actionParameters_classifyConnectorTaxonomySchema.get("shareDenyTokens");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_shareTokens = actionParameters_classifyConnectorTaxonomySchema.get("shareTokens");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_title = actionParameters_classifyConnectorTaxonomySchema.get("title");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_tokens = actionParameters_classifyConnectorTaxonomySchema.get("tokens");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_tokensHierarchy = actionParameters_classifyConnectorTaxonomySchema.get("tokensHierarchy");
    MetadataBuilder actionParameters_classifyConnectorTaxonomy_visibleInTrees = actionParameters_classifyConnectorTaxonomySchema.get("visibleInTrees");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_allReferences = actionParameters_classifySmbDocumentInFolderSchema.get("allReferences");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_allRemovedAuths = actionParameters_classifySmbDocumentInFolderSchema.get("allRemovedAuths");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_attachedAncestors = actionParameters_classifySmbDocumentInFolderSchema.get("attachedAncestors");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_autocomplete = actionParameters_classifySmbDocumentInFolderSchema.get("autocomplete");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_caption = actionParameters_classifySmbDocumentInFolderSchema.get("caption");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_createdBy = actionParameters_classifySmbDocumentInFolderSchema.get("createdBy");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_createdOn = actionParameters_classifySmbDocumentInFolderSchema.get("createdOn");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_deleted = actionParameters_classifySmbDocumentInFolderSchema.get("deleted");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_denyTokens = actionParameters_classifySmbDocumentInFolderSchema.get("denyTokens");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_detachedauthorizations = actionParameters_classifySmbDocumentInFolderSchema.get("detachedauthorizations");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_errorOnPhysicalDeletion = actionParameters_classifySmbDocumentInFolderSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_estimatedSize = actionParameters_classifySmbDocumentInFolderSchema.get("estimatedSize");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_hidden = actionParameters_classifySmbDocumentInFolderSchema.get("hidden");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_id = actionParameters_classifySmbDocumentInFolderSchema.get("id");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_legacyIdentifier = actionParameters_classifySmbDocumentInFolderSchema.get("legacyIdentifier");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_logicallyDeletedOn = actionParameters_classifySmbDocumentInFolderSchema.get("logicallyDeletedOn");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_manualTokens = actionParameters_classifySmbDocumentInFolderSchema.get("manualTokens");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_markedForParsing = actionParameters_classifySmbDocumentInFolderSchema.get("markedForParsing");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_markedForPreviewConversion = actionParameters_classifySmbDocumentInFolderSchema.get("markedForPreviewConversion");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_markedForReindexing = actionParameters_classifySmbDocumentInFolderSchema.get("markedForReindexing");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_migrationDataVersion = actionParameters_classifySmbDocumentInFolderSchema.get("migrationDataVersion");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_modifiedBy = actionParameters_classifySmbDocumentInFolderSchema.get("modifiedBy");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_modifiedOn = actionParameters_classifySmbDocumentInFolderSchema.get("modifiedOn");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_path = actionParameters_classifySmbDocumentInFolderSchema.get("path");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_pathParts = actionParameters_classifySmbDocumentInFolderSchema.get("pathParts");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_principalpath = actionParameters_classifySmbDocumentInFolderSchema.get("principalpath");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_removedauthorizations = actionParameters_classifySmbDocumentInFolderSchema.get("removedauthorizations");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_schema = actionParameters_classifySmbDocumentInFolderSchema.get("schema");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_shareDenyTokens = actionParameters_classifySmbDocumentInFolderSchema.get("shareDenyTokens");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_shareTokens = actionParameters_classifySmbDocumentInFolderSchema.get("shareTokens");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_title = actionParameters_classifySmbDocumentInFolderSchema.get("title");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_tokens = actionParameters_classifySmbDocumentInFolderSchema.get("tokens");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_tokensHierarchy = actionParameters_classifySmbDocumentInFolderSchema.get("tokensHierarchy");
    MetadataBuilder actionParameters_classifySmbDocumentInFolder_visibleInTrees = actionParameters_classifySmbDocumentInFolderSchema.get("visibleInTrees");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_allReferences = actionParameters_classifySmbFolderInFolderSchema.get("allReferences");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_allRemovedAuths = actionParameters_classifySmbFolderInFolderSchema.get("allRemovedAuths");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_attachedAncestors = actionParameters_classifySmbFolderInFolderSchema.get("attachedAncestors");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_autocomplete = actionParameters_classifySmbFolderInFolderSchema.get("autocomplete");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_caption = actionParameters_classifySmbFolderInFolderSchema.get("caption");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_createdBy = actionParameters_classifySmbFolderInFolderSchema.get("createdBy");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_createdOn = actionParameters_classifySmbFolderInFolderSchema.get("createdOn");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_deleted = actionParameters_classifySmbFolderInFolderSchema.get("deleted");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_denyTokens = actionParameters_classifySmbFolderInFolderSchema.get("denyTokens");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_detachedauthorizations = actionParameters_classifySmbFolderInFolderSchema.get("detachedauthorizations");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_errorOnPhysicalDeletion = actionParameters_classifySmbFolderInFolderSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_estimatedSize = actionParameters_classifySmbFolderInFolderSchema.get("estimatedSize");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_hidden = actionParameters_classifySmbFolderInFolderSchema.get("hidden");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_id = actionParameters_classifySmbFolderInFolderSchema.get("id");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_legacyIdentifier = actionParameters_classifySmbFolderInFolderSchema.get("legacyIdentifier");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_logicallyDeletedOn = actionParameters_classifySmbFolderInFolderSchema.get("logicallyDeletedOn");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_manualTokens = actionParameters_classifySmbFolderInFolderSchema.get("manualTokens");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_markedForParsing = actionParameters_classifySmbFolderInFolderSchema.get("markedForParsing");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_markedForPreviewConversion = actionParameters_classifySmbFolderInFolderSchema.get("markedForPreviewConversion");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_markedForReindexing = actionParameters_classifySmbFolderInFolderSchema.get("markedForReindexing");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_migrationDataVersion = actionParameters_classifySmbFolderInFolderSchema.get("migrationDataVersion");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_modifiedBy = actionParameters_classifySmbFolderInFolderSchema.get("modifiedBy");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_modifiedOn = actionParameters_classifySmbFolderInFolderSchema.get("modifiedOn");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_path = actionParameters_classifySmbFolderInFolderSchema.get("path");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_pathParts = actionParameters_classifySmbFolderInFolderSchema.get("pathParts");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_principalpath = actionParameters_classifySmbFolderInFolderSchema.get("principalpath");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_removedauthorizations = actionParameters_classifySmbFolderInFolderSchema.get("removedauthorizations");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_schema = actionParameters_classifySmbFolderInFolderSchema.get("schema");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_shareDenyTokens = actionParameters_classifySmbFolderInFolderSchema.get("shareDenyTokens");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_shareTokens = actionParameters_classifySmbFolderInFolderSchema.get("shareTokens");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_title = actionParameters_classifySmbFolderInFolderSchema.get("title");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_tokens = actionParameters_classifySmbFolderInFolderSchema.get("tokens");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_tokensHierarchy = actionParameters_classifySmbFolderInFolderSchema.get("tokensHierarchy");
    MetadataBuilder actionParameters_classifySmbFolderInFolder_visibleInTrees = actionParameters_classifySmbFolderInFolderSchema.get("visibleInTrees");
  }

  private void createUserDocumentSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder userDocumentSchemaType, MetadataSchemaBuilder userDocumentSchema) {
  }

  private void createDdvCapsuleLanguageSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder ddvCapsuleLanguageSchemaType, MetadataSchemaBuilder ddvCapsuleLanguageSchema) {
  }

  private void createStorageSpaceSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder storageSpaceSchemaType, MetadataSchemaBuilder storageSpaceSchema) {
  }

  private void createGroupSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder groupSchemaType, MetadataSchemaBuilder groupSchema) {
  }

  private void createWorkflowInstanceSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder workflowInstanceSchemaType, MetadataSchemaBuilder workflowInstanceSchema) {
  }

  private void createBagInfoSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder bagInfoSchemaType, MetadataSchemaBuilder bagInfoSchema) {
  }

  private void createDdvMediumTypeSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder ddvMediumTypeSchemaType, MetadataSchemaBuilder ddvMediumTypeSchema) {
  }

  private void createFilingSpaceSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder filingSpaceSchemaType, MetadataSchemaBuilder filingSpaceSchema) {
  }

  private void createWorkflowSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder workflowSchemaType, MetadataSchemaBuilder workflowSchema) {
  }

  private void createDdvFolderTypeSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder ddvFolderTypeSchemaType, MetadataSchemaBuilder ddvFolderTypeSchema) {
  }

  private void createCollectionSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder collectionSchemaType, MetadataSchemaBuilder collectionSchema) {
  }

  private void createConnectorHttpDocumentSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder connectorHttpDocumentSchemaType, MetadataSchemaBuilder connectorHttpDocumentSchema) {
  }

  private void createUniformSubdivisionSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder uniformSubdivisionSchemaType, MetadataSchemaBuilder uniformSubdivisionSchema) {
  }

  private void createAuthorizationDetailsSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder authorizationDetailsSchemaType, MetadataSchemaBuilder authorizationDetailsSchema) {
  }

  private void createAdministrativeUnitSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder administrativeUnitSchemaType, MetadataSchemaBuilder administrativeUnitSchema) {
  }

  private void createDdvDocumentTypeSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder ddvDocumentTypeSchemaType, MetadataSchemaBuilder ddvDocumentTypeSchema) {
  }

  private void createFolderSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder folderSchemaType, MetadataSchemaBuilder folderSchema) {
    MetadataBuilder folder_createdByRobot = folderSchema.create("createdByRobot").setType(MetadataValueType.STRING);
    folder_createdByRobot.setSystemReserved(true);
    folder_createdByRobot.setUndeletable(true);
  }

  private void createTaskSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder taskSchemaType, MetadataSchemaBuilder task_approvalSchema, MetadataSchemaBuilder taskSchema) {
    MetadataBuilder task_approval_allReferences = task_approvalSchema.get("allReferences");
    MetadataBuilder task_approval_allRemovedAuths = task_approvalSchema.get("allRemovedAuths");
    MetadataBuilder task_approval_assignCandidates = task_approvalSchema.get("assignCandidates");
    MetadataBuilder task_approval_assignedOn = task_approvalSchema.get("assignedOn");
    MetadataBuilder task_approval_assignedTo = task_approvalSchema.get("assignedTo");
    MetadataBuilder task_approval_attachedAncestors = task_approvalSchema.get("attachedAncestors");
    MetadataBuilder task_approval_autocomplete = task_approvalSchema.get("autocomplete");
    MetadataBuilder task_approval_caption = task_approvalSchema.get("caption");
    MetadataBuilder task_approval_createdBy = task_approvalSchema.get("createdBy");
    MetadataBuilder task_approval_createdOn = task_approvalSchema.get("createdOn");
    MetadataBuilder task_approval_deleted = task_approvalSchema.get("deleted");
    MetadataBuilder task_approval_denyTokens = task_approvalSchema.get("denyTokens");
    MetadataBuilder task_approval_detachedauthorizations = task_approvalSchema.get("detachedauthorizations");
    MetadataBuilder task_approval_dueDate = task_approvalSchema.get("dueDate");
    MetadataBuilder task_approval_errorOnPhysicalDeletion = task_approvalSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder task_approval_estimatedSize = task_approvalSchema.get("estimatedSize");
    MetadataBuilder task_approval_finishedBy = task_approvalSchema.get("finishedBy");
    MetadataBuilder task_approval_finishedOn = task_approvalSchema.get("finishedOn");
    MetadataBuilder task_approval_hidden = task_approvalSchema.get("hidden");
    MetadataBuilder task_approval_id = task_approvalSchema.get("id");
    MetadataBuilder task_approval_legacyIdentifier = task_approvalSchema.get("legacyIdentifier");
    MetadataBuilder task_approval_logicallyDeletedOn = task_approvalSchema.get("logicallyDeletedOn");
    MetadataBuilder task_approval_manualTokens = task_approvalSchema.get("manualTokens");
    MetadataBuilder task_approval_markedForParsing = task_approvalSchema.get("markedForParsing");
    MetadataBuilder task_approval_markedForPreviewConversion = task_approvalSchema.get("markedForPreviewConversion");
    MetadataBuilder task_approval_markedForReindexing = task_approvalSchema.get("markedForReindexing");
    MetadataBuilder task_approval_migrationDataVersion = task_approvalSchema.get("migrationDataVersion");
    MetadataBuilder task_approval_modifiedBy = task_approvalSchema.get("modifiedBy");
    MetadataBuilder task_approval_modifiedOn = task_approvalSchema.get("modifiedOn");
    MetadataBuilder task_approval_path = task_approvalSchema.get("path");
    MetadataBuilder task_approval_pathParts = task_approvalSchema.get("pathParts");
    MetadataBuilder task_approval_principalpath = task_approvalSchema.get("principalpath");
    MetadataBuilder task_approval_removedauthorizations = task_approvalSchema.get("removedauthorizations");
    MetadataBuilder task_approval_schema = task_approvalSchema.get("schema");
    MetadataBuilder task_approval_shareDenyTokens = task_approvalSchema.get("shareDenyTokens");
    MetadataBuilder task_approval_shareTokens = task_approvalSchema.get("shareTokens");
    MetadataBuilder task_approval_title = task_approvalSchema.get("title");
    MetadataBuilder task_approval_tokens = task_approvalSchema.get("tokens");
    MetadataBuilder task_approval_tokensHierarchy = task_approvalSchema.get("tokensHierarchy");
    MetadataBuilder task_approval_visibleInTrees = task_approvalSchema.get("visibleInTrees");
    MetadataBuilder task_approval_workflowIdentifier = task_approvalSchema.get("workflowIdentifier");
    MetadataBuilder task_approval_workflowRecordIdentifiers = task_approvalSchema.get("workflowRecordIdentifiers");
  }

  private void createContainerRecordSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder containerRecordSchemaType, MetadataSchemaBuilder containerRecordSchema) {
  }

  private void createSearchEventSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder searchEventSchemaType, MetadataSchemaBuilder searchEventSchema) {
  }

  private void createRetentionRuleSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder retentionRuleSchemaType, MetadataSchemaBuilder retentionRuleSchema) {
  }

  private void createConnectorSmbDocumentSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder connectorSmbDocumentSchemaType, MetadataSchemaBuilder connectorSmbDocumentSchema) {
  }

  private void createCapsuleSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder capsuleSchemaType, MetadataSchemaBuilder capsuleSchema) {
  }

  private void createDocumentSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder documentSchemaType, MetadataSchemaBuilder document_emailSchema, MetadataSchemaBuilder documentSchema) {
    MetadataBuilder document_createdByRobot = documentSchema.create("createdByRobot").setType(MetadataValueType.STRING);
    document_createdByRobot.setSystemReserved(true);
    document_createdByRobot.setUndeletable(true);
    MetadataBuilder document_email_actualDepositDate = document_emailSchema.get("actualDepositDate");
    MetadataBuilder document_email_actualDepositDateEntered = document_emailSchema.get("actualDepositDateEntered");
    MetadataBuilder document_email_actualDestructionDate = document_emailSchema.get("actualDestructionDate");
    MetadataBuilder document_email_actualDestructionDateEntered = document_emailSchema.get("actualDestructionDateEntered");
    MetadataBuilder document_email_actualTransferDate = document_emailSchema.get("actualTransferDate");
    MetadataBuilder document_email_actualTransferDateEntered = document_emailSchema.get("actualTransferDateEntered");
    MetadataBuilder document_email_administrativeUnit = document_emailSchema.get("administrativeUnit");
    MetadataBuilder document_email_alertUsersWhenAvailable = document_emailSchema.get("alertUsersWhenAvailable");
    MetadataBuilder document_email_allReferences = document_emailSchema.get("allReferences");
    MetadataBuilder document_email_allRemovedAuths = document_emailSchema.get("allRemovedAuths");
    MetadataBuilder document_email_applicableCopyRule = document_emailSchema.get("applicableCopyRule");
    MetadataBuilder document_email_archivisticStatus = document_emailSchema.get("archivisticStatus");
    MetadataBuilder document_email_attachedAncestors = document_emailSchema.get("attachedAncestors");
    MetadataBuilder document_email_author = document_emailSchema.get("author");
    MetadataBuilder document_email_autocomplete = document_emailSchema.get("autocomplete");
    MetadataBuilder document_email_borrowed = document_emailSchema.get("borrowed");
    MetadataBuilder document_email_caption = document_emailSchema.get("caption");
    MetadataBuilder document_email_category = document_emailSchema.get("category");
    MetadataBuilder document_email_categoryCode = document_emailSchema.get("categoryCode");
    MetadataBuilder document_email_closingDate = document_emailSchema.get("closingDate");
    MetadataBuilder document_email_comments = document_emailSchema.get("comments");
    MetadataBuilder document_email_company = document_emailSchema.get("company");
    MetadataBuilder document_email_confidential = document_emailSchema.get("confidential");
    MetadataBuilder document_email_content = document_emailSchema.get("content");
    MetadataBuilder document_email_contentCheckedOutBy = document_emailSchema.get("contentCheckedOutBy");
    MetadataBuilder document_email_contentCheckedOutDate = document_emailSchema.get("contentCheckedOutDate");
    MetadataBuilder document_email_contentHashes = document_emailSchema.get("contentHashes");
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
    MetadataBuilder document_email_essential = document_emailSchema.get("essential");
    MetadataBuilder document_email_estimatedSize = document_emailSchema.get("estimatedSize");
    MetadataBuilder document_email_expectedDepositDate = document_emailSchema.get("expectedDepositDate");
    MetadataBuilder document_email_expectedDestructionDate = document_emailSchema.get("expectedDestructionDate");
    MetadataBuilder document_email_expectedTransferDate = document_emailSchema.get("expectedTransferDate");
    MetadataBuilder document_email_favorites = document_emailSchema.get("favorites");
    MetadataBuilder document_email_filingSpace = document_emailSchema.get("filingSpace");
    MetadataBuilder document_email_folder = document_emailSchema.get("folder");
    MetadataBuilder document_email_formCreatedBy = document_emailSchema.get("formCreatedBy");
    MetadataBuilder document_email_formCreatedOn = document_emailSchema.get("formCreatedOn");
    MetadataBuilder document_email_formModifiedBy = document_emailSchema.get("formModifiedBy");
    MetadataBuilder document_email_formModifiedOn = document_emailSchema.get("formModifiedOn");
    MetadataBuilder document_email_hasContent = document_emailSchema.get("hasContent");
    MetadataBuilder document_email_hidden = document_emailSchema.get("hidden");
    MetadataBuilder document_email_id = document_emailSchema.get("id");
    MetadataBuilder document_email_inheritedRetentionRule = document_emailSchema.get("inheritedRetentionRule");
    MetadataBuilder document_email_isCheckoutAlertSent = document_emailSchema.get("isCheckoutAlertSent");
    MetadataBuilder document_email_isModel = document_emailSchema.get("isModel");
    MetadataBuilder document_email_keywords = document_emailSchema.get("keywords");
    MetadataBuilder document_email_legacyIdentifier = document_emailSchema.get("legacyIdentifier");
    MetadataBuilder document_email_logicallyDeletedOn = document_emailSchema.get("logicallyDeletedOn");
    MetadataBuilder document_email_mainCopyRule = document_emailSchema.get("mainCopyRule");
    MetadataBuilder document_email_mainCopyRuleIdEntered = document_emailSchema.get("mainCopyRuleIdEntered");
    MetadataBuilder document_email_manualTokens = document_emailSchema.get("manualTokens");
    MetadataBuilder document_email_markedForParsing = document_emailSchema.get("markedForParsing");
    MetadataBuilder document_email_markedForPreviewConversion = document_emailSchema.get("markedForPreviewConversion");
    MetadataBuilder document_email_markedForReindexing = document_emailSchema.get("markedForReindexing");
    MetadataBuilder document_email_migrationDataVersion = document_emailSchema.get("migrationDataVersion");
    MetadataBuilder document_email_mimetype = document_emailSchema.get("mimetype");
    MetadataBuilder document_email_modifiedBy = document_emailSchema.get("modifiedBy");
    MetadataBuilder document_email_modifiedOn = document_emailSchema.get("modifiedOn");
    MetadataBuilder document_email_openingDate = document_emailSchema.get("openingDate");
    MetadataBuilder document_email_path = document_emailSchema.get("path");
    MetadataBuilder document_email_pathParts = document_emailSchema.get("pathParts");
    MetadataBuilder document_email_principalpath = document_emailSchema.get("principalpath");
    MetadataBuilder document_email_published = document_emailSchema.get("published");
    MetadataBuilder document_email_publishingExpirationDate = document_emailSchema.get("publishingExpirationDate");
    MetadataBuilder document_email_publishingStartDate = document_emailSchema.get("publishingStartDate");
    MetadataBuilder document_email_removedauthorizations = document_emailSchema.get("removedauthorizations");
    MetadataBuilder document_email_retentionRule = document_emailSchema.get("retentionRule");
    MetadataBuilder document_email_sameInactiveFateAsFolder = document_emailSchema.get("sameInactiveFateAsFolder");
    MetadataBuilder document_email_sameSemiActiveFateAsFolder = document_emailSchema.get("sameSemiActiveFateAsFolder");
    MetadataBuilder document_email_schema = document_emailSchema.get("schema");
    MetadataBuilder document_email_shareDenyTokens = document_emailSchema.get("shareDenyTokens");
    MetadataBuilder document_email_shareTokens = document_emailSchema.get("shareTokens");
    MetadataBuilder document_email_subject = document_emailSchema.get("subject");
    MetadataBuilder document_email_title = document_emailSchema.get("title");
    MetadataBuilder document_email_tokens = document_emailSchema.get("tokens");
    MetadataBuilder document_email_tokensHierarchy = document_emailSchema.get("tokensHierarchy");
    MetadataBuilder document_email_type = document_emailSchema.get("type");
    MetadataBuilder document_email_version = document_emailSchema.get("version");
    MetadataBuilder document_email_visibleInTrees = document_emailSchema.get("visibleInTrees");
  }

  private void createDdvUserFunctionSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder ddvUserFunctionSchemaType, MetadataSchemaBuilder ddvUserFunctionSchema) {
  }

  private void createConnectorInstanceSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder connectorInstanceSchemaType, MetadataSchemaBuilder connectorInstance_httpSchema, MetadataSchemaBuilder connectorInstance_ldapSchema, MetadataSchemaBuilder connectorInstance_smbSchema, MetadataSchemaBuilder connectorInstanceSchema) {
    MetadataBuilder connectorInstance_http_allReferences = connectorInstance_httpSchema.get("allReferences");
    MetadataBuilder connectorInstance_http_allRemovedAuths = connectorInstance_httpSchema.get("allRemovedAuths");
    MetadataBuilder connectorInstance_http_attachedAncestors = connectorInstance_httpSchema.get("attachedAncestors");
    MetadataBuilder connectorInstance_http_autocomplete = connectorInstance_httpSchema.get("autocomplete");
    MetadataBuilder connectorInstance_http_availableFields = connectorInstance_httpSchema.get("availableFields");
    MetadataBuilder connectorInstance_http_caption = connectorInstance_httpSchema.get("caption");
    MetadataBuilder connectorInstance_http_code = connectorInstance_httpSchema.get("code");
    MetadataBuilder connectorInstance_http_connectorType = connectorInstance_httpSchema.get("connectorType");
    MetadataBuilder connectorInstance_http_createdBy = connectorInstance_httpSchema.get("createdBy");
    MetadataBuilder connectorInstance_http_createdOn = connectorInstance_httpSchema.get("createdOn");
    MetadataBuilder connectorInstance_http_deleted = connectorInstance_httpSchema.get("deleted");
    MetadataBuilder connectorInstance_http_denyTokens = connectorInstance_httpSchema.get("denyTokens");
    MetadataBuilder connectorInstance_http_detachedauthorizations = connectorInstance_httpSchema.get("detachedauthorizations");
    MetadataBuilder connectorInstance_http_enabled = connectorInstance_httpSchema.get("enabled");
    MetadataBuilder connectorInstance_http_errorOnPhysicalDeletion = connectorInstance_httpSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder connectorInstance_http_estimatedSize = connectorInstance_httpSchema.get("estimatedSize");
    MetadataBuilder connectorInstance_http_hidden = connectorInstance_httpSchema.get("hidden");
    MetadataBuilder connectorInstance_http_id = connectorInstance_httpSchema.get("id");
    MetadataBuilder connectorInstance_http_lastTraversalOn = connectorInstance_httpSchema.get("lastTraversalOn");
    MetadataBuilder connectorInstance_http_legacyIdentifier = connectorInstance_httpSchema.get("legacyIdentifier");
    MetadataBuilder connectorInstance_http_logicallyDeletedOn = connectorInstance_httpSchema.get("logicallyDeletedOn");
    MetadataBuilder connectorInstance_http_manualTokens = connectorInstance_httpSchema.get("manualTokens");
    MetadataBuilder connectorInstance_http_markedForParsing = connectorInstance_httpSchema.get("markedForParsing");
    MetadataBuilder connectorInstance_http_markedForPreviewConversion = connectorInstance_httpSchema.get("markedForPreviewConversion");
    MetadataBuilder connectorInstance_http_markedForReindexing = connectorInstance_httpSchema.get("markedForReindexing");
    MetadataBuilder connectorInstance_http_migrationDataVersion = connectorInstance_httpSchema.get("migrationDataVersion");
    MetadataBuilder connectorInstance_http_modifiedBy = connectorInstance_httpSchema.get("modifiedBy");
    MetadataBuilder connectorInstance_http_modifiedOn = connectorInstance_httpSchema.get("modifiedOn");
    MetadataBuilder connectorInstance_http_path = connectorInstance_httpSchema.get("path");
    MetadataBuilder connectorInstance_http_pathParts = connectorInstance_httpSchema.get("pathParts");
    MetadataBuilder connectorInstance_http_principalpath = connectorInstance_httpSchema.get("principalpath");
    MetadataBuilder connectorInstance_http_propertiesMapping = connectorInstance_httpSchema.get("propertiesMapping");
    MetadataBuilder connectorInstance_http_removedauthorizations = connectorInstance_httpSchema.get("removedauthorizations");
    MetadataBuilder connectorInstance_http_schema = connectorInstance_httpSchema.get("schema");
    MetadataBuilder connectorInstance_http_shareDenyTokens = connectorInstance_httpSchema.get("shareDenyTokens");
    MetadataBuilder connectorInstance_http_shareTokens = connectorInstance_httpSchema.get("shareTokens");
    MetadataBuilder connectorInstance_http_title = connectorInstance_httpSchema.get("title");
    MetadataBuilder connectorInstance_http_tokens = connectorInstance_httpSchema.get("tokens");
    MetadataBuilder connectorInstance_http_tokensHierarchy = connectorInstance_httpSchema.get("tokensHierarchy");
    MetadataBuilder connectorInstance_http_traversalCode = connectorInstance_httpSchema.get("traversalCode");
    MetadataBuilder connectorInstance_http_traversalSchedule = connectorInstance_httpSchema.get("traversalSchedule");
    MetadataBuilder connectorInstance_http_visibleInTrees = connectorInstance_httpSchema.get("visibleInTrees");
    MetadataBuilder connectorInstance_ldap_allReferences = connectorInstance_ldapSchema.get("allReferences");
    MetadataBuilder connectorInstance_ldap_allRemovedAuths = connectorInstance_ldapSchema.get("allRemovedAuths");
    MetadataBuilder connectorInstance_ldap_attachedAncestors = connectorInstance_ldapSchema.get("attachedAncestors");
    MetadataBuilder connectorInstance_ldap_autocomplete = connectorInstance_ldapSchema.get("autocomplete");
    MetadataBuilder connectorInstance_ldap_availableFields = connectorInstance_ldapSchema.get("availableFields");
    MetadataBuilder connectorInstance_ldap_caption = connectorInstance_ldapSchema.get("caption");
    MetadataBuilder connectorInstance_ldap_code = connectorInstance_ldapSchema.get("code");
    MetadataBuilder connectorInstance_ldap_connectorType = connectorInstance_ldapSchema.get("connectorType");
    MetadataBuilder connectorInstance_ldap_createdBy = connectorInstance_ldapSchema.get("createdBy");
    MetadataBuilder connectorInstance_ldap_createdOn = connectorInstance_ldapSchema.get("createdOn");
    MetadataBuilder connectorInstance_ldap_deleted = connectorInstance_ldapSchema.get("deleted");
    MetadataBuilder connectorInstance_ldap_denyTokens = connectorInstance_ldapSchema.get("denyTokens");
    MetadataBuilder connectorInstance_ldap_detachedauthorizations = connectorInstance_ldapSchema.get("detachedauthorizations");
    MetadataBuilder connectorInstance_ldap_enabled = connectorInstance_ldapSchema.get("enabled");
    MetadataBuilder connectorInstance_ldap_errorOnPhysicalDeletion = connectorInstance_ldapSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder connectorInstance_ldap_estimatedSize = connectorInstance_ldapSchema.get("estimatedSize");
    MetadataBuilder connectorInstance_ldap_hidden = connectorInstance_ldapSchema.get("hidden");
    MetadataBuilder connectorInstance_ldap_id = connectorInstance_ldapSchema.get("id");
    MetadataBuilder connectorInstance_ldap_lastTraversalOn = connectorInstance_ldapSchema.get("lastTraversalOn");
    MetadataBuilder connectorInstance_ldap_legacyIdentifier = connectorInstance_ldapSchema.get("legacyIdentifier");
    MetadataBuilder connectorInstance_ldap_logicallyDeletedOn = connectorInstance_ldapSchema.get("logicallyDeletedOn");
    MetadataBuilder connectorInstance_ldap_manualTokens = connectorInstance_ldapSchema.get("manualTokens");
    MetadataBuilder connectorInstance_ldap_markedForParsing = connectorInstance_ldapSchema.get("markedForParsing");
    MetadataBuilder connectorInstance_ldap_markedForPreviewConversion = connectorInstance_ldapSchema.get("markedForPreviewConversion");
    MetadataBuilder connectorInstance_ldap_markedForReindexing = connectorInstance_ldapSchema.get("markedForReindexing");
    MetadataBuilder connectorInstance_ldap_migrationDataVersion = connectorInstance_ldapSchema.get("migrationDataVersion");
    MetadataBuilder connectorInstance_ldap_modifiedBy = connectorInstance_ldapSchema.get("modifiedBy");
    MetadataBuilder connectorInstance_ldap_modifiedOn = connectorInstance_ldapSchema.get("modifiedOn");
    MetadataBuilder connectorInstance_ldap_path = connectorInstance_ldapSchema.get("path");
    MetadataBuilder connectorInstance_ldap_pathParts = connectorInstance_ldapSchema.get("pathParts");
    MetadataBuilder connectorInstance_ldap_principalpath = connectorInstance_ldapSchema.get("principalpath");
    MetadataBuilder connectorInstance_ldap_propertiesMapping = connectorInstance_ldapSchema.get("propertiesMapping");
    MetadataBuilder connectorInstance_ldap_removedauthorizations = connectorInstance_ldapSchema.get("removedauthorizations");
    MetadataBuilder connectorInstance_ldap_schema = connectorInstance_ldapSchema.get("schema");
    MetadataBuilder connectorInstance_ldap_shareDenyTokens = connectorInstance_ldapSchema.get("shareDenyTokens");
    MetadataBuilder connectorInstance_ldap_shareTokens = connectorInstance_ldapSchema.get("shareTokens");
    MetadataBuilder connectorInstance_ldap_title = connectorInstance_ldapSchema.get("title");
    MetadataBuilder connectorInstance_ldap_tokens = connectorInstance_ldapSchema.get("tokens");
    MetadataBuilder connectorInstance_ldap_tokensHierarchy = connectorInstance_ldapSchema.get("tokensHierarchy");
    MetadataBuilder connectorInstance_ldap_traversalCode = connectorInstance_ldapSchema.get("traversalCode");
    MetadataBuilder connectorInstance_ldap_traversalSchedule = connectorInstance_ldapSchema.get("traversalSchedule");
    MetadataBuilder connectorInstance_ldap_visibleInTrees = connectorInstance_ldapSchema.get("visibleInTrees");
    MetadataBuilder connectorInstance_smb_allReferences = connectorInstance_smbSchema.get("allReferences");
    MetadataBuilder connectorInstance_smb_allRemovedAuths = connectorInstance_smbSchema.get("allRemovedAuths");
    MetadataBuilder connectorInstance_smb_attachedAncestors = connectorInstance_smbSchema.get("attachedAncestors");
    MetadataBuilder connectorInstance_smb_autocomplete = connectorInstance_smbSchema.get("autocomplete");
    MetadataBuilder connectorInstance_smb_availableFields = connectorInstance_smbSchema.get("availableFields");
    MetadataBuilder connectorInstance_smb_caption = connectorInstance_smbSchema.get("caption");
    MetadataBuilder connectorInstance_smb_code = connectorInstance_smbSchema.get("code");
    MetadataBuilder connectorInstance_smb_connectorType = connectorInstance_smbSchema.get("connectorType");
    MetadataBuilder connectorInstance_smb_createdBy = connectorInstance_smbSchema.get("createdBy");
    MetadataBuilder connectorInstance_smb_createdOn = connectorInstance_smbSchema.get("createdOn");
    MetadataBuilder connectorInstance_smb_deleted = connectorInstance_smbSchema.get("deleted");
    MetadataBuilder connectorInstance_smb_denyTokens = connectorInstance_smbSchema.get("denyTokens");
    MetadataBuilder connectorInstance_smb_detachedauthorizations = connectorInstance_smbSchema.get("detachedauthorizations");
    MetadataBuilder connectorInstance_smb_enabled = connectorInstance_smbSchema.get("enabled");
    MetadataBuilder connectorInstance_smb_errorOnPhysicalDeletion = connectorInstance_smbSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder connectorInstance_smb_estimatedSize = connectorInstance_smbSchema.get("estimatedSize");
    MetadataBuilder connectorInstance_smb_hidden = connectorInstance_smbSchema.get("hidden");
    MetadataBuilder connectorInstance_smb_id = connectorInstance_smbSchema.get("id");
    MetadataBuilder connectorInstance_smb_lastTraversalOn = connectorInstance_smbSchema.get("lastTraversalOn");
    MetadataBuilder connectorInstance_smb_legacyIdentifier = connectorInstance_smbSchema.get("legacyIdentifier");
    MetadataBuilder connectorInstance_smb_logicallyDeletedOn = connectorInstance_smbSchema.get("logicallyDeletedOn");
    MetadataBuilder connectorInstance_smb_manualTokens = connectorInstance_smbSchema.get("manualTokens");
    MetadataBuilder connectorInstance_smb_markedForParsing = connectorInstance_smbSchema.get("markedForParsing");
    MetadataBuilder connectorInstance_smb_markedForPreviewConversion = connectorInstance_smbSchema.get("markedForPreviewConversion");
    MetadataBuilder connectorInstance_smb_markedForReindexing = connectorInstance_smbSchema.get("markedForReindexing");
    MetadataBuilder connectorInstance_smb_migrationDataVersion = connectorInstance_smbSchema.get("migrationDataVersion");
    MetadataBuilder connectorInstance_smb_modifiedBy = connectorInstance_smbSchema.get("modifiedBy");
    MetadataBuilder connectorInstance_smb_modifiedOn = connectorInstance_smbSchema.get("modifiedOn");
    MetadataBuilder connectorInstance_smb_path = connectorInstance_smbSchema.get("path");
    MetadataBuilder connectorInstance_smb_pathParts = connectorInstance_smbSchema.get("pathParts");
    MetadataBuilder connectorInstance_smb_principalpath = connectorInstance_smbSchema.get("principalpath");
    MetadataBuilder connectorInstance_smb_propertiesMapping = connectorInstance_smbSchema.get("propertiesMapping");
    MetadataBuilder connectorInstance_smb_removedauthorizations = connectorInstance_smbSchema.get("removedauthorizations");
    MetadataBuilder connectorInstance_smb_schema = connectorInstance_smbSchema.get("schema");
    MetadataBuilder connectorInstance_smb_shareDenyTokens = connectorInstance_smbSchema.get("shareDenyTokens");
    MetadataBuilder connectorInstance_smb_shareTokens = connectorInstance_smbSchema.get("shareTokens");
    MetadataBuilder connectorInstance_smb_title = connectorInstance_smbSchema.get("title");
    MetadataBuilder connectorInstance_smb_tokens = connectorInstance_smbSchema.get("tokens");
    MetadataBuilder connectorInstance_smb_tokensHierarchy = connectorInstance_smbSchema.get("tokensHierarchy");
    MetadataBuilder connectorInstance_smb_traversalCode = connectorInstance_smbSchema.get("traversalCode");
    MetadataBuilder connectorInstance_smb_traversalSchedule = connectorInstance_smbSchema.get("traversalSchedule");
    MetadataBuilder connectorInstance_smb_visibleInTrees = connectorInstance_smbSchema.get("visibleInTrees");
  }

  private void createDdvStorageSpaceTypeSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder ddvStorageSpaceTypeSchemaType, MetadataSchemaBuilder ddvStorageSpaceTypeSchema) {
  }

  private void createDdvContainerRecordTypeSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder ddvContainerRecordTypeSchemaType, MetadataSchemaBuilder ddvContainerRecordTypeSchema) {
  }

  private void createSavedSearchSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder savedSearchSchemaType, MetadataSchemaBuilder savedSearchSchema) {
  }

  private void createConnectorLdapUserDocumentSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder connectorLdapUserDocumentSchemaType, MetadataSchemaBuilder connectorLdapUserDocumentSchema) {
  }

  private void createDdvVariablePeriodSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder ddvVariablePeriodSchemaType, MetadataSchemaBuilder ddvVariablePeriodSchema) {
  }

  private void createDecommissioningListSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder decommissioningListSchemaType, MetadataSchemaBuilder decommissioningListSchema) {
  }

  private void createEmailToSendSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder emailToSendSchemaType, MetadataSchemaBuilder emailToSendSchema) {
  }

  private void createEventSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder eventSchemaType, MetadataSchemaBuilder eventSchema) {
  }

  private void createRobotLogSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder robotLogSchemaType, MetadataSchemaBuilder robotLogSchema) {
  }

  private void createConnectorTypeSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder connectorTypeSchemaType, MetadataSchemaBuilder connectorTypeSchema) {
  }

  private void createPrintableSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder printableSchemaType, MetadataSchemaBuilder printable_labelSchema, MetadataSchemaBuilder printable_reportSchema, MetadataSchemaBuilder printableSchema) {
    MetadataBuilder printable_label_allReferences = printable_labelSchema.get("allReferences");
    MetadataBuilder printable_label_allRemovedAuths = printable_labelSchema.get("allRemovedAuths");
    MetadataBuilder printable_label_attachedAncestors = printable_labelSchema.get("attachedAncestors");
    MetadataBuilder printable_label_autocomplete = printable_labelSchema.get("autocomplete");
    MetadataBuilder printable_label_caption = printable_labelSchema.get("caption");
    MetadataBuilder printable_label_createdBy = printable_labelSchema.get("createdBy");
    MetadataBuilder printable_label_createdOn = printable_labelSchema.get("createdOn");
    MetadataBuilder printable_label_deleted = printable_labelSchema.get("deleted");
    MetadataBuilder printable_label_denyTokens = printable_labelSchema.get("denyTokens");
    MetadataBuilder printable_label_detachedauthorizations = printable_labelSchema.get("detachedauthorizations");
    MetadataBuilder printable_label_errorOnPhysicalDeletion = printable_labelSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder printable_label_estimatedSize = printable_labelSchema.get("estimatedSize");
    MetadataBuilder printable_label_hidden = printable_labelSchema.get("hidden");
    MetadataBuilder printable_label_id = printable_labelSchema.get("id");
    MetadataBuilder printable_label_isdeletable = printable_labelSchema.get("isdeletable");
    MetadataBuilder printable_label_jasperfile = printable_labelSchema.get("jasperfile");
    MetadataBuilder printable_label_legacyIdentifier = printable_labelSchema.get("legacyIdentifier");
    MetadataBuilder printable_label_logicallyDeletedOn = printable_labelSchema.get("logicallyDeletedOn");
    MetadataBuilder printable_label_manualTokens = printable_labelSchema.get("manualTokens");
    MetadataBuilder printable_label_markedForParsing = printable_labelSchema.get("markedForParsing");
    MetadataBuilder printable_label_markedForPreviewConversion = printable_labelSchema.get("markedForPreviewConversion");
    MetadataBuilder printable_label_markedForReindexing = printable_labelSchema.get("markedForReindexing");
    MetadataBuilder printable_label_migrationDataVersion = printable_labelSchema.get("migrationDataVersion");
    MetadataBuilder printable_label_modifiedBy = printable_labelSchema.get("modifiedBy");
    MetadataBuilder printable_label_modifiedOn = printable_labelSchema.get("modifiedOn");
    MetadataBuilder printable_label_path = printable_labelSchema.get("path");
    MetadataBuilder printable_label_pathParts = printable_labelSchema.get("pathParts");
    MetadataBuilder printable_label_principalpath = printable_labelSchema.get("principalpath");
    MetadataBuilder printable_label_removedauthorizations = printable_labelSchema.get("removedauthorizations");
    MetadataBuilder printable_label_schema = printable_labelSchema.get("schema");
    MetadataBuilder printable_label_shareDenyTokens = printable_labelSchema.get("shareDenyTokens");
    MetadataBuilder printable_label_shareTokens = printable_labelSchema.get("shareTokens");
    MetadataBuilder printable_label_title = printable_labelSchema.get("title");
    MetadataBuilder printable_label_tokens = printable_labelSchema.get("tokens");
    MetadataBuilder printable_label_tokensHierarchy = printable_labelSchema.get("tokensHierarchy");
    MetadataBuilder printable_label_visibleInTrees = printable_labelSchema.get("visibleInTrees");
    MetadataBuilder printable_report_allReferences = printable_reportSchema.get("allReferences");
    MetadataBuilder printable_report_allRemovedAuths = printable_reportSchema.get("allRemovedAuths");
    MetadataBuilder printable_report_attachedAncestors = printable_reportSchema.get("attachedAncestors");
    MetadataBuilder printable_report_autocomplete = printable_reportSchema.get("autocomplete");
    MetadataBuilder printable_report_caption = printable_reportSchema.get("caption");
    MetadataBuilder printable_report_createdBy = printable_reportSchema.get("createdBy");
    MetadataBuilder printable_report_createdOn = printable_reportSchema.get("createdOn");
    MetadataBuilder printable_report_deleted = printable_reportSchema.get("deleted");
    MetadataBuilder printable_report_denyTokens = printable_reportSchema.get("denyTokens");
    MetadataBuilder printable_report_detachedauthorizations = printable_reportSchema.get("detachedauthorizations");
    MetadataBuilder printable_report_errorOnPhysicalDeletion = printable_reportSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder printable_report_estimatedSize = printable_reportSchema.get("estimatedSize");
    MetadataBuilder printable_report_hidden = printable_reportSchema.get("hidden");
    MetadataBuilder printable_report_id = printable_reportSchema.get("id");
    MetadataBuilder printable_report_isdeletable = printable_reportSchema.get("isdeletable");
    MetadataBuilder printable_report_jasperfile = printable_reportSchema.get("jasperfile");
    MetadataBuilder printable_report_legacyIdentifier = printable_reportSchema.get("legacyIdentifier");
    MetadataBuilder printable_report_logicallyDeletedOn = printable_reportSchema.get("logicallyDeletedOn");
    MetadataBuilder printable_report_manualTokens = printable_reportSchema.get("manualTokens");
    MetadataBuilder printable_report_markedForParsing = printable_reportSchema.get("markedForParsing");
    MetadataBuilder printable_report_markedForPreviewConversion = printable_reportSchema.get("markedForPreviewConversion");
    MetadataBuilder printable_report_markedForReindexing = printable_reportSchema.get("markedForReindexing");
    MetadataBuilder printable_report_migrationDataVersion = printable_reportSchema.get("migrationDataVersion");
    MetadataBuilder printable_report_modifiedBy = printable_reportSchema.get("modifiedBy");
    MetadataBuilder printable_report_modifiedOn = printable_reportSchema.get("modifiedOn");
    MetadataBuilder printable_report_path = printable_reportSchema.get("path");
    MetadataBuilder printable_report_pathParts = printable_reportSchema.get("pathParts");
    MetadataBuilder printable_report_principalpath = printable_reportSchema.get("principalpath");
    MetadataBuilder printable_report_removedauthorizations = printable_reportSchema.get("removedauthorizations");
    MetadataBuilder printable_report_schema = printable_reportSchema.get("schema");
    MetadataBuilder printable_report_shareDenyTokens = printable_reportSchema.get("shareDenyTokens");
    MetadataBuilder printable_report_shareTokens = printable_reportSchema.get("shareTokens");
    MetadataBuilder printable_report_title = printable_reportSchema.get("title");
    MetadataBuilder printable_report_tokens = printable_reportSchema.get("tokens");
    MetadataBuilder printable_report_tokensHierarchy = printable_reportSchema.get("tokensHierarchy");
    MetadataBuilder printable_report_visibleInTrees = printable_reportSchema.get("visibleInTrees");
  }

  private void createThesaurusConfigSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder thesaurusConfigSchemaType, MetadataSchemaBuilder thesaurusConfigSchema) {
  }

  private void createUserTaskSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder userTaskSchemaType, MetadataSchemaBuilder userTask_borrowExtensionRequestSchema, MetadataSchemaBuilder userTask_borrowRequestSchema, MetadataSchemaBuilder userTask_reactivationRequestSchema, MetadataSchemaBuilder userTask_returnRequestSchema, MetadataSchemaBuilder userTaskSchema) {
    MetadataBuilder userTask_borrowExtensionRequest_administrativeUnit = userTask_borrowExtensionRequestSchema.get("administrativeUnit");
    MetadataBuilder userTask_borrowExtensionRequest_allReferences = userTask_borrowExtensionRequestSchema.get("allReferences");
    MetadataBuilder userTask_borrowExtensionRequest_allRemovedAuths = userTask_borrowExtensionRequestSchema.get("allRemovedAuths");
    MetadataBuilder userTask_borrowExtensionRequest_assignedOn = userTask_borrowExtensionRequestSchema.get("assignedOn");
    MetadataBuilder userTask_borrowExtensionRequest_assignee = userTask_borrowExtensionRequestSchema.get("assignee");
    MetadataBuilder userTask_borrowExtensionRequest_assigneeGroupsCandidates = userTask_borrowExtensionRequestSchema.get("assigneeGroupsCandidates");
    MetadataBuilder userTask_borrowExtensionRequest_assigneeUsersCandidates = userTask_borrowExtensionRequestSchema.get("assigneeUsersCandidates");
    MetadataBuilder userTask_borrowExtensionRequest_assigner = userTask_borrowExtensionRequestSchema.get("assigner");
    MetadataBuilder userTask_borrowExtensionRequest_attachedAncestors = userTask_borrowExtensionRequestSchema.get("attachedAncestors");
    MetadataBuilder userTask_borrowExtensionRequest_autocomplete = userTask_borrowExtensionRequestSchema.get("autocomplete");
    MetadataBuilder userTask_borrowExtensionRequest_caption = userTask_borrowExtensionRequestSchema.get("caption");
    MetadataBuilder userTask_borrowExtensionRequest_comments = userTask_borrowExtensionRequestSchema.get("comments");
    MetadataBuilder userTask_borrowExtensionRequest_contents = userTask_borrowExtensionRequestSchema.get("contents");
    MetadataBuilder userTask_borrowExtensionRequest_createdAuthorizations = userTask_borrowExtensionRequestSchema.get("createdAuthorizations");
    MetadataBuilder userTask_borrowExtensionRequest_createdBy = userTask_borrowExtensionRequestSchema.get("createdBy");
    MetadataBuilder userTask_borrowExtensionRequest_createdOn = userTask_borrowExtensionRequestSchema.get("createdOn");
    MetadataBuilder userTask_borrowExtensionRequest_decision = userTask_borrowExtensionRequestSchema.get("decision");
    MetadataBuilder userTask_borrowExtensionRequest_deleted = userTask_borrowExtensionRequestSchema.get("deleted");
    MetadataBuilder userTask_borrowExtensionRequest_denyTokens = userTask_borrowExtensionRequestSchema.get("denyTokens");
    MetadataBuilder userTask_borrowExtensionRequest_description = userTask_borrowExtensionRequestSchema.get("description");
    MetadataBuilder userTask_borrowExtensionRequest_detachedauthorizations = userTask_borrowExtensionRequestSchema.get("detachedauthorizations");
    MetadataBuilder userTask_borrowExtensionRequest_dueDate = userTask_borrowExtensionRequestSchema.get("dueDate");
    MetadataBuilder userTask_borrowExtensionRequest_endDate = userTask_borrowExtensionRequestSchema.get("endDate");
    MetadataBuilder userTask_borrowExtensionRequest_errorOnPhysicalDeletion = userTask_borrowExtensionRequestSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder userTask_borrowExtensionRequest_escalationAssignee = userTask_borrowExtensionRequestSchema.get("escalationAssignee");
    MetadataBuilder userTask_borrowExtensionRequest_estimatedHours = userTask_borrowExtensionRequestSchema.get("estimatedHours");
    MetadataBuilder userTask_borrowExtensionRequest_estimatedSize = userTask_borrowExtensionRequestSchema.get("estimatedSize");
    MetadataBuilder userTask_borrowExtensionRequest_hidden = userTask_borrowExtensionRequestSchema.get("hidden");
    MetadataBuilder userTask_borrowExtensionRequest_id = userTask_borrowExtensionRequestSchema.get("id");
    MetadataBuilder userTask_borrowExtensionRequest_isLate = userTask_borrowExtensionRequestSchema.get("isLate");
    MetadataBuilder userTask_borrowExtensionRequest_isModel = userTask_borrowExtensionRequestSchema.get("isModel");
    MetadataBuilder userTask_borrowExtensionRequest_lastReminder = userTask_borrowExtensionRequestSchema.get("lastReminder");
    MetadataBuilder userTask_borrowExtensionRequest_legacyIdentifier = userTask_borrowExtensionRequestSchema.get("legacyIdentifier");
    MetadataBuilder userTask_borrowExtensionRequest_linkedContainers = userTask_borrowExtensionRequestSchema.get("linkedContainers");
    MetadataBuilder userTask_borrowExtensionRequest_linkedDocuments = userTask_borrowExtensionRequestSchema.get("linkedDocuments");
    MetadataBuilder userTask_borrowExtensionRequest_linkedFolders = userTask_borrowExtensionRequestSchema.get("linkedFolders");
    MetadataBuilder userTask_borrowExtensionRequest_logicallyDeletedOn = userTask_borrowExtensionRequestSchema.get("logicallyDeletedOn");
    MetadataBuilder userTask_borrowExtensionRequest_manualTokens = userTask_borrowExtensionRequestSchema.get("manualTokens");
    MetadataBuilder userTask_borrowExtensionRequest_markedForParsing = userTask_borrowExtensionRequestSchema.get("markedForParsing");
    MetadataBuilder userTask_borrowExtensionRequest_markedForPreviewConversion = userTask_borrowExtensionRequestSchema.get("markedForPreviewConversion");
    MetadataBuilder userTask_borrowExtensionRequest_markedForReindexing = userTask_borrowExtensionRequestSchema.get("markedForReindexing");
    MetadataBuilder userTask_borrowExtensionRequest_migrationDataVersion = userTask_borrowExtensionRequestSchema.get("migrationDataVersion");
    MetadataBuilder userTask_borrowExtensionRequest_modelTask = userTask_borrowExtensionRequestSchema.get("modelTask");
    MetadataBuilder userTask_borrowExtensionRequest_modifiedBy = userTask_borrowExtensionRequestSchema.get("modifiedBy");
    MetadataBuilder userTask_borrowExtensionRequest_modifiedOn = userTask_borrowExtensionRequestSchema.get("modifiedOn");
    MetadataBuilder userTask_borrowExtensionRequest_nextReminderOn = userTask_borrowExtensionRequestSchema.get("nextReminderOn");
    MetadataBuilder userTask_borrowExtensionRequest_nextTaskCreated = userTask_borrowExtensionRequestSchema.get("nextTaskCreated");
    MetadataBuilder userTask_borrowExtensionRequest_nextTasks = userTask_borrowExtensionRequestSchema.get("nextTasks");
    MetadataBuilder userTask_borrowExtensionRequest_nextTasksDecisions = userTask_borrowExtensionRequestSchema.get("nextTasksDecisions");
    MetadataBuilder userTask_borrowExtensionRequest_numberOfReminders = userTask_borrowExtensionRequestSchema.get("numberOfReminders");
    MetadataBuilder userTask_borrowExtensionRequest_parentTask = userTask_borrowExtensionRequestSchema.get("parentTask");
    MetadataBuilder userTask_borrowExtensionRequest_parentTaskDueDate = userTask_borrowExtensionRequestSchema.get("parentTaskDueDate");
    MetadataBuilder userTask_borrowExtensionRequest_path = userTask_borrowExtensionRequestSchema.get("path");
    MetadataBuilder userTask_borrowExtensionRequest_pathParts = userTask_borrowExtensionRequestSchema.get("pathParts");
    MetadataBuilder userTask_borrowExtensionRequest_principalpath = userTask_borrowExtensionRequestSchema.get("principalpath");
    MetadataBuilder userTask_borrowExtensionRequest_progressPercentage = userTask_borrowExtensionRequestSchema.get("progressPercentage");
    MetadataBuilder userTask_borrowExtensionRequest_question = userTask_borrowExtensionRequestSchema.get("question");
    MetadataBuilder userTask_borrowExtensionRequest_readByUser = userTask_borrowExtensionRequestSchema.get("readByUser");
    MetadataBuilder userTask_borrowExtensionRequest_reason = userTask_borrowExtensionRequestSchema.get("reason");
    MetadataBuilder userTask_borrowExtensionRequest_relativeDueDate = userTask_borrowExtensionRequestSchema.get("relativeDueDate");
    MetadataBuilder userTask_borrowExtensionRequest_reminderFrequency = userTask_borrowExtensionRequestSchema.get("reminderFrequency");
    MetadataBuilder userTask_borrowExtensionRequest_reminders = userTask_borrowExtensionRequestSchema.get("reminders");
    MetadataBuilder userTask_borrowExtensionRequest_removedauthorizations = userTask_borrowExtensionRequestSchema.get("removedauthorizations");
    MetadataBuilder userTask_borrowExtensionRequest_schema = userTask_borrowExtensionRequestSchema.get("schema");
    MetadataBuilder userTask_borrowExtensionRequest_shareDenyTokens = userTask_borrowExtensionRequestSchema.get("shareDenyTokens");
    MetadataBuilder userTask_borrowExtensionRequest_shareTokens = userTask_borrowExtensionRequestSchema.get("shareTokens");
    MetadataBuilder userTask_borrowExtensionRequest_starredByUsers = userTask_borrowExtensionRequestSchema.get("starredByUsers");
    MetadataBuilder userTask_borrowExtensionRequest_startDate = userTask_borrowExtensionRequestSchema.get("startDate");
    MetadataBuilder userTask_borrowExtensionRequest_status = userTask_borrowExtensionRequestSchema.get("status");
    MetadataBuilder userTask_borrowExtensionRequest_statusType = userTask_borrowExtensionRequestSchema.get("statusType");
    MetadataBuilder userTask_borrowExtensionRequest_taskCollaborators = userTask_borrowExtensionRequestSchema.get("taskCollaborators");
    MetadataBuilder userTask_borrowExtensionRequest_taskCollaboratorsGroups = userTask_borrowExtensionRequestSchema.get("taskCollaboratorsGroups");
    MetadataBuilder userTask_borrowExtensionRequest_taskCollaboratorsGroupsWriteAuthorizations = userTask_borrowExtensionRequestSchema.get("taskCollaboratorsGroupsWriteAuthorizations");
    MetadataBuilder userTask_borrowExtensionRequest_taskCollaboratorsWriteAuthorizations = userTask_borrowExtensionRequestSchema.get("taskCollaboratorsWriteAuthorizations");
    MetadataBuilder userTask_borrowExtensionRequest_taskFollowers = userTask_borrowExtensionRequestSchema.get("taskFollowers");
    MetadataBuilder userTask_borrowExtensionRequest_taskFollowersIds = userTask_borrowExtensionRequestSchema.get("taskFollowersIds");
    MetadataBuilder userTask_borrowExtensionRequest_title = userTask_borrowExtensionRequestSchema.get("title");
    MetadataBuilder userTask_borrowExtensionRequest_tokens = userTask_borrowExtensionRequestSchema.get("tokens");
    MetadataBuilder userTask_borrowExtensionRequest_tokensHierarchy = userTask_borrowExtensionRequestSchema.get("tokensHierarchy");
    MetadataBuilder userTask_borrowExtensionRequest_type = userTask_borrowExtensionRequestSchema.get("type");
    MetadataBuilder userTask_borrowExtensionRequest_visibleInTrees = userTask_borrowExtensionRequestSchema.get("visibleInTrees");
    MetadataBuilder userTask_borrowExtensionRequest_workHours = userTask_borrowExtensionRequestSchema.get("workHours");
    MetadataBuilder userTask_borrowExtensionRequest_workflow = userTask_borrowExtensionRequestSchema.get("workflow");
    MetadataBuilder userTask_borrowExtensionRequest_workflowInstance = userTask_borrowExtensionRequestSchema.get("workflowInstance");
    MetadataBuilder userTask_borrowRequest_administrativeUnit = userTask_borrowRequestSchema.get("administrativeUnit");
    MetadataBuilder userTask_borrowRequest_allReferences = userTask_borrowRequestSchema.get("allReferences");
    MetadataBuilder userTask_borrowRequest_allRemovedAuths = userTask_borrowRequestSchema.get("allRemovedAuths");
    MetadataBuilder userTask_borrowRequest_assignedOn = userTask_borrowRequestSchema.get("assignedOn");
    MetadataBuilder userTask_borrowRequest_assignee = userTask_borrowRequestSchema.get("assignee");
    MetadataBuilder userTask_borrowRequest_assigneeGroupsCandidates = userTask_borrowRequestSchema.get("assigneeGroupsCandidates");
    MetadataBuilder userTask_borrowRequest_assigneeUsersCandidates = userTask_borrowRequestSchema.get("assigneeUsersCandidates");
    MetadataBuilder userTask_borrowRequest_assigner = userTask_borrowRequestSchema.get("assigner");
    MetadataBuilder userTask_borrowRequest_attachedAncestors = userTask_borrowRequestSchema.get("attachedAncestors");
    MetadataBuilder userTask_borrowRequest_autocomplete = userTask_borrowRequestSchema.get("autocomplete");
    MetadataBuilder userTask_borrowRequest_caption = userTask_borrowRequestSchema.get("caption");
    MetadataBuilder userTask_borrowRequest_comments = userTask_borrowRequestSchema.get("comments");
    MetadataBuilder userTask_borrowRequest_contents = userTask_borrowRequestSchema.get("contents");
    MetadataBuilder userTask_borrowRequest_createdAuthorizations = userTask_borrowRequestSchema.get("createdAuthorizations");
    MetadataBuilder userTask_borrowRequest_createdBy = userTask_borrowRequestSchema.get("createdBy");
    MetadataBuilder userTask_borrowRequest_createdOn = userTask_borrowRequestSchema.get("createdOn");
    MetadataBuilder userTask_borrowRequest_decision = userTask_borrowRequestSchema.get("decision");
    MetadataBuilder userTask_borrowRequest_deleted = userTask_borrowRequestSchema.get("deleted");
    MetadataBuilder userTask_borrowRequest_denyTokens = userTask_borrowRequestSchema.get("denyTokens");
    MetadataBuilder userTask_borrowRequest_description = userTask_borrowRequestSchema.get("description");
    MetadataBuilder userTask_borrowRequest_detachedauthorizations = userTask_borrowRequestSchema.get("detachedauthorizations");
    MetadataBuilder userTask_borrowRequest_dueDate = userTask_borrowRequestSchema.get("dueDate");
    MetadataBuilder userTask_borrowRequest_endDate = userTask_borrowRequestSchema.get("endDate");
    MetadataBuilder userTask_borrowRequest_errorOnPhysicalDeletion = userTask_borrowRequestSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder userTask_borrowRequest_escalationAssignee = userTask_borrowRequestSchema.get("escalationAssignee");
    MetadataBuilder userTask_borrowRequest_estimatedHours = userTask_borrowRequestSchema.get("estimatedHours");
    MetadataBuilder userTask_borrowRequest_estimatedSize = userTask_borrowRequestSchema.get("estimatedSize");
    MetadataBuilder userTask_borrowRequest_hidden = userTask_borrowRequestSchema.get("hidden");
    MetadataBuilder userTask_borrowRequest_id = userTask_borrowRequestSchema.get("id");
    MetadataBuilder userTask_borrowRequest_isLate = userTask_borrowRequestSchema.get("isLate");
    MetadataBuilder userTask_borrowRequest_isModel = userTask_borrowRequestSchema.get("isModel");
    MetadataBuilder userTask_borrowRequest_lastReminder = userTask_borrowRequestSchema.get("lastReminder");
    MetadataBuilder userTask_borrowRequest_legacyIdentifier = userTask_borrowRequestSchema.get("legacyIdentifier");
    MetadataBuilder userTask_borrowRequest_linkedContainers = userTask_borrowRequestSchema.get("linkedContainers");
    MetadataBuilder userTask_borrowRequest_linkedDocuments = userTask_borrowRequestSchema.get("linkedDocuments");
    MetadataBuilder userTask_borrowRequest_linkedFolders = userTask_borrowRequestSchema.get("linkedFolders");
    MetadataBuilder userTask_borrowRequest_logicallyDeletedOn = userTask_borrowRequestSchema.get("logicallyDeletedOn");
    MetadataBuilder userTask_borrowRequest_manualTokens = userTask_borrowRequestSchema.get("manualTokens");
    MetadataBuilder userTask_borrowRequest_markedForParsing = userTask_borrowRequestSchema.get("markedForParsing");
    MetadataBuilder userTask_borrowRequest_markedForPreviewConversion = userTask_borrowRequestSchema.get("markedForPreviewConversion");
    MetadataBuilder userTask_borrowRequest_markedForReindexing = userTask_borrowRequestSchema.get("markedForReindexing");
    MetadataBuilder userTask_borrowRequest_migrationDataVersion = userTask_borrowRequestSchema.get("migrationDataVersion");
    MetadataBuilder userTask_borrowRequest_modelTask = userTask_borrowRequestSchema.get("modelTask");
    MetadataBuilder userTask_borrowRequest_modifiedBy = userTask_borrowRequestSchema.get("modifiedBy");
    MetadataBuilder userTask_borrowRequest_modifiedOn = userTask_borrowRequestSchema.get("modifiedOn");
    MetadataBuilder userTask_borrowRequest_nextReminderOn = userTask_borrowRequestSchema.get("nextReminderOn");
    MetadataBuilder userTask_borrowRequest_nextTaskCreated = userTask_borrowRequestSchema.get("nextTaskCreated");
    MetadataBuilder userTask_borrowRequest_nextTasks = userTask_borrowRequestSchema.get("nextTasks");
    MetadataBuilder userTask_borrowRequest_nextTasksDecisions = userTask_borrowRequestSchema.get("nextTasksDecisions");
    MetadataBuilder userTask_borrowRequest_numberOfReminders = userTask_borrowRequestSchema.get("numberOfReminders");
    MetadataBuilder userTask_borrowRequest_parentTask = userTask_borrowRequestSchema.get("parentTask");
    MetadataBuilder userTask_borrowRequest_parentTaskDueDate = userTask_borrowRequestSchema.get("parentTaskDueDate");
    MetadataBuilder userTask_borrowRequest_path = userTask_borrowRequestSchema.get("path");
    MetadataBuilder userTask_borrowRequest_pathParts = userTask_borrowRequestSchema.get("pathParts");
    MetadataBuilder userTask_borrowRequest_principalpath = userTask_borrowRequestSchema.get("principalpath");
    MetadataBuilder userTask_borrowRequest_progressPercentage = userTask_borrowRequestSchema.get("progressPercentage");
    MetadataBuilder userTask_borrowRequest_question = userTask_borrowRequestSchema.get("question");
    MetadataBuilder userTask_borrowRequest_readByUser = userTask_borrowRequestSchema.get("readByUser");
    MetadataBuilder userTask_borrowRequest_reason = userTask_borrowRequestSchema.get("reason");
    MetadataBuilder userTask_borrowRequest_relativeDueDate = userTask_borrowRequestSchema.get("relativeDueDate");
    MetadataBuilder userTask_borrowRequest_reminderFrequency = userTask_borrowRequestSchema.get("reminderFrequency");
    MetadataBuilder userTask_borrowRequest_reminders = userTask_borrowRequestSchema.get("reminders");
    MetadataBuilder userTask_borrowRequest_removedauthorizations = userTask_borrowRequestSchema.get("removedauthorizations");
    MetadataBuilder userTask_borrowRequest_schema = userTask_borrowRequestSchema.get("schema");
    MetadataBuilder userTask_borrowRequest_shareDenyTokens = userTask_borrowRequestSchema.get("shareDenyTokens");
    MetadataBuilder userTask_borrowRequest_shareTokens = userTask_borrowRequestSchema.get("shareTokens");
    MetadataBuilder userTask_borrowRequest_starredByUsers = userTask_borrowRequestSchema.get("starredByUsers");
    MetadataBuilder userTask_borrowRequest_startDate = userTask_borrowRequestSchema.get("startDate");
    MetadataBuilder userTask_borrowRequest_status = userTask_borrowRequestSchema.get("status");
    MetadataBuilder userTask_borrowRequest_statusType = userTask_borrowRequestSchema.get("statusType");
    MetadataBuilder userTask_borrowRequest_taskCollaborators = userTask_borrowRequestSchema.get("taskCollaborators");
    MetadataBuilder userTask_borrowRequest_taskCollaboratorsGroups = userTask_borrowRequestSchema.get("taskCollaboratorsGroups");
    MetadataBuilder userTask_borrowRequest_taskCollaboratorsGroupsWriteAuthorizations = userTask_borrowRequestSchema.get("taskCollaboratorsGroupsWriteAuthorizations");
    MetadataBuilder userTask_borrowRequest_taskCollaboratorsWriteAuthorizations = userTask_borrowRequestSchema.get("taskCollaboratorsWriteAuthorizations");
    MetadataBuilder userTask_borrowRequest_taskFollowers = userTask_borrowRequestSchema.get("taskFollowers");
    MetadataBuilder userTask_borrowRequest_taskFollowersIds = userTask_borrowRequestSchema.get("taskFollowersIds");
    MetadataBuilder userTask_borrowRequest_title = userTask_borrowRequestSchema.get("title");
    MetadataBuilder userTask_borrowRequest_tokens = userTask_borrowRequestSchema.get("tokens");
    MetadataBuilder userTask_borrowRequest_tokensHierarchy = userTask_borrowRequestSchema.get("tokensHierarchy");
    MetadataBuilder userTask_borrowRequest_type = userTask_borrowRequestSchema.get("type");
    MetadataBuilder userTask_borrowRequest_visibleInTrees = userTask_borrowRequestSchema.get("visibleInTrees");
    MetadataBuilder userTask_borrowRequest_workHours = userTask_borrowRequestSchema.get("workHours");
    MetadataBuilder userTask_borrowRequest_workflow = userTask_borrowRequestSchema.get("workflow");
    MetadataBuilder userTask_borrowRequest_workflowInstance = userTask_borrowRequestSchema.get("workflowInstance");
    MetadataBuilder userTask_reactivationRequest_administrativeUnit = userTask_reactivationRequestSchema.get("administrativeUnit");
    MetadataBuilder userTask_reactivationRequest_allReferences = userTask_reactivationRequestSchema.get("allReferences");
    MetadataBuilder userTask_reactivationRequest_allRemovedAuths = userTask_reactivationRequestSchema.get("allRemovedAuths");
    MetadataBuilder userTask_reactivationRequest_assignedOn = userTask_reactivationRequestSchema.get("assignedOn");
    MetadataBuilder userTask_reactivationRequest_assignee = userTask_reactivationRequestSchema.get("assignee");
    MetadataBuilder userTask_reactivationRequest_assigneeGroupsCandidates = userTask_reactivationRequestSchema.get("assigneeGroupsCandidates");
    MetadataBuilder userTask_reactivationRequest_assigneeUsersCandidates = userTask_reactivationRequestSchema.get("assigneeUsersCandidates");
    MetadataBuilder userTask_reactivationRequest_assigner = userTask_reactivationRequestSchema.get("assigner");
    MetadataBuilder userTask_reactivationRequest_attachedAncestors = userTask_reactivationRequestSchema.get("attachedAncestors");
    MetadataBuilder userTask_reactivationRequest_autocomplete = userTask_reactivationRequestSchema.get("autocomplete");
    MetadataBuilder userTask_reactivationRequest_caption = userTask_reactivationRequestSchema.get("caption");
    MetadataBuilder userTask_reactivationRequest_comments = userTask_reactivationRequestSchema.get("comments");
    MetadataBuilder userTask_reactivationRequest_contents = userTask_reactivationRequestSchema.get("contents");
    MetadataBuilder userTask_reactivationRequest_createdAuthorizations = userTask_reactivationRequestSchema.get("createdAuthorizations");
    MetadataBuilder userTask_reactivationRequest_createdBy = userTask_reactivationRequestSchema.get("createdBy");
    MetadataBuilder userTask_reactivationRequest_createdOn = userTask_reactivationRequestSchema.get("createdOn");
    MetadataBuilder userTask_reactivationRequest_decision = userTask_reactivationRequestSchema.get("decision");
    MetadataBuilder userTask_reactivationRequest_deleted = userTask_reactivationRequestSchema.get("deleted");
    MetadataBuilder userTask_reactivationRequest_denyTokens = userTask_reactivationRequestSchema.get("denyTokens");
    MetadataBuilder userTask_reactivationRequest_description = userTask_reactivationRequestSchema.get("description");
    MetadataBuilder userTask_reactivationRequest_detachedauthorizations = userTask_reactivationRequestSchema.get("detachedauthorizations");
    MetadataBuilder userTask_reactivationRequest_dueDate = userTask_reactivationRequestSchema.get("dueDate");
    MetadataBuilder userTask_reactivationRequest_endDate = userTask_reactivationRequestSchema.get("endDate");
    MetadataBuilder userTask_reactivationRequest_errorOnPhysicalDeletion = userTask_reactivationRequestSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder userTask_reactivationRequest_escalationAssignee = userTask_reactivationRequestSchema.get("escalationAssignee");
    MetadataBuilder userTask_reactivationRequest_estimatedHours = userTask_reactivationRequestSchema.get("estimatedHours");
    MetadataBuilder userTask_reactivationRequest_estimatedSize = userTask_reactivationRequestSchema.get("estimatedSize");
    MetadataBuilder userTask_reactivationRequest_hidden = userTask_reactivationRequestSchema.get("hidden");
    MetadataBuilder userTask_reactivationRequest_id = userTask_reactivationRequestSchema.get("id");
    MetadataBuilder userTask_reactivationRequest_isLate = userTask_reactivationRequestSchema.get("isLate");
    MetadataBuilder userTask_reactivationRequest_isModel = userTask_reactivationRequestSchema.get("isModel");
    MetadataBuilder userTask_reactivationRequest_lastReminder = userTask_reactivationRequestSchema.get("lastReminder");
    MetadataBuilder userTask_reactivationRequest_legacyIdentifier = userTask_reactivationRequestSchema.get("legacyIdentifier");
    MetadataBuilder userTask_reactivationRequest_linkedContainers = userTask_reactivationRequestSchema.get("linkedContainers");
    MetadataBuilder userTask_reactivationRequest_linkedDocuments = userTask_reactivationRequestSchema.get("linkedDocuments");
    MetadataBuilder userTask_reactivationRequest_linkedFolders = userTask_reactivationRequestSchema.get("linkedFolders");
    MetadataBuilder userTask_reactivationRequest_logicallyDeletedOn = userTask_reactivationRequestSchema.get("logicallyDeletedOn");
    MetadataBuilder userTask_reactivationRequest_manualTokens = userTask_reactivationRequestSchema.get("manualTokens");
    MetadataBuilder userTask_reactivationRequest_markedForParsing = userTask_reactivationRequestSchema.get("markedForParsing");
    MetadataBuilder userTask_reactivationRequest_markedForPreviewConversion = userTask_reactivationRequestSchema.get("markedForPreviewConversion");
    MetadataBuilder userTask_reactivationRequest_markedForReindexing = userTask_reactivationRequestSchema.get("markedForReindexing");
    MetadataBuilder userTask_reactivationRequest_migrationDataVersion = userTask_reactivationRequestSchema.get("migrationDataVersion");
    MetadataBuilder userTask_reactivationRequest_modelTask = userTask_reactivationRequestSchema.get("modelTask");
    MetadataBuilder userTask_reactivationRequest_modifiedBy = userTask_reactivationRequestSchema.get("modifiedBy");
    MetadataBuilder userTask_reactivationRequest_modifiedOn = userTask_reactivationRequestSchema.get("modifiedOn");
    MetadataBuilder userTask_reactivationRequest_nextReminderOn = userTask_reactivationRequestSchema.get("nextReminderOn");
    MetadataBuilder userTask_reactivationRequest_nextTaskCreated = userTask_reactivationRequestSchema.get("nextTaskCreated");
    MetadataBuilder userTask_reactivationRequest_nextTasks = userTask_reactivationRequestSchema.get("nextTasks");
    MetadataBuilder userTask_reactivationRequest_nextTasksDecisions = userTask_reactivationRequestSchema.get("nextTasksDecisions");
    MetadataBuilder userTask_reactivationRequest_numberOfReminders = userTask_reactivationRequestSchema.get("numberOfReminders");
    MetadataBuilder userTask_reactivationRequest_parentTask = userTask_reactivationRequestSchema.get("parentTask");
    MetadataBuilder userTask_reactivationRequest_parentTaskDueDate = userTask_reactivationRequestSchema.get("parentTaskDueDate");
    MetadataBuilder userTask_reactivationRequest_path = userTask_reactivationRequestSchema.get("path");
    MetadataBuilder userTask_reactivationRequest_pathParts = userTask_reactivationRequestSchema.get("pathParts");
    MetadataBuilder userTask_reactivationRequest_principalpath = userTask_reactivationRequestSchema.get("principalpath");
    MetadataBuilder userTask_reactivationRequest_progressPercentage = userTask_reactivationRequestSchema.get("progressPercentage");
    MetadataBuilder userTask_reactivationRequest_question = userTask_reactivationRequestSchema.get("question");
    MetadataBuilder userTask_reactivationRequest_readByUser = userTask_reactivationRequestSchema.get("readByUser");
    MetadataBuilder userTask_reactivationRequest_reason = userTask_reactivationRequestSchema.get("reason");
    MetadataBuilder userTask_reactivationRequest_relativeDueDate = userTask_reactivationRequestSchema.get("relativeDueDate");
    MetadataBuilder userTask_reactivationRequest_reminderFrequency = userTask_reactivationRequestSchema.get("reminderFrequency");
    MetadataBuilder userTask_reactivationRequest_reminders = userTask_reactivationRequestSchema.get("reminders");
    MetadataBuilder userTask_reactivationRequest_removedauthorizations = userTask_reactivationRequestSchema.get("removedauthorizations");
    MetadataBuilder userTask_reactivationRequest_schema = userTask_reactivationRequestSchema.get("schema");
    MetadataBuilder userTask_reactivationRequest_shareDenyTokens = userTask_reactivationRequestSchema.get("shareDenyTokens");
    MetadataBuilder userTask_reactivationRequest_shareTokens = userTask_reactivationRequestSchema.get("shareTokens");
    MetadataBuilder userTask_reactivationRequest_starredByUsers = userTask_reactivationRequestSchema.get("starredByUsers");
    MetadataBuilder userTask_reactivationRequest_startDate = userTask_reactivationRequestSchema.get("startDate");
    MetadataBuilder userTask_reactivationRequest_status = userTask_reactivationRequestSchema.get("status");
    MetadataBuilder userTask_reactivationRequest_statusType = userTask_reactivationRequestSchema.get("statusType");
    MetadataBuilder userTask_reactivationRequest_taskCollaborators = userTask_reactivationRequestSchema.get("taskCollaborators");
    MetadataBuilder userTask_reactivationRequest_taskCollaboratorsGroups = userTask_reactivationRequestSchema.get("taskCollaboratorsGroups");
    MetadataBuilder userTask_reactivationRequest_taskCollaboratorsGroupsWriteAuthorizations = userTask_reactivationRequestSchema.get("taskCollaboratorsGroupsWriteAuthorizations");
    MetadataBuilder userTask_reactivationRequest_taskCollaboratorsWriteAuthorizations = userTask_reactivationRequestSchema.get("taskCollaboratorsWriteAuthorizations");
    MetadataBuilder userTask_reactivationRequest_taskFollowers = userTask_reactivationRequestSchema.get("taskFollowers");
    MetadataBuilder userTask_reactivationRequest_taskFollowersIds = userTask_reactivationRequestSchema.get("taskFollowersIds");
    MetadataBuilder userTask_reactivationRequest_title = userTask_reactivationRequestSchema.get("title");
    MetadataBuilder userTask_reactivationRequest_tokens = userTask_reactivationRequestSchema.get("tokens");
    MetadataBuilder userTask_reactivationRequest_tokensHierarchy = userTask_reactivationRequestSchema.get("tokensHierarchy");
    MetadataBuilder userTask_reactivationRequest_type = userTask_reactivationRequestSchema.get("type");
    MetadataBuilder userTask_reactivationRequest_visibleInTrees = userTask_reactivationRequestSchema.get("visibleInTrees");
    MetadataBuilder userTask_reactivationRequest_workHours = userTask_reactivationRequestSchema.get("workHours");
    MetadataBuilder userTask_reactivationRequest_workflow = userTask_reactivationRequestSchema.get("workflow");
    MetadataBuilder userTask_reactivationRequest_workflowInstance = userTask_reactivationRequestSchema.get("workflowInstance");
    MetadataBuilder userTask_returnRequest_administrativeUnit = userTask_returnRequestSchema.get("administrativeUnit");
    MetadataBuilder userTask_returnRequest_allReferences = userTask_returnRequestSchema.get("allReferences");
    MetadataBuilder userTask_returnRequest_allRemovedAuths = userTask_returnRequestSchema.get("allRemovedAuths");
    MetadataBuilder userTask_returnRequest_assignedOn = userTask_returnRequestSchema.get("assignedOn");
    MetadataBuilder userTask_returnRequest_assignee = userTask_returnRequestSchema.get("assignee");
    MetadataBuilder userTask_returnRequest_assigneeGroupsCandidates = userTask_returnRequestSchema.get("assigneeGroupsCandidates");
    MetadataBuilder userTask_returnRequest_assigneeUsersCandidates = userTask_returnRequestSchema.get("assigneeUsersCandidates");
    MetadataBuilder userTask_returnRequest_assigner = userTask_returnRequestSchema.get("assigner");
    MetadataBuilder userTask_returnRequest_attachedAncestors = userTask_returnRequestSchema.get("attachedAncestors");
    MetadataBuilder userTask_returnRequest_autocomplete = userTask_returnRequestSchema.get("autocomplete");
    MetadataBuilder userTask_returnRequest_caption = userTask_returnRequestSchema.get("caption");
    MetadataBuilder userTask_returnRequest_comments = userTask_returnRequestSchema.get("comments");
    MetadataBuilder userTask_returnRequest_contents = userTask_returnRequestSchema.get("contents");
    MetadataBuilder userTask_returnRequest_createdAuthorizations = userTask_returnRequestSchema.get("createdAuthorizations");
    MetadataBuilder userTask_returnRequest_createdBy = userTask_returnRequestSchema.get("createdBy");
    MetadataBuilder userTask_returnRequest_createdOn = userTask_returnRequestSchema.get("createdOn");
    MetadataBuilder userTask_returnRequest_decision = userTask_returnRequestSchema.get("decision");
    MetadataBuilder userTask_returnRequest_deleted = userTask_returnRequestSchema.get("deleted");
    MetadataBuilder userTask_returnRequest_denyTokens = userTask_returnRequestSchema.get("denyTokens");
    MetadataBuilder userTask_returnRequest_description = userTask_returnRequestSchema.get("description");
    MetadataBuilder userTask_returnRequest_detachedauthorizations = userTask_returnRequestSchema.get("detachedauthorizations");
    MetadataBuilder userTask_returnRequest_dueDate = userTask_returnRequestSchema.get("dueDate");
    MetadataBuilder userTask_returnRequest_endDate = userTask_returnRequestSchema.get("endDate");
    MetadataBuilder userTask_returnRequest_errorOnPhysicalDeletion = userTask_returnRequestSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder userTask_returnRequest_escalationAssignee = userTask_returnRequestSchema.get("escalationAssignee");
    MetadataBuilder userTask_returnRequest_estimatedHours = userTask_returnRequestSchema.get("estimatedHours");
    MetadataBuilder userTask_returnRequest_estimatedSize = userTask_returnRequestSchema.get("estimatedSize");
    MetadataBuilder userTask_returnRequest_hidden = userTask_returnRequestSchema.get("hidden");
    MetadataBuilder userTask_returnRequest_id = userTask_returnRequestSchema.get("id");
    MetadataBuilder userTask_returnRequest_isLate = userTask_returnRequestSchema.get("isLate");
    MetadataBuilder userTask_returnRequest_isModel = userTask_returnRequestSchema.get("isModel");
    MetadataBuilder userTask_returnRequest_lastReminder = userTask_returnRequestSchema.get("lastReminder");
    MetadataBuilder userTask_returnRequest_legacyIdentifier = userTask_returnRequestSchema.get("legacyIdentifier");
    MetadataBuilder userTask_returnRequest_linkedContainers = userTask_returnRequestSchema.get("linkedContainers");
    MetadataBuilder userTask_returnRequest_linkedDocuments = userTask_returnRequestSchema.get("linkedDocuments");
    MetadataBuilder userTask_returnRequest_linkedFolders = userTask_returnRequestSchema.get("linkedFolders");
    MetadataBuilder userTask_returnRequest_logicallyDeletedOn = userTask_returnRequestSchema.get("logicallyDeletedOn");
    MetadataBuilder userTask_returnRequest_manualTokens = userTask_returnRequestSchema.get("manualTokens");
    MetadataBuilder userTask_returnRequest_markedForParsing = userTask_returnRequestSchema.get("markedForParsing");
    MetadataBuilder userTask_returnRequest_markedForPreviewConversion = userTask_returnRequestSchema.get("markedForPreviewConversion");
    MetadataBuilder userTask_returnRequest_markedForReindexing = userTask_returnRequestSchema.get("markedForReindexing");
    MetadataBuilder userTask_returnRequest_migrationDataVersion = userTask_returnRequestSchema.get("migrationDataVersion");
    MetadataBuilder userTask_returnRequest_modelTask = userTask_returnRequestSchema.get("modelTask");
    MetadataBuilder userTask_returnRequest_modifiedBy = userTask_returnRequestSchema.get("modifiedBy");
    MetadataBuilder userTask_returnRequest_modifiedOn = userTask_returnRequestSchema.get("modifiedOn");
    MetadataBuilder userTask_returnRequest_nextReminderOn = userTask_returnRequestSchema.get("nextReminderOn");
    MetadataBuilder userTask_returnRequest_nextTaskCreated = userTask_returnRequestSchema.get("nextTaskCreated");
    MetadataBuilder userTask_returnRequest_nextTasks = userTask_returnRequestSchema.get("nextTasks");
    MetadataBuilder userTask_returnRequest_nextTasksDecisions = userTask_returnRequestSchema.get("nextTasksDecisions");
    MetadataBuilder userTask_returnRequest_numberOfReminders = userTask_returnRequestSchema.get("numberOfReminders");
    MetadataBuilder userTask_returnRequest_parentTask = userTask_returnRequestSchema.get("parentTask");
    MetadataBuilder userTask_returnRequest_parentTaskDueDate = userTask_returnRequestSchema.get("parentTaskDueDate");
    MetadataBuilder userTask_returnRequest_path = userTask_returnRequestSchema.get("path");
    MetadataBuilder userTask_returnRequest_pathParts = userTask_returnRequestSchema.get("pathParts");
    MetadataBuilder userTask_returnRequest_principalpath = userTask_returnRequestSchema.get("principalpath");
    MetadataBuilder userTask_returnRequest_progressPercentage = userTask_returnRequestSchema.get("progressPercentage");
    MetadataBuilder userTask_returnRequest_question = userTask_returnRequestSchema.get("question");
    MetadataBuilder userTask_returnRequest_readByUser = userTask_returnRequestSchema.get("readByUser");
    MetadataBuilder userTask_returnRequest_reason = userTask_returnRequestSchema.get("reason");
    MetadataBuilder userTask_returnRequest_relativeDueDate = userTask_returnRequestSchema.get("relativeDueDate");
    MetadataBuilder userTask_returnRequest_reminderFrequency = userTask_returnRequestSchema.get("reminderFrequency");
    MetadataBuilder userTask_returnRequest_reminders = userTask_returnRequestSchema.get("reminders");
    MetadataBuilder userTask_returnRequest_removedauthorizations = userTask_returnRequestSchema.get("removedauthorizations");
    MetadataBuilder userTask_returnRequest_schema = userTask_returnRequestSchema.get("schema");
    MetadataBuilder userTask_returnRequest_shareDenyTokens = userTask_returnRequestSchema.get("shareDenyTokens");
    MetadataBuilder userTask_returnRequest_shareTokens = userTask_returnRequestSchema.get("shareTokens");
    MetadataBuilder userTask_returnRequest_starredByUsers = userTask_returnRequestSchema.get("starredByUsers");
    MetadataBuilder userTask_returnRequest_startDate = userTask_returnRequestSchema.get("startDate");
    MetadataBuilder userTask_returnRequest_status = userTask_returnRequestSchema.get("status");
    MetadataBuilder userTask_returnRequest_statusType = userTask_returnRequestSchema.get("statusType");
    MetadataBuilder userTask_returnRequest_taskCollaborators = userTask_returnRequestSchema.get("taskCollaborators");
    MetadataBuilder userTask_returnRequest_taskCollaboratorsGroups = userTask_returnRequestSchema.get("taskCollaboratorsGroups");
    MetadataBuilder userTask_returnRequest_taskCollaboratorsGroupsWriteAuthorizations = userTask_returnRequestSchema.get("taskCollaboratorsGroupsWriteAuthorizations");
    MetadataBuilder userTask_returnRequest_taskCollaboratorsWriteAuthorizations = userTask_returnRequestSchema.get("taskCollaboratorsWriteAuthorizations");
    MetadataBuilder userTask_returnRequest_taskFollowers = userTask_returnRequestSchema.get("taskFollowers");
    MetadataBuilder userTask_returnRequest_taskFollowersIds = userTask_returnRequestSchema.get("taskFollowersIds");
    MetadataBuilder userTask_returnRequest_title = userTask_returnRequestSchema.get("title");
    MetadataBuilder userTask_returnRequest_tokens = userTask_returnRequestSchema.get("tokens");
    MetadataBuilder userTask_returnRequest_tokensHierarchy = userTask_returnRequestSchema.get("tokensHierarchy");
    MetadataBuilder userTask_returnRequest_type = userTask_returnRequestSchema.get("type");
    MetadataBuilder userTask_returnRequest_visibleInTrees = userTask_returnRequestSchema.get("visibleInTrees");
    MetadataBuilder userTask_returnRequest_workHours = userTask_returnRequestSchema.get("workHours");
    MetadataBuilder userTask_returnRequest_workflow = userTask_returnRequestSchema.get("workflow");
    MetadataBuilder userTask_returnRequest_workflowInstance = userTask_returnRequestSchema.get("workflowInstance");
  }

  private void createDdvTaskStatusSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder ddvTaskStatusSchemaType, MetadataSchemaBuilder ddvTaskStatusSchema) {
  }

  private void createUserFolderSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder userFolderSchemaType, MetadataSchemaBuilder userFolderSchema) {
  }

  private void createReportSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder reportSchemaType, MetadataSchemaBuilder reportSchema) {
  }

  private void createCategorySchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder categorySchemaType, MetadataSchemaBuilder categorySchema) {
  }

  private void createTemporaryRecordSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder temporaryRecordSchemaType, MetadataSchemaBuilder temporaryRecord_ConsolidatedPdfSchema, MetadataSchemaBuilder temporaryRecord_batchProcessReportSchema, MetadataSchemaBuilder temporaryRecord_exportAuditSchema, MetadataSchemaBuilder temporaryRecord_importAuditSchema, MetadataSchemaBuilder temporaryRecord_scriptReportSchema, MetadataSchemaBuilder temporaryRecord_sipArchiveSchema, MetadataSchemaBuilder temporaryRecord_vaultScanReportSchema, MetadataSchemaBuilder temporaryRecordSchema) {
    MetadataBuilder temporaryRecord_ConsolidatedPdf_allReferences = temporaryRecord_ConsolidatedPdfSchema.get("allReferences");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_allRemovedAuths = temporaryRecord_ConsolidatedPdfSchema.get("allRemovedAuths");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_attachedAncestors = temporaryRecord_ConsolidatedPdfSchema.get("attachedAncestors");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_autocomplete = temporaryRecord_ConsolidatedPdfSchema.get("autocomplete");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_caption = temporaryRecord_ConsolidatedPdfSchema.get("caption");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_content = temporaryRecord_ConsolidatedPdfSchema.get("content");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_createdBy = temporaryRecord_ConsolidatedPdfSchema.get("createdBy");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_createdOn = temporaryRecord_ConsolidatedPdfSchema.get("createdOn");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_daysBeforeDestruction = temporaryRecord_ConsolidatedPdfSchema.get("daysBeforeDestruction");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_deleted = temporaryRecord_ConsolidatedPdfSchema.get("deleted");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_denyTokens = temporaryRecord_ConsolidatedPdfSchema.get("denyTokens");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_destructionDate = temporaryRecord_ConsolidatedPdfSchema.get("destructionDate");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_detachedauthorizations = temporaryRecord_ConsolidatedPdfSchema.get("detachedauthorizations");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_errorOnPhysicalDeletion = temporaryRecord_ConsolidatedPdfSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_estimatedSize = temporaryRecord_ConsolidatedPdfSchema.get("estimatedSize");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_hidden = temporaryRecord_ConsolidatedPdfSchema.get("hidden");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_id = temporaryRecord_ConsolidatedPdfSchema.get("id");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_legacyIdentifier = temporaryRecord_ConsolidatedPdfSchema.get("legacyIdentifier");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_logicallyDeletedOn = temporaryRecord_ConsolidatedPdfSchema.get("logicallyDeletedOn");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_manualTokens = temporaryRecord_ConsolidatedPdfSchema.get("manualTokens");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_markedForParsing = temporaryRecord_ConsolidatedPdfSchema.get("markedForParsing");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_markedForPreviewConversion = temporaryRecord_ConsolidatedPdfSchema.get("markedForPreviewConversion");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_markedForReindexing = temporaryRecord_ConsolidatedPdfSchema.get("markedForReindexing");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_migrationDataVersion = temporaryRecord_ConsolidatedPdfSchema.get("migrationDataVersion");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_modifiedBy = temporaryRecord_ConsolidatedPdfSchema.get("modifiedBy");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_modifiedOn = temporaryRecord_ConsolidatedPdfSchema.get("modifiedOn");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_path = temporaryRecord_ConsolidatedPdfSchema.get("path");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_pathParts = temporaryRecord_ConsolidatedPdfSchema.get("pathParts");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_principalpath = temporaryRecord_ConsolidatedPdfSchema.get("principalpath");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_removedauthorizations = temporaryRecord_ConsolidatedPdfSchema.get("removedauthorizations");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_schema = temporaryRecord_ConsolidatedPdfSchema.get("schema");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_shareDenyTokens = temporaryRecord_ConsolidatedPdfSchema.get("shareDenyTokens");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_shareTokens = temporaryRecord_ConsolidatedPdfSchema.get("shareTokens");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_title = temporaryRecord_ConsolidatedPdfSchema.get("title");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_tokens = temporaryRecord_ConsolidatedPdfSchema.get("tokens");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_tokensHierarchy = temporaryRecord_ConsolidatedPdfSchema.get("tokensHierarchy");
    MetadataBuilder temporaryRecord_ConsolidatedPdf_visibleInTrees = temporaryRecord_ConsolidatedPdfSchema.get("visibleInTrees");
    MetadataBuilder temporaryRecord_batchProcessReport_allReferences = temporaryRecord_batchProcessReportSchema.get("allReferences");
    MetadataBuilder temporaryRecord_batchProcessReport_allRemovedAuths = temporaryRecord_batchProcessReportSchema.get("allRemovedAuths");
    MetadataBuilder temporaryRecord_batchProcessReport_attachedAncestors = temporaryRecord_batchProcessReportSchema.get("attachedAncestors");
    MetadataBuilder temporaryRecord_batchProcessReport_autocomplete = temporaryRecord_batchProcessReportSchema.get("autocomplete");
    MetadataBuilder temporaryRecord_batchProcessReport_caption = temporaryRecord_batchProcessReportSchema.get("caption");
    MetadataBuilder temporaryRecord_batchProcessReport_content = temporaryRecord_batchProcessReportSchema.get("content");
    MetadataBuilder temporaryRecord_batchProcessReport_createdBy = temporaryRecord_batchProcessReportSchema.get("createdBy");
    MetadataBuilder temporaryRecord_batchProcessReport_createdOn = temporaryRecord_batchProcessReportSchema.get("createdOn");
    MetadataBuilder temporaryRecord_batchProcessReport_daysBeforeDestruction = temporaryRecord_batchProcessReportSchema.get("daysBeforeDestruction");
    MetadataBuilder temporaryRecord_batchProcessReport_deleted = temporaryRecord_batchProcessReportSchema.get("deleted");
    MetadataBuilder temporaryRecord_batchProcessReport_denyTokens = temporaryRecord_batchProcessReportSchema.get("denyTokens");
    MetadataBuilder temporaryRecord_batchProcessReport_destructionDate = temporaryRecord_batchProcessReportSchema.get("destructionDate");
    MetadataBuilder temporaryRecord_batchProcessReport_detachedauthorizations = temporaryRecord_batchProcessReportSchema.get("detachedauthorizations");
    MetadataBuilder temporaryRecord_batchProcessReport_errorOnPhysicalDeletion = temporaryRecord_batchProcessReportSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder temporaryRecord_batchProcessReport_estimatedSize = temporaryRecord_batchProcessReportSchema.get("estimatedSize");
    MetadataBuilder temporaryRecord_batchProcessReport_hidden = temporaryRecord_batchProcessReportSchema.get("hidden");
    MetadataBuilder temporaryRecord_batchProcessReport_id = temporaryRecord_batchProcessReportSchema.get("id");
    MetadataBuilder temporaryRecord_batchProcessReport_legacyIdentifier = temporaryRecord_batchProcessReportSchema.get("legacyIdentifier");
    MetadataBuilder temporaryRecord_batchProcessReport_logicallyDeletedOn = temporaryRecord_batchProcessReportSchema.get("logicallyDeletedOn");
    MetadataBuilder temporaryRecord_batchProcessReport_manualTokens = temporaryRecord_batchProcessReportSchema.get("manualTokens");
    MetadataBuilder temporaryRecord_batchProcessReport_markedForParsing = temporaryRecord_batchProcessReportSchema.get("markedForParsing");
    MetadataBuilder temporaryRecord_batchProcessReport_markedForPreviewConversion = temporaryRecord_batchProcessReportSchema.get("markedForPreviewConversion");
    MetadataBuilder temporaryRecord_batchProcessReport_markedForReindexing = temporaryRecord_batchProcessReportSchema.get("markedForReindexing");
    MetadataBuilder temporaryRecord_batchProcessReport_migrationDataVersion = temporaryRecord_batchProcessReportSchema.get("migrationDataVersion");
    MetadataBuilder temporaryRecord_batchProcessReport_modifiedBy = temporaryRecord_batchProcessReportSchema.get("modifiedBy");
    MetadataBuilder temporaryRecord_batchProcessReport_modifiedOn = temporaryRecord_batchProcessReportSchema.get("modifiedOn");
    MetadataBuilder temporaryRecord_batchProcessReport_path = temporaryRecord_batchProcessReportSchema.get("path");
    MetadataBuilder temporaryRecord_batchProcessReport_pathParts = temporaryRecord_batchProcessReportSchema.get("pathParts");
    MetadataBuilder temporaryRecord_batchProcessReport_principalpath = temporaryRecord_batchProcessReportSchema.get("principalpath");
    MetadataBuilder temporaryRecord_batchProcessReport_removedauthorizations = temporaryRecord_batchProcessReportSchema.get("removedauthorizations");
    MetadataBuilder temporaryRecord_batchProcessReport_schema = temporaryRecord_batchProcessReportSchema.get("schema");
    MetadataBuilder temporaryRecord_batchProcessReport_shareDenyTokens = temporaryRecord_batchProcessReportSchema.get("shareDenyTokens");
    MetadataBuilder temporaryRecord_batchProcessReport_shareTokens = temporaryRecord_batchProcessReportSchema.get("shareTokens");
    MetadataBuilder temporaryRecord_batchProcessReport_title = temporaryRecord_batchProcessReportSchema.get("title");
    MetadataBuilder temporaryRecord_batchProcessReport_tokens = temporaryRecord_batchProcessReportSchema.get("tokens");
    MetadataBuilder temporaryRecord_batchProcessReport_tokensHierarchy = temporaryRecord_batchProcessReportSchema.get("tokensHierarchy");
    MetadataBuilder temporaryRecord_batchProcessReport_visibleInTrees = temporaryRecord_batchProcessReportSchema.get("visibleInTrees");
    MetadataBuilder temporaryRecord_exportAudit_allReferences = temporaryRecord_exportAuditSchema.get("allReferences");
    MetadataBuilder temporaryRecord_exportAudit_allRemovedAuths = temporaryRecord_exportAuditSchema.get("allRemovedAuths");
    MetadataBuilder temporaryRecord_exportAudit_attachedAncestors = temporaryRecord_exportAuditSchema.get("attachedAncestors");
    MetadataBuilder temporaryRecord_exportAudit_autocomplete = temporaryRecord_exportAuditSchema.get("autocomplete");
    MetadataBuilder temporaryRecord_exportAudit_caption = temporaryRecord_exportAuditSchema.get("caption");
    MetadataBuilder temporaryRecord_exportAudit_content = temporaryRecord_exportAuditSchema.get("content");
    MetadataBuilder temporaryRecord_exportAudit_createdBy = temporaryRecord_exportAuditSchema.get("createdBy");
    MetadataBuilder temporaryRecord_exportAudit_createdOn = temporaryRecord_exportAuditSchema.get("createdOn");
    MetadataBuilder temporaryRecord_exportAudit_daysBeforeDestruction = temporaryRecord_exportAuditSchema.get("daysBeforeDestruction");
    MetadataBuilder temporaryRecord_exportAudit_deleted = temporaryRecord_exportAuditSchema.get("deleted");
    MetadataBuilder temporaryRecord_exportAudit_denyTokens = temporaryRecord_exportAuditSchema.get("denyTokens");
    MetadataBuilder temporaryRecord_exportAudit_destructionDate = temporaryRecord_exportAuditSchema.get("destructionDate");
    MetadataBuilder temporaryRecord_exportAudit_detachedauthorizations = temporaryRecord_exportAuditSchema.get("detachedauthorizations");
    MetadataBuilder temporaryRecord_exportAudit_errorOnPhysicalDeletion = temporaryRecord_exportAuditSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder temporaryRecord_exportAudit_estimatedSize = temporaryRecord_exportAuditSchema.get("estimatedSize");
    MetadataBuilder temporaryRecord_exportAudit_hidden = temporaryRecord_exportAuditSchema.get("hidden");
    MetadataBuilder temporaryRecord_exportAudit_id = temporaryRecord_exportAuditSchema.get("id");
    MetadataBuilder temporaryRecord_exportAudit_legacyIdentifier = temporaryRecord_exportAuditSchema.get("legacyIdentifier");
    MetadataBuilder temporaryRecord_exportAudit_logicallyDeletedOn = temporaryRecord_exportAuditSchema.get("logicallyDeletedOn");
    MetadataBuilder temporaryRecord_exportAudit_manualTokens = temporaryRecord_exportAuditSchema.get("manualTokens");
    MetadataBuilder temporaryRecord_exportAudit_markedForParsing = temporaryRecord_exportAuditSchema.get("markedForParsing");
    MetadataBuilder temporaryRecord_exportAudit_markedForPreviewConversion = temporaryRecord_exportAuditSchema.get("markedForPreviewConversion");
    MetadataBuilder temporaryRecord_exportAudit_markedForReindexing = temporaryRecord_exportAuditSchema.get("markedForReindexing");
    MetadataBuilder temporaryRecord_exportAudit_migrationDataVersion = temporaryRecord_exportAuditSchema.get("migrationDataVersion");
    MetadataBuilder temporaryRecord_exportAudit_modifiedBy = temporaryRecord_exportAuditSchema.get("modifiedBy");
    MetadataBuilder temporaryRecord_exportAudit_modifiedOn = temporaryRecord_exportAuditSchema.get("modifiedOn");
    MetadataBuilder temporaryRecord_exportAudit_path = temporaryRecord_exportAuditSchema.get("path");
    MetadataBuilder temporaryRecord_exportAudit_pathParts = temporaryRecord_exportAuditSchema.get("pathParts");
    MetadataBuilder temporaryRecord_exportAudit_principalpath = temporaryRecord_exportAuditSchema.get("principalpath");
    MetadataBuilder temporaryRecord_exportAudit_removedauthorizations = temporaryRecord_exportAuditSchema.get("removedauthorizations");
    MetadataBuilder temporaryRecord_exportAudit_schema = temporaryRecord_exportAuditSchema.get("schema");
    MetadataBuilder temporaryRecord_exportAudit_shareDenyTokens = temporaryRecord_exportAuditSchema.get("shareDenyTokens");
    MetadataBuilder temporaryRecord_exportAudit_shareTokens = temporaryRecord_exportAuditSchema.get("shareTokens");
    MetadataBuilder temporaryRecord_exportAudit_title = temporaryRecord_exportAuditSchema.get("title");
    MetadataBuilder temporaryRecord_exportAudit_tokens = temporaryRecord_exportAuditSchema.get("tokens");
    MetadataBuilder temporaryRecord_exportAudit_tokensHierarchy = temporaryRecord_exportAuditSchema.get("tokensHierarchy");
    MetadataBuilder temporaryRecord_exportAudit_visibleInTrees = temporaryRecord_exportAuditSchema.get("visibleInTrees");
    MetadataBuilder temporaryRecord_importAudit_allReferences = temporaryRecord_importAuditSchema.get("allReferences");
    MetadataBuilder temporaryRecord_importAudit_allRemovedAuths = temporaryRecord_importAuditSchema.get("allRemovedAuths");
    MetadataBuilder temporaryRecord_importAudit_attachedAncestors = temporaryRecord_importAuditSchema.get("attachedAncestors");
    MetadataBuilder temporaryRecord_importAudit_autocomplete = temporaryRecord_importAuditSchema.get("autocomplete");
    MetadataBuilder temporaryRecord_importAudit_caption = temporaryRecord_importAuditSchema.get("caption");
    MetadataBuilder temporaryRecord_importAudit_content = temporaryRecord_importAuditSchema.get("content");
    MetadataBuilder temporaryRecord_importAudit_createdBy = temporaryRecord_importAuditSchema.get("createdBy");
    MetadataBuilder temporaryRecord_importAudit_createdOn = temporaryRecord_importAuditSchema.get("createdOn");
    MetadataBuilder temporaryRecord_importAudit_daysBeforeDestruction = temporaryRecord_importAuditSchema.get("daysBeforeDestruction");
    MetadataBuilder temporaryRecord_importAudit_deleted = temporaryRecord_importAuditSchema.get("deleted");
    MetadataBuilder temporaryRecord_importAudit_denyTokens = temporaryRecord_importAuditSchema.get("denyTokens");
    MetadataBuilder temporaryRecord_importAudit_destructionDate = temporaryRecord_importAuditSchema.get("destructionDate");
    MetadataBuilder temporaryRecord_importAudit_detachedauthorizations = temporaryRecord_importAuditSchema.get("detachedauthorizations");
    MetadataBuilder temporaryRecord_importAudit_errorOnPhysicalDeletion = temporaryRecord_importAuditSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder temporaryRecord_importAudit_estimatedSize = temporaryRecord_importAuditSchema.get("estimatedSize");
    MetadataBuilder temporaryRecord_importAudit_hidden = temporaryRecord_importAuditSchema.get("hidden");
    MetadataBuilder temporaryRecord_importAudit_id = temporaryRecord_importAuditSchema.get("id");
    MetadataBuilder temporaryRecord_importAudit_legacyIdentifier = temporaryRecord_importAuditSchema.get("legacyIdentifier");
    MetadataBuilder temporaryRecord_importAudit_logicallyDeletedOn = temporaryRecord_importAuditSchema.get("logicallyDeletedOn");
    MetadataBuilder temporaryRecord_importAudit_manualTokens = temporaryRecord_importAuditSchema.get("manualTokens");
    MetadataBuilder temporaryRecord_importAudit_markedForParsing = temporaryRecord_importAuditSchema.get("markedForParsing");
    MetadataBuilder temporaryRecord_importAudit_markedForPreviewConversion = temporaryRecord_importAuditSchema.get("markedForPreviewConversion");
    MetadataBuilder temporaryRecord_importAudit_markedForReindexing = temporaryRecord_importAuditSchema.get("markedForReindexing");
    MetadataBuilder temporaryRecord_importAudit_migrationDataVersion = temporaryRecord_importAuditSchema.get("migrationDataVersion");
    MetadataBuilder temporaryRecord_importAudit_modifiedBy = temporaryRecord_importAuditSchema.get("modifiedBy");
    MetadataBuilder temporaryRecord_importAudit_modifiedOn = temporaryRecord_importAuditSchema.get("modifiedOn");
    MetadataBuilder temporaryRecord_importAudit_path = temporaryRecord_importAuditSchema.get("path");
    MetadataBuilder temporaryRecord_importAudit_pathParts = temporaryRecord_importAuditSchema.get("pathParts");
    MetadataBuilder temporaryRecord_importAudit_principalpath = temporaryRecord_importAuditSchema.get("principalpath");
    MetadataBuilder temporaryRecord_importAudit_removedauthorizations = temporaryRecord_importAuditSchema.get("removedauthorizations");
    MetadataBuilder temporaryRecord_importAudit_schema = temporaryRecord_importAuditSchema.get("schema");
    MetadataBuilder temporaryRecord_importAudit_shareDenyTokens = temporaryRecord_importAuditSchema.get("shareDenyTokens");
    MetadataBuilder temporaryRecord_importAudit_shareTokens = temporaryRecord_importAuditSchema.get("shareTokens");
    MetadataBuilder temporaryRecord_importAudit_title = temporaryRecord_importAuditSchema.get("title");
    MetadataBuilder temporaryRecord_importAudit_tokens = temporaryRecord_importAuditSchema.get("tokens");
    MetadataBuilder temporaryRecord_importAudit_tokensHierarchy = temporaryRecord_importAuditSchema.get("tokensHierarchy");
    MetadataBuilder temporaryRecord_importAudit_visibleInTrees = temporaryRecord_importAuditSchema.get("visibleInTrees");
    MetadataBuilder temporaryRecord_scriptReport_allReferences = temporaryRecord_scriptReportSchema.get("allReferences");
    MetadataBuilder temporaryRecord_scriptReport_allRemovedAuths = temporaryRecord_scriptReportSchema.get("allRemovedAuths");
    MetadataBuilder temporaryRecord_scriptReport_attachedAncestors = temporaryRecord_scriptReportSchema.get("attachedAncestors");
    MetadataBuilder temporaryRecord_scriptReport_autocomplete = temporaryRecord_scriptReportSchema.get("autocomplete");
    MetadataBuilder temporaryRecord_scriptReport_caption = temporaryRecord_scriptReportSchema.get("caption");
    MetadataBuilder temporaryRecord_scriptReport_content = temporaryRecord_scriptReportSchema.get("content");
    MetadataBuilder temporaryRecord_scriptReport_createdBy = temporaryRecord_scriptReportSchema.get("createdBy");
    MetadataBuilder temporaryRecord_scriptReport_createdOn = temporaryRecord_scriptReportSchema.get("createdOn");
    MetadataBuilder temporaryRecord_scriptReport_daysBeforeDestruction = temporaryRecord_scriptReportSchema.get("daysBeforeDestruction");
    MetadataBuilder temporaryRecord_scriptReport_deleted = temporaryRecord_scriptReportSchema.get("deleted");
    MetadataBuilder temporaryRecord_scriptReport_denyTokens = temporaryRecord_scriptReportSchema.get("denyTokens");
    MetadataBuilder temporaryRecord_scriptReport_destructionDate = temporaryRecord_scriptReportSchema.get("destructionDate");
    MetadataBuilder temporaryRecord_scriptReport_detachedauthorizations = temporaryRecord_scriptReportSchema.get("detachedauthorizations");
    MetadataBuilder temporaryRecord_scriptReport_errorOnPhysicalDeletion = temporaryRecord_scriptReportSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder temporaryRecord_scriptReport_estimatedSize = temporaryRecord_scriptReportSchema.get("estimatedSize");
    MetadataBuilder temporaryRecord_scriptReport_hidden = temporaryRecord_scriptReportSchema.get("hidden");
    MetadataBuilder temporaryRecord_scriptReport_id = temporaryRecord_scriptReportSchema.get("id");
    MetadataBuilder temporaryRecord_scriptReport_legacyIdentifier = temporaryRecord_scriptReportSchema.get("legacyIdentifier");
    MetadataBuilder temporaryRecord_scriptReport_logicallyDeletedOn = temporaryRecord_scriptReportSchema.get("logicallyDeletedOn");
    MetadataBuilder temporaryRecord_scriptReport_manualTokens = temporaryRecord_scriptReportSchema.get("manualTokens");
    MetadataBuilder temporaryRecord_scriptReport_markedForParsing = temporaryRecord_scriptReportSchema.get("markedForParsing");
    MetadataBuilder temporaryRecord_scriptReport_markedForPreviewConversion = temporaryRecord_scriptReportSchema.get("markedForPreviewConversion");
    MetadataBuilder temporaryRecord_scriptReport_markedForReindexing = temporaryRecord_scriptReportSchema.get("markedForReindexing");
    MetadataBuilder temporaryRecord_scriptReport_migrationDataVersion = temporaryRecord_scriptReportSchema.get("migrationDataVersion");
    MetadataBuilder temporaryRecord_scriptReport_modifiedBy = temporaryRecord_scriptReportSchema.get("modifiedBy");
    MetadataBuilder temporaryRecord_scriptReport_modifiedOn = temporaryRecord_scriptReportSchema.get("modifiedOn");
    MetadataBuilder temporaryRecord_scriptReport_path = temporaryRecord_scriptReportSchema.get("path");
    MetadataBuilder temporaryRecord_scriptReport_pathParts = temporaryRecord_scriptReportSchema.get("pathParts");
    MetadataBuilder temporaryRecord_scriptReport_principalpath = temporaryRecord_scriptReportSchema.get("principalpath");
    MetadataBuilder temporaryRecord_scriptReport_removedauthorizations = temporaryRecord_scriptReportSchema.get("removedauthorizations");
    MetadataBuilder temporaryRecord_scriptReport_schema = temporaryRecord_scriptReportSchema.get("schema");
    MetadataBuilder temporaryRecord_scriptReport_shareDenyTokens = temporaryRecord_scriptReportSchema.get("shareDenyTokens");
    MetadataBuilder temporaryRecord_scriptReport_shareTokens = temporaryRecord_scriptReportSchema.get("shareTokens");
    MetadataBuilder temporaryRecord_scriptReport_title = temporaryRecord_scriptReportSchema.get("title");
    MetadataBuilder temporaryRecord_scriptReport_tokens = temporaryRecord_scriptReportSchema.get("tokens");
    MetadataBuilder temporaryRecord_scriptReport_tokensHierarchy = temporaryRecord_scriptReportSchema.get("tokensHierarchy");
    MetadataBuilder temporaryRecord_scriptReport_visibleInTrees = temporaryRecord_scriptReportSchema.get("visibleInTrees");
    MetadataBuilder temporaryRecord_sipArchive_allReferences = temporaryRecord_sipArchiveSchema.get("allReferences");
    MetadataBuilder temporaryRecord_sipArchive_allRemovedAuths = temporaryRecord_sipArchiveSchema.get("allRemovedAuths");
    MetadataBuilder temporaryRecord_sipArchive_attachedAncestors = temporaryRecord_sipArchiveSchema.get("attachedAncestors");
    MetadataBuilder temporaryRecord_sipArchive_autocomplete = temporaryRecord_sipArchiveSchema.get("autocomplete");
    MetadataBuilder temporaryRecord_sipArchive_caption = temporaryRecord_sipArchiveSchema.get("caption");
    MetadataBuilder temporaryRecord_sipArchive_content = temporaryRecord_sipArchiveSchema.get("content");
    MetadataBuilder temporaryRecord_sipArchive_createdBy = temporaryRecord_sipArchiveSchema.get("createdBy");
    MetadataBuilder temporaryRecord_sipArchive_createdOn = temporaryRecord_sipArchiveSchema.get("createdOn");
    MetadataBuilder temporaryRecord_sipArchive_daysBeforeDestruction = temporaryRecord_sipArchiveSchema.get("daysBeforeDestruction");
    MetadataBuilder temporaryRecord_sipArchive_deleted = temporaryRecord_sipArchiveSchema.get("deleted");
    MetadataBuilder temporaryRecord_sipArchive_denyTokens = temporaryRecord_sipArchiveSchema.get("denyTokens");
    MetadataBuilder temporaryRecord_sipArchive_destructionDate = temporaryRecord_sipArchiveSchema.get("destructionDate");
    MetadataBuilder temporaryRecord_sipArchive_detachedauthorizations = temporaryRecord_sipArchiveSchema.get("detachedauthorizations");
    MetadataBuilder temporaryRecord_sipArchive_errorOnPhysicalDeletion = temporaryRecord_sipArchiveSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder temporaryRecord_sipArchive_estimatedSize = temporaryRecord_sipArchiveSchema.get("estimatedSize");
    MetadataBuilder temporaryRecord_sipArchive_hidden = temporaryRecord_sipArchiveSchema.get("hidden");
    MetadataBuilder temporaryRecord_sipArchive_id = temporaryRecord_sipArchiveSchema.get("id");
    MetadataBuilder temporaryRecord_sipArchive_legacyIdentifier = temporaryRecord_sipArchiveSchema.get("legacyIdentifier");
    MetadataBuilder temporaryRecord_sipArchive_logicallyDeletedOn = temporaryRecord_sipArchiveSchema.get("logicallyDeletedOn");
    MetadataBuilder temporaryRecord_sipArchive_manualTokens = temporaryRecord_sipArchiveSchema.get("manualTokens");
    MetadataBuilder temporaryRecord_sipArchive_markedForParsing = temporaryRecord_sipArchiveSchema.get("markedForParsing");
    MetadataBuilder temporaryRecord_sipArchive_markedForPreviewConversion = temporaryRecord_sipArchiveSchema.get("markedForPreviewConversion");
    MetadataBuilder temporaryRecord_sipArchive_markedForReindexing = temporaryRecord_sipArchiveSchema.get("markedForReindexing");
    MetadataBuilder temporaryRecord_sipArchive_migrationDataVersion = temporaryRecord_sipArchiveSchema.get("migrationDataVersion");
    MetadataBuilder temporaryRecord_sipArchive_modifiedBy = temporaryRecord_sipArchiveSchema.get("modifiedBy");
    MetadataBuilder temporaryRecord_sipArchive_modifiedOn = temporaryRecord_sipArchiveSchema.get("modifiedOn");
    MetadataBuilder temporaryRecord_sipArchive_path = temporaryRecord_sipArchiveSchema.get("path");
    MetadataBuilder temporaryRecord_sipArchive_pathParts = temporaryRecord_sipArchiveSchema.get("pathParts");
    MetadataBuilder temporaryRecord_sipArchive_principalpath = temporaryRecord_sipArchiveSchema.get("principalpath");
    MetadataBuilder temporaryRecord_sipArchive_removedauthorizations = temporaryRecord_sipArchiveSchema.get("removedauthorizations");
    MetadataBuilder temporaryRecord_sipArchive_schema = temporaryRecord_sipArchiveSchema.get("schema");
    MetadataBuilder temporaryRecord_sipArchive_shareDenyTokens = temporaryRecord_sipArchiveSchema.get("shareDenyTokens");
    MetadataBuilder temporaryRecord_sipArchive_shareTokens = temporaryRecord_sipArchiveSchema.get("shareTokens");
    MetadataBuilder temporaryRecord_sipArchive_title = temporaryRecord_sipArchiveSchema.get("title");
    MetadataBuilder temporaryRecord_sipArchive_tokens = temporaryRecord_sipArchiveSchema.get("tokens");
    MetadataBuilder temporaryRecord_sipArchive_tokensHierarchy = temporaryRecord_sipArchiveSchema.get("tokensHierarchy");
    MetadataBuilder temporaryRecord_sipArchive_visibleInTrees = temporaryRecord_sipArchiveSchema.get("visibleInTrees");
    MetadataBuilder temporaryRecord_vaultScanReport_allReferences = temporaryRecord_vaultScanReportSchema.get("allReferences");
    MetadataBuilder temporaryRecord_vaultScanReport_allRemovedAuths = temporaryRecord_vaultScanReportSchema.get("allRemovedAuths");
    MetadataBuilder temporaryRecord_vaultScanReport_attachedAncestors = temporaryRecord_vaultScanReportSchema.get("attachedAncestors");
    MetadataBuilder temporaryRecord_vaultScanReport_autocomplete = temporaryRecord_vaultScanReportSchema.get("autocomplete");
    MetadataBuilder temporaryRecord_vaultScanReport_caption = temporaryRecord_vaultScanReportSchema.get("caption");
    MetadataBuilder temporaryRecord_vaultScanReport_content = temporaryRecord_vaultScanReportSchema.get("content");
    MetadataBuilder temporaryRecord_vaultScanReport_createdBy = temporaryRecord_vaultScanReportSchema.get("createdBy");
    MetadataBuilder temporaryRecord_vaultScanReport_createdOn = temporaryRecord_vaultScanReportSchema.get("createdOn");
    MetadataBuilder temporaryRecord_vaultScanReport_daysBeforeDestruction = temporaryRecord_vaultScanReportSchema.get("daysBeforeDestruction");
    MetadataBuilder temporaryRecord_vaultScanReport_deleted = temporaryRecord_vaultScanReportSchema.get("deleted");
    MetadataBuilder temporaryRecord_vaultScanReport_denyTokens = temporaryRecord_vaultScanReportSchema.get("denyTokens");
    MetadataBuilder temporaryRecord_vaultScanReport_destructionDate = temporaryRecord_vaultScanReportSchema.get("destructionDate");
    MetadataBuilder temporaryRecord_vaultScanReport_detachedauthorizations = temporaryRecord_vaultScanReportSchema.get("detachedauthorizations");
    MetadataBuilder temporaryRecord_vaultScanReport_errorOnPhysicalDeletion = temporaryRecord_vaultScanReportSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder temporaryRecord_vaultScanReport_estimatedSize = temporaryRecord_vaultScanReportSchema.get("estimatedSize");
    MetadataBuilder temporaryRecord_vaultScanReport_hidden = temporaryRecord_vaultScanReportSchema.get("hidden");
    MetadataBuilder temporaryRecord_vaultScanReport_id = temporaryRecord_vaultScanReportSchema.get("id");
    MetadataBuilder temporaryRecord_vaultScanReport_legacyIdentifier = temporaryRecord_vaultScanReportSchema.get("legacyIdentifier");
    MetadataBuilder temporaryRecord_vaultScanReport_logicallyDeletedOn = temporaryRecord_vaultScanReportSchema.get("logicallyDeletedOn");
    MetadataBuilder temporaryRecord_vaultScanReport_manualTokens = temporaryRecord_vaultScanReportSchema.get("manualTokens");
    MetadataBuilder temporaryRecord_vaultScanReport_markedForParsing = temporaryRecord_vaultScanReportSchema.get("markedForParsing");
    MetadataBuilder temporaryRecord_vaultScanReport_markedForPreviewConversion = temporaryRecord_vaultScanReportSchema.get("markedForPreviewConversion");
    MetadataBuilder temporaryRecord_vaultScanReport_markedForReindexing = temporaryRecord_vaultScanReportSchema.get("markedForReindexing");
    MetadataBuilder temporaryRecord_vaultScanReport_migrationDataVersion = temporaryRecord_vaultScanReportSchema.get("migrationDataVersion");
    MetadataBuilder temporaryRecord_vaultScanReport_modifiedBy = temporaryRecord_vaultScanReportSchema.get("modifiedBy");
    MetadataBuilder temporaryRecord_vaultScanReport_modifiedOn = temporaryRecord_vaultScanReportSchema.get("modifiedOn");
    MetadataBuilder temporaryRecord_vaultScanReport_path = temporaryRecord_vaultScanReportSchema.get("path");
    MetadataBuilder temporaryRecord_vaultScanReport_pathParts = temporaryRecord_vaultScanReportSchema.get("pathParts");
    MetadataBuilder temporaryRecord_vaultScanReport_principalpath = temporaryRecord_vaultScanReportSchema.get("principalpath");
    MetadataBuilder temporaryRecord_vaultScanReport_removedauthorizations = temporaryRecord_vaultScanReportSchema.get("removedauthorizations");
    MetadataBuilder temporaryRecord_vaultScanReport_schema = temporaryRecord_vaultScanReportSchema.get("schema");
    MetadataBuilder temporaryRecord_vaultScanReport_shareDenyTokens = temporaryRecord_vaultScanReportSchema.get("shareDenyTokens");
    MetadataBuilder temporaryRecord_vaultScanReport_shareTokens = temporaryRecord_vaultScanReportSchema.get("shareTokens");
    MetadataBuilder temporaryRecord_vaultScanReport_title = temporaryRecord_vaultScanReportSchema.get("title");
    MetadataBuilder temporaryRecord_vaultScanReport_tokens = temporaryRecord_vaultScanReportSchema.get("tokens");
    MetadataBuilder temporaryRecord_vaultScanReport_tokensHierarchy = temporaryRecord_vaultScanReportSchema.get("tokensHierarchy");
    MetadataBuilder temporaryRecord_vaultScanReport_visibleInTrees = temporaryRecord_vaultScanReportSchema.get("visibleInTrees");
  }

  private void createUserSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder userSchemaType, MetadataSchemaBuilder userSchema) {
  }

  private void createFacetSchemaTypeMetadatas(MetadataSchemaTypesBuilder types, MetadataSchemaTypeBuilder facetSchemaType, MetadataSchemaBuilder facet_fieldSchema, MetadataSchemaBuilder facet_querySchema, MetadataSchemaBuilder facetSchema) {
    MetadataBuilder facet_field_active = facet_fieldSchema.get("active");
    MetadataBuilder facet_field_allReferences = facet_fieldSchema.get("allReferences");
    MetadataBuilder facet_field_allRemovedAuths = facet_fieldSchema.get("allRemovedAuths");
    MetadataBuilder facet_field_attachedAncestors = facet_fieldSchema.get("attachedAncestors");
    MetadataBuilder facet_field_autocomplete = facet_fieldSchema.get("autocomplete");
    MetadataBuilder facet_field_caption = facet_fieldSchema.get("caption");
    MetadataBuilder facet_field_createdBy = facet_fieldSchema.get("createdBy");
    MetadataBuilder facet_field_createdOn = facet_fieldSchema.get("createdOn");
    MetadataBuilder facet_field_deleted = facet_fieldSchema.get("deleted");
    MetadataBuilder facet_field_denyTokens = facet_fieldSchema.get("denyTokens");
    MetadataBuilder facet_field_detachedauthorizations = facet_fieldSchema.get("detachedauthorizations");
    MetadataBuilder facet_field_elementPerPage = facet_fieldSchema.get("elementPerPage");
    MetadataBuilder facet_field_errorOnPhysicalDeletion = facet_fieldSchema.get("errorOnPhysicalDeletion");
    MetadataBuilder facet_field_estimatedSize = facet_fieldSchema.get("estimatedSize");
    MetadataBuilder facet_field_facetType = facet_fieldSchema.get("facetType");
    MetadataBuilder facet_field_fieldDatastoreCode = facet_fieldSchema.get("fieldDatastoreCode");
    MetadataBuilder facet_field_hidden = facet_fieldSchema.get("hidden");
    MetadataBuilder facet_field_id = facet_fieldSchema.get("id");
    MetadataBuilder facet_field_legacyIdentifier = facet_fieldSchema.get("legacyIdentifier");
    MetadataBuilder facet_field_logicallyDeletedOn = facet_fieldSchema.get("logicallyDeletedOn");
    MetadataBuilder facet_field_manualTokens = facet_fieldSchema.get("manualTokens");
    MetadataBuilder facet_field_markedForParsing = facet_fieldSchema.get("markedForParsing");
    MetadataBuilder facet_field_markedForPreviewConversion = facet_fieldSchema.get("markedForPreviewConversion");
    MetadataBuilder facet_field_markedForReindexing = facet_fieldSchema.get("markedForReindexing");
    MetadataBuilder facet_field_migrationDataVersion = facet_fieldSchema.get("migrationDataVersion");
    MetadataBuilder facet_field_modifiedBy = facet_fieldSchema.get("modifiedBy");
    MetadataBuilder facet_field_modifiedOn = facet_fieldSchema.get("modifiedOn");
    MetadataBuilder facet_field_openByDefault = facet_fieldSchema.get("openByDefault");
    MetadataBuilder facet_field_order = facet_fieldSchema.get("order");
    MetadataBuilder facet_field_orderResult = facet_fieldSchema.get("orderResult");
    MetadataBuilder facet_field_pages = facet_fieldSchema.get("pages");
    MetadataBuilder facet_field_path = facet_fieldSchema.get("path");
    MetadataBuilder facet_field_pathParts = facet_fieldSchema.get("pathParts");
    MetadataBuilder facet_field_principalpath = facet_fieldSchema.get("principalpath");
    MetadataBuilder facet_field_removedauthorizations = facet_fieldSchema.get("removedauthorizations");
    MetadataBuilder facet_field_schema = facet_fieldSchema.get("schema");
    MetadataBuilder facet_field_shareDenyTokens = facet_fieldSchema.get("shareDenyTokens");
    MetadataBuilder facet_field_shareTokens = facet_fieldSchema.get("shareTokens");
    MetadataBuilder facet_field_title = facet_fieldSchema.get("title");
    MetadataBuilder facet_field_tokens = facet_fieldSchema.get("tokens");
    MetadataBuilder facet_field_tokensHierarchy = facet_fieldSchema.get("tokensHierarchy");
    MetadataBuilder facet_field_usedByModule = facet_fieldSchema.get("usedByModule");
    MetadataBuilder facet_field_visibleInTrees = facet_fieldSchema.get("visibleInTrees");
    MetadataBuilder facet_query_active = facet_querySchema.get("active");
    MetadataBuilder facet_query_allReferences = facet_querySchema.get("allReferences");
    MetadataBuilder facet_query_allRemovedAuths = facet_querySchema.get("allRemovedAuths");
    MetadataBuilder facet_query_attachedAncestors = facet_querySchema.get("attachedAncestors");
    MetadataBuilder facet_query_autocomplete = facet_querySchema.get("autocomplete");
    MetadataBuilder facet_query_caption = facet_querySchema.get("caption");
    MetadataBuilder facet_query_createdBy = facet_querySchema.get("createdBy");
    MetadataBuilder facet_query_createdOn = facet_querySchema.get("createdOn");
    MetadataBuilder facet_query_deleted = facet_querySchema.get("deleted");
    MetadataBuilder facet_query_denyTokens = facet_querySchema.get("denyTokens");
    MetadataBuilder facet_query_detachedauthorizations = facet_querySchema.get("detachedauthorizations");
    MetadataBuilder facet_query_elementPerPage = facet_querySchema.get("elementPerPage");
    MetadataBuilder facet_query_errorOnPhysicalDeletion = facet_querySchema.get("errorOnPhysicalDeletion");
    MetadataBuilder facet_query_estimatedSize = facet_querySchema.get("estimatedSize");
    MetadataBuilder facet_query_facetType = facet_querySchema.get("facetType");
    MetadataBuilder facet_query_fieldDatastoreCode = facet_querySchema.get("fieldDatastoreCode");
    MetadataBuilder facet_query_hidden = facet_querySchema.get("hidden");
    MetadataBuilder facet_query_id = facet_querySchema.get("id");
    MetadataBuilder facet_query_legacyIdentifier = facet_querySchema.get("legacyIdentifier");
    MetadataBuilder facet_query_logicallyDeletedOn = facet_querySchema.get("logicallyDeletedOn");
    MetadataBuilder facet_query_manualTokens = facet_querySchema.get("manualTokens");
    MetadataBuilder facet_query_markedForParsing = facet_querySchema.get("markedForParsing");
    MetadataBuilder facet_query_markedForPreviewConversion = facet_querySchema.get("markedForPreviewConversion");
    MetadataBuilder facet_query_markedForReindexing = facet_querySchema.get("markedForReindexing");
    MetadataBuilder facet_query_migrationDataVersion = facet_querySchema.get("migrationDataVersion");
    MetadataBuilder facet_query_modifiedBy = facet_querySchema.get("modifiedBy");
    MetadataBuilder facet_query_modifiedOn = facet_querySchema.get("modifiedOn");
    MetadataBuilder facet_query_openByDefault = facet_querySchema.get("openByDefault");
    MetadataBuilder facet_query_order = facet_querySchema.get("order");
    MetadataBuilder facet_query_orderResult = facet_querySchema.get("orderResult");
    MetadataBuilder facet_query_pages = facet_querySchema.get("pages");
    MetadataBuilder facet_query_path = facet_querySchema.get("path");
    MetadataBuilder facet_query_pathParts = facet_querySchema.get("pathParts");
    MetadataBuilder facet_query_principalpath = facet_querySchema.get("principalpath");
    MetadataBuilder facet_query_removedauthorizations = facet_querySchema.get("removedauthorizations");
    MetadataBuilder facet_query_schema = facet_querySchema.get("schema");
    MetadataBuilder facet_query_shareDenyTokens = facet_querySchema.get("shareDenyTokens");
    MetadataBuilder facet_query_shareTokens = facet_querySchema.get("shareTokens");
    MetadataBuilder facet_query_title = facet_querySchema.get("title");
    MetadataBuilder facet_query_tokens = facet_querySchema.get("tokens");
    MetadataBuilder facet_query_tokensHierarchy = facet_querySchema.get("tokensHierarchy");
    MetadataBuilder facet_query_usedByModule = facet_querySchema.get("usedByModule");
    MetadataBuilder facet_query_visibleInTrees = facet_querySchema.get("visibleInTrees");
  }

  public void applySchemasDisplay(SchemasDisplayManager manager) {
    SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);
    SchemaTypesDisplayConfig typesConfig = manager.getTypes(collection);
    transaction.setModifiedCollectionTypes(manager.getTypes(collection).withFacetMetadataCodes(asList("folder_default_schema", "folder_default_archivisticStatus", "folder_default_category", "folder_default_administrativeUnit", "folder_default_filingSpace", "folder_default_mediumTypes", "folder_default_copyStatus")));
    transaction.add(manager.getType(collection, "actionParameters").withSimpleSearchStatus(false).withAdvancedSearchStatus(false).withManageableStatus(false).withMetadataGroup(resourcesProvider.getLanguageMap(asList("default", "default:tab.taxonomy", "tab.advanced", "tab.defaultValues", "tab.mappings", "tab.options"))));
    transaction.add(manager.getSchema(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan").withFormMetadataCodes(asList("actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultAdminUnit", "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCategory", "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultUniformSubdivision", "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultRetentionRule", "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCopyStatus", "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultOpenDate", "actionParameters_classifyConnectorFolderDirectlyInThePlan_actionAfterClassification", "actionParameters_classifyConnectorFolderDirectlyInThePlan_documentMapping", "actionParameters_classifyConnectorFolderDirectlyInThePlan_folderMapping", "actionParameters_classifyConnectorFolderDirectlyInThePlan_folderType", "actionParameters_classifyConnectorFolderDirectlyInThePlan_documentType")).withDisplayMetadataCodes(asList("actionParameters_classifyConnectorFolderDirectlyInThePlan_title", "actionParameters_classifyConnectorFolderDirectlyInThePlan_actionAfterClassification", "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultAdminUnit", "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCategory", "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCopyStatus", "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultOpenDate", "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultRetentionRule", "actionParameters_classifyConnectorFolderDirectlyInThePlan_documentMapping", "actionParameters_classifyConnectorFolderDirectlyInThePlan_folderMapping", "actionParameters_classifyConnectorFolderDirectlyInThePlan_folderType", "actionParameters_classifyConnectorFolderDirectlyInThePlan_documentType")).withSearchResultsMetadataCodes(asList("actionParameters_classifyConnectorFolderDirectlyInThePlan_title", "actionParameters_classifyConnectorFolderDirectlyInThePlan_modifiedOn")).withTableMetadataCodes(asList("actionParameters_classifyConnectorFolderDirectlyInThePlan_title", "actionParameters_classifyConnectorFolderDirectlyInThePlan_modifiedOn")));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan_actionAfterClassification").withMetadataGroup("tab.options").withInputType(MetadataInputType.RADIO_BUTTONS).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultAdminUnit").withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCategory").withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultCopyStatus").withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.RADIO_BUTTONS).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultOpenDate").withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.FIELD).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultRetentionRule").withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan_defaultUniformSubdivision").withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan_documentMapping").withMetadataGroup("tab.to.hide").withInputType(MetadataInputType.CONTENT).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan_documentType").withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan_folderMapping").withMetadataGroup("tab.to.hide").withInputType(MetadataInputType.CONTENT).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderDirectlyInThePlan_folderType").withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getSchema(collection, "actionParameters_classifyConnectorFolderInParentFolder").withFormMetadataCodes(asList("actionParameters_classifyConnectorFolderInParentFolder_defaultParentFolder", "actionParameters_classifyConnectorFolderInParentFolder_defaultOpenDate", "actionParameters_classifyConnectorFolderInParentFolder_actionAfterClassification", "actionParameters_classifyConnectorFolderInParentFolder_documentMapping", "actionParameters_classifyConnectorFolderInParentFolder_folderMapping", "actionParameters_classifyConnectorFolderInParentFolder_folderType", "actionParameters_classifyConnectorFolderInParentFolder_documentType")).withDisplayMetadataCodes(asList("actionParameters_classifyConnectorFolderInParentFolder_title", "actionParameters_classifyConnectorFolderInParentFolder_actionAfterClassification", "actionParameters_classifyConnectorFolderInParentFolder_defaultOpenDate", "actionParameters_classifyConnectorFolderInParentFolder_defaultParentFolder", "actionParameters_classifyConnectorFolderInParentFolder_documentMapping", "actionParameters_classifyConnectorFolderInParentFolder_folderMapping", "actionParameters_classifyConnectorFolderInParentFolder_folderType", "actionParameters_classifyConnectorFolderInParentFolder_documentType")).withSearchResultsMetadataCodes(asList("actionParameters_classifyConnectorFolderInParentFolder_title", "actionParameters_classifyConnectorFolderInParentFolder_modifiedOn")).withTableMetadataCodes(asList("actionParameters_classifyConnectorFolderInParentFolder_title", "actionParameters_classifyConnectorFolderInParentFolder_modifiedOn")));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderInParentFolder_actionAfterClassification").withMetadataGroup("tab.options").withInputType(MetadataInputType.RADIO_BUTTONS).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderInParentFolder_defaultOpenDate").withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.FIELD).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderInParentFolder_defaultParentFolder").withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderInParentFolder_documentMapping").withMetadataGroup("tab.to.hide").withInputType(MetadataInputType.CONTENT).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderInParentFolder_documentType").withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderInParentFolder_folderMapping").withMetadataGroup("tab.to.hide").withInputType(MetadataInputType.CONTENT).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorFolderInParentFolder_folderType").withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getSchema(collection, "actionParameters_classifyConnectorTaxonomy").withFormMetadataCodes(asList("actionParameters_classifyConnectorTaxonomy_inTaxonomy", "actionParameters_classifyConnectorTaxonomy_pathPrefix", "actionParameters_classifyConnectorTaxonomy_delimiter", "actionParameters_classifyConnectorTaxonomy_defaultParentFolder", "actionParameters_classifyConnectorTaxonomy_defaultAdminUnit", "actionParameters_classifyConnectorTaxonomy_defaultCategory", "actionParameters_classifyConnectorTaxonomy_defaultUniformSubdivision", "actionParameters_classifyConnectorTaxonomy_defaultRetentionRule", "actionParameters_classifyConnectorTaxonomy_defaultCopyStatus", "actionParameters_classifyConnectorTaxonomy_defaultOpenDate", "actionParameters_classifyConnectorTaxonomy_documentMapping", "actionParameters_classifyConnectorTaxonomy_folderMapping", "actionParameters_classifyConnectorTaxonomy_actionAfterClassification", "actionParameters_classifyConnectorTaxonomy_folderType", "actionParameters_classifyConnectorTaxonomy_documentType")).withDisplayMetadataCodes(asList("actionParameters_classifyConnectorTaxonomy_title", "actionParameters_classifyConnectorTaxonomy_actionAfterClassification", "actionParameters_classifyConnectorTaxonomy_defaultParentFolder", "actionParameters_classifyConnectorTaxonomy_defaultAdminUnit", "actionParameters_classifyConnectorTaxonomy_defaultCategory", "actionParameters_classifyConnectorTaxonomy_defaultCopyStatus", "actionParameters_classifyConnectorTaxonomy_defaultOpenDate", "actionParameters_classifyConnectorTaxonomy_defaultRetentionRule", "actionParameters_classifyConnectorTaxonomy_delimiter", "actionParameters_classifyConnectorTaxonomy_documentMapping", "actionParameters_classifyConnectorTaxonomy_folderMapping", "actionParameters_classifyConnectorTaxonomy_inTaxonomy", "actionParameters_classifyConnectorTaxonomy_pathPrefix", "actionParameters_classifyConnectorTaxonomy_folderType", "actionParameters_classifyConnectorTaxonomy_documentType")).withSearchResultsMetadataCodes(asList("actionParameters_classifyConnectorTaxonomy_title", "actionParameters_classifyConnectorTaxonomy_modifiedOn")).withTableMetadataCodes(asList("actionParameters_classifyConnectorTaxonomy_title", "actionParameters_classifyConnectorTaxonomy_modifiedOn")));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_actionAfterClassification").withMetadataGroup("tab.options").withInputType(MetadataInputType.RADIO_BUTTONS).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_defaultAdminUnit").withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_defaultCategory").withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_defaultCopyStatus").withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.RADIO_BUTTONS).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_defaultOpenDate").withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.FIELD).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_defaultParentFolder").withMetadataGroup("tab.to.hide").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_defaultRetentionRule").withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_defaultUniformSubdivision").withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_delimiter").withMetadataGroup("default:tab.taxonomy").withInputType(MetadataInputType.FIELD).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_documentMapping").withMetadataGroup("tab.to.hide").withInputType(MetadataInputType.CONTENT).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_documentType").withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_folderMapping").withMetadataGroup("tab.to.hide").withInputType(MetadataInputType.CONTENT).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_folderType").withMetadataGroup("tab.defaultValues").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_inTaxonomy").withMetadataGroup("default:tab.taxonomy").withInputType(MetadataInputType.FIELD).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifyConnectorTaxonomy_pathPrefix").withMetadataGroup("default:tab.taxonomy").withInputType(MetadataInputType.FIELD).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getSchema(collection, "actionParameters_classifySmbDocumentInFolder").withFormMetadataCodes(asList("actionParameters_classifySmbDocumentInFolder_inFolder", "actionParameters_classifySmbDocumentInFolder_documentType", "actionParameters_classifySmbDocumentInFolder_majorVersions", "actionParameters_classifySmbDocumentInFolder_actionAfterClassification")).withDisplayMetadataCodes(asList("actionParameters_classifySmbDocumentInFolder_title", "actionParameters_classifySmbDocumentInFolder_actionAfterClassification", "actionParameters_classifySmbDocumentInFolder_inFolder", "actionParameters_classifySmbDocumentInFolder_majorVersions")).withSearchResultsMetadataCodes(asList("actionParameters_classifySmbDocumentInFolder_title", "actionParameters_classifySmbDocumentInFolder_modifiedOn")).withTableMetadataCodes(asList("actionParameters_classifySmbDocumentInFolder_title", "actionParameters_classifySmbDocumentInFolder_modifiedOn")));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifySmbDocumentInFolder_actionAfterClassification").withMetadataGroup("tab.options").withInputType(MetadataInputType.RADIO_BUTTONS).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifySmbDocumentInFolder_documentType").withMetadataGroup("Valeurs par défaut").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifySmbDocumentInFolder_inFolder").withMetadataGroup("Valeurs par défaut").withInputType(MetadataInputType.LOOKUP).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    transaction.add(manager.getMetadata(collection, "actionParameters_classifySmbDocumentInFolder_majorVersions").withMetadataGroup("Valeurs par défaut").withInputType(MetadataInputType.FIELD).withHighlightStatus(false).withVisibleInAdvancedSearchStatus(false));
    manager.execute(transaction.build());
  }

  public void applyGeneratedRoles() {
    RolesManager rolesManager = appLayerFactory.getModelLayerFactory().getRolesManager();;
    rolesManager.updateRole(rolesManager.getRole(collection, "ADM").withNewPermissions(asList("core.accessDeleteAllTemporaryRecords", "core.batchProcess", "core.deleteContentVersion", "core.deletePublicSavedSearch", "core.ldapConfigurationManagement", "core.manageConnectors", "core.manageEmailServer", "core.manageExcelReport", "core.manageFacets", "core.manageGlobalLinks", "core.manageLabels", "core.manageMetadataExtractor", "core.manageMetadataSchemas", "core.managePrintableReport", "core.manageSearchBoost", "core.manageSecurity", "core.manageSystemCollections", "core.manageSystemConfiguration", "core.manageSystemDataImports", "core.manageSystemGroups", "core.manageSystemGroupsActivation", "core.manageSystemUpdates", "core.manageSystemUsers", "core.manageTaxonomies", "core.manageTrash", "core.manageValueList", "core.managerTemporaryRecords", "core.modifyPublicSavedSearch", "core.seeAllTemporaryRecords", "core.unlimitedBatchProcess", "core.useExternalAPIS", "core.viewEvents", "core.viewLoginNotificationAlert", "core.viewSystemBatchProcesses", "core.viewSystemState", "rm.borrowContainer", "rm.borrowFolder", "rm.borrowingFolderDirectly", "rm.borrowingRequestOnContainer", "rm.borrowingRequestOnFolder", "rm.cartBatchDelete", "rm.consultClassificationPlan", "rm.consultRetentionRule", "rm.createActiveFolderToSemiActiveDecommissioningList", "rm.createDecommissioningList", "rm.createDocuments", "rm.createFolders", "rm.createInactiveDocuments", "rm.createSemiActiveDocuments", "rm.createSubFolders", "rm.createSubFoldersInInactiveFolders", "rm.createSubFoldersInSemiActiveFolders", "rm.decommissioning", "rm.deleteBorrowedDocuments", "rm.deleteContainers", "rm.deleteInactiveDocuments", "rm.deleteInactiveFolders", "rm.deletePublishedDocuments", "rm.deleteSemiActiveDocuments", "rm.deleteSemiActiveFolders", "rm.displayContainers", "rm.duplicateInactiveFolders", "rm.duplicateSemiActiveFolders", "rm.editActiveFolderToSemiActiveDecommissioningList", "rm.editDecommissioningList", "rm.generateSIPArchives", "rm.manageBagInfo", "rm.manageClassificationPlan", "rm.manageContainers", "rm.manageDocumentAuthorizations", "rm.manageFolderAuthorizations", "rm.manageReports", "rm.manageRequestOnContainer", "rm.manageRequestOnFolder", "rm.manageRetentionRule", "rm.manageShare", "rm.manageStorageSpaces", "rm.manageUniformSubdivisions", "rm.modifyFolderDecomDate", "rm.modifyImportedDocuments", "rm.modifyImportedFolders", "rm.modifyInactiveBorrowedFolder", "rm.modifyInactiveDocuments", "rm.modifyInactiveFolders", "rm.modifyOpeningDateFolder", "rm.modifySemiActiveBorrowedFolder", "rm.modifySemiActiveDocuments", "rm.modifySemiActiveFolders", "rm.processDecommissioningList", "rm.publishAndUnpublishDocuments", "rm.reactivationRequestOnFolder", "rm.returnOtherUsersDocuments", "rm.returnOtherUsersFolders", "rm.shareDocuments", "rm.shareFolders", "rm.shareImportedDocuments", "rm.shareImportedFolders", "rm.shareInactiveDocuments", "rm.shareInactiveFolders", "rm.shareSemiActiveDocuments", "rm.shareSemiActiveFolders", "rm.uploadInactiveDocuments", "rm.uploadSemiActiveDocuments", "rm.useGroupCart", "rm.useMyCart", "rm.viewDocumentAuthorizations", "rm.viewFolderAuthorizations", "rm.viewSystemFilename", "robots.manageRobots", "tasks.manageWorkflows", "tasks.startWorkflows")));
    rolesManager.updateRole(rolesManager.getRole(collection, "U").withNewPermissions(asList("core.batchProcess", "rm.borrowContainer", "rm.borrowFolder", "rm.borrowingRequestOnContainer", "rm.borrowingRequestOnFolder", "rm.cartBatchDelete", "rm.createDocuments", "rm.createFolders", "rm.createSubFolders", "rm.deletePublishedDocuments", "rm.deleteSemiActiveDocuments", "rm.deleteSemiActiveFolders", "rm.modifySemiActiveBorrowedFolder", "rm.publishAndUnpublishDocuments", "rm.reactivationRequestOnFolder", "rm.shareDocuments", "rm.shareFolders", "rm.shareSemiActiveDocuments", "rm.shareSemiActiveFolders", "rm.uploadSemiActiveDocuments", "rm.useMyCart")));
    rolesManager.updateRole(rolesManager.getRole(collection, "M").withNewPermissions(asList("core.batchProcess", "core.viewLoginNotificationAlert", "manageLabels", "rm.borrowContainer", "rm.borrowFolder", "rm.borrowingRequestOnContainer", "rm.borrowingRequestOnFolder", "rm.cartBatchDelete", "rm.createActiveFolderToSemiActiveDecommissioningList", "rm.createDecommissioningList", "rm.createDocuments", "rm.createFolders", "rm.createSubFolders", "rm.decommissioning", "rm.deletePublishedDocuments", "rm.deleteSemiActiveDocuments", "rm.deleteSemiActiveFolders", "rm.editActiveFolderToSemiActiveDecommissioningList", "rm.manageContainers", "rm.manageDocumentAuthorizations", "rm.manageFolderAuthorizations", "rm.modifyOpeningDateFolder", "rm.modifySemiActiveBorrowedFolder", "rm.publishAndUnpublishDocuments", "rm.reactivationRequestOnFolder", "rm.shareDocuments", "rm.shareFolders", "rm.shareSemiActiveDocuments", "rm.shareSemiActiveFolders", "rm.uploadSemiActiveDocuments", "rm.useMyCart", "rm.viewFolderAuthorizations")));
    rolesManager.updateRole(rolesManager.getRole(collection, "RGD").withNewPermissions(asList("core.accessDeleteAllTemporaryRecords", "core.batchProcess", "core.deleteContentVersion", "core.deletePublicSavedSearch", "core.ldapConfigurationManagement", "core.manageConnectors", "core.manageEmailServer", "core.manageExcelReport", "core.manageFacets", "core.manageGlobalLinks", "core.manageLabels", "core.manageMetadataExtractor", "core.manageMetadataSchemas", "core.managePrintableReport", "core.manageSearchBoost", "core.manageSecurity", "core.manageSystemCollections", "core.manageSystemConfiguration", "core.manageSystemDataImports", "core.manageSystemGroups", "core.manageSystemGroupsActivation", "core.manageSystemUpdates", "core.manageSystemUsers", "core.manageTaxonomies", "core.manageTrash", "core.manageValueList", "core.managerTemporaryRecords", "core.modifyPublicSavedSearch", "core.seeAllTemporaryRecords", "core.unlimitedBatchProcess", "core.useExternalAPIS", "core.viewEvents", "core.viewLoginNotificationAlert", "core.viewSystemBatchProcesses", "core.viewSystemState", "rm.borrowContainer", "rm.borrowFolder", "rm.borrowingFolderDirectly", "rm.borrowingRequestOnContainer", "rm.borrowingRequestOnFolder", "rm.cartBatchDelete", "rm.consultClassificationPlan", "rm.consultRetentionRule", "rm.createActiveFolderToSemiActiveDecommissioningList", "rm.createDecommissioningList", "rm.createDocuments", "rm.createFolders", "rm.createInactiveDocuments", "rm.createSemiActiveDocuments", "rm.createSubFolders", "rm.createSubFoldersInInactiveFolders", "rm.createSubFoldersInSemiActiveFolders", "rm.decommissioning", "rm.deleteBorrowedDocuments", "rm.deleteContainers", "rm.deleteInactiveDocuments", "rm.deleteInactiveFolders", "rm.deletePublishedDocuments", "rm.deleteSemiActiveDocuments", "rm.deleteSemiActiveFolders", "rm.displayContainers", "rm.duplicateInactiveFolders", "rm.duplicateSemiActiveFolders", "rm.editActiveFolderToSemiActiveDecommissioningList", "rm.editDecommissioningList", "rm.generateSIPArchives", "rm.manageBagInfo", "rm.manageClassificationPlan", "rm.manageContainers", "rm.manageDocumentAuthorizations", "rm.manageFolderAuthorizations", "rm.manageReports", "rm.manageRequestOnContainer", "rm.manageRequestOnFolder", "rm.manageRetentionRule", "rm.manageShare", "rm.manageStorageSpaces", "rm.manageUniformSubdivisions", "rm.modifyFolderDecomDate", "rm.modifyImportedDocuments", "rm.modifyImportedFolders", "rm.modifyInactiveBorrowedFolder", "rm.modifyInactiveDocuments", "rm.modifyInactiveFolders", "rm.modifyOpeningDateFolder", "rm.modifySemiActiveBorrowedFolder", "rm.modifySemiActiveDocuments", "rm.modifySemiActiveFolders", "rm.processDecommissioningList", "rm.publishAndUnpublishDocuments", "rm.reactivationRequestOnFolder", "rm.returnOtherUsersDocuments", "rm.returnOtherUsersFolders", "rm.shareDocuments", "rm.shareFolders", "rm.shareImportedDocuments", "rm.shareImportedFolders", "rm.shareInactiveDocuments", "rm.shareInactiveFolders", "rm.shareSemiActiveDocuments", "rm.shareSemiActiveFolders", "rm.uploadInactiveDocuments", "rm.uploadSemiActiveDocuments", "rm.useGroupCart", "rm.useMyCart", "rm.viewDocumentAuthorizations", "rm.viewFolderAuthorizations", "rm.viewSystemFilename", "tasks.manageWorkflows")));
  }
}