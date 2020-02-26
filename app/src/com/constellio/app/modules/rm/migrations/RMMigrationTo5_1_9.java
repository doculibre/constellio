package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.rm.RMTypes;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.model.CopyRetentionRuleFactory;
import com.constellio.app.modules.rm.model.CopyRetentionRuleInRuleFactory;
import com.constellio.app.modules.rm.model.calculators.FolderCopyStatusCalculator3;
import com.constellio.app.modules.rm.model.calculators.category.CategoryCopyRetentionRulesOnDocumentTypesCalculator;
import com.constellio.app.modules.rm.model.calculators.category.CategoryLevelCalculator;
import com.constellio.app.modules.rm.model.calculators.decommissioningList.DecomListUniformCategoryCalculator2;
import com.constellio.app.modules.rm.model.calculators.decommissioningList.DecomListUniformCopyRuleCalculator2;
import com.constellio.app.modules.rm.model.calculators.decommissioningList.DecomListUniformCopyTypeCalculator2;
import com.constellio.app.modules.rm.model.calculators.decommissioningList.DecomListUniformRuleCalculator2;
import com.constellio.app.modules.rm.model.calculators.document.DocumentActualDepositDateCalculator;
import com.constellio.app.modules.rm.model.calculators.document.DocumentActualDestructionDateCalculator;
import com.constellio.app.modules.rm.model.calculators.document.DocumentActualTransferDateCalculator;
import com.constellio.app.modules.rm.model.calculators.document.DocumentApplicableCopyRulesCalculator;
import com.constellio.app.modules.rm.model.calculators.document.DocumentArchivisticStatusCalculator;
import com.constellio.app.modules.rm.model.calculators.document.DocumentExpectedDepositDateCalculator;
import com.constellio.app.modules.rm.model.calculators.document.DocumentExpectedDestructionDateCalculator;
import com.constellio.app.modules.rm.model.calculators.document.DocumentExpectedTransferDateCalculator;
import com.constellio.app.modules.rm.model.calculators.document.DocumentIsSameInactiveFateAsFolderCalculator;
import com.constellio.app.modules.rm.model.calculators.document.DocumentIsSameSemiActiveFateAsFolderCalculator;
import com.constellio.app.modules.rm.model.calculators.document.DocumentMainCopyRuleCalculator;
import com.constellio.app.modules.rm.model.calculators.document.DocumentRetentionRuleCalculator;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;

public class RMMigrationTo5_1_9 implements MigrationScript {
	@Override
	public String getVersion() {
		return "5.1.9";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
			throws Exception {
		new SchemaAlterationsFor5_1_9(collection, provider, factory).migrate();
		setupDisplayConfigs(collection, factory);
		updateDecommissioningPermissions(collection, factory);
	}

	private void setupDisplayConfigs(String collection, AppLayerFactory factory) {
		SchemasDisplayManager schemaDisplayManager = factory.getMetadataSchemasDisplayManager();

		SchemaTypesDisplayTransactionBuilder transaction = schemaDisplayManager.newTransactionBuilderFor(collection);

		transaction.in(RetentionRule.SCHEMA_TYPE).addToDisplay(
				RetentionRule.SCOPE,
				RetentionRule.PRINCIPAL_DEFAULT_DOCUMENT_COPY_RETENTION_RULE,
				RetentionRule.SECONDARY_DEFAULT_DOCUMENT_COPY_RETENTION_RULE,
				RetentionRule.DOCUMENT_COPY_RETENTION_RULES
		).beforeTheHugeCommentMetadata();

		transaction.in(RetentionRule.SCHEMA_TYPE)
				.addToForm(RetentionRule.SCOPE).atFirstPosition()
				.in(RetentionRule.SCHEMA_TYPE).addToForm(
				RetentionRule.PRINCIPAL_DEFAULT_DOCUMENT_COPY_RETENTION_RULE,
				RetentionRule.SECONDARY_DEFAULT_DOCUMENT_COPY_RETENTION_RULE,
				RetentionRule.DOCUMENT_COPY_RETENTION_RULES).afterMetadata(RetentionRule.COPY_RETENTION_RULES);

		transaction.in(Document.SCHEMA_TYPE).removeFromDisplay(
				Document.ACTUAL_DEPOSIT_DATE_ENTERED,
				Document.ACTUAL_DESTRUCTION_DATE_ENTERED,
				Document.ACTUAL_TRANSFER_DATE_ENTERED);

		transaction.in(Document.SCHEMA_TYPE).removeFromForm(
				Document.ACTUAL_DEPOSIT_DATE_ENTERED,
				Document.ACTUAL_DESTRUCTION_DATE_ENTERED,
				Document.ACTUAL_TRANSFER_DATE_ENTERED);

		transaction.in(Document.SCHEMA_TYPE).addToDisplay(
				Document.COPY_STATUS,
				Document.FOLDER_ARCHIVISTIC_STATUS,
				Document.FOLDER_CATEGORY,
				Document.FOLDER_RETENTION_RULE,
				Document.MAIN_COPY_RULE,
				Document.FOLDER_ACTUAL_TRANSFER_DATE,
				Document.FOLDER_EXPECTED_TRANSFER_DATE,
				Document.FOLDER_ACTUAL_DEPOSIT_DATE,
				Document.FOLDER_ACTUAL_DESTRUCTION_DATE,
				Document.FOLDER_EXPECTED_DEPOSIT_DATE,
				Document.FOLDER_EXPECTED_DESTRUCTION_DATE
		).beforeTheHugeCommentMetadata();

		schemaDisplayManager.execute(transaction.build());
	}

	private void updateDecommissioningPermissions(String collection, AppLayerFactory factory) {
		RolesManager roleManager = factory.getModelLayerFactory().getRolesManager();

		Role manager = roleManager.getRole(collection, RMRoles.MANAGER);
		List<String> permissions = new ArrayList<>(manager.getOperationPermissions());
		permissions.remove(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST);
		permissions.remove(RMPermissionsTo.EDIT_DECOMMISSIONING_LIST);

		roleManager.updateRole(manager.withPermissions(permissions));
	}

	public static class SchemaAlterationsFor5_1_9 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationsFor5_1_9(String collection, MigrationResourcesProvider provider,
											AppLayerFactory factory) {
			super(collection, provider, factory);

			configureTableMetadatas(collection, factory);
		}

		private void configureTableMetadatas(String collection, AppLayerFactory factory) {
			SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
			SchemasDisplayManager manager = factory.getMetadataSchemasDisplayManager();

			for (MetadataSchema metadataSchema : RMTypes.rmSchemas(factory, collection)) {
				if ("default".equals(metadataSchema.getLocalCode())) {
					SchemaDisplayConfig config = manager.getSchema(collection, metadataSchema.getCode());
					transaction.add(config.withTableMetadataCodes(config.getSearchResultsMetadataCodes()));
				} else {
					SchemaDisplayConfig customConfig = manager.getSchema(collection, metadataSchema.getCode());
					transaction.add(customConfig.withTableMetadataCodes(new ArrayList<String>()));
				}
			}
			manager.execute(transaction);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			createNewRetentionRulesMetadatas();

			setupCategoryMetadatasCalculators();

			setupFoldersMetadatasCalculators();
			setupDocumentsMetadatasCalculators();
			alterContainerSchema();

			createNewDecommissioningListMetadata();

			for (MetadataSchemaTypeBuilder typeBuilder : builder.getTypes()) {
				MetadataSchemaBuilder schemaBuilder = typeBuilder.getDefaultSchema();
				if (schemaBuilder.hasMetadata("description")) {
					schemaBuilder.get("description").setEnabled(true).setEssentialInSummary(true);
				}
			}

		}

		private void createNewDecommissioningListMetadata() {
			MetadataSchemaBuilder schema = types().getSchemaType(DecommissioningList.SCHEMA_TYPE)
					.getDefaultSchema();
			MetadataSchemaTypeBuilder document = types().getSchemaType(Document.SCHEMA_TYPE);

			schema.createUndeletable(DecommissioningList.DOCUMENTS_REPORT_CONTENT).setType(CONTENT);
			schema.createUndeletable(DecommissioningList.FOLDERS_REPORT_CONTENT).setType(CONTENT);
			schema.createUndeletable("documents").defineReferencesTo(document).setMultivalue(true);

			schema.get(DecommissioningList.UNIFORM_CATEGORY).defineDataEntry().asCalculated(
					DecomListUniformCategoryCalculator2.class);

			schema.get(DecommissioningList.UNIFORM_COPY_RULE).defineDataEntry().asCalculated(
					DecomListUniformCopyRuleCalculator2.class);

			schema.get(DecommissioningList.UNIFORM_RULE).defineDataEntry().asCalculated(
					DecomListUniformRuleCalculator2.class);

			schema.get(DecommissioningList.UNIFORM_COPY_TYPE).defineDataEntry().asCalculated(
					DecomListUniformCopyTypeCalculator2.class);

			schema.get(DecommissioningList.TYPE).setDefaultRequirement(true);
		}

		private void setupCategoryMetadatasCalculators() {
			MetadataSchemaBuilder defaultSchema = types().getSchemaType(Category.SCHEMA_TYPE)
					.getDefaultSchema();
			defaultSchema.create(Category.LEVEL).setType(MetadataValueType.NUMBER)
					.defineDataEntry().asCalculated(CategoryLevelCalculator.class);
			defaultSchema.create(Category.COPY_RETENTION_RULES_ON_DOCUMENT_TYPES)
					.defineStructureFactory(CopyRetentionRuleInRuleFactory.class).setMultivalue(true)
					.defineDataEntry().asCalculated(CategoryCopyRetentionRulesOnDocumentTypesCalculator.class);
		}

		private void setupFoldersMetadatasCalculators() {
			MetadataSchemaBuilder defaultSchema = types().getSchemaType(Folder.SCHEMA_TYPE)
					.getDefaultSchema();
			defaultSchema.get(Folder.COPY_STATUS)
					.defineDataEntry().asCalculated(FolderCopyStatusCalculator3.class);
		}

		private void setupDocumentsMetadatasCalculators() {
			MetadataSchemaTypeBuilder retentionRuleSchemaType = types().getSchemaType(RetentionRule.SCHEMA_TYPE);
			MetadataSchemaTypeBuilder folderSchemaType = types().getSchemaType(Folder.SCHEMA_TYPE);
			MetadataBuilder folderRetentionRule = folderSchemaType.getDefaultSchema().getMetadata(Folder.RETENTION_RULE);
			MetadataBuilder folderCopyStatus = folderSchemaType.getDefaultSchema().getMetadata(Folder.COPY_STATUS);
			MetadataSchemaBuilder defaultSchema = types().getSchemaType(Document.SCHEMA_TYPE)
					.getDefaultSchema();
			defaultSchema.create(Document.INHERITED_FOLDER_RETENTION_RULE)
					.defineReferencesTo(retentionRuleSchemaType)
					.defineDataEntry().asCopied(defaultSchema.get(Document.FOLDER), folderRetentionRule);
			defaultSchema.create(Document.COPY_STATUS).defineAsEnum(CopyType.class)
					.defineDataEntry().asCopied(defaultSchema.get(Document.FOLDER), folderCopyStatus);
			defaultSchema.getMetadata(Document.FOLDER_RETENTION_RULE)
					.defineDataEntry().asCalculated(DocumentRetentionRuleCalculator.class);
			defaultSchema.getMetadata(Document.FOLDER_ARCHIVISTIC_STATUS)
					.defineDataEntry().asCalculated(DocumentArchivisticStatusCalculator.class);
			defaultSchema.getMetadata(Document.FOLDER_ACTUAL_DEPOSIT_DATE)
					.defineDataEntry().asCalculated(DocumentActualDepositDateCalculator.class);
			defaultSchema.getMetadata(Document.FOLDER_ACTUAL_DESTRUCTION_DATE)
					.defineDataEntry().asCalculated(DocumentActualDestructionDateCalculator.class);
			defaultSchema.getMetadata(Document.FOLDER_ACTUAL_TRANSFER_DATE)
					.defineDataEntry().asCalculated(DocumentActualTransferDateCalculator.class);
			defaultSchema.getMetadata(Document.FOLDER_EXPECTED_DEPOSIT_DATE)
					.defineDataEntry().asCalculated(DocumentExpectedDepositDateCalculator.class);
			defaultSchema.getMetadata(Document.FOLDER_EXPECTED_DESTRUCTION_DATE)
					.defineDataEntry().asCalculated(DocumentExpectedDestructionDateCalculator.class);
			defaultSchema.getMetadata(Document.FOLDER_EXPECTED_TRANSFER_DATE)
					.defineDataEntry().asCalculated(DocumentExpectedTransferDateCalculator.class);
			defaultSchema.createUndeletable(Document.APPLICABLE_COPY_RULES)
					.defineStructureFactory(CopyRetentionRuleInRuleFactory.class).setMultivalue(true)
					.defineDataEntry().asCalculated(DocumentApplicableCopyRulesCalculator.class);
			defaultSchema.createUndeletable(Document.SAME_SEMI_ACTIVE_FATE_AS_FOLDER).setType(BOOLEAN)
					.defineDataEntry().asCalculated(DocumentIsSameSemiActiveFateAsFolderCalculator.class);
			defaultSchema.createUndeletable(Document.SAME_INACTIVE_FATE_AS_FOLDER).setType(BOOLEAN)
					.defineDataEntry().asCalculated(DocumentIsSameInactiveFateAsFolderCalculator.class);
			defaultSchema.createUndeletable(Document.MAIN_COPY_RULE)
					.defineStructureFactory(CopyRetentionRuleFactory.class)
					.defineDataEntry().asCalculated(DocumentMainCopyRuleCalculator.class);
			defaultSchema.createUndeletable(Document.ACTUAL_DEPOSIT_DATE_ENTERED).setType(MetadataValueType.DATE);
			defaultSchema.createUndeletable(Document.ACTUAL_DESTRUCTION_DATE_ENTERED).setType(MetadataValueType.DATE);
			defaultSchema.createUndeletable(Document.ACTUAL_TRANSFER_DATE_ENTERED).setType(MetadataValueType.DATE);

		}

		private void createNewRetentionRulesMetadatas() {
			MetadataSchemaBuilder defaultSchema = types().getSchemaType(RetentionRule.SCHEMA_TYPE)
					.getDefaultSchema();
			defaultSchema.createUndeletable(RetentionRule.DOCUMENT_COPY_RETENTION_RULES)
					.setMultivalue(true)
					.setType(STRUCTURE)
					.defineStructureFactory(CopyRetentionRuleFactory.class);
			defaultSchema.createUndeletable(RetentionRule.PRINCIPAL_DEFAULT_DOCUMENT_COPY_RETENTION_RULE)
					.setType(STRUCTURE)
					.defineStructureFactory(CopyRetentionRuleFactory.class);
			defaultSchema.createUndeletable(RetentionRule.SECONDARY_DEFAULT_DOCUMENT_COPY_RETENTION_RULE)
					.setType(STRUCTURE)
					.defineStructureFactory(CopyRetentionRuleFactory.class);
			defaultSchema.createUndeletable(RetentionRule.SCOPE)
					.defineAsEnum(RetentionRuleScope.class);
			defaultSchema.get(RetentionRule.COPY_RETENTION_RULES).setDefaultRequirement(false);
		}

		private void alterContainerSchema() {
			MetadataSchemaBuilder defaultSchema = types().getSchemaType(ContainerRecord.SCHEMA_TYPE).getDefaultSchema();
			defaultSchema.getMetadata(ContainerRecord.POSITION).setEssential(false);
		}
	}
}
